# Marketing and Promotions Business Design

## Purpose

Describe the coupon system, group buying, and other marketing features for nop-app-mall. These features are **deferred in MVP** but designed here for future implementation reference.

## Coupon System (Deferred)

### Domain Overview

The coupon system supports multiple coupon types, distribution channels, and usage restrictions. The design is informed by both mall and litemall coupon models.

### Core Entities

| Entity | Table | Role |
|--------|-------|------|
| Coupon | `app_mall_coupon` | Coupon template definition |
| CouponUser | `app_mall_coupon_user` | User's coupon instances |

### Coupon Data Model

- **Identity**: name, description, tag (e.g., "New user only"), code (for redemption)
- **Amount**: discount (fixed amount off), min (minimum spend threshold)
- **Distribution**: type (0=universal/user-claim, 1=registration gift, 2=code redemption), total (0=unlimited), limit per user
- **Validity**: time_type (0=relative N days from claim, 1=fixed date range), days (if relative), start_time/end_time (if fixed)
- **Product scope**: goods_type (0=all products, 1=by category, 2=by specific goods), goods_value (JSON array of category IDs or goods IDs)
- **Status**: 0=active, 1=expired, 2=off-shelf

### Coupon User Data Model

- Fields: user_id, coupon_id, status, used_time, start_time, end_time, order_id
- Status: 0=unused, 1=used, 2=expired, 3=off-shelf

### Business Rules

- discount > 0 required
- min >= discount (minimum spend must be at least the discount amount)
- total=0 means unlimited quantity
- User claim limit: 0=unlimited, N=max N per user
- Coupon can only be used once
- Coupon validity: either relative (N days from claim) or fixed date range
- Product scope: restrict to all / specific categories / specific goods
- Expired coupons automatically marked via scheduled task

### Coupon Lifecycle

```
Admin creates coupon (status=active)
  ├── User claims coupon → CouponUser created (status=unused)
  │     ├── User uses in order → CouponUser status=used, order_id set
  │     ├── Expiry time reached → CouponUser status=expired
  │     └── Admin off-shelf → CouponUser status=off-shelf
  ├── Admin off-shelf coupon → Coupon status=off-shelf
  └── Total claimed == total → No more claims accepted
```

### Coupon Validation (at Order Time)

1. Coupon exists and status = active
2. CouponUser exists and status = unused
3. CouponUser not expired (current time within start_time/end_time)
4. Order goods_price >= coupon.min (minimum spend met)
5. Product scope validation: check if order goods match goods_type/goods_value restriction
6. All pass: coupon is applicable with discount amount

### Coupon and Order Interaction

- Coupon discount recorded on order as coupon_price
- If order cancelled: restore CouponUser status to unused, clear order_id
- If order refunded: restore CouponUser status to unused, clear order_id
- Coupon validation happens at order creation time; coupon is consumed in order transaction

## Group Buying / Groupon (Deferred)

### Domain Overview

Group buying allows multiple users to purchase the same goods at a discounted price when a minimum number of participants is reached.

### Core Entities

| Entity | Table | Role |
|--------|-------|------|
| GrouponRules | `app_mall_groupon_rules` | Group buy rule definition |
| Groupon | `app_mall_groupon` | Individual participation record |

### Groupon Rules Data Model

- Fields: goods_id, goods_name, pic_url, discount (amount off per participant), discount_member (minimum members for discount), expire_time, status
- Status: 0=active, 1=auto-expired, 2=manually off-shelf

### Groupon Participation Data Model

- Fields: order_id, groupon_id (0=opening group, else parent groupon ID), rules_id, user_id, creator_user_id, share_url, create_time, status
- Status: 0=unpaid, 1=active, 2=failed, 3=success

### Business Rules

