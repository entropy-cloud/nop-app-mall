# Project Context

## Purpose

Keep this file as the shortest current snapshot an AI agent needs before doing useful work.

Update it in place. Do not create dated copies.

## Project Identity

- Project name: nop-app-mall
- Product type: demo e-commerce application (mall) built on the Nop Platform
- Primary users: developers learning Nop Platform; demo/test users browsing the mall
- Current milestone: baseline implementation from litemall requirements
- Documentation freshness: `partially stale`

## Active Work

- Active requirement: `docs/requirements/mvp.md`
- Active owner doc: `docs/design/app-overview.md`
- Active plan: `none`
- Active backlog item: `docs/backlog/README.md#baseline-alignment`
- AI autonomy: `plan-first`
- Current blocker: `documentation baseline needs alignment with live code`

Rule:

- If active requirement is `none`, agents may help create or clarify requirements and context, but must not implement product behavior.
- If AI autonomy is not `implement`, agents must follow `docs/context/ai-autonomy-policy.md` before changing product behavior.
- If documentation freshness is `stale` or `unknown`, agents may research, audit, and draft alignment docs, but must not implement product behavior until the baseline is re-established or a human confirms the intended behavior.
- If documentation freshness is `partially stale`, agents may implement only slices whose active requirement, owner doc, codebase-map route, and touched code area have been verified fresh; otherwise treat the slice as `plan-first` or `research-only`.

## Current Technical Baseline

- Frontend stack: Baidu AMIS (JSON-driven UI in `.view.xml` files)
- Backend stack: Java 17+, Quarkus, Nop Platform (nop-entropy 2.0.0-SNAPSHOT)
- Database/model source: XML models in `model/app-mall.orm.xml`, `model/app-mall.api.xml`; generated ORM XML

## Verification Commands

| Purpose                   | Command                                                                                                                  |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| Install dependencies      | `./mvnw dependency:resolve -DskipTests` (requires nop-entropy parent built first)                                        |
| Run app locally           | `./mvnw clean package -DskipTests && java -Dfile.encoding=UTF8 -Dquarkus.profile=dev -jar app-mall-app/target/app-mall-app-1.0-SNAPSHOT-runner.jar` |
| Typecheck / compile check | `./mvnw compile -DskipTests`                                                                                             |
| Build                     | `./mvnw clean package -DskipTests`                                                                                       |
| Lint / static check       | `none`                                                                                                                   |
| Unit tests                | `./mvnw test`                                                                                                            |
| E2E / integration tests   | `none`                                                                                                                   |

## Optional Layers Currently In Use

Mark only the optional layers this project actually maintains.

- [ ] `docs/discussions/`
- [x] `docs/audits/`
- [ ] `docs/testing/`
- [x] `docs/skills/`
- [ ] `docs/analysis/`
- [ ] `docs/retrospectives/`
- [ ] `docs/lessons/`

## AI Block Conditions

AI MUST stop and wait for human input before proceeding when:

- verification commands are all placeholders and cannot be inferred from the project
- any change touches payment or data-deletion paths with no existing test coverage and no owner doc describing expected behavior
- any change modifies XML models (`model/*.orm.xml`, `model/*.api.xml`) without explicit human approval — these drive code generation

These are project-specific hard stops in addition to `AGENTS.md`, `docs/context/ai-autonomy-policy.md`, source-of-truth conflict rules, and required plan/closure audit rules.

For ambiguity that does not affect user-visible behavior, contracts, protected areas, or closure evidence, resolve by writing assumptions into the relevant doc and proceed according to the autonomy policy. Mark uncertain assumptions explicitly so humans can review later.

## Notes For AI Agents

- If this file is empty or stale, ask for or create a context update before large implementation work.
- AI may correct factual context from live repo evidence, but must not loosen autonomy, remove blockers, mark stale docs fresh, or downgrade protected areas without human confirmation or human-approved owner-doc evidence.
- Do not infer current milestone or active plan from chat alone.
- Do not report verification success while commands still contain `<fill real command>` placeholders.
- Building requires `nop-entropy` parent POM to be available in local Maven repository first.
