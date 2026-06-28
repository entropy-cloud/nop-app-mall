# 营销活动效果报表导出（P22 deferred successor）

> Plan Status: completed
> Last Reviewed: 2026-06-29
> Mission: mall
> Work Item: P22 deferred successor — 营销活动效果分析 xlsx/pdf 模板化导出
> Source: `docs/plans/2026-06-28-0340-1-phase22-marketing-management-backend-plan.md` → `Deferred But Adjudicated → 复杂报表导出（PDF/Excel）`（Successor Required: `yes`，触发条件「P18/P19 引入 nop-report 后」）
> Related: `docs/plans/2026-06-28-2352-1-nop-report-engine-introduction-plan.md`（前置：引入 nop-report 引擎 + funnel/product/order 导出先例）、`docs/plans/2026-06-29-0119-1-user-coupon-report-export-plan.md`（前置：多 sheet xlsx 导出先例，本计划同模式扩展到营销域）、`docs/plans/2026-06-28-1610-1-phase22-promotion-usage-model-gap-plan.md`（交付 4 效果 `@BizQuery` 的 P22 ORM-dependent slice）
> Audit: required

## Current Baseline

**触发条件已满足（live repo 核验）：** P22 计划 Deferred「复杂报表导出（PDF/Excel）」触发条件为「P18/P19 引入 nop-report 后」。nop-report 引擎已于 2026-06-29 引入并交付 5 大主题域 + 商品导出（`2026-06-28-2352-1` done、`2026-06-29-0119-1` done）。触发条件客观成立。

**4 个营销效果 `@BizQuery` 已存在（本计划复用，不新增查询/不改口径）：**

| 指标面 | `@BizQuery`（BizModel） | Bean | 字段 |
| ------ | ---------------------- | ---- | ---- |
| 满减效果 | `getPromotionEffectiveness(activityId?, startDate?, endDate?)` — `LitemallPromotionActivityBizModel:84` | `PromotionEffectivenessBean` | `promotedOrderCount` / `totalGmv` / `totalDiscount` / `participantCount` |
| 秒杀按场次效果 | `getFlashSaleEffectiveness(flashSaleId?, startDate?, endDate?)` — `LitemallFlashSaleBizModel:448` | `FlashSaleEffectivenessBean` | `dealOrderCount` / `totalGmv` / `participantCount` / `soldOutRate` / `rejectedCount` |
| 拼团效果 | `getPinTuanEffectiveness(activityId?, startDate?, endDate?)` — `LitemallPinTuanActivityBizModel:129` | `PinTuanEffectivenessBean` | `openedGroups` / `successGroups` / `participantCount` / `totalGmv` |
| 优惠券核销 | `getCouponUsageStatistics(couponId?, startDate?, endDate?)` — `LitemallCouponBizModel:76` | `CouponUsageStatisticsBean` | `claimedCount` / `usedCount` / `pulledGmv` |

- 口径由 `docs/design/marketing-and-promotions.md:843`「效果分析口径」表 + `system-configuration.md:413` 持有，本计划不改口径。
- 4 方法均 `@BizQuery`（经 `@SqlLibMapper LitemallMarketing.sql-lib.xml` 聚合），时间窗空兜底全量。

**导出基建（已就绪，本计划复用）：**
- `IReportEngine` 注入先例：`LitemallOrderBizModel:234`（`exportReport`）、`LitemallGoodsBizModel:99`（`exportGoodsReport`）。注入字段 `IReportEngine reportEngine;`（`@Inject`，非 private）。
- 导出 `@BizQuery` 模式（`LitemallOrderBizModel.exportReport:1753`，`@Auth(roles="admin")`）：`reportEngine.getRenderer(path, ReportRenderTypes.validate(renderType))` → `output.generateToResource(resource, scope)` → 返回 `WebContentBean("application/octet-stream", file, fileName)`；非法 name 走 `AppMallErrors.ERR_REPORT_NAME_INVALID`。
- 模板路径 `_vfs/nop/main/report/<name>.xpt.xml`；`ReportRenderTypes.validate` 校验 `xlsx|pdf|html`。
- nop-report-core/nop-report-pdf 已在 `app-mall-service/pom.xml` 引入（无新 Maven 依赖）。

