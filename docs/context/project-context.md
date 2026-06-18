# Project Context

## Purpose

Keep this file as the shortest low-churn context an AI agent needs before doing useful work.

This file is not an execution dashboard. Do not mirror active plans, current blockers, current backlog rows, or dated audit/log status here. Discover those from their owning files when needed.

## Project Identity

- Project name: nop-app-mall
- Product type: commercial-grade e-commerce application (mall) built on the Nop Platform
- Primary users: mall shoppers, mall operators/admins, and developers learning Nop Platform from a realistic business app
- Product baseline: formal commercial mall behavior derived from litemall requirements and Nop Platform conventions
- Execution state: owned by the relevant requirement, backlog/roadmap, plan, audit, and log files; not mirrored here

Rule:

- Before changing product behavior, identify the requirement or roadmap slice that authorizes the change and the owner docs that control the touched behavior.
- If AI autonomy is not clearly `implement` for the selected backlog/roadmap item, follow `docs/context/ai-autonomy-policy.md` before changing product behavior.
- If the relevant requirement, owner doc, or code route is stale or disputed, treat the slice as `plan-first` or `research-only` until the baseline is re-established or a human confirms the intended behavior.

## Current Technical Baseline

- Frontend stack: Baidu AMIS (JSON-driven UI in `.view.xml` files)
- Backend stack: Java 17+, Quarkus, Nop Platform (nop-entropy 2.0.0-SNAPSHOT)
- Database/model source: XML models in `model/app-mall.orm.xml`, `model/app-mall.api.xml`; generated ORM XML

## Documentation Boundary Reminder

- `docs/design/` owns business semantics, roles, workflows, and state transitions.
- `docs/architecture/` owns technical structure and implementation strategy.
- `model/*.orm.xml` and `model/*.api.xml` remain authoritative for persisted model and generated contract truth.
- `docs/backlog/` owns roadmap and implementation-order signals.
- `docs/plans/` and `docs/logs/` are ordinary harness records for execution and trajectory memory; `docs/audits/` is for specialized audit records, not routine plan-local audit evidence.

## Verification Commands

| Purpose                   | Command                                                                                                                  |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| Install dependencies      | `./mvnw dependency:resolve -DskipTests` (requires nop-entropy parent built first)                                        |
| Run app locally           | `./mvnw clean package -DskipTests && java -Dfile.encoding=UTF8 -Dquarkus.profile=dev -jar app-mall-app/target/app-mall-app-1.0-SNAPSHOT-runner.jar` |
| Typecheck / compile check | `./mvnw compile -DskipTests`                                                                                             |
| Build                     | `./mvnw clean package -DskipTests`                                                                                       |
| Lint / static check       | `none`                                                                                                                   |
| Unit tests                | `./mvnw test`                                                                                                            |
| E2E / integration tests   | `cd e2e && npx playwright test`                                                                                          |

## AI Block Conditions

AI MUST stop and wait for human input before proceeding when:

- verification commands are all placeholders and cannot be inferred from the project
- any change touches payment or data-deletion paths with no existing test coverage and no owner doc describing expected behavior
- any change modifies XML models (`model/*.orm.xml`, `model/*.api.xml`) without explicit human approval — these drive code generation

These are project-specific hard stops in addition to `AGENTS.md`, `docs/context/ai-autonomy-policy.md`, source-of-truth conflict rules, and required plan/closure audit rules.

For ambiguity that does not affect user-visible behavior, contracts, protected areas, or closure evidence, resolve by writing assumptions into the relevant doc and proceed according to the autonomy policy. Mark uncertain assumptions explicitly so humans can review later.

## Notes For AI Agents

- If this file is empty or factually wrong, ask for or create a context update before large implementation work.
- AI may correct factual context from live repo evidence, but must not loosen autonomy, downgrade protected areas, or declare disputed product behavior settled without human confirmation or human-approved owner-doc evidence.
- Do not infer current execution state from chat alone; read the relevant backlog/roadmap, plan, audit, or log files.
- Do not report verification success while commands still contain `<fill real command>` placeholders.
- Building requires `nop-entropy` parent POM to be available in local Maven repository first.
