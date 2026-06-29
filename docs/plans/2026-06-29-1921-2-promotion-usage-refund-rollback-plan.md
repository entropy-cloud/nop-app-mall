# 营销参与退款额度回滚

> Plan Status: completed
> Mission: mall
> Work Item: promotion-usage-refund-rollback（关闭 P22 model-gap deferred「满减参与记录退款回滚」）
> Last Reviewed: 2026-06-29
> Source: `docs/backlog/enhanced-features-roadmap.md` §22；`docs/plans/2026-06-28-1610-1-phase22-promotion-usage-model-gap-plan.md:190`（Deferred But Adjudicated → 满减参与记录的退款回滚，Classification: optimization candidate，Successor Required: yes，触发条件「业务要求退款释放限购额度时」）
> Related: `docs/plans/2026-06-28-1610-1-phase22-promotion-usage-model-gap-plan.md`（PromotionUsage 实体 + maxPerUser 强一致 + usage 写入 origin）、`docs/plans/2026-06-27-1742-1-phase15-full-discount-promotion-plan.md`（P15 满减 origin）
> Audit: required

## Current Baseline

- **PromotionUsage 实体已落地。** `LitemallPromotionUsage`（userId/promotionActivityId/orderId/meetAmount/discountAmount/addTime，唯一键 `uk_promoUsage_user_order` (userId,orderId) 于 `model/app-mall.orm.xml:1554-1556`，索引 userId+promotionActivityId、orderId）由 `2026-06-28-1610-1` 交付，记录"参与事实"（命中即记录）。实体使用逻辑删除（`useLogicalDelete="true"` + `deleteFlagProp="deleted"`，`:1505-1506`），故 `deleteEntity` 为软删除；`maxPerUser` 守卫的 `findCount` 自动应用 `deleted=false` 过滤 → 删除即释放额度。
- **usage 在 submit 写入，退款不回滚（确认的 gap）。** `LitemallOrderBizModel.submit`（`:630-638`）在订单命中满减时写一条 PromotionUsage（同 `@BizMutation` tx）。maxPerUser 守卫（`:564-580`）按 `(userId, promotionActivityId)` 计数 **既有 usage 记录数** 判定是否超限。**当前取消/退款路径不删除/不标记该 usage 记录**，故用户取消订单或全额退款后，限购额度仍被占用（"参与过即计数"语义）。源计划明确将"退款是否回退 usage 计数（释放限购额度）"列为 deferred successor。
- **满减与 groupon/pintuan 可共存于同一订单（无互斥）。** submit 仅 groupon↔pinTuan 互斥（`:312-321`），满减 promotion 与 groupon 独立计算（`:546`/`:582`）、均可持久化。故一笔订单可同时带满减 PromotionUsage 与 groupon/pintuan。
- **全额取消/退款镜像先例共 6 处（完整盘点）：** 经 `grep returnCoupon|returnDeductedPoints|returnOrderDeductedPoints|payService.refund` 全量核验，既有「全额取消/退款 → 还券 + 还积分」路径共 6 处，本计划须在每处全额退款同步释放满减 usage（镜像）：
  1. `LitemallOrderBizModel.cancel`（`:696`，CREATED→CANCEL）：还券 `:724-727` + 还积分 `:732`。
  2. `LitemallOrderBizModel.cancelExpiredOrders`（`:2251`）：批量超时取消，镜像 cancel（还券 `:2296` + 还积分 `:2301`）。
  3. `LitemallAftersaleBizModel.refund`（`:129`）：全额退款（`isWholeOrder` 判定 `:143`）还券 `:209-216` + 还积分 `:222`；部分项级**不**还券/还积分。
  4. `LitemallAftersaleBizModel.confirmReturnReceived`（`:282`，RETURNED→REFUND）：全额退货退款（`isWholeOrder` `:292`）还券 `:339-346` + 还积分 `:347`；部分项级不还。注释 `:338` 明确「mirrors refund() whole-order branch」。
  5. `LitemallGrouponBizModel.refundGrouponOrder`（`:234`）：拼团失败/团购退款全额（refundFee=actualPrice），还券 `:284` + 还积分 `:289`。
  6. `LitemallPinTuanActivityBizModel.refundMemberOrder`（:`~340`）：拼团成员退款，还券 `:392` + 还积分 `:395`。
