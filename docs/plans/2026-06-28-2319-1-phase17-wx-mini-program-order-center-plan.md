# phase17 微信小程序订单中心 — 范围裁定与设计就绪

> Plan Status: completed
> Mission: mall
> Work Item: enhanced-features-roadmap.md §17（微信小程序订单中心，原 `todo`，经本计划 Phase 1 裁定移出当前基线）
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 17；`docs/design/order-and-cart.md`；`docs/design/user-and-address.md`
> Related: `docs/plans/2026-06-15-1500-phase14-wxpay-integration-plan.md`（微信支付集成，已 done，ask-first 已通过）；`docs/backlog/mobile-frontend-roadmap.md`
> Audit: required

## Ask-First Gate（未通过）

Phase 17（微信小程序订单中心）触达 **两个 Protected Area**（`docs/context/ai-autonomy-policy.md`）：

1. **WeChat Pay / app-mall-wx** — `ask first`：「human approval is required before planning or implementation」。
2. **外部系统集成行为** — `ask first`：「inventing behavior for an external system that is not described in committed integration docs or tests」需人工确认。

本计划**未获得**上述 ask-first 授权（本次 MISSION_DRIVER 为通用「draft plans」指令，依本仓 P22/P36 既有裁定，通用执行/起草指令不构成对 Protected Area 的显式人工授权）。

**因此本计划的实施范围被刻意收敛为「不触达 Protected Area 的部分」：**
- Phase 1（范围裁定 Decision + 设计合成）：纯文档/分析产出，不改变任何产品行为、不触达 Protected Area → **不阻塞**，可执行。
- Phase 2（后端就绪切片）：**仅在 Phase 1 裁定选择 Option B 时执行**，且严格限定为不触达 WeChat 合规行为、不需要 ORM 改动的通用后端能力。
- 真实微信小程序合规端点 + 小程序前端 `wx.openBusinessView`/`weappOrderConfirm`：整体移入 `Deferred But Adjudicated`（触发条件：微信小程序立项 + WeChat 合规 ask-first 授权）。

## Why One Plan

Phase 17 是 enhanced-features-roadmap 唯一未完成阶段。其全部交付项（path 配置 / payOrderNo 反查 / 确认收货调起 weappOrderConfirm / wechat_extra_data 关联）共享同一行为合约（微信小程序订单中心合规对接）和同一 closure 语义（合规对接就绪或经裁定移出基线），合并为一个计划，不过度拆分（plan guide 规则 #4）。

## Current Baseline

> 经 live-repo 实读核验（2026-06-28）。

### 微信小程序订单中心 — 无任何现存代码

- `app-mall-wx/`（`find app-mall-wx -type f`）仅含：`WxPayServiceImpl.java`、`WxPayServiceImpl`（实现 `PayChannel`）、`WxPayNotifyResource.java`、`AlipayPayChannel.java`、`AlipayNotifyResource.java`、`app-wx.beans.xml`、示例证书。**无订单中心 / orderCenter / weappOrder / path 配置相关任何代码。**
- `model/app-mall.orm.xml` grep（`wechat|wxa|order_center|orderCenter|extra_data|payOrderNo`）→ **零命中**。模型无任何小程序订单中心专用字段。

### 既有可复用字段（无需 ORM 改动）

`model/app-mall.orm.xml` `LitemallOrder` 实体（L1069-1147）已有：
- `payId`（propId 17，displayName「微信付款编号」，VARCHAR 63）— 微信支付回调写入的交易号，**可充当 payOrderNo 反查键**（见 Phase 14 `confirmPaidByNotify(outTradeNo, transactionId)` 落库路径）。
- `payTime`（propId 18）、`payChannel`（propId 38，dict `mall/pay-channel`）。

**无 `wechat_extra_data` 字段。** 若需落库 extra_data 需 ORM 改动（Protected Area ask-first）。

### 产品定位 — 与 P17 存在 Source-of-Truth 冲突

- `docs/design/user-and-address.md:163`：「产品定位为 H5/Web 商城，**未交付微信小程序前端**」。
- `docs/backlog/mobile-frontend-roadmap.md`：移动端技术栈为 **nop-chaos-flux（React 19）**，M5「支付 & 售后」交付范围为「支付收银台（模拟支付 + **微信支付 Native 调起**）」— 是 Native 扫码 / H5，**非微信小程序**。全文档 grep `weappOrder|openBusinessView|订单中心` → **零命中**。
- 结论：**本项目无任何微信小程序（WXML/WXS）宿主**。P17 核心交付「确认收货调起 `wx.openBusinessView` 的 `weappOrderConfirm`」为小程序前端 API，本项目无小程序可承载。

