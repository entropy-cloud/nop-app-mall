# mobile-m5 支付 & 售后

> Plan Status: draft
> Last Reviewed: 2026-06-30
> Source: `docs/backlog/mobile-frontend-roadmap.md` Mobile Phase 5；`docs/analysis/2026-06-21-mobile-mall-functional-design-analysis.md`（收银台、售后类型/原因）
> Related: 前置 `2026-06-30-1900-1-mobile-m4-address-order-plan.md`（M4，submit 产出 CREATED 订单 + 订单列表/详情为收银台/售后入口）；M3（订单详情「去支付」入口）
> Audit: required

## Current Baseline

**前置（M1+M2+M3+M4 交付后成立）：**
- `apps/mall-mobile/` 全脚手架/auth/路由/store/半游客就绪；购物车/结算/订单列表/详情可走通至 `submit`→CREATED(101) 订单（M4）。
- 订单卡片/详情「去支付」入口已就位（M4 标注「支付功能即将上线」占位）——本计划 Phase 1 接管。
- API 客户端层 `src/api/`：`order-api.ts`（M4 建，含 myOrders/getMyOrder 等）已存在；**支付/售后 API 模块尚未建**。

**后端 API（已 done，本计划纯消费，零后端改动——已逐项核对实测契约）：**
- 支付模式检测 `PayService.isEnabled()`（`@BizQuery`，`app-mall-api/.../pay/PayService.java`）：`false`=demo 模式可用 `pay`；`true`=真实微信支付走 `prepay`。
- 收银台渠道 `LitemallOrderBizModel.getEnabledPayChannels(orderId)`（`@BizQuery`，返回 `PayChannelViewBean` 列表 WECHAT/ALIPAY/BALANCE + 当前用户余额，owner-checked；渠道受 `pay_channels` 配置开关，默认 WECHAT on）。
- 模拟支付 `LitemallOrderBizModel.pay(orderId)`（`@BizMutation`，**仅 demo 模式（isEnabled=false）或零元单可用**；真实支付且 actualPrice>0 抛 `ERR_ORDER_USE_REAL_PAYMENT`）。
- 微信支付 `LitemallOrderBizModel.prepay(orderId)`（`@BizMutation`，返回 `{order, codeUrl}`，codeUrl 为微信 Native 码）。
- 余额/组合支付 `payByBalance(orderId,confirmCredential)` / `payWithCombo(orderId,useBalanceAmount,confirmCredential)`（`confirmCredential`=登录密码复验）——**依赖钱包余额（M8），本计划不实现其提交 UI**。
- 异步确认（非消费端）：微信回调 `/wxpay/notify`→`confirmPaidByNotify` 翻 CREATED(101)→PAY(201) + payTime；移动端**轮询 `getMyOrder` 观察状态翻转**。
- 售后 `LitemallAftersaleBizModel`（`app-mall-service/.../entity/LitemallAftersaleBizModel.java`，login-required、owner-checked）：`apply(@RequestBean AftersaleApplyRequest{orderId,orderItemId(可空=整单),type(0未收到货/1不想要/2退货退款),reason(必为枚举中文串:不想要了/质量问题/少发漏发/商品损坏/与描述不符/七天无理由),amount,pictures,comment})` / `cancel(id)` / `submitReturnLogistics(id,returnShipChannel,returnShipSn)`(type=2 退货用) / `userList()`(无分页全量) / `userDetail(id)`。
- 文件上传：平台 `/f/...` 端点 + `LitemallMaterialBizModel.uploadMaterial(fileUpload)`（接收 `/f/download/{fileId}` 形式 fileRef）——售后 pictures 上传走平台文件端点拿 fileRef。

**Gap：** 收银台/支付结果/售后均为零实现；订单「去支付」无落点。

## Goals

- 支付收银台：据 `PayService.isEnabled()` 走 demo `pay` 或真实 `prepay`（codeUrl 展示扫码）+ 渠道展示（`getEnabledPayChannels`）；支付中轮询 `getMyOrder` 观察状态翻转。
- 支付结果页（成功/失败/处理中）。
- 售后申请（仅退款 type=0/1、退货退款 type=2 + 原因枚举 + 图片）。
- 售后列表 + 详情 + 撤回(cancel) + 进度时间线 + 退货物流提交(submitReturnLogistics)。

