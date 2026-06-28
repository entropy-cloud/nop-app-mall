# 用户画像算法化（P19 RFM/生命周期 → P20 用户详情/分群）

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Mission: mall
> Work Item: P20 deferred successor「算法化用户画像/RFM/生命周期」（触发条件已满足：P19 done）
> Source: `docs/plans/2026-06-28-0340-2-phase20-user-operations-workbench-plan.md` → `Deferred But Adjudicated → 算法化用户画像/RFM/生命周期`（Successor Required: yes，触发条件「P19 启动用户分析报表时」）
> Related: `docs/plans/2026-06-28-1027-2-phase19-report-system-extension-plan.md`（P19 聚合 RFM/生命周期/留存报表，已 done）、`docs/plans/2026-06-28-0340-2-phase20-user-operations-workbench-plan.md`（P20 用户工作台，当前画像=手工标签集合展示）
> Audit: required

## Current Baseline

> 来源：实读 live repo（HEAD 经 grep/read 核验），非旧计划记忆。

**P19 已实现 RFM/生命周期/留存，但全部为「聚合分段计数」，无 per-user 画像：**
- `LitemallOrderBizModel.getUserRfm`（`LitemallOrderBizModel.java:1308`）：按全量用户计算 R/F/M 中位数阈值（`rMedian`/`fMedian`/`mMedian`），逐用户分类到 8 段（`labelRfm`，`重要价值/保持/发展/挽留` × `一般价值/保持/发展/挽留`），**仅返回 `List<RfmSegmentBean>`（segment + userCount 聚合计数）**，不返回任何单个用户的 R/F/M 值或归属段。
- `LitemallOrderBizModel.getUserLifecycle`（`:1373`）：逐用户按 first/last pay time + churnDays(默认 90) 分类到 `新客/活跃/沉睡/流失`，**仅返回 `List<LifecycleSegmentBean>`（聚合计数 + 占比）**。
- `getUserRetention`（`:1246`）：留存同期群（D1/D7/D30），聚合。底层 per-user 中间数据（`orderMapper.getUserPaymentSummaryAllTime`/`getUserPaymentSummaryInPeriod` 返回 `UserPaymentSummaryBean`：userId/orderCount/totalAmount/firstPayTime/lastPayTime）已存在，但仅用于聚合，未对单用户暴露。
- 分类逻辑现状：`labelRfm`（`:1357`，`private static`）**可直接复用**；但**生命周期分类规则内联在 `getUserLifecycle`（`:1393-1411`），无独立 `classifyLifecycleStage(...)` 私有方法**（`makeLifecycleSegment` `:1423` 仅构造聚合 bean，非分类器）——per-user 画像复用 lifecycle 规则需先抽取分类逻辑（见 Decision B / Phase 1 重构项）。

**P20 用户详情页画像=手工标签集合，无算法化 RFM/生命周期：**
- `mall/user-ops/user-detail.page.yaml`：消费 `@query:LitemallUserBlacklist__getUserWorkbenchSummary`（`LitemallUserBlacklistBizModel.java:125`，返回 `UserWorkbenchSummaryBean`：基本信息 + orderCount/totalSpending + pointsBalance + coupon 计数 + footprint/feedback 计数）。**无 RFM 段、无生命周期阶段、无 R/F/M 原始值**。
- 分群页 `app/mall/pages/LitemallUserTag/segment.page.yaml`（`LitemallUserTag.view.xml:27` 的「按标签分群」按钮跳转此页）：仅按**手工标签**分群（`findUsersByTag`），无按 RFM 段/生命周期阶段的算法化分群维度。
- P20 计划显式记录：「算法化用户画像/RFM/生命周期 — 归 P19 报表体系扩展；本计划画像=标签集合展示。Successor Required: yes（P19 启动用户分析报表时）」。**P19 已 done → 触发条件已满足，successor 到期。**

**差距：** P19 的 RFM/生命周期分类只产出报表聚合数字，运营在用户详情页看不到单个用户的算法化画像（R/F/M 值、RFM 段、生命周期阶段），分群页也无法按算法化维度圈选用户。算法分类逻辑已存在，仅需 per-user 暴露 + 前台接线。

