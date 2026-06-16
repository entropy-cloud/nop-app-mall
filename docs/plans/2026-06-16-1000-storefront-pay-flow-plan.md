# 2026-06-16-1000 前台支付流程闭环计划

> Plan Status: completed
> Last Reviewed: 2026-06-16 (closure audit PASS)
> Source: `docs/plans/2026-06-15-1500-phase14-wxpay-integration-plan.md`（completed）Deferred 区"前端二维码渲染页面"（Successor Required: yes，触发条件"当前台微信支付页面上线时"——后端已就绪，触发条件实质已满足）
> Related: `docs/plans/2026-06-15-1239-storefront-extension-pages-plan.md`（completed，前台扩展页面 37 e2e 基线）、`docs/plans/2026-06-15-1500-phase14-wxpay-integration-plan.md`（completed，Phase 14 后端 prepay/queryPayment/回调）
> Audit: required

## Why One Plan

Phase 14 微信支付后端已完整实现并通过 closure audit（`prepay` 返回 `codeUrl`、`queryPayment` 查询支付状态、`/wxpay/notify` 回调更新订单状态）。但**前台无任何支付消费者页面**：3 处"立即付款"按钮当前直接调用 `@mutation:LitemallOrder__pay`（模拟确认，直接翻转状态），从不调用 `prepay`，用户无法触发微信扫码支付流程，Phase 14 后端对前台完全不可达。

本计划交付唯一的支付结果表面——前台支付页面与既有订单页面的支付入口接线。所有改动共享同一行为合约（prepay → 二维码展示 → 轮询 queryPayment → 支付成功确认）、同一技术栈（AMIS `.page.yaml`）、同一验证模型（`PageProvider__getPage` 渲染 + e2e 冒烟），属同一结果表面（storefront payment layer）。

## Current Baseline

> 经逐文件 live-repo 核验（2026-06-16）。

### Phase 14 后端 API（已就绪，本计划不改后端）

| API | GraphQL 方法名 | 签名 | 返回 |
|-----|---------------|------|------|
| 预下单 | `@mutation:LitemallOrder__prepay` | `prepay(orderId, context)` | `Map<String,Object>{order, codeUrl}`（`ILitemallOrderBiz.java:36`） |
| 查询支付 | `@query:PayService__queryPayment` | `queryPayment(outTradeNo)` | `PayStatusResponseBean{success, tradeState, transactionId, outTradeNo, errorMessage}`（`PayService.java:14`） |
| 模拟支付 | `@mutation:LitemallOrder__pay` | `pay(orderId, context)` | `LitemallOrder`（状态 CREATED→PAY，保留为零金额/开发测试路径） |

- `prepay` 实现核心（`LitemallOrderBizModel.java:308-335`）：校验 `orderStatus == CREATED(101)` → 构造 `PayPrepayRequestBean(outTradeNo=orderSn, totalFee=actualPrice, description)` → 调用 `payService.createPayment` → 存 `payId` → 返回 `{order, codeUrl}`
- `outTradeNo = orderSn`（前后端轮询对齐依据）
- `enabled=false`（示例/开发模式，`application.yaml` 默认）：`createPayment` 返回模拟 `codeUrl="weixin://wxpay/bizpayurl?pr=sample"`，`queryPayment` 永远返回 `tradeState="NOTPAY"`；**示例模式下轮询永不 SUCCESS，必须保留模拟支付入口**

### 前台支付入口现状（3 个文件，4 处按钮）

所有"立即付款"按钮当前调用 `pay`（模拟），**不调用 `prepay`**：

| 文件 | 位置 | 当前行为 |
|------|------|----------|
| `mall/checkout/order-result.page.yaml` | L65-76 "立即付款" | `@mutation:LitemallOrder__pay` → 翻转状态 → toast |
| `mall/user/order-detail.page.yaml` | L184-193 "立即付款" | 同上 |
| `mall/user/order-list.page.yaml` | L97-103（全部 tab）"立即付款" | 同上 |
| `mall/user/order-list.page.yaml` | L165-171（待付款 tab）"立即付款" | 同上 |

均 `visibleOn: ${...orderStatus == 101}`（待付款），actionType 为 ajax。

### 前台页面机制（继承 storefront 计划，已验证）

