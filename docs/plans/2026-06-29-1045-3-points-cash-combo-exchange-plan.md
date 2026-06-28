# 积分+现金组合兑换（关闭 P27/P29 deferred successor）

> Plan Status: completed
> Last Reviewed: 2026-06-29
> Mission: mall
> Work Item: 积分商城组合兑换 — 纯积分兑换（已交付）扩展为积分+现金组合兑换
> Source: `docs/plans/2026-06-29-0900-1-points-mall-exchange-plan.md` → `Deferred → 积分+现金组合兑换`（:168-172，Successor Required: yes，触发条件「业务要求积分+现金组合兑换时」）；该计划 Decision D3（:64）抉择方案 B（首期仅纯积分），备选 A（消费 P30/P29 通道，兑换订单挂独立 outTradeNo）作为本 successor 实现路径备忘
> Related: `docs/plans/2026-06-28-1822-1-phase30-multi-payment-channels-plan.md`（P30 多支付通道，done）、`docs/plans/2026-06-28-1400-1-phase29-wallet-recharge-plan.md`（P29 钱包，done）、`docs/plans/2026-06-29-0330-1-wallet-recharge-refund-flow-plan.md`（充值退款异步对账先例，done）
> Audit: required

## Current Baseline

**触发条件已满足（live repo 核验）：** P27 Deferred「积分+现金组合兑换」触发条件为「业务要求积分+现金组合兑换时」。本计划即该业务要求的执行 slice。所有技术前置已完成：P30 多支付通道（done，PayChannel 策略抽象 + 微信/支付宝通道 + 余额支付路径）、P29 钱包（done，debitBalance/creditBalance + RC outTradeNo 派生 + PaymentCallback 回调路由）。纯积分兑换已交付（`2026-06-29-0900-1` done）。触发条件客观成立。

**纯积分兑换现状（`LitemallPointsExchangeOrderBizModel.java`）：**
- `exchange(@BizMutation)`（:80-228）：鉴权 → PointsGoods 状态/时间窗/零售在售校验 → maxPerUser 限购 → 地址快照 → `pointsGoodsMapper.reduceExchangeStock` 原子扣库存（:180）→ 建单 `exchangeStatus=PENDING` + saveEntity（:188-208）→ `dao().flushSession()`（:214）→ `pointsAccountBiz.spendPoints(..., "mall-exchange", exchangeOrderId, ...)`（:220-223）。
- 履约：`shipExchangeOrder`（PENDING→SHIPPED，:277）、`confirmExchangeOrder`（SHIPPED→COMPLETED，:299）、`cancelExchangeOrder`（PENDING→CANCELLED + 还库存 + 退积分，:314-351）。

**PointsExchangeOrder 实体（`model/app-mall.orm.xml:2245-2307`）：** 列含 pointsPrice/quantity/totalPoints/exchangeStatus（dict `mall/exchange-status` :2278-2279）/address 快照/shipCode。**状态机 `mall/exchange-status`（:157-162）仅 PENDING=0/SHIPPED=10/COMPLETED=20/CANCELLED=30，无「待支付」态，无 payStatus/payChannel/cashPrice/walletPayAmount 列。**

**PointsGoods 实体（:2202-2244）：** 价格字段仅 `pointsPrice`（int，:2213，纯积分）。**无 cashPrice / 组合价字段。**

