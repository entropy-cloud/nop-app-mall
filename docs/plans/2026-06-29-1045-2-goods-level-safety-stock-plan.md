# 商品级安全库存字段与聚合预警（关闭 P18-D4 + P36 deferred）

> Plan Status: completed
> Last Reviewed: 2026-06-29
> Mission: mall
> Work Item: LitemallGoods.safetyStock 字段 + 三级库存预警阈值解析（per-SKU → per-goods → global）
> Source: 两个 deferred 项：
>   - `docs/plans/2026-06-28-1027-1-phase18-dashboard-redesign-plan.md` → `Deferred → 每商品独立安全库存字段（D4）`（:136-141，model-gap，Successor Required: yes，触发条件「按商品粒度安全库存预警需求出现时（与 P36 商品运营增强的库存预警可合并落地）」）
>   - `docs/plans/2026-06-28-1027-3-phase36-goods-operations-enhancement-plan.md` → `Deferred → 每商品（goods 级）安全库存`（:153-158，Successor Required: yes）
> Related: P38 库存语义化（`mall_stock_threshold_tight` 全局阈值来源）、P36 库存预警工作台（per-SKU `safeStock` 路径）、P18 Dashboard 库存预警计数（goods 聚合路径）
> Audit: required

## Current Baseline

**触发条件已满足（live repo 核验）：** P18-D4 触发条件为「按商品粒度安全库存预警需求出现时（与 P36 商品运营增强的库存预警可合并落地）」。P36 已 done（库存预警工作台已交付），「合并落地」窗口已开。当前两条预警代码路径阈值口径不一致（见下），是运营可感知的语义缺口，触发条件客观成立。

**`LitemallGoods` ORM（`model/app-mall.orm.xml:668-769`）：** 23 列，**无 `safetyStock`/`safeStock`/`safety_stock` 列**（全工程 grep 确认 `LitemallGoods` 上零命中；仅 `LitemallGoodsProduct.safeStock` 存在于 :826）。使用逻辑删除。下一个可用 propId = 24（`videoUrl` 在 :714，`</columns>` 在 :715）。

**既有 per-SKU 安全库存：** `LitemallGoodsProduct.safeStock`（`:826`，int，「安全库存预警线」，P36 已消费）。

**两条库存预警代码路径（口径分歧，本计划统一）：**

- **Path A（P36 工作台，SKU 粒度，per-SKU + 全局回退）：** `LitemallGoodsBizModel.getStockWarningList`（`:665-713`）。阈值解析 `:686-695`：`product.safeStock` 非空且 >0 → 用 safeStock；否则 → 全局 `mall_stock_threshold_tight`。返回 `thresholdSource ∈ {"safeStock","global"}`。DTO `StockWarningSkuBean`（`app-mall-dao/.../dto/StockWarningSkuBean.java`）。
- **Path B（P18 Dashboard，goods 聚合，仅全局阈值）：** `LitemallOrderBizModel.getTodoAggregation`（`:1329-1357`，:1347 取全局阈值，:1348 调 SQL-lib）→ `LitemallOrder.sql-lib.xml:getStockWarningList`（`:125-143`）`HAVING SUM(p.NUMBER) <= ${threshold}`（仅全局，**不查 per-SKU safeStock，无 per-goods 阈值**）。DTO `StockWarningItemBean`（仅 goodsId/goodsName/totalStock，无阈值来源字段）。

**全局阈值配置：** `mall_stock_threshold_tight`（默认 10），`LitemallGoodsBizModel.CONFIG_STOCK_THRESHOLD_TIGHT`（`:60`）、`resolveStockThreshold`（`:286-297`）；`LitemallOrderBizModel` 有重复副本（`:2236-2247`）。存于 `LitemallSystem`（`ILitemallSystemBiz.getConfig()`）。`docs/design/system-configuration.md:40` 定义。

