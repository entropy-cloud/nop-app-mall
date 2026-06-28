# P18 Dashboard 重做

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` §18；`docs/analysis/2026-06-21-mobile-mall-functional-design-analysis.md` A-01
> Related: `docs/plans/2026-06-28-1027-2-phase19-report-system-extension-plan.md`（successor，复用本计划建立的 stat API 基建）
> Audit: required

## Current Baseline

**现有 Dashboard 极简（"仅几个累计总数"），需重做。** 已通过 live repo 核验：

- 后台统计看板页面 `app-mall-web/.../_vfs/app/mall/pages/mall/stat/stat-dashboard.page.yaml`：仅 3 个 `service` 块，用 `tpl` 文本渲染——订单统计（总数/总金额/各状态计数）、用户统计（总数/今日/周/月新增）、商品销量排行 TOP10 表格。**无任何图表（chart）、无趋势、无同环比、无实时订单流、无待办聚合。**
- 3 个 `@BizQuery` 统计方法在 `LitemallOrderBizModel.java:909/921/935`：`getOrderStatistics` / `getGoodsSalesRanking` / `getUserStatistics`，经 `LitemallOrder.sql-lib.xml`（`getOrderStatistics`/`getGoodsSalesRanking`/`getUserStatistics`）聚合查询，返回 `OrderStatisticsBean`/`GoodsStatisticsBean`/`UserStatisticsBean`。
- 测试基线：`TestLitemallOrderStatisticsBizModel.java` 通过 `IGraphQLEngine` 验证上述 3 个查询。
- 菜单：`app-mall.action-auth.xml:191` `mall-stat-dashboard`（经营看板，orderNo 601，SUBM）。
- owner doc `system-configuration.md`「报表与统计」（line 261-290）：明确"采用 AMIS chart 组件 + 现有 GraphQL 统计 API，未引入 nop-report 引擎""复杂报表导出（PDF/Excel）需 nop-report 引擎，当前不在范围内"。
- owner doc **无** Dashboard 重做的详细设计章节（仅有基础「报表与统计」段）——本计划须先把 Dashboard 业务口径落到 `system-configuration.md`。

**差距：** 无实时核心指标卡（今日 GMV/同环比、订单数、UV、转化率、客单价、退货率）、无销售趋势图（时/天/周/月 + 同环比）、无实时订单流、无待办事项聚合（待发货/待退款/售后待审核/库存预警）。

## Goals

- 重做后台经营看板：用 AMIS chart 组件呈现实时核心指标卡 + 销售趋势图 + 实时订单流 + 待办聚合，替换现有 tpl 文本板。
- 新增支撑看板的 `@BizQuery` 统计 API（基于现有 SQL-lib 模式扩展），口径稳定、可经 `IGraphQLEngine` 测试。
- 明确各运营指标的业务口径（GMV/UV/转化率/客单价/退货率/同环比计算基准）并落入 owner doc。
- 同步「同环比」时序聚合能力（按时/天/周/月分组），为 P19 报表扩展复用。

## Non-Goals

- **nop-report 引擎引入**（见 Decision D1；PDF/Excel 导出归 P19/successor）。
- 多主题域深度报表（销售漏斗/留存/RFM/毛利等）——归 P19。
- 移动端前端看板（移动端有独立 roadmap `mobile-frontend-roadmap.md`）。
- 匿名访客 UV 精确统计（需独立访客/会话表，见 Decision D3 model-gap successor）。
- 实时推送（WebSocket / 小程序订阅消息）——本基线看板为拉取式刷新。

## Task Route

- Type: `app-layer design change` + `implementation-only change`
- Owner Docs: `docs/design/system-configuration.md`（「报表与统计」段需扩展 Dashboard 口径）
- Skill Selection Basis: 后端新增 `@BizQuery` 统计方法 → `nop-backend-dev`；前端 AMIS chart 看板页 → `nop-frontend-dev`；含新增 `@BizQuery` 方法须 `IGraphQLEngine` 测试 → `nop-testing`。本计划不触及 ORM（`model/app-mall.orm.xml`），不涉及 codegen / orm-modeler。

## Infrastructure And Config Prereqs

- 无新增基础设施。看板为拉取式刷新，依赖既有 GraphQL + AMIS。
- 待办聚合的"库存预警阈值"复用 P38 已落地的全局配置键族（`mall_stock_threshold_tight` 等，存于 `LitemallSystem`，经 `ILitemallSystemBiz.getConfig()` 读取）；若需独立"安全库存"阈值，复用同一配置机制（见 Decision D4）。

## Execution Plan

### Phase 1 - 后端看板统计 API（Add-heavy）

Status: completed
Targets: `app-mall-service/.../entity/LitemallOrderBizModel.java`、`app-mall-dao/.../mapper/LitemallOrderMapper.java`、`app-mall-dao/.../sql/LitemallOrder.sql-lib.xml`、`app-mall-api/.../biz/ILitemallOrderBiz.java`、可能新增 stat bean 类
Required Skill: `nop-backend-dev`、`nop-testing`

> 宿主说明：新 4 个看板 `@BizQuery` 沿用既有先例挂载于 `LitemallOrderBizModel`（现有 3 个 stat 方法即在此并经 `IGraphQLEngine` 测试验证，挂载路径已证明可用）。若该方法数膨胀过大，独立 stat-host 重构（含非实体 BizModel 挂载确认）作为 successor。

- Item Types: `Add | Decision | Proof`
- Prereqs: none

- [x] **Skill loading gate:** 加载 `nop-backend-dev`、`nop-testing`，逐篇读完其 routing table 标为"必读"的文档，列于下。每写完一个方法用 selfcheck 校验。
  - Docs read: `nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`、`nop-entropy/docs-for-ai/02-core-guides/service-layer.md`、`nop-entropy/docs-for-ai/02-core-guides/error-handling.md`、`nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`、`nop-entropy/docs-for-ai/05-examples/ibiz-and-bizmodel.java`
- [x] **Decision D1：可视化技术路径（AMIS chart vs nop-report）。** 抉择：**采用 AMIS chart 组件**（延续现有 baseline，`stat-dashboard.page.yaml` 已是 AMIS；AMIS 自带 chart/crd 类型足以覆盖指标卡/趋势折线/实时列表）。备选（引入 nop-report 引擎）被否——**引擎定位不对齐**：nop-report（`nop-entropy` 有 `nop-report` 模块及 `docs-for-ai/03-modules/nop-report.md`、`03-runbooks/generate-report.md`、`02-core-guides/reporting-and-notification-integration.md`）是 Excel 模板驱动的**可打印/可导出**（XPT/xlsx/pdf/html + 套打）报表引擎，而非实时看板可视化引擎；实时指标卡/趋势图/实时订单流用 AMIS chart 是正确工具。残留风险：当出现"导出 PDF/Excel 报表"硬需求时仍需 nop-report，触发条件记入 P19（报表导出场景）/successor。本抉择与 owner doc「报表与统计」line 279-282 一致；roadmap §18 所列"引入 nop-report 依赖"按引擎定位归并到 P19 导出场景（见本计划 Deferred）。
- [x] **Decision D2：核心指标口径定义。** 在 `system-configuration.md`「报表与统计」落定：今日 GMV = 当日支付订单（status≥201，含已支付及之后各态）`actualPrice`（实付费用，对应 D2 文本中 `goodsTotalPrice`，本仓实际字段名为 `actualPrice`，与现有 `getOrderStatistics` totalAmount 口径一致）之和（按 `payTime` 归属当日）；订单数 = 当日支付订单数；客单价 = GMV/订单数；退货率 = 当日售后已完成退款单数 / 当日支付订单数；销售趋势按时/天/周/月分组聚合 GMV 与订单数；同环比 = (本期-对比期)/对比期（日环比对比昨日、周同比对比上周同日）。
- [x] **Decision D3：UV / 转化率口径与匿名访客缺口。** 抉择：UV = 当日有足迹行为（`LitemallFootprint`）的去重 `userId`（下单用户必有前置浏览足迹，故以足迹为 UV 主信号）；转化率 = 当日支付用户数 / 当日 UV。备选（匿名访客 UV）被否——本基线无访客/会话表，匿名流量未持久化。残留风险：UV 不含匿名浏览，转化率偏高；精确匿名 UV 作为 model-gap successor（需新增访客表，触发条件"匿名流量分析需求出现时"，见 Deferred）。
- [x] **Decision D4：待办聚合口径与库存预警阈值来源。** 待发货=订单 status=201 计数；待退款=订单 status=202（退款中）计数；售后待审核=`LitemallAftersale.status=1`（REQUEST）计数；库存预警=在售商品聚合库存（`SUM(LitemallGoodsProduct.number)`）≤ 阈值的商品计数，阈值复用 P38 `mall_stock_threshold_tight`（全局配置，经 `ILitemallSystemBiz.getConfig()`）。备选（每商品独立安全库存字段）作为 model-gap successor（需 ORM 新增列，触发条件"按商品粒度安全库存需求出现时"，见 Deferred）。
- [x] **Add：** `getDashboardMetrics(@BizQuery)`：返回今日核心指标卡（todayGmv/yesterdayGmv/lastWeekGmv/gmvDayRatio/gmvWeekRatio、orderCount、uv、conversionRate、aov、returnRate），跨实体经各自 `I*Biz`（订单/售后/足迹）聚合，禁止 `daoProvider().daoFor()` 绕过。
- [x] **Add：** `getSalesTrend(@BizQuery)`：按 `granularity`(hour/day/week/month) + 时间区间返回时序点（dateLabel/gmv/orderCount），SQL-lib 分组聚合。
- [x] **Add：** `getRealtimeOrders(@BizQuery)`：返回最近 N 条订单流（orderSn/userId/gmvStatus/addTime），分页或 limit。
- [x] **Add：** `getTodoAggregation(@BizQuery)`：返回待办四项计数（pendingShip/pendingRefund/aftersalePendingReview/stockWarning）+ 库存预警明细（商品名/聚合库存）。
- [x] **Add：** 对应 SQL-lib 查询（`getDashboardGmv`/`getPaidOrderPoints`/`getStockWarningList` 等）落 `LitemallOrder.sql-lib.xml`（及售后/足迹侧查询走各自 sql-lib 或 `I*Biz`）。
- [x] **Proof：** 每个新增 `@BizQuery` 方法经 `IGraphQLEngine`（`JunitBaseTestCase`）测试，覆盖空数据 / 有数据 / 跨日边界；扩充 `TestLitemallOrderStatisticsBizModel`。

Exit Criteria:

- [x] 4 个新增看板 `@BizQuery` 方法可经 GraphQL 调用返回符合 D2/D3/D4 口径的数据（成功 + 空数据 + 边界模式）。
- [x] **API 测试：** 新增 `@BizQuery` 方法通过 `IGraphQLEngine` 测试（`JunitBaseTestCase` 8 项全绿）。
- [x] `docs/design/system-configuration.md`「报表与统计」补 Dashboard 指标口径（D2/D3/D4）。
- [x] `docs/logs/` 更新。

### Phase 2 - 前端看板重做（Add-heavy）

Status: completed
Targets: `app-mall-web/.../_vfs/app/mall/pages/mall/stat/stat-dashboard.page.yaml`（重做）、i18n
Required Skill: `nop-frontend-dev`

- Item Types: `Add | Proof`
- Prereqs: Phase 1 完成

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完 routing table 必读文档，列于下；每个页面文件完成后 selfcheck。
  - Docs read: `nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`、`nop-entropy/docs-for-ai/02-core-guides/delta-customization.md`、`nop-entropy/docs-for-ai/00-start-here/application-project-defaults.md`；项目样例 `marketing-effect.page.yaml`
- [x] **Add：** 重做 `stat-dashboard.page.yaml`：核心指标卡区（消费 `getDashboardMetrics`，展示 GMV + 同环比箭头/订单数/UV/转化率/客单价/退货率）、销售趋势图（AMIS `chart` 折线/柱状，消费 `getSalesTrend`，granularity 切换）、实时订单流列表（消费 `getRealtimeOrders`）、待办聚合区（消费 `getTodoAggregation`，待办计数 + 库存预警明细）。
- [x] **Add：** 看板刷新机制（拉取式 reload，时间筛选 / granularity 切换联动各 service 的 `sendOn`/`reload`），与现有看板刷新模式一致。
- [x] **Add：** i18n 标签补齐（`mall-stat-dashboard` 命名空间既有，按现有模式扩展）。
- [x] **Proof：** 前端页面渲染冒烟（参照既有 e2e 页面渲染验证模式），确认 4 区块正确消费 GraphQL 并渲染。

Exit Criteria:

- [x] 重做后的看板页面 4 区块（指标卡/趋势图/实时订单流/待办聚合）可渲染并消费对应 `@BizQuery`。
- [x] 现有 `mall-stat-dashboard` 菜单入口仍可达，向后兼容（不破坏既有路由）。
- [x] `docs/logs/` 更新。

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗式审计，3 轮
- Evidence:
  - Round 1 `ses_0f3ee5bf2ffeGFdwV8wKN10oMw`：MAJOR_OBJECTION——D1 以"nop-report 无 docs-for-ai"为由延后，事实有误（docs 实际存在）。已据此修订 D1 为引擎定位理由（nop-report 为打印/导出引擎非看板可视化引擎），并将 roadmap §18"引入 nop-report"显式 adjudicate 至 Deferred。
  - Round 2 `ses_0f3e5cc57ffeOGzSAZsapQTfFu`：PASS——D1 已据实重述、roadmap 交付项未静默丢弃、Phase 2 去掉 nop-testing、验证命令对齐 project-context。
  - Round 3（终审）`ses_0f3e0d453ffeV3FC1jAmUNVWL2`：PASS——baseline 全量复核通过，连续两轮 clean，consensus 达成。
  - 结论：无 blocker、无 major objection；D2/D3/D4 口径与 I*Biz 跨实体约束、规则 #15（IGraphQLEngine）均满足。

## Closure Gates

- [x] in-scope behavior is complete（4 指标区 + 4 stat API 全部落地）
- [x] relevant docs are aligned（`system-configuration.md` 口径已落）
- [x] verification has run（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS + `./mvnw test` 全工作区 281 全绿 + `./mvnw -pl app-mall-web -am -DskipTests compile` BUILD SUCCESS）
- [x] all new `@BizQuery` methods tested via `IGraphQLEngine`（`TestLitemallOrderStatisticsBizModel` 8 例覆盖 4 新方法 + 空数据/边界）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop 平台 phase 未写 `none`
- [x] skill loading verification：各 phase 已扫可用 skill、加载匹配 skill、读完 routing 必读文档（路径列于 skill loading gate）、每方法/页面 selfcheck
- [x] text consistency verified：status/phases/gates/log 一致
- [x] closure audit was performed by a different agent/session（independent closure auditor session，见 Closure 段证据）
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 匿名访客 UV / 精确转化率（D3）

- Classification: `model-gap`
- Why Not Blocking Closure: 登录用户 UV（足迹+下单去重）已可表达看板转化语义；匿名访客流量本基线未持久化，应用层无替代路径会失真，故不强行拼凑。
- Successor Required: yes
- Model Gap Detail: 缺少访客/会话表（如 `LitemallVisit`/session 记录），无法统计匿名 UV。建议新增访客记录实体（userId 可空 + 访问时间 + 页面）。触发条件：匿名流量分析或精确全站转化率需求出现时。

### 每商品独立安全库存字段（D4）

- Classification: `model-gap`
- Why Not Blocking Closure: 全局阈值（复用 P38 `mall_stock_threshold_tight`）已可驱动看板库存预警计数；按商品粒度安全库存为运营增强，不影响看板正确性。
- Successor Required: yes
- Model Gap Detail: `LitemallGoods` 缺 `safetyStock` 列。建议新增可空 int 列。触发条件：按商品粒度安全库存预警需求出现时（与 P36 商品运营增强的库存预警可合并落地）。
- **已由 successor 关闭（2026-06-29）：** `docs/plans/2026-06-29-1045-2-goods-level-safety-stock-plan.md` 已完成。`LitemallGoods.safetyStock`（propId=24，可空 INTEGER）已落地；P18 Dashboard `getTodoAggregation` SQL-lib `HAVING COALESCE(NULLIF(g.SAFETY_STOCK,0), ${threshold})` 使 goods 聚合级阈值生效；`StockWarningItemBean` 扩展 `safetyStock`/`thresholdSource`。

### nop-report 引擎引入（D1）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: Dashboard 可视化用 AMIS chart 完整覆盖；nop-report 价值在可导出报表，属 P19 导出场景，非看板阻塞项。
- Successor Required: yes
- **Successor Closed（2026-06-29）**: 已由 successor 计划 `docs/plans/2026-06-28-2352-1-nop-report-engine-introduction-plan.md` 关闭——引入 `nop-report-core`+`nop-report-pdf` + 4 个 `.xpt.xml` 模板 + `exportGoodsReport`/`exportReport` xlsx/pdf 导出能力。

## Closure

<!-- 闭合审计须由独立 subagent（不同 session）执行，此处留给闭合审计员填写。 -->

Status Note: 实现完成（Phase 1 后端 + Phase 2 前端），全量验证通过（281 测试全绿 + uber-jar 构建 + web 编译）。独立 closure audit 已由不同 session 执行并通过（见下）。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure auditor（不同 session/context，非实现 agent）
- Audit Verdict: **APPROVED**（无 blocker、无 major objection）
- Live-Repo Re-verification（逐项核验，非信任 [x]）：
  - 后端 4 个新 `@BizQuery`：`ILitemallOrderBiz.java:116/122/131/138` 接口声明 + `LitemallOrderBizModel.java:974/1020/1062/1074` 实现，均 `@BizQuery @Override`，跨实体经 `ILitemallAftersaleBiz`/`ILitemallSystemBiz`（非 `daoProvider().daoFor()` 绕过）。
  - SQL-lib 3 查询：`LitemallOrder.sql-lib.xml:83/110/125`（`getDashboardGmv`/`getPaidOrderPoints`/`getStockWarningList`）+ `LitemallOrderMapper.java:38/41/44`，运行时由 `LitemallOrderBizModel.java:986-988/1035/1091` 实际调用（非空挂）。
  - 前端 4 区块：`stat-dashboard.page.yaml` 4 个 `service` 块分别 `@query:LitemallOrder__getDashboardMetrics/getSalesTrend/getRealtimeOrders/getTodoAggregation` 消费，AMIS `chart` 趋势 + `table` 订单流/库存明细 + 指标卡 grid + 待办计数均实渲染（Anti-Hollow：无空 body、无 `return null` 占位、无未达 service）。
  - 测试：`TestLitemallOrderStatisticsBizModel.java` `extends JunitBaseTestCase` + `IGraphQLEngine`（非实体级纯逻辑测试），8 例覆盖 4 新方法（含 hour/day granularity + 边界 + 空数据），满足规则 #15。
  - owner doc：`system-configuration.md:280/285`「经营看板指标口径（P18）」章节落 D2/D3/D4 全口径。
  - 日志：`docs/logs/2026/06-28.md:1-29` Phase 18 段含交付内容/关键决策/已解决坑。
  - 菜单向后兼容：`stat-dashboard.page.yaml` 路径未变，`mall-stat-dashboard` 入口仍可达。
- Five-Point Consistency：Plan Status(completed) / Phase 1 Status(completed) / Phase 2 Status(completed) / 全部 Exit Criteria([x]) / 全部 Closure Gates([x]) / 日志 —— 全部一致。
- Deferred Honesty：D1(nop-report, out-of-scope→P19)/D3(匿名 UV, model-gap)/D4(每商品安全库存, model-gap) 三项均明确 Classification + Successor + 触发条件，无 in-scope live defect 隐藏为 deferred。
- Anti-Slacking：无 `optional/consider/maybe/nice to have` 禁词用于 in-scope 项。

Follow-up:

- nop-report 引擎引入（当 PDF/Excel 报表导出需求落地时，归 P19 续作）。