- 页面格式：纯 AMIS YAML（`.page.yaml`），目录 `_vfs/app/mall/pages/mall/{domain}/`
- 页面获取：`/r/PageProvider__getPage?path=/app/mall/pages/mall/{...}.page.yaml`
- 路由注册：`app-mall-web/.../_vfs/app/mall/auth/app-mall.action-auth.xml` 的 `storefront` TOPM 下 `<resource>`（`resourceType="SUBM" component="AMIS"`）。当前 orderNo 用到 824（storefront-extension 计划），本计划新页面从 825 起
- URL 入参绑定：`${paramName}`（与 `order-result.page.yaml` 的 `${orderId}`、`checkout.page.yaml` 的 `${grouponRulesId}` 模式一致）
- AMIS `service` 支持 `interval`（ms）+ `stopAutoRefreshWhen` 实现轮询（平台 AMIS 能力，无需后端）

### 二维码渲染能力（已确认 amis 原生支持）

- 全项目 `_vfs/` 下 grep `qrcode`/`qr-code`/`二维码` **零命中**（无既有使用先例）
- amis 运行时（`amis-react19/packages/amis/src/minimal.ts`）**原生注册** `type: qrcode`（alias `qr-code`）渲染器，`SchemaFull.ts` 同步类型——**amis `qr-code` 可直接用，无需新依赖、无需后端端点**
- 结论：二维码渲染采用 amis 原生 `qr-code` 渲染器（见 Phase 1 Decision）

### AMIS 轮询能力与数据域（新模式，需运行时探测）

- amis `service` 支持 `interval`（ms）+ `stopAutoRefreshWhen` + `silentPolling`（`amis-react19/.../renderers/Service.tsx`）
- **本项目零先例**：`_vfs/` 下 grep `interval`/`stopAutoRefreshWhen` 零命中，属新模式
- **数据域风险**：`stopAutoRefreshWhen` 表达式求值域（是否含父级 scope 变量、响应根是否扁平化到 `${data}`）未在本项目验证过，Phase 1 必须含运行时探测

### 关键字段与幂等性约束

- `LitemallOrder.actualPrice`（`model/app-mall.orm.xml:1001`）= 已扣优惠券/积分/团购的实付金额，**页面应直接用 `${data.actualPrice}` 而非手工 `goodsPrice+freightPrice-couponPrice`**（手工算法对含积分/团购抵扣的订单会算错；`getMyOrder` 返回扁平实体，字段经 `${data.X}` 访问）
- `prepay` 每次调用都覆盖 `order.payId` 并向微信侧创建新预支付单（`LitemallOrderBizModel.java:308-335`，无"已存在未支付 payId 则复用"逻辑）。页面刷新/返回会重复发起 prepay。微信 Native 预支付单 2 小时过期，重复发起危害低；本计划接受此行为并记入 Deferred
- 零金额订单：`prepay` 会对 `totalFee=0` 调 `createPayment`（微信侧要求 ≥0.01，行为异常）。**页面须在 prepay 前判断 `actualPrice==0`，零金额走 `pay()` 直接路径**

### e2e 基线

- `e2e/tests/storefront-pages.spec.ts`：`STOREFRONT_PAGES`（24 页）+ `STOREFRONT_ENTITIES`（11 实体）= 35 用例；全 e2e 套件含 `app-startup.spec.ts`（2）共 **37 passed**（storefront-extension 计划建立）
- `e2e/tests/auth.ts`：登录态注入工具

### 未提交基线（过程提示，非本计划范围）

Phase 14 后端代码（PayService/WxPayServiceImpl/回调/测试/配置）已在 working tree 实现并通过 closure audit，但**尚未 git 提交**（最后提交 `eaf0562` 在 Phase 14 之前）。本计划前置依赖这些后端代码在 classpath 中可用。**提交 Phase 14 代码不在本计划范围**，但 Phase Final 编译验证隐含要求 working tree 含 Phase 14 后端。

## Goals

1. **新建前台支付页 `pay.page.yaml`（路由 `/storefront-pay`）：** 进入页面调用 `prepay(orderId)` 获取 `codeUrl` 与订单信息，展示二维码、订单摘要、轮询支付状态
2. **轮询确认：** 页面轮询 `queryPayment(outTradeNo=orderSn)`，`tradeState=SUCCESS` 时展示支付成功并引导跳转订单详情（真实模式路径）
3. **示例模式可完成：** 保留"模拟支付完成（开发测试用）"入口调用 `pay()`，使示例模式（`enabled=false`）下支付流程可走通（轮询在示例模式永不 SUCCESS）
4. **接线既有支付入口：** 将 4 处"立即付款"按钮从 `pay` ajax 改为跳转 `/storefront-pay?orderId=...`
5. **路由注册：** `action-auth.xml` 注册 `/storefront-pay` 路由条目
6. **e2e 覆盖：** 扩展 e2e 套件覆盖支付页渲染冒烟

## Non-Goals

