# Project Conventions

## Purpose

This file captures project-wide rules that AI agents should apply by default.

Keep it short. If a rule becomes detailed lookup material, move the detail to `docs/references/` and link it here.

## File-In / File-Out

- Important inputs should be written to files before implementation.
- Important outputs should be written back to the repo, not left only in chat.
- Raw inputs belong in `docs/input/`.
- Synthesized implementation-ready requirements belong in `docs/requirements/`.

## Design Split

- Requirement/app behavior design belongs in `docs/requirements/` and `docs/design/`.
- Technical architecture design belongs in `docs/architecture/`.
- Persisted entity shape, field sets, and data dictionaries belong in `model/*.orm.xml`.
- Cross-reference instead of duplicating the same rule in multiple docs.

## Nop Platform Conventions

- Code generation is driven by XML models. Never manually edit generated code.
- ORM entities and mappers are generated from `model/app-mall.orm.xml`.
- API interfaces are generated from `model/app-mall.api.xml`.
- Delta ORM overrides are generated from `model/nop-auth-delta.orm.xml`.
- Business logic goes in `*.xbiz.xml` and BizModel Java classes.
- Frontend views are AMIS JSON in `.view.xml` files.
- The delta module (`app-mall-delta`) uses Nop's delta mechanism to override nop-auth.
- Always regenerate from models after XML model changes (`codegen.sh` / `codegen.bat`).

## Review Rule

- High-risk or high-ambiguity requirement and design drafts should get an independent subagent or reviewer pass.
- Every created plan requires independent plan audit before implementation and closure audit before completion.
- Independent review should cite files and evidence, not only say "looks good."
- If no independent reviewer is available, record that limitation in the plan or log. Cold replay is not a second reviewer and never resolves protected-area or source-of-truth conflicts by itself.

## Bug Rule

- Every non-trivial bug fix should add or update automated test coverage.
- If automated coverage is impossible, record the reason and manual proof.

## Comment Policy

- Prefer no comments by default.
- Add comments only when a local constraint is easy to misread and code alone is not enough.

## Verification Rule

- Keep verification commands current in `docs/context/project-context.md`.
- Do not report verification success for commands that were not actually run.
- Do not keep placeholder verification commands after copying the template.
- Build requires `nop-entropy` parent to be available in local Maven repository.
