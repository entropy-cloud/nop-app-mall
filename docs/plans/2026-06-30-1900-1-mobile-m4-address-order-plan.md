# mobile-m4 地址 & 订单

> Plan Status: draft
> Last Reviewed: 2026-06-30
> Source: `docs/backlog/mobile-frontend-roadmap.md` Mobile Phase 4；`docs/analysis/2026-06-21-mobile-mall-functional-design-analysis.md`（结算页结构、订单 Tab 维度）
> Related: 前置 `2026-06-30-1343-1-mobile-m1-scaffold-infra-plan.md`（M1）+ `2026-06-30-1343-2-mobile-m2-home-category-plan.md`（M2）+ `2026-06-30-1343-3-mobile-m3-product-detail-cart-plan.md`（M3，购物车结算入口已就位、目标页 `#/page/checkout` 当前为 Placeholder）；后续 `2026-06-30-1900-2-mobile-m5-payment-aftersale-plan.md`（M5，支付收银台消费本计划 submit 产出的 CREATED 订单）、`2026-06-30-1900-3-mobile-m6-profile-interaction-plan.md`（M6，个人中心订单聚合入口消费本计划订单列表）
> Audit: required

## Current Baseline

**前置（M1+M2+M3 交付后成立）：**
- `apps/mall-mobile/`（`@nop-chaos/mall-mobile`，React 19 + Zustand，纯 React TSX 实现）可 dev 启动；渲染器/env/路由/Tab/全局状态/auth/半游客/401 续期就绪。
- 已落地页：home / category / search / brand(list+detail) / topic(detail) / goods(detail) / cart / auth(login+register+forgot)。`src/pages/profile.tsx` 为 Placeholder（M6 填充）。
- `src/pages/cart.tsx` 结算按钮已就位但目标 `#/page/checkout` 落到 `<Placeholder>`（M3 Deferred「结算/下单页 → M4」）——本计划 Phase 2 接管该路由。
- API 客户端层 `src/api/`（`rpc.ts` 提供 `postRpc<T>` + `PageBean<T>` + `asPage`，命中 `/r/<BizApi>__<method>` REST RPC）：已有 catalog/cart/brand/product/footprint/collect/comment(summary) 模块；**地址/订单/优惠券/Region 模块尚未建**。
- Store（Zustand）：`accessToken/refreshToken/userInfo/cartBadge`；hooks：`useAsync`/`usePagedList`；组件：`page-shell`/`state-views`(骨架/空/错/重试)/`infinite-scroll`/`quantity-stepper` 等。

**后端 API（已 done，本计划纯消费，零后端改动——已逐项核对 BizModel 实测契约）：**
- 地址 `LitemallAddressBizModel`（`app-mall-service/.../entity/LitemallAddressBizModel.java`，全部 login-required、owner-checked）：`list` / `detail(id)` / `add(name,phone,province,city,county,addressDetail,areaCode,isDefault)`（**注意入参名 `phone`**，首地址自动默认，上限 20）/ `updateAddress(...)` / `deleteAddress(id)` / `setDefault(id)`。**无泛型 update/delete，须用 `updateAddress`/`deleteAddress`/`setDefault`**。
- Region 级联 `LitemallRegionBizModel.getRegionTree()`（`@BizQuery @Auth(publicAccess=true)`，单次返回 province→city→district 全树，节点 `{id,name,type,code,children[]}`，`type` 1/2/3）——移动端单次拉取并本地缓存，`LitemallAddress.areaCode` 对应 region `code`。
- 结算预览 `LitemallCartBizModel.checkedList()`（`@BizQuery`，返回当前用户 checked=true 购物车行，结算页商品清单来源）。
- 运费 `LitemallSystemBizModel.getFreightPrice()`（`@BizQuery`，读 config `mall_freight_price`，**统一平邮运费**，非按区计算）。
- 优惠券（结算页选择，**非领券中心**）：`LitemallCouponBizModel.listMyCoupons(status,page,pageSize)`（`@BizQuery`，我的优惠券分页）+ `LitemallCouponUserBizModel.selectCouponForOrder(couponUserId,...)`（`@BizQuery`，预览某券对本单抵扣额）。
- 下单 `LitemallOrderBizModel.submit(...)`（`@BizMutation`，服务端读 checked 购物车行；入参 `addressId`(EXPRESS 必填)/`message`/`freightPrice`/`couponUserId`/`grouponRulesId`/`grouponId`/`usePoints`/`deliveryType`/`pickupStoreId`...，**本计划仅用 addressId+freightPrice+couponUserId+message（EXPRESS）**；submit 后订单 = CREATED(101)）。
- 订单 `LitemallOrderBizModel`：`myOrders(status)`（`@BizQuery`，**无分页参数返回全量倒序**；`status==null`→全部、`status==-1`→待付款(CREATED 101)、否则精确匹配 orderStatus）/ `getMyOrder(orderId)` / `cancel(orderId)` / `confirm(orderId)`(待收货 301→已完成 401，发积分) / `deleteOrder(orderId)`(仅终态/取消态可删)。
- 订单状态常量（`_AppMallDaoConstants`，用于 Tab）：`101` CREATED 待付款 / `102,103` 已取消 / `201` PAY 待发货 / `301` SHIP 待收货 / `401,402` 已完成。
- 订单商品评价维度：`LitemallOrderGoods.comment` 字段可查询（`LitemallOrderGoodsBizModel` 有 `comment==0` 过滤的 uncommented 查询，L30）——「待评价」Tab 据 订单 status∈{401,402} 且存在 comment==0 的 orderGoods 客户端判定。

