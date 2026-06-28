# phase29 钱包余额与充值

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 29；`docs/design/wallet-and-assets.md`
> Related: `docs/plans/2026-06-27-2029-1-phase27-points-system-plan.md`（积分账户同为 account+flow 模式，本计划复用其模式）
> Audit: required

## Current Baseline

**ORM 模型已就绪（`model/app-mall.orm.xml`）：**
- `LitemallWallet`（钱包账户表）：`id` / `userId` / `balance`(DECIMAL) / `totalRecharge`(DECIMAL) / `totalSpent`(DECIMAL) / `version`(乐观锁) / `addTime` / `updateTime`；唯一键 `uk_litemall_wallet_user` on `userId`。`versionProp="version"` 已配置。
- `LitemallWalletFlow`（钱包流水表）：`id` / `walletId` / `changeType`(dict `mall/wallet-change-type`) / `changeAmount`(DECIMAL) / `balanceAfter`(DECIMAL) / `sourceType`(VARCHAR 50) / `sourceId`(VARCHAR 50) / `remark` / `addTime` / `updateTime`；`to-one wallet` 关系已建。
- `LitemallRecharge`（充值记录表）：`id` / `userId` / `walletId` / `amount`(DECIMAL) / `giftAmount`(DECIMAL) / `payChannel`(dict `mall/pay-channel`) / `payStatus`(dict `mall/pay-status`) / `addTime` / `updateTime` / `deleted`(逻辑删除)；`to-one wallet` 关系已建。

**字典已就绪：**
- `mall/wallet-change-type`：`RECHARGE`(0,充值) / `PAY`(10,支付) / `REFUND`(20,退款) / `WITHDRAW`(30,提现)
- `mall/pay-channel`：`WECHAT`(0) / `ALIPAY`(10) / `BALANCE`(20)
- `mall/pay-status`：`UNPAID`(0) / `PAID`(10) / `FAILED`(20) / `REFUNDED`(30)

**生成脚手架已就绪（空 CrudBizModel 桩）：**
- `LitemallWalletBizModel`、`LitemallWalletFlowBizModel`、`LitemallRechargeBizModel` — 均为空 `extends CrudBizModel<T>` 桩，无任何业务方法。
- 对应 entity / mapper(生成) / api / beans / xbiz / xmeta / view.xml(生成桩) 均存在。

**参照模式（`LitemallPointsAccountBizModel`，P27 已落地）：**
- account + flow 双表原子操作：`flushSession` + `clearEntitySessionCache` + 自定义 Mapper `updateBalanceIfVersion` 条件 UPDATE（version 不匹配返回 0 → 抛乐观锁冲突异常）。
- `changeType` × `sourceType` 分类法：changeType 严格映射字典三值，细分来源落入 sourceType。
- `LitemallPointsAccountMapper`（`@SqlLibMapper` + EQL `updateBalanceIfVersion`）为本计划 Wallet Mapper 的直接参照。

**支付基础设施已就绪：**
- `PayService` 接口（`createPayment` / `queryPayment` / `refund` / `isEnabled`）：`WxPayServiceImpl`（真实微信支付，app-mall-wx）、`MockPayServiceImpl`（demo 模式）。
- `IPaymentCallback` 桥接接口 + `PaymentCallbackImpl`（`@Named("paymentCallback")`）：将微信异步通知路由到 `orderBiz.confirmPaidByNotify(outTradeNo, transactionId)`，在 system context 下执行，幂等。
- 订单 prepay → 微信扫码 → 异步通知 → confirmPaidByNotify 流程已验证可用。

**缺口：**
- `LitemallWalletMapper`（自定义 Mapper）**不存在**——需新建（参照 `LitemallPointsAccountMapper` + sql-lib EQL）。
- `LitemallWallet.sql-lib.xml` **不存在**——需新建。
- 钱包账户无生命周期管理（无自动创建、无余额查询 API）。
- 无原子余额操作（充值入账 / 余额扣减 / 后台调账）。
- 无钱包流水写入与查询。
- 无充值流程（充值套餐配置、充值下单、支付集成、回调入账）。
- `PaymentCallbackImpl` 仅路由订单回调，不支持充值回调。
- 无前端钱包 / 充值 / 流水页面，无后台充值记录 / 套餐配置页面。
- **充值套餐无持久化实体**：ORM 无 `RechargePackage` / `RechargePlan` 表（model-gap，见 Decision）。

## Goals

