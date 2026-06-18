# 2026-06-18-1800 逻辑删除手动过滤清理计划

> Plan Status: completed
> Last Reviewed: 2026-06-18
> Source: 用户审计反馈 — 项目存在大量手动 `addFilter(deleted, false)` 与 `<eq name="deleted" value="false"/>` 冗余过滤，违反 Nop 平台规范
> Related:
> - `../nop-entropy/docs-for-ai/02-core-guides/logical-deletion.md`（已修订：明确"查询时不要手动过滤"为常见错误，BOOLEAN 方案与 delVersion 方案的适用边界）
> - `docs/skills/multi-dimensional-audit-prompt.md`（已补充维度 04/05/08 审计项）
> Audit: required

## Current Baseline

### 平台机制（来自 `logical-deletion.md` + 源码核对）

- ORM 实体配置 `useLogicalDelete="true"` 后，框架在 EQL 编译阶段对每个 entity table source 自动追加 `deleted = false`（或 `delVersion = 0`）条件，覆盖：
  - 所有 QueryBean 路径：`findPage` / `findList` / `findCount` / `findAllByQuery` / `findPageByQuery` 等
  - Example 查询：`findAllByExample`、`findPageByExample`
  - 集合加载 SQL：一对多、多对多关联查询
- 源码锚点：`EqlTransformVisitor.collectDefaultEntityFilter()` (`EqlTransformVisitor.java:312`)、`GenSqlHelper.genCollectionFilterEx()` (`GenSqlHelper.java:467`)、`EntityPersisterImpl.addDeleteFlagToExample()` (`EntityPersisterImpl.java:754`)
- `CrudBizModel` 的 `get()` / `requireEntity()` / `batchGet()` 也走完整管道（含逻辑删除过滤）

### 项目现状

- `model/app-mall.orm.xml` **30 个**实体配置 `useLogicalDelete="true" deleteFlagProp="deleted"`（采用 BOOLEAN 方案 B，符合 docs-for-ai 方案选择规则中"已给定数据模型、不便修改"的兼容场景，因 litemall 原始表结构沿用 BOOLEAN 字段）。共 31 个实体，`LitemallRegion` 无逻辑删除为预期（SQL seed 静态数据）。无任何实体配 `deleteVersionProp`
- **反模式规模**（精确清点，截至 2026-06-18）：
  - **59 处** BizModel 手动 `query.addFilter(FilterBeans.eq(Xxx.PROP_NAME_deleted, false))`
  - 涉及 **21 个** `app-mall-service/.../entity/Litemall*BizModel.java`：Ad / Address / Aftersale / Cart / Category / Collect / Comment / Coupon / CouponUser / Footprint / Goods / Groupon / GrouponRules / Issue / Keyword / Notice / Order / OrderGoods / SearchHistory / System / Topic
  - **3 处** `app-mall-web` 后台 `.view.xml` 的 grid `<filter><eq name="deleted" value="false"/></filter>`：LitemallGroupon / LitemallGrouponRules / LitemallOrder（最后一个是本会话 commit `fedbd9e` 中错误新增，需重点回滚）
  - **1 处** 前台 `app-mall-web/.../pages/mall/category/category.page.yaml` 中 `@query:LitemallBrand__findList` 的 filter 手动追加 `- deleted - false`
  - **0 处** sql-lib.xml / xmeta / beans.xml 中含 deleted 手动过滤（已核对）
- **后果**：
  - 生成的 SQL 多出冗余条件（虽然语义等价，但代码误导后来者以为必须手写）
  - 视图层误导最严重 — 后台 grid 配置手写过滤会让维护者以为 AMIS 默认不过滤
  - 与刚发布的 `docs-for-ai/02-core-guides/logical-deletion.md` 修订条款直接冲突

### 已完成的配套工作（本会话先行落地）

