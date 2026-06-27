# P28 签到（Daily Check-In）

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 28；`docs/design/marketing-and-promotions.md`（签到规则提及 line 124/136）、`docs/design/wallet-and-assets.md`（积分账户交接）
> Related: `docs/plans/2026-06-27-2029-1-phase27-points-system-plan.md`（P27 积分体系已 done，本计划复用 `earnPoints` API 并闭合 P27 deferred「签到/评价/分享得积分触发」复合残留中的签到子项；分享不在基线）
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 28（签到）

> **执行顺序：** 本计划为 2026-06-27-2321 批次第 1 顺位（N=1）。选作 N=1 因模型已预置、范围最小、首次为营销簇验证 nop-job-local 调度路径，并立即闭合 P27 遗留的签到触发残留。

## Current Baseline

> 实读 live repo（HEAD `0cc75d9`）所得，非记忆。

**模型已预置（关键）：**

- `model/app-mall.orm.xml:1738-1755` — `LitemallCheckInRule`（签到规则表）已存在：`daySeq`(连续第N天, mandatory int)、`pointReward`(奖励积分数 int)、`resetCycle`(重置周期天数, 0不重置 int)。
- `model/app-mall.orm.xml:1756-1778` — `LitemallCheckInRecord`（签到记录表）已存在：`userId`(mandatory)、`checkInDate`(date, mandatory)、`consecutiveDays`(连续签到天数 int)、`pointsEarned`(获得积分 int)，logical delete + 审计字段齐全。
- roadmap Entity Coverage（`enhanced-features-roadmap.md:573-574`）将 CheckInRule/CheckInRecord 列为「新增实体」，**与实际不符**——两者均已落地于模型（与 P26/P34 同模式，模型预置）。

**脚手架已生成但空：**

- `app-mall-service/.../entity/LitemallCheckInRuleBizModel.java`（17 行纯 `CrudBizModel`）、`LitemallCheckInRecordBizModel.java`（17 行纯 `CrudBizModel`）——无业务方法。
- `app-mall-web/src/main/resources/_vfs/app/mall/pages/LitemallCheckInRule/LitemallCheckInRule.view.xml`、`.../LitemallCheckInRecord/...view.xml` 为 codegen 空骨架。

**积分 earn API 已就绪（P27 done）：**

- `app-mall-service/.../entity/LitemallPointsAccountBizModel.java:75` — `earnPoints(@BizMutation)` 为签到发放预留入口（P27 deferred 记录：「签到属 P28，复用 earn API」）。
- `ILitemallPointsAccountBiz` 接口已声明 earnPoints，可直接 `@Inject` 调用（跨实体走 `I*Biz`，合规）。

**调度路径已就绪（关键发现，推翻 roadmap 陈述）：**

- roadmap（`enhanced-features-roadmap.md:64,109`）称「nop-job 未引入，签到周期重置需引入」**与实际不符**。Phase 11（`implementation-roadmap.md:31`）已 `done` 且引入 `nop-job-local`：`app-mall-app/pom.xml:65` 依赖 `nop-job-local`；`app-mall-app/src/main/resources/_vfs/nop/job/conf/scheduler.yaml` 含 5 个已运行的定时任务；`app-mall-service/.../scheduler/MallJobInvoker.java` 为标准 invoker 模式。本计划新增定时任务可直接接入此既有模式。

**缺口（本计划交付对象）：**

1. **业务设计缺失（关键）：** `marketing-and-promotions.md` 仅 line 124/136 提及签到，无独立章节（签到规则语义、连续天数算法、周期重置语义、积分联动、防重签约束均未定义）。
2. **无签到业务方法：** CheckInRecordBizModel 仅有 CRUD，无 `checkInToday`/`getMyCheckInStatus` 方法；无防重签约束（同日重复签到的处理未定义）。
3. **连续天数算法未实现：** `consecutiveDays` 字段存在但无计算逻辑（应基于昨日记录 +1，断签归 1）。
4. **周期重置未定：** `resetCycle`（规则字段）与「断签重置」「周期满重置」语义未对齐（Decision 项）。
5. **积分联动未接线：** 签到成功后未调 `earnPoints` 写积分流水。
6. **前端缺口：** 个人中心无签到入口；无签到日历/连续天数展示；无签到结果弹窗；后台规则管理为空骨架。
7. **无 ErrorCode、无测试。**