**Gap：** 地址/结算/订单全为零实现；购物车结算入口无落点（Placeholder）。

## Goals

- 地址管理：列表/新增/编辑/删除/默认设置，地区级联选择（消费 `getRegionTree` 本地缓存）。
- 结算页（`#/page/checkout`）：地址选择 + 商品清单（`checkedList`）+ 优惠券选择（`listMyCoupons`+`selectCouponForOrder` 预览）+ 运费（`getFreightPrice`）+ 提交（`submit`），接管 M3 遗留 Placeholder。
- 订单列表：全部/待付款/待发货/待收货/待评价 五 Tab（消费 `myOrders`，客户端分 Tab）。
- 订单详情 + 取消订单 + 确认收货 + 删除订单。

## Non-Goals

- 支付收银台 / 支付结果页 —— M5（本计划止于 submit 产出 CREATED 订单后跳收银台入口）。
- 售后申请/列表 —— M5。
- 评价提交表单 / 我的评价 —— M6（本计划订单详情/列表提供「去评价」入口，提交 UI 归 M6；「待评价」Tab 仅做订单筛选展示）。
- 领券中心 / 兑换码 / 团购 / 拼团 —— M7（结算页不暴露 groupon/pinTuan/usePoints 等增强入参）。
- 自提(PICKUP `deliveryType=10`)结算 —— 增强后端 P31 范围，本计划结算仅 EXPRESS。
- 按区/按模板运费计算 —— 后端为统一平邮 `mall_freight_price`，无按区运费契约，不模拟。
- 后端改动 —— 纯消费既有 API。

## Task Route

- Type: `implementation-only change`（业务设计已在 `docs/design/order-and-cart.md`、`docs/design/user-and-address.md` 落地）
- Owner Docs: `docs/design/order-and-cart.md`（订单/购物车/结算语义与边界）、`docs/design/user-and-address.md`（地址语义）、`docs/backlog/mobile-frontend-roadmap.md`（M4 范围 + mobile 组件复用强制）
- Skill Selection Basis: 见各 phase。总体为 nop-chaos-flux 移动端纯消费，非 AMIS。

## Infrastructure And Config Prereqs

- M1+M2+M3 已交付；本地后端运行（dev proxy `/r`→`MALL_BACKEND_ORIGIN`）。
- 无新增密钥/迁移/回滚。

## Execution Plan

### Phase 1 - 地址管理 + 地区级联选择

