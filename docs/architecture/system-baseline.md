# System Baseline

## Purpose

Record the current supported implementation baseline for `nop-app-mall`.

## Runtime Shape

- Monolithic Quarkus application packaged as uber-jar
- Runs with `java -jar app-mall-app/target/app-mall-app-1.0-SNAPSHOT-runner.jar`
- Supports dev and prod profiles (`-Dquarkus.profile=dev`)
- Supports native image compilation via GraalVM

## Frontend Stack

The application has two independent frontend surfaces sharing the same backend GraphQL APIs:

### Web / Admin Console (existing, delivered)

- Baidu AMIS (JSON-driven low-code UI framework)
- Views defined as `.view.xml` files in `app-mall-web/src/main/resources/_vfs/`
- No separate frontend build step — AMIS renders JSON server-side
- AMIS editor available via `nop-web-amis-editor` dependency

### Mobile Mall H5 (M1 delivered; M2+ per `docs/backlog/mobile-frontend-roadmap.md`)

- **nop-chaos-flux** — JSON-to-React low-code framework (write JSON Schema, engine compiles & renders to React 19). Same paradigm as AMIS but for the mobile surface.
- Mobile pages consume the already-delivered backend GraphQL/REST RPC APIs (Phase 1-14); no new backend work for mobile
- Mobile development MUST reference and directly reuse nop-chaos-flux's mobile mechanism; do not build a bespoke mobile UI stack
- Mobile native components come from `flux-renderers-mobile` package (pull-refresh / infinite-scroll / swipe-cell / countdown / notice-bar); event-driven, request sinking — components never hold data-fetch logic, all requests go through action/data-source layer
- State management on mobile: Zustand (see State Management Approach below)

**Engineering landing (Decision D1, cross-repo):** the mobile app lives in the nop-chaos-flux monorepo at `apps/mall-mobile/` (`@nop-chaos/mall-mobile`), consuming flux packages via `workspace:*`. It is NOT placed inside the `nop-app-mall` Maven repo because all flux packages are `private:true` workspace packages — cross-repo consumption would require publishing or `pnpm link`, and the canonical `apps/playground/` reference app already lives inside the monorepo. This mirrors the existing `nop-entropy` sibling-repo dependency pattern (mall plan docs drive flux-repo code; delivery is cross-repo). `pnpm-workspace.yaml` already globs `apps/*`, so the app is auto-discovered.

**Auth consumption layer (Decision D3):** the mobile app purely consumes the existing `LoginApi` (platform `login`/`logout`/`refreshToken` + delta `signUp`/`sendResetCode`/`resetPassword`) over Nop's REST RPC transport `POST /r/LoginApi__<op>` with bare-args JSON body and `{status:0,msg,data}` envelope. No new backend mutations/queries.

- `src/env.ts` `createFetcher` — the flux `RendererEnv.fetcher`: injects `Authorization: Bearer <accessToken>` from the Zustand store, parses the Nop envelope (`status:0` ⇒ ok), and on HTTP 401 calls `refreshAccessToken` then **replays the original request once** with the new token; refresh failure ⇒ `onUnauthorized` ⇒ redirect to login.
- `src/auth/refresh.ts` `refreshAccessToken` — **single-flight**: concurrent 401s share one in-flight refresh promise (one `LoginApi__refreshToken` network call), writing the refreshed token back to the store. Refresh failure clears auth.
- `src/store/` (Zustand + `persist`) — `accessToken` / `refreshToken` / `userInfo` persisted to `localStorage` (partialized; `cartBadge` is session-only). `src/guards/require-auth.ts` implements **half-guest mode**: a protected-action set (add-to-cart / checkout / view-profile / orders / collect / …) is intercepted when logged out, redirecting to `#/auth/login?returnTo=<original intent>`; after login the user returns to the original intent.
- Vite dev server proxies `/api` (GraphQL) and `/r` (REST RPC) to the running Quarkus backend (`MALL_BACKEND_ORIGIN`, default `http://localhost:8080`); CORS is avoided by the dev proxy (production reverse-proxy is a deferred successor).

**Reference location (nop-chaos-flux repo):** `~/app/nop-chaos-flux-wt/nop-chaos-flux-master/`

- `apps/mall-mobile/` — the delivered M1 app (scaffold + router + Tab shell + Zustand store + auth pages + token intercept + half-guest guard)
- `flux-guide/README.md` — core architecture + file index
- `flux-guide/01-quickstart.md` — 17 most-used code snippets
- `flux-guide/02-reference.md` — expression syntax, API config, event system, Action Algebra
- `flux-guide/flux-types/` — TypeScript interfaces for all components (the authoritative field knowledge source)
- `flux-guide/design-patterns/` — business-scenario cookbook
- `flux-guide/mobile/` — mobile native components topic (`README.md` + per-component guides)
- `packages/flux-renderers-mobile/` — mobile renderer implementations

## Backend Stack

- Java 17+
- Quarkus framework
- Nop Platform (nop-entropy 2.0.0-SNAPSHOT)
- Maven multi-module project (9 modules)
- Nop Platform components: `nop-auth`, `nop-sys`, `nop-quarkus-web-orm-starter`

## State Management Approach