- 用户钱包账户生命周期（懒创建）+ 原子余额操作（充值入账 / 余额扣减内部 API / 后台调账），乐观锁并发安全。
- 钱包流水全量记录（`balanceAfter` 快照 + `changeType` × `sourceType` 分类法），账户与流水任意时刻可对账。
- 充值套餐配置（管理员）+ 充值流程（用户选套餐/自定义金额 → 创建充值记录 → 微信支付 → 异步回调入账），复用现有 `PayService` + `IPaymentCallback` 基础设施。
- 前台：钱包页（余额 + 收支汇总）、充值页（套餐选择 + 自定义金额 + 支付）、钱包流水页（时间/类型筛选）。
- 后台：充值记录列表、充值套餐配置（经现有 `LitemallSystem` 后台 CRUD 编辑）。
- 所有新增 `@BizMutation` / `@BizQuery` 方法通过 `IGraphQLEngine` 测试。

## Non-Goals

- **余额支付（收银台通道）**：余额作为收银台支付通道的展示、支付密码 / 短信确认、订单扣款接线——属 P30（Protected Area ask-first）。本计划实现钱包的原子 `debit` 内部 API（P30 届时接线调用），但不实现收银台余额支付入口与确认流程。
- **提现**：字典 `WITHDRAW`(30) 预留但设计文档无提现流程，不实现。
- **支付宝充值**：P30 范围，本计划充值仅走微信支付通道（`payChannel=WECHAT`）。
- **充值套餐持久化实体**：不新增 ORM 实体（ORM 变更为 Protected Area），套餐配置存入现有 `LitemallSystem` 表（keyName=`recharge_packages`，value=JSON 数组），复用 `ILitemallSystemBiz.getConfig` 读取 + 现有后台 CRUD 编辑（见 Phase 2 Decision）。
- **充值退款流程**：字典 `mall/pay-status REFUNDED`(30) 与 `mall/wallet-change-type REFUND`(20) 预留语义，但 `PaymentCallbackImpl` 当前仅桥接 `onPaymentSuccess`（无退款异步通知），充值退款不在本计划范围。退款场景发生时由后台调账（`adminAdjust`）人工兜底。
- **钱包账户注册时预创建**：`wallet-and-assets.md` 原文"钱包账户在用户注册时自动创建"，本计划改用懒创建（首次访问时创建），功能等价（0 余额账户在被访问前对用户不可见）。**owner-doc 文本漂移为 non-degradable，Phase 1 强制 reconcile 该行（见 Exit Criteria）。**

## Task Route

- Type: `implementation-only change`
- Owner Docs: `docs/design/wallet-and-assets.md`（钱包账户/充值/流水语义）
- Skill Selection Basis: 后端 BizModel 方法开发→`nop-backend-dev`；BizModel API 测试→`nop-testing`；AMIS 前台/后台页面→`nop-frontend-dev`

## Infrastructure And Config Prereqs

- 复用现有微信支付配置（`wxpay.enabled` / mchId / cert 等，Phase 5b 已落地）。充值走同一 `PayService.createPayment`，无需新增支付配置。
- 充值套餐配置存入现有 `LitemallSystem` 表（keyName=`recharge_packages`，value 为 JSON 数组），通过 `ILitemallSystemBiz.getConfig` 读取、通过现有 `LitemallSystem` 后台 CRUD 编辑，无需新增基础设施或 ORM 实体。
- 充值回调复用现有 `WxPayNotifyResource` → `PaymentCallbackImpl` 路由，无需新增回调端点。
- No infra prereqs beyond existing baseline.

## Execution Plan

### Phase 1 - 钱包账户核心（原子操作 + 流水）