## Goals

- 提供 per-user 算法化画像 `@BizQuery getUserPortrait(userId)`：返回 R/F/M 原始值 + RFM 段（复用 `labelRfm`）+ 生命周期阶段（复用 lifecycle 规则）+ 首末单时间 + 订单数/累计消费，复用既有 `orderMapper` 查询与分类逻辑，**不新增 ORM、不新增 SQL-lib 聚合**。
- 用户详情页 `user-detail.page.yaml` 新增「算法画像」面板展示 RFM 段 + 生命周期阶段 + R/F/M 值，使 P20 画像从「手工标签集合」升级为「算法化 + 手工标签」并存。
- 分群页 `segment.page.yaml` 增加算法化分群维度（按 RFM 段/生命周期阶段圈选用户成员列表），使算法化分群可操作（当前仅手工标签分群）。
- 所有新增 `@BizQuery` 通过 `IGraphQLEngine` 测试。

## Non-Goals

- **RFM/lifecycle 分类口径本身的重设计**——复用 P19 已落地且经测试的 `labelRfm` + lifecycle 规则，不改变阈值算法或分段命名（口径一致性优先）。
- **画像/分群的实时推荐或 ML 模型**——本计划为规则化 RFM/生命周期，非算法模型。
- **移动端画像展示**——移动前端有独立 roadmap。
- **新增用户标签实体或 ORM 改动**——画像为只读计算视图，不落库（见 Decision）。

## Task Route

- Type: `app-layer design change` + `implementation-only change`（业务设计在 `system-configuration.md` 报表口径 + `user-and-address.md` 用户运营已落地；per-user 画像口径为既有聚合口径的 per-user 投影，但对 owner doc 为新增口径，须补齐）
- Owner Docs: `docs/design/system-configuration.md`（报表与统计 → 用户分析：补 per-user 画像口径）、`docs/design/user-and-address.md`（用户运营工作台 → 用户详情画像 + 算法化分群维度）
- Skill Selection Basis: 后端 `@BizQuery` per-user 画像 + 复用分类逻辑 + 跨实体走 `I*Biz`/既有 mapper → `nop-backend-dev`；AMIS 用户详情/分群页 → `nop-frontend-dev`；含新增 `@BizQuery` → `IGraphQLEngine` 测试 → `nop-testing`。**不触及 `model/*.orm.xml`**（复用 `orderMapper` 既有查询），无 ORM skill。

## Infrastructure And Config Prereqs

- 无新增基础设施。复用 P19 已建立的 `orderMapper`（`@SqlLibMapper` `/app/mall/sql/LitemallOrder.sql-lib.xml`）per-user 支付汇总查询与 Java 分类逻辑。
- churnDays 复用 P19 默认 90（`DEFAULT_CHURN_DAYS`），不引入新配置。

## Execution Plan

### Phase 1 - per-user 算法画像 API（Add-heavy）

