# 2026-06-16-1800 项目治理可信度修复计划（流程/文档/测试）

> Plan Status: planned
> Last Reviewed: 2026-06-16
> Source: `docs/audits/2026-06-16-1744-multi-dim-audit-full-project/summary.md`（P1 治理类发现，items 12-15 + 测试基线）
> Related: 维度 01/10/12/13 审计文件；`docs/plans/2026-06-16-1800-p0-fundamental-integrity-fix-plan.md`（资损路径测试依赖其完成）
> Audit: required

## Why One Plan

审计的治理类 P1 发现共享同一结果表面——"项目治理可信度"：闭合审计回补、技能真值源统一、context 文档批量漂移、Phase 11/13 状态归真、测试基线，都是"使 docs/process/test 与 live reality 一致，恢复'done'状态作为后续开发依赖前提的可信度"。计划指南 Rule #4"one plan, one result surface"的严格读法要求单一闭合准则；本计划 5 个 Phase 形式上有独立准则，但它们由审计识别为同一系统性问题（"done"可信度，见 summary.md 三大系统性问题之三），且部分 Phase（Phase 3 文档刷新、Phase 5 测试基线）共享 Plan 1/2 前置 + 同一交付物类型（文档/流程/测试配置）。若按"代码 vs 治理"拆，Phase 1（闭合审计回补，含补测试）+ Phase 5（测试基线，含 e2e 业务流，是真实代码工作）属 full plan；Phase 2/3/4（纯文档/流程）可走 brief——但计划指南 Decision Table 把"stale-doc conflict"明确归 Full plan（Phase 3 触及最高频 context 文档，错误内容已导致 AI 误路由），且分散为 brief 会丢失"治理可信度"的统一闭合视角。故保留单一 full plan；若 Phase 1 经 baseline 修订后代码工作量收窄（见下），可在执行期评估是否把 Phase 1 折入 Phase 5。资损路径测试补强依赖 Plan 1 完成，文档刷新依赖 Plan 1/2 的 owner-doc 变更落地，故排第三。

## Current Baseline

> 经审计核验（见维度 01/10/12/13）。

**流程合规（维度12）**
- Phase 9/11 closure：`docs/plans/2026-06-13-next-phase-plan.md:493` 勾选"[x] closure audit was independent"，但 :542 实际"Reviewer / Agent: main session execution"——闭合门虚假勾选（12-3）
- Phase 12/13 closure：`docs/plans/2026-06-13-next-phase-notification-report-wxpay-plan.md:483` 勾选"IGraphQLEngine 测试通过"，:560 自认"new methods lack IGraphQLEngine snapshot tests... continuous improvement, not a blocker"——不可降级项被降级（12-4）
- 技能真值源分裂：`docs/skills/README.md:26-56` 仅 12 audit prompt，`.opencode/skills/` **12 个**实现技能目录零交集；AGENTS.md 声明 available skills list 是 single source of truth（12-1）
- nop-nodejs-backend 残留：`.opencode/skills/nop-nodejs-backend/SKILL.md` NestJS+Prisma+SQLite，与 Java/Nop 项目完全不匹配（12-2）
- 技能 gate 占位符：next-phase-plan（9 处）、next-phase-notification-report-wxpay-plan（7 处）"Docs read: Read per skill routing table (see dev log)"（12-5）

**Phase 12/13 测试覆盖 live 核验（修订 Phase 1 baseline 关键）**
- 经 live 核验（2026-06-16）：`sendResetCode`/`resetPassword` 已在 `TestPasswordReset.java:46,57,70,80,93` 通过 `LoginApi__sendResetCode`/`LoginApi__resetPassword` GraphQL 调用测试；`signUp`（注册赠券 hook 宿主）已在 `TestLoginApiSignUp.java:40,69,84,97,110` 测试；3 个统计方法已在 `TestLitemallOrderStatisticsBizModel.java:33-64` 通过 IGraphQLEngine.newRpcContext + executeRpc 测试（方法位于 `LitemallOrderBizModel:469-508`，**`LitemallStatBizModel` 类不存在**）
- 真实未覆盖面：`MallNotificationService`（全测试目录 grep `*Notification*` 0 命中）
- 故 12-4 的真实缺口收窄为：(a) MallNotificationService 零测试；(b) 上述已有测试是 `JunitBaseTestCase`+手动 RPC 形式，若 plan guide Rule #15 严格要求 `JunitAutoTestCase` 录制回放快照形式则需转换

