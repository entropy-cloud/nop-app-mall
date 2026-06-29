# segment-directed-marketing 分群定向营销消息推送

> Plan Status: completed
> Last Reviewed: 2026-06-30
> Source: deferred successor — P35 站内信/消息中心（`2026-06-28-0530-1-phase35-message-center-plan.md` Deferred「MARKETING 定向投放」）
> Related: `2026-06-28-0530-1-phase35-message-center-plan.md`, `2026-06-28-1822-2-user-portrait-algorithmization-plan.md`, `2026-06-28-0340-2-phase20-user-operations-workbench-plan.md`
> Audit: required

## Current Baseline

P35 消息中心已交付**事件触发的 ORDER 消息 + 全员 SYSTEM 公告**基座；P20 用户运营工作台 + `user-portrait-algorithmization` successor 已交付**算法化用户分群**能力。但「向某个分群定向推送 MARKETING 营销消息」的投放动作缺失——P35 Deferred「MARKETING 定向投放」触发条件「P20 标签分群能力被运营正式采用时」现已具备（分群解析能力已交付且可运营）。Live-repo 证据：

- `LitemallOrderBizModel.getSegmentMembers(segmentType, segmentValue, page, pageSize)`（`:2145-2207`）：`@BizQuery`，分页返回 `SegmentMemberBean`。支持 `segmentType=rfm|lifecycle`（其他抛 `ERR_USER_PORTRAIT_INVALID_SEGMENT_TYPE`）。RFM 段值 = 中文 8 类（重要价值用户 / 重要保持用户 / 重要发展用户 / 重要挽留用户 / 一般价值用户 / 一般保持用户 / 一般发展用户 / 一般挽留用户，见 `:2031-2044`）；生命周期段值 = 中文 4 类（新客 / 活跃 / 沉睡 / 流失，见 `:2046-2055`）。内部流程：`orderMapper.getUserPaymentSummaryAllTime()` → `computeRfmThresholds` → 逐用户 `classifyRfmSegment`/`classifyLifecycleStage` → 匹配 → 排序 → 分页（pageSize 上限 500）。
- `LitemallUserTagBizModel.findUsersByTag(tag, page, context)`（`:75-86`）：`@BizQuery`，按 `LitemallUserTag.tag` 等值过滤分页（`DEFAULT_PAGE_SIZE=20` 硬编码，无 pageSize 入参）返回打标用户（手工标签分群解析路径，`LitemallUserTag` 实体 + `idx_userTag_userId_tag` 已存在）。整量解析可经 `ILitemallUserTagBiz.findList(eq(tag))` 取全量。
- `LitemallUserMessageBizModel.sendUserMessage(userId, msgType, title, content)`（`:167-183`）：`@BizMutation`，单用户写入一条 `UserMessage`（msgType 可选，缺省 ORDER）。
- `LitemallUserMessageBizModel.broadcastSystemMessage(title, content)`（`:134-163`）：`@BizMutation @Auth(roles="admin")`，遍历活跃 `NopAuthUser`（`daoProvider().daoFor(NopAuthUser.class)` fallback 先例）逐人写一条 SYSTEM 消息，返回送达数。
- `MallNotificationService.sendUserMessage(userId, msgType, title, content)`（`:125-135`）：委托 `userMessageBiz.sendUserMessage`，null-guard + catch+LOG（业务事件触发用）。
- `MSG_TYPE_MARKETING`（=10；ORDER=0/MARKETING=10/SYSTEM=20）字典已就绪；既有调用方：`LitemallCouponUserBizModel:400`（领券）、`LitemallMemberLevelBizModel:297`（等级提升发券）——均为单用户事件触发，无分群批量投放。
- 前端 `app-mall-web/.../LitemallUserTag/segment.page.yaml`：三 Tab 分群查询页（按标签 / 按 RFM 段 / 按生命周期），select 选项即上述中文枚举，分别调 `findUsersByTag` / `getSegmentMembers`。
- 前端 `app-mall-web/.../mall/message-ops/broadcast.page.yaml`：admin 系统公告下发页（title/content → `broadcastSystemMessage` → 送达数 feedback）。
- owner doc `docs/design/system-configuration.md`（消息中心章节）：描述事件 ORDER + 全员 SYSTEM，MARKETING 定向投放标注为 successor。