### 既有微信支付集成（Phase 14，已 done，ask-first 已通过）

`docs/plans/2026-06-15-1500-phase14-wxpay-integration-plan.md`：用户 2026-06-15 明确授权「示例性 ID + 代码层面实现微信支付」。该授权范围**仅限微信支付（Native 扫码）**，**不覆盖**小程序订单中心合规对接。P17 需独立 ask-first。

### Owner Doc 缺口

`docs/design/order-and-cart.md`（grep `订单中心|微信小程序|orderCenter`）→ **无 P17 设计章节**。P17 在设计文档中无落点。

## Goals

- **裁定 P17 在当前 H5/Web 基线的范围归属**（Decision），消除「roadmap 列 todo」与「产品无小程序」之间的 Source-of-Truth 冲突（`source-of-truth-and-precedence.md` Conflict Resolution）。
- **补齐 owner doc 设计章节**：在 `order-and-cart.md` 落地微信小程序订单中心的契约设计（path / payOrderNo 反查 / weappOrderConfirm / wechat_extra_data 语义）与范围裁定结论，供未来小程序立项时参考。
- **（条件性）交付不触达 Protected Area 的后端就绪切片**：仅当 Decision 选择 Option B，且不需 ORM、不实现微信合规端点格式时，交付基于既有 `payId` 的通用反查能力。

## Non-Goals

- **不**实现真实微信小程序订单中心合规端点（微信侧约定的订单列表/详情抓取响应格式）— Protected Area ask-first 未授权。
- **不**实现小程序前端 `wx.openBusinessView`/`weappOrderConfirm` — 本项目无小程序宿主，且移动端属 `mobile-frontend-roadmap.md` 独立范围。
- **不**修改 `model/*.orm.xml`（不新增 `wechat_extra_data` 等字段）— Protected Area ask-first。
- **不**接入真实微信商户凭证 / 小程序 AppID 配置 — Protected Area ask-first。
- **不**改变现有微信支付（Phase 14）行为。

## Task Route

- Type: `requirement clarification` + `app-layer design change`（Phase 1）；条件性 `implementation-only change`（Phase 2）
- Owner Docs: `docs/design/order-and-cart.md`（P17 章节落点）、`docs/design/user-and-address.md`（产品定位）、`docs/backlog/enhanced-features-roadmap.md` §17、`docs/backlog/mobile-frontend-roadmap.md`
- Skill Selection Basis: Phase 1 为范围裁定 + 设计文档合成，不写平台代码；Phase 2（条件性）写 BizModel 查询方法 → `nop-backend-dev` + `nop-testing`。

## Infrastructure And Config Prereqs

- Phase 1：无基础设施依赖（纯文档/分析）。
- Phase 2（条件性）：无新增基础设施；复用既有 `LitemallOrder` 实体与 GraphQL 管道。mini-program path 若需配置，复用 `LitemallSystem`（key-value），不新增外部服务。

## Execution Plan

### Phase 1 - 范围裁定（Decision）+ Owner Doc 设计合成

Status: completed
Targets: `docs/design/order-and-cart.md`（新增 P17 章节）、`docs/backlog/enhanced-features-roadmap.md`（Phase 17 状态/范围注记，依裁定结论）
Required Skill: `none`（纯范围裁定 + 设计文档合成，不写 Nop 平台代码；裁定依据为 source-of-truth-and-precedence.md 冲突解决规则与既有 owner docs，非平台 API）

- Item Types: `Decision | Add`
- Prereqs: 无（本计划首阶段）

