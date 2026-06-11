# 2026-06-03 Design Completeness Assessment

## Purpose

Assess whether the current `docs/design/` set is already sufficiently complete as the stable app-layer design baseline for `nop-app-mall`, and identify the minimum remaining gaps required to reach an "enough for sustained implementation" state.

## Question

Current question: has `docs/design/` reached a sufficiently complete design baseline?

This assessment uses the following precedence:

- Primary baseline for current supported scope: `docs/requirements/commercial-baseline.md`
- Stable app behavior owner: `docs/design/*.md`
- Technical boundary owner: `docs/architecture/*.md`
- Persisted model and generated contract truth: `model/*.orm.xml`, `model/*.api.xml`
- Raw source intent and broader future capability reference: `docs/input/litemall-requirements.md`

`docs/input/litemall-requirements.md` is used here to detect requirement gaps, owner-doc gaps, and future gap-analysis candidates. It does not override the active commercial baseline by itself.

## Verdict

Verdict:

- First commercial loop verdict: `sufficiently complete`
- Full commercial baseline verdict: `not yet sufficiently complete`

Current `docs/design/` is already broad, mostly internally organized, and owner-boundary-aware. Against the first complete commercial loop defined in `docs/requirements/commercial-baseline.md`, it is strong enough to route and support implementation at a business level.

What is not yet justified is a stronger claim such as "the repo now has a fully settled stable design baseline for the full commercial capability set named in `docs/requirements/commercial-baseline.md`". That broader claim remains open.

The limiting issue is not missing top-level domains, and it is not that every live model/page/service surface must already be treated as approved product scope. The limiting issue is that several broader commercial areas are still only partially settled across requirement and design, so a repo-wide completeness claim would still be premature.

In practice, the current design baseline is:

- sufficient for overview-level routing
- sufficient for the first commercial loop
- not yet sufficient to declare the full commercial baseline fully settled without additional requirement/design closure

## What "Enough" Means Here

For this repo, "sufficiently complete" needs to be evaluated in two scopes already distinguished by `docs/requirements/commercial-baseline.md`.

### Scope A: First complete commercial loop

Design is sufficiently complete for the first complete commercial loop when all of the following are true:

1. Every in-scope commercial capability from `docs/requirements/commercial-baseline.md` has a clear design owner.
2. Core flows specify actors, preconditions, allowed actions, state meaning, and important failure or timeout branches at business level.
3. Roles and protected actions are specific enough to guide implementation and review for the active baseline.
4. Design docs can be traced to major supported behaviors without turning stable owner docs into model, entity, or page inventories.

### Scope B: Full commercial baseline

Design is sufficiently complete for the full commercial baseline only when all of the following are true:

1. The broader capability set has first been settled in `docs/requirements/` instead of inferred from raw input or implementation inventory.
2. Those broader commercial areas have clear design ownership and sufficient owner-doc depth.
3. Core flows specify actors, preconditions, allowed actions, state meaning, and important failure or timeout branches at business level.
4. Roles and protected actions are specific enough to guide implementation and review, not just broad enough to describe intent.
5. Design docs can be traced to major supported behaviors without turning stable owner docs into model, entity, or page inventories.

## Source Inventory

Reviewed source groups:

- Context and routing: `AGENTS.md`, `docs/index.md`, `docs/context/project-context.md`, `docs/context/ai-autonomy-policy.md`, `docs/context/codebase-map.md`, `docs/context/source-of-truth-and-precedence.md`, `docs/context/conventions.md`
- Requirement baseline: `docs/requirements/commercial-baseline.md`, `docs/requirements/product-scope.md`
- Design baseline: all files under `docs/design/`
- Architecture baseline: `docs/architecture/project-vision.md`, `docs/architecture/system-baseline.md`, `docs/architecture/module-boundaries.md`
- Raw source intent: `docs/input/litemall-requirements.md`
- Live model/contract surface: `model/app-mall.orm.xml`, `model/app-mall.api.xml`, `model/nop-auth-delta.orm.xml`
- Live implementation surface: `app-mall-web/src/main/resources/_vfs/app/mall/pages/`, `app-mall-service/src/main/java/app/mall/service/entity/`, `app-mall-delta/src/main/resources/_vfs/_delta/`
- Prior design convergence evidence: `docs/plans/2026-06-02-design-review-convergence-plan.md`, `docs/audits/2026-06-02-design-review-convergence.md`, `docs/audits/2026-06-02-closure-audit-design-review-convergence.md`

