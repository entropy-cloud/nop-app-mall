# nop-report 引擎引入与模板化报表导出

> Plan Status: completed
> Last Reviewed: 2026-06-29
> Source: 多来源收敛：
>   - **P1 审计发现（主驱动）**：`docs/audits/2026-06-16-1744-multi-dim-audit-full-project/01-requirement-design-alignment.md` 维度 01-2（P1，复核状态「已保留」）：「Phase 13 标 done，但 roadmap 交付范围要求的三项（nop-report 依赖、报表模板、后台看板）均未做」→ 建议「真正引入 nop-report 或 Phase 13 改 partial」。该项从未被实质 remediate（路由到 `2026-06-16-1800-p1-governance-credibility-plan.md` → Deferred「Phase 11/13 功能完整补全」→ `2026-06-17-1830-phase11-13-completion-plan.md` 选 AMIS Option B 绕过 nop-report，Phase 13 仍标 done）。
>   - **roadmap 合约缺口**：`docs/backlog/implementation-roadmap.md` Phase 13 交付范围 L381「引入 nop-report 依赖」+ L383「创建报表模板」从未交付；L75 Baseline 仍记「nop-report 引擎未引入，无看板模板和导出能力」。后台看板部分已由 AMIS chart 交付（L384），nop-report 引擎+模板部分仍缺口。
>   - **三个 enhanced-features Deferred successor**：P18 D1 / P19 E1 / P36 xlsx（均 Successor Required: yes）
> Related: `docs/plans/2026-06-17-1830-phase11-13-completion-plan.md`（选 AMIS Option B，deferred nop-report）、`docs/design/system-configuration.md`（导出口径多处标注 nop-report 为 successor）
> Audit: required

## Current Baseline

**nop-report 缺口的来源链（live repo 核验）：**
- `implementation-roadmap.md` Phase 13 交付范围 L381「引入 nop-report 依赖」+ L383「创建报表模板」+ L384「后台统计看板」— L384 已由 AMIS chart 交付（P18 进一步增强），**L381+L383 从未交付**。
- `implementation-roadmap.md:32` Phase Status 标 Phase 13 = `done`（以 AMIS chart 看板 + 3 统计 API + SQL 数据集 为交付依据），但 L75 Baseline 仍记「`nop-report` 引擎未引入，无看板模板和导出能力」— status 与 baseline 并存（status 接受 AMIS 替代，baseline 记录 nop-report 事实缺口）。
- 多维审计 `01-requirement-design-alignment.md` 维度 01-2（P1）从未实质 remediate：路由链 = `2026-06-16-1800-p1-governance-credibility-plan.md` Deferred（L223-226，触发条件「Phase 4 Decision 选 B 后另立补全计划」）→ `2026-06-17-1830-phase11-13-completion-plan.md` Phase 2 Decision L89 选「选项 B（AMIS chart）」、L90「N/A（选 B，不引入 nop-report）」，标 Phase 13 done。
- P18（Dashboard 重做）、P19（报表扩展）、P36（商品运营）三个 enhanced-features 计划各自 Deferred nop-report 为 successor（Successor Required: yes）。

**nop-report 平台模块现状（live repo 核验）：**
- `nop-entropy/nop-report/` 存在，含 12 子模块（core/pdf/docx/api/dao/service/web/ext/demo/codegen/app/meta）
- 平台文档齐全：`docs-for-ai/03-runbooks/generate-report.md`（XPT 模板语法 / IReportEngine 调用 / 数据集 / 套打 / imp 自动生成）、`docs-for-ai/03-modules/nop-report.md`、`docs-for-ai/02-core-guides/reporting-and-notification-integration.md`（默认路线：报表优先走 nop-report，禁止自建导出框架）
- 渲染入口是 `IReportEngine`（`nop-report-core`），三种渲染类型：`xlsx`（OOXML 二进制 `IBinaryTemplateOutput`）、`pdf`（PDFBox 直接渲染）、`html`（屏幕预览 `ITextTemplateOutput`）
- 调用范例：`nop-report-demo/.../biz/ReportDemoBizModel.java` + 测试 `TestReportDemoBizModel.java`