**前端消费页（缺导出入口）：**
- `mall/marketing/marketing-effect.page.yaml`：4 个 service 面板（`promotionEffect` / `couponUsage` / `pinTuanEffect` / `flashSaleEffect`）消费上述 4 query，**无任何导出入口**（无 xlsx/pdf/csv 按钮，grep 确认零 `export`/`导出`/`xlsx`/`pdf`/`csv` 命中）。
- 顶部 form 提供 `dateRange`（input-date-range，format `X`）+ `promotionActivityId`（满减活动 ID，可选）+ 刷新按钮（reload 4 面板）。
- 对比先例：`stat-funnel.page.yaml` / `stat-product.page.yaml` / `stat-user.page.yaml` / `stat-order.page.yaml` 均已有 `exportReport` 模板导出按钮（格式 select + blob 按钮）；本页是 5 大统计页之外唯一无导出的效果看板。

**owner doc：** `system-configuration.md:413` 记载效果分析看板消费 4 效果 query，未提及导出（导出为 P22 Deferred successor）。`marketing-and-promotions.md:853` 记载 `marketing-effect.page.yaml` 面板口径，未涉及导出。

**模块：** `app-mall-service`、`app-mall-web`。

**核心缺口：** 营销活动效果分析 4 面板无 xlsx/pdf 模板化导出；nop-report 引擎已就绪但未覆盖营销域。本 successor 以单 xlsx 多 sheet 形式交付 4 面板效果导出。

## Goals

- 营销活动效果分析获得与 funnel/product/order/user/coupon 一致的 nop-report 模板化导出（xlsx/pdf），复用既有 4 个效果 `@BizQuery`，不改查询逻辑/SQL-lib/口径。
- 新增 `exportMarketingReport(renderType, startDate?, endDate?, promotionActivityId?, flashSaleId?, pinTuanActivityId?, couponId?, context)` `@BizQuery @Auth(admin)` 导出入口（单报表方法，无 `reportName` 参数，沿 `exportGoodsReport:560` 单报表先例），渲染单 xlsx 多 sheet（满减/秒杀/拼团/优惠券 4 sheet）。
- `marketing-effect.page.yaml` 补齐 xlsx/pdf 导出入口（对齐统计页既有导出按钮模式，直接调 `exportMarketingReport`）。
- owner doc `system-configuration.md` / `marketing-and-promotions.md` 标注营销效果导出已交付，关闭 P22 Deferred「复杂报表导出」successor。
- 导出经 `IGraphQLEngine`（`JunitAutoTestCase` 录制回放）测试覆盖。

## Non-Goals

- **不新增/不改变 4 个效果 `@BizQuery` 的口径**——本计划只给既有查询补导出入口与 xpt 模板，不改 SQL-lib/聚合逻辑/时间窗兜底语义（口径在 P22 + owner doc 落定）。
- **不引入新 Maven 依赖**——nop-report 引擎已在前置计划引入。
- **不新增 CSV 兜底**——4 面板为单 Bean 聚合指标（非列表），CSV 兜底形态复杂且价值低（xlsx 已覆盖）；与 user-coupon-export 计划「不新增用户分析 CSV 兜底」抉择一致。
- **不改 ORM 模型**——复用既有效果查询与既有字段；无 Protected Area（ORM/api.xml/auth/payment/WeChat）触及。
- **不实现活动明细列表导出**（如逐活动 ROI 明细行）——本计划导出 4 面板的聚合指标（与看板展示同口径）；逐活动/逐场次明细列表导出为独立结果面（触发条件：运营要求逐活动明细导出时）。
- **不更新 roadmap 阶段状态**——本计划为 deferred successor（非 roadmap 阶段），`enhanced-features-roadmap.md` P22 Phase Status 保持 `done`。
- **PDF 中文字体部署**——PDFBox 内置 Helvetica 仅 Latin，CJK 需 `_vfs/fonts/` 提供 TTF（部署期关注，已在 owner doc 注记）；本计划模板表头/指标名沿用 Latin 字段名先例（与 user-coupon-export 一致），导出适配层将中文指标名映射为 Latin 代码。