**支付编排可复用原语（P30/P29，本计划消费而非新建集成）：**
- `PayChannel` 策略（`app-mall-api/.../pay/PayChannel.java`）+ 注册表 `PayChannelRegistryImpl`（`<ioc:collect-beans>` 注册）。已注册通道：`wxPayService`（WECHAT）、`alipayPayChannel`（ALIPAY）。
- ⚠️ **不存在 `BalancePayChannel` bean**（grep 零命中；`TestPayChannelBizModel.java:122` 注释「BalancePayChannel in Phase 2」为误导）。余额支付走独立路径 `LitemallOrderBizModel.payByBalance`（:824-877），不经 PayChannel 策略。
- `markOrderPaidCore`（`LitemallOrderBizModel:811-820`）：共享「订单已支付」尾部（设状态 + payTime + afterCommit 通知）。
- 钱包：`walletBiz.debitBalance(userId, amount, WALLET_CHANGE_TYPE_PAY, SOURCE_TYPE_PAY, sourceId, ...)`（`payByBalance:862-867` 用 orderSn 作 sourceId；余额支付同步、不产生 outTradeNo）。
- 充值 outTradeNo 派生先例：`LitemallRechargeBizModel.deriveOutTradeNo`（:307-310）= `OUT_TRADE_NO_PREFIX("RC") + %08d(rechargeId)`；回调路由 `PaymentCallbackImpl.onPaymentSuccess`（:59-75）按前缀分流：`RC`→`confirmRechargeByNotify`，else→`orderBiz.confirmPaidByNotify`。退款回调 `onRefundSuccess`（:98-129）仅处理 RC + order。

**积分扣减：** `LitemallPointsAccountBizModel.spendPoints`（:100-112）→ `mutateBalance`（:158-206，乐观锁 + balanceAfter 快照 + flushSession/clearCache 副作用）。幂等：earn 路径应用层 `(sourceType,sourceId)` 查重（:89-95）；spend 依赖调用方 sourceId 唯一。

**owner doc 设计缺口（本计划必须先补设计）：** `docs/design/marketing-and-promotions.md`（积分商城段 :216-249）+ `wallet-and-assets.md` **均只记录「组合兑换 deferred」，无实际组合流程设计**——未规定：PointsGoods 是否新增 cashPrice、兑换单是否新增 pay 字段、状态机是否加待支付态、outTradeNo 前缀、积分与现金的扣减顺序/原子性、组合兑换的取消/退款路径。

**前端：** `points-mall.page.yaml`（:96-120）兑换按钮调 `exchange`（仅 pointsGoodsId/quantity）；`my-exchange-orders.page.yaml`（:88-98）取消按钮。无现金 UI。

**模块：** `model/app-mall.orm.xml` → 代码生成 → `app-mall-service`（BizModel + 支付编排 + 回调路由）→ `app-mall-web`（兑换页 + 我的兑换单 + 收银）。

**核心缺口：** 组合兑换流程在 owner doc 与代码均未设计/实现；需先做 app-layer 设计决策，再 ORM 扩展，再支付编排。

## Goals

- 在积分商城支持「积分+现金」组合兑换：用户以 `pointsPrice × quantity` 积分 + `cashPrice × quantity` 现金兑换商品。
- 复用既有 P30 多支付通道（微信/支付宝）+ P29 钱包余额支付现金部分，与主订单支付流不串账（独立 outTradeNo 前缀 + 独立回调分支）。
- 补齐 owner doc 组合兑换设计（`marketing-and-promotions.md`），使流程、状态机、原子性、退款路径有权威口径。
- 关闭 P27/P29 Deferred「积分+现金组合兑换」successor。

## Non-Goals

- **不新建支付集成** — 消费已 done 的 P30 通道策略 + P29 钱包；不引入新支付 SDK（支付宝真实 SDK 仍为 P30 deferred successor）。
- **不实现「纯现金兑换」** — 组合兑换必须含积分分量（纯积分已交付，纯现金非积分商城语义）。
- **不改纯积分兑换路径** — `exchange`（纯积分）保持现状；组合兑换单独方法/或扩展同一方法按 PointsGoods 类型分流（见 Decision）。
- **不实现组合兑换的物流差异** — 履约沿用既有 ship/confirm/cancel（实物）；虚拟商品的组合兑换不在本计划。
- **不引入定时取消未支付组合单的调度任务为独立特性** — 若 Decision 选「积分先扣+现金待支付」需超时取消，则复用 nop-job-local 既有可能（见 Decision E3），不单独立项。
- **不更新 roadmap 阶段状态** — deferred successor，P27 Phase Status 保持 `done`。

## Task Route

