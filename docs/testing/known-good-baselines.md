# Known-Good Baselines

## Purpose

Record the latest verified project state so future AI sessions can tell whether a failure is new or pre-existing.

This file is lightweight. Record only meaningful baselines, not every local command run.

## Baselines

No verified baseline recorded yet. Add the first row after a successful full verification run.

| Date | Source | Git State | Scope | Commands Passed | Known Failures | Evidence | Notes |
| ---- | ------ | --------- | ----- | --------------- | -------------- | -------- | ----- |

## When To Update

Update this file when:

- full typecheck/build/lint/test verification passes after a meaningful change
- a previously failing command becomes green and should be remembered
- a team intentionally accepts a known failing command and records it as a known failure, not as a passed command

## Rule

Do not mark a command as passed unless it actually ran in the current repository state.

`Commands Passed` must contain only passing commands. Put accepted failures in `Known Failures` with the reason and evidence.

`full` means all real verification commands configured in `docs/context/project-context.md`. Commands explicitly marked `none` are excluded and should be noted.
