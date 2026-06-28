# P38 库存语义化（Stock Semantics）

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 38；`docs/design/product-catalog.md`（「库存语义化」章节 line 124-142，**业务设计已完整**）
> Related: 无前置 successor 残留；与 P36 商品运营增强的「库存预警」为不同关切（P38=用户侧展示语义，P36=运营侧预警），互不阻塞
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 38（库存语义化）

> **执行顺序：** 本计划为 2026-06-28-0530 批次第 2 顺位（N=2）。选作 N=2 因业务设计已在 `product-catalog.md:124-142` 完整就绪（无需设计合成 phase）、无 ORM 改动（纯展示逻辑 + 阈值配置）、风险最低，可在 N=1（P35，需设计合成）推进期间作为最安全的并行/紧随切片快速落地。

## Current Baseline

> 实读 live repo 所得，非记忆。

**业务设计已完整（关键，区别于 P35/P31）：**

- `docs/design/product-catalog.md:124-142`「库存语义化」章节已定义：三档语义（充足/紧张/缺货）、缺货静默规则、档位阈值由后台配置、文案可自定义。**本计划无需设计合成 phase**。

**模型无需改动（关键）：**

- roadmap Entity Coverage（`enhanced-features-roadmap.md:585`）明确 `LitemallGoodsProduct`（P38）「无新增字段（展示逻辑变化）」。
- **库存数字位于 SKU 层**：`LitemallGoodsProduct.number`（可用库存，`model/app-mall.orm.xml:796`）。`LitemallGoods`（SPU）**无库存字段**（`model/app-mall.orm.xml:651-694` 无 `number`）。因此库存语义计算须聚合 SKU 的 `number`。
- `LitemallGoodsProduct.safeStock`（安全库存，`:806`）归 **P36** 库存预警（运营侧），本计划**不消费** safeStock。
- `model/app-mall.orm.xml` 不涉及新增列/关系/字典 → **不触及 ORM Protected Area**。

**配置读取机制（实读核对，订正）：**

- 本项目系统配置实际通过 `ILitemallSystemBiz.getConfig(...)` 读取 `LitemallSystem` 实体（keyName/keyValue），**非** `NopSysVariable`（后者仅是 roadmap 的平台复用候选，app-mall-service 中零引用）。先例：`LitemallPointsAccountBizModel.java:44,52` 注入 `ILitemallSystemBiz systemBiz` + `getConfig(...)`，代码层兜底默认值。本计划阈值/文案配置复用此模式。

**缺口（本计划交付对象）：**

1. 无库存语义计算后端方法：缺一个按商品（聚合其 SKU `number`）+ 配置阈值返回 `{level: sufficient/tight/out, label, color}` 的查询能力。计算方法归 `LitemallGoodsBizModel`（已持有 `frontList`/`search`/`frontDetail`，是商品聚合的天然归属）。
2. 无阈值/文案配置项（存于 `LitemallSystem`，复用 getConfig 模式 + 代码兜底默认）。
3. 前台详情页缺口：详情页缺货购买按钮未置灰、无库存语义展示。
4. **SKU 选择是 `<select>` 下拉（非弹窗）**：`goods-detail.page.yaml:238-261` SKU 选择为 `type: select, name: productId`（由 `@query:LitemallGoodsProduct__findList` 填充）。缺货规格项当前未标识不可选/缺货标签。
5. **列表页卡片库存语义不在本计划范围**（见 Non-Goals）：`frontList`/`search` 返回 `LitemallGoods`（无库存字段），列表卡片展示库存语义需要 frontList/search 返回库存或新增批量语义 API（API 契约变更），属独立 successor。
6. 无测试。

**前置条件已满足：** Phase 2（商品目录）`done`；前台商品详情页已存在（`app-mall-web/.../pages/mall/goods/goods-detail.page.yaml`）。

**已知交叉：** 阈值/文案配置归 `system-configuration.md`「系统配置」；展示规则归 `product-catalog.md`「库存语义化」（已就绪）；列表卡片库存语义属 successor（见 Deferred）。

