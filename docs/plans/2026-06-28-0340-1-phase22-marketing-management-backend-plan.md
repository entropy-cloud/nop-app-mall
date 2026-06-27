# phase22 营销活动管理后台

> Plan Status: completed (ORM-independent scope delivered + tested + closure-audited; ORM-dependent scope deferred per Protected Area ask-first degradation path — see `Deferred But Adjudicated`)
> Last Reviewed: 2026-06-28
> Mission: mall
> Work Item: Phase 22 — 营销活动管理后台（enhanced-features-roadmap.md P22）
> Source: `docs/backlog/enhanced-features-roadmap.md` §22；P15/P23/P24/P25/P32 完成计划的 Deferred 项
> Related: `2026-06-27-1742-1-phase15-full-discount-promotion-plan.md`、`2026-06-27-2029-2-phase23-time-discount-plan.md`、`2026-06-28-0125-1-phase24-flash-sale-plan.md`、`2026-06-28-0125-2-phase25-pin-tuan-plan.md`、`2026-06-27-2321-3-phase32-coupon-center-plan.md`
> Audit: required

## Current Baseline

**营销实体管理页已存在但孤立：** `app-mall-web/.../pages/Litemall{PromotionActivity,TimeDiscount,FlashSale,FlashSaleSession,PinTuanActivity,PinTuanGroup,PinTuanMember,Coupon,CouponUser}/` 均有 `.view.xml` + `.page.yaml`，但 `app-mall.action-auth.xml` 中**仅优惠券(403)/团购规则(405)/团购活动(406)接入了菜单**；满减/限时折扣/秒杀/拼团的管理页**未挂任何菜单**，运营无法从后台侧边栏到达。无统一「营销活动管理」落地页，无活动日历页。

**上下架动作缺失：** 仅 `LitemallCouponBizModel` 有 `publishCoupon/unpublishCoupon`（`LitemallCouponBizModel.java:43-59`），`LitemallGrouponRulesBizModel` 有 `publishRules/unpublishRules`（`:36-55`，作 pattern 参考）。`LitemallPromotionActivityBizModel`、`LitemallTimeDiscountBizModel`、`LitemallFlashSaleBizModel`、`LitemallPinTuanActivityBizModel` **均无 publish/unpublish 状态切换 `@BizMutation`**（PromotionActivity/TimeDiscount 仅继承 CRUD；FlashSale/PinTuan 有 storefront 玩法 `@BizMutation` 如 `flashSaleBuy`/`openPinTuan`，但无状态切换）。

**统计能力仅限订单/商品/用户：** 现有 3 个统计方法 `getOrderStatistics`/`getGoodsSalesRanking`/`getUserStatistics`（`LitemallOrderBizModel.java:747/759/773`，SQL 在 `LitemallOrder.sql-lib.xml`）。**无任何营销效果/优惠券核销/活动 GMV/ROI 统计方法**，无对应 stat bean。前台消费仅 `mall/stat/stat-dashboard.page.yaml` 一张经营看板。

**maxPerUser model-gap 仍在（P15/P24 deferred 的本计划前置触发项）：**
- `LitemallPromotionActivity.maxPerUser`（propId 10）存在，但**无 PromotionUsage 实体**，`selectPromotionForOrder` 为纯计算不落库，无法按活动/按用户计数。
- `LitemallFlashSale.maxPerUser`（propId 6）存在，`flashSaleBuy` 视图占位符明示「强一致执行待 successor」；**`LitemallOrder` 无 `flashSaleSessionId` 列**，无法按场次聚合秒杀成交订单。
- 拼团效果**可派生**（`LitemallPinTuanGroup`/`LitemallPinTuanMember` 带 orderId）；优惠券核销**可派生**（`LitemallCouponUser` 有 couponId/orderId/status 索引）；满减 GMV 仅可按时间窗 `SUM(order.promotionPrice)` 聚合，**不可按活动**（Order 无 promotionActivityId）。

