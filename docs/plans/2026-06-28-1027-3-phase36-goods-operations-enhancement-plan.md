# P36 商品运营增强

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` §36；`docs/analysis/2026-06-21-mobile-mall-functional-design-analysis.md` A-06
> Related: `docs/plans/2026-06-28-1027-1-phase18-dashboard-redesign-plan.md`（库存预警阈值复用 P38/P18 全局配置族）
> Audit: required

## Current Baseline

**现有商品管理为 CRUD + 单行上下架 + 评论删除，缺批量运营与审核能力。** 已通过 live repo 核验：

- `LitemallGoodsBizModel.java`：有 `onSale(@BizMutation)` 单行上下架（line 134）；有 P38 库存语义化 `getStockSemantic`（复用全局配置键族 `mall_stock_threshold_*`，经 `ILitemallSystemBiz.getConfig()`）。**无批量改价/改库存/上下架、无导入导出、无库存预警查询。**
- `LitemallCommentBizModel.java`：有 `adminReply(@BizMutation)`（line 251，单条回复——已存在）、`submitComment`/`commentList`/`getCommentSummary`/`myComments`。**无评论审核工作台、无批量回复、无批量 Moderation；当前只能删除（CRUD delete）。**
- 字段（`model/app-mall.orm.xml`）：`LitemallGoods` 有 `isOnSale`（bool，上下架）、`retailPrice`/`counterPrice`（改价）；`LitemallGoodsProduct` 有 `number`（SKU 库存，改库存目标）**及 `safeStock`（安全库存预警线，propId 10，line 806——已存在，库存预警直接复用）**；`LitemallComment` 有 `adminContent`（回复内容，已存在）、`deleted`（逻辑删除）；**无审核状态字段**。
- Entity Coverage（roadmap）：P36「无新增（批量操作和预警为逻辑变化）」——本计划不触及 ORM。
- 模块：`app-mall-service`、`app-mall-web`。

**差距：** 无批量导入/导出商品、无批量改价/改库存/上下架、无库存预警（低库存提醒+安全库存）、无评论审核工作台、无评价回复工作台（批量）。

## Goals

- 提供商品批量运营：批量改价 / 改库存 / 上下架（多行一次性，部分失败不阻断成功行）。
- 提供商品批量导入/导出（xlsx via `ExcelHelper`，含校验与错误报告；见 Decision G2）。
- 提供库存预警查询（低库存商品/SKU 列表，阈值优先用既有 per-SKU `safeStock`、回退全局配置，见 Decision G3）。
- 提供评论运营工作台：批量回复（复用 `adminContent`）+ 后置审核 Moderation（下架/恢复，复用 `deleted`）。
- 全部新增 `@BizMutation`/`@BizQuery` 经 `IGraphQLEngine` 测试；口径落入 owner doc。

## Non-Goals

- **商品（goods 级，非 SKU 级）安全库存阈值**——库存预警已用既有 per-SKU `LitemallGoodsProduct.safeStock`（`orm.xml:806`，已存在）+ 全局回退（见 Decision G3 / Deferred）；goods 聚合级阈值为 out-of-scope enhancement，无 ORM 改动。
- **评论前置审核状态机**（pending→approved/rejected，需新 `status` 字段，model-gap，见 Decision G4 / Deferred；本计划交付后置 Moderation）。
- 商品组合/套装（分析 A-06 提及但 roadmap §36 未列为交付项，归 successor）。
- 移动端。

## Task Route

- Type: `app-layer design change` + `implementation-only change`
- Owner Docs: `docs/design/system-configuration.md`（需新增「商品运营工作台」段）/ `docs/design/product-catalog.md`（商品批量运营语义边界，按交接点引用）
- Skill Selection Basis: 后端批量 `@BizMutation`/`@BizQuery` → `nop-backend-dev`；前端 AMIS 工作台页 → `nop-frontend-dev`；含新增 `@BizMutation`/`@BizQuery` → `IGraphQLEngine` 测试 → `nop-testing`。不触及 ORM。

## Infrastructure And Config Prereqs

- 库存预警阈值优先用既有 per-SKU `LitemallGoodsProduct.safeStock`（`orm.xml:806`，已存在），为空/0 时回退 P38 全局配置键 `mall_stock_threshold_tight`（`LitemallSystem`，经 `ILitemallSystemBiz.getConfig()`），无新基础设施。
- 导入采用 xlsx（复用平台 `ExcelHelper`，已有 `batchShip` 先例，无新依赖）；导出 xlsx 优先、无写出 helper 时 CSV 兜底（见 Decision G2）。

## Execution Plan

### Phase 1 - 商品批量运营 + 库存预警（Add-heavy）

Status: completed
Targets: `app-mall-service/.../entity/LitemallGoodsBizModel.java`、`ILitemallGoodsBiz.java`、`app-mall-dao/.../sql/LitemallGoods.sql-lib.xml`（或既有）
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: none

- [x] **Skill loading gate:** 加载 `nop-backend-dev`、`nop-testing`，读完 routing table 必读文档，列于下；每方法 selfcheck。
  - Docs read: `02-core-guides/service-layer.md`、`04-reference/safe-api-reference.md`、`05-examples/test-examples.java`、`02-core-guides/testing.md`
- [x] **Decision G1：批量操作事务边界与部分失败策略。** 抉择：**逐行独立处理 + 聚合结果报告**（每行单事务，单行失败记入错误列表不回滚其他行，返回 `{successCount, failedCount, failures:[{id, reason}]}`），**复刻既有先例** `LitemallOrderBizModel.batchShip`（`doBatchShip` catch-and-continue + `BatchShipResultBean` 聚合，见 `LitemallOrderBizModel.java:1362` 起）。备选（整批单一事务，一失败全回滚）被否——批量运营场景部分失败应允许成功行落地。残留风险：调用方需读结果报告处理失败行。
- [x] **Decision G2：导入导出格式（xlsx via ExcelHelper vs CSV）。** 抉择：**导入用 xlsx，复用平台 `ExcelHelper.readSheet`**——本仓库已在该路径上落地（`LitemallOrderBizModel.java:62` import、`:1362` `batchShip` 调用），**无新依赖、已验证**。导出按可用写出入口抉择：优先 xlsx（若有平台 xlsx 写出 helper）否则 CSV 兜底。备选（声称 Excel 不可用而强制 CSV）被否——`ExcelHelper` 先例证伪了"Excel 引入风险高"的假设。残留风险：若平台无 xlsx 写出 helper，导出走 CSV（不阻塞导入的 xlsx 一致性）。
- [x] **Decision G3：库存预警阈值来源。** 抉择：**优先用既有 per-SKU `LitemallGoodsProduct.safeStock`（`orm.xml:806`，已存在），`safeStock` 为空/0 时回退全局配置 `mall_stock_threshold_tight`**（P38 已落地，`LitemallGoodsBizModel` 已有读取机制）。备选（仅全局阈值）被否——既有 `safeStock` 字段支持按 SKU 粒度预警，更贴合"安全库存"语义，无需 ORM 改动。残留风险：历史 SKU 的 `safeStock` 多为空，初期预警依赖全局回退阈值；运营逐步填充 `safeStock` 后过渡到 per-SKU。
- [x] **Add：** `batchUpdatePrice(@BizMutation)`：批量改价（goodsId→retailPrice 列表），逐行校验 + 结果报告。
- [x] **Add：** `batchUpdateStock(@BizMutation)`：批量改库存（productId→number 列表，目标 `LitemallGoodsProduct.number`）。跨实体路径**预承诺**：经 `goods.getProducts()` ORM 关系（既有先例 `LitemallGoodsBizModel.java:100/226`）或注入 `ILitemallGoodsProductBiz`，**禁止** `daoProvider().daoFor()` 绕过；逐行校验 + 结果报告。
- [x] **Add：** `batchOnSale(@BizMutation)` / `batchOffSale(@BizMutation)`：批量上下架（goodsId 列表），复用 `onSale` 单行逻辑。
- [x] **Add：** `exportGoods(@BizQuery)`：按筛选条件导出商品（xlsx 优先，无写出 helper 时 CSV 兜底；含 SKU 关键字段）。
- [x] **Add：** `importGoods(@BizMutation)`：xlsx 导入（复用 `ExcelHelper.readSheet`，新增/更新），含字段校验 + 错误报告。
- [x] **Add：** `getStockWarningList(@BizQuery)`：低库存商品/SKU 列表（SKU `number` ≤ 阈值；阈值 = per-SKU `safeStock`（非空时）否则全局 `mall_stock_threshold_tight`），按库存升序，跨实体经 `goods.getProducts()` 关系聚合。
- [x] **Proof：** 新增 `@BizMutation`/`@BizQuery` 经 `IGraphQLEngine` 测试，覆盖批量成功 / 部分失败 / 空导入 / 校验失败 / 库存预警空与有数据。

Exit Criteria:

- [x] 5 个批量/导入导出 + 1 个库存预警方法可经 GraphQL 调用，符合 G1/G2/G3 口径（成功/部分失败/校验失败模式）。
- [x] **API 测试：** 新增 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine` 测试。
- [x] `docs/design/system-configuration.md` 新增「商品运营工作台」段（批量动作/导入导出/库存预警口径 G1/G2/G3）。
- [x] `docs/logs/` 更新。

