# Codebase Map

## Purpose

This file gives AI agents a compact map of the live repository so they do not rediscover the structure by repeatedly searching imports and directories.

Keep it current enough to route common work. Do not turn it into a full architecture document.

## Entry Points

| Area              | Path                                        | Notes                                              | Last Verified | Confidence |
| ----------------- | ------------------------------------------- | -------------------------------------------------- | ------------- | ---------- |
| Frontend app      | `app-mall-web/src/main/resources/`          | AMIS JSON views in `_vfs/app/mall/pages/`          | 2026-06-02    | high       |
| Backend app entry | `app-mall-app/`                             | Quarkus uber-jar, `quarkus-maven-plugin`           | 2026-06-02    | high       |
| API layer         | `app-mall-api/src/main/java/`               | External API interfaces                            | 2026-06-02    | high       |
| Service layer     | `app-mall-service/src/main/java/`           | BizModel classes, business logic                   | 2026-06-02    | high       |
| DAO layer         | `app-mall-dao/src/main/java/`               | Entity classes, ORM mappers                        | 2026-06-02    | high       |
| Code generation   | `app-mall-codegen/src/main/java/`           | Code generator from XML models                     | 2026-06-02    | high       |
| Tests             | `*/src/test/java/`                          | Unit tests per module                              | 2026-06-02    | medium     |
| Config            | `app-mall-app/src/main/resources/`          | Quarkus config, application.yaml                   | 2026-06-02    | high       |
| SQL deploy        | `deploy/sql/`                               | MySQL, PostgreSQL, Oracle DDL scripts              | 2026-06-02    | high       |
| Engineering tools | `tools/`                                    | Node.js scripts for docs/code quality checks       | 2026-06-02    | medium     |
| XML models        | `model/app-mall.orm.xml`, `model/app-mall.api.xml`, `model/nop-auth-delta.orm.xml` | ORM, API, and delta code generation source | 2026-06-02    | high       |

## Common Change Routes

| Task Type                         | Start Here                    | Then Check                          | Verification          | Last Verified | Confidence |
| --------------------------------- | ----------------------------- | ----------------------------------- | --------------------- | ------------- | ---------- |
| Add page/screen                   | `app-mall-web/src/main/resources/_vfs/app/mall/pages/` | `model/app-mall.api.xml` | `./mvnw compile -DskipTests && run app` | 2026-06-02 | high |
| Add API/handler                   | `app-mall-service/src/main/java/` | `model/app-mall.api.xml`      | `./mvnw test`         | 2026-06-02    | medium     |
| Change model/schema               | `model/app-mall.orm.xml`     | `app-mall-dao/`, `deploy/sql/`      | `./codegen.sh` then `./mvnw compile` | 2026-06-02 | high |
| Change permissions                | `app-mall-delta/`             | `docs/design/roles-and-permissions.md` | `./mvnw test`    | 2026-06-02    | low        |
| Fix UI behavior                   | `app-mall-web/src/main/resources/_vfs/` | AMIS docs                 | visual check          | 2026-06-02    | medium     |
| Add business logic                | `app-mall-service/src/main/java/` | `*.xbiz.xml` files             | `./mvnw test`         | 2026-06-02    | medium     |
| Change WeChat Pay                 | `app-mall-wx/src/main/java/`  | `app-mall-api/` (PayService)        | `./mvnw test`         | 2026-06-02    | low        |

## Large Or Fragile Files

| Path                                    | Risk                               | Preferred Approach                                   |
| --------------------------------------- | ---------------------------------- | ---------------------------------------------------- |
| `model/app-mall.orm.xml`               | Code generation source of truth    | Edit model then regenerate, never edit generated code |
| `model/app-mall.api.xml`               | API generation source of truth     | Edit model then regenerate                            |
| `model/nop-auth-delta.orm.xml`         | Delta ORM model for nop-auth overrides | Edit model then regenerate, drives app-mall-delta |
| `docs/input/litemall-requirements.md`   | Very large (1876+ lines)           | Reference for requirement synthesis, not direct impl  |
| `deploy/sql/mysql/`                     | Database DDL                       | Align with ORM model changes                          |
| `app-mall-delta/`                       | Overrides nop-auth via delta mech  | Respect Nop delta customization rules                 |
| `*_gen.xml` or files in `target/`       | Auto-generated, will be overwritten| Never edit; regenerate from XML models                |

## Project-Specific Search Hints

- Use file patterns: `**/*.xbiz.xml` for business logic, `**/*.view.xml` for AMIS pages, `**/*.orm.xml` for ORM mappings
- Use content anchors: BizModel classes extend `CrudBizModel<T>` and use `@BizModel` annotation
- Avoid editing generated files: check for `_gen` suffix or files under `target/` directories
- For AMIS views: search `app-mall-web/src/main/resources/_vfs/`
- For delta customizations: search `app-mall-delta/src/main/resources/_vfs/`
- XML models use `.orm.xml` for ORM and `.api.xml` for API definitions; use `nop-cli convert` to generate XLSX when needed

## Update Rule

Update this file when a change creates a new major entry point, moves common code, adds a new test location, or repeatedly causes agents to rediscover the same path.

If a listed path is missing, placeholders remain, or live imports contradict this map, do not treat the map as authority. Verify with the live repo, then update the map or mark the row low confidence before implementation.

If `Last Verified` is old for the project's pace, predates major structural changes, or the task touches a listed route's boundary, verify the live repo before relying on the row. Low-confidence rows do not block low-risk work after live verification, but protected-area, migration, or cross-module work should update the row before implementation.
