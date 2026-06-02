# App Overview

## Purpose

Describe the stable supported app-level product baseline.

## Main Surfaces Or Pages

- Admin management portal
- Mall storefront for product browsing, cart, checkout, and user self-service
- User center for profile, orders, and address management

## Primary Navigation Model

- Admin: sidebar navigation with menu groups for user management, product management, order management, marketing/content operations, and system management
- Mall: storefront navigation for categories, search/discovery, cart, and user center

## Main User Roles

- Super admin: full system access
- Admin: store operation within assigned responsibilities
- Mall user: browsing, purchasing, and self-service order management

## Core Workflows

- Product management: category -> brand -> goods -> goods product (SKU) -> storefront presentation -> [product-catalog.md](product-catalog.md)
- Order workflow: cart -> checkout -> payment -> shipping -> completion -> refund/after-sale where eligible -> [order-and-cart.md](order-and-cart.md)
- User management: registration -> profile -> address -> orders -> [user-and-address.md](user-and-address.md)
- Marketing and content operations: coupons, group buying, engagement, promotion content -> [marketing-and-promotions.md](marketing-and-promotions.md)
- System operations: configuration, storage, notices, operational tasks, logs, reporting -> [system-configuration.md](system-configuration.md)

## Key Domain Areas

- Identity and access: User, Address, Admin, Role, Permission
- Catalog: Category, Brand, Goods, GoodsProduct (SKU), GoodsSpecification, GoodsAttribute
- Commerce: Cart, Order, OrderGoods, payment state, shipment state, refund and after-sale state
- Marketing and engagement: Coupon, CouponUser, GrouponRules, Groupon, Comment, Collect, Footprint, SearchHistory, Keyword
- Content and operations: Topic, Ad, Issue, Feedback, Notice, Storage, Region, configuration, admin log, statistics

## Integration Points

- Payment capability, including WeChat Pay where configured
- File storage capability for goods, brand, avatar, and content assets
- Platform authentication and authorization capability
- Notification delivery capability for user and operational messages

## Boundary

- This file owns app-layer surfaces, roles, workflows, and domain areas.
- Persisted entities, fields, and dictionaries are defined in `model/*.orm.xml`.
- Technical implementation details belong in `docs/architecture/`.
- Implementation order belongs in `docs/backlog/` or plans, not in this overview.

## Rule

Keep this file stable and product-facing. If a feature changes the supported app baseline, update this file or a narrower owner doc in the same change.

Do not duplicate long-term product vision from `docs/architecture/project-vision.md` or implementation sequencing from `docs/backlog/`.