- Type: `app-layer design change`（owner doc 无组合兑换流程设计，必须先补设计决策；route 非 pure implementation-only）
- Owner Docs: `docs/design/marketing-and-promotions.md`（积分商城 :216-249，组合兑换设计落点）、`docs/design/wallet-and-assets.md`（钱包原语引用 :30-115）
- Skill Selection Basis: 设计决策阶段无 skill（纯业务/架构决策）；ORM 扩展 → `nop-orm-modeler` + `nop-database-design`；支付编排 BizModel + 回调路由 → `nop-backend-dev` + `nop-testing`；前端兑换/收银 → `nop-frontend-dev`

## Infrastructure And Config Prereqs

- 无新增端口/环境变量。
- **Protected Area（支付）：** 现金部分消费 P30 通道 + P29 钱包，不新建支付集成；但需新增 outTradeNo 前缀分支 + 回调路由分支（`PaymentCallbackImpl`），属于「在既有支付回调路径上新增业务路由」。本计划在 Phase 1 Decision E4 显式裁定这是「消费既有通道能力」还是「修改通道行为」（后者需 ask-first）。授权机制：MISSION_DRIVER「execute the entire plan」类指令构成 Protected Area 授权（与 P29/P30 先例一致）。
- 回滚：新列/新状态为增量；回滚 = 移除 ORM 扩展 + 移除回调分支。无数据迁移。

## Execution Plan

### Phase 1 - 设计决策 + owner doc + ORM 扩展

Status: completed
Targets: `docs/design/marketing-and-promotions.md`（组合兑换设计段）、`model/app-mall.orm.xml`（PointsGoods :2202-2244、PointsExchangeOrder :2245-2307、exchange-status dict :157-162）
Required Skill: `nop-orm-modeler`, `nop-database-design`（设计决策项本身无 skill）

