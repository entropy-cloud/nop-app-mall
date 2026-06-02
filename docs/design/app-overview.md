# App Overview

## Purpose

Describe the current supported app-level baseline.

## Main Surfaces Or Pages

- Admin management portal (AMIS-based CRUD pages)
- Mall storefront (product browsing, cart, checkout)
- User self-service (profile, orders, address management)

## Primary Navigation Model

- Admin: sidebar navigation with menu groups for user management, product management, order management, system management
- Mall: top-level tabs for categories, cart, user center

## Main User Roles

- Super admin: full system access
- Admin: product and order management
- Mall user: browsing, purchasing, order management

## Core Workflows

- Product management: category -> brand -> goods -> goods product (SKU) → [product-catalog.md](product-catalog.md)
- Order workflow: cart -> checkout -> payment -> shipping -> completion → [order-and-cart.md](order-and-cart.md)
- After-sales: order -> refund/return request -> review -> resolution → [order-and-cart.md](order-and-cart.md)
- User management: registration -> profile -> address -> orders → [user-and-address.md](user-and-address.md)
- Marketing: coupon -> groupon -> promotions → [marketing-and-promotions.md](marketing-and-promotions.md)

## Key Domain Objects

### MVP Domain Objects

- User, Address, Admin
- Category, Brand, Goods, GoodsProduct (SKU), GoodsSpecification, GoodsAttribute
- Order, OrderGoods, Cart
- Storage, Region

### Deferred Domain Objects

- Coupon, CouponUser
- GrouponRules, Groupon
- Comment, Aftersale
- Collect, Footprint, SearchHistory, Keyword
- Topic, Ad, Issue, Feedback
- Notice

## Integration Points

- WeChat Pay (`app-mall-wx`)
- File storage (local/cloud via Storage entity)
- Nop Platform auth system (via `app-mall-delta`)

## Rule

Keep this file current. If a feature changes the supported app baseline, update this file or a narrower owner doc in the same change.

This file owns current app behavior, surfaces, roles, and workflows.

Do not duplicate long-term product vision from `docs/architecture/project-vision.md` or current milestone scope from `docs/requirements/product-scope.md`.
