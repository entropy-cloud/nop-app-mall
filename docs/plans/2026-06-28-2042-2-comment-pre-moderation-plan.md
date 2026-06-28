# phase36b 评论前置审核状态机（auditStatus pending→approved/rejected + 预审开关 + 管理员审核工作台）

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Mission: mall
> Work Item: P36 Deferred successor — 评论前置审核状态机（Decision G4 model-gap）
> Source: `docs/backlog/enhanced-features-roadmap.md` §36；`2026-06-28-1027-3-phase36-goods-operations-enhancement-plan.md` → `Deferred But Adjudicated → 评论前置审核状态机（G4）`（model-gap）
> Related: `2026-06-27-2321-2-phase33-structured-comment-plan.md`（P33 结构化评价 origin）、`docs/design/system-configuration.md` 商品运营增强 → 评论审核模型（Decision G4）
> Audit: required

## Current Baseline

> 来源：实读 live repo（HEAD 经 grep/read + 子代理基线扫描核验），非旧计划记忆。

**评论无审核状态字段，发布即公开：**
- `LitemallComment` 实体（`model/app-mall.orm.xml:440-491`）15 列（id…semanticRating[propId=15]）。**无 auditStatus / status 字段**。最后 propId=15。
- `LitemallCommentBizModel.submitComment`（`LitemallCommentBizModel.java:74-168`）发布评论：校验订单已收货/归属/未评过，原子条件更新 `orderGoods.comment` 标志，发积分奖励。**发布即公开可见**，无预审门控。
- `commentList`（`:170-188`，`@Auth(publicAccess=true)`）+ `getCommentSummary`（`:190-234`，public）公共读取，**无 auditStatus 过滤**（当前仅靠 Crud 管道默认 `deleted` 过滤）。

**后置审核已落地（P36），前置审核缺失：**
- `batchModerateComments`（`:301-335`，action=hide/restore，置 `deleted`）+ `batchAdminReply`（`:267-299`）+ `getCommentReviewList`（`:337-371`，管理员工作台列表）均已落地（P36 shipped）。
- **无 auditStatus / preModerat / moderationStatus / pendingModerat**（grep 全仓零匹配）。当前仅后置 Moderation（下架/恢复），无前置预审状态机。
- `system-configuration.md:541-544`（Decision G4）明确："前置审核状态机（pending→approved/rejected）需新 `status` 字段，为 model-gap successor（见计划 Deferred）。"

**系统配置模式（feature toggle）：**
- 本项目**不用** `NopSysVariable`，用自定义 `LitemallSystem` 实体（key/value 行）经 `ILitemallSystemBiz.getConfig(keyName, ctx)` 读取（`ILitemallSystemBiz.java:17`，实现 `LitemallSystemBizModel.java:22-29`）。
- 布尔开关先例：`MallNotificationService.java:111` `getConfig("mall_message_event_enabled_" + eventKey, ctx)`（per-event 布尔）；`LitemallCommentBizModel.resolveCommentReward`（`:448-460`）为 string→int 防御解析先例（empty/null=off，解析失败=off）。

**roadmap 状态：** Phase 36 `done`；本计划为 P36 Deferred successor，不在 roadmap Phase 列表内，完成后更新本计划 Deferred 来源引用。

## Goals