## Task Route

- Type: `implementation-only change`（闭合已 deferred 的 successor，业务语义与触发条件已在 P22 owner doc 与 Deferred 中界定）
- Owner Docs: `docs/design/system-configuration.md`（营销活动管理后台 → 效果分析看板导出口径）、`docs/design/marketing-and-promotions.md`（效果分析口径表）
- Skill Selection Basis: 新增 `@BizQuery exportMarketingReport` + `IReportEngine` xpt 模板适配 + 跨实体经 `I*Biz` 注入 → `nop-backend-dev`；导出为 `@BizQuery`，须经 `IGraphQLEngine` 测试 → `nop-testing`；前端 AMIS 导出按钮/page.yaml → `nop-frontend-dev`。不触及 `model/*.orm.xml`，无 ORM skill。

## Infrastructure And Config Prereqs

- 无新基建。nop-report-core/nop-report-pdf 已引入；新模板放 `app-mall-service/src/main/resources/_vfs/nop/main/report/`（与既有 6 个 `.xpt.xml` 同目录，模板属 service 模块）；app-mall-web 对 nop-ooxml-xlsx 的 precompile 域依赖已在前置计划配置。
- 回滚策略：纯新增（1 导出方法 + 1 模板 + 前端按钮 + doc），不改既有 funnel/product/order/user/coupon 路径；回滚 = 移除新增方法/模板/按钮，无数据迁移。

## Execution Plan

### Phase 1 - 后端：exportMarketingReport + xpt 多 sheet 模板 + 测试（Add-heavy）

