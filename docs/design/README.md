# Design Docs Index

## Purpose

`docs/design/` holds stable app-layer owner docs.

Use this directory for:

- product feature baselines
- page and flow behavior
- roles and permissions
- app-shell behavior

Use `docs/architecture/` for cross-cutting technical structure.

## Scope Boundary

- `docs/requirements/` owns what should be built for the current slice
- `docs/design/` owns the stable app-layer baseline after that slice is accepted
- `docs/architecture/` owns technical design and cross-feature structure

When a feature depends on both business design and technical design, keep the two concerns in separate files and cross-reference them.

## Starter Files

- `app-overview.md`
- `feature-inventory.md`
- `roles-and-permissions.md`

## Detailed Business Design Docs

| Doc | Domain | MVP Status |
|-----|--------|------------|
| `product-catalog.md` | Goods, category, brand, SKU, specifications, search | Core (implemented) |
| `order-and-cart.md` | Cart, order lifecycle, state machine, checkout, payment, shipping | Core (implemented) |
| `user-and-address.md` | User profile, authentication, address management, region | Core (implemented) |
| `marketing-and-promotions.md` | Coupon, groupon, search history, favorites, topics, ads | Deferred |
| `system-configuration.md` | System config, file storage, notifications, scheduled tasks, admin logs | Partial (deferred items noted) |

## Reference Projects

Design informed by study of these widely-used Java e-commerce open source projects:

| Project | Stars | Relevance |
|---------|-------|-----------|
| [macrozheng/mall](https://github.com/macrozheng/mall) (83k+) | Enterprise-grade Spring Boot + MyBatis e-commerce system | Architecture patterns, promotion model, RBAC |
| [linlinjava/litemall](https://github.com/linlinjava/litemall) (20k+) | Spring Boot + Vue + WeChat mini-program mall | Direct requirements source, order state machine, groupon model |
| [YunaiV/yudao-cloud](https://github.com/YunaiV/yudao-cloud) (19k+) | Spring Cloud Alibaba multi-tenant system | Multi-tenant patterns, permission model |