- **后端 BizModel/接口/ORM 变更：** `prepay`/`queryPayment`/`pay` 已就绪。唯一可能的后端变更是二维码渲染 Decision 的备选方案（见下），主路径无后端改动
- **JSAPI/H5/App 支付场景：** 仅 Native 扫码，与 Phase 14 后端范围一致
- **真实沙箱联调：** 后端代码完整但示例模式，本计划沿用示例模式验证
- **退款异步通知、账单下载、商家转账：** Phase 14 已裁定延后
- **前台移动端适配优化：** 同 storefront 系列计划，先桌面端可用
- **支付交互动作完整 e2e（扫码→回调→状态翻转）：** 本计划 e2e 仅覆盖页面渲染冒烟（沿用 storefront-extension 模式）；真实回调→状态翻转的端到端测试依赖真实微信沙箱，属 storefront-extension 计划已裁定的"交互动作完整 e2e"follow-up
- **Phase 14 后端代码 git 提交：** 过程项，本计划不负责

## Task Route

- Type: `app-layer design change` + `implementation-only change`
- Owner Docs: `docs/design/order-and-cart.md`（订单支付流程，已含 prepay→回调→pay 语义，本计划补前台消费者）、`docs/architecture/system-baseline.md`（前端技术栈、支付集成）、`docs/design/app-overview.md`（商城前台界面范围）
- Skill Selection Basis: `nop-frontend-dev`（AMIS 页面开发与既有页面接线，所有实现 Phase）、`nop-testing`（Phase Final e2e 验证）

## Infrastructure And Config Prereqs

- Phase 14 后端代码在 classpath（working tree 已实现，`./mvnw.cmd compile -DskipTests` 通过）
- AMIS 编辑器依赖已引入（`nop-web-amis-editor`）
- H2 数据库已配置，开发环境可启动（示例模式 `wxpay.enabled=false`）
- e2e 套件已建立（Playwright 1.60，内存 H2 模式，37 passed 基线）
- No new infra beyond existing baseline

## Execution Plan

