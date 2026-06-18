# Module Boundaries

## Purpose

Define the main code ownership boundaries for `nop-app-mall`.

## Application Shell

### app-mall-app

- Responsibility: Quarkus runnable application entry point
- Allowed dependencies: all other app-mall modules
- Forbidden dependencies: none (this is the aggregator)
- Owner docs: `docs/architecture/system-baseline.md`

## Domain Modules

### app-mall-api

- Responsibility: External API interfaces and shared DTOs
- Allowed dependencies: nop-entropy API libraries
- Forbidden dependencies: app-mall-service, app-mall-dao, app-mall-web
- Owner docs: `docs/architecture/system-baseline.md`

### app-mall-dao

- Responsibility: Data access layer — generated entity classes, ORM mappers
- Allowed dependencies: nop-entropy ORM libraries, app-mall-api
- Forbidden dependencies: app-mall-service, app-mall-web, app-mall-app
- Owner docs: `docs/architecture/system-baseline.md`, `model/app-mall.orm.xml`

### app-mall-service

- Responsibility: Business service implementations, BizModel classes, `*.xbiz.xml`
- Allowed dependencies: app-mall-api, app-mall-dao, nop-entropy service libraries
- Forbidden dependencies: app-mall-web, app-mall-app
- Owner docs: `docs/architecture/system-baseline.md`, `docs/design/app-overview.md`

### app-mall-web

- Responsibility: Frontend AMIS views in `.view.xml` files
- Allowed dependencies: app-mall-api, app-mall-service, nop-entropy web libraries
- Forbidden dependencies: app-mall-dao (access data through service layer)
- Owner docs: `docs/architecture/system-baseline.md`, `docs/design/app-overview.md`

### app-mall-wx

- Responsibility: WeChat Pay integration
- Allowed dependencies: app-mall-api, WeChat SDK
- Forbidden dependencies: app-mall-web, app-mall-dao
- Owner docs: `docs/architecture/system-baseline.md`

### app-mall-delta

- Responsibility: Delta customization layer over nop-auth
- Allowed dependencies: nop-auth modules
- Forbidden dependencies: app-mall-web, app-mall-service business logic
- Owner docs: `docs/design/roles-and-permissions.md`

### app-mall-meta

- Responsibility: Metadata module
- Allowed dependencies: nop-entropy meta libraries
- Forbidden dependencies: app-mall-web, app-mall-service
- Owner docs: `docs/architecture/system-baseline.md`

## Code Generation

### app-mall-codegen

- Responsibility: Code generation from XML models
- Allowed dependencies: nop-entropy codegen libraries
- Forbidden dependencies: runtime modules (service, web, app)
- Owner docs: `model/app-mall.orm.xml`, `model/app-mall.api.xml`

## Shared Data/Service Layer

- Shared via app-mall-api (interfaces) and app-mall-dao (entities)
- No shared UI components between admin and mall (separate AMIS pages)

## Test Ownership

- Unit/integration tests live in `app-mall-service/src/test/java/` (service-layer BizModel tests via IGraphQLEngine)
- Other modules have per-module `src/test/java/` when needed
- E2E tests in `e2e/` (Playwright, TypeScript): `e2e/tests/*.spec.ts` — storefront page rendering, auth, app startup smoke tests

## Rule

If a recurring design argument depends on module ownership, write the answer here instead of re-litigating it in chat.

## Deploy DDL vs ORM Model Sync Status

The ORM model `model/app-mall.orm.xml` is the source of truth for schema (columns, types, indexes). Three dialect DDL files under `deploy/sql/{mysql,postgresql,oracle}/_create_app-mall.sql` are used for manual production deployment only (dev/test runtime uses `init-database-schema: true` which builds the schema from the ORM model).

- **Index sync (updated 2026-06-16):** The platform DDL template `_create_{appName}.sql.xgen` (nop-entropy `ddl.xlib` `CreateTables`) does **not** emit `CREATE INDEX`, so `_create_app-mall.sql` contains only table definitions and primary keys — **zero indexes**. All 31 ORM `<index>` definitions are instead propagated to a separate, regen-safe file `deploy/sql/{dialect}/_create_index.sql` (MySQL/Oracle uppercase column names, PostgreSQL lowercase). Run `_create_index.sql` **after** `_create_app-mall.sql` during manual production deployment. Root cause lives in the platform (`CreateTables` does not call `AddIndex`); the project-side `_create_index.sql` is the mitigation.
- **Drift watch (non-blocking):** `_create_index.sql` covers only indexes. Other dimensions (column types, new columns) may still drift between ORM model and `_create_app-mall.sql`. Trigger to fully re-align: next full codegen DDL regeneration once the platform template emits indexes.
- **Deleted entities:** `LitemallAdmin`, `LitemallRole`, `LitemallPermission` are absent from all three DDL files (user/role/permission handled by `nop-auth` tables customized via `app-mall-delta`).
