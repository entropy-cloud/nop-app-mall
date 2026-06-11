# 2026-06-03 User Admin Credential Hardening Plan

> Plan Status: completed
> Last Reviewed: 2026-06-03
> Source: user request to draft a design-grounded implementation plan, audit it, then execute it
> Related: none
> Audit: required

## Current Baseline

- `docs/design/user-and-address.md` defines that profile update flows must not expose or return sensitive credential data.
- `app-mall-web/src/main/resources/_vfs/app/mall/pages/LitemallUser/LitemallUser.view.xml` currently only inherits `_gen/_LitemallUser.view.xml` and does not narrow the generated CRUD surface.
- The current retained file already declares `list`, `pick-list`, `view`, `edit`, and `add`, but those declarations alone are not yet sufficient proof that inherited child columns and layouts are removed; this plan must use explicit override semantics that produce a narrowed effective schema.
- The generated page `app-mall-web/src/main/resources/_vfs/app/mall/pages/LitemallUser/_gen/_LitemallUser.view.xml` exposes `weixinOpenid` and `sessionKey` in the user list, detail form, and edit form, which creates an avoidable sensitive-data exposure in the admin UI.
- In the generated view, `pick-list` prototypes `list`, so any list-column exposure automatically carries into picker usage unless the retained page narrows the inherited grid.
- In the generated view, `add` prototypes `edit`, so any edit-form exposure automatically carries into add-form rendering unless the retained page narrows the inherited form.
- The `main` page list uses `@pageSelection`, and the `view` / `update` pages use `@formSelection`, so this slice must verify the final retained view definition rather than assuming compile success proves runtime field selection is safe.
- `app-mall-web/src/main/resources/_vfs/app/mall/pages/LitemallUser/main.page.yaml` and `picker.page.yaml` provide page entry points that can be used to inspect the fully resolved page schema after template expansion.
- This slice can be implemented in the non-generated view layer and does not require editing `model/*.orm.xml`, generated files, or protected auth/delta code paths.

## Goals

- Remove sensitive credential fields from the backend user-management page's default list and form surfaces.
- Keep the admin user page aligned with the existing user-and-address owner doc without changing persisted model shape or auth semantics.

## Non-Goals

- No changes to `model/*.orm.xml`, generated view files, or API contracts.
- No redesign of admin permissions, user lifecycle, or login-channel binding behavior.
- No attempt to harden every admin page in this plan.

## Task Route

- Type: `implementation-only change`
- Owner Docs: `docs/requirements/commercial-baseline.md`, `docs/design/user-and-address.md`, `docs/context/project-context.md`, `../nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`
- Skill Selection Basis: `Skill: none`. The work is a small repo-specific XView retention-layer customization plus required independent audits. No reusable skill in `docs/skills/README.md` is a better fit than the normal docs-driven workflow.
- Skill Selection Review Result: reviewed against `plan-audit-prompt.md`, `code-quality-audit-prompt.md`, and the repo skill registry; no execution skill is a better fit for authoring or implementing this page-local view override, while independent plan/closure review remains mandatory through subagents.

## Infrastructure And Config Prereqs

- No infra prereqs beyond existing baseline.

## Execution Plan