Status: completed
Targets: `app-mall-service/.../entity/LitemallOrderBizModel.java`、`app-mall-dao/.../biz/ILitemallOrderBiz.java`（接口声明）、`app-mall-dao/.../dto/`（画像 bean）
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision`
- Prereqs: 无

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完其 routing table 标为必读的文档；列出已读路径。每方法完成后用 skill selfcheck 校验（跨实体走 `I*Biz`/既有 mapper、`CoreMetrics.currentTimeMillis()`、NopException、无 anti-pattern）。
  - Docs read: `../nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`、`../nop-entropy/docs-for-ai/02-core-guides/service-layer.md`、`../nop-entropy/docs-for-ai/02-core-guides/error-handling.md`、`../nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`、`../nop-entropy/docs-for-ai/02-core-guides/testing.md`
- [x] **Decision A：画像 API 归属 BizModel。** 抉择（含备选 + 残留风险）：画像放 `LitemallOrderBizModel`（与 P19 `getUserRfm`/`getUserLifecycle` 同源，分类逻辑在同文件可直接复用）还是 `LitemallUserBlacklistBizModel`（与 `getUserWorkbenchSummary` 同源，用户详情页已消费）。**抉择：放 `LitemallOrderBizModel`**（分类逻辑在同文件，复用最近；`labelRfm` 为 `private static` 可直接调；用户详情页经 `@query:LitemallOrder__getUserPortrait` 调用，无需 page 层注入）。备选：放 `LitemallUserBlacklistBizModel` 需跨实体复用分类逻辑（复制或暴露）——被否（逻辑重复、口径漂移风险）。残留风险：无。
- [x] **Decision B：RFM 阈值 + lifecycle 分类的 per-user 复用方式（含重构 + 口径抉择）。** 抉择（含备选 + 残留风险）：RFM 段依赖全量用户中位数阈值，lifecycle 分类规则在 `getUserLifecycle`（`:1393-1411`）**内联、无独立方法**。两处抉择：
  - **重构：** 将 `getUserRfm` 内联阈值计算抽取为 `computeRfmThresholds(summaries)`，将 `getUserLifecycle` 内联分类抽取为 `classifyLifecycleStage(firstPayTime, lastPayTime, today, churnDays, inPeriod, firstInPeriod)`；`getUserRfm`/`getUserLifecycle` 改调抽取后的方法（行为不变、P19 测试不回归）。
  - **口径抉择（all-time vs period）：** P19 报表 `getUserRfm`/`getUserLifecycle` 用 **period** 口径（`getUserPaymentSummaryInPeriod`，默认近 30 天）。per-user 画像用于「该用户当前全貌」，**抉择 all-time/当前快照口径**：R=距末单天数（all-time lastPayTime）、F=累计单数、M=累计消费（均取自 `getUserPaymentSummaryAllTime`）；RFM 阈值用 `computeRfmThresholds(allTimeSummaries)`（全量 all-time 阈值）；lifecycle 用 `classifyLifecycleStage` 以「活跃窗口=近30天、churn=90天」判定当前阶段（`inPeriod`/`firstInPeriod` 由 all-time 数据派生：`inPeriod = allTimeLastPayTime 落在活跃窗口内`、`firstInPeriod = allTimeFirstPayTime 落在活跃窗口内`，无需额外 period 查询，保持 all-time 口径自洽）。**画像口径与报表 period 口径不同（意图不同：报表看期间分布，画像看用户当前全貌）——本计划不主张跨口径段值相等，仅保证分类逻辑同源（同一私有方法，无逻辑分叉）。**
  - 备选：画像改用 period 口径 + period 阈值以求与报表段值严格一致——被否（period 内无消费用户 R/F/M 退化、且用户详情看当前全貌更自然）；在 `getUserPortrait` 内复制逻辑——被否（逻辑分叉漂移风险）；仅返回 R/F/M 原始值不标段（信息量不足）。
  - 残留风险：(1) 抽取为既有 P19 代码重构，须以 P19 既有 IGraphQLEngine 测试全绿证明零行为回归；(2) all-time 阈值与 period 阈值不同，画像 RFM 段与报表段不逐值相等（已显式记录，非缺陷）。
  - **执行补记：** 重构 `getUserRfm` 内联阈值 → 抽取 `computeRfmThresholds(summaries) + RfmThresholds` 私有载体 + `classifyRfmSegment(s, thresholds)`；重构 `getUserLifecycle` 内联分类 → 抽取 `classifyLifecycleStage(firstInPeriod, inPeriod, lastAllDate, today, churnDays)`，`getUserLifecycle` 改 switch 调用。两者均与 P19 同源（行为不变）。P19 既有 16 测试（TestLitemallOrderStatisticsBizModel）全绿证明零回归。
- [x] **Add（重构）:** 抽取 `computeRfmThresholds()` + `classifyLifecycleStage(...)` 私有方法，`getUserRfm`/`getUserLifecycle` 改调，P19 既有测试不回归。
- [x] **Add:** `@BizQuery getUserPortrait(userId)` 返回 `UserPortraitBean`（**all-time/当前快照口径**）：recencyDays(距末单天数)/frequency(累计单数)/monetary(累计消费)/rfmSegment(`labelRfm`，对 all-time R/F/M 用 `computeRfmThresholds(allTimeSummaries)` 阈值分类)/lifecycleStage(新客/活跃/沉睡/流失，`classifyLifecycleStage` 以活跃窗口=近30天、churn=90天判定)/firstPayTime/lastPayTime。复用 `orderMapper.getUserPaymentSummaryAllTime` 取目标用户 + 全量阈值；分类经抽取后的私有方法（与 P19 同源、无逻辑分叉）。无消费记录用户返回「未消费」画像（rfmSegment/lifecycleStage 置空 + 计数 0）。接口声明入 `ILitemallOrderBiz`。
  - **执行补记：** 修复 `getUserPaymentSummaryAllTime` SQL（原 `0 AS TOTAL_AMOUNT` → `COALESCE(SUM(ACTUAL_PRICE), 0) AS TOTAL_AMOUNT`），原 SQL 把 M 恒置 0 导致画像 M 不可用；该修复对 `getUserLifecycle`（不读 totalAmount）零影响。新增 ErrorCode `ERR_USER_PORTRAIT_INVALID_SEGMENT_TYPE/VALUE`。
- [x] **Proof:** `IGraphQLEngine` 测试 `getUserPortrait`：有消费用户（all-time R/F/M 值 + 按 all-time 阈值分类的 rfmSegment 正确 + lifecycle 阶段正确）、无消费用户（未消费画像）、**分类逻辑同源验证**（`getUserPortrait` 与 `getUserRfm`/`getUserLifecycle` 调用同一 `labelRfm`/`computeRfmThresholds`/`classifyLifecycleStage` 私有方法，无逻辑分叉——不主张跨口径段值相等）；P19 既有 `getUserRfm`/`getUserLifecycle` 测试全绿（证明重构零回归）。
  - **执行补记：** 新增 `TestLitemallUserPortraitBizModel` 共 9 测试全绿（Phase 1 部分 5 测试 + Phase 2 部分 4 测试）。`./mvnw -pl app-mall-service -am test`：360 测试全绿（前 351 + 新 9）。`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS。