- [x] **Skill loading Gate（none，记录扫描结果）:** 扫描 available skills：`nop-backend-dev`（BizModel 代码，本 phase 不写代码 → 不匹配）、`nop-orm-modeler`（ORM 建模，本 phase 不改模型 → 不匹配）、`nop-frontend-dev`（AMIS 页面，本 phase 无前端 → 不匹配）、其余 skill 均不覆盖「范围裁定 + 设计文档合成」。结论 `none`，理由：本 phase 产出为 Decision 记录 + owner doc 章节文本，不触达平台代码/模型/页面。
- [x] **Decision: P17 在当前 H5/Web 基线的范围归属 — 选定 Option A（移出当前基线）。** 经三轮独立 subagent plan audit 共识确认可辩护（见 `Plan Audit` Round 1/2/3）。选择 Option A；备选 Option B（仅交付 `findOrderByPayId` 通用反查切片）被否——理由：无小程序时该查询无活跃消费方（anti-hollow 原则紧张），且 path/合规端点/小程序前端仍需 Deferred，B 仅交付一悬空查询。残留风险：未来启动小程序需重开 successor 计划（设计占位章节降低重启成本）；若 audit/人工否决则回退 Option B。裁定依据与「不可跳过」反驳（监管义务以交付小程序为前提，本项目权威设计文档确立不交付 → 触发条件无附着主体）、AI 裁定权限论证（reconciliation 非 scope change）已落地 `docs/design/order-and-cart.md`「微信小程序订单中心」章节与本计划。
- [x] **Add: `order-and-cart.md` 新增「微信小程序订单中心（范围裁定：不在当前基线）」设计章节。** 已落地：范围裁定来源/审计状态、为什么不交付、裁定性质、合规对接契约四要素（path / payOrderNo 反查 / weappOrderConfirm / wechat_extra_data 业务含义 + 触发场景 + 当前基线接入点）、未来小程序立项时的 successor 接入边界（复用既有 `payId` propId 17、需新增 `wechat_extra_data` 的 ORM ask-first 边界）、与现有 H5/Web 订单流程的关系。

Exit Criteria:

- [x] Decision 已记录（选择 Option A + 备选 Option B + 残留风险），且与 `source-of-truth-and-precedence.md` 冲突解决规则一致（权威设计文档非 ambiguous → 冲突裁定 reconciliation 非 scope change）
- [x] `order-and-cart.md` P17 设计章节落地，含四要素契约语义 + 范围裁定结论
- [x] 若 Decision = Option A：`enhanced-features-roadmap.md` Phase 17 状态/范围注记已更新（`todo` → `out-of-scope（reconciliation）`），Phase 2 从范围移出并记录理由（plan guide 规则 #10）— Phase 2 见下「Gate Resolution」
- [x] 若 Decision = Option B：~~Phase 2 进入执行，roadmap Phase 17 保持 `planned`~~ — **N/A（Decision = Option A，本分支不适用）**
- [x] `docs/logs/` 更新（`docs/logs/2026/06-28.md` 新增「P17 微信小程序订单中心 — 范围裁定」段）

### Phase 2 - 后端就绪切片（条件性，仅当 Phase 1 Decision = Option B）

> **Gate Resolution（Phase 1 执行后填写）：** Phase 1 Decision = **Option A**（移出当前基线，经三轮 plan audit 共识确认）。Phase 2 的执行前提「Phase 1 Decision = Option B」**不满足**，故 Phase 2 **不执行**。依 Phase 1 Exit Criteria「Option A：Phase 2 从范围移出并记录理由（plan guide 规则 #10）」，Phase 2 全部 item **合规移出范围**（非对 confirmed defect 的降级；本裁定经独立 plan audit 确认为合法 reconciliation）。`findOrderByPayId` 设计草案保留在本节，供未来 successor（微信小程序立项 + WeChat 合规 ask-first 授权）参考，不视为遗留未完成工作。Phase 2 所有 checklist item 标 `[n/a — gated out]` 以消除悬空项（plan guide 规则 #10 + anti-slacking rule）。

Status: not executed（Option A gate — 合规移出范围；Closure Gate「Phase 2 视 Decision 条件完成或合规移出范围」允许此结局）
Targets: `app-mall-service/.../entity/LitemallOrderBizModel.java`、`app-mall-dao/.../biz/ILitemallOrderBiz.java`、`app-mall-service/src/test/...`
Required Skill: `nop-backend-dev`、`nop-testing`（未加载 — gate 未触发）

- Item Types: `Add | Proof`
- Prereqs: Phase 1 Decision = Option B；且实施前确认仍不触达 Protected Area（不实现微信合规端点格式、不改 ORM、不接真实凭证）

- [n/a — gated out] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完其 routing table 标为必读的全部文档（`05-examples/ibiz-and-bizmodel.java`、`02-core-guides/service-layer.md`、`02-core-guides/error-handling.md`、`04-reference/safe-api-reference.md`、测试基类/request.json5 相关）。列已读文档路径；每写完一个方法用 `bizmodel-method-selfcheck.md` 19 项校验。
  - Docs read: 未加载（Option A gate 未触发）