**前端：**
- 工作台 `stock-warning.page.yaml`（`app-mall-web/.../mall/goods-ops/`）：消费 `LitemallGoods__getStockWarningList`，列 safeStock/threshold/thresholdSource（thresholdSource 映射 safeStock→per-SKU、global→全局回退）。
- Dashboard `stat-dashboard.page.yaml`（:158-195）：消费 `LitemallOrder__getTodoAggregation`，明细表仅 goodsName/totalStock，无阈值列。
- 商品编辑表单：全工程 grep 确认无任何 `safetyStock`/`safeStock` 输入项（商品 form/view 无此字段）。

**owner doc：** `system-configuration.md:538-542`（Decision G3）仅描述 per-SKU `safeStock` + 全局回退，**无 per-goods safetyStock 语义**。`product-catalog.md` 库存语义化段（:124-142）为展示侧三档语义（充足/紧张/缺货），与运营侧安全库存阈值是不同概念。

**模块：** `model/app-mall.orm.xml` → 代码生成 → `app-mall-dao`（DTO + SQL-lib + mapper）→ `app-mall-service`（BizModel 阈值解析）→ `app-mall-web`（工作台 + Dashboard + 商品表单）。

**核心缺口：** `LitemallGoods` 无 per-goods 安全库存字段；两条预警路径阈值口径不一致（Path A 有 per-SKU 中间档，Path B 仅全局），运营无法按商品粒度设置聚合级预警线。

## Goals

- 为 `LitemallGoods` 新增可空 `safetyStock`（int）列，作为商品聚合级安全库存预警线。
- 统一两条预警路径的阈值解析为**三级优先**：per-SKU `safeStock`（非空且 >0）→ per-goods `safetyStock`（非空且 >0）→ 全局 `mall_stock_threshold_tight`。
- Path A（`getStockWarningList`）补 per-goods 中间档；`thresholdSource` 扩展 `{"safeStock","safetyStock","global"}`。
- Path B（Dashboard SQL-lib `getStockWarningList`）改 `HAVING` 比较 `COALESCE(g.SAFETY_STOCK, ${threshold})`，使 goods 聚合级阈值生效；`StockWarningItemBean` 补 `safetyStock`/`thresholdSource`。
- 商品编辑表单补 `safetyStock` 输入；工作台/Dashboard 明细补 safetyStock/thresholdSource 展示。
- owner doc `system-configuration.md`/`product-catalog.md` 更新三级阈值口径。
- 关闭 P18-D4 + P36 两个 deferred 条目。

## Non-Goals

- **不改 per-SKU `safeStock` 语义** — `LitemallGoodsProduct.safeStock` 仍为 SKU 粒度最高优先档，本计划在其与全局之间插入 per-goods 档。
- **不删除/不重构全局 `mall_stock_threshold_tight`** — 它仍是最终回退档；`resolveStockThreshold` 的重复副本（OrderBizModel）是否合并为本计划之外的清理项，记入 Follow-up。
- **不做历史 `safetyStock` 回填** — 新列可空，存量商品 `safetyStock=null` → 走既有 per-SKU/全局路径，零回归。
- **不改展示侧三档库存语义**（P38 充足/紧张/缺货）— 那是面向 C 端的展示语义，与本运营侧安全库存阈值是不同关注点。
- **不引入「按分类/品牌安全库存」聚合** — 仅 goods 粒度。
- **不更新 roadmap 阶段状态** — deferred successor，各 Phase Status 保持 `done`。

## Task Route

- Type: `implementation-only change`（业务语义在 owner doc 已有 per-SKU + 全局框架，本计划补 per-goods 中间档，无 net-new 业务领域，仅阈值粒度增强）
- Owner Docs: `docs/design/system-configuration.md`（G3 阈值来源 :538-542）、`docs/design/product-catalog.md`（库存语义 :124-142）
- Skill Selection Basis: ORM 新增列 → `nop-orm-modeler` + `nop-database-design`；BizModel + SQL-lib + DTO + `@BizQuery` 行为变更 → `nop-backend-dev` + `nop-testing`；前端表单/工作台/Dashboard → `nop-frontend-dev`

## Infrastructure And Config Prereqs

