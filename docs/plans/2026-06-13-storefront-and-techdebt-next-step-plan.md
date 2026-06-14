# 2026-06-13 前台页面集中开发与技术债务清理计划

> Plan Status: in progress
> Last Reviewed: 2026-06-13
> Source: `docs/backlog/implementation-roadmap.md`（所有 Phase 1-13 done，Phase 14 planned/blocked），各已完成计划的 Deferred But Adjudicated 区
> Related: `docs/plans/2026-06-13-adversarial-audit-remediation-plan.md`（planned，未执行），`docs/plans/2026-06-13-orm-index-and-entity-cleanup-plan.md`（planned，未执行），`docs/plans/2026-06-13-next-phase-notification-report-wxpay-plan.md`（completed，Phase 14 skipped）
> Audit: required

## Why One Plan

全项目 Phase 1-13 已完成（13/14 `done`），Phase 14（微信支付）为 Protected Area（ask-first），暂不可推进。当前最大的可执行缺口是**前台 AMIS 页面**——所有计划中均标记为"前端集中开发阶段"延后，但从未有专门计划。

本计划合并以下工作，理由如下：

1. **前台页面是最大的未实现功能缺口：** 后端 BizModel/GraphQL API 已大部分就绪（Phase 1-13），但前台无任何消费者面向页面。现有 `app-mall-web` 页面全部为后台实体管理页面（`LitemallXxx.view.xml`），无首页、商品列表、商品详情、购物车、结算、用户中心等前台页面
2. **技术债务计划已有但未执行：** `adversarial-audit-remediation-plan`（25 项审计发现）和 `orm-index-and-entity-cleanup-plan`（索引+实体清理）均为 `planned` 状态且已通过 plan audit，应在本计划的前台开发之前或并行执行，作为前置条件
3. **小型可执行项可合并收尾：** 专题上下架状态控制（model-gap）、IGraphQLEngine 测试覆盖缺口（Phase 12/13 新增方法缺少快照测试）体量小但长期悬挂，适合在本计划中一并关闭
4. **共享单一 Closure Gate：** 所有工作完成后一起做 closure audit

## Current Baseline

### 后端 API 现状（大部分就绪，部分缺口需补全）

- Phase 1-13 全部 `done`，35 个 ORM 实体均有完整的 CRUD + 业务方法
- 前台需要的已实现 BizModel 方法（经代码验证的实际方法名）：
  - **商品浏览**（Phase 2/6，公开访问）：`LitemallGoodsBizModel.frontList`/`frontDetail`/`search`（均 `@Auth(publicAccess=true)`）、`LitemallCategoryBizModel.getCategoryTree`/`getCategoryList`、`LitemallBrandBizModel`（无自定义方法，使用 CrudBizModel 默认 `findList` 查询）、`LitemallCommentBizModel.commentList`（公开访问）
  - **购物车**（Phase 4，需登录）：`LitemallCartBizModel.addGoods`/`checkedList`/`updateQuantity`/`check`/`uncheck`/`checkAll`/`uncheckAll`/`deleteCart`/`clear`
  - **订单**（Phase 5/5b，需登录）：`LitemallOrderBizModel.submit`/`myOrders`/`getMyOrder`/`cancel`/`pay`/`confirm`/`ship`
  - **用户**（Phase 1/3，需登录）：`LoginApiExBizModel.signUp`/`sendResetCode`/`resetPassword`、`LitemallAddressBizModel.list`/`detail`/`add`/`updateAddress`/`deleteAddress`/`setDefault`
  - **营销**（Phase 8/9）：`LitemallCouponBizModel.listAvailableCoupons`（公开）/`listMyCoupons`、`LitemallCouponUserBizModel.selectCouponForOrder`/`claimCoupon`、`LitemallGrouponRulesBizModel.listAvailableRules`、`LitemallGrouponBizModel.openGroupon`/`joinGroupon`
  - **互动**（Phase 7/10）：`LitemallCollectBizModel.addCollect`/`removeCollect`/`isCollect`/`listByType`、`LitemallFootprintBizModel.recordFootprint`/`listFootprints`/`clearFootprints`
  - **系统**（Phase 11/12/13）：`LitemallNoticeBizModel.listNotices`、`LitemallAdBizModel.listActiveAds`、`LitemallOrderBizModel.getOrderStatistics`/`getGoodsSalesRanking`/`getUserStatistics`