Status: planned
Targets: `src/api/address-api.ts`、`src/api/region-api.ts`、`src/pages/address/`（list/edit）、`src/components/region-picker.tsx`
Required Skill: `none`（nop-chaos-flux 移动端纯消费，非 AMIS；`nop-frontend-dev` 触发词 view.xml/AMIS/grid/form 不匹配，`nop-backend-dev`/`nop-testing` 为后端/测试导向而本 phase 无后端改动、无新增 `@BizQuery`/`@BizMutation`。方法源：`flux-guide/design-patterns/form.md`、`flux-guide/design-patterns/crud.md`、`docs/design/user-and-address.md` 地址语义）

- Item Types: `Add | Decision`
- Prereqs: M1 完成

- [ ] **Skill loading gate:** 通读 `flux-guide/design-patterns/form.md`（表单+校验）、`flux-guide/design-patterns/crud.md`（列表+操作动作）、`flux-guide/design-patterns/conditional.md`（级联显隐）；复阅 `docs/design/user-and-address.md`（地址/默认地址语义）+ 实测契约 `app-mall-service/.../entity/LitemallAddressBizModel.java`（list/detail/add/updateAddress/deleteAddress/setDefault，入参 `phone`）+ `LitemallRegionBizModel.java`（`getRegionTree` 公开）。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Add:** `region-api.ts`（`getRegionTree`，单次拉取 + 模块级缓存，避免重复请求）+ `address-api.ts`（list/detail/add/updateAddress/deleteAddress/setDefault，对齐实测入参名 `phone`）。
- [ ] **Add:** 地区级联选择器 `region-picker.tsx`（省→市→区 三级，从缓存的 tree 客户端派生；选中写回 `province/city/county/areaCode`）。
- [ ] **Add:** 地址列表页（list + 默认标记 + 编辑/删除入口）+ 地址新增/编辑页（表单校验：手机号格式、地区必选、详情必填；首地址后端自动默认）；默认设置（`setDefault`）、删除（`deleteAddress`，二次确认）。
- [ ] **Decision D1（地址上限与默认语义）：** 抉择——前端信任后端 `mall_address_limit=20` 上限与「首地址自动默认」语义，不在前端硬编码重复规则；`setDefault` 由后端清空其他默认。备选（前端复制上限规则）——否决理由：单一事实源在后端配置，前端复制会漂移。残留风险：无。
- [ ] **Proof:** vitest（region-picker 级联派生 + 地址表单校验 + add/update/delete/setDefault RPC 接线 mock）；手动烟测（地址 CRUD + 默认切换 + 级联选择）；`typecheck`+`build`+`lint`。

Exit Criteria:

- [ ] 地址 CRUD + 默认设置消费既有 API 正确；地区级联从 `getRegionTree` 派生且本地缓存
- [ ] 地址交互元素满足 M0 44×44px 触摸基线；骨架/空/错/重试态覆盖地址列表/表单
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯消费），IGraphQLEngine 后端测试项不适用
- [ ] No owner-doc update required（地址/默认语义已在 `user-and-address.md` 明确）
- [ ] `docs/logs/` 更新

### Phase 2 - 结算页（地址 + 清单 + 优惠券 + 运费 + 提交）

