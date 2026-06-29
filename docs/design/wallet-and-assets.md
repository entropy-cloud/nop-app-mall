# 钱包与资产业务设计

## 目的

说明 `nop-app-mall` 中商城用户的钱包余额、充值交易和资产流水的业务设计。

## 边界

- 本文档负责用户钱包账户、余额变动、充值流程和资产记录的语义。
- 持久化模型结构、字段集和字典定义以 `model/*.orm.xml` 为准。
- 支付集成实现属于 `docs/architecture/`。

## 领域概览

钱包与资产领域包含以下概念：

- Wallet：每个商城用户的钱包账户，记录当前可用余额。
- Recharge：用户通过支付渠道向钱包充值的交易记录。
- Wallet Flow：每笔余额变动的流水记录，包含来源和去向。
- Points：用户的积分账户和流水，积分作为一种用户资产在 `marketing-and-promotions.md` 定义的规则中产生和消耗。

## 钱包账户

### 业务规则

- 每个商城用户有且只有一个钱包账户。
- 钱包账户记录当前可用余额、累计充值总额和累计消费总额。
- 钱包账户采用懒创建（lazy-create）：首次被访问（`getMyWallet` 查询或 `creditBalance` 入账）时按需创建，而非在用户注册时预创建。功能等价——0 余额账户在被访问前对用户不可见（`getMyWallet` 对未创建用户返回内存空壳 balance=0，不落库）。与积分账户的懒创建策略保持一致。
- 钱包余额不可为负值。

### 并发安全与原子操作

- 钱包余额的入账（credit）与扣减（debit）均为**单事务内原子操作**：以 `version` 乐观锁执行 `update wallet set balance=balance±N where id=? and version=?`，version 不匹配即抛 `nop.err.mall.wallet.version-conflict`。
- `debit` 前校验 `balance - N >= 0`（余额不可为负），余额不足抛 `nop.err.mall.wallet.insufficient`。
- 每次变动同时写入一条流水记录（含 `balanceAfter` 变动后余额快照），使账户表与流水表可在任意时刻对账。
- 高并发单账户需重试；本基线非秒杀场景，乐观锁失败即返回错误由调用方重试。

### 暴露模型与资金安全

- `creditBalance` / `debitBalance` 为 `@BizAction` 内部方法，**不暴露 GraphQL**，仅由受信 BizModel 通过 `ILitemallWalletBiz` 注入调用（如充值入账、P30 余额支付接线）。理由：钱包涉及资金，公开 `@BizMutation(userId, amount)` 会让任意登录用户可对任意 userId 加/扣余额（资金盗刷风险）。
- `adminAdjust` 为 `@BizMutation @Auth(roles="admin")`，后台调账专用。

### 钱包流水与 changeType × sourceType 分类法

- 每笔钱包余额的增加或减少均产生一条流水记录（`LitemallWalletFlow`）。
- 流水字段：钱包ID（`walletId`）、变动类型（`changeType`，字典 `mall/wallet-change-type`）、变动金额（`changeAmount`）、变动后余额（`balanceAfter`）、来源类型（`sourceType`，VARCHAR(50)）、来源业务 ID（`sourceId`）、备注（`remark`）、变动时间（`addTime`）。
- **字典复用约定（不改模型）：** `mall/wallet-change-type` 字典有四值 `RECHARGE`(0, 充值) / `PAY`(10, 支付) / `REFUND`(20, 退款) / `WITHDRAW`(30, 提现)。`changeType` 严格映射这四值。**所有细分来源/去向落入 `sourceType` 字段**：
  - 钱包充值入账 = `RECHARGE` + `sourceType=recharge`
  - 后台调账（加） = `RECHARGE` + `sourceType=admin-adjust`
  - 后台调账（扣） = `PAY` + `sourceType=admin-adjust`
  - 余额支付扣款 = `PAY` + `sourceType=pay`（P30 接线）
  - 退款返还 = `REFUND` + `sourceType=recharge-refund`（充值退款异步对账已落地，见下「充值退款对账」）
  - 提现 = `WITHDRAW` + `sourceType=withdraw`（未实现，预留语义）