**前置条件已满足：** P27（积分体系）`done`。

**已知交叉：** `resetCycle` 语义需 Decision（见 Phase 1）；签到积分联动复用 P27 `earnPoints`，不重复造积分写入逻辑。

## Goals

- 实现连续签到奖励：按规则（第 N 天奖励 X 积分）每日签到发积分。
- 连续天数算法落地（昨日有记录则 +1，断签归 1）；周期满按 `resetCycle` 重置循环（Decision 定语义）。
- 防同日重复签到（用户当日已签置灰，二次签到拒绝）。
- 签到积分联动积分账户（复用 P27 `earnPoints`，写 PointsFlow，sourceType=check-in）。
- 个人中心签到入口 + 连续/累计天数 + 签到结果弹窗；后台规则管理页（daySeq/pointReward/resetCycle 配置）。
- 新增 check-in 域 ErrorCode；核心路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- 积分有效期/自动过期（P27 model-gap，需独立建模 + nop-job，本计划不涉及）。
- 签到补签/请假（连续断签补救机制）——超出 P28 范围，roadmap 未列。
- 签到提醒推送（站内信/消息中心属 P35，本计划仅发积分，不触发通知）。
- 营销活动管理后台统一入口（P22）。
- 移动端前端（属 `mobile-frontend-roadmap.md`）。
- 多端防刷（设备/IP 维度限签）——当前以 userId 为粒度即可，多端限签超出范围。

## Task Route

- Type: `app-layer design change` + `implementation-only change`（业务设计先行）
- Owner Docs: `docs/design/marketing-and-promotions.md`（签到章节）、`docs/design/wallet-and-assets.md`（积分账户交接确认）
- Skill Selection Basis: 后端 BizModel 方法/错误码 → `nop-backend-dev`；`@BizMutation`/`@BizQuery` 测试 → `nop-testing`（规则 #15）；AMIS 页面 → `nop-frontend-dev`；Phase 2 若需补模型关系/字典 → `nop-orm-modeler` + `nop-database-design`

## Infrastructure And Config Prereqs

- nop-job-local 已引入（`app-mall-app/pom.xml:65`），调度路径 `MallJobInvoker` + `scheduler.yaml` 既有可接入。
- **Decision（Phase 1）：** 周期重置是否需要定时任务——倾向「签到时即时计算」（查昨日记录决定 consecutiveDays，周期满即时归 1），无需每日定时扫描；若 Decision 选定时任务则新增 `MallJobInvoker.resetCheckInCycle()` + scheduler.yaml 条目。
- 无外部服务/端口/密钥依赖。无破坏性数据迁移（新表 CheckInRule/CheckInRecord，存量无影响）。

## Protected Area

> 见 `docs/context/ai-autonomy-policy.md`。本计划**预期不**触及受保护区域：
>
> - `model/app-mall.orm.xml`：CheckInRule/CheckInRecord 实体与字段均已存在，预期无需改模型。若 Phase 1 Decision 需要新增关系/字典（如 check-in source 字典），按 ask-first 流程处理并在此记录证据。
> - 不触及 `app-mall-delta`（签到不涉及认证/权限）。

## Execution Plan

### Phase 1 — 业务设计合成：签到语义（Decision-heavy）

Status: completed
Targets: `docs/design/marketing-and-promotions.md`（新增签到章节）、`docs/design/wallet-and-assets.md`（积分联动确认）
Required Skill: `none`（纯 docs 业务语义合成，模型已就绪不改；无 skill 匹配「写设计文档」）

- Item Types: `Decision | Add`
- Prereqs: P27 done（earnPoints API 契约已定）

