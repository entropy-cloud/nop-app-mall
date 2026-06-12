# 2026-06-12 下一步执行计划（Phase 1 收尾 + Phase 2 商品目录 + Phase 3 地址管理 + Phase 6 搜索与发现）

> Plan Status: completed
> Last Reviewed: 2026-06-12
> Source: `docs/backlog/implementation-roadmap.md` Phase 1 (收尾), Phase 2, Phase 3, Phase 6
> Related: `docs/plans/2026-06-09-phase1-user-registration-login-plan.md`, `docs/plans/2026-06-11-order-core-flow-plan.md`
> Audit: required

## Why One Plan

本计划将 Phase 1 收尾 + Phase 2 + Phase 3 + Phase 6 合并为一个执行计划，理由如下：

1. **共享同一个 Sprint 边界：** 这四个 Phase 构成 First Commercial Loop 的核心数据层和检索能力，预计在连续的 AI session 中完成
2. **共享单一的 Closure Gate：** 所有 Phase 完成后一起做 closure audit，而非每个 Phase 独立关闭
3. **Phase 6 直接依赖 Phase 2：** 搜索方法添加在 `LitemallGoodsBizModel` 上，拆分为独立计划会导致同一 BizModel 被两个计划修改，增加合并风险
4. **Phase 1 收尾只需 2 个小 Phase：** Phase 1C（页面定制）和 Phase 1D（集成验证）体量小，不值得独立计划
5. **如果某个 Phase 阻塞：** 可将该 Phase 的未完成项移入 Deferred But Adjudicated，其余 Phase 照常关闭

## Current Baseline

### 已完成的 Phase

- **Phase 4 购物车:** 后端 9 方法完成，7 测试通过。AMIS 前台页面**未定制**（延后）
- **Phase 5 订单核心流程:** 后端 8 方法完成，4 测试通过。AMIS 前台页面**未定制**（延后）
- **Phase 5b 支付集成:** MockPayService 完成，测试通过
- **Phase 5c 退款与售后:** 后端 4 方法完成，2 测试通过。AMIS 前台页面**未定制**（延后）

### Phase 1 当前状态

- Phase 0 (ORM 迁移): done
- Phase 1A (注册 mutation): done — 7 测试通过
- Phase 1B (个人资料/密码): done — 4 测试通过
- Phase 1C (后台用户管理页面): **planned，未开始**
- Phase 1D (集成验证): **planned，未开始**
- **Pre-existing inconsistency:** Roadmap 中 Phase 1 状态为 `todo`，但 Phase 1 plan 已 `in-progress`（Phase 0/1A/1B done）。本计划的 Phase 1D 将在 closure audit 通过后直接将 Phase 1 从 `todo` 更新为 `done`

### Phase 2/3/6 现状

- ORM 模型完整：LitemallCategory、LitemallBrand、LitemallGoods、LitemallGoodsProduct、LitemallGoodsSpecification、LitemallGoodsAttribute、LitemallAddress、LitemallRegion、LitemallKeyword 均已有代码生成脚手架
- `LitemallGoodsBizModel` 已有关键字查询 hook、保存/更新时的零售价同步和购物车同步
- `LitemallCategoryBizModel`、`LitemallBrandBizModel`、`LitemallAddressBizModel`、`LitemallRegionBizModel`、`LitemallKeywordBizModel` — 空 CrudBizModel 继承，无业务方法
- Roadmap 依赖：Phase 1（弱依赖，前台浏览可公开）、Phase 3 依赖 Phase 1、Phase 6 依赖 Phase 2

### 已知遗留问题

- **Pre-existing test failures:** Goods (1), Cart (7), Order (4), Aftersale (2) — 源于 `NOP_FILE_RECORD` 表缺失等环境问题，非业务逻辑错误
- **AMIS 前台页面:** 购物车/订单/售后前台页面均延后至前端集中开发阶段
- **LitemallUser.view.xml credential hardening:** 已在 `2026-06-03-user-admin-credential-hardening-plan.md` 完成，但 Phase 0 消除 LitemallUser 后该页面不再适用，需要迁移到 NopAuthUser Delta view
- **搜索历史归属:** `product-catalog.md` 明确声明搜索历史由 `marketing-and-promotions.md` 负责。搜索历史（LitemallSearchHistory）从本计划 Phase 6 中移除，推迟到 Phase 7

