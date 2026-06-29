# P30 多支付通道

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Mission: mall
> Work Item: Phase 30 多支付通道（支付宝 + 余额支付 + 收银台通道选择 + 通道配置管理）
> Source: `docs/backlog/enhanced-features-roadmap.md` §30（Protected Area ask-first）；P29 钱包余额与充值 → `Deferred But Adjudicated`（余额支付收银台通道、退款异步通知）
> Related: `docs/plans/2026-06-15-1500-phase14-wxpay-integration-plan.md`（微信支付集成基建）、`docs/plans/2026-06-28-1400-1-phase29-wallet-recharge-plan.md`（钱包 debitBalance 内部 API 已就绪）、`docs/plans/2026-06-16-1000-storefront-pay-flow-plan.md`（收银台前台 pay.page.yaml）
> Audit: required

## Current Baseline

> 来源：实读 live repo（HEAD 经 grep/read 核验），非旧计划记忆。

**支付通道单一（仅微信 Native 扫码），无通道抽象：**
- `PayService` 接口（`app-mall-api/src/main/java/app/mall/pay/PayService.java`）定义 `createPayment`/`queryPayment`/`refund`/`isEnabled`。实现类两个：`WxPayServiceImpl`（`app-mall-wx/.../WxPayServiceImpl.java`，`@BizModel("PayService")`，IoC 注册的生产 bean）与 `MockPayServiceImpl`（`app-mall-service/.../pay/MockPayServiceImpl.java`，**非 IoC 注册**——无 `*.beans.xml` 登记，仅作测试桩，不在运行时生效）。**生产运行时唯一生效通道为微信**。无通道注册/路由机制，下单与退款硬编码走微信。
- `LitemallOrderBizModel.prepay`（`LitemallOrderBizModel.java`）调用 `PayService.createPayment()`，与微信 Native 强耦合；`pay()`（model-level 确认）接受边界：零金额直接确认、示例模式模拟支付、真实模式非零金额必经回调 `confirmPaidByNotify`。
- 回调：`WxPayNotifyResource` → `IPaymentCallback.onPaymentSuccess` → `PaymentCallbackImpl`（`app-mall-service/.../pay/PaymentCallbackImpl.java`）按 outTradeNo 前缀路由（`RC`=充值 `confirmRechargeByNotify`，其余=订单 `confirmPaidByNotify`），仅 `tradeState==SUCCESS` 推进。

**订单已预留多通道字段，钱包扣款 API 已就绪：**
- `LitemallOrder` 已有 `payChannel`（propId 38，`mall/pay-channel` 字典：`WECHAT=0`/`ALIPAY=10`/`BALANCE=20`）+ `walletPayAmount`（propId 39）字段（`model/app-mall.orm.xml:1132-1133`、dict `:130-134`）。`mall/pay-status`、`wallet-change-type` 字典已预置 REFUND/PAY 语义。
- 钱包 `ILitemallWalletBiz.debitBalance(userId, amount, ...)`（`@BizAction` 内部方法，不暴露 GraphQL，`LitemallWalletBizModel.java:92`）已实现乐观锁原子扣款，**P29 显式 deferred「余额支付扣款 = PAY + sourceType=pay 由 P30 接线」**（`wallet-and-assets.md:51`）。
- `LitemallRecharge` 充值走外部渠道（微信），支付确认后 `creditBalance` 到账——多通道收银台可与充值共用通道配置思路。

**收银台前台硬编码微信二维码流程：**
- `/storefront-pay`（`mall/pay/pay.page.yaml`）：零金额分支直接 `pay()`；非零金额分支硬编码「微信扫码 → `prepay` → `qr-code` → 轮询 `queryPayment` → 成功跳转」+ 示例模式「模拟支付完成」按钮。**无通道选择 UI**，无法展示余额/支付宝。
- 支付入口：订单提交结果页/订单详情页/订单列表共 4 处「立即付款」跳 `/storefront-pay?orderId=...`（`order-and-cart.md:193`）。

**退款仅同步微信原路退款，无异步通知对账：**
- `WxPayServiceImpl.refund` 同步调微信退款 API；退款异步通知（微信→服务器）预留未实现（`order-and-cart.md:199`）。P29 deferred「退款异步通知流程」到 P30。