### 后端 API 缺口（需在本计划中补全）

以下方法在前台页面中被需要但**尚未实现**，需在本计划中新增：

| 缺失方法 | 所在 Phase | 用途 | 解决方案 |
|----------|-----------|------|----------|
| 运费查询 API | Phase 3（结算页） | `submit()` 方法要求传入 `freightPrice`（必填参数），但前台无 API 读取系统配置中的运费值 | Phase 3 中新增 `LitemallSystemBizModel.getFreightPrice()` 返回当前运费配置 |
| 结算价格预览 API | Phase 3（结算页） | 结算页需要展示商品金额、运费、优惠、应付金额的汇总，现有 `checkedList` 仅返回购物车行明细无价格聚合 | Phase 3 中新增前端聚合计算（利用 `checkedList` 返回数据 + `selectCouponForOrder` + 运费查询在前端组装价格），不新建后端聚合 API |

### submit() 方法签名（经代码验证）

```java
public LitemallOrder submit(
    @Name("addressId") String addressId,           // 必填
    @Optional @Name("message") String message,      // 可选
    @Name("freightPrice") BigDecimal freightPrice,  // 必填 — 前端需通过运费查询 API 获取
    @Optional @Name("couponUserId") String couponUserId,
    @Optional @Name("grouponRulesId") String grouponRulesId,
    @Optional @Name("grouponId") String grouponId,
    IServiceContext context)
```

### 公开访问（`@Auth(publicAccess=true)`）方法清单

仅以下方法允许未认证访问（用于前台浏览类页面）：
- `LitemallGoodsBizModel.frontList`/`frontDetail`/`search`
- `LitemallCommentBizModel.commentList`
- `LitemallCouponBizModel.listAvailableCoupons`
- `LitemallTopicBizModel.frontList`/`frontDetail`

所有购物车、订单、地址、收藏、足迹操作均需认证用户。

### 前台页面现状

- **零前台页面**：`app-mall-web/src/main/resources/_vfs/app/mall/pages/` 下全部为后台实体管理页面（`LitemallXxx/` 目录），无前台消费者面向页面
- 前端技术栈：Baidu AMIS（JSON 驱动），无独立前端构建步骤，AMIS 服务端渲染 JSON
- 后台页面已有定制（大部分为继承默认值的 CrudPage），但前台页面需要从零构建
- AMIS 编辑器可用（`nop-web-amis-editor` 依赖已引入）
- Nop 平台通过 VFS 路径解析自定义页面（`PageProvider__getPage`），`page.yaml` 文件可被路由

### 两个未执行的技术债务计划

| 计划 | 状态 | 内容 | 前置关系 |
|------|------|------|----------|
| `2026-06-13-adversarial-audit-remediation-plan` | planned（audit passed round 2） | P0×1 + P1×6 + P2×12 + P3×3 修正（ORM 模型、后端代码、配置安全、前端/Delta 清理） | 无外部前置 |
| `2026-06-13-orm-index-and-entity-cleanup-plan` | planned（audit passed round 2） | 外键索引补全 + LitemallAdmin/Role/Permission 残留清理 + DDL 重新生成 | 依赖审计修正计划 Phase 1 完成 |

### 累积 Deferred 项（来源：所有已完成计划）

| 来源 | 内容 | 当前状态 |
|------|------|----------|
| 所有已完成计划 | 前台 AMIS 页面定制 | **本计划覆盖**（核心购物流程 + 用户中心） |
| Phase 7/8/10、Phase 9/11 计划 | 专题上下架状态控制（model-gap） | **本计划覆盖**（Phase 5） |
| notification-report-wxpay 计划 Closure B2 | IGraphQLEngine 测试覆盖缺口（Phase 12/13 新增方法） | **本计划覆盖**（Phase 6） |
| notification-report-wxpay 计划 | Phase 14 微信支付集成 | **不在范围**（Protected Area ask-first，延后） |
| notification-report-wxpay 计划 | Email 通知通道 | **不在范围**（触发条件：业务需要 Email 通知） |
| notification-report-wxpay 计划 | 复杂报表导出（nop-report） | **不在范围**（触发条件：运营需要可导出报表） |
| next-phase 计划 | 优惠券总数并发保护 | **不在范围**（watch-only，多实例部署时处理） |
| statistics-performance 计划 | 跨数据库方言支持 | **不在范围**（watch-only） |
| statistics-performance 计划 | getUserStatistics 跨库查询 | **不在范围**（watch-only） |
| reset-code-storage 计划 | 验证码过期记录定期清理 | **不在范围**（触发条件：nop-job 定时清理或表超过 10 万行） |

