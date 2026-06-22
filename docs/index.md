# nop-app-mall Docs Index

## Purpose

This `docs/` tree is the durable memory and routing surface for `nop-app-mall`.

- start here before making workflow, requirement, design, or implementation changes
- prefer the smallest file that answers the current question
- keep durable conclusions in files, not only in chat

## Routing Authority

This file is the top-level docs router.

- `docs/index.md` owns navigation and directory responsibilities
- `AGENTS.md` owns agent workflow rules and execution expectations
- `docs/design/` and `docs/architecture/` own the stable project attractor
- `model/*.orm.xml` and `model/*.api.xml` own persisted model and generated contract truth

## Read This First

| If you need to...                                                    | Read this first                                     | Then read                                                                                                                                      |
| -------------------------------------------------------------------- | --------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| Understand mandatory AI context and current project state            | `docs/context/README.md`                            | `docs/context/project-context.md`, `docs/context/ai-autonomy-policy.md`, `docs/context/codebase-map.md`                                        |
| Run, build, or verify the project                                    | `docs/context/project-context.md`                   | `docs/context/codebase-map.md`, `docs/architecture/system-baseline.md`                                                                         |
| Understand the lightweight default development workflow              | `docs/process/application-development-workflow.md`  | `AGENTS.md`                                                                                                                                    |
| Choose the next AI-ready work item                                   | `docs/backlog/README.md`                            | `docs/context/ai-autonomy-policy.md`, the relevant requirement and owner doc                                                                   |
| Read raw PM, prototype, article, or card-set inputs                  | `docs/input/README.md`                              | the active file in `docs/input/`                                                                                                               |
| Read explanatory methodology articles                                | `docs/articles/README.md`                           | the relevant article under `docs/articles/`                                                                                                    |
| Clarify ambiguous requirements                                       | `docs/discussions/README.md`                        | `docs/requirements/00-requirement-synthesis-guide.md`                                                                                          |
| Route a task before coding                                           | `AGENTS.md`                                         | `docs/skills/README.md`, the relevant owner doc, and `docs/plans/00-plan-authoring-and-execution-guide.md`                                     |
| Decide whether an existing skill applies                             | `docs/skills/README.md`                             | the relevant owner doc and requirement                                                                                                         |
| Understand the project goal and product shape                        | `docs/architecture/project-vision.md`               | `docs/design/app-overview.md`                                                                                                                  |
| Understand the stable app-layer baseline                             | `docs/design/app-overview.md`                       | `docs/design/feature-inventory.md`, `docs/design/domain-design-guidelines.md`, `docs/design/roles-and-permissions.md`                         |
| Plan mobile frontend work (nop-chaos-flux)                           | `docs/backlog/mobile-frontend-roadmap.md`            | `docs/backlog/enhanced-features-roadmap.md` (shared backend APIs)                                                                            |
| Understand the stable technical baseline                             | `docs/architecture/system-baseline.md`              | `docs/architecture/module-boundaries.md`                                                                                                       |
| Understand Nop implementation decision order                         | `../nop-entropy/docs-for-ai/00-start-here/application-project-defaults.md` | `../nop-entropy/docs-for-ai/02-core-guides/architecture-principles.md`, `../nop-entropy/docs-for-ai/02-core-guides/model-first-development.md`, `../nop-entropy/docs-for-ai/02-core-guides/service-layer.md` |
| Decide whether a change should use model, Delta, hooks, or Java      | `../nop-entropy/docs-for-ai/INDEX.md`               | `../nop-entropy/docs-for-ai/03-runbooks/prefer-delta-over-direct-modification.md`, `../nop-entropy/docs-for-ai/03-runbooks/extend-crud-with-hooks.md`, `../nop-entropy/docs-for-ai/03-runbooks/write-bizmodel-method.md` |
| Implement standard CRUD or CRUD-plus-extra-logic                     | `../nop-entropy/docs-for-ai/02-core-guides/service-layer.md` | `../nop-entropy/docs-for-ai/03-runbooks/extend-crud-with-hooks.md`, `../nop-entropy/docs-for-ai/03-runbooks/choose-entity-bizmodel-processor.md` |
| Understand persisted model or dictionary truth                       | `docs/context/source-of-truth-and-precedence.md`    | `model/app-mall.orm.xml`, `model/app-mall.api.xml`                                                                                             |
| Look up Nop Platform APIs, patterns, or conventions                  | `../nop-entropy/docs-for-ai/INDEX.md`               | the relevant guide under `nop-entropy/docs-for-ai/`                                                                                            |
| Understand owner-doc precedence and source-of-truth boundaries       | `docs/context/source-of-truth-and-precedence.md`    | the relevant owner doc                                                                                                                         |
| Start or review a non-trivial implementation                         | `AGENTS.md`                                         | `docs/skills/README.md`, `docs/plans/00-plan-authoring-and-execution-guide.md`, the relevant plan if one exists, and `docs/audits/00-audit-execution-guide.md` |
| Review a planned or completed slice                                  | the relevant file under `docs/plans/`               | ordinary plan/closure audit evidence is in the plan; use `docs/audits/` only for specialized audit records                                    |
| Review audit workflows or specialized audit records                  | `docs/audits/00-audit-execution-guide.md`           | the relevant prompt in `docs/skills/`                                                                                                          |
| Understand which docs should use dated filenames versus stable names | `docs/references/document-naming-and-timeliness.md` | the relevant guide in the target directory                                                                                                     |
| Quickly copy a recommended filename pattern for a new dated document | `docs/references/document-naming-and-timeliness.md` | the `Quick Copy Set` section                                                                                                                   |
| Copy a ready-made dated document skeleton                            | `docs/examples/README.md`                           | rename the closest `.example.md` file                                                                                                          |
| See one realistic small feature walkthrough                          | `docs/examples/complete-small-app-walkthrough.md`   | then copy the closest skeleton from `docs/examples/`                                                                                           |
| Check what docs must be updated after a change                       | `docs/references/maintenance-checklist.md`          | the most relevant file in `docs/design/` or `docs/architecture/`                                                                               |
| Review recent implementation history                                 | `docs/logs/index.md`                                | the latest dated log file                                                                                                                      |
| Look up a past subtle regression                                     | `docs/bugs/00-bug-fix-note-writing-guide.md`        | the relevant file in `docs/bugs/`                                                                                                              |
| Record or review exploratory/manual testing                          | `docs/testing/index.md`                             | the relevant dated test note                                                                                                                   |
| Check the latest known-good verification state                       | `docs/testing/known-good-baselines.md`              | latest dated testing or log note                                                                                                               |
| Review tradeoffs or open design investigations                       | `docs/analysis/README.md`                           | the relevant analysis note                                                                                                                     |
| Review durable reusable engineering lessons                          | `docs/lessons/README.md`                            | the relevant numbered lesson                                                                                                                   |
| Read implementation-ready requirements                               | `docs/requirements/README.md`                       | the requirement file relevant to the selected slice                                                                                            |
| Review why a landed result still missed expectation                  | `docs/retrospectives/README.md`                     | the relevant retrospective note                                                                                                                |