- Server-side state via Quarkus
- AMIS manages client-side form state for the web/admin console
- Mobile (nop-chaos-flux) uses Zustand for client-side state (user info, cart badge, etc.)
- No state management library on the AMIS web side

## Data Access Approach

- Nop ORM with generated entity classes and mappers
- Code generated from `model/app-mall.orm.xml`
- Supports MySQL, PostgreSQL, Oracle, H2 (for testing)
- Auto-creates schema on first start
- SQL libraries in `*.sql-lib.xml` files

## Testing Stack

- JUnit (via Quarkus test framework)
- H2 in-memory database for testing

## Build And Package Tools

- Maven with Maven Wrapper (`mvnw`/`mvnw.cmd`)
- Requires `nop-entropy` parent POM in local repository
- Build: `./mvnw clean package -DskipTests`
- Code generation: `codegen.sh` / `codegen.bat`

## Deployment Shape

- Single uber-jar deployment
- Docker support via `.dockerignore` and build scripts
- Dev/prod profile switching via Quarkus
- Database auto-initialization on first start

## External Platforms Or Enterprise Systems

- WeChat Pay (`app-mall-wx` module)
  - SDK: `wechatpay-java` 0.2.17 (官方 SDK, 公钥模式)
  - 支付场景: Native 扫码（已验证），JSAPI/H5/App 预留架构
  - 配置驱动: `WxPayServiceImpl.enabled` 控制是否启用微信支付；`enabled=false` 时走模拟支付路径（日志 + 模拟响应）
  - 回调端点: `POST /wxpay/notify`（通过 `app-mall-wx` JAX-RS 资源）
  - IoC 绑定: `PayService` 接口 — `WxPayServiceImpl` 为 `app-wx.beans.xml` 中注册的唯一活跃 bean；`enabled=false` 时内部退化为演示模式（模拟响应），不涉及 bean 切换或 `MockPayServiceImpl`
  - 模块构成: `app-mall-api`（请求/响应 bean 与接口）、`app-mall-wx`（微信实现与回调端点）
- File storage (local or cloud)

## Reporting And Notification Route

- Reporting should prefer the platform `nop-report` module as the default implementation route when the business requirement is already settled and the app needs managed datasets, templates, or exportable report outputs.
- **Deviation recorded (2026-06-13, Plan Phase 2B):** For the current requirement (admin statistics dashboard with simple aggregation queries + chart display), BizModel statistics methods + AMIS chart components were chosen over `nop-report`. Rationale: lighter weight, faster development, no heavy dependency needed for simple aggregation. Trigger for revisiting: when operations need complex reports with multi-level grouping, cross-tables, or Excel export, introduce `nop-report` at that time.
- Notification delivery should prefer platform integration capabilities instead of ad hoc channel code. In practice, `nop-integration` is the default route for email, SMS, and similar outbound channels.
- **Deviation recorded (2026-06-13, Plan Phase 1B):** `MallNotificationService` uses `ISmsSender` for SMS delivery but does not persist notification records (no `NopSysNotice` integration). Rationale: the current notification requirement is fire-and-forget SMS delivery; notification history can be added later when the business requires it. The `NopSysNotice` integration remains the recommended path when audit trail is needed.
- App-layer design should stay at the business level: who receives which notification, from which business event, and what report view is required. Channel adapters, report-engine configuration, dataset wiring, retry strategy, and delivery integration details belong to architecture and implementation.
- Current live evidence already shows SMS integration usage via `MallNotificationService` wrapping `io.nop.integration.api.sms.ISmsSender`. Additional channels such as email should follow the same integration-first pattern instead of introducing parallel bespoke notification infrastructure.

## Stable Rules

- Module dependency order: codegen -> api -> dao -> service -> web -> app; wx/delta/meta are additional modules
- Never edit generated code; always regenerate from XML models
- Delta customization in `app-mall-delta` for overriding platform behavior
- AMIS views follow AMIS JSON conventions
- Business logic in `*.xbiz.xml` and BizModel Java classes
- SQL changes must align with XML model changes

## Nop Implementation Defaults

- Default decision order: business fact from local owner docs, then Nop implementation route from `../nop-entropy/docs-for-ai/00-start-here/application-project-defaults.md` and `../nop-entropy/docs-for-ai/02-core-guides/architecture-principles.md`.
- Default technical order: `Model -> Delta -> Java`; see `../nop-entropy/docs-for-ai/02-core-guides/model-first-development.md`.
- Standard entity services should stay on `CrudBizModel<T>` when possible; see `../nop-entropy/docs-for-ai/02-core-guides/service-layer.md`.
- Standard CRUD should prefer generated/meta-driven behavior first. If CRUD needs extra pre/post handling, prefer `defaultPrepareSave(...)`, `defaultPrepareUpdate(...)`, `defaultPrepareQuery(...)`, `defaultPrepareDelete(...)`, or related hooks before creating heavier custom flows; see `../nop-entropy/docs-for-ai/03-runbooks/extend-crud-with-hooks.md`.
- When customizing existing platform/app resources, prefer Delta overlays instead of direct modification; see `../nop-entropy/docs-for-ai/03-runbooks/prefer-delta-over-direct-modification.md`.

## Update Rule

When the supported baseline changes, update this file in the same change.