- **跨实体调用路径已就绪。** `promotionUsageBiz`（`ILitemallPromotionUsageBiz`）已注入 `LitemallOrderBizModel:195`（覆盖 1/2）；`LitemallAftersaleBizModel` 已注入 `orderBiz`（`ILitemallOrderBiz`，`:74`）；`LitemallGrouponBizModel`/`LitemallPinTuanActivityBizModel` 亦注入 `orderBiz`。故回滚 helper 暴露于 `ILitemallOrderBiz` 即可在 6 处复用，无 `daoProvider().daoFor()` 绕过风险。
- **PromotionUsage 无 status 字段。** 实体仅记录参与事实字段，无 `status`/`released` 标记位。回滚方式需 Decision（软删除记录 vs 加状态字段）。

## Goals

- 全额订单取消 / 全额退款（含售后 refund、售后 confirmReturnReceived 退货退款、团购失败 refundGrouponOrder、拼团 refundMemberOrder）时，释放该订单占用的满减参与额度（回滚 PromotionUsage），使用户可在 maxPerUser 限额内再次参与。
- 镜像既有还券/还积分的全部 6 处全额取消/退款先例，复用相同的「全额 vs 部分项级」边界判定，保持满减参与与优惠券/积分的退款回滚语义完全一致。
- 关闭 P22 model-gap plan 的 deferred「满减参与记录退款回滚」条目。

## Non-Goals

- 部分项级退款回滚 usage（满减为**订单整体级**折扣，基于订单商品总价门槛；部分项退款后订单仍参与了满减，参与事实成立，usage 保留——与部分项退款不还券/不还积分同口径）。
- 秒杀 usage 回滚（秒杀 `flashSaleSessionId`/maxPerUser 走 `LitemallLog` 计数 + 独立 createFlashSaleOrder 路径，非 PromotionUsage 记录——不同结果面，见 Deferred）。
- 拼团**自身的**开团/参团成员记录回滚（拼团成员记录 PinTuanMember 与满减 PromotionUsage 是不同实体；本计划只回滚该订单上可能的满减 PromotionUsage，不动拼团成员记录）。
- PromotionUsage 新增 status 字段（Decision 选软删除，无模型改动）。
- 历史已取消/已退款订单的存量 usage 回填（基线存量小，回填无可靠依据；仅对新发生的取消/退款生效）。

## Task Route

- Type: `implementation-only change`（业务设计已在 `marketing-and-promotions.md` 满减章节覆盖「已知约束」；本计划为 deferred 的执行 slice，关闭退款↔限购额度交互）
- Owner Docs: `docs/design/marketing-and-promotions.md`（满减送章节「已知约束/退款回滚」）
- Skill Selection Basis: 后端 BizModel 改动（cancel/cancelExpiredOrders/refund 三处 hook + helper） → `nop-backend-dev` + `nop-testing`；无 ORM/前端改动

## Infrastructure And Config Prereqs

- No infra prereqs beyond existing baseline（无 ORM 改动、无新外部服务、无端口/密钥）。
- 无 Protected Area 触发（不动 `model/app-mall.orm.xml`，不动支付/合规）。

## Decision Points (to resolve in-phase)