Status: completed
Targets: `app-mall-service/src/main/java/app/mall/service/entity/LitemallWalletBizModel.java`、`app-mall-service/src/main/java/app/mall/service/entity/LitemallWalletFlowBizModel.java`、`app-mall-dao/src/main/java/app/mall/dao/mapper/LitemallWalletMapper.java`(新建)、`app-mall-dao/src/main/resources/_vfs/app/mall/sql/LitemallWallet.sql-lib.xml`(新建)、`app-mall-service/src/main/java/app/mall/service/AppMallErrors.java`、`app-mall-dao/src/main/java/app/mall/biz/ILitemallWalletBiz.java`
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision`
- Prereqs: 无（本 Phase 为钱包基座，不依赖充值流程）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完各自 routing table 标为必读的全部文档，列出已读路径。每写完一个方法用 selfcheck 校验无 anti-pattern。
  - Docs read: nop-backend-dev skill（05-examples/ibiz-and-bizmodel.java, 02-core-guides/service-layer.md, 02-core-guides/error-handling.md, 04-reference/safe-api-reference.md, 03-runbooks/write-bizmodel-method.md, 04-reference/bizmodel-method-selfcheck.md）；nop-testing skill（05-examples/test-examples.java, 02-core-guides/testing.md）；参照实体 `LitemallPointsAccountBizModel`/`LitemallPointsAccountMapper`/`LitemallPointsAccount.sql-lib.xml`/`LitemallCouponUserBizModel.claimCouponForUser`（@BizAction 模式）/`PaymentCallbackImpl`/`LitemallOrderBizModel.prepay+confirmPaidByNotify`
- [x] **Add（LitemallWalletMapper + sql-lib）:** 新建 `LitemallWalletMapper`（`@SqlLibMapper("/app/mall/sql/LitemallWallet.sql-lib.xml")`），方法 `updateBalanceIfVersion(id, newBalance, newTotalRecharge, newTotalSpent, currentVersion)` 返回 affected rows。sql-lib EQL 参照 `LitemallPointsAccount.sql-lib.xml` 的 `updateBalanceIfVersion`（条件 `WHERE id=? AND version=?`）。Decimal 类型参数。
- [x] **Add（ErrorCodes）:** 在 `AppMallErrors.java` 新增：`ERR_WALLET_NOT_FOUND`、`ERR_WALLET_INSUFFICIENT`（余额不足）、`ERR_WALLET_VERSION_CONFLICT`（乐观锁冲突）。均 extend `NopException` + ErrorCode。
- [x] **Add（WalletBizModel 原子操作）:**
  - `@BizQuery getMyWallet(context)` — 查询当前用户钱包（含 balance/totalRecharge/totalSpent），未创建返回空壳（balance=0）。仅用 context 取 userId，不接受 userId 参数（防越权）。
  - `findOrCreateWallet(userId, context)` — 包级内部方法，按 userId 查找或创建（balance=0, totalRecharge=0, totalSpent=0, version=0），唯一键 `uk_litemall_wallet_user` 保证不重复。
  - `@BizAction creditBalance(userId, amount, changeType, sourceType, sourceId, remark, context)` — **内部方法（不暴露 GraphQL）**：乐观锁 UPDATE（flushSession + clearEntitySessionCache + mapper.updateBalanceIfVersion），成功后写 WalletFlow（balanceAfter 快照），更新 totalRecharge（仅 sourceType=recharge 时累加）。参照 `LitemallPointsAccountBizModel.mutateBalance`。由 RechargeBizModel 注入 `ILitemallWalletBiz` 调用。
  - `@BizAction debitBalance(userId, amount, changeType, sourceType, sourceId, remark, context)` — **内部方法（不暴露 GraphQL）**：校验 balance - amount >= 0（不足抛 ERR_WALLET_INSUFFICIENT），乐观锁 UPDATE，写 WalletFlow。为 P30 余额支付的内部 API，本计划不接线收银台。
  - `@BizMutation @Auth(roles="admin") adminAdjust(@Name userId, @Name amount, @Name remark, context)` — 后台调账（admin 鉴权）：amount > 0 入账 / amount < 0 扣款，sourceType=`admin-adjust`，sourceId=`adjust-`+timestamp。测试经 IGraphQLEngine。
- [x] **Add（WalletFlowBizModel）:** 保持 CrudBizModel 基础，补充按 walletId + 时间范围 + changeType 的查询能力（通过 QueryBean filter，无需自定义 SQL）。
- [x] **Decision（钱包懒创建 vs 注册时预创建）:** 抉择 A（懒创建，首次 `getMyWallet` / `creditBalance` 时创建）。备选 B（注册流程 hook 预创建）。理由：懒创建与 PointsAccount 一致、避免耦合注册流程、0 余额账户在被访问前对用户不可见（功能等价）。残留风险：若将来需要"注册即有可见钱包入口"，懒创建首次访问有一次性创建延迟（可忽略）。抉择写入 `wallet-and-assets.md`。
- [x] **Decision（creditBalance/debitBalance 暴露模型）:** 抉择 A（`@BizAction` 内部方法，不暴露 GraphQL，仅由受信 BizModel 通过 `ILitemallWalletBiz` 注入调用）。备选 B（`@BizMutation` 公开 + userId 参数，参照 PointsAccount.earnPoints/spendPoints）。理由：钱包涉及资金，公开 `@BizMutation(userId, amount)` 会让任意登录用户可对任意 userId 加/扣余额（资金安全风险）；PointsAccount 的积分风险等级低于钱包。残留风险：内部方法须经 IBiz 接口声明（`creditBalance`/`debitBalance` 加入 `ILitemallWalletBiz`），测试经 `I*XxxBiz` 而非 IGraphQLEngine（见 Exit Criteria）。抉择写入 plan。
- [x] **Proof:** `./mvnw test -pl app-mall-service -am` — 新增 `TestLitemallWalletBizModel`（`JunitAutoTestCase`）：`getMyWallet`、`adminAdjust` 经 `IGraphQLEngine` 录制回放验证；`creditBalance`、`debitBalance` 经注入 `ILitemallWalletBiz` 接口调用验证（`@BizAction` 不走 GraphQL）。覆盖：`getMyWallet`（首次懒创建）、`creditBalance`（入账 + flow 校验 + balanceAfter 快照）、`debitBalance`（正常扣款 + 余额不足拒绝）、`adminAdjust`（入账/扣款双向 + 越权拒绝非 admin）。

Exit Criteria:

- [x] 钱包账户懒创建 + 原子余额操作（credit/debit/adminAdjust）落库，乐观锁冲突时抛 ERR_WALLET_VERSION_CONFLICT
- [x] 钱包流水每次变动写入（balanceAfter 快照），账户与流水可对账
- [x] **API 测试：** `getMyWallet`、`adminAdjust` 通过 `IGraphQLEngine` 测试（`JunitAutoTestCase` 录制回放）；`creditBalance`、`debitBalance` 为 `@BizAction` 内部方法，通过注入 `ILitemallWalletBiz` 接口测试
- [x] **owner-doc 更新（强制，non-degradable drift）：** 更新 `docs/design/wallet-and-assets.md` — reconcile"钱包账户在用户注册时自动创建"行改为懒创建语义；并补充钱包 `changeType × sourceType` 分类法（RECHARGE/PAY/REFUND/WITHDRAW × recharge/pay/admin-adjust 等来源）作为流水语义权威定义
- [x] `docs/logs/` updated

### Phase 2 - 充值流程（套餐配置 + 支付集成 + 回调入账）

Status: completed
Targets: `app-mall-service/.../entity/LitemallRechargeBizModel.java`、`app-mall-service/.../pay/PaymentCallbackImpl.java`(扩展)、`app-mall-service/.../entity/LitemallWalletBizModel.java`(credit 调用方)
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision`
- Prereqs: Phase 1（钱包原子操作）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读必读文档，列路径，每方法 selfcheck。
  - Docs read: nop-backend-dev + nop-testing（Phase 1 已加载）；参照 `LitemallOrderBizModel.prepay`/`confirmPaidByNotify`/`pay`（demo 守卫）/`LitemallSystemBizModel.getConfig`/`MockPayServiceImpl`/`LitemallCouponUserBizModel.claimCouponForUser`（@BizAction + IBiz 注入）/`TestConcurrencyGuards`（ormTemplate.runInSession）
