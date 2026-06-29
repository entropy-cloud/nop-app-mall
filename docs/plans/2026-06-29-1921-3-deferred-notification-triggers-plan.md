# 业务事件通知触发补全

> Plan Status: completed
> Mission: mall
> Work Item: deferred-notification-triggers（关闭两份源计划 deferred「过期预警站内信」+「自提核销成功通知」）
> Last Reviewed: 2026-06-29
> Source: `docs/backlog/enhanced-features-roadmap.md` §35（站内信/消息中心，已 done）；两份源计划 Deferred But Adjudicated：
>   - `docs/plans/2026-06-29-1200-1-points-validity-auto-expiry-plan.md:153`（过期预警站内信，触发条件「运营要求积分过期前主动提醒用户时，接线 MallNotificationService.sendUserMessage + 扫描近 N 天到期批次」）
>   - `docs/plans/2026-06-28-0530-3-phase31-pickup-delivery-plan.md:249`（自提核销成功触发站内信，触发条件「P35 done 且运营要求核销成功通知用户时，接线 MallNotificationService.sendUserMessage」）
> Related: `docs/plans/2026-06-28-0530-1-phase35-message-center-plan.md`（P35 站内信中心，sendUserMessage + isEventMessageEnabled 基座，已 done——本计划前置条件已满足）
> Audit: required

## Current Baseline

- **P35 站内信中心已 done（前置条件满足）。** `MallNotificationService.sendUserMessage(userId, msgType, title, content)`（`MallNotificationService.java:125`）已落地：系统上下文写 UserMessage，失败 LOG 且吞（站内信为旁路，不回滚核心事务）。`isEventMessageEnabled(eventKey, context)`（`:106`）提供事件开关门控（默认 enabled，config key=`mall_message_event_enabled_{eventKey}`）。msg-type 字典（`model/app-mall.orm.xml:122`）：ORDER=0 / MARKETING=10 / SYSTEM=20。
- **积分过期前置预警未推送（确认的 gap）。** `LitemallPointsAccountBizModel.expirePoints`（`:193`，`@BizMutation`）在批次过期**之后**扣减余额写 EXPIRE 流水，**不主动通知用户**。`getMyPointsExpiryHint`（`:260`，`@BizQuery`）为**拉取式**前端提示（my-points 页「即将过期」alert），**非主动推送**。源计划 deferred 明确要求「过期前主动提醒」——需扫描近 N 天到期批次并推站内信。
- **自提核销成功未通知用户（确认的 gap）。** `LitemallOrderBizModel.verifyPickupOrder`（`:1061`，`@BizMutation @Auth(roles="admin")`）核销成功后推进 401 + 写 pickupTime + 送积分（`earnPointsForOrderConfirm`），**不主动发站内信**——核销结果仅由订单详情/后台反馈呈现。源计划 deferred 明确要求「核销成功通知用户」。
- **调度基座成熟，可复用。** `scheduler.yaml`（9 个 job 已注册）+ `MallJobInvoker`（`new ServiceContextImpl()` → 调 biz method → LOG.info）模式成熟。积分过期提醒适合新增一个每日 job（参照 `dispatch-birthday-coupons` 每日 86400000ms 模式）。
- **事件通知接线先例成熟。** 订单支付/发货/退款/团购失败/拼团失败均已经 `txn().afterCommit` + `isEventMessageEnabled` 接线 `MallNotificationService`（见 `LitemallOrderBizModel`/`LitemallAftersaleBizModel` 多处）。本计划为同模式扩展，无新基座。

## Goals

- **积分过期前置预警推送：** 每日扫描近 N 天（可配置）即将过期的积分批次，向对应用户主动推站内信提醒（从「拉取式 hint」升级为「主动推送 reminder」）。
- **自提核销成功通知：** 门店核销成功后主动向用户推站内信（订单消息），与既有支付/发货/退款通知同模式。
- 关闭两份源计划的 deferred 条目。

## Non-Goals

