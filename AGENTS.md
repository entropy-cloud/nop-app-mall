# AGENTS.md

## Project Intent

`nop-app-mall` is a demo e-commerce application (mall) built on the Nop Platform, using Attractor-Guided Engineering workflow for AI-assisted application development.

This repository is an application-layer product built on the Nop low-code platform (nop-entropy), not a framework-core project.

The repo is the source of truth. Chat is only a temporary working surface.

Before writing non-trivial code, agents must first understand:

- `docs/context/project-context.md`
- `docs/context/ai-autonomy-policy.md`
- `docs/context/codebase-map.md`
- the active requirement listed in project context
- the active owner doc listed in project context
- the relevant raw inputs under `docs/input/` when requirement meaning depends on source material

Read `docs/context/source-of-truth-and-precedence.md` when facts conflict or you are unsure which artifact owns the answer.
Read `docs/process/application-development-workflow.md` when planning or workflow decisions are part of the task.

## Task Routing

Before writing code, agents MUST classify the task first:

1. Determine the task type:
   - requirement clarification
   - app-layer design change
   - architecture change
   - implementation-only change
   - bug investigation
   - verification or audit work
2. Use `docs/index.md` to read the owner docs for that task type before acting.
3. Check `docs/skills/README.md` for candidate reusable skills before drafting or revising a plan.
4. For non-trivial work, record the chosen route and planned skill usage in the plan before implementation.

Do not jump from a feature request directly to code unless the route is already obvious from the active requirement and owner docs.

## Operating Rules

1. Prefer file-in, file-out collaboration.
2. Do not treat chat summaries as durable project memory.
3. Do not jump from raw PM text or prototype screenshots straight to code when scope is still unclear.
4. If input is ambiguous, first create or update a file in `docs/discussions/` or `docs/requirements/`.
5. Create or update a plan before implementation when the planning triggers below apply.
6. Keep `docs/design/` and `docs/architecture/` focused on the current supported baseline, not migration history.
7. Keep logs short, dated, and append-only. After completing any significant code change, you MUST update the daily dev log at `docs/logs/{year}/{month}-{day}.md` (reverse chronological, see `docs/logs/00-log-writing-guide.md` for format).
8. Record non-obvious regressions in `docs/bugs/`.
9. If prototype and implementation diverge materially, capture the reason in `docs/retrospectives/` instead of silently moving on.
10. Promote repeated process lessons into `docs/skills/` or `docs/audits/` only when the pattern is recurring enough to justify reuse.
11. For high-risk or high-ambiguity requirement, design, or plan drafts, request an independent subagent or reviewer pass and revise until major objections are resolved. Every created plan MUST pass an independent plan audit before implementation begins and an independent closure audit before being marked complete.
12. Keep code comments minimal. Prefer self-explanatory code; add only rare comments when a local constraint is otherwise easy to misread.
13. When a referenced file is not found at its expected path, check `docs/archive/` before concluding it does not exist. Archived files retain their original relative name under `docs/archive/`. Do not move files to `docs/archive/` without human approval.
14. Treat reusable skills as method selectors, not substitutes for requirements, design, or architecture docs. Business knowledge belongs in owner docs first.
15. When the same error pattern keeps recurring, do not stop at prose-only lessons. First promote it into a reusable audit prompt, checklist, or review playbook when that method is still missing. If the defect pattern still recurs, then evaluate promotion into a heuristic script, static check, lint rule, CI guard, or codemod, tuned to the copied project's real conventions and false-positive tolerance.

## Nop Platform Specific Rules

- Code generation is driven by XML models (`model/*.orm.xml`, `model/*.api.xml`). XLSX files can be generated from XML via `nop-cli convert` but are not the source of truth. Do not manually edit generated code; regenerate from models instead.
- The delta module (`app-mall-delta`) overrides `nop-auth` behavior. Changes here must respect Nop's delta customization mechanism.
- Frontend views are AMIS JSON definitions inside `.view.xml` files in `app-mall-web`. Follow AMIS conventions.
- ORM XML files (`*.orm.xml`) define entity mappings. Do not edit these directly; edit `model/*.orm.xml` and regenerate.
- Business logic lives in `*.xbiz.xml` and BizModel Java classes in `app-mall-service`.
- SQL libraries are defined in `*.sql-lib.xml` files.
- Build requires `nop-entropy` parent to be built first.
- The project uses Maven multi-module structure. Module dependency order: codegen -> api -> dao -> service -> web -> app, with wx/delta/meta as additional modules.

