# Plan Authoring And Execution Guide

## Goal

`docs/plans/` is for non-trivial execution slices that need explicit scope, closure criteria, and proof.

## When To Write A Plan

Write a plan when the task:

- changes API, database/model, auth, integration, deployment, or public contract behavior
- changes user-visible behavior across more than one feature surface
- touches multiple modules and changes shared behavior
- is expected to take more than one AI session
- modifies more than 5 total files or is likely to exceed roughly 200 changed lines
- needs staged implementation or explicit proof before closure

Skip a formal plan only for local low-risk edits such as copy changes, small styling fixes, test-only cleanups, and single-file behavior fixes with clear existing tests.

## Analysis / Audit Path

Not every non-trivial task needs a full execution plan.

For docs-only research, analysis, audit, or review work, do **not** create a separate plan by default.

Prefer the no-plan path when all of the following are true:

- the task does not change code, model, SQL, config, auth behavior, or supported product behavior
- the main deliverable is an analysis, audit, review, or recommendation document
- closure depends primarily on the quality of the output artifact, not on staged implementation work
- there is no protected-area change, migration, or multi-surface behavioral rollout hidden inside the task

For this path, lightweight tracking via the agent's built-in todo list is usually enough.

Create a lightweight `analysis / audit brief` only when durable coordination still matters, such as:

- multi-session analysis work
- multiple output artifacts that must close together
- disputed scope or source-of-truth conflicts
- expected reviewer handoff where the output document alone is not enough context

When needed, the brief may live either as:

- a short section at the top of the output document, or
- a lightweight dated plan file when durable tracking is still useful

Minimum contents for a brief:

- goal
- scope
- source-of-truth and precedence basis
- expected output files
- review method

For this path, prefer spending review effort on the final analysis/audit artifact rather than repeatedly refining the brief itself.

## Plan Decision Table

| Scope                                                                                                                               | Plan Level | Audit Rule                                                    | Examples                                                                               |
| ----------------------------------------------------------------------------------------------------------------------------------- | ---------- | ------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| Trivial local edit                                                                                                                  | No plan    | No plan audit                                                 | typo/copy change, single style tweak, test-only cleanup                                |
| Docs-only analysis / audit / review with no behavior change                                                                        | No separate plan by default; brief only when coordination complexity justifies it | if a brief exists, do one lightweight sanity-check review of the brief; repeated adversarial review should focus on the output artifact | design completeness assessment, architecture comparison, document audit, backlog research note |
| Non-trivial tracked work                                                                                                            | Full plan  | independent plan audit and independent closure audit required | small UI polish with docs/test update, simple local bug fix with clear existing test   |
| Contract, data/model, API, auth, permission, integration, deployment, cross-surface, stale-doc conflict, or clearly high-risk scope | Full plan  | independent plan audit and independent closure audit required | checkout flow, login behavior, data migration, external webhook, multi-module refactor |

If unsure, use a full plan. If the task is clearly docs-only and the real risk is weak analysis rather than weak execution sequencing, prefer the no-plan path or a lightweight brief instead.

## Minimum Rules