- `sourceId` 记录来源业务 ID（rechargeId 等），支撑幂等查重（如同一 rechargeId 不重复入账）。

### 支持行为

- 用户可查看钱包余额和收支汇总（累计充值/累计消费）。
- 用户可查看钱包流水，支持按时间和类型（changeType / sourceType）筛选。

## 充值

### 业务意图

- 充值允许用户通过外部支付渠道向钱包余额存入资金。

### 充值流程

- 用户选择充值金额（支持固定套餐或自定义金额），选择支付渠道后发起支付。
- 支付成功后，钱包余额增加对应金额。
- 充值套餐支持赠送金额配置（如充 100 送 10）。

### 实现说明

- **充值套餐存储：** 套餐配置存入现有 `LitemallSystem` 表（keyName=`recharge_packages`，value 为 JSON 数组 `[{id, label, amount, giftAmount}]`），经 `ILitemallSystemBiz.getConfig` 读取、经现有 `LitemallSystem` 后台 CRUD 编辑。不新增 ORM 实体（ORM 变更为 Protected Area）。无配置时返回默认套餐 `[{amount:100, giftAmount:10}]` 兜底。
- **outTradeNo 派生（非存储）：** `LitemallRecharge` 无 `outTradeNo` 列。outTradeNo = `"RC" + String.format("%08d", rechargeId)`（如 `RC00000001`），满足微信支付 6–32 字符要求。去掉 `RC` 前缀 parse 即得 rechargeId。
- **回调路由：** `PaymentCallbackImpl.onPaymentSuccess` 按 outTradeNo 前缀分流——`RC` 开头 → `rechargeBiz.confirmRechargeByNotify`；否则 → `orderBiz.confirmPaidByNotify`（现有行为不变）。**不变量：** 订单 outTradeNo = `generateOrderSn()` = 32 位小写 hex UUID，永不以大写 `RC` 开头；若未来 `generateOrderSn()` 引入字母前缀，必须重新评估 RC 路由分流。
- **confirmRechargeByNotify 暴露模型（资金安全）：** `@BizAction` 内部方法（不暴露 GraphQL），仅由 `PaymentCallbackImpl` 经 `ILitemallRechargeBiz` 注入调用。若为公开 `@BizMutation`，攻击者可先 `createRecharge(amount=1000)` 得 rechargeId，再按 `RC`+填充格式算出 outTradeNo 直接调确认接口给钱包充值（资金盗刷）。`confirmRecharge`（demo 手动确认）有 `!payService.isEnabled()` 守卫故风险低，保持 `@BizMutation`。
- **充值入账：** `creditRecharge` 将充值金额 + 赠送金额一次性调 `creditBalance` 入账（`changeType=RECHARGE`，`sourceType=recharge`，`sourceId=rechargeId` 幂等），并回填 `walletId`、更新 `payStatus=PAID`。

### 充值退款对账（P29 deferred successor）

