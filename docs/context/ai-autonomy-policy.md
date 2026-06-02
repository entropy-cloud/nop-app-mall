# AI Autonomy Policy

## Purpose

This file defines when AI agents may proceed without asking and when they must stop for human input.

Keep it short and project-specific. Update it whenever the team wants AI to take more or less initiative.

AI may make this file stricter by marking work more constrained, but AI must not loosen protected areas, change `ask-first`/`blocked`/`research-only` work to `implement`, or remove blockers without explicit human confirmation or owner-doc evidence marked as human-approved.

AI-authored or AI-modified docs, including owner docs, cannot be used as evidence to loosen autonomy, clear blockers, mark docs fresh, or downgrade protected areas unless a human explicitly approves that evidence.

## Autonomy Levels

Use these labels in `docs/backlog/`, roadmap entries, and plans when relevant:

- `implement` - AI may implement after reading the listed requirement, owner doc, and verification commands.
- `plan-first` - AI may draft or update the plan, but implementation waits for plan audit and any protected-area approval required by the table below.
- `ask-first` - AI must ask before changing code or user-visible behavior.
- `research-only` - AI may inspect, summarize, and propose options, but must not modify product behavior.
- `blocked` - AI must not proceed until the blocker is resolved in files or by human confirmation.

## Reviewer Availability

- Reviewer availability: `subagent`

Rules:

- `human` or `subagent` - use that reviewer for required plan and closure audits.
- `none` - cold replay may be used only for non-protected, non-high-risk plans. Cold replay is not a second reviewer; it is a documented self-check performed after implementation context is set aside.
- Protected areas, unresolved product risk, or source-of-truth conflicts still require human/subagent review or must remain blocked.

## AI May Proceed Without Asking When

- the work item is marked `implement` or the user directly requests a local low-risk change
- the relevant requirement or roadmap slice has concrete acceptance criteria
- the relevant owner docs are identified through `docs/index.md`, the requirement, backlog/roadmap item, or plan
- for backlog-selected work, the backlog row is `ready`, has no stale links, and does not require a missing plan
- verification commands in `docs/context/project-context.md` are real commands, not placeholders
- protected-area placeholders in this file have been replaced with real entries or explicit `none`
- the selected slice has explicitly verified relevant requirement, owner docs, codebase-map route, and touched code area when freshness is uncertain or disputed
- the task does not touch a protected area below
- open questions are explicitly non-blocking

## AI Must Ask Or Stop Before

- changing product scope when the requirement or owner doc is ambiguous
- changing database/model shape, data deletion, payment, auth, permission, deployment, or external integration behavior without an owner doc and test strategy
- modifying XML models (`model/*.orm.xml`, `model/*.api.xml`) — these drive code generation and must be human-approved
- inventing behavior for an external system that is not described in committed integration docs or tests
- skipping required verification because commands are missing, broken, or too slow
- closing a plan whose audit, verification, docs, or checklist evidence is missing
- proceeding when live code and owner docs conflict and resolving the conflict would change user-visible behavior or public contracts
- loosening autonomy labels, protected-area rules, or blockers without human confirmation or human-approved owner-doc evidence
- proceeding with implementation when the relevant requirement, owner doc, code route, or touched area is stale, unknown, or disputed; first perform baseline research or a plan-first alignment slice
- editing generated code (under `target/` or generated from XML models) instead of regenerating from models

## Protected Areas

| Area                 | Rule        | Required Evidence                      |
| -------------------- | ----------- | -------------------------------------- |
| WeChat Pay (app-mall-wx) | ask first   | owner doc + tests                      |
| Data deletion (orders, users) | ask first   | owner doc + tests                      |
| Auth/permissions (app-mall-delta) | plan-first | owner doc + tests                      |
| XML models (model/*.orm.xml, model/*.api.xml) | ask first | human approval + regenerated code      |
| Database schema (deploy/sql/) | plan-first | migration script + owner doc           |

Protected-area rule meanings:

- `ask first` - human approval is required before planning or implementation.
- `plan-first` - AI may draft the plan, but implementation requires plan audit plus the required evidence in the table. If reviewer availability is `none`, implementation stays blocked.
- `research-only` or `blocked` - AI may not change product behavior.

## Backlog Selection Rule

If the user asks AI to continue work without naming a task, choose the highest-priority item in `docs/backlog/README.md` whose autonomy is `implement` and whose blockers are `none`.

Before implementing the selected item, re-check planning triggers. `Plan: none` does not waive the plan guide.

Direct user requests for local low-risk edits do not require a backlog row, but they still must satisfy the no-plan path and verification rules.

If no safe `implement` item exists, summarize the top blocked, `plan-first`, or `ask-first` item and ask for a decision.
