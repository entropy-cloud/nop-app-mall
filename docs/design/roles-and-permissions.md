# Roles And Permissions

## Purpose

Record the current supported role model for the application.

## Boundary

- This document owns business-facing role meanings, visibility rules, and action restrictions.
- Technical auth implementation detail belongs in `docs/architecture/`.
- Persisted role and permission structures are defined in platform and model artifacts, not in this prose doc.

## Roles

| Role        | Description |
| ----------- | ----------- |
| Super admin | Full system access across the mall management baseline |
| Admin       | Product and order operations within assigned responsibilities |
| Mall user   | Browsing, purchasing, and self-service order management |

## Permissions

- Admin pages require authenticated admin session
- Mall user pages require authenticated user session
- Public storefront browsing is available without login unless the specific action requires authentication
- Role and permission assignment must match the business restrictions defined below

## Visibility Rules

- Admin portal: visible only to authenticated admin users
- Mall storefront: product browsing public; cart/orders require user login
- User center: visible only to authenticated mall users

## Action Restrictions

- Order cancellation: user can cancel unpaid orders only
- Refund request: user can request refund only for eligible paid orders according to order and after-sale rules
- Direct refund: admin can directly refund eligible paid orders according to order and after-sale rules
- Product management: admin only
- Admin account and role management: super admin only

## Approval Or Audit Requirements

- Order status changes follow a fixed state machine
- User-initiated refund and after-sale requests require admin review where the order policy requires approval
- Direct admin refund actions must remain auditable

## Rule

If role-based behavior affects implementation, keep this document aligned with live behavior.