Status: planned
Targets: `src/api/order-api.ts`、`src/api/coupon-api.ts`（结算选择用）、`src/pages/checkout/index.tsx`（接管 `#/page/checkout`）
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/design-patterns/form.md`、`flux-guide/mobile/README.md`、`docs/design/order-and-cart.md` 结算/提交语义）

- Item Types: `Add | Decision`
- Prereqs: Phase 1（地址选择）

- [ ] **Skill loading gate:** 通读 `flux-guide/design-patterns/form.md`、`flux-guide/mobile/README.md`（M0 触摸基线）、`flux-guide/02-reference.md`（表达式/数据流）；复阅 `docs/design/order-and-cart.md`（结算/提交/运费/优惠券边界）+ 实测契约 `LitemallOrderBizModel.submit`（入参 addressId/freightPrice/couponUserId/message，服务端读 checked 购物车）+ `LitemallCartBizModel.checkedList` + `LitemallSystemBizModel.getFreightPrice` + `LitemallCouponBizModel.listMyCoupons` + `LitemallCouponUserBizModel.selectCouponForOrder`。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Decision D2（结算优惠券选择范围）：** 抉择——结算页内嵌**最小优惠券选择**（`listMyCoupons` 列出我的可用券 + `selectCouponForOrder` 预览抵扣额，用户选一张传 `couponUserId` 给 submit；无效券由 submit 服务端拒绝并提示），**不含领券中心/兑换码浏览**（M7）。备选（结算页不选券，全部留 M7）——否决理由：roadmap M4 结算范围明确含「优惠券选择」，且后端契约完备。残留风险：可用券判定（满减/分类限制）前端不做复杂匹配，依赖 `selectCouponForOrder` 返回的抵扣额与 submit 校验。
- [ ] **Decision D3（运费来源）：** 抉择——运费消费 `LitemallSystem.getFreightPrice()`（统一平邮），submit 时回传同一 `freightPrice`。备选（前端按区计算）——否决理由：后端无按区/按模板运费契约，`submit` 自身亦取 `mall_freight_price` 兜底。残留风险：包邮门槛/满减免邮属营销（M7），本计划不实现。
- [ ] **Add:** `order-api.ts`（submit 占位 + 后续 phase 补 myOrders 等）、`coupon-api.ts`（`listMyCoupons` + `selectCouponForOrder`）。
- [ ] **Add:** 结算页 `#/page/checkout`：地址选择卡（无地址引导新增/选默认）+ 商品清单（`checkedList`）+ 优惠券选择（D2）+ 运费（`getFreightPrice`）+ 合计 + 留言 + 提交按钮（`submit`→CREATED 订单→跳收银台入口，M5 未就绪前提示「支付功能即将上线」并保留订单）。
- [ ] **Add:** 提交校验（EXPRESS 必须有地址；无 checked 商品则引导回购物车）+ 骨架/空/错/重试态 + 防重复提交（提交中禁用）。
- [ ] **Proof:** vitest（结算数据编排 checkedList+freight+coupon 预览 mock、submit 入参、缺地址/无商品/重复提交分支）；手动烟测（购物车结算→结算页→提交）；`typecheck`+`build`+`lint`。

Exit Criteria:

- [ ] 结算页消费 checkedList/getFreightPrice/listMyCoupons/selectCouponForOrder/submit 正确；提交产出 CREATED 订单并跳收银台入口（M5 未就绪则禁用并提示且保留订单）
- [ ] 优惠券选择最小闭环（列出+预览抵扣+传 couponUserId）；无效券由 submit 拒绝并提示
- [ ] 结算页交互元素满足 M0 44×44px；骨架/空/错/重试 + 防重复提交覆盖
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯消费），IGraphQLEngine 后端测试项不适用
- [ ] No owner-doc update required（结算/运费/优惠券边界已在 `order-and-cart.md` 明确）
- [ ] `docs/logs/` 更新

### Phase 3 - 订单列表（五 Tab）+ 订单详情 + 取消/确认收货/删除