- **触发场景：** 微信/支付宝商户后台对一笔充值支付主动发起退款（重复扣款客诉、运营手工退款等）。渠道侧退款异步通知到达 `PaymentCallbackImpl.onRefundSuccess`，按 outTradeNo 前缀 `RC` 路由到 `LitemallRechargeBizModel.refundRechargeByNotify`（与正向 `confirmRechargeByNotify` 对称的受信 `@BizAction` 内部入口，不暴露 GraphQL）。
- **回冲语义：** `payStatus==PAID` 的充值被回冲——调 `walletBiz.debitBalance(userId, amount+giftAmount, changeType=REFUND, sourceType=recharge-refund, sourceId=rechargeId)` 原子扣减（乐观锁），写 `WalletFlow(REFUND, recharge-refund)` 含 `balanceAfter` 快照，并将 `payStatus` 推进至 `REFUNDED`。`sourceType=recharge-refund` 而非 owner doc 此前预留的宽泛 `refund`，以明确归属充值退款对账、与正向 `sourceType=recharge` 对称可追溯（未来若出现「订单退款返还到钱包」场景可再用独立 sourceType 区分）。
- **幂等（渠道重放安全）：**
  - `payStatus==REFUNDED` → 已回冲，no-op（重放通知安全）。
  - `payStatus==UNPAID` → 从未入账，无需回冲，no-op。
  - `payStatus==PAID` 的并发退款通知（渠道重放/并发）→ `debitBalance` 乐观锁保证不双扣（仅一个 `updateBalanceIfVersion` 命中），败者 catch `ERR_WALLET_VERSION_CONFLICT` 后记 WARN 并视为幂等 return（胜者已推进 REFUNDED，下一次重试命中 REFUNDED 守卫）。
- **降级（余额不足）：** 用户在退款通知到达前已花完充值余额时，`debitBalance` 抛 `ERR_WALLET_INSUFFICIENT`。`refundRechargeByNotify` catch + 记 WARN 人工对账，**不推进 REFUNDED**、**不允许负余额**（保留 `debitBalance` 资金安全不变量）。运营经 `adminAdjust` 负向调账 + 标记兜底。残留风险（充值→快速消费→渠道退款三连）为低频边缘，记为 watch-only successor。
- **与正向入账对称：** `confirmRechargeByNotify`（UNPAID→PAID，credit amount+gift，RECHARGE/recharge）↔ `refundRechargeByNotify`（PAID→REFUNDED，debit amount+gift，REFUND/recharge-refund），同为 `@BizAction` 受信内部入口、同按 outTradeNo `RC` 前缀路由、同幂等（状态守卫 + sourceId 源级去重 + 乐观锁）。

### 业务规则

- 充值订单走外部支付渠道（微信/支付宝），支付确认后余额到账。
- 充值赠送的金额作为系统赠送余额入账，与用户充值金额在语义上不做严格区分。
- 充值记录对用户可见，包含充值金额、赠送金额、支付渠道和充值时间。

### 管理员动作

- 管理员可查看用户的充值记录。
- 管理员可配置充值套餐（固定金额与赠送金额的组合）。

### 前端页面（P29 落地）

- **钱包页（`LitemallWallet.view.xml`）：** 后台视角的钱包账户列表（balance/totalRecharge/totalSpent 收支汇总）+ 「钱包调账」动作（`adminAdjust`，admin 鉴权）。bounded-merge 生成桩。
- **充值记录页（`LitemallRecharge.view.xml`）：** 充值记录列表（userId/amount/giftAmount/payChannel/payStatus/addTime + userId/payStatus 筛选）+ 只读查看 + 「发起充值」入口（`createRecharge`）。套餐配置经现有 `LitemallSystem` 后台 CRUD 编辑（keyName=`recharge_packages` 的 JSON value），不新增独立富配置页。
- **钱包流水页（`LitemallWalletFlow.view.xml`）：** 流水列表（changeType/changeAmount/balanceAfter 快照/sourceType/sourceId/remark/addTime + changeType/sourceType 筛选）。bounded-merge 生成桩。

## 余额支付