- Item Types: `Decision | Add | Proof`
- Prereqs: 无

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`（ORM 项）；设计决策项读 owner doc 现状。读完必读文档，列路径。
  - Docs read: `model/app-mall.orm.xml`(dicts/PointsGoods/PointsExchangeOrder)、`_AppMallDaoConstants.java`、`nop-orm-modeler` SKILL.md、`nop-database-design` SKILL.md、既有价格列约定(DECIMAL precision=10 scale=2)、`LitemallRechargeBizModel`(outTradeNo 派生先例)、`PaymentCallbackImpl`(RC 路由先例)、`marketing-and-promotions.md`(积分商城段)
- [x] **Decision E1：PointsGoods 价格模型。** 抉择：**新增可空 `cashPrice`（BigDecimal，propId 15，DECIMAL(10,2)）列**；`cashPrice` 非空且 >0 → 该商品支持组合兑换；`cashPrice` 为空/0 → 仅纯积分（现状）。备选：新建 PointsGoodsType 枚举字段区分纯积分/组合——但 cashPrice 非空即隐含组合语义，无需额外枚举。残留风险：需后台商品编辑页暴露 cashPrice 输入（Phase 3）。
- [x] **Decision E2：兑换单支付状态建模。** 抉择：**PointsExchangeOrder 新增 `payStatus`(propId 21, dict `mall/pay-status`) + `payChannel`(propId 22, dict `mall/pay-channel` int，与 LitemallOrder/Recharge 一致非 String) + `cashPrice`(propId 23, BigDecimal 快照) + `walletPayAmount`(propId 24, BigDecimal) 列**；exchange-status 状态机新增 `AWAITING_PAYMENT=5`（已 codegen 生成 `EXCHANGE_STATUS_AWAITING_PAYMENT`）。组合兑换流程：建单 `exchangeStatus=AWAITING_PAYMENT` → 现金支付回调确认 → `exchangeStatus=PENDING`（待发货）。备选：不加状态、用独立 payStatus 驱动——但履约链（ship 等）依赖 exchangeStatus，复用状态机更内聚。残留风险：纯积分路径状态机不变（E2 仅组合路径新增 AWAITING_PAYMENT）。
- [x] **Decision E3：积分与现金扣减顺序与原子性。** 抉择：**积分先扣（建单时）+ 现金后付（用户支付）**，与纯积分兑换的「建单即扣积分」一致；若用户未支付现金，组合单停留在 AWAITING_PAYMENT，由超时取消任务（`MallJobInvoker.cancelExpiredExchangeOrders`，复用 nop-job-local）还库存 + 退积分。备选 A（现金先付再扣积分）——破坏与纯积分路径一致性，且现金支付是异步回调更难回滚积分预留。备选 B（两阶段预留）——过度复杂。残留风险：AWAITING_PAYMENT 期间积分已扣，需超时取消任务兜底（Phase 2 交付项，非 deferred）。
- [x] **Decision E4：Protected Area（支付）裁定。** 抉择：**新增 outTradeNo 前缀 `PE`（points-exchange）+ `PaymentCallbackImpl.onPaymentSuccess` 新增 `PE` 分支路由到新 `confirmExchangePaidByNotify`**，归类为「消费既有 P30 通道策略（prepay/refund）的 新业务路由」，**非修改通道行为**（通道策略接口不变），故不需额外 ask-first；MISSION_DRIVER「execute the entire plan」授权覆盖。退款回调 `onRefundSuccess` 同理新增 `PE` 分支 → `refundExchangeOrderByNotify`。备选：复用 order 的 outTradeNo（orderSn）——会与主订单回调路由串账，不可行。残留风险：`PaymentCallbackImpl` 路由不变式（order outTradeNo 为 32 位小写 hex UUID，不以大写 PE 开头）已在 `PaymentCallbackImpl` javadoc 核验保持。
- [x] **Decision E5：余额支付分量建模。** 抉择：**组合兑换的现金部分余额支付走「直接 debitBalance」路径**（mimic `payByBalance`，不经不存在的 BalancePayChannel），exchange 单设 `payChannel=PAY_CHANNEL_BALANCE` + `walletPayAmount`，sourceId 用 exchangeOrderId；不为本计划创建 `BalancePayChannel` bean（记入 Deferred，触发条件「余额需作为统一 PayChannel 策略被多处复用时」）。备选：先建 BalancePayChannel——扩大范围，且 P30 已用直接 debit 路径，保持一致。
- [x] **Add（owner doc）：** `marketing-and-promotions.md` 补「组合兑换流程」设计子节（价格模型 E1 / 状态机 E2 / 原子性 E3 / outTradeNo 与回调 E4 / 余额分量 E5 / 取消退款路径 / 履约流转），关闭 owner doc 设计缺口；原「组合兑换 Deferred」标注改为「已交付」。
- [x] **Add（ORM）：** PointsGoods 增加 `cashPrice`（BigDecimal，可空，propId 15）；PointsExchangeOrder 增加 `payStatus`/`payChannel`/`cashPrice`/`walletPayAmount`（propId 21-24）；exchange-status dict 增加 `AWAITING_PAYMENT=5`。codegen 已生成 `_LitemallPointsGoods._cashPrice`、`_LitemallPointsExchangeOrder._payStatus/_payChannel/_cashPrice/_walletPayAmount`、`EXCHANGE_STATUS_AWAITING_PAYMENT=5`；deploy SQL（mysql/postgresql/oracle `_create_app-mall.sql`）已同步再生成。
- [x] **Proof：** `mvn install -DskipTests` BUILD SUCCESS；`_gen` 实体含新字段（PROP_NAME_cashPrice/payStatus/payChannel/walletPayAmount）；dict 含 `AWAITING_PAYMENT=5`；既有 `TestLitemallPointsExchangeOrderBizModel` 12 测试全绿（纯积分路径零回归）。

Exit Criteria:

- [x] E1-E5 五个 Decision 各记录抉择/备选/残留风险
- [x] owner doc `marketing-and-promotions.md` 组合兑换设计落地（含状态机图、原子性、退款路径）
- [x] ORM 扩展落地，纯积分路径字段/状态零回归（cashPrice/payStatus 可空，12 测试绿）
- [x] `mvn install` BUILD SUCCESS

### Phase 2 - 后端组合兑换收银编排 + 回调路由 + 测试

Status: completed
Targets: `app-mall-service/.../LitemallPointsExchangeOrderBizModel.java`、`app-mall-service/.../pay/PaymentCallbackImpl.java`、`LitemallRechargeBizModel`（outTradeNo 派生先例）
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Fix | Proof`
- Prereqs: Phase 1（ORM + dict + owner doc 设计就绪）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完必读文档，列路径。每个方法写完 selfcheck（NopException/ErrorCode、@Inject 非 private、跨实体用 I*Biz）。
  - Docs read: `nop-backend-dev` SKILL.md（service-layer/IBiz 契约/反模式表）、`nop-testing` SKILL.md（IGraphQLEngine + @BizAction 需 runInSession 先例）、既有 `exchange`/`payByBalance`/`confirmRechargeByNotify`/`refundRechargeByNotify`/`cancelExpiredOrders` 先例、`IPayChannelRegistry`/`PayChannel`、`ILitemallWalletBiz`、`AppMallErrors`。
