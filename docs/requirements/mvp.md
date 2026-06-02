# MVP Scope

## Purpose

Define the smallest credible product scope.

## Must-Have Features

- User registration and login
- Product browsing (category, brand, search)
- Shopping cart
- Order creation and management (including order state machine 101-402)
- Admin product management
- Admin order management (including ship and admin-initiated refund for paid orders)

## Deferred Features

- WeChat Pay integration
- Group buying (groupon)
- Coupon system
- After-sales for received orders (refund/return after delivery confirmation)
- Comment/Review system
- Content management (topics, ads, notices, issues, feedback)
- Storage management (cloud backend)
- Search history, favorites, browse footprint
- Statistics dashboard

## Manual Operations Allowed In MVP

- Database schema creation (auto-created on first start)
- Admin user creation (default nop/123)

## Mocked Or Simulated Integrations Allowed In MVP

- Payment processing (mocked or skipped)
- File upload/storage

## Exit Criteria For MVP Completion

- All must-have features implemented and testable
- Admin can manage products and orders
- User can browse, add to cart, and create orders
- Application builds and runs without errors

## Rule

This file owns the current MVP boundary only.

Do not duplicate long-term product vision from `docs/architecture/project-vision.md` or current app behavior from `docs/design/app-overview.md`.