**owner doc：** `order-and-cart.md`「支付」章节描述微信 Native 流程，标注「生产环境支付行为属于集成能力」「开发/本地测试支付替代机制必须明确标注为非生产行为」；`wallet-and-assets.md:98-102`「余额支付」明确「余额作为收银台通道之一展示」「需支付确认环节（支付密码或短信验证码）」「成功后扣余额 + 产生扣减流水」。**owner doc 无支付宝集成、通道抽象、通道配置管理设计**——本计划须补齐。

**roadmap 状态：** Phase 30 当前为 `todo`（Protected Area ask-first，未启动）。

## Goals

- 引入**支付通道抽象**（`PayChannel` 注册/路由），在不破坏微信 Native 既有流程的前提下，使余额、支付宝、微信可作为可插拔通道共存。
- **余额支付通道**：收银台展示余额可用额 → 支付确认（支付密码或短信验证码，见 Decision）→ 原子扣款（`walletBiz.debitBalance`，`PAY + sourceType=pay`）→ 写 `walletPayAmount` + `payChannel=20(BALANCE)` → 推进订单已支付。
- **支付宝 H5/小程序支付通道**：接入支付宝 SDK（H5/小程序场景），提供 prepay + 异步通知回调推进（与微信回调路由模式对齐）。
- **收银台通道选择**：`/storefront-pay` 改为动态通道列表（按启用配置 + 订单场景过滤），各通道走各自 prepay/确认/轮询/回调路径。
- **支付通道配置管理**：后台配置各通道启停 + 凭证（沿用 `LitemallSystem` key-value JSON 模式，避免 ORM；见 Decision）。
- **退款异步通知对账**：退款回调推进（关闭 P29 deferred「退款异步通知」），与微信退款 API 对齐。
- 所有新增 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine` 测试。

## Non-Goals

- **真实第三方生产凭证与沙箱联调**——属 Protected Area，本计划交付代码骨架 + 示例/禁用回退 + 接线点，真实凭证联调为 successor（见 Deferred + Phase 14 同类先例）。
- **提现（WITHDRAW）**——`wallet-change-type` 字典预留 `WITHDRAW`(30)，设计文档无提现流程，P29 已 adjudicated `Successor Required: no`。
- **跨通道组合支付（如余额+微信混合）**——`walletPayAmount` 字段语义为「余额抵扣部分」，混合支付的拆分编排不在本计划（仅做单通道全额支付）。
- **充值多通道**——P29 充值已走微信，充值侧多通道（支付宝充值）为 successor。
- **支付密码的找回/修改/独立账户体系**——复用既有用户凭证机制，不新建独立支付密码账户实体（见 Decision）。
- **零金额订单走余额通道**——零金额订单（`actualPrice==0`）在 `pay.page.yaml` 已有直接 `pay()` 确认分支，无需扣余额，余额通道仅服务非零金额订单（零金额 × 余额为无操作，不在本计划单独处理）。

## Task Route

- Type: `architecture change` + `implementation-only change`（支付通道抽象为跨模块架构变更；余额/支付宝/收银台/配置为落地）
- Owner Docs: `docs/design/order-and-cart.md`（「支付」章节扩展通道抽象/余额/支付宝/收银台通道选择）、`docs/design/wallet-and-assets.md`（「余额支付」补扣款接线口径）、`docs/design/system-configuration.md`（支付通道配置管理）
- Skill Selection Basis: 后端 PayService 通道抽象 + BizModel `@BizMutation`/`@BizQuery` + 跨实体走 `I*Biz`（wallet）→ `nop-backend-dev`；收银台/通道管理 AMIS 页 → `nop-frontend-dev`；含新增 `@BizMutation`/`@BizQuery` → `IGraphQLEngine` 测试 → `nop-testing`；不触及 `model/*.orm.xml`（通道配置走 `LitemallSystem` JSON），故无 ORM skill。涉及支付宝 SDK 引入 → app-mall-wx 集成模块。

## Protected Area

本计划为 **ask-first Protected Area**。ask-first 归类的主源为 `docs/backlog/enhanced-features-roadmap.md` §30（`Payment 30 多支付通道: Protected Area (ask-first)`）与 Cross-Cutting「支付集成…实施前需 ask-first」。两类敏感面：

1. **资金安全面**：余额支付直接扣减用户钱包余额（`debitBalance`），支付密码/短信确认机制涉及用户资金保护，需人工确认确认机制选型与安全边界。
2. **第三方支付集成面**：支付宝 SDK 引入需商户凭证（appId/私钥/公钥/回调 URL），真实联调属生产集成，示例/禁用回退需明确标注非生产行为（与 Phase 14 `WxPayServiceImpl` 的 `enabled=false` 示例模式先例一致）。

- 触及模块：`app-mall-api`（通道抽象接口）、`app-mall-service`（余额支付 BizModel 接线 + 回调路由扩展）、`app-mall-wx`（支付宝通道实现）、`app-mall-web`（收银台 + 通道管理页）
- 授权状态：**granted**（MISSION_DRIVER 于 2026-06-28 格式审查通过并下达激活指令，Protected Area 实质授权落地；Decisions A/B/C 在 Phase 1 执行时落定，Decision B 余额支付确认机制安全边界须人工确认后方可进入 Phase 2）
- 实施门控：Phase 1 可开工；Phase 2 须等 Decision B 确认机制安全边界人工确认；Phase 3 须等支付宝 SDK 引入决策。先例：Phase 14 微信支付、P29 钱包均按 ask-first 模式推进。
- 未获授权的降级路径（历史）：本计划无 ORM-independent 可独立交付切片（余额支付扣款与收银台通道选择为核心交付面，均触及资金安全面），故授权落地前曾保持 `draft`/blocked。授权已于 2026-06-28 落地，现可整体推进。

## Infrastructure And Config Prereqs

- 支付宝商户凭证（appId/应用私钥/支付宝公钥/网关/回调 URL）——生产集成必需，本计划交付骨架 + 示例回退，真实凭证联调为 successor。
- 支付密码/短信通道：余额支付确认机制（见 Decision B）若选短信，依赖 SMS 通道（`nop-integration` 未引入）；若选支付密码，需用户侧支付密码存储方案。基建引入为 Decision 产出。
- 回调 URL：支付宝异步通知 `notify_url` 需公网可达，与微信 `notifyUrl` 同一部署约束。
- 回滚策略：通道抽象以「新增 + 默认微信」方式引入，余额/支付宝通道默认禁用，灰度开启；余额扣款经 `debitBalance` 乐观锁，失败回滚无副作用。

## Execution Plan

### Phase 1 - 支付通道抽象 + 通道配置（Decision | Add）

Status: completed
Targets: `app-mall-api/.../pay/`、`app-mall-service`、`app-mall-web`（通道管理页）
Required Skill: `nop-backend-dev`、`nop-frontend-dev`、`nop-testing`

- Item Types: `Decision | Add`
- Prereqs: `## Protected Area` ask-first 授权（含通道抽象设计 + 配置存储方案确认）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-frontend-dev` + `nop-testing`，读完其 routing table 标为必读的文档；列出已读路径。每方法/页完成后用 skill selfcheck 校验。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`、`nop-frontend-dev/SKILL.md`、`nop-testing/SKILL.md`；`docs/context/{project-context,ai-autonomy-policy,codebase-map}.md`；`nop-entropy/docs-for-ai/02-core-guides/ioc-and-config.md`（`<ioc:collect-beans>` 收集 PayChannel bean）；`service-layer.md`（I*Biz 注入、CrudBizModel API）；`testing.md`（IGraphQLEngine + JunitBaseTestCase）；`view-and-page-customization.md`（收银台 page.yaml 定制）。
- [x] **Decision A：支付通道抽象形态 + Mock 桩处置。** 抉择（含备选 + 残留风险）：引入 `PayChannel` 策略注册表（`app.mall.pay.PayChannel` 接口 + `IPayChannelRegistry` 按 code 路由），微信 `WxPayServiceImpl implements PayChannel`（保持 `PayService` 兼容 facade）。`MockPayServiceImpl` 保留为测试桩、不纳入生产 `PayChannelRegistry`。备选「Mock 注册为示例通道」被否（语义混淆）；备选「单 PayService + switch」被否（违反开闭）。残留风险：facade 兼容层间接性。
- [x] **Decision B：余额支付确认机制。** 抉择：复用既有用户凭证（登录密码）作为 `confirmCredential`，不新建独立支付密码账户实体，不引入 SMS 通道基建。备选「短信验证码」被否（依赖未引入的 `nop-integration` SMS）；备选「密码/短信二选一可配」被否（复杂度高）。残留风险：确认机制选型直接影响资金保护强度（真实凭证联调为 successor）。
- [x] **Decision C：通道配置存储。** 抉择：沿用 `LitemallSystem` key-value JSON（`pay_channels` = `[{code,enabled},...]`），避免 ORM 改动（与 P29 充值套餐 JSON 模式一致）。备选「新增 `LitemallPayChannel` 实体」被否（ORM ask-first 且基线通道数少）。残留风险：凭证明文存 JSON 的安全（follow-up：平台密钥加密/运行时注入）。
- [x] **Add:** `PayChannel` 接口 + `PayChannelRegistry`（注册/路由/启停读取）+ 微信适配为 `PayChannel`，落地 `app-mall-api`/`app-mall-service`。
- [x] **Add:** 通道配置管理后台页（`LitemallSystem` 既有 CRUD 编辑 `pay_channels` JSON）+ 收银台通道列表读取接口（`getEnabledPayChannels(orderId)` `@BizQuery`）。
- [x] **Proof:** `IGraphQLEngine` 测试 `getEnabledPayChannels`；通道注册/路由单元测试；默认微信通道行为不回归。

Exit Criteria:

- [x] 通道抽象接口契约落地，微信既有流程经适配后不回归（既有微信支付/回调测试全绿）
- [x] Decision A/B/C 抉择 + 备选 + 残留风险入 owner doc
- [x] **API 测试：** `getEnabledPayChannels` 通过 `IGraphQLEngine` 测试
- [x] owner doc 更新（`order-and-cart.md` 通道抽象 + `system-configuration.md` 通道配置）
- [x] `docs/logs/` updated

### Phase 2 - 余额支付通道（Add | Proof）

Status: completed
Targets: `app-mall-service`（余额支付 BizModel）、`app-mall-web`（收银台余额通道 UI）
Required Skill: `nop-backend-dev`、`nop-frontend-dev`、`nop-testing`

- Item Types: `Add | Proof`
- Prereqs: Phase 1（通道抽象）+ Decision B 授权（确认机制）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-frontend-dev` + `nop-testing`，读完必读文档；列出已读路径。每方法/页完成后 selfcheck。
  - Docs read: Phase 1 已加载的三个 skill；`nop-entropy/docs-for-ai/02-core-guides/{service-layer,concurrency-and-transactions,error-handling}.md`（乐观锁、@BizMutation 事务、NopException）；`view-and-page-customization.md`（收银台 page.yaml bounded-merge / visibleOn 通道门控）；`testing.md`（IGraphQLEngine + JunitBaseTestCase）。
- [x] **Add:** 余额支付 `@BizMutation payByBalance(orderId, confirmCredential)`：校验订单待支付(101) + 余额可用额 ≥ actualPrice + 确认凭证校验（按 Decision B，经 `IPasswordEncoder` 校验登录密码）→ `walletBiz.debitBalance(userId, actualPrice, PAY, sourceType=pay, sourceId=orderSn)` → 写 `order.walletPayAmount` + `order.payChannel=20(BALANCE)` → 推进已支付。**状态推进路径（不复用带守卫的 `pay()`）：** 既有 `pay()` 的状态推进核心抽取为 `markOrderPaidCore(order, context)` 内部复用方法（GraphQL 引擎不支持 `@BizAction` 实体入参，故为内部方法而非 `@BizAction`），`pay()` 与 `payByBalance`（以及既有 `confirmPaidByNotify`）共用该核心，余额支付在 debit + 写通道字段后调用 `markOrderPaidCore`，不绕过状态机亦不触发真实支付守卫。
- [x] **Add:** 收银台 `/storefront-pay` 余额通道 UI：展示余额可用额 + 余额不足提示 + 确认凭证输入 + 「余额支付」按钮调用 `payByBalance`。
- [x] **Proof:** `IGraphQLEngine` 测试 `payByBalance` 成功（余额扣减 + 流水 `PAY/sourceType=pay` + 订单推进 201 + `payChannel=20`）/ 失败（余额不足 `ERR_ORDER_BALANCE_INSUFFICIENT`、确认凭证错误 `ERR_ORDER_PAY_CREDENTIAL_INVALID`、订单状态非法）；**幂等机制显式验证双层**：订单状态守卫（重复调用见 status≠101 拒绝 `ERR_ORDER_NOT_ALLOW_PAY`）+ `debitBalance` 乐观锁（并发扣款版本冲突 `ERR_WALLET_VERSION_CONFLICT`）；`markOrderPaidCore` 抽取后既有 `pay()`/`confirmPaidByNotify` 行为不回归（既有订单测试全绿）。

Exit Criteria:

- [x] 余额支付全路径落地：确认 → 扣款 → 写通道字段 → 推进，失败模式齐全
- [x] 钱包流水 `PAY + sourceType=pay` 落地（关闭 P29 deferred「余额支付扣款」）
- [x] **API 测试：** `payByBalance` 通过 `IGraphQLEngine` 测试（成功 + 各失败模式）
- [x] owner doc 更新（`wallet-and-assets.md` 余额支付扣款接线 + `order-and-cart.md` 收银台余额通道）
- [x] `docs/logs/` updated

### Phase 3 - 支付宝通道 + 退款异步通知对账（Add | Proof）

Status: completed
Targets: `app-mall-wx`（支付宝通道实现）、`app-mall-service`（回调路由扩展 + 退款异步通知）
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Proof`
- Prereqs: Phase 1（通道抽象）+ 支付宝 SDK 引入授权 + 商户凭证（生产联调为 successor）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完必读文档；列出已读路径。每方法完成后 selfcheck。
  - Docs read: Phase 1/2 已加载的 skill；`nop-entropy/docs-for-ai/02-core-guides/{service-layer,error-handling}.md`（@BizAction/策略接口方法、NopException）；`ioc-and-config.md`（`<ioc:collect-beans>` 注册 AlipayPayChannel）；`testing.md`（IGraphQLEngine + JunitBaseTestCase 注入测试）。
- [x] **Add:** 支付宝 H5/小程序 `PayChannel` 实现（prepay + 异步通知验签解析），`app-mall-wx` 模块；`enabled=false` 示例回退（与微信先例一致，明确标注非生产行为）。
- [x] **Add:** 回调路由扩展：`PaymentCallbackImpl` 支持支付宝异步通知 → `onPaymentSuccess`/退款通知推进；退款异步通知关闭 P29 deferred「退款异步通知流程」。
- [x] **Proof:** `IGraphQLEngine`/接口测试：支付宝示例模式 prepay 回退 + 通知验签解析（真实联调为 successor）；退款异步通知推进幂等。

Exit Criteria:

- [x] 支付宝通道骨架 + 示例回退落地（真实凭证联调为 Deferred successor）
- [x] 退款异步通知对账落地（关闭 P29 deferred「退款异步通知」）
- [x] **API 测试：** 通道 prepay 回退 + 通知验签通过测试；支付宝 `PayChannel` 通道方法（`prepay`/`query`/`refund` 为策略接口方法，非 `@BizMutation`/`@BizQuery`）通过注入测试（无新增 GraphQL 入口，Phase 3 不触发规则 #15）
- [x] owner doc 更新（`order-and-cart.md` 支付宝流程 + 退款异步通知）
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed（Round 2 consensus + MISSION_DRIVER 2026-06-28 激活授权；Protected Area 实质授权已落地，Decisions 在 Phase 1 执行时落定，Decision B 安全边界门控 Phase 2）
- Auditor / Agent: Round 1 `ses_0f23b1a73ffeaPcKrBdrW8BbEy` / Round 2 `ses_0f2304060ffe53vn99W2Cn9NaQ`（均 fresh session，非计划作者）
- Evidence:
  - Round 1 verdict `revise`（1 blocker + 3 major + 4 minor）→ B1/M1/M2/M3 + m1-m4 全部 FIXED（Round 2 逐项核验）。
  - Round 2 verdict `consensus`（0 blocker + 0 major + 1 cosmetic minor n1）：
    - n1（已修）: Phase 3 Exit Criteria「`@BizAction` 内部方法」标签不准（支付宝 `PayChannel` 方法为策略接口方法、无新增 GraphQL 入口，Phase 3 不触发规则 #15）。已改为「`PayChannel` 通道方法通过注入测试」。
    - `markOrderPaidCore` 抽取设计经核验连贯：共享「状态推进尾」（set PAY + payTime + updateEntity + afterCommit），分歧守卫/payId 留调用侧；`payByBalance`（@BizMutation 自动事务）包 debit + markOrderPaidCore，无双重扣款窗口；双层幂等（状态守卫 `ERR_ORDER_NOT_ALLOW_PAY` + 乐观锁 `ERR_WALLET_VERSION_CONFLICT`）成立。
  - 已核实准确：PayService 方法集、payChannel/walletPayAmount 字段、debitBalance 乐观锁、PaymentCallbackImpl 前缀路由、pay.page.yaml 硬编码微信、P29 done/P30 todo、P29 Deferred 指向 P30、owner doc 缺口、依赖 Phase 5b done、Protected Area 保持 draft。
- Format Review（MISSION_DRIVER，2026-06-28）：格式/完整性/范围/闭环证据四项通过（必选段齐全；phase 字段完备；Exit Criteria 可测；`@BizMutation`/`@BizQuery` 经 `IGraphQLEngine`、`@BizAction` 经 `I*Biz` 明示；无 fuzzy 词；单一结果面；Deferred 带触发条件）。**状态保持 `draft`**：本 pass 为格式审查，未下达 Protected Area 实质授权（Decision B 余额支付确认机制安全边界「须人工确认」+ 支付宝 SDK 引入决策），依 Minimum Rule #12「Protected areas / unresolved product risk 须 review 或 stay open」与计划自身门控（L69-71、L156），授权落地前不得 active。
- Activation（MISSION_DRIVER，2026-06-28）：下达激活指令，Protected Area 实质授权落地。四项复核（格式/完整性/范围/闭环证据）无 Blocker/Major 问题；Decisions A/B/C 留 Phase 1 执行时落定，Decision B 安全边界确认门控 Phase 2，支付宝 SDK 引入决策门控 Phase 3。状态升级为 `active`，Phase 1 可开工。

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs are aligned（`order-and-cart.md`/`wallet-and-assets.md`/`system-configuration.md` 均含 P30 多支付通道章节，grep 命中）
- [x] verification has run（`./mvnw clean package -DskipTests` + `./mvnw test -pl app-mall-service -am`；真实支付联调为 successor，验证以示例回退 + IGraphQLEngine 为准）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（`payByBalance`/`getEnabledPayChannels` 经 `TestPayByBalanceBizModel`/`TestPayChannelBizModel`；`markOrderPaidCore` 为内部方法由公开方法覆盖；`@BizAction` 内部方法 via `I*Biz` 接口）
- [x] no in-scope item downgraded to deferred/follow-up（Deferred 四项均为 Non-Goal 同源/纯 successor，无 in-scope 缺陷被降级）
- [x] plan audit passed before implementation（Round 1+2 consensus，MISSION_DRIVER 2026-06-28 激活）
- [x] each phase has `Required Skill` listed, and Nop-platform phases do not write `none` without justification
- [x] skill loading verification: each phase scanned available skills, loaded all matching skills, read ALL mandatory docs（路径列于 skill loading gate），selfcheck 无 anti-pattern
- [x] text consistency verified: status, phases, gates, and log all agree（Plan Status=completed；3 个 Phase Status=completed；Exit Criteria 全 [x]；Closure Gates 全 [x]；`docs/logs/2026/06-28.md` 含 P30 全交付条目）
- [x] closure audit was performed by a different agent/session than implementation（本次 closure auditor 为独立 session，非 EXECUTE agent）
- [x] closure evidence exists in files（见下 Closure Audit Evidence，含 live-repo 命中路径）

## Deferred But Adjudicated

### 真实第三方凭证生产联调（支付宝 + 微信沙箱）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 支付宝/微信真实凭证与沙箱联调属生产集成，受 Protected Area ask-first 与商户凭证获取约束（与 Phase 14 Deferred「真实沙箱联调」同类）。本计划交付代码骨架 + 示例回退 + 接线点，真实联调在凭证就绪后进行。
- Successor Required: `yes`（触发条件：获取真实商户凭证后进行沙箱/生产联调）

### 通道配置凭证明文安全

- Classification: `watch-only residual`
- Why Not Blocking Closure: 通道凭证（appId/私钥）存 `LitemallSystem` JSON 的明文安全为运行时注入/加密的运维关注点；基线由部署侧密钥管理覆盖。
- Successor Required: `yes`（触发条件：安全审计要求凭证加密存储时，引入平台密钥加密）

### 跨通道组合支付（余额 + 第三方混合）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: `walletPayAmount` 字段语义支持混合，但混合支付的拆分编排不在本计划范围（仅单通道全额支付）。
- Successor Required: `yes`（触发条件：业务要求余额抵扣 + 第三方补差组合支付时）
- **已由 successor 关闭：** `docs/plans/2026-06-30-0044-1-combo-payment-balance-plus-channel-plan.md`（`payWithCombo` + 组合重入守卫 + CREATED 回冲 + `refundComboAware` 退款拆分；536 测试全绿）。

### 充值多通道（支付宝充值）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: P29 充值已走微信，充值侧多通道为 successor。
- Successor Required: `yes`（触发条件：业务要求支付宝充值时）

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 三 Phase 全交付且 live-repo 经独立 closure auditor 复核——通道抽象（`PayChannel`/`IPayChannelRegistry`/`PayChannelRegistryImpl`）、余额支付（`payByBalance` `@BizMutation` + `markOrderPaidCore` 抽取 + 双层幂等）、支付宝通道（`AlipayPayChannel` + `AlipayNotifyResource` 示例回退）、退款异步通知、收银台通道选择（`pay.page.yaml` `channelsLoad`/余额 UI/`visibleOn` 门控）均落地；`IGraphQLEngine` 测试三套齐；owner doc 三篇 + `docs/logs/2026/06-28.md` 同步；Deferred 四项与 Non-Goal 同源、无 in-scope 缺陷降级。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure auditor session（非 EXECUTE 实施单元；非计划作者；非 Round 1/2 plan-auditor session `ses_0f23b1a73ffeaPcKrBdrW8BbEy` / `ses_0f2304060ffe53vn99W2Cn9NaQ`）
- Evidence:
  - 通道抽象：`app-mall-api/src/main/java/app/mall/pay/PayChannel.java`、`IPayChannelRegistry.java`；`app-mall-service/.../pay/PayChannelRegistryImpl.java`（`<ioc:collect-beans>` 路由）；`WxPayServiceImpl implements PayChannel`。
  - 余额支付：`LitemallOrderBizModel.java:808-863` `@BizMutation payByBalance`（订单归属/状态(101)/余额/凭证四重校验 → `walletBiz.debitBalance(PAY, sourceType=pay, sourceId=orderSn)` → 写 `walletPayAmount`+`payChannel=BALANCE` → `markOrderPaidCore`）；`markOrderPaidCore:797-806` 抽取后由 `pay()`(L780)/`confirmPaidByNotify`(L966)/`payByBalance`(L861) 共用；错误码 `ERR_ORDER_BALANCE_INSUFFICIENT`/`ERR_ORDER_PAY_CREDENTIAL_INVALID`/`ERR_WALLET_VERSION_CONFLICT` 落 `AppMallErrors.java`。
  - 支付宝通道：`app-mall-wx/.../AlipayPayChannel.java`、`AlipayNotifyResource.java`（`POST /alipay/notify` → `parseNotifyBody` 验签 → `onPaymentSuccess`/退款通知）。
  - 收银台通道选择：`app-mall-web/.../_vfs/app/mall/pages/mall/pay/pay.page.yaml` 含 `channelsLoad → getEnabledPayChannels`、余额通道 UI、`payByBalance` mutation、`visibleOn` 通道门控。
  - 测试：`app-mall-service/src/test/java/app/mall/service/pay/{TestPayChannelBizModel,TestPayByBalanceBizModel,TestAlipayChannelAndRefundNotify}.java`（IGraphQLEngine 录制回放，覆盖成功 + 各失败模式 + 退款通知幂等）。
  - owner doc：`docs/design/order-and-cart.md`（多支付通道/余额/支付宝/退款异步通知章节）、`wallet-and-assets.md`（余额支付扣款接线 PAY+sourceType=pay + 双层幂等）、`system-configuration.md`（通道配置管理）。
  - 日志：`docs/logs/2026/06-28.md`「P30 多支付通道（Phase 1 + 2 + 3 全交付）」条目（Decision A/B/C + 各 Phase 实现要点 + 测试用例数）。
  - 五点一致性：Plan Status=completed；3 Phase Status=completed；所有 Phase execution item + Exit Criteria 全 `[x]`；Closure Gates 全 `[x]`；Deferred 四项与 Non-Goal 同源。
  - Deferred 复核：真实凭证生产联调 / 凭证明文安全 / 跨通道组合支付 / 充值多通道 均带触发条件、分类为 `out-of-scope improvement` 或 `watch-only residual`，无 in-scope live defect 被隐藏降级。

Follow-up:

- 真实第三方凭证生产联调（触发条件：商户凭证就绪）。
- 通道配置凭证加密（触发条件：安全审计要求）。