## Evaluation Dimensions

This assessment uses six dimensions.

1. Scope coverage against the active commercial baseline
2. Coverage against broader raw litemall source intent without letting raw input override the active requirement
3. Workflow and state clarity for business-critical paths
4. Role and protected-action specificity
5. Owner-doc boundary discipline
6. Traceability from design docs to live model, page, service, and auth surfaces

## Findings Summary

### What Is Already Strong

- `docs/design/` has a stable owner-doc structure instead of mixing everything into one large document.
- `app-overview.md`, `feature-inventory.md`, `domain-design-guidelines.md`, and `domain-glossary.md` provide a usable navigation layer.
- `product-catalog.md`, `order-and-cart.md`, `user-and-address.md`, `marketing-and-promotions.md`, `system-configuration.md`, and `roles-and-permissions.md` mostly respect design-vs-architecture-vs-model boundaries.
- The first commercial loop in `docs/requirements/commercial-baseline.md` is reflected at a high level in current design docs.
- Previous convergence work already removed several demo-grade and boundary-mixing problems.

### Minor Consistency Risk Inside The Design Set

- `docs/design/README.md` describes `marketing-and-promotions.md` more narrowly than `docs/design/domain-design-guidelines.md`, which also routes comment, feedback, footprint, search history, and keyword into that owner area.
- This is not severe enough to change the main verdict, but it is a real routing inconsistency and should be normalized in a later doc-maintenance pass.

### Why Full Commercial-Baseline Closure Is Still Open

- The design set is complete by domain headings.
- The first complete commercial loop appears sufficiently covered.
- Broader commercial areas are still not settled at enough owner-doc depth.
- Several business capabilities already named by the full commercial baseline or supported raw input are still only summarized in stable design docs.
- Stronger repo evidence exists for some of those areas, but generic generated CRUD or thin entity wrappers are not treated here as automatic proof of approved scope.

## Coverage And Traceability

### Capability-Level Traceability

| Capability / source area | Raw source | Current design owner | Assessment |
| --- | --- | --- | --- |
| User registration, login, profile, address | `4.1`, `7.8` | `user-and-address.md` | largely covered |
| Product catalog, category, brand, SKU, search/filter | `4.2`, `7.2` | `product-catalog.md` | largely covered |
| Cart, checkout, order lifecycle, payment-state progression | `4.2`, `4.3`, `5.1`, `5.2`, `7.1`, `8.1` | `order-and-cart.md` | mostly covered for baseline |
| Coupon and groupon commercial semantics | `4.4`, `4.5`, `7.4`, `7.5`, `8.2`, `8.3` | `marketing-and-promotions.md` plus `order-and-cart.md` | materially clarified for current baseline |
| Review / comment rules | `4.6`, `7.6` | `marketing-and-promotions.md` | materially clarified |
| Post-receipt after-sale workflow | `4.7`, `5.4`, `7.7`, `8.4` | partly in `order-and-cart.md` | materially clarified |
| Search history and keyword governance | `4.8`, `7.10` | split across `product-catalog.md` and `marketing-and-promotions.md` | materially clarified for current baseline |
| Role/permission operations and protected admin actions | `4.9.1`, `7.11` | `roles-and-permissions.md` | adequate for first-loop baseline; richer depth remains requirement-first |
| System configuration, storage, notice, logging, statistics | `4.9.3` plus system sections | `system-configuration.md` | partially covered |

### Live Surface Traceability