- **D1 — 回滚方式：软删除记录 vs 加 status 字段。** 抉择：**软删除 PromotionUsage 记录**（实体已 `useLogicalDelete` + `deleted` flag；`deleteEntity` 软删，`maxPerUser` 的 `findCount` 自动应用 `deleted=false` 过滤 → 额度自然释放；镜像还券 `returnCoupon` 的作废语义）。备选：新增 `released` 状态字段（需 ORM 改动 + 计数 query 加 `released=false` 过滤，复杂度高且引入模型变更）。残留风险：软删后参与历史不可追溯（但效果归因 query 按时间窗聚合有效参与，已退款订单本就不应计入）。
- **D2 — 回滚边界：全额 vs 部分项级。** 抉择：**仅全额取消/全额退款回滚**（镜像还券/还积分边界）。判定条件复用既有 `isWholeOrder`（`LitemallAftersaleBizModel:143/292`，`StringHelper.isEmpty(orderItemId)`）+ cancel/cancelExpiredOrders/groupon-refund/pintuan-refund 天然全额。部分项退款保留 usage。
- **D3 — 回滚幂等：** 软删除按 `(userId, orderId)` 唯一键 `uk_promoUsage_user_order`（`:1554`）定位，重复调用软删 0 行无副作用，天然幂等；orderId 永不复用，UK 无冲突。

## Execution Plan

### Phase 1 - PromotionUsage 回滚后端（6 处全额退款镜像）