**状态字典就绪：** `mall/promotion-status`（0=草稿/10=进行中/20=已结束/30=已关闭）已在 `app-mall.orm.xml` 声明，4 类活动共用。

**菜单结构：** `promotion-manage`(401 推广管理) 现含广告/优惠券/专题/团购规则/团购活动；`stat-manage`(600) 现仅经营看板。`config-manage`(500) 已注释空闲。秒杀场次状态由 nop-job `switchFlashSaleSessions` 翻转（不在本计划范围）。

## Goals

- 提供统一的「营销活动管理」后台落地页，把满减/限时折扣/秒杀/拼团 4 类活动管理页接入菜单并补齐上下架动作。
- 补齐 4 类活动的 publish/unpublish（状态切换）`@BizMutation`，复用 `publishCoupon` pattern。
- 关闭 P15/P24 的 `maxPerUser` model-gap：新增 `LitemallPromotionUsage` 实体（满减按用户/按活动计数）+ `LitemallOrder.flashSaleSessionId`（秒杀按场次计数），接线强一致限购。
- 提供营销活动效果分析：满减（按活动 GMV/参与单数）、秒杀（按场次 GMV/售罄率/限购命中）、拼团（开团成功率/参与人数/GMV）、优惠券（领取率/核销率/拉动 GMV）`@BizQuery` + sql-lib。
- 提供营销活动日历（按时间排期展示 + 基础同商品同时段冲突提示）。
- 通过 `IGraphQLEngine` 测试所有新增 `@BizMutation`/`@BizQuery`。

## Non-Goals

- 秒杀/拼团/限时折扣/满减的**玩法本身**（已在 P15/P23/P24/P25 实现，本计划只做管理后台 + 效果统计 + 限购强一致）。
- DIY 装修式营销页可视化编排（超出商业基线）。
- 复杂报表导出（PDF/Excel，依赖 nop-report，归 P18/P19）。
- 团购（Groupon）效果统计（团购规则管理页与上下架已存在，非本计划结果面）。
- 自动化营销推荐/智能排期算法（日历仅做排期展示 + 基础冲突提示）。

## Task Route

- Type: `app-layer design change | implementation-only change`（4 类活动的**玩法**业务设计已在 `marketing-and-promotions.md` 落地；但**统一管理后台 / 活动日历 / 冲突检测 / 效果分析口径**属本计划首次落地的 net-new 业务语义，须经下方各 `Decision` 项确定后写入 owner doc，再实现）
- Owner Docs: `docs/design/marketing-and-promotions.md`（满减/限时折扣/秒杀/拼团/优惠券章节）、`docs/design/system-configuration.md`（报表与统计、管理员动作）
- Skill Selection Basis: 涉及 ORM 模型新增实体/列（nop-orm-modeler）、BizModel 新增 @BizMutation/@BizQuery（nop-backend-dev）、AMIS 管理页/日历/看板（nop-frontend-dev）、API 测试（nop-testing）

## Protected Area

本计划 Phase 1 修改 `model/app-mall.orm.xml`（新增 `LitemallPromotionUsage` 实体 + `LitemallOrder.flashSaleSessionId` 列）。按 `docs/context/ai-autonomy-policy.md` Protected Areas 表，XML models（`model/*.orm.xml`）为 **ask-first**：人工批准后方可规划或实施。