- discount_member >= 2 (need at least 2 people)
- discount > 0
- expire_time must be in the future
- User cannot join their own group
- User cannot join the same group twice
- Group leader (groupon_id=0) initiates; followers (groupon_id=leader's ID) join

### Group Lifecycle

```
Admin creates GrouponRules (status=active, linked to goods)
  ├── User opens group (groupon_id=0) → Groupon created (status=unpaid)
  │     ├── User pays → Groupon status=active, share image generated
  │     │     ├── More users join → Groupon records created
  │     │     │     ├── Participants >= discount_member - 1 → Group SUCCESS
  │     │     │     │     All participants' Groupon status → success
  │     │     │     └── Continue waiting
  │     │     └── Expire time reached, not enough participants → Group FAILED
  │     └── User cancels/unpaid timeout → Groupon status=failed
  └── Admin off-shelf → Rules status=off-shelf
```

### Group Success Detection

- After each payment callback for a groupon order:
  1. Count followers with groupon_id = leader's groupon ID and status in (active, unpaid)
  2. discount_member = total members needed INCLUDING the leader
  3. If followers.count >= discount_member - 1:
     - Example: discount_member=3 means leader + 2 followers needed. When 2 followers have paid, group succeeds.
     - Set leader's Groupon status = success
     - Set all followers' Groupon status = success
  4. Otherwise: continue waiting

### Groupon and Order Interaction

- Groupon discount recorded on order as groupon_price
- Groupon record created as part of order creation transaction
- Payment callback updates groupon status
- Order cancellation may trigger groupon failure

## Flash Sale / Seckill (Not Planned)

Not included in nop-app-mall scope. Mall project implements this with:
- Three-table design (promotion date range × daily time sessions × products)
- Dedicated flash_price and flash_stock per product
- High-concurrency handling with Redis stock deduction

This is intentionally excluded as it adds significant complexity inappropriate for a demo application.

## Full Reduction / Tiered Pricing (Not Planned)

Not included in nop-app-mall scope. Mall project implements:
- Product-level ladder pricing (buy N items → percentage off)
- Full reduction (spend ¥X → ¥Y off)
- Member-tier-specific pricing

These are useful for production e-commerce but add complexity beyond demo scope.

## Search and Content Features (Deferred)

### Search History

- Record user search keywords
- Show recent search history (limit count, e.g., 20)
- Support clearing search history
- Auto-record on each search action

### Collect / Favorites

- User can collect goods and topics
- Collect type: 0=goods, 1=topic
- List collected items with pagination
- Check collected status on goods detail page

### Browse Footprint

- Record goods browsing history per user
- Show recent browsed goods (limit count, e.g., 100)
- Support clearing footprint

### Topic / Special

- Admin-managed topic pages with rich content
- Topic linked to goods (JSON array of goods IDs)
- Topic has title, subtitle, content, pic, read count, sort order

### Advertisement

- Admin-managed ad banners
- Position-based (e.g., homepage banner)
- Time-limited (start_time/end_time)
- Enable/disable toggle

## Nop Platform Implementation Notes

- Coupon and Groupon entities defined in `model/app-mall.orm.xml`
- BizModel classes with `@BizMutation` for claim/use/validate operations
- Scheduled tasks for coupon expiry and groupon timeout via Nop Platform task scheduling
- Product scope validation uses SQL queries against goods/category tables
- Group success detection triggered in order payment callback BizModel method
- All marketing features are deferred in MVP; these designs serve as implementation reference
- When implementing, follow Nop convention: Model → Delta → Java decision order

## Reference Projects Comparison

| Aspect | mall (macrozheng) | litemall | nop-app-mall |
|--------|-------------------|----------|-------------|
| Coupon types | 4 types (universal/gift/shopping/registration) | 3 types (universal/registration/code) | 3 types (litemall style) |
| Flash sale | Full 3-table design | Not implemented | Not planned |
| Group buying | Not implemented | Full groupon system | Full groupon (deferred, litemall style) |
| Tiered pricing | Product-level ladder tables | Not implemented | Not planned |
| Full reduction | Product-level reduction tables | Not implemented | Not planned |
| Member pricing | Per-tier member prices | Not implemented | Not planned |