- [x] **Decision（充值套餐存储方案）:** 抉择 A（存入现有 `LitemallSystem` 表，keyName=`recharge_packages`，value 为 JSON 数组 `[{id, label, amount, giftAmount}]`；读取经 `ILitemallSystemBiz.getConfig("recharge_packages")`，编辑经现有 `LitemallSystem` 后台 CRUD 页）。备选 B1（`NopSysVariable` — 否决：app 内无 NopSysVariable 读写 API，`getConfig` 实际读的是 `LitemallSystem` 表而非 NopSysVariable）。备选 B2（新增 `RechargePackage` ORM 实体 — 否决：ORM 变更为 Protected Area ask-first）。理由：套餐是少量、低频变更的配置，`LitemallSystem` 表已有 getConfig 读路径 + 后台 CRUD，零新增基础设施/实体。残留风险：JSON 经通用 CRUD 编辑体验较粗糙（触发条件：套餐数 > 50 或需 per-package 富信息时 → 升级为实体，见 Deferred）。抉择写入 plan + `wallet-and-assets.md`。
- [x] **Decision（outTradeNo 派生方案，B1/B2）:** `LitemallRecharge` 无 `outTradeNo` 列（ORM 变更为 Protected Area，不新增列）。抉择：outTradeNo **派生而非存储** — `outTradeNo = "RC" + String.format("%08d", rechargeId)`（如 `RC00000001`，10 字符，满足微信支付 6–32 字符要求，去掉 `RC` 前缀 parse int 即得 rechargeId）。备选（存储列）需 ORM 变更，否决。残留风险：派生值依赖 rechargeId 单调性（seq，成立）。回调路径：`confirmRechargeByNotify(outTradeNo, transactionId)` 按前缀校验 `RC` → strip → `Integer.parseInt` → `findById(rechargeId)`。
- [x] **Decision（RC 前缀路由安全不变量，M3）:** `PaymentCallbackImpl` 按 outTradeNo 前缀分流（`RC`→recharge，否则→order）。安全前提（已核实）：订单 outTradeNo = `order.getOrderSn()` = `generateOrderSn()` = `StringHelper.generateUUID()`（32 位小写 hex `[0-9a-f]`，`LitemallOrderBizModel.java:1648-1650`），永不以大写 `RC` 开头。**不变量：若未来 `generateOrderSn()` 引入字母前缀，必须重新评估 RC 路由分流。** 写入 plan。
- [x] **Add（充值套餐读取 API）:** `@BizQuery getRechargePackages(context)` — 经 `ILitemallSystemBiz.getConfig("recharge_packages")` 读取 JSON 并解析为列表；无配置时返回默认套餐列表（如 `[{amount:100,giftAmount:10}]` 兜底）。经 IGraphQLEngine 测试。
- [x] **Add（createRecharge 充值下单）:** `@BizMutation createRecharge(@Name amount, @Optional packageId, context)` — 校验金额 > 0；若指定 packageId 则从套餐读取 amount + giftAmount（金额必须匹配套餐）；创建 `LitemallRecharge`（payStatus=UNPAID, payChannel=WECHAT）；**派生 outTradeNo = "RC" + zero-padded(rechargeId)**；调 `PayService.createPayment(outTradeNo, amount, "钱包充值")`（参照 `LitemallOrderBizModel.prepay`）；返回 recharge + codeUrl。
- [x] **Add（confirmRecharge demo 手动确认）:** `@BizMutation confirmRecharge(@Name rechargeId, context)` — 仅当 `!payService.isEnabled()` 时允许（参照 order.pay 的 demo 路径）；校验 payStatus=UNPAID；调 `creditRecharge`（入账）。
- [x] **Add（confirmRechargeByNotify 受信回调）:** `@BizAction confirmRechargeByNotify(@Name outTradeNo, @Name transactionId, context)` — **内部方法（不暴露 GraphQL，安全要求见下条 Decision）**：受信入口（由 PaymentCallbackImpl 经注入 `ILitemallRechargeBiz` 调用）；校验 outTradeNo 以 `RC` 开头 → strip 前缀 → `findById(rechargeId)`；幂等（payStatus != UNPAID → skip）；调 `creditRecharge`。
- [x] **Decision（confirmRechargeByNotify 暴露模型，资金安全）:** 抉择 A（`@BizAction` 内部方法，不暴露 GraphQL，仅由 `PaymentCallbackImpl` 经 `ILitemallRechargeBiz` 注入调用）。备选 B（`@BizMutation` 公开）。理由：`confirmRechargeByNotify` 内部调 `creditBalance` 直接给钱包加余额；若为公开 `@BizMutation`，攻击者可先 `createRecharge(amount=1000)` 得 rechargeId=5，再按公开的 `RC`+`%08d` 格式算出 `RC00000005` 直接调 `confirmRechargeByNotify`，无需真实支付即可给钱包充值（直接资金盗刷）。`confirmRecharge`（demo 路径）有 `!payService.isEnabled()` 守卫故风险低，保持 `@BizMutation`。残留风险：内部方法须经 `ILitemallRechargeBiz` 接口声明，测试经 `I*XxxBiz`（见 Exit Criteria）。抉择写入 plan。
- [x] **Add（creditRecharge 入账内部方法）:** 私有/包级方法 — 更新 Recharge payStatus=PAID；调 `walletBiz.creditBalance(userId, amount+giftAmount, RECHARGE, "recharge", rechargeId, remark, context)`（一次性入账充值金额 + 赠送金额，sourceId=rechargeId 幂等）。
- [x] **Add（PaymentCallbackImpl 扩展）:** 在 `onPaymentSuccess(outTradeNo, transactionId)` 中按 outTradeNo 前缀路由：以 `RC` 开头 → `rechargeBiz.confirmRechargeByNotify`；否则 → `orderBiz.confirmPaidByNotify`（现有行为不变）。注入 `ILitemallRechargeBiz`。
- [x] **Proof:** `./mvnw test -pl app-mall-service -am` — 新增 `TestLitemallRechargeBizModel`：`getRechargePackages`（读 LitemallSystem + 兜底默认）、`createRecharge`（创建 UNPAID 记录 + outTradeNo 派生格式校验）、`confirmRecharge`（demo 入账 + 钱包余额增加 + flow 写入）经 `IGraphQLEngine` 验证；`confirmRechargeByNotify`（回调入账 + 幂等）经注入 `ILitemallRechargeBiz` 接口验证（`@BizAction` 不走 GraphQL）；PaymentCallbackImpl 路由（RC 前缀→recharge，其他→order）。

