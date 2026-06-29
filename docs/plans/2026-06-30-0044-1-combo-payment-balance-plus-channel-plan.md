# combo-payment 组合支付（余额抵扣 + 第三方通道补差，含安全退款）

> Plan Status: completed
> Last Reviewed: 2026-06-30
> Source: deferred successor — P30 多支付通道（`2026-06-28-1822-1-phase30-multi-payment-channels-plan.md` Deferred「跨通道组合支付（余额 + 第三方混合）」，触发条件「业务要求余额抵扣 + 第三方补差组合支付时」；本计划由 MISSION_DRIVER「从 deferred items 拉取下一批计划」指令授权拉前）
> Related: `2026-06-28-1822-1-phase30-multi-payment-channels-plan.md`, `2026-06-28-1400-1-phase29-wallet-recharge-plan.md`, `2026-06-29-0330-1-wallet-recharge-refund-flow-plan.md`
> Audit: required

## Current Baseline

P30 多支付通道已交付**单通道全额支付**：每笔订单在收银台只能选一个通道（余额 / 微信 / 支付宝[enabled=false 示例]）一次付清。组合支付（余额抵扣一部分 + 第三方通道补差剩余）的编排缺失。Live-repo 证据：

- `LitemallOrder.walletPayAmount` 字段已存在（P30），语义支持「余额抵扣 + 第三方补差」，但无代码写入「部分余额」——当前只有 `payByBalance` 写 `walletPayAmount = actualPrice`（全额）。
- `LitemallOrderBizModel.pay()`（`:777-802`）：手工/示例路径，`actualPrice>0 && payService.isEnabled()` 时抛 `ERR_ORDER_USE_REAL_PAYMENT` 强制 prepay→微信→异步通知；否则 `markOrderPaidCore`。仅守 `status==101`。
- `LitemallOrderBizModel.payByBalance()`（`:830-883`）：余额**全额**支付——校验状态(101)/归属/凭证 → `walletBiz.debitBalance(actualPrice, PAY, sourceType=pay, orderSn)` → 写 `walletPayAmount=actualPrice`+`payChannel=BALANCE` → `markOrderPaidCore`。仅守 `status==101`。
- `LitemallOrderBizModel.markOrderPaidCore()`（`:817-826`）：共享「已支付」尾部（设 ORDER_STATUS_PAY + payTime + 持久化 + afterCommit 通知）。
- `LitemallOrderBizModel.prepay()`（`:745-773`）：`totalFee = order.actualPrice`（全额），仅守 `status==101`。
- `LitemallOrderBizModel.confirmPaidByNotify()`（`:962-987`）：微信异步通知可信入口，幂等（非 101 no-op），设 `payChannel=WECHAT` 后 `markOrderPaidCore`——**不触碰 `walletPayAmount`**。
- `LitemallOrderBizModel.cancel()`（`:699-741`）与 `cancelExpiredOrders()`（`:2315-2371`）：CAS 守卫（101→CANCEL/AUTO_CANCEL）后逐项反转还库/还券/还积分/释放满减，**不涉及钱包**（单通道下 CREATED 订单未扣余额）。
- `LitemallOrderBizModel.cancelExpiredPickupOrders()`（`:2470-2497`）：**已支付**自提订单超时退款——`payChannel==BALANCE` 走 `walletBiz.creditBalance(actualPrice, REFUND, sourceType=order-refund)`，其余通道走 `payService.refund(totalFee=actualPrice, refundFee=actualPrice)`。**不感知 `walletPayAmount`**。
- `LitemallOrderBizModel` 已有私有常量 `SOURCE_TYPE_ORDER_REFUND = "order-refund"`（`:2538`），用于整单退款的钱包回冲。
- 退款侧（**已支付**订单）现状——均不感知 `walletPayAmount`，组合订单会丢钱包部分并对通道超额退款（全站共 5 处 `payService.refund` 退款点，与 `ILitemallOrderBiz` `releasePromotionUsage` javadoc 所列整单退款站点一致）：
  - `LitemallAftersaleBizModel.refund()`（`:157-169`）：`payService.refund(totalFee=actualPrice, refundFee=entity.getAmount())`（refundFee 为售后金额，可整单可单项）。
  - `LitemallAftersaleBizModel.confirmReturnReceived()`（`:309-320`）：同上。
  - `LitemallOrderBizModel.cancelExpiredPickupOrders()`（`:2484-2497`）：已支付自提订单超时退款（非 BALANCE 分支）。
  - `LitemallGrouponBizModel.refundGrouponOrder()`（`:257-259`）：`payService.refund(totalFee=actualPrice, refundFee=actualPrice)` 整单。
  - `LitemallPinTuanActivityBizModel.refundMemberOrder()`（`:366-368`）：拼团失败整单退款（守 `ORDER_STATUS_PAY`），`payService.refund(totalFee=actualPrice, refundFee=actualPrice)`，结构同 refundGrouponOrder。