## Non-Goals

- 钱包余额/组合支付提交 UI —— M8（钱包）；本计划收银台列渠道但仅实现 demo+wxpay 提交，余额/组合（需密码凭证+钱包）留 M8。
- 微信 JSAPI / 小程序调起 —— 本项目 H5 定位（见 M1 Deferred、`user-and-address.md:170`），真实支付仅展示 codeUrl 供扫码，不做小程序原生调起。
- 售后 admin 退款/审核（batchApprove/refund/confirmReturnReceived）—— 运营后端，非消费端。
- 支付宝渠道实际调起 —— 渠道受配置开关，默认 off；收银台仅展示已启用渠道，ALIPAY 调起契约未交付时不实现。
- 后端改动 —— 纯消费。

## Task Route

- Type: `implementation-only change`（业务设计已在 `docs/design/order-and-cart.md` 落地）
- Owner Docs: `docs/design/order-and-cart.md`（订单/支付/售后状态机）、`docs/backlog/mobile-frontend-roadmap.md`（M5 范围）
- Skill Selection Basis: 见各 phase。总体为 nop-chaos-flux 移动端纯消费，非 AMIS。

## Infrastructure And Config Prereqs

- M1-M4 已交付；本地后端运行。
- 真实微信支付依赖后端 `wxpay.enabled` 配置与 `/wxpay/notify` 回调可达；demo 模式（enabled=false）为移动端开发默认验证路径。
- 无新增密钥/迁移/回滚（密钥在后端配置）。

## Execution Plan

### Phase 1 - 支付收银台 + 支付结果页

Status: planned
Targets: `src/api/pay-api.ts`、`src/pages/pay/`（cashier/result）
Required Skill: `none`（nop-chaos-flux 移动端纯消费，非 AMIS；`nop-frontend-dev` 触发词 view.xml/AMIS 不匹配，`nop-backend-dev`/`nop-testing` 为后端/测试导向而本 phase 无后端改动、无新增 `@BizQuery`/`@BizMutation`。方法源：`flux-guide/design-patterns/conditional.md`（支付模式分支）、`flux-guide/mobile/countdown.md`（轮询/超时）、`docs/design/order-and-cart.md` 支付状态机）

- Item Types: `Add | Decision`
- Prereqs: M4（CREATED 订单 + 「去支付」入口）

- [ ] **Skill loading gate:** 通读 `flux-guide/design-patterns/conditional.md`（模式分支显隐）、`flux-guide/mobile/countdown.md`（轮询节奏/超时）、`flux-guide/mobile/README.md`（M0 触摸基线）；复阅 `docs/design/order-and-cart.md`（支付状态机 101→201）+ 实测契约 `PayService.isEnabled` + `LitemallOrderBizModel.getEnabledPayChannels/prepay/pay/payByBalance/payWithCombo`。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Decision D1（支付模式分支）：** 抉择——收银台先查 `PayService.isEnabled()`：`false`→demo 路径调 `pay(orderId)`（立即成功→结果页）；`true`→真实路径调 `prepay(orderId)` 取 `codeUrl`，H5 展示二维码供扫码（H5 无法原生调起，见 Non-Goals），随后轮询 `getMyOrder`（节流 + 超时上限）观察 orderStatus 101→201 翻转→结果页。备选（仅做 demo）——否决理由：roadmap M5 明确含「微信支付 Native 调起」。残留风险：H5 扫码体验弱于小程序，属已知产品边界（与 M1 Deferred 一致）。
- [ ] **Decision D2（余额/组合支付边界）：** 抉择——收银台通过 `getEnabledPayChannels` 展示已启用渠道，但**仅实现 demo `pay` + wxpay `prepay` 提交**；余额/组合渠道若被启用则展示「即将上线（钱包功能）」禁用态，实际提交 UI 归 M8（钱包）。备选（本 phase 实现余额支付）——否决理由：余额支付需钱包余额 + 登录密码复验 UI，属 M8 范围，跨计划拆分制造依赖。残留风险：渠道开关默认 BALANCE off，不影响主流程。
- [ ] **Add:** `pay-api.ts`（isEnabled/getEnabledPayChannels/pay/prepay）。
- [ ] **Add:** 收银台页（订单摘要 + 渠道列表 + 模式分支提交 D1/D2 + 取消返回订单详情）+ 轮询逻辑（真实模式节流轮询 + 超时降级「处理中」）。
- [ ] **Add:** 支付结果页（成功/失败/处理中三态，成功→订单详情，处理中→引导继续等待/查看订单）。
- [ ] **Proof:** vitest（isEnabled 分支：demo pay 立即成功 / 真实 prepay→codeUrl→轮询翻转 mock / 超时降级；渠道展示；结果页三态）；手动烟测（demo 模式下单→支付→结果；真实模式 codeUrl 展示）；`typecheck`+`build`+`lint`。