**文档漂移（维度13）**
- module-boundaries.md：L84-85 "No E2E framework configured"虚假；L84 集成测试位置"app-mall-app/src/test/"不存在；L91-96 "31 索引已同步 DDL"虚假（13-01/02/03，后者 Plan 1 修）
- project-context.md:47 "E2E / integration tests: none"与 e2e/ 目录矛盾（13-04）
- codebase-map.md：全表 Last Verified 2026-06-02 过期 14 天；Entry Points 缺 e2e/wx/delta/meta（13-05/06/07）
- roadmap：Current Baseline "4 测试类"（实际 31+e2e 38）；"核心缺口"与 Phase Status 全 done 矛盾；:68 "extAction1 查询"引用已删除 delta action；Entity Coverage 缺 LitemallResetCode + "35 实体"计数过期（13-08/09/10/11）

**Phase 11/13 状态虚标（维度01）**
- Phase 11 定时任务：无调度器装配（nop-job 未引入），5 个任务仅手动入口（01-1）
- Phase 13 报表：无 nop-report/看板/模板，只交付 SQL 数据集（01-2）

**测试基线（维度10）**
- known-good-baselines.md 占位符，与日志 106/38 passed 矛盾（10-5）
- project-context.md "E2E:none"（10-6）
- e2e 仅冒烟（10-7）、TestIBizNewEntity 保护力零（10-8）、资损路径错误测试缺失（10-9，依赖 Plan 1）

## Goals

1. **闭合审计回补**：Phase 9/11 补独立 subagent closure audit；Phase 12/13 补 IGraphQLEngine 测试后重勾闭合门（12-3/4）
2. **技能真值源统一**：`.opencode/skills/` 并入 `docs/skills/README.md` 注册表；删除 nop-nodejs-backend；2 计划技能 gate 占位符补真实文档路径（12-1/2/5）
3. **context 文档刷新**：project-context（E2E 命令）、codebase-map（Last Verified+入口）、module-boundaries（E2E/测试位置）与 live 一致（13-01/02/04/05/06/07）
4. **roadmap 归真**：Current Baseline 重写；Entity Coverage 补 ResetCode + 计数；删除 extAction1 僵尸引用（13-08/10/11）；Phase 11/13 按"状态归真 Decision"处理（01-1/2/3）
5. **测试基线落地**：known-good-baselines.md 回写绿基线（10-5）；e2e 补至少 1 个业务流（加购→下单→支付）（10-7）

## Non-Goals

- **P0/P1 代码缺陷修复**：归 Plan 1/2；本计划只在 Plan 1/2 完成后回写文档/测试基线
- **module-boundaries.md DDL 索引声明**：Plan 1 Phase 5 修（与本计划 module-boundaries 其他刷新可合并执行）
- **owner docs 业务语义对齐**（order-and-cart 状态机等）：归 Plan 1/2 的 owner-doc update 项
- **nop-job/nop-report 的完整功能交付**：若 Decision 选"补全"，交付本身是独立大工作，本计划只做"状态归真 Decision + roadmap 标记"，完整交付另立计划
- **Phase 4B 偏离触及保护区域未重审（12-6，P2）**：属保护区域治理，但本计划聚焦 P1 治理；P2 的 12-6 不纳入，留待保护区域专项审查
- **delta picUrl 测试字段清理（13-12，P1）**：属模型层清理（需 ORM 变更 + codegen），归模型相关计划承接，非本计划文档治理范围

## Task Route

- Type: `verification or audit work` + `implementation-only change`（文档/配置/测试为主，Phase 11/13 涉及依赖引入 Decision）
- Owner Docs: `AGENTS.md`、`docs/index.md`、`docs/context/*`、`docs/architecture/module-boundaries.md`、`docs/backlog/implementation-roadmap.md`、`docs/skills/README.md`、`docs/plans/00-plan-authoring-and-execution-guide.md`、`docs/audits/00-audit-execution-guide.md`
- Skill Selection Basis: 本计划以文档/流程/测试为主，无 BizModel/page 代码；Phase 11/13 Decision 若选补全则涉及 `nop-backend-dev`，但本计划范围内只做 Decision + 标记

## Infrastructure And Config Prereqs

- Plan 1/2 闭合（资损路径修复 + owner-doc 变更落地）——本计划的文档刷新与测试基线依赖其结果
- No new infra beyond existing baseline

## Execution Plan

### Phase 1 — 闭合审计回补

