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

- Tests live in each module's `src/test/java/`
- Integration tests in `app-mall-app/src/test/`
- No E2E test framework currently configured

## Rule

If a recurring design argument depends on module ownership, write the answer here instead of re-litigating it in chat.
