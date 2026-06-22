# Roadmap And Backlog

## Purpose

This file is a stable index that routes agents to the correct requirement, owner doc, and roadmap.

Dynamic status (Phase todo/planned/done) lives in `docs/backlog/implementation-roadmap.md`. This file does NOT track status.

The backlog is not a replacement for requirements, design owner docs, architecture owner docs, or plans. It only answers what to consider next and where to find it.

## Work Items

| Priority | Item | Requirement | Owner Doc | Notes |
| -------- | ---- | ----------- | --------- | ----- |
| P0 | Commercial baseline alignment | `docs/requirements/commercial-baseline.md` | `docs/design/app-overview.md` | Stable docs now use commercial baseline framing |
| P1 | Design-to-code gap implementation | `docs/requirements/commercial-baseline.md` | `docs/design/*.md` | Roadmap: `docs/backlog/implementation-roadmap.md`; First Commercial Loop = Phase 1-5c + 6; start with Phase 1 |
| P2 | Feature gap analysis | `docs/input/litemall-requirements.md` | `docs/design/feature-inventory.md` | Synthesize missing commercial capability gaps |
| P3 | Enhanced features (backend + AMIS) | `docs/design/*.md` | `docs/design/*.md` | Roadmap: `docs/backlog/enhanced-features-roadmap.md`; Phase 15-38 |
| P4 | Mobile frontend (nop-chaos-flux) | `docs/design/*.md` | `docs/design/*.md` | Roadmap: `docs/backlog/mobile-frontend-roadmap.md`; M1-M9 |

## Selection Rule

1. Read `docs/backlog/implementation-roadmap.md` to check current Phase status and dependencies.
2. Choose the highest-priority item whose Phase status allows work to proceed.
3. Before implementation, confirm the linked requirement, owner doc, autonomy policy (from `docs/context/ai-autonomy-policy.md`), and planning triggers are still valid. Do not infer readiness from chat alone.
4. If the roadmap status appears stale, downgrade or ask before implementation.

Agents must not upgrade Phase status, change autonomy to `implement`, or clear blockers without human confirmation or human-approved owner-doc evidence.