- 无新增端口/环境变量/外部服务。
- 新列为可空 int，无数据迁移、无回填、无不可逆变更；回滚 = 从 ORM 移除列并重新生成。

## Execution Plan

### Phase 1 - ORM 模型扩展（LitemallGoods.safetyStock）

Status: completed
Targets: `model/app-mall.orm.xml`（LitemallGoods :668-769，在 :714 videoUrl 后、:715 `</columns>` 前插入）
Required Skill: `nop-orm-modeler`, `nop-database-design`

- Item Types: `Decision | Add | Proof`
- Prereqs: 无

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读完必读文档，列路径。建模后 selfcheck。
  - Docs read: `.opencode/skills/nop-orm-modeler/SKILL.md`、`.opencode/skills/nop-database-design/SKILL.md`、`model/app-mall.orm.xml` LitemallGoods :668-769（既有 23 列，propId 1-23，无 mandatory 不可空约束历史模式：safeStock/number 等 int 列允许 null）；`LitemallGoodsProduct.safeStock` :826 同型对照。
- [x] **Decision S1：`safetyStock` 列定义。** 抉择：可空 int 列 `code="SAFETY_STOCK" name="safetyStock" propId="24" stdDataType="int" stdSqlType="INTEGER"`（无 mandatory，displayName「安全库存预警线（商品聚合级）」，`i18n-en:displayName="Safety Stock (Goods Level)"` 与既有 LitemallGoods 全列 en 风格一致）。理由：与既有 `LitemallGoodsProduct.safeStock`（:826）同型，可空保证存量零回归。备选：decimal——库存为整数单位，int 足够。残留风险：无。
- [x] **Add：** 在 `LitemallGoods` 增加 `safetyStock` 列（propId 24）。
- [x] **Proof：** `mvn install` 编译通过；`_gen/LitemallGoods.java` 含 `safetyStock` getter/setter（:116-118 PROP 常量；:306-307 字段；:1330-1343 getter/setter）；生成 DDL 含新列（`deploy/sql/mysql/_create_app-mall.sql:415 SAFETY_STOCK INTEGER NULL COMMENT '安全库存预警线（商品聚合级）'`，postgresql/oracle 同步生成）。

Exit Criteria:

- [x] `safetyStock` 列落地，propId 无冲突，命名与既有 safeStock 一致风格
- [x] S1 Decision 记录抉择/备选/残留风险
- [x] `mvn install` BUILD SUCCESS，生成产物含新列

### Phase 2 - 后端三级阈值解析 + DTO/SQL-lib + 测试