1. **Start from live baseline.** Read the repo first, then write `Current Baseline`. Do not rely on memory or old plans. For net-new features, the baseline must inventory all existing code the feature will touch or contradict — hardcoded values, missing hooks, incompatible patterns. An inventory is not optional.
2. **Write Goals and Non-Goals.** If either is unclear, the plan boundary is not ready.
3. **Use checkboxes for execution and closure.** Unchecked items mean unfinished work until closure.
4. **One plan, one result surface.** If the plan needs multiple independent closure criteria, it is too wide. Split it. Multi-module extraction or migration that shares the same behavioral contract and closure criteria is still ONE result surface — do not over-split.
5. **Proof before closure.** Do not mark a plan complete until the repo contains verifiable proof for every exit criterion.
6. **No code-design dumps.** The plan captures scope, proof, and closure logic, not low-level implementation detail. Exception: refactoring and extraction plans MUST include the interface contracts between extracted modules — these are structural boundary definitions, not implementation pseudocode.
7. **Tag items with types.** Each execution item must be `Fix`, `Add`, `Decision`, `Proof`, or `Follow-up`. `Fix` covers defect repairs; `Add` covers net-new code or config. An item may carry multiple types (e.g., `Decision | Add`); when it does, all implied obligations apply. A confirmed live defect or contract drift must be `Fix`, not `Follow-up`. When 80%+ of items in a phase share one type, declare the uniform type at the phase level instead of per-item (e.g., `Phase 1 — Fix-heavy (8/10 items tagged Fix)`).
8. **Record skill usage deliberately.** For each phase or item where a reusable skill matters, record `Skill: <name>` or `Skill: none`. Skills choose the work method, not the business truth. If a skill is named, its required inputs and expected output must already be clear from `docs/skills/README.md` and the referenced owner docs.
9. **Record Decisions with rationale.** Every `Decision` item must document the choice, the alternatives considered, and the residual risk if any. Write the rationale into the plan or a referenced doc. If a decision requires prototyping or exploration before committing, add a temporary `Explore` item that must conclude before the `Decision` resolves. Framework-forced or obvious choices (e.g., "must match existing framework pattern") can be noted as constrained without full alternatives analysis.
10. **Checklist integrity before closure.** Before marking a plan complete, no in-scope checklist item may remain unchecked. Either complete it or explicitly move it out of scope with a written reason. Scope narrowing after plan approval is a scope change and must be recorded with rationale; silently removing items from scope is a violation.
11. **Text consistency before closure.** Before closing, verify that `Plan Status`, every phase `Status`, every phase `Exit Criteria`, `Closure Gates`, and the `docs/logs/` entry all agree. No `completed` at the top while a phase inside still says `planned`.
12. **Independent plan and closure audit.** Do not implement a created plan until it has passed plan audit, and do not mark it complete as a side effect of finishing the last implementation slice. Use a separate review pass. Protected areas, unresolved product risk, and source-of-truth conflicts require human/subagent review or stay open.
    - **Mandatory auto-trigger.** After writing or substantially revising a plan, the agent MUST immediately launch an independent subagent for adversarial plan audit. Do not wait for user prompting, do not ask whether to audit, and do not treat plan creation as finished until the audit completes.
    - **Independent subagent adversarial review.** The plan audit must use an independent subagent (not the same session/context that wrote the plan). The reviewer must challenge the plan against live repo evidence, not trust the plan's own claims. Audit scope includes: baseline accuracy, goal clarity, dependency ordering, missing considerations, protected-area compliance, Reference Docs completeness, and anti-slacking compliance.
    - **Consensus before implementation.** If the audit finds blockers or major objections, revise the plan and re-audit. Repeat until the independent reviewer reports no blocker and no major objection. Two consecutive clean audit rounds after the latest substantive revision constitute consensus. Only then may implementation begin.
    - Record normal plan-audit and closure-audit evidence inside the plan by default.
    - Do not create `docs/audits/` files for ordinary plan-audit or closure-audit failures; revise the plan or work and audit again.
    - Use `docs/audits/` only for specialized, complex, disputed, reusable, or future-replay-worthy audit records.
    - For docs-only `analysis / audit` work, one sanity-check review of the brief is usually enough when a brief exists at all.
    - For docs-only `analysis / audit` work, repeated adversarial review should target the output artifact, not the planning artifact.
13. **Non-degradable items** cannot be downgraded to non-blocking follow-ups: confirmed live defects, confirmed contract drift, confirmed owner-doc drift, and CI/lint rules already fixed in the repo.
14. **Every phase must list `Required Skill`.** 根据 AGENTS.md 的 Mandatory Skill Loading 规则，扫描 available skills 列表，找到 description 或触发词覆盖本 phase 工作内容的 skill。执行时必须加载匹配的 skill 并读完其路由的文档后才能写代码（见 When Executing #6）。Nop 平台相关 phase 写 `none` 会 fail plan audit。如果多个 skill 匹配，全部列出。
15. **BizModel 方法必须通过 API 层测试。** 所有 `@BizMutation` 和 `@BizQuery` 方法必须通过 `IGraphQLEngine`（`JunitAutoTestCase` 录制回放）测试，不能只写实体级纯逻辑测试。`@BizAction` 方法无法通过 GraphQL 调用，如果需要测试则通过注入 `I*XxxBiz` 接口直接调用。含有新增 `@BizQuery`/`@BizMutation`/`@BizAction` 方法的 phase，如果 available skills 中有测试相关的 skill，`Required Skill` 必须同时包含该 skill，测试项必须明确指定通过 `IGraphQLEngine` 还是 `I*XxxBiz` 接口测试。

### Anti-Slacking Rule

Every in-scope item before closure must land in exactly one state: `landed`, `adjudicated as residual-risk-only`, `moved to explicit successor ownership`, or `removed from scope with recorded reason`.

The following words are forbidden for in-scope items: `optional`, `if time permits`, `consider`, `maybe`, `nice to have`, `as needed`. If an item is truly optional, move it out of scope explicitly rather than leaving it in a fuzzy state.

A `Follow-up` item must name the trigger condition that would promote it into scope (e.g., "when user count exceeds 10K"). A `Deferred But Adjudicated` item must name the event or decision that would reopen it (e.g., "if the new API is adopted, this work may become redundant").