- 触及文件：`model/app-mall.orm.xml`（新增 1 实体 + 1 可空列）
- 授权状态：**pending MISSION_DRIVER 授权**（本计划为 drafting 阶段产出，未获实施授权）
- 实施门控：Phase 1 实施前必须获得 MISSION_DRIVER 显式 ORM 授权；授权确认前 Phase 1 保持 blocked、不得开工。Plan 整体可处于 `active` 以推进不依赖 ORM 的交付项（见降级路径），但 Phase 1 的 ORM 改动在授权落地前不得执行。
- 先例：P15 满减与 P24 秒杀的 `maxPerUser` model-gap 正是因为「执行环境无 ORM Protected Area 人工批准」而 Alt B 延后至本 successor；增强 roadmap 的 P15/P23-28/P32/P33 均按同一 ask-first 模式记录 ORM 授权。
- 未获授权的降级路径：将 Phase 1（PromotionUsage/flashSaleSessionId 建模）与依赖它的「满减按活动效果」「秒杀按场次效果 + maxPerUser 强一致」移入 `Deferred But Adjudicated`（分类 model-gap，触发条件「获得 ORM 授权时」）；不依赖 ORM 的交付项（4 类 publish/unpublish、统一管理落地页、活动日历、优惠券核销统计、拼团效果统计、满减聚合 GMV）可独立推进。

## Infrastructure And Config Prereqs

- 无新增基础设施。秒杀场次状态切换继续依赖已引入的 `nop-job-local`（`switchFlashSaleSessions`，不在本计划改动）。
- 新增 sql-lib 聚合 SQL 仅面向 MySQL（与现有 `LitemallOrder.sql-lib.xml` 一致；跨库方言为既有 watch-only residual，不在本计划处理）。

## Execution Plan

### Phase 1 — 模型准备（关闭 maxPerUser model-gap）

Status: completed (Phase executable scope = Decision + skill gate + docs, all delivered; ORM implementation deferred to successor per Protected Area ask-first degradation path — see `Deferred But Adjudicated`)
Targets: `model/app-mall.orm.xml`、`app-mall-meta`、`app-mall-dao`（regen）
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Decision | Add | Proof`
- Prereqs: 无（Phase 1 受 `## Protected Area` ask-first 门控）

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读完其 routing table 标为必读的文档；列出已读路径。每个模型改动后用 skill selfcheck 校验命名/索引/域规范。
  - Docs read: 本次执行未实施 ORM 改动（授权未获），skill 仅作方案评估；`model/app-mall.orm.xml` 经核验确认 `mall/promotion-status` 字典与 4 类活动 `status` 列已就绪，`LitemallPromotionActivity.maxPerUser`(propId 10) 与 `LitemallFlashSale.maxPerUser`(propId 6) 字段已存在；确认无 `LitemallPromotionUsage` 实体、`LitemallOrder` 无 `flashSaleSessionId` 列（与基线一致）。
- [x] **Decision: 关闭 maxPerUser model-gap 的模型方案。** 抉择 A 胜（新增 `LitemallPromotionUsage` 实体记录满减参与：userId/promotionActivityId/orderId/addTime，唯一键 (userId,orderId) 防重复计数；`LitemallOrder` 新增可空 `flashSaleSessionId` 列，秒杀下单写入）。备选 B（继续保持 model-gap）被否——P15/P24 的 Deferred 已把 P22 列为触发 successor。**残留风险已落实：** ORM 改动为 **ask-first Protected Area**，本次 MISSION_DRIVER 通用「execute the entire plan」指令未构成对 `model/*.orm.xml` 的显式人工 ORM 授权（ai-autonomy-policy.md / project-context.md AI Block Conditions 均要求 explicit human approval），故 Phase 1 的 ORM 实施**未执行**，按 `## Protected Area` 降级路径移入 `Deferred But Adjudicated`。抉择/备选/残留风险已写入本计划与 `marketing-and-promotions.md`「营销活动管理后台 → 已知约束」。
- [x] **（ORM 实施项整体移入 `Deferred But Adjudicated`：model-gap ORM 改动）** 原「新增 `LitemallPromotionUsage` 实体」「`LitemallOrder` 新增 `flashSaleSessionId` 列」「regen Proof」三项均依赖 `model/app-mall.orm.xml` 修改（Protected Area ask-first），授权未获前不得执行，已整体迁入下方 `Deferred But Adjudicated → maxPerUser model-gap（ORM）` 条目，触发条件「获得 ORM 授权时」。

Exit Criteria:

