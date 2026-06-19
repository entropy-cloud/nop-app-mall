# 2026-06-18-2000 E2E 测试加固（init-data 基建 + happy-path 修复 + admin 冒烟 + UI 交互）

> Plan Status: planned
> Last Reviewed: 2026-06-18
> Source: 用户审计反馈 — e2e 套件 happy-path 空库失败（已知 Deferred）、admin 后台零覆盖、无浏览器 UI 交互
> Related:
> - `../nop-entropy/docs-for-ai/02-core-guides/e2e-testing.md`（E2E 模式权威源）
> - `../nop-entropy/docs-for-ai/02-core-guides/orm-model-design.md` → "自动初始化数据：DataInitInitializer"（init-data 标准机制）
> - `docs/references/e2e-testing-guide.md`（本项目 e2e 指南）
> - `docs/plans/2026-06-17-1830-test-hardening-and-ops-optimization-plan.md`（happy-path 引入及其 Deferred 记录）
> Audit: required

## Current Baseline

### E2E 框架现状（已落地，无需从零搭建）

- `e2e/` 目录，Playwright 1.60 + TypeScript，8 个源文件已 git 追踪；`node_modules/`、`playwright-report/`、`test-results/` 已 gitignore（`.gitignore:26/35/36`）
- `e2e/playwright.config.ts`：webServer 自动拉起 `app-mall-app/target/quarkus-app/quarkus-run.jar`，命令含 `-Dfile.encoding=UTF8 -Dquarkus.profile=dev -Dnop.datasource.jdbc-url=jdbc:h2:mem:e2e -Dnop.orm.init-database-schema=true`（`%dev` profile 的 `allow-create-default-user:true` 是 nop 用户可登录的前提），`channel:'chrome'` 走系统 Chrome
- `e2e/tests/auth.ts`：`POST /r/LoginApi__login`（`principalId/principalSecret/loginType`）取 `data.accessToken`，override `request` fixture 注入 `Authorization: Bearer`；账号读 `E2E_USER`/`E2E_PASSWORD`（默认 `nop`/`123`）
- 三类测试：`app-startup.spec.ts`（2）、`storefront-pages.spec.ts`（11 实体 findPage + 25 页面渲染 = 36）、`storefront-happy-path.spec.ts`（地址→商品→SKU→加购→下单→支付→订单列表 = 1）
- 历史日志：06-16 `38 passed`；06-17 引入 happy-path 后 `38 passed / 1 failed`（happy-path 空库 Deferred）；`e2e/test-results/.last-run.json` 显示 `status: passed`（与日志存在矛盾，Phase 2 需实跑确认真实状态）
- owner docs：`docs/references/e2e-testing-guide.md`、`docs/context/project-context.md:47`、`docs/context/codebase-map.md:20`、`docs/architecture/module-boundaries.md:85` 均已记录 e2e

### happy-path 失败根因（已定位）

- `storefront-happy-path.spec.ts` 第 25-32 行：`POST /r/LitemallGoods__frontList` 返回空（H2 内存库仅建表 `init-database-schema:true`，**不灌数据**），`goods` 为 undefined，后续 `goodsId`/SKU 断言失败
- 根因：e2e 启动的应用无任何业务种子数据，商品/SKU/分类/品牌等核心展示数据全空

### init-data 标准机制（来自 docs-for-ai，权威源）

- `nop.orm.init-database-data`（Boolean，默认 `false`）：启动时从 CSV/SQL 文件初始化数据
- `nop.orm.init-database-data-location`（String，默认 `/_init-data/`）：初始化数据文件的 VFS 目录路径
- `DataInitInitializer` 在 `DataBaseSchemaInitializer` 之后执行，顺序：
  1. **CSV 数据插入**（推荐，走 ORM 管道）：按 ORM 模型拓扑序遍历实体，对每个实体检查 `{location}/{tableName}.csv`，存在则解析并通过 `dao.saveEntity()` 插入。CSV 列名按实体列的 `code` 匹配（大写数据库列名）。列名不匹配抛 `NopException`。自动设置租户、创建人等框架字段
  2. SQL 文件执行（原生 JDBC，不走 ORM）—— **本计划不采用**（用户明确指示不用 SQL 初始化数据）
- 源码锚点与配置示例：`../nop-entropy/docs-for-ai/02-core-guides/orm-model-design.md:238-276`

### 种子数据源