### Nop Platform Documentation (`nop-entropy/docs-for-ai/`)

The Nop Platform's authoritative development documentation lives at `../nop-entropy/docs-for-ai/` (sibling directory). This is the primary reference for all Nop platform conventions, APIs, and development patterns.

**When to read it:** Before implementing any feature that involves Nop platform APIs, code generation, BizModel patterns, page/view customization, delta customization, testing, or any non-trivial platform interaction.

**How to use it:**

1. Start with `docs-for-ai/INDEX.md` — contains a routing table mapping ~40 common tasks to the correct document.
2. Recommended lookup order: INDEX → `00-start-here/` → `03-runbooks/` → `02-core-guides/` → `01-repo-map/` → `04-reference/`
3. `00-start-here/ai-defaults.md` — core decision framework: Model → Delta → Java, anti-patterns table, self-check list.
4. `02-core-guides/` — 21 canonical pattern documents covering model-first development, service layer (CrudBizModel), page customization, delta mechanism, auth, testing, etc.
5. `03-runbooks/` — 34 task-oriented step-by-step guides for common operations (create entity, write BizModel method, build page, etc.).
6. `04-reference/common-java-helpers.md` and `04-reference/safe-api-reference.md` — quick reference for platform helper utilities and CrudBizModel safe APIs.

**Key rules from platform docs that apply to this project:**

- Decision order: Model → Delta → Java. Always prefer model/Delta/customization over writing new Java code.
- Never manually edit generated files (files under `_gen/`, with `_` prefix, or `_app.orm.xml`/`_service.beans.xml`).
- Use `CrudBizModel<T>` for standard entity services; use `@BizQuery`/`@BizMutation` annotations.
- Use platform helpers: `CoreMetrics.currentTimeMillis()` not `System.currentTimeMillis()`, `JsonTool` not third-party JSON libs, `StringHelper` not Apache Commons.
- `@Inject` fields cannot be `private` in Nop's IoC container.
- `@BizMutation` auto-wraps transactions; do not add `@Transactional` unless you need explicit propagation control.
- For page customization, use the three-layer model (grid/form/page) with `bounded-merge` and `x:prototype` patterns.

## Read This First

- `docs/context/project-context.md`
- `docs/context/ai-autonomy-policy.md`
- `docs/context/codebase-map.md`
- the active requirement listed in `docs/context/project-context.md`
- the active owner doc listed in `docs/context/project-context.md`

Read additionally when needed:

- `docs/context/source-of-truth-and-precedence.md` for ownership or conflict questions
- `docs/context/conventions.md` for project-wide conventions
- `docs/process/application-development-workflow.md` for workflow questions
- `docs/index.md` when you need routing beyond the active files

## Documentation Ownership

- `docs/context/` owns mandatory AI context, source-of-truth precedence, and project-wide conventions.
- `docs/backlog/` owns prioritized candidate work and AI-ready next actions.
- `docs/input/` owns raw external inputs such as PM notes, card docs, article extracts, prototype references, and copied source material.
- `docs/discussions/` owns requirement clarification conversations and unresolved question records.
- `docs/requirements/` owns implementation-ready requirement synthesis.
- `docs/design/` owns stable app-layer business and feature design.
- `docs/architecture/` owns cross-cutting technical and module-boundary truth.
- `docs/lessons/` owns durable reusable lessons extracted from bugs, audits, and retrospectives.
- `docs/plans/` owns execution and closure criteria for non-trivial work.
- `docs/audits/` owns audit workflow records and audit methodology.
- `docs/skills/` owns reusable prompts, review playbooks, and audit prompt templates.
- `docs/logs/` owns dated implementation memory.
- `docs/testing/` owns manual and exploratory testing records.
- `docs/bugs/` owns non-obvious bug histories and regression notes.
- `docs/analysis/` owns research, tradeoff analysis, and rejected directions.
- `docs/retrospectives/` owns post-implementation gap analysis and process improvements.