## When Executing

1. Before implementation, record plan audit evidence.
2. **Roadmap update (plan creation):** If this plan implements a roadmap phase in `docs/backlog/implementation-roadmap.md`, update the phase status from `todo` to `planned`. Do this when the plan passes its plan audit and implementation is about to begin.
3. When you start a slice, update its `Status` to `in progress`.
4. When you finish a slice, update its `Status` to `completed` and check off all its execution items and exit criteria.
6. **MANDATORY SKILL LOADING GATE: Before executing ANY phase, load every skill listed in `Required Skill` for that phase.** 见 Minimum Rules #14。此规则适用于所有 agent（主 agent、子 agent、审核 agent）。加载 skill 后读完其路由的文档再写代码。后端代码每写完一个方法用 selfcheck 校验；前端和测试代码在每个文件/类完成后校验。如果 phase 委托给 subagent，prompt 必须包含 `Required Skill` 名称和加载后读文档+自检的指令。
7. Confirm the listed `Skill` still matches the task and available inputs. If not, update the plan before proceeding.
8. If a slice changes the live baseline or public contract, its exit criteria must include the doc-update step. If no doc update is needed, write `No owner-doc update required` explicitly.
9. Do not mark a slice complete because the function signature exists. Verify that the behavior, error handling, and test coverage land too. **BizModel `@BizMutation`/`@BizQuery` 方法的测试必须通过 `IGraphQLEngine` 验证（`JunitAutoTestCase` 录制回放），不能只用实体级纯逻辑测试替代。`@BizAction` 方法需要测试时通过 `I*XxxBiz` 接口调用。**
10. If an item cannot be completed, move it to `Deferred But Adjudicated` with classification and reason. Do not leave it unchecked in the execution list.
11. **编码阶段发现模型问题时的处理规则：**
    - 编码时发现 ORM 模型缺少关系、字段或字典等，**不直接修改模型**（模型必须事先准备好）。
    - **非阻塞**：问题不影响业务正确性（如缺少一个 ORM 关系，可以用 `I*Biz` 替代），记入当前 plan 的 `Deferred But Adjudicated` 区，标注分类为 `model-gap`、缺失内容、建议的修复方式、以及触发条件（"下次修改此模型时补充"）。继续实现。
    - **阻塞**：问题影响业务正确性且没有合理的运行时替代路径（如缺少关键字段导致无法完成核心流程），停止当前实现，更新 plan 状态为 blocked，等待确认后再决定是补充模型还是调整范围。
    - **不阻塞时禁止的行为**：用 `daoProvider().daoFor()` 绕过模型缺失来"凑合实现"。模型缺失应该在模型层面修复，运行时代码应使用正确的路径（`I*Biz` 或 ORM 关系 getter）。
12. Keep `docs/logs/` in sync with plan progress. A single aggregate log entry at plan closure is sufficient when all phases cover the same feature in one sprint; individual phase entries are required only when a phase spans a different day or a distinct deliverable.

## When Closing

Before setting `Plan Status: completed`, do all of the following:

**All created plans:**

1. Check every phase `Exit Criteria` — every one must be `[x]`.
2. Check every `Closure Gates` item — every one must be `[x]`.
3. Verify text consistency: top status, phase statuses, exit criteria, closure gates, and log entry all agree.
4. Distinguish "interface exists" from "behavior is complete". Verify the actual runtime behavior with a test or demo, not just the type signature.
5. Run the real verification commands for the repo. For plans whose primary result surface is visual, behavioral, or UX-driven, customize the verification gates with explicit justification in the plan.
6. Perform an independent closure audit.

**Full closure** (multi-session, multi-module, or high-risk plans — add these):

7. Re-read the entire plan from the top, not just the most recent slice.
8. Record independent audit evidence in the plan's `Closure` section. Link a stored audit file only when the audit qualifies as a specialized, complex, disputed, reusable, or future-replay-worthy record.

If any of these fail, the plan stays open.

## When Closed

After `Plan Status: completed` has been set and the closure audit has passed:

1. **Roadmap update (closure):** If this plan implements a roadmap phase in `docs/backlog/implementation-roadmap.md`, update the phase status from `planned` to `done`. **Do this ONLY after the closure audit has passed — never before.** The roadmap must reflect only verified, audited completion, not implementation progress.
2. If the plan reveals new platform reuse opportunities, update the roadmap's `Nop Platform Reuse` section.
3. If the plan changes entity coverage (e.g., an entity was migrated to platform), update the roadmap's `Entity Coverage` section.

**Critical rule: Never update the roadmap phase status before the closure audit passes.** Premature roadmap updates create false confidence. If the closure audit finds issues, the plan reopens and the roadmap must still show `planned`.