### Phase 1 - Harden The Admin User Page Surface

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/LitemallUser/LitemallUser.view.xml`, `docs/logs/2026/06-03.md`
Skill: `none`

- Item Types: `Fix | Decision | Proof`
- Prereqs: plan audit passed

- [x] Decision: keep the fix in the non-generated `LitemallUser.view.xml` retention layer and use explicit override semantics that narrow inherited children in the effective schema, such as `cols x:override="bounded-merge"` for list structures and layout replacement/removal plus rebuilt safe layouts for form structures. Alternatives considered: editing `_gen/_LitemallUser.view.xml` (rejected because generated files are non-editable), changing model/API truth (rejected because this slice is UI-surface hardening rather than contract change), and copying the full generated page into the retained file (rejected because it increases drift and maintenance cost). Residual risk: if another page or custom page fragment renders the same fields outside this view, that remains out of scope for this plan.
  - Skill: `none`
- [x] Fix: restrict the admin user list to business-safe columns and remove `weixinOpenid` and `sessionKey` from view/edit/add form layouts.
  - Skill: `none`
- [x] Fix: preserve ordinary admin management fields needed for user operations while avoiding broader behavior changes unrelated to sensitive-field exposure.
  - Skill: `none`
- [x] Proof: verify the retained `LitemallUser.view.xml` uses explicit override semantics that narrow `list`, `pick-list`, `view`, `edit`, and `add` instead of relying on empty placeholder nodes.
  - Skill: `none`
- [x] Proof: inspect the resolved admin page schema from the existing `main.page.yaml` and `picker.page.yaml` entry points so the plan proves the final merged runtime surface no longer includes `weixinOpenid` and `sessionKey` where those entries are reachable.
  - Skill: `none`
- [x] Proof: verify that ordinary admin-safe fields such as `username`, `nickname`, `mobile`, `avatar`, and `status` remain available on the page so the hardening does not break routine user management.
  - Skill: `none`
- [x] Proof: run `./mvnw compile -DskipTests` to validate the view-layer customization still parses and builds in the repo's standard verification path.
  - Skill: `none`

Exit Criteria:

- [x] The backend user page no longer exposes `weixinOpenid` or `sessionKey` in its default list, pick-list, view, edit, or add surfaces.
- [x] Plan and closure evidence record override-strategy proof, resolved-page proof, and build proof for this UI-surface hardening slice.
- [x] No owner-doc update required because this slice narrows an implementation drift to match the existing owner-doc rule that profile-related management surfaces must avoid exposing sensitive credential data, without changing the supported business baseline.
- [x] `docs/logs/` updated.

## Plan Audit

- Status: passed
- Reviewer / Agent: `general` subagent
- Evidence: initial audits `ses_176cb8247ffewRcFe4meC6jp4P` and `ses_176c91de6ffelEzBLmP54D141Q` failed on proof strength, baseline completeness, and missing effective-schema strategy; the revised plan passed on re-audit in `ses_176c6a7fbffeM5nmXdpDCEBVUU` with no remaining blocking findings.

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs are aligned
- [x] verification has run (`./mvnw compile -DskipTests`, `./mvnw -pl app-mall-app -am package -DskipTests`, plus authenticated `PageProvider__getPage` proof for `main.page.yaml` and `picker.page.yaml` showing sensitive fields are absent and admin-safe fields remain)
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was independent
- [x] closure evidence exists in files

## Deferred But Adjudicated

None.

## Closure

Status Note: implementation completed after the retained `LitemallUser` XView explicitly narrowed inherited list, pick-list, and form surfaces, build verification passed, and authenticated `PageProvider__getPage` output for `main.page.yaml` and `picker.page.yaml` confirmed the final resolved page schemas no longer exposed `weixinOpenid` or `sessionKey` while routine admin-safe fields remained.

Closure Audit Evidence:

- Reviewer / Agent: `OpenCode`
- Verdict: pass
- Evidence: re-ran the independent closure audit against `docs/plans/2026-06-03-user-admin-credential-hardening-plan.md`, `app-mall-web/src/main/resources/_vfs/app/mall/pages/LitemallUser/LitemallUser.view.xml`, `app-mall-web/target/classes/_vfs/app/mall/pages/LitemallUser/LitemallUser.view.xml`, `docs/logs/2026/06-03.md`, and the runtime proof files `C:/Users/a758371/AppData/Local/Temp/opencode/litemall-user-main-page.json` plus `C:/Users/a758371/AppData/Local/Temp/opencode/litemall-user-picker-page.json`; confirmed the retained and built XView definitions explicitly narrow `list`, `pick-list`, `view`, `edit`, and inherited `add`, the resolved runtime page payloads do not expose `weixinOpenid` or `sessionKey`, routine admin-safe fields remain present, and the log and closure gates are consistent with the implemented slice.

Follow-up:

- None.