- `LitemallWalletBizModel`：`debitBalance`/`creditBalance`（`@BizAction` 原子，乐观锁）就绪。
- 前端 `app-mall-web/.../mall/pay/pay.page.yaml`：三互斥分支——零金额（`pay`）/ 余额可用（`payByBalance` 确认弹窗）/ 微信（`prepay`+QR+`PayService__queryPayment` 轮询）。无组合分支。
- owner doc `docs/design/order-and-cart.md`（多支付通道）+ `docs/design/wallet-and-assets.md`（余额支付扣款接线）：均描述单通道全额支付。
- `PaymentCallbackImpl.onRefundSuccess`：异步退款通知**不**驱动退款副作用（仅对账/记 WARN），退款资金动作在同步 refund 路径（见上）。

**核心缺口：** (1) 无组合支付编排；(2) CREATED 组合待支付订单取消/超时无余额回冲；(3) 已支付组合订单进入现有退款路径会丢失钱包部分并对通道超额退款（资金安全缺陷，必须随组合支付一并交付）。`pay`/`payByBalance`/`prepay`/新 `payWithCombo` 仅守 `status==101`，组合待支付窗口（101 且 `walletPayAmount>0`）缺重入守卫。

## Goals

- 用户可在收银台选择「余额抵扣一部分 + 第三方通道（微信）补差剩余」组合支付，一次性完成订单支付。
- 组合余额部分先原子扣减（乐观锁防双花），剩余金额创建第三方预支付；补差异步通知到达后订单进入已支付（`payChannel`=第三方、`walletPayAmount`=余额抵扣）。
- 组合待支付期间（订单仍 101 且已扣余额）防重入：`pay`/`payByBalance`/`prepay`/`payWithCombo` 拒绝在 `walletPayAmount>0` 时二次进入。
- CREATED 组合订单被取消/超时取消时，已扣余额对称回冲（与还库/还券/还积分/释放满减同链）。
- 已支付组合订单进入任何退款路径（售后 refund/confirmReturnReceived、自提超时退款、团购失败退款、拼团失败退款，全站五处 `payService.refund` 站点）时，按比例拆分：钱包部分回冲钱包、剩余部分退第三方通道——不丢资金、不对通道超额退款。

## Non-Goals

- 支付宝通道真实组合（支付宝 `enabled=false` 示例回退，属 P30 Deferred「真实第三方凭证生产联调」Protected Area；组合编排对 `PayChannel` 抽象透明，支付宝启用后自动适用，不在本计划验证范围）。
- 余额 + 多个第三方通道混合（仅余额 + 单一第三方通道）。
- admin 主动发起（充值/订单）退款同步动作（属 `2026-06-29-0330-1` Deferred 同类 successor）。
- 钱包余额冻结/预占机制（采用扣减+回冲模型）。
- 退款金额比例分摊的财务级精度（采用比例分摊 + 余数归通道，Decision D3；财务级成本中心分摊为 successor）。
- 积分商城「积分+现金」组合兑换订单（`LitemallPointsExchangeOrder`，已由 `2026-06-29-1045-3` 交付，独立实体与本计划订单级组合支付无关）。

## Task Route

- Type: `implementation-only change`
- Owner Docs: `docs/design/order-and-cart.md`（多支付通道章节补组合支付 + 组合退款拆分）, `docs/design/wallet-and-assets.md`（余额支付扣款接线补组合 + 取消/退款回冲）
- Skill Selection Basis: 后端新增 `@BizMutation` + 退款路径 Fix（nop-backend-dev + 规则 #15 强制 nop-testing）、前端收银台 page.yaml（nop-frontend-dev）、IGraphQLEngine 测试（nop-testing）

## Infrastructure And Config Prereqs

