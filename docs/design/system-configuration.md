# System Configuration Business Design

## Purpose

Describe the business-managed configuration, file-storage expectations, operational jobs, notifications, and admin-operation records for `nop-app-mall`.

## Boundary

- This document owns business-facing configuration categories and operational behavior.
- Persisted config keys, entities, field sets, and dictionaries are defined in `model/*.orm.xml` or the platform-owned system configuration model.
- Technical scheduling, storage, and integration strategy belongs in `docs/architecture/`.

## System Configuration

### Business Role

System configuration stores editable operational settings that change mall behavior without redefining product scope.

### Current Supported Configuration Categories

- storefront display counts for homepage sections
- freight amount and free-shipping threshold
- order timeout and auto-confirm timing
- per-user address limit
- mall identity and contact information

### Business Rules

- Supported configuration items are predefined by the application baseline.
- Configuration changes should take effect quickly for newly evaluated business flows.
- Operational settings should be editable only by the appropriate administrative role.

## File Storage

### Business Role

- File storage supports goods images, brand images, avatars, and other mall assets.

### Business Rules

- Uploaded assets must be retrievable by the app after upload.
- Allowed upload categories and size limits are centrally controlled.
- Storage backend choice should not change the business meaning of uploaded assets.

### Business Baseline

- Local storage may be used when it preserves the business meaning and retrievability of uploaded assets.
- Cloud-backed storage is an infrastructure choice and should not change asset semantics.

## Notifications

Notification categories include:

- payment confirmation
- shipping updates
- administrative order alerts

## Notice

- Notice means admin-authored system or operational announcements shown to users or operators inside the product surface.
- Notice is a content-like communication object.
- Notice is different from notification events such as payment confirmation or shipping updates, which are delivery-triggered operational messages.

## Scheduled Operational Tasks

### Business Baseline

- auto-cancel overdue unpaid orders
- auto-confirm overdue shipped orders

### Additional Operational Tasks

- coupon expiry handling
- groupon expiry handling
- comment-window expiry handling

## Admin Operation Log

### Business Role

- Record meaningful admin actions for auditability and troubleshooting.

### Logged Areas

- product management
- order operations such as ship and refund
- user and admin management
- system configuration changes
- future marketing operations

## Data Statistics

Reporting areas include:

- order counts and revenue
- pending-order workload
- goods and inventory summaries
- user growth and activity

## Relationship To Other Owner Docs

- Freight and timeout effects are used by `order-and-cart.md`.
- Notification and promotion use cases may expand `marketing-and-promotions.md` in later phases.