- 余额作为支付通道之一在收银台展示（详见 `order-and-cart.md`「多支付通道」）。
- 余额支付需要有支付确认环节（P30 Decision B：复用用户登录密码，经平台 `IPasswordEncoder` 校验；不新建独立支付密码账户实体，不引入 SMS 通道基建）。
- 余额支付成功后，钱包余额扣减对应金额，并产生余额扣减流水（`changeType=PAY(10)` + `sourceType=pay` + `sourceId=orderSn`）。
- **扣款接线（P30 已落地）：** 收银台 `payByBalance(orderId, confirmCredential)`（`@BizMutation`）调用 `ILitemallWalletBiz.debitBalance(userId, actualPrice, WALLET_CHANGE_TYPE_PAY, SOURCE_TYPE_PAY, orderSn, ...)` 原子扣款（乐观锁），写 `order.walletPayAmount` + `order.payChannel=20(BALANCE)` 后推进订单已支付。关闭 P29 deferred「余额支付扣款 = PAY + sourceType=pay」。余额通道仅服务非零金额订单（零金额订单走收银台零金额分支直接 `pay()`，不扣余额）。
- **双层幂等：** 订单状态守卫（重复调用见 status≠101 拒绝 `ERR_ORDER_NOT_ALLOW_PAY`）+ `debitBalance` 乐观锁（并发扣款版本冲突 `ERR_WALLET_VERSION_CONFLICT`）。

## 积分账户

### 业务意图

- 积分账户记录用户的积分余额和变动历史，支撑积分获取和消耗的核算。
- 每个商城用户有且只有一个积分账户，记录当前可用积分（`balance`）、累计获得（`totalEarned`）、累计消耗（`totalSpent`）。
- 账户在用户首次获得积分时自动创建（首次 `earnPoints` 调用），无需注册时预创建。
- 持久化字段、字典、唯一键（`uk_litemall_points_account_user` on `userId`）以 `model/app-mall.orm.xml` 为准。

### 并发安全与原子操作

- 积分账户的获取（earn）与消耗（spend）均为**单事务内原子操作**：以 `version` 乐观锁执行 `update account set balance=balance±N where id=? and version=?`，version 不匹配即抛出 ErrorCode 失败。
- `spend` 前校验 `balance - N >= 0`（余额不可为负），余额不足抛 `nop.err.mall.points.insufficient`。
- 每次变动同时写入一条流水记录（含 `balanceAfter` 变动后余额快照），使账户表与流水表可在任意时刻对账。
- 高并发单账户需重试；本基线非秒杀场景，乐观锁失败即返回错误由调用方重试。

### 积分流水与 changeType × sourceType 分类法

- 每笔积分的增加或减少均产生一条流水记录（`LitemallPointsFlow`）。
- 流水字段：变动时间（`addTime`）、变动类型（`changeType`，字典 `mall/points-change-type`）、变动数量（`changeAmount`）、变动后余额（`balanceAfter`）、来源类型（`sourceType`，VARCHAR(50)）、来源业务 ID（`sourceId`）、备注（`remark`）。
- **字典复用约定（不改模型）：** `mall/points-change-type` 字典仅有三值 `EARN`(0, 获取)/`SPEND`(10, 消耗)/`EXPIRE`(20, 过期)。`changeType` 严格映射这三值——获取类全用 `EARN`、消耗类全用 `SPEND`、过期用 `EXPIRE`。**所有细分来源/去向落入 `sourceType` 字段**：
  - 购物赠送 = `EARN` + `sourceType=order-confirm-earn`
  - 取消/整单退款返还抵扣积分 = `EARN` + `sourceType=refund-return`
  - 结算积分抵扣 = `SPEND` + `sourceType=order-deduct`
  - 后台手工调账（加） = `EARN` + `sourceType=admin-adjust`
  - 后台手工调账（扣） = `SPEND` + `sourceType=admin-adjust`
  - 签到（P28） = `EARN` + `sourceType=check-in`
  - 评价（P33） = `EARN` + `sourceType=comment-reward`
- `sourceId` 记录来源业务 ID（orderId / commentId / checkInRecordId 等），支撑幂等查重（如同一 orderId 不重复赠送）。

### 业务规则

