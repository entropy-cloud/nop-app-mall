# mobile-m6 个人中心 & 互动

> Plan Status: draft
> Last Reviewed: 2026-06-30
> Source: `docs/backlog/mobile-frontend-roadmap.md` Mobile Phase 6；`docs/analysis/2026-06-21-mobile-mall-functional-design-analysis.md`（个人中心聚合、评价提交）
> Related: 前置 `2026-06-30-1900-1-mobile-m4-address-order-plan.md`（M4，订单列表/详情为个人中心订单聚合入口与评价入口）；M3（collect/footprint/comment-summary 已部分消费于详情页）
> Audit: required

## Current Baseline

**前置（M1-M5 交付后成立）：**
- `apps/mall-mobile/` 全脚手架/auth/路由/store/半游客就绪；购物车/结算/订单/支付/售后链路可走通（M4+M5）。
- `src/pages/profile.tsx` 为 Placeholder（「M6 将填充」）——本计划 Phase 1 实体化。Tab 壳「我的」Tab 已注册指向 profile。
- 底部「我的」Tab 与购物车角标已就位（M1）；登录态由 store + 半游客 guard 管理。
- API 客户端层 `src/api/`：已有 `collect-api.ts`（add/remove/isCollected，**列表方法为 `listByType` 尚未接**）、`footprint-api.ts`（仅 recordFootprint，**list/clear 尚未接**）、`comment-api.ts`（仅 summary，**submitComment/myComments 尚未接**）；**profile/password API 模块尚未建**。

**后端 API（已 done，本计划纯消费，零后端改动——已逐项核对实测契约）：**
- 个人资料 `NopAuthUserExBizModel`（`app-mall-delta/.../biz/NopAuthUserExBizModel.java`，BizModel 名 `NopAuthUser`）：`getMyProfile()`（`@BizQuery`，password/salt 由 xmeta 剥离）/ `updateMyProfile(nickName,gender,avatar,phone,email,birthday)`（`@BizMutation`，null=不变；username 不可改）。
- 修改密码 `NopAuthUser__changeSelfPassword(oldPassword,newPassword)`（平台继承 `@BizMutation`，见 `TestNopAuthUserProfile.java:76-113` 已验证）。
- 收藏 `LitemallCollectBizModel`：`addCollect(type,valueId)` / `removeCollect(type,valueId)` / `isCollect(type,valueId)` / **`listByType(type,page,pageSize)`**（`@BizQuery` 分页；**注意列表方法名是 `listByType` 非 `list`**；type=0 商品）。
- 足迹 `LitemallFootprintBizModel`：`recordFootprint(goodsId)` / **`listFootprints(page,pageSize)`** / **`clearFootprints()`**（**无单条删除，仅清空全部**）。
- 评价 `LitemallCommentBizModel`：`submitComment(orderGoodsId,content,star,hasPicture,picUrls,pros,cons,semanticRating)`（`@BizMutation`，**`orderGoodsId` 非 goodsId**，自动解析 valueId=orderGoods.goodsId；资格门禁：订单须 CONFIRM(401)/AUTO_CONFIRM(402) + 拥有者 + orderGoods.comment==0；预审 `mall_comment_pre_moderation` 默认 off）/ `myComments(page,pageSize)`（`@BizQuery` 分页）；`commentList`/`getCommentSummary` 已公开（M3 消费）。
- 文件上传：平台 `/f/...` 端点 + `LitemallMaterialBizModel.uploadMaterial(fileUpload)`（fileRef `/f/download/{fileId}`）——头像/评价图上传走平台文件端点（M5 已封装 `image-upload`，本计划复用）。

**Gap：** 个人中心/资料/密码/收藏列表/足迹列表/评价提交均为零实现；profile Tab 为占位。

## Goals

- 个人中心页（实体化 profile.tsx）：用户信息 + 订单聚合入口（按状态：待付款/待发货/待收货/待评价，消费 M4 订单列表）+ 功能入口聚合（收藏/足迹/地址/优惠券(M7入口)/反馈(M8)/设置）。
- 个人资料编辑（昵称/头像/性别/手机）+ 修改密码。
- 收藏列表（`listByType`）+ 足迹列表（`listFootprints`）+ 清空足迹（`clearFootprints`）。
- 评价入口 + 评价提交（`submitComment`）+ 我的评价（`myComments`）。