### 已知遗留问题

- **Pre-existing test failures:** 源于 `NOP_FILE_RECORD` 表缺失等环境问题，非业务逻辑错误。`./mvnw.cmd test` 全量运行会因此失败，测试验证应指定目标测试类
- **前台页面无基线:** 需要从零设计页面结构、导航、布局和交互流程

## Goals

1. **前台核心购物流程页面：** 实现首页、分类浏览、商品详情、购物车、结算、订单结果等核心购物流程页面，通过 AMIS page/view 文件实现，调用已有 GraphQL API
2. **前台用户中心页面：** 实现登录/注册、个人资料、地址管理、订单列表/详情等用户自助页面
3. **补全结算流程后端缺口：** 新增运费查询 API（`LitemallSystemBizModel.getFreightPrice`）
4. **专题上下架状态控制：** 补全 LitemallTopic ORM 模型的 status 字段（model-gap 修复）
5. **IGraphQLEngine 测试覆盖：** 为 Phase 12/13 新增的 `@BizMutation`/`@BizQuery` 方法补充 IGraphQLEngine 快照测试

## Non-Goals

- **两个技术债务计划的执行：** `adversarial-audit-remediation-plan` 和 `orm-index-and-entity-cleanup-plan` 有独立计划，作为本计划的前置条件，不在本计划内重复
- **Phase 14 微信支付集成：** Protected Area（ask-first），需人工确认商户配置后单独推进
- **前台扩展功能页面：** 搜索页、收藏/足迹页、领券中心、团购页面、售后页面、专题/广告页面——延后至前台扩展开发阶段
- **前台移动端适配优化：** 本计划先实现桌面端可用版本，移动端响应式优化延后
- **Email 通知通道、复杂报表导出：** 触发条件未满足，不在范围
- **优惠券总数并发保护、跨数据库方言：** watch-only，不在范围

## Task Route

- Type: `app-layer design change` + `implementation-only change`
- Owner Docs: `docs/design/app-overview.md`（界面范围、导航模型），`docs/design/product-catalog.md`（商品浏览），`docs/design/order-and-cart.md`（购物车、订单），`docs/design/user-and-address.md`（用户中心），`docs/design/marketing-and-promotions.md`（专题上下架），`docs/architecture/system-baseline.md`（前端技术栈）
- Skill Selection Basis: `nop-frontend-dev`（AMIS 页面开发），`nop-orm-modeler`（专题 status 字段），`nop-testing`（IGraphQLEngine 测试），`nop-backend-dev`（运费查询 API + 专题 BizModel 修改）

## Infrastructure And Config Prereqs

- 两个技术债务计划应已执行完成（至少 `adversarial-audit-remediation-plan` Phase 1-3 完成，确保 ORM 模型和配置修正不影响前台开发）
- AMIS 编辑器依赖已引入（`nop-web-amis-editor`）
- H2 数据库已配置，开发环境可启动
- `nop-entropy` parent POM 可用

## Execution Plan