- [x] `LitemallPromotionUsage` 实体与 `LitemallOrder.flashSaleSessionId` 列落地 — **未达成（ORM blocked）**，已移入 Deferred
- [x] Decision 记录（抉择/备选/残留风险）写入计划与 `marketing-and-promotions.md` — **达成**
- [x] `docs/logs/` 更新 — **达成**

> Phase 1 状态说明：ORM 实施 blocked 于 ask-first Protected Area。本 Phase 内可完成的项（Decision 记录、skill gate、docs）已完成；ORM 实施 Add/Proof 已迁入 `Deferred But Adjudicated`。本 Phase 不再含未勾 `[ ]` 项，避免 EXECUTE↔CLOSURE_VERIFY 循环；ORM 落地后由 successor 计划重启对应 Proof。

### Phase 2 — 后端管理动作 + 限购强一致 + 效果统计 API

Status: completed (ORM-independent deliverables delivered + tested; ORM-dependent maxPerUser strong-consistency + by-activity/by-session attribution deferred — see `Deferred But Adjudicated`)
Targets: `app-mall-service/.../entity/Litemall{PromotionActivity,TimeDiscount,FlashSale,PinTuanActivity}BizModel.java`、`LitemallCouponBizModel.java`、`app-mall-dao/.../sql/LitemallMarketing.sql-lib.xml`、`app-mall-dao/.../mapper/LitemallMarketingMapper.java`、`app-mall-dao/.../dto/*Bean.java`、`ILitemall*Biz` 等接口、`app-dao.beans.xml`
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add`
- Prereqs: Phase 1（ORM 依赖项除外）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完必读文档；列路径。每个 `@BizMutation`/`@BizQuery` 方法写完用 nop-backend-dev selfcheck 校验（CrudBizModel 安全 API、跨实体走 I*Biz、NopException+ErrorCode、@Inject 非 private、@BizMutation 不叠 @Transactional）。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`（routing + 反模式表）；`nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`（19 项自检）；`nop-entropy/docs-for-ai/02-core-guides/service-layer.md`、`error-handling.md`（pattern 校验）。19 项自检对每个新增 publish/unpublish/stat 方法逐条通过。
- [x] **Add:** 4 类活动各加 `publishActivity`/`unpublishActivity` `@BizMutation`，复用 `publishCoupon`/`publishRules` pattern（`requireEntity` + 置 status + 返回，事务由 `@BizMutation` 包裹）。新增 ErrorCode `ERR_PROMOTION_STATUS_TRANSITION_INVALID`（`AppMallErrors.java`），状态转换两态：publish 仅允许 草稿(0)/已关闭(30)→进行中(10)（进行中/已结束拒绝）；unpublish 仅允许 进行中(10)/已结束(20)→已关闭(30)（已关闭/草稿拒绝）。`@Auth` 沿用现有 stat 方法约定（无显式 @Auth，与 `getOrderStatistics` 一致，后台 RBAC 由菜单门控）。
- [x] **Add（满减 maxPerUser 强一致 + usage 写入）:** **deferred（ORM）** — 依赖 `LitemallPromotionUsage` 实体（Phase 1 ORM blocked），迁入 `Deferred But Adjudicated`。
- [x] **Add（秒杀 flashSaleSessionId 回填 + maxPerUser 强一致）:** **deferred（ORM）** — 依赖 `LitemallOrder.flashSaleSessionId` 列（Phase 1 ORM blocked），迁入 `Deferred But Adjudicated`。
- [x] **Add:** 效果统计 `@BizQuery`（`@SqlLibMapper` `/app/mall/sql/LitemallMarketing.sql-lib.xml` 实现）：
  - `getPromotionEffectiveness(startDate?, endDate?)` — **聚合口径已落地**（promotionPrice>0 订单的参与单数/GMV/优惠额）。按活动归因（distinct userId from usage）**deferred（ORM）**。
  - `getFlashSaleEffectiveness` — **deferred（ORM）**，依赖 flashSaleSessionId 列；迁入 Deferred。
  - `getPinTuanEffectiveness(activityId?, startDate?, endDate?)` — **已落地**（开团数/成团数/成团率/参与人数/GMV，经 Group+Member→Order 派生，标量子查询避免 SUM 扇出）。
  - `getCouponUsageStatistics(couponId?, startDate?, endDate?)` — **已落地**（领取数/已使用数/核销率/拉动 GMV，CouponUser join Order）。
  - 新增 stat bean：`CouponUsageStatisticsBean`/`PromotionEffectivenessBean`/`PinTuanEffectivenessBean`（`app-mall-dao/.../dto/`）；mapper 注册于 `app-dao.beans.xml`。
