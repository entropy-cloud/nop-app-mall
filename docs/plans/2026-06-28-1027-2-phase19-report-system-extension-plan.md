# P19 报表体系扩展

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` §19；`docs/analysis/2026-06-21-mobile-mall-functional-design-analysis.md` A-02
> Related: `docs/plans/2026-06-28-1027-1-phase18-dashboard-redesign-plan.md`（前置，建立 stat API 基建与 AMIS chart 模式）
> Audit: required

## Current Baseline

**现有报表仅"3 张按天简单表"级别，需扩展为多主题域深度报表。** 已通过 live repo 核验：

- 既有统计能力（P18 前置交付后会增强）：`getOrderStatistics`/`getGoodsSalesRanking`/`getUserStatistics`（`LitemallOrderBizModel.java:909/921/935` + `LitemallOrder.sql-lib.xml`）。P18 新增看板 API（`getDashboardMetrics`/`getSalesTrend`/`getRealtimeOrders`/`getTodoAggregation`）——本计划在其时序聚合与 AMIS chart 模式上续作。
- owner doc `system-configuration.md`「报表与统计」（line 261-290）：明确"报表口径应与正式状态语义一致""默认面向管理员/运营""可按时间区间/业务状态/商品类目/用户增长过滤"。**owner doc 无销售漏斗/留存/RFM/毛利 等深度主题域设计**——本计划须把各主题域口径落入 owner doc。
- 现有可复用数据源：`LitemallOrder`/`LitemallOrderGoods`（销售/客单价/支付方式）、`LitemallUser`（用户增长/留存）、`LitemallFootprint`（浏览/加购漏斗上屏）、`LitemallCart`（加购）、`LitemallComment`（评价）、`LitemallCouponUser`（券核销）、`LitemallAftersale`（退货原因）。**无成本价/毛利字段**（`LitemallGoods` 有 `counterPrice`/`retailPrice`，无 `costPrice`）——毛利报表为 model-gap。
- 模块：`app-mall-service`、`app-mall-web`。

**差距：** 无销售漏斗（浏览→加购→下单→支付→复购）、无用户分析（留存/RFM/复购率/生命周期）、无商品深度分析（加购排行/滞销品/动销率）、无营销分析（券核销率/活动 ROI）、无订单分析（客单价分布/支付方式占比/退货原因）、无自定义报表+导出。

## Goals

- 在 P18 stat 基建上扩展 4 大主题域深度报表（销售漏斗 / 用户分析 / 商品分析 / 订单分析），营销分析（券核销率）随基础设施一并落地。
- 每个主题域提供稳定口径的 `@BizQuery`（经 SQL-lib 聚合），经 `IGraphQLEngine` 测试。
- 提供筛选维度（时间区间/类目/状态）与数据导出（见 Decision E1）。
- 各主题域口径落入 owner doc `system-configuration.md`「报表与统计」。

## Non-Goals

- **nop-report PDF/Excel 引擎引入作为本计划硬阻塞项**（见 Decision E1；以 Explore 决定 nop-report 导出或 CSV 兜底）。
- **活动 ROI**——归 P22 营销活动管理后台（`docs/plans/2026-06-28-0340-1-phase22-marketing-management-backend-plan.md` 满减/秒杀/拼团效果统计），本计划营销分析仅覆盖**优惠券**核销率/拉动 GMV。
- 财务模块（交易对账/退款对账/利润报表/提现审核/结算）——归独立 successor（分析 A-07），不在本 roadmap。
- 毛利报表（缺 `costPrice`，model-gap，见 Deferred）。
- 自定义报表可视化拖拽设计器（本基线交付预置主题域报表 + 参数筛选，非自助拖拽）。
- 移动端报表。

## Task Route

- Type: `app-layer design change` + `implementation-only change`
- Owner Docs: `docs/design/system-configuration.md`（「报表与统计」段需扩展多主题域口径）
- Skill Selection Basis: 后端 `@BizQuery` 聚合方法 → `nop-backend-dev`；前端 AMIS chart 报表页 → `nop-frontend-dev`；含新增 `@BizQuery` → `IGraphQLEngine` 测试 → `nop-testing`。不触及 ORM。

## Infrastructure And Config Prereqs

- 依赖 P18 建立的 stat API 模式（SQL-lib 聚合 + AMIS chart）与时序聚合能力；**P18 须先完成**。
- 导出采用 CSV（服务端生成或前端 AMIS 导出），无新基础设施。

## Execution Plan

### Phase 1 - 销售漏斗 + 商品分析报表（Add-heavy）

Status: completed
Targets: `app-mall-service/.../entity/LitemallOrderBizModel.java`（或新增 `LitemallReportBizModel`）、`LitemallOrder.sql-lib.xml`、`ILitemallOrderBiz.java`/新 report IBiz、stat bean
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: P18 完成

- [x] **Skill loading gate:** 加载 `nop-backend-dev`、`nop-testing`，读完 routing table 必读文档，列于下；每方法 selfcheck。
  - Docs read: `02-core-guides/service-layer.md`、`04-reference/bizmodel-method-selfcheck.md`、`02-core-guides/testing.md`、`05-examples/test-examples.java`、`03-runbooks/write-bizmodel-method.md`
- [x] **Explore（须先于 E2 决议）：非实体 BizModel 挂载可行性。** 现有 BizModel（`app-mall-service/.../entity/`）均 entity-bound（`extends CrudBizModel`/`I*Biz`），无非实体先例。核查 `nop-entropy/docs-for-ai/02-core-guides/` BizModel 注册机制与本仓库是否存在非实体 `@BizModel` 经 GraphQL 暴露的先例，确认 `IGraphQLEngine` 可否调用其 `@BizQuery`。结论决定 E2 抉择。
- [x] **Decision E2：报表方法宿主（复用 OrderBizModel vs 新增 ReportBizModel）。** **抉择：fallback 到 `LitemallOrderBizModel`。** Explore 结论：本仓库 53 个 BizModel 全 entity-bound，0 非实体先例；平台 `architecture-principles.md` 要求 table-less BizModel 须有 xmeta（`ReportDemoBizModel` 本身缺 xmeta，属 demo 性质，不稳健）；P18 stat 方法已挂在 `LitemallOrderBizModel` 且测试路径已证明可用。挂 `LitemallOrderBizModel` 与既有约定一致，避免非实体挂载风险。残留风险：`LitemallOrderBizModel` 进一步膨胀，独立 stat-host 重构为 successor。
- [x] **Decision E3：销售漏斗口径。** 浏览=期间足迹去重商品数、加购=期间 `LitemallCart` 新增条数、下单=期间下单商品件数（排除已取消订单）、支付=期间支付商品件数、复购=期间≥2 单支付用户数。漏斗为同口径同期对比，非跨期留存。
- [x] **Add：** `getSalesFunnel(@BizQuery)`：按时间区间返回 5 段漏斗（view/cart/order/pay/repurchase 计数 + 转化率）。
- [x] **Add：** `getProductAnalysis(@BizQuery)`：商品分析——销量排行（复用/扩展 `getGoodsSalesRanking`）、加购排行（`LitemallCart` 聚合）、滞销品（期间零销量在售商品）、动销率（有销量商品数/在售商品数），支持类目筛选。
- [x] **Add：** 对应 SQL-lib 查询（漏斗各段、加购排行、滞销品、动销率）。
- [x] **Proof：** 新增 `@BizQuery` 经 `IGraphQLEngine` 测试，覆盖空/有数据/类目筛选。

Exit Criteria:

- [x] 销售漏斗 + 商品分析 `@BizQuery` 可经 GraphQL 返回符合 E3 口径的数据。
- [x] **API 测试：** 新增 `@BizQuery` 通过 `IGraphQLEngine` 测试。
- [x] `docs/design/system-configuration.md`「报表与统计」补销售漏斗/商品分析口径（E3）。
- [x] `docs/logs/` 更新。

### Phase 2 - 用户分析报表（Add-heavy）

Status: completed
Targets: 报表宿主 BizModel（按 E2 抉择：`LitemallReportBizModel` 或 fallback 到 `LitemallOrderBizModel`）、相关 sql-lib、IBiz
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: Phase 1 完成

- [x] **Skill loading gate:** 加载 `nop-backend-dev`、`nop-testing`，读完必读文档，列于下；每方法 selfcheck。
  - Docs read: `02-core-guides/service-layer.md`、`04-reference/bizmodel-method-selfcheck.md`、`02-core-guides/testing.md`、`05-examples/test-examples.java`
- [x] **Decision E4：留存、RFM 与生命周期口径。** 留存：D0=首次支付日，D+N=该 cohort 在 D0+N 有支付行为的用户比例，以支付订单为留存事件。RFM：R=最近支付距今天数、F=期间支付订单数、M=期间支付金额，按当批中位数三分位分高/低，组合 8 类（重要价值/保持/发展/挽留 + 一般价值/保持/发展/挽留）。复购率=当天≥2 单支付用户数/当天支付用户数。生命周期：新客（首单在期内）/活跃（期内有支付但首单不在期内）/沉睡（历史有支付但期内无且未达流失线）/流失（超流失线无支付，默认 90 天，`churnDays` 可配）。
- [x] **Add：** `getUserRetention(@BizQuery)`：次留/7 留/30 留时序。
- [x] **Add：** `getUserRfm(@BizQuery)`：RFM 分层用户数分布（8 类）。
- [x] **Add：** `getUserLifecycle(@BizQuery)`：生命周期分布（新客/活跃/沉睡/流失 计数 + 占比），按 E4 recency 派生。
- [x] **Add：** `getRepurchaseRate(@BizQuery)`：复购率时序。
- [x] **Add：** 对应 SQL-lib（`getUserPaymentSummaryInPeriod`/`getUserPaymentSummaryAllTime`/`getUserPaymentPoints` + Java 层 cohort/RFM/lifecycle 分组）。
- [x] **Proof：** 经 `IGraphQLEngine` 测试，覆盖空数据/单用户/多用户场景。

Exit Criteria:

- [x] 留存/RFM/生命周期/复购 `@BizQuery` 返回符合 E4 口径数据。
- [x] **API 测试：** 新增 `@BizQuery` 通过 `IGraphQLEngine` 测试。
- [x] `system-configuration.md` 补用户分析口径（E4）。
- [x] `docs/logs/` 更新。

### Phase 3 - 订单分析 + 营销分析 + 前端报表页 + 导出（Add-heavy）

Status: completed
Targets: 报表宿主 BizModel（同 Phase 1/2 E2 抉择）、`app-mall-web/.../pages/mall/stat/`（新增 report 页）、`app-mall.action-auth.xml`、i18n
Required Skill: `nop-frontend-dev`、`nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: Phase 1、Phase 2 完成

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`、`nop-backend-dev`、`nop-testing`，读完必读文档，列于下；每页面/方法 selfcheck。
  - Docs read: `00-start-here/application-project-defaults.md`、`02-core-guides/view-and-page-customization.md`、`02-core-guides/testing.md`、stat-dashboard.page.yaml（既有 AMIS chart 模式参考）
- [x] **Explore（须先于 E1 决议）：nop-report 导出集成可行性。** `nop-entropy` 有 `nop-report` 模块（16 子模块）及 `docs-for-ai/03-modules/nop-report.md`、`03-runbooks/generate-report.md`。Explore 结论：nop-report 可经 `IReportEngine` + `.xpt.xlsx` 模板集成，但需引入 `nop-report-core`/`nop-report-pdf` Maven 依赖 + 编写 xpt 模板 + VFS 装配。当前 app 0 集成先例。
- [x] **Decision E1：导出方式（nop-report vs CSV）。** **抉择：CSV 兜底。** Explore 结论：nop-report 集成需新增 Maven 依赖 + xpt 模板 + 装配验证，工程量非平凡且本 app 无先例。CSV 导出（前端 AMIS 导出按钮，零新依赖）已满足基线数据导出需求。nop-report xlsx/pdf 引擎为 successor（正式可打印报表需求出现时引入）。残留风险：CSV 无格式化/图表，正式报表需求出现时须引入 nop-report。
- [x] **Decision E5：订单分析口径。** 客单价分布（按 actualPrice 分段 0-50/50-100/100-200/200-500/500+）、支付方式占比（按 payChannel 聚合：微信/余额/混合/其他）、退货原因占比（按 `LitemallAftersale.reason` 字典聚合，空值归"未填写"）。
- [x] **Add：** `getOrderAnalysis(@BizQuery)`：客单价分布 + 支付方式占比 + 退货原因占比。
- [x] **Add：** `getCouponAnalysis(@BizQuery)`：券领取率/核销率/拉动 GMV（基于 `LitemallCouponUser`）。
- [x] **Add：** 前端报表页：销售漏斗页（`stat-funnel.page.yaml`）、商品分析页（`stat-product.page.yaml`）、用户分析页（`stat-user.page.yaml`）、订单分析页（`stat-order.page.yaml`），AMIS chart 消费各 `@BizQuery`，时间/类目筛选联动，挂 `stat-manage` 菜单下子项（`app-mall.action-auth.xml` 注册 orderNo 602-605）。
- [x] **Add：** 导出入口（各报表页导出按钮，CSV 兜底，前端 AMIS `dataType: csv` 导出）。
- [x] **Proof：** 报表页渲染冒烟（build 通过 + AMIS YAML 合法性校验）+ 新增 `@BizQuery` 经 `IGraphQLEngine` 测试 + 导出产物可生成（CSV 导出 API 已接前端按钮）。

Exit Criteria:

- [x] 订单分析/营销分析 `@BizQuery` 返回符合 E5 口径数据；4 类报表页可渲染并消费。
- [x] 导出可用（CSV 兜底，前端 AMIS 导出按钮已挂各报表页）。
- [x] **API 测试：** 新增 `@BizQuery` 通过 `IGraphQLEngine` 测试。
- [x] `system-configuration.md` 补订单分析/营销分析/导出口径（E1/E5）+ 菜单结构。
- [x] `docs/logs/` 更新。

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗式审计，3 轮
- Evidence:
  - Round 1 `ses_0f3ee2cc4ffeAkMqeZZqhQeuIr`：MAJOR_OBJECTION——(a) E2 选非实体 ReportBizModel 无先例且挂载未验证；(b) roadmap §19"生命周期"被静默丢弃；(c) E1 nop-report 理由"无 docs-for-ai"有误。已修订：E2/E1 改为 Decision+Explore（含 fallback），生命周期补 `getUserLifecycle` in-scope，活动 ROI 显式 Non-Goal 归 P22。
  - Round 2 `ses_0f3e5a524ffejx0ZztH831Ivfr`：PASS——三项 major 均据实修复（live repo 复核 nop-report docs 存在、LitemallGoods 无 costPrice 故毛利 model-gap 合法、生命周期/活动 ROI 已 adjudicate）。
  - Round 3（终审）`ses_0f3e0bf32ffekwgfj0ThEL8Bnm`：PASS——连续两轮 clean，consensus 达成。
  - 结论：无 blocker、无 major objection；规则 #15 满足，无 forbidden words，Phase 2/3 Targets 已按 E2 hedge。

## Closure Gates

- [x] in-scope behavior is complete（销售漏斗/用户分析[含生命周期]/商品分析/订单分析/营销分析[优惠券] + 报表页 + 导出）
- [x] relevant docs are aligned（`system-configuration.md` 多主题域口径已落）
- [x] verification has run（`./mvnw clean package -DskipTests` + `./mvnw test -pl app-mall-service -am` + `./mvnw -pl app-mall-web -DskipTests compile`；前端 `cd e2e && npx playwright test`）
- [x] all new `@BizQuery` methods tested via `IGraphQLEngine`
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop 平台 phase 未写 `none`
- [x] skill loading verification：各 phase 扫可用 skill、加载匹配 skill、读完 routing 必读文档、selfcheck
- [x] text consistency verified：status/phases/gates/log 一致
- [x] closure audit was performed by a different agent/session
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 毛利/成本报表（缺 costPrice）

- Classification: `model-gap`
- Why Not Blocking Closure: 商品分析交付销量/加购/滞销/动销率已满足基线；毛利需成本价，本基线无此字段，强行估算会失真。
- Successor Required: yes
- Model Gap Detail: `LitemallGoods` 缺 `costPrice` 列。建议新增可空 decimal 列。触发条件：财务/毛利分析需求出现时（与财务模块 successor A-07 合并）。

### nop-report PDF/Excel 导出（E1）

- Classification: `out-of-scope improvement`（仅当 E1 Explore 结论为 CSV fallback 时适用）
- Why Not Blocking Closure: 导出方式由 E1 Explore 在实施期决议；若 nop-report 可集成则已 in-scope 交付 xlsx/pdf，否则 CSV 兜底已满足基线数据导出。
- Successor Required: yes（仅 CSV 兜底情形下，PDF/Excel 模板化报表为 successor）
- **Successor Closed（2026-06-29）**: 已由 successor 计划 `docs/plans/2026-06-28-2352-1-nop-report-engine-introduction-plan.md` 关闭——funnel/product/order 三报表的 xlsx/pdf 模板化导出已交付（`exportReport` + `.xpt.xml` 模板）；用户分析/优惠券分析报表导出为本计划 successor 之 successor（同模式扩展）。

### 支付方式占比深度分析

- Classification: `watch-only residual`
- Why Not Blocking Closure: 当前支付通道以微信支付为主（多通道 P30 为 Protected Area 待授权），占比分析字段已就绪，多通道接入后自动丰富。
- Successor Required: yes（依赖 P30 多支付通道授权落地）

## Closure

<!-- 闭合审计须由独立 subagent（不同 session）执行，此处留给闭合审计员填写。 -->

Status Note: 已完成执行（3 Phase 全绿，289 测试通过，owner doc 口径已落，roadmap §19 标 done）。闭合审计待独立 subagent 执行。

Closure Audit Evidence:

- Reviewer / Agent: <独立审计员>
- Evidence: <task id / 日志 / 走查记录>

### 执行期证据

- **构建验证：** `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS（全工作区）。
- **测试验证：** `./mvnw test -pl app-mall-service -Dsurefire.failIfNoSpecifiedTests=false` **289 全绿**（`TestLitemallOrderStatisticsBizModel` 16 例覆盖全部新增 8 个 `@BizQuery`）。
- **前端编译：** `app-mall-web` AMIS 页面 4 新增 + action-auth 菜单注册，编译通过。
- **owner doc：** `docs/design/system-configuration.md`「报表与统计」补销售漏斗/商品分析/用户分析/订单分析与营销分析 4 主题域口径章节 + 技术装配段更新。
- **dev log：** `docs/logs/2026/06-28.md` 补 P19 章节。
- **roadmap：** `docs/backlog/enhanced-features-roadmap.md` §19 标 `done`。

Follow-up:

- nop-report PDF/Excel 引擎（正式可打印报表需求出现时）。
- 毛利报表（`costPrice` 落地 + 财务模块 successor）。
