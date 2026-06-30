# mobile-m3 商品详情 & 购物车

> Plan Status: active
> Last Reviewed: 2026-06-30
> Source: `docs/backlog/mobile-frontend-roadmap.md` Mobile Phase 3；`docs/analysis/2026-06-21-mobile-mall-functional-design-analysis.md`（SKU 强制选择、服务端购物车、数量=1 减号变删除）
> Related: 前置 `2026-06-30-1343-1-mobile-m1-scaffold-infra-plan.md`（M1）+ `2026-06-30-1343-2-mobile-m2-home-category-plan.md`（M2，提供商品列表入口）；后续 M4（结算/订单）消费本计划购物车结算入口
> Audit: required

## Current Baseline

**前置（M1+M2 交付后成立）：**
- app 脚手架/auth/路由/Tab/全局状态就绪（M1）；首页/分类/搜索/品牌可浏览并产出商品列表入口（M2）。

**后端 API（已 done，本计划消费）：**
- 商品详情：`LitemallGoods` 详情（含 gallery、SKU `LitemallGoodsProduct` 规格/库存/价格、详情富文本）。
- 购物车 CRUD：`LitemallCart`（add/update/delete/list，服务端购物车，按 userId + goodsId + productId 维度）。
- 收藏：`LitemallCollect`（add/list/delete）。
- 足迹：`LitemallFootprint`（add/list/delete，浏览商品详情时记录）。
- **评论摘要：** `LitemallCommentBizModel.getCommentSummary(type,valueId)`（`@BizQuery @Auth(publicAccess=true)`，`:219-265`）返回 `totalCount`/`goodRate`/`starDistribution`/`prosTags`/`consTags`；`commentList`（`:191-216`）亦公开。**结构化评论后端已就绪**（`pros`/`cons`/`semanticRating` 字段 + summary 聚合均存在）；本计划 Phase 1 消费基础摘要（总数/星级/好评率），结构化展示 UI（标签云/有图筛选/优缺点表单）归 M9。

**移动端组件就绪：** `flux-renderers-mobile` swipe-cell（左滑删除，购物车行）、countdown（如需促销倒计时，本计划范围外）、flux `dialog`/`drawer`（SKU 选择弹层）。

**Gap：** 商品详情/购物车/收藏/足迹均为零实现；M2 商品列表点击无落点。

## Goals

- 商品详情页：图片轮播 + 价格 + SKU 规格选择（强制选齐再加购）+ 评价摘要 + 详情图文。
- 收藏 / 取消收藏；加入购物车；浏览即记足迹。
- 购物车页：列表 / 勾选 / 数量调整 / 左滑删除 / 清空 / 结算入口（跳转结算属 M4，本计划止于入口）。

## Non-Goals

- 结算页 / 下单 / 订单 —— M4。
- 结构化评价（优点/缺点/标签云/好评率）—— M9（依赖增强后端）。
- 营销价/限时折扣/秒杀在详情页的横幅 —— M7。
- 购物车营销价拼接 —— M9。
- 后端改动 —— 纯消费。

## Task Route

- Type: `implementation-only change`（业务设计已在 `docs/design/product-catalog.md`、`docs/design/order-and-cart.md` 落地）
- Owner Docs: `docs/design/product-catalog.md`（商品/SKU/收藏/足迹/评价）、`docs/design/order-and-cart.md`（购物车语义与边界）、`docs/backlog/mobile-frontend-roadmap.md`（M3 范围）
- Skill Selection Basis: 见各 phase。总体为 nop-chaos-flux 移动端，非 AMIS。

## Infrastructure And Config Prereqs

- M1+M2 已交付；本地后端运行。
- 无新增密钥/迁移。

## Execution Plan

### Phase 1 - 商品详情页（轮播 + 价格 + SKU 选择 + 评价摘要 + 详情图文）

Status: planned
Targets: `src/pages/goods/detail/`
Required Skill: `none`（nop-chaos-flux 移动端，非 AMIS；`nop-frontend-dev` 触发词 view.xml/AMIS 不匹配。方法源：`flux-guide/01-quickstart.md` §5 dialog/§8 action、`flux-guide/design-patterns/form.md`、`flux-guide/design-patterns/conditional.md` SKU 显隐、`docs/design/product-catalog.md` SKU 语义）

