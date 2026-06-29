# 剩余业务事件站内信通知（拼团成功 + 评价奖励）

> Plan Status: completed
> Last Reviewed: 2026-06-30
> Source: `docs/plans/2026-06-29-1921-3-deferred-notification-triggers-plan.md` → `Deferred But Adjudicated → 其他业务事件站内信补全（拼团成功 / 评价奖励）`（Classification: `out-of-scope improvement`，Successor Required: `partial`，触发条件「各特性要求事件→站内信接线时」）；P25 拼团 done、P33 评价 done、P35 站内信/消息中心 done → 触发条件已满足，successor 到期。
> Related: `docs/plans/2026-06-29-1921-3-deferred-notification-triggers-plan.md`、`docs/plans/2026-06-29-2330-2-coupon-expiry-notification-plan.md`（同模式先例）、`docs/plans/2026-06-30-0044-2-segment-directed-marketing-push-plan.md`
> Audit: required

## Current Baseline

站内信/消息中心（P35）事件通知已覆盖：支付/发货/退款/自提核销/自提超时/拼团失败/团购失败（ORDER）、积分过期预警/优惠券过期预警/分群定向推送（SYSTEM/MARKETING）。所有事件经 `MallNotificationService`（`app-mall-service/.../notification/MallNotificationService.java`）统一投递：

- `sendUserMessage(userId, msgType, title, content)`（:125-137）— null-guard `userId` 空 skip（:126），委托 `userMessageBiz.sendUserMessage` 5 参版（:133），失败 log+吞（:134-136，站内信为副作用通道不回滚主事务）。
- `isEventMessageEnabled(eventKey, context)`（:106-117）— 读 `mall_message_event_enabled_{eventKey}` 配置，缺省/异常视为开（:112-115）；调用方在主事务读开关 → 关闭时把 `userId` 解析为 `null` → afterCommit 内 sendUserMessage 静默 skip（uid-null-skip 模式）。
- msgType 常量（`_AppMallDaoConstants`）：`MSG_TYPE_ORDER=0`（:379）、`MSG_TYPE_MARKETING=10`（:384）、`MSG_TYPE_SYSTEM=20`（:389）。
- `LitemallUserMessage` 实体（`_gen/_LitemallUserMessage.java`）字段：id/userId/msgType/title/content/isRead/readTime/addTime/updateTime/deleted。**无 orderId 字段** — 现有 ORDER 消息把订单标识嵌入 title/content 自由文本（如 `"订单 " + orderSn + " 已支付成功"`）。**无独立幂等键列** — 幂等由应用层 query 实现（`userId+msgType+title+addTime 当日窗口`，模板 `hasTodayExpiryReminder` PointsAccountBizModel:355-365）；状态迁移类事件靠上游 CAS/状态守卫保证单次触发（如 `verifyPickupOrder` CAS :1273）。

两个未接线的事件触发点（均经 explore subagent `ses_0eb5da7e` 复核 live repo 确认为 net-new 行为）：

1. **拼团成功（成团）** — `LitemallPinTuanActivityBizModel.markPinTuanSuccess`（:306-315，private）：仅当 group 当前 `PIN_TUAN_GROUP_STATUS_ACTIVE=0` 时翻转为 `PIN_TUAN_GROUP_STATUS_SUCCESS=10`（状态级幂等，重复调用 no-op）。调用点：`openPinTuan`（:163，`minUserCount<=1` 团长一人成团）、`joinPinTuan`（:216，`totalCount>=minUserCount`）。`notificationService` 已注入（:75-76）。**当前成团无任何通知**（仅失败路径 `refundMemberOrder:398-400` 投递 `pintuan-fail` ORDER 消息）。成团需通知团内**全部成员**（团长 + 已参团成员），需按 groupId 查 `LitemallPinTuanMember` 集合逐人投递。
2. **评价奖励（评价审核通过 / 预审关时即时发放）** — `LitemallCommentBizModel.batchAuditComments`（:366-435，`@BizMutation @Auth(admin)`）approve 分支（:403-422）：PENDING→APPROVED + `earnPoints(userId, reward, ..., SOURCE_TYPE_COMMENT_REWARD="comment-reward", sourceId=comment.id, ...)`（:410-413），重复发放 catch `ERR_POINTS_DUPLICATE_EARN` 视为成功（:416-421，幂等）。`submitComment`（:165-175）预审关时即时 `earnPoints` 同 sourceType。`MallNotificationService` **未注入** LitemallCommentBizModel（现有注入：orderGoodsBiz/orderGoodsMapper/pointsAccountBiz/systemBiz :58-68）。

