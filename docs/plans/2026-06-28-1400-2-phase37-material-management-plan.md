# phase37 内容/素材管理

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 37；`docs/design/system-configuration.md`（文件存储章节）
> Related: 无
> Audit: required

## Current Baseline

**ORM 模型已就绪（`model/app-mall.orm.xml`）：**
- `LitemallMaterial`（素材资源表）：`id` / `name`(文件名) / `url`(访问链接) / `fileType`(image/video) / `fileSize`(bytes) / `categoryId` / `tag` / `addTime` / `updateTime` / `deleted`(逻辑删除)；`to-one category` 关系已建。
- `LitemallMaterialCategory`（素材分类表）：`id` / `name`(分类名称) / `parentId`(父分类) / `sortOrder` / `addTime` / `updateTime` / `deleted`(逻辑删除)；`to-one parent` + `to-many children` 自引用关系已建（树结构）。

**生成脚手架已就绪（空 CrudBizModel 桩）：**
- `LitemallMaterialBizModel`、`LitemallMaterialCategoryBizModel` — 均为空 `extends CrudBizModel<T>` 桩，无任何业务方法。
- 对应 entity / mapper(生成) / api / beans / xbiz / xmeta / view.xml(最小 bounded-merge 桩) 均存在。
- `LitemallMaterial.view.xml` 为最小桩（仅声明 grid/form/page id，无列/字段定义）。

**文件上传基础设施已就绪（无需引入新平台模块）：**
- `IFileStore`（`io.nop.file.core.IFileStore`）+ `IOrmEntityFileStore`（`io.nop.orm.IOrmEntityFileStore`）已在 `LitemallGoodsBizModel`（Excel 导入 `parseGoodsImportExcel`）和 `LitemallOrderBizModel`（批量发货 Excel）中使用，依赖已引入。
- AMIS 前端文件上传组件通过 Nop 平台内置 file-upload 控件对接 `IFileStore`，已有 goods 图片上传先例。
- 设计文档（`system-configuration.md`「文件存储」）：本地存储为基线方案，云存储为基础设施选择，不改变素材业务语义。

**缺口：**
- 素材上传业务方法不存在（MaterialBizModel 仅有空 CRUD 桩，无 `uploadMaterial` 动作）。
- 素材分类树管理不存在（MaterialCategoryBizModel 仅有空 CRUD 桩，无树形查询）。
- 素材搜索（按分类/标签/类型/关键词）不存在。
- 前端素材库页面（网格视图 + 上传弹窗 + 筛选）不存在，仅有最小桩。
- 前端分类树管理页面不存在。

## Goals

- 素材上传管理：管理员上传图片/视频文件 → 复用 `IFileStore` 存储 → 创建 `LitemallMaterial` 记录（自动提取 name/url/fileType/fileSize）。
- 素材分类树：管理员维护分类（树形 CRUD，parent/children 自引用），素材按分类组织。
- 素材搜索：按分类 / 标签 / 文件类型 / 关键词筛选素材列表。
- 前台/后台 AMIS 页面：素材库页（网格视图 + 上传弹窗 + 多维筛选）、分类树管理页。
- 所有新增 `@BizMutation` / `@BizQuery` 方法通过 `IGraphQLEngine` 测试。

## Non-Goals