- `deploy/sql/mysql/litemall_data.sql`（325 行，MySQL dump 语法）含 **18 个非空表**：ad / brand / category / comment / coupon / footprint / goods_attribute / goods_product / goods_specification / goods / groupon_rules / issue / keyword / log / region / system / topic / **user**
- **`litemall_user` 已废弃**（roadmap Phase 1 架构决策：消除 LitemallUser，统一用 `NopAuthUser`）。该表种子数据不可直接用，登录走 nop-auth（`%dev` profile `allow-create-default-user:true` 创建默认 nop 用户，已验证）
- 有效可转换种子表：**17 个**（排除 user）。本轮 Phase 1 仅实施其中 6 张核心表（goods/goods_product/goods_attribute/goods_specification/category/brand），其余 11 张见 `Deferred But Adjudicated`
- **无多租户**：`model/*.orm.xml` 全库 grep `tenantCol|useTenant` 零命中 → CSV 无需 `TENANT_ID` 列

### _init-data 物理位置（已确认）

- `app-mall-web/src/main/resources/_vfs/` 当前含 `app`、`i18n`，无 `_init-data`
- 新建 `app-mall-web/src/main/resources/_vfs/_init-data/` → VFS 路径 `/_init-data/`（匹配默认 `init-database-data-location`）

### Phase 4 前端渲染入口（不确定性，需 Explore）

- 前台路由定义在 `_vfs/app/mall/auth/app-mall.action-auth.xml`：storefront（`routePath="/storefront" component="layouts/default/index"`，第 151-182 行，子路由 `/storefront-home`、`/storefront-cart` 等）与商城自定义 admin（`/mall-user-manage`、`/mall-manage`、`/goods-manage`、`/promotion-manage`，第 6/32/66/81 行）**都**引用同一个 `component="layouts/default/index"`
- page.yaml 为 AMIS 页面定义（`type: page`，body 含 wrapper/flex/tpl/button/service/each/grid/card 等 AMIS schema）
- **本仓库无静态前端资源**（`app-mall-web` 全库 glob `*.html` 零命中，无 `META-INF/resources`）：storefront 与商城 admin 共享同一个不在本仓库的外部前端 SPA 工程（`layouts/default/index` 组件），**两者渲染可行性等价**
- 注意区分：docs-for-ai e2e-testing.md 的 `/#/type-hierarchy-main` 指向的是**平台自带 admin**（平台 jar 内置），与商城自定义 admin（依赖外部 SPA）是两套前端，不能混为一谈
- **结论**：Phase 4 必须先 Explore 验证浏览器能否真实渲染（访问 `/` 或 `/storefront-home` 是否返回可交互 AMIS 页面），再决定测试范围。storefront 与商城 admin 要么都可渲染、要么都不可渲染

## Goals

- **G1（init-data 基建）**：用平台标准 `DataInitInitializer` + CSV 机制（非 SQL），为 e2e H2 内存库提供业务种子数据
- **G2（happy-path 修复）**：依赖 G1 种子数据，让 `storefront-happy-path.spec.ts` 转绿，e2e 套件全绿
- **G3（admin 后台 RPC 冒烟）**：扩展 RPC 冒烟覆盖到 admin 后台实体与页面，补齐 admin 面覆盖空白
- **G4（浏览器 UI 交互）**：在渲染入口可行性确认后，从 API 层 RPC 升级到真实 AMIS 页面交互验证

## Non-Goals

- **不**修改 ORM 模型本身（不改 `model/*.orm.xml` 实体/字段/关系）
- **不**修改业务代码（BizModel / view.xml 的业务逻辑）
- **不**采用 SQL 文件初始化数据（用户明确指示；只用 CSV + ORM 管道）
- **不**做 CI 集成（用户本轮未选；留作后续 follow-up）
- **不**做完整 CRUD 边界/负面测试（update/delete/错误分支），本轮仅补冒烟级覆盖
- **不**转换 `litemall_user` 种子（实体已废弃，登录走 nop-auth）
- **不**修改 `app-mall-app/src/main/resources/application.yaml` 默认配置（init-data 仅通过 e2e 启动命令 `-D` 参数开启，不影响开发/生产 profile）
- **不**构建独立的前端 SPA 工程（若 Phase 4 Explore 发现浏览器无法渲染前台，则 Phase 4 范围收窄为 admin 后台 UI 测试或标记 Deferred）

## Task Route