Exit Criteria:

- [x] 充值下单 → 微信支付 → 异步回调入账全流程可用（demo 模式走手动确认）；outTradeNo 派生格式 `RC`+8位填充满足微信 6–32 字符
- [x] 充值入账金额 = 充值金额 + 赠送金额，产生 RECHARGE 类型流水，sourceId=rechargeId 幂等
- [x] PaymentCallbackImpl 按前缀路由充值/订单回调，现有订单回调行为不变（RC 路由不变量已记录）
- [x] **API 测试：** `getRechargePackages` / `createRecharge` / `confirmRecharge` 通过 `IGraphQLEngine` 测试；`confirmRechargeByNotify` 为 `@BizAction` 内部方法（资金安全，不暴露 GraphQL），通过注入 `ILitemallRechargeBiz` 接口测试
- [x] **owner-doc 更新（强制）：** 更新 `wallet-and-assets.md`（充值套餐存储 Decision[存入 LitemallSystem] + 充值流程[outTradeNo 派生/回调路由]实现说明）
- [x] `docs/logs/` updated

### Phase 3 - 前台页面 + 后台页面

Status: completed
Targets: `app-mall-web/.../pages/LitemallWallet/LitemallWallet.view.xml`、`app-mall-web/.../pages/LitemallRecharge/LitemallRecharge.view.xml`、`app-mall-web/.../pages/LitemallWalletFlow/LitemallWalletFlow.view.xml`、`app-mall-web/.../pages/LitemallRecharge/LitemallRecharge.lib.xjs`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 1 + Phase 2（后端 API 就绪）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读必读文档，列路径，每文件完成后 selfcheck。
  - Docs read: nop-frontend-dev skill（00-start-here/application-project-defaults.md, 02-core-guides/view-and-page-customization.md, 02-core-guides/delta-customization.md, 03-runbooks/prefer-delta-over-direct-modification.md）；参照 `LitemallPointsAccount.view.xml`（bounded-merge + adjust dialog/simple pattern）
