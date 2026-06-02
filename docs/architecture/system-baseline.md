# System Baseline

## Purpose

Record the current supported implementation baseline for `nop-app-mall`.

## Runtime Shape

- Monolithic Quarkus application packaged as uber-jar
- Runs with `java -jar app-mall-app/target/app-mall-app-1.0-SNAPSHOT-runner.jar`
- Supports dev and prod profiles (`-Dquarkus.profile=dev`)
- Supports native image compilation via GraalVM

## Frontend Stack

- Baidu AMIS (JSON-driven low-code UI framework)
- Views defined as `.view.xml` files in `app-mall-web/src/main/resources/_vfs/`
- No separate frontend build step — AMIS renders JSON server-side
- AMIS editor available via `nop-web-amis-editor` dependency

## Backend Stack

- Java 17+
- Quarkus framework
- Nop Platform (nop-entropy 2.0.0-SNAPSHOT)
- Maven multi-module project (9 modules)
- Nop Platform components: `nop-auth`, `nop-sys`, `nop-quarkus-web-orm-starter`

## State Management Approach

- Server-side state via Quarkus
- AMIS manages client-side form state
- No separate state management library

## Data Access Approach

- Nop ORM with generated entity classes and mappers
- Code generated from `model/app-mall.orm.xlsx`
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
- File storage (local or cloud)

## Stable Rules

- Module dependency order: codegen -> api -> dao -> service -> web -> app; wx/delta/meta are additional modules
- Never edit generated code; always regenerate from Excel models
- Delta customization in `app-mall-delta` for overriding platform behavior
- AMIS views follow AMIS JSON conventions
- Business logic in `*.xbiz.xml` and BizModel Java classes
- SQL changes must align with Excel model changes

## Update Rule

When the supported baseline changes, update this file in the same change.