**本项目 nop-report 集成现状：**
- `app-mall-service/pom.xml` 及所有模块 pom **零 nop-report 依赖**（grep 确认）
- `IReportEngine` 无任何注入点；无 `.xpt.xlsx`/`.xpt.xml` 模板文件
- roadmap `enhanced-features-roadmap.md` §Nop Platform Reuse 标「报表引擎 nop-report 未引入」

**现有导出实现（CSV 兜底，需升级）：**
- **P36 商品导出**（`LitemallGoodsBizModel.exportGoods` @BizQuery @Auth(roles=admin)，`app-mall-service/.../entity/LitemallGoodsBizModel.java:519-546`）：后端拼 CSV 字符串返回 `GoodsExportResultBean.csvContent`，列 = goodsId/goodsSn/name/retailPrice/counterPrice/isOnSale。前端 `mall-goods-io.page.yaml` 消费。
- **P19 报表导出**（`LitemallOrderBizModel` 8 个 @BizQuery：`getSalesFunnel`@1342/`getProductAnalysis`@1361/`getUserRetention`@1414/`getUserRfm`@1476/`getUserLifecycle`@1574/`getRepurchaseRate`@1666/`getOrderAnalysis`@1718/`getCouponAnalysis`@1743）：返回聚合 Bean，**无后端导出方法**，导出由前端 AMIS 客户端 CSV（`stat-funnel.page.yaml`/`stat-product.page.yaml`/`stat-user.page.yaml`/`stat-order.page.yaml` 各页导出按钮）。

**owner doc 已记录的 successor 约定（`docs/design/system-configuration.md`）：**
- L320「导出方式：CSV 兜底（前端 AMIS 导出，零新依赖）。nop-report xlsx/pdf 引擎为 successor（复杂模板化报表需求出现时引入）。」
- L388「导出方式（E1）：CSV 兜底... nop-report xlsx/pdf 引擎为 successor」
- L534「导出按可用写出入口抉择：本基线无平台 xlsx 写出 helper，走 CSV 兜底... 模板化 xlsx 导出为 successor，可随 nop-report 引入续作。」

**核心缺口：** nop-report 引擎未引入（Phase 13 交付范围 L381/L383 缺口 + 审计 01-2 P1 未 remediate），导出能力停留在 CSV 兜底，无格式化/模板化 xlsx/pdf 输出。

## Goals

- 引入 `nop-report-core` + `nop-report-pdf` Maven 依赖，装配 `IReportEngine` 可用 — 关闭 `implementation-roadmap.md` Phase 13 交付范围 L381「引入 nop-report 依赖」缺口
- 创建 `.xpt.xlsx` 报表模板 — 关闭 Phase 13 交付范围 L383「创建报表模板」缺口
- 提供 `IReportEngine` 驱动的 xlsx/pdf 模板化导出能力，替代现有 CSV 兜底（CSV 作为回退保留）
- 覆盖两类导出场景作为落地先例：(1) 商品导出（平铺列表型）；(2) 报表导出（聚合指标型，至少覆盖销售漏斗 + 商品分析 + 订单分析 3 个有明确列表/指标数据的报表）
- 前端导出入口增加格式选择（xlsx / pdf / csv）
- 关闭审计 01-2（P1）nop-report 部分 + P18 D1 / P19 E1 / P36 xlsx 四个 Deferred successor

## Non-Goals

- **Dashboard 看板本身改造** — 后台可视化看板已由 AMIS chart 完整交付（`2026-06-17-1830` Phase 2 Option B + P18 重做增强），nop-report 价值在导出而非看板渲染。审计 01-2 的「后台看板」部分已由 AMIS 关闭，本计划仅关闭「nop-report 依赖 + 报表模板」部分。
- **运行时报表定义 CRUD**（`nop_report_definition`/`nop_report_dataset` 等实体表的后台管理页面）— 本计划只做代码驱动模板导出（`IReportEngine` + 静态 `.xpt.xlsx`），不引入运行时报表定义管理。触发条件：运营需自助配置报表模板时。
- **套打（overlay printing）** — 无业务需求（发票/单据打印未在当前基线）。
- **nop-report-docx（DOCX 输出）** — 无业务需求。
- **毛利/成本报表** — 依赖 `LitemallGoods.costPrice` 字段（P19 model-gap，独立 successor，与财务模块合并）。
- **nop-report-service/web/dao 子模块引入** — 渲染只需 core+pdf，不引入 CRUD/DAO 层。

