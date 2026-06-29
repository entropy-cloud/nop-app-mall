# Pickup Delivery Auto-Timeout Cancel/Refund

> Plan Status: completed
> Mission: mall
> Work Item: pickup-auto-timeout（关闭 P31 deferred「已支付未自提订单自动超时取消/退款」）
> Last Reviewed: 2026-06-29
> Source: `docs/backlog/enhanced-features-roadmap.md` §31（P31 done）；`docs/plans/2026-06-28-0530-3-phase31-pickup-delivery-plan.md:243` → `Deferred But Adjudicated → 已支付未自提订单自动超时取消/退款`
> Related: `docs/plans/2026-06-28-0530-3-phase31-pickup-delivery-plan.md`（P31 配送方式扩展/自提，已 done——本计划为其 deferred successor）
> Audit: required

## Current Baseline

> 实读 live repo 所得，非记忆。

**自提订单生命周期缺口（确认的 gap）：**

- 自提订单（`deliveryType=PICKUP`）支付后合法停留在 `orderStatus=201`（已支付/待自提），等待门店 `verifyPickupOrder` 核销推进到 401。**已支付但长期未自提的订单无自动超时取消/退款路径**——由运营在订单运营工作台人工处理（`order-and-cart.md:283` 显式记录此残留风险）。
- 既有超时调度只覆盖两种场景：
  - `cancelExpiredOrders`（`LitemallOrderBizModel.java:2290`）——仅处理 `orderStatus=101`（待支付）， unpaid 超时 → `AUTO_CANCEL(103)`，restore stock + return coupon/points/promotion usage。**不触及已支付订单，不退款。**
  - `confirmExpiredOrders`（`:2350`）——仅处理 `orderStatus=301`（已发货/SHIPPED），超时 → `AUTO_CONFIRM(402)`。
- 自提订单（201 + PICKUP）落在两个既有超时调度之间：既非待支付（不进 cancelExpiredOrders），也非已发货（不进 confirmExpiredOrders）。`getOverdueUnshippedOrders`（`:2854`）显式 Java post-filter 排除 PICKUP（`:2876`），故也不污染逾期未发货列表。

**退款基建已就绪（复用对象）：**

- `payService.refund(PayRefundRequestBean)`（`LitemallAftersaleBizModel.java:164`）——PayService facade（MockPayServiceImpl 示例 / WxPayServiceImpl 生产），in-tx 外部退款失败则抛异常回滚。
- 退款完整副作用链（`LitemallAftersaleBizModel.refund` + `confirmReturnReceived`）：`payService.refund` → `goodsProductMapper.addStock`（还库）→ `couponUserBiz.returnCoupon`（还券）→ `returnOrderDeductedPoints`（还积分）→ `releasePromotionUsage`（释放满减参与额度）。
- `order.payChannel`（propId=38, dict `mall/pay-channel`）记录支付通道。P30 多支付通道：`PAY_CHANNEL_BALANCE`（余额支付）与 wxpay/mock 通道。

**调度基建已就绪：**

- `MallJobInvoker`（`app-mall-service/.../scheduler/MallJobInvoker.java`）含 11 个 job 方法，模式统一：`new ServiceContextImpl()` → `biz.method(params, ctx)` → `LOG.info`。
- `scheduler.yaml`（`app-mall-app/.../_vfs/nop/job/conf/scheduler.yaml`）含 10 个 job 配置（`expirePinTuans` 方法存在但未在 yaml 注册——既有不一致，非本计划范围）。
- 超时常量当前为 `MallJobInvoker` 硬编码（`ORDER_CANCEL_TIMEOUT_MINUTES=30`、`ORDER_CONFIRM_TIMEOUT_MINUTES=10080`）；`sendPointsExpiryReminders` 为 config-driven 先例（方法内读 config，invoker 不传参）。

**owner doc 已记录残留风险：**

- `order-and-cart.md:283` 第 4 项「已支付未自提订单生命周期」明确：「自动超时取消作为 successor」。
- `order-and-cart.md:322-324` 自提订单与既有作业边界：走 `pay/cancel/deleteOrder/售后/改价`，不走 `ship/confirm/confirmExpiredOrders`。

**前置条件已满足：** P31（自提核销）done、P5b（支付）done、P5c（退款/售后）done、P30（多支付通道）done。

## Goals