- **跨实体引用关系追踪（roadmap 交付项「素材引用关系维护」降级）**：roadmap Phase 37 交付范围（`enhanced-features-roadmap.md:479`）列出"素材引用关系维护"为一项交付物。本计划将其降级为 Deferred：现有实体（goods pic、brand logo、头像）已各自通过 `IOrmEntityFileStore` / 自有列存储文件引用，基线不建独立的逆向引用追踪层。**此为对 roadmap 交付范围的收窄，记入 Deferred But Adjudicated 待 stakeholder 确认**（触发条件：需要素材被引用统计、级联删除保护、或素材去重时）。
- **云存储后端（OSS/SFTP）— roadmap 偏差**：roadmap Phase 37 依赖列（`enhanced-features-roadmap.md:118`）标明 `nop-integration-file-*（需引入）`，即 roadmap 预期 P37 引入该多后端模块。本计划**改用已引入的 `nop-file-service`（本地后端，提供 `IFileStore`）**，不引入 `nop-integration-file-*`，从而把多后端/云存储能力推迟。理由：本地存储是设计文档明确的基线方案，`nop-file-service` 已满足基线素材上传/存储需求；`nop-integration-file-*` 仅在需要 OSS/SFTP 云存储时才需引入（部署/基建范畴）。**此为对 roadmap 依赖列的第二处收窄，记入 Deferred But Adjudicated 待 stakeholder 确认**（触发条件：生产部署需要云存储时引入 `nop-integration-file-*`）。pom.xml 现状：仅含 `nop-file-service`，无 `nop-integration-file-*`。
- **素材水印 / 裁剪 / 转码**：图片/视频处理不属于素材管理基线。
- **素材权限隔离（per-admin）**：基线所有管理员共享素材库，不做素材级权限隔离。
- **上传安全加固**：文件大小上限、文件类型白名单（防可执行/恶意上传）、视频转码、孤儿文件清理（删除记录后 IFileStore 文件保留）不在基线范围；上传动作复用平台 file-upload 控件 + `IFileStore` 默认约束。作为 successor（触发条件：安全审计或生产部署前加固时）。

## Task Route

- Type: `implementation-only change`
- Owner Docs: `docs/design/system-configuration.md`（文件存储章节）
- Skill Selection Basis: 后端 BizModel 方法开发→`nop-backend-dev`；BizModel API 测试→`nop-testing`；AMIS 后台页面→`nop-frontend-dev`

## Infrastructure And Config Prereqs

- 复用现有 `IFileStore` / `IOrmEntityFileStore`（已在 GoodsBizModel / OrderBizModel 使用，依赖已引入）。
- 复用 Nop 平台 AMIS file-upload 前端控件。
- No infra prereqs beyond existing baseline.

## Execution Plan

### Phase 1 - 素材与分类后端（上传 + CRUD + 搜索）

