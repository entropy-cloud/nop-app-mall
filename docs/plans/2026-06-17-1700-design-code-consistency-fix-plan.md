# 2026-06-17-1700 设计文档与代码一致性修复

> Plan Status: completed
> Last Reviewed: 2026-06-17
> Source: `docs/analysis/2026-06-17-design-doc-consistency-audit.md`
> Related: 无（post-baseline 对齐切片，roadmap 所有 Phase 已 done）
> Audit: required

## Current Baseline

审计报告 `docs/analysis/2026-06-17-design-doc-consistency-audit.md` 确认的待处理项（状态码字典层已全对齐，偏差集中在业务规则自然语言与代码守卫精度）：

- **F-doc-code-1【重要】** 售后申请资格文档过度收窄。`docs/design/order-and-cart.md` 售后范围/流转规则写"对已收货订单的售后"、"只有已完成收货的订单才能进入可申请售后状态"；但 `LitemallAftersaleBizModel.java:194-196` 允许 `PAY(201)‖CONFIRM(401)‖AUTO_CONFIRM(402)`，且 `model/app-mall.orm.xml` aftersale-type 字典含 `GOODS_MISS 未收货退款=0`。**代码+字典为真相，文档为 doc-drift。**
- **F-name-1** `order-and-cart.md` 迁移规则与 text-art 叙述用"已申请退款"作订单状态名，订单状态表无此码（对应 202 退款中），实际语义是售后 REQUEST（orderStatus 保持 201）。
- **F-doc-code-2** `marketing-and-promotions.md` 团购活动状态表述列"开团未支付、开团中、开团失败或开团成功"四态；`app-mall.orm.xml` `LitemallGroupon.status` 与 `LitemallGrouponBizModel` 仅持久化 `0/1/2`，"开团成功"是参团人数 vs 规则要求人数的计算结果，非持久状态枚举。
- **F-doc-code-3** 团购超时(204)不可删除。`LitemallOrderBizModel.deleteOrder:478-482` 删除白名单含 `REFUND_CONFIRM(203)` 不含 `GROUPON_EXPIRED(204)`；两者都是已退款终态，文档当 204 为可删终态。**已获 human 确认：改代码，204 加入白名单。确认记录见 `docs/discussions/2026-06-17-204-deletable-confirmation.md`。**

F1/F2（本次 flow-overview.md 引入的 2 处回归）已在审计后立即修复（L3 规则文本已改、订单主状态机 mermaid 已去掉 202 中间态）；其中售后状态机 mermaid 触发边（`flow-overview.md:98`）由本计划 Phase 1 ripple 项补齐。

## Goals

- 售后资格文档与代码+字典对齐：明确售后覆盖"未发货退款"与"已收货退款/退货退款"两类
- 迁移叙述消除"已申请退款"订单状态名命名混淆
- 团购"开团成功"在文档中澄清为计算值而非持久状态
- 团购超时(204)订单纳入用户可删除白名单，与 203 已退款一致，新增 IGraphQLEngine 测试

## Non-Goals

- 不改 ORM 模型 / 字典 / DDL（字典层已对齐）
- 不改售后申请代码（代码已是真相）
- 不重构 text-art 状态叙述为 mermaid（flow-overview.md 已有 mermaid，此处仅修命名）
- 不处理审计报告中未列入的项

## Task Route

- Type: `implementation-only change`（文档对齐 + 单点代码守卫修复）
- Owner Docs: `docs/design/order-and-cart.md`、`docs/design/marketing-and-promotions.md`、`docs/design/flow-overview.md`
- Skill Selection Basis: Phase 2 修改 BizModel `@BizMutation` 守卫并加 IGraphQLEngine 测试 → nop-backend-dev + nop-testing；Phase 1 纯散文文档编辑，无 Nop 平台交互 → none（带理由）

## Infrastructure And Config Prereqs

No infra prereqs beyond existing baseline. 测试沿用 `TestLitemallOrderBizModel` 的 `@NopTestConfig(localDb=true)` 内存库模式。

