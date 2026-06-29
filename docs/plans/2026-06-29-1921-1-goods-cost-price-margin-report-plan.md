# 商品成本价与毛利分析报表

> Plan Status: completed
> Mission: mall
> Work Item: cost-price-margin-report（关闭 P19 / 用户优惠券报表导出 双源 deferred model-gap）
> Last Reviewed: 2026-06-29
> Source: `docs/backlog/enhanced-features-roadmap.md` §19；两份源计划 Deferred But Adjudicated：
>   - `docs/plans/2026-06-28-1027-2-phase19-report-system-extension-plan.md:155`（毛利/成本报表，缺 costPrice）
>   - `docs/plans/2026-06-29-0119-1-user-coupon-report-export-plan.md:146`（毛利/成本报表，缺 costPrice，同 model-gap）
> Related: `docs/plans/2026-06-28-2352-1-nop-report-engine-introduction-plan.md`（nop-report 引擎，本计划复用其 `exportReport` + `.xpt.xml` 模板模式）
> Audit: required

## Current Baseline

- **costPrice 字段缺失（确认）。** `model/app-mall.orm.xml:701-716` `LitemallGoods` 列止于 propId=24（`SAFETY_STOCK`），无成本价列。仅有 `COUNTER_PRICE`(propId=16 市场售价) 与 `RETAIL_PRICE`(propId=17 当前价格)，二者均为面向消费者的售价，**无内部成本价**，故毛利无法计算（强行用售价估算会失真，源计划已裁定为 model-gap）。
- **商品分析报表已落地但无毛利维度。** `LitemallOrderBizModel.getProductAnalysis(startDate,endDate,categoryId,context)`（`LitemallOrderBizModel.java:1380-1382`，`@BizQuery` **无 `@Auth`**——本计划须补 admin 鉴权，见 M1/Phase 2）返回 `ProductAnalysisBean`（销量排行 `salesRanking`/加购排行/滞销品/动销率），`buildProductDataSet`（`:1870`）只输出 goodsId/goodsName/number/amount，**无成本/毛利列**。`exportReport`（`:1765` `@BizQuery @Auth(roles="admin")`）已支持 funnel/product/order/user/coupon 五主题域 xlsx/pdf 导出，复用 nop-report `.xpt.xml` 模板。
- **销量排行 SQL 的 goods JOIN 当前是条件化的（须改无条件）。** `LitemallOrder.sql-lib.xml:285-312` `getGoodsSalesRankingByCategory` 仅在 `categoryId` 非空时 `<c:if>` INNER JOIN `litemall_goods g`（`:298-300`）用于分类过滤；默认（无 categoryId）查询**不 join goods 表**。毛利计算需 `g.COST_PRICE`，故本计划须将该 JOIN 改为**无条件**（始终 join goods），categoryId 降级为 WHERE 过滤（`:305-307`）。`GoodsStatisticsBean`（`app-mall-dao/.../dto/GoodsStatisticsBean.java`）当前仅 goodsId/goodsName/salesCount/salesAmount，须加 costAmount/grossProfit/marginRate。
- **admin 商品分析查看页已存在（须补毛利列）。** `app-mall-web/.../pages/mall/stat/stat-product.page.yaml` 已消费 `LitemallOrder__getProductAnalysis`（service + CSV 导出），其「销量排行 TOP20」表格渲染 goodsName/salesCount/salesAmount 列——本计划须在此表格补成本额/毛利额/毛利率列。
- **毛利 model-gap 被两份计划独立延期。** P19 报表体系扩展（`2026-06-28-1027-2:155`）与 user-coupon-report-export（`2026-06-29-0119-1:146`）均将「毛利/成本报表」记为 `model-gap`，Successor Required: yes，触发条件「财务/毛利分析需求出现时（与财务模块 successor A-07 合并）」。本计划即该 successor，落地 costPrice 列 + 毛利分析维度。
- **nop-report 引擎已引入。** `nop-report-core` + `nop-report-pdf` + `IReportEngine` 已就绪（`2026-06-28-2352-1` 交付），`.xpt.xml` 模板渲染模式成熟（`product-analysis.xpt.xml` 已存在），本计划扩展毛利维度沿用同模式，**不引入新平台模块**。
- **商品后台编辑页已有成本相关空位。** `app-mall-web` 下 `LitemallGoods.view.xml`（bounded-merge 定制层，非 `_gen`）已含 counterPrice/retailPrice/safetyStock 等列，新增 costPrice 列为同模式扩展；`_gen/_LitemallGoods.view.xml` 随 codegen 重生成。