- [x] **Add（用户钱包页）:** `LitemallWallet.view.xml` — 钱包余额展示卡（balance + totalRecharge + totalSpent 收支汇总）+ "充值"入口按钮（跳转充值页）+ 钱包流水列表（按 addTime 倒序 + changeType 筛选 + 时间范围筛选）。bounded-merge 生成桩。
- [x] **Add（充值页）:** `LitemallRecharge.view.xml` — 充值套餐网格（`getRechargePackages`）+ 自定义金额输入 + 选择后调 `createRecharge` → 展示微信支付 codeUrl（扫码）或 demo 模式直接 `confirmRecharge`。
- [x] **Add（钱包流水页）:** `LitemallWalletFlow.view.xml` — 流水列表（changeType 标签色 + changeAmount 收支色 + balanceAfter 快照列 + sourceType 显示 + 时间范围筛选）。
- [x] **Add（后台充值记录页）:** `LitemallRecharge` 后台视图增强（admin 视角）— 充值记录列表（userId/amount/giftAmount/payChannel/payStatus/addTime 筛选）+ 只读查看。bounded-merge 生成桩。
- [x] **Add（后台充值套餐配置）:** 套餐配置经现有 `LitemallSystem` 后台 CRUD 页编辑（keyName=`recharge_packages` 的 JSON value），本计划**不新增独立富配置页、不新增 `@BizMutation`**（Phase 2 的 `getRechargePackages` 已提供读取）。独立富配置页为 follow-up（触发条件见 Deferred 充值套餐实体）。Phase 3 因此仅前端技能，无后端方法新增。
- [x] **Proof:** `./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS。视图 XML 通过 AMIS schema 校验（启动时加载无报错）。

Exit Criteria:

- [x] 用户钱包页（余额 + 收支汇总 + 流水筛选）、充值页（套餐 + 支付）、流水页完整可用
- [x] 后台充值记录列表可用；套餐配置经现有 `LitemallSystem` 后台 CRUD 编辑可用（无新增后端方法）
- [x] 所有新增视图 bounded-merge 生成桩，无手编 `_gen` 文件
- [x] **owner-doc 更新（强制）：** 更新 `wallet-and-assets.md`（前端页面落地说明）
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed
- Auditor / Agent: independent subagents (4 adversarial rounds, fresh sessions)
- Evidence:
  - R1 `ses_0f31c9e0cffeg0dJEywdIKiTV4` — BLOCKER：3 blockers（outTradeNo 长度不足微信限制 / outTradeNo 存储与派生矛盾 / 套餐存储误用 NopSysVariable 无读写 API）+ 6 major objections（owner-doc drift 可选化 / 钱包分类法未入文档 / RC 路由未引证 / 缺暴露 Decision / Phase3 配置模糊 / 退款流程未处置）。Baseline 全部核实属实。
  - R2 `ses_0f30f2c89ffe4YtUnNvLnbmBBp` — 原 9 项全部 RESOLVED；新发现 NEW-1（MAJOR）：`confirmRechargeByNotify` 为公开 `@BizMutation` 且内部给钱包加余额 = 资金盗刷向量（攻击者按 `RC`+填充格式直接回调入账）。
  - 修订：confirmRechargeByNotify 改 `@BizAction` 内部 + 资金安全 Decision；测试改经 `ILitemallRechargeBiz`。
  - R3 `ses_0f30a4530ffeEdlqPYelkxdDqh` — PASS：NEW-1 RESOLVED，无回归，方法/测试映射一致（`@BizAction` Javadoc 证不暴露 GraphQL；`confirmPaidByNotify`/`claimCouponForUser` 佐证 IBiz 注入调用可行）。
  - R4 `ses_0f30842c6ffemopLkh0LRNmOx0` — PASS（连续第二轮 clean）：整 plan 对抗复核通过；`@Auth(roles="admin")` 为真实 Nop 注解且与 `adjustPoints` 同模式；RC-UUID 不变量源码核实；并发/幂等/NopException/Protected Area 均覆盖。minor：confirmRecharge(demo) 建议加 ownership 校验（非阻塞）。
  - 共识：R3+R4 两轮连续 clean，达到 consensus。

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs are aligned（`wallet-and-assets.md` 更新 — 懒创建 reconcile + changeType×sourceType 分类法 + 充值流程实现说明 + 前端页面落地说明）
- [x] verification has run: `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` BUILD SUCCESS + `./mvnw test -pl app-mall-service -am` 全绿（334 tests, 0 failures） + `./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（getMyWallet/adminAdjust/getRechargePackages/createRecharge/confirmRecharge）；`@BizAction` methods（creditBalance/debitBalance/ensureWallet/confirmRechargeByNotify）tested via injected `ILitemallWalletBiz`/`ILitemallRechargeBiz` interface（confirmRechargeByNotify wrapped in `ormTemplate.runInSession` since `@BizAction` does not auto-open a session）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed
- [x] skill loading verification: each phase scanned available skills, loaded all matching skills, read ALL mandatory docs, selfchecked after each method/class
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 余额支付（收银台通道）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 余额作为收银台支付通道（展示 / 支付密码确认 / 订单扣款接线）属 P30（Protected Area ask-first）。本计划已实现钱包原子 `debitBalance` 内部 API，P30 届时接线调用即可。
- Successor Required: `yes`（P30 多支付通道）