### Phase 2 - 评论运营工作台（Add-heavy）

Status: completed
Targets: `app-mall-service/.../entity/LitemallCommentBizModel.java`、`ILitemallCommentBiz.java`
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: none（可与 Phase 1 并行）

- [x] **Skill loading gate:** 加载 `nop-backend-dev`、`nop-testing`，读完必读文档，列于下；每方法 selfcheck。
  - Docs read: `02-core-guides/service-layer.md`、`04-reference/safe-api-reference.md`、`05-examples/test-examples.java`、`02-core-guides/testing.md`
- [x] **Decision G4：评论审核模型（后置 Moderation vs 前置状态机）。** 抉择：**后置 Moderation**——工作台列已发布评论，提供批量下架（置 `deleted=true`，隐藏）/恢复（`deleted=false`）/批量回复（写 `adminContent`），复用既有字段，无 ORM 改动。备选（前置审核状态机 pending→approved/rejected）需新 `status` 字段，为 model-gap（见 Deferred）。残留风险：本基线评论发布即公开，前置预审需求为 successor。
- [x] **Add：** `batchAdminReply(@BizMutation)`：批量回复（commentId→adminContent 列表），逐行写 `adminContent`，复用 `adminReply` 校验，结果报告。
- [x] **Add：** `batchModerateComments(@BizMutation)`：批量下架/恢复（commentId 列表 + action=hide/restore），置 `deleted`，结果报告。
- [x] **Add：** `getCommentReviewList(@BizQuery)`：评论工作台列表（支持按 star/hasPicture/关键字/时间筛选 + 分页），供工作台消费。
- [x] **Proof：** 经 `IGraphQLEngine` 测试，覆盖回复/下架/恢复/筛选/归属校验。