## Task Route

- Type: `architecture change`（引入新平台模块依赖 + 导出架构升级）
- Owner Docs: `docs/design/system-configuration.md`（导出口径）、`docs/architecture/`（模块依赖）
- Skill Selection Basis: 后端 BizModel 方法（nop-backend-dev）、前端 AMIS 导出按钮（nop-frontend-dev）、API 测试（nop-testing）；nop-report 无专用 skill，平台文档通过 skill routing table 的必读文档覆盖

## Infrastructure And Config Prereqs

- nop-report-core / nop-report-pdf 已在 `nop-entropy` 构建产物中（nop-entropy parent 已 built）
- 无外部服务依赖（PDFBox 为 nop-report-pdf 传递依赖）
- 无数据迁移（不引入 nop-report DAO 表）
- 无 env/secrets/CORS 变化

## Execution Plan

### Phase 1 - 引入 nop-report 依赖 + 商品导出先例（Add-heavy）

Status: completed
Targets: `app-mall-service/pom.xml`、`app-mall-service/.../entity/LitemallGoodsBizModel.java`、`app-mall-service/src/main/resources/_vfs/nop/main/report/goods-export.xpt.xml`（新增模板）
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: 无

- [x] **Skill loading gate:** 加载 `nop-backend-dev`、`nop-testing`，读完必读文档，列于下；每方法 selfcheck。
  - Docs read: `nop-entropy/docs-for-ai/03-runbooks/generate-report.md`（XPT 语法/IReportEngine 调用/imp 自动生成）、`nop-entropy/docs-for-ai/03-modules/nop-report.md`、`nop-entropy/docs-for-ai/02-core-guides/service-layer.md`、`nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`、`nop-entropy/docs-for-ai/02-core-guides/testing.md`
- [x] **Decision R1：依赖范围。** 引入 `nop-report-core`（IReportEngine 引擎）+ `nop-report-pdf`（PDF 渲染器），不引入 service/web/dao/docx。理由：渲染入口是 `IReportEngine`（runbook 明确「没有独立 ReportService 类」），CRUD BizModel 非渲染入口。备选（引入 nop-report-app 全量）被否——带入不需要的 DAO 表与后台页面。残留风险：若后续需运行时报表定义管理，须追加 service/web 模块。
- [x] **Decision R2：模板设计方式。** 抉择手工编写报表模板。**执行偏差（已记录）**：实际采用 `.xpt.xml`（XML 序列化，`XptModelLoader` 与 `.xpt.xlsx` 同等接受，均经 `XptModelInitializer` 解析 `*=^ds!field` 文本简写），因手写二进制 `.xpt.xlsx` 不切实际且易错。模板表头用 Latin 字段名（与现有 CSV 列对齐，PDF 渲染兼容 ASCII）。备选 `buildXptModelFromImpModel` 为复杂列表场景的后续优化。残留风险：模板需随导出列变更同步维护。
- [x] **Add：** `app-mall-service/pom.xml` 新增 `nop-report-core` + `nop-report-pdf` 依赖（scope=compile）。附带：`app-mall-web/pom.xml` 将 `nop-ooxml-xlsx` 由 test 提升为 compile，因 nop-report-core 的 core 初始化（`XlsxDslModelLoaderFactory`）在 app-mall-web precompile（compile 域）需 `IExcelWorkbookGenerator`/`XlsxObjectLoader`。
- [x] **Add：** 编写 `goods-export.xpt.xml` 模板（VFS 路径 `/nop/main/report/goods-export.xpt.xml`），表头行 + 数据行 `expandType=r` 展开 `goodsList` 数据集，列 = goodsId/goodsSn/name/retailPrice/counterPrice/isOnSale（与现有 CSV 列对齐）。
- [x] **Add：** `LitemallGoodsBizModel` 注入 `IReportEngine`，新增 `exportGoodsReport(@BizQuery @Auth(roles=admin))`：接受 `renderType`(xlsx|pdf) + 现有筛选参数，构造 `IEvalScope`（`goodsList` = 复用 `exportGoods` 的查询逻辑），调 `reportEngine.getRenderer(path, renderType).generateToResource()`，返回 `WebContentBean`（二进制下载）。**保留现有 `exportGoods` CSV 不删除**（向后兼容 + CSV 回退）。
- [x] **Proof：** `exportGoodsReport` 经 `IGraphQLEngine` 测试（xlsx + pdf 两种 renderType），验证返回非空二进制 + 文件名正确；空结果集不报错。

