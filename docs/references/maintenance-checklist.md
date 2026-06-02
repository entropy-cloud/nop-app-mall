# Documentation Maintenance Checklist

## Purpose

Use this file after changes land to check whether repo memory stayed in sync.

## Always Review After Non-Trivial Code Changes

1. The relevant owner doc in `docs/design/` or `docs/architecture/`
2. The daily log in `docs/logs/`
3. Any affected requirement, roadmap/backlog item, plan, bug note, or testing note
4. `docs/testing/known-good-baselines.md` when a meaningful full verification baseline was established

## Change Triggers

### Architecture Or Boundary Change

Review:

- `docs/architecture/system-baseline.md`
- `docs/architecture/module-boundaries.md`
- `docs/context/source-of-truth-and-precedence.md`
- `docs/design/README.md` if the app-layer boundary changed
- `docs/index.md` if routing changed
- `docs/process/application-development-workflow.md` if the default workflow guidance changed

### Product Intent Or Scope Change

Review:

- the relevant file in `docs/input/` if the source material itself changed
- the relevant file in `docs/discussions/` if requirement interpretation changed
- the relevant file in `docs/requirements/`
- `docs/architecture/project-vision.md` if the change affects long-term direction

### App-Layer Feature Or Flow Change

Review:

- the most relevant file in `docs/design/`
- `model/*.orm.xml` if the change affects persisted field or dictionary truth
- `docs/requirements/` if user-facing scope changed
- `docs/testing/` if manual/exploratory proof was needed

### Non-Trivial Implementation Slice

Review:

- the relevant plan under `docs/plans/`, if this slice used a plan
- the relevant file under `docs/audits/` only if a separate specialized audit record was created
- `docs/logs/YYYY/MM-DD.md`
- `docs/testing/` if exploratory/manual proof was needed

Created plans require plan audit before implementation and closure audit before completion.

### Subtle Regression Or Root Cause Discovery

Review:

- `docs/bugs/`
- `docs/retrospectives/` if the problem exposed a process or requirement gap
- any affected owner doc

## Verification Baseline

Use the real project commands from `docs/context/project-context.md`.

If that file still contains placeholders, fill it before claiming verification success.