### 充值套餐持久化实体

- Classification: `optimization candidate`
- Why Not Blocking Closure: 套餐配置存入 `LitemallSystem` 表（keyName=`recharge_packages` JSON），经 getConfig 读 + 后台 CRUD 编辑，功能完整。ORM 实体升级为 Protected Area，且基线套餐数量少。
- Successor Required: `yes`（触发条件：套餐数 > 50 或需要 per-package 富信息[图片/有效期/独立富配置页]时）

### 充值退款流程

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: `PaymentCallbackImpl` 仅桥接 `onPaymentSuccess`，无退款异步通知；`mall/pay-status REFUNDED` 与 `wallet-change-type REFUND` 字典预留语义但流程不实现。退款场景由后台 `adminAdjust` 人工兜底。
- Successor Required: `yes`（P30 多支付通道引入退款异步通知时一并补齐）

### 提现（WITHDRAW）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 字典 `WITHDRAW`(30) 预留，设计文档无提现流程。
- Successor Required: `no`

## Closure

<!-- Closure audit MUST be performed by an independent subagent. Do NOT fill this section yourself. -->

Status Note: All three Phases landed in the live repo and reconcile with the plan. Phase 1 (wallet core) — `LitemallWalletBizModel` exposes `getMyWallet`/`adminAdjust` (`@BizQuery`/`@BizMutation`, IGraphQLEngine-tested) plus `creditBalance`/`debitBalance`/`ensureWallet` (`@BizAction` internal, ILitemallWalletBiz-tested) backed by `LitemallWalletMapper.updateBalanceIfVersion` EQL (optimistic version UPDATE) + flow snapshotting; `ERR_WALLET_NOT_FOUND`/`ERR_WALLET_INSUFFICIENT`/`ERR_WALLET_VERSION_CONFLICT` land in `AppMallErrors`. Phase 2 (recharge flow) — `LitemallRechargeBizModel` adds `getRechargePackages`/`createRecharge`/`confirmRecharge` (IGraphQLEngine-tested) and `confirmRechargeByNotify` (`@BizAction` internal, ILitemallRechargeBiz-tested, idempotent on replayed notify); `PaymentCallbackImpl` routes by `RC` prefix with the order-UUID-vs-RC invariant documented in source. Phase 3 (frontend) — `LitemallWallet`/`LitemallRecharge`/`LitemallWalletFlow` view.xml all present as bounded-merge stubs. Owner doc `docs/design/wallet-and-assets.md` reconciled (lazy-create + changeType×sourceType taxonomy + recharge impl notes + frontend section). Log `docs/logs/2026/06-28.md` carries three Phase entries with green verification (334 service tests, web compile BUILD SUCCESS). Anti-hollow check passed: every new method has a real body and is wired into callers (rechargeBiz → walletBiz.creditBalance, PaymentCallbackImpl → rechargeBiz.confirmRechargeByNotify); no swallowed exceptions, no `return null` placeholders on the happy path. No in-scope defect downgraded to Deferred; the four Deferred items are genuine out-of-scope/optimization classifications with successor triggers. Plan can close.