| Live surface | Evidence | Current design traceability | Assessment |
| --- | --- | --- | --- |
| ORM entities in `model/app-mall.orm.xml` | broad entity set across catalog, order, coupon, groupon, comment, aftersale, notice, role, permission, keyword, search history, feedback | model breadth confirms concept presence, but does not by itself prove approved scope depth | supporting evidence only |
| AMIS pages in `app-mall-web/.../pages/` | CRUD pages exist for `LitemallComment`, `LitemallAftersale`, `LitemallKeyword`, `LitemallRole`, `LitemallPermission`, `LitemallNoticeAdmin`, etc. | many are likely generated/admin scaffolding; they indicate inventory, not automatic design debt | supporting evidence only |
| BizModels in `app-mall-service/src/main/java/app/mall/service/entity/` | `CrudBizModel` exists for most business entities, plus extra mutations in `LitemallAftersaleBizModel` | custom behavior in `LitemallAftersaleBizModel` is strong evidence of meaningful business workflow; thin CRUD wrappers are weaker evidence | mixed |
| Auth delta in `app-mall-delta/` | live auth override surface exists | confirms auth customization exists, but does not by itself prove richer business RBAC workflow scope | supporting evidence only |
| API model in `model/app-mall.api.xml` | currently only `MallService.findMallProducts` is defined | this is mainly an implementation-contract gap, but it reduces contract-side traceability for design review | implementation gap, not direct design gap |

## Gap Analysis

### Gap 1: Review / Comment semantics have been materially clarified

Evidence:

- Raw source has a dedicated review module and explicit rules in `4.6` and `7.6`.
- Live repo has `LitemallComment` model, page, and BizModel surfaces.
- `marketing-and-promotions.md` now defines comment/review lifecycle, eligibility boundary, visibility intent, and the relation between review, order completion, and after-sale.

Existing coverage:

- Owner assignment already exists via `docs/design/domain-design-guidelines.md`, which routes comment/review into the marketing and interaction area.
- The previous owner-depth gap has been materially reduced.

Why it matters:

- Review eligibility is tied to order completion and time windows.
- It crosses order completion, user actions, and admin moderation.
- Remaining work is now mostly implementation follow-through and optional moderation-depth refinement rather than owner-level ambiguity.

Classification: `partially closed owner-depth gap`

Minimum fix:

- Keep later implementation aligned with the now-clarified review lifecycle and order-completion boundary.

### Gap 2: Post-receipt after-sale design has been materially clarified

Evidence:

- Raw source contains dedicated after-sale module, refund flow, rules, and state machine sections: `4.7`, `5.4`, `7.7`, `8.4`.
- Live repo includes `LitemallAftersale` model/page/BizModel surface and after-sale dictionaries in `model/app-mall.orm.xml`.
- `order-and-cart.md` now defines post-receipt after-sale lifecycle, applicant/admin actions, cancellation, refusal, refund completion, and resulting visibility semantics in stable business terms.

Existing coverage:

- Ownership already exists in `order-and-cart.md` and `domain-design-guidelines.md`.
- The document already records pre-shipment refund and acknowledges post-receipt after-sale as a separate concept.
- The previous workflow-depth gap has been materially reduced.

Why it matters:

- After-sale is not a minor extension. It is a separate workflow with its own states and protected actions.
- The clarified lifecycle now matches the stronger live evidence already present in `LitemallAftersaleBizModel` without pulling implementation mechanics into the owner doc.

Classification: `partially closed owner-depth gap`

Minimum fix:

- Keep later implementation aligned with the clarified after-sale lifecycle and role-visibility rules.

### Gap 3: Richer role/permission management is a broader raw-input candidate, not a proven full-baseline blocker

Evidence:

- Raw source defines role creation, permission assignment, and button-level protected actions in `4.9.1` and `7.11`.
- Live repo includes `LitemallRole`, `LitemallPermission`, `LitemallUserRole` model/service/page surfaces and an auth delta module under `app-mall-delta/`.
- Current `roles-and-permissions.md` defines only three coarse business roles and a few action restrictions.

