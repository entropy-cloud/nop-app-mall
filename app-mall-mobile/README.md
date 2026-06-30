# app-mall-mobile

Placeholder directory reserved for the nop-app-mall mobile H5 frontend.

- Tech stack: **nop-chaos-flux** (JSON-to-React low-code framework, React 19 + Vite + Zustand + pnpm)
- This is NOT a Maven module; it is a standalone pnpm/Vite frontend, sibling to the `app-mall-*` Maven modules
- Consumes the already-delivered backend GraphQL APIs (Phase 1-14); no new backend work
- Mobile development MUST directly reuse nop-chaos-flux's mobile mechanism (`flux-renderers-mobile`); see `docs/backlog/mobile-frontend-roadmap.md` → "nop-chaos-flux Reference"

## Status

Not yet scaffolded. This directory exists so the `mall-mobile` mission-driver can reference it as `moduleDir`. The actual project scaffold (Vite + React 19 + router + token management + Zustand stores) will be created by **Mobile Phase 1 (M1)** — see `docs/backlog/mobile-frontend-roadmap.md`.

Until M1 lands, the `pnpm test/build/lint/typecheck` commands in `missions/mall-mobile.json` have no `package.json` to run against; the mission's CHECK step treats this as "frontend not scaffolded yet" and proceeds to DRAFT_PLANS.

## nop-chaos-flux reference

Repo: `~/app/nop-chaos-flux-wt/nop-chaos-flux-master/` — see its `flux-guide/` (especially `flux-guide/mobile/README.md`) before starting M1.