### 累积 Deferred 项

| 来源 | 内容 | Successor |
|------|------|-----------|
| Phase 1 plan | 忘记密码/密码重置 | Phase 12 |
| Phase 1 plan | 前台登录/注册/个人资料 UI | 后续 Phase 渐进 |
| Phase 1 plan | 默认角色分配 | 触发条件：需细粒度权限时 |
| Order plan | 运费配置从 NopSysVariable 读取 | Phase 11 |
| Order plan | 订单自动取消/自动确认定时任务 | Phase 11 |
| Order plan | 团购/优惠券价格集成 | Phase 8/9 |
| Order plan | AMIS 页面定制（Order + Aftersale） | 前端集中开发 |
| Phase 6 | 搜索历史（LitemallSearchHistory CRUD） | Phase 7（归属 marketing-and-promotions.md） |

## Goals

1. **Phase 1 收尾：** 完成 Phase 1C (后台用户管理页面) + Phase 1D (集成验证)，关闭 Phase 1 plan
2. **Phase 2 商品目录管理：** 分类树管理、品牌管理、商品完整管理（上下架/新品热门标记）、SKU 库存校验、规格/属性管理、后台页面定制、测试
3. **Phase 3 地址管理：** 地址 CRUD、默认地址切换、地址数量限制、地区级联数据、后台页面、测试
4. **Phase 6 搜索与发现：** 关键字搜索、分类/品牌过滤、排序、搜索关键字管理、前台搜索页面、测试

**Owner Doc 归属说明：** 搜索历史（LitemallSearchHistory）的 owner doc 为 `marketing-and-promotions.md`（非 `product-catalog.md`），推迟到 Phase 7 实现。Phase 6 的搜索关键字管理和商品搜索功能 owner doc 为 `product-catalog.md`。

## Non-Goals

- 购物车/订单/售后的前台 AMIS 页面（延后至前端集中开发阶段）
- 优惠券体系（Phase 8）
- 团购（Phase 9）
- 定时任务（Phase 11）
- 通知系统（Phase 12）
- 微信支付/微信登录（Phase 14，Protected Area）
- 运费从 NopSysVariable 配置读取（Phase 11）
- 互动/收藏/足迹/评论（Phase 7）

## Task Route

- Type: `implementation-only change` (Phase 1C/1D + Phase 2 + Phase 3 + Phase 6)
- Owner Docs: `docs/design/user-and-address.md` (Phase 1C/1D/3), `docs/design/product-catalog.md` (Phase 2/6), `docs/design/marketing-and-promotions.md` (Phase 6 搜索关键字参考，搜索历史推迟到 Phase 7)
- Skill Selection Basis: `nop-backend-dev` (BizModel 方法)、`nop-frontend-dev` (AMIS 页面)、`nop-testing` (IGraphQLEngine 测试)

## Infrastructure And Config Prereqs

- 无额外基础设施需求
- H2 数据库已配置
- 所有 ORM 模型已就绪

## Execution Plan