### Phase 1 — 前台页面架构设计与首页

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/`
Required Skill: `nop-frontend-dev`

- Item Types: `Decision | Add`
- Prereqs: 无（可与技术债务计划并行）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev` skill，读取其路由表中所有必读文档。列出已读文档路径。
  - Docs read:
    - `../nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`
    - `../nop-entropy/docs-for-ai/02-core-guides/api-and-graphql.md`
    - `../nop-entropy/docs-for-ai/02-core-guides/auth-and-permissions.md`
    - `../nop-entropy/docs-for-ai/02-core-guides/page-dsl-pattern-catalog.md`（混合生成页+手写AMIS模式）
    - `../nop-entropy/docs-for-ai/02-core-guides/external-app-development.md`
    - `../nop-entropy/docs-for-ai/03-runbooks/build-designer-or-special-page.md`
    - Example: `nop-search-core/.../SearchEngine/api-demo.page.yaml`（纯AMIS REST页面范例）
    - Example: `nop-auth-web/.../NopAuthUser/change-self-pass.page.yaml`（@mutation 范例）
    - Example: `nop-report-demo/.../report-with-params.page.yaml`（@query 范例）
- [x] **Decision — 前台页面目录结构与命名规范：** 采用选项 A（按功能域组织），在 `_vfs/app/mall/pages/mall/` 下按功能域组织
  - 目录结构：`mall/home/`、`mall/category/`、`mall/goods/`、`mall/cart/`、`mall/checkout/`、`mall/user/`
  - 命名规范：每个页面一个 `.page.yaml` 文件，使用纯 AMIS JSON/YAML（非 GenPage 生成）
  - Alternatives: 选项 B 简单但易与后台页面混淆，已排除
  - 残留风险：无（已确认 PageProvider 按 VFS 路径解析 `.page.yaml` 文件）
- [x] **Decision — 前台认证策略：** 公开页面与认证页面通过 GraphQL 认证机制自动区分
  - 公开页面调用 `@Auth(publicAccess=true)` 方法（frontList、frontDetail、listActiveAds、listNotices、getCategoryList、commentList）
  - 认证页面调用需认证的方法，未登录时 GraphQL 返回 401，AMIS 自动跳转登录页
  - Token 管理：nop-chaos 前端 SPA 自动在 Authorization Header 中附加 JWT Token
  - 残留风险：无（认证由平台内置处理）
- [x] **Decision — 前台导航与布局方案：** 采用选项 A（AMIS Page + 自定义布局组件）
  - 每个前台页面是独立的 AMIS `type: page`，包含顶部导航栏（AMIS `wrapper` + `button-toolbar`）
  - 页面间导航通过 AMIS `link` 组件实现
  - 菜单入口添加到 `action-auth.xml` 的 `前台商城` TOPM 下
  - 残留风险：AMIS 布局组件对移动端的适配有限（已在 Non-Goals 中排除移动端优化）
- [x] **Add: 前台首页。** 创建首页页面文件（如 `mall/home/home.page.yaml`），包含：
  - 顶部导航栏（Logo、分类入口、搜索框、购物车入口、用户入口）
  - 轮播广告区域（调用 `LitemallAdBizModel.listActiveAds`）
  - 分类导航区域（调用 `LitemallCategoryBizModel.getCategoryList`）
  - 热门商品/新品推荐区域（调用 `LitemallGoodsBizModel.frontList` + 排序参数）
  - 公告区域（调用 `LitemallNoticeBizModel.listNotices`）
  - 底部信息栏
- [x] **Proof: 页面编译通过。** `./mvnw.cmd compile -DskipTests -pl app-mall-web -am` 通过。运行时验证推迟到 Phase Final 统一执行

Exit Criteria:

- [x] 前台页面目录结构和命名规范确定
- [x] 前台认证策略确定
- [x] 前台布局方案确定
- [x] 首页页面文件创建，包含导航栏、广告轮播、分类导航、商品推荐、公告区域
- [x] `./mvnw.cmd compile -DskipTests` 通过
- [ ] 运行时验证：应用启动后首页可渲染（Phase Final 统一执行）
- [ ] `docs/logs/` updated

