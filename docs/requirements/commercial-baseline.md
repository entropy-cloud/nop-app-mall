# Commercial Baseline Requirements

## Purpose

Define the commercial product baseline that guides implementation slices.

This project may start from small complete loops, but each loop is implemented as formal product behavior rather than temporary or demo-only behavior.

## Product Capabilities

- User registration and login
- Product browsing (category, brand, search)
- Shopping cart
- Order creation and management, including the business order state machine
- Admin product management
- Admin order management, including shipment and admin-initiated refund for paid orders
- Payment integration and payment-state progression
- File upload/storage for product, brand, avatar, and content assets
- Group buying, coupon, after-sales, comment/review, content, notification, and reporting capabilities as commercial product areas

## First Complete Commercial Loop

The first complete loop should prove the formal shopping and operation path end to end:

- user registration and login
- product browsing by category, brand, and search
- cart maintenance
- checkout and order creation
- payment-state progression sufficient for paid-order operations
- admin product management
- admin order shipment and refund handling

This first loop is not a disposable prototype. Unsupported capabilities remain commercial product areas whose implementation order is tracked outside stable design docs.

## Manual Operations Allowed During Early Commercial Slices

- Database schema creation (auto-created on first start)
- Admin user creation (default nop/123) when no self-service admin provisioning exists yet

## Development Or Local Integration Substitutes

- A local or simulated payment path may exist only as development/test support or as an explicitly documented non-production mode.
- File upload/storage may use a local backend when the business meaning of uploaded assets is preserved.

## Completion Criteria For The First Commercial Loop

- All must-have features implemented and testable
- Admin can manage products and orders
- User can browse, add to cart, and create orders
- Application builds and runs without errors

## Rule

This file owns the implementation-ready commercial baseline and first complete loop.

Do not duplicate long-term product vision from `docs/architecture/project-vision.md` or stable app behavior from `docs/design/app-overview.md`.

Put implementation sequencing into `docs/backlog/` or a roadmap, not into every design doc.