- 已支付未自提订单（`orderStatus=201 && deliveryType=PICKUP`）超过可配置超时阈值后，自动取消并退款。
- 退款复用既有完整副作用链（payService.refund/wallet credit-back → 还库 → 还券 → 还积分 → 释放满减参与额度 → 通知）。
- 新增定时任务（`scheduler.yaml` + `MallJobInvoker` 入口 + BizModel 方法），模式与 `cancelExpiredOrders`/`confirmExpiredOrders` 一致。
- 超时阈值经 `ILitemallSystemBiz.getConfig` 读取（config key `mall_pickup_timeout_days`），运营可调。
- 核心路径通过 `IGraphQLEngine` 测试。
- 关闭 P31 deferred 条目。

## Non-Goals

- 自提订单的逾期提醒（「即将超时」站内信）——本计划仅做超时后的取消/退款，不做前置提醒（为独立 successor）。
- 多门店独立库存回退——自提订单扣减统一商品库存，取消时还回统一库存（与 P31 baseline 一致）。
- 超时阈值的前端管理 UI——config key 经 `LitemallSystem` 表维护，后台系统配置页已有通用 CRUD，本计划不新增专属管理页。
- 已核销（401）或已售后的自提订单的超时——仅处理 201 待自提状态。

## Task Route

- Type: `implementation-only change`（业务设计已在 `order-and-cart.md` 自提核销章节覆盖；本计划补入「自动超时取消/退款」生命周期 successor 语义）
- Owner Docs: `docs/design/order-and-cart.md`（自提核销章节「已支付未自提订单生命周期」残留风险 → 更新为已实现）、`docs/design/system-configuration.md`（定时任务清单新增 pickup-timeout job）
- Skill Selection Basis: 后端 BizModel 方法/错误码/调度 job 接线 → `nop-backend-dev`；`@BizMutation` 测试 → `nop-testing`（规则 #15）

## Infrastructure And Config Prereqs

- 无新外部服务/端口/密钥。
- 无 Protected Area 触发（不动 ORM、不动支付/合规；退款复用既有 `payService.refund` facade + wallet credit-back 既有路径）。
- 新增 config key：`mall_pickup_timeout_days`（自提超时天数，缺省 14）。

## Decision Points (to resolve in-phase)

- **D1 — 超时单位与缺省值：** 抉择 **天**（`mall_pickup_timeout_days`，缺省 14 天）。自提为到店履约，用户需合理到店窗口；14 天覆盖正常自提周期，过长停留指示放弃。备选小时粒度（更精细但运营心智负担大，自提非即时场景）。残留风险：缺省值需运营按实际到店率调整。
- **D2 — 退款路由（balance vs wxpay/mock）：** 自提订单经 P30 多支付通道支付，`order.payChannel` 记录通道。余额支付（`PAY_CHANNEL_BALANCE`）的退款需**原路退回钱包**（`ILitemallWalletBiz.creditBalance(userId, amount, changeType=WALLET_CHANGE_TYPE_REFUND, sourceType=refund, sourceId=orderSn, remark, ctx)`），不能只调 `payService.refund`（mock 会成功但不实际退钱包）。wxpay/mock 通道走 `payService.refund`。抉择：按 `order.payChannel` 分流——余额走 wallet credit-back，其余走 payService.refund（镜像 P30 `payByBalance` 的逆路径，参照 `LitemallPointsExchangeOrderBizModel.java:778-779` 余额退款先例）。备选：统一走 `payService.refund`（余额退款会丢失，不可接受）。残留风险：新增支付通道时需同步扩展此分流逻辑。**注：** 现有 `LitemallAftersaleBizModel.refund` 不按 `payChannel` 分流（仅调 `payService.refund`，MockPayServiceImpl 不退钱包），故本计划引入的 balance 分流为既有售后路径暂缺的路由——属安全改进而非纯复用。
- **D3 — 目标订单状态：** 取消后退款的终态。抉择 **`ORDER_STATUS_REFUND_CONFIRM(203)`**（退款完成，与售后全额退款整单的终态一致 `LitemallAftersaleBizModel:185`）。备选 `AUTO_CANCEL(103)`（语义为「支付超时取消」，但已支付订单退款不是超时取消语义）。残留风险：无（203 为退款完成的既有终态）。
- **D4 — 自动超时 job 与 verifyPickupOrder 的并发竞争：** `cancel()` 仅 CAS 匹配 `CREATED(101)`（`:700-701`），201 订单不能经 `cancel()` 取消。201 状态下自提订单的真正并发竞争者是 **`verifyPickupOrder`（201→401）** 和售后 **GOODS_MISS 退款（201→203）**。关键风险：`verifyPickupOrder`（`:1064-1108`）使用 **read-check-write 无 CAS、无乐观锁**（`LitemallOrder` 无 `versionProp`，`app-mall.orm.xml:1082-1084`），`updateEntity` 为普通 UPDATE。若自动超时 CAS 先翻转 201→203 并执行退款，随后 `verifyPickupOrder` 的 stale `updateEntity` 会覆写 203→401（但不逆转退款）→ 用户既获退款又获订单完成（double-spend）。抉择：**CAS-guard `verifyPickupOrder`**——将其 `updateEntity`（设 401）改为 `orderMapper.updateStatusIfMatch(orderId, 401, 201)` CAS 守卫；若 CAS 败（已被超时 job 翻转），reload 查看状态为 203，抛 `ERR_PICKUP_ORDER_NOT_VERIFIABLE`（与既有守卫一致）。备选：接受竞争窗口（极窄但 double-spend 不可接受）。残留风险：改动既有 `verifyPickupOrder` 需同步更新其测试。