## Recommended Default Path

For most small and medium projects, the default path is:

1. `docs/context/`
2. `docs/backlog/` when choosing work
3. `docs/input/`
4. `docs/requirements/`
5. `docs/design/`, `docs/architecture/`, and `model/*.orm.xml` when the question touches persisted model truth
6. route the task and select candidate reusable skills
7. `docs/plans/` when planning triggers apply
8. `docs/audits/` only when specialized, complex, disputed, reusable, or future-replay-worthy audit evidence needs a separate file
9. `docs/logs/`
10. `docs/bugs/` when needed

Use `docs/discussions/`, extra `docs/testing/` notes, `docs/skills/`, `docs/analysis/`, and `docs/retrospectives/` only when the task complexity or ambiguity justifies them.

## Skill Routing

| If the task is...                       | Read this first                                       | Then decide                                                                                               |
| --------------------------------------- | ----------------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| unclear requirement                     | `docs/requirements/00-requirement-synthesis-guide.md` | whether a requirement file or discussion file is needed first                                             |
| non-trivial implementation              | `AGENTS.md`                                           | which skills are needed per phase or item, then use `docs/plans/00-plan-authoring-and-execution-guide.md` |
| document, plan, or closure verification | `docs/skills/README.md`                               | which audit prompt or review skill applies                                                                |
| repeated known method or review pattern | the relevant owner doc                                | whether an existing skill applies or a new one should be created                                          |

Skills select the work method. They do not replace requirements, design, architecture, or owner-doc routing.

## Directory Roles

- `docs/process/` - workflow and operating process documents
- `docs/context/` - mandatory AI context, owner precedence, and project-wide conventions
- `docs/backlog/` - roadmap, implementation ordering, candidate work, and AI-ready next actions
- `docs/input/` - raw external inputs and copied source material
- `docs/discussions/` - optional requirement clarification and unresolved question records
- `docs/requirements/` - synthesized implementation-ready requirement docs
- `docs/design/` - stable app-layer feature and business-flow owner docs
- `docs/architecture/` - stable technical baseline and module-boundary docs
- `model/` - XML model source of truth for persisted entities, dictionaries, and generated contracts
- `docs/lessons/` - durable engineering lessons extracted from repeated issues and recoveries
- `docs/references/` - stable lookup guides and maintenance aids
- `docs/articles/` - outward-facing methodology and explanatory articles
- `docs/examples/` - small copyable skeletons for dated working documents
- `docs/plans/` - execution plans with closure criteria
- `docs/audits/` - audit methods and specialized audit records; ordinary plan/closure audit evidence belongs in the plan by default
- `docs/skills/` - optional reusable AI prompts and audit/review playbooks
- `docs/logs/` - dated implementation memory
- `docs/testing/` - optional exploratory and manual testing notes
- `docs/bugs/` - complex regression history and root-cause notes
- `docs/analysis/` - optional investigations, comparisons, and design tradeoffs
- `docs/retrospectives/` - optional post-delivery gap analysis and process improvements
- `docs/archive/` - inactive documents moved here by human decision; kept for historical reference

## Core Principle

Use files for durable truth.

- input captures where requirements came from
- context captures mandatory project rules and source-of-truth precedence
- backlog captures roadmap and implementation-order signals
- discussions capture what was unclear
- requirements capture what should be built
- design and architecture capture business and technical owner truth
- model files capture persisted data and generated contract truth
- source-of-truth precedence tells which artifact wins for each question
- plans capture how a non-trivial slice will be closed
- audits capture how claims were challenged
- logs, tests, and bug notes preserve proof and memory
- retrospectives explain why the last iteration still missed the mark

## Naming Rule

- stable owner docs keep stable names
- time-sensitive records should usually include dates
- see `docs/references/document-naming-and-timeliness.md`