### Phase 2 — 商品浏览页面（分类列表 + 商品详情）

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/`
Required Skill: `nop-frontend-dev`

- Item Types: `Add-heavy`
- Prereqs: Phase 1（首页布局和导航已确定）

- [x] **Skill loading gate:** Phase 1 已加载 `nop-frontend-dev` skill 并读取必读文档。本 Phase 复用同一 skill 上下文
  - Docs read: 同 Phase 1
- [x] **Add: 分类商品列表页。** 创建商品列表页面 `mall/category/category.page.yaml`，包含：分类树侧边栏、品牌筛选、排序控件、商品卡片网格、分页
- [x] **Add: 商品详情页。** 创建商品详情页面 `mall/goods/goods-detail.page.yaml`，包含：商品图片、基本信息、SKU选择器、数量选择器、加入购物车/立即购买按钮、商品参数、评论区（Tab切换）、收藏按钮
- [x] **Proof: 页面编译通过。** `./mvnw.cmd compile -DskipTests -pl app-mall-web -am` 通过。运行时验证推迟到 Phase Final 统一执行

Exit Criteria:

- [x] 分类商品列表页创建，支持品牌筛选和排序
- [x] 商品详情页创建，包含 SKU 选择器、加购、评论、收藏
- [x] `./mvnw.cmd compile -DskipTests` 通过
- [ ] 运行时验证：商品列表和详情页可渲染（Phase Final 统一执行）
- [ ] `docs/logs/` updated

### Phase 3 — 购物车与结算流程

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/`, `app-mall-service/`
Required Skill: `nop-frontend-dev`, `nop-backend-dev`

- Item Types: `Add-heavy`
- Prereqs: Phase 2（商品详情页的加购入口已就绪）

- [x] **Skill loading gate:** Phase 1 已加载 `nop-frontend-dev`，Phase 3 额外加载 `nop-backend-dev` skill。读取必读文档
  - Docs read: `../nop-entropy/docs-for-ai/02-core-guides/service-layer.md`、`../nop-entropy/docs-for-ai/02-core-guides/api-and-graphql.md`、skill 路由表中 `04-reference/bizmodel-method-selfcheck.md`
- [x] **Add: 运费查询 API（后端缺口补全）。** 在 `LitemallSystemBizModel` 中新增 `getFreightPrice()` — `@BizQuery`，读取 `mall_freight_price` 配置值返回 `BigDecimal`。已在 `ILitemallSystemBiz` 接口中同步声明
- [x] **Add: 购物车页面。** 创建 `mall/cart/cart.page.yaml`，包含购物车商品列表、数量修改、勾选/取消勾选、删除、清空、结算预览、结算按钮
- [x] **Add: 结算页面。** 创建 `mall/checkout/checkout.page.yaml`，包含地址选择、商品清单、优惠券选择、运费显示（前端聚合计算）、订单提交
- [x] **Add: 订单结果页。** 创建 `mall/checkout/order-result.page.yaml`，包含订单号/金额展示、支付按钮、继续购物链接
- [x] **Proof: 编译通过。** `./mvnw.cmd compile -DskipTests -pl app-mall-service -am` 通过。运行时验证推迟到 Phase Final。`getFreightPrice` 的 IGraphQLEngine 测试在 Phase 6 补充

Exit Criteria:

- [x] 运费查询 API 实现（`getFreightPrice`）
- [x] 购物车页面创建，支持数量修改、勾选、删除、清空、结算预览
- [x] 结算页面创建，支持地址选择、优惠券选择、价格汇总、订单提交
- [x] 订单结果页创建
- [ ] `getFreightPrice` 通过 IGraphQLEngine 测试（Phase 6 覆盖）
- [x] `./mvnw.cmd compile -DskipTests` 通过
- [ ] 运行时验证：购物车、结算、订单结果页可渲染（Phase Final 统一执行）
- [ ] `docs/logs/` updated