Existing coverage:

- For the current first commercial loop, the current coarse role semantics appear adequate.
- The document already covers the highest-value restrictions such as admin-only product management, super-admin-only role management, and user/admin separation.

Why it matters:

- The raw litemall source describes richer RBAC workflow than the current requirement baseline does.
- If the project later adopts that richer scope, requirement synthesis should come first.

Classification: `requirement gap`

Minimum fix:

- First synthesize the intended RBAC scope under `docs/requirements/`.
- After that requirement is settled, expand `roles-and-permissions.md` with role-management workflow, permission-allocation semantics, and a protected-action matrix at business level.
- Keep technical auth-resource mapping in architecture or delta owners.

### Gap 4: Search design covers browsing and history, but keyword governance is still shallow

Evidence:

- Raw source `4.8` and `7.10` includes hot keyword behavior, search history, and search recording rules.
- Live repo includes `LitemallKeyword` and `LitemallSearchHistory` surfaces.
- Current design splits search between `product-catalog.md` and `marketing-and-promotions.md`, but does not clearly own keyword curation, hot/default keywords, or how search-history and keyword governance relate.

Existing coverage:

- `product-catalog.md` already covers storefront search/filter behavior.
- `marketing-and-promotions.md` already covers search history as an interaction capability.
- The unresolved part is keyword-governance depth and cross-owner clarity.

Why it matters:

- Search is a real product surface, not just a filter option.
- Owner routing already exists, but keyword governance and cross-owner handoff depth remain weak.
- This is not a proven blocker for the current commercial baseline or for full closure of the requirement-defined commercial baseline as it exists today.

Classification: `existing owner depth gap`

Minimum fix:

- Add explicit keyword and search-governance coverage in the existing owner docs, or declare a sharper ownership split between catalog discovery and marketing/search operations.

### Gap 5: Notification and reporting semantics have been materially clarified, but still need future implementation follow-through

Evidence:

- `system-configuration.md` covers configuration, storage, notice, scheduled operations, logs, and statistics.
- `docs/requirements/commercial-baseline.md` explicitly names notification and reporting as commercial product areas.
- The current design and architecture set now distinguishes notice from notification more clearly, defines notification as business-event delivery, defines reporting as manager-facing operating views, and routes implementation to `nop-report` and `nop-integration` at the architecture level.

Existing coverage:

- Owner assignment already exists in `system-configuration.md` and `domain-design-guidelines.md`.
- The current doc now distinguishes notice from notification, records core operational categories, and clarifies that channel failure does not change core business facts.
- `docs/architecture/system-baseline.md` now provides a default technical route: use `nop-report` for reporting outputs and `nop-integration` for outbound channels such as SMS and email.

Why it matters:

- System and operations behavior is where design often looks complete while hiding unresolved semantics.
- Notification and reporting are already named in the commercial baseline and already have a design owner; the remaining work is now mainly implementation follow-through and future detail refinement rather than a major owner-doc gap.

Classification: `existing owner depth gap`

Minimum fix:

- Keep later implementation aligned with the clarified business semantics and the `nop-report` / `nop-integration` architecture route.

### Gap 6: Traceability from design facts to repo surfaces could be easier, but this is not itself a completeness blocker

Evidence:

- `feature-inventory.md` maps capabilities to owner docs, not live entities or surfaced admin pages.
- The live repo contains a much wider set of already-materialized entities and pages than the current design docs explicitly trace.

Why it matters:

- Reviewers can still spend extra time proving whether a specific repo surface is in current scope, dormant, or merely generated scaffolding.

Classification: `design governance improvement`

Minimum fix:

- Keep `feature-inventory.md` capability-level.
- If needed, capture extra traceability in analysis/audit artifacts or add only narrow cross-references in ambiguous owner docs, rather than creating a repo-wide entity/page mapping requirement.

## What Is Not A Design Gap

### `model/app-mall.api.xml` is underbuilt, but that is not by itself proof that design is incomplete