Exit Criteria:

- [x] `IReportEngine` 可注入且 `getRenderer` 返回有效渲染器（xlsx/pdf）；`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` 构建通过（依赖装配成功）。
- [x] `exportGoodsReport` @BizQuery 经 `IGraphQLEngine` 测试通过（xlsx/pdf 双格式 + 空集 + 非法 renderType 拒绝）。
- [x] 现有 `exportGoods` CSV 不回归（既有 `TestLitemallGoodsOpsWorkbench` 19 测试全绿）。

### Phase 2 - 报表导出（P19 successor）

Status: completed
Targets: `app-mall-service/.../entity/LitemallOrderBizModel.java`、`app-mall-service/src/main/resources/_vfs/nop/main/report/*.xpt.xml`（新增报表模板若干）
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: Phase 1 完成（依赖 + 引擎可用）

- [x] **Skill loading gate:** 加载 `nop-backend-dev`、`nop-testing`，读完必读文档，列于下；每方法 selfcheck。
  - Docs read: `nop-entropy/docs-for-ai/03-runbooks/generate-report.md`、`nop-entropy/docs-for-ai/02-core-guides/service-layer.md`、`nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`、`nop-entropy/docs-for-ai/02-core-guides/testing.md`
- [x] **Decision R3：报表导出覆盖范围。** 覆盖 3 个有明确列表/指标数据的报表：销售漏斗（`getSalesFunnel` 5 段 + 转化率）、商品分析（`getProductAnalysis` 销量排行列表）、订单分析（`getOrderAnalysis` 客单价分布 + 支付方式 + 退货原因）。用户分析（RFM/留存/生命周期，纯分布计数）与优惠券分析（领取/核销率）**移出本计划范围**，记入 Deferred But Adjudicated。理由：前 3 个报表数据结构最适合 xlsx 表格呈现，先证明聚合 Bean → 模板的模式；后 2 类为同模式扩展，独立追加。
- [x] **Add：** 编写 3 个 `.xpt.xml` 报表模板（`sales-funnel.xpt.xml`/`product-analysis.xpt.xml`/`order-analysis.xpt.xml`），数据集通过 `*=^ds!field` 文本简写绑定，Bean 字段经 BizModel 适配为 map 列表（如 GoodsStatisticsBean.salesCount→number）。
- [x] **Add：** `LitemallOrderBizModel` 注入 `IReportEngine`，新增 `exportReport(@BizQuery @Auth(roles=admin))`：接受 `reportName`(funnel|product|order) + `renderType`(xlsx|pdf) + 时间/categoryId 筛选，复用 `getSalesFunnel`/`getProductAnalysis`/`getOrderAnalysis` 构造数据集，渲染返回 `WebContentBean`。
- [x] **Proof：** `exportReport` 经 `IGraphQLEngine` 测试，覆盖 3 个 reportName × xlsx/pdf + 空数据 + 非法 reportName/renderType 拒绝。

Exit Criteria:

- [x] `exportReport` @BizQuery 经 `IGraphQLEngine` 测试通过（3 reportName × xlsx/pdf + 空集 + 非法入参拒绝）。
- [x] 报表导出产物可生成且包含对应 Bean 字段数据（funnel 5 段、product 排行列、order 三类分布）。