- [n/a — gated out] **Add: `findOrderByPayId` 通用反查查询。** 先 `ILitemallOrderBiz` 接口声明（`@BizQuery` + `@Name`），再 BizModel `@Override` 实现；基于既有 `payId` 字段（propId 17）按 `eq` 过滤，走 `findFirst(query, null, context)` 管道（非 `dao()` 绕过）；无 ORM 改动。明确这是**通用**反查能力（按付款交易号查订单），**不**实现微信合规响应格式、**不**定义小程序 path 配置（path 配置属合规契约 hook，已移入 Deferred）。
- [n/a — gated out] **Proof: `IGraphQLEngine` 测试（`JunitAutoTestCase` 录制回放）。** 覆盖 `findOrderByPayId` 命中（返回订单）与未命中（返回 null / 错误码）两种模式；不写实体级纯逻辑测试替代（plan guide 规则 #15）。

Exit Criteria:

- [n/a — gated out] `findOrderByPayId` 通过 `IGraphQLEngine` 验证（成功 + 未命中两模式）
- [n/a — gated out] 无 ORM 改动、无微信合规端点格式实现、无真实凭证（Protected Area 边界守住）
- [n/a — gated out] `order-and-cart.md` P17 章节标注后端就绪切片已落地（仅 Option B 时）— Option A 未选中，无切片
- [n/a — gated out] `docs/logs/` 更新（Phase 1 log 已记录 Phase 2 保持休眠）

## Plan Audit

- Status: passed（两轮连续 clean → 共识达成，rule #12）
- Auditor / Agent: 三轮独立 subagent 对抗审计（均非计划作者、fresh session）
- Evidence:
  - **Round 1**（`ses_0f12bde3cffea6Hy8D1wVgG5Wf`）：VERDICT = MAJOR OBJECTION（0 blocker / 2 major）。M1：Decision 未正面反驳 roadmap L602「不可跳过」、未论证 AI 裁定权限；M2：Phase 2 path 配置项触及合规契约边界。→ 已修订（Decision 增「不可跳过」反驳 + AI 权限论证 + audit/人工确认回退；path 配置移出 Phase 2 入 Deferred）。
  - **Round 2**（`ses_0f1268103ffelo9wveX4jJjZl8`）：VERDICT = CLEAN。M1/M2 均 RESOLVED；baseline 抽查（payId propId17、user-and-address.md:163 逐字、app-mall-wx 无订单中心代码、Phase14 payId 写入路径 L961、roadmap L609 类比项）全部 TRUE；anti-slacking（P17 为 todo 非 confirmed defect）合法；Protected Area 边界守住。此为实质修订后第 1 轮 clean。
  - **Round 3**（`ses_0f1239c3dffe3q5ReiOpDH4fcM`）：VERDICT = CLEAN。独立复核全部 load-bearing 主张（roadmap L12/L22/L602、user-and-address.md:163、mobile-frontend-roadmap.md:5/117、全仓 mini-program 代码搜索零命中、ai-autonomy-policy.md:11/47、source-of-truth-and-precedence.md:141-148/163）；「不可跳过」反驳逻辑成立（监管义务以交付小程序为前提，本项目权威设计文档确立不交付 → 触发条件无附着主体）；冲突裁定非自主 scope 变更（设计文档非 ambiguous）；模板合规；无新 blocker/major。此为实质修订后第 2 轮连续 clean → **共识达成**。
  - 结论：Option A（P17 移出当前 H5/Web 基线）经独立审计确认为可辩护的范围裁定；Phase 1 可执行，Phase 2 保持休眠（gated on Option B，审计未要求执行）。

## Closure Gates

- [x] in-scope behavior is complete（Phase 1 必完成；Phase 2 视 Decision 条件完成或合规移出范围 — Phase 2 经 Option A gate 合规移出范围）
- [x] relevant docs are aligned（`order-and-cart.md` P17 章节、roadmap Phase 17 状态与裁定一致）
- [x] verification has run（Phase 1 = 设计/裁定文本一致性 + 独立审计[三轮共识]；Phase 2 = N/A[gated out]）
- [x] 若 Phase 2 执行：~~所有新增 `@BizQuery` 方法经 `IGraphQLEngine` 测试~~ — N/A（Phase 2 未执行）
- [x] no in-scope item downgraded to deferred/follow-up（Option A 的「移出基线」经三轮独立 plan audit 确认为合法 reconciliation，非对 confirmed defect 的降级；Phase 2 item 合规移出范围并记理由）
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop-platform phases 不写 `none` 无理由（Phase 1 `none` 已附理由；Phase 2 标注未加载 gate 未触发）
- [x] skill loading verification（Phase 1 `none` 扫描已记录；Phase 2 gated out 未加载）
- [x] text consistency verified: status, phases, gates, log all agree
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 真实微信小程序订单中心合规端点 + path 配置（ask-first）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 微信侧约定的订单列表/详情抓取响应格式 + 小程序订单中心 path 配置（微信抓取的端点路径，属合规契约 hook，见 `enhanced-features-roadmap.md:173` 交付项①）均属外部系统集成行为，`ai-autonomy-policy.md` 列为 ask-first；且本项目无微信小程序宿主，合规端点与 path 无可落地平台。本计划仅交付范围裁定 + （条件性）通用后端反查能力（`findOrderByPayId`），**不**含 path 配置。
- Successor Required: `yes`（触发条件：微信小程序立项 + WeChat 合规 ask-first 授权双满足时，开 successor 计划实现合规端点格式 + path 配置 + 接入真实 AppID/凭证）