## Execution Plan

### Phase 1 - 自动超时取消/退款后端

Status: completed
Targets: `app-mall-service/.../entity/LitemallOrderBizModel.java`、`app-mall-api/.../ILitemallOrderBiz.java`、`app-mall-service/.../scheduler/MallJobInvoker.java`、`app-mall-app/.../scheduler.yaml`、`app-mall-service/.../AppMallErrors.java`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Decision`
- Prereqs: P31 done、P30 done（余额退款路径依赖 P30 wallet credit-back）

- [x] **Skill loading gate:** 扫描 available skills，加载 `nop-backend-dev` + `nop-testing`。读完 routing table 必读文档（含调度 job 接线、跨实体调用 IBiz-first、`@BizMutation` 事务边界、错误处理、bizmodel-method-selfcheck）。列已读文档路径如下。每写完一个方法用 skill selfcheck 校验（`@Inject` 非 private、NopException+ErrorCode、CoreMetrics、无 `@Transactional` 叠加、IBiz 接口先于 impl 声明等）。
  - Docs read: `nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`、`nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`、`nop-entropy/docs-for-ai/05-examples/ibiz-and-bizmodel.java`、`nop-entropy/docs-for-ai/02-core-guides/service-layer.md`、`nop-entropy/docs-for-ai/02-core-guides/error-handling.md`
- [x] **Decision D1/D2/D3/D4：** 确认超时单位（天/缺省 14）+ 退款路由（balance→wallet credit-back 含 changeType=REFUND / 其余→payService.refund）+ 目标状态（REFUND_CONFIRM 203）+ verifyPickupOrder CAS-guard 并发修复，记录备选与残留风险。
- [x] **Add（查询）：** `ILitemallOrderBiz` + `LitemallOrderBizModel` 新增 `cancelExpiredPickupOrders(int timeoutDays, IServiceContext context)` `@BizMutation`（参照 `cancelExpiredOrders:2290` 签名，timeoutDays 由 invoker 传入）：`CoreMetrics.currentDateTime().minusDays(timeoutDays)` cutoff → `QueryBean` filter `orderStatus==201 && deliveryType==PICKUP && payTime<=cutoff`（`deliveryType` eq 查询算子受 xmeta 支持，`:2870` 注明 eq/in/dateBetween 均可用，直接 filter）→ `doFindListByQueryDirectly` → limit 100 → collect IDs → **`dao().clearEntitySessionCache()`**（参照 `:2310`——conditional UPDATE on session-managed entity 会被路由为 in-memory 报 0）→ 逐单 CAS 守卫 + 退款编排。
- [x] **Add（退款编排）：** `cancelExpiredPickupOrders` 内逐单：CAS `orderMapper.updateStatusIfMatch(orderId, REFUND_CONFIRM, PAY)`（并发守卫，败方 skip）→ 按 `order.payChannel` 分流退款（D2：余额走 `walletBiz.creditBalance(userId, actualPrice, WALLET_CHANGE_TYPE_REFUND, sourceType=refund, sourceId=orderSn, remark, ctx)`；其余走 `payService.refund(PayRefundRequestBean)`，totalFee=actualPrice, refundFee=actualPrice）→ 还库 `goodsProductMapper.addStock` → 还券 `couponUserBiz.returnCoupon` → 还积分 `returnDeductedPoints` → 释放满减参与额度 `releasePromotionUsage` → set endTime → `updateEntity` → **通知**（`txn().afterCommit` 推送 ORDER 站内信「订单超时取消并退款」，参照 `LitemallAftersaleBizModel:236` 退款通知 + `isEventMessageEnabled("pickup_timeout", ctx)` 事件开关）+ `logManager.logOrderSucceed("自提订单超时自动取消退款")`。
- [x] **Add（config 读取）：** config key `mall_pickup_timeout_days` 读取在 invoker 层（`MallJobInvoker.cancelExpiredPickupOrders` 内读 `systemBiz.getConfig`，参照 `sendPointsExpiryReminders` 模式），BizModel 方法接收显式 `timeoutDays` 参数（参照 `cancelExpiredOrders` 签名，便于 API 直调与测试）。
- [x] **Fix（CAS-guard verifyPickupOrder — D4 并发修复）：** 将 `verifyPickupOrder`（`:1108`）的 `updateEntity(order, "verifyPickupOrder", context)` 改为 CAS `orderMapper.updateStatusIfMatch(orderId, 401, 201)`：CAS 胜→继续既有副作用（积分/pickupTime/通知）；CAS 败→reload 查 `orderStatus`，若为 203（已被超时退款）抛 `ERR_PICKUP_ORDER_NOT_VERIFIABLE`（与既有状态守卫一致）。同步更新 `TestLitemallPickupDeliveryBizModel` 既有用例。
- [x] **Add（job 入口）：** `MallJobInvoker.cancelExpiredPickupOrders()`：读 `mall_pickup_timeout_days`（缺省 14）→ `new ServiceContextImpl()` → `orderBiz.cancelExpiredPickupOrders(timeoutDays, context)` → `LOG.info`（参照 `cancelExpiredOrders:55-59` invoker 模式 + `sendPointsExpiryReminders:132` config 读取模式）。
- [x] **Add（scheduler）：** `scheduler.yaml` 注册 `cancel-expired-pickup-orders` job（repeatInterval=3600000 每小时，参照 confirm-expired-orders）。
- [x] **Add（ErrorCode）：** 如退款失败需新增 ErrorCode（`ERR_PICKUP_AUTO_CANCEL_REFUND_FAILED`），NopException 抛出（参照 `ERR_AFTERSALE_REFUND_FAILED`）。
- [x] **Proof：** `TestLitemallPickupDeliveryBizModel` 新增 IGraphQLEngine 测试（`LitemallOrder__cancelExpiredPickupOrders` mutation）：(a) 超时 PICKUP 订单→退款+还库+还券+还积分+释放满减+状态 203+通知+日志；(b) 未超时 PICKUP 订单不处理；(c) EXPRESS 已支付订单不受影响；(d) 已核销(401) PICKUP 订单不处理；(e) 余额支付 PICKUP 订单→wallet credit-back（assert 钱包余额增加 **且** `payService.refund` **未**被调用）；(f) 并发幂等（CAS 守卫，重复调用 no-op）；(g) config 缺省/覆盖；(h) **D4 竞争**：verifyPickupOrder 在超时退款后 CAS 败→抛 ERR_PICKUP_ORDER_NOT_VERIFIABLE（不 double-spend）。
  - 验证命令：`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` + `./mvnw test -pl app-mall-service -am`

Exit Criteria:

- [x] 超时未自提订单（201+PICKUP）被自动取消退款到 203（REFUND_CONFIRM），退款经正确通道（balance→wallet credit-back / wxpay→payService）+ 通知 + 日志
- [x] 未超时 / EXPRESS / 已核销订单不受影响
- [x] 还库/还券/还积分/释放满减参与额度副作用链完整落地
- [x] D4 并发修复：`verifyPickupOrder` CAS-guard，超时退款后核销 CAS 败→拒绝（无 double-spend）
- [x] **API 测试：** `cancelExpiredPickupOrders` `@BizMutation` 通过 `IGraphQLEngine` 测试（`JunitAutoTestCase` 录制回放）
- [x] owner doc `order-and-cart.md` 残留风险更新（successor 已实现）+ `system-configuration.md` 定时任务清单更新
- [x] P31 deferred 条目标注「已由 successor 关闭」
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent（fresh sessions `ses_0ec411d47ffeReeYql7qfqoeX3` round-1, `ses_0ec2f5a4effeoGsQ6cYBis330i` round-2）
- Evidence: Round-1 REVISE（4 blockers: B1 config-read 矛盾 / B2 缺 clearEntitySessionCache / B3 D4 并发分析误指 cancel() / B4 Goal 承诺通知但执行缺漏 + 5 minors）→ 全部修订 → Round-2 PASS（"All four blockers and all five minors resolved. No new blockers introduced. D4 CAS-guard approach is sound. Plan is approved for implementation."）。`creditBalance` 签名精确匹配 `ILitemallWalletBiz.java:27-33`；`WALLET_CHANGE_TYPE_REFUND=20` 确认 `_AppMallDaoConstants.java:369`；`updateStatusIfMatch` CAS 201→401 可行确认 `LitemallOrderMapper.java:29-31`。

## Closure Gates

- [x] in-scope behavior is complete（`LitemallOrderBizModel.cancelExpiredPickupOrders:2434` 落地；`MallJobInvoker.cancelExpiredPickupOrders:148` 接线；`scheduler.yaml` 注册 `cancel-expired-pickup-orders` job；`verifyPickupOrder:1110-1121` CAS-guard D4 并发修复）
- [x] relevant docs are aligned（`docs/design/order-and-cart.md:283` 第 4 项从残留风险更新为 successor 已实现；`docs/design/system-configuration.md:264/271` 定时任务清单新增 `cancelExpiredPickup-orders` + 调度频率；`docs/logs/2026/06-29.md` 同步）
- [x] verification has run（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS 全 10 模块；`./mvnw test -pl app-mall-service -Dtest=TestLitemallPickupDeliveryBizModel` 22 测试 0 失败；全工作区 `./mvnw test` 519 测试 0 失败）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（`TestLitemallPickupDeliveryBizModel:502` 经 `callMutation("LitemallOrder", "cancelExpiredPickupOrders", ...)` 验证 9 场景）；本计划无 `@BizAction`
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation（Round-1 REVISE → Round-2 PASS）
- [x] each phase has `Required Skill` listed（`nop-backend-dev` + `nop-testing`），Nop-platform phases do not write `none` without justification
- [x] skill loading verification: Phase 1 加载 `nop-backend-dev` + `nop-testing`，已读 routing table 必读文档（bizmodel-method-selfcheck / safe-api-reference / ibiz-and-bizmodel / service-layer / error-handling），每写完一个方法用 selfcheck 校验无 anti-pattern
- [x] text consistency verified: top status=completed / Phase 1 status=completed / 全 Exit Criteria [x] / Closure Gates 全 [x] / `docs/logs/2026/06-29.md` 描述一致
- [x] closure audit was performed by a different agent/session than implementation（本次 closure audit 为独立 fresh session）
- [x] closure evidence exists in files（见下方 Closure Audit Evidence）

## Deferred But Adjudicated

### 自提订单即将超时提醒

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划仅做超时后的取消/退款。超时前 N 天向用户推站内信提醒（「您的自提订单即将超时取消」）为独立通知 successor，依赖通知触发编排。
- Successor Required: `yes`（触发条件：运营要求自提超时前置提醒时，参照积分过期预警模式新增前置提醒 job）

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 全 Phase 1 completed 并通过独立闭合审计。已支付未自提订单（201+PICKUP+超 cutoff）自动 CAS 翻转 203 + 按 payChannel 分流退款（balance→wallet credit-back / 其余→payService.refund）+ 复用还库/还券/还积分/释放满减/通知副作用链；每小时调度；D4 并发修复 `verifyPickupOrder` CAS-guard 防 double-spend。9 IGraphQLEngine 测试全绿（含 D4 竞争 + config 缺省/覆盖 + 余额分流），全工作区 519 测试 0 失败。owner-doc（order-and-cart/system-configuration/logs）同步；P31 deferred「已支付未自提订单自动超时取消/退款」标注 Successor Closed。可闭合。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure auditor（fresh session，非实施 agent）
- 实证核对（live repo evidence）：
  - `app-mall-service/src/main/java/app/mall/service/entity/LitemallOrderBizModel.java:2434` `cancelExpiredPickupOrders(timeoutDays, ctx)` `@BizMutation` 实现存在：`CoreMetrics.currentDateTime().minusDays(timeoutDays)` cutoff → `QueryBean` filter `orderStatus==201 && deliveryType==PICKUP && payTime<=cutoff` → `doFindListByQueryDirectly` → limit 100 → `dao().clearEntitySessionCache()` → 逐单 CAS `updateStatusIfMatch(orderId, 203, 201)` 守卫 → payChannel 分流退款 → 还库/还券/还积分/释放满减/通知/日志（与 Exit Criteria #103/#105 一致）。
  - `app-mall-dao/src/main/java/app/mall/biz/ILitemallOrderBiz.java:310` 接口声明 `cancelExpiredPickupOrders` 存在。（注：plan Targets 行误写为 `app-mall-api/.../ILitemallOrderBiz.java`，实际 IBiz 接口在 `app-mall-dao` 模块——非阻塞文档小误。）
  - `app-mall-service/src/main/java/app/mall/service/scheduler/MallJobInvoker.java:148` `cancelExpiredPickupOrders()` 入口存在，读 `mall_pickup_timeout_days` config（缺省 14）→ `new ServiceContextImpl()` → `orderBiz.cancelExpiredPickupOrders(timeoutDays, context)`。
  - `app-mall-app/src/main/resources/_vfs/nop/job/conf/scheduler.yaml:25` 注册 `cancel-expired-pickup-orders` job。
  - `app-mall-service/src/main/java/app/mall/service/AppMallErrors.java:617` `ERR_PICKUP_AUTO_CANCEL_REFUND_FAILED` ErrorCode 定义；`:604` `ERR_PICKUP_ORDER_NOT_VERIFIABLE` 存在。
  - D4 并发修复落地：`LitemallOrderBizModel.java:1116` `verifyPickupOrder` CAS `orderMapper.updateStatusIfMatch(orderId, 401, 201)`，CAS 败→reload→抛 `ERR_PICKUP_ORDER_NOT_VERIFIABLE`（`:1121`）。
  - IGraphQLEngine 测试落地：`app-mall-service/src/test/java/app/mall/service/entity/TestLitemallPickupDeliveryBizModel.java:456-718` 新增 9 场景测试（超时/未超时/EXPRESS 不影响/已核销不处理/余额 wallet credit-back `MockPayServiceImpl.setForceRefundFailure(true)` 证明不调 payService.refund + 钱包余额=actualPrice/并发幂等 CAS 守卫/config 覆盖/config 缺省/D4 竞争后 verifyPickupOrder 拒绝）。
  - 验证命令结果（`docs/logs/2026/06-29.md:19`）：`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS 全 10 模块；`./mvnw test -pl app-mall-service -Dtest=TestLitemallPickupDeliveryBizModel` 22 测试 0 失败（+9 新增）；全工作区 `./mvnw test` 519 测试 0 失败。
  - owner-doc 同步：`docs/design/order-and-cart.md:283` 第 4 项「已支付未自提订单生命周期」从残留风险更新为 successor 已实现（含 D4 CAS-guard 语义）；`docs/design/system-configuration.md:264/271` 定时任务清单新增 `自提订单超时取消退款` 行 + 调度频率行。
  - P31 deferred 闭环：`docs/plans/2026-06-28-0530-3-phase31-pickup-delivery-plan.md:247` 「已支付未自提订单自动超时取消/退款」补 `Successor Closed:` 行指向本计划（closure audit 中发现并修复——此前 P31 此条缺 Successor Closed 标注，与 Exit Criteria #109 承诺不符，已补齐）。
- 五点一致性核对：Plan Status=completed / Phase 1 Status=completed / 全 Exit Criteria [x] / 全 Closure Gates [x] / `docs/logs/2026/06-29.md` 描述一致，全部对齐。
- Anti-Hollow 核对：`cancelExpiredPickupOrders` 经 `MallJobInvoker` → `scheduler.yaml` 真接线调度（非空方法体）；退款副作用链复用既有 `payService.refund`/`walletBiz.creditBalance`/`goodsProductMapper.addStock`/`couponUserBiz.returnCoupon`/`returnDeductedPoints`/`releasePromotionUsage`；CAS 守卫非空操作；通知 `txn().afterCommit` 非空推送。
- Deferred 诚实性核对：仅一项 Deferred（自提订单即将超时提醒，out-of-scope improvement，含触发条件 successor），无 in-scope live defect 或 contract drift 隐藏。
- Verdict: **approved** — 计划可闭合。

Follow-up:

- 自提订单即将超时提醒见 Deferred But Adjudicated（含触发条件）。