### Phase 3 - 前端导出入口 + owner doc + 收口（Add-heavy）

Status: completed
Targets: `app-mall-web/.../pages/mall/`（goods-io + stat 报表页）、`docs/design/system-configuration.md`
Required Skill: `nop-frontend-dev`、`nop-backend-dev`

- Item Types: `Add | Proof`
- Prereqs: Phase 1、Phase 2 完成

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`、`nop-backend-dev`，读完必读文档，列于下；每页面 selfcheck。
  - Docs read: `nop-entropy/docs-for-ai/00-start-here/application-project-defaults.md`、`nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`、既有 `goods-io.page.yaml`/`stat-*.page.yaml`（导出按钮模式参考）
- [x] **Add：** 商品工作台导出按钮增加格式选择（xlsx/pdf 调 `exportGoodsReport`，csv 保留调 `exportGoods`）。
- [x] **Add：** 3 个报表页（stat-funnel/stat-product/stat-order）导出按钮增加 xlsx/pdf 选项（调 `LitemallOrder__exportReport`）。附带修复 stat-order 三处 `${... | pick: ...}` 未加引号的严格 YAML 解析问题（语义不变）。
- [x] **Proof：** `app-mall-web` 编译通过（`./mvnw -pl app-mall-web -DskipTests compile`）+ AMIS YAML 合法性（`TestReportExportPageLoad` 4 页经 `PageProvider.getPage` 加载校验全绿）。
- [x] **Add：** 更新 `docs/design/system-configuration.md` 导出口径（3 处）：将「CSV 兜底，nop-report 为 successor」更新为「nop-report 模板化导出（xlsx/pdf）已引入，CSV 保留为回退」。

Exit Criteria:

- [x] 前端导出按钮支持 xlsx/pdf/csv 三格式选择，编译通过。
- [x] `docs/design/system-configuration.md` 导出口径已更新，不再标 nop-report 为未引入 successor。
- [x] `docs/logs/` 更新（`docs/logs/2026/06-29.md`）。

## Plan Audit

- Status: passed (3 轮，Round 1 MAJOR_OBJECTION → 修订 → Round 2/3 连续 PASS，consensus 达成)
- Auditor / Agent: 独立 subagent 对抗式审计
- Evidence:
  - Round 1 `ses_0f10eef26ffeDzh10AN2vSEX48`：MAJOR_OBJECTION——(a) trigger 循环论证（仅以 successor-named-it 为据）；(b) 未 reconcile `2026-06-17-1830` 选 AMIS 绕过 nop-report 的 stale completed 计划；(c) 未引用审计 01-2（P1）与 implementation-roadmap L381/L383 合约缺口。已修订：Source 重锚于审计 01-2 + roadmap 合约缺口 + reconcile stale 完成计划链。
  - Round 2 `ses_0f1058487ffejCUWjD5XniEMFp`：PASS——四项 Round 1 objection 全部据 live repo 证据修复（审计 01-2 真实且未 remediate、roadmap L381/L383 要求 nop-report、completion plan L89/L90 选 AMIS 已 reconcile、子模块计数修正、R3 hedge 消除）。
  - Round 3 `ses_0f10159f0ffeEOBqwGotfgcXee`：PASS——trigger 确认 genuinely open（grep 全工程确认 01-2 从未 remediate）、scope 单一结果面、XPT bean-driven 导出可行性经 runbook 验证、两个 Deferred 为合法 adjudicated split 非 slacking、Source/Baseline/Goals/Non-Goals/Closure Gates 无 drift。
  - 结论：连续两轮 clean，consensus 达成，无 blocker、无 major objection。

## Closure Gates

- [x] in-scope behavior is complete（nop-report 引擎引入 + 商品导出 + 报表导出 + 前端格式选择）
- [x] relevant docs are aligned（`system-configuration.md` 导出口径更新 + `implementation-roadmap.md` L75 Baseline nop-report 缺口标记更新）
- [x] verification has run（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` + `./mvnw test -pl app-mall-service -am` + `./mvnw -pl app-mall-web -DskipTests compile`）— 全绿
- [x] all new `@BizQuery` methods tested via `IGraphQLEngine`（`exportGoodsReport` + `exportReport`）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop 平台 phase 未写 `none`
- [x] skill loading verification：各 phase 扫可用 skill、加载匹配 skill、读完 routing 必读文档、selfcheck
- [x] text consistency verified：status/phases/gates/log 一致
- [x] closure audit was performed by a different agent/session
- [x] closure evidence exists in files
- [x] 审计 01-2（P1）nop-report 部分已 remediate：`01-requirement-design-alignment.md` 维度 01-2 已标注 Remediation（2026-06-29）；看板部分已由 AMIS 关闭
- [x] `2026-06-17-1830-phase11-13-completion-plan.md` Phase 2 nop-report 绕过已 reconcile（L90 已加 Reconcile 注：AMIS=可视化看板，nop-report=模板导出，两能力面并行不冲突）
- [x] 四个 Deferred 源条目（审计 01-2 / P18 D1 / P19 E1 / P36 xlsx）在本计划 closure 后于各自源计划标注「已由 successor 关闭」