- 过期**之后**的通知（expirePoints 已扣减余额，UserMessage 流水由前端 my-points 页「积分明细」呈现，不需重复推送；本计划只做**过期前**预警）。
- SMS/Email 多渠道推送（源 deferred 仅指站内信；SMS/Email 通道 `nop-integration` 未引入，为独立基建 successor）。
- 站内信中心 UI/未读徽章/消息列表（P35 已交付，本计划不改动消息中心前台）。
- 其他业务事件的站内信补全（如优惠券即将过期、拼团成功、评价奖励等到货通知——非两份源 deferred 范围，各自 successor）。

## Task Route

- Type: `implementation-only change`（业务设计已在 `system-configuration.md` 消息中心章节 + `wallet-and-assets.md` 积分有效期章节 + `order-and-cart.md` 自提核销章节覆盖；本计划为 deferred 的执行 slice，接线既有 sendUserMessage 基座）
- Owner Docs: `docs/design/system-configuration.md`（站内信/消息中心章节，事件→msgType 映射）、`docs/design/wallet-and-assets.md`（积分有效期「过期预警」）、`docs/design/order-and-cart.md`（自提核销「核销成功通知」）
- Skill Selection Basis: 后端 BizModel + 调度 job 接线 → `nop-backend-dev` + `nop-testing`；无 ORM/前端改动

## Infrastructure And Config Prereqs

- No infra prereqs beyond existing baseline（P35 站内信 + nop-job-local 已就绪；无新外部服务、无端口/密钥）。
- 无 Protected Area 触发（不动 ORM、不动支付/合规）。
- 新增 config key：`mall_points_expiry_remind_days`（预警提前天数，默认 3）、`mall_message_event_enabled_points_expiry_remind`（事件开关）、`mall_message_event_enabled_pickup_verify`（事件开关）。

## Decision Points (to resolve in-phase)

- **D1 — 预警 msgType：** 抉择 **SYSTEM(20)**（积分过期为账户状态提醒，非营销促活）。备选 MARKETING(10)（驱动消费）。残留风险：若运营希望计入营销消息分类则改 MARKETING（config 不变，仅 msgType 值）。
- **D2 — 预警频率与幂等：** 抉择 **每日一次、每用户至多一条**（job 每日 86400000ms，扫描窗口内批次，按 userId 去重发一条聚合消息「您有 N 积分将于 X 月 X 日过期」）。幂等机制：在 `LitemallPointsAccountBizModel` 注入 `ILitemallUserMessageBiz`（非 private），按 `userId + msgType(SYSTEM) + title(积分即将过期) + addTime(today)` 查询当日是否已存在同标题站内信，存在则跳过，避免 job 重跑或时区抖动重复推送（addTime domain=`createTime`，dateTimeBetween 在 xmeta 默认白名单内）。备选：每批次一条（消息噪音大）。
- **D3 — 预警窗口查询机制：** 现有 `ILitemallPointsExpireBatchBiz` 仅有 `findExpiredBatches`(expireTime≤now)、`findExpirableBatchesForUser`(单用户)、`findSoonestNonExpiredForUser`(单用户 limit 1)，**无跨用户窗口查询**。新增方法 `findBatchesExpiringWithin(int days, int limit, IServiceContext context)` 于 `ILitemallPointsExpireBatchBiz` + `LitemallPointsExpireBatchBizModel`，用 `doFindListByQueryDirectly` + `FilterBeans.ge/le`(expireTime 在 now~now+days) + `gt`(remainingPoints>0)，镜像 `findExpiredBatches`（`LitemallPointsExpireBatchBizModel.java:40-47`）模板。提前天数读 `mall_points_expiry_remind_days` config，缺省 3。
- **D4 — 核销通知触发点与 msgType：** 在 `verifyPickupOrder` 核销成功（非幂等跳过分支）后，推送 **ORDER(0)** 站内信「订单 {orderSn} 已核销成功」（与支付/发货通知同 msgType）。沿用既有 afterCommit 模式：`isEventMessageEnabled("pickup_verify", context)` 关闭时 userId 解析为 null，由 `sendUserMessage` 内部 null-guard 跳过（与 `LitemallOrderBizModel:817-819/1014-1016` 支付/发货通知同模式）。幂等跳过分支（alreadyVerified）不推送。事件开关 `mall_message_event_enabled_pickup_verify`。