## Notes For Analysis / Audit Work

- Do not inflate docs-only analysis work into implementation-style phases unless staged execution is genuinely the risk.
- Default to no separate plan; use the built-in todo mechanism for lightweight progress tracking unless durable coordination needs more.
- Closure should usually be based on output quality and durable review evidence, not on implementation-flavored checklists.
- For docs-only work, verification may consist of source review, artifact completeness, and independent audit evidence rather than build/test commands, as long as that proof is explicitly recorded.
- If the task later turns into real implementation work, promote the brief into a full plan instead of stretching the brief beyond its purpose.

## Template

> **文件名规范**: 遵守 `docs/references/document-naming-and-timeliness.md`，使用 `docs/plans/YYYY-MM-DD-topic-plan.md` 格式。不允许无日期前缀的纯主题名。

```md
# <plan-id> <title>

> Plan Status: planned
> Last Reviewed: YYYY-MM-DD
> Source: <requirement / bug / analysis / request>
> Related: <related plans, optional>
> Audit: required

## Current Baseline

- <what is true today>
- <what gap remains>

## Goals

- <result to achieve>

## Non-Goals

- <explicitly excluded work>

## Task Route

- Type: `<requirement clarification | app-layer design change | architecture change | implementation-only change | bug investigation | verification or audit work>`
- Owner Docs: `<paths>`
- Skill Selection Basis: `<why these skills or none apply>`

## Infrastructure And Config Prereqs

- <ports, env vars, CORS, secrets, .env, external services this feature depends on>
- <if none, write "No infra prereqs beyond existing baseline">
- <for data-migration plans: include rollback strategy or script path>

## Execution Plan

### Phase 1 - <name>

Status: planned
Targets: `<paths>`
Skill: `<skill-name | none>`
Required Skill: `<scan available skills, list all whose description/trigger matches this phase; none if no match>`

- Item Types: `Fix | Decision | Proof | Follow-up`
- Prereqs: <phases or external dependencies that must complete first>

- [ ] **Skill loading gate:** Scan available skills and load every skill listed in `Required Skill` above. Read all docs the skills route you to. After each method/class written, use the selfcheck mechanism provided by the loaded skill(s) to verify no anti-patterns.
  - Skill: `<skill-name | none>`
- [ ] <implementation item>
      - Skill: `<skill-name | none>`
- [ ] <Decision: record rationale and alternatives in the item or a referenced doc>
  - Skill: `<skill-name | none>`
- [ ] <Proof: specify test strategy (unit/integration/e2e) and exact verification commands>
  - Skill: `<skill-name | none>`

Exit Criteria:

- [ ] <behavior lands — specify success and failure modes>
- [ ] **API 测试：** 所有新增 `@BizMutation`/`@BizQuery` 方法通过 `IGraphQLEngine` 测试（`JunitAutoTestCase` 录制回放）；`@BizAction` 方法通过 `I*XxxBiz` 接口测试
- [ ] <relevant docs updated, or No owner-doc update required>
- [ ] `docs/logs/` updated

## Plan Audit

- Status: <pending | passed>
- Reviewer / Agent: <independent reviewer, subagent, or cold-replay proxy>
- Evidence: <task id and short findings/disposition summary; link audit file only when separately justified>

## Closure Gates

- [ ] in-scope behavior is complete
- [ ] relevant docs are aligned
- [ ] verification has run (specify which commands; customize for visual/UX domains if needed)
- [ ] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine` (not entity-level unit tests only); `@BizAction` methods tested via `I*XxxBiz` interface if applicable
- [ ] no in-scope item downgraded to deferred/follow-up
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill` listed, and Nop-platform phases do not write `none` without justification
- [ ] skill loading verification: each phase scanned available skills, loaded all matching skills, read routed docs, and selfchecked after each method/class (no anti-patterns in the output)
- [ ] text consistency verified: status, phases, gates, and log all agree
- [ ] closure audit was independent
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### <item name>

- Classification: `watch-only residual | optimization candidate | out-of-scope improvement | model-gap`
- Why Not Blocking Closure: <reason>
- Successor Required: `yes | no`
- Model Gap Detail (if Classification is `model-gap`): <what relationship/field/dict is missing in which ORM model, suggested fix, and trigger condition for fixing>

## Closure

Status Note: <why the plan can close>

Closure Audit Evidence:

- Reviewer / Agent: <independent reviewer or cold-replay proxy>
- Evidence: <task id / log link / walkthrough record; link audit file only when separately justified>

Follow-up:

- <non-blocking follow-up items only; confirmed defects must not appear here>
```