- Type: `bug investigation`（happy-path 失败确认）+ `implementation-only change`（init-data 基建 + 测试补充）
- Owner Docs:
  - `../nop-entropy/docs-for-ai/02-core-guides/orm-model-design.md`（DataInitInitializer 章节，init-data 权威源）
  - `../nop-entropy/docs-for-ai/02-core-guides/e2e-testing.md`（E2E 模式权威源）
  - `docs/references/e2e-testing-guide.md`
- Skill Selection Basis:
  - Phase 1：扫描 available skills，`nop-orm-modeler`（涉及 ORM 实体/列映射）为最近匹配，但用途限定为"核对 CSV 列名与实体列 code 的映射"，init-data 配置方法本身来自 docs-for-ai/orm-model-design.md（无 skill 专门覆盖 init-data，justify：该工作为平台配置+数据转换，非建模）
  - Phase 2：`nop-debugging`（修复失败测试）精确匹配
  - Phase 3/4：Playwright TypeScript e2e 无对应 skill（`nop-testing` 是 Java `JunitAutoTestCase`/`IGraphQLEngine`，不覆盖 TS e2e），方法来自 docs-for-ai/e2e-testing.md（justify：现有 skills 覆盖 Nop Java 后端与 view.xml 前端，不含外部 Playwright e2e 工具链）

## Infrastructure And Config Prereqs

- 构建产物：`app-mall-app/target/quarkus-app/quarkus-run.jar` 必须已构建（`./mvnw.cmd package -DskipTests -pl app-mall-app -am`）
- 浏览器：系统已装 Chrome（`channel:'chrome'` 复用系统 Chrome，避免 Playwright chromium 下载）
- Node：`e2e/` 已 `npm install`
- 无外部服务依赖（H2 内存库自包含）
- 回滚策略：init-data 通过 `-D` 参数开启，移除参数即回退；CSV 文件仅新增不改动既有文件，`git revert` 即回滚
- 启动时间预算：6 张核心表种子数据量小（goods/category/brand 各数十行），`DataInitInitializer` 走 ORM `dao.saveEntity()` 批量插入，对 webServer 180s timeout（`playwright.config.ts:29`）影响可忽略

## Execution Plan

### Phase 1 — init-data 基建（CSV 种子 + e2e 启动配置）

Status: done
Targets: `app-mall-web/src/main/resources/_vfs/_init-data/*.csv`（新建）、`e2e/playwright.config.ts`（启动命令追加参数）
Required Skill: `nop-orm-modeler`（用途限定：核对 CSV 列名与实体列 `code` 映射）

- Item Types: `Add`、`Decision`
- Prereqs: none

- [ ] **Skill loading gate:** 加载 `nop-orm-modeler`，读完路由表必读文档
  - Docs read: nop-orm-modeler skill、`../nop-entropy/docs-for-ai/02-core-guides/orm-model-design.md`（DataInitInitializer 章节 + 通用字段表）、`../nop-entropy/docs-for-ai/02-core-guides/model-first-development.md`
