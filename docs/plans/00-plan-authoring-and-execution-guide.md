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
14. **Every phase must list `Required Pre-Reading`.** The executor MUST read all listed docs BEFORE writing any code for that phase. If the phase does not require any platform docs beyond the global `Task Route` owner docs, write `none`. If the phase involves Nop platform features (BizModel, view.xml, ORM, delta, xbiz, etc.), the pre-reading must cover the relevant `docs-for-ai/` guide — a bare `none` for a Nop-platform phase will fail plan audit. **This is a hard gate, not a suggestion.** The agent must read these docs and confirm understanding before proceeding to implementation items. Violating this rule (writing code without reading the listed docs) is a mandatory rework trigger.

### Anti-Slacking Rule

Every in-scope item before closure must land in exactly one state: `landed`, `adjudicated as residual-risk-only`, `moved to explicit successor ownership`, or `removed from scope with recorded reason`.

The following words are forbidden for in-scope items: `optional`, `if time permits`, `consider`, `maybe`, `nice to have`, `as needed`. If an item is truly optional, move it out of scope explicitly rather than leaving it in a fuzzy state.

A `Follow-up` item must name the trigger condition that would promote it into scope (e.g., "when user count exceeds 10K"). A `Deferred But Adjudicated` item must name the event or decision that would reopen it (e.g., "if the new API is adopted, this work may become redundant").

## When Executing

1. Before implementation, record plan audit evidence.
2. When you start a slice, update its `Status` to `in progress`.
3. When you finish a slice, update its `Status` to `completed` and check off all its execution items and exit criteria.
4. **MANDATORY PRE-READING GATE: Before executing ANY phase, read EVERY doc listed in `Required Pre-Reading` for that phase.** This is non-negotiable. Do NOT skip this step, do NOT rely on cached knowledge from previous sessions, and do NOT assume the listed docs merely repeat what you already know. After reading, confirm you understand the key rules (especially anti-patterns, safe APIs, and return-type conventions) before writing a single line of code. If the plan does not list any pre-reading, check whether the `Task Route` section implies platform-doc reading (e.g. Nop platform features) — if so, add the missing pre-reading before starting. **If a phase is delegated to a subagent, the subagent prompt MUST include the full `Required Pre-Reading` list and the instruction to read them before coding.**
5. Confirm the listed `Skill` still matches the task and available inputs. If not, update the plan before proceeding.
6. If a slice changes the live baseline or public contract, its exit criteria must include the doc-update step. If no doc update is needed, write `No owner-doc update required` explicitly.
7. Do not mark a slice complete because the function signature exists. Verify that the behavior, error handling, and test coverage land too.
8. If an item cannot be completed, move it to `Deferred But Adjudicated` with classification and reason. Do not leave it unchecked in the execution list.
9. Keep `docs/logs/` in sync with plan progress. A single aggregate log entry at plan closure is sufficient when all phases cover the same feature in one sprint; individual phase entries are required only when a phase spans a different day or a distinct deliverable.

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
Required Pre-Reading: `<paths to platform docs or owner docs that MUST be read in full BEFORE writing any code for this phase; list only docs that are new or non-obvious — if the required reading is already clear from the global Task Route, write none>`

- Item Types: `Fix | Decision | Proof | Follow-up`
- Prereqs: <phases or external dependencies that must complete first>

- [ ] **Pre-flight:** Read all docs listed in `Required Pre-Reading` above. Confirm understanding of key rules (anti-patterns, safe APIs, return-type conventions) before proceeding.
  - Skill: `<skill-name | none>`
- [ ] <implementation item>
      - Skill: `<skill-name | none>`
- [ ] <Decision: record rationale and alternatives in the item or a referenced doc>
  - Skill: `<skill-name | none>`
- [ ] <Proof: specify test strategy (unit/integration/e2e) and exact verification commands>
  - Skill: `<skill-name | none>`

Exit Criteria:

- [ ] <behavior lands — specify success and failure modes>
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
- [ ] no in-scope item downgraded to deferred/follow-up
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Pre-Reading` listed, and Nop-platform phases do not skip `docs-for-ai/` references
- [ ] pre-flight reading verification: code in each phase follows the patterns and anti-patterns documented in its `Required Pre-Reading` (no `Reference Docs` anti-patterns in the output)
- [ ] text consistency verified: status, phases, gates, and log all agree
- [ ] closure audit was independent
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### <item name>

- Classification: `watch-only residual | optimization candidate | out-of-scope improvement`
- Why Not Blocking Closure: <reason>
- Successor Required: `yes | no`

## Closure

Status Note: <why the plan can close>

Closure Audit Evidence:

- Reviewer / Agent: <independent reviewer or cold-replay proxy>
- Evidence: <task id / log link / walkthrough record; link audit file only when separately justified>

Follow-up:

- <non-blocking follow-up items only; confirmed defects must not appear here>
```