Exit Criteria:

- [x] per-user 画像 API 落地（all-time/当前快照口径），分类逻辑与 P19 同源（同一私有方法，无逻辑分叉），无消费用户有明确画像
- [x] Decision A/B 抉择 + 备选 + 残留风险记录（含 all-time vs period 口径抉择）
- [x] **API 测试：** `getUserPortrait` 通过 `IGraphQLEngine` 测试（有/无消费 + 分类逻辑同源 + P19 零回归）
- [x] `docs/logs/` updated

### Phase 2 - 用户详情画像面板 + 算法化分群维度（Add | Proof）

Status: completed
Targets: `app-mall-web/.../pages/mall/user-ops/user-detail.page.yaml`、`app-mall-web/.../pages/LitemallUserTag/segment.page.yaml`、`app-mall-service`（分群成员 API）、`app-mall-dao/.../biz/ILitemallOrderBiz.java`（接口声明）
Required Skill: `nop-frontend-dev`、`nop-backend-dev`、`nop-testing`

- Item Types: `Add | Proof`
- Prereqs: Phase 1（`getUserPortrait` API + 抽取的 `computeRfmThresholds`/`classifyLifecycleStage`）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev` + `nop-backend-dev` + `nop-testing`，读完必读文档；列出已读路径。每页/方法完成后 selfcheck。
  - Docs read: `../nop-entropy/docs-for-ai/00-start-here/application-project-defaults.md`、`../nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`、`../nop-entropy/docs-for-ai/02-core-guides/delta-customization.md`、`../nop-entropy/docs-for-ai/02-core-guides/page-dsl-pattern-catalog.md`、`../nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`、`../nop-entropy/docs-for-ai/02-core-guides/testing.md`
- [x] **Decision C：算法化分群成员列表 API 的计算与性能。** 抉择（含备选 + 残留风险）：算法化分群（按 RFM 段/生命周期阶段圈选用户）需对**全量用户**逐个分类后过滤，比单用户画像重得多且为运营高频操作。**抉择：`@BizQuery getSegmentMembers(segmentType, segmentValue, page, pageSize)`（admin 上下文报表场景，与 `getUserRfm`/`getUserLifecycle` 同属运营报表无额外显式 auth 注解，沿用既有报表权限边界）复用 Phase 1 抽取的 `computeRfmThresholds(allTimeSummaries)` + `classifyLifecycleStage` 对全量 `orderMapper.getUserPaymentSummaryAllTime` 结果逐用户分类（all-time 口径，与 `getUserPortrait` 一致），Java 端过滤命中段后分页返回（返回 userId/userName/lastPayTime/orderCount/totalAmount）。**备选：SQL-lib 直接聚合命中段（需新增 SQL + 口径与 Java 分类双写漂移）——被否；预计算物化段表（需 ORM ask-first + 失效策略）——被否（超出非 Protected 范围）。残留风险：全量分类的 CPU/内存成本随用户量增长（基线用户量可接受；规模化记 Deferred「分段物化」）。
  - **执行补记：** 实际返回 `PageBean<SegmentMemberBean>`（userId/rfmSegment/lifecycleStage/orderCount/totalAmount/lastPayTime；userName 未连线因 NopAuthUser 跨实体查询未在 P19 既有 mapper 暴露，避免新增跨实体 IBiz 注入超出计划范围，后续如需用户名展示可在 successor 接 ILitemallUserBiz）。命中段排序按 orderCount desc → totalAmount desc。默认 page=1/pageSize=20，pageSize 上限 500。
- [x] **Add:** `user-detail.page.yaml` 新增「算法画像」面板：调用 `@query:LitemallOrder__getUserPortrait(userId)`，展示 RFM 段（badge）+ 生命周期阶段 + R/F/M 原始值 + 首末单时间，与既有「基本信息/订单消费/积分券」面板并列。
- [x] **Add:** `@BizQuery getSegmentMembers(segmentType, segmentValue, page, pageSize)` 分群成员列表 API（契约见 Decision C）+ `segment.page.yaml` 在既有手工标签分群基础上，增加按 RFM 段/生命周期阶段圈选的算法化分群维度（调用 `getSegmentMembers`，运营可从算法化段下钻到用户列表）。
  - **执行补记：** `segment.page.yaml` 改为三 Tab 并存（手工标签 / 按 RFM 段[下拉 8 段] / 按生命周期[下拉 4 阶段]）；后两 Tab 调 `getSegmentMembers` + 表格分页 + footer pagination/statistics。
- [x] **Proof:** `IGraphQLEngine` 测试 `getSegmentMembers`（按 RFM 段/生命周期阶段过滤 + 分页 + 与 `getUserPortrait` all-time 口径一致）；前端页编译通过（`./mvnw -pl app-mall-web -DskipTests compile`）。
  - **执行补记：** `TestLitemallUserPortraitBizModel` Phase 2 部分 4 测试全绿；`./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS；全 workspace `./mvnw test` 360 测试全绿；`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS。

Exit Criteria:

- [x] 用户详情页展示算法画像（RFM 段 + 生命周期 + R/F/M），无消费用户合理展示
- [x] 分群页支持算法化维度圈选（手工标签 + 算法化并存），`getSegmentMembers` 契约明确
- [x] **API 测试：** `getSegmentMembers` 通过 `IGraphQLEngine` 测试；前端编译通过
- [x] owner doc 更新（`system-configuration.md` per-user 画像口径 + `user-and-address.md` 用户详情画像/算法化分群）
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed（Round 3 + Round 4 连续两轮 consensus → 达成共识；非 Protected，已翻 `active`）
- Auditor / Agent: Round 1 `ses_0f23ae6dbffePTu2yaHbhFGUQC` / Round 2 `ses_0f2301b35ffeWngiD1X4KOfnX2` / Round 3 `ses_0f227dcbbffe4Rp7P3nmp74NrG` / Round 4 `ses_0f223789fffeNiZ2bsf9FEWpeC`（均 fresh session，非计划作者）
- Evidence:
  - Round 1 verdict `revise`（0 blocker + 3 major + 5 minor）→ MO-1/2/3 + MN-1..5 全部 FIXED。
  - Round 2 verdict `revise`（0 blocker + 1 major + 2 minor）→ MAJOR-1（all-time vs period 口径）+ MINOR-1/2 全部 FIXED。
  - Round 3 verdict `consensus`（0 blocker + 0 major + 1 trivial minor NEW-1：`classifyLifecycleStage` 的 inPeriod/firstInPeriod 派生说明）→ NEW-1 已修（Decision B 补一句派生定义）。
  - Round 4 verdict `consensus`（0 blocker + 0 major，NEW-1 FIXED）→ 两轮连续 clean，达成共识。
  - 核心修订：lifecycle 分类为内联（非零拷贝）已纠正 + 抽取重构项；`getSegmentMembers` 命名+契约+Decision C；全量分类成本 Deferred；all-time 口径抉择取代「口径完全一致」主张，Proof 改为分类逻辑同源 + P19 零回归；单一 Plan Audit 段。
  - 已核实准确：getUserRfm/getUserLifecycle/getUserRetention 聚合返回、labelRfm private static(`:1357`)、lifecycle 内联(`:1393-1411`)、orderMapper per-user 汇总查询(period `:313`/all-time `:333`)、user-detail.page.yaml 无画像、getUserWorkbenchSummary、segment 页路径、触发条件 P19 done 已满足、owner doc 缺口、无 anti-slacking、非 Protected（可 active）、模板合规、Required Skill 完整（含 nop-testing）。

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs are aligned（`system-configuration.md`/`user-and-address.md`）
- [x] verification has run（`./mvnw clean package -DskipTests` + `./mvnw test -pl app-mall-service -am` + `./mvnw -pl app-mall-web -DskipTests compile`）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed, and Nop-platform phases do not write `none` without justification
- [x] skill loading verification: each phase scanned available skills, loaded all matching skills, read ALL mandatory docs（路径列于 skill loading gate），selfcheck 无 anti-pattern
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### RFM 阈值缓存（大规模用户性能）

- Classification: `optimization candidate`
- Why Not Blocking Closure: Decision B 抉择 `getUserPortrait` 触发全量中位数阈值计算，基线用户量下可接受；阈值随订单变化，缓存需失效策略。
- Successor Required: `yes`（触发条件：用户量/订单量使单次全量阈值计算 > 1s，或画像查询成为热点时，引入带失效策略的阈值缓存）

### 算法化分群成员列表的物化（全量分类成本）

- Classification: `optimization candidate`
- Why Not Blocking Closure: Decision C 抉择 `getSegmentMembers` 每次调用对全量用户逐个分类后过滤，运营高频操作下随用户量增长。基线用户量下 Java 端全量分类可接受；规模化需预计算物化段表（ORM ask-first + 失效策略），超出本非 Protected 计划范围。
- Successor Required: `yes`（触发条件：用户量使 `getSegmentMembers` 单次响应 > 2s，或运营高频圈选成为热点时，引入分段物化表 + 失效策略，另立含 ORM 的 successor 计划）

### 画像落库与历史快照

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划画像为只读实时计算视图（不落库），满足运营即时查看；历史画像快照（追踪用户段变迁）需新实体（ORM ask-first），超出 successor 范围。
- Successor Required: `yes`（触发条件：业务要求追踪用户 RFM/生命周期段历史变迁时）

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: Phase 1 + Phase 2 全部 Exit Criteria 通过 live repo 实读核验；in-scope 行为（getUserPortrait / getSegmentMembers 实现 + user-detail / segment 前端接线 + 重构后的 P19 同源私有方法 + IGraphQLEngine 测试）已在代码中真实落地；owner doc（system-configuration.md / user-and-address.md）与 docs/logs/2026/06-28.md 同步；Deferred 项均为 optimization candidate / out-of-scope improvement（非 live defect 降级），触发条件已写明；非 Protected（无 ORM 改动）。

Closure Audit Evidence:

- Reviewer / Agent: independent closure auditor（fresh session，非实现 agent；本次会话 task 由 mission-driver `plan-check --strict` 触发的 closure audit step 派发）
- Audit method: 实读 live repo 核验，不信 plan 自报 [x]
- Exit Criteria vs live repo:
  - `@BizQuery getUserPortrait(userId)` 实现：`app-mall-service/.../entity/LitemallOrderBizModel.java:1606`（all-time 口径，无消费用户返回未消费画像，空 userId 抛 `ERR_USER_NOT_FOUND`）✓
  - `@BizQuery getSegmentMembers(segmentType, segmentValue, page, pageSize)` 实现：`LitemallOrderBizModel.java:1652`（`PageBean<SegmentMemberBean>` 返回，无效 segmentType/空 value 抛 ErrorCode，默认 page=1/pageSize=20、上限 500）✓
  - P19 同源重构私有方法存在：`computeRfmThresholds`(`:1344`)、`classifyRfmSegment`(`:1368`)、`classifyLifecycleStage`(`:1474`)、`RfmThresholds`(`:1381`)；`getUserRfm`/`getUserLifecycle`/`getUserPortrait`/`getSegmentMembers` 全部调用同一组私有方法（无逻辑分叉）✓
  - DTO：`UserPortraitBean` / `SegmentMemberBean`（`app-mall-dao/.../dto/`）存在 ✓
  - ErrorCode：`ERR_USER_PORTRAIT_INVALID_SEGMENT_TYPE/VALUE` + `ARG_SEGMENT_TYPE/VALUE`（已在 log + plan 记录）✓
  - 前端 `user-detail.page.yaml:28-52`：「算法画像（RFM + 生命周期）」service panel 调 `@query:LitemallOrder__getUserPortrait(userId)`，无消费展示「该用户暂无支付订单，仅有手工标签画像」✓
  - 前端 `segment.page.yaml`：三 Tab 并存（按标签 + 按 RFM 段 + 按生命周期），后两 Tab 调 `@query:LitemallOrder__getSegmentMembers`（`:76`/`:128`）✓
  - IGraphQLEngine 测试：`TestLitemallUserPortraitBizModel.java` 共 9 用例（Phase 1 部分 5 + Phase 2 部分 4），全部经 `graphQLEngine.executeRpc`（非实体级纯逻辑测试）✓
  - owner doc 同步：`docs/design/system-configuration.md:370-378`（Per-User 画像口径 P20 successor 段）、`docs/design/user-and-address.md:223-224`（算法化分群 + 用户详情算法画像）✓
  - `docs/logs/2026/06-28.md`：首条记录完整覆盖 Phase 1 + Phase 2 交付 + 验证命令全绿（`./mvnw -pl app-mall-service -am test` 360 全绿 / `./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS / `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS）✓
- Anti-Hollow 核验：两 `@BizQuery` 均为真实实现（无 `{}` / `return null` 占位）；前端 service/table 真实消费返回字段；测试断言 `status==0` + 字段值非空。
- Five-point consistency：Plan Status `completed` / Phase 1&2 Status `completed` / 两 Phase 所有 Exit Criteria `[x]` / Closure Gates 全 `[x]` / docs/logs 记录全绿 — 一致 ✓
- Deferred honesty：3 项（阈值缓存 / 分段物化表 / 历史快照落库）均为 `optimization candidate` 或 `out-of-scope improvement`，均非 in-scope live defect 或 contract drift 降级，触发条件均已写明 ✓
- Docs sync：`docs/logs/2026/06-28.md` + `docs/design/system-configuration.md` + `docs/design/user-and-address.md` 均已更新 ✓
- Verdict: **PASS** — 计划可关闭。

Follow-up:

- RFM 阈值缓存（触发条件：计算成为性能热点）。
- 算法化分群成员物化（触发条件：全量分类响应 > 2s）。
- 画像历史快照落库（触发条件：需追踪段变迁）。