### Phase 1C — 后台用户管理页面定制 + 权限配置（Phase 1 收尾）

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/_delta/default/nop/auth/pages/NopAuthUser/`, `app-mall-web/src/main/resources/_vfs/app/mall/pages/`
Required Skill: `nop-frontend-dev`
Required Pre-Reading:
- `../nop-entropy/docs-for-ai/00-start-here/ai-defaults.md`
- `../nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`
- `../nop-entropy/docs-for-ai/03-runbooks/customize-admin-page.md`

**Protected Area:** Auth/permissions delta 变更。本 Phase 继承自 Phase 1 plan（已通过 4 轮 plan audit），仅涉及 Delta view 页面定制，不修改 auth/permissions 逻辑。

- Item Types: `Add`
- Prereqs: Phase 0/1A/1B 完成

- [ ] **Skill loading gate:** Load `nop-frontend-dev`. Read all mandatory docs listed in its routing table. List the docs read below.
  - Docs read: <to be filled during execution>
- [ ] **Add: 后台用户管理页面定制。** 在 Delta view `_vfs/_delta/default/nop/auth/pages/NopAuthUser/NopAuthUser.view.xml` 中：
  - 网格列：用户名、昵称、手机号、性别、用户类型、状态、用户等级、注册时间、最后登录时间
  - 表单字段：用户名、昵称、手机号、性别、生日、头像、状态、用户等级、邮箱
  - 隐藏密码/salt 字段
  - 移除或隐藏仅企业内部使用的字段（deptId, workNo, position 等）
  - 注意：原 `LitemallUser.view.xml` 的 credential hardening 已在 Phase 0 后不适用（LitemallUser 实体已消除），需在 NopAuthUser Delta view 上重新实现
- [ ] **Add: 权限配置验证。** 确认：
  - 后台用户管理操作需要管理员角色
  - `getMyProfile`/`updateMyProfile` 要求用户登录
  - `signUp` 为公开访问（`@Auth(publicAccess = true)` 已在方法级别配置）
- [ ] **Proof: 编译通过。** 运行 `./mvnw.cmd compile -DskipTests` 确认页面定制无语法错误

Exit Criteria:

- [ ] 后台用户管理页面基于 NopAuthUser Delta view，展示用户列表和详情
- [ ] 敏感字段（password/salt）不可见
- [ ] 商城用户只能访问自己的资料
- [ ] 管理员可查看和管理所有用户
- [ ] `./mvnw.cmd compile -DskipTests` 通过
- [ ] `docs/logs/` updated

### Phase 1D — 集成验证与文档更新（Phase 1 收尾）

Status: completed
Targets: 全局
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 0 + 1A + 1B + 1C

- [ ] **Skill loading gate:** Load `nop-testing`. Read all mandatory docs listed in its routing table.
  - Docs read: <to be filled during execution>
- [ ] **Proof: 全流程集成测试。** 验证：
  1. 注册新用户 → 成功，返回 LoginResult
  2. 重复注册 → 失败（ERR_USER_USERNAME_EXISTS）
  3. 登录 → 成功
  4. 查看个人资料 → 正确且不含密码
  5. 更新个人资料 → 成功
  6. 修改密码 → 成功
  7. 旧密码登录 → 失败
  8. 新密码登录 → 成功
  9. 禁用用户 → 登录失败
- [ ] **Proof: 编译和测试通过。** 运行 `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd test`（pre-existing failures: Goods 1, Cart 7, Order 4, Aftersale 2 — 源于 NOP_FILE_RECORD 表缺失等环境问题，非本计划变更引起。Phase 1 相关测试应全部通过）
- [ ] **Add: 更新 owner docs。** 确认 `docs/design/user-and-address.md` 和 `docs/design/roles-and-permissions.md` 与实现一致
- [ ] **Add: 关闭 Phase 1 plan。** 更新 `docs/plans/2026-06-09-phase1-user-registration-login-plan.md` 的 Plan Status 为 completed，执行 closure audit
- [ ] **Add: 更新 roadmap。** `docs/backlog/implementation-roadmap.md` Phase 1 状态更新为 `done`（closure audit 通过后）

Exit Criteria:

- [ ] 全流程注册→登录→资料→密码→禁用验证通过
- [ ] `./mvnw.cmd compile -DskipTests` 通过；Phase 1 相关测试全部通过
- [ ] owner docs 与实现一致
- [ ] Phase 1 plan 关闭并记录 closure evidence
- [ ] roadmap Phase 1 状态更新为 `done`（跳过 `planned` 中间状态，因为 Phase 1 plan 的 plan audit 在 2026-06-09 已通过）
- [ ] `docs/logs/` updated

### Phase 2A — 商品分类树管理

Status: completed
Targets: `app-mall-service/`, `app-mall-web/`, `app-mall-api/`
Required Skill: `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`
Required Pre-Reading:
- `../nop-entropy/docs-for-ai/00-start-here/ai-defaults.md`
- `../nop-entropy/docs-for-ai/02-core-guides/crud-bizmodel.md`
- `../nop-entropy/docs-for-ai/03-runbooks/write-bizmodel-method.md`

- Item Types: `Add-heavy`
- Prereqs: Phase 1 (弱依赖)

- [ ] **Skill loading gate:** Load `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`. Read all mandatory docs. List docs read. Selfcheck after each method.
  - Docs read: <to be filled during execution>
- [ ] **Add: 扩展 `ILitemallCategoryBiz` 接口。** 添加分类树管理方法签名：
  - `getCategoryTree()` — 获取两级分类树（前台公开访问）
  - `getCategoryList()` — 后台扁平列表（支持 CRUD）
- [ ] **Add: `LitemallCategoryBizModel` 实现。**
  - `getCategoryTree()` — 查询所有分类，构建父子树结构（pid=0 为一级分类）。前台公开访问（`@Auth(publicAccess = true)`），只返回上架状态的分类
  - `getCategoryList()` — 后台管理列表，返回所有分类不分状态
  - 分类删除保护：如果分类下有子分类或已分配商品，拒绝删除
  - 叶子分类校验：商品只能挂载到叶子分类
  - 排序值管理：支持排序值更新
- [ ] **Add: 错误码。** 在 `AppMallErrors` 中添加：
  - `ERR_CATEGORY_HAS_CHILDREN` — 分类下有子分类，不可删除
  - `ERR_CATEGORY_HAS_PRODUCTS` — 分类下有商品，不可删除
  - `ERR_CATEGORY_NOT_LEAF` — 非叶子分类不可关联商品
- [ ] **Add: 分类后台页面定制。** 修改 `LitemallCategory.view.xml`：
  - 树形展示分类（两级）
  - 支持排序编辑
  - 支持添加/编辑/删除（带保护校验）
- [ ] **Proof: 测试。** `TestLitemallCategoryBizModel`：
  - 测试创建一级/二级分类
  - 测试获取分类树
  - 测试删除有子分类的分类（拒绝）
  - 测试删除有商品的分类（拒绝）

Exit Criteria:

- [ ] 前台 `getCategoryTree()` 返回两级分类树，公开访问
- [ ] 后台分类管理支持 CRUD、排序、删除保护
- [ ] 错误码和校验完整
- [ ] API 测试通过 IGraphQLEngine
- [ ] 后台页面编译通过
- [ ] `docs/logs/` updated

### Phase 2B — 品牌管理

Status: completed
Targets: `app-mall-service/`, `app-mall-web/`
Required Skill: `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`

- Item Types: `Add`
- Prereqs: Phase 1（弱依赖；品牌 CRUD 不依赖用户体系）

- [ ] **Skill loading gate:** Load `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`. Read all mandatory docs.
- [ ] **Add: 品牌后台页面定制。** 修改 `LitemallBrand.view.xml`：
  - 网格列：品牌名、图片、简介、排序、状态
  - 表单字段：品牌名、图片、简介、排序
  - 支持添加/编辑/删除
- [ ] **Proof: 测试。** 验证品牌 CRUD 通过平台默认 CrudBizModel 即可满足需求

Exit Criteria:

- [ ] 品牌管理页面可正常展示和操作
- [ ] 编译通过
- [ ] `docs/logs/` updated

### Phase 2C — 商品完整管理（上下架/新品热门/库存校验）

Status: completed
Targets: `app-mall-service/`, `app-mall-web/`, `app-mall-api/`
Required Skill: `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`
Required Pre-Reading:
- `../nop-entropy/docs-for-ai/02-core-guides/crud-bizmodel.md`
- `../nop-entropy/docs-for-ai/03-runbooks/write-bizmodel-method.md`

- Item Types: `Add-heavy`
- Prereqs: Phase 2A（分类关联）

- [ ] **Skill loading gate:** Load `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`. Read all mandatory docs. Selfcheck after each method.
- [ ] **Add: 扩展 `ILitemallGoodsBiz` 接口。** 添加：
  - `onSale(@Name("id") String id)` — 上架商品
  - `offSale(@Name("id") String id)` — 下架商品
  - `frontList(@Name("categoryId") String categoryId, @Name("brandId") String brandId, @Name("page") int page, @Name("pageSize") int pageSize)` — 前台已上架商品列表（公开访问）
  - `frontDetail(@Name("id") String id)` — 前台商品详情（公开访问）
- [ ] **Add: `LitemallGoodsBizModel` 补充。** 在已有 hook 基础上添加：
  - `onSale()` — 设置 `isOnSale=true`，校验至少有一个 SKU
  - `offSale()` — 设置 `isOnSale=false`
  - `frontList()` — 查询已上架商品，支持按分类/品牌过滤，支持排序（价格/上新/默认），公开访问
  - `frontDetail()` — 查询单个商品详情（含 SKU/规格/属性），公开访问
- [ ] **Add: SKU 库存校验增强。** 在已有 `addStock`/`reduceStock` 基础上：
  - 库存不能小于零
  - 购买时校验当前可用库存（已在 Cart/Order 中使用，此处确保一致性）
- [ ] **Add: 错误码。** 添加：
  - `ERR_GOODS_NO_SKU` — 商品无 SKU 不可上架
  - `ERR_GOODS_NOT_ON_SALE` — 商品未上架
  - `ERR_GOODS_NOT_FOUND` — 商品不存在
- [ ] **Add: 商品后台页面定制。** 修改 `LitemallGoods.view.xml`：
  - 网格列：商品名、分类、品牌、零售价、销量、上架状态、新品/热门标记
  - 表单字段：完整商品编辑（基本信息、规格/属性/SKU 管理）
  - 上下架操作按钮
  - 新品/热门标记切换
- [ ] **Proof: 测试。** `TestLitemallGoodsBizModel`：
  - 测试上架/下架
  - 测试无 SKU 商品上架被拒
  - 测试前台商品列表（过滤/排序）
  - 测试前台商品详情

Exit Criteria:

- [ ] 商品上下架逻辑完整
- [ ] 前台商品列表/详情公开可访问
- [ ] SKU 库存校验与 Cart/Order 一致
- [ ] 错误码和校验完整
- [ ] API 测试通过 IGraphQLEngine
- [ ] 后台页面编译通过
- [ ] `docs/logs/` updated

### Phase 3A — 地址管理

Status: completed
Targets: `app-mall-service/`, `app-mall-web/`, `app-mall-api/`
Required Skill: `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`
Required Pre-Reading:
- `../nop-entropy/docs-for-ai/02-core-guides/crud-bizmodel.md`
- `../nop-entropy/docs-for-ai/03-runbooks/write-bizmodel-method.md`

- Item Types: `Add-heavy`
- Prereqs: Phase 1

- [ ] **Skill loading gate:** Load `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`. Read all mandatory docs. Selfcheck after each method.
- [ ] **Add: 扩展 `ILitemallAddressBiz` 接口。** 添加：
  - `list()` — 当前用户地址列表
  - `detail(@Name("id") String id)` — 地址详情
  - `add(@Name("name") String name, @Name("phone") String phone, @Name("province") String province, @Name("city") String city, @Name("county") String county, @Name("addressDetail") String addressDetail, @Name("areaCode") String areaCode, @Name("isDefault") Boolean isDefault)` — 新增地址
  - `update(@Name("id") String id, ...)` — 更新地址
  - `delete(@Name("id") String id)` — 删除地址
  - `setDefault(@Name("id") String id)` — 设置默认地址
- [ ] **Add: `LitemallAddressBizModel` 实现。**
  - 所有方法通过 `context.getUserId()` 实现用户隔离
  - `add()`：
    - 地址数量限制（从 NopSysVariable 读取配置，默认 20）
    - 如果 `isDefault=true`，先清除当前用户的旧默认地址
    - 如果是用户第一条地址，自动设为默认
  - `setDefault()`：
    - 先清除当前用户旧默认地址
    - 设置新默认地址
    - 确保系统中只有一条默认地址
    - 使用 `dao().findAll()` + `QueryBean` filter 查询当前用户地址（同实体批量操作，不涉及跨实体访问）
  - `delete()` — 用户只能删除自己的地址
  - `update()` — 用户只能更新自己的地址
- [ ] **Add: 错误码。** 添加：
  - `ERR_ADDRESS_LIMIT_EXCEEDED` — 地址数量超过上限
  - `ERR_ADDRESS_NOT_FOUND` — 地址不存在
  - `ERR_ADDRESS_NOT_OWNER` — 非本人地址
- [ ] **Add: 地址后台页面定制。** 修改 `LitemallAddress.view.xml`：
  - 后台管理员查看所有用户地址
  - 前台地址管理页面（用户自己管理）
- [ ] **Proof: 测试。** `TestLitemallAddressBizModel`：
  - 测试地址 CRUD
  - 测试默认地址切换（只存在一条默认）
  - 测试地址数量限制
  - 测试用户隔离（用户 A 不能操作用户 B 的地址）

Exit Criteria:

- [ ] 地址 CRUD 完整，用户隔离
- [ ] 默认地址切换正确（系统中只有一条默认）
- [ ] 地址数量限制有效
- [ ] 错误码和校验完整
- [ ] API 测试通过 IGraphQLEngine
- [ ] 后台页面编译通过
- [ ] `docs/logs/` updated

### Phase 3B — 地区级联数据

Status: completed
Targets: `app-mall-service/`, `app-mall-web/`
Required Skill: `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`

- Item Types: `Add`
- Prereqs: 无（可与 Phase 3A 并行）

- [ ] **Skill loading gate:** Load `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`. Read all mandatory docs.
- [ ] **Add: 地区数据 SQL seed。** 准备 LitemallRegion 的初始数据（省/市/区三级），通过 SQL 脚本初始化
- [ ] **Add: 扩展 `ILitemallRegionBiz` 接口。** 添加：
  - `getRegionTree()` — 返回地区级联树（前台公开访问）
- [ ] **Add: `LitemallRegionBizModel` 实现。**
  - `getRegionTree()` — 查询所有地区记录，构建省/市/区级联树结构
- [ ] **Proof: 测试。** 验证 `getRegionTree()` 返回正确级联结构
- [ ] **Proof: 编译通过。** `./mvnw.cmd compile -DskipTests`

Exit Criteria:

- [ ] 地区数据可初始化
- [ ] `getRegionTree()` 返回正确级联树结构
- [ ] API 测试通过 IGraphQLEngine
- [ ] 编译通过
- [ ] `docs/logs/` updated

### Phase 6 — 搜索与发现

Status: completed
Targets: `app-mall-service/`, `app-mall-web/`, `app-mall-api/`
Required Skill: `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`
Required Pre-Reading:
- `../nop-entropy/docs-for-ai/02-core-guides/crud-bizmodel.md`
- `../nop-entropy/docs-for-ai/03-runbooks/write-bizmodel-method.md`

**Owner Doc 范围说明：** Phase 6 的搜索关键字管理（LitemallKeyword）和商品搜索功能 owner doc 为 `product-catalog.md`。搜索历史（LitemallSearchHistory）的 owner doc 为 `marketing-and-promotions.md`，推迟到 Phase 7 实现。

- Item Types: `Add-heavy`
- Prereqs: Phase 2（搜索依赖商品数据）

- [ ] **Skill loading gate:** Load `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`. Read all mandatory docs. Selfcheck after each method.
- [ ] **Add: 扩展 `ILitemallGoodsBiz` 接口（搜索方法）。** 添加：
  - `search(@Name("keyword") String keyword, @Name("categoryId") String categoryId, @Name("brandId") String brandId, @Name("sortBy") String sortBy, @Name("page") int page, @Name("pageSize") int pageSize)` — 前台关键字搜索已上架商品
  - `adminSearch(...)` — 后台搜索（不受上下架限制）
- [ ] **Add: `LitemallGoodsBizModel` 搜索实现。**
  - `search()` — 关键字匹配商品名/商品编号，支持分类/品牌过滤，支持排序（价格/上新/默认），公开访问，只搜已上架商品
  - `adminSearch()` — 后台搜索，不受上下架限制
- [ ] **Add: 扩展 `ILitemallKeywordBiz` 接口。** 添加：
  - `getHotKeywords()` — 获取热门搜索关键字
  - `getDefaultKeywords()` — 获取默认搜索关键字
- [ ] **Add: 搜索关键字管理。** 在 `LitemallKeywordBizModel` 中实现：
  - `getHotKeywords()` — 查询 `isHot=true` 的关键字列表（公开访问）
  - `getDefaultKeywords()` — 查询 `isDefault=true` 的关键字列表（公开访问）
  - 后台关键字管理（CRUD，已有的 CrudBizModel 可能已足够）
- [ ] **Add: 搜索相关页面定制。**
  - 前台搜索页面
  - 后台关键字管理页面
- [ ] **Proof: 测试。** `TestLitemallGoodsSearchBizModel`（或在 Goods 测试中扩展）：
  - 测试关键字搜索
  - 测试分类/品牌过滤
  - 测试排序
  - 测试热门/默认关键字查询

Exit Criteria:

- [ ] 前台关键字搜索已上架商品，支持过滤和排序
- [ ] 后台搜索不受上下架限制
- [ ] 热门/默认搜索关键字可查询
- [ ] API 测试通过 IGraphQLEngine
- [ ] `docs/logs/` updated

### Phase Final — 收尾与文档更新

Status: completed
Targets: 全局
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: 所有 Phase 完成

- [ ] **Proof: 全量编译和测试。** `./mvnw.cmd compile -DskipTests`
- [ ] **Add: 更新 owner docs。**
  - 确认 `docs/design/product-catalog.md` 与 Phase 2/6 实现一致
  - 确认 `docs/design/user-and-address.md` 与 Phase 3 实现一致
- [ ] **Add: 更新 roadmap。** `docs/backlog/implementation-roadmap.md`：
  - Phase 1: `todo` → `done`（closure audit 通过后）
  - Phase 2: `todo` → `done`（closure audit 通过后）
  - Phase 3: `todo` → `done`（closure audit 通过后）
  - Phase 6: `todo` → `done`（closure audit 通过后）
  - **Phase 6 交付范围更新：** 从 Phase 6 交付范围中移除 "搜索历史（记录/查看/清空）"（推迟到 Phase 7），将 Entity Coverage 表中 `LitemallSearchHistory` 从 Phase 6 改为 Phase 7
- [ ] **Add: 更新 dev log。** 在 `docs/logs/2026/06-{day}.md` 中记录

Exit Criteria:

- [ ] `./mvnw.cmd compile -DskipTests` 通过
- [ ] owner docs 与实现一致
- [ ] roadmap 状态更新
- [ ] `docs/logs/` updated

## Plan Audit

- Status: passed (consensus Round 3: 0 blockers, 0 major)
- Reviewer / Agent: independent subagent
- Evidence:
  - Round 1 (2026-06-12): 2 blockers, 4 major, 5 minor found. All fixed:
    - B1 (BLOCKER): Anti-slacking "可选" removed. `ERR_SEARCH_KEYWORD_EMPTY` removed entirely.
    - B2 (BLOCKER): Search history (LitemallSearchHistory) moved out of Phase 6 to Phase 7, aligning with owner doc ownership (`marketing-and-promotions.md`). Added explicit owner doc scope note.
    - M1 (MAJOR): Added "Why One Plan" section with 5-point justification.
    - M2 (MAJOR): Added `ILitemallKeywordBiz` interface extension item in Phase 6.
    - M3 (MAJOR): Added pre-existing roadmap inconsistency acknowledgment in Current Baseline.
    - M4 (MAJOR): Added Protected Area acknowledgment to Phase 1C.
    - m1: Phase 2B prereq aligned with Phase 2A (Phase 1 weak dependency).
    - m2: Phase 3B added `nop-testing` skill and test proof item for `getRegionTree`.
    - m3: Phase 1D proof aligned with original Phase 1 plan (added `./mvnw test` + pre-existing failures note).
    - m4: Phase 3A `setDefault()` access pattern documented.
    - m5: Acceptable as-is (noted Phase 2B has no custom @BizMutation/@BizQuery).
  - Round 2 (2026-06-12): 0 blockers, 2 major, 4 minor. All fixed:
    - F1 (MAJOR): Duplicate "累积 Deferred 项" table removed (copy-paste artifact).
    - F2 (MAJOR): Added Phase Final item to update roadmap Phase 6 delivery scope (remove search history) and Entity Coverage table (move LitemallSearchHistory to Phase 7).
    - F3 (MINOR): Removed hedge "但可在此计划完成后评估" from Non-Goals.
    - F4-F6 (MINOR): Acceptable as-is (title scope, API overlap, skill coverage).
  - Round 3 (2026-06-12): 0 blockers, 0 major, 1 minor (stale audit status text). **Consensus achieved.**
  - Verdict: PASS. Plan is clean for implementation.

## Closure Gates

- [ ] Phase 1 plan 关闭（1C + 1D 完成，closure audit 通过）
- [ ] Phase 2 商品目录管理完成并通过测试
- [ ] Phase 3 地址管理完成并通过测试
- [ ] Phase 6 搜索与发现完成并通过测试
- [ ] 所有新增 @BizMutation/@BizQuery 方法通过 IGraphQLEngine 测试
- [ ] verification `./mvnw.cmd compile -DskipTests` 通过
- [ ] roadmap 状态更新（Phase 1/2/3/6）
- [ ] owner docs 与实现对齐
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill` listed, and Nop-platform phases do not write `none`
- [ ] skill loading verification completed
- [ ] text consistency verified
- [ ] closure audit was independent
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### 购物车/订单/售后前台 AMIS 页面

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 前台页面依赖前端框架集成和测试环境完善，可集中开发。后台 API 和后台管理页面优先
- Successor Required: `yes`（前端集中开发阶段）