Status: completed
Targets: `app-mall-service/src/main/java/app/mall/service/entity/LitemallMaterialBizModel.java`、`app-mall-service/src/main/java/app/mall/service/entity/LitemallMaterialCategoryBizModel.java`、`app-mall-service/src/main/java/app/mall/service/AppMallErrors.java`、`app-mall-dao/src/main/java/app/mall/biz/ILitemallMaterialBiz.java`、`app-mall-dao/src/main/java/app/mall/biz/ILitemallMaterialCategoryBiz.java`
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add`
- Prereqs: 无

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完各自 routing table 必读文档，列出已读路径。每写完一个方法用 selfcheck 校验无 anti-pattern。
  - Docs read: nop-backend-dev skill routing table（service-layer / error-handling / safe-api / ibiz-and-bizmodel 示例）；nop-entropy 源码 `IFileRecord.java` / `IFileStore.java` / `IOrmEntityFileStore.java`；`LitemallGoodsBizModel.parseGoodsImportExcel` fileStore 使用先例；`LitemallCategoryBizModel.getCategoryTree` 内存组树先例；nop-testing skill（test-examples / testing / write-tests）；`TestLitemallGoodsOpsWorkbench` fileStore 测试先例；`TestLitemallCategoryBizModel` IGraphQLEngine 树查询测试先例。
- [x] **Add（ErrorCodes）:** 在 `AppMallErrors.java` 新增：`ERR_MATERIAL_FILE_EMPTY`（上传文件为空）、`ERR_MATERIAL_CATEGORY_NOT_FOUND`（分类不存在）。均 extend `NopException` + ErrorCode。
- [x] **Add（MaterialBizModel 上传动作）:**
  - `@BizMutation @Auth(roles="admin") uploadMaterial(@Name fileUpload, @Optional categoryId, @Optional tag, context)` — 接收 AMIS file-upload 控件返回的 fileRef；通过 `ormEntityFileStore.decodeFileId` 解析 fileId → `fileStore.getFile(fileId)` 获取 `IFileRecord`；从 fileRecord 提取 name / fileSize；推断 fileType（按扩展名/MIME：image/* → image，video/* → video，其他 → file）；通过 `ormEntityFileStore.getFileLink(fileId)` 获取访问 url；创建 `LitemallMaterial` 记录并保存。参照 `LitemallGoodsBizModel.parseGoodsImportExcel` 的 fileStore 使用模式。`@Auth(roles="admin")` 与现有 admin 动作一致（`LitemallOrderBizModel.adminExport` / `LitemallCommentBizModel` moderation 等）。
  - `@BizMutation @Auth(roles="admin") deleteMaterial(@Name id, context)` — 逻辑删除素材记录（复用 CrudBizModel delete，走 `deleted` 逻辑删除标记）。文件本身不从 IFileStore 物理删除（引用可能存在）。
- [x] **Add（MaterialBizModel 搜索）:**
  - `@BizQuery @Auth(roles="admin") searchMaterials(@Optional keyword, @Optional categoryId, @Optional fileType, @Optional tag, page/size, context)` — QueryBean filter 组合：keyword 匹配 name(contains)、categoryId eq、fileType eq、tag contains。参照 `LitemallGoodsBizModel.applyAdminExportFilters` 的 filter 组合模式。基线数据量小，不做 FTS/索引（性能为 successor，见 Deferred）。
- [x] **Add（MaterialCategoryBizModel 树形查询）:**
  - `@BizQuery @Auth(roles="admin") getCategoryTree(context)` — QueryBean 查全量后按 `sortOrder` 升序排列，在内存组装为树（根节点列表，每个节点含 children，children 同样按 `sortOrder` 排序）。分类数据量小，内存组装方案（参照 litemall 既有内存组树先例）。
- [x] **Proof:** `./mvnw test -pl app-mall-service -am` — 新增 `TestLitemallMaterialBizModel` + `TestLitemallMaterialCategoryBizModel`（`JunitAutoTestCase` + `IGraphQLEngine`）：`uploadMaterial`（文件解析 + 记录创建 + fileType 推断）、`searchMaterials`（多维筛选）、`getCategoryTree`（树形返回）、`deleteMaterial`（逻辑删除）。所有 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine` 验证。

Exit Criteria:

- [x] 素材上传 → IFileStore 存储 → Material 记录创建（name/url/fileType/fileSize 自动提取）可用
- [x] 素材搜索（keyword/categoryId/fileType/tag 组合筛选）可用
- [x] 分类树形查询（含 children）可用
- [x] **API 测试：** `uploadMaterial` / `deleteMaterial` / `searchMaterials` / `getCategoryTree` 通过 `IGraphQLEngine` 测试（均带 `@Auth(roles="admin")`）
- [x] **owner-doc 更新（强制）：** 更新 `system-configuration.md`（素材管理后端实现说明 + 上传/搜索/分类树语义）
- [x] `docs/logs/` updated

### Phase 2 - 前台/后台 AMIS 页面