## Non-Goals

- 积分账户 / 钱包余额 / 消息中心 / 反馈提交 / FAQ / 联系客服 —— M8。
- 结构化评价展示（标签云/有图筛选/优缺点可视化）—— M9（后端 summary/字段已就绪，M6 仅做评价提交 + 基础我的评价列表）。
- 领券中心 / 团购 / 我的优惠券浏览 —— M7（个人中心仅留入口占位）。
- 头像裁剪 / 高级图片编辑 —— 仅上传 fileRef，不做客户端裁剪。
- 后端改动 —— 纯消费。

## Task Route

- Type: `implementation-only change`（业务设计已在 `docs/design/user-and-address.md`、`docs/design/product-catalog.md` 落地）
- Owner Docs: `docs/design/user-and-address.md`（用户/资料/密码语义）、`docs/design/product-catalog.md`（收藏/足迹/评价语义）、`docs/backlog/mobile-frontend-roadmap.md`（M6 范围）
- Skill Selection Basis: 见各 phase。总体为 nop-chaos-flux 移动端纯消费，非 AMIS。

## Infrastructure And Config Prereqs

- M1-M5 已交付；本地后端运行。
- 头像/评价图上传依赖平台 `/f/...` 文件端点（M5 `image-upload` 已封装则复用）。
- 无新增密钥/迁移/回滚。

## Execution Plan

### Phase 1 - 个人中心页（实体化）+ 订单聚合入口 + 功能入口

Status: planned
Targets: `src/pages/profile/index.tsx`（接管 profile Tab，替换 Placeholder）
Required Skill: `none`（nop-chaos-flux 移动端纯消费，非 AMIS；`nop-frontend-dev` 触发词 view.xml/AMIS 不匹配，`nop-backend-dev`/`nop-testing` 为后端/测试导向而本 phase 无后端改动、无新增 `@BizQuery`/`@BizMutation`。方法源：`flux-guide/design-patterns/cards.md`、`flux-guide/mobile/README.md`、`docs/design/user-and-address.md`）

- Item Types: `Add`
- Prereqs: M4（订单列表为聚合入口数据源）

- [ ] **Skill loading gate:** 通读 `flux-guide/design-patterns/cards.md`（聚合卡片）、`flux-guide/mobile/README.md`（M0 触摸基线）、`flux-guide/02-reference.md`；复阅 `docs/design/user-and-address.md`（用户信息）+ M4 订单列表契约（`myOrders(status)`）。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Add:** 个人中心页：用户信息卡（头像/昵称，未登录→登录引导）+ 订单聚合入口（待付款/待发货/待收货/待评价 四快捷入口，跳 M4 订单列表对应 Tab + 数量角标可空）+ 功能入口聚合（我的收藏/我的足迹/收货地址/我的优惠券(M7占位)/意见反馈(M8占位)/设置(资料/密码/退出)）。
- [ ] **Add:** 退出登录（`LoginApi__logout` + 清 store + 跳首页，二次确认）。
- [ ] **Proof:** vitest（未登录/已登录分支 + 聚合入口跳转 mock + 退出登录清 store）；手动烟测；`typecheck`+`build`+`lint`。

Exit Criteria:

- [ ] 个人中心页实体化消费 store userInfo + 订单聚合入口跳 M4 Tab 正确
- [ ] 功能入口聚合就位（M7/M8 入口为占位禁用/提示）；M0 44×44px 触摸基线满足
- [ ] 退出登录清 store + 跳首页
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯消费），IGraphQLEngine 后端测试项不适用
- [ ] No owner-doc update required
- [ ] `docs/logs/` 更新

### Phase 2 - 个人资料编辑 + 修改密码