- 积分余额不可为负值。
- 用户可查看积分余额和流水记录，支持按时间范围和类型筛选。
- **积分有效期：** 业务规则要求积分有有效期，过期积分自动扣减并产生 `EXPIRE` 流水。**已实现（successor 交付）**——
  - **有效期批次账本：** 每笔 earn（含正向调账）生成一条 `LitemallPointsExpireBatch`（`totalPoints`/`remainingPoints`/`expireTime`，唯一键 `(sourceType,sourceId)` 与 earn 流水同源兜底幂等）。有效期时长由 `mall_points_validity_days` 配置（缺失默认 **730 天**）。
  - **FIFO 消耗：** spend 与负向调账按 `expireTime ASC`（最早过期先消耗）扣减各批次 `remainingPoints`。
  - **存量不过期：** 特性上线前已存在的 balance（无批次）视为不过期存量，自然消耗（spend 先耗批次，余量从 balance 扣），不回填为过期批次（无可靠 earn 时间戳）。
  - **不变量：** `account.balance` 为可用积分真相源；`balance >= SUM(batch.remainingPoints WHERE userId AND remainingPoints>0)` 恒成立（差额即存量）。earn/spend/adjust/expire 四路径在同一事务内同步两侧。
  - **自动过期编排：** nop-job-local 定时任务 `expire-points`（每小时）扫描到期批次，经 `PointsAccount.version` 乐观锁 CAS 扣减 `balance` 并写 `EXPIRE(20)` 流水（`sourceType=expire`, `sourceId=batchId`），单轮限 500 批、并发败者顺延下轮，幂等可重放安全。
  - **过期预警推送（successor of 前端提示）：** nop-job-local 定时任务 `send-points-expiry-reminders`（每日 86400000ms）扫描 `expireTime ∈ [now, now+remindDays]` 且 `remainingPoints>0` 的批次，按 userId 聚合（Σ points + 最早 expireTime）后推送一条 `SYSTEM` 站内信「积分即将过期」（`MallNotificationService.sendUserMessage`）。提前天数由 `mall_points_expiry_remind_days` 配置（缺失默认 **3 天**）。事件开关 `mall_message_event_enabled_points-expiry-remind` 关闭时整体跳过；幂等：当日同 userId+msgType(SYSTEM)+title 已存在则跳过（job 重跑/时区抖动不重复推送）。从「拉取式 hint」升级为「主动推送 reminder」。
  - **前端提示：** 「我的积分」页消费 `getMyPointsExpiryHint`，展示最近一笔未到期批次的「即将过期」提示（仅存量时不展示）。账户/流水/抵扣/获取基座不依赖有效期语义。

### 获取规则（积分来源）

积分获取规则配置详见 `marketing-and-promotions.md`「积分体系」章节；本文件仅持有账户/流水语义。主要触发源：

- **购物赠送**（主触发源，已落地）：订单确认收货（confirm）时按 `mall_points_earn_per_yuan` × `actualPrice` 赠送；幂等（同 orderId 不重复赠送，按 `sourceType=order-confirm-earn` + `sourceId=orderId` 查重）。
- 签到（P28，已落地）：每日签到按规则档位发积分，调用 earn API（`sourceType=check-in`, `sourceId=CheckInRecord.id`），业务设计见 `marketing-and-promotions.md`「签到 / Daily Check-In」。评价（P33）、分享（不在基线）作为后续接入源复用同一 earn API。

### 抵扣与返还语义

- 抵扣在结算页的勾选行为、积分兑换比例与抵扣上限由 `order-and-cart.md` 价格语义负责引用结果。
- 取消/整单退款的积分返还语义由 `order-and-cart.md` 退款流程负责引用结果（与券恢复对称：仅订单级返还，item 级部分退款不返还）。

## 与其他 Owner Docs 的关系

- 积分获取规则和积分商城兑换由 `marketing-and-promotions.md` 负责。
- 积分在结算页的抵扣行为由 `order-and-cart.md` 负责。
- 用户个人资料页面由 `user-and-address.md` 负责，本文件补充钱包余额和积分展示的具体资产语义。