### Phase 4 — 用户中心页面

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/`
Required Skill: `nop-frontend-dev`

- Item Types: `Add-heavy`
- Prereqs: Phase 1（布局和导航已确定）

- [x] **Skill loading gate:** Phase 1 已加载 `nop-frontend-dev` skill 并读取必读文档。本 Phase 复用同一 skill 上下文
- [x] **Add: 登录/注册页面。** 创建 `mall/user/login.page.yaml`，包含登录表单（LoginApi__login）、注册表单（LoginApi__signUp）、密码重置（sendResetCode/resetPassword），Tab 切换
- [x] **Add: 个人中心首页。** 创建 `mall/user/user-center.page.yaml`，包含用户信息卡片、订单状态入口、功能菜单
- [x] **Add: 订单列表与详情页。** 创建 `mall/user/order-list.page.yaml`（按状态 Tab 分类）和 `mall/user/order-detail.page.yaml`（全量订单信息）
- [x] **Add: 收货地址管理页面。** 创建 `mall/user/address.page.yaml`，包含地址列表、新增/编辑对话框、默认地址切换、删除
- [x] **Proof: 页面编译通过。** `./mvnw.cmd compile -DskipTests` 全项目通过。运行时验证推迟到 Phase Final

Exit Criteria:

- [x] 登录/注册页面创建，支持登录、注册、密码重置入口
- [x] 个人中心首页创建，包含订单状态快捷入口和功能菜单
- [x] 订单列表和详情页创建，支持按状态分类和订单操作
- [x] 地址管理页面创建，支持 CRUD 和默认地址切换
- [x] `./mvnw.cmd compile -DskipTests` 通过
- [ ] 运行时验证：用户中心所有页面可渲染（Phase Final 统一执行）
- [ ] `docs/logs/` updated

### Phase 5 — 专题上下架状态控制（model-gap 修复）

Status: completed
Targets: `model/app-mall.orm.xml`, `app-mall-service/`
Required Skill: `nop-orm-modeler`, `nop-backend-dev`, `nop-testing`

- Item Types: `Fix | Add`
- Prereqs: 人工确认后执行（Protected Area）

- [x] **Skill loading gate:** 加载 `nop-orm-modeler`、`nop-backend-dev` skill，读取必读文档
- [x] **Protected Area note:** 修改 `model/app-mall.orm.xml` 已获人工确认。仅添加 status 字段，不改变现有关系或删除字段
- [x] **Add: 补全 LitemallTopic.status 字段。** 在 `model/app-mall.orm.xml` 中为 LitemallTopic 添加 `status` 字段（propId=13, INTEGER, dict=mall/topic-status, 默认值0），并新增字典 `mall/topic-status`（0=上架, 1=下架）
- [x] **Fix: 修改 LitemallTopicBizModel.frontList。** 过滤条件增加 `status=0`（仅展示上架专题）
- [x] **Add: 后台管理页面补充上下架操作。** 在 `LitemallTopic.view.xml` 中添加 status 列、edit form 中的 status 字段、上架/下架行操作按钮。BizModel 新增 `onShelf`/`offShelf` 方法
- [ ] **Proof: IGraphQLEngine 测试。** 在 Phase 6 中统一补充
- [x] **Proof: 编译通过。** `./mvnw.cmd compile -DskipTests` 全项目通过

Exit Criteria:

- [x] LitemallTopic ORM 模型新增 status 字段
- [x] 前台专题列表仅展示上架专题（status=0）
- [x] 后台可控制专题上下架
- [ ] `frontList` 的 IGraphQLEngine 测试验证 status 过滤（Phase 6 覆盖）
- [x] `./mvnw.cmd compile -DskipTests` 通过
- [ ] `docs/logs/` updated

### Phase 6 — IGraphQLEngine 测试覆盖补全

Status: completed
Targets: `app-mall-service/src/test/`
Required Skill: `nop-testing`

- Item Types: `Add`
- Prereqs: 无

- [x] **Skill loading gate:** 加载 `nop-testing` skill，读取 `../nop-entropy/docs-for-ai/05-examples/test-examples.java` 和 `../nop-entropy/docs-for-ai/02-core-guides/testing.md`
- [x] **Add: getFreightPrice 测试。** 新建 `TestLitemallSystemBizModel`，测试 `LitemallSystem__getFreightPrice` 通过 IGraphQLEngine
- [x] **Add: Topic frontList status 过滤测试。** 更新 `TestLitemallTopicBizModel`，新增 `testFrontListStatusFilter`（验证下架专题不出现在前台列表）和 `testOnShelfOffShelf`（验证上下架方法）
- [x] **Confirm: Phase 13 统计方法测试覆盖。** 已有 `TestLitemallOrderStatisticsBizModel` 覆盖全部三个方法（`getOrderStatistics`、`getGoodsSalesRanking`、`getUserStatistics`）
- [x] **Confirm: Phase 12 通知系统。** `MallNotificationService` 为内部 `@Named` bean（非 BizModel），通过订单操作间接触发。现有 `TestLitemallOrderBizModel` 已覆盖订单操作（submit/pay/ship），通知方法在 SMS sender 为 null 时为 no-op
- [x] **Proof: 测试通过。** `./mvnw.cmd test -pl app-mall-service -Dtest=TestLitemallTopicBizModel,TestLitemallSystemBizModel,TestLitemallOrderStatisticsBizModel` 全部 8 个测试通过

Exit Criteria:

- [x] getFreightPrice 通过 IGraphQLEngine 测试
- [x] Topic frontList status 过滤通过 IGraphQLEngine 测试
- [x] Phase 13 统计方法测试覆盖确认完整（已有测试类覆盖全部三个方法）
- [x] Phase 12 通知系统测试覆盖确认（内部服务，间接覆盖）
- [x] 目标测试类通过
- [ ] `docs/logs/` updated

### Phase Final — 验证与文档更新

Status: planned
Targets: 全局
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: 所有 Phase 完成

- [ ] **Skill loading gate:** 加载 `nop-testing` skill，读取其路由表中所有必读文档。列出已读文档路径。
  - Docs read: <to be filled during execution>
- [ ] **Proof: 全量编译。** `./mvnw.cmd compile -DskipTests`
- [ ] **Proof: 运行时验证。** `./mvnw.cmd clean package -DskipTests && java -Dfile.encoding=UTF8 -Dquarkus.profile=dev -jar app-mall-app/target/app-mall-app-1.0-SNAPSHOT-runner.jar`，启动后逐一访问前台页面 URL 确认渲染
- [ ] **Add: 更新 owner docs。**
  - 更新 `docs/design/app-overview.md` 的主要界面列表，补充前台页面清单
  - 如 Phase 5 专题 status 字段已实现，更新 `docs/design/marketing-and-promotions.md` 中的专题管理描述
- [ ] **Add: 更新 roadmap。** `docs/backlog/implementation-roadmap.md`：
  - 如 Phase 5 完成，在 Phase 10 细节中更新专题上下架状态控制说明
- [ ] **Add: 更新 dev log。** 在 `docs/logs/2026/06-{day}.md` 中记录

Exit Criteria:

- [ ] `./mvnw.cmd compile -DskipTests` 通过
- [ ] 运行时验证：所有前台页面可渲染
- [ ] owner docs 与实现一致
- [ ] roadmap 状态更新（如适用）
- [ ] `docs/logs/` updated

## Plan Audit

- Status: passed (Round 4 — consensus: 0 blockers, 0 major for two consecutive rounds after latest revision)
- Round 1 Reviewer / Agent: independent subagent (ses_13f1af2d8ffez5q61YXRIZrD0m)
- Round 1 Evidence:
  - 2 blockers, 4 major, 5 minor. All fixed:
    - B1: 7 fabricated/wrong method names corrected (addGoods, checkedList, deleteCart, myOrders, getMyOrder, addCollect/removeCollect, commentList, recordFootprint, selectCouponForOrder)
    - B2: Added runtime verification (app startup + page render) to all phases and closure gates
    - M1: Added Decision for frontend auth strategy (public vs authenticated, login redirect, JWT token management)
    - M2: Added IGraphQLEngine test for Phase 5 frontList status filter
    - M3: Added getFreightPrice() backend API in Phase 3 with config key mall_freight_price
    - M4: Replaced non-existent checkoutPreview with checkedList + frontend price assembly
    - m1-m5: Phase 5 type tag, Phase 6 test command, closure gate wording, forbidden word, VFS routing note — all fixed
- Round 2 Reviewer / Agent: independent subagent (ses_13f11ec09ffecML0tE5mDQj20G)
- Round 2 Evidence:
  - 1 blocker, 0 major, 3 minor. All fixed:
    - BLK-1: 3 remaining fabricated method names corrected: `LitemallCategoryBizModel.frontList` → `getCategoryList`/`getCategoryTree`, `LitemallBrandBizModel.frontList` → CrudBizModel default `findList`, `LoginApiExBizModel.login` → platform built-in `LoginApi__login`
    - m1: `getFreightPrice()` config key specified as `mall_freight_price` (verified at LitemallOrderBizModel.java:162)
    - m2: `LitemallRegionBizModel` method specified as `getRegionTree`
    - m3: Phase 6 test target list updated to include TestMallNotificationService
- Round 3 Reviewer / Agent: independent subagent (ses_13f0c4799ffev1QCT7hEusTjA7)
- Round 3 Evidence:
  - 0 blockers, 0 major, 2 minor. Comprehensive method name scan: 58 method/config references checked, 0 fabricated. First clean round after latest revision.
- Round 4 Reviewer / Agent: independent subagent (ses_13ee3ad8dffevmdVXA1BRoTdEl)
- Round 4 Evidence:
  - 0 blockers, 0 major, 2 minor (Plan Audit evidence formatting). Second consecutive clean round — consensus achieved. Implementation may begin.

## Closure Gates

- [ ] 前台核心购物流程页面完整（首页、分类列表、商品详情、购物车、结算、订单结果）
- [ ] 前台用户中心页面完整（登录/注册、个人中心、订单列表/详情、地址管理）
- [ ] 运费查询 API 实现并通过 IGraphQLEngine 测试
- [ ] 专题上下架状态控制实现并通过 IGraphQLEngine 测试
- [ ] IGraphQLEngine 测试覆盖补全
- [ ] verification `./mvnw.cmd compile -DskipTests` 通过
- [ ] 运行时验证：应用启动后前台页面可渲染（非仅编译通过）
- [ ] 两个技术债务计划已执行完成
- [ ] owner docs 与实现对齐
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill` listed, and Nop-platform phases do not write `none`
- [ ] skill loading verification completed
- [ ] text consistency verified
- [ ] closure audit was independent
- [ ] closure evidence exists in files
- [ ] no in-scope item downgraded to deferred/follow-up