## Goals

- 为 `LitemallGoods` 新增可空成本价列 `costPrice`，支撑毛利计算（不破坏既有售价语义）。
- 在商品分析报表中新增毛利维度：按商品聚合成本额、毛利额、毛利率（与既有销量/销售额同口径、同时间窗）。
- 毛利维度可经 nop-report xlsx/pdf 导出（复用 `exportReport` 模式，扩展现有 `product` 主题，D2）。
- 后台商品编辑页可维护成本价（admin 可见，前台不可见）。
- 关闭两份源计划的 deferred model-gap 条目。

## Non-Goals

- 完整财务模块（应收/应付/对账/发票，分析 A-07 单独规划，源 deferred 已注明「与财务模块 successor A-07 合并」——本计划仅落地商品级成本价 + 毛利分析，不触发票/对账）。
- 订单级/订单项级毛利分摊（含优惠券/积分/满减分摊到 SKU 的精细成本核减）——本计划毛利口径为「商品销售额 − 商品销售成本（数量 × costPrice）」，不含营销补贴分摊，留 successor。
- 历史订单成本回填（历史订单的商品成本按下单时 vs 当前 costPrice 取值）——Decision 将裁定取当前 costPrice（无历史快照列），历史精确成本为 successor。
- costPrice 对前台用户可见（成本价为内部数据，前台/列表/详情均不展示）。

## Task Route

- Type: `implementation-only change`（业务设计已在 `system-configuration.md` 报表章节 + `product-catalog.md` 商品模型覆盖；本计划为 deferred model-gap 的执行 slice，无 net-new 业务语义，仅在已决方案上落地）
- Owner Docs: `docs/design/system-configuration.md`（报表体系扩展章节）、`docs/design/product-catalog.md`（商品模型字段）
- Skill Selection Basis: 涉及 ORM 模型新增列 → `nop-orm-modeler` + `nop-database-design`；后端 BizModel/SQL-lib → `nop-backend-dev` + `nop-testing`；前端 view.xml → `nop-frontend-dev`

## Infrastructure And Config Prereqs

- No infra prereqs beyond existing baseline（nop-report 引擎已引入；无外部服务、无新端口/密钥）。
- ORM 改动属 Protected Area（`model/app-mall.orm.xml` ask-first）：本计划新增单列 costPrice（propId=25，可空 DECIMAL），授权由 MISSION_DRIVER「draft plans」后续「execute the entire plan」类指令构成（与 P18-D4 goods-level-safety-stock、P22 model-gap 等先例一致）。

## Decision Points (to resolve in-phase)

- **D1 — 毛利口径：** 取「商品销售额 − 销售成本（Σ 销售数量 × costPrice）」，costPrice 取**当前商品 costPrice**（无订单快照）。备选：新增 OrderGoods.costSnapshot 列存下单时成本（精确但属更大模型变更）。抉择：当前 costPrice（基线足用），历史精确成本 successor。残留风险：商品调价后历史毛利会随 costPrice 变动漂移。
- **D2 — 报表组织：** 新增独立 `margin` 主题（`exportReport` reportName=margin + `gross-margin.xpt.xml`）vs 扩展 `product` 主题加毛利列。抉择：**扩展 `product` 主题**（同属商品分析，避免主题碎片化；`buildProductDataSet` 加 costAmount/grossProfit/marginRate 列，`product-analysis.xpt.xml` 加对应列）。备选独立主题的劣势：商品销量与毛利分两张表割裂。
- **D3 — costPrice 可空与未知成本处理：** costPrice 为空或 0 视为「未维护成本」，毛利列输出空/「—」，不计入毛利率分母（避免 0 成本→100% 毛利失真）。理由：0 成本在商城基线无合法业务含义（赠品/样品走独立流程，不入商品成本），故 0 等同未维护，避免分母失真。