- 无新增基础设施。复用既有微信预支付（`payService.createPayment`）、钱包原子操作（`walletBiz.debitBalance`/`creditBalance`）、`markOrderPaidCore` 状态机、既有退款路径。
- 示例模式（`payService.isEnabled()==false`）下组合补差走既有「模拟」回退语义；真实微信扫码联调受 P30 Deferred「真实第三方凭证生产联调」Protected Area 约束，与本计划代码正确性解耦。
- **Protected Area 评估：** 本计划仅复用已授权原语（钱包扣减/回冲、`payService` facade），**不**改 `model/*.orm.xml`、**不**改 `app-mall-wx`、**不**接真实支付凭证——不触发 ask-first。

## Execution Plan

### Phase 1 - 后端组合支付核心 + 重入守卫 + CREATED 回冲

Status: completed
Targets: `app-mall-service/src/main/java/app/mall/service/entity/LitemallOrderBizModel.java`, `app-mall-dao/src/main/java/app/mall/biz/ILitemallOrderBiz.java`, `app-mall-service/src/main/java/app/mall/service/AppMallErrors.java`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Decision | Fix`
- Prereqs: P30（已完成）、P29 钱包（已完成）

- [x] **Skill loading gate:** 扫描 available skills，加载 `nop-backend-dev` 与 `nop-testing`。读完路由表必读文档（CrudBizModel / 错误处理 / 跨实体调用 I*Biz / ask-first anti-patterns）。每写完一个方法 selfcheck。Docs read 项下列出已读路径。
  - Docs read: `AGENTS.md`（Nop 规则全量）、`docs/context/project-context.md`、`docs/context/codebase-map.md`、nop-backend-dev skill（路由表：service-layer / error-handling / safe-api-reference / bizmodel-method-selfcheck）、nop-testing skill（test-examples / testing）、`ILitemallOrderBiz.java`、`LitemallOrderBizModel.java`（pay/payByBalance/prepay/markOrderPaidCore/cancel/cancelExpiredOrders/cancelExpiredPickupOrders）、`LitemallWalletBizModel.java`、`AppMallErrors.java`、`TestPayByBalanceBizModel.java`（测试范式）。
- [x] **Decision D1（扣减时机与放弃回冲）：** 抉择「先原子扣减余额部分 → 创建第三方补差预支付；CREATED 订单取消/超时对称回冲 `walletPayAmount`」。备选：(a) 通知到达才扣减——拒绝（余额双花竞态）；(b) 钱包冻结/预占——拒绝（基线无冻结机制，扩大钱包模型）。残留风险：第三方通知在超时取消之后到达——`confirmPaidByNotify` 对非 101 幂等 no-op（既有），余额已回冲，迟到通知静默丢弃需人工对账（记 Deferred）。
- [x] **Decision D2（已支付组合订单通道字段语义）：** 抉择 `payChannel`=第三方通道（WECHAT）、`walletPayAmount`=余额抵扣。备选：新增 `payChannel=COMBO` + 拆分报表——拒绝（破坏既有单通道口径与 `cancelExpiredPickupOrders` BALANCE 分支语义，扩大字典与报表面）。`confirmPaidByNotify` 既有逻辑设 WECHAT 且不触碰 `walletPayAmount`，天然满足。残留风险：报表「按通道」统计会把组合订单归入 WECHAT——可接受（余额抵扣作补充字段，wallet-and-assets 口径已说明）。
- [x] **Add 新增 `@BizMutation payWithCombo(orderId, useBalanceAmount, confirmCredential)`：** 校验存在/状态(101)/归属 → **重入守卫**（`walletPayAmount!=null && >0` 抛 `ERR_ORDER_COMBO_PENDING`）→ 校验 `useBalanceAmount>0` → `verifyPayCredential`（复用 P30 Decision B）→ 查余额 → `useBalanceAmount > balance` 抛 `ERR_ORDER_BALANCE_INSUFFICIENT`（与 payByBalance 一致，不静默截断）→ `debitAmount = min(useBalanceAmount, actualPrice)` → `walletBiz.debitBalance(debitAmount, PAY, sourceType=pay, orderSn)` → 重载订单 → 设 `walletPayAmount=debitAmount`+持久化 → `remainder=actualPrice−debitAmount`；`remainder==0` 则 `payChannel=BALANCE`+`markOrderPaidCore`（退化为全额余额）；`remainder>0` 则 `payService.createPayment(totalFee=remainder)`+设 `payId`+持久化+返回 `codeUrl`（订单保持 101）。接口声明同步 `ILitemallOrderBiz`。
- [x] **Fix 重入守卫（防双扣）：** `payByBalance` 与 `prepay` 在既有 `status==101` 守卫后补「`walletPayAmount>0` 抛 `ERR_ORDER_COMBO_PENDING`」守卫；`pay`（示例零金额确认）同样补守卫。保证组合待支付窗口不可被任一支付入口二次进入。
- [x] **Fix `cancel()` 与 `cancelExpiredOrders()` 余额回冲：** CAS 守卫成功、加载订单后，若 `walletPayAmount!=null && >0` 则 `walletBiz.creditBalance(userId, walletPayAmount, REFUND, sourceType=order-refund, orderSn, "组合支付取消退还余额")`——**复用既有 `SOURCE_TYPE_ORDER_REFUND`**（与 `cancelExpiredPickupOrders` 钱包回冲 taxonomy 一致，不新增常量）。CAS 守卫保证幂等。普通（无 `walletPayAmount`）订单取消零回归。
- [x] **Add ErrorCode：** `ERR_ORDER_COMBO_PENDING`（组合待支付期间重入拒绝）、`ERR_ORDER_COMBO_AMOUNT_INVALID`（useBalanceAmount 非法/超过实付）；复用既有 `ERR_ORDER_BALANCE_INSUFFICIENT`/`ERR_ORDER_PAY_CREDENTIAL_INVALID`/`ERR_ORDER_NOT_ALLOW_PAY`。入 `app-mall-service/.../AppMallErrors.java`，中文 description。

Exit Criteria:

- [x] `payWithCombo` 落地非空壳：remainder>0 返回 codeUrl 且订单保持 101 且 `walletPayAmount` 落库 / remainder==0 退化为全额余额进 202 / 余额不足拒绝 / 凭证错误拒绝 / 非法金额拒绝 / 非 101 拒绝 / **重入拒绝（`walletPayAmount>0`）** 均有明确分支。
- [x] `payByBalance`/`prepay`/`pay` 重入守卫落地；普通单通道支付零回归。
- [x] `cancel`/`cancelExpiredOrders` 对「带 `walletPayAmount` 的 CREATED 订单」回冲余额，CAS 守卫保证幂等；普通订单取消零回归。
- [x] **API 测试：** `payWithCombo`/`payByBalance`(重入)/`prepay`(重入)/`cancel`(回冲)/`cancelExpiredOrders`(回冲) 通过 `IGraphQLEngine`（`JunitBaseTestCase`）测试，覆盖成功组合 + 退化全额 + 重入拒绝 + 取消/超时回冲 + 各失败模式。
- [x] 跨实体走 `ILitemallWalletBiz`；`@Inject` 非 private；`NopException`+ErrorCode；无 `@Transactional` 叠加；无 `System.currentTimeMillis`/原生 JSON lib。
- [x] owner doc 更新组合支付 + CREATED 回冲 + 重入守卫口径（含 D1/D2 rationale 与残留风险）。
- [x] `docs/logs/2026/06-30.md` 更新。

### Phase 2 - 后端组合感知退款拆分（资金安全）

Status: completed
Targets: `app-mall-service/src/main/java/app/mall/service/entity/LitemallOrderBizModel.java`, `app-mall-service/src/main/java/app/mall/service/entity/LitemallAftersaleBizModel.java`, `app-mall-service/src/main/java/app/mall/service/entity/LitemallGrouponBizModel.java`, `app-mall-service/src/main/java/app/mall/service/entity/LitemallPinTuanActivityBizModel.java`, `app-mall-dao/src/main/java/app/mall/biz/ILitemallOrderBiz.java`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Decision | Fix`
- Prereqs: Phase 1

