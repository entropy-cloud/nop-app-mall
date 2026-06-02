# Marketing and Promotions Business Design

## Purpose

Describe marketing capabilities for coupons, group buying, content promotion, and engagement features.

## Boundary

- This document owns business semantics and feature rules for marketing surfaces.
- Persisted model shape, field sets, and dictionaries are defined in `model/app-mall.orm.xml`.
- Technical implementation strategies belong in `docs/architecture/`.

## Coupon System

### Business Intent

- Coupons provide rule-based discounts that can be claimed, granted, or redeemed.
- Coupon applicability may depend on minimum spend, product scope, and validity window.

### Business Rules

- A coupon has one business meaning and can only be used once per granted instance.
- Coupon claim and use may be limited per user.
- Coupon validity may be relative to claim time or fixed to a date range.
- Coupon scope may apply to all goods, selected categories, or selected goods.
- Cancelled or refunded qualifying orders should restore coupon usability according to the supported order policy.

### Lifecycle

- Admin creates coupon rules.
- Users receive coupons by claim, registration gift, or redemption flow.
- Users apply an eligible coupon during checkout.
- Used, expired, or off-shelf coupon states remove further eligibility.

## Group Buying / Groupon

### Business Intent

- Group buying rewards enough users purchasing the same goods together.
- One user starts a group and other users join it before expiry.

### Business Rules

- Groupon rules are tied to a specific goods item.
- Group success depends on reaching the required participant count before the deadline.
- Users cannot join their own group as a follower.
- Users cannot join the same active group multiple times.
- Groupon discount affects order pricing only when the group-business conditions are satisfied.

### Lifecycle

- Admin defines groupon rules.
- A user opens a group through an order.
- Other users join before expiry.
- The group ends in success when enough valid participants pay, otherwise it times out.

## Search And Engagement Features

### Search History

- Record recent user searches.
- Support viewing and clearing search history.

### Favorites

- Users can favorite goods and topic-like promotional content.
- Favorite status should be queryable from detail surfaces.

### Browse Footprint

- Record recent browse history for user convenience.
- Support viewing and clearing footprint records.

## Content Promotion

### Topic / Special Content

- Admins can publish themed promotional content that links users toward goods discovery.

### Advertisement

- Admins can manage banner-style promotion placements.
- Promotions may be time-bound and enable/disable controlled.

### FAQ And Feedback

- FAQ and feedback support storefront guidance and customer communication.

## Out Of Scope

- Flash sale / seckill is not part of the supported baseline.
- Tiered pricing, full reduction, and member-tier pricing are not part of the supported baseline.