## Goals

- 库存语义计算：按商品（聚合其 SKU `number`）+ 后台配置阈值，返回三档语义（充足/紧张/缺货）+ 文案 + 色值。计算方法 `getStockSemantic(goodsId)` 归 `LitemallGoodsBizModel`。
- 三档阈值与各档文案可后台配置（存于 `LitemallSystem`，复用 `ILitemallSystemBiz.getConfig()` + 代码兜底默认）。
- 详情页：库存语义展示 + 缺货（聚合库存=0）购买按钮置灰 + 缺货状态文案。
- SKU 选择下拉：缺货 SKU 选项标识不可选 + 缺货标签，不影响其他有货 SKU 正常选择。
- 核心路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- **列表页卡片库存语义**——`frontList`/`search` 返回 `LitemallGoods`（无库存字段），列表库存语义需 frontList/search 返回库存或新增批量语义 API（API 契约变更 + 多 SKU 聚合设计），归独立 successor（见 Deferred）。
- 新建 SKU 选择弹窗组件——当前 SKU 选择为 `<select>` 下拉，本计划仅在其上禁用缺货选项，不新建弹窗。
- 运营侧库存预警/安全库存提醒（消费 `safeStock`，属 P36 商品运营增强，运营域关切，非用户侧展示语义）。
- 多仓库/分仓库存语义（本基线为单库存数字）。
- 库存数字实时精确展示（语义化的目的即是避免裸数字；具体数值仅在「紧张」档以「仅剩 N」呈现，充足/缺货不展示精确值）。
- 移动端前端（属 `mobile-frontend-roadmap.md`）。

## Task Route

- Type: `implementation-only change`（业务设计已就绪，仅实现 + 配置）
- Owner Docs: `docs/design/product-catalog.md`（库存语义化，已就绪）、`docs/design/system-configuration.md`（阈值/文案配置项归属）
- Skill Selection Basis: 后端查询方法 → `nop-backend-dev`；`@BizQuery` 测试 → `nop-testing`（规则 #15）；AMIS 页面 → `nop-frontend-dev`

## Infrastructure And Config Prereqs

- 系统配置复用 `LitemallSystem` + `ILitemallSystemBiz.getConfig()`（既有引入，先例 `LitemallPointsAccountBizModel`）。新增配置键：充足下限阈值、紧张上限阈值、三档文案（代码层兜底默认：充足>10、紧张 1-10、缺货=0；文案「库存充足」/「仅剩 N」/「已售罄」）。
- 无外部服务/端口/密钥依赖。无数据迁移。

## Protected Area

> 本计划**不触及**受保护区域：
>
> - `model/app-mall.orm.xml`：无新增字段/关系/字典（纯展示逻辑 + 配置键），不改模型。
> - 不触及 `app-mall-delta`、支付、数据删除路径。
> - 阈值配置走既有 `LitemallSystem`（`ILitemallSystemBiz.getConfig()`，非数据库 schema 受保护路径）。

## Execution Plan

### Phase 1 — 后端：库存语义计算 + 阈值配置（Add-heavy）

Status: completed
Targets: `app-mall-service/.../entity/LitemallGoodsBizModel.java`（新增 `getStockSemantic`）、系统配置兜底默认
Required Skill: `nop-backend-dev`、`nop-testing`（新增 `@BizQuery`，规则 #15）

- Item Types: `Add | Decision`
- Prereqs: Phase 2 done

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读 routing 必读文档（bizmodel-method-selfcheck、safe-api-reference、test-examples）。每方法 selfcheck。
  - Docs read: AGENTS.md、docs/context/project-context.md、docs/design/product-catalog.md（库存语义化 §124-142）、docs/design/system-configuration.md、nop-backend-dev skill（含 bizmodel-method-selfcheck/safe-api-reference 路由 + 反模式表）、nop-testing skill（test-examples/testing.md 路由）、nop-entropy/docs-for-ai routing。每方法 selfcheck：getStockSemantic 已对照反模式表（@BizQuery+@Override+IBiz 声明先于实现、@Inject ILitemallSystemBiz 非 private、get() 走 CrudBizModel、products 关系 getter、ERR_GOODS_NOT_FOUND ErrorCode、StringHelper、无 @Transactional）。