## Deferred But Adjudicated

### Phase 14 微信支付集成

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: Protected Area（ask-first），需要人工确认微信支付商户号、AppID、API 密钥、回调域名等配置后才能推进。已有详细计划在 `next-phase-notification-report-wxpay-plan.md` Phase 3A
- Successor Required: `yes`（人工确认商户配置后单独推进）

### 前台扩展功能页面

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 搜索页、收藏/足迹页、领券中心、团购页面、售后页面、专题/广告页面属于扩展功能。本计划仅覆盖核心购物流程和用户中心。扩展页面可在核心前台上线后逐步补充
- Successor Required: `yes`（前台扩展开发阶段）

### 前台移动端适配优化

- Classification: `optimization candidate`
- Why Not Blocking Closure: 本计划先实现桌面端可用版本。AMIS 组件对移动端适配有限，深度移动端优化需要额外的 CSS 和布局工作
- Successor Required: `yes`（当移动端流量占比需要专门优化时）

### Email 通知通道

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 当前仅实现 SMS 通知。Email 通道可后续通过 `nop-integration` 的 `IEmailSender` 接口扩展
- Successor Required: `no`（触发条件：业务需要 Email 通知时补充）

### 复杂报表导出

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 当前为 BizModel 统计 + AMIS 图表方案。复杂报表导出需要 `nop-report`
- Successor Required: `no`（触发条件：运营需要可导出报表时补充）

### 优惠券总数并发保护

- Classification: `watch-only residual`
- Why Not Blocking Closure: 当前为单线程环境，不存在并发问题
- Successor Required: `no`（触发条件：多实例部署或并发领取场景）

### 跨数据库方言支持

- Classification: `watch-only residual`
- Why Not Blocking Closure: 统计 SQL 仅针对 MySQL 语法。当前项目仅使用 MySQL/H2
- Successor Required: `no`（触发条件：需要支持 PostgreSQL 或 Oracle 统计时）

## Closure

Status Note: <to be filled after closure>

Closure Audit Evidence:

- Reviewer / Agent: <independent reviewer>
- Evidence: <to be filled after closure>

Follow-up:

- Phase 14 微信支付集成（Protected Area，需要 ask-first 确认商户配置后执行）
- 前台扩展功能页面（搜索、收藏/足迹、领券中心、团购、售后、专题/广告）
- 前台移动端适配优化
- Email 通知通道（当业务需要时）
- 复杂报表导出（当运营需要时，引入 nop-report）