Status: completed
Targets: `app-mall-service/.../entity/LitemallPromotionActivityBizModel.java`（新增 `exportMarketingReport` `@BizQuery` + `IReportEngine`/`ILitemallFlashSaleBiz`/`ILitemallPinTuanActivityBiz`/`ILitemallCouponBiz` 注入 + 适配方法）、`app-mall-api/.../entity/ILitemallPromotionActivityBiz.java`（接口声明）、`app-mall-service/src/main/resources/_vfs/nop/main/report/marketing-effect.xpt.xml`（模板与既有 6 个 `.xpt.xml` 同目录于 app-mall-service）
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Explore | Proof`
- Prereqs: 前置 nop-report 引擎计划已完成（done）；P22 ORM-dependent slice 已交付 4 效果 query（done）

- [x] **Skill loading gate:** 扫描 available skills，加载 `nop-backend-dev`、`nop-testing`，读完各 skill routing table 标为必读的文档并列于下；每写完一个方法用 selfcheck 校验无 anti-pattern。
  - Docs read: `nop-entropy/docs-for-ai/02-core-guides/service-layer.md`、`nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`（19 项逐方法校验 exportMarketingReport）、`nop-entropy/docs-for-ai/02-core-guides/error-handling.md`、`nop-entropy/docs-for-ai/00-start-here/ai-defaults.md` 反模式表、`nop-entropy/docs-for-ai/02-core-guides/testing.md`、`nop-entropy/docs-for-ai/03-runbooks/generate-report.md`（xpt 多 sheet 模式）
- [x] **Explore（须先于 Decision 定稿）：跨 BizModel 聚合可行性。** 核实 4 效果 query 分布在 4 个不同 BizModel（PromotionActivity / FlashSale / PinTuanActivity / Coupon）；确认 `IReportEngine` 已在 Order/Goods BizModel 注入先例（`LitemallOrderBizModel:234` / `LitemallGoodsBizModel:99`）；确认跨实体调用规则要求注入 `I*Biz` 接口（`ai-defaults.md` 反模式表）。结论：在 `LitemallPromotionActivityBizModel` 注入 `IReportEngine` + 3 个 sibling `I*Biz` 聚合可行 → 支持 Decision D1 选项 A。
- [x] **Decision D1（须先于编码，依赖 Explore 结论）：导出入口归属 BizModel。** 抉择**选项 A**：新增 `exportMarketingReport` `@BizQuery @Auth(roles="admin")` 于 `LitemallPromotionActivityBizModel`（营销活动主实体），注入 `IReportEngine` + `ILitemallFlashSaleBiz` + `ILitemallPinTuanActivityBiz` + `ILitemallCouponBiz`，聚合 4 效果 Bean 到单一 scope 渲染多 sheet xlsx。备选 B（扩展现有 `LitemallOrderBizModel.exportReport` 加 `marketing` reportName）被否——营销效果与订单域语义不同，强挂 OrderBizModel 违反单一职责，且需在 OrderBizModel 注入 4 个营销 I*Biz 污染订单域。备选 C（4 个 BizModel 各加导出方法）被否——破坏「单一 xlsx 多 sheet 聚合报表」结果面，且入口分散难运营。残留风险：`LitemallPromotionActivityBizModel` 注入 3 个 sibling I*Biz 增加耦合，但均为营销域内聚合，符合「效果分析统一面」语义。
- [x] **Decision D2：导出结构。** 抉择**单 xlsx 多 sheet**（`exportMarketingReport` 渲染 4 sheet：满减/秒杀/拼团/优惠券，每面板一 sheet）。每 sheet 为单行指标表（与看板展示同口径）。沿用 user-coupon-export 计划「多 sheet 导出」先例与 Latin 表头/指标名约定（适配层 `buildXxxDataSet` 将中文指标名映射为 Latin 代码，如 `promotedOrderCount`/`totalGmv`/`soldOutRate`/`successRate`）。参数透传：`promotionActivityId`→满减按活动归因、`flashSaleId`→秒杀按场次、`pinTuanActivityId`→拼团按活动、`couponId`→指定券；空则时间窗全量口径（与 query 兜底一致）。**方法签名不带 `reportName`**——本方法为单报表导出（非 `exportReport` 多报表分发），沿 `LitemallGoodsBizModel.exportGoodsReport(renderType, ...):560` 单报表先例，消除「单值参数是否需校验」的歧义。**实现期修订（selfcheck rule #5）：** 方法有 7 个业务字段（renderType + 时间窗 + 4 归因 ID）>5，按 BizModel 方法自检规则 #5 改用 `@RequestBean MarketingReportRequest` `@DataBean`（沿 `AftersaleApplyRequest` 先例），GraphQL 入参形状不变（同 7 字段 map），不构成公共契约变更。
- [x] **Add：** `ILitemallPromotionActivityBiz` 新增 `exportMarketingReport(@RequestBean MarketingReportRequest, context)` 声明（无 `reportName`）；`LitemallPromotionActivityBizModel` 实现：注入 `IReportEngine reportEngine` + 3 sibling I*Biz（`@Inject` 非 private），调 4 效果 query，经 `buildPromotionEffectDataSet`/`buildFlashSaleEffectDataSet`/`buildPinTuanEffectDataSet`/`buildCouponUsageDataSet` 适配为单元素 `List<Map>`（`Collections.singletonList`，与 `buildCouponDataSet` 先例一致）并 `scope.setLocalValue`，复用 `ReportRenderTypes.validate`（非法 `renderType` 由其抛出）+ `reportEngine.getRenderer("/nop/main/report/marketing-effect.xpt.xml", safeType)` 渲染，返回 `WebContentBean`。异常沿用 `NopException.adapt(e)` + 资源清理（`resource.delete()` on failure），与 `exportGoodsReport:575-585` 同模式。派生率指标（拼团成团率 `successGroups/openedGroups`、优惠券核销率 `usedCount/claimedCount`）在适配层计算（与看板 AMIS 表达式同口径），原 query 口径不变。
- [x] **Add：** `_vfs/nop/main/report/marketing-effect.xpt.xml`（4 sheet，表头/指标名 Latin，`*=^ds!field` 简写，与既有模板同模式）。
- [x] **Proof：** 经 `IGraphQLEngine` 测试 `LitemallPromotionActivity__exportMarketingReport`：`renderType` × {xlsx, pdf}；带 `promotionActivityId`/`flashSaleId` 按归因导出；空集（无数据时间窗）不报错；非法 `renderType` 走 `ReportRenderTypes.validate` 失败。新增 5 用例并入既有 `TestLitemallPromotionActivityBizModel`。验证命令：`./mvnw test -pl app-mall-service -am`（全绿，427 tests）。

Exit Criteria:

- [x] `exportMarketingReport` 渲染单 xlsx 多 sheet（4 面板），返回 `WebContentBean`（成功）；非法 `renderType` 走 `ReportRenderTypes.validate` 失败（失败模式）。
- [x] **API 测试：** `exportMarketingReport`（新增 `@BizQuery`）通过 `IGraphQLEngine`（`JunitBaseTestCase` + `IGraphQLEngine`，沿 `TestLitemallGoodsOpsWorkbench`/`TestLitemallOrderStatisticsBizModel` 导出测试先例）测试，覆盖 xlsx/pdf/归因参数/空集/非法入参。
- [x] 4 效果 query 口径与看板展示一致（导出适配层不改 query 逻辑，仅适配 + 派生率指标与 AMIS 表达式同口径）。
- [x] owner doc 更新归 Phase 2；`docs/logs/` 更新（于计划闭合时合并一条）。

**实现期发现的预置缺陷修复（偏离 Non-Goal「不改 SQL-lib」，已论证）：** `LitemallMarketing.sql-lib.xml` 中 `getCouponUsageStatistics`/`getPinTuanEffectiveness`/`getFlashSaleEffectiveness` 3 条 query 使用 `(${id} IS NULL OR col = ${id})` 惯用法处理可选 ID 过滤。Nop xpl-sql 中 `${arg}` 对 null 值输出**空文本**（非 `?` 绑定），故 id 为 null 时生成 `( IS NULL OR col = )` 非法 SQL，导致 3 条 query 在 id 为 null 时抛 `bad-sql-grammar`。该缺陷同时影响**既有** `marketing-effect.page.yaml` 3 个面板（coupon/pinTuan/flashSale 面板均不带 id 调用，即生产 latent bug）。修复：改用本项目既有惯用法 `<c:if test="${id != null and !id.isEmpty()}">` 条件块（`LitemallOrder.sql-lib.xml:199` 先例），**口径不变**（null=全量聚合、非 null=按 id 过滤，与 owner doc 记载一致），非 null 路径 SQL 与原先逐字等价。修复后既有非 null 用例（TestLitemallFlashSaleBizModel/TestLitemallPinTuanActivityBizModel/TestLitemallCouponBizModel）与新增 null 路径用例全绿，全服务测试套件 427 通过。bug 笔记见 `docs/bugs/04-marketing-effectiveness-queries-null-id-sql-grammar.md`。

### Phase 2 - 前端导出入口 + owner doc + 收口（Add-heavy）

Status: completed
Targets: `app-mall-web/.../pages/mall/marketing/marketing-effect.page.yaml`、`docs/design/system-configuration.md`、`docs/design/marketing-and-promotions.md`、`app-mall-web` 前端页加载冒烟测试
Required Skill: `nop-frontend-dev`、`nop-testing`

- Item Types: `Add | Proof`
- Prereqs: Phase 1 完成（`exportMarketingReport` 可用）

- [x] **Skill loading gate:** 扫描 available skills，加载 `nop-frontend-dev`、`nop-testing`，读完 routing table 必读文档并列于下；每页面完成后 selfcheck。
  - Docs read: `nop-entropy/docs-for-ai/00-start-here/application-project-defaults.md`、`nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`（page.yaml 为 AMIS JSON 页，非 view.xml 三层模型，本计划仅增量加 AMIS 控件）、`nop-entropy/docs-for-ai/02-core-guides/testing.md`；并参考 stat-funnel/stat-user 既有 `exportReport` 按钮先例（`stat-funnel.page.yaml:32-52`）。
- [x] **Add：** `marketing-effect.page.yaml` 增 xlsx/pdf 模板导出入口（格式选择 select `marketingFormat`（value xlsx/pdf）+ 按钮 `actionType: ajax`，`url: '@query:LitemallPromotionActivity__exportMarketingReport'`，`data.renderType` 由格式选择绑定 + `startDate`/`endDate` 派生自 `dateRange`（`date | date`，与既有面板同）+ 可选 `promotionActivityId`，`responseType: blob`），对齐 stat-funnel/stat-user 既有导出按钮模式（直接调单报表方法，无需 `reportName`）。
- [x] **Proof：** 前端页加载冒烟（沿用 `TestReportExportPageLoad` 模式，新增 `testMarketingEffectPageLoads`，覆盖 `marketing-effect.page.yaml` 经 `PageProvider.getPage` 加载校验）。验证命令：`./mvnw -pl app-mall-web -am test`（全绿，8 web tests）。
- [x] **Add：** 更新 `docs/design/system-configuration.md`（营销活动管理后台 → 效果分析看板，补「支持 `exportMarketingReport` xlsx/pdf 多 sheet 导出，与统计页同模式；关闭 P22 Deferred 复杂报表导出 successor」）+ `docs/design/marketing-and-promotions.md:853`（效果面板口径节，补导出说明 + 可选 ID 过滤经 `<c:if>` 实现）。标注 P22 Deferred「复杂报表导出」successor 已由本计划关闭。

Exit Criteria:

- [x] `marketing-effect.page.yaml` 具备 xlsx/pdf 导出入口并指向 `exportMarketingReport`（成功）；前端页加载冒烟通过。
- [x] owner doc `system-configuration.md` / `marketing-and-promotions.md` 标注营销效果导出已交付。
- [x] `docs/logs/` 更新。

## Plan Audit

- Status: passed
- Auditor / Agent: independent subagent, adversarial, 2 rounds
- Evidence:
  - Round 1 (`ses_0f068dab6ffeMvwxXwoLtfN44o`)：`PASS` — 0 blockers, 0 major。触发条件判定：P22 Deferred「复杂报表导出」触发条件「P18/P19 引入 nop-report 后」**客观成立**（`app-mall-service/pom.xml:105,109` 已引入 nop-report-core/pdf；前置 nop-report 计划 done）；为本 roadmap 全 done 后**唯一**触发条件已满足的 deferred successor（其余 `Successor Required: yes` 项均需 ops 决策/真实凭证/性能阈值），返回 "nothing" 会留下已到期义务未闭合 → 起草本计划为正确判定（非 anti-slacking）。Baseline 全部主张经 live repo 核验属实（4 效果 query file:line + 4 Bean 字段 + `marketing-effect.page.yaml` 零导出 + `IReportEngine` 注入先例 + 多 sheet xpt 先例 `user-analysis.xpt.xml` 4 sheet + 3 sibling I*Biz 接口存在 + 无 ORM/api.xml 改动）。D1/D2 设计 sound、scope right-sized（5/6 统计面已支持导出，营销为唯一缺口）。每 phase 列 Required Skill（非 bare none）；rule #15 IGraphQLEngine 测试义务 Phase 1 Proof/Exit 显式声明；anti-slacking 无违禁词。2 项 MINOR（m1 单报表方法 `reportName` 参数歧义 / m2 模板模块路径未显式）已采纳修订。
  - Round 2 (`ses_0f062c190ffe98uKITEcpFun9X`，修订后 confirming)：`PASS` — 0 blockers, 0 major, 0 minor。m1 删 `reportName` 参数（沿 `exportGoodsReport:560` 单报表先例）+ m2 Targets/Infrastructure 显式 `app-mall-service/.../report/` 路径，均正确一致落地（Goals/D2/Phase 1 Add·Proof·Exit/Phase 2 前端·Exit/Infrastructure 全文无残留 `reportName=marketing` 或 stale Contingency，无内部矛盾）；两项先例主张经 live repo 核验为真；baseline 抽查全部成立。依 repo 先例（一修订 + 一 clean confirming round = consensus）达成共识，可翻 `active` 并开始实施。
- 备注：无独立 audit 文件（普通 plan-audit 证据默认入 plan）。

## Closure Gates

- [x] in-scope behavior is complete（营销 4 面板 xlsx/pdf 多 sheet 导出 + 前端入口 + owner doc）
- [x] relevant docs are aligned（`system-configuration.md` / `marketing-and-promotions.md` 导出口径）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 427 全绿 + `./mvnw -pl app-mall-web -am test` 8 全绿 + `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` BUILD SUCCESS）
- [x] all new `@BizQuery` methods tested via `IGraphQLEngine`（新增 `exportMarketingReport` `@BizQuery` 经 `IGraphQLEngine` 测试，覆盖 xlsx/pdf/归因参数/空集/非法入参，沿 `TestLitemallGoodsOpsWorkbench` 导出测试先例）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop 平台 phase 未写 `none`
- [x] skill loading verification：各 phase 扫可用 skill、加载匹配 skill、读完 routing 必读文档（路径列于 skill loading gate）、每方法/页 selfcheck
- [x] text consistency verified：status/phases/gates/log 一致
- [x] closure audit was performed by a different agent/session than implementation（独立闭合审计 session 核验，证据见 Closure Audit Evidence）
- [x] closure evidence exists in files（Phase 1/2 items 全 [x] + `docs/logs/2026/06-29.md` + `docs/bugs/04-marketing-effectiveness-queries-null-id-sql-grammar.md` + owner docs + P22 源计划 Successor Closed 标注）

## Deferred But Adjudicated

### 逐活动/逐场次 ROI 明细列表导出

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划导出 4 面板聚合指标（与看板展示同口径，单行/单 sheet）；逐活动/逐场次的明细行列表导出（如每场秒杀一行 ROI、每满减活动一行）为独立结果面，需新增按维度分组的明细 query（现有 4 query 仅返回单 Bean 聚合）。
- Successor Required: `yes`（触发条件：运营要求按活动/场次明细列表导出时，新增明细维度 query + 列表型 xpt 模板）

### PDF CJK 字体部署

- Classification: `watch-only residual`
- Why Not Blocking Closure: PDFBox 内置 Helvetica 仅 Latin；本计划模板表头/指标名沿用 Latin 代码先例（与 user-coupon-export 一致），导出适配层将中文指标名映射为 Latin。CJK 字体部署为部署期关注。
- Successor Required: `no`（触发条件：营销效果 PDF 需呈现中文指标名时，在 `_vfs/fonts/` 提供 TTF）

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 实现已全部完成（Phase 1/2 items 全 [x]）。`exportMarketingReport` `@BizQuery` 经 `IGraphQLEngine` 测试覆盖 xlsx/pdf/归因/空集/非法入参；app-mall-service 427 + app-mall-web 8 测试全绿；`clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS。owner doc（`system-configuration.md`/`marketing-and-promotions.md`）已标注导出交付。P22 源计划 Deferred「复杂报表导出」successor 已标 Successor Closed。**实现期发现并修复预置缺陷**：`LitemallMarketing.sql-lib.xml` 3 query 的 null-id 可选过滤（`${id} IS NULL OR ...`）在 Nop xpl-sql 下对 null 输出空文本致非法 SQL，改 `<c:if>` 条件块（口径不变，见 `docs/bugs/04-...`）。Closure Gates 中「独立闭合审计」gate 留待独立 subagent 填写下方证据后勾选。