Status: completed
Targets: `app-mall-dao/.../dto/StockWarningSkuBean.java`、`StockWarningItemBean.java`、`app-mall-dao/.../sql/LitemallOrder.sql-lib.xml:125-143`、`app-mall-service/.../LitemallGoodsBizModel.java:665-713`、`LitemallOrderBizModel.java:1329-1357`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Fix | Proof`
- Prereqs: Phase 1（safetyStock 列已生成）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完必读文档，列路径。每个方法写完 selfcheck。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`（IBiz/CrudBizModel 模式 + 反模式表）、`.opencode/skills/nop-testing/SKILL.md`（IGraphQLEngine + JunitBaseTestCase + runInSession 模式）、`LitemallGoodsBizModel.getStockWarningList:665-713`（既有 per-SKU + 全局两档路径）、`LitemallOrderBizModel.getTodoAggregation:1329-1357`（既有调用 SQL-lib 传全局阈值）。
- [x] **Add：** `StockWarningSkuBean` 增加 `safetyStock`（Integer）字段；`thresholdSource` 取值集扩展含 `"safetyStock"`（per-goods 档）。— `app-mall-dao/.../dto/StockWarningSkuBean.java`
- [x] **Add：** `StockWarningItemBean`（Dashboard）增加 `safetyStock`（Integer）+ `thresholdSource`（String）字段（与 Path A 对齐）。— `app-mall-dao/.../dto/StockWarningItemBean.java`
- [x] **Fix（Path A）：** `LitemallGoodsBizModel.getStockWarningList`（:686-695）阈值解析改为三级：`product.safeStock`（非空且 >0）→ `goods.safetyStock`（非空且 >0）→ 全局。`thresholdSource` 标注实际命中档（`safeStock`/`safetyStock`/`global`），`bean.setSafetyStock(goods.getSafetyStock())`。
- [x] **Fix（Path B）：** `LitemallOrder.sql-lib.xml:getStockWarningList`（:125-143）`HAVING` 改为 `HAVING COALESCE(SUM(p.NUMBER),0) <= COALESCE(NULLIF(g.SAFETY_STOCK, 0), ${threshold})`，SELECT 增加 `g.SAFETY_STOCK AS SAFETY_STOCK` + `CASE WHEN g.SAFETY_STOCK IS NOT NULL AND g.SAFETY_STOCK > 0 THEN 'safetyStock' ELSE 'global' END AS THRESHOLD_SOURCE`；GROUP BY 增加 `g.SAFETY_STOCK`；`getTodoAggregation`（:1347-1348）保留全局阈值作为 fallback 入参（per-goods 阈值在 SQL 内 COALESCE 生效，无 Java 入参变更）。
- [x] **Proof（IGraphQLEngine）：** Path A `LitemallGoods__getStockWarningList` 三级优先测试——`testGetStockWarningListWithGoodsLevelSafetyStock`（per-SKU 空命中 per-goods）、`testGetStockWarningListSkuTierPriorityOverGoodsTier`（per-SKU 优先于 per-goods）、`testGetStockWarningListSafetyStockZeroFallsBackToGlobal`（safetyStock=0 视为无效回退 global），三场景各验证 `threshold`/`thresholdSource`。
- [x] **Proof（IGraphQLEngine）：** Path B `LitemallOrder__getTodoAggregation` 的 `stockWarning` 计数与 `stockWarningDetails` 在 per-goods safetyStock 设置时按 goods 阈值触发——`testGetTodoAggregationStockWarningWithGoodsLevelSafetyStock`：totalStock=15 > 全局 10 默认不预警；safetyStock=20 后 15 ≤ 20 触发预警且 `thresholdSource=safetyStock`。
- [x] **Proof：** 全量 `mvn test` 通过（既有预警测试无回归，全部 54 项绿）。

Exit Criteria:

- [x] 两条预警路径阈值解析统一为三级优先，口径一致
- [x] **API 测试：** `getStockWarningList` 与 `getTodoAggregation` 均经 `IGraphQLEngine` 验证三级优先语义
- [x] 全量测试绿
- [x] `docs/logs/` 更新（Phase 3 一并落日志）

### Phase 3 - 前端 + owner doc

Status: completed
Targets: `app-mall-web/.../mall/goods-ops/stock-warning.page.yaml`、`mall/stat/stat-dashboard.page.yaml`、商品编辑 view；`docs/design/system-configuration.md`、`docs/design/product-catalog.md`
Required Skill: `nop-frontend-dev`, `nop-testing`

- Item Types: `Add | Proof`
- Prereqs: Phase 2（后端契约已变更）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完必读文档，列路径。每文件完成后 selfcheck。
  - Docs read: `.opencode/skills/nop-frontend-dev/SKILL.md`（XView 三层模型 + bounded-merge + 反模式表）、`LitemallGoods/LitemallGoods.view.xml`（既有 edit form layout + bounded-merge cols 模式）、`stock-warning.page.yaml`（既有 SKU 粒度工作台 + thresholdSource mapping）、`stat-dashboard.page.yaml:158-195`（既有 Dashboard 待办聚合 + 库存预警明细）。
