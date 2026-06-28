# walletRechargeRefund 钱包充值退款异步对账（P29 deferred successor）

> Plan Status: completed
> Last Reviewed: 2026-06-29
> Mission: mall
> Work Item: P29 deferred successor — 充值退款流程（recharge-channel refund async reconciliation）
> Source: `docs/backlog/enhanced-features-roadmap.md` §29 Deferred；`docs/plans/2026-06-28-1400-1-phase29-wallet-recharge-plan.md` → `Deferred But Adjudicated → 充值退款流程`
> Related: `docs/plans/2026-06-28-1822-1-phase30-multi-payment-channels-plan.md`（P30 交付 `onRefundSuccess` 幂等对账，触发本 successor 的前置条件）、`docs/plans/2026-06-28-1400-1-phase29-wallet-recharge-plan.md`（P29 充值入账正向流程 origin）
> Audit: required

## Current Baseline

**正向充值流程（已交付，P29 done）：**
- `LitemallRechargeBizModel.confirmRechargeByNotify`（`LitemallRechargeBizModel.java:182`，`@BizAction`）：微信支付异步通知签名校验通过后，将 UNPAID→PAID，调 `walletBiz.creditBalance(amount+giftAmount, changeType=RECHARGE, sourceType=recharge, sourceId=rechargeId)` 一次性入账，幂等（UNPAID→PAID 状态守卫 + sourceId=rechargeId 源级去重）。
- outTradeNo 派生（非存储）：`"RC" + String.format("%08d", rechargeId)`（如 `RC00000001`）。
- `PaymentCallbackImpl.onPaymentSuccess`（`PaymentCallbackImpl.java:59`）：按 outTradeNo 前缀分流——`RC` 开头 → `rechargeBiz.confirmRechargeByNotify`；否则 → `orderBiz.confirmPaidByNotify`。

**退款异步通知对账（P30 已交付订单侧，recharge 侧为缺口）：**
- `PaymentCallbackImpl.onRefundSuccess`（`PaymentCallbackImpl.java:91`）：订单侧已实现幂等对账（订单已在 REFUND/REFUND_CONFIRM 终态 → no-op；非终态 → 记 WARN 人工对账）。**`RC` 前缀分支（`:97-101`）当前显式忽略**：`// Recharge refunds are not tracked here (recharge has no refund flow in baseline).` 仅记 INFO 并 return —— 即充值支付的退款异步通知到达时，**钱包余额（amount+giftAmount）不被回冲，存在资金一致性缺口**。
- 该分支的注释与 P29/P30 计划的 Deferred 一致：P29 Deferred「充值退款流程」触发条件 = 「P30 多支付通道引入退款异步通知时一并补齐」；P30 已交付 `onRefundSuccess` 幂等钩子（订单侧），recharge 侧 reversal 为待补的 successor。

**已就绪的字典/状态语义（无需 ORM 改动）：**
- `WALLET_CHANGE_TYPE_REFUND=20`（`_AppMallDaoConstants.java:369`，`mall/wallet-change-type` 字典 REFUND/退款）已存在。
- `PAY_STATUS_REFUNDED=30`（`_AppMallDaoConstants.java:434`）已存在，recharge 当前未使用。
- `LitemallWalletBizModel.debitBalance`（`LitemallWalletBizModel.java:92`，`@BizAction` 内部 API）：乐观锁原子扣减，余额不足抛 `ERR_WALLET_INSUFFICIENT`，自动写 `LitemallWalletFlow`（含 balanceAfter 快照、sourceId 源级去重）。
- owner doc `docs/design/wallet-and-assets.md:52`：`退款返还 = REFUND + sourceType=refund（退款流程未实现，预留语义）`——语义预留，本 successor 落地 recharge-refund 场景。

**手动兜底（已有）：** `LitemallWalletBizModel.adminAdjust`（`@BizMutation @Auth(admin)`）可由运营人工调账回冲。

**缺口：** 无自动化的充值退款对账路径。当微信侧对一笔充值支付发起退款（如重复扣款客诉、运营在微信商户后台手工退款），异步退款通知到达 `onRefundSuccess` 的 `RC` 分支被忽略，钱包余额不回冲 → 资金一致性缺口（用户已花的赠送/充值余额无法自动回收）。

## Goals