- `LitemallComment` 新增 `auditStatus` 字段 + 字典 `mall/comment-audit-status`（PENDING/APPROVED/REJECTED）。
- 预审开关 `mall_comment_pre_moderation`（`LitemallSystem` 配置）：OFF 时评论发布即审核通过（auditStatus=APPROVED/null，公开可见，行为同今）；ON 时新评论 auditStatus=PENDING（公开不可见，待管理员审核）。
- 管理员批量审核 mutation `batchAuditComments(commentIds, action=approve/reject)`：PENDING→APPROVED（公开可见）/REJECTED（公开不可见）。
- 公共 `commentList`/`getCommentSummary` 仅返回审核通过（APPROVED 或 auditStatus 为空）评论；管理员 `getCommentReviewList` 增 `auditStatus` 筛选 + 工作台批量通过/拒绝按钮。
- 向后兼容：历史评论 auditStatus 为空视为已通过（公开可见），无需数据迁移。
- 所有新增 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine` 测试。

## Non-Goals

- 后置审核（hide/restore via deleted）—— P36 已交付，本计划不改动。
- 评论积分奖励 / 结构化评价（pros/cons/semanticRating）—— P27/P33 已交付。
- AI 内容自动审核（图像/文本自动打标）。
- 评价回复（adminReply/batchAdminReply）—— P36 已交付。
- 评论发布资格/窗口期调整 —— 既有 submitComment 守卫不变。

## Task Route

- Type: `implementation-only change`（Decision G4 已在 `system-configuration.md` 界定 model-gap successor；本计划为执行 slice）
- Owner Docs: `docs/design/system-configuration.md`（商品运营增强 → 评论审核模型 Decision G4：补前置审核状态机/开关/向后兼容/公共过滤机制/积分发放时机）。公共可见性口径同步至 `system-configuration.md`（本计划 Phase 4 落地），不在 `marketing-and-promotions.md` 另写。
- Skill Selection Basis: ORM 新增列 + 字典（nop-orm-modeler、nop-database-design）、BizModel 改 submitComment/public 查询过滤 + 新增 @BizMutation（nop-backend-dev）、AMIS 工作台增 Tab/按钮（nop-frontend-dev）、API 测试 IGraphQLEngine（nop-testing）

## Protected Area

本计划 Phase 1 修改 `model/app-mall.orm.xml`（`LitemallComment` 新增 `auditStatus` 可空列 + 新字典 `mall/comment-audit-status`）。按 `docs/context/ai-autonomy-policy.md` Protected Areas 表，XML models 为 **ask-first**。

- 触及文件：`model/app-mall.orm.xml`（1 实体加 1 可空列 + 1 新字典）
- 授权状态：**pending MISSION_DRIVER ORM 授权**（plan 已 active 并通过 4 轮审计；Phase 1 ORM 实施待显式 ORM 授权开工）
- 实施门控：Phase 1（ORM 实施）在获 MISSION_DRIVER 显式 ORM 授权前不得开工。先例：P22(1610)/P26/P27/P28/P32/P33 均按同一 ask-first 模式新增 ORM 列，授权由 MISSION_DRIVER「execute the entire plan」类指令构成。
- 未获授权的降级路径：将 Phase 1 ORM + 依赖它的 Phase 2/3/4 整体移入 `Deferred But Adjudicated`（分类 model-gap，触发条件「获 ORM 授权时」）。本计划无 ORM-independent 切片（auditStatus 字段为前置审核前置依赖），故授权前保持 `draft`/blocked。

## Infrastructure And Config Prereqs

- 无新增基础设施。预审开关经既有 `LitemallSystem` 表配置（key=`mall_comment_pre_moderation`，value=`1`/`0`），由部署/运营在后台系统配置页维护。
- 无新增调度任务。

## Execution Plan

### Phase 1 — 模型准备（Comment auditStatus 字段 + 字典 + 常量）

Status: completed
Targets: `model/app-mall.orm.xml`、`app-mall-meta`、`app-mall-dao`（regen）、`_AppMallDaoConstants.java`
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Add | Decision | Proof`
- Prereqs: 无（Phase 1 受 `## Protected Area` ask-first 门控）

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读完 routing table 必读文档；列出已读路径。模型改动后 selfcheck。
  - Docs read: `.opencode/skills/nop-orm-modeler/SKILL.md`、`.opencode/skills/nop-database-design/SKILL.md`、`model/app-mall.orm.xml`（`LitemallComment` + `mall/aftersale-status` dict 先例）、`app-mall-dao/_AppMallDaoConstants.java`（常量命名先例 `AFTERSALE_STATUS_*`）。