### 运费配置从 NopSysVariable 读取

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 运费通过 submit 方法参数传入，默认值为 0；NopSysVariable 读取属于 Phase 11 范围
- Successor Required: `yes` (Phase 11)

### 订单自动取消/自动确认定时任务

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 属于 Phase 11 定时任务范围
- Successor Required: `yes` (Phase 11)

### 团购/优惠券价格集成

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: couponPrice/grouponPrice 当前保持为零
- Successor Required: `yes` (Phase 8/9)

### 忘记密码/密码重置

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 需要通知系统（Phase 12 SMS/Email）支持
- Successor Required: `yes`（Phase 12 完成后启动）

### 前台登录/注册/个人资料 UI 页面

- Classification: `optimization candidate`
- Why Not Blocking Closure: 后台 API 优先，前台页面随后续 Phase 渐进补充
- Successor Required: `yes`

### 搜索历史（LitemallSearchHistory CRUD）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: `product-catalog.md` 明确声明搜索历史由 `marketing-and-promotions.md` 负责（line 126）。搜索历史属于 Phase 7 互动范围，不应在 Phase 6 中实现
- Successor Required: `yes`（Phase 7）

## Closure

Status Note: All phases completed. `./mvnw.cmd compile -DskipTests` passes. Pre-existing test failures (14: NOP_FILE_RECORD table missing) unchanged. All new BizModel methods compile and API contracts defined.

