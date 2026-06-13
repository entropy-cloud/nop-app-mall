# 2026-06-13-orm-index-and-entity-cleanup-plan ORM 索引补全与实体残留清理

> Plan Status: planned
> Last Reviewed: 2026-06-13
> Source: `docs/audits/2026-06-13-adversarial-review-full-project.md` AR-4 [P1]；`docs/audits/2026-06-12-multi-dimensional-audit-full-project.md` H2
> Related: `docs/plans/2026-06-13-adversarial-audit-remediation-plan.md`（Phase 1 修正 ORM 类型/精度错误后，本计划可执行）
> Audit: required

## Current Baseline

- `model/app-mall.orm.xml` 有 30 个实体（LitemallAdmin/Role/Permission/UserRole 已从 ORM 模型中消除，但 API/web/meta/service 残留文件仍存在）
- 整个 ORM 模型仅在 `LitemallGoods.name` 上定义了一个唯一索引（`goodsNameKey`），所有外键列无索引
- 已消除实体的残留文件：
  - API: `LitemallAdminApi.java`, `LitemallAdminInputBean.java`, `LitemallAdminOutputBean.java`（Role/Permission 同理）
  - Web: `LitemallAdmin/LitemallAdmin.view.xml`, `LitemallAdmin.lib.xjs`, `main.page.yaml`, `picker.page.yaml`，`_gen/` 目录（Role/Permission 同理）
  - Meta: `app-mall-meta/.../LitemallAdmin/LitemallAdmin.xmeta`，`_LitemallAdmin.xmeta`（Role/Permission 同理）
  - Service xbiz: `app-mall-service/.../LitemallAdmin/LitemallAdmin.xbiz`，`_LitemallAdmin.xbiz`（Role/Permission 同理）
  - 注：xbiz 文件在 `app-mall-service/` 而非 `app-mall-meta/`
- 已消除实体的 BizModel 和 `_service.beans.xml` 注册已清理（确认无残留）
- `deploy/sql/` 下有 MySQL/PostgreSQL/Oracle 三种方言的 DDL 脚本
- `codegen.sh` 可从 ORM 模型重新生成代码

## Goals

1. 为 `model/app-mall.orm.xml` 中所有高频查询外键列和状态列添加索引（包括 FK 关系列、domain=userId 列、以及高频过滤的 status 列）
2. 清理已消除实体（LitemallAdmin/LitemallRole/LitemallPermission）的 API/web/meta 残留文件
3. 重新生成 DDL 脚本，确保数据库 schema 与 ORM 模型一致

## Non-Goals

- 不修改业务逻辑或 BizModel
- 不修改前端 view.xml 的业务定制内容
- 不重新执行全量 `codegen.sh`（仅重新生成 DDL 和必要的索引相关产物）
- 不处理 LitemallUser 残留（已在主修正计划 Phase 4 中处理）

## Task Route

- Type: `implementation-only change`
- Owner Docs: `docs/architecture/module-boundaries.md`（残留清理）
- Skill Selection Basis: Phase 1 涉及 ORM 模型修改 → `nop-orm-modeler`；Phase 2 涉及文件删除 → 无特殊 skill；Phase 3 涉及 DDL 生成 → `nop-orm-modeler`

## Infrastructure And Config Prereqs

- 修改 `model/app-mall.orm.xml` 后需要运行 `./mvnw compile -DskipTests` 验证编译
- 需要 `nop-cli` 或 `codegen.sh` 重新生成 DDL

## Execution Plan

### Phase 1 — ORM 模型索引补全（Fix-heavy）

Status: planned
Targets: `model/app-mall.orm.xml`
Required Skill: `nop-orm-modeler`

- Item Types: `Fix`
- Prereqs: 建议在 `2026-06-13-adversarial-audit-remediation-plan` Phase 1 完成后执行（避免 ORM 模型并发修改冲突）

- [ ] **Skill loading gate:** 加载 `nop-orm-modeler` skill，读取其路由表中所有必读文档。列出已读文档路径。
  - Docs read: <list paths here>