Exit Criteria:

- [ ] 收银台据 isEnabled 走 demo `pay` 或真实 `prepay`+轮询正确；支付结果三态落地
- [ ] 渠道来自 `getEnabledPayChannels`；余额/组合渠道为禁用占位（D2）
- [ ] 收银台/结果页交互元素满足 M0 44×44px；骨架/错/重试态覆盖
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯消费），IGraphQLEngine 后端测试项不适用
- [ ] owner doc 更新：若 `order-and-cart.md` 未声明 H5 支付边界（codeUrl 扫码、无小程序调起）则补一行；否则 No owner-doc update required
- [ ] `docs/logs/` 更新

### Phase 2 - 售后申请（仅退款 / 退货退款）

Status: planned
Targets: `src/api/aftersale-api.ts`、`src/pages/aftersale/apply.tsx`、`src/components/image-upload.tsx`（通用，平台 fileRef）
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/design-patterns/form.md`、`flux-guide/design-patterns/conditional.md`（type 显隐退货物流）、`docs/design/order-and-cart.md` 售后语义）

- Item Types: `Add | Decision`
- Prereqs: M4（订单详情/商品为售后入口）

- [ ] **Skill loading gate:** 通读 `flux-guide/design-patterns/form.md`、`flux-guide/design-patterns/conditional.md`；复阅 `docs/design/order-and-cart.md`（售后类型/原因/状态）+ 实测契约 `LitemallAftersaleBizModel.apply/cancel/submitReturnLogistics/userList/userDetail`（`AftersaleApplyRequest` 字段 + reason 枚举中文串 + type 0/1/2 依赖订单状态）+ 平台文件上传 fileRef 机制（`LitemallMaterialBizModel.uploadMaterial`）。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Decision D3（图片上传机制）：** 抉择——售后 pictures 走**平台文件上传端点**拿 `/f/download/{fileId}` 形式 fileRef，多图拼接后传 `apply.pictures`（对齐后端既有存储约定）；封装通用 `image-upload` 组件复用于 M6（评价图/头像）。备选（base64 内联）——否决理由：后端按 fileRef 解析，base64 不兼容。残留风险：平台文件上传端点确切路径执行时核对（Nop 标准 `/f/...`），若前端 fetcher 未覆盖 multipart 则按平台约定的 JSON fileRef 提交方式适配。
- [ ] **Add:** `aftersale-api.ts`（apply/cancel/submitReturnLogistics/userList/userDetail）+ `image-upload.tsx`（平台 fileRef 上传，多图 + 预览 + 删除）。
- [ ] **Add:** 售后申请页（选订单/商品 → type 选择 0/1/2 据 `order-and-cart.md` 状态约束显隐 → reason 枚举下拉（必为后端中文串）→ 金额 → 图片上传 D3 → 备注 → 提交 `apply`，校验+二次确认+防重复）。
- [ ] **Proof:** vitest（type 显隐 + reason 枚举映射 + image-upload fileRef mock + apply 入参 + 校验/防重复分支）；手动烟测（订单详情→申请售后→提交）；`typecheck`+`build`+`lint`。

Exit Criteria:

- [ ] 售后申请消费 apply 正确；type 0/1/2 + reason 枚举 + 图片 fileRef 上传落地
- [ ] image-upload 组件可复用（供 M6）；M0 44×44px 触摸基线满足
- [ ] 骨架/错/重试 + 防重复提交覆盖
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯消费），IGraphQLEngine 后端测试项不适用
- [ ] No owner-doc update required（售后类型/原因已在 `order-and-cart.md` 明确）
- [ ] `docs/logs/` 更新

### Phase 3 - 售后列表 + 详情 + 撤回 + 进度时间线 + 退货物流

Status: planned
Targets: `src/pages/aftersale/`（list/detail）
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/design-patterns/cards.md`、`flux-guide/design-patterns/timeline.md` 若有否则自建、`docs/design/order-and-cart.md` 售后状态机）

