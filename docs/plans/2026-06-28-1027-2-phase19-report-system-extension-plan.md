# P19 报表体系扩展

> Plan Status: active
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

Status: planned
Targets: `app-mall-service/.../entity/LitemallOrderBizModel.java`（或新增 `LitemallReportBizModel`）、`LitemallOrder.sql-lib.xml`、`ILitemallOrderBiz.java`/新 report IBiz、stat bean
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: P18 完成

- [ ] **Skill loading gate:** 加载 `nop-backend-dev`、`nop-testing`，读完 routing table 必读文档，列于下；每方法 selfcheck。
  - Docs read: <执行时填写>
- [ ] **Explore（须先于 E2 决议）：非实体 BizModel 挂载可行性。** 现有 BizModel（`app-mall-service/.../entity/`）均 entity-bound（`extends CrudBizModel`/`I*Biz`），无非实体先例。核查 `nop-entropy/docs-for-ai/02-core-guides/` BizModel 注册机制与本仓库是否存在非实体 `@BizModel` 经 GraphQL 暴露的先例，确认 `IGraphQLEngine` 可否调用其 `@BizQuery`。结论决定 E2 抉择。
- [ ] **Decision E2：报表方法宿主（复用 OrderBizModel vs 新增 ReportBizModel）。** 抉择依据上一步 Explore：**若**非实体 BizModel 可干净挂载并经 `IGraphQLEngine` 测试 → 新增独立 `LitemallReportBizModel`（单一职责，避免 `LitemallOrderBizModel` 膨胀）；**否则（fallback）** → 挂载于 `LitemallOrderBizModel`（与现有 3 stat 方法 + P18 新增 4 看板方法同一先例，测试路径已证明可用，接受膨胀）。备选（默认新增 ReportBizModel 而不验证挂载）被否——无先例的挂载方式会使全 plan 的 `IGraphQLEngine` 测试策略落空。残留风险：fallback 路径下 `LitemallOrderBizModel` 进一步膨胀，独立 stat-host 重构为 successor。
- [ ] **Decision E3：销售漏斗口径。** 浏览=期间足迹去重商品数（或 PV）、加购=期间 `LitemallCart` 新增条数、下单=期间下单商品件数、支付=期间支付商品件数、复购=期间≥2 单支付用户数。漏斗为同口径同期对比，非跨期留存。
- [ ] **Add：** `getSalesFunnel(@BizQuery)`：按时间区间返回 5 段漏斗（view/cart/order/pay/repurchase 计数 + 转化率）。
- [ ] **Add：** `getProductAnalysis(@BizQuery)`：商品分析——销量排行（复用/扩展 `getGoodsSalesRanking`）、加购排行（`LitemallCart` 聚合）、滞销品（期间零销量在售商品）、动销率（有销量商品数/在售商品数），支持类目筛选。
- [ ] **Add：** 对应 SQL-lib 查询（漏斗各段、加购排行、滞销品、动销率）。
- [ ] **Proof：** 新增 `@BizQuery` 经 `IGraphQLEngine` 测试，覆盖空/有数据/类目筛选。

Exit Criteria:

- [ ] 销售漏斗 + 商品分析 `@BizQuery` 可经 GraphQL 返回符合 E3 口径的数据。
- [ ] **API 测试：** 新增 `@BizQuery` 通过 `IGraphQLEngine` 测试。
- [ ] `docs/design/system-configuration.md`「报表与统计」补销售漏斗/商品分析口径（E3）。
- [ ] `docs/logs/` 更新。

### Phase 2 - 用户分析报表（Add-heavy）

Status: planned
Targets: 报表宿主 BizModel（按 E2 抉择：`LitemallReportBizModel` 或 fallback 到 `LitemallOrderBizModel`）、相关 sql-lib、IBiz
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: Phase 1 完成

- [ ] **Skill loading gate:** 加载 `nop-backend-dev`、`nop-testing`，读完必读文档，列于下；每方法 selfcheck。
  - Docs read: <执行时填写>