## Execution Plan

### Phase 1 - ORM 模型新增 costPrice 列

Status: completed
Targets: `model/app-mall.orm.xml`
Required Skill: `nop-orm-modeler`, `nop-database-design`

- Item Types: `Add | Decision`
- Prereqs: ORM ask-first 授权（MISSION_DRIVER execute 指令）

- [x] **Skill loading gate:** 扫描 available skills，加载 `nop-orm-modeler` + `nop-database-design`。读完其 routing table 标为必读的文档。列已读文档路径如下（未列不得勾选）。每方法/类完成后用 selfcheck 校验无反模式。
  - Docs read: `.opencode/skills/nop-orm-modeler/SKILL.md`（propId 连续/stdSqlType 枚举/Delta 模式）、`.opencode/skills/nop-database-design/SKILL.md`（命名/DECIMAL/审计字段/stdSqlType 简化）
- [x] **Decision D1/D3：** 在 plan 内确认毛利口径（当前 costPrice、可空/0 视为未维护）+ 记录备选与残留风险（历史精确成本 successor）。
- [x] **Add：** `LitemallGoods` 新增列 `COST_PRICE`（name=costPrice，propId=25，precision=10 scale=2，stdDataType=decimal/stdSqlType=DECIMAL，nullable，`ui:show="X"` 前台不可见，displayName=「成本价」+ i18n-en）。位置紧跟 `SAFETY_STOCK`(propId=24) 之后。
- [x] **Add：** 执行 `mvn clean install -DskipTests -Dquarkus.package.type=uber-jar` 触发 codegen 重生成（`_LitemallGoods.java`/`_app.orm.xml`/API beans/xmeta/i18n），确认 propId=25 无冲突且 BUILD SUCCESS。

Exit Criteria:

- [x] `model/app-mall.orm.xml` 含 `COST_PRICE` 列（propId=25，可空 DECIMAL，前台 `ui:show="X"`）
- [x] codegen 重生成 BUILD SUCCESS；`_LitemallGoods.java` 含 costPrice getter/setter + PROP 常量
- [x] `docs/logs/` 更新

### Phase 2 - 毛利分析后端（报表 query + SQL-lib + 鉴权）