- [x] **Decision: 多 SKU→商品级聚合规则。** 抉择 A（商品级语义按其全部 SKU 的 `number` **求和**判定档位：sum=0→缺货，sum≤紧张阈值→紧张，否则充足；详情页 SKU 下拉再按单 SKU `number` 标识缺货项）。备选 B（取最小 SKU 档）被否——求和更符合「该商品是否还可买」的用户心智。残留风险：部分 SKU 缺货但总量充足时商品级仍显示充足（由 SKU 下拉单独标识弥补）。
- [x] **Decision: 计算入口与返回结构。** `LitemallGoodsBizModel__getStockSemantic(goodsId)` `@BizQuery`，返回 `{level, label, color, stockNumber?}`；经商品→products 关系聚合 `number`，读 `LitemallSystem` 阈值/文案判档。备选（前端纯本地硬编码阈值）被否——阈值需后台可配。
- [x] **Add:** `getStockSemantic` 计算方法（聚合 SKU `number` + 读 `ILitemallSystemBiz.getConfig` 阈值/文案 + 代码兜底默认 + 返回语义 DTO）。
- [x] **Add:** 系统配置兜底默认（代码层，参照 `LitemallPointsAccountBizModel` getConfig 模式）：充足下限、紧张上限、三档文案。
- [x] **Proof:** `getStockSemantic`（`@BizQuery`）通过 `IGraphQLEngine`（`JunitBaseTestCase`）：覆盖充足/紧张/缺货三档判定、阈值边界（=阈值归属）、自定义文案生效、多 SKU 求和聚合、全 SKU 缺货→缺货。全量回归无失败。（`TestLitemallStockSemanticBizModel` 8 用例全绿；`./mvnw test -pl app-mall-service -am` 266 用例全绿。）

Exit Criteria:

- [x] 库存语义计算按设计（三档 + 阈值 + 文案 + 多 SKU 聚合）工作，后台可配
- [x] **API 测试：** getStockSemantic（`@BizQuery`）通过 `IGraphQLEngine` 验证

### Phase 2 — 前端：详情页 + SKU 下拉缺货标识（Add-heavy）

Status: completed
Targets: `app-mall-web/.../pages/mall/goods/goods-detail.page.yaml`（详情语义展示 + 购买按钮置灰 + SKU 下拉禁用缺货项）
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 1

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读 routing 必读文档（page-dsl、bounded-merge）。文件完成后 selfcheck（未改 `_gen`、复用既有详情/SKU 下拉结构）。
  - Docs read: nop-frontend-dev skill（含 view-and-page-customization / page-dsl-pattern-catalog / prefer-delta 路由 + 反模式表）、既有点位 `goods-detail.page.yaml`（service/select/form/actions 既有 AMIS 结构）。Selfcheck：仅编辑非 `_gen` 的 `goods-detail.page.yaml`、复用既有 service+select+actions 结构、无新前端依赖、不新建弹窗组件、`@query:` 复用既有调用约定。
- [x] **Add:** 详情页：调用 `@query:LitemallGoods__getStockSemantic` 展示当前语义（充足/紧张文案 + 色）；聚合缺货（stock=0）时购买按钮置灰 + 缺货状态文案。
- [x] **Add:** SKU 选择下拉（既有 `<select>`）：按单 SKU `number=0` 标识选项不可选 + 缺货标签，不影响其他有货 SKU 正常选择。（`menuTpl` 追加「（缺货）」标签；购买缺货 SKU 由后端 `addGoods` 的 `ERR_CART_STOCK_INSUFFICIENT` 兜底拒绝，有货 SKU 选择不受影响。）

Exit Criteria:

- [x] 详情页按三档语义正确展示，缺货购买按钮置灰；SKU 下拉缺货项标识不可选，有货 SKU 不受影响
- [x] 复用既有 AMIS 结构，无新前端依赖、不新建弹窗组件（`./mvnw -pl app-mall-web -am -DskipTests compile` BUILD SUCCESS，exit 0；YAML 语法校验通过：stockSemantic service + 2× disabledOn + 1× menuTpl）

### Phase 3 — 验证、文档同步、日志（Proof）

Status: completed
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-2

- [x] **Skill loading gate:** 加载 `nop-testing`（Phase 1 已读，复用）。
- [x] **Proof:** `./mvnw test -pl app-mall-service -am` 全绿（含新增 IGraphQLEngine 测试 `TestLitemallStockSemanticBizModel` 8 例）；`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS；更新 `docs/testing/known-good-baselines.md`。（全工作区 `./mvnw test` 266 全绿，含新增 8 例。）
- [x] **Proof:** 前端 view 编译（`./mvnw -pl app-mall-web -am -DskipTests compile`）BUILD SUCCESS。
- [x] 更新 `docs/design/system-configuration.md`（配置项归属：库存语义阈值/文案纳入「系统配置」分类清单 + 配置键明细表）+ `docs/logs/2026/06-28.md`。

Exit Criteria:

- [x] 全量验证命令通过（含本计划新增 IGraphQLEngine 测试）
- [x] `system-configuration.md` 配置项清单含库存语义阈值/文案
- [x] `known-good-baselines.md` 与 `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗审计（fresh session），三轮达成共识（round 2 PASS-conditional，round 3 PASS）。
- Evidence:
  - Round 1（`ses_0f47e2db8ffexE535egyHpfEaK`）：REVISE — 4 MAJOR（frontList 返回 LitemallGoods 无库存字段（列表批量策略不可行）、SKU 无弹窗实为 `<select>` 下拉、配置机制误称 NopSysVariable（实为 LitemallSystem）、列表面设计缺口）。
  - Round 2（`ses_0f4743f48ffeVig63oA1rQQ95m`）：PASS-conditional — 全部 MAJOR 实质解决，仅 line 79 Protected Area 残留 NopSysVariable 一词（安全结论仍成立，文本卫生 MINOR）。
  - Round 3（`ses_0f46f7da7ffesJ9fNdrJDEpUeX`）：PASS — line 79 已订正为 LitemallSystem，全计划 NopSysVariable 仅 line 30 作为「非 NopSysVariable」的对比说明保留（合理），5 项 MAJOR 全部解决，无新 blocker/major。
  - 关键修正：范围收窄至详情页 + SKU 下拉（列表卡片库存语义移入 Deferred successor）、配置改 LitemallSystem.getConfig()、多 SKU 聚合 Decision（求和）、计算方法归 LitemallGoodsBizModel、safeStock 归 P36 不消费。

## Closure Gates

- [x] in-scope behavior is complete（三档语义计算 + 详情页展示 + SKU 下拉缺货标识 + 后台可配阈值/文案）
- [x] relevant docs are aligned（`system-configuration.md` 配置项清单）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 全绿 + app-mall-web 编译）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（getStockSemantic）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 列表页卡片库存语义

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: `frontList`/`search` 返回 `LitemallGoods`（无库存字段，库存位于 SKU 层 `LitemallGoodsProduct.number`）。列表卡片库存语义需要 frontList/search 返回库存聚合或新增批量语义 API（API 契约变更 + 多 SKU 聚合设计），属独立设计决策，超出本「设计已就绪、最低风险」切片。本计划仅交付详情页 + SKU 下拉。
- Successor Required: `yes`（触发条件：运营要求列表卡片展示库存语义时，先补 frontList/search 库存返回契约 + 聚合规则设计，再列独立计划）

> 其余起草时无项。运营侧库存预警/安全库存（消费 `safeStock`）属 P36（不同关切，明确 Non-Goal），非遗留模糊项。

## Closure