**核心缺口：** 无「选定分群 → 向全部分群成员批量推送 MARKETING 站内信」的投放动作；`getSegmentMembers` 分页（≤500）且不支持手工标签，无法直接用于「全量成员投放」。

## Goals

- 运营可在后台选择一个分群（手工标签 / RFM 段 / 生命周期阶段），填写标题与内容后，向该分群的**全部**匹配成员一次性推送一条 MARKETING 站内信，并返回送达人数。
- 分群成员解析复用既有能力：手工标签走 `LitemallUserTag`、RFM/生命周期走算法化分类（与 `getSegmentMembers` / P19 同源逻辑），避免口径分叉。
- 后台新增「分群定向推送」页面，挂在消息中心运营菜单下，与既有「系统公告下发」并列。

## Non-Goals

- 定向推送的外部通道（SMS / Email / 微信模板消息）——属 P35 Follow-up「多通道通知编排 + nop-integration 引入」，本计划仅交付站内信 MARKETING 通道。
- 自动化/定时定向投放（如「每周向沉睡用户推一张券」）——本计划为 admin 手动一次性投放；自动化触发引擎为独立 successor。
- 推送效果回溯（触达/点击/转化统计）——属报表 successor，不在本计划。
- 推送频控/防骚扰/用户退订——基线 admin 手动投放，频控为 successor（触发条件：投放频率上升影响体验时）。
- 分群成员去重跨多次推送——每次投放独立生成消息（与 `broadcastSystemMessage` 同模型，无去重）。

## Task Route

- Type: `implementation-only change`
- Owner Docs: `docs/design/system-configuration.md`（消息中心章节补「MARKETING 分群定向投放」）, `docs/design/marketing-and-promotions.md`（营销触达补定向推送口径）
- Skill Selection Basis: 后端新增 `@BizMutation`（nop-backend-dev + 规则 #15 强制 nop-testing）、前端 admin page.yaml（nop-frontend-dev）、IGraphQLEngine 测试（nop-testing）

## Infrastructure And Config Prereqs

- 无新增基础设施。复用既有 `UserMessage` 实体、`MSG_TYPE_MARKETING` 字典、`getSegmentMembers`/`findUsersByTag` 解析能力。

## Execution Plan

### Phase 1 - 后端分群定向投放 + 成员解析抽取