- [x] **Decision: auditStatus 向后兼容语义。** 抉择 A（推荐）：`auditStatus` 可空（nullable）；**null = 已通过（公开可见）**，PENDING/REJECTED = 公开不可见。历史评论（null）无需迁移即公开。预审开关 OFF 时新评论写 auditStatus=null（或 APPROVED）→ 行为同今。备选 B（mandatory + 默认 APPROVED + 迁移历史）被否——需数据迁移且与"null=通过"的宽松语义相比无额外收益。残留风险：公共查询须显式过滤（auditStatus 为空 OR APPROVED），不能仅靠 deleted 管道。抉择/备选/残留风险写入 `system-configuration.md`。
- [x] **Add:** `LitemallComment` 新增可空列 `auditStatus`(int, propId=16, "审核状态", ext:dict="mall/comment-audit-status")。落地 `model/app-mall.orm.xml`。
- [x] **Add:** 新字典 `mall/comment-audit-status`（valueType=int）：PENDING(0, "待审核")/APPROVED(1, "已通过")/REJECTED(2, "已拒绝")。落地 `model/app-mall.orm.xml`。注：列定义为可空（nullable），历史/未开预审的行 auditStatus 为 null（=已通过），DB 不强制 default 0（避免历史行被误置 PENDING）。
- [x] **Add:** 字典 `mall/comment-audit-status` 落地后由 regen 生成对应常量（按 `AFTERSALE_STATUS_*` 先例，常量名随字典 code 派生）；BizModel 引用 regen 产物，不手改 `_AppMallDaoConstants.java`（生成文件）。
- [x] **Proof:** regen + 全模块编译通过；新列/字典出现在生成 DDL 与 `_gen` 代码。验证命令：`./mvnw -pl app-mall-codegen -am generate-test-resources` + `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C`。

Exit Criteria:

- [x] auditStatus 列 + 字典落地，regen + 编译通过
- [x] `docs/logs/` 更新

### Phase 2 — 后端（预审开关 + 发布写 auditStatus + 公共查询过滤 + 批量审核）