Status: completed
Targets: `app-mall-service/.../LitemallOrderBizModel.java`、`app-mall-service/.../LitemallAftersaleBizModel.java`、`app-mall-service/.../LitemallGrouponBizModel.java`、`app-mall-service/.../LitemallPinTuanActivityBizModel.java`、`app-mall-api/.../ILitemallOrderBiz.java`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Decision`
- Prereqs: 无（PromotionUsage 实体已就绪）

- [x] **Skill loading gate:** 扫描 available skills，加载 `nop-backend-dev` + `nop-testing`。读完 routing table 标为必读的文档（含跨实体调用、错误处理、`@BizMutation` 事务边界、IBiz-first）。列已读文档路径如下。每写完一个方法用 skill selfcheck 校验（`@Inject` 非 private、NopException+ErrorCode、无 `daoProvider().daoFor()` 绕过、CoreMetrics 等）。
  - Docs read: `nop-entropy/docs-for-ai/05-examples/ibiz-and-bizmodel.java`、`nop-entropy/docs-for-ai/02-core-guides/service-layer.md`、`nop-entropy/docs-for-ai/02-core-guides/error-handling.md`、`nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`、`nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`。确认本仓「内部 helper 无 biz 注解」先例（`ILitemallPromotionActivityBiz.resolvePromotionForOrderInternal`、`ILitemallPointsAccountBiz.resolveEarnPerYuan` 等不经 GraphQL 暴露、仅供跨实体调用），`releasePromotionUsage` 据此声明为普通接口方法（selfcheck #2 的内部 helper 例外）。
- [x] **Decision D1/D2/D3：** 确认软删除 + 仅全额边界 + (userId,orderId) UK 幂等，记录备选与残留风险。D1=软删除（实体 `useLogicalDelete`+`deleted` flag，`deleteEntity` 软删，`findCount`/`findList` 自动 `deleted=false` 过滤→额度释放；备选 released 状态字段需 ORM 改动+计数过滤，复杂度高，弃）；D2=仅全额（复用 `isWholeOrder`/天然全额路径，镜像还券/还积分边界）；D3=`(userId,orderId)` UK 定位，重复软删 0 行幂等（orderId 永不复用）。
- [x] **Add：** 在 `LitemallOrderBizModel` 新增 `releasePromotionUsage(String orderId, IServiceContext context)`（**普通接口方法**，非 `@BizMutation`/`@BizQuery`/`@BizAction`——避免被当作独立 GraphQL 入口；经 `ILitemallOrderBiz` 暴露供跨实体调用）：按 `orderId` 查 PromotionUsage（`promotionUsageBiz`，复用 `ILitemallPromotionUsageBiz`），对匹配记录 `deleteEntity` 软删除（D1）。helper 放在还券/还积分同区域，注释说明镜像先例 + 全额边界 + 软删除语义。
- [x] **Add（hook 1）：** `cancel`（`:696`）在还券/还积分之后调用 `releasePromotionUsage(order.orm_idString(), context)`（同 tx；CREATED→CANCEL 天然全额）。
- [x] **Add（hook 2）：** `cancelExpiredOrders`（`:2251`）批量取消路径，在还券/还积分之后调用 `releasePromotionUsage`（镜像 cancel）。
- [x] **Add（hook 3）：** `LitemallAftersaleBizModel.refund`（`:129`）在 `isWholeOrder`（`:143`）为真、还券（`:209-216`）/还积分（`:222`）同区域调用 `orderBiz.releasePromotionUsage(order.orm_idString(), context)`。部分项退款分支不调用（D2）。
- [x] **Add（hook 4）：** `LitemallAftersaleBizModel.confirmReturnReceived`（`:282`）在 `isWholeOrder`（`:292`）为真、还券（`:339-346`）/还积分（`:347`）同区域调用 `orderBiz.releasePromotionUsage(...)`（镜像 refund 全额分支；注释 `:338` 已表明该路径 mirrors refund() whole-order branch）。部分项退货退款不调用。
- [x] **Add（hook 5）：** `LitemallGrouponBizModel.refundGrouponOrder`（`:234`）在还券（`:284`）/还积分（`:289`）之后调用 `orderBiz.releasePromotionUsage(...)`（团购失败退款全额；满减可与 groupon 共存于同一订单，故须释放）。
- [x] **Add（hook 6）：** `LitemallPinTuanActivityBizModel.refundMemberOrder`（:`~340`）在还券（`:392`）/还积分（`:395`）之后调用 `orderBiz.releasePromotionUsage(...)`（同 5）。
- [x] **Proof：** 全额取消/退款经 `IGraphQLEngine` 测试（`JunitBaseTestCase`）：(a) 命中满减下单写 usage → cancel → usage 软删 + 同活动可再次参与（`testCancelReleasesPromotionUsageAndAllowsReparticipate`）；(b) cancelExpiredOrders 批量取消释放 usage；(c) aftersale `refund` 全额退款释放 usage；(d) aftersale `confirmReturnReceived` 全额退货退款释放 usage；(e) groupon 失败退款释放共存满减 usage；(f) pintuan 成员退款释放共存满减 usage；(g) 部分项退款**不**释放 usage（计数不变）；(h) 重复取消/退款幂等（无异常，软删 0 行 no-op）；(i) 非满减订单无 PromotionUsage 可删，无副作用。

Exit Criteria:

- [x] `releasePromotionUsage` helper 落地（`ILitemallOrderBiz` 暴露），6 处全额退款 hook 全部接线
- [x] 全额取消/退款释放额度，用户可再次参与；部分项退款保留 usage
- [x] **API 测试：** cancel / aftersale refund / confirmReturnReceived / groupon / pintuan 经 `IGraphQLEngine` 验证 usage 回滚 + maxPerUser 再参与；`releasePromotionUsage` 为普通接口方法，经调用方 `@BizMutation` 的 GraphQL 测试传递覆盖（非独立 `@BizMutation`，无需独立 GraphQL 测试，符合 Rule #15）
- [x] 回滚幂等（重复调用无副作用）
- [x] owner doc 更新（`marketing-and-promotions.md` 满减「已知约束/退款回滚」）
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed（consensus）
- Auditor / Agent: 独立 subagent（fresh session）
- Round 1（`ses_0ecda139fffeNuOGy5DZyAKeX5`）：`revise` — 2 blockers（B1 `confirmReturnReceived` 全额退货退款路径漏 hook；B2 groupon/pintuan 全额退款 Goal/Non-Goal 矛盾 +「不同结果面」rationale 错误——满减可与 groupon/pintuan 共存）+ 1 major（M1 baseline 仅盘 3 处，实有 6 处镜像先例）。已全部修订：baseline 补全 6 处镜像先例盘点 + 满减↔groupon 可共存说明；Goals/Non-Goals 覆盖全部全额退款；Phase 1 扩为 6 hook（cancel/cancelExpiredOrders/aftersale.refund/aftersale.confirmReturnReceived/groupon.refundGrouponOrder/pintuan.refundMemberOrder）；D1 改软删除（实体 useLogicalDelete + findCount 自动过滤）；Deferred 重分类秒杀/拼团成员。
- Round 2（`ses_0eccdc254ffe44r3Me3MguT6aM`）：`consensus` — Round 1 B1/B2/M1 逐项核验 ADDRESSED（live repo 对照：confirmReturnReceived:282/339-347、groupon↔pintuan mutex:312-321、满减↔groupon 独立、6 处镜像先例 grep 精确 6 对）；releasePromotionUsage 为普通接口方法（Rule #15 经调用方 @BizMutation GraphQL 传递覆盖）；无 ORM/Protected Area 触发；一结果面；anti-slacking 合规。未引入新 blocker/major。consensus 达成，可进入实施。

## Closure Gates

- [x] in-scope behavior is complete（全额取消/退款回滚 PromotionUsage + 再次参与可用）
- [x] relevant docs are aligned（`marketing-and-promotions.md`）
- [x] verification has run（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` + `./mvnw test`，全绿：494 service 测试 0 失败，+9 新增）
- [x] all new/变更的 `@BizMutation` 方法 tested via `IGraphQLEngine`（cancel / aftersale refund / confirmReturnReceived / groupon refund / pintuan refund 路径全覆盖）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop-platform phase 不写 `none` 无 justify
- [x] skill loading verification: phase 扫描/加载/读必读文档/selfcheck（路径列于 skill loading gate）
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation（独立 closure-audit subagent，fresh session）
- [x] closure evidence exists in files（本节 Closure Audit Evidence + `docs/logs/2026/06-29.md` + live repo file:line 证据）
- [x] 源 deferred（`2026-06-28-1610-1:190` 满减参与记录退款回滚）在本计划 closure 后于源计划标注「已由 successor 关闭」