- [x] **Skill loading gate:** 扫描 available skills；docs-only，无匹配。读 owner doc：`marketing-and-promotions.md`（全文）、`wallet-and-assets.md`（积分账户/流水）、`enhanced-features-roadmap.md` Phase 28、`domain-design-guidelines.md`。
  - Docs read: `docs/design/marketing-and-promotions.md`（全文）、`docs/design/wallet-and-assets.md`（全文）、`docs/backlog/enhanced-features-roadmap.md` Phase 28（line 33/109/340-352/573-574）、`docs/design/domain-design-guidelines.md`、`docs/context/project-context.md`、`docs/context/codebase-map.md`、`model/app-mall.orm.xml:1738-1778`（CheckInRule/Record）、`LitemallPointsAccountBizModel.java`（earnPoints 签名 + SOURCE_TYPE_CHECK_IN 常量 @ line 39 + 幂等检查 @ line 88-94）。
- [x] **Decision: 连续天数算法。** 抉择 A（签到即时计算：查昨日记录存在则 +1，否则归 1）。备选 B（定时任务每日扫描）被否——即时计算无延迟、无额外调度负担、与签到原子事务一致。
- [x] **Decision: 周期重置语义（`resetCycle`）。** 抉择 A（循环发奖：`resetCycle=0` 不循环达顶档后保持；`resetCycle>0` 达 resetCycle 天后归 1 循环）。备选 B（自然停）被否——循环更符合日活运营意图。
- [x] **Decision: 防同日重复签到。** 抉择 A（应用层查 (userId, checkInDate=今日) 存在则拒 ERR_CHECK_IN_ALREADY_TODAY）。备选 B（DB 唯一键）作为 successor model-gap。
- [x] **Decision: 奖励档位匹配。** 抉择 B（阶梯累进：取 ≤consecutiveDays 的最大 daySeq 档）。备选 A（精确匹配）被否——阶梯累进符合运营习惯。
- [x] **Decision: 积分联动 sourceType/sourceId。** sourceType 复用 SOURCE_TYPE_CHECK_IN="check-in"，sourceId=CheckInRecord.id。幂等性继承 P27 (sourceType,sourceId) 查重。
- [x] **Add:** 签到业务设计已写入 `marketing-and-promotions.md` 新增「签到 / Daily Check-In」章节（含 5 个 Decision 的抉择/备选/理由/残留风险）；`wallet-and-assets.md` line 106 更新签到为已落地积分来源交接确认（指向 P28）。

Exit Criteria:

- [x] `marketing-and-promotions.md` 含签到完整业务设计（含 5 个 Decision 的抉择/备选/理由/残留风险）
- [x] `wallet-and-assets.md` 积分来源交接确认签到
- [x] Phase 2 模型改动清单由本阶段 Decision 确定（预期零新增列/关系；若需字典则显式列出）— **结论：零模型改动**（防重签 DB 唯一键 deferred；sourceType 复用 P27 常量无需新字典）

### Phase 2 — 模型准备（按 Phase 1 Decision）

