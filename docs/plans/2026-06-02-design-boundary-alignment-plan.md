# 2026-06-02 Design Boundary Alignment Plan

> Plan Status: completed
> Last Reviewed: 2026-06-02
> Source: user request to continue design-layer cleanup and clarify whether AGENTS/index/other docs need updates
> Related: none
> Audit: required

## Current Baseline

- `docs/design/` is intended to hold stable app-layer owner docs, while `docs/architecture/` holds technical structure and `model/*.orm.xml` holds database truth.
- The newly added domain design docs under `docs/design/` currently mix business rules and state flows with persistence details, table names, field lists, Nop platform implementation notes, and module-specific realization details.
- Routing and ownership docs (`AGENTS.md`, `docs/index.md`, `docs/context/source-of-truth-and-precedence.md`, `docs/context/conventions.md`, `docs/process/application-development-workflow.md`, `docs/references/maintenance-checklist.md`) describe or maintain the design/architecture split, but they do not yet state clearly enough that ORM models and dictionaries remain authoritative for persisted model shape.

## Goals

- Align `docs/design/` with a business/domain-owner role: semantics, roles, flows, business rules, and state transitions.
- Make the persistence/model boundary explicit: persisted entities, fields, and dictionaries stay authoritative in `model/*.orm.xml`.
- Update the key routing and ownership docs so future sessions follow the same boundary by default.
- Make the retained-content rule explicit: design docs may keep business-facing state names, transition rules, and concept-level entity references, but they should not act as a second field/table/dictionary catalog.

## Non-Goals

- No product behavior changes.
- No ORM model, API model, generated code, or SQL changes.
- No attempt to fully backfill every missing technical owner doc beyond what is needed for the clarified boundary.

## Task Route

- Type: `app-layer design change`
- Owner Docs: `docs/design/README.md`, `docs/design/app-overview.md`, `docs/design/feature-inventory.md`, `docs/design/product-catalog.md`, `docs/design/order-and-cart.md`, `docs/design/user-and-address.md`, `docs/design/marketing-and-promotions.md`, `docs/design/system-configuration.md`, `docs/context/README.md`, `docs/context/project-context.md`, `docs/context/source-of-truth-and-precedence.md`, `docs/context/conventions.md`, `docs/process/application-development-workflow.md`, `docs/references/maintenance-checklist.md`, `docs/index.md`, `AGENTS.md`
- Skill Selection Basis: `Skill: none`. This slice is a repo-specific documentation boundary cleanup; no reusable skill in `docs/skills/README.md` is a better fit than the normal docs-driven workflow plus independent audits.

## Infrastructure And Config Prereqs

- No infra prereqs beyond existing baseline.

## Execution Plan

### Phase 1 - Define Boundary And Audit Scope

Status: completed
Targets: `docs/context/`, `docs/process/application-development-workflow.md`, `docs/references/maintenance-checklist.md`, `docs/index.md`, `AGENTS.md`, `docs/design/README.md`, `docs/architecture/README.md`
Skill: `none`

- Item Types: `Decision | Proof`
- Prereqs: none

- [x] Record the explicit ownership split between app-layer design, technical architecture, and ORM-model truth in the relevant routing/precedence docs.
  - Skill: `none`
- [x] Record the retained-content rule for design docs: keep business semantics and state transitions; remove duplicate table, field, dictionary, annotation, and module-implementation catalogs.
  - Skill: `none`
- [x] Run an independent plan audit that checks both plan soundness and whether any additional owner/index docs must be updated for the boundary to hold.
  - Skill: `none`

Exit Criteria:

- [x] The repo has a clear written rule for `docs/design/` vs `docs/architecture/` vs `model/*.orm.xml`.
- [x] The implementation scope includes every routing file needed to keep that rule durable.
- [x] `docs/context/project-context.md` reflects the active plan and documentation-alignment status.
- [x] `docs/logs/` updated.

### Phase 2 - Refactor Domain Design Docs To Match The Boundary

Status: completed
Targets: `docs/design/*.md`
Skill: `none`

- Item Types: `Fix | Decision | Proof`
- Prereqs: Phase 1 boundary decisions and plan audit passed

- [x] Remove persistence-detail duplication, table lists, field inventories, and platform-specific implementation notes from the domain design docs while preserving business semantics.
  - Skill: `none`
- [x] Keep state-machine semantics and business workflows in design docs, while pointing persisted codes, dictionary values, and field-level truth back to `model/*.orm.xml`.
  - Skill: `none`
- [x] Normalize high-level overview docs (`app-overview.md`, `feature-inventory.md`) so they advertise the tightened owner-doc boundary consistently with the domain docs.
  - Skill: `none`
- [x] Update any affected design index/overview text so the directory advertises the new boundary correctly.
  - Skill: `none`

Exit Criteria:

- [x] `docs/design/` describes supported business behavior and domain rules without acting as a second ORM schema spec.
- [x] Any retained references to persisted codes or entities are framed as business semantics and point to the model as the source of truth.
- [x] No in-scope design doc still contains table-by-table, field-by-field, or platform-annotation implementation sections as normative truth.
- [x] `docs/logs/` updated.

## Plan Audit

- Status: passed
- Reviewer / Agent: `general` subagent
- Evidence: initial audit `ses_177b69a87ffe3Omb0Ao5ndOGCG` reported fail because workflow/router files and retained-content rules were under-scoped; revised plan re-audit in the same session passed.

## Closure Gates

- [x] in-scope boundary docs are aligned
- [x] relevant design docs are aligned with the new ownership split
- [x] document proof has run and is recorded in files: plan audit, closure audit, updated routing docs, and a check that no in-scope design doc remains a schema surrogate
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was independent
- [x] closure evidence exists in files

## Deferred But Adjudicated

None.

## Closure

Status Note: completed after design docs were reduced to business semantics, routing docs were aligned to the same boundary, and independent closure audit findings were resolved.

Closure Audit Evidence:

- Reviewer / Agent: `general` subagent
- Evidence: initial closure audit `ses_177a7c02effeQtv8Vx3qk4YeOR` found remaining persisted-code duplication in `docs/design/order-and-cart.md`, a maintenance-checklist contradiction, and open plan text. Those findings were fixed in the repo, and the plan was then closed against the updated files.

Follow-up:

- None.