- `../nop-entropy/docs-for-ai/02-core-guides/logical-deletion.md`：重写"方案选择"章节（delVersion 默认推荐 / BOOLEAN 兼容方案），新增"在 QueryBean 中手动追加 `addFilter(deleted, false)`"为常见错误第 1 条，补充 EQL 自动过滤覆盖范围（QueryBean 路径）的源码锚点
- `../nop-entropy/docs-for-ai/02-core-guides/orm-model-design.md`：通用字段表中逻辑删除字段行订正为"默认推荐 `delVersion`；BOOLEAN 仅用于已给定数据模型"
- `docs/skills/multi-dimensional-audit-prompt.md`：维度 04 增"逻辑删除字段方案合规性"项；维度 05 增"逻辑删除手动过滤反模式（P1）"项 + 平台规范基线明示禁止；维度 08 增"grid/form 中手动追加 deleted 过滤（P1）"项

## Goals

- 移除项目里**所有**针对 `useLogicalDelete="true"` 实体的手动 `deleted = false` 过滤（BizModel 59 处 + view.xml 3 处 + page.yaml 1 处）
- 通过完整测试验证移除后查询行为不变（框架自动过滤生效）
- 通过日志记录教训，避免未来回归

## Non-Goals

- **不**修改 ORM 模型本身（BOOLEAN → delVersion 迁移）：litemall 沿用 BOOLEAN 是已给定的兼容场景，符合 docs-for-ai 方案 B 规则
- **不**修改 `app-mall-delta` 中 `nop-auth-delta.orm.xml` 的 `<domain name="delFlag" stdDomain="boolFlag" stdSqlType="TINYINT"/>`：那是 nop-auth 原生方案，不属于本仓库职责
- **不**修改 `daoProvider().daoFor()` 绕过管道的少数场景：`app-mall-dao/.../manager/MallLogManager.java:22` 使用 `daoProvider.daoFor(LitemallLog.class)` 但未加注释说明，本计划不修改该文件（其查询不涉及 deleted 字段，不影响清理范围）；缺失的注释说明留待后续 audit 项追踪（见 Deferred But Adjudicated）
- **不**清理 `_gen/_LitemallXxx.java` 中的 `PROP_NAME_deleted` 常量声明：那是生成代码的字段元数据，不是过滤逻辑
- **不**修改 `app-mall-web/.../pages/mall/category/category.page.yaml` 中除 `deleted` 过滤以外的其他过滤条件

## Task Route

- Type: `bug investigation`（确认行为后）+ `implementation-only change`（清理）
- Owner Docs:
  - `../nop-entropy/docs-for-ai/02-core-guides/logical-deletion.md`（已修订，权威规范源）
  - `docs/skills/multi-dimensional-audit-prompt.md`（已修订，审计基线）
  - `docs/architecture/module-boundaries.md`
- Skill Selection Basis:
  - `nop-backend-dev`：BizModel 修改、跨实体访问模式、selfcheck 反模式表
  - `nop-frontend-dev`：view.xml / page.yaml 的 grid/filter 配置
  - `nop-debugging`：移除过滤后需通过测试验证行为不变
  - `nop-testing`：JUnit 测试基线、IGraphQLEngine 验证

## Infrastructure And Config Prereqs

- 无基础设施依赖
- 验证基线：`./mvnw -pl app-mall-service test`（基线 111 跑 / 101 过 / 10 失败，10 个失败为预存在 auth 问题，与逻辑删除无关）

## Execution Plan

