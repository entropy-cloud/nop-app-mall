# Roadmap And Backlog

## Purpose

Use this file for implementation ordering and AI-ready candidate work.

The backlog is not a replacement for requirements, design owner docs, architecture owner docs, or plans. It only answers what to consider next and what readiness constraints apply.

## Roadmap

| Priority | Item | Requirement | Owner Doc | Readiness | AI Autonomy | Notes |
| -------- | ---- | ----------- | --------- | --------- | ----------- | ----- |
| P0 | Commercial baseline alignment | `docs/requirements/commercial-baseline.md` | `docs/design/app-overview.md` | `done` | `plan-first` | Stable docs now use commercial baseline framing |
| P1 | Design-to-code gap implementation | `docs/requirements/commercial-baseline.md` | `docs/design/*.md` | `in-progress` | `plan-first` | Roadmap: `docs/backlog/implementation-roadmap.md`; First Commercial Loop = Phase 1-5c + 6; start with Phase 1 |
| P2 | Feature gap analysis | `docs/input/litemall-requirements.md` | `docs/design/feature-inventory.md` | `needs-requirement` | `research-only` | Synthesize missing commercial capability gaps |

## Readiness Invariants

`ready` means all of these are true:

- requirement path exists and has testable acceptance criteria
- owner doc path exists and is not known stale for this slice
- verification commands in `docs/context/project-context.md` are real
- blocking open questions are absent or explicitly non-blocking
- protected areas are configured in `docs/context/ai-autonomy-policy.md`
- planning triggers were checked

`Plan: none` is valid only when the item clearly qualifies for the no-plan path in `docs/plans/00-plan-authoring-and-execution-guide.md`. If a plan is required, set AI autonomy to `plan-first` until the plan audit passes.

Agents may downgrade stale rows from `ready` to `needs-*` or `blocked` with evidence. Agents must not upgrade rows to `ready`, change autonomy to `implement`, or clear blockers without human confirmation or human-approved owner-doc evidence.

## Readiness Values

- `idea` - not ready for implementation
- `needs-requirement` - raw input exists but no implementation-ready requirement exists
- `needs-design` - requirement exists but owner doc is missing or stale
- `in-progress` - currently being implemented, planned, or aligned
- `ready` - AI may proceed according to the autonomy label
- `blocked` - cannot proceed until the blocker is resolved
- `done` - completed and verified

## AI Autonomy Values

Use the values from `docs/context/ai-autonomy-policy.md`:

- `implement`
- `plan-first`
- `ask-first`
- `research-only`
- `blocked`

## Selection Rule

When asked to continue without a named task, choose the highest-priority `ready` item whose `AI Autonomy` is `implement` and whose blocker is absent.

Before implementation, confirm the linked requirement, owner doc, autonomy policy, and planning triggers are still valid. Do not infer readiness from chat alone.

If the table is stale, downgrade the row or ask before implementation.