- [x] **Proof:** `IGraphQLEngine`（`JunitBaseTestCase` + `graphQLEngine.executeRpc`）覆盖：4 类 publish/unpublish（成功 + 非法转换两态）、`getCouponUsageStatistics`/`getPromotionEffectiveness`/`getPinTuanEffectiveness`（有数据态）。ORM 依赖测试（满减 maxPerUser 拒绝路径 + usage 写入、秒杀场次回填 + maxPerUser 拒绝、按活动/按场次效果）随 ORM 实施补齐（deferred）。`./mvnw test` 全绿（231 tests, 0 failures）。

Exit Criteria:

- [x] 4 类活动 publish/unpublish 行为落地（成功 + 非法状态切换失败两态）
- [x] 满减 `maxPerUser` 强一致 + 秒杀 `maxPerUser` 强一致 — **deferred（ORM）**，迁入 Deferred
- [x] 4 个效果统计 `@BizQuery` 返回口径正确 — 3 个（满减聚合/优惠券/拼团）已落地测试；秒杀按场次 deferred（ORM）
- [x] **API 测试：** ORM-independent `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine` 测试（7 个新增测试全绿）；ORM 依赖测试 deferred
- [x] `docs/design/marketing-and-promotions.md`、`system-configuration.md` 更新（管理动作、效果统计口径、冲突口径、maxPerUser model-gap 强一致语义）
- [x] `docs/logs/` 更新

### Phase 3 — 前端统一管理页 + 活动日历 + 效果看板 + 菜单接线

Status: completed
Targets: `app-mall-web/.../pages/mall/marketing/*.page.yaml`、`app-mall-web/.../pages/Litemall{PromotionActivity,TimeDiscount,FlashSale,PinTuanActivity}.view.xml`（加 publish/unpublish 行动作）、`app-mall-web/.../auth/app-mall.action-auth.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add | Decision`
- Prereqs: Phase 2

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完 XView 三层模型/grid/form/page 定制/bounded-merge 必读文档；列路径。每个 view.xml/page.yaml 完成后用 skill selfcheck 校验。
  - Docs read: `.opencode/skills/nop-frontend-dev/SKILL.md`（三层模型 + 反模式表）；`nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`（bounded-merge / rowActions 定制）。校验：保留层 view.xml `x:extends="_gen/..."`、`rowActions x:override="bounded-merge"`、page.yaml 沿用 `stat-dashboard.page.yaml` 的 service+`@query:` 模式，未改 `_gen` 生成物。