- [x] **Add：** `stock-warning.page.yaml` 增加 `safetyStock`（per-goods 安全库存）列；`thresholdSource` 映射扩展 `safetyStock→商品级`。同时 alert 文案改为三级优先描述。
- [x] **Add：** `stat-dashboard.page.yaml` 库存预警明细表增加 `safetyStock`/`thresholdSource` 列（与工作台对齐）。
- [x] **Add：** 商品编辑 view（admin 商品 form）增加 `safetyStock` 输入项（可空 int）—— `LitemallGoods.view.xml` edit form layout `isOnSale` 行加 `safetyStock`，`add` form 经 `x:prototype="edit"` 自动继承。
- [x] **Add（owner doc）：** `system-configuration.md` 更新 G3 为三级阈值口径（per-SKU → per-goods → global）；同步 :339 待办聚合库存预警口径与 :521 工作台方法描述；`product-catalog.md` 库存段补「运营侧安全库存阈值」子节，明确与展示侧三档语义的关注点区分。
- [x] **Proof：** 前端页面渲染冒烟（codegen 生成 `_gen/_LitemallGoods.view.xml:100/116` 已含 safetyStock 在 query/edit form；工作台/Dashboard/商品表单 safetyStock 可见可编辑）；owner doc 口径与实现一致。

Exit Criteria:

- [x] 三处前端 surface 补 safetyStock 展示/编辑
- [x] owner doc 三级口径落地，与实现一致
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed（共识达成：Round 1 PASS）
- Auditor / Agent: 独立 subagent，fresh session（task `ses_0efdaaaebffejEqJGZy9f40DfZ`）
- Evidence: 全量 baseline 引用经 live repo 核验准确（LitemallGoods 无 safetyStock、propId 24 空闲；两条预警路径口径分歧确认 Path B 仅全局；DTO/SQL-lib/前端/owner doc 缺口确认；P18-D4 + P36 源 deferred verbatim）。三级优先阈值语义在两路径均正确（Path B 聚合层仅 goods→global 两档，per-SKU 不适用，COALESCE SQL 正确）。单结果面、Protected Area（ORM ask-first）合规、anti-slacking 合规、Required Skill 每 phase 齐全、API 测试规则（getStockWarningList/getTodoAggregation 经 IGraphQLEngine）满足。Minors（cosmetic/implementation-detail）：i18n-en:displayName 省略、Path B thresholdSource 计算位置未指定、Goals 措辞对 Path B 档数轻微夸大——均不阻塞，执行时按既有 repo 约定处理。

## Closure Gates

