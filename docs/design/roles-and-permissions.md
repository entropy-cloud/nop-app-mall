# Roles And Permissions

## Purpose

Record the current supported role model for the application.

## Roles

| Role        | Description                          | Backend Source                 |
| ----------- | ------------------------------------ | ------------------------------ |
| Super admin | Full system access, all operations   | `nop-auth` via delta module    |
| Admin       | Product and order management         | `nop-auth` role configuration  |
| Mall user   | Browsing, purchasing, order mgmt     | `nop-auth` user role           |

## Permissions

- Admin pages require authenticated admin session
- Mall user pages require authenticated user session
- Permission control is managed through Nop Platform's auth system (`nop-auth`) with delta customizations in `app-mall-delta`
- Action-level permissions defined in `*.action-auth.xml` files
- Data-level permissions defined in `*.data-auth.xml` files

## Visibility Rules

- Admin portal: visible only to authenticated admin users
- Mall storefront: product browsing public; cart/orders require user login
- User center: visible only to authenticated mall users

## Action Restrictions

- Order cancellation: user can cancel unpaid orders only
- Refund request: user can request for orders within return window
- Product management: admin only
- User management: super admin only

## Approval Or Audit Requirements

- Payment processing requires WeChat Pay callback verification
- Order status changes follow a fixed state machine
- After-sales review requires admin approval

## Rule

If role-based behavior affects implementation, keep this document aligned with live behavior.