Status: planned
Targets: `src/api/user-api.ts`、`src/pages/profile/`（edit-profile/change-password）
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/design-patterns/form.md`、`docs/design/user-and-address.md` 资料/密码语义）

- Item Types: `Add | Decision`
- Prereqs: Phase 1

- [ ] **Skill loading gate:** 通读 `flux-guide/design-patterns/form.md`（表单+校验）；复阅 `docs/design/user-and-address.md`（资料/密码）+ 实测契约 `NopAuthUserExBizModel.getMyProfile/updateMyProfile` + 平台 `NopAuthUser__changeSelfPassword`（见 `TestNopAuthUserProfile.java:76-113`）+ 平台文件上传 fileRef（`LitemallMaterialBizModel.uploadMaterial`）。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Decision D1（头像上传机制）：** 抉择——头像走平台文件上传端点拿 fileRef（`/f/download/{fileId}`）→ 传 `updateMyProfile.avatar`，**复用 M5 `image-upload` 组件**；不做客户端裁剪。备选（URL 文本输入）——否决理由：体验差且与既有 fileRef 存储约定不一致。残留风险：平台文件上传端点确切路径执行时核对（Nop 标准 `/f/...`），与 M5 D3 一致。
- [ ] **Add:** `user-api.ts`（getMyProfile/updateMyProfile/changeSelfPassword）。
- [ ] **Add:** 资料编辑页（拉 getMyProfile 回填 → 编辑 nickName/gender/avatar(D1)/phone → updateMyProfile 仅传变更字段 → 成功刷新 store userInfo）+ 表单校验（手机号格式）。
- [ ] **Add:** 修改密码页（旧密码 + 新密码 + 确认 → `changeSelfPassword`，成功提示并可选重登）+ 校验（新旧不同、确认一致、长度）。
- [ ] **Proof:** vitest（资料回填+部分字段更新 mock、头像 fileRef、密码校验+changeSelfPassword 成功/旧密码错分支）；手动烟测；`typecheck`+`build`+`lint`。

Exit Criteria:

- [ ] 资料编辑（含头像 fileRef 上传）消费 updateMyProfile 正确，成功刷新 store
- [ ] 修改密码消费 changeSelfPassword 正确（成功 + 旧密码错态）
- [ ] 表单交互满足 M0 44×44px；骨架/错/重试态覆盖
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯消费），IGraphQLEngine 后端测试项不适用
- [ ] No owner-doc update required
- [ ] `docs/logs/` 更新

### Phase 3 - 收藏列表 + 足迹列表（含清空）

Status: planned
Targets: `src/api/collect-api.ts`（补 listByType）、`src/api/footprint-api.ts`（补 listFootprints/clearFootprints）、`src/pages/profile/`（favorites/footprints）
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/mobile/infinite-scroll.md`、`flux-guide/design-patterns/crud.md`、`docs/design/product-catalog.md` 收藏/足迹语义）

- Item Types: `Add | Decision`
- Prereqs: Phase 1

- [ ] **Skill loading gate:** 通读 `flux-guide/mobile/infinite-scroll.md`（触底分页）、`flux-guide/design-patterns/crud.md`（列表+删除动作）；复阅 `docs/design/product-catalog.md`（收藏/足迹）+ 实测契约 `LitemallCollectBizModel.listByType`（**注意方法名**）+ `LitemallFootprintBizModel.listFootprints/clearFootprints`（**仅清空全部，无单条删除**）。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Decision D2（足迹删除语义）：** 抉择——足迹仅支持「清空全部」（`clearFootprints`，二次确认），无单条删除（后端无此契约）。备选（前端伪造单删）——否决理由：后端无单删契约，伪造会与服务端不一致。残留风险：用户期望单删——属已知产品边界（与 c-shopping/芋道一致），记 Deferred。
- [ ] **Add:** `collect-api.ts` 补 `listByType(type,page,pageSize)` + `footprint-api.ts` 补 `listFootprints/clearFootprints`。
- [ ] **Add:** 收藏列表页（`listByType` type=0 分页 + infinite-scroll + 取消收藏 `removeCollect` + 点击跳商品详情 + 空态）。
- [ ] **Add:** 足迹列表页（`listFootprints` 分页 + infinite-scroll + 清空 `clearFootprints` D2 + 点击跳商品详情 + 空态）。
- [ ] **Proof:** vitest（分页 mock + 取消收藏 + 清空足迹二次确认 + 空态）；手动烟测；`typecheck`+`build`+`lint`。

Exit Criteria:

- [ ] 收藏/足迹列表消费 listByType/listFootprints 分页正确；取消收藏/清空足迹可用
- [ ] 足迹仅清空全部（D2）；空态/骨架/错/重试 + M0 44×44px 覆盖
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯消费），IGraphQLEngine 后端测试项不适用
- [ ] No owner-doc update required
- [ ] `docs/logs/` 更新