- [ ] **索引补全:** 在 `model/app-mall.orm.xml` 中为以下列添加 `<indexes>` 定义：
  - `LitemallOrder`: `idx_order_userId` (userId), `idx_order_status` (orderStatus — 高频过滤列，非 FK 但在所有订单查询中使用)
  - `LitemallCart`: `idx_cart_userId` (userId), `idx_cart_goodsId` (goodsId)
  - `LitemallFootprint`: `idx_footprint_userId` (userId), `idx_footprint_goodsId` (goodsId)
  - `LitemallCollect`: `idx_collect_userId` (userId)
  - `LitemallComment`: `idx_comment_userId` (userId)
  - `LitemallCouponUser`: `idx_couponUser_userId` (userId), `idx_couponUser_orderId` (orderId), `idx_couponUser_couponId` (couponId)
  - `LitemallFeedback`: `idx_feedback_userId` (userId)
  - `LitemallSearchHistory`: `idx_searchHistory_userId` (userId)
  - `LitemallAddress`: `idx_address_userId` (userId)
  - `LitemallOrderGoods`: `idx_orderGoods_orderId` (orderId), `idx_orderGoods_goodsId` (goodsId), `idx_orderGoods_productId` (productId — 有正式 ORM to-one 关系)
  - `LitemallAftersale`: `idx_aftersale_orderId` (orderId), `idx_aftersale_userId` (userId)
  - `LitemallGroupon`: `idx_groupon_orderId` (orderId), `idx_groupon_grouponRulesId` (grouponRulesId), `idx_groupon_userId` (userId — domain=userId，与所有其他 userId 列保持一致)
  - `LitemallGrouponRules`: `idx_grouponRules_goodsId` (goodsId)
  - `LitemallGoods`: `idx_goods_categoryId` (categoryId), `idx_goods_brandId` (brandId)
  - `LitemallGoodsProduct`: `idx_product_goodsId` (goodsId)
  - `LitemallGoodsSpecification`: `idx_spec_goodsId` (goodsId)
  - `LitemallGoodsAttribute`: `idx_attr_goodsId` (goodsId)
  - `LitemallCategory`: `idx_category_pid` (pid)
  - `LitemallRegion`: `idx_region_pid` (pid)
- [ ] **Verification:** `./mvnw compile -DskipTests` 编译通过

Exit Criteria:

- [ ] 所有上述外键列在 ORM 模型中有对应索引定义
- [ ] `./mvnw compile -DskipTests` 通过
- [ ] No owner-doc update required（索引为内部优化）

### Phase 2 — 已消除实体残留文件清理（Fix-heavy）

Status: planned
Targets: `app-mall-api/`, `app-mall-web/`, `app-mall-meta/`
Required Skill: none（纯文件删除操作）

- Item Types: `Fix`
- Prereqs: 无

- [ ] 删除 `app-mall-api/src/main/java/app/mall/api/crud/LitemallAdminApi.java`
- [ ] 删除 `app-mall-api/src/main/java/app/mall/api/beans/LitemallAdminInputBean.java`
- [ ] 删除 `app-mall-api/src/main/java/app/mall/api/beans/LitemallAdminOutputBean.java`
- [ ] 删除 `app-mall-web/src/main/resources/_vfs/app/mall/pages/LitemallAdmin/` 整个目录（含 `_gen/`、view.xml、lib.xjs、page.yaml）
- [ ] 删除 `app-mall-meta/src/main/resources/_vfs/app/mall/model/LitemallAdmin/` 整个目录（含 xmeta）
- [ ] 删除 `app-mall-service/src/main/resources/_vfs/app/mall/model/LitemallAdmin/` 整个目录（含 xbiz）— **注：xbiz 在 service 模块而非 meta 模块**
- [ ] 对 `LitemallRole` 执行相同操作（3 个 API 文件 + web 目录 + meta 目录 + service xbiz 目录）
- [ ] 对 `LitemallPermission` 执行相同操作（3 个 API 文件 + web 目录 + meta 目录 + service xbiz 目录）
- [ ] 从 `deploy/sql/mysql/litemall_data.sql` 中移除所有引用 `litemall_admin`、`litemall_role`、`litemall_permission` 表的 INSERT 语句（约 lines 37-43, 268-294）
- [ ] 搜索项目内是否还有其他文件引用 `LitemallAdmin`、`LitemallRole`、`LitemallPermission`（除 `_gen` 生成文件外），如有则一并清理
- [ ] **Verification:** `./mvnw compile -DskipTests` 编译通过