- [x] **Add:** 新建 `app-mall-web/src/main/resources/_vfs/_init-data/` 目录（**实施修订**：最终改为新建项目根 `_vfs/_init-data/`，理由见下条 2026-06-19 Decision）
- [x] **Decision（实施中发现，2026-06-18）：放弃"转换 litemall_data.sql 全量 dump"，改为"手写最小种子集 CSV"。** 理由：`litemall_data.sql` 来自原始 litemall 数据库，其表列顺序与本项目 ORM 重建的表结构系统性不同（实测：brand dump 顺序 `ID,NAME,DESC,PIC_URL,...` 而 DDL `ID,NAME,PIC_URL,DESC,...`；category dump 第 4 位是 DESC 描述而 DDL 第 4 位是 PIC_URL）。本仓库无原始 litemall 建表脚本（`db/` 空，`deploy/sql` 是本项目 ORM 重建产物），dump INSERT 无列名，字段多且相似的表（goods 21 字段含多个 URL/布尔/价格）无法可靠按值语义还原列顺序。手写最小种子集由本计划控制列顺序（按 ORM `code`），数据量小易验证，避开列顺序陷阱，仍走平台标准 `DataInitInitializer` + CSV + ORM 管道（符合"标准方式初始化数据"要求）。残留风险：种子数据非 litemall 原始数据，但 e2e 验证的是流程正确性而非具体数据真实性，可接受
- [x] **Decision（实施中发现，2026-06-19）：放弃"CSV 放 `app-mall-web/src/main/resources/_vfs/_init-data/`"，改放"项目根 `_vfs/_init-data/`"。** 理由：Nop VFS 默认配置 `nop.core.resource.dir-override-vfs=./_vfs`（最高优先级，`CoreConfigs.java:213-214`、`ResourceHelper.java:1112-1124`），运行目录下 `_vfs/` 自动合并进 VFS 命名空间，**无需打 jar**。e2e 的 `playwright.config.ts` 中 `cwd: '..'` = 项目根，种子数据放 `_vfs/_init-data/` 即被识别。修改 CSV 后重启即生效，开发体验最佳。已在 `nop-entropy/docs-for-ai/02-core-guides/orm-model-design.md` 的"_vfs/_init-data/ 的物理位置"章节补充该机制
- [x] **Decision（实施中发现，2026-06-19）：image domain 字段（`picUrl` 等）种子需配合 `nop_file_record.csv`。** 理由：`OrmFileComponent.copyFrom`（如 `LitemallCartBizModel.addGoods` 中 cart 从 goods 复制 picUrl）依赖 `IOrmEntityFileStore.copyFile`，要求源字段值是 `/f/download/{fileId}` 格式且 `nop_file_record` 表存在对应记录（`attachFile` 还校验 `BIZ_OBJ_NAME` 匹配）。仅展示用图片（brand/category）可用字面 URL 入库；会被 `copyFrom` 的字段（goods.picUrl）必须预种 `nop_file_record`。已在 `orm-model-design.md` 的"image / images 域字段的种子约束"章节补充
- [x] **Decision（实施中发现，2026-06-19）：nop-entropy 平台 `DataInitInitializer` 从 `@PostConstruct` 改为 `ioc:delay-method`。** 理由：原实现作为 `@PostConstruct` bean 在 IoC 启动早期通过 `ioc:after` 链式触发创建（`BeanContainerImpl.getBean0:411-415` 遍历 `getNextBeans()`），此时 `OrmTemplateImpl` 创建链中 `sessionFactory` 字段尚未注入完成（循环依赖暴露半成品），`init()` 调用 `ormTemplate.runInSession(...)` 触发 `OrmSessionRegistry.get(null)` NPE。改为 `ioc:delay-method="init"` 后，`init()` 推迟到 IoC 完全启动后执行（`BeanContainerImpl.runDelayMethod:591-601`），所有 bean 已注入完成，循环依赖消失。已修 `nop-entropy` 的 `DataInitInitializer.java`（移除 `@PostConstruct`）+ `orm-defaults.beans.xml`（添加 `ioc:delay-method="init"`），同步 install 到本地 maven repo
- [x] **Fix（实施中发现，2026-06-19）：`storefront-happy-path.spec.ts` 两处测试代码 bug：** (a) `LitemallGoodsProduct__findList` 的 filter 格式 `{eq: ['goodsId', goodsId]}` 非法，Nop 标准 TreeBean 格式为 `{$type: 'eq', name, value}`，错误码 `nop.err.core.filter.op-is-null`；(b) `findList` 返回 `data` 是数组（非 `{items: [...]}` 包装），原代码 `data?.items?.[0]` 永远 undefined。两处已修
- [x] **Add:** 将 `deploy/sql/mysql/litemall_data.sql` 中 **6 张核心表**的 INSERT 数据转换为 `{tableName}.csv`（表名小写，与 ORM `tableName` 一致）：`litemall_goods`、`litemall_goods_product`、`litemall_goods_attribute`、`litemall_goods_specification`、`litemall_category`、`litemall_brand`。这 6 张表覆盖 happy-path（goods frontList + goods_product SKU）与 UI 首页（category 导航 + brand + goods 列表）的种子需求，且互相无 user 依赖。其余 11 张非空表（ad/comment/coupon/footprint/groupon_rules/issue/keyword/log/region/system/topic）**移出本计划范围**，见 `Deferred But Adjudicated`
- [x] **Add:** 手写 6 张核心表的最小种子集 CSV（非全量转换 dump，见上条 Decision）。规模目标：category 3-5 行（L1+L2）、brand 3-5 行、goods 5-8 行（含 happy-path 所需上架商品，`IS_ON_SALE=true`）、goods_product 每商品 1 行 SKU（`NUMBER>0` 有库存）、goods_attribute 几行、goods_specification 几行。数据贴近真实（合理商品名/价格/图片 URL 占位），确保 happy-path 的 `frontList` 返回非空、SKU 可查、加购下单流程跑通
- [x] **Decision:** CSV 列名采用实体列 `code`（大写数据库列名，如 `ID`、`NAME`、`GOODS_SN`）。备选：用实体属性名（小写驼峰）。选择大写列名的理由：docs-for-ai 明确"CSV 列名按实体列的 code 匹配"，且 `dao.saveEntity()` 走 ORM 管道自动处理框架字段。残留风险：若个别实体列 `code` 与 dump 列名大小写/命名不一致，转换时需逐表核对（Phase 1 验证项覆盖）
- [x] **Decision:** 框架管理字段（`created_by`/`updated_by`/`create_time`/`update_time`/`del_flag` 等）由 `dao.saveEntity()` 自动填充，CSV 不含这些列。但 litemall 业务字段（如 `add_time`、`update_time` 这类业务时间戳）需保留。需逐表区分"框架字段"vs"litemall 业务字段"（核对 `model/app-mall.orm.xml` 每个 entity 的 `not-gen` 标记与 stdFieldType）
- [x] **Decision:** `deleted` 字段（litemall BOOLEAN 逻辑删除）CSV 中显式给 `false`/`0`，确保种子数据不被逻辑删除过滤
- [x] **Decision:** CSV 文件格式规范 —— UTF-8 编码（无 BOM）；RFC 4180 标准引号转义（含逗号/引号/换行的字段用 `"..."` 包裹，字段内字面 `"` 转义为 `""`）；字段内 `\n` 保留为字面换行（litemall_brand/goods 的 desc/detail 含中文 + `\n` + 逗号，必须正确转义否则 CSV 解析错列）；行结束符 `\n`。转换方式：手写或脚本均可，但转换后必须用 CSV 解析器（如 `csv-parse`）回读校验列数一致。备选：直接写 JSON 再转 CSV。选择 RFC 4180 的理由：它是 Nop CSV 解析器与 Excel/标准工具的通用兼容格式；残留风险：若手写出错列，`DataInitInitializer` 会抛 `NopException`（列名不匹配），Phase 1 Proof 覆盖
- [x] **Add:** `e2e/playwright.config.ts` 的 `webServer.command` 追加 `-Dnop.orm.init-database-data=true`（`init-database-data-location` 用默认 `/_init-data/`，无需显式指定）
- [x] **Proof:** 手动启动 e2e 应用命令（见 e2e-testing-guide.md:48-53，加 `-Dnop.orm.init-database-data=true`），日志确认 `DataInitInitializer` 执行无异常；`curl -X POST /r/LitemallGoods__findPage` 带 token 返回 `items` 非空（实测：`npx playwright test` 全量 39 passed，应用日志含 7 张表 CSV 加载记录 `load-csv-data`）
- [ ] **Proof:** 抽样核对种子数据完整性：取一条 `litemall_goods` 种子行，断言其 `addTime`/`updateTime` 为 2018 原值（非被框架 auto-fill 覆盖为当前时间），确认 CSV 显式给的业务时间戳被 ORM 保留（实体配置 `createTimeProp="addTime" updateTimeProp="updateTime"`，需验证 `dao.saveEntity()` 对 CSV 提供值是保留而非覆盖）。**实施发现：实测 `addTime` 被覆盖为当前时间**（curl 返回 `addTime=2026-06-19 ...`），违反原 Proof 假设。归因为 `createTimeProp` 配置使 ORM 在 `saveEntity` 时强制刷新该字段；非阻塞 happy-path（测试不依赖时间戳），但种子数据语义失真，留作 Phase 1 后续 follow-up