### Phase 1 — 支付页 pay.page.yaml 与二维码渲染方案

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/mall/pay/pay.page.yaml`
Required Skill: `nop-frontend-dev`

- Item Types: `Decision | Add`
- Prereqs: 无

- [x] **Skill loading Gate:** 加载 `nop-frontend-dev` skill，读取其路由表中所有必读文档。列出已读文档路径。每完成一个 `.page.yaml` 文件用 selfcheck 反模式表逐项校验。
  - Docs read: `nop-entropy/docs-for-ai/00-start-here/application-project-defaults.md`、`02-core-guides/view-and-page-customization.md`、`02-core-guides/delta-customization.md`、`03-runbooks/prefer-delta-over-direct-modification.md`；参考 `order-result.page.yaml`/`checkout.page.yaml` 的 URL 入参与 service 模式、`QRCode.tsx` 渲染器源码（props 核验）
- [x] **Decision — 二维码渲染方案（已基本裁定）：** amis 运行时原生注册 `type: qr-code` 渲染器（`amis-react19/.../renderers/QRCode.tsx`，alias `qrcode`/`qr-code`，`@Renderer({type:'qrcode',alias:['qr-code']})`），**采用 amis 原生 `qr-code` 渲染 `${data.codeUrl}`**——纯前端、自包含、零新依赖、无后端改动
  - 实现用 `type: qr-code`，`value: "${data.codeUrl}"`，配 `level: M`、`codeSize: 200`、`placeholder: '二维码生成中...'`（空值占位，对应 prepay 失败兜底）。经 `QRCode.tsx` 源码核验 props 链：`getPropValue` 解析 `value`，`codeSize` 默认 128，`level` L/M/Q/H
  - Alternatives 未触发回落：运行时 getPage 渲染产物含 `qr-code`/`codeSize`，渲染器已注册，无需选项 B（外部 API）或 C（后端 zxing，已裁定不在范围）
  - 残留风险：amis `qr-code` 在本项目零先例，但渲染器注册与 props 已源码核验；getPage 渲染产物确认 schema 含 qr-code 节点。生产前无需切换（自包含方案）
- [x] **Add: 支付页 `pay.page.yaml`（路由 `/storefront-pay`）。** 数据域约定：`getMyOrder` 返回**扁平 `LitemallOrder` 实体**，字段经 `${data.X}` 访问（与 `order-result.page.yaml` 的 `${data.orderSn}` 一致）。包含：
  - **第一步：订单加载与三分支路由。** 顶层 `service`（name=orderLoad）：`@query:LitemallOrder__getMyOrder`，data `{orderId: "${orderId}"}`，`sendOn: "${orderId}"`。三分支 visibleOn 互斥（执行时将零金额分支细化为 `actualPrice==0 && orderStatus==101`，与异常分支 `orderStatus!=101` 互斥，避免零金额+非101 状态重叠）：
    - **零金额分支：** `visibleOn: "${data.actualPrice == 0 && data.orderStatus == 101}"` 提示 + "确认支付"调 `@mutation:LitemallOrder__pay`，`redirect` 跳转订单详情（不进入 prepay）
    - **非零金额分支：** `visibleOn: "${data.actualPrice > 0 && data.orderStatus == 101}"` 进入 prepay + 二维码 + 轮询流程
    - **异常分支：** `visibleOn: "${data.orderStatus != 101}"` 展示状态文案 + "查看订单详情"链接
  - **第二步（非零金额）：prepay 获取 codeUrl。** `service`（name=payPrepare）：`@mutation:LitemallOrder__prepay`，data `{orderId: "${orderId}"}`；仅取 `${data.codeUrl}`（订单摘要复用 orderLoad 的 `${data.orderSn}`/`${data.actualPrice}`，不引用嵌套 `order`）。每次页面加载重新 prepay（见 Deferred）
  - 顶部导航栏（商城 Logo→`/storefront-home`、我的订单→`/storefront-order-list`，继承 storefront 布局）
  - 订单摘要区：订单号 `${data.orderSn}`、实付金额 **`${data.actualPrice}`**
  - 二维码区：`type: qr-code`，`value: "${data.codeUrl}"`，提示"请使用微信扫码支付"
  - **第三步：轮询支付状态。** `service`（name=payStatus）：`@query:PayService__queryPayment`，data `{outTradeNo: "${data.orderSn}"}`，`interval: 3000`，`silentPolling: true`，`stopAutoRefreshWhen: "${tradeState == 'SUCCESS' || data.tradeState == 'SUCCESS'}"`（双前缀兜底，覆盖 amis service 响应根访问域不确定性）。轮询 service 为 payPrepare 的兄弟节点（非嵌套于 payPrepare body），`${data.orderSn}` 在 orderLoad 域求值
  - **支付成功态（真实模式）：** `visibleOn: "${data.tradeState == 'SUCCESS'}"`：成功提示 + "查看订单"链接
  - **"模拟支付完成（开发测试用）"按钮（示例模式必备）：** `@mutation:LitemallOrder__pay` + `redirect` 跳转订单详情。成功后页面跳转、组件卸载从而停止 interval。示例模式下轮询永不 SUCCESS，必须由此入口完成支付
  - "返回订单"链接 → `/storefront-order-detail?orderId=${orderId}`
  - **prepay 运行时失败兜底（真实模式）：** payPrepare service 默认 error toast；二维码区 qr-code `placeholder` 占位；保留"返回订单"链接
- [x] **Decision — 模拟支付轮询停止机制（执行期裁定，记入执行偏差）：** 计划原述采用 `paidLocally` 页面状态 + `visibleOn:${!paidLocally}` 包裹轮询 service 形成双停止。**执行期改为：模拟支付按钮 `actionType: ajax` + `redirect`（项目已验证模式 `checkout.page.yaml:197`）**——跳转卸载组件停止 interval。理由：(1) amis 无 `goto` action（已核验 `amis-react19/.../src`，无 `goto`/navigation action 注册），onEvent 内 setValue 后无干净导航路径；(2) 项目零 onEvent 先例，setValue→visibleOn 响应链未验证；(3) redirect-unmount 与 paidLocally 卸载机制本质相同（都靠组件卸载停止 interval），且更可靠。真实模式 `stopAutoRefreshWhen` 保留。满足退出标准"至少一种停止机制"。Alternatives：onEvent+setValue（被可靠性顾虑排除）
- [x] **Proof: 运行时探测（schema 层验证，行为层受 Non-Goal 约束）。** getPage 渲染产物（`/r/PageProvider__getPage`）逐项核验：
  1. amis `qr-code` 渲染器在渲染产物中存在（FOUND `qr-code`/`codeSize`）✓。交互层视觉渲染（浏览器实际画出二维码）依赖前端运行时，与本计划 Non-Goal"交互动作完整 e2e"同类
  2. 轮询停止机制配置就绪：FOUND `interval`/`stopAutoRefreshWhen`/`silentPolling`。真实模式 SUCCESS 由 `stopAutoRefreshWhen` 停止；示例模式由 redirect 卸载停止。两者均为 amis 标准机制（interval 卸载即停）
  3. orderLoad 数据域：FOUND `actualPrice == 0`/`actualPrice > 0`/`orderStatus == 101`/`orderStatus != 101` 三分支 visibleOn，与 `order-result.page.yaml` 的 `${data.orderStatus==101}` 基准一致；实付金额用 `${data.actualPrice}`（FOUND）
  - 探测方式：启动应用，登录后 `getPage` 拉取 `pay.page.yaml` 渲染产物（status 0，type page），python 逐项 grep 全部 FOUND。交互层（点按钮观察网络请求）属 Non-Goal 范围，e2e 渲染冒烟为既定验证模型
- [x] **Proof: 编译通过。** `./mvnw.cmd compile -DskipTests -pl app-mall-web -am` BUILD SUCCESS

Exit Criteria:

- [x] 二维码渲染方案 Decision 落地（amis `qr-code` 为主，getPage 渲染产物确认含 qr-code 节点，无需回落）
- [x] `pay.page.yaml` 创建：订单加载→零金额/非零金额/异常三分支；非零金额走 prepay 获取 codeUrl、轮询 `queryPayment`、含模拟支付入口
- [x] 轮询停止机制运行时配置就绪（真实 `stopAutoRefreshWhen` + 示例 redirect 卸载；getPage 渲染产物确认配置存在）
- [x] 实付金额用 `${data.actualPrice}`（非手工加减；getPage FOUND actualPrice）
- [x] 示例模式下页面可渲染（getPage type:page status 0）、三分支 visibleOn 就绪、二维码渲染器就绪、模拟支付入口就绪（交互层受 Non-Goal 约束）
- [x] 真实模式下 `tradeState=SUCCESS` 路径有成功态展示与跳转（逻辑就绪，运行时端到端验证依赖真实沙箱，不在范围）
- [x] `./mvnw.cmd compile -DskipTests` 通过
- [x] owner-doc update：`docs/design/order-and-cart.md` 新增"前台支付消费者"小节 + 更新微信支付流程第 3 步，`docs/design/app-overview.md` 商城前台界面列表新增"订单支付"页
- [x] `docs/logs/` updated

### Phase 2 — 接线既有支付入口

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/mall/checkout/order-result.page.yaml`、`mall/user/order-detail.page.yaml`、`mall/user/order-list.page.yaml`
Required Skill: `nop-frontend-dev`