- Item Types: `Add | Decision`
- Prereqs: M2 完成（商品列表入口）

- [ ] **Skill loading gate:** 通读 `flux-guide/01-quickstart.md` §5（dialog 弹层，SKU 选择用 drawer/dialog surface）+ §8（action 链）+ §7（条件显隐）、`flux-guide/design-patterns/conditional.md`（SKU 规格组合显隐/禁售）、`flux-guide/design-patterns/form.md`；复阅 `docs/design/product-catalog.md`（SKU/规格/库存/评价）。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Decision D1（SKU 选择交互）：** 抉择——底部 drawer 弹层选择规格，规格未选齐时「加入购物车/立即购买」禁用并提示（对齐设计分析 §2.1 芋道「加车前强制选规格」）。备选（详情页内联展开）——否决理由：多规格内联占屏过大，drawer 是移动端标配。残留风险：SKU 规格组合可用性需前端据 `LitemallGoodsProduct` 规格矩阵计算（无库存组合置灰）。
- [ ] **Add:** 商品详情页 schema：gallery 轮播 + 价格/划线价 + 标题 + SKU 选择 drawer（规格矩阵 → 选中 product，查库存）+ 评价摘要（消费公开 `getCommentSummary`：总数/好评率/星级分布）+ 详情富文本。
- [ ] **Proof:** vitest（SKU 矩阵 → product 解析、未选齐禁用 mock）；手动烟测详情页 + SKU 选择；`typecheck`+`build`。

Exit Criteria:

- [ ] 商品详情消费既有 API 渲染正确；SKU 选齐方可加购，无库存组合置灰
- [ ] 评价摘要消费 `getCommentSummary` 展示（总数/好评率/星级分布）
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯前端消费，IGraphQLEngine 后端测试项不适用）
- [ ] No owner-doc update required
- [ ] `docs/logs/` 更新

### Phase 2 - 收藏 / 取消收藏 + 加入购物车 + 足迹记录

Status: planned
Targets: `src/pages/goods/detail/`（动作接线）、`src/store/`（足迹/收藏态联动）
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/02-reference.md` Action Algebra/事件、`docs/design/product-catalog.md` 收藏/足迹语义）

- Item Types: `Add`
- Prereqs: Phase 1

- [ ] **Skill loading gate:** 通读 `flux-guide/02-reference.md`（Action Algebra then/onError、事件系统）；复阅 `docs/design/product-catalog.md` 收藏/足迹。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Add:** 收藏/取消收藏（`LitemallCollect` addCollect/removeCollect，登录态——未登录走 M1 半游客拦截）。
- [ ] **Add:** 加入购物车（`LitemallCart.addGoods`，传 goodsId + 选中的 productId + 数量；登录态校验）。**运行时去重维度为 userId + productId**（productId→goodsId 1:1，见 `LitemallCartBizModel.addGoods`）；成功后刷新购物车角标（M1 store）。
- [ ] **Add:** 足迹记录（进入详情页 onMount 调 `LitemallFootprint.recordFootprint(goodsId)`，登录态；未登录静默跳过不阻塞浏览）。
- [ ] **Proof:** vitest（收藏/加购/足迹 action 接线 mock，含未登录走拦截分支）；手动烟测；`typecheck`+`build`。

Exit Criteria:

- [ ] 收藏/加购/足迹消费既有 API 正确；加购后购物车角标刷新
- [ ] 未登录触发收藏/加购时走 M1 半游客拦截；足迹未登录静默跳过
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯前端消费，IGraphQLEngine 后端测试项不适用）
- [ ] No owner-doc update required
- [ ] `docs/logs/` 更新

### Phase 3 - 购物车页（列表/勾选/数量/左滑删除/清空/结算入口）

Status: planned
Targets: `src/pages/cart/`
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/mobile/swipe-cell.md`、`flux-guide/mobile/README.md` 组合、`flux-guide/design-patterns/crud.md` 列表操作、`docs/design/order-and-cart.md` 购物车语义）

- Item Types: `Add | Decision`
- Prereqs: Phase 2

