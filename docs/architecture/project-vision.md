# Project Vision

## Purpose

Describe the long-term product and engineering attractor for `nop-app-mall`.

## Product Goal

nop-app-mall is a commercial-grade e-commerce reference application showcasing the Nop Platform's development workflow. It demonstrates how to build a full-featured mall application using Nop's meta-programming, code generation, and delta customization capabilities.

## Primary Users

- Mall shoppers
- Mall operators and administrators
- Developers and teams evaluating Nop through a realistic business application

## Constraints That Must Stay True

- Built on Nop Platform (nop-entropy) as the foundation
- XML models drive code generation for ORM and API layers
- Delta customization mechanism for overriding platform behavior
- Quarkus as the runtime framework
- AMIS as the frontend framework (JSON-driven, no separate frontend build)
- Maven multi-module architecture with fixed dependency order
- Java 17+ required

## Explicit Non-Goals

- Not a disposable prototype or throwaway demo implementation
- Not a framework-core project (this is application-layer)
- Not a mobile application (web-only)
- Not a microservices architecture (monolithic Quarkus app)
- Not a template for other Nop applications (the AGE template is separate)

## Success Criteria For The First Commercial Milestone

- All major litemall features implemented using Nop Platform conventions
- Code generation workflow validated end-to-end
- Delta customization demonstrated for auth module
- WeChat Pay integration functional

## Required Human Decision Points That AI Should Not Silently Invent

- Roadmap priority of which litemall-derived capabilities to implement first
- Database schema design choices
- Payment integration configuration
- Production deployment strategy
- Performance and scalability requirements

## Notes

- Keep this document stable and high level.
- Do not turn it into a backlog.
- Do not duplicate commercial baseline requirements from `docs/requirements/commercial-baseline.md`.
- Do not duplicate app surfaces from `docs/design/app-overview.md`.
- Move implementation sequencing into `docs/backlog/` or plans.