Status: completed
Targets: `LitemallCommentBizModel.java`、`ILitemallCommentBiz.java`（声明 batchAuditComments + getCommentReviewList 新 auditStatus 参数）、`AppMallErrors.java`
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Fix | Proof`
- Prereqs: Phase 1（auditStatus 字段 + 字典 + 常量）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完 routing table 必读文档；列出已读路径。每方法写完 selfcheck（NopException/ErrorCode、跨实体 I*Biz、@Inject 非 private）。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`（service-layer / error-handling / safe-api / 反模式表）、`.opencode/skills/nop-testing/SKILL.md`（IGraphQLEngine / JunitBaseTestCase / request 模式）、`LitemallCommentBizModel.java`（既有 batchModerateComments/doFindPageByQueryDirectly 先例）、`LitemallPointsAccountBizModel.java`（earnPoints 幂等 ERR_POINTS_DUPLICATE_EARN 守卫）。
- [x] **Decision: 公共查询过滤机制（xmeta 约束）。** 公共 `commentList`/`getCommentSummary` 须过滤 `(auditStatus 为空 OR APPROVED)`，但默认生成的 int 列 xmeta 不允许 `isNull`/`or` 复合操作符（本仓库 `applyShowTypeFilter` 因 "star xmeta 仅允许 eq/in" 而绕道 `in`，见 `LitemallCommentBizModel.java:391-393`；`getCommentReviewList` 因 `contains` 改用 `doFindPageByQueryDirectly`，见 `:349,370`）。抉择 A（推荐）：公共查询改用 `doFindPageByQueryDirectly`/`doFindListByQueryDirectly`（本文件及 5 个其他 BizModel 已有先例）构造 `OR(isNull(auditStatus), eq(auditStatus,APPROVED))` 过滤。备选 B（扩 xmeta 允许 auditStatus 的 isNull/or）被否——为单字段放宽公共端 xmeta 操作符面更广、审计风险更高。**安全理由（必记）：** `directly` 变体绕过 xmeta 操作符限制但不绕过 `@Auth(publicAccess=true)` 的公开语义；查询仍由 BizModel 显式构造（valueId/type 维度 + auditStatus 过滤），不暴露任意字段筛选能力，与既有 `getCommentReviewList` 同一安全模型。抉择/备选/安全理由写入 `system-configuration.md`。
- [x] **Decision: 预审期积分发放时机。** `submitComment` 现于 `:157-165` 立即发放积分（`sourceType=comment-reward`, `sourceId=comment.id`）。预审 ON 时若仍 submit 即发，用户在审核前已得积分且 REJECTED 无回收路径。抉择 A（推荐）：**预审 ON 时积分延迟至审核通过发放**（`batchAuditComments` action=approve 时发放，复用同一 sourceType/sourceId=comment.id 保幂等）；预审 OFF 时维持 submit 即发（行为同今）。备选 B（submit 即发 + REJECTED 时 clawback）被否——需新增扣减路径与对账复杂度，且 submit 即发与"审核通过才公开"语义不一致。抉择/备选写入 `system-configuration.md`。残留风险：审核通过发放积分使 batchAuditComments 的 approve 分支需承载积分发放编排（经 `I*LitemallPointsAccountBiz`）。
- [x] **Add:** 预审开关读取 `isPreModerationEnabled(ctx)`（注入 `ILitemallSystemBiz`，常量 `CONFIG_COMMENT_PRE_MODERATION="mall_comment_pre_moderation"`，empty/null/"0"/"false"/解析失败=OFF，"1"/"true"=ON，复刻 `resolveCommentReward` 防御解析模式）。
- [x] **Fix:** `submitComment` 写 auditStatus：预审 ON → `COMMENT_AUDIT_PENDING`，且**积分发放延迟**（不在 submit 发，改由 approve 发，见 Decision）；预审 OFF → auditStatus=null（公开可见，行为同今）+ submit 即发积分（不变）。其余发布守卫不变。
- [x] **Fix:** 公共查询 `commentList` + `getCommentSummary` 改用 `doFindPageByQueryDirectly`/`doFindListByQueryDirectly`（见 Decision 机制）增审核过滤：仅返回 `auditStatus 为空 OR APPROVED` 的评论（PENDING/REJECTED 不公开）。`getCommentSummary` 的 goodRate/starDistribution/标签云**仅基于公开可见集合（APPROVED/null）聚合**，与 list 口径一致（避免汇总含未公开 PENDING 导致 goodRate 与可见列表不符）。
- [x] **Add:** 管理员 mutation `batchAuditComments(commentIds, action=approve/reject, ctx)` `@Auth(roles="admin")`：逐行守卫 `auditStatus==PENDING`（非 PENDING 跳过），approve→APPROVED **并发放积分**（预审 ON 延迟发放分支，经 `I*LitemallPointsAccountBiz`，sourceId=comment.id 保幂等；幂等捕获 `ERR_POINTS_DUPLICATE_EARN`），reject→REJECTED；catch-and-continue + 结果报告（复刻 `batchModerateComments` 先例）。
- [x] **Fix:** `getCommentReviewList` 增 `auditStatus` 筛选维度（支持按 PENDING/APPROVED/REJECTED 过滤，默认不限定=全部，避免预审未开租户工作台默认空），供工作台待审核筛选。
- [x] **Add:** ErrorCode `ERR_COMMENT_AUDIT_ACTION_INVALID`（"审核动作不合法（仅支持 approve/reject）"）于 `AppMallErrors.java`（既有 `ERR_COMMENT_MODERATION_ACTION_INVALID` 仅 hide/restore，不复用）。
- [x] **Proof:** 新增/改动方法通过 `IGraphQLEngine`：覆盖 预审 ON 发布→PENDING（公开不可见 + 积分未发）→approve→公开可见 + 积分发放（幂等）；预审 OFF 发布→公开可见 + 积分即发（行为同今）；reject→公开不可见 + 不发积分；batchAuditComments 部分失败；commentList/getCommentSummary 仅聚合 APPROVED/null。验证命令：`./mvnw test -pl app-mall-service -am`。

Exit Criteria:

- [x] 预审开关 ON 时新评论 PENDING（不公开 + 积分延迟），OFF 时公开可见 + 积分即发（行为同今）
- [x] 公共 commentList/getCommentSummary 经 `doFind*ByQueryDirectly` 仅返回/聚合 APPROVED/null
- [x] batchAuditComments(approve/reject) 落地 + 守卫；approve 分支发放积分（幂等）
- [x] **API 测试：** batchAuditComments 通过 `IGraphQLEngine`；submitComment/commentList/getCommentSummary 改动通过录制回放
- [x] `docs/logs/` 更新