## Deferred But Adjudicated

### 秒杀（FlashSale）usage 回滚

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 秒杀 maxPerUser 走 `LitemallLog` 计数（`LitemallFlashSaleBizModel` REQUIRES_NEW 写 Log）+ 独立 createFlashSaleOrder 路径，非 PromotionUsage 实体——与满减 usage 回滚是不同结果面（不同计数存储）。
- Successor Required: `yes`（触发条件：秒杀要求退款释放限购额度时，评估 `LitemallLog` 计数回滚机制）

### 拼团成员记录（PinTuanMember）回滚

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划回滚的是订单上的满减 `PromotionUsage`（hook 6 已覆盖拼团退款时释放共存满减 usage）。拼团**自身的**开团/参团成员记录（PinTuanMember）是独立实体，记录团成员资格，与满减参与计数无关——拼团退款已由 `refundMemberOrder` 退款 + 标记成员失败处理，不属本结果面。
- Successor Required: `no`（触发条件：拼团要求退款后回滚成员资格记录时——当前成员记录已由 refundMemberOrder 的成员状态推进覆盖）

### PromotionUsage 软删除/状态字段（保留参与历史）

- Classification: `optimization candidate`
- Why Not Blocking Closure: D1 抉择软删除记录，软删后参与历史对运营不可见（`findCount` 自动过滤 deleted）。运营若需保留「曾参与但已退款」的历史与有效参与区分，可加 `released` 状态字段替代软删 + 计数过滤 `released=false`。
- Successor Required: `no`（触发条件：运营要求保留退款参与历史且与有效参与区分时，加状态字段替代软删）

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 全额取消/退款回滚 PromotionUsage 单 Phase 已落地——`releasePromotionUsage` helper（普通接口方法，无 biz 注解，匹配内部 helper 先例）+ 6 处全额取消/退款镜像 hook 全部接线（均在还券/还积分之后的 whole-order 分支），9 个 IGraphQLEngine 测试覆盖全部 hook + 部分项保留 + 幂等 + 非满减无副作用；494 service 测试 0 失败。owner doc（`marketing-and-promotions.md:462`）、log（`docs/logs/2026/06-29.md`）、roadmap（`enhanced-features-roadmap.md:27`）、源计划 deferred 标注（`2026-06-28-1610-1:195`）均已同步。五点一致、anti-hollow 通过、deferred 诚实（秒杀/拼团成员为不同结果面，非隐藏缺陷）。可关闭。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure-audit subagent（fresh session，与实现 agent 不同 session/context）
- Evidence: 独立 closure audit（2026-06-29）逐项核对 live repo：
  - **helper 落地**：`ILitemallOrderBiz.java:293`（接口声明 + 文档说明 6 处调用点）+ `LitemallOrderBizModel.java:2547-2561`（实现：`promotionUsageBiz.findList` by orderId + `deleteEntity` 软删除循环；非空方法体，非 return null 占位）。
  - **6 hook 全接线**（均在还券/还积分之后）：hook1 `LitemallOrderBizModel.cancel:735`、hook2 `cancelExpiredOrders:2332`、hook3 `LitemallAftersaleBizModel.refund:227`（`isWholeOrder` 分支内）、hook4 `confirmReturnReceived:357`（`isWholeOrder` 分支内）、hook5 `LitemallGrouponBizModel.refundGrouponOrder:294`、hook6 `LitemallPinTuanActivityBizModel.refundMemberOrder:400`。部分项退款分支不调用（D2 边界确认）。
  - **测试覆盖（IGraphQLEngine，Rule #15）**：TestLitemallOrderBizModel (a)`testCancelReleasesPromotionUsageAndAllowsReparticipate:360` / (b)`testCancelExpiredOrdersReleasesPromotionUsage:385` / (h)`testReleasePromotionUsageIdempotent:400` / (i)`testCancelNonPromotionOrderNoUsageSideEffect:422`；TestLitemallAftersaleBizModel (c)`testWholeRefundReleasesPromotionUsage:825` / (d)`testWholeConfirmReturnReceivedReleasesPromotionUsage:839` / (g)`testItemLevelRefundKeepsPromotionUsage:862`；TestLitemallGrouponExpireBizModel (e)`testExpireGrouponsReleasesPromotionUsage:124`；TestLitemallPinTuanActivityBizModel (f)`testExpirePinTuansReleasesPromotionUsage:378`。共 9 测试覆盖全部退出标准 + 失败模式。
  - **验证**：`docs/logs/2026/06-29.md:15` 记录 `mvn clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS（10/10 模块）+ `mvn test -pl app-mall-service` 494 测试 0 失败（+9 新增）。
  - **docs sync**：`docs/design/marketing-and-promotions.md:462`（退款回滚 usage 已实现节）、`docs/backlog/enhanced-features-roadmap.md:27`（P22 deferred 已闭环）、`docs/plans/2026-06-28-1610-1-phase22-promotion-usage-model-gap-plan.md:195`（源 deferred 标注 successor 关闭）。
  - **anti-hollow**：helper 经 6 处 hook 全部可达，无空方法体/无 return null 占位/无吞异常；soft-delete 经 `findList`（自动过滤 deleted=true）天然幂等。
  - **deferred 诚实**：秒杀 usage（`LitemallLog` 计数，不同结果面）、拼团成员记录（PinTuanMember 独立实体）、状态字段优化候选——均非隐藏的 in-scope 缺陷。
  - **verdict**：approved — 全部 Closure Gates 满足，plan 可关闭。

Follow-up:

- 秒杀 usage 回滚（见 Deferred，触发条件）。拼团成员记录回滚已由 refundMemberOrder 成员状态推进覆盖。