- [ ] **Decision E4：留存、RFM 与生命周期口径。** 留存：次留/7 留/30 留 = 首次行为日 D0 用户在 D+1/D+7/D+30 仍有支付行为的比例（以支付订单为留存事件）。RFM：R=最近支付距今天数、F=期间支付订单数、M=期间支付金额，按阈值（后台可配或均匀分段）分高/中/低三分位。复购率=期间≥2 单支付用户数/期间支付用户数。**生命周期**（roadmap §19 用户分析显式列项，不得静默丢弃）：新客（首单在期内）/活跃（期内有支付）/沉睡（历史有支付但期内无且未达流失线）/流失（超流失线无支付，默认 90 天，后台可配），按订单 recency 派生分布。
- [ ] **Add：** `getUserRetention(@BizQuery)`：次留/7 留/30 留时序。
- [ ] **Add：** `getUserRfm(@BizQuery)`：RFM 分层用户数分布（8 类或三分位简化）。
- [ ] **Add：** `getUserLifecycle(@BizQuery)`：生命周期分布（新客/活跃/沉睡/流失 计数 + 占比），按 E4 recency 派生。
- [ ] **Add：** `getRepurchaseRate(@BizQuery)`：复购率时序。
- [ ] **Add：** 对应 SQL-lib（留存按 D0/D+N 自连接聚合、RFM 分段聚合）。
- [ ] **Proof：** 经 `IGraphQLEngine` 测试，覆盖单用户/多用户/无复购场景。

Exit Criteria:

- [ ] 留存/RFM/生命周期/复购 `@BizQuery` 返回符合 E4 口径数据。
- [ ] **API 测试：** 新增 `@BizQuery` 通过 `IGraphQLEngine` 测试。
- [ ] `system-configuration.md` 补用户分析口径（E4）。
- [ ] `docs/logs/` 更新。

### Phase 3 - 订单分析 + 营销分析 + 前端报表页 + 导出（Add-heavy）

