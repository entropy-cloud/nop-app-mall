# Project Vision

## Purpose

Describe the long-term product and engineering attractor for `nop-app-mall`.

## Product Goal

nop-app-mall is a demo e-commerce application showcasing the Nop Platform's development workflow. It demonstrates how to build a full-featured mall application using Nop's meta-programming, code generation, and delta customization capabilities.

## Primary Users

- Developers learning the Nop Platform
- Teams evaluating Nop for production use
- Demo/test users browsing and purchasing products

## Constraints That Must Stay True

- Built on Nop Platform (nop-entropy) as the foundation
- XML models drive code generation for ORM and API layers
- Delta customization mechanism for overriding platform behavior
- Quarkus as the runtime framework
- AMIS as the frontend framework (JSON-driven, no separate frontend build)
- Maven multi-module architecture with fixed dependency order
- Java 17+ required

## Explicit Non-Goals

- Not a production-ready e-commerce platform
- Not a framework-core project (this is application-layer)
- Not a mobile application (web-only)
- Not a microservices architecture (monolithic Quarkus app)
- Not a template for other Nop applications (the AGE template is separate)

## Success Criteria For The First Production Milestone

- All major litemall features implemented using Nop Platform conventions
- Code generation workflow validated end-to-end
- Delta customization demonstrated for auth module
- WeChat Pay integration functional

## Required Human Decision Points That AI Should Not Silently Invent

- Scope and priority of which litemall features to implement
- Database schema design choices
- Payment integration configuration
- Production deployment strategy
- Performance and scalability requirements

## Notes

- Keep this document stable and high level.
- Do not turn it into a backlog.
- Do not duplicate current milestone scope from `docs/requirements/product-scope.md`.
- Do not duplicate current app surfaces from `docs/design/app-overview.md`.
- Move implementation sequencing into `docs/plans/` or `docs/requirements/`.