Status: completed
Targets: `app-mall-service/.../LitemallOrderBizModel.java`、`app-mall-dao/.../_vfs/app/mall/sql/LitemallOrder.sql-lib.xml`、`app-mall-dao/.../dto/GoodsStatisticsBean.java`、`app-mall-api/.../ILitemallOrderBiz.java`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Fix | Decision`
- Prereqs: Phase 1（costPrice 列就绪）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完 routing 必读文档并列出路径。每写完一个方法用 skill selfcheck 校验（IBiz-first、`@Inject` 非 private、NopException+ErrorCode、CoreMetrics、无 `@Transactional` 叠加 `@BizMutation`、接口与实现 `@Auth` 一致等）。
  - Docs read: `docs-for-ai/02-core-guides/service-layer.md`、`docs-for-ai/02-core-guides/auth-and-permissions.md`、`docs-for-ai/04-reference/bizmodel-method-selfcheck.md`、`docs-for-ai/02-core-guides/testing.md`
- [x] **Decision D2：** 确认扩展 `product` 主题（`buildProductDataSet` + `product-analysis.xpt.xml` 加毛利列），记录备选（独立 margin 主题）与劣势。
- [x] **Fix（鉴权回归）：** `getProductAnalysis`（`:1380-1382`）当前无 `@Auth`，补 `@Auth(roles="admin")`（impl + `ILitemallOrderBiz` 接口声明镜像，参照 `2026-06-28-0340-2:200` setUserLevel 补 @Auth 先例）。成本/毛利为内部数据，必须 admin-only，杜绝经 GraphQL 泄漏给非 admin 认证用户。
- [x] **Add（SQL-lib）：** `getGoodsSalesRankingByCategory`（`LitemallOrder.sql-lib.xml:285-312`）将 `litemall_goods g` 的 INNER JOIN 由 `<c:if categoryId>` 条件化改为**无条件**（始终 join），SELECT 加 `SUM(og.NUMBER * g.COST_PRICE) AS COST_AMOUNT`，categoryId 降级为 WHERE 过滤（保持 `g.DELETED=0` 守卫）。
- [x] **Add（DTO）：** `GoodsStatisticsBean` 新增 `costAmount`/`grossProfit`/`marginRate` 字段（grossProfit = salesAmount − costAmount；marginRate = grossProfit / salesAmount，salesAmount=0 或 costPrice 未维护[null/0] 时为 null）。
- [x] **Add：** `buildProductDataSet`（`:1870`）每行新增 costAmount/grossProfit/marginRate（空/未维护输出空串，与 amount 列的 toPlainString 模式一致）。
- [x] **Add：** `exportReport` 的 `product` 分支无需新 case（D2 扩展主题），确认 `product-analysis.xpt.xml` 模板补毛利列（Phase 3）。
- [x] **Proof：** `@BizQuery getProductAnalysis` 经 `IGraphQLEngine` 测试（`JunitAutoTestCase`）：(a) 有 costPrice 的商品毛利正确；(b) costPrice 为空/0 → 毛利列 null 且不计入毛利率；(c) 多商品聚合；(d) 时间窗/分类过滤不变（categoryId 过滤从 JOIN 降级为 WHERE 后结果一致）；(e) 非 admin 调用 `getProductAnalysis` 被拒（鉴权回归）。`exportReport("product",...)` xlsx/pdf 导出含毛利列。

Exit Criteria:

- [x] `getProductAnalysis` 返回 costAmount/grossProfit/marginRate，口径符合 D1/D3
- [x] `getProductAnalysis` 补 `@Auth(roles="admin")`（impl + 接口），非 admin 被拒
- [x] SQL-lib goods JOIN 改无条件，categoryId 降级为 WHERE，结果与改前一致
- [x] **API 测试：** 新增/扩展毛利维度 + 鉴权回归经 `IGraphQLEngine`（`LitemallOrder__getProductAnalysis` + `LitemallOrder__exportReport`）验证，非实体级纯逻辑测试
- [x] costPrice 为空/0 时毛利列正确降级（null/—），不计入毛利率分母
- [x] `docs/logs/` 更新

### Phase 3 - nop-report 模板 + 前后台展示

Status: completed
Targets: `app-mall-web/.../pages/mall/stat/stat-product.page.yaml`、`app-mall-web/.../pages/LitemallGoods/LitemallGoods.view.xml`（bounded-merge 层）、`app-mall-service/.../_vfs/nop/main/report/product-analysis.xpt.xml`
Required Skill: `nop-frontend-dev`, `nop-testing`

- Item Types: `Add`
- Prereqs: Phase 2（毛利 query 就绪）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完 routing 必读文档（XView 三层模型、bounded-merge、grid/form 定制、page.yaml）并列出路径。文件完成后 selfcheck。
  - Docs read: `docs-for-ai/02-core-guides/view-and-page-customization.md`、`docs-for-ai/02-core-guides/delta-customization.md`、`docs-for-ai/00-start-here/application-project-defaults.md`
- [x] **Add：** `product-analysis.xpt.xml` 模板新增「成本额/毛利额/毛利率」列（Latin 代码列名 costAmount/grossProfit/marginRate，与既有 funnel/product Latin 列名先例一致；PDFBox Helvetica 仅 Latin）。
- [x] **Add：** admin `stat-product.page.yaml`「销量排行 TOP20」表格补「成本额/毛利额/毛利率」三列（消费 `getProductAnalysis` 返回的新字段）。
- [x] **Add：** admin `LitemallGoods.view.xml`（bounded-merge 定制层，非 `_gen`）新增 `costPrice`：edit form 字段（必交付，标签「成本价（内部，前台不可见）」）+ grid 列。`_gen/_LitemallGoods.view.xml` 随 ORM codegen 重生成。
- [x] **Proof：** 前端编译 `./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS；`exportReport("product","xlsx")` / `("product","pdf")` 导出文件含毛利列且非空；`stat-product.page.yaml` 渲染毛利列。