**缺口：** 拼团成团、评价奖励（积分发放）两个用户可感知的关键时刻无站内信通知，是 P35 事件覆盖的剩余 successor（同批次「优惠券即将过期」已由 `2026-06-29-2330-2` 闭环）。

## Goals

- 拼团成团（group ACTIVE→SUCCESS）时，向团内全部成员投递一条 ORDER 站内信（含活动/商品标识）。
- 评价获得积分发放时（预审开→approve 路径；预审关→submit 即时路径），向评价用户投递一条 SYSTEM 站内信（含积分数量；reward=0 不投递）。
- 两事件均经既有 `mall_message_event_enabled_{key}` 开关 + uid-null-skip 模式控制，afterCommit 投递不回滚主事务。
- 幂等靠上游状态守卫（拼团 ACTIVE→SUCCESS；评价 PENDING→APPROVED / 提交守卫）——与既有状态迁移类事件一致，**非**聚合 reminder 语义；评价奖励为非聚合事件，同用户同日不同评价各获奖励应各收消息。

## Non-Goals

- SMS/Email 多渠道（依赖 `nop-integration` 引入，基建 successor，触发条件未满足）。
- 其他业务事件站内信（如团购成团、收藏降价、商品上架提醒等）— 各特性独立 successor，非本结果面。
- `LitemallUserMessage` 加 `orderId`/幂等键列（ORM model-gap，需 ask-first 授权；本计划沿用既有 title/content 嵌入标识 + 应用层 query 幂等模式）。
- 前端新增通知页面或消息样式（复用 P35 既有消息中心列表/未读徽章，仅后端投递新消息行）。
- PinTuan 成团的其他运营通知（如开团提醒、参团提醒）。

## Task Route

- Type: `implementation-only change`
- Owner Docs: `docs/design/system-configuration.md`（消息中心/事件通知章节）、`docs/design/marketing-and-promotions.md`（拼团 + 评价章节）
- Skill Selection Basis: 后端 BizModel afterCommit 钩子 + `@BizMutation` 扩展 + IGraphQLEngine 测试 → `nop-backend-dev` + `nop-testing`（mandatory skill loading）；无前端 view.xml 改动 → 不加载 `nop-frontend-dev`。

## Infrastructure And Config Prereqs

- 无新基建。复用已引入的 `MallNotificationService` + `nop-sys NopSysVariable`（事件开关存储）。
- 新增两条事件开关配置（缺省视为开，与既有事件一致）：`mall_message_event_enabled_pintuan-success`、`mall_message_event_enabled_comment-reward`。

## Execution Plan

### Phase 1 — 拼团成团通知 + 评价奖励通知后端实现

Status: completed
Targets: `app-mall-service/src/main/java/app/mall/service/entity/LitemallPinTuanActivityBizModel.java`、`app-mall-service/src/main/java/app/mall/service/entity/LitemallCommentBizModel.java`（新增注入 `MallNotificationService`）。复用既有 `MallNotificationService.sendUserMessage`（4 参）直接投递，不新增便捷封装；复用已注入的 `pinTuanMemberBiz`（`LitemallPinTuanActivityBizModel.java:58`，`ICrudBiz` 已提供 `findList`）查成员集，不改 `ILitemallPinTuanMemberBiz` 接口。
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: P25 拼团 done、P33 评价 done、P35 消息中心 done（均已满足）

