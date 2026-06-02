# User and Address Business Design

## Purpose

Describe the user management, authentication, address management, and user profile domain for nop-app-mall.

## Domain Overview

The user system supports two distinct user types: **mall users** (customers) and **admin users** (store managers). Authentication is handled by Nop Platform's `nop-auth` module with delta customizations in `app-mall-delta`.

### Core Entities

| Entity | Table | Role |
|--------|-------|------|
| User | Nop auth user (via delta) | Mall customer profile |
| Address | `app_mall_address` | User shipping addresses |
| Admin | Nop auth admin (via delta) | Store management user |
| Region | `app_mall_region` | Administrative region hierarchy |

## User Management

### User Data Model

- Core identity managed by Nop Platform auth (nop-auth)
- Extended profile fields via delta customization:
  - nickname, gender (0=unknown, 1=male, 2=female)
  - birthday, phone, avatar_url
  - user_level (0=normal, 1=VIP, 2=advanced VIP)
  - wechat_openid (for WeChat mini-program login, deferred in MVP)
  - status (0=active, 1=disabled, 2=deleted)

### Authentication

- MVP: username/password login via Nop Platform auth
- Deferred: WeChat mini-program OAuth login (auto-register if new user)
- Login returns JWT token for subsequent API calls
- Token expiry managed by Nop Platform auth configuration

### User Registration (MVP)

- Admin-created accounts (default admin account: nop/123)
- Self-registration: username, password, optional nickname
- Deferred: WeChat auto-registration on first login

### User Profile Operations

- **Get profile**: return current user info (excluding password)
- **Update profile**: modify nickname, avatar, gender, birthday, phone
- **Change password**: validate old password, set new password
- All operations require authenticated user

### User Status Management (Admin)

- Active (0): normal user, can browse and purchase
- Disabled (1): cannot login or place orders
- Deleted (2): soft delete, data retained

## Address Management

### Data Model

- Fields: user_id, name (recipient), phone, province_id, city_id, area_id, address (detail), is_default, region_code, postal_code (optional)

### Business Rules

- Each user can have up to 20 addresses
- One address must be the default (is_default=1)
- When setting an address as default, unset previous default
- Province/city/area must be complete (all three levels)
- Phone must be valid format
- Deleting the default address: prompt user to set a new default or auto-set the first remaining address
- User can only manage their own addresses

### Address Operations

- **Add address**: validate field completeness, enforce 20-address limit
- **Update address**: modify any field, handle default flag change
- **Delete address**: soft delete, handle default address logic
- **List addresses**: all addresses for current user, default first
- **Set default**: toggle is_default flag, unset previous default

## Region Data

### Data Model

- Fields: pid (parent ID), name, type (1=province, 2=city, 3=district), code
- Tree structure: province → city → district
- Self-referencing via pid (province pid=0, city pid=province_id, district pid=city_id)

### Data Source

- Pre-loaded administrative region data (China provinces, cities, districts)
- Read-only for users and admins
- Used for address selection cascading dropdowns

## Admin User Management

### Admin Roles

- **Super admin**: full system access, can manage other admins and roles
- **Admin**: product and order management, limited system access
- Role-based access control managed by Nop Platform auth (nop-auth)

### Admin Operations

- Admin CRUD via Nop Platform's built-in admin management
- Role assignment and permission configuration
- Operation logging for audit trail

## Nop Platform Implementation Notes

- User and Admin entities extend Nop Platform's auth user model
- Delta customizations in `app-mall-delta/` module
- Do not directly modify nop-auth; use delta mechanism
- Address BizModel extends `CrudBizModel<Address>` with ownership validation
- Region data is read-only; provide query endpoints only
- Use `@BizQuery` for read operations, `@BizMutation` for write operations
- Address ownership check: verify user_id matches current authenticated user

## Reference Projects Comparison

| Aspect | mall (macrozheng) | litemall | nop-app-mall |
|--------|-------------------|----------|-------------|
| User model | Separate member table | Separate user table | Nop auth delta |
| Admin model | Separate admin with RBAC | Admin + role + permission | Nop auth RBAC |
| WeChat login | Optional channel | Deep integration | Deferred |
| Region data | Not included | Pre-loaded hierarchy | Pre-loaded (litemall style) |
| Address limit | Not enforced | 20 per user | 20 per user |