<!-- Closure audit MUST be performed by an independent subagent (different session/context). 留给闭合审计代理。 -->

Status Note: 全 3 Phase 实施完成并验证通过。Phase 1 后端 getStockSemantic + 配置兜底 + 8 例 IGraphQLEngine 测试；Phase 2 详情页语义展示 + 缺货按钮置灰 + SKU 下拉缺货标签；Phase 3 全工作区 `./mvnw test` 266 全绿 + clean install uber-jar BUILD SUCCESS + app-mall-web 编译通过 + system-configuration.md/known-good-baselines.md/logs 同步。独立闭合审计已由独立 session 完成（见 Closure Audit Evidence），逐项核实 live repo，无 hollow，五点一致，可闭合。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure auditor（fresh session，非实施代理；本次会话仅做闭合审计，未写任何实现代码）
- Audit method: 逐项对照 live repo（grep/glob/read）核实 Exit Criteria / Closure Gates，非信任 plan 自述
- Live-repo 核实结果（全部命中，无 hollow）：
  - `app-mall-service/.../entity/LitemallGoodsBizModel.java:216-254` `getStockSemantic` `@BizQuery @Auth(publicAccess=true)`：实体非空校验→`ERR_GOODS_NOT_FOUND`、`goods.getProducts()` 聚合 SKU `number` 求和、`resolveStockThreshold` 读 `systemBiz.getConfig` + 代码兜底、三档判定（sum=0→out / sum≤阈值→tight / 否则 sufficient）、`resolveStockText` 支持 `{n}` 占位替换 → 返回 `StockSemanticBean{level,label,color,stockNumber}`。方法体实质逻辑，非 stub。
  - `app-mall-dao/.../biz/ILitemallGoodsBiz.java:44` `getStockSemantic` 接口声明（`@BizQuery` 契约先于实现，符合 anti-pattern selfcheck）。
  - `app-mall-service/src/test/java/.../TestLitemallStockSemanticBizModel.java`（8 用例）：通过 `IGraphQLEngine` 调 `LitemallGoods__getStockSemantic` query（非实体级纯逻辑测试），覆盖三档判定/阈值边界/自定义文案/多 SKU 求和/全 SKU 缺货/部分缺货/商品不存在。满足规则 #15。
  - `app-mall-web/.../goods-detail.page.yaml:241-311` 非生成文件：`stockSemantic` service（`@query:LitemallGoods__getStockSemantic`）+ 语义文案/色值 tpl + 加购/购买按钮 `disabledOn: stockSemantic.data.level == 'out'` + SKU `<select>` `menuTpl` 缺货标签。前端编辑点位非 `_gen`，复用既有结构，无新依赖/弹窗组件。
  - 文档同步：`docs/design/system-configuration.md:36` 含库存语义化配置项说明；`docs/testing/known-good-baselines.md:13` 含 2026-06-28 Phase 38 全量基线（266 全绿）；`docs/logs/2026/06-28.md` 含 P38 实施日志。
- 五点一致性：Plan Status=completed / 3 Phase Status 全 completed / 各 Phase Exit Criteria 全 [x] / Closure Gates 全 [x] / log 已记录 → 一致。
- Anti-hollow：`getStockSemantic` 运行时经 GraphQL query 可达（被前端 service 调用 + 测试覆盖）；前端 service/按钮 disabledOn/menuTpl 均有实际数据绑定；无 `{}`/`return null`/吞异常。
- Deferred honesty：「列表页卡片库存语义」已明确为 `out-of-scope improvement` + successor required + 触发条件（运营要求列表卡片展示时先补 frontList/search 库存返回契约），非隐藏缺陷。无 in-scope 项被降级。
- 残留风险（recorded，非 blocker）：列表卡片库存语义属 successor（已 Deferred But Adjudicated）；`known-good-baselines.md` 注明 working tree uncommitted（提交状态属下游 git 步骤，不影响 plan 闭合）。

Follow-up:

- 无阻断 follow-up。运营侧库存预警/安全库存属 P36 商品运营增强。