- Item Types: `Add`
- Prereqs: Phase 2

- [ ] **Skill loading gate:** 通读 `flux-guide/design-patterns/cards.md`、`flux-guide/02-reference.md`（时间线数据驱动渲染）；复阅 `docs/design/order-and-cart.md`（售后状态流转 REQUEST→...→完成/退款）+ 实测契约 `userList/userDetail/cancel/submitReturnLogistics`。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Add:** 售后列表页（`userList` 全量客户端展示 + 状态标签 + 入口详情）。
- [ ] **Add:** 售后详情页（申请信息 + 进度时间线据状态流转驱动 + 撤回 `cancel`（仅 REQUEST 态，二次确认）+ type=2 退货时 `submitReturnLogistics`（物流公司+单号）入口）。
- [ ] **Proof:** vitest（列表/详情/时间线状态驱动 mock + cancel 状态门禁 + submitReturnLogistics 接线）；手动烟测（申请→列表→详情→撤回 / 退货物流提交）；`typecheck`+`build`+`lint`。

Exit Criteria:

- [ ] 售后列表/详情消费 userList/userDetail 正确；撤回(状态门禁)+退货物流提交可用；进度时间线据状态驱动
- [ ] 交互元素满足 M0 44×44px；骨架/空/错/重试态覆盖
- [ ] 无新增 `@BizMutation`/`@BizQuery`（纯消费），IGraphQLEngine 后端测试项不适用
- [ ] No owner-doc update required
- [ ] `docs/logs/` 更新

## Plan Audit

- Status: pending
- Auditor / Agent: <独立 subagent（fresh session，非起草者）>
- Evidence: <待独立审计>

## Closure Gates

- [ ] in-scope behavior is complete
- [ ] relevant docs are aligned（H5 支付边界若涉 owner doc 则同步）
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

### 钱包余额支付 / 组合支付提交 UI

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: `payByBalance`/`payWithCombo` 需钱包余额 + 登录密码复验 UI，属 M8（钱包余额与充值）；M5 收银台展示渠道但余额/组合为禁用占位。
- Successor Required: `yes`（触发条件：M8 启动时）

### 微信小程序/JSAPI 原生调起

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本项目 H5 定位（`user-and-address.md:170`、M1 Deferred），真实支付仅 codeUrl 扫码；原生调起属微信小程序前端（远期/独立）。
- Successor Required: `yes`（触发条件：交付微信小程序前端时）

### 支付宝渠道实际调起

- Classification: `watch-only residual`
- Why Not Blocking Closure: 渠道受 `pay_channels` 配置开关，默认 ALIPAY off；后端 ALIPAY 调起契约未交付，收银台仅展示已启用渠道。
- Successor Required: `yes`（触发条件：后端交付 ALIPAY 调起契约且启用时）

## Closure

<!-- 闭合审计须由独立 subagent 执行（不同 session/context），此处留空。 -->

Status Note: <闭合时填>

Closure Audit Evidence:

- Reviewer / Agent: <独立 reviewer，MUST NOT be implementing agent>
- Evidence: <闭合时填>

Follow-up:

- 钱包余额/组合支付提交 UI（见 Deferred，M8 启动时）
- 微信小程序/JSAPI 原生调起（见 Deferred，交付小程序前端时）
- 支付宝渠道调起（见 Deferred，后端契约交付且启用时）
