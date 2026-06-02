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

- `docs/requirements/` owns what should be built for a specified slice or product baseline
- `docs/design/` owns the stable app-layer baseline after that slice is accepted
- `docs/architecture/` owns technical design and cross-feature structure
- `model/*.orm.xml` owns persisted entity shape, field sets, dictionaries, and ORM-model truth

When a feature depends on both business design and technical design, keep the two concerns in separate files and cross-reference them.

Design docs may keep business-facing entity names, state meanings, and transition rules.

Design docs should not duplicate table catalogs, field-by-field schema definitions, dictionary catalogs, or platform-specific implementation sections that belong in `model/*.orm.xml` or `docs/architecture/`.

## Starter Files

- `app-overview.md`
- `feature-inventory.md`
- `domain-design-guidelines.md`
- `roles-and-permissions.md`

## Detailed Business Design Docs

| Doc | Domain |
|-----|--------|
| `product-catalog.md` | Goods, category, brand, SKU, specifications, search |
| `order-and-cart.md` | Cart, order lifecycle, state machine, checkout, payment, shipping, refund, after-sale |
| `user-and-address.md` | User profile, authentication, address management, region |
| `marketing-and-promotions.md` | Coupon, groupon, search history, favorites, topics, ads |
| `system-configuration.md` | System config, file storage, notifications, notices, scheduled tasks, admin logs, statistics |

## Authoring Rule

- Keep `docs/design/` focused on business semantics, roles, workflows, and supported behavior.
- Use `../nop-entropy/docs-for-ai/02-core-guides/application-project-docs-and-domain-design.md` for general Nop application owner-doc and domain-design rules.
- Use `domain-design-guidelines.md` only for the mall-specific domain ownership map and local interpretation.
- When a doc needs persisted-model detail, cite `model/app-mall.orm.xml` instead of restating the schema.
- When a doc needs implementation strategy, cite `docs/architecture/` instead of embedding platform detail here.
- Do not use design docs as a roadmap or implementation-status tracker. Put implementation order in `docs/backlog/` or plans.

## Reference Projects

Design informed by study of these widely-used Java e-commerce open source projects:

| Project | Stars | Relevance |
|---------|-------|-----------|
| [macrozheng/mall](https://github.com/macrozheng/mall) (83k+) | Enterprise-grade Spring Boot + MyBatis e-commerce system | Architecture patterns, promotion model, RBAC |
| [linlinjava/litemall](https://github.com/linlinjava/litemall) (20k+) | Spring Boot + Vue + WeChat mini-program mall | Direct requirements source, order state machine, groupon model |
| [YunaiV/yudao-cloud](https://github.com/YunaiV/yudao-cloud) (19k+) | Spring Cloud Alibaba multi-tenant system | Multi-tenant patterns, permission model |