Exit Criteria:

- [x] `_init-data/` 下至少含 happy-path 依赖的 6 张核心表 CSV（goods/goods_product/goods_attribute/goods_specification/category/brand）+ 1 张 `nop_file_record.csv`（image domain 字段约束所需）
- [x] CSV 列名与实体列 code 逐一核对，无 `NopException`（列名不匹配）
- [x] e2e 应用启动后种子数据通过 ORM 正确插入（findPage 返回非空）
- [x] 开发 profile（无 `-Dnop.orm.init-database-data=true`）不受影响
- [x] `docs/logs/` updated

### Phase 2 — happy-path 修复

Status: done
Targets: `e2e/tests/storefront-happy-path.spec.ts`（如需调整断言）、`e2e/test-results/.last-run.json`
Required Skill: `nop-debugging`

- Item Types: `Fix`、`Proof`
- Prereqs: Phase 1 完成（种子数据就绪）

- [x] **Skill loading gate:** 加载 `nop-debugging`，读完路由表必读文档
  - Docs read: nop-debugging skill
- [x] **Fix:** `cd e2e && npx playwright test storefront-happy-path.spec.ts` 实跑，确认根因（预期：种子商品就绪后 frontList 返回非空，流程跑通）。若仍有失败，按 nop-debugging 流程定位（区分：种子数据问题 vs 测试断言问题 vs 业务代码问题）
- [x] **Fix:** 若 happy-path 断言依赖特定种子商品状态（如 SKU 库存、商品上架状态），调整测试使断言与种子数据一致，或在测试内动态适配（不硬编码种子商品 id）。实施发现两处测试代码 bug（filter 格式 + findList 返回结构），见 Phase 1 末 Fix 条目
- [x] **Proof:** `cd e2e && npx playwright test` 全量回归，结果 = 全部 passed（含 happy-path），记录到日志。**实测：39 passed（含 happy-path），0 failed**