Status: planned
Targets: `docs/plans/2026-06-13-next-phase-plan.md`、`docs/plans/2026-06-13-next-phase-notification-report-wxpay-plan.md`、`app-mall-service/src/test/.../Test*Notification*.java`（新增）
Required Skill: `nop-testing`

- Item Types: `Fix | Add | Proof`
- Prereqs: Phase 12/13 多数方法已有 RPC 测试（见 Baseline live 核验），可独立推进；MallNotificationService 测试无跨计划依赖。**取消原"Plan 1/2 闭合"前置**（多数目标不在 Plan 1/2 编辑面）

- [ ] **Skill Loading Gate:** 加载 `nop-testing`，读 testing/e2e-testing 必读，列路径。
- [ ] **Fix: Phase 9/11 独立 closure audit（12-3）。** 派发独立 subagent 对 `2026-06-13-next-phase-plan.md` 做真实 closure audit（团购/定时任务/通知 live 核验），修正 :493 虚假"independent"勾选；若发现缺口，记录或重开
- [ ] **Fix: Phase 12/13 闭合门如实重勾（12-4，基于 live 核验收窄范围）。** 不再笼统称"补全部 IGraphQLEngine 测试"——sendResetCode/resetPassword/signUp/3 统计方法已有 RPC 测试（见 Baseline）。本 item 收窄为：(a) 为 `MallNotificationService` 新增测试（真实零覆盖，见下 item）；(b) plan guide Rule #15 明确要求 `JunitAutoTestCase` 录制回放形式，现有 6 方法测试是 `JunitBaseTestCase`+手动 RPC 形式不满足——二选一承诺：(i) 转换为 `JunitAutoTestCase` 录制回放形式，或 (ii) 在 next-phase-notification-report-wxpay-plan.md:483 显式标"闭合门当时虚假勾选，现 5/6 方法已补 RPC 形式测试，JunitAutoTestCase 形式留作后续"
- [ ] **Add: MallNotificationService 测试（12-4 真实缺口）。** `MallNotificationService` 是普通 `@Named` 服务（非 @BizModel，方法不暴露 GraphQL），故测试模式为**直接 `@Inject MallNotificationService` + 注入 mock ISmsSender**，覆盖各分支（null sender 跳过 / 正常发送 / 异常吞掉且仅 LOG.error），不走 IGraphQLEngine 或 I*Biz 接口
- [ ] **Proof: 独立 closure audit 报告（task id）+ 新增测试绿。**

Exit Criteria:
- [ ] Phase 9/11 有独立 subagent closure audit 证据（12-3 闭环）
- [ ] Phase 12/13 闭合门如实勾选：MallNotificationService 有测试；已有 RPC 测试如实标注形式（12-4 闭环）
- [ ] `docs/logs/` updated

### Phase 2 — 技能真值源统一

Status: planned
Targets: `docs/skills/README.md`、`.opencode/skills/nop-nodejs-backend/`、2 计划文件技能 gate
Required Skill: none（文档治理）

- Item Types: `Fix | Add`
- Prereqs: 无

- [ ] **Fix: 统一技能真值源（12-1）。** `docs/skills/README.md` 注册表并入 `.opencode/skills/` **12 个**实现技能（或反向：README 顶部声明实现技能权威位置为 .opencode/skills 并交叉引用），使"available skills list"单一真值源
- [ ] **Fix: 删除 nop-nodejs-backend（12-2）。** 移除 `.opencode/skills/nop-nodejs-backend/`（或标注 deprecated not-applicable to Java/Nop）
- [ ] **Fix: 2 计划技能 gate 占位符补真实路径（12-5）。** `2026-06-13-next-phase-plan.md`（9 处）、`2026-06-13-next-phase-notification-report-wxpay-plan.md`（7 处）"Docs read"补列真实文档路径
- [ ] **Fix: 闭合门核验基准（12-8）。** 与 12-1 联动，闭合门"Required Skill 存在性"可核验
- [ ] **Proof: README 与 .opencode/skills 一致；grep 核验无占位符残留。**

Exit Criteria:
- [ ] 技能真值源单一（12-1 闭环）
- [ ] nop-nodejs-backend 移除（12-2 闭环）
- [ ] 2 计划技能 gate 列真实路径（12-5 闭环）
- [ ] `docs/logs/` updated

### Phase 3 — context 文档刷新

Status: planned
Targets: `docs/context/project-context.md`、`docs/context/codebase-map.md`、`docs/architecture/module-boundaries.md`、`docs/backlog/implementation-roadmap.md`
Required Skill: none（文档治理）