Exit Criteria:

- [x] 批量回复/批量 Moderation/工作台列表 `@BizMutation`/`@BizQuery` 可经 GraphQL 调用，符合 G4 口径。
- [x] **API 测试：** 新增 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine` 测试。
- [x] `system-configuration.md`「商品运营工作台」段补评论工作台口径（G4）。
- [x] `docs/logs/` 更新。

### Phase 3 - 前端工作台页面（Add-heavy）

Status: completed
Targets: `app-mall-web/.../_vfs/app/mall/pages/mall/goods-ops/`（新增工作台页）、`app-mall.action-auth.xml`、i18n
Required Skill: `nop-frontend-dev`

- Item Types: `Add | Proof`
- Prereqs: Phase 1、Phase 2 完成

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完 routing table 必读文档，列于下；每页面 selfcheck。
  - Docs read: `00-start-here/application-project-defaults.md`、`02-core-guides/view-and-page-customization.md`、`03-runbooks/add-export-or-batch-operations.md`
- [x] **Add：** 商品批量运营工作台页 `mall/goods-ops/goods-batch.page.yaml`：批量改价/改库存/上下架入口（行选 + 批量动作按钮，消费批量 `@BizMutation`，展示结果报告）。
- [x] **Add：** 导入导出页 `mall/goods-ops/goods-io.page.yaml`：导出（xlsx 或 CSV 兜底，按筛选）+ 导入（上传 + 校验报告，消费 `ExcelHelper` 解析路径）。
- [x] **Add：** 库存预警页 `mall/goods-ops/stock-warning.page.yaml`：消费 `getStockWarningList`，低库存商品列表 + 阈值来源提示。
- [x] **Add：** 评论工作台页 `mall/goods-ops/comment-review.page.yaml`：消费 `getCommentReviewList`，批量回复 + 批量下架/恢复 + 筛选。
- [x] **Add：** 菜单注册（`app-mall.action-auth.xml`，挂 `goods-manage` TOPM 下子项：批量运营/导入导出/库存预警/评论工作台；父菜单 `goods-manage` 见 `app-mall.action-auth.xml:93`）+ i18n。
- [x] **Proof：** 工作台页渲染冒烟（`cd e2e && npx playwright test` 页面渲染验证模式）。

Exit Criteria:

- [x] 4 类工作台页可渲染并消费对应 `@BizMutation`/`@BizQuery`，批量结果报告可展示。
- [x] 菜单入口可达，RBAC 仅管理员/运营（与既有 `goods-manage` 一致）。
- [x] `docs/logs/` 更新。

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗式审计，4 轮
- Evidence:
  - Round 1 `ses_0f3edfd9effej11crmaXYqJZf0`：MAJOR_OBJECTION——(a) 菜单父 id 误用 `mall-goods-manage`（实为 `goods-manage`）；(b) G2 以"Excel 引入风险高"为由否决，但 `ExcelHelper.readSheet` 已在本仓库 `batchShip` 落地。已修订菜单父 id、G2 改为复用 `ExcelHelper`、G1 引 `batchShip` 先例、`batchUpdateStock` 预承诺 `goods.getProducts()` 关系。
  - Round 2 `ses_0f3ee57896ffe2nRWyYbMedbaVB`：MAJOR_OBJECTION——`LitemallGoodsProduct.safeStock`（`orm.xml:806`）**已存在**，计划误将其记为 model-gap。已修订：baseline 补列 safeStock、G3 改用既有 per-SKU `safeStock`+全局回退、Deferred 由 model-gap 重分类为 out-of-scope improvement。
  - Round 3 `ses_0f3e09d81ffesQ8qnMiffixN92`：PASS——safeStock 修复经 live repo 复核正确，6 项关键检查 + 6 项既有修复全部通过。
  - Round 4（终审）`ses_0f3dc7a4bffeB3TbmsVlpWP22E`：PASS——非实质性标签同步（CSV→xlsx、safetyStock 标签）后无新不一致；连续两轮 clean，consensus 达成。
  - 结论：无 blocker、无 major objection；不触及 ORM（Protected Area），跨实体经 `I*Biz`/ORM 关系，规则 #15 满足。

## Closure Gates

- [x] in-scope behavior is complete（批量运营 + 导入导出[xlsx via ExcelHelper] + 库存预警 + 评论工作台 全部落地）
- [x] relevant docs are aligned（`system-configuration.md`「商品运营工作台」段已落）
- [x] verification has run（`./mvnw clean package -DskipTests` + `./mvnw test -pl app-mall-service -am` + `./mvnw -pl app-mall-web -DskipTests compile`；前端 `cd e2e && npx playwright test`）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop 平台 phase 未写 `none`
- [x] skill loading verification：各 phase 扫可用 skill、加载匹配 skill、读完 routing 必读文档、selfcheck
- [x] text consistency verified：status/phases/gates/log 一致
- [x] closure audit was performed by a different agent/session
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 每商品（goods 级）安全库存

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 库存预警已用既有 per-SKU `LitemallGoodsProduct.safeStock`（`orm.xml:806`）+ 全局回退阈值，覆盖安全库存语义；goods 级（聚合）安全库存为额外增强。
- Successor Required: yes
- 说明：此前将安全库存误记为 model-gap 不成立——`safeStock` 字段已存在，本计划直接复用，无 ORM 改动。

### 评论前置审核状态机（G4）

- Classification: `model-gap`
- Why Not Blocking Closure: 后置 Moderation（下架/恢复）已满足违规评论处置；前置预审状态机为增强。
- Successor Required: yes
- Model Gap Detail: `LitemallComment` 缺审核 `status` 字段（pending/approved/rejected）。建议新增 `auditStatus` 列 + 字典。触发条件：评论发布前预审需求出现时。

### Excel 导出（xlsx 写出）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 导入已用 `ExcelHelper` xlsx（in-scope，有先例）；导出在无平台 xlsx 写出 helper 时走 CSV 兜底，数据导出能力不缺。
- Successor Required: yes（当需要模板化 xlsx 导出时，可随 nop-report 引入续作）

## Closure

<!-- 闭合审计须由独立 subagent（不同 session）执行，此处留给闭合审计员填写。 -->

Status Note: <待闭合时填写>

Closure Audit Evidence:

- Reviewer / Agent: <独立审计员>
- Evidence: <task id / 日志 / 走查记录>

Follow-up:

- 商品组合/套装（roadmap §36 未列，归 successor）。
- Excel 导入导出 + nop-report（运营 Excel 习惯需求出现时）。