- [x] in-scope behavior is complete（live repo 核验：`model/app-mall.orm.xml:715` 列已落地；`LitemallGoodsBizModel.java:684-712` 三级优先阈值解析 + `bean.setSafetyStock`；`LitemallOrder.sql-lib.xml:133-142` COALESCE/NULLIF/HAVING；`StockWarningSkuBean.java:17` + `StockWarningItemBean.java:15-16` 字段；`stock-warning.page.yaml:41/45/50` + `stat-dashboard.page.yaml:196/198/202` + `LitemallGoods.view.xml:96` 前端三处）
- [x] relevant docs are aligned（`system-configuration.md:339/521/540-548` Decision G3 三级口径 + `product-catalog.md:144-149` 运营侧安全库存阈值子节，经 grep 核验）
- [x] verification has run（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` + `./mvnw test` 全绿 known-good baseline，54 项测试通过含 4 新增三级阈值场景；前端渲染冒烟见 `_gen/_LitemallGoods.view.xml:79/100/116`）
- [x] 新增/变更的 `@BizQuery`（`getStockWarningList` 行为变更经 `TestLitemallGoodsOpsWorkbench` 3 场景：`testGetStockWarningListWithGoodsLevelSafetyStock`/`testGetStockWarningListSkuTierPriorityOverGoodsTier`/`testGetStockWarningListSafetyStockZeroFallsBackToGlobal`；`getTodoAggregation` 行为变更经 `TestLitemallOrderStatisticsBizModel.testGetTodoAggregationStockWarningWithGoodsLevelSafetyStock`）— 两测试类均通过 `IGraphQLEngine.newRpcContext` 调用 `LitemallGoods__getStockWarningList`/`LitemallOrder__getTodoAggregation`，非实体级纯逻辑测试
- [x] no in-scope item downgraded to deferred/follow-up（Deferred But Adjudicated 区仅含「resolveStockThreshold 重复副本合并」一项，分类 optimization candidate，与本计划三级阈值正确性解耦，非 in-scope 降级）
- [x] plan audit passed before implementation（Round 1 PASS，task `ses_0efdaaaebffejEqJGZy9f40DfZ`，见 `Plan Audit` 段）
- [x] each phase has `Required Skill` listed（Phase 1: `nop-orm-modeler`, `nop-database-design`；Phase 2: `nop-backend-dev`, `nop-testing`；Phase 3: `nop-frontend-dev`, `nop-testing`）
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck（Docs read 路径已列在各 phase 的 Skill loading gate item 中）
- [x] text consistency verified: status/phases/gates/log 一致（Plan Status `completed` / 3 Phase Status 均 `completed` / 3 Phase Exit Criteria 全 `[x]` / Closure Gates 全 `[x]` / `docs/logs/2026/06-29.md` 三 Phase 完整记录）
- [x] closure audit was performed by a different agent/session（本次 closure audit 由独立 closure auditor fresh session 执行，非实施 agent）
- [x] closure evidence exists in files（见下 `Closure Audit Evidence` 段；live repo 核验记录在案）
- [x] P18-D4 + P36 两个源 deferred 条目在本计划 closure 后于各自源计划标注「已由 successor 关闭」（P18 `2026-06-28-1027-1-phase18-dashboard-redesign-plan.md:142`；P36 `2026-06-28-1027-3-phase36-goods-operations-enhancement-plan.md:159`，均 grep 核验到位）

## Deferred But Adjudicated

### resolveStockThreshold 重复副本合并

- Classification: `optimization candidate`
- Why Not Blocking Closure: `LitemallGoodsBizModel.resolveStockThreshold`（:286-297）与 `LitemallOrderBizModel.resolveStockThreshold`（:2236-2247）重复；本计划三级阈值主逻辑在各自 BizModel 内就近实现，合并为共享 helper 为独立清理项，不影响三级优先正确性。
- Successor Required: `no`（触发条件：下次重构库存预警阈值解析时统一抽取 helper）

## Closure

<!-- 闭合审计必须由独立 subagent 执行，勿自行填写。 -->

Status Note: 三 Phase 全部完成。`LitemallGoods.safetyStock` 列已落地（propId=24，可空 int），两条预警路径（Path A SKU 粒度工作台 + Path B Dashboard goods 聚合）阈值解析统一为三级优先（per-SKU safeStock → per-goods safetyStock → global mall_stock_threshold_tight），口径一致；DTO/SQL-lib/前端/owner doc 全部对齐。`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` + `./mvnw test` 全绿 known-good baseline（54 项测试通过含 4 新增三级阈值场景）。源 deferred 项 P18-D4 / P36 已分别于各自源计划标注「已由 successor 关闭」。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure auditor（fresh session，非实施 agent；本任务由 MISSION_DRIVER `[MISSION_DRIVER] You are an independent closure auditor` 触发）
- Audit Date: 2026-06-29
- Live-repo evidence verified:
  - **Phase 1（ORM）:** `model/app-mall.orm.xml:715` `<column code="SAFETY_STOCK" name="safetyStock" propId="24" stdSqlType="INTEGER"/>`；`app-mall-dao/.../_gen/_LitemallGoods.java:116-118/306-307/1327-1343` 含 PROP 常量/字段/getter-setter；`app-mall-dao/.../_vfs/app/mall/orm/_app.orm.xml:752` 同步生成；propId 24 无冲突（`videoUrl`=23 在 :714）。
  - **Phase 2（后端）:** `LitemallGoodsBizModel.java:684-712` 三级优先阈值解析 + `thresholdSource ∈ {"safeStock","safetyStock","global"}` + `bean.setSafetyStock(goods.getSafetyStock())`；`LitemallOrder.sql-lib.xml:133-135/141-142` `SELECT g.SAFETY_STOCK AS SAFETY_STOCK` + `CASE WHEN ... 'safetyStock' ELSE 'global' END` + `HAVING COALESCE(SUM(p.NUMBER),0) <= COALESCE(NULLIF(g.SAFETY_STOCK,0), ${threshold})`（NULLIF 处理 safetyStock=0 与 null 等价，与 Java 「非空且 >0」口径一致）+ `GROUP BY ... g.SAFETY_STOCK`；`StockWarningSkuBean.java:17` `safetyStock` + `StockWarningItemBean.java:15-16` `safetyStock`/`thresholdSource`。
  - **Phase 3（前端 + doc）:** `stock-warning.page.yaml:5/41/45/50`（alert 三级文案 + safetyStock 列 + thresholdSource 映射 `safetyStock→商品级`）；`stat-dashboard.page.yaml:196-202`（明细 safetyStock/thresholdSource 列）；`LitemallGoods.view.xml:96`（edit form `isOnSale` 行后加 `safetyStock`，标签含「空或0表示不启用商品级阈值」）；`_gen/_LitemallGoods.view.xml:79/100/116` codegen 已含；`system-configuration.md:339/521/540-548`（G3 三级口径）+ `product-catalog.md:144-149`（运营侧阈值子节）。
  - **测试（IGraphQLEngine）:** `TestLitemallGoodsOpsWorkbench`（`@Inject IGraphQLEngine graphQLEngine` :53；`callQuery` 经 `graphQLEngine.newRpcContext(GraphQLOperationType.query, "LitemallGoods__getStockWarningList", ...)`）三个新增场景 `testGetStockWarningListWithGoodsLevelSafetyStock`（:421）/ `testGetStockWarningListSkuTierPriorityOverGoodsTier`（:459）/ `testGetStockWarningListSafetyStockZeroFallsBackToGlobal`（:484）；`TestLitemallOrderStatisticsBizModel` `testGetTodoAggregationStockWarningWithGoodsLevelSafetyStock`（:142，经 `LitemallOrder__getTodoAggregation` GraphQL 调用）。
  - **源 deferred 关闭标记:** P18-D4 `2026-06-28-1027-1-phase18-dashboard-redesign-plan.md:142`；P36 `2026-06-28-1027-3-phase36-goods-operations-enhancement-plan.md:159`，均带「已由 successor 关闭（2026-06-29）」+ 实施摘要。
  - **日志:** `docs/logs/2026/06-29.md` 含三 Phase 完整记录（Skill loading / Decision S1 / Add / Fix / Proof / 验证结果）。
- Anti-Hollow check: `getStockWarningList` 与 `getTodoAggregation` 均非空实现/非 `return null` placeholder；新列被两条预警路径运行时消费（Path A `goods.getSafetyStock()` :684、:693、:708；Path B SQL `g.SAFETY_STOCK`）；DTO 字段被前端 page.yaml 与测试断言消费。
- Five-point consistency: Plan Status `completed` / 3 Phase Status 均 `completed` / 3 Phase Exit Criteria 全 `[x]` / Closure Gates 全 `[x]` / 日志条目一致。
- Deferred honesty: Deferred But Adjudicated 区仅「resolveStockThreshold 重复副本合并」一项，分类 optimization candidate，与本计划三级阈值正确性解耦，非隐藏缺陷。
- Verdict: **approved** — 三 Phase Exit Criteria 全部对齐 live repo，无 hollow 实现；新增/变更的 `@BizQuery` 经 `IGraphQLEngine` 验证三级优先语义（非实体级纯逻辑测试）；源 deferred 项 P18-D4 / P36 已于各自源计划标注 successor closed；文本一致性、Protected Area（ORM ask-first 由 MISSION_DRIVER「execute the entire plan」授权，与项目先例一致）、anti-slacking 合规。计划可关闭。

Follow-up:

- 无新增非阻塞 follow-up；唯一非阻塞项「resolveStockThreshold 重复副本合并」已记录在 `Deferred But Adjudicated` 区（分类 optimization candidate，触发条件：下次重构库存预警阈值解析时统一抽取 helper）。