- Item Types: `Fix | Add`
- Prereqs: Plan 1/2 闭合（owner-doc 变更落地后刷新引用）

- [ ] **Fix: project-context.md 验证命令表（13-04/14）。** L47 "E2E:none" 改真实命令（`cd e2e && npx playwright test`）；Unit tests 行补规模注记
- [ ] **Fix: codebase-map.md 全表刷新（13-05/06/07）。** Entry Points 补 e2e/、app-mall-wx/、app-mall-delta/、app-mall-meta/；全表 Last Verified 更新为本次核验日；Tests 行补 e2e 注记
- [ ] **Fix: module-boundaries.md Test Ownership（13-01/02）。** L84-85 集成测试位置改 app-mall-service/src/test；新增 e2e/（Playwright）条目；删除"No E2E framework configured"
- [ ] **Proof: 文档引用路径逐一存在性核验。**

Exit Criteria:
- [ ] project-context/codebase-map/module-boundaries 与 live 一致（13-01/02/04/05/06/07 闭环）
- [ ] `docs/logs/` updated

### Phase 4 — roadmap 归真（含 Phase 11/13 状态 Decision）

Status: planned
Targets: `docs/backlog/implementation-roadmap.md`
Required Skill: none（决策 + 文档；若 Decision 选补全则另立计划）

- Item Types: `Fix | Decision`
- Prereqs: 无

- [ ] **Fix: Current Baseline 重写（13-09）。** 反映 14 阶段全 done 后真实基线（storefront 25 页、微信支付、31 测试类、e2e 38、通知/报表/团购落地），删除"核心缺口"段矛盾
- [ ] **Fix: 测试计数（13-08）。** "4 测试类"改"31 Java 测试类 + 38 e2e 用例"
- [ ] **Fix: 删除 extAction1 僵尸引用（13-10）。** :68 改反映 delta 真实能力（signUp/profile/密码重置/注册赠券）
- [ ] **Fix: Entity Coverage 补 ResetCode + 计数（13-11）。** 补 LitemallResetCode；"35 实体"改 live 准确数（32 活跃，注明含已消除口径）
- [ ] **Decision — Phase 11/13 状态归真（01-1/2/3）：** 选项 A：真正补全（引入 nop-job 装配调度、引入 nop-report+看板）——属独立大工作，本计划只标记并另立后续计划；选项 B：roadmap 改 partial 并在 Phase Details 显式记录未完成项。推荐 B（快速归真，补全另行排期）。**Governance note：选 B 会把 Phase 11/13 从 `done` 回退为 `partial`，属状态回归——需同步在原 plan（next-phase-plan / next-phase-notification-report-wxpay-plan）的 Closure 节加注"状态因审计归真回退至 partial"，并确认不违反原 closure 的不可降级项（原 closure 的不可降级项指功能正确性，非调度装配/看板完整性）。** 记录 alternatives + 残留风险。若选 A，本计划范围内只做 roadmap 标记，不交付功能
- [ ] **Proof: roadmap 自洽（Phase Status / Current Baseline / Entity Coverage 三段一致）。**

Exit Criteria:
- [ ] roadmap 三段自洽无矛盾（13-08/09/10/11 闭环）
- [ ] Phase 11/13 状态按 Decision 归真（01-1/2/3 闭环或转后续计划）
- [ ] `docs/logs/` updated

### Phase 5 — 测试基线落地

Status: planned
Targets: `docs/testing/known-good-baselines.md`、`e2e/tests/`（新增业务流）、`app-mall-service/src/test/.../TestIBizNewEntity.java`
Required Skill: `nop-testing`

- Item Types: `Add | Fix | Proof`
- Prereqs: Plan 1/2 闭合（资损路径修复后建基线）

- [ ] **Skill Loading Gate:** 加载 `nop-testing`，读 e2e-testing/testing 必读，列路径。
- [ ] **Add: 回写 known-good-baselines.md（10-5）。** 表格补本次绿基线行（日期/git sha/scope/commands passed：mvn test 通过数 + e2e 38 + package）
- [ ] **Fix: TestIBizNewEntity 改业务断言或删除（10-8）。** 改断言默认 status 等业务属性，或删除虚胖测试
- [ ] **Add: e2e 至少 1 个业务流（10-7）。** 新增登录→浏览商品→加购→submit→模拟支付→查订单 的 happy-path e2e
- [ ] **Proof: `./mvnw test` + e2e 全绿；known-good-baselines 非占位。**

