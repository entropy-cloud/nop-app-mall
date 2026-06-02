# User and Address Business Design

## Purpose

Describe mall-user identity, admin-user roles, profile behavior, address management, and region usage for `nop-app-mall`.

## Boundary

- This document owns user-facing and admin-facing account semantics, profile rules, and address behavior.
- Persisted model shape, field sets, and dictionaries are defined in `model/*.orm.xml`.
- Platform-auth implementation detail belongs in `docs/architecture/`.

## Domain Overview

The app distinguishes two business user groups:

- mall users who browse, purchase, and maintain their own profile and addresses
- admin users who operate the store and manage business data

The address domain supports delivery information and region-based selection for checkout.

## Mall User Management

### Supported User Capabilities

- register and sign in
- view current profile information
- update personal profile details
- change password
- manage delivery addresses
- view personal orders and order state

### Business Rules

- Only authenticated users can view or modify their own profile and addresses.
- Disabled users must not be able to continue normal mall use.
- Profile updates must not expose or return sensitive credential data.
- Additional login channels such as WeChat must preserve the same user identity and account-safety semantics when enabled.

## Registration And Authentication

### Business Baseline

- Username/password login is supported.
- Self-registration is supported for mall users.
- Admin-created accounts may also exist where needed.

### Additional Login Channels

- WeChat mini-program login and auto-registration are integration capabilities that must map to the same mall-user identity rules.

## Address Management

### Business Rules

- Each user may maintain multiple delivery addresses up to the configured limit.
- One address acts as the default delivery address.
- Changing the default address must leave exactly one default address afterward.
- Users may only manage addresses they own.
- Address data must be sufficient for delivery, including recipient, contact method, region, and detailed address.

### Supported Behavior

- Add address.
- Edit address.
- Delete address.
- List addresses with the default address surfaced clearly.
- Set or change the default address.

## Region Data

### Business Role

- Region data supports cascading address selection.
- Region data is reference data rather than user-managed business content.
- Region hierarchy must be stable enough for address entry and delivery presentation.

## Admin User Management

### Business Roles

- Super admin has full store-management access.
- Admin users handle operational scopes such as product and order management.

### Supported Behavior

- Manage admin accounts and role assignment.
- Apply permission boundaries appropriate to operational responsibilities.
- Record meaningful admin operations for audit and troubleshooting.

## Relationship To Other Owner Docs

- Permission semantics are further constrained by `roles-and-permissions.md`.
- Order ownership and order-facing user actions are defined in `order-and-cart.md`.