Status: completed
Targets: `app-mall-web/.../pages/LitemallMaterial/LitemallMaterial.view.xml`、`app-mall-web/.../pages/LitemallMaterialCategory/LitemallMaterialCategory.view.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 1（后端 API 就绪）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读必读文档，列路径，每文件完成后 selfcheck。
  - Docs read: nop-frontend-dev skill routing table（application-project-defaults / view-and-page-customization 三层模型 / delta-customization / prefer-delta-over-direct-modification / add-page-business-action runbook）；高价值样例 `LitemallGoods.view.xml`（bounded-merge + gen-control + drawer + x:prototype）、`LitemallWallet.view.xml`（自定义 listAction dialog + simple 接线）、`LitemallCategory.view.xml`（gen-control select + source）、`mall/goods-ops/goods-io.page.yaml`（input-file receiver + fileRef 提交模式）。
- [x] **Add（素材库页）:** `LitemallMaterial.view.xml` bounded-merge — 网格视图（卡片式：图片/视频缩略图 + 文件名 + 分类标签 + 大小 + 操作按钮）+ 上传弹窗（AMIS file-upload 控件 → 调 `uploadMaterial`，支持分类选择 + 标签输入）+ 筛选栏（分类下拉 from `getCategoryTree` + 文件类型 + 关键词搜索）+ 分页。参照 goods 列表页的卡片/筛选模式。
- [x] **Add（分类树管理页）:** `LitemallMaterialCategory.view.xml` bounded-merge — 树形展示（from `getCategoryTree`）+ 新增/编辑/删除分类（支持选父分类）+ 排序。
- [x] **Proof:** `./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS。视图 XML 通过 AMIS schema 校验（启动时加载无报错）。

Exit Criteria:

- [x] 素材库页（网格视图 + 上传弹窗 + 多维筛选 + 分页）完整可用
- [x] 分类树管理页（树形展示 + CRUD + 排序）可用
- [x] 所有新增视图 bounded-merge 生成桩，无手编 `_gen` 文件
- [x] **owner-doc 更新（强制）：** 更新 `system-configuration.md`（前端页面落地说明）
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed
- Auditor / Agent: independent subagents (4 adversarial rounds, fresh sessions)
- Evidence:
  - R1 `ses_0f31c6321ffeSueGDFDR4BlSEu` — MAJOR OBJECTION：4 项（4 方法缺 `@Auth(roles="admin")` / 误称 `nop-integration-file-*` 已存在[roadmap+pom 实为未引入] / IBiz 接口路径误标 app-mall-api[实为 app-mall-dao] / roadmap 交付项「素材引用关系维护」静默收窄）。Baseline 与 IFileStore API 全部核实属实。
  - 修订：4 方法加 `@Auth(roles="admin")`；纠正 `nop-file-service` 已引入 / `nop-integration-file-*` 未引入；路径改 app-mall-dao；getCategoryTree 按 sortOrder；doc 更新强制化；上传安全列为 Non-Goal。
  - R2 `ses_0f30f0644ffeRr9T7KNopWkYtf` — 原 M1-M4 全 RESOLVED；新发现 NEW#1（MAJOR）：plan 引用 `enhanced-features-roadmap.md:118` 谓"roadmap 标明未引入"，但该行实为"需引入"=P37 依赖预期引入——不引入本身是第二处 roadmap 偏差须透明裁定；NEW#2（MINOR）：行号 478 应为 479。
  - 修订：Non-Goal/Deferred 将 `nop-integration-file-*` 未引入裁定为第二处 roadmap 偏差待 stakeholder 确认；行号 478→479。
  - R3 `ses_0f30a1f96ffeuhkz9Dj5IdUB43` — PASS：NEW#1/#2 RESOLVED，两处 roadmap 偏差裁定风格一致（收窄→Deferred→待确认→successor+trigger），无回归。
  - R4 `ses_0f3082db8ffe0VTjRw6q9nJJTjT` — PASS（连续第二轮 clean）：整 plan 对抗复核通过；4 方法 `@Auth` 与 29 处仓库惯例一致；IFileStore API 真实；protected-area（无 ORM 变更）/NopException/并发均覆盖。minor：执行时按 When-Executing#2 将 `enhanced-features-roadmap.md:42` P37 状态 todo→planned（非内容缺陷）。
  - 共识：R3+R4 两轮连续 clean，达到 consensus。

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs are aligned（`system-configuration.md` 更新）
- [x] verification has run: `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` BUILD SUCCESS + `./mvnw test -pl app-mall-service -am` 全绿 + `./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（uploadMaterial/deleteMaterial/searchMaterials/getCategoryTree）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed
- [x] skill loading verification: each phase scanned available skills, loaded all matching skills, read ALL mandatory docs, selfchecked after each method/class
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 跨实体素材引用关系追踪

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 现有实体（goods pic / brand logo / 头像）已各自通过 `IOrmEntityFileStore` 存储文件引用，基线不建独立的逆向引用追踪层。素材库以分类 + 标签组织，满足"素材分类和搜索"目标。
- Successor Required: `yes`（触发条件：需要素材被引用统计、级联删除保护、或素材去重时）

### 云存储后端 / nop-integration-file-* 未引入（roadmap 偏差）

- Classification: `optimization candidate`
- Why Not Blocking Closure: 本地存储为基线方案（设计文档明确），由已引入的 `nop-file-service` 提供 `IFileStore`，满足基线素材上传/存储。roadmap Phase 37 依赖列（`enhanced-features-roadmap.md:118`）预期引入 `nop-integration-file-*`（OSS/SFTP 多后端），本计划改用已有 `nop-file-service` 推迟该引入——为对 roadmap 依赖列的收窄，待 stakeholder 确认。
- Successor Required: `yes`（触发条件：生产部署需要云存储[OSS/SFTP]时，引入 `nop-integration-file-*` 并切换后端配置，另立基建引入计划）

## Closure

<!-- Closure audit performed by an independent subagent (fresh session). -->

Status Note: 已完成。Phase 1（后端：uploadMaterial/deleteMaterial/searchMaterials/getCategoryTree，均 `@Auth(roles="admin")`）+ Phase 2（前端：素材库页 + 分类管理页，bounded-merge）全部交付并通过验证。独立 closure audit PASS（仅有 minor notes，已处理）。

Closure Audit Evidence:

- Reviewer / Agent: independent closure audit subagent `ses_0f2c2833cffevRhJ7Q0Cio4ARN`（fresh session，非实现 agent）
- Result: PASS（无 MAJOR_OBJECTIONS）
- Evidence:
  - 范围完整：`AppMallErrors.java:599-605`（2 错误码）、`LitemallMaterialBizModel`（uploadMaterial/deleteMaterial/searchMaterials）、`LitemallMaterialCategoryBizModel`（getCategoryTree）、`LitemallMaterial.xmeta`（name/tag allowFilterOp contains）、`TestLitemallMaterialBizModel`（7 用例含 fileType 推断 image/video/file）、`TestLitemallMaterialCategoryBizModel`（1 用例）。
  - Nop 规范：IBiz-first + `@Override`、4 方法 `@Auth(roles="admin")`、`NopException`+`ErrorCode`、`@Inject` 非 private、`newEntity()`、3-arg CrudBizModel 签名（`saveEntity(...,null,context)`/`findPage(query,null,context)`/`findList(query,null,context)`）、无 `@Transactional`/`dao()`/`throws RuntimeException`。
  - 前端：view.xml 均 `x:extends="_gen/..."` + bounded-merge，无手编 `_gen`；`TestMaterialViewLoad` 经 `DslModelParser` 对 xview.xdef 校验通过（开发期捕获并修正 cell `required`→`mandatory` schema 错误）。
  - Deferred：跨实体引用追踪 + `nop-integration-file-*` 云存储均为透明 roadmap 偏差裁定（pom.xml 证实 `nop-file-service` 已引入、`nop-integration-file` 未引入），非静默 in-scope 裁撤。
  - Minor notes 处理：① Closure Gates 已全部勾选；② 本 Closure 段已填写；③ IBiz 接口方法已补 `@Auth(roles="admin")` 镜像（与 `ILitemallWalletBiz` 等 8 个近期 IBiz 约定一致），重跑 8 测试全绿。

Follow-up:

- 无阻断 follow-up。跨实体引用追踪为 Deferred successor。