- [ ] **Skill loading gate:** 通读 `flux-guide/mobile/swipe-cell.md`（左滑操作）、`flux-guide/mobile/README.md`、`flux-guide/design-patterns/crud.md`（列表 + 操作动作）；复阅 `docs/design/order-and-cart.md` 购物车语义与边界（结算归 M4）。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Decision D2（数量调整与删除交互）：** 抉择——数量步进器调整（`LitemallCart.updateQuantity`）；swipe-cell 手势滑动露出删除按钮（组件支持左/右滑双向，按 `flux-guide/mobile/swipe-cell.md` 约定配置动作区）；数量为 1 时减号变删除图标（对齐设计分析 §3.2 c-shopping 模式，消除 0 数量歧义）；底部清空 + 结算入口。备选（复选框行内删除按钮）——否决理由：滑动手势删除节省横向空间，移动端主流。残留风险：数量变更需防抖/乐观更新策略——update 与原值相同时不发请求（设计分析 §3.3 新蜂模式）。
- [ ] **Add:** 购物车列表（`LitemallCart` checkedList）+ 单选/全选勾选（check/uncheck/checkAll）+ 数量步进（updateQuantity，防抖+不变不发的优化）+ swipe-cell 滑动删除（deleteCart）+ 清空（clear）+ 结算入口（跳 M4 结算页，M4 未就绪前入口禁用或提示）。
- [ ] **Add:** 购物车 Tab 角标与列表选中态联动（M1 store）。
- [ ] **Proof:** vitest（勾选/数量防抖/滑动删除 mock，数量=1 减号变删除分支）；手动烟测；`typecheck`+`build`。

Exit Criteria:

- [ ] 购物车 CRUD 消费既有 API 正确；勾选/数量/滑动删除/清空可用；结算入口存在（M4 未就绪则禁用并提示）
- [ ] 购物车角标与列表/选中态联动一致
- [ ] swipe-cell 操作按钮满足 M0 44×44px 触摸基线
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯前端消费，IGraphQLEngine 后端测试项不适用）
- [ ] No owner-doc update required（结算边界已在 `order-and-cart.md` 明确）
- [ ] `docs/logs/` 更新

## Plan Audit

- Status: pending
- Auditor / Agent: <独立 subagent，fresh session>
- Evidence: <task id + 发现/处置摘要>

## Closure Gates

- [ ] in-scope behavior is complete
- [ ] relevant docs are aligned（无业务语义变更）
- [ ] verification has run（`pnpm --filter @nop-chaos/mall-mobile typecheck`+`build`+`test`；手动 e2e 烟测；视觉/行为驱动，前端 vitest + 手动烟测）
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯消费），IGraphQLEngine 后端测试项不适用
- [ ] no in-scope item downgraded to deferred/follow-up
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill`（`none` + 非 AMIS 理由，符合规则 #14）
- [ ] skill loading verification: flux-guide 路由文档已读并列路径
- [ ] text consistency verified
- [ ] closure audit performed by different agent/session
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### 详情页营销横幅（限时折扣/秒杀/会员价）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 属 M7 范围且依赖增强后端营销 API（设计分析 M-05/M-09）。
- Successor Required: `yes`（触发条件：M7 启动时）

### 结构化评价展示（标签云/有图筛选/优缺点表单）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: **后端已就绪**——`LitemallComment` 已有 `pros`/`cons`/`semanticRating` 字段，`getCommentSummary`（公开）已返回 `goodRate`/`starDistribution`/`prosTags`/`consTags`，`commentList` 支持 `showType` 有图筛选。M9 负责的是**结构化展示 UI 与评价提交表单**（标签云可视化、优缺点动态列表、5 级语义文案），**非后端增强**。本计划 Phase 1 仅消费基础摘要。
- Successor Required: `yes`（触发条件：M9 启动时——为展示 UI 工作，非等待后端）

## Closure

<!-- 闭合审计须由独立 subagent 执行，此处留空。 -->

Status Note: <待闭合填写>

Closure Audit Evidence:

- Reviewer / Agent: <独立 reviewer>
- Evidence: <task id / 记录>

Follow-up:

- 详情页营销横幅（见 Deferred，M7 启动时）
- 结构化评价展示 UI（见 Deferred，M9 启动时——后端 summary/字段已就绪，M9 为展示 UI 工作）