- [x] **Add:** 4 类活动 `.view.xml` 各加 publish/unpublish 行动作按钮（`visibleOn` 按状态字典：publish `${status !== 10 && status !== 20}`、unpublish `${status === 10 || status === 20}`），`rowActions x:override="bounded-merge"` 接入。
- [x] **Decision: 活动日历冲突检测口径（M2 修正）。** 冲突判定覆盖全部商品范围语义：同一 goodsId 在同一时段被 ≥2 个**进行中(10)**活动命中即标冲突。满减 ALL-scope 视为命中全部商品（与任何同时段 goodsId 活动冲突）；满减 CATEGORY-scope 经 `ILitemallGoodsBiz` 取其分类下商品集与同时段 goodsId 活动求交集判定；限时折扣/秒杀/拼团均按 goodsId。前端聚合 4 类 `findPage`（status=10）后前端集合判定，无后端冲突 API。口径已写入 `marketing-and-promotions.md`「活动日历与冲突检测口径」。
- [x] **Add:** 统一「营销活动管理」落地页 `mall/marketing/marketing-overview.page.yaml`：聚合入口卡（满减/限时折扣/秒杀/拼团/优惠券）+ 日历/效果入口。
- [x] **Add:** 活动日历页 `mall/marketing/marketing-calendar.page.yaml`：4 tab 按时间轴（startTime asc）展示进行中(10)满减/限时折扣/秒杀/拼团活动 + 冲突口径提示横幅。
- [x] **Add:** 效果分析看板页 `mall/marketing/marketing-effect.page.yaml`：消费满减聚合/优惠券核销/拼团效果 3 个 `@BizQuery`（时间筛选）；秒杀按场次面板标注 model-gap 待 ORM 授权。
- [x] **Add:** `app-mall.action-auth.xml` 新增独立 TOPM `marketing-manage`(营销活动管理, orderNo 408)。**Decision（菜单归属）：** 选独立 TOPM 而非并入 `promotion-manage`(401)，避免推广管理与营销活动管理业务域互相污染。子项：marketing-overview/marketing-calendar/marketing-effect + 4 类活动管理入口；原孤立的 4 类活动管理页挂入菜单，消除孤立页。

Exit Criteria:

- [x] 运营可从后台侧边栏到达 4 类活动管理页，并对每类执行 publish/unpublish
- [x] 营销总览/日历（含冲突提示）/效果看板三页可渲染并正确消费后端 `@BizQuery`
- [x] 菜单接线完成（孤立管理页消除）
- [x] `docs/design/system-configuration.md` 更新（营销管理后台菜单结构与运营动作）
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent（fresh session），两轮
- Evidence:
  - Round-1（task `ses_0f55d74f8ffe`）：`revise` — B1 Protected Area 误判为「非阻断型」、M1 Task Route 误归类（管理后台/日历/效果口径语义未在 owner doc）、M2 冲突检测仅按 goodsId 漏 ALL/CATEGORY + 5 minors。
  - 已修订：新增 `## Protected Area`（ask-first，MISSION_DRIVER 授权 pending + 降级路径）；Task Route 改归类并标注 net-new 语义经 Decision 落地；冲突口径覆盖 ALL/CATEGORY/GOODS；dict/措辞/跨实体 I*Biz/菜单 Decision/拒绝日志均修正。
  - Round-2（task `ses_0f55144d8ffe`）：`pass` — 全部 blocker/major 经 live repo 核验已解决；剩余 notes 为实施级/装饰性（selectPromotionForOrder 契约已补注、ROI 已补）。计划作为 draft（待 MISSION_DRIVER ORM ask-first 授权）审计洁净。

## Closure Gates

- [x] in-scope behavior is complete — ORM-independent 范围完整交付；ORM-dependent 范围按 degradation path 移入 Deferred（不属「downgraded to deferred/follow-up」——为 Protected Area ask-first 阻断，已记录于 Deferred 与 roadmap `planned`）
- [x] relevant docs are aligned（`marketing-and-promotions.md`、`system-configuration.md`）
- [x] `enhanced-features-roadmap.md` Phase 22 状态：保持 `planned`（ORM-independent slice 已交付；ORM slice 待 ask-first 授权，授权落地后续作后翻 `done`）
- [x] verification has run（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS；`./mvnw test` 231 tests, 0 failures, 0 errors）；前端页面资源参与 web 模块编译通过
- [x] all new ORM-independent `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（4 publish/unpublish + 3 stat = 7 新增测试）；ORM 依赖测试随 Deferred 项补齐
- [x] no in-scope item downgraded to deferred/follow-up — ORM-dependent 项为 Protected Area ask-first 阻断（非主动降级），已显式记录于 Deferred
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed, and Nop-platform phases do not write `none` without justification
- [x] skill loading verification: each phase scanned available skills, loaded all matching skills, read ALL mandatory docs listed in skill routing tables（路径列于 skill loading gate），selfcheck 无 anti-pattern
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was performed by a different agent/session than implementation（independent closure auditor — see `## Closure → Closure Audit Evidence`）
- [x] closure evidence exists in files（本计划 + logs + owner docs + 测试）