- [x] **Skill loading gate:** 加载 `nop-backend-dev` 与 `nop-testing`，读完路由表必读文档（同 Phase 1）。每方法 selfcheck。
  - Docs read: nop-backend-dev skill（service-layer / error-handling / safe-api-reference / bizmodel-method-selfcheck）、nop-testing skill、`LitemallAftersaleBizModel.java`（refund/confirmReturnReceived）、`LitemallGrouponBizModel.java`（refundGrouponOrder）、`LitemallPinTuanActivityBizModel.java`（refundMemberOrder）、`TestLitemallAftersaleBizModel.java`（refund-failure 断言口径）。
- [x] **Decision D3（退款金额钱包/通道分摊）：** 抉择「按比例分摊：walletPortion = refundAmount × walletPayAmount / actualPrice；channelPortion = refundAmount − walletPortion；余数（BigDecimal 尾差）归通道」。统一处理整单（refundAmount=actualPrice → walletPortion=walletPayAmount 恰好）与部分（售后单项）退款。备选：(a) 通道优先（先耗尽通道再钱包）——拒绝（组合订单通道收到的就是补差，通道优先会延迟钱包回冲，语义不清）；(b) 钱包优先——拒绝（同前）。残留风险：多次部分退款的累计尾差——余数归通道，钱包累计回冲可能差分（scale 控制，记 owner doc）。
- [x] **Add 组合感知退款拆分 helper（接口契约）：** 在 `ILitemallOrderBiz` 新增 public `refundComboAware(LitemallOrder order, BigDecimal refundAmount, IServiceContext context)` 返回 `boolean`（通道退款成功；app-mall-dao 不可见 PayRefundResponseBean，故用 boolean 保留各调用方域特定错误语义 `ERR_AFTERSALE_REFUND_FAILED`/`ERR_PICKUP_AUTO_CANCEL_REFUND_FAILED`/refundFailures），`@Override` 于 `LitemallOrderBizModel`：`walletPayAmount>0` 且 `actualPrice>0` → 按比例算 walletPortion/channelPortion → `walletBiz.creditBalance(walletPortion, REFUND, order-refund, orderSn)` + `channelPortion>0` 时 `payService.refund(totalFee=actualPrice, refundFee=channelPortion)`；`walletPayAmount==0/null` 或 `actualPrice==0` → `payService.refund(totalFee=actualPrice, refundFee=refundAmount)`（既有行为，`actualPrice>0` 防御守卫避免除零）。这是 Phase 2 五个退款点的共享契约（rule #6）。
- [x] **Fix 售后退款组合感知：** `LitemallAftersaleBizModel.refund()` 与 `confirmReturnReceived()` 将直接 `payService.refund(totalFee=actualPrice, refundFee=entity.getAmount())` 替换为 `orderBiz.refundComboAware(order, entity.getAmount(), context)`（经 `ILitemallOrderBiz` 注入），保留 `ERR_AFTERSALE_REFUND_FAILED` 失败语义；移除 aftersale 不再使用的 `PayService` 注入与 PayRefund*Bean 导入。
- [x] **Fix 自提超时退款组合感知：** `LitemallOrderBizModel.cancelExpiredPickupOrders()` `else` 分支（非 BALANCE 通道）改为 `refundComboAware(order, actualPrice, context)`；`BALANCE` 分支保持既有 `creditBalance`（全额余额订单 `walletPayAmount==actualPrice`，`refundComboAware` 亦可统一，但为最小改动 + 零回归，保留既有 BALANCE 分支并在注释说明等价）。
- [x] **Fix 团购失败退款组合感知：** `LitemallGrouponBizModel.refundGrouponOrder()` 将 `payService.refund(totalFee=actualPrice, refundFee=actualPrice)` 替换为 `orderBiz.refundComboAware(order, actualPrice, context)`（经 `ILitemallOrderBiz` 注入），保留 try-catch 记 refundFailures 批次语义；移除不再使用的 `PayService` 注入与 PayRefund*Bean 导入。
- [x] **Fix 拼团失败退款组合感知：** `LitemallPinTuanActivityBizModel.refundMemberOrder()` 将 `payService.refund(totalFee=actualPrice, refundFee=actualPrice)` 替换为 `orderBiz.refundComboAware(order, actualPrice, context)`（经 `ILitemallOrderBiz` 注入）——结构同团购失败退款，同为整单退款站点；移除不再使用的 `PayService` 注入与 PayRefund*Bean 导入。