Status: completed
Targets: `app-mall-service/src/main/java/app/mall/service/entity/LitemallUserMessageBizModel.java`, `app-mall-dao/src/main/java/app/mall/biz/ILitemallUserMessageBiz.java`, `app-mall-service/src/main/java/app/mall/service/entity/LitemallOrderBizModel.java`, `app-mall-dao/src/main/java/app/mall/biz/ILitemallOrderBiz.java`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Decision | Fix`
- Prereqs: P35 消息中心（已完成）、P20+user-portrait-algorithmization 分群（已完成）

- [x] **Skill loading gate:** 扫描 available skills，加载 `nop-backend-dev` 与 `nop-testing`。读完路由表必读文档（CrudBizModel / 错误处理 / 跨实体调用 I*Biz / ask-first anti-patterns）。每写完一个方法 selfcheck。Docs read 项下列出已读路径。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`、`.opencode/skills/nop-testing/SKILL.md`、`nop-entropy/docs-for-ai/02-core-guides/service-layer.md`、`nop-entropy/docs-for-ai/02-core-guides/error-handling.md`、`nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`、`nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`。每方法（`collectRfmLifecycleMatches`/`sendSegmentMessage`）写完即按 19 项 selfcheck（IBiz 先声明→@Override public→@BizMutation/@Auth→@Name→跨实体走 I*Biz→@Inject 非 private→NopException+ErrorCode→无 @Transactional 叠加→newEntity/saveEntity 三参→CoreMetrics）逐项校验通过。
- [x] **Decision D1（分群类型范围）：** 抉择支持全部三类（手工标签 / RFM / 生命周期）。备选：仅 RFM+生命周期（拒绝——手工标签是运营最直观的营销分群，且 `LitemallUserTag` + `findUsersByTag` 已可解析，无模型缺口）。残留风险：手工标签随用户打标变化，投放时刻快照语义（与分群查询一致，可接受）。
- [x] **Decision D2（批量写入模型）：** 抉择「逐成员 `saveEntity` 写入」（与 `broadcastSystemMessage` 同先例），返回送达数；不引入 bulk-insert（基线无平台 bulk helper）。备选：异步队列投放（拒绝——基线 admin 手动投放，同步写入 + 返回送达数即可，大分群性能 successor）。残留风险：超大分群（数万）同步写入耗时——记入 Deferred（触发条件：单分群成员 > 1 万时评估异步化）。
- [x] **Fix 抽取共享成员解析（接口契约 + 零回归）：** 在 `ILitemallOrderBiz` 新增 public 方法声明 `List<SegmentMemberBean> collectRfmLifecycleMatches(String segmentValue, boolean byRfm)`，`LitemallOrderBizModel` `@Override` 实现（**public，非 private**——跨实体经 `ILitemallOrderBiz` 注入调用，rule #6 接口契约）。实现体即现 `getSegmentMembers:2165-2204` 主体（装载全量 `getUserPaymentSummaryAllTime` → 阈值 → 分类 → 匹配 → 排序）。`getSegmentMembers` 改为调用它后分页——既有行为/返回不变（零回归，由既有 `getSegmentMembers` 测试守护）。**注：** RFM/生命周期分群仅含 `lastPayTime != null`（有支付历史）的用户（既有 `:2176-2178` 守卫），owner doc 需说明此限制。
- [x] **Add `@BizMutation @Auth(roles="admin") sendSegmentMessage(segmentType, segmentValue, title, content)`：** 校验 title/content 非空 → 按 `segmentType` 解析全量成员 userId：`tag`→`userTagBiz.findList(eq(tag))` 取 userId 去重；`rfm`/`lifecycle`→`orderBiz.collectRfmLifecycleMatches(segmentValue, "rfm".equals(type))`（经 `ILitemallOrderBiz` 注入）取 userId；非法 segmentType 抛 `ERR_USER_PORTRAIT_INVALID_SEGMENT_TYPE`（既有）→ 逐成员 `newEntity`+`setMsgType(MSG_TYPE_MARKETING)`+save → 返回送达数（空分群返回 0，不抛错）。接口声明同步 `ILitemallUserMessageBiz`（`app-mall-dao/.../app/mall/biz/`）。跨实体走 `ILitemallOrderBiz`/`ILitemallUserTagBiz`（非 daoProvider 绕过；`broadcastSystemMessage` 的 `daoProvider().daoFor(NopAuthUser)` 为既有先例，本计划不新增同类绕过）。
- [x] **Decision D3（空分群语义）：** 抉择「空分群返回 0 不抛错」（投放动作成功但无人送达），不新增 ErrorCode。备选：抛 `ERR_MESSAGE_SEGMENT_EMPTY`——拒绝（空分群为正常运营状态如新标签，非错误）。

Exit Criteria:

- [x] `sendSegmentMessage` 落地且非空壳：tag/rfm/lifecycle 三类均能解析全量成员并逐人写入 MARKETING 消息；空分群返回 0；非法 segmentType/title-content 空 拒绝。
- [x] `collectRfmLifecycleMatches` 抽取后 `getSegmentMembers` 行为零回归（既有 `getSegmentMembers` 测试全绿，分页/排序/口径不变）。
- [x] **API 测试：** `sendSegmentMessage` 通过 `IGraphQLEngine`（`JunitAutoTestCase` 录制回放）测试，覆盖 tag/rfm/lifecycle 三类投放 + 空分群 + 非法类型 + 送达数断言；`getSegmentMembers` 既有测试零回归一并纳入。
- [x] 跨实体走 `I*Biz`；`@Inject` 非 private；`NopException`+ErrorCode；无 `@Transactional` 叠加；无 `System.currentTimeMillis`/原生 JSON lib。
- [x] `docs/design/system-configuration.md` + `docs/design/marketing-and-promotions.md` 更新分群定向投放口径（含 D1/D2 rationale 与残留风险）。
- [x] `docs/logs/2026/06-30.md` 更新。

### Phase 2 - 前端分群定向推送页

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/mall/message-ops/segment-push.page.yaml`, `app-mall-web/src/main/resources/_vfs/app/mall/auth/app-mall.action-auth.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 1

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完路由表必读文档（AMIS page.yaml / 业务动作按钮 / `@mutation` 调用约定 / 菜单 action-auth 注册）。文件完成后 selfcheck。
  - Docs read: `.opencode/skills/nop-frontend-dev/SKILL.md`、`nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`、既有 `message-ops/broadcast.page.yaml`（@mutation 调用 + feedback 先例）、`LitemallUserTag/segment.page.yaml`（三类 segmentValue 选项先例）、`app-mall.action-auth.xml`（SUBM 菜单注册先例）。selfcheck：page.yaml 为新增运行时页（非 `_gen` 手编）、`@mutation:LitemallUserMessage__sendSegmentMessage` 与 Phase 1 方法签名一致、`visibleOn` 按 segmentType 切换、菜单挂载于 `mall-manage` 与「系统公告下发」并列。
- [x] **Add 分群定向推送页：** 新增 `mall/message-ops/segment-push.page.yaml`，结构对齐 `broadcast.page.yaml` + `segment.page.yaml`：segmentType select（标签 / RFM / 生命周期）→ segmentValue 动态控件（标签：input-text 标签码；RFM：select 8 中文段值；生命周期：select 4 中文段值，控件 `visibleOn` 按 segmentType 切换）→ title/content → 「推送」按钮调 `@mutation:LitemallUserMessage__sendSegmentMessage` → feedback 送达人数。
- [x] **Add 菜单注册：** `app-mall.action-auth.xml` 在消息中心运营菜单下挂「分群定向推送」子菜单（与「系统公告下发」并列）。

Exit Criteria:

- [x] 推送页运行时可达，三类 segmentType 切换 segmentValue 控件正确，真实调用 `sendSegmentMessage` 并展示送达数。
- [x] 菜单真实挂载可导航；page.yaml 符合 AMIS 约定，无 `_gen` 手编；编译通过。

### Phase 3 - 验证、owner doc 同步与回归

Status: completed
Targets: `docs/design/system-configuration.md`, `docs/design/marketing-and-promotions.md`, `docs/logs/2026/06-30.md`, `docs/testing/known-good-baselines.md`
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1, Phase 2

- [x] **Skill loading gate:** 加载 `nop-testing`，读完路由表必读文档（`JunitAutoTestCase` / `IGraphQLEngine` / request.json5 / RECORDING→CHECKING）。
  - Docs read: `.opencode/skills/nop-testing/SKILL.md`、`nop-entropy/docs-for-ai/02-core-guides/testing.md`、既有 `TestLitemallUserMessageBizModel`（`JunitBaseTestCase` + `@NopTestConfig(localDb=true, initDatabaseSchema=TRUE)` + IGraphQLEngine rpc helper + actAs/signUpUser/myList 先例）。