- [x] **Skill loading Gate:** 扫描 available skills，加载 `nop-backend-dev` + `nop-testing`。读完 `nop-backend-dev` SKILL routing table 标为「必读」的所有文档（含 `nop-entropy/docs-for-ai/` 相关 runbook + `error-handling.md` + `ai-defaults.md` 反模式表）+ `nop-testing` SKILL routing table 必读文档（IGraphQLEngine 录制回放、JunitAutoTestCase）。逐篇列出已读路径。每写完一个方法用 `nop-backend-dev` selfcheck 校验无反模式（@Inject 非 private、NopException+ErrorCode、@BizMutation 不叠 @Transactional、CoreMetrics、IBiz 接口先于 impl、跨实体注入 I*Biz）。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`、`.opencode/skills/nop-testing/SKILL.md`、`nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`（19 项自检清单）、`nop-entropy/docs-for-ai/02-core-guides/error-handling.md`（两档策略 + 异常传播边界）；并复核 live-repo 先例 `LitemallOrderBizModel.verifyPickupOrder`（:1289-1297 pickup_verify afterCommit 模式）、`LitemallPinTuanActivityBizModel.refundMemberOrder`（:396-400 主事务捕获先例）、`MallNotificationService`（sendUserMessage/isEventMessageEnabled）。本计划仅扩展既有 private 方法 + 既有 @BizMutation（无新增 public GraphQL 方法），故 selfcheck 第 1-7 项（接口/注解/DTO）不触发新接口改动；第 8-20 项：newEntity/IBiz 注入/ErrorCode/CoreMetrics/JsonTool 均合规。
- [x] **Decision D1：拼团成团通知投递范围与时机。** 抉择（方案 C）：在 `markPinTuanSuccess`（:306-315）状态翻转成功后，于**主事务内**读开关解析 `notify` 标志（开关关 → 跳过），并在主事务内按 groupId 经已注入的 `pinTuanMemberBiz`（:58）查全部成员、组装每人的 `(uid, title, content)` 载荷列表；afterCommit 内**仅遍历载荷列表**逐人 `sendUserMessage(uid, ...)`，afterCommit 内**不做任何 DB 查询**。备选 A（在 `openPinTuan`/`joinPinTuan` 两调用点分别投递）— 否决理由：两调用点重复逻辑 + `markPinTuanSuccess` 已是唯一状态翻转汇聚点，集中投递消除重复。备选 B（同步投递不 afterCommit）— 否决理由：站内信副作用不应回滚主事务，须 afterCommit（与 `pintuan-fail` 先例 :398-400 对称）。备选 D（原草案「afterCommit 内查成员」）— 否决理由：与同文件先例不符（`refundMemberOrder:396-400` 在主事务捕获 orderSn/mobile/refundUserId，afterCommit 仅 send；`verifyPickupOrder:1292-1297` 同），afterCommit 内查 DB 需新 session 且存在成员集在 commit/afterCommit 间变更的最终一致风险。方案 C 与既有先例完全对齐，消除该残留风险。残留风险：无（成员集在主事务捕获时刻即成团快照，afterCommit 仅消费已捕获列表）。记录抉择 + 备选 A/B/D + 理由于本 item + owner doc。**实施补充：** 落地时发现 joinPinTuan 同事务 `saveEntity` 的成员对后续 `findList` 不可见（未 flush），在查成员前加 `dao().flushSession()` 保证本次 join 成员落库可见——与方案 C「主事务内查成员」语义一致（flush 仍在主事务内）。
- [x] **Decision D2：拼团成团通知幂等策略。** 抉择：依赖上游 `markPinTuanSuccess` 的 ACTIVE→SUCCESS 状态守卫（状态级单次翻转，重复调用 no-op）保证成团只触发一次投递，**不**额外加当日 query 守卫（与 `pickup_verify`/`payment`/`ship`/`pintuan-fail` 等状态迁移类事件先例一致，区别于 reminder 聚合类）。备选（加当日 query 守卫）— 否决理由：状态守卫已是充分幂等，额外 query 为冗余。残留风险：无（ACTIVE→SUCCESS 状态翻转在 `markPinTuanSuccess` 内不可重复，:310-311 守卫）。title 采用固定文案（如「拼团成功」），活动/商品/团标识嵌入 content 自由文本（与既有 ORDER 消息先例一致，无 orderId 字段）。
- [x] **Decision D3：评价奖励通知触发边界（reward=0 是否投递）。** 抉择：**reward>0 才投递**（评价奖励通知的业务语义是「你获得了 X 积分」，reward=0 无奖励可通知，投递空奖励消息会误导用户）。备选（reward=0 也投递「评价已通过」）— 否决理由：评价通过本身的状态可见于用户评价列表，站内信价值在积分到账；且 `submitComment` 预审关路径 reward 恒 0 时不触发。在 approve/submit 两路径 `earnPoints` 调用点之后、仅当 `reward>0` 时投递。
- [x] **Decision D4：评价奖励通知幂等策略。** 抉择（方案 B）：**不做消息层 query 幂等**，依赖上游状态守卫——approve 路径靠 `batchAuditComments` 的 PENDING→APPROVED 守卫（`:398-401` 仅处理 PENDING，已 APPROVED 跳过），submit 预审关路径靠评价提交本身的「一订单商品一次 + 时间窗口」守卫。这与状态迁移类事件（`payment`/`ship`/`pickup_verify`/`pintuan-fail` 靠 CAS/状态守卫）一致；评价奖励本质是「PENDING→APPROVED 发放积分」状态迁移事件，非 reminder 聚合事件。**关键区分**：评价奖励**非聚合**——同一用户同日两条不同评价各获奖励应各收 1 条消息（共 2 条），故 `hasTodayExpiryReminder` 聚合模板（按 userId+title+当日 1 条）**不可复用**（会错误抑制同日不同评价的合法第二条）。备选 A（title 嵌 commentId 做 per-comment query 幂等）— 否决理由：变长 title 损害消息列表 UX 且状态守卫已充分；commentId 仅入 content 自由文本（query 不可用）。积分发放的 `(sourceType=comment-reward, sourceId=commentId)` 幂等（ERR_POINTS_DUPLICATE_EARN catch）独立于通知幂等，保证不会双发积分。残留风险：跨事务并发审核竞态（两个并发 `batchAuditComments` 均读到 PENDING 后各提交）极罕见，最坏仅重复通知一条（绝不重复发放积分，积分层有独立幂等），按既有状态迁移事件同类别接受为残留。记录抉择 + 备选 + 理由 + 残留。
- [x] **Add：拼团成团通知。** `markPinTuanSuccess` 翻转 SUCCESS 后：主事务内 `boolean notify = notificationService.isEventMessageEnabled(EVENT_KEY_PINTUAN_SUCCESS, context)`；`if(notify)` 在主事务内经已注入的 `pinTuanMemberBiz`（:58）按 groupId `findList` 全部成员，组装 `List<MemberNotify{uid,content}>` 载荷（title 固定「拼团成功」，content 嵌活动名/商品名/团标识，无 orderId 字段沿用自由文本先例）；`txn().afterCommit(null, () -> 载荷列表.forEach(n -> notificationService.sendUserMessage(n.uid, MSG_TYPE_ORDER, 固定title, n.content)))`。afterCommit 内零 DB 查询（方案 C，与 `refundMemberOrder:396-400` 先例对齐）。
- [x] **Add：评价奖励通知（approve 路径）。** `batchAuditComments` approve 分支 `earnPoints` 之后：`reward>0` 时（D3），主事务内读 `isEventMessageEnabled(EVENT_KEY_COMMENT_REWARD, context)` 解析 `notifyUid`（关 → null）；afterCommit 内 `sendUserMessage(notifyUid, MSG_TYPE_SYSTEM, 固定 title「评价奖励到账」, content 含 reward 积分数 + commentId)`。注入 `MallNotificationService` 到 LitemallCommentBizModel（@Inject 非 private）。幂等靠 PENDING→APPROVED 状态守卫（D4 方案 B），不加 query 守卫。
- [x] **Add：评价奖励通知（submit 预审关即时路径）。** `submitComment`（:165-175）预审关即时 `earnPoints` 后，同 approve 路径模式投递（reward>0 + 开关 + afterCommit）。与 approve 路径共用固定 title「评价奖励到账」+ content 模板。幂等靠评价提交守卫（D4 方案 B）。
- [x] **Add：事件开关常量。** 在相关 BizModel 加 `static final String EVENT_KEY_PINTUAN_SUCCESS = "pintuan-success"`、`EVENT_KEY_COMMENT_REWARD = "comment-reward"`（参照 PointsAccountBizModel:80 / CouponUserBizModel:59 先例，统一抽常量）。
- [x] **Proof：** `@BizMutation`/`@BizQuery`（本计划扩展的是既有 `@BizMutation` batchAuditComments/submitComment + private 方法 markPinTuanSuccess；无新增 public GraphQL 方法）→ 成团通知经既有 `openPinTuan`/`joinPinTuan` mutation 间接触发，评价通知经既有 `batchAuditComments`/`submitComment` mutation 触发，均通过 `IGraphQLEngine`（`JunitBaseTestCase` 录制回放）验证消息行落库。新增 IGraphQLEngine 测试：拼团成团（minUserCount=2，join 第 2 人成团 → 两人各收 1 条 ORDER）、拼团未成团不通知（join 第 1 人仍 ACTIVE → 0 条）、拼团开关关闭 uid-null-skip（0 条）、评价 approve reward>0（1 条 SYSTEM）、评价 approve reward=0（0 条）、评价 reject（0 条）、评价 submit 预审关 reward>0（1 条）、**同用户同日两条不同评价 approve 各得奖励 → 各收 1 条共 2 条（锁定 D4 非聚合语义）**。验证命令：`./mvnw test -pl app-mall-service -am`。

Exit Criteria:

- [x] 拼团成团（ACTIVE→SUCCESS）向全部成员各投递 1 条 ORDER 站内信；未成团 / 开关关闭 / 状态非 ACTIVE 不投递
- [x] 评价 reward>0 发放（approve + submit 预审关两路径）各投递 1 条 SYSTEM 站内信；reward=0 / reject / 开关关闭不投递
- [x] 评价奖励非聚合语义：同用户同日两条不同评价 approve 各收 1 条消息；幂等靠 PENDING→APPROVED 状态守卫（重复 approve 同评价不双发，积分层独立幂等）
- [x] **API 测试：** 所有触发路径经 `IGraphQLEngine`（`JunitBaseTestCase` RPC 断言）验证消息落库；无新增 public `@BizMutation`/`@BizQuery`（扩展既有方法），故无需新 GraphQL schema 测试，但既有 mutation 的消息副作用必须经 RPC 断言
- [x] owner docs 更新（`system-configuration.md` 事件通知清单 + `marketing-and-promotions.md` 拼团/评价通知语义）
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗性审计，三轮（fresh sessions，非起草 agent）
- Evidence:
  - **Round 1**（`ses_0eb5a1468ffe1qDquX5M3PzDEI`）：Verdict `MAJOR_OBJECTION`（无 blocker）。基线/受保护区/API 测试/skill 全清；2 major：M1（D1 提议 afterCommit 内查成员，与同文件 `refundMemberOrder:396-400` / `verifyPickupOrder:1292-1297` 主事务捕获先例不符）+ M2（D4 复用聚合模板 `hasTodayExpiryReminder` 会错误抑制同日不同评价的合法第二条消息，评价奖励非聚合）；3 minor（Targets 含「如需」违禁词 / D1「或既有注入」含糊 / Proof 缺 distinct-comments 用例）。已据全部发现修订 D1（方案 C 主事务捕获 + afterCommit 零查询）/ D2 / D4（方案 B 状态守卫 + 非聚合语义 + 并发竞态残留）/ Targets / Proof。
  - **Round 2**（`ses_0eb5393b1ffeSEb4YqaIsEEaD7`）：Verdict `PASS`。M1/M2/m1/m2/m3 全部 RESOLVED（live-repo 证据复核：`pinTuanMemberBiz:57-58` 注入、`findList:327` 先例、`markPinTuanSuccess:306-315` ACTIVE→SUCCESS、`refundMemberOrder:396-400` 主事务捕获先例、CommentBizModel 未注入 MallNotificationService、PENDING 守卫:397-402）。无新问题。anti-slacking grep 零命中、文本一致、API 测试合规、ORM-independent、skill 齐。
  - **Round 3 确认轮**（`ses_0eb51fa7fffek4dqbdAB4FD95H`）：Verdict `PASS`。针对 D1/D4 两个关键设计 claim 再次独立复核 live repo（markPinTuanSuccess 状态守卫 / pinTuanMemberBiz.findList / refundMemberOrder 先例 / CommentBizModel 未注入 / batchAuditComments PENDING 守卫 / submitComment 即时路径 / 非聚合语义一致 / 无遗漏双发路径）；P25/P33/P35 done 确认 successor 触发条件已满足；Non-Goals 未非法强推未满足触发条件的 deferred。
  - **共识判定：** 最新实质修订后连续两轮 clean（Round 2 + Round 3），满足 plan-authoring guide consensus rule。Plan 可置 `active`。

## Closure Gates

- [x] in-scope behavior is complete（拼团成团通知 + 评价奖励通知两路径 + 开关 + 幂等）
- [x] relevant docs are aligned（system-configuration.md 事件清单 + marketing-and-promotions.md）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 全绿，551 tests 0 failures）
- [x] all new 通知触发路径经 `IGraphQLEngine` 验证消息落库（非纯实体级断言）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（Phase 1 nop-backend-dev + nop-testing），Nop-platform phase 不写 `none`
- [x] skill loading verification: 扫描 + 加载 `nop-backend-dev`+`nop-testing` + 读完 routing table 必读文档（路径列入 skill loading gate）+ 每方法 selfcheck 无反模式
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

> 起草时预期无延期项。SMS/Email 多渠道（依赖 nop-integration 引入）、其他业务事件站内信（各特性独立 successor）在 Non-Goals 显式移出。`LitemallUserMessage.orderId`/幂等键列（ORM model-gap，需 ask-first）在 Non-Goals 移出，沿用既有自由文本 + 应用层 query 幂等模式。

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: completed — 拼团成功站内信（ORDER，团内全部成员）+ 评价奖励站内信（SYSTEM，reward>0 两路径）均已落地、测试全绿、文档同步。Closure audit PASS，无 blocker / 无 open finding。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure auditor（adversarial session `ses_0eb3d6f43ffeupynDURdmh6Ncj`，非实现 agent）
- Evidence:
  - **实现复核（live repo）：**
    - `LitemallPinTuanActivityBizModel.java:316-377` — `markPinTuanSuccess` 翻转后读开关（`isEventMessageEnabled` :331）+ `dao().flushSession()`（:336，保证本次 join 成员可见）+ `collectGroupMemberUserIds`（:350）+ `buildPinTuanSuccessContent`（:365，`ILitemallGoodsBiz goodsBiz` 注入 :78-79 解析商品名）+ `txn().afterCommit`（:342-347）零 DB 查询逐人 `sendUserMessage(ORDER,"拼团成功")`。幂等靠 ACTIVE→SUCCESS 守卫（:320-324）。常量 `EVENT_KEY_PINTUAN_SUCCESS`/`PINTUAN_SUCCESS_TITLE`（:85-86）。
    - `LitemallCommentBizModel.java:585-594` — `notifyCommentReward`（开关→notifyUid-null，afterCommit `sendUserMessage(SYSTEM,"评价奖励到账",含 reward+commentId)`）。两路径接线：submit 预审关（:184，`if reward>0`）+ `batchAuditComments` approve（:435，earnPoints try-catch 后）。reward=0/reject 不投递。常量（:73-74），`notificationService` 注入（:76-77，非 private）。幂等靠 PENDING→APPROVED 守卫（:409-413），非聚合。
  - **测试复核（9 用例，经 IGraphQLEngine RPC 断言 `LitemallUserMessage` 行数）：** `TestLitemallPinTuanActivityBizModel` ×3（全员各收 1 / 未成团 0 / 开关关 0）；`TestLitemallCommentBizModel` ×2（submit reward>0 收 1 / reward=0 收 0）；`TestLitemallCommentOpsWorkbench` ×4（approve reward>0 收 1 / reward=0 收 0 / reject 收 0 / 同用户两条不同评价收 +2 锁定 D4 非聚合）。重跑 `mvn -pl app-mall-service test -Dtest=TestLitemallPinTuanActivityBizModel,TestLitemallCommentOpsWorkbench,TestLitemallCommentBizModel` → Tests run: 58, Failures: 0, Errors: 0 — BUILD SUCCESS。
  - **文档复核：** `system-configuration.md:169/171/176`（事件清单 +2 行 + 分类 rationale）、`marketing-and-promotions.md:859/201`（拼团成团通知 + 评价奖励通知语义）、`docs/logs/2026/06-30.md:5-11`（dated 日志 + 全绿验证记录 551 tests）。
  - **反模式 grep：** 两 BizModel 无 `System.currentTimeMillis` / 第三方 JSON / `@Transactional` / `private @Inject`（零命中）。
  - **11 项 Closure Gates 全部 met**（独立审计 gate 表，无 open finding）。

Follow-up:

- 无（无 confirmed defect，无 deferred 残留）。