Exit Criteria:

- [ ] 项目中不再存在 `LitemallAdmin`、`LitemallRole`、`LitemallPermission` 相关的 API/Web/Meta 文件
- [ ] 无编译错误（其他代码不引用这些已删除的类）
- [ ] `./mvnw compile -DskipTests` 通过

### Phase 3 — DDL 重新生成与验证（Proof）

Status: planned
Targets: `deploy/sql/`
Required Skill: `nop-orm-modeler`

- Item Types: `Proof`
- Prereqs: Phase 1（索引添加后重新生成 DDL）

- [ ] **Skill loading gate:** 加载 `nop-orm-modeler` skill，读取其路由表中所有必读文档。列出已读文档路径。
  - Docs read: <list paths here>
- [ ] **Decision:** DDL 重新生成方式。`codegen.sh` 当前从 `model/app-mall.orm.xlsx` 生成（XLSX 为源），但本项目的 source of truth 是 `model/app-mall.orm.xml`。**选择方案：** 使用 `nop-cli convert` 将 XML 转为 XLSX 后执行 `codegen.sh`，或直接使用 `nop-cli gen-db` 从 XML 生成 DDL。执行前需确认 `nop-cli` 支持的精确命令
- [ ] 使用选定命令重新生成 `deploy/sql/mysql/_create_app-mall.sql`
- [ ] 验证生成的 MySQL DDL 包含所有新增索引的 `CREATE INDEX` 语句
- [ ] 验证生成的 MySQL DDL 不包含 LitemallAdmin/Role/Permission 的 `CREATE TABLE`
- [ ] 手动同步索引定义到 `deploy/sql/postgresql/_create_app-mall.sql` 和 `deploy/sql/oracle/_create_app-mall.sql`（语法差异需手动处理）
- [ ] **Verification:** 对比生成前后的 DDL diff，确认变更仅包含新增索引

Exit Criteria:

- [ ] MySQL DDL 包含所有新增索引
- [ ] MySQL DDL 不包含 LitemallAdmin/Role/Permission 表
- [ ] PostgreSQL 和 Oracle DDL 同步更新
- [ ] `./mvnw compile -DskipTests` 通过
- [ ] `./mvnw test` 全部通过
- [ ] `docs/logs/` updated

## Plan Audit

- Status: passed (round 2)
- Reviewer / Agent: independent subagent (task ses_140e35b4dffey0WZg6CDMt00ZL)
- Round 1 findings: 2 blockers (entity count 35→30, xbiz file location wrong), 3 major (data.sql seed data, OrderGoods.productId missing, Groupon.userId missing), 1 minor (Phase 3 DDL command vague)
- Round 1 disposition: All blockers and major objections addressed:
  - B1: Entity count corrected to 30
  - B2: Phase 2 now deletes xbiz from `app-mall-service/` separately from xmeta in `app-mall-meta/`
  - M1: Added `litemall_data.sql` seed data cleanup to Phase 2
  - M2: Added `LitemallOrderGoods.productId` index
  - M3: Added `LitemallGroupon.userId` index
  - M4/M5: Phase 3 now has explicit Decision item for DDL generation command; Goal scope clarified to include status columns

## Closure Gates

- [ ] in-scope behavior is complete
- [ ] verification has run: `./mvnw compile -DskipTests` && `./mvnw test`
- [ ] no in-scope item downgraded to deferred/follow-up
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill` listed
- [ ] skill loading verification complete
- [ ] text consistency verified
- [ ] closure audit was independent
- [ ] closure evidence exists in files

## Deferred But Adjudicated

无。

## Closure

Status Note: <to be filled after closure>

Closure Audit Evidence:

- Reviewer / Agent: <independent reviewer>
- Evidence: <to be filled after closure>