Closure Audit Evidence:

- Reviewer / Agent: independent closure auditor (separate session/context from implementation)
- Evidence: 独立 session 重新核验全部 Exit Criteria 与 Closure Gates 对照 live repo（非信任 [x] 标记）：
  1. **后端方法非空壳：** `LitemallPromotionActivityBizModel.exportMarketingReport`（`LitemallPromotionActivityBizModel.java:134-162`）实现完整——`ReportRenderTypes.validate` 校验 renderType、4 效果 query 调用（`getPromotionEffectiveness`/`flashSaleBiz.getFlashSaleEffectiveness`/`pinTuanActivityBiz.getPinTuanEffectiveness`/`couponBiz.getCouponUsageStatistics`）、4 `buildXxxEffectDataSet` 适配为单元素 `List<Map>`、`scope.setLocalValue`、`reportEngine.getRenderer("/nop/main/report/marketing-effect.xpt.xml", safeType).generateToResource`、返回 `WebContentBean`；异常路径 `NopException.adapt(e)` + `resource.delete()`（与 `exportGoodsReport` 同模式，非空 `catch{}`）。
  2. **注入非 private：** `IReportEngine reportEngine` / `ILitemallFlashSaleBiz flashSaleBiz` / `ILitemallPinTuanActivityBiz pinTuanActivityBiz` / `ILitemallCouponBiz couponBiz`（`LitemallPromotionActivityBizModel.java:65-75`）均 `@Inject` 包级可见（非 private，符合 Nop IoC 规则）。
  3. **接口声明：** `ILitemallPromotionActivityBiz.java:50-52` `@BizQuery WebContentBean exportMarketingReport(@RequestBean MarketingReportRequest, IServiceContext)`。
  4. **RequestBean：** `MarketingReportRequest` `@DataBean`（`app-mall-dao/dto/MarketingReportRequest.java`）7 字段（renderType + 时间窗 + 4 归因 ID），与 D2 selfcheck rule #5 修订一致。
  5. **xpt 模板：** `marketing-effect.xpt.xml` 4 sheet（promotion/flashSale/pinTuan/coupon），表头 Latin，`*=^ds!field` 单对象展开，4 数据集名与 `scope.setLocalValue` 一致（promotionEffect/flashSaleEffect/pinTuanEffect/couponUsage）。
  6. **API 测试（IGraphQLEngine）：** `TestLitemallPromotionActivityBizModel` 5 新测试（`testExportMarketingReportXlsx`/`Pdf`/`WithAttribution`/`EmptyDataNotError`/`InvalidRenderType`）均经 `graphQLEngine.newRpcContext(GraphQLOperationType.query, "LitemallPromotionActivity__exportMarketingReport", ...).executeRpc`——非实体级纯逻辑测试，覆盖成功/失败模式（xlsx/pdf/归因/空集/非法入参）。
  7. **前端入口：** `marketing-effect.page.yaml:22-42` 格式选择 select（xlsx/pdf）+ `actionType:ajax responseType:blob` 按钮，`url:'@query:LitemallPromotionActivity__exportMarketingReport'`，`data.renderType` 绑定格式选择，startDate/endDate 派生自 dateRange。
  8. **预置缺陷修复落地：** `docs/bugs/04-marketing-effectiveness-queries-null-id-sql-grammar.md` 存在；非 null 路径既有测试（FlashSale/PinTuan/Coupon）无回归。
  9. **Docs sync：** `docs/logs/2026/06-29.md` 详细条目；`docs/design/system-configuration.md:414` + `marketing-and-promotions.md:854` 导出口径已更新；P22 源计划 `2026-06-28-0340-1` Successor Closed 标注（line 200）。
  10. **Deferred 诚实：** 两 deferred 项分类正确（逐活动明细 `out-of-scope improvement` + PDF CJK 字体 `watch-only residual`），无 in-scope live defect 藏 Deferred；4 效果 query 口径未改（Non-Goal 守住）。
  11. **五点一致性：** Plan Status `completed` / Phase 1-2 Status `completed` + Exit Criteria 全 [x] / Closure Gates 全 [x]（含本审计 gate）/ Closure evidence 齐全 / log entry 一致。
  - Verdict: **approved** — 所有 Exit Criteria 与 Closure Gates 对照 live repo 核验通过，无 blocker，无 major，无 minor。

Follow-up:

- 逐活动/逐场次 ROI 明细列表导出（见 Deferred But Adjudicated，触发条件：运营要求按活动/场次明细列表导出时）