- 当微信退款异步通知到达 `RC` 前缀 outTradeNo 时，**幂等回冲**钱包余额（amount+giftAmount）并记 `WalletFlow(REFUND, sourceType=recharge-refund, sourceId=rechargeId)`，将 `LitemallRecharge.payStatus` 推进至 `REFUNDED`。
- 与既有正向入账（`creditRecharge` RECHARGE）和 P30 订单侧对账（`onRefundSuccess` 订单分支）对称、互不干扰。
- 余额不足以全额回冲时安全降级（记 WARN 人工对账，不静默、不卡死重试）。

## Non-Goals

- **不**新增 admin「发起充值退款」同步动作（本 successor 仅处理**渠道侧主动退款**的异步对账；admin 主动退款充值场景为 watch-only，触发条件「运营要求主动退充值」时再开）。
- **不**改 ORM 模型（复用既有 `WALLET_CHANGE_TYPE_REFUND` / `PAY_STATUS_REFUNDED` / 派生 outTradeNo；ORM 改动为 Protected Area ask-first，本 successor 不触及）。
- **不**处理订单退款（订单侧由 P30 `onRefundSuccess` 订单分支负责，行为不变）。
- **不**实现 wallet `WITHDRAW` 提现（P29 Deferred `no successor`）。
- **不**改前端视图（充值退款无用户可见入口，仅异步对账；充值记录页 `payStatus` 渲染 REFUNDED 由既有字典 mapping 自动覆盖，无需改 view）。
- **不**更新 roadmap 阶段状态：本计划为 deferred successor（非 roadmap 阶段），`enhanced-features-roadmap.md` / `implementation-roadmap.md` 无对应阶段需翻 `done`/`planned`；P29 Phase Status 保持 `done` 不变。

## Task Route

- Type: `implementation-only change`（闭合已 deferred 的 successor，业务语义与触发条件已在 P29/P30 owner doc 与 Deferred 中界定）
- Owner Docs: `docs/design/wallet-and-assets.md`（充值章节 + changeType×sourceType 语义表）
- Skill Selection Basis: 新增 `@BizAction` 方法 + 改 `PaymentCallbackImpl` 跨实体桥接 + `IGraphQLEngine`/`I*Biz` 测试 → `nop-backend-dev`（BizModel/BizAction/跨实体/错误处理）+ `nop-testing`（@BizAction 经 I*Biz 接口测试、JunitAutoTestCase）匹配。

## Infrastructure And Config Prereqs

- 无新增基建。复用既有微信支付退款异步通知端点（`app-mall-wx` `WxPayNotifyResource` → `IPaymentCallback.onRefundSuccess`），该链路 P30 已就绪。
- 真实退款异步通知需微信商户凭证（P30 Deferred「真实第三方凭证生产联调」，Protected Area）；本 successor 的代码路径与单元/集成测试在模拟支付/模拟通知下即可验证（复用 `TestAlipayChannelAndRefundNotify` 的 `paymentCallback.onRefundSuccess(...)` 直接调用模式）。

## Execution Plan

### Phase 1 — 充值退款异步对账（后端 + 测试 + owner doc）

