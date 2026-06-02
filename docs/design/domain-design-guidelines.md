# Domain Design Guidelines

## Purpose

Define the `nop-app-mall` project-specific domain design supplement.

General Nop application rules are owned by `../nop-entropy/docs-for-ai/02-core-guides/application-project-docs-and-domain-design.md`.

Use this file only for mall-specific domain ownership and local interpretation. Do not copy general Nop platform rules here.

## Upstream Rules

Before changing mall design docs, apply these shared Nop application rules:

- `../nop-entropy/docs-for-ai/00-start-here/application-project-defaults.md`
- `../nop-entropy/docs-for-ai/02-core-guides/application-project-docs-and-domain-design.md`
- `../nop-entropy/docs-for-ai/02-core-guides/domain-logic-and-ddd.md`

Local rule:

- If this file conflicts with the shared Nop application rules, fix this file unless the conflict is caused by a deliberate mall-specific business constraint.
- Mall-specific business constraints must be traceable to `docs/requirements/`, `docs/design/`, `docs/architecture/`, or `model/*.orm.xml` / `model/*.api.xml`.

## Mall Domain Areas

Use these domain areas as the stable owner-doc map for `nop-app-mall`:

| Domain Area | Owner Doc | Owns |
| ----------- | --------- | ---- |
| Catalog | `product-catalog.md` | Category, brand, goods, SKU, specification, attribute, catalog search, storefront presentation |
| Commerce | `order-and-cart.md` | Cart, checkout, order lifecycle, payment state, shipment state, cancellation, refund, after-sale |
| Identity and address | `user-and-address.md` | Mall user, admin user, profile, authentication baseline, address, region |
| Marketing and engagement | `marketing-and-promotions.md` | Coupon, group buying, favorites, comments, topics, ads, feedback, footprint, search history, keyword |
| Operations | `system-configuration.md` | Business configuration, storage, notices, operational tasks, admin logs, statistics |
| Roles and permissions | `roles-and-permissions.md` | Business-facing role meanings, visibility, and protected actions across domain areas |

## Cross-Domain Ownership

- Checkout is owned by `order-and-cart.md`; it may reference catalog availability, user address, coupon eligibility, and payment capability without copying their full rules.
- Payment, refund, and after-sale business states are owned by `order-and-cart.md`; integration mechanics belong in `docs/architecture/` or implementation docs.
- Coupon and group-buying eligibility are owned by `marketing-and-promotions.md`; final order price and order state effects are owned by `order-and-cart.md`.
- User identity and address ownership are owned by `user-and-address.md`; order delivery and shipment outcomes are owned by `order-and-cart.md`.
- Role meanings and protected action visibility are owned by `roles-and-permissions.md`; each domain doc may mention role participation only where needed for business flow clarity.
- Storage, notices, scheduled operation effects, admin logs, and statistics business semantics are owned by `system-configuration.md`; technical scheduling, storage adapter, notification delivery, and reporting implementation belong in `docs/architecture/`.

## Mall-Specific Authoring Rules

- Keep storefront, admin, and system-initiated actions distinct.
- Keep commercial behavior formal even when implementation proceeds in small complete slices.
- Describe payment and refund states as business outcomes; do not invent external WeChat Pay behavior beyond the owner docs and code route.
- Keep persisted field sets, status codes, and dictionary values in `model/*.orm.xml` and `model/*.api.xml`.
- Put implementation sequencing in `docs/backlog/` or `docs/plans/`, not in domain design docs.

## Update Rule

Update this file only when the mall domain ownership map or local interpretation changes.

When changing a single feature's supported behavior, update the owning design doc instead.
