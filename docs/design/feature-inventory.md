# Feature Inventory

## Purpose

Map product capability areas to their stable owner docs.

This file is not a roadmap, backlog, or implementation-status matrix. Use it to route readers to the owner doc for a capability, not to track whether the capability is currently being implemented.

## Capability Map

| Capability Area | Owner Doc | Notes |
| ---------------- | --------- | ----- |
| User and admin account management | `docs/design/user-and-address.md` | Registration, login, profile, admin accounts, addresses, region data |
| Roles and permissions | `docs/design/roles-and-permissions.md` | Business role meanings, visibility, protected actions |
| Product catalog | `docs/design/product-catalog.md` | Category, brand, goods, SKU, specifications, attributes, catalog search |
| Cart and checkout | `docs/design/order-and-cart.md` | Cart lines, checkout preconditions, price components, order submission |
| Order lifecycle | `docs/design/order-and-cart.md` | Payment, shipment, receipt, cancellation, refund, after-sale state meanings |
| Marketing and promotions | `docs/design/marketing-and-promotions.md` | Coupons, group buying, engagement, promotion content, feedback surfaces |
| System configuration and operations | `docs/design/system-configuration.md` | Business configuration, storage, notices, operational tasks, admin logs, statistics |

## Rule

- Add a row only when it helps route a reader to a stable owner doc.
- Do not add per-feature implementation status, roadmap phase labels, plan links, or backlog state here.
- Put implementation ordering in `docs/backlog/` or a plan.
- Persisted model details remain authoritative in `model/*.orm.xml`.
