# Source Of Truth And Precedence

## Purpose

This file defines which artifact answers which question.

Use it to avoid mixing stable truth, execution notes, and historical context.

## Precedence By Question

### What should be built now?

Primary source:

- `docs/requirements/`
- `docs/backlog/` or roadmap files when the question is implementation order

Support sources:

- `docs/input/`
- `docs/discussions/`

Rule:

- `docs/input/` preserves raw source material (including `docs/input/litemall-requirements.md`)
- `docs/requirements/` is the implementation-ready interpretation
- if they differ, update the requirement file explicitly or record the sequencing decision in the roadmap/backlog instead of silently relying on chat memory

### What is the supported app behavior?

Primary source:

- `docs/design/`

Rule:

- `docs/design/` owns app-layer feature, flow, role, and page behavior
- `docs/design/` may describe business-facing states and concept-level entities, but it should not duplicate persisted field catalogs or ORM dictionaries
- feature requirement files may drive changes, but stable app behavior should converge into owner docs under `docs/design/`
- `docs/design/` should not repeat roadmap status, active plan state, current blockers, or scattered implementation-status matrices

### What is the current supported technical structure?

Primary source:

- `docs/architecture/`

Rule:

- `docs/architecture/` owns technical boundaries, module responsibilities, and cross-cutting implementation rules
- implementation-specific transaction, locking, caching, scheduling, integration, and framework patterns belong here rather than in `docs/design/`

### What is the database truth?

Primary source:

- `model/app-mall.orm.xml` (XML ORM model)
- `model/app-mall.api.xml` (XML API model)
- `model/nop-auth-delta.orm.xml` (delta ORM model for nop-auth overrides)
- `deploy/sql/` (DDL scripts)
- generated `*.orm.xml` files

Rule:

- database definitions are owned by the XML model and generated ORM XML, not by plan text or prose documentation
- documentation may explain intent, but the model files are the source of truth
- DDL scripts in `deploy/sql/` should be aligned with model changes
- data dictionaries, persisted status codes, and field-level definitions should be maintained in the model rather than duplicated in design docs

### What is the API contract truth?

Primary source:

- `model/app-mall.api.xml` (XML API model)
- generated API interface code
- `*.xbiz.xml` business logic definitions

Rule:

- prose docs may summarize API intent, but the model/generated code wins as executable contract

### What is the external integration truth?

Primary source:

- `app-mall-wx/` WeChat Pay integration code
- `app-mall-api/` PayService interface

Rule:

- do not invent external system behavior from UI requirements alone

### What is the environment/deployment truth?

Primary source:

- `app-mall-app/src/main/resources/application.yaml`
- `deploy/sql/` migration scripts
- `build.bat`, `build.sh`, `run-dev.sh`, `run-prod.sh`

Rule:

- plans and docs may describe deployment intent, but the committed config artifacts are the operational source

### How should this slice be executed and closed?

Primary source:

- `docs/plans/`

Rule:

- plans are execution contracts, not long-term owner docs
- active plan status should be read from plan files themselves, not mirrored into broad context files

### What actually happened during execution?

Primary source:

- `docs/logs/`

Support sources:

- `docs/testing/`
- `docs/bugs/`
- `docs/audits/` when a specialized audit record was created
- `docs/retrospectives/`

### What should future AI sessions learn from repeated failures?

Primary source:

- `docs/skills/`
- `docs/lessons/`

Rule:

- use `docs/skills/` for reusable prompts and playbooks
- use `docs/lessons/` for reusable engineering lessons and cautionary patterns

## Conflict Resolution

- If raw input and synthesized requirements disagree, update `docs/requirements/` or reopen clarification before coding.
- If requirements and owner docs disagree, decide whether the requirement changes the supported baseline; then update `docs/design/` or `docs/architecture/` explicitly.
- If live code and owner docs disagree, treat it as either implementation drift or stale docs; do not silently choose one.
- If resolving a conflict changes user-visible behavior, data/model shape, API behavior, auth/permission behavior, or external integration behavior, stop and ask for confirmation.
- If verification fails, the plan is not closed even if implementation appears complete.
- If XML model files and generated code disagree, the XML model wins; regenerate and align DDL scripts.

## Legacy Or Stale-Docs Mode

Use this mode when the relevant requirement, owner doc, codebase-map route, or touched implementation area is stale, unknown, or disputed for the selected slice.

- Live code and executable contracts are evidence of current behavior, not automatically desired behavior.
- Owner docs are intended attractors only after they are revalidated against live code, requirements, and human/product intent.
- Before changing behavior, classify each conflict as `implementation drift`, `doc drift`, or `intentional legacy behavior` in a requirement, discussion, analysis, or plan file.
- AI autonomy defaults to `research-only` or `plan-first` until a baseline audit or human confirmation records what should be preserved versus changed. This restriction applies to the affected slice only.
- Do not "fix" code to match stale docs or rewrite docs to match code without recording the drift classification.

## Simple Rule Of Thumb

- stable behavior and structure belong in owner docs
- implementation order belongs in backlog or roadmap files
- execution belongs in plans and logs
- history and diagnosis belong in bugs, audits, testing notes, retrospectives, and lessons