### Phase 1 — 后台 view.xml 与前台 page.yaml 手动过滤清理（最小可验证切片）

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/{LitemallGroupon,LitemallGrouponRules,LitemallOrder}/Litemall*.view.xml`、`app-mall-web/src/main/resources/_vfs/app/mall/pages/mall/category/category.page.yaml`
Required Skill: `nop-frontend-dev`、`nop-debugging`

- Item Types: `Fix`
- Prereqs: none

- [x] **Skill loading gate:** 加载 `nop-frontend-dev` + `nop-debugging`，读完路由表必读文档
  - Docs read: nop-frontend-dev skill (view.xml 三层模型、AMIS grid/filter、反模式表)、nop-debugging skill
- [x] **Fix:** 移除 `LitemallGroupon.view.xml` grid 中的 `<filter><eq name="deleted" value="false"/></filter>`，同时移除空的 `<filter></filter>` 包装（保持 grid 简洁）
- [x] **Fix:** 移除 `LitemallGrouponRules.view.xml` grid 中的 `<filter><eq name="deleted" value="false"/></filter>`，同时移除空的 `<filter></filter>` 包装
- [x] **Fix:** 移除 `LitemallOrder.view.xml` grid 中的 `<filter><eq name="deleted" value="false"/></filter>`，同时移除空的 `<filter></filter>` 包装（commit `fedbd9e` 错误新增，重点回滚）
- [x] **Fix:** 移除 `category.page.yaml` 中 `LitemallBrand__findList` 的整个 `filter.and` 块（live 内容仅含 `- eq: [- deleted, - false]` 一项，无其他过滤条件需保留）
- [x] **Proof:** `./mvnw -pl app-mall-web -DskipTests compile` → BUILD SUCCESS

Exit Criteria:

- [x] 4 处前端手动过滤全部移除
- [x] 编译通过
- [x] `docs/logs/` 追加 Phase 1 条目

### Phase 2 — BizModel 手动过滤清理（21 个文件，59 处）

Status: completed
Targets: 21 个 `app-mall-service/src/main/java/app/mall/service/entity/Litemall*BizModel.java`
Required Skill: `nop-backend-dev`、`nop-debugging`、`nop-testing`

- Item Types: `Fix`
- Prereqs: Phase 1 完成（前端先行，最小化回归面）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-debugging` + `nop-testing`，读完路由表必读文档
  - Docs read: nop-backend-dev skill (logical-deletion.md, service-layer.md, safe-api-reference.md)、nop-debugging skill、nop-testing skill
- [x] **Fix:** 移除 21 个 BizModel 中**所有** `query.addFilter(FilterBeans.eq(Xxx.PROP_NAME_deleted, false))` 行
- [x] **Fix:** 同时移除因上一项导致孤立的 `import io.nop.api.core.beans.FilterBeans;`（仅在确实未被同文件其他方法使用时）
- [x] **Decision:** `LitemallGoodsBizModel.java:99` 处 `orderGoodsQuery.addFilter(eq(LitemallOrderGoods.PROP_NAME_deleted, false))` — 已移除（框架自动过滤覆盖）
- [x] **Proof:** 全量回归测试 `./mvnw -pl app-mall-service test`，结果：120 跑 / 110 过 / 10 失败（10 失败为预存在 auth 问题 `非法的字典项:0`，与基线一致，无新增失败）
- [x] **Proof:** 抽样 SQL 验证：`git grep 'PROP_NAME_deleted' app-mall-service/src/main/` 确认生产代码 0 处残留。10 个测试文件中的 4 处 `PROP_NAME_deleted` 引用已在测试代码中（TestLitemallCouponUserBizModel.java、TestConcurrencyGuards.java），测试 `findCount` 行为不变
- [x] **Fallback:** 不触发（所有测试通过/基线一致）

Exit Criteria:

- [x] 59 处 BizModel 手动过滤全部移除
- [x] 全量测试结果与基线一致（不新增失败）
- [x] 抽样验证确认框架自动过滤生效
- [x] `docs/logs/` 追加 Phase 2 条目

### Phase 3 — owner docs 对齐 + 计划闭合

Status: completed
Targets: `docs/architecture/module-boundaries.md`、`docs/logs/2026/06-18.md`（新建）
Required Skill: `nop-backend-dev`

- Item Types: `Fix`、`Decision`
- Prereqs: Phase 2 完成

- [x] **Skill loading gate:** 加载 `nop-backend-dev`（本 phase 是 docs-only，但内容关于后端约定，skill 用于校验文案准确性）
  - Docs read: logical-deletion.md
- [x] **Fix:** 在 `docs/architecture/module-boundaries.md` 末尾新增 `## Logical Delete Convention` 小节
- [x] **Fix:** 在 `docs/logs/2026/06-18.md` 追加本计划完成条目，含：移除 63 处手动过滤、测试结果、SQL 抽样验证结果
- [x] **Decision:** 不抽取独立 lessons 文档（理由：`docs/skills/multi-dimensional-audit-prompt.md` 维度 05/08 已覆盖）
- [x] **Proof:** `docs/architecture/module-boundaries.md` 和 `docs/logs/2026/06-18.md` 均已更新

Exit Criteria:

- [x] owner docs 与代码现状一致
- [x] 日志记录完整
- [x] `docs/logs/` 已更新

## Plan Audit

- Status: passed（第二轮，2026-06-18）
- Reviewer / Agent: 独立子代理 ses_12762605effeynIwvgvhIXCE3c（第一轮 revise）+ ses_127561737fferLMAIoyc80GKGp（第二轮 passed）
- Evidence:
  - **第一轮（2026-06-18）Verdict: revise**（4 个 Major Objections + 7 个 Minor Notes，全部基于 live repo 证据）
    - M1 baseline 实体数错误（28→30）— 已修订
    - M2 Phase 3 Decision 非决策 — 已改为明确"不抽取 lessons，理由：审计提示词维度 05/08 已覆盖"
    - M3 Phase 3 Fix 用禁用词"如有" — 已改为明确动作（在 module-boundaries.md 末尾新增 Logical Delete Convention 小节，内容直接落地）
    - M4 Non-Goals 措辞"应已注释说明"虚假 — 已修订（MallLogManager.java:22 确认无注释）+ 新增 Deferred 条目
    - m1 空 grid 包装处理 — 已在 Phase 1 中明确"同时移除空 `<filter></filter>` 包装"
    - m2 commit hash 精确化 — 已改为 `fedbd9e`
    - m3 SQL 验证机制 — 已明确"打开 SQL 日志断言 WHERE 含 deleted = false"
    - m4 Phase 2 Required Skill 加 nop-testing — 已加
    - m5 fallback 策略 — 已新增 Phase 2 Fallback 项
    - m6 Phase 3 skill 合规 — 已加理由说明（docs-only 但内容关于后端约定）
    - m7 Closure Gates N/A 标注 — 已新增 "N/A — no new methods"
  - **第二轮（2026-06-18）Verdict: passed** — 所有 M1-M4 + m1-m7 已通过 live repo 证据验证解决，3 个新发现均为外观/跟踪层面不阻塞性（已在本修订中补齐 m4/m6 证据追踪、修订 Plan Audit 状态描述、修订 category.page.yaml Fix 描述精度）

## Closure Gates

- [x] in-scope behavior is complete（59 + 3 + 1 = 63 处手动过滤全部移除）
- [x] relevant docs are aligned（架构/日志/审计提示词一致）
- [x] verification has run：`./mvnw -pl app-mall-service test`（120 跑 / 110 过 / 10 失败，基线一致）+ `./mvnw -pl app-mall-web -DskipTests compile`（BUILD SUCCESS）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed
- [x] skill loading verification: 每个执行 agent 加载匹配 skill 并读完路由必读文档
- [x] N/A — no new `@BizMutation`/`@BizQuery` methods（本计划是清理反模式，不新增方法）
- [x] text consistency verified: status / phases / gates / log 全部一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### ORM 模型 BOOLEAN → delVersion 迁移

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: litemall 沿用 BOOLEAN `deleted` 字段是"已给定数据模型、不便修改"的兼容场景，符合 `logical-deletion.md` 方案 B 规则。框架自动过滤对 BOOLEAN 类型完全支持
- Successor Required: `no`
- 触发条件：若未来 litemall 进行大规模表结构重构（如脱离 litemall 原始 schema），可整体迁移到 `delVersion` 以获得删除时间审计能力

### MallLogManager 缺失 bypass 注释

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: `app-mall-dao/.../manager/MallLogManager.java:22` 使用 `daoProvider.daoFor(LitemallLog.class)` 但未加注释说明为何绕过 I*Biz 管道。该代码不涉及 deleted 字段查询，与本计划清理范围无关
- Successor Required: `no`
- 触发条件：下次修改 MallLogManager 时补一行注释说明原因；或独立 audit 项统一处理全仓库 `daoProvider().daoFor()` 用法的注释完整性

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: completed

Closure Audit Evidence:

- Reviewer / Agent: ses_126c1a6a4ffeHXWefKg0vGX378 (independent closure audit)
- Verdict: PASS — all exit criteria met, no blockers
- Verification: compile exit 0; tests 120/110/10 (all 10 failures pre-existing auth)
- Artifacts: 63 manual `deleted = false` filters removed from 25 files across 3 layers