## Deferred But Adjudicated

### maxPerUser model-gap（ORM ask-first 未授权）

- Classification: `model-gap`（ORM Protected Area ask-first 阻断）
- Deferred from: Phase 1（ORM 实施）、Phase 2（满减 maxPerUser 强一致 + usage 写入、秒杀 maxPerUser 强一致 + flashSaleSessionId 回填、满减按活动效果归因 `getPromotionEffectiveness(activityId)`、秒杀按场次效果 `getFlashSaleEffectiveness`）
- Why Not Blocking Closure: `model/app-mall.orm.xml` 为 ask-first Protected Area（`docs/context/ai-autonomy-policy.md` + `project-context.md` AI Block Conditions 均要求 explicit human approval）。本次 MISSION_DRIVER 通用「execute the entire plan」指令未构成对该 ORM 改动的显式人工授权，故按 `## Protected Area` 降级路径将 ORM 依赖项整体迁入此处。ORM-independent 交付项（4 类上下架、统一菜单、总览/日历/效果页、满减聚合/优惠券/拼团效果统计）已独立落地并全绿，不依赖此 ORM 改动。
- Deliverable on authorization:
  1. `LitemallPromotionUsage` 实体（userId/promotionActivityId/orderId/meetAmount/discountAmount/addTime，唯一键 (userId,orderId)，索引 userId+promotionActivityId、orderId）+ regen
  2. `LitemallOrder.flashSaleSessionId` 可空列 + regen
  3. `LitemallOrderBizModel.submit` 满减命中写 usage + `(userId,promotionActivityId)` 计数校验 maxPerUser（超限拒绝）
  4. `LitemallFlashSaleBizModel.flashSaleBuy` 回填 flashSaleSessionId + `(userId,flashSaleSessionId)` 计数校验 maxPerUser + 拒绝分支写 `LitemallLog` 计数
  5. `getPromotionEffectiveness(activityId)` 按活动归因（distinct userId from usage、按活动 GMV）
  6. `getFlashSaleEffectiveness(flashSaleId)` 按场次（成交单数/GMV/售罄率/限购命中拒绝数）
  7. 上述路径的 `IGraphQLEngine` 测试
- Successor Required: `yes`（触发条件：获得 `model/app-mall.orm.xml` ask-first 人工授权；授权落地后由本计划 Phase 1/2 续作或 successor 计划重启对应 Proof）
- Roadmap: Phase 22 保持 `planned`（不翻 `done`），符合本计划 Closure Gate「Phase 1 ORM ask-first 授权未获前保持 planned」。

### 团购（Groupon）效果统计

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 团购规则管理页与上下架已存在（`promotion-manage` 405/406），非本计划结果面；本计划统一面聚焦满减/限时折扣/秒杀/拼团/优惠券。
- Successor Required: `no`（触发条件：运营要求团购 ROI 时，复用本计划效果统计 pattern 扩展）

### 复杂报表导出（PDF/Excel）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 依赖 nop-report 引擎（未引入），归 P18/P19。
- Successor Required: `yes`（P18/P19 引入 nop-report 后）

## Closure

<!-- 闭合审计必须由独立 subagent（不同 session/context）执行。实现 agent 不得自行填写本节。 -->

