# Context Index

## Purpose

`docs/context/` holds the short, low-churn context AI agents should read before making non-trivial changes.

This directory is intentionally small. It exists because important project rules hidden under `docs/references/` are easy for AI agents to miss.

## Read These First

1. `project-context.md`
2. `ai-autonomy-policy.md`
3. `codebase-map.md`
4. `source-of-truth-and-precedence.md`
5. `conventions.md`

These files jointly define the default boundary between app-layer design truth, technical architecture truth, and XML-model truth.

## What Belongs Here

- stable project context that should orient every AI session
- autonomy rules for when AI may proceed, plan, ask, research, or stop
- compact codebase routing that prevents repeated rediscovery
- source-of-truth and owner-doc precedence rules
- project-wide conventions that are not merely optional lookup material

## What Does Not Belong Here

- long design documents
- active plan, current blocker, or current backlog status mirrors
- roadmap sequencing or implementation ordering
- one-off analysis
- daily logs
- execution plans
- detailed reference tables

Put those in their owning directories and link to them from context only when needed.
