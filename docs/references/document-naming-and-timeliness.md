# Document Naming And Timeliness

## Purpose

This guide distinguishes stable owner docs from time-sensitive process records.

For small and medium projects, this keeps the repo easy to navigate without forcing every file into the same naming style.

## Two Categories

### 1. Stable Owner Docs

These describe stable supported baseline truth and should usually keep stable names without dates.

Use stable names for:

- `docs/process/`
- `docs/architecture/`
- `docs/design/`
- `docs/references/`
- `docs/skills/`
- long-lived requirement baseline files such as `docs/requirements/product-scope.md` and `docs/requirements/commercial-baseline.md`

Examples:

- `docs/design/app-overview.md`
- `docs/architecture/system-baseline.md`
- `docs/process/application-development-workflow.md`

Rule:

- these files should be updated in place
- do not create a new dated version just because the content changed
- do not use stable owner docs to mirror active plans, current blockers, roadmap sequencing, or repeated feature status matrices

### 2. Time-Sensitive Records

These capture execution history, investigation context, or dated decisions.

These files should usually include a date in the path or filename.

Use dated naming for:

- `docs/logs/`
- `docs/testing/`
- `docs/discussions/`
- `docs/analysis/`
- `docs/audits/` for specialized audit records, not ordinary plan-local audit evidence
- `docs/retrospectives/`
- most one-off requirement synthesis files and implementation plans

Rule:

- time-sensitive directories should not maintain hand-written per-record indexes unless a human explicitly accepts the maintenance cost
- plans and audits should be discovered by filename, status inside the file, or search rather than mirrored into global context files
- a directory may keep one lightweight guide or README for naming and usage rules, but it should not duplicate every dated record

## Recommended Path Conventions

### Logs

- `docs/logs/YYYY/MM-DD.md`

### Testing Notes

- `docs/testing/YYYY/MM-DD.md`

### Discussions

- `docs/discussions/YYYY-MM-DD-topic.md`

Examples:

- `docs/discussions/2026-05-21-user-management-scope.md`
- `docs/discussions/2026-05-21-order-status-rules.md`
- `docs/discussions/2026-05-21-prototype-gap-checkout-flow.md`

### Analysis

- `docs/analysis/YYYY-MM-DD-topic.md`

Examples:

- `docs/analysis/2026-05-21-menu-structure-options.md`
- `docs/analysis/2026-05-21-prototype-feasibility-review.md`
- `docs/analysis/2026-05-21-auth-strategy-comparison.md`

### Audits

- `docs/audits/YYYY-MM-DD-<kind>-<topic>.md`

Use separate audit files only for specialized, complex, disputed, reusable, or future-replay-worthy audits. Ordinary plan-audit and closure-audit evidence should stay in the corresponding plan.

Examples:

- `docs/audits/2026-05-21-document-audit-user-management.md`
- `docs/audits/2026-05-21-multi-dimensional-audit-checkout-flow.md`
- `docs/audits/2026-05-21-doc-code-alignment-audit-order-list.md`

### Retrospectives

- `docs/retrospectives/YYYY-MM-DD-topic.md`

Examples:

- `docs/retrospectives/2026-05-21-checkout-prototype-gap.md`
- `docs/retrospectives/2026-05-21-pm-handoff-missing-analysis.md`

### Plans

For small and medium projects, prefer a simple dated plan name:

- `docs/plans/YYYY-MM-DD-topic-plan.md`

Examples:

- `docs/plans/2026-05-21-user-list-plan.md`
- `docs/plans/2026-05-21-role-permission-alignment-plan.md`

If the project later accumulates many plans and needs stronger indexing, you may add a numeric prefix:

- `docs/plans/NNN-YYYY-MM-DD-topic-plan.md`

Examples:

- `docs/plans/012-2026-05-21-user-list-plan.md`
- `docs/plans/013-2026-05-21-checkout-validation-plan.md`

### One-Off Requirement Synthesis Files

If the file is a one-off slice rather than a stable baseline file, prefer a dated name:

- `docs/requirements/YYYY-MM-DD-feature-name.md`

Examples:

- `docs/requirements/2026-05-21-user-management.md`
- `docs/requirements/2026-05-21-order-refund-flow.md`
- `docs/requirements/2026-05-21-dashboard-homepage.md`

## Bug Notes

Bug notes are historical, but they are usually referenced by issue identity rather than by date.

For small and medium projects, either of these is acceptable:

- `docs/bugs/01-short-bug-name.md`
- `docs/bugs/YYYY-MM-DD-short-bug-name.md`

Examples:

- `docs/bugs/01-order-status-double-submit.md`
- `docs/bugs/2026-05-21-login-token-refresh-loop.md`

Recommendation:

- if bug notes will become a long-lived library, prefer numbered filenames
- if bug notes will stay few and mainly serve local team memory, date-based filenames are acceptable

## Simple Rule Of Thumb

- if the file answers "what is the stable supported baseline?" -> stable name
- if the file answers "what happened in this round / this day / this investigation?" -> dated name

## Quick Copy Set

Use these as ready-made patterns:

```text
docs/logs/2026/05-21.md
docs/testing/2026/05-21.md
docs/discussions/2026-05-21-user-management-scope.md
docs/analysis/2026-05-21-auth-strategy-comparison.md
docs/audits/2026-05-21-document-audit-user-management.md
docs/plans/2026-05-21-user-list-plan.md
docs/requirements/2026-05-21-order-refund-flow.md
docs/retrospectives/2026-05-21-checkout-prototype-gap.md
docs/bugs/01-order-status-double-submit.md
```