## Default Workflow

1. Gather raw materials in `docs/input/`.
2. If needed, clarify ambiguity in `docs/discussions/`.
3. Synthesize implementation-ready requirements in `docs/requirements/`.
4. Split stable design output into app-layer design under `docs/design/` and technical design under `docs/architecture/`, with the two referencing each other when needed.
5. Route the task and select candidate reusable skills.
6. Write or update a plan when the planning triggers apply, and record skill usage per phase or item when relevant.
7. Audit the plan before implementation.
8. Implement the smallest complete slice.
9. Run verification.
10. Run closure audit for created plans.
11. Record logs and any needed bug notes.

## Optional Workflow Layers

Use these when warranted by task complexity. Plan and closure audits are mandatory for created plans.

- `docs/audits/` for document audits and plan/closure audit evidence
- `docs/testing/` for manual or exploratory proof
- `docs/retrospectives/` for material requirement/prototype gaps
- `docs/skills/` for reusable prompts after repeated failures
- `docs/lessons/` for durable engineering lessons after repeated failures or important recoveries

Use `multi-dimensional-audit-prompt.md` when work must be challenged across several dimensions at once. Use `open-ended-audit-prompt.md` when the standard checklist may miss hidden risks. These prompts are generic defaults and MUST be customized after copy to match the project's real owner docs, protected areas, verification model, and recurring failure patterns.

## Planning Rule

Create a plan when the task has any of these traits:

- changes API, database/model, auth, integration, deployment, or public contract behavior
- changes user-visible behavior across more than one feature surface
- touches multiple modules and changes shared behavior
- is expected to take more than one AI session
- modifies more than 5 total files or is likely to exceed roughly 200 changed lines
- needs staged execution or explicit closure gates
- has unresolved product or technical risk that must not be hidden in chat

Skip a formal plan only for local low-risk edits such as copy changes, small styling fixes, test-only cleanups, and single-file behavior fixes with clear existing tests.

All created plans MUST pass independent subagent or reviewer audit before implementation begins and again before the plan is marked complete. Protected areas, unresolved product risk, and source-of-truth conflicts require human/subagent review or stay open.

## Skill Usage Rule

Before using a reusable skill, confirm all of the following:

- the task type and route are already clear from the requirement and owner docs
- the skill matches the work method, not just a similar business label
- required inputs listed in `docs/skills/README.md` are available
- the expected output is known and can be stored in the correct docs location

For non-trivial plans, each phase or item that depends on a reusable skill should record `Skill: <name>` or `Skill: none`.

## Prompting Guidance For Agents

- Do not generate a full product from a single feature list.
- Do not optimize for demo completeness.
- Prefer small complete slices over broad placeholder coverage.
- Prefer existing project patterns over invented abstractions.
- If information is missing, write the missing assumptions into a requirement, discussion, or plan file instead of silently inventing them.
- Do not put code-level implementation detail into plan files unless the detail is required for scope or closure reasoning.
- Prefer citing the existing owner doc instead of restating the same rule in multiple files.
- Do not hide mandatory rules in `docs/references/`; if an AI must apply it by default, put it in `docs/context/` or `AGENTS.md`.
- Use `docs/backlog/` and `docs/context/ai-autonomy-policy.md` to decide whether AI may choose and execute the next task without asking.
- When editing Nop platform files, follow the platform's conventions: XML models for code generation, delta customization for overrides, AMIS JSON for views.

## Docs Maintenance

After completing any significant code change, you MUST:

1. **Update the daily dev log** at `docs/logs/{year}/{month}-{day}.md` (reverse chronological, see `docs/logs/00-log-writing-guide.md` for format).
2. **Update relevant owner docs** in `docs/design/` or `docs/architecture/` when the change affects app-layer behavior or technical structure.

When verification passes completely (full green), record the verification status in the log entry and include it in the git commit message. This provides reliable known-good baselines for future debugging.

## Verification Baseline

Do not assume this template's example commands are valid for the copied project.

Use the real commands listed in `docs/context/project-context.md`.

If verification commands are blank or still placeholders, stop and fill them before reporting verification success.