## Execution Plan

### Phase 1 - 文档对齐（doc-drift 修复）

Status: completed
Targets: `docs/design/order-and-cart.md`、`docs/design/marketing-and-promotions.md`、`docs/design/flow-overview.md`
Required Skill: `none`（纯散文文档编辑，不涉及 BizModel/view/ORM/test 等 Nop 平台交互；available skills 均为 Nop 平台专用，描述不匹配）

- Item Types: `Fix`
- Prereqs: 无

- [x] **Skill loading gate:** 扫描 available skills，无匹配（nop-backend-dev/nop-frontend-dev/nop-testing 等均针对 Nop 平台代码，散文文档编辑不匹配）。无需读平台文档。
- [x] **Fix（F-doc-code-1）** 修订 `order-and-cart.md` 售后范围：将"对已收货订单的售后"扩为"覆盖未发货退款与已收货退款/退货退款"；售后流转规则将"只有已完成收货的订单才能进入可申请售后状态"改为"已支付未发货(201)或已完成收货(401/402)、且当前售后状态为可申请(INIT)的订单可进入可申请售后"，与 `LitemallAftersaleBizModel:194-196` 及 aftersale-type 字典 GOODS_MISS 对齐
- [x] **Fix（F-name-1）** 修订 `order-and-cart.md` 迁移规则与 text-art 叙述：消除"已申请退款"作为订单状态的措辞，改为按订单状态码语义描述（已支付→已退款 203），将"用户申请退款"动作明确归到售后状态机（REQUEST）描述，避免读者误以为存在"已申请退款"订单状态码
- [x] **Fix（F-doc-code-2）** 修订 `marketing-and-promotions.md` 团购活动状态：明确持久化状态为 0 开团未支付 / 1 开团中 / 2 开团失败，"开团成功"由"有效参团人数 ≥ 规则要求人数"计算得出、非 status 枚举值
- [x] **Fix | Proof（ripple）** 复查 `flow-overview.md`：售后状态机 mermaid 触发边 `[*] --> 可申请售后: 订单完成收货` 改为 `订单已支付或完成收货`；确认 L3 售后资格规则（已在前序修复中拆分）与新文档一致

Exit Criteria:

- [x] 四处文档编辑落地，售后资格、迁移命名、团购状态在 order-and-cart/marketing/flow-overview 三文档间口径一致
- [x] 售后资格描述与 `LitemallAftersaleBizModel:194-196` + aftersale-type 字典一致
- [x] 无新增"已申请退款"订单状态码表述
- [x] 注：`order-and-cart.md:81` 表格行"已超时团购 204... 删除"不在本 phase 编辑范围（文档本就正确，依赖 Phase 2 代码落地后自然对齐）
- [x] `docs/logs/` updated（与 Phase 2 合并一条）

### Phase 2 - 代码修复：团购超时(204)订单可删除

Status: completed
Targets: `app-mall-service/src/main/java/app/mall/service/entity/LitemallOrderBizModel.java`、`app-mall-service/src/test/java/app/mall/service/entity/TestLitemallOrderBizModel.java`
Required Skill: `nop-backend-dev`（修改 @BizMutation 守卫）、`nop-testing`（IGraphQLEngine 测试）

- Item Types: `Fix | Proof`
- Prereqs: Phase 1 完成（文档先对齐，避免代码与文档再次错位窗口）

- [x] **Skill loading gate:** 加载 nop-backend-dev + nop-testing，读完各自 routing table 必读文档。列出已读文档路径。每写完一个方法用 selfcheck 校验。
  - Docs read: `nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`（19 项自检）。本次仅改既有 `deleteOrder` 守卫条件（加 GROUPON_EXPIRED），未改签名/注解/错误码/注入，selfcheck 19 项全部仍通过