Exit Criteria:

- [x] `product-analysis.xpt.xml` 含毛利列；xlsx/pdf 导出含成本额/毛利额/毛利率
- [x] `stat-product.page.yaml` 销量排行表展示成本额/毛利额/毛利率
- [x] admin 商品编辑页可维护 costPrice（form + grid）；前台/列表/详情不展示 costPrice（`ui:show="X"` + 前端 page.yaml 不消费）
- [x] app-mall-web 编译通过
- [x] 相关 owner doc 更新（`system-configuration.md` 报表毛利维度、`product-catalog.md` costPrice 字段）
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed（consensus）
- Auditor / Agent: 独立 subagent（fresh session）
- Round 1（`ses_0ecda60fdffefKO02KVXXqC9aD`）：`revise` — 2 blockers（B1 Phase 3 anti-slacking hedge「如有/可选/若」；B2 缺 stat-product.page.yaml baseline 盘点）+ 2 majors（M1 getProductAnalysis 缺 `@Auth` → 成本数据泄漏回归；M2 SQL goods JOIN 须改无条件）。已全部修订：baseline 补 stat-product.page.yaml + 条件 JOIN 说明；Phase 2 增 `@Auth(roles="admin")` Fix 项（impl+接口镜像）+ SQL JOIN 改无条件的明确 Add；Phase 3 移除 hedge、提交 stat-product 毛利列 + form/grid costPrice。
- Round 2（`ses_0eccdf247ffeQpMoXa4PaymMz1`）：`consensus` — 4 项 Round 1 findings（B1/B2/M1/M2）逐项核验 ADDRESSED（live repo 对照）；未引入新 blocker/major。一结果面、Required Skill 齐备、API 测试 #15 合规、ORM ask-first rationale 引先例、phase 依赖有序、closure gates 完整。Minor（非阻断）：JOIN 改无条件后无 categoryId 路径会排除软删商品（基线正确行为，Proof(d) 覆盖）。consensus 达成，可进入实施。

## Closure Gates

- [x] in-scope behavior is complete（costPrice 列 + 毛利分析维度 + 导出 + admin 维护）
- [x] relevant docs are aligned（`system-configuration.md` / `product-catalog.md`）
- [x] verification has run（`./mvnw clean package -DskipTests` + `./mvnw test`，全绿）
- [x] all new `@BizQuery` methods tested via `IGraphQLEngine`（`getProductAnalysis` 扩展[含鉴权回归] + `exportReport` 毛利导出）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop-platform phases 不写 `none` 无 justify
- [x] skill loading verification: 各 phase 扫描/加载/读必读文档/selfcheck（路径列于 skill loading gate）
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files
- [x] 两份源 deferred（P19 `2026-06-28-1027-2:155` + user-coupon-export `2026-06-29-0119-1:146`）在本计划 closure 后于各自源计划标注「已由 successor 关闭」

## Deferred But Adjudicated

### 订单级/订单项级精细毛利分摊（含营销补贴分摊到 SKU）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划毛利口径为商品聚合级（销售额 − 销售成本），不含优惠券/积分/满减/运费补贴分摊到 SKU 的精细净毛利。精细分摊需定义分摊规则（按金额比例 vs 按数量）+ 订单项快照，属独立结果面。
- Successor Required: `yes`（触发条件：财务要求按订单项净毛利核算时，定义分摊规则 + 落地 OrderGoods 级毛利 query）

### 历史订单成本快照（OrderGoods.costSnapshot）