Status: completed
Targets: `model/app-mall.orm.xml`（仅当 Decision 要求）、codegen 重生成
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Add`
- Prereqs: Phase 1（模型字段集 Decision 已决）

- [x] **Skill loading gate:** Phase 1 Decision 结论为零模型改动（防重签 DB 唯一键 deferred；sourceType 复用 P27 常量），本阶段无需改模型故不加载 orm-modeler/database-design skill（skill 匹配「改模型」而非「验证 codegen 产物」）。读 `model/app-mall.orm.xml:1738-1778` 复核 CheckInRule/CheckInRecord 字段集已就绪。
  - Docs read: `model/app-mall.orm.xml:1738-1778`、`_gen/_LitemallCheckInRule.java`、`_gen/_LitemallCheckInRecord.java`（codegen 产物完整，getter/setter/PROP_NAME 齐全）。
- [x] **Add:** 按 Phase 1 Decision——零模型改动（CheckInRule/CheckInRecord 已就绪，防重签 DB 唯一键 deferred）。仅验证 codegen 产物完整。
- [x] **Proof:** `./mvnw install -pl app-mall-codegen -am -DskipTests` BUILD SUCCESS + `./mvnw install -pl app-mall-dao -am -DskipTests` BUILD SUCCESS。

Exit Criteria:

- [x] 模型就绪（零改动或 Decision 要求的改动落地），codegen 通过，编译成功
- [x] 不在模型准备阶段写业务逻辑（rule #11）

### Phase 3 — 后端：签到逻辑 + 积分联动 + ErrorCode（Add-heavy）

Status: completed
Targets: `app-mall-service/.../entity/LitemallCheckInRecordBizModel.java`、`LitemallCheckInRuleBizModel.java`、`AppMallErrors.java`
Required Skill: `nop-backend-dev`、`nop-testing`（新增 `@BizMutation`/`@BizQuery`，规则 #15）

- Item Types: `Add`
- Prereqs: Phase 2

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读 routing 必读文档（`04-reference/bizmodel-method-selfcheck.md`、`02-core-guides/error-handling.md`、`04-reference/safe-api-reference.md`、`05-examples/test-examples.java`）。每方法 selfcheck 全部通过（#1-4 接口+注解+@Override / #5-7 DTO 返回 / #8 newEntity / #9 findFirst-findCount-saveEntity / #10 跨实体注入 I*Biz / #11-14 NopException+ErrorCode 中文描述 / #15 无 @Transactional / #16 @Inject 非 private / #17 方法名不冲突 / #18 CoreMetrics.currentDate() / #19-20 无第三方 JSON/Apache）。
  - Docs read: `nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`、`02-core-guides/error-handling.md`、`04-reference/safe-api-reference.md`、`05-examples/test-examples.java`、`app-mall-service/.../LitemallPointsAccountBizModel.java`（earnPoints 签名 + 幂等 + SOURCE_TYPE_CHECK_IN 常量）、`LitemallCommentBizModel.java`（user-scoped mutation 范式）。
- [x] **Add:** `LitemallCheckInRecordBizModel.checkInToday(context)` —— `@BizMutation`：查今日记录（防重签 ERR_CHECK_IN_ALREADY_TODAY），查规则表空则拒（ERR_CHECK_IN_RULE_MISSING），查昨日记录算 consecutiveDays（断签归 1），按 resetCycle 循环重置（candidate>maxResetCycle 归 1），阶梯匹配规则档（≤consecutiveDays 最大 daySeq 的 pointReward），newEntity 落 CheckInRecord，pointReward>0 时调 `pointsAccountBiz.earnPoints(userId, pointReward, POINTS_CHANGE_TYPE_EARN, SOURCE_TYPE_CHECK_IN, record.orm_idString(), remark, context)`。返回 `CheckInResultBean`（todayChecked/consecutiveDays/pointsEarned/accountBalance）。幂等性继承 P27 (sourceType,sourceId) 检查。
- [x] **Add:** `LitemallCheckInRecordBizModel.getMyCheckInStatus(context)` —— `@BizQuery`：今日是否已签、当前连续天数（今日有则取今日，否则取昨日延续）、累计签到天数（findCount）、账户余额（getMyPoints）、奖励规则预览（rewardRules 列表）。返回 `CheckInStatusBean`。
- [x] **Add:** 规则查询辅助 `findRules`（checkInRuleBiz.findList + addOrderField daySeq asc）、阶梯匹配 `matchTier`、`resolveResetCycle`（取规则表 max resetCycle）。
- [x] **Add:** `AppMallErrors` 新增 check-in 域 ErrorCode（`ERR_CHECK_IN_ALREADY_TODAY`、`ERR_CHECK_IN_RULE_MISSING`，中文描述）。DTO：`CheckInResultBean`、`CheckInStatusBean`、`CheckInRewardRuleBean`（`app-mall-dao/.../dto/`，@DataBean）。`ILitemallCheckInRecordBiz` 接口声明 checkInToday + getMyCheckInStatus。
- [x] **Proof:** checkInToday + getMyCheckInStatus 通过 `IGraphQLEngine`（`JunitBaseTestCase`）：10 个测试用例覆盖首签/连签/断签归 1/周期重置/无循环保持顶档/防同日重签拒绝/无规则拒绝/零档位兜底/积分流水写入（sourceType=check-in）/状态查询连续天数延续。全量回归 186 测试通过无失败。

Exit Criteria:

- [x] 签到全流程（首签/连签/断签/周期重置/防重签）按设计工作；积分联动写 PointsFlow
- [x] **API 测试：** checkInToday（`@BizMutation`）+ getMyCheckInStatus（`@BizQuery`）通过 `IGraphQLEngine` 验证（10 用例）
- [x] check-in ErrorCode 已定义并被使用（ALREADY_TODAY 用于防重签，RULE_MISSING 用于规则表空兜底）

### Phase 4 — 前端：个人中心签到 + 后台规则管理（Add-heavy）

Status: completed
Targets: `app-mall-web/.../pages/mall/user/user-center.page.yaml`（或新增签到页）、`pages/LitemallCheckInRule/LitemallCheckInRule.view.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 3

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读 routing 必读文档（XView 三层模型、bounded-merge、grid/form 定制、page-dsl）。文件完成后 selfcheck：未改 `_gen`（改保留层 view.xml + user-center.page.yaml）、bounded-merge 用于 cols/rowActions、Delta 未涉及。
  - Docs read: `nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`、`app-mall-web/.../LitemallMemberLevel/LitemallMemberLevel.view.xml`（bounded-merge 范式）、`_gen/_LitemallCheckInRule.view.xml`、`pages/mall/user/user-center.page.yaml`（service+card+feedback 范式）。