- [x] **Proof：** `./mvnw test -pl app-mall-service -am` 全绿（记录测试数，重点 `getSegmentMembers` 零回归 + `sendSegmentMessage` 新增用例）；`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` BUILD SUCCESS；`./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS。`known-good-baselines.md` 追加本 successor 行。
- [x] **Proof：** owner doc 两篇更新（定向投放流程 / 三类分群口径 / D1-D3 残留风险 / RFM 与生命周期分群仅含 有支付历史用户 的限制），log 条目含验证命令与结果。

Exit Criteria:

- [x] 全量 service 测试 + web 编译 + uber-jar install 全绿，数字记录于 log 与 baseline。
- [x] owner doc 与 log 与实现一致。

## Plan Audit

- Status: passed（Round 2 + Round 3 连续两轮 clean = 共识，guide rule #12）
- Auditor / Agent: 独立 subagent 链——Round 1 `ses_0f6e2acf4ffeFKq5zYPISQJLI2`（BLOCKER：跨实体 private/接口矛盾 + 错误模块路径 + action-auth 目录 + findUsersByTag 签名 + MSG_TYPE 值 + @Auth 简写）→ 修订；Round 2 `ses_0ebacf4d2ffeynMBQZ7MCwuAxu`（CLEAN，首 clean）；Round 3 `ses_0eba28bfeffefO6VxAk6IZkcDU`（CLEAN，次 clean = 共识）
- Evidence: Round 3 实读 live repo 复核全部 baseline 行号精确命中——`getSegmentMembers :2145`（rfm/lifecycle only + pageSize cap 500 + lastPayTime 守卫）、`findUsersByTag(tag,page,context)`+`DEFAULT_PAGE_SIZE=20`、`MSG_TYPE_MARKETING=10`、`@Auth(roles="admin")`、`collectRfmLifecycleMatches` 为 `ILitemallOrderBiz` 新增 public 方法（`@Override` public，非 private，rule #6）、Phase 1 目标 `app-mall-dao/.../app/mall/biz/ILitemall{UserMessage,Order}Biz.java`、Phase 2 目标 `app-mall-web/.../_vfs/app/mall/auth/app-mall.action-auth.xml` 均存在；既有 `getSegmentMembers` IGraphQLEngine 测试守护零回归；无 `model/*.orm.xml` 改动。无 blocker/major。

## Closure Gates

- [x] in-scope behavior is complete（tag/rfm/lifecycle 三类定向投放 + 空分群 + 非法类型 + 送达数）
- [x] relevant docs are aligned（system-configuration.md / marketing-and-promotions.md）
- [x] verification has run（`mvn test -pl app-mall-service -am` / uber-jar install / web compile）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（sendSegmentMessage）；`collectRfmLifecycleMatches` 抽取由既有 `getSegmentMembers` 测试守护零回归
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（Phase 1 nop-backend-dev+nop-testing / Phase 2 nop-frontend-dev / Phase 3 nop-testing）
- [x] skill loading verification: 各 phase 已扫描+加载匹配 skill+读完路由表必读文档（路径列入 skill loading gate）+ 每方法/文件 selfcheck
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 超大分群同步投放耗时

- Classification: `optimization candidate`
- Why Not Blocking Closure: Decision D2 抉择同步逐成员写入（与 `broadcastSystemMessage` 同先例）；基线 admin 手动投放分群规模有限。
- Successor Required: `yes`（触发条件：单分群成员 > 1 万时，评估异步队列投放 + 进度查询）

### 定向推送多通道（SMS / Email / 微信模板消息）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划仅交付站内信 MARKETING 通道；外部通道依赖 P35 Follow-up「nop-integration 引入」+ 通道编排。
- Successor Required: `yes`（触发条件：SMS/Email/微信模板消息统一模板与重试需求出现时，引入 nop-integration 并扩展投放通道选择）

### 自动化/定时定向投放

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划为 admin 手动一次性投放；自动化触发引擎（如「沉睡用户自动发券」）为独立 successor，可与 P20「自动化会员专属券/生日礼包发放」successor 合并评估。
- Successor Required: `yes`（触发条件：运营要求按规则自动定期定向投放时）

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 三 Phase 全部 completed 且全绿验证通过（service 542 测试 + web 编译 + uber-jar install）；in-scope 行为（tag/rfm/lifecycle 三类定向投放 + 空分群返回 0[D3] + 非法 segmentType/空 title 拒绝）落地于 `LitemallUserMessageBizModel.sendSegmentMessage:187-249`（非空壳——真实校验+成员解析+逐人写入+返回送达数）；`collectRfmLifecycleMatches` 抽取零回归（既有 `getSegmentMembers` 测试守护）；前端推送页运行时可达且真实调用 `@mutation:LitemallUserMessage__sendSegmentMessage`；owner doc 两篇 + log + known-good-baseline 同步。Deferred But Adjudicated 三项均为真正的 successor（超大分群异步化 / 多通道 / 自动化），无确认缺陷藏匿。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure auditor（不同 session/context，非实现 agent）。Live-repo 复核全部命中：
  - 后端实现非空壳：`LitemallUserMessageBizModel.java:187-249` `sendSegmentMessage`（`@BizMutation @Auth(roles="admin")`，title/content 非空校验 → ERR_MESSAGE_SEGMENT_TITLE_OR_CONTENT_EMPTY；tag→`userTagBiz.findList(eq(tag))` 去重 / rfm-lifecycle→`orderBiz.collectRfmLifecycleMatches` 去重 → 逐成员 `newEntity`+`setMsgType(MSG_TYPE_MARKETING)`+`saveEntity` 三参 → 空分群返回 0 → 非法 type 抛 ERR_USER_PORTRAIT_INVALID_SEGMENT_TYPE）。`LitemallOrderBizModel.java:2329-2373` `collectRfmLifecycleMatches`（`@Override public`，装载全量→阈值→分类→过滤→排序，`getSegmentMembers:2318` 改为调用它后分页=零回归）。
  - 接口契约：`ILitemallUserMessageBiz.java:58`、`ILitemallOrderBiz.java:316` 双侧声明齐全；`@Inject ILitemallOrderBiz`/`ILitemallUserTagBiz` 非 private。
  - IGraphQLEngine 测试：`TestLitemallUserMessageBizModel.java:290-418` 6 例（tag 送达数=1 断言 / rfm 经 getUserPortrait 取实际段值送达≥1 / lifecycle 同 / 空分群返回 0 / 非法 segmentType 拒绝 / 空 title 拒绝），全部 `rpc(mutation, "LitemallUserMessage__sendSegmentMessage")`。
  - 前端运行时可达：`segment-push.page.yaml`（103 行，segmentType select + visibleOn 切换 tagValue/rfmValue/lifecycleValue + title/content + `@mutation:LitemallUserMessage__sendSegmentMessage` + feedback 送达数）；`app-mall.action-auth.xml:80` `mall-message-segment-push` orderNo=211 SUBM 挂于 mall-manage（与系统公告下发并列）。
  - 验证数字（实读 log/baseline）：`./mvnw test -pl app-mall-service -am` Tests run: 542, Failures: 0, Errors: 0；uber-jar install BUILD SUCCESS；web compile BUILD SUCCESS；全工作区 `./mvnw test` 550（542 service + 8 web）零下游破坏。
  - 文档同步：`docs/design/system-configuration.md:157,187,196`（MARKETING 行 + 分群定向推送能力行 + 口径）；`docs/design/marketing-and-promotions.md:965,968`（分群定向营销推送章节含 D1-D3）；`docs/logs/2026/06-30.md:5-18`；`docs/testing/known-good-baselines.md:13`。
  - 反空壳 / 五点一致性 / 延迟诚实 / 文档同步 / 规则 #13 非降级 全部通过。Verdict: APPROVED。

Follow-up:

- 超大分群（成员 > 1 万）同步投放耗时 → 评估异步队列投放 + 进度查询（已在 Deferred But Adjudicated 登记，Classification `optimization candidate`，非 confirmed defect）。