Exit Criteria:

- [x] `refundComboAware` helper 落地非空壳：组合订单（`walletPayAmount>0`）整单退款 → 钱包回冲 walletPayAmount + 通道退 (actualPrice−walletPayAmount)；部分退款 → 按比例分摊；普通订单（无 walletPayAmount）→ 既有全额通道退款零回归。
- [x] 售后 refund/confirmReturnReceived、自提超时、团购失败、拼团失败五条退款路径均经 helper，组合订单不丢钱包部分、不对通道超额退款。
- [x] **API 测试：** 各退款路径的组合感知行为通过 `IGraphQLEngine`/`I*Biz` 接口测试（组合订单整单退款拆分 + 部分退款分摊 + 普通订单零回归 + 通道失败时钱包仍回冲）；`refundComboAware` 作为 `ILitemallOrderBiz` 方法经接口测试；既有售后/团购/拼团/自提测试零回归。
- [x] 跨实体走 `I*Biz`；`@Inject` 非 private；`NopException`+ErrorCode；无 anti-pattern。
- [x] owner doc 更新组合退款拆分口径（含 D3 比例分摊 + 余数规则 + 累计尾差残留风险）。
- [x] `docs/logs/2026/06-30.md` 更新。

### Phase 3 - 前端收银台组合支付 UI

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/mall/pay/pay.page.yaml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 1（Phase 2 退款无前端面，可并行）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完路由表必读文档（XView 三层模型 / AMIS page.yaml / bounded-merge / 业务动作按钮 / `@mutation` 调用约定）。文件完成后 selfcheck。
  - Docs read: nop-frontend-dev skill（view-and-page-customization / delta-customization / page-dsl-pattern-catalog）、既有 `pay.page.yaml`（零金额/余额全额/微信全额三分支 + channelsLoad/prepay/payStatus 模式）、`docs/design/order-and-cart.md` 前台支付消费者章节。