## Deferred But Adjudicated

### 用户分析/优惠券分析报表导出

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: Phase 2 覆盖 3 个核心报表（漏斗/商品/订单）已证明聚合 Bean → 模板模式；用户分析（RFM/留存/生命周期分布）与优惠券分析（领取/核销率）为同模式扩展，模板工作量独立。
- Successor Required: yes
- 说明：触发条件「用户分析/优惠券分析需 xlsx/pdf 导出时」，复用 Phase 2 的 `exportReport` 模式追加 reportName + 模板。

### 运行时报表定义 CRUD（nop-report-service/web）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划用静态 `.xpt.xlsx` 模板 + 代码驱动渲染已满足导出需求；运行时自助报表定义管理（`nop_report_definition`/`nop_report_dataset` 后台页面）为运营增强。
- Successor Required: yes
- 说明：触发条件「运营需自助配置报表模板/数据集时」，引入 nop-report-service/web 模块。

## Closure

<!-- 闭合审计须由独立 subagent（不同 session）执行，此处留给闭合审计员填写。 -->

Status Note: closed — 三 Phase 全部交付（nop-report 引擎 + 4 模板 + 2 @BizQuery + 前端三格式导出入口），14 closure gates 全部满足，独立 closure audit（2026-06-29）CLOSURE_PASS（10 项 check 9 PASS + 1 项 text-consistency 经本闭合修复后 PASS）。

Closure Audit Evidence:

- Reviewer / Agent: 独立 subagent closure audit（ses_0f0ce846affeghpERizcWUh0x2，2026-06-29）
- Evidence:
  - Check 1-8,10 PASS：deps（app-mall-service/pom.xml:103-110；app-mall-web/pom.xml:37-40）、goods 模板+方法（goods-export.xpt.xml + LitemallGoodsBizModel.exportGoodsReport @558-586，旧 exportGoods 保留）、report 模板+方法（3 模板 + LitemallOrderBizModel.exportReport @1750-1791）、IGraphQLEngine 测试（TestLitemallGoodsOpsWorkbench:272-301 + TestLitemallOrderStatisticsBizModel:243-310）、前端 4 页 + stat-order YAML 引号修复、owner doc/roadmap 3 处更新、审计 01-2 Remediation、3 source plan Successor Closed。
  - Check 9 初轮 FAIL（closure gates 未勾选 + Closure 段占位符）→ 已修复（本闭合 tick 全部 gates + 填充 Closure 段），text-consistency 一致性恢复。
  - 全量验证：`mvn clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS；`mvn test -pl app-mall-service -am` 410 测试全绿；`mvn -pl app-mall-web test` 6 测试全绿。日志见 `docs/logs/2026/06-29.md`。

Follow-up:

- 用户分析/优惠券分析报表导出（见 Deferred But Adjudicated）
- 运行时报表定义 CRUD（见 Deferred But Adjudicated）