Status: completed
Targets: `app-mall-service/.../entity/LitemallRechargeBizModel.java`（新增 `refundRechargeByNotify` @BizAction）、`app-mall-dao/.../biz/ILitemallRechargeBiz.java`（接口声明）、`app-mall-service/.../pay/PaymentCallbackImpl.java`（`:97-101` RC 分支接线）、`app-mall-service/.../entity/LitemallWalletBizModel.java`（新增 `SOURCE_TYPE_RECHARGE_REFUND` 常量）、`app-mall-service/src/test/...`（测试）、`docs/design/wallet-and-assets.md`（语义落地注记）
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Fix | Decision | Proof`
- Prereqs: P29（done）、P30（done，交付 `onRefundSuccess` 钩子）

- [x] **Skill loading gate:** 扫描 available skills，加载 `nop-backend-dev` 与 `nop-testing`。读完 `nop-backend-dev` routing table 标为必读的文档（含 `docs-for-ai/02-core-guides/service-layer.md`、`error-handling.md`、`docs-for-ai/00-start-here/ai-defaults.md` 反模式表）与 `nop-testing` routing table 必读文档（含 `testing.md` / `JunitAutoTestCase` / `IGraphQLEngine` 录制回放）。每写完一个方法用 skill selfcheck 校验反模式（跨实体经 `I*Biz`、异常 `NopException`+ErrorCode、`@BizAction` 不暴露 GraphQL、无 `System.currentTimeMillis`/第三方 JSON）。
  - Docs read: `nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`（19 项逐方法校验）、`nop-entropy/docs-for-ai/02-core-guides/service-layer.md`（ICrudBiz.get 签名 `ignoreUnknown=true` 返 null）、`nop-entropy/docs-for-ai/02-core-guides/error-handling.md`、`nop-entropy/docs-for-ai/00-start-here/ai-defaults.md` 反模式表、`nop-entropy/docs-for-ai/02-core-guides/testing.md`
- [x] **Add:** `LitemallWalletBizModel` 新增 `public static final String SOURCE_TYPE_RECHARGE_REFUND = "recharge-refund";`（与既有 `SOURCE_TYPE_RECHARGE/PAY/ADMIN_ADJUST` 同位），用于 WalletFlow 细分来源。**命名偏离 owner doc 预留的 `sourceType=refund` 的理由：** `refund` 过于宽泛（未来可能出现「订单退款返还到钱包」场景），`recharge-refund` 明确归属充值退款对账，与正向 `SOURCE_TYPE_RECHARGE=recharge` 对称可追溯；owner doc 更新时同步标注此命名抉择。
- [x] **Add:** `ILitemallRechargeBiz` 新增 `@BizAction void refundRechargeByNotify(@Name("outTradeNo") String outTradeNo, @Optional @Name("outRefundNo") String outRefundNo, IServiceContext context);`——受信内部入口（与 `confirmRechargeByNotify` 同安全模型：`@BizAction` 不暴露 GraphQL，仅由 `PaymentCallbackImpl` 经注入调用；理由同 owner doc `wallet-and-assets.md:78` 资金安全论证——公开 `@BizMutation(rechargeId)` 会允许攻击者对任意充值触发钱包回冲）。
- [x] **Add:** `LitemallRechargeBizModel.refundRechargeByNotify` 实现：
  - 解析 outTradeNo（`RC` 前缀 + parseInt 还原 rechargeId），非法/无前缀 → 静默 return（与 `confirmRechargeByNotify:186` 一致）；recharge 不存在 → return。
  - **幂等守卫 1：** `payStatus == UNPAID` → 从未入账，无需回冲，return（防: 未支付却被退款通知的异常场景）。
  - **幂等守卫 2：** `payStatus == REFUNDED` → 已回冲，return（渠道重放安全）。
  - **回冲：** `payStatus == PAID` → 调 `walletBiz.debitBalance(userId, amount+giftAmount, changeType=WALLET_CHANGE_TYPE_REFUND, sourceType=SOURCE_TYPE_RECHARGE_REFUND, sourceId=rechargeId, remark="充值退款回冲 "+rechargeId)`；成功后将 fresh recharge `payStatus=REFUNDED` 并 `updateEntity`。
- [x] **Decision D1：`debitBalance` 两类失败模式的处理。** `debitBalance`（`LitemallWalletBizModel.mutateBalance`）有两种失败抛 `NopException`，本方法须分别裁定：
  - **D1a 余额不足（`ERR_WALLET_INSUFFICIENT`，`mutateBalance:137`）：** 抉择 **catch + 记 WARN 人工对账，不推进 REFUNDED**。备选 B（允许钱包负余额）被否——破坏 `debitBalance` 资金安全不变量；备选 C（卡死让微信无限重试）被否——重试无效且无人工出口。**残留风险：** 用户在退款通知到达前已花完充值余额时，需运营人工介入（`adminAdjust` 负向调账 + 标记），记入 Deferred。
  - **D1b 乐观锁冲突（`ERR_WALLET_VERSION_CONFLICT`，`mutateBalance:162`）：** 同一 `rechargeId` 的并发退款通知（渠道重放/并发）可能同时通过 `payStatus==PAID` 读守卫后竞争同一 Wallet 行的版本号。抉择 **catch + 记 WARN + 视为幂等 return（不推进、不重抛）**——胜者已完成回冲并将 `payStatus` 推至 REFUNDED，败者 catch 后记 WARN 日志（含 userId/rechargeId/outRefundNo，与 D1a 及订单侧 `onRefundSuccess:118` WARN 对称，留观测痕迹）后直接返回；下一次渠道重试到达时 `payStatus==REFUNDED` 命中幂等守卫 2 no-op。备选（重抛让 `WxPayNotifyResource` 返 5xx 触发微信有限重试）亦可达到最终一致，但选择 catch幂等 return 避免一次瞬态 5xx 且语义更清晰。**不变量依据：** 乐观锁保证不会双扣（只有一个 `updateBalanceIfVersion` 命中 affected=1），故此路径无资金正确性风险，仅为 failure-mode 显式化。与 P30 订单侧 `onRefundSuccess` 的「终态幂等 no-op」哲学一致。
- [x] **Fix:** `PaymentCallbackImpl.onRefundSuccess` `:97-101` 的 RC 分支：由「记 INFO 并 return」改为调用 `rechargeBiz.refundRechargeByNotify(outTradeNo, outRefundNo, systemContext)`（`PaymentCallbackImpl` 已 `@Inject ILitemallRechargeBiz rechargeBiz` 于 `:47-48`，无需新增注入字段），并更新方法 javadoc（`充值退款异步对账`）与 RC 分支注释（移除 "recharge has no refund flow in baseline"）。订单分支（`:102` 起）行为不变。
- [x] **Proof（测试，经 `I*LitemallRechargeBiz` 接口 + 直接调用，@BizAction 不经 GraphQL）：** 在 `TestLitemallRechargeBizModel`（或复用 `TestAlipayChannelAndRefundNotify` 模式）新增。**注意：** `refundRechargeByNotify` 为 `@BizAction`（不自动开 session），测试须按 P29 `confirmRechargeByNotify` 既有先例包裹 `ormTemplate.runInSession(...)`（见 `TestLitemallRechargeBizModel` 既有用法）。
  1. PAID 充值 → `refundRechargeByNotify` → 钱包余额回冲 amount+giftAmount、`WalletFlow(changeType=REFUND, sourceType=recharge-refund, sourceId=rechargeId)` 落地、`payStatus=REFUNDED`、`balanceAfter` 快照正确。
  2. **幂等（重放）：** REFUNDED 充值再次 `refundRechargeByNotify` → no-op（余额/流水/payStatus 不变）。
  3. **未入账：** UNPAID 充值 `refundRechargeByNotify` → no-op（不回冲、不抛）。
  4. **非法 outTradeNo / 不存在 recharge：** no-op（不抛）。
  5. **余额不足（D1）：** 充值入账后经 `payByBalance`/`adminAdjust` 消费至余额 < amount+giftAmount → `refundRechargeByNotify` → 不抛、不推进 REFUNDED、记 WARN（断言 `payStatus` 仍 PAID、余额不变、`LitemallLog` 或日志可观测）。
  6. **PaymentCallbackImpl 集成：** `onRefundSuccess("RC"+rechargeId, refundNo)` → 等价于直接调 `refundRechargeByNotify`（RC 路由生效，订单分支不被误触）；同时保留既有订单侧 `onRefundSuccess` 用例不回归。
  - 验证命令：`./mvnw test -pl app-mall-service -am`（全绿）+ `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C`（BUILD SUCCESS）。

Exit Criteria:

- [x] `RC` 前缀退款异步通知正确路由到 `refundRechargeByNotify`，PAID 充值的 amount+giftAmount 被幂等回冲，`payStatus` 推进 REFUNDED，`WalletFlow` 落地 REFUND/recharge-refund 流水。
- [x] 幂等（REFUNDED 重放 / UNPAID 未入账 / 非法 outTradeNo / 不存在 recharge）均为 no-op，渠道重放安全。
- [x] 余额不足时安全降级（不抛、不卡死、记 WARN 人工对账），`debitBalance` 资金安全不变量不被破坏。
- [x] **API 测试：** 新增 `@BizAction refundRechargeByNotify` 经 `ILitemallRechargeBiz` 接口注入测试（@BizAction 不暴露 GraphQL，按 plan guide 规则 #15 经 `I*XxxBiz` 接口测试）；`PaymentCallbackImpl.onRefundSuccess` RC 路由经直接调用集成测试。
- [x] owner doc `docs/design/wallet-and-assets.md:52` 由「退款流程未实现，预留语义」更新为「充值退款异步对账已落地（REFUND + sourceType=recharge-refund）；余额不足降级与人工对账语义」；充值章节补「充值退款对账」小节（触发场景/幂等/降级/与正向入账对称）。
- [x] `docs/logs/2026/{06-29}.md` 追加本 successor 交付条目。

## Plan Audit

- Status: passed
- Auditor / Agent: independent subagent, two rounds
  - Round 1 (`ses_0f093fbb0ffeX6cv1aECJX47EE`): `REVISE` — 0 blockers, 1 MAJOR (M1: D1 漏 `ERR_WALLET_VERSION_CONFLICT` 失败模式) + 4 minors (m1 sourceType 命名理由 / m2 @BizAction 测试 runInSession 包裹 / m3 PaymentCallbackImpl 已注入无需新 @Inject / m4 无 roadmap 阶段更新说明)。关键裁定：trigger 合法（P29 Deferred「充值退款流程」触发条件「P30 引入退款异步通知」客观成立，P30 已交付 `onRefundSuccess` 且 RC 分支显式留作 no-op 等待本 successor）；draft-vs-nothing 判定正确（全 roadmap done/out-of-scope，本项为唯一 trigger-met deferred successor，非 anti-slacking）；baseline 零漂移；Protected Area（ORM）规避正确；Required Skill + @BizAction 经 I*Biz 测试合规。
  - Round 2 (`ses_0f08ea946ffe2KeNggP1U6Xok0`，修订后): `PASS` — 0 blockers, 0 major。M1 + m1–m4 全部确认解决（含逐项 plan 行号佐证）；trigger/draft-vs-nothing/baseline 独立复核通过；D1b catch 经复核确认为资金安全（乐观锁保证不双扣）；1 非阻塞 nit（D1b catch 加 WARN 与 D1a 对称）已采纳并入 D1b 文本。依 repo 先例（P30 一修订+一 clean = consensus）达成共识，实施可开始。
- Evidence: 见上两轮 task id；无独立 audit 文件（普通 plan-audit 证据默认入 plan）。

## Closure Gates

- [x] in-scope behavior is complete（RC 退款异步对账回冲 + 幂等 + 降级）
- [x] relevant docs are aligned（`wallet-and-assets.md` 语义落地注记）
- [x] verification has run：`./mvnw test -pl app-mall-service -am` 全绿（422 tests）+ `uber-jar` BUILD SUCCESS
- [x] 新增 `@BizAction refundRechargeByNotify` 经 `ILitemallRechargeBiz` 接口测试（非实体级纯逻辑测试替代）；`PaymentCallbackImpl.onRefundSuccess` RC 路由集成测试
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（`nop-backend-dev` + `nop-testing`，非 `none`）
- [x] skill loading verification：本 phase 已扫描并加载匹配 skill，读完 routing table 必读文档（路径记入 skill loading gate），每方法 selfcheck 无反模式
- [x] text consistency verified：Plan Status / Phase Status / Exit Criteria / Closure Gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### admin 主动发起充值退款（同步动作）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本 successor 仅处理**渠道侧主动退款**的异步对账（微信商户后台退款 / 重复扣款客诉）。admin 在系统内主动发起「退充值」（调微信退款 API + 同步推进 + 异步对账）为独立同步动作场景，需新增 admin mutation + WeChat refund API 调用编排，属独立结果面。
- Successor Required: `yes`（触发条件：运营要求在系统内主动发起充值退款时，新增 `@BizMutation @Auth(admin) refundRecharge` 同步动作 + 复用本 successor 的 `refundRechargeByNotify` 做异步对账幂等兜底）

### 余额不足时的自动负余额/部分回冲

- Classification: `watch-only residual`
- Why Not Blocking Closure: D1 抉择 catch+WARN 人工对账，保留 `debitBalance` 不允许负余额的资金安全不变量。用户在退款通知到达前已花完充值余额的场景为低频边缘（需充值→快速消费→渠道侧退款三连），由 `adminAdjust` 人工对账兜底。
- Successor Required: `no`（触发条件：高频出现余额不足回冲且运营要求自动化时，评估部分回冲 + 挂账模型）

### 真实微信沙箱/生产凭证联调

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本 successor 代码路径与测试在模拟支付/直接调用 `onRefundSuccess` 下即可验证；真实退款异步通知的端到端联调受 P30 Deferred「真实第三方凭证生产联调」（Protected Area ask-first）约束，与本 successor 代码正确性解耦。
- Successor Required: `yes`（触发条件：获取真实商户凭证后进行沙箱/生产联调，继承 P30 Deferred）

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 单 Phase 计划，所有执行项、Exit Criteria、Closure Gates 均已 `[x]`，repo 中可逐项验证落地（见下）。本次 closure audit 由独立 session 执行，非实施 agent 自证。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure-audit subagent（独立 session，非实施 agent）
- Evidence（逐项对照 LIVE repo 核验，2026-06-29）：
  - `LitemallWalletBizModel.java:43` 落地 `SOURCE_TYPE_RECHARGE_REFUND = "recharge-refund"`（与既有 `SOURCE_TYPE_RECHARGE/PAY/ADMIN_ADJUST` 同位）。
  - `ILitemallRechargeBiz.java:55` 声明 `refundRechargeByNotify(outTradeNo, outRefundNo, context)`，`LitemallRechargeBizModel.java:210-283` 实现：outTradeNo 解析（RC 前缀 + parseInt，非法/不存在 no-op）、幂等守卫 1（UNPAID no-op）、幂等守卫 2（REFUNDED no-op）、PAID→`walletBiz.debitBalance(amount+giftAmount, REFUND, recharge-refund, rechargeId)` 回冲 + 推进 REFUNDED。`@BizAction` 不暴露 GraphQL，与 `confirmRechargeByNotify` 同安全模型。
  - Decision D1 两类失败模式落地：D1a `ERR_WALLET_INSUFFICIENT` 与 D1b `ERR_WALLET_VERSION_CONFLICT` 经 `catch (NopException)` 统一处理，`LitemallRechargeBizModel.java:259-274` 记 WARN（含 rechargeId/userId/outRefundNo/errorCode，与 D1a/订单侧 onRefundSuccess WARN 对称）后 return，不推进 REFUNDED、不重抛，资金安全不变量不被破坏。
  - `PaymentCallbackImpl.java:97-110` `onRefundSuccess` RC 分支由「记 INFO 并 return」改为调用 `rechargeBiz.refundRechargeByNotify(...)`，复用既有 `@Inject ILitemallRechargeBiz`（无新增注入）；javadoc（`:90-96`）与 RC 分支注释（`:104`）已更新，移除 "recharge has no refund flow in baseline"；订单分支（`:111` 起）行为不变。
  - 测试：`TestLitemallRechargeBizModel.java` 新增 6 测试（PAID 回冲 / REFUNDED 重放幂等 / UNPAID no-op / 非法+不存在 outTradeNo no-op / D1a 余额不足降级 / PaymentCallbackImpl RC 路由）。`@BizAction` 经注入 `ILitemallRechargeBiz` + `ormTemplate.runInSession` 包裹（与 `confirmRechargeByNotify` 既有先例一致），符合 plan guide 规则 #15（@BizAction 经 I*Biz 接口测试）。文件共 15 `@Test`。
  - 验证命令（log 记录全绿）：`mvn test -pl app-mall-service -am` 422 全绿（含 `TestAlipayChannelAndRefundNotify` 订单侧无回归）；`mvn clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` 全模块 BUILD SUCCESS。
  - owner doc `docs/design/wallet-and-assets.md:52` 由「`refund`（退款流程未实现，预留语义）」更新为「`recharge-refund`（充值退款异步对账已落地）」；`:81` 起新增「充值退款对账」小节（回冲语义 / 幂等 / D1 降级 / 与正向入账对称）。
  - log `docs/logs/2026/06-29.md` 已追加本 successor Phase 1 交付条目（Skill loading / Add / Fix / Proof / Decision / 验证 / IoC 注入缺口发现逐一记录）。
  - 文本一致性：Plan Status `completed` ↔ Phase 1 Status `completed` ↔ 7 Exit Criteria 全 `[x]` ↔ 10 Closure Gates 全 `[x]` ↔ log 一致。
  - Anti-Hollow：`refundRechargeByNotify` 实现体无空 `{}`/`return null`/吞异常，经 `PaymentCallbackImpl.onRefundSuccess` RC 分支运行时可达；6 测试覆盖正向回冲与各类降级路径。
  - Deferred 诚实：3 项 Deferred（admin 主动退款同步动作 / 负余额自动回冲 / 真实微信沙箱联调）均带触发条件与 Successor 标注，无活缺陷或契约漂移隐藏其中。
- Verdict: `approved` — 所有 Exit Criteria 与 Closure Gates 经独立 session 对照 LIVE repo 核验通过，本 plan 可标记 `completed`。

Follow-up:

- 无新增非阻塞 follow-up（既有 Deferred 三项已在「Deferred But Adjudicated」节明确归属，不在 closure 时新增）。
