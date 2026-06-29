# phase22b 关闭 maxPerUser model-gap（满减/秒杀限购强一致 + 按活动/按场次效果归因）

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Mission: mall
> Work Item: Phase 22 ORM-dependent successor — 关闭 P15/P22/P24 遗留的 maxPerUser model-gap
> Source: `docs/backlog/enhanced-features-roadmap.md` §22；`2026-06-28-0340-1-phase22-marketing-management-backend-plan.md` → `Deferred But Adjudicated → maxPerUser model-gap`（7 项 deliverable）
> Related: `2026-06-27-1742-1-phase15-full-discount-promotion-plan.md`（P15 满减 maxPerUser model-gap origin）、`2026-06-28-0125-1-phase24-flash-sale-plan.md`（P24 秒杀 maxPerUser model-gap origin）、`2026-06-28-0340-1-phase22-marketing-management-backend-plan.md`（P22 ORM-independent slice delivered，ORM slice 待本 successor）
> Audit: required

## Current Baseline

> 来源：实读 live repo（HEAD 经 grep/read 核验），非旧计划记忆。

**满减限购字段已存在但无参与计数实体：**
- `LitemallPromotionActivity.maxPerUser`（`model/app-mall.orm.xml:1418`，propId 10，"每人限参与次数(0不限)"）字段已存在。
- **无 `LitemallPromotionUsage` 实体**（`model/app-mall.orm.xml` 全文无此 entity）——满减参与无法落库计数。
- `LitemallOrderBizModel.submit`（`LitemallOrderBizModel.java:499`）调用 `promotionActivityBiz.selectPromotionForOrder(goodsPriceTotal, couponScopeIds, context)`，该方法**仅返回 `BigDecimal` 折扣额**（`LitemallPromotionActivityBizModel.java:99-136`），**不返回命中的 activityId**，因此 submit 当前无法知道命中了哪个活动、更无法写参与记录。
- 满减命中后 `order.setPromotionPrice(promotionPrice)`（`:507`），无 usage 写入、无 maxPerUser 计数校验。

**秒杀限购字段已存在但无场次归因列：**
- `LitemallFlashSale.maxPerUser`（`model/app-mall.orm.xml:1558`，propId 6，"每人限购"）字段已存在。
- `LitemallFlashSaleSession` 实体已存在（`model/app-mall.orm.xml:1587`，带 `refPropName="flashSale"`）。
- **`LitemallOrder` 无 `flashSaleSessionId` 列**（`model/app-mall.orm.xml:1132-1133` 仅 `payChannel`(38)/`walletPayAmount`(39)，无 flashSaleSessionId）——秒杀成交订单无法按场次聚合。
- `LitemallFlashSaleBizModel.flashSaleBuy`（`LitemallFlashSaleBizModel.java:138`）已实现 maxPerOrder 守卫（`:214-220`），但**显式标注 `maxPerUser deferred as model-gap`**（`:214`）；订单经 `orderBiz.createFlashSaleOrder(...)`（`:276`）创建，**不写入 flashSaleSessionId**，无 maxPerUser 计数校验。

**效果统计已有聚合口径但缺按活动/按场次归因：**
- `LitemallPromotionActivityBizModel.getPromotionEffectiveness(startDate?, endDate?)`（`:83`）已落地**聚合口径**（promotionPrice>0 订单的参与单数/GMV/优惠额），但**按活动归因（distinct userId from usage、按活动 GMV）deferred（ORM）**。
- `getFlashSaleEffectiveness` **整体 deferred（ORM）**，依赖 flashSaleSessionId 列。
- `getCouponUsageStatistics`/`getPinTuanEffectiveness` 已落地（不依赖本计划 ORM）。
- 上述 stat 经 `LitemallMarketingMapper`（`@SqlLibMapper` `/app/mall/sql/LitemallMarketing.sql-lib.xml`）+ 注册于 `app-dao.beans.xml:40`。

**状态字典就绪：** `mall/promotion-status`（0=草稿/10=进行中/20=已结束/30=已关闭）已在 `app-mall.orm.xml` 声明，满减/秒杀共用。

**roadmap 状态：** Phase 22 当前为 `planned`（ORM-independent slice 已交付并 closure-audited；ORM slice 待本 successor 关闭后翻 `done`）。

## Goals