### Phase 3 — 前端（评论审核工作台 待审核筛选 + 通过/拒绝按钮）

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/mall/goods-ops/comment-review.page.yaml`（P36 落地的 flat form+crud 工作台）
Required Skill: `nop-frontend-dev`

- Item Types: `Add | Proof`
- Prereqs: Phase 2（后端 mutation 就绪）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完 routing table 必读文档；列出已读路径。selfcheck（XView 三层/bounded-merge）。
  - Docs read: `.opencode/skills/nop-frontend-dev/SKILL.md`（XView 三层模型 / bounded-merge / 业务按钮 / 反模式表）、既有 `comment-review.page.yaml`（flat form+crud 结构 + 既有"批量下架/恢复" feedback 结果表先例）。
- [x] **Add:** 工作台筛选 form 增 `auditStatus` select（PENDING/APPROVED/REJECTED 选项，clearable，默认不限定=全部），crud 增 `auditStatus` 列（字典渲染）。
- [x] **Add:** crud `bulkActions` 增"批量通过"/"批量拒绝"按钮（调 `@mutation:LitemallComment__batchAuditComments`，action=approve/reject，复刻既有"批量下架/恢复"的 feedback 结果表结构）。
- [x] **Proof:** `./mvnw -pl app-mall-web -DskipTests compile` 通过；page.yaml 合法（`yaml.safe_load` 通过）。

Exit Criteria:

- [x] 工作台含 auditStatus 筛选 + 列 + 批量通过/拒绝按钮，编译通过
- [x] `docs/logs/` 更新

### Phase 4 — Owner Doc 同步 + 闭合

Status: completed
Targets: `docs/design/system-configuration.md`
Required Skill: `none`（文档同步）

- Item Types: `Add | Proof`
- Prereqs: Phase 1-3

- [x] **Add:** `system-configuration.md` Decision G4 段补"前置审核状态机"：auditStatus 字段、PENDING/APPROVED/REJECTED 流转、预审开关、向后兼容（null=通过）语义、公共可见性口径、Decision A 抉择/备选/残留风险。
- [x] **Proof:** owner doc 与 live 代码一致（字段名/字典值/方法名核对）。

Exit Criteria:

- [x] `system-configuration.md` 含前置审核状态机段，与代码一致
- [x] `docs/logs/` 更新（聚合闭合日志）

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent（fresh session），4 轮对抗式审计
- Evidence:
  - R1 `ses_0f1bbf92cf`：revise — M1（公共查询过滤机制未定，受 xmeta 约束）/ M2（积分发放时机为隐藏 Decision，REJECTED 无回收）/ M3（getCommentSummary 汇总口径与可见性不一致）。baseline 准确、Protected Area 合规、确为 P36 G4 Deferred successor。
  - 修订：新增 Phase 2 两 Decision——过滤机制择 `doFind*ByQueryDirectly`（含 public 端安全理由）；积分预审 ON 延迟至 approve 发放（幂等 sourceId=comment.id）；getCommentSummary 仅聚合 APPROVED/null。
  - R2 `ses_0f1b2e654f`：**pass** — 无 blocker/major；3 Major 全修并经 live code 验证。
  - 一致性微调：Goals 去"Tab"措辞；Phase 2 Targets 增 `ILitemallCommentBiz.java`；常量由 regen 生成不手改生成文件；getCommentReviewList 默认不限定=全部。
  - R3 `ses_0f1ac8584f`：**pass** — 无 blocker/major；微调未引入回归。
  - R4 `ses_0f1a67783f`：**pass** — 无 blocker/major；Owner Docs 路由无悬挂按需；Required Skill/API 测试/Closure Gates 齐全；baseline 抽验准确（auditStatus 全仓零匹配）。
  - 共识：R2/R3/R4 连续 clean（无 blocker/major）。实施仍受 ORM ask-first 门控。

## Closure Gates

- [x] in-scope behavior is complete（auditStatus + 预审开关 + 批量审核 + 公共过滤 + 工作台 + owner doc）
- [x] relevant docs are aligned（system-configuration.md）
- [x] verification has run（regen + 全模块编译 + service 测试 + web 编译）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（batchAuditComments）；submitComment/commentList/getCommentSummary 改动无回归
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（Phase 4 文档同步为非平台 phase，`none` 有正当理由）
- [x] skill loading verification: each phase scanned + loaded + read mandatory docs + selfchecked
- [x] text consistency verified: status, phases, gates, log all agree
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

> 起草时无可延期项。AI 内容自动审核、REJECTED 评论作者可见性增强、预审开关按分类粒度、积分发放 clawback（vs 延迟发放） 在 Non-Goals / Decision 显式裁定（含理由）。

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 全部 4 个 Phase 已交付并经独立闭合审计核验。所有 Exit Criteria/Closure Gates 已 `[x]`；owner doc (`system-configuration.md`) 与 live 代码一致；`docs/logs/2026/06-28.md` 含聚合闭合日志（397 tests, 0 failures, 0 errors）；Phase 间状态/文本一致。可闭合。

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计 subagent（fresh session，非实施 agent；由 MISSION_DRIVER 闭合审计任务触发）
- Audit Method: 重读计划全文 + 逐项 grep/read 核验 live repo（`./`）每个 Exit Criterion 对应代码/文档/日志，反 hollow 校验方法体非空/已接线/异常未吞。
- Evidence:
  - Phase 1（ORM）：`model/app-mall.orm.xml:481` `auditStatus`(propId=16, INTEGER, ext:dict="mall/comment-audit-status") 落地；`:152` 字典 `mall/comment-audit-status`(PENDING/APPROVED/REJECTED) 落地；nullable 不强制 default（历史行 null=已通过）。
  - Phase 2（后端）：`LitemallCommentBizModel.java:364-435` `batchAuditComments`（`@BizMutation`+`@Auth(roles="admin")`，真实方法体：守卫 `auditStatus==PENDING`、approve 发放幂等积分捕获 `ERR_POINTS_DUPLICATE_EARN`、reject→REJECTED、catch-and-continue + `BatchCommentResultBean` 报告）；`:584-585` `isPreModerationEnabled`（复刻 `resolveCommentReward` 防御解析）；`:292` `CONFIG_COMMENT_PRE_MODERATION`；`:142-144` `submitComment` 写 auditStatus 分支；`getCommentReviewList` 增 `auditStatus` 参数（`:443`）；`AppMallErrors.java:630` `ERR_COMMENT_AUDIT_ACTION_INVALID`；`ILitemallCommentBiz.java:64` 声明。反 hollow 通过——方法均有真实实现并经 GraphQL/IBiz 接线。
  - API 测试：`TestLitemallCommentBizModel.java`（line 601/712/740/762）+ `TestLitemallCommentOpsWorkbench.java`（line 286-374）通过 `IGraphQLEngine` 调 `LitemallComment__batchAuditComments` mutation 覆盖 approve/reject/跳过/部分失败/非法动作/空；非纯实体级测试，满足 Minimum Rule #15。
  - Phase 3（前端）：`comment-review.page.yaml:34,74,89-92,114-117` 筛选 form `auditStatus` select + crud 列 + 「批量通过」/「批量拒绝」bulkActions 调 `@mutation:LitemallComment__batchAuditComments`。
  - Phase 4（owner doc）：`docs/design/system-configuration.md:522-589` 含完整「前置审核状态机」小节（字段/字典/向后兼容 Decision A 抉择/备选/残留风险/预审开关/状态流转/公共可见性口径 Decision 过滤机制+安全理由+备选/积分发放时机 Decision 延迟+备选+残留风险/工作台）；运营动作表增 `batchAuditComments` 行。字段名/字典值/方法名与 live 代码一致。
  - 日志：`docs/logs/2026/06-28.md` 含聚合闭合日志（line 3-39），4 Phase 全覆盖；`./mvnw test -pl app-mall-service -am` = 397 tests, 0 failures, 0 errors。
  - 文本一致性：`Plan Status: completed` / 4 Phase `Status: completed` / 所有 Exit Criteria `[x]` / Closure Gates 全 `[x]` / 日志一致。
- Verdict: **approved** — 无 blocker/major；可闭合。