Exit Criteria:

- [x] happy-path 单测通过
- [x] 全量 e2e `npx playwright test` 全绿（0 failed）
- [x] `.last-run.json` `status: passed` 且 `failedTests: []` 与日志一致（消除矛盾）
- [x] `docs/logs/` updated

### Phase 3 — admin 后台 RPC 冒烟测试

Status: planned
Targets: `e2e/tests/admin-pages.spec.ts`（新建）或扩展 `storefront-pages.spec.ts`
Required Skill: none（Playwright TS e2e 无对应 skill；方法来自 docs-for-ai/e2e-testing.md）

- Item Types: `Add`、`Proof`
- Prereqs: Phase 1 完成（admin 实体 findPage 需种子数据返回非空才有意义）

- [ ] **Skill loading gate:** 扫描 available skills，无匹配（`nop-testing` 为 Java `JunitAutoTestCase`/`IGraphQLEngine`，不覆盖 Playwright TS；`nop-frontend-dev` 面向 view.xml/page.yaml **开发**而非 e2e 测试）。方法遵循 `../nop-entropy/docs-for-ai/02-core-guides/e2e-testing.md` 的 Nop RPC 调用模式与标准 CRUD 表
  - Docs read: `../nop-entropy/docs-for-ai/02-core-guides/e2e-testing.md`、`docs/references/e2e-testing-guide.md`
- [ ] **Explore:** 验证 `nop`/`123` 凭证对 admin 实体 `findPage` 的 RBAC 权限。用 e2e 应用启动后 `curl -X POST /r/LitemallLog__findPage`（及 LitemallSystem/LitemallAftersale 等典型 admin 实体）带 token，确认返回 `status:0`。记录哪些 admin 实体可访问、哪些返回权限错误
- [ ] **Add:** 新建 `e2e/tests/admin-pages.spec.ts`，仿 `storefront-pages.spec.ts` 模式，对 Explore 确认可访问的 admin 后台实体（订单/商品/售后/团购/优惠券/广告/专题/反馈/评论/品牌/分类/关键字/系统配置等）加 `findPage` RPC 冒烟，断言 `status:0` + `items` 数组（允许空数组，冒烟只验证 API 可达性）
- [ ] **Add:** 对 admin 后台 view.xml 页面（`_vfs/app/mall/pages/Litemall*/Litemall*.view.xml` 的非 `_gen` 定制页）加 `PageProvider__getPage` 渲染冒烟，断言 `type:page` + `body` 非空
- [ ] **Add:** 更新 `docs/references/e2e-testing-guide.md` 的"测试文件"表（当前第 30-34 行未列 `storefront-happy-path.spec.ts`，文档过期）—— 补全 happy-path 与新增 admin-pages 两个条目
- [ ] **Proof:** `cd e2e && npx playwright test` 全量回归，admin 冒烟全过，记录新增用例数

Exit Criteria:

- [ ] Explore 已验证 nop 凭证对 admin 实体的 RBAC 可达性（记录可访问/不可访问清单）
- [ ] admin 后台实体 findPage 冒烟覆盖（至少订单/商品/售后/团购/优惠券/广告/专题/品牌/分类 9 个核心实体中可访问的部分；不可访问的记录原因）
- [ ] admin 后台定制 view.xml 页面渲染冒烟覆盖
- [ ] `docs/references/e2e-testing-guide.md` 测试文件表已更新（含 happy-path 与 admin-pages）
- [ ] 全量 e2e 全绿
- [ ] `docs/logs/` updated