Status: planned
Targets: 报表宿主 BizModel（同 Phase 1/2 E2 抉择）、`app-mall-web/.../pages/mall/stat/`（新增 report 页）、`app-mall.action-auth.xml`、i18n
Required Skill: `nop-frontend-dev`、`nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: Phase 1、Phase 2 完成

- [ ] **Skill loading gate:** 加载 `nop-frontend-dev`、`nop-backend-dev`、`nop-testing`，读完必读文档，列于下；每页面/方法 selfcheck。
  - Docs read: <执行时填写>
- [ ] **Explore（须先于 E1 决议）：nop-report 导出集成可行性。** `nop-entropy` 有 `nop-report` 模块及 `docs-for-ai/03-modules/nop-report.md`、`03-runbooks/generate-report.md`、`02-core-guides/reporting-and-notification-integration.md`。按 runbook 核查：引入 `nop-report-core`/`nop-report-service` 依赖后能否在本仓库产出 xlsx/pdf 报表（XPT 模板或代码生成），以及与 Quarkus 装配是否冲突。结论决定 E1。
- [ ] **Decision E1：导出方式（nop-report vs CSV）。** 抉择依据上一步 Explore：**若** nop-report 可干净集成（roadmap §19 Platform Reuse 已将其列为本特性复用项）→ 引入 nop-report 提供 xlsx/pdf 导出（Excel 模板化报表，nop-report 的正确定位）；**否则（fallback）** → CSV 导出（前端 AMIS 导出或服务端生成，零新依赖）。备选（默认 CSV 并声称 nop-report 不可用）被否——nop-report `docs-for-ai` 实际存在，须按 Explore 结论抉择而非预设。残留风险：nop-report 集成若装配复杂，CSV 作为兜底不阻塞报表主体交付。
- [ ] **Decision E5：订单分析口径。** 客单价分布（按金额分段计数）、支付方式占比（按订单支付方式聚合；本基线主要微信支付，多通道接入 P30 后丰富）、退货原因占比（按 `LitemallAftersale.reason` 字典聚合）。
- [ ] **Add：** `getOrderAnalysis(@BizQuery)`：客单价分布 + 支付方式占比 + 退货原因占比。
- [ ] **Add：** `getCouponAnalysis(@BizQuery)`：券领取率/核销率/拉动 GMV（基于 `LitemallCouponUser`）。
- [ ] **Add：** 前端报表页：销售漏斗页、用户分析页、商品分析页、订单分析页（AMIS chart 消费各 `@BizQuery`，时间/类目筛选联动），挂 `stat-manage` 菜单下子项（`app-mall.action-auth.xml` 注册）。
- [ ] **Add：** 导出入口（各报表页导出按钮，按 E1 抉择接 nop-report xlsx/pdf 或 CSV）。
- [ ] **Proof：** 报表页渲染冒烟 + 新增 `@BizQuery` 经 `IGraphQLEngine` 测试 + 导出产物可生成。

Exit Criteria:

- [ ] 订单分析/营销分析 `@BizQuery` 返回符合 E5 口径数据；4 类报表页可渲染并消费。
- [ ] 导出可用（按 E1 抉择：nop-report xlsx/pdf 或 CSV）。
- [ ] **API 测试：** 新增 `@BizQuery` 通过 `IGraphQLEngine` 测试。
- [ ] `system-configuration.md` 补订单分析/营销分析/导出口径（E1/E5）+ 菜单结构。
- [ ] `docs/logs/` 更新。

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗式审计，3 轮
- Evidence:
  - Round 1 `ses_0f3ee2cc4ffeAkMqeZZqhQeuIr`：MAJOR_OBJECTION——(a) E2 选非实体 ReportBizModel 无先例且挂载未验证；(b) roadmap §19"生命周期"被静默丢弃；(c) E1 nop-report 理由"无 docs-for-ai"有误。已修订：E2/E1 改为 Decision+Explore（含 fallback），生命周期补 `getUserLifecycle` in-scope，活动 ROI 显式 Non-Goal 归 P22。
  - Round 2 `ses_0f3e5a524ffejx0ZztH831Ivfr`：PASS——三项 major 均据实修复（live repo 复核 nop-report docs 存在、LitemallGoods 无 costPrice 故毛利 model-gap 合法、生命周期/活动 ROI 已 adjudicate）。
  - Round 3（终审）`ses_0f3e0bf32ffekwgfj0ThEL8Bnm`：PASS——连续两轮 clean，consensus 达成。
  - 结论：无 blocker、无 major objection；规则 #15 满足，无 forbidden words，Phase 2/3 Targets 已按 E2 hedge。

## Closure Gates

- [ ] in-scope behavior is complete（销售漏斗/用户分析[含生命周期]/商品分析/订单分析/营销分析[优惠券] + 报表页 + 导出）
- [ ] relevant docs are aligned（`system-configuration.md` 多主题域口径已落）
- [ ] verification has run（`./mvnw clean package -DskipTests` + `./mvnw test -pl app-mall-service -am` + `./mvnw -pl app-mall-web -DskipTests compile`；前端 `cd e2e && npx playwright test`）
- [ ] all new `@BizQuery` methods tested via `IGraphQLEngine`
- [ ] no in-scope item downgraded to deferred/follow-up
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill` listed，Nop 平台 phase 未写 `none`
- [ ] skill loading verification：各 phase 扫可用 skill、加载匹配 skill、读完 routing 必读文档、selfcheck
- [ ] text consistency verified：status/phases/gates/log 一致
- [ ] closure audit was performed by a different agent/session
- [ ] closure evidence exists in files

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

### 支付方式占比深度分析

- Classification: `watch-only residual`
- Why Not Blocking Closure: 当前支付通道以微信支付为主（多通道 P30 为 Protected Area 待授权），占比分析字段已就绪，多通道接入后自动丰富。
- Successor Required: yes（依赖 P30 多支付通道授权落地）

## Closure

<!-- 闭合审计须由独立 subagent（不同 session）执行，此处留给闭合审计员填写。 -->

Status Note: <待闭合时填写>

Closure Audit Evidence:

- Reviewer / Agent: <独立审计员>
- Evidence: <task id / 日志 / 走查记录>

Follow-up:

- nop-report PDF/Excel 引擎（正式可打印报表需求出现时）。
- 毛利报表（`costPrice` 落地 + 财务模块 successor）。