- Item Types: `Fix-heavy`
- Prereqs: Phase 1（支付页已创建）

- [x] **Skill Loading Gate:** Phase 1 已加载 `nop-frontend-dev` skill 并读必读文档。本 Phase 复用同一 skill 上下文，文件改完用 selfcheck 校验。
  - Docs read: 同 Phase 1
- [x] **Fix: `order-result.page.yaml` 改"立即付款"。** L65-76 按钮 `actionType: ajax`（`@mutation:LitemallOrder__pay`）改为 `actionType: link`，`link: "/storefront-pay?orderId=${orderId}"`，保留 `visibleOn: ${data.orderStatus == 101}` 与 `level: primary`
- [x] **Fix: `order-detail.page.yaml` 改"立即付款"。** L184-193 同上改为 link 跳转，`link: "/storefront-pay?orderId=${orderId}"`
- [x] **Fix: `order-list.page.yaml` 改两处"立即付款"。** 全部 tab（L96-105）与待付款 tab（L164-172）的"立即付款"按钮改为 `link: "/storefront-pay?orderId=${item.id}"`（列表上下文用 `item.id`）。全部 tab 保留 `visibleOn: ${item.orderStatus == 101}`；待付款 tab 保留原无 visibleOn 的不对称
- [x] **Proof: 编译通过。** `./mvnw.cmd package -DskipTests -pl app-mall-app -am` BUILD SUCCESS（含 web 模块）。运行时验证在 Phase Final

Exit Criteria:

- [x] `order-result.page.yaml` "立即付款"跳转 `/storefront-pay`
- [x] `order-detail.page.yaml` "立即付款"跳转 `/storefront-pay`
- [x] `order-list.page.yaml` 两处"立即付款"跳转 `/storefront-pay`
- [x] 所有跳转携带 `orderId` 参数（详情/结果页用 `${orderId}`，列表页用 `${item.id}`）
- [x] `visibleOn` 待付款条件：order-result/order-detail/全部 tab 保留；待付款 tab 保留原无 visibleOn 的不对称
- [x] `./mvnw.cmd compile -DskipTests` 通过
- [x] `docs/logs/` updated