- 关闭 P15/P22/P24 遗留的 `maxPerUser` model-gap：新增 `LitemallPromotionUsage` 实体 + `LitemallOrder.flashSaleSessionId` 列。
- 满减限购强一致：submit 命中满减时写 `PromotionUsage`，按 `(userId, promotionActivityId)` 计数校验 `maxPerUser`（超限拒绝）。
- 秒杀限购强一致：`flashSaleBuy` 回填 `flashSaleSessionId`，按 `(userId, flashSaleSessionId)` 计数校验 `maxPerUser`（超限拒绝 + 写 `LitemallLog` 计数）。
- 按活动/按场次效果归因：`getPromotionEffectiveness(activityId?)` 按活动归因（distinct userId from usage、按活动 GMV）；`getFlashSaleEffectiveness(flashSaleId?)` 按场次（成交单数/GMV/售罄率/限购命中拒绝数）。
- 所有新增 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine` 测试。
- 关闭后 roadmap Phase 22 翻 `done`。

## Non-Goals

- 满减/秒杀/拼团/限时折扣的**玩法本身**（已在 P15/P23/P24/P25 实现）。
- 营销管理后台 UI / 日历 / 优惠券 / 拼团效果统计（P22 ORM-independent slice 已交付）。
- 跨活动的全局限购（如"每用户每日总参与次数"）——本计划仅做单活动级 maxPerUser。
- 满减参与记录的**退款回滚**（订单取消/退款时是否回退 usage 计数）——本计划仅记录参与事实，退款回滚为 successor（见 Deferred）。
- 复杂报表导出（PDF/Excel，依赖 nop-report，归 P18/P19 successor）。

## Task Route

- Type: `implementation-only change`（业务设计已在 `marketing-and-promotions.md` 落地；本计划为 P22 Deferred 的 7 项 deliverable 的执行 slice，无 net-new 业务语义，仅在已决模型方案上落地）
- Owner Docs: `docs/design/marketing-and-promotions.md`（满减/秒杀章节、营销活动管理后台 → 已知约束）、`docs/design/system-configuration.md`（营销效果统计口径）
- Skill Selection Basis: ORM 新增实体/列（nop-orm-modeler、nop-database-design）、BizModel 新增/改动 @BizMutation/@BizQuery + 跨实体走 I*Biz（nop-backend-dev）、API 测试 IGraphQLEngine（nop-testing）

## Protected Area

本计划 Phase 1 修改 `model/app-mall.orm.xml`（新增 `LitemallPromotionUsage` 实体 + `LitemallOrder.flashSaleSessionId` 可空列）。按 `docs/context/ai-autonomy-policy.md` Protected Areas 表，XML models（`model/*.orm.xml`）为 **ask-first**：人工批准后方可实施。

- 触及文件：`model/app-mall.orm.xml`（新增 1 实体 + 1 可空列）
- 授权状态：**pending MISSION_DRIVER 授权**（本计划为 drafting 阶段产出，`Plan Status: draft`）
- 实施门控：Phase 1（ORM 实施）在获得 MISSION_DRIVER 显式 ORM 授权前不得开工。先例：P15/P20/P22/P24/P26/P27/P28/P32/P33 均按同一 ask-first 模式新增 ORM 实体，授权由 MISSION_DRIVER「execute the entire plan」类指令构成（见 P20 `2026-06-28-0340-2` Phase 1 先例）。
- 未获授权的降级路径：将 Phase 1 ORM 实施 + 依赖它的 Phase 2/3/4 全部移入 `Deferred But Adjudicated`（分类 model-gap，触发条件「获得 ORM 授权时」）——但这会使本计划整体无独立可交付项，故本计划在授权落地前保持 `draft`/blocked，不部分推进（与 P22 不同，本计划无 ORM-independent 切片）。

## Infrastructure And Config Prereqs

- 无新增基础设施。秒杀场次状态切换继续依赖已引入的 `nop-job-local`（`switchFlashSaleSessions`，不在本计划改动）。
- 新增 sql-lib 聚合 SQL 仅面向 MySQL（与现有 `LitemallMarketing.sql-lib.xml` 一致；跨库方言为既有 watch-only residual，不在本计划处理）。

## Execution Plan

### Phase 1 — 模型准备（PromotionUsage 实体 + Order.flashSaleSessionId 列）

Status: completed
Targets: `model/app-mall.orm.xml`、`app-mall-meta`、`app-mall-dao`（regen）
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Add | Proof`
- Prereqs: 无（Phase 1 受 `## Protected Area` ask-first 门控）

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读完其 routing table 标为必读的文档；列出已读路径。每处模型改动后用 skill selfcheck 校验命名/索引/域/字典规范。
  - Docs read: `.opencode/skills/nop-orm-modeler/SKILL.md`、`.opencode/skills/nop-database-design/SKILL.md`、`docs/context/project-context.md`、`docs/context/codebase-map.md`
- [x] **Add:** `LitemallPromotionUsage` 实体（表 `litemall_promotion_usage`）。列：`id`(seq 主键)、`userId`(varchar, not null)、`promotionActivityId`(varchar, not null)、`orderId`(varchar, not null)、`meetAmount`(decimal 10,2, 满减门槛命中时的 goodsPrice)、`discountAmount`(decimal 10,2, 实际优惠额)、`addTime`（平台自动三件套 addTime/updateTime/deleted）。唯一键 `(userId, orderId)`（防同一订单重复计数）；索引 `idx_promoUsage_user_activity (userId, promotionActivityId)`（限购计数查询）、`idx_promoUsage_order (orderId)`（按订单反查）、`idx_promoUsage_activity (promotionActivityId)`（按活动归因）。字典无。落地 `model/app-mall.orm.xml`。
- [x] **Add:** `LitemallOrder.flashSaleSessionId` 可空列（propId 续排，varchar，nullable，displayName"秒杀场次ID"）。秒杀下单写入，非秒杀订单为 null。落地 `model/app-mall.orm.xml`。
- [x] **Proof:** regen 通过、编译通过；新实体/列出现在生成 DDL 与 _gen 代码。验证命令：`./mvnw -pl app-mall-codegen -am generate-test-resources`（regen）+ `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar`（全模块编译）。

Exit Criteria:

- [x] `LitemallPromotionUsage` 实体与 `LitemallOrder.flashSaleSessionId` 列落地，regen/编译通过
- [x] 实体命名/索引/域符合 nop-database-design selfcheck
- [x] `docs/logs/` 更新

### Phase 2 — 满减 maxPerUser 强一致 + usage 写入

Status: completed
Targets: `app-mall-service/.../entity/LitemallPromotionActivityBizModel.java`、`app-mall-dao/.../biz/ILitemallPromotionActivityBiz.java`、`app-mall-service/.../entity/LitemallOrderBizModel.java`、`app-mall-dao/.../entity/_gen`（regen PromotionUsage BizModel/IBiz）、`app-mall-service/.../entity/LitemallPromotionUsageBizModel.java`（regen 产物，本计划仅注入使用）
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: Phase 1

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完必读文档；列路径。每个 `@BizMutation`/`@BizQuery`/内部方法写完用 nop-backend-dev selfcheck 校验（CrudBizModel 安全 API、跨实体走 I*Biz、NopException+ErrorCode、@Inject 非 private、@BizMutation 不叠 @Transactional）。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`、`../nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`、`../nop-entropy/docs-for-ai/02-core-guides/service-layer.md`
- [x] **Decision: 命中活动 id 的获取方式。** `selectPromotionForOrder` 当前仅返回 `BigDecimal`（GraphQL-facing `@BizQuery`，前端预览用）。submit 写 usage 需命中 activityId。抉择：**抽取内部 helper `resolvePromotionForOrderInternal(goodsPrice, scopeIds, ctx)` 返回小结果对象 `{activityId, discount, meetAmount}`**（null 表示无命中），submit 调用此 helper；`selectPromotionForOrder` 保持 public `@BizQuery` 不变（内部委托 helper 取 discount 返回），**不破坏既有 GraphQL 契约**。备选（改 `selectPromotionForOrder` 返回结构体）被否——破坏前端预览契约 + 已有测试。残留风险：helper 与 public 方法逻辑须保持单一真相源（public 委托 helper）。把抉择写入 `marketing-and-promotions.md`。
- [x] **Add:** `resolvePromotionForOrderInternal`（包级/内部方法，非 GraphQL）返回 `{activityId, discount, meetAmount}`；`selectPromotionForOrder` 改为委托它取 `.discount`。
- [x] **Add:** `LitemallOrderBizModel.submit` 在满减命中（`discount > 0 && activityId != null`）后：先按 `(userId, activityId)` 查现有 usage 计数，若 `activity.maxPerUser > 0 && count >= maxPerUser` 抛 `NopException(ERR_PROMOTION_MAX_PER_USER)`（新增 ErrorCode，`AppMallErrors.java`）；校验通过则 `promotionUsageBiz` 注入并写入 `PromotionUsage`（userId/activityId/orderId/meetAmount/discountAmount）。usage 写入与订单创建在同一 `@BizMutation` 事务内。
- [x] **Proof:** `IGraphQLEngine` 覆盖：满减命中写 usage（单条）、maxPerUser 超限拒绝（第 N+1 单）、多活动不互相干扰、selectPromotionForOrder 契约未变（discount 返回值一致）。

Exit Criteria:

- [x] 满减命中写 PromotionUsage；maxPerUser 超限拒绝（成功 + 拒绝两态）
- [x] `selectPromotionForOrder` GraphQL 契约不变（既有预览测试仍绿）
- [x] **API 测试：** 新增/改动 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine` 测试
- [x] `docs/design/marketing-and-promotions.md` 更新（满减限购强一致语义 + 命中活动 id 抉择）
- [x] `docs/logs/` 更新

### Phase 3 — 秒杀 maxPerUser 强一致 + 场次归因

Status: completed
Targets: `app-mall-service/.../entity/LitemallFlashSaleBizModel.java`、`app-mall-service/.../entity/LitemallOrderBizModel.java`（`createFlashSaleOrder` 签名）、`app-mall-dao/.../biz/ILitemallOrderBiz.java`、`app-mall-dao/.../sql/LitemallFlashSale.sql-lib.xml`（maxPerUser 计数查询）
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Proof`
- Prereqs: Phase 1

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完必读文档；列路径。每方法 selfcheck。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`（Phase 2 已加载，沿用）、`../nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`
- [x] **Add:** `createFlashSaleOrder` 接受 `flashSaleSessionId` 参数并写入 `order.setFlashSaleSessionId(...)`；`flashSaleBuy`（`:276`）调用处传入 `flashSaleSessionId`。
- [x] **Add:** `flashSaleBuy` 在库存扣减前/订单创建前加 maxPerUser 计数守卫：按 `(userId, flashSaleSessionId)` 查 `LitemallOrder`（`flashSaleSessionId=? and userId=? and orderStatus >= 101` 且非取消态）已成交+待支付计数，若 `activity.maxPerUser > 0 && count >= maxPerUser` 抛 `NopException(ERR_FLASH_SALE_MAX_PER_USER)`（新增 ErrorCode）+ 写 `LitemallLog`（拒绝计数，参照既有 `MallLogManager` pattern）。
- [x] **Proof:** `IGraphQLEngine` 覆盖：秒杀单成功（flashSaleSessionId 回填）、maxPerUser 超限拒绝（含 Log 写入）、不同场次互不干扰。

Exit Criteria:

- [x] 秒杀订单 flashSaleSessionId 回填；maxPerUser 超限拒绝（成功 + 拒绝两态）+ 拒绝写 Log
- [x] **API 测试：** 改动 `@BizMutation` 通过 `IGraphQLEngine` 测试
- [x] `docs/design/marketing-and-promotions.md` 更新（秒杀限购强一致语义 + 场次归因）
- [x] `docs/logs/` 更新

### Phase 4 — 按活动/按场次效果统计归因

Status: completed
Targets: `app-mall-service/.../entity/LitemallPromotionActivityBizModel.java`（`getPromotionEffectiveness` 扩展 activityId 归因）、`app-mall-service/.../entity/LitemallFlashSaleBizModel.java`（新增 `getFlashSaleEffectiveness`）、`app-mall-dao/.../sql/LitemallMarketing.sql-lib.xml`、`app-mall-dao/.../mapper/LitemallMarketingMapper.java`、`app-mall-dao/.../dto/*Bean.java`、`app-dao.beans.xml`、`app-mall-web/.../pages/mall/marketing/marketing-effect.page.yaml`（秒杀按场面板接线消费 `getFlashSaleEffectiveness`，移除 model-gap 占位）
Required Skill: `nop-backend-dev`、`nop-testing`、`nop-frontend-dev`

- Item Types: `Add | Proof`
- Prereqs: Phase 1

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing` + `nop-frontend-dev`，读完必读文档；列路径。每方法 selfcheck；前端文件完成后整体 selfcheck（XView 三层模型、bounded-merge、AMIS 约定）。
  - Docs read: `.opencode/skills/nop-frontend-dev/SKILL.md`、`../nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`、`../nop-entropy/docs-for-ai/02-core-guides/page-dsl-pattern-catalog.md`
- [x] **Add:** `getPromotionEffectiveness(@Optional activityId?, @Optional startDate?, @Optional endDate?)` 扩展按活动归因：`activityId` 非空时经 `PromotionUsage` 按 `promotionActivityId` 聚合（distinct userId 参与人数、SUM(关联 order.actualPrice) GMV、SUM(discountAmount) 优惠额、COUNT(DISTINCT orderId) 参与单数）；`activityId` 空时保持现有聚合口径。sql-lib 经 `LitemallMarketingMapper`。
- [x] **Add:** `getFlashSaleEffectiveness(@Optional flashSaleId?, @Optional startDate?, @Optional endDate?)` `@BizQuery`：按 `flashSaleId`（经 Order.flashSaleSessionId → Session.flashSaleId 关联）聚合成交单数/GMV（SUM actualPrice）/售罄率（已售 sessionStock 占比）/限购命中拒绝数（从 `LitemallLog` 拒绝计数，若 Log 结构允许；否则标 N/A）。新增 `FlashSaleEffectivenessBean`。
- [x] **Add:** `marketing-effect.page.yaml` 秒杀按场效果面板接线消费 `getFlashSaleEffectiveness`，移除现有 "ORM model-gap，待 ask-first 授权后补齐" 占位（`marketing-effect.page.yaml:98`）；满减按活动归因面板同步接线 `getPromotionEffectiveness(activityId?)`（移除 `:42` 占位）。
- [x] **Proof:** `IGraphQLEngine` 覆盖：满减按活动归因（有/无 activityId 两态）、秒杀按场次归因、空数据态。前端面板接线经页面加载（service 配置或 AMIS source）验证数据返回结构匹配。

Exit Criteria:

- [x] 满减按活动归因 + 秒杀按场次归因返回口径正确
- [x] **API 测试：** 新增/改动 `@BizQuery` 通过 `IGraphQLEngine` 测试
- [x] 前端效果看板页 `marketing-effect.page.yaml` 秒杀按场面板从"model-gap 待 ORM"翻为可用（接线消费 `getFlashSaleEffectiveness`）
- [x] `docs/design/system-configuration.md` 更新（满减按活动/秒杀按场次效果口径）
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 plan-audit subagent（fresh session，先于 Phase 1 实施）
- Evidence: 4 phase 全部 Required Skill 列齐（Phase 1 `nop-orm-modeler`+`nop-database-design`、Phase 2/3 `nop-backend-dev`+`nop-testing`、Phase 4 `nop-backend-dev`+`nop-testing`+`nop-frontend-dev`）；Protected Area（`model/app-mall.orm.xml`）ask-first 门控路径明确（授权由 MISSION_DRIVER「execute the entire plan」类指令构成，与 P20 先例一致）；Closure Gates 与 phase Exit Criteria 对齐；无 MAJOR_OBJECTION。Minor：建议满减参与记录退款回滚显式入 Deferred（已采纳，见下）。

## Closure Gates

- [x] in-scope behavior is complete（PromotionUsage 实体 + flashSaleSessionId 列 + 满减/秒杀 maxPerUser 强一致 + 按活动/按场次归因）
- [x] relevant docs are aligned（`marketing-and-promotions.md`、`system-configuration.md`）
- [x] `enhanced-features-roadmap.md` Phase 22 状态：ORM slice 关闭后 `planned` → `done`（实读 `enhanced-features-roadmap.md:27` 已翻 `done`）
- [x] verification has run（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS；`./mvnw test` 351 tests, 0 failures，见 `docs/logs/2026/06-28.md` Phase 4 entry）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（非实体级纯逻辑测试）—— `TestLitemallOrderBizModel`/`TestLitemallFlashSaleBizModel`/`TestLitemallPromotionActivityBizModel` 经 `callQuery`/`callMutation` 覆盖
- [x] no in-scope item downgraded to deferred/follow-up（Deferred 仅为满减退款回滚 successor，非本 model-gap scope）
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed, and Nop-platform phases do not write `none` without justification
- [x] skill loading verification: each phase scanned available skills, loaded all matching skills, read ALL mandatory docs（路径列于 skill loading gate），selfcheck 无 anti-pattern
- [x] text consistency verified: status, phases, gates, and log all agree（`Plan Status: completed`、4 phase `Status: completed`、Closure Gates 全 `[x]`、log 4 entries 全绿）
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 满减参与记录的退款回滚

- Classification: `optimization candidate`
- Why Not Blocking Closure: 本计划 PromotionUsage 记录的是"参与事实"（命中即记录），用于限购计数与效果归因。订单取消/退款时是否回退 usage 计数（从而释放限购额度）涉及退款↔营销交互的额外语义，非本 model-gap 关闭的核心。当前行为：退款不回退计数（与"每人限参与 N 次"的严格语义一致——参与过即计数）。
- Successor Required: `yes`（触发条件：业务要求退款释放限购额度时）
- **已由 successor 关闭（2026-06-29）：** `docs/plans/2026-06-29-1921-2-promotion-usage-refund-rollback-plan.md` 交付 `releasePromotionUsage` + 6 处全额取消/退款镜像 hook（cancel/cancelExpiredOrders/售后 refund/售后 confirmReturnReceived/团购 refundGrouponOrder/拼团 refundMemberOrder），全额取消/退款软删除 PromotionUsage 释放 maxPerUser 额度；部分项退款保留 usage（满减为订单级折扣）。

## Closure

<!-- 闭合审计必须由独立 subagent（不同 session/context）执行。实现 agent 不得自行填写本节。 -->

Status Note: 4 phase 全部交付并通过 `IGraphQLEngine` 测试，roadmap Phase 22 翻 `done`。PromotionUsage 实体 + `LitemallOrder.flashSaleSessionId` 列关闭 P15/P22/P24 遗留的 maxPerUser model-gap；满减/秒杀限购强一致 + 按活动/按场次效果归因落地；前端看板秒杀面板从「待 ORM」翻为可用。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure-audit subagent（fresh session，不同于实施 agent）
- Live-repo 验证（grep/read 对照 plan claims）：
  - Phase 1：`model/app-mall.orm.xml:1478` `LitemallPromotionUsage` 实体存在；`model/app-mall.orm.xml:1135` `LitemallOrder.flashSaleSessionId` 列存在（propId 41，nullable）。
  - Phase 2：`ILitemallPromotionActivityBiz.java:31` `resolvePromotionForOrderInternal` 声明；`LitemallPromotionActivityBizModel.java:115` 实现；`LitemallOrderBizModel.java:509` submit 调用，`:536` 抛 `ERR_PROMOTION_MAX_PER_USER`；`AppMallErrors.java:316` 错误码定义；`TestLitemallOrderBizModel.java` 3 用例覆盖命中写入/超限拒绝/限内允许。
  - Phase 3：`LitemallFlashSaleBizModel.java:153` `flashSaleBuy` 接受 `flashSaleSessionId`；`:242-261` maxPerUser 守卫 + REQUIRES_NEW 写 `LitemallLog`；`LitemallOrderBizModel.java:1703` `createFlashSaleOrder` 签名扩 `@Optional flashSaleSessionId`，`:1749` 写入；`TestLitemallFlashSaleBizModel.java` 新增 maxPerUser 拒绝+Log、跨场次互不干扰、按场效果归因用例。
  - Phase 4：`LitemallPromotionActivityBizModel.java:84` `getPromotionEffectiveness(@Optional activityId?)` 双态分支；`LitemallFlashSaleBizModel.java:448` `getFlashSaleEffectiveness` 新增；`LitemallMarketing.sql-lib.xml:113` + `LitemallMarketingMapper.java:26` sql-lib/mapper 落地；`marketing-effect.page.yaml:30/107` 两处 `@query:` 接线，无「model-gap」/「待 ORM」占位残留。
  - Roadmap：`docs/backlog/enhanced-features-roadmap.md:27` Phase 22 已标 `done` 并引本 plan + 0340-1 ORM-independent slice 双切片。
  - 日志：`docs/logs/2026/06-28.md` 4 个 Phase entry（每个含 BUILD SUCCESS + 全绿 test 计数 + owner-doc 更新）。
- Verdict: PASS — 4 phase Exit Criteria 全部对齐 live repo，无 hollow 实现，无 in-scope 缺项被降级为 deferred/follow-up。文本一致性（Plan Status / 4 phase Status / Closure Gates / log）全一致。Deferred 中「满减参与记录退款回滚」为合法 successor（触发条件：业务要求退款释放限购额度），非本 model-gap 缺项。

Follow-up:

- 满减参与记录退款回滚（见 Deferred，触发条件：业务要求退款释放限购额度）。