- [x] **Add 收银台组合支付分支：** 在 `pay.page.yaml` 既有余额通道与微信通道之间新增组合分支——`visibleOn` 余额通道可用且 `actualPrice>0`：展示可用余额 + 「使用余额抵扣」输入（默认 min(余额, actualPrice)，可调小，不得超过余额/实付）+ 实时算「剩余需支付 = actualPrice − 抵扣」；按钮调 `@mutation:LitemallOrder__payWithCombo`（orderId/useBalanceAmount/confirmCredential，确认弹窗复用 P30 登录密码确认样式）；`remainder>0` 展示微信扫码（复用既有 `prepay` QR + `PayService__queryPayment` 轮询，针对组合补差），`remainder==0` 直接跳订单详情。保留既有零金额/余额全额/微信全额分支不破坏。

Exit Criteria:

- [x] 组合分支运行时可达且真实调用 `payWithCombo`；抵扣金额与剩余金额展示正确；退化全额（抵扣=实付）直接完成。
- [x] 既有零金额/余额全额/微信全额分支零回归（visibleOn 互斥不串扰）。
- [x] page.yaml 符合 AMIS 约定，无 `_gen` 手编；编译通过。

### Phase 4 - 验证、owner doc 同步与回归

Status: completed
Targets: `docs/design/order-and-cart.md`, `docs/design/wallet-and-assets.md`, `docs/logs/2026/06-30.md`, `docs/testing/known-good-baselines.md`
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1, Phase 2, Phase 3

- [x] **Skill loading gate:** 加载 `nop-testing`，读完路由表必读文档（`JunitAutoTestCase` / `IGraphQLEngine` / request.json5 / RECORDING→CHECKING）。
  - Docs read: nop-testing skill（test-examples / testing / write-tests）、`TestPayByBalanceBizModel.java`（JunitBaseTestCase + IGraphQLEngine + 手写订单/wallet 种子范式）、`TestLitemallAftersaleBizModel.java`（refund-failure 断言口径 + WxPayServiceImpl.setForceRefundFailure）。