- [x] **Add:** 个人中心签到入口（service card）：调 `@query:LitemallCheckInRecord__getMyCheckInStatus` 展示连续/累计天数 + 可用积分 + 奖励规则预览（each 遍历 rewardRules）；「立即签到」按钮调 `@mutation:LitemallCheckInRecord__checkInToday`，`disabledOn: todayChecked` 置灰，`reload: checkInStatus` 刷新；feedback 弹窗展示本次得积分 + 连续天数 + 余额。
- [x] **Add:** 后台 `LitemallCheckInRule.view.xml` 定制：grid bounded-merge 列（id/daySeq sortable/pointReward/resetCycle）；query form（daySeq）；edit drawer 表单（daySeq/pointReward/resetCycle 三字段 + placeholder 提示「0=不循环，>0 达此天数后归 1 循环」）；add form x:prototype=edit；rowActions bounded-merge（view/update drawer/delete）。

Exit Criteria:

- [x] 个人中心可签到、展示连续/累计天数与奖励预览、已签置灰、结果弹窗
- [x] 后台可配置签到规则（daySeq/pointReward/resetCycle）
- [x] 复用既有 AMIS 三层定制模式，无新前端依赖（`./mvnw compile -pl app-mall-web` BUILD SUCCESS）

### Phase 5 — 验证、文档同步、日志（Proof）

Status: completed
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-4