Closure Audit Evidence:

- Reviewer / Agent: independent closure auditor subagent (fresh session, distinct from the implementing agent — this pass)
- Audit scope: re-verified all Phase Exit Criteria and Closure Gates against live repo evidence; did not trust plan `[x]` marks blindly.
- Live-repo evidence inspected:
  - `app-mall-service/src/main/java/app/mall/service/entity/LitemallWalletBizModel.java` — all five methods (`getMyWallet`/`creditBalance`/`debitBalance`/`adminAdjust`/`ensureWallet`) + private `mutateBalance` present with real bodies; `@Auth(roles="admin")` on `adminAdjust`; optimistic-lock path uses `dao().flushSession()` + `clearEntitySessionCache()` + `walletMapper.updateBalanceIfVersion` then throws `ERR_WALLET_VERSION_CONFLICT` on 0 affected rows; flow snapshot written with `balanceAfter`. Matches Phase 1 Exit Criteria 1 & 2.
  - `app-mall-service/src/main/java/app/mall/service/entity/LitemallRechargeBizModel.java` — `getRechargePackages` reads `LitemallSystem` config with default fallback; `createRecharge` derives `RC`+zero-padded id and calls `payService.createPayment`; `confirmRecharge` has `!payService.isEnabled()` demo guard + ownership check; `confirmRechargeByNotify` is `@BizAction` (not GraphQL), idempotent on non-UNPAID. Matches Phase 2 Exit Criteria 1–3.
  - `app-mall-service/src/main/java/app/mall/service/pay/PaymentCallbackImpl.java` — `onPaymentSuccess` routes by `RC` prefix → `rechargeBiz.confirmRechargeByNotify` (system context), else → `orderBiz.confirmPaidByNotify`. RC-routing invariant documented in class Javadoc. Existing order path unchanged.
  - `app-mall-dao/src/main/java/app/mall/dao/mapper/LitemallWalletMapper.java` + `app-mall-dao/src/main/resources/_vfs/app/mall/sql/LitemallWallet.sql-lib.xml` — `@SqlLibMapper` + EQL `updateBalanceIfVersion` with `WHERE id=? AND version=?`, mirrors `LitemallPointsAccountMapper`.
  - `app-mall-service/src/main/java/app/mall/service/AppMallErrors.java` — `ERR_WALLET_NOT_FOUND`/`ERR_WALLET_INSUFFICIENT`/`ERR_WALLET_VERSION_CONFLICT`/`ERR_RECHARGE_*` all present.
  - `app-mall-service/src/test/java/app/mall/service/entity/TestLitemallWalletBizModel.java` (8 cases) + `TestLitemallRechargeBizModel.java` (9 cases) — `@BizQuery`/`@BizMutation` exercised via `IGraphQLEngine` (`rpc(op, action, data)`); `@BizAction` (`creditBalance`/`debitBalance`/`confirmRechargeByNotify`) exercised via injected `ILitemallWalletBiz`/`ILitemallRechargeBiz`, with `confirmRechargeByNotify` wrapped in `ormTemplate.runInSession` since `@BizAction` does not auto-open a session. Covers lazy-create, version advance, idempotent replay, RC-vs-non-RC routing, ownership rejection, insufficient-balance rejection.
  - `app-mall-web/src/main/resources/_vfs/app/mall/pages/LitemallWallet/LitemallWallet.view.xml`, `.../LitemallRecharge/LitemallRecharge.view.xml`, `.../LitemallWalletFlow/LitemallWalletFlow.view.xml` — all three Phase 3 view files present.
  - `docs/design/wallet-and-assets.md` — lazy-create reconcile (line 28), changeType×sourceType taxonomy (line 43+), recharge implementation notes (lines 75–79), frontend section (lines 92–96). Owner-doc drift reconciled.
  - `docs/logs/2026/06-28.md` — three dated Phase entries (Phase 1 / 2 / 3) each carrying `./mvnw` verification status; aggregate service test count 334 green, web compile BUILD SUCCESS.
- Text consistency: Plan Status `completed` ↔ Phase 1/2/3 `Status: completed` ↔ all Exit Criteria `[x]` ↔ all Closure Gates `[x]` ↔ log entries agree.
- Deferred honesty: the four Deferred items (余额支付/充值套餐实体/充值退款/提现) are genuine out-of-scope or optimization candidates with explicit successor triggers — no in-scope defect or contract drift hidden there.
- Verdict: APPROVED — plan is complete and may close.

Follow-up:

- P30 接线余额支付时调用 `debitBalance`，届时实现收银台余额通道展示 + 支付密码确认。