Status: planned
Targets: `src/api/order-api.ts`（补全 myOrders/getMyOrder/cancel/confirm/deleteOrder）、`src/pages/order/`（list/detail）
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/design-patterns/tabs.md`、`flux-guide/design-patterns/cards.md`、`docs/design/order-and-cart.md` 订单状态语义）

- Item Types: `Add | Decision`
- Prereqs: Phase 2（下单链路就绪）

- [ ] **Skill loading gate:** 通读 `flux-guide/design-patterns/tabs.md`（Tab 切换）、`flux-guide/design-patterns/cards.md`（订单卡片）、`flux-guide/02-reference.md`；复阅 `docs/design/order-and-cart.md`（订单状态机：101/201/301/401/102,103）+ 实测契约 `LitemallOrderBizModel.myOrders/getMyOrder/cancel/confirm/deleteOrder`。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Decision D4（待评价 Tab 判定 + 评价入口边界）：** 抉择——「待评价」Tab = `myOrders(401或402)` 客户端再筛「存在 comment==0 的 orderGoods」；订单详情/卡片对未评价商品展示「去评价」入口，**点击后路由到评价页占位（M6 接管 `submitComment`）**。备选（本 phase 内做评价提交）——否决理由：评价提交属 M6，跨计划拆分制造依赖。残留风险：依赖订单详情/列表载荷暴露 orderGoods.comment 标志——执行时校验载荷，若未暴露则记 `model-gap`（不阻塞，可退化为「已完成=待评价」粗粒度 Tab 并在 M6 精化）。
- [ ] **Add:** `order-api.ts` 补全 myOrders/getMyOrder/cancel/confirm/deleteOrder。
- [ ] **Add:** 订单列表页五 Tab（全部 null / 待付款 -1 / 待发货 201 / 待收货 301 / 待评价 D4），Tab 切换重拉；订单卡片（状态标签+商品缩略+金额+操作按钮：待付款→去支付(M5入口)/取消；待收货→确认收货；已完成→去评价入口(M6)/删除）。
- [ ] **Add:** 订单详情页（状态+地址+商品清单+金额明细+物流占位+操作：取消/确认收货/删除/去支付/去评价，按状态显隐）。
- [ ] **Proof:** vitest（Tab 切换+状态过滤 mock、待评价客户端筛选、cancel/confirm/deleteOrder 接线 + 二次确认）；手动烟测（下单→订单列表→详情→取消/确认收货/删除）；`typecheck`+`build`+`lint`。

Exit Criteria:

- [ ] 订单列表五 Tab + 详情消费既有 API 正确；取消/确认收货/删除按状态机可用
- [ ] 「待评价」Tab 客户端筛选落地（若载荷缺 comment 标志则按 D4 退化并记 model-gap）
- [ ] 订单交互元素满足 M0 44×44px；骨架/空/错/重试态覆盖
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯消费），IGraphQLEngine 后端测试项不适用
- [ ] No owner-doc update required
- [ ] `docs/logs/` 更新

## Plan Audit

- Status: pending
- Auditor / Agent: <独立 subagent（fresh session，非起草者）>
- Evidence: <待独立审计>

## Closure Gates

- [ ] in-scope behavior is complete
- [ ] relevant docs are aligned
- [ ] verification has run（`pnpm --filter @nop-chaos/mall-mobile typecheck`+`build`+`test`+`lint` + 手动 e2e 烟测；视觉/行为驱动，前端 vitest + 手动烟测）
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯消费），IGraphQLEngine 后端测试项不适用
- [ ] no in-scope item downgraded to deferred/follow-up
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill` listed（均为 `none` + 非 AMIS 技术栈理由，符合规则 #14）
- [ ] skill loading verification: 各 phase 通读 flux-guide 路由文档，路径列于 skill loading gate
- [ ] text consistency verified: status, phases, gates, and log all agree
- [ ] closure audit was performed by a different agent/session than implementation
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### 结算页团购/拼团/积分抵扣入参

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: `submit` 虽接受 grouponRulesId/grouponId/usePoints/pinTuanActivityId 等增强入参，但团购/拼团/积分抵扣属 M7（营销）与积分体系；M4 结算仅 EXPRESS 基础提交。
- Successor Required: `yes`（触发条件：M7 启动时）

### 自提(PICKUP)结算

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: `deliveryType=10` 自提为增强后端 P31 范围（自提门店/核销码）；M4 结算仅 EXPRESS 配送。
- Successor Required: `yes`（触发条件：P31 自提交付后）

### 评价提交表单

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 评价提交（`submitComment`）+ 我的评价属 M6；本计划仅提供「待评价」Tab 与「去评价」入口占位。
- Successor Required: `yes`（触发条件：M6 启动时）

## Closure

<!-- 闭合审计须由独立 subagent 执行（不同 session/context），此处留空。 -->

Status Note: <闭合时填>

Closure Audit Evidence:

- Reviewer / Agent: <独立 reviewer，MUST NOT be implementing agent>
- Evidence: <闭合时填>

Follow-up:

- 结算页团购/拼团/积分抵扣（见 Deferred，M7 启动时）
- 自提结算（见 Deferred，P31 交付后）
- 评价提交表单（见 Deferred，M6 启动时）