- [x] **Add：** `exchangeCombo(@BizMutation)`（独立方法，纯积分 `exchange` 路径零回归）——组合兑换：校验 PointsGoods.cashPrice>0 → 扣积分（复用 spendPoints，sourceType=mall-exchange）→ 扣 exchangeStock → 建单 `exchangeStatus=AWAITING_PAYMENT` + payStatus=UNPAID + cashPrice 快照 → 派生 outTradeNo（`PE`+paddedId）→ 返回收银参数（outTradeNo/cashPrice/totalCash）。maxPerUser 计数含 AWAITING_PAYMENT（audit minor）。
- [x] **Add：** `confirmExchangePaidByNotify`（@BizAction，内部，资金安全非 GraphQL 暴露）mimic `confirmRechargeByNotify`——按 PE outTradeNo 反查、幂等（仅 AWAITING_PAYMENT→PENDING）、设 payStatus=PAID。
- [x] **Add：** `PaymentCallbackImpl.onPaymentSuccess` 新增 `PE` 前缀分支 → `confirmExchangePaidByNotify`；`onRefundSuccess` 新增 `PE` 分支 → 新 `refundExchangeOrderByNotify`。路由不变式保持（order outTradeNo 为 32 位小写 hex，不以大写 PE/RC 开头）。
- [x] **Add：** 组合单取消/退款路径——`cancelExchangeOrder` 扩展：AWAITING_PAYMENT/PENDING→CANCELLED，还库存 + 退积分 + （若已付现金）触发退款（余额 creditBalance 同步 + payStatus=REFUNDED；通道 payChannel.refund 异步，回调 `refundExchangeOrderByNotify` 推进 REFUNDED）。
- [x] **Add：** AWAITING_PAYMENT 超时取消定时任务（复用 nop-job-local，`MallJobInvoker.cancelExpiredExchangeOrders` + scheduler.yaml `cancel-expired-exchange-orders`，mimic cancelExpiredOrders，原子 mapper `LitemallPointsExchangeOrderMapper.updateStatusIfMatch`）——还库存 + 退积分，现金未付无需退款。
- [x] **Proof（IGraphQLEngine）：** `exchangeCombo` 经 `IGraphQLEngine` 验证：建单 AWAITING_PAYMENT + 积分已扣 + outTradeNo 派生（PE 前缀）+ cashPrice/totalCash。
- [x] **Proof（IGraphQLEngine / I*Biz）：** `confirmExchangePaidByNotify`（@BizAction 经 `ILitemallPointsExchangeOrderBiz` 调用，runInSession）幂等推进 AWAITING_PAYMENT→PENDING；`payComboByBalance`（@BizMutation 经 IGraphQLEngine）扣余额推进 PENDING；`cancelExchangeOrder` 组合路径退积分+退款（余额退回同步 REFUNDED）。
- [x] **Proof（IGraphQLEngine）：** 纯积分 `exchange` 路径零回归（cashPrice 为空走原路径，combo 字段为 null）。
- [x] **Proof：** 全量 `mvn test` 通过（app-mall-service 470 测试 0 失败，含新增 15 组合测试 + 既有 12 纯积分测试全绿）；`mvn clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS。

Exit Criteria:

- [x] 组合兑换建单/支付回调/取消退款/超时取消全链路落地
- [x] Protected Area（E4）路由裁定落地，`PaymentCallbackImpl` 路由不变式保持（测试以 NPE-as-routing-proof 先例验证 PE 分支，沿用 TestLitemallRechargeBizModel RC 验证手法）
- [x] **API 测试：** `exchangeCombo`/`cancelExchangeOrder`/`payComboByBalance`/`payComboByChannel`（@BizMutation）经 `IGraphQLEngine`；`confirmExchangePaidByNotify`（@BizAction）经 `ILitemallPointsExchangeOrderBiz` 接口（runInSession）
- [x] 纯积分路径零回归（既有 12 测试 + 新 combo 测试中纯积分断言）
- [x] 全量测试绿（470/470）
- [x] `docs/logs/` 更新（见下方闭合阶段）

### Phase 3 - 前端 + owner doc 收尾

Status: completed
Targets: `app-mall-web/.../mall/points/points-mall.page.yaml`、`my-exchange-orders.page.yaml`、新增组合兑换收银页；admin PointsGoods 编辑 view
Required Skill: `nop-frontend-dev`, `nop-testing`

- Item Types: `Add | Proof`
- Prereqs: Phase 2（后端契约就绪）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完必读文档，列路径。每文件 selfcheck。
  - Docs read: `nop-frontend-dev` SKILL.md（XView 三层/bounded-merge/反模式表）、既有 `points-mall.page.yaml`/`my-exchange-orders.page.yaml`/`LitemallPointsGoods.view.xml`。
- [x] **Add：** `points-mall.page.yaml` 兑换按钮按 PointsGoods.cashPrice 分流（纯积分走 exchange；组合走 exchangeCombo + 收银）；展示积分+现金双价（cashPrice>0 时显示「积分+现金组合兑换」+ `${pointsPrice} 积分 + ¥${cashPrice}`）。
- [x] **Add：** 组合兑换收银——`my-exchange-orders.page.yaml` 组合单(AWAITING_PAYMENT/5)展示「余额支付」入口（payComboByBalance + 登录密码确认对话框，inline 通道选择），兑换单内联通道选择（余额）。
- [x] **Add：** `my-exchange-orders.page.yaml` 组合单状态展示 AWAITING_PAYMENT(5) + 待支付（余额支付入口）/取消入口（exchangeStatus 0||5，取消文案区分已付现金将退回）。
- [x] **Add：** admin `LitemallPointsGoods.view.xml` grid 增 `cashPrice` 列；edit form 增 `cashPrice` 字段（layout + cell placeholder「组合兑换现金单价，留空/0=仅纯积分兑换」）。
- [x] **Proof：** `mvn install -pl app-mall-web -am` BUILD SUCCESS（page.yaml/view.xml 语法校验通过）；owner doc（marketing-and-promotions.md 组合兑换设计）与实现一致。

Exit Criteria:

- [x] 前端组合兑换全链路（兑换→收银→我的单→后台编辑）可走通（page.yaml/view.xml 构建通过，后端契约 Phase 2 已验证）
- [x] owner doc 与实现一致
- [x] `docs/logs/` 更新（`docs/logs/2026/06-29.md` 三 Phase 记录）

## Plan Audit

- Status: passed（共识达成：Round 1 PASS）
- Auditor / Agent: 独立 subagent，fresh session（task `ses_0efda7bd2ffepQQdv2BEkhNfpw`）
- Evidence: 全量 baseline 经 live repo 核验准确（纯积分 exchange 流程 :80-228；PointsExchangeOrder/PointsGoods 无 pay/cashPrice 字段；exchange-status dict 仅 4 态无 AWAITING_PAYMENT；PayChannel 注册表仅 WECHAT+ALIPAY、确认无 BalancePayChannel bean；payByBalance 直接 debit 非通道策略；RC outTradeNo 派生 + PaymentCallback 路由 + 路由不变式 generateOrderSn=UUID 小写 hex；spendPoints/mutateBalance 乐观锁 + flush 副作用；owner doc 组合兑换设计缺口确认；D3 + Deferred verbatim）。E1-E5 五 Decision sound（含 Protected Area E4 裁定「新增 PE 前缀+回调分支=消费非通道行为修改」可辩护，与 P29 RC 先例 + 前置计划 D3 授权一致；E3 原子性积分先扣+超时取消任务正确 in-scope 非 deferred）。单结果面、Phase 1 设计决策阶段正确关闭 owner doc 设计缺口、anti-slacking/API 测试规则/Required Skill 全合规。Minors（执行细节）：exchangeCombo maxPerUser 计数应含 AWAITING_PAYMENT、纯积分单 payStatus null 语义、路由不变式 lowercase hex 显式化——均不阻塞。

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs are aligned（marketing-and-promotions.md 组合兑换设计落地）
- [x] verification has run（`mvn install` + `mvn test` 全绿 + 前端渲染冒烟）
- [x] 新增 `@BizMutation`（exchangeCombo/cancelExchangeOrder 扩展）经 `IGraphQLEngine`；`@BizAction`（confirmExchangePaidByNotify）经 `I*Biz` 接口测试
- [x] no in-scope item downgraded to deferred/follow-up（E3 超时取消任务为本计划交付项，非 deferred；E5 BalancePayChannel 为合法 adjudicated split）
- [x] plan audit passed before implementation
- [x] Protected Area（支付 E4）裁定记录，授权来源明确
- [x] each phase has `Required Skill` listed
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [x] text consistency verified: status/phases/gates/log 一致
- [x] closure audit was performed by a different agent/session
- [x] closure evidence exists in files
- [x] P27/P29 Deferred「积分+现金组合兑换」在本计划 closure 后于源计划标注「已由 successor 关闭」

## Deferred But Adjudicated

### BalancePayChannel 策略 bean

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 组合兑换余额分量走「直接 debitBalance」路径（与 P30 `payByBalance` 一致），不经 PayChannel 策略；当前不存在 `BalancePayChannel` bean（grep 零命中）。统一余额通道策略为多场景复用的独立基建项，不阻塞组合兑换闭环。
- Successor Required: `yes`（触发条件：余额需作为统一 PayChannel 策略被兑换/订单/充值等多处复用，且要求通道列表一致呈现时，建 BalancePayChannel bean + 收编 payByBalance 逻辑）

## Closure

<!-- 闭合审计必须由独立 subagent 执行，勿自行填写。 -->

Status Note: 三 Phase 全部完成。积分+现金组合兑换全链路已落地：PointsGoods.cashPrice（propId 15）+ PointsExchangeOrder payStatus/payChannel/cashPrice/walletPayAmount（propId 21-24）+ exchange-status `AWAITING_PAYMENT=5`；`exchangeCombo`(@BizMutation) 建单 AWAITING_PAYMENT + 积分先扣 + 派生 PE outTradeNo；`payComboByBalance`/`payComboByChannel`(@BizMutation) 现金分量；`confirmExchangePaidByNotify`/`refundExchangeOrderByNotify`(@BizAction) 回调推进/退款对账；`cancelExpiredExchangeOrders`(@BizMutation) 超时取消兜底；`cancelExchangeOrder` 扩展支持 AWAITING_PAYMENT + 已付现金退款。PaymentCallbackImpl 新增 PE 前缀分支（onPaymentSuccess:78-81 + onRefundSuccess:117-122），路由不变式保持（order outTradeNo 32 位小写 hex 与大写 PE/RC 互斥）。app-mall-service **470 测试全绿**（含 `TestLitemallPointsExchangeComboBizModel` 15 新增场景 + 既有 12 纯积分测试零回归）；`mvn clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS。owner doc `marketing-and-promotions.md` 组合兑换设计落地；`docs/logs/2026/06-29.md` 三 Phase 记录。源 P27/P29 Deferred「积分+现金组合兑换」已于源计划 `2026-06-29-0900-1:173` + roadmap `enhanced-features-roadmap.md:585` 标 Successor Closed。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure auditor subagent（fresh session，非实施 agent）
- Audit method: 全量 re-verify Exit Criteria + Closure Gates 对 live repo 证据，不信任 plan 自身 `[x]` 标记
- Evidence:
  - **后端代码核验：** `LitemallPointsExchangeOrderBizModel.java` 含 `exchangeCombo`(:386)、`confirmExchangePaidByNotify`(:633)、`refundExchangeOrderByNotify`(:660)、`cancelExpiredExchangeOrders`(:696)；`ILitemallPointsExchangeOrderBiz` 接口声明齐备（exchangeCombo:53/confirmExchangePaidByNotify:82/refundExchangeOrderByNotify:91/cancelExpiredExchangeOrders:100）；`PaymentCallbackImpl.onPaymentSuccess` PE 分支(:78-81) + `onRefundSuccess` PE 分支(:117-122) 路由不变式保持；`MallJobInvoker.cancelExpiredExchangeOrders`(:109) + `scheduler.yaml`(:89) `cancel-expired-exchange-orders` job 注册；`LitemallPointsExchangeOrderMapper.updateStatusIfMatch` 原子状态守卫。
  - **ORM 模型核验：** `model/app-mall.orm.xml` PointsGoods `cashPrice`(propId 15, :2243) + PointsExchangeOrder `payStatus`/`payChannel`/`cashPrice`/`walletPayAmount`(propId 21-24, :2300-2306) + exchange-status dict `AWAITING_PAYMENT=5`(:159)。
  - **前端核验：** `points-mall.page.yaml` 兑换分流 `exchangeCombo`(:148)；`my-exchange-orders.page.yaml` 余额支付入口 `payComboByBalance`(:98)；admin `LitemallPointsGoods.view.xml` cashPrice col/cell(:12/34/47)。
  - **测试核验：** `TestLitemallPointsExchangeComboBizModel` 15 @Test 方法（建单/余额支付/凭证/余额不足/通道禁用/回调推进/幂等/PE 路由/路由不变式/AWAITING 取消/已付取消退余额/超时取消/maxPerUser 含 AWAITING/纯积分零回归）；`@BizAction` 经 `ILitemallPointsExchangeOrderBiz` + runInSession 调用先例沿用。app-mall-service 全量 `grep @Test | wc -l` = 470 与 plan/log 口径一致。
  - **owner doc 核验：** `marketing-and-promotions.md` 组合兑换设计段（E1-E5 Decisions + 状态机 + 原子性 E3 + 取消退款路径 + outTradeNo PE 路由 E4 + 超时取消 :259/277/287）。
  - **日志核验：** `docs/logs/2026/06-29.md:1-28` 三 Phase 详细记录 + 470 测试 known-good baseline。
  - **源 deferred 关闭标记核验：** 源计划 `2026-06-29-0900-1-points-mall-exchange-plan.md:173` Successor Closed 标注（含实施摘要）；`docs/backlog/enhanced-features-roadmap.md:585` P27/P29 Deferred 闭环说明（不改 Phase Status——deferred successor，P27/P29 保持 done，与 Non-Goal 一致）。
  - **Deferred honesty：** BalancePayChannel（out-of-scope improvement，Successor Required: yes + 明确触发条件「余额需作为统一 PayChannel 策略被兑换/订单/充值等多处复用」）为合法 adjudicated split，非遗漏 in-scope 缺陷；无其他 in-scope 缺项被隐藏到 Deferred/Follow-up。
  - **Anti-Hollow：** 所有新增方法均在 PaymentCallbackImpl 路由/scheduler job/前端 page.yaml 被实际调用，非死代码；exchangeCombo/cancelExpiredExchangeOrders 经 IGraphQLEngine + I*Biz 测试验证运行时行为。
  - **文本一致性：** Plan Status(completed) / 三 Phase Status(completed) / 三 Phase Exit Criteria(全 [x]) / Closure Gates(全 [x]) / log（470 全绿）全一致。
- Verdict: PASS — 三 Phase Exit Criteria 全部对齐 live repo 证据，无 hollow 实现，无 in-scope 缺项被降级为 deferred/follow-up（E3 超时取消任务为本计划交付项已落地；E5 BalancePayChannel 为合法 successor split），Protected Area E4 裁定可辩护，源 P27/P29 Deferred 已闭环标注。可闭合。

Follow-up:

- BalancePayChannel 统一通道策略 bean（adjudicated successor，触发条件：余额需作为统一 PayChannel 策略被兑换/订单/充值等多处复用，且要求通道列表一致呈现时）
