# 2026-06-02 Doc Governance Simplification Plan

> Plan Status: completed
> Last Reviewed: 2026-06-02
> Source: user request to remove MVP framing, reduce high-maintenance status docs, and align with Attractor-Guided Engineering template/articles
> Related: `docs/skills/design-doc-audit-prompt.md`, `docs/context/project-context.md`, `docs/design/`
> Audit: required

## Current Baseline

- The project has adopted AGE-style owner docs, but several copied-template assumptions remain too execution-state-heavy for this commercial mall project.
- `docs/design/` and related routing docs still use MVP/deferred/current-active wording in many places, which can bias agents toward temporary demo-grade implementation instead of formal commercial slices.
- `docs/context/project-context.md`, `docs/backlog/README.md`, and some indexes carry active-plan/current-blocker/freshness fields that create recurring maintenance cost.
- The AGE template and articles distinguish stable attractor owner docs from harness records: plans, audits, logs, bugs, and testing notes are trajectory/control evidence, not the attractor itself.

## Goals

- Revise the design-doc audit prompt first so future reviews challenge MVP/deferred/status sprawl and commercial-quality drift.
- Revise normative documentation-positioning docs so stable owner docs stay stable and time-sensitive execution state stays in its own records.
- Revise current project docs to remove MVP framing and scattered deferred/active status where it does not belong.
- Preserve required Nop platform source-of-truth rules for model/API/generated contract ownership.

## Non-Goals

- No product behavior implementation.
- No XML model, API model, generated code, SQL, or Java changes.
- No archive/delete of historical audit or plan records without human approval.
- No attempt to rewrite all historical dated logs/audits/plans into the new terminology.

## Task Route

- Type: `documentation governance/process change` and `verification or audit work`
- Owner Docs: `AGENTS.md`, `docs/index.md`, `docs/context/project-context.md`, `docs/context/source-of-truth-and-precedence.md`, `docs/process/application-development-workflow.md`, `docs/references/document-naming-and-timeliness.md`, `docs/references/maintenance-checklist.md`, `docs/skills/design-doc-audit-prompt.md`, `docs/design/`
- Skill Selection Basis: `Skill: none`. This is a project-specific documentation governance correction informed by the AGE template and articles; existing generic audit prompts are inputs, not the work method.

## Infrastructure And Config Prereqs

- No infra prereqs beyond existing baseline.

## Execution Plan

### Phase 1 - Revise Design Audit Prompt

Status: completed
Targets: `docs/skills/design-doc-audit-prompt.md`, `docs/skills/README.md`
Skill: `none`

- Item Types: `Fix | Decision | Proof`
- Prereqs: AGE template/articles read

- [x] Remove MVP/current-vs-deferred framing from the prompt.
  - Skill: `none`
- [x] Add checks for formal commercial-quality slices, anti-temporary implementation bias, and avoiding scattered roadmap/status content in stable design docs.
  - Skill: `none`
- [x] Keep boundary checks for `docs/design/`, `docs/architecture/`, `model/*.orm.xml`, and `model/*.api.xml`.
  - Skill: `none`

Exit Criteria:

- [x] Prompt explicitly audits stable commercial design without using MVP/deferred as the core lens.
- [x] Prompt still produces concrete severity-ordered findings.

### Phase 2 - Revise Normative Documentation Positioning

Status: completed
Targets: `AGENTS.md`, `docs/index.md`, `docs/context/project-context.md`, `docs/context/source-of-truth-and-precedence.md`, `docs/process/application-development-workflow.md`, `docs/references/document-naming-and-timeliness.md`, `docs/references/maintenance-checklist.md`, related READMEs as needed
Skill: `none`

- Item Types: `Fix | Decision | Proof`
- Prereqs: Phase 1 complete

- [x] Remove active-plan/current-blocker/active-requirement routing as mandatory maintained state where it causes recurring update cost.
  - Skill: `none`
- [x] Clarify that stable owner docs carry the attractor and time-sensitive plans/audits/logs carry trajectory/control evidence.
  - Skill: `none`
- [x] Clarify that execution status should be discovered from the relevant dated record files, not mirrored into broad context files.
  - Skill: `none`
- [x] Keep hard stop and verification-command information that agents truly need before safe implementation.
  - Skill: `none`

Exit Criteria:

- [x] Normative docs no longer require maintaining active plan/current blocker mirrors.
- [x] Stable-vs-time-sensitive responsibilities are explicit.
- [x] Required safety rules and Nop platform source-of-truth rules remain discoverable.

### Phase 3 - Revise Existing Project Docs

Status: completed
Targets: `docs/requirements/`, `docs/design/`, `docs/backlog/`, selected stable indexes or READMEs
Skill: `none`

- Item Types: `Fix | Decision | Proof`
- Prereqs: Phase 2 complete

- [x] Replace MVP framing with commercial baseline / product baseline / first complete commercial slice language.
  - Skill: `none`
- [x] Stop scattering deferred/active status through design docs; keep implementation ordering in backlog or roadmap only when needed.
  - Skill: `none`
- [x] Update feature inventory and related docs so they describe product capabilities and owner docs, not a repeated status matrix.
  - Skill: `none`
- [x] Avoid rewriting historical dated records except where a stable routing doc depends on them.
  - Skill: `none`

Exit Criteria:

- [x] Stable docs no longer present the project as an MVP project.
- [x] Stable docs do not repeatedly maintain deferred/active lists across many files.
- [x] Any remaining roadmap/status content is concentrated in a low-maintenance place.
- [x] `docs/logs/` updated.

## Plan Audit

- Status: passed
- Reviewer / Agent: `general` subagent
- Evidence: initial audit `ses_1774bed48ffees5ItrdPVBi2Z3` passed with low-severity suggestions; route label and closure gate coverage revised before implementation.

## Closure Gates

- [x] design audit prompt updated first
- [x] normative documentation-positioning docs updated second
- [x] existing project docs updated third
- [x] no intended stable docs still frame the commercial product as an MVP project
- [x] no broad context file requires active-plan/current-blocker mirroring
- [x] active-requirement and active-owner-doc mirrors are removed or deliberately retained with low-maintenance rationale
- [x] plan audit passed before implementation
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was independent
- [x] closure evidence exists in files

## Deferred But Adjudicated

None.

## Closure

Status Note: completed after independent closure audit confirmed the documentation governance simplification landed and stable docs no longer carry MVP/deferred or active-status mirrors.

Closure Audit Evidence:

- Reviewer / Agent: `general` subagent
- Evidence: `ses_1773ff663ffe13gW7iZ2vxW06V`

Follow-up:

- None.