- [x] **Fix（F-doc-code-3）** `LitemallOrderBizModel.deleteOrder` 删除白名单（:478-482）追加 `GROUPON_EXPIRED(204)`，使 204 与 203 已退款一致可删。逐方法 selfcheck
- [x] **Proof** 在 `TestLitemallOrderBizModel` 新增 `testDeleteGrouponExpiredOrder`：submit→pay 到 201 → DAO 直置 orderStatus=204（模拟团购超时已发生）→ 调 `LitemallOrder__deleteOrder`（mutation，IGraphQLEngine）→ 断言 `deleteResult.getStatus()==0`；通过 `daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId)` 直查断言 `deleted==true`、`orderStatus` 保持 204 不变（deleteOrder 是软删，不改 orderStatus）。反向（防白名单误放宽）：PAY=201 删除被拒；SHIP=301 删除被拒

Exit Criteria:

- [x] 204 订单可通过 `deleteOrder` 软删除，测试通过 IGraphQLEngine 验证
- [x] 非白名单状态删除仍被拒（PAY=201 + SHIP=301 反向断言）
- [x] `@BizMutation` 守卫改动通过 selfcheck 无反模式
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed（Round 1 needs-revision → 修订 → Round 2 passed）
- Round 1 Reviewer / Agent: 独立 general subagent（task ses_12c164e02ffeJKrtkrAxGP57gQ）— 裁决 needs-revision，Blocker B1 + Major M1/M2 + 4 minor
- Round 2 Reviewer / Agent: 独立 general subagent（task ses_12c0f1fc2ffeoMNGx3inDYyn7r）— 裁决 passed，可进入实施
- 修订处置：B1 新增 `docs/discussions/2026-06-17-204-deletable-confirmation.md`；M1 反向断言加 PAY=201；M2 断言措辞明确化；minors 全采纳

## Closure Gates

- [x] in-scope behavior is complete（售后文档对齐 + 204 可删 + 测试）
- [x] relevant docs are aligned（order-and-cart / marketing-and-promotions / flow-overview 三文档口径一致且与代码一致）
- [x] verification has run：`./mvnw.cmd test -pl app-mall-service -Dtest=TestLitemallOrderBizModel` → 8/8 通过；全量 `./mvnw.cmd test -pl app-mall-service` → 111/111 通过，零回归
- [x] 已为 204 删除路径新增 IGraphQLEngine 测试，正向（204 可删）+ 反向（201/301 被拒）断言齐全
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation（Round 2 passed）
- [x] each phase has `Required Skill` listed（Phase 1 none 带理由；Phase 2 nop-backend-dev+nop-testing）
- [x] skill loading verification: Phase 2 已加载 nop-backend-dev+nop-testing、读完 selfcheck 文档、selfcheck 19 项通过
- [x] text consistency verified: status=completed / phases=completed / gates checked / log 一致
- [x] closure audit was independent
- [x] closure evidence exists in files

## Deferred But Adjudicated

无。审计报告中 F1/F2 已在前序修复，其余项全部在本计划范围内。

## Closure

Status Note: 四项审计发现全部落地——售后资格文档对齐代码+字典（F-doc-code-1）、迁移命名混淆消除（F-name-1）、团购"开团成功"澄清为计算值（F-doc-code-2）、团购超时(204)订单纳入删除白名单并补测试（F-doc-code-3）。111 测试全过零回归。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure audit subagent — Round 1 task ses_12c02a16effeN9hciVj5kA7GFf（needs-revision，Major M1 跨域引用表漏改 + Minor m1/m3）→ 修订 → Round 2 task ses_12bff75d9ffe0bctuSK6JlpwXX（passed）
- Evidence: M1（`order-and-cart.md:261` 跨域引用表售后资格行）+ m1（`:117` 状态扩展段）已修，与 `:215` 售后流转规则、代码守卫 `LitemallAftersaleBizModel:194-196`、aftersale-type 字典三方一致；grep 全文无"收货完成是售后资格"残留；代码与测试无改动（F-doc-code-3 + testDeleteGrouponExpiredOrder 已 Round 1 验证）

Follow-up:

- 无