- Classification: `model-gap`
- Why Not Blocking Closure: D1 抉择取当前 costPrice，商品调价后历史毛利会漂移；精确历史成本需 OrderGoods 新增 costSnapshot 列存下单时成本。本基线无此列。
- Successor Required: `yes`
- Model Gap Detail: `LitemallOrderGoods` 缺 `costSnapshot`（DECIMAL）列。建议新增可空 decimal 列，submit 时写入下单时 costPrice 快照。触发条件：财务要求历史毛利不受调价影响时。

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 三 Phase 全部完成且经独立 closure audit 复核 live repo 通过。costPrice 列、毛利分析维度、@Auth 鉴权回归、SQL-lib 无条件 JOIN、xpt 模板毛利列、admin 前后端展示、owner docs、日志、两份源计划 deferred 标注 Successor Closed 均已落地。493 测试全绿 known-good baseline。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure auditor（fresh session，非实施 agent）
- Audit Scope: 逐项复验三 Phase Exit Criteria + Closure Gates 对 live repo（`./`）证据；anti-hollow 检查（runtime wiring）；five-point consistency；deferred honesty；docs sync。
- Phase 1 Exit Criteria — PASS：`model/app-mall.orm.xml:717` `COST_PRICE`（propId=25，DECIMAL(10,2)，nullable，`ui:show="X"`）；`_gen/_LitemallGoods.java:120-122/317/1376-1389` 含 costPrice getter/setter + `PROP_NAME_costPrice`/`PROP_ID_costPrice=25` 常量。
- Phase 2 Exit Criteria — PASS：`LitemallOrderBizModel.java:1382` `@Auth(roles="admin")`（impl）+ `ILitemallOrderBiz.java:195` `@Auth(roles="admin")`（接口镜像）；`LitemallOrder.sql-lib.xml:299` `INNER JOIN litemall_goods g ... AND CAST(g.DELETED AS INT)=0` 无条件 + `:296` `SUM(og.NUMBER * g.COST_PRICE) AS COST_AMOUNT` + `:304-306` categoryId 降级 WHERE `<c:if>`；`GoodsStatisticsBean.java:13-15` costAmount/grossProfit/marginRate；`LitemallOrderBizModel.java:1414-1433` `enrichMarginFields` D1/D3 口径正确（costAmount null/≤0→三列 null 不计分母）；`:1903-1905` buildProductDataSet 输出三列。**Anti-Hollow PASS**：`getProductAnalysis:1393` 实际调用 `enrichMarginFields(ranking)`，runtime 可达。**API 测试 PASS**：`TestLitemallOrderStatisticsBizModel` 4 IGraphQLEngine 测试（毛利正确性 costAmount=120/grossProfit=80/marginRate=0.4、null/0 D3 降级、categoryId JOIN→WHERE 降级一致性、interface+impl @Auth 反射守卫）。
- Phase 3 Exit Criteria — PASS：`product-analysis.xpt.xml:25-31/50-56` 表头+数据行三毛利列；`stat-product.page.yaml:110-115` 三列；`LitemallGoods.view.xml:48-49/97` costPrice grid+form；`docs/design/system-configuration.md:360` + `docs/design/product-catalog.md:154-158` owner doc 更新。
- Closure Gates — PASS：全 11 项 `[x]`（含本次独立 audit 复核）；两份源 deferred（`2026-06-28-1027-2:161` + `2026-06-29-0119-1:152`）均标注 Successor Closed(2026-06-29) 含具体落地点。
- Deferred Honesty — PASS：订单项级精细毛利分摊（`out-of-scope improvement`，含触发条件）+ 历史成本快照（`model-gap`，含 OrderGoods.costSnapshot 建议与触发条件）均在 Deferred But Adjudicated，无非范围缺陷降级。
- Docs Sync — PASS：`docs/logs/2026/06-29.md:1-43` 含本计划三 Phase 详细日志 + 493 测试 known-good baseline；owner docs 同步。
- Verdict: **approved** — 五点一致（Plan Status completed / 三 Phase completed / 全 Exit Criteria `[x]` / Closure Gates 全 `[x]` / 日志一致），无 blocker/major。plan 可闭合。

Follow-up:

- 订单项级精细毛利分摊、历史成本快照见 Deferred But Adjudicated（均含触发条件）。