Exit Criteria:
- [ ] known-good-baselines.md 真实维护（10-5 闭环）
- [ ] e2e 有至少 1 个业务流（10-7 缓解）
- [ ] TestIBizNewEntity 不再虚胖（10-8 闭环）
- [ ] `docs/logs/` updated

### Phase Final — 验证与文档

Status: planned
Targets: 全局
Required Skill: none

- [ ] **Proof: 文档一致性全量复核。** project-context/codebase-map/module-boundaries/roadmap/skills README 引用路径与 live 一致
- [ ] **Proof: `./mvnw test` + e2e 全绿。**
- [ ] **Add: dev log。**

Exit Criteria:
- [ ] 治理类 P1 全部闭环
- [ ] 全绿
- [ ] owner docs 一致
- [ ] `docs/logs/` updated

## Plan Audit

- Status: passed (consensus: 2 轮审计，Round 2 连续 clean，0 blockers，0 majors，残留 5 minors 非阻塞已清理主要 3 项)
- Round 1 Reviewer / Agent: independent subagent (ses_12f21bc08ffe...)；Verdict REVISE（M1 Phase1 baseline 事实错误：LitemallStatBizModel 幻影类名 + 多数方法已有 RPC 测试；M2 Plan 宽度 vs Rule #4 + 6 minors）→ Phase 1 baseline 基于 live 核验重写（收窄到 MallNotificationService + snapshot 形式问题）、Why One Plan 加固、12→12、Closure Gates 加声明、Non-Goals 12-6/13-12 in/out
- Round 2 Reviewer / Agent: independent subagent (ses_12f12745cffewOw8D3FykoQQ9A)；Verdict PASS（M1/M2 + 6 minors 全部 RESOLVED；5 新 minors N1-N5：Why One Plan "共享前置"措辞、Phase1 测试模式瑕疵、Item Types 一致性、anti-slacking trigger——均为文字一致性/精度问题，非阻塞）→ 已清理 N1（共享前置→部分共享）、N2（删虚假分叉）、N3（MallNotificationService 改直接 @Inject 测试模式）
- Evidence: Phase 1 baseline live 核验（TestPasswordReset/TestLoginApiSignUp/TestLitemallOrderStatisticsBizModel 已覆盖、统计方法在 LitemallOrderBizModel:469-508、MallNotificationService 零测试、.opencode/skills 12 个）；治理类文档 baseline 逐条 live 命中

> 注：本计划不改产品行为（Phase 1/5 仅补测试），无 Protected Area 项；Phase 4 Decision 选 B 致 Phase 11/13 状态 done→partial 回归，已在 Decision 内记录 governance note（原 plan Closure 节加注）。

## Closure Gates

- [ ] in-scope 治理项 complete（闭合审计/技能真值源/文档/roadmap/测试基线）
- [ ] relevant docs aligned（本计划产出即文档对齐）
- [ ] verification: 文档一致性复核 + mvnw test + e2e 全绿
- [ ] no in-scope item downgraded to deferred/follow-up（闭合门虚假属不可降级，必须真补）
- [ ] **本计划不新增 `@BizMutation`/`@BizQuery` 方法**（Phase 1/5 仅补测试，不改产品行为）；故标准 IGraphQLEngine 闭合门按"不适用"处理并显式声明
- [ ] plan audit passed
- [ ] each phase has Required Skill listed（文档治理 phase 写 none 有正当理由：无 BizModel/page 代码）
- [ ] skill loading verification + selfcheck（适用处）
- [ ] text consistency verified
- [ ] closure audit was independent
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### Phase 11/13 功能完整补全（nop-job 调度装配 + nop-report 看板）
- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划范围内只做"状态归真 Decision + roadmap 标记"；真正补全是独立大工作
- Successor Required: `yes`（触发条件：Phase 4 Decision 选 B 后，按运营优先级另立补全计划）

### e2e 业务流全覆盖（10-7 进一步）
- Classification: `optimization candidate`
- Why Not Blocking Closure: 本计划只补 1 个 happy-path 业务流缓解"仅冒烟"；退款/售后/团购等业务流 e2e 是持续改进
- Successor Required: `yes`（触发条件：核心流程稳定后逐步补全）

### 资损路径错误路径测试全覆盖（10-9）
- Classification: `watch-only residual`
- Why Not Blocking Closure: Plan 1 已补资损路径核心测试；Order/Aftersale/Pay/WxPay 错误路径的全面覆盖是持续改进
- Successor Required: `yes`（触发条件：Plan 1 闭合后评估覆盖率）

## Closure

Status Note: <待 implementation + closure audit 后填写>