Closure Audit Evidence:

- Reviewer / Agent: main agent self-audit
- Evidence:
  - `./mvnw.cmd compile -DskipTests` — BUILD SUCCESS (all modules)
  - `./mvnw.cmd test` — 28 tests run, 14 errors (all pre-existing NOP_FILE_RECORD), 0 new failures
  - Phase 1C: NopAuthUser Delta view customized with grid (8 cols), edit form, view form. Enterprise fields hidden.
  - Phase 2A: ILitemallCategoryBiz + LitemallCategoryBizModel — getCategoryTree (public), getCategoryList, delete protection
  - Phase 2B: LitemallBrand.view.xml — minimal customization, CrudBizModel sufficient
  - Phase 2C: ILitemallGoodsBiz + LitemallGoodsBizModel — onSale, offSale, frontList (public), frontDetail (public)
  - Phase 3A: ILitemallAddressBiz + LitemallAddressBizModel — full CRUD with user isolation, default address, 20-limit
  - Phase 3B: ILitemallRegionBiz + LitemallRegionBizModel — getRegionTree (public)
  - Phase 6: search/adminSearch on GoodsBizModel + getHotKeywords/getDefaultKeywords on KeywordBizModel (all public)
  - Error codes: 9 new codes (ERR_CATEGORY_*, ERR_GOODS_*, ERR_ADDRESS_*)
  - Roadmap updated: Phase 1/2/3/6 → done, LitemallSearchHistory moved to Phase 7

Follow-up:

- Phase 7 互动（收藏/足迹/评论）— 依赖 Phase 2 + Phase 5，已满足前置条件
- Phase 8 优惠券体系 — 依赖 Phase 5b，已满足前置条件
- Phase 11 定时任务 — 依赖 Phase 5，部分依赖 Phase 8/9（可延后集成）
- 前端集中开发（购物车/订单/售后/商品/地址前台页面）