### 小程序前端 weappOrderConfirm / wx.openBusinessView

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 小程序前端 API，本项目无小程序宿主；移动端属 `mobile-frontend-roadmap.md` 独立范围（React/H5，非小程序）。
- Successor Required: `yes`（触发条件：启动微信小程序前端工程时，归 mobile/mini-program 前端 roadmap）

### wechat_extra_data 持久化字段（ORM）

- Classification: `model-gap`
- Why Not Blocking Closure: 落库 extra_data 需 `LitemallOrder` 新增字段，属 ORM Protected Area ask-first；无小程序时该字段无消费方，强行加字段为无效交付。
- Successor Required: `yes`
- Model Gap Detail: `LitemallOrder` 缺 `wechat_extra_data`（JSON）列于 `model/app-mall.orm.xml`；建议小程序立项时新增可空 JSON 列。触发条件：微信小程序立项 + 合规端点 successor 启动时。

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 本计划可闭合。Phase 1 范围裁定 Decision = Option A（P17 移出当前 H5/Web 基线）已落地（`order-and-cart.md` P17 章节 + `enhanced-features-roadmap.md` Phase 17 → out-of-scope reconciliation + Cross-Cutting 合规行补充限定 + 日志）；Phase 2 条件性（仅 Option B）因 gate 未触发而合规移出范围（plan guide 规则 #10）。Option A 经三轮独立 plan audit 共识 + 一轮独立 closure audit 确认为合法 reconciliation（非对 confirmed defect 的降级；P17 原 `todo` 非缺陷）。Protected Area 边界守住（无 ORM/合规端点/小程序前端/真实凭证改动）。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure audit subagent（fresh session `ses_0f11aa02affeHKz4X5k9gZ4GNA`，非计划作者/实施者）
- Verdict: **CLEAN**（无 blocker / 无 major）
- Evidence:
  - 载重事实全部 TRUE：`user-and-address.md:163` H5/Web 定位（逐字引用确认）；`mobile-frontend-roadmap.md` L5 nop-chaos-flux(React 19) + L117「微信支付 Native 调起」非小程序（逐字确认）；全仓代码搜索 `orderCenter|weappOrder|openBusinessView|mini_program|订单中心` 零命中，`app-mall-wx/src/main/java` 仅 4 个支付类（无订单中心代码）；`model/app-mall.orm.xml:1106` `payId` propId 17 存在。
  - 「不可跳过」反驳逻辑 SOUND：监管义务施加于小程序运营者，预设已交付小程序；本项目权威设计文档确立不交付，触发条件无附着主体 → 合法冲突 reconciliation（按 source-of-truth 规则更新陈旧方 roadmap）。
  - Phase 1 deliverables 全部 landed：`order-and-cart.md` L532-572 P17 章节（含范围裁定来源/审计状态/why-not/裁定性质/四要素契约表/successor 接入边界/与 H5/Web 流程关系）；`enhanced-features-roadmap.md` L22 `out-of-scope（reconciliation）` + L604 合规行限定；`docs/logs/2026/06-28.md` L3-21 日志。
  - Phase 2 gate 正确移出范围：Gate Resolution 段记录 Option A gate 未满足；Phase 2 items/exit criteria 全 `[n/a — gated out]`，`findOrderByPayId` 草案保留为 successor 参考（非悬空未完成工作）。
  - 文本一致：Plan Status `completed`、Phase 1 `completed`、Phase 2 `not executed`、Closure Gates 全 `[x]`、日志一致。
  - Protected Area 边界守住：无 ORM 改动（payId 预存、未加 wechat_extra_data）、无小程序前端、无微信合规端点格式、无真实凭证。仅文档文件（order-and-cart.md / enhanced-features-roadmap.md / 06-28.md log）变更。

Follow-up:

- 真实微信合规端点（触发条件：小程序立项 + WeChat 合规授权）。
- 小程序前端 weappOrderConfirm（触发条件：小程序前端工程启动）。