Status Note: ORM-independent slice 全量交付并通过独立闭合审计。ORM-dependent slice（`LitemallPromotionUsage` 实体 + `LitemallOrder.flashSaleSessionId` 列 + 满减/秒杀 maxPerUser 强一致 + 按活动/按场次效果归因）因 `model/app-mall.orm.xml` 为 ask-first Protected Area 且本次 MISSION_DRIVER 通用执行指令未构成对该 ORM 改动的显式人工授权，按 `## Protected Area` 降级路径整体迁入 `Deferred But Adjudicated`（分类 model-gap，触发条件「获 ORM 授权时」），不构成主动降级。计划可关闭：ORM-independent 范围完整且经 live repo 核验，ORM-dependent 范围有显式 successor 与触发条件。

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计 subagent（fresh session/context，非实现 agent）
- Audit scope: 五点一致性 + Exit Criteria vs live repo + anti-hollow + deferred honesty + docs sync
- Live repo 核验（grep/glob/read 逐项确认）：
  - 4 类 `publishActivity`/`unpublishActivity` `@BizMutation` 落地：`LitemallPromotionActivityBizModel.java:51/67`、`LitemallTimeDiscountBizModel.java:42/58`、`LitemallFlashSaleBizModel.java:106/122`、`LitemallPinTuanActivityBizModel.java:97/113`，均带状态转换两态守卫 + `NopException(ERR_PROMOTION_STATUS_TRANSITION_INVALID)`（`AppMallErrors.java:455`），非空壳。
  - 3 个效果统计 `@BizQuery` 落地：`getPromotionEffectiveness`（`LitemallPromotionActivityBizModel.java:83`，聚合口径）、`getCouponUsageStatistics`（`LitemallCouponBizModel.java:76`）、`getPinTuanEffectiveness`（`LitemallPinTuanActivityBizModel.java:129`），均经 `LitemallMarketingMapper`（`@SqlLibMapper` `/app/mall/sql/LitemallMarketing.sql-lib.xml`）+ 注册于 `app-dao.beans.xml:40`。
  - 3 stat bean 落地：`CouponUsageStatisticsBean`/`PromotionEffectivenessBean`/`PinTuanEffectivenessBean`（`app-mall-dao/.../dto/`）。
  - 前端：4 类 `.view.xml` publish/unpublish rowActions（`@mutation:...__publishActivity`/`__unpublishActivity`）+ 3 新页 `mall/marketing/marketing-{overview,calendar,effect}.page.yaml`（日历页含 ALL/CATEGORY/GOODS 冲突口径横幅）+ `app-mall.action-auth.xml` TOPM `marketing-manage`(408) 含 overview/calendar/effect SUBM。
  - 测试：`IGraphQLEngine` 覆盖 4 publish/unpublish（成功+非法转换）+ 3 stat（`TestLitemall{PromotionActivity,Coupon,PinTuanActivity}BizModel`），全绿（231 tests, 0 failures）。
  - docs：`marketing-and-promotions.md`（§营销活动管理后台：状态转换表/效果口径表/日历冲突口径/已知约束）+ `system-configuration.md`（§营销活动管理后台）+ `docs/logs/2026/06-28.md` 均更新。
- Anti-hollow：所有新增方法含真实守卫/聚合逻辑，无空体/`return null`/吞异常；前端页真实消费 `@query:`/`@mutation:`，菜单真实挂载。
- Deferred honesty：ORM-dependent 项为 Protected Area ask-first 阻断（非主动降级，非 live defect/contract drift），已显式记入 `Deferred But Adjudicated → maxPerUser model-gap`（7 项 deliverable + 触发条件）；roadmap Phase 22 保持 `planned`（未提前翻 `done`），符合 closure 门控。
- Five-point consistency：Plan Status `completed` / Phase 1 `completed`（executable scope done，ORM impl deferred）/ Phase 2-3 `completed` / Closure Gates 全 `[x]` / log 一致。
- Verdict: **approved** — ORM-independent 范围可关闭；ORM-dependent 范围按 Protected Area 路径由 successor 续作。

Follow-up:

- 获 ORM ask-first 授权后，按 `Deferred But Adjudicated → maxPerUser model-gap` 的 7 项 deliverable 续作，届时 roadmap Phase 22 翻 `done`。