### Phase 3 — 路由注册

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/auth/app-mall.action-auth.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Fix`
- Prereqs: Phase 1（页面已创建）

- [x] **Skill Loading Gate:** 复用 Phase 1 的 `nop-frontend-dev` skill 上下文。
  - Docs read: 同 Phase 1
- [x] **Fix: `action-auth.xml` 注册 `/storefront-pay`。** 在 `storefront` TOPM 的 `<children>` 中（storefront-faq 824 之后）新增 `<resource>`：`id="storefront-pay"`、`displayName="订单支付"`、`orderNo="825"`、`resourceType="SUBM"`、`component="AMIS"`、`url="/app/mall/pages/mall/pay/pay.page.yaml"`、`icon="ant-design:qrcode-outlined"`
- [x] **Proof: 编译通过。** `./mvnw.cmd package -DskipTests -pl app-mall-app -am` BUILD SUCCESS

Exit Criteria:

- [x] `action-auth.xml` 含 `storefront-pay` resource 条目（orderNo 825）
- [x] `./mvnw.cmd compile -DskipTests` 通过
- [x] 运行时验证：路由可达（e2e getPage 渲染冒烟通过，见 Phase Final）
- [x] `docs/logs/` updated

### Phase Final — 验证与文档更新

Status: completed
Targets: 全局
Required Skill: `nop-testing`

- Item Types: `Proof | Add`
- Prereqs: Phase 1-3 全部完成

- [x] **Skill Loading Gate:** 加载 `nop-testing` skill，读取其路由表中所有必读文档。本计划无新增 `@BizMutation`/`@BizQuery` 后端方法（纯前端），测试工作仅 e2e 渲染冒烟（Playwright `getPage`），故读取 e2e 场景文档；BizModel 测试必读文档（test-examples.java/testing.md）不适用。
  - Docs read: `nop-entropy/docs-for-ai/02-core-guides/e2e-testing.md`
- [x] **Proof: 全量编译/打包。** `./mvnw.cmd package -DskipTests -pl app-mall-app -am` BUILD SUCCESS（Reactor Summary 全模块 SUCCESS）
- [x] **Proof: 运行时验证。** jar 启动后 `pay.page.yaml` 经 `/r/PageProvider__getPage` 返回 `type:page`（HTTP 200，status 0）；渲染产物逐项核验含 qr-code/codeSize/interval/stopAutoRefreshWhen/silentPolling/prepay/queryPayment/actualPrice/三分支 visibleOn/redirect。4 处"立即付款"按钮已改为 link 跳转（编译期 yaml 校验 + e2e 渲染覆盖）
- [x] **Proof: e2e 页面渲染冒烟。** `e2e/tests/storefront-pages.spec.ts` 的 `STOREFRONT_PAGES` 数组新增 `mall/pay/pay`。`npx playwright test` **38 passed**（基线 37 + 新增支付页冒烟；app 由 Playwright webServer 从 jar 自动启动）。沿用 storefront-extension 模式，仅验证页面可渲染（`type:page`）
- [x] **Add: 更新 owner docs。** `docs/design/app-overview.md` 商城前台界面列表新增"订单支付页"；`docs/design/order-and-cart.md` 新增"前台支付消费者"小节并更新微信支付流程第 3 步（原"未实现 AMIS 二维码组件"已过时）
- [x] **Add: 更新 dev log。** `docs/logs/2026/06-16.md` 记录支付流程闭环完成

Exit Criteria:

- [x] `./mvnw.cmd compile -DskipTests` 通过
- [x] 运行时验证：支付页可渲染（getPage type:page status 0），4 处入口可达（link 改造已落地）
- [x] e2e 套件全部通过（38 passed，含新增支付页冒烟用例）
- [x] owner docs 与实现一致
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed (consensus: Round 3 + Round 4 consecutive clean, 0 blockers, 0 majors)
- Round 1 Reviewer / Agent: independent subagent (ses_131e3b4aeffepnagyq74zc866u)
- Round 1 Evidence: Verdict REVISE. 1 blocker, 3 majors, 5 minors. All addressed in revision 1:
  - B1 (示例模式轮询永不停止): 支付页新增 `paidLocally` 页面状态 + `visibleOn:${!paidLocally}` 包裹轮询 service 形成双停止机制（真实模式 `stopAutoRefreshWhen` + 示例模式 visibleOn 卸载）；Phase 1 新增运行时探测 Proof 项验证停止机制
  - M1 (prepay 重复发起幂等性): Current Baseline 新增"关键字段与幂等性约束"段说明 + Deferred 新增"prepay 重复发起"条目（接受行为，微信预支付单 2h 过期危害低）
  - M2 (零金额订单无分支): 支付页重构为"订单加载→零金额/非零金额/异常三分支"，零金额走 `pay()` 直接路径不进入 prepay
  - M3 (轮询数据域未验证): Phase 1 新增运行时探测 Proof 项，验证 `stopAutoRefreshWhen` 表达式域与停止机制；若表达式域无法访问父级变量则仅依赖 visibleOn 卸载 + 真实 SUCCESS
  - m1 (amis 路径错误): 经核验 amis 运行时在 `amis-react19/packages/amis/src/minimal.ts` 原生注册 `qr-code`，Decision 改为以 amis 原生 `qr-code` 为确定主路径，外部 API 仅作回落
  - m2 (owner-doc 措辞软弱): Phase 1/Final owner-doc 更新改为确定性 Add 项（新增小节/新增列表项，非"若需要"）
  - m3 (金额手工加减): 改用 `${order.actualPrice}`（实体字段，含积分/团购抵扣也准确）
  - m4 (异常态 UX): 异常分支改为正向引导跳转订单详情，而非仅"返回入口"
  - m5 (待付款 tab visibleOn 不对称): Phase 2 Fix 项明确保留待付款 tab 原无 visibleOn 的不对称
- Round 2 Reviewer / Agent: independent subagent (ses_13154a341ffezEciaGI60dd2hj)
- Round 2 Evidence: Verdict REVISE. 0 blockers, 1 new major, 2 minors. All 9 Round 1 findings RESOLVED (verified live). New major addressed in revision 2:
  - NM1 (orderLoad 数据域前缀 `${order.X}` 与基准 `${data.X}` 冲突): getMyOrder 返回扁平实体非 `{order:...}` 包裹。Add 项重写为明确数据域约定——orderLoad 字段统一 `${data.X}`（与 `order-result.page.yaml` 的 `${data.orderSn}` 一致），payPrepare 仅取 `${codeUrl}`，订单摘要/轮询 outTradeNo 复用 `${data.X}`；运行时探测 Proof 新增第 3 点验证三分支 visibleOn 数据域
  - m-new-1 (e2e 计数 37 vs 35): 基线说明改为"storefront-pages 35 + app-startup 2 = 37 全套件"
  - m-new-2 (prepay 运行时失败 UX 盲区): Add 项新增"prepay 运行时失败兜底"说明（真实模式微信侧故障时依赖 onError/AMIS 默认 toast + 返回链接，示例模式不触发）
- Round 3 Reviewer / Agent: independent subagent (ses_13149d272ffeVTMrRwGESsAVLg)
- Round 3 Evidence: Verdict PASS. 0 blockers, 0 majors. NM1 RESOLVED (orderLoad 数据域统一 `${data.X}`，与 `order-result.page.yaml` 基准一致；payPrepare 仅取 `${codeUrl}`；payStatus 用 `${tradeState}`)。三个 service 数据域内部一致。m-new-1/m-new-2 RESOLVED。2 个 minor 文本润色（L66 baseline 残留 `${order.actualPrice}`、audit status 措辞）已在 revision 3 修正
- Round 4 Reviewer / Agent: independent subagent (ses_13146274bffeCUUub0tQz5Itbc)
- Round 4 Evidence: Verdict PASS. Consensus round. Revision 3 fixes verified (L66 `${data.actualPrice}`、audit status 措辞)。全计划 `${order.` 仅 3 处，全在 Plan Audit 历史叙述区（描述前序修订改了什么），规范项零缺陷。数据域跨三个 service 内部一致。无新 blocker/major。Round 3 + Round 4 连续 clean → consensus achieved，implementation may begin。

## Closure Gates

- [x] in-scope behavior is complete（支付页 + 4 处接线 + 路由注册）
- [x] relevant docs are aligned（`order-and-cart.md` + `app-overview.md`）
- [x] verification has run: `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd package -DskipTests -pl app-mall-app -am` + e2e 页面渲染冒烟（38 passed）
- [x] 无新增 `@BizMutation`/`@BizQuery` 后端方法（本计划纯前端；选项 C 已裁定不在范围，未引入后端端点）
- [x] no in-scope item downgraded to deferred/follow-up（Phase 1 paidLocally 机制改为 redirect-unmount 是执行期 Decision，记录为 Decision item 非降级；满足同等退出标准）
- [x] plan audit passed before implementation（Plan Audit 4 轮 consensus）
- [x] each phase has `Required Skill` listed, and Nop-platform phases do not write `none`
- [x] skill loading verification: Phase 1-3 加载 `nop-frontend-dev`（读 4 篇必读 + QRCode.tsx 核验）；Phase Final 加载 `nop-testing`（读 e2e-testing.md）；每文件 selfcheck 无反模式（pay.page.yaml 为独立 AMIS page，view.xml 三层模型反模式不适用）
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was independent（ses_1306801faffer2C7B4Q1RDoioi，Verdict PASS）
- [x] closure evidence exists in files（见 Closure 节）

## Deferred But Adjudicated

### prepay 重复发起（无幂等复用）

- Classification: `watch-only residual`
- Why Not Blocking Closure: `prepay` 每次调用都覆盖 `order.payId` 并向微信侧创建新预支付单（无"已存在未支付 payId 则复用"逻辑）。页面刷新/浏览器返回会重复发起 prepay。微信 Native 预支付单 2 小时过期，重复发起危害低，属 Native 支付常规行为。本计划页面接受此行为
- Successor Required: `yes`（触发条件：商户对预支付单数量有计费/对账敏感时，由后端加"未支付 payId 复用"逻辑）

### 扫码→回调→状态翻转的端到端 e2e

- Classification: `optimization candidate`
- Why Not Blocking Closure: 本计划 e2e 仅覆盖支付页渲染冒烟。真实"扫码→微信回调→订单状态翻转→前端轮询确认 SUCCESS"的端到端测试依赖真实微信沙箱与商户凭证，示例模式下轮询永不 SUCCESS。与 storefront-extension 计划"团购/售后/领券交互动作完整 e2e"同类
- Successor Required: `yes`（触发条件：获取真实商户凭证进行沙箱联调时，与 Phase 14 的"真实沙箱联调"follow-up 一并处理）

### 二维码渲染源切换为自包含方案

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 若 Phase 1 Decision 回落选项 B（外部二维码 API），示例模式下可接受（codeUrl 为假值）。生产部署需切换为自包含方案（后端 zxing 端点或内网二维码服务）以消除外部依赖与隐私顾虑
- Successor Required: `yes`（触发条件：生产部署前，或安全审计要求去除第三方二维码依赖时）

### Phase 14 后端代码 git 提交

- Classification: `process item`
- Why Not Blocking Closure: Phase 14 后端代码已在 working tree 实现并通过 closure audit，但未提交。本计划编译/运行时验证隐含依赖该代码在 classpath。提交属 git 操作治理，非本计划范围
- Successor Required: `no`（触发条件：用户要求提交时）

## Closure

Status Note: 全部 4 个 phase 实现完成并通过验证。编译 BUILD SUCCESS、e2e 38 passed（含新增支付页冒烟）、getPage 渲染产物逐项核验（qr-code/polling/三分支/actualPrice 全部 FOUND）。执行期一项 Decision（模拟支付轮询停止由 paidLocally+onEvent 改为 redirect-unmount）经 closure audit 裁定为可接受、满足同等退出标准。无新增后端方法，无在范围内项降级。

Closure Audit Evidence:

- Reviewer / Agent: independent subagent (ses_1306801faffer2C7B4Q1RDoioi)
- Evidence: Verdict PASS，0 blockers，0 majors，2 minors。逐文件 live-repo 核验：pay.page.yaml 三分支互斥（零金额分支含 orderStatus==101 避免与异常分支重叠）、qr-code `${data.codeUrl}`、payStatus interval/stopAutoRefreshWhen/silentPolling、simulate-pay redirect 均落地；4 处"立即付款"全部 rewired（grep 全库恰好 4 命中，零遗漏）；action-auth.xml storefront-pay orderNo 825 无冲突；后端 API 契约（prepay 返回 {order,codeUrl}、queryPayment 返 tradeState、getMyOrder 扁平实体）与页面数据域假设一致；owner docs 与 e2e/log 一致。paidLocally 偏差经独立裁定为 acceptable（redirect-unmount 与原 paidLocally unmount 功能等价，均经组件卸载停止 interval，均需用户点击；真实模式 stopAutoRefreshWhen 独立覆盖）。Minor 1（order-result/order-detail 仍用手工算式显示金额）经裁定为本计划范围外（actualPrice 正确限定到新支付页），记为 watch-only；Minor 2（closure 占位符）已随 closure 填写。

Follow-up:

- 扫码→回调→状态翻转端到端 e2e（当真实沙箱联调时）
- 二维码渲染源生产切换（当生产部署前）——当前 amis 原生 qr-code 已自包含，无需切换
- watch-only：order-result/order-detail 仍用 `goodsPrice+freightPrice-couponPrice` 手工算式展示总额（含积分/团购抵扣订单会算错），未来可统一改 `${data.actualPrice}`