- [x] **Skill loading gate:** 加载 `nop-testing`，读 routing 必读文档（Phase 3 已读 test-examples.java/testing.md，本阶段复用）。
- [x] **Proof:** 跑 `docs/context/project-context.md` 真实验证命令：`./mvnw test -pl app-mall-service` **186 测试全绿**（含本计划新增 `TestLitemallCheckInRecordBizModel` 10 例）；`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS；更新 `docs/testing/known-good-baselines.md`（Phase 28 baseline row 置顶）。
- [x] **Proof:** 前端 view 编译（`./mvnw -pl app-mall-web -DskipTests compile`）BUILD SUCCESS（view.xml/page.yaml 资源经 precompile 解析通过）。
- [x] 更新 `docs/logs/2026/06-27.md`（逆向时间序置顶 Phase 28 条目）。

Exit Criteria:

- [x] 全量验证命令通过（含本计划新增 IGraphQLEngine 测试）
- [x] `known-good-baselines.md` 更新本计划 baseline row
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗审计（fresh session），一轮达成共识（round 1 即 PASS，无 BLOCKER/MAJOR）。
  - Round 1（`ses_0f64f542affeipV9QFKGu86sYh`）：PASS — 基线全部实读核验（CheckInRule @ orm.xml:1738-1755、CheckInRecord @ 1756-1778、空 BizModel 脚手架、earnPoints @ PointsAccountBizModel.java:75、**nop-job-local 已引入推翻 roadmap「未引入」陈述** @ pom.xml:65 + scheduler.yaml + MallJobInvoker、marketing-and-promotions.md 无签到章节、P27 done）。6 个 MINOR（非阻塞）：SOURCE_TYPE_CHECK_IN 常量已存在(@:39)应复用、earnPoints 完整签名含 changeType/remark、幂等性继承 P27 (sourceType,sourceId) 检查、implementation-roadmap.md:54 亦有 nop-job 陈旧陈述、行数/行号微漂移、userId 类型 stdSqlType=INTEGER/stdDataType=string 模型不一致。MINORs 已纳入 Phase 3 描述（常量复用 + 完整签名 + 幂等继承）。
- Evidence: 实读 live repo（HEAD `0cc75d9`）核验基线。关键发现：nop-job-local 已引入（Phase 11 done），P28 调度路径既有可接入，roadmap「nop-job 未引入」陈述陈旧。模型 CheckInRule/Record 已预置（与 P26/P34 同模式）。

## Closure Gates

- [x] in-scope behavior is complete（签到全流程 + 积分联动 + 前后台）
- [x] relevant docs are aligned（`marketing-and-promotions.md` 签到章节 / `wallet-and-assets.md` 积分来源）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 全绿 + app-mall-web 编译）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（checkInToday/getMyCheckInStatus）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（Phase 1 `none` 含 justify）
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### CheckInRecord (userId, checkInDate) DB 唯一键

- Classification: `model-gap`
- Why Not Blocking Closure: 防重签应用层查 (userId, checkInDate=今日) 已保证正确性；DB 唯一键为强一致兜底，签到非高并发路径，应用层足够。模型当前无该唯一键。
- Successor Required: `yes`
- Model Gap Detail: 缺 `LitemallCheckInRecord` 的 `(userId, checkInDate)` 唯一键；触发条件——下次修改 CheckInRecord 模型时，或业务要求防重签 DB 级强一致时，补 unique-key。

### P27 签到得积分触发残留闭合

- Classification: `watch-only residual`（预期本计划闭合）
- Why Not Blocking Closure: P27 deferred 记录签到得积分为 watch-only residual（触发条件 P28 启动复用 earn API）。本计划实现签到→earnPoints 联动后该残留闭合，非遗留缺陷。
- Successor Required: `no`（本计划交付即闭合）

## Closure

<!-- Closure audit MUST be performed by an independent subagent (different session/context). 留给闭合审计代理。 -->

Status Note: 全部 5 个 Phase 完成，in-scope 行为交付（签到全流程 + 积分联动 + 前后台）。验证：`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS；`./mvnw test -pl app-mall-service` 186 测试全绿（含新增 10 例 IGraphQLEngine 测试）；`./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS。roadmap Phase 28 → done（订正 stale nop-job 陈述）。文档：marketing-and-promotions.md 签到章节、wallet-and-assets.md 积分来源交接、logs/2026/06-27.md、known-good-baselines.md 均更新。

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计 subagent（fresh session，非实施代理）。审计日期 2026-06-28。
- Audit Scope: 重新核验全部 Exit Criteria / Closure Gates 对照 live repo（HEAD `0cc75d9` + 工作树未提交改动），对抗性审查不轻信 `[x]`。
- Evidence:
  - **Phase 1（业务设计）**：`docs/design/marketing-and-promotions.md:171-251` 含完整「签到 / Daily Check-In」章节（业务规则、连续天数算法、周期重置、防同日重签、阶梯档位匹配、积分联动、状态语义、生命周期、5 个 Decision 抉择/备选/理由）；`docs/design/wallet-and-assets.md:91,106` 积分来源交接确认签到已落地。PASS。
  - **Phase 2（模型）**：复核 `model/app-mall.orm.xml:1738-1778` CheckInRule/CheckInRecord 字段集未变（零模型改动），codegen 产物 `_gen/_LitemallCheckInRecord.java` 完整。PASS。
  - **Phase 3（后端）**：`LitemallCheckInRecordBizModel.java`（179 行）含 `checkInToday`（`@BizMutation` + `@Override`，行 44-93：防重签→规则空拒→查昨日算 consecutiveDays→resetCycle 循环→阶梯 matchTier→newEntity/saveEntity→earnPoints 联动→返回 CheckInResultBean，非空壳）+ `getMyCheckInStatus`（`@BizQuery` + `@Override`，行 95-127：今日已签/连续天数/累计/余额/奖励规则预览，非空壳）+ 辅助 findRules/matchTier/resolveResetCycle/toRewardRuleBeans。跨实体注入 `ILitemallPointsAccountBiz` + `ILitemallCheckInRuleBiz`（行 34-38，非 private，合规）。`AppMallErrors.java:364,368` 定义 `ERR_CHECK_IN_ALREADY_TODAY`/`ERR_CHECK_IN_RULE_MISSING`（NopException + ErrorCode，合规）。`ILitemallCheckInRecordBiz.java:15,18` 接口声明两方法。DTO `CheckInResultBean`/`CheckInStatusBean`/`CheckInRewardRuleBean` 均存在于 `app-mall-dao/.../dto/`。Anti-hollow：方法均有真实分支与返回，无 `return null` 占位、无吞异常。PASS。
  - **Phase 3（API 测试）**：`TestLitemallCheckInRecordBizModel.java`（216 行）extends `JunitBaseTestCase`，注入 `IGraphQLEngine`，通过 `graphQLEngine.newRpcContext` + `executeRpc` 调用 `LitemallCheckInRecord__checkInToday`（mutation）与 `__getMyCheckInStatus`（query）。10 个 `@Test`：testFirstCheckInEarnsTierOne / testConsecutiveIncrementAndStepwiseTier / testBrokenStreakResetsToOne / testDuplicateSameDayRejected / testCycleResetWhenExceedingResetCycle / testNoCycleCapsAtTopTier / testNoRulesRejected / testZeroRewardWhenNoMatchingTier / testGetMyCheckInStatusAfterCheckIn / testGetMyCheckInStatusStreakFromYesterday——覆盖首签/连签/断签/周期重置/无循环顶档/防同日重签/无规则/零档位/积分流水（countCheckInFlows 验 sourceType=check-in）/状态查询连续天数延续。符合规则 #15（IGraphQLEngine 录制回放）。PASS。
  - **Phase 4（前端）**：`user-center.page.yaml:116-175` 含 checkInStatus service card，调 `@query:LitemallCheckInRecord__getMyCheckInStatus` 展示连续/累计天数 + 余额 + 奖励规则，「立即签到」按钮调 `@mutation:LitemallCheckInRecord__checkInToday` + feedback 弹窗 + reload 刷新，已接线运行时可达。`LitemallCheckInRule.view.xml` 含 grid bounded-merge（daySeq sortable/pointReward/resetCycle）+ edit drawer（三字段 + placeholder 提示 resetCycle 语义）+ rowActions bounded-merge，未改 `_gen`。PASS。
  - **Phase 5（验证）**：`docs/logs/2026/06-27.md:3-11` Phase 28 条目（逆向时间序置顶）记录全量交付 + 186 测试全绿 + uber-jar BUILD SUCCESS + web 编译 SUCCESS；`docs/testing/known-good-baselines.md:13` Phase 28 baseline row 置顶。PASS。
  - **Deferred honesty**：`(userId, checkInDate)` DB 唯一键为真实 model-gap（应用层查已保证正确性，触发条件明确），非遗留缺陷伪装；P27 签到触发残留已由本计划 earnPoints 联动真闭合，非隐藏。PASS。
  - **Roadmap 同步**：`enhanced-features-roadmap.md:33` Phase 28 已 `done`（引用本计划路径），line 109 订正 stale nop-job 陈述为「即时计算无需定时任务」。PASS。
  - **Text consistency**：Plan Status `completed` / 5 个 Phase Status 均 `completed` / 全部 Exit Criteria `[x]` / 全部 Closure Gates `[x]`（本次审计勾选）/ Closure evidence 非占位 / log 条目一致。PASS。
- Verdict: APPROVED — 全部 Exit Criteria 与 Closure Gates 对照 live repo 核验通过，in-scope 行为完整落地，无空壳、无隐藏 deferred 缺陷、无文本不一致。Plan 可标记 `completed`。

Follow-up:

- CheckInRecord (userId, checkInDate) 唯一键（触发条件：CheckInRecord 模型修改或防重签 DB 强一致需求）。