`model/app-mall.api.xml` currently exposes only `MallService.findMallProducts`. That is a serious contract-side incompleteness signal for implementation planning, but it is not itself a design-owner failure. It should be classified as an `implementation gap` or `contract modeling gap` unless and until a missing API contract reveals a missing business rule.

### Coarse design summaries are acceptable when the live surface is also coarse

Not every entity needs a dedicated design section. The problem is not that `docs/design/` lacks one file per table. The problem is that several already-important business surfaces have crossed the threshold where summary-only coverage is no longer enough.

## Overall Classification Of Remaining Gaps

| Gap | Classification |
| --- | --- |
| Review/comment workflow | materially clarified owner area |
| Post-receipt after-sale workflow | materially clarified owner area |
| Coupon/groupon/content marketing owner semantics | materially clarified owner area |
| Richer role/permission admin workflow | requirement gap beyond current baseline proof |
| Notification/reporting operational semantics | partially closed owner-depth gap |
| Design-to-live-surface traceability | design governance improvement |
| Sparse API model | implementation gap |
| Any broader litemall capability beyond active commercial baseline | requirement gap or justified out-of-scope, depending on future requirement synthesis |

## Baseline Blockers Vs Broader Design Debt

### Blockers for the first commercial loop

- none proven as blockers from current evidence

Current design appears adequate to continue first-loop work as long as implementation stays within the already-documented product/catalog/cart/order/user/admin baseline.

### Open items that still limit an aggressive repo-wide completeness claim

- notification/reporting is now routed and partially clarified, but future work may still need more owner-level settlement if the product baseline expands around admin notices, delivery policy, or richer reporting semantics
- richer admin RBAC workflow remains under-specified at the design/requirement boundary if humans later want more than the currently proven baseline
- the repository still has weak contract-side API modeling and only partial design-to-live-surface traceability, which does not prove a design-owner failure but does reduce confidence in a maximal completeness claim

These are now mostly governance, contract, or broader-baseline follow-up risks rather than the sharpest unresolved owner-doc holes inside the currently targeted commercial slice.

## Recommended Path To Reach Sufficient Completeness

1. Keep the current design directory structure. The problem is not top-level decomposition.
2. Strengthen the weakest existing owner docs instead of creating many new files immediately.
3. Continue future implementation from the now-clarified comment/review and post-receipt after-sale owner docs instead of reopening ownership questions.
4. Keep richer RBAC workflow as a broader requirement candidate unless humans explicitly promote it.
5. Treat repo-wide traceability as an optional review aid, not as a stable owner-doc completeness gate.
6. Keep `docs/input/litemall-requirements.md` as secondary evidence and convert any accepted broader gaps into requirement or design updates deliberately, not by implication.

## Bottom Line

The answer is not "design is missing entirely," and it is not "design is already complete in the broadest possible sense." The accurate state is:

- the design baseline is structurally mature
- the design baseline is commercially framed rather than demo-framed
- the first commercial loop is documented well enough to continue
- review/comment and post-receipt after-sale semantics have now been materially clarified in stable owner docs
- coupon, groupon, search-keyword, and content-marketing semantics have now also been materially clarified in stable owner docs for the current baseline slice
- the main residual caution is no longer a sharp owner-doc hole in those marketing surfaces, but broader governance/contract confidence gaps that still make a maximal repo-wide completeness claim somewhat stronger than the evidence

So the current design should be treated as `sufficiently complete for the first commercial loop and materially closer to full commercial-baseline coverage, with residual caution mainly around governance/contract confidence rather than obvious missing business-owner semantics`.

## Residual Risks

- Some live surfaces may still be intentionally provisional or generated scaffolding; this assessment now treats them only as supporting evidence unless requirement or stronger custom behavior indicates otherwise.
- The current repo does not yet provide a strong contract-side API model baseline, which weakens one axis of traceability.
- If humans intend to keep some live surfaces dormant, that intent should be written explicitly into requirement or design files; otherwise they continue to look like unsupported design debt.