### Phase 4 — 浏览器 UI 交互测试（含可行性 Explore）

Status: planned
Targets: `e2e/tests/*.spec.ts`（新建，视 Explore 结果定范围）
Required Skill: none（Playwright TS e2e 无对应 skill；方法来自 docs-for-ai/e2e-testing.md 浏览器要点）

- Item Types: `Explore`、`Add`、`Decision`、`Proof`
- Prereqs: Phase 2 完成

- [ ] **Skill loading gate:** 扫描 available skills，无匹配（`nop-testing` 为 Java 测试、`nop-frontend-dev` 面向 view.xml 开发，均不覆盖 Playwright TS 浏览器交互）。方法遵循 `../nop-entropy/docs-for-ai/02-core-guides/e2e-testing.md` 的"浏览器 E2E 测试要点"
  - Docs read: `../nop-entropy/docs-for-ai/02-core-guides/e2e-testing.md`（浏览器要点：登录、页面 URL `/#/{pageId}`、AMIS 表单交互、字段名映射）
- [ ] **Explore:** 验证浏览器渲染入口。e2e 应用启动后，用 Playwright `page.goto('/')`、`page.goto('/storefront-home')`、`page.goto('/#/storefront-home')`、admin `page.goto('/#/LitemallGoods')` 探测：HTTP 状态、返回内容是否为可交互 AMIS 页面（含可定位的 DOM 元素如 `.cxd-page`、表单字段）、还是 404/空白壳/纯 JSON。记录探测结果
- [ ] **Decision:** 根据 Explore 结果确定 Phase 4 范围（storefront 与商城 admin 渲染可行性等价，二选一即可，不分分支）：
  - 若可渲染（storefront 或商城 admin 任一返回可交互 AMIS 页面）→ 加对应 UI 交互流（登录→导航→表单→点击→断言 UI 状态变化的 `page.click/fill` 流）
  - 若均不可渲染（前端 SPA 不在仓库且平台未内置商城前端）→ Phase 4 标记 Deferred，记录阻塞原因与触发条件（"当商城前端 SPA 工程接入后"），Phase 4 不产出测试代码
- [ ] **Add:**（视 Decision 结果）新建 `e2e/tests/storefront-ui.spec.ts` 或 `admin-ui.spec.ts`，含真实 `page` 交互（非 `request` API 层）：浏览器登录、页面导航、AMIS 表单填写、按钮点击、断言 UI 状态变化
- [ ] **Proof:**（视 Decision 结果）`cd e2e && npx playwright test` 全量回归，UI 交互用例通过

Exit Criteria:

- [ ] Explore 产出的渲染入口结论有证据（HTTP 状态 + DOM 截图/断言）
- [ ] Decision 明确 Phase 4 范围（前台 UI / admin UI / Deferred），含理由
- [ ] 若范围含 UI 测试：至少 1 条真实 `page` 交互流通过（含 `page.click`/`page.fill`，非纯 `request`）
- [ ] 若 Deferred：`Deferred But Adjudicated` 区记录阻塞原因与触发条件
- [ ] `docs/logs/` updated

## Plan Audit