### Phase 4 - 评价提交 + 我的评价

Status: planned
Targets: `src/api/comment-api.ts`（补 submitComment/myComments）、`src/pages/profile/`（comment-submit/my-comments）
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/design-patterns/form.md`、`flux-guide/mobile/README.md`、`docs/design/product-catalog.md` 评价语义）

- Item Types: `Add`
- Prereqs: Phase 1（个人中心入口）+ M4（待评价入口/订单商品 orderGoodsId 来源）

- [ ] **Skill loading gate:** 通读 `flux-guide/design-patterns/form.md`、`flux-guide/mobile/README.md`；复阅 `docs/design/product-catalog.md`（评价资格/字段）+ 实测契约 `LitemallCommentBizModel.submitComment`（**入参 `orderGoodsId` 非 goodsId**；star 1-5；picUrls/pros/cons 为 JSON 数组串；资格门禁 401/402 + comment==0）+ `myComments`。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Add:** `comment-api.ts` 补 `submitComment/myComments`。
- [ ] **Add:** 评价提交页（从订单详情/待评价入口带 `orderGoodsId` 进入 → 星级 + 内容 + 图片上传(复用 image-upload，picUrls JSON 串) + 提交 `submitComment` + 校验 + 防重复；预审态提示「审核中」）。
- [ ] **Add:** 我的评价列表页（`myComments` 分页 + infinite-scroll + 评价卡片 + 空态）。
- [ ] **Proof:** vitest（评价提交入参 orderGoodsId/star/picUrls mock + 校验/防重复 + myComments 分页）；手动烟测（订单详情→去评价→提交→我的评价）；`typecheck`+`build`+`lint`。

Exit Criteria:

- [ ] 评价提交消费 submitComment 正确（orderGoodsId + star + picUrls JSON 串）；我的评价消费 myComments 分页
- [ ] 评价资格/预审态处理；M0 44×44px；骨架/空/错/重试 + 防重复覆盖
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
- [ ] verification has run（`pnpm --filter @nop-chaos/mall-mobile typecheck`+`build`+`test`+`lint` + 手动 e2e 烟测）
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯消费），IGraphQLEngine 后端测试项不适用
- [ ] no in-scope item downgraded to deferred/follow-up
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill` listed（均为 `none` + 非 AMIS 技术栈理由，符合规则 #14）
- [ ] skill loading verification: 各 phase 通读 flux-guide 路由文档，路径列于 skill loading gate
- [ ] text consistency verified: status, phases, gates, and log all agree
- [ ] closure audit was performed by a different agent/session than implementation
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### 积分账户 / 钱包 / 消息中心 / 反馈 / FAQ / 联系客服

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 属 M8（扩展功能）；个人中心仅留入口占位。
- Successor Required: `yes`（触发条件：M8 启动时）

### 结构化评价展示（标签云/有图筛选/优缺点可视化）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 后端 summary/字段已就绪，结构化**展示 UI** 属 M9；M6 仅做评价提交 + 基础我的评价列表。
- Successor Required: `yes`（触发条件：M9 启动时）

### 领券中心 / 我的优惠券浏览 / 团购

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 属 M7（营销）；个人中心仅留「我的优惠券」入口占位。
- Successor Required: `yes`（触发条件：M7 启动时）

### 足迹单条删除

- Classification: `watch-only residual`
- Why Not Blocking Closure: 后端 `LitemallFootprintBizModel` 无单删契约（仅 `clearFootprints` 清空全部）；M6 仅支持清空全部（D2）。
- Successor Required: `yes`（触发条件：后端补足迹单删契约时）

## Closure

<!-- 闭合审计须由独立 subagent 执行（不同 session/context），此处留空。 -->

Status Note: <闭合时填>

Closure Audit Evidence:

- Reviewer / Agent: <独立 reviewer，MUST NOT be implementing agent>
- Evidence: <闭合时填>

Follow-up:

- 积分/钱包/消息/反馈/FAQ（见 Deferred，M8 启动时）
- 结构化评价展示（见 Deferred，M9 启动时）
- 领券中心/我的优惠券/团购（见 Deferred，M7 启动时）
- 足迹单删（见 Deferred，后端补契约时）