- [x] **Proof：** `./mvnw test -pl app-mall-service` 全绿（**536 测试全绿**：新增 `TestComboPaymentBizModel` 10 + `TestComboRefundSplitBizModel` 5；既有支付/售后/团购/拼团/自提测试零回归）；`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` BUILD SUCCESS；`./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS。`known-good-baselines.md` 追加本 successor 行。
- [x] **Proof：** owner doc 两篇更新（组合支付流程 / CREATED 回冲 / 重入守卫 / 组合退款拆分 / D1-D3 rationale 与残留风险），log 条目含验证命令与结果。

Exit Criteria:

- [x] 全量 service 测试 + web 编译 + uber-jar install 全绿，数字记录于 log 与 baseline。
- [x] owner doc 与 log 与实现一致。

## Plan Audit

- Status: passed（Round 3 + Round 4 连续两轮 clean = 共识，guide rule #12）
- Auditor / Agent: 独立 subagent 链——Round 1 `ses_0f6e2d3ffemdXJS9IwBClutr`（MAJOR：已支付组合订单退款丢钱包部分 + 无重入守卫 + 缺 roadmap 链接）→ 修订（扩 Phase 2 退款拆分 + 重入守卫）；Round 2 `ses_0f6ebb6e2dffe6M9QC75TMJZe0P`（MAJOR：Phase 2 退款站点遗漏第 5 处 `LitemallPinTuanActivityBizModel.refundMemberOrder`）→ 修订（补第 5 站点）；Round 3 `ses_0eba5ec5effeiTFLFBjx4WSqoe`（CLEAN，首 clean）；Round 4 `ses_0eba2adf6ffeWTL716M0EHTzKL`（CLEAN，次 clean = 共识）
- Evidence: Round 4 实读 live repo 复核——baseline 全部行号精确命中；`grep payService.refund` 全站恰 5 处（aftersale.refund `:164` / aftersale.confirmReturnReceived `:315` / cancelExpiredPickupOrders `:2491` / refundGrouponOrder `:259` / refundMemberOrder `:368`）全部纳入 Phase 2 `refundComboAware` 拆分；重入守卫 + CREATED 回冲 + D3 比例分摊(actualPrice>0 守卫) + `refundComboAware` 接口契约(rule #6) 均落地；各 phase Required Skill 齐备（P1/P2 nop-backend-dev+nop-testing / P3 nop-frontend-dev / P4 nop-testing）；D1-D3 rationale+alternatives+residual 齐全；无 `model/*.orm.xml` 改动、无真实支付凭证。无 blocker/major。

## Closure Gates

- [x] in-scope behavior is complete（组合支付成功/退化/重入拒绝/CREATED 回冲/组合退款拆分/各失败模式）
- [x] relevant docs are aligned（order-and-cart.md / wallet-and-assets.md）
- [x] verification has run（`mvn test -pl app-mall-service` 536 全绿 / uber-jar install / web compile）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（payWithCombo + refundComboAware 经 I*Biz/IGraphQLEngine 测试 + cancel/cancelExpiredOrders/各退款路径扩展）；`@BizAction` 无新增
- [x] no in-scope item downgraded to deferred/follow-up（组合退款拆分为资金安全 in-scope，不得降级）
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（Phase 1/2 nop-backend-dev+nop-testing / Phase 3 nop-frontend-dev / Phase 4 nop-testing）
- [x] skill loading verification: 各 phase 已扫描+加载匹配 skill+读完路由表必读文档（路径列入 skill loading gate）+ 每方法/文件 selfcheck
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation（独立 closure auditor subagent，与实现 agent 不同 session）
- [x] closure evidence exists in files（见下方 `Closure` 段；live-repo 证据：`payWithCombo`/`rejectComboReentry`/`creditBackComboBalance`/`refundComboAware` 落地 + 5 退款站点 + 2 测试类 + owner doc/log/baseline 同步）

## Deferred But Adjudicated

### 第三方通知在超时取消之后到达的竞态（notify-after-cancel）

- Classification: `watch-only residual`
- Why Not Blocking Closure: `confirmPaidByNotify` 对非 101 订单既有幂等 no-op；组合下余额已由取消链回冲，迟到通知静默丢弃。此为「notify-after-cancel」既有边缘类别，人工对账兜底，与 P30 既有模型一致。
- Successor Required: `no`（触发条件：高频迟到通知且运营要求自动化对账时，评估通知时序守卫模型）

### 支付宝通道真实组合联调

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 组合编排对 `PayChannel` 抽象透明；支付宝 `enabled=false` 示例回退，真实联调受 P30 Deferred「真实第三方凭证生产联调」Protected Area 约束。
- Successor Required: `yes`（触发条件：获取真实支付宝商户凭证并启用通道后，组合补差经支付宝端到端联调）

### 退款分摊财务级精度

- Classification: `optimization candidate`
- Why Not Blocking Closure: Decision D3 比例分摊 + 余数归通道满足基线资金安全（整单退恰好、部分退误差为 scale 尾差）。
- Successor Required: `yes`（触发条件：财务要求成本中心级精确分摊或多通道组合时，评估独立分摊引擎）

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 4 个 Phase 全部 `completed`、Closure Gates 全部 `[x]`、Plan Status `completed`，文本一致。独立 closure auditor 复核 live repo 证据全部命中（非空壳、非占位），实现、测试、owner doc、log、baseline、roadmap 均落地，可闭合。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure auditor subagent（与实现 agent 不同 session；本任务由 MISSION_DRIVER 闭合审计指令驱动，非实现 agent 自审）
- Evidence:
  - **Phase 1（payWithCombo + 重入守卫 + CREATED 回冲）**：`LitemallOrderBizModel.java:938` `@BizMutation payWithCombo` 非空壳——校验存在/状态(101)/归属 + `rejectComboReentry`（:908，`walletPayAmount>0` 抛 `ERR_ORDER_COMBO_PENDING`）+ `verifyPayCredential` + 余额校验 + `debitBalance(PAY, sourceType=pay, orderSn)` + 写 `walletPayAmount` + remainder==0 退化 `payChannel=BALANCE`+`markOrderPaidCore` / remainder>0 `createPayment(totalFee=remainder)`+设 payId 订单保持 101。重入守卫 4 处调用全部落地：`pay:767`/`payByBalance:802`/`prepay:859`/`payWithCombo:954`。CREATED 回冲 2 处：`cancel:725`/`cancelExpiredOrders:2507` 调 `creditBackComboBalance`（:923，复用 `SOURCE_TYPE_ORDER_REFUND`，CAS 幂等，普通订单 no-op）。`AppMallErrors.java:71` 新增 `ERR_ORDER_COMBO_PENDING`/`ERR_ORDER_COMBO_AMOUNT_INVALID`，中文 description。
  - **Phase 2（refundComboAware 5 站点）**：`LitemallOrderBizModel.java:2891` `refundComboAware` 非空壳——D3 比例分摊 `walletPortion=refundAmount×walletPayAmount/actualPrice` scale=2 HALF_UP 余数归通道 + `creditBalance(REFUND, order-refund)` + `channelPortion>0` 时 `payService.refund(totalFee=actualPrice, refundFee=channelPortion)`；`actualPrice>0` 防御守卫；非组合订单零回归既有全额通道退款。5 站点全部经 helper 路由：`LitemallAftersaleBizModel.refund():154`、`confirmReturnReceived():301`、`cancelExpiredPickupOrders():2649`（非 BALANCE 分支，BALANCE 分支保留既有 creditBalance）、`LitemallGrouponBizModel.refundGrouponOrder():251`、`LitemallPinTuanActivityBizModel.refundMemberOrder():360`。`payService.refund` 直接调用仅余 helper 内部两处（:2920/:2953），aftersale/groupon/pintuan 已无残留。`ILitemallOrderBiz.java:340` 接口声明。
  - **Phase 3（前端）**：`pay.page.yaml:157-246` 组合分支运行时可达，`@mutation:LitemallOrder__payWithCombo`（:215）真实调用，抵扣输入 + 密码确认弹窗 + remainder>0 微信补差 QR / remainder==0 成功跳转 feedback；既有零金额/余额全额/微信全额分支零回归。
  - **Phase 4（验证）**：`TestComboPaymentBizModel.java`（10 例，IGraphQLEngine 经 `LitemallOrder__payWithCombo` mutation）+ `TestComboRefundSplitBizModel.java`（5 例，`I*LitemallOrderBiz` 接口测试）存在；`docs/logs/2026/06-30.md`、`docs/testing/known-good-baselines.md`（536 测试全绿 + uber-jar install + web compile BUILD SUCCESS）、owner doc `order-and-cart.md`/`wallet-and-assets.md`（组合支付流程 + CREATED 回冲 + 重入守卫 + 退款拆分 + D1-D3 rationale）、`enhanced-features-roadmap.md`（P30 Deferred 闭环标记）全部同步。
  - **Anti-hollow / 资金安全**：无空函数体、无 `return null` 占位、无吞异常；组合订单退款不丢钱包部分、不对通道超额退款（D3 比例分摊）；重入守卫防双扣钱包/二次预支付；CREATED 取消对称回冲防丢余额。`confirmPaidByNotify` 既有逻辑对迟到通知幂等 no-op（Deferred 已记录）。
  - **无模型改动**：复用既有 `walletPayAmount`/`payChannel` 字段 + `PayService`/`walletBiz`/`markOrderPaidCore`，无 `model/*.orm.xml` 改动、无真实支付凭证、无 Protected Area 越界。
  - **Deferred honesty**：3 项 Deferred（notify-after-cancel watch-only / 支付宝真实联调 out-of-scope / 退款分摊财务级精度 optimization）均非 in-scope live defect 降级，触发条件明确。

Follow-up:

- 见上方 `Deferred But Adjudicated` 三项（非阻断；confirmed defect 不得出现在此，已确认无降级）。