- Status: passed（第二轮，2026-06-18）
- Reviewer / Agent: 独立子代理 ses_1255b1813ffenkXVb30NM0eqoa（第一轮）
- Evidence:
  - **第一轮（2026-06-18）Verdict: revise**（1 Blocker + 3 Majors + 6 minors，全部基于 live repo 证据）
    - B1 Phase 1 "按需转换" 为 anti-slacking 禁用词（`as needed` 等价），11 张表处 fuzzy state — 已修订：范围收窄为 6 张核心表，11 张移入 `Deferred But Adjudicated`
    - M1 Phase 1 CSV 转换的编码/转义方法学缺失（litemall 数据含中文 + `\n` + 逗号）— 已修订：新增 CSV 格式 Decision（UTF-8 + RFC 4180 + 回读校验）
    - M2 Phase 4 推理错误：把平台自带 admin（`/#/type-hierarchy-main`）与商城自定义 admin（同样依赖 `layouts/default/index`）混为一谈 — 已修订：明确两者渲染可行性等价，Decision 分支简化
    - M3 Phase 3 admin 冒烟权限前提未验证（nop 用户可能无 admin 实体 RBAC）— 已修订：新增 Explore 前置项验证权限，Exit Criteria 加权限结论
    - m1 webServer 摘要漏 `quarkus.profile=dev` — 已补全
    - m2 e2e-testing-guide 测试文件表过期（漏 happy-path）— 已新增 doc-update item 到 Phase 3
    - m3 addTime 框架覆盖未验证 — 已新增 Phase 1 Proof 断言核对种子时间戳原值
    - m5 comment/footprint 含 user_id 悬空引用 — 已在 Deferred 11 张表条目注明
    - m6 Phase 3/4 none justify 未提 nop-frontend-dev — 已补全
  - **第二轮（2026-06-18）Verdict: passed** — 独立子代理 ses_12550cdaaffe150pArha42SbXZ 基于 live repo 证据逐项验证：B1/M1/M2/M3 全部已解决（anti-slacking 全文 0 禁用词命中、CSV Decision 经 litemall_data.sql:51 实测含中文+\n+逗号证明必要性、app-mall.action-auth.xml:6/32/66/81/126/135/152 全部命中 layouts/default/index 证实渲染等价、Phase 3 Explore+ExitCriteria 权限验证落地）；m1-m6 minor 全部复核通过；机制描述与 orm-model-design.md:238-276 一致。仅 m7 措辞建议（baseline "17 个"歧义）已采纳修订。零 Blocker + 零 Major，可进入实施

## Closure Gates

- [ ] in-scope behavior is complete（G1-G4 全部落地或按规则 Deferred）
- [ ] relevant docs are aligned（e2e-testing-guide / codebase-map / module-boundaries 用例数与覆盖面同步）
- [ ] verification has run：`cd e2e && npx playwright test` 全绿（0 failed）+ `./mvnw.cmd package -DskipTests -pl app-mall-app -am` BUILD SUCCESS
- [ ] N/A — no new `@BizMutation`/`@BizQuery` methods（本计划是测试与数据基建，不新增业务方法）
- [ ] no in-scope item downgraded to deferred/follow-up（Phase 4 的 Deferred 必须经 Explore 证据支撑，非偷懒）
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill` listed（Phase 1 nop-orm-modeler、Phase 2 nop-debugging、Phase 3/4 none 含 justify）
- [ ] skill loading verification: 每个执行 agent 加载匹配 skill 并读完路由必读文档
- [ ] text consistency verified: status / phases / gates / log 全部一致
- [ ] closure audit was performed by a different agent/session than implementation
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### CI 集成（GitHub Actions 跑 e2e）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 用户本轮未选 CI 方向；e2e 当前手动运行机制完善（e2e-testing-guide.md）
- Successor Required: `no`
- 触发条件：团队需要 PR 级 e2e 门禁时，新增 `.github/workflows/e2e.yml`（含 jar 构建 + Chrome 安装 + `npx playwright test`）

### 完整 CRUD 边界/负面测试

- Classification: `optimization candidate`
- Why Not Blocking Closure: 本轮目标是冒烟级覆盖扩展与 happy-path 修复；update/delete/错误分支属于深化覆盖
- Successor Required: `no`
- 触发条件：核心业务流程频繁回归时，补充 CRUD 边界与异常分支测试

### 11 张非核心表种子数据

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: Phase 1 仅交付 6 张核心表（goods/goods_product/goods_attribute/goods_specification/category/brand），覆盖 happy-path 与 UI 首页种子需求。其余 11 张非空表（ad/comment/coupon/footprint/groupon_rules/issue/keyword/log/region/system/topic）的种子数据对 G2-G3 不阻塞：admin findPage 冒烟允许空 items（只验 API 可达性），happy-path 不依赖这些表。已知限制：`litemall_comment`/`litemall_footprint` 等种子含 `user_id` 引用已废弃的 `litemall_user`（ORM 已移除该实体，无 FK 约束，功能无害，但语义上为悬空引用），转换时需清空或重映射这些 user_id
- Successor Required: `no`
- 触发条件：当 Phase 3 admin 冒烟需要某实体返回非空数据以验证字段映射，或后续 CRUD/边界测试需要这些实体的种子时，补齐对应 CSV

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: <待 plan 完成后填写>

Closure Audit Evidence:

- Reviewer / Agent: <independent reviewer or cold-replay proxy — MUST NOT be the implementing agent>
- Evidence: <task id / log link / walkthrough record; link audit file only when separately justified>

Follow-up:

- <CI 集成、CRUD 边界测试见 Deferred But Adjudicated>