## Execution Plan

### Phase 1 - 积分过期前置预警推送（调度 job + 站内信）

Status: completed
Targets: `app-mall-service/.../LitemallPointsAccountBizModel.java`、`app-mall-api/.../ILitemallPointsAccountBiz.java`、`app-mall-dao/.../ILitemallPointsExpireBatchBiz.java`、`app-mall-service/.../LitemallPointsExpireBatchBizModel.java`、`app-mall-service/.../scheduler/MallJobInvoker.java`、`app-mall-app/.../scheduler.yaml`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Decision`
- Prereqs: P35 站内信中心 done（已满足）

- [x] **Skill loading gate:** 扫描 available skills，加载 `nop-backend-dev` + `nop-testing`。读完 routing table 必读文档（含调度 job 接线、跨实体调用 IBiz-first、`@BizMutation` 事务边界、错误处理）。列已读文档路径如下。每写完一个方法用 skill selfcheck 校验（`@Inject` 非 private、NopException+ErrorCode、CoreMetrics、无 `@Transactional` 叠加等）。
  - Docs read: `nop-backend-dev` skill（routing table + 反模式表）、`nop-testing` skill（JunitBaseTestCase + IGraphQLEngine 模式）、`../nop-entropy/docs-for-ai/02-core-guides/service-layer.md`（跨实体 IBiz-first）、`../nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`（doFindListByQueryDirectly 模板）。已对照 `MallNotificationService`/`LitemallOrderBizModel:817-819` afterCommit + isEventMessageEnabled 模式实现。
- [x] **Decision D1/D2/D3：** 确认 SYSTEM msgType + 每日每用户一条聚合 + 新增 `findBatchesExpiringWithin` 窗口查询 + remindDays config 缺省 3 + 幂等查 UserMessage，记录备选与残留风险。
- [x] **Add（窗口查询）：** `ILitemallPointsExpireBatchBiz` + `LitemallPointsExpireBatchBizModel` 新增 `findBatchesExpiringWithin(int days, int limit, IServiceContext context)`：`doFindListByQueryDirectly` + `FilterBeans.ge/le`(expireTime now~now+days) + `gt`(remainingPoints>0)，镜像 `findExpiredBatches`（`:40-47`）模板。
- [x] **Add（注入）：** `LitemallPointsAccountBizModel` 注入 `MallNotificationService` + `ILitemallUserMessageBiz`（均非 private，IBiz-first）。
- [x] **Add（预警方法）：** `LitemallPointsAccountBizModel` 新增 `sendPointsExpiryReminders(IServiceContext context)` `@BizMutation` + `ILitemallPointsAccountBiz` 接口声明（镜像 `expirePoints:191-193` + `MallJobInvoker:124` 经接口调用模式）：读 `mall_points_expiry_remind_days`（缺省 3）→ 调 `findBatchesExpiryWithin` → 按 userId 聚合（Σ points + 最早 expireTime）→ 幂等检查（`userMessageBiz` 查当日同标题站内信存在则跳过）→ `notificationService.sendUserMessage(userId, SYSTEM, "积分即将过期", "您有 N 积分将于 X 月 X 日过期，请尽快使用")`。
- [x] **Add（job）：** `MallJobInvoker.sendPointsExpiryReminders()`（参照 `dispatchBirthdayCoupons` 模式：`new ServiceContextImpl()` → `pointsAccountBiz.sendPointsExpiryReminders(context)` → LOG.info）。
- [x] **Add（scheduler）：** `scheduler.yaml` 注册 `send-points-expiry-reminders` job（repeatInterval=86400000，参照 birthday job）。
- [x] **Proof：** `sendPointsExpiryReminders` 经 `IGraphQLEngine`（`@BizMutation` 经 `LitemallPointsAccount__sendPointsExpiryReminders`）测试：(a) 窗口内批次→对应用户收到一条聚合站内信；(b) 窗口外/remainingPoints=0 不发；(c) 同用户多批次聚合为一条；(d) 幂等（重跑当日不重复发）；(e) 事件开关关闭不发。

Exit Criteria:

- [x] `findBatchesExpiringWithin` + `sendPointsExpiryReminders` 落地 + scheduler job 注册 + MallNotificationService 接线
- [x] 窗口内用户收到聚合预警站内信；窗口外/已耗尽批次不发；幂等不重复
- [x] **API 测试：** `sendPointsExpiryReminders` 经 `IGraphQLEngine` 验证（`@BizMutation`）；`findBatchesExpiringWithin` 经 `I*Biz` 接口或传递覆盖
- [x] `docs/logs/` 更新

### Phase 2 - 自提核销成功通知（事件 hook）

Status: completed
Targets: `app-mall-service/.../LitemallOrderBizModel.java`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Decision`
- Prereqs: P35 done（已满足；与 Phase 1 互不依赖，可并行）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`（同 Phase 1，已读文档沿用，列出新增必读篇如有）。每方法 selfcheck。
  - Docs read: 沿用 Phase 1 必读篇；额外对照 `LitemallOrderBizModel.markOrderPaidCore:814-823` + `ship:1017-1019` 的 afterCommit + isEventMessageEnabled + uid-null-skip 模式（D4 抉择依据）。
- [x] **Decision D4：** 确认 ORDER msgType + 核销成功分支推送 + alreadyVerified 幂等分支不推送 + 沿用 afterCommit + userId-null-skip 模式，记录备选。
- [x] **Add：** `verifyPickupOrder`（`:1061`）在核销成功分支（`updateEntity` 之后、return 之前）增加 afterCommit 推送，沿用 `:817-819/1014-1016` 支付/发货通知模式：`String uid = notificationService.isEventMessageEnabled("pickup_verify", context) ? order.getUserId() : null; final String sn = order.getOrderSn(); txn().afterCommit(null, () -> notificationService.sendUserMessage(uid, ORDER_MSG_TYPE, "订单核销成功", "订单 " + sn + " 已核销成功"));`（开关关闭时 uid=null，sendUserMessage 内部 null-guard 跳过）。幂等跳过分支（alreadyVerified=true return）**不**推送。
- [x] **Proof：** `verifyPickupOrder` 经 `IGraphQLEngine`（`JunitAutoTestCase`，调 `LitemallOrder__verifyPickupOrder` mutation）：(a) 核销成功→用户收到 ORDER 站内信；(b) 重复核销（幂等跳过）不重复推送；(c) 事件开关关闭不发（uid=null）；(d) 非自提/状态不符拒绝路径不发。

Exit Criteria:

- [x] `verifyPickupOrder` 核销成功推送 ORDER 站内信；幂等跳过/拒绝分支不推送
- [x] **API 测试：** `verifyPickupOrder` 经 `IGraphQLEngine` 验证通知触发 + 幂等不重复
- [x] owner doc 更新（`system-configuration.md` 事件→msgType 映射 + `order-and-cart.md` 自提核销通知 + `wallet-and-assets.md` 过期预警）
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed（consensus）
- Auditor / Agent: 独立 subagent（fresh session）
- Round 1（`ses_0ecd9bc4cffeNOKKNJlhAk7Qfe`）：`revise`（无 blocker，2 majors）— M1 窗口查询机制未点名（须新增 `findBatchesExpiringWithin` IBiz 方法）；M2 幂等查询机制未点名（须注入 `ILitemallUserMessageBiz`）。已全部修订：D2/D3 明确 `findBatchesExpiringWithin(days,limit,context)` 新方法（镜像 findExpiredBatches）+ `@Inject ILitemallUserMessageBiz` 幂等查询（addTime dateTimeBetween）；Phase 1 Targets + Add 项列明两机制；m1-m4 同修（Phase 2 prereq 改 P35 done、验证命令改 ./mvnw、D4 沿用 afterCommit+uid-null-skip 模式、sendPointsExpiryReminders 确定 @BizMutation+接口声明）。
- Round 2（`ses_0eccd9cabffeoFZ6kkwLB4A54v`）：`consensus` — M1/M2/m1-m4 逐项核验 ADDRESSED（live repo 对照：ILitemallPointsExpireBatchBiz 无窗口查询、LitemallPointsAccountBizModel 未注入 userMessageBiz/NotificationService、project-context 用 ./mvnw、afterCommit 模式 :817-819 一致）；捆绑两 deferred 为一结果面（同 sendUserMessage 基座 + afterCommit 模式 + 同 owner doc）判定合理；API 测试 #15 合规；无 ORM/Protected Area 触发；anti-slacking 合规。未引入新 blocker/major（仅 m5 cosmetic Targets 行已补 ILitemallPointsAccountBiz.java）。consensus 达成，可进入实施。

## Closure Gates

- [x] in-scope behavior is complete（积分过期预警推送 + 自提核销成功通知）
- [x] relevant docs are aligned（`system-configuration.md` / `wallet-and-assets.md` / `order-and-cart.md`）
- [x] verification has run（`./mvnw clean package -DskipTests` + `./mvnw test`，全绿）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（`sendPointsExpiryReminders` / `verifyPickupOrder` 扩展）；`findBatchesExpiringWithin` 窗口查询经 `I*Biz` 接口传递覆盖
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop-platform phase 不写 `none` 无 justify
- [x] skill loading verification: 各 phase 扫描/加载/读必读文档/selfcheck（路径列于 skill loading gate）
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files
- [x] 两份源 deferred（points-validity `2026-06-29-1200-1:153` + pickup `2026-06-28-0530-3:249`）在本计划 closure 后于各自源计划标注「已由 successor 关闭」

## Deferred But Adjudicated

### SMS/Email 多渠道积分过期预警

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 源 deferred 仅指站内信；SMS/Email 通道 `nop-integration` 未引入，为独立基建 successor。
- Successor Required: `yes`（触发条件：引入 `nop-integration` SMS/Email 通道且运营要求多渠道预警时，扩展 `MallNotificationService.sendSms` 路径）

### 其他业务事件站内信补全（优惠券即将过期/拼团成功/评价奖励等）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划仅覆盖两份源 deferred（积分过期预警 + 自提核销）。其他事件通知为各自特性的 successor，非本计划结果面。
- Successor Required: `yes`（触发条件：各特性要求事件→站内信接线时，按本计划同模式扩展）

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 全量交付。两份源 deferred（积分过期预警 + 自提核销成功通知）已闭环：Phase 1 落地 `sendPointsExpiryReminders` 每日 job + 聚合 SYSTEM 站内信 + remindDays 配置 + 幂等；Phase 2 落地 `verifyPickupOrder` 核销成功 ORDER 站内信 + 事件开关 + 幂等跳过分支不推送。510 测试全绿（502 service + 8 web，+8 新增）。

Closure Audit Evidence:

- Reviewer / Agent: 独立 subagent（fresh session `ses_0ec5b5d23ffedoLmLGPIZJi2bj`，非实施 agent）
- Evidence: PASS verdict（0 blocker / 0 major）。逐项核验 live repo：Phase 1 七处实现（窗口查询/注入/预警方法/job/scheduler）+ Phase 2 三处实现（afterCommit/uid-null-skip/幂等跳过分支不推送）均落地；Nop 反模式表全清（无 @Inject private、无 @Transactional 叠加、CoreMetrics 一致、IBiz 接口先于 impl 声明）；测试覆盖 8 新增用例（5 points + 3 pickup，均经 IGraphQLEngine mutation）；owner docs 三处对齐；两份源 deferred 已标注 Successor Closed；plan 一致性（Plan Status=completed / Phases=completed / items 全 tick）。MINOR：日志测试数描述已修正（502 service + 8 web = 510 总）。

Follow-up:

- SMS/Email 多渠道、其他事件站内信见 Deferred But Adjudicated（含触发条件）。
