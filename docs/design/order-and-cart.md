# Order and Cart Business Design

## Purpose

Describe the order lifecycle, shopping cart, checkout flow, price calculation, and state machine for nop-app-mall.

## Domain Overview

The order system is the most complex domain in the mall. It spans cart management, checkout, payment, shipping, confirmation, after-sales, and closure. The design draws from both macrozheng/mall and linlinjava/litemall, adapted to Nop Platform conventions.

### Core Entities

| Entity | Table | Role |
|--------|-------|------|
| Cart | `app_mall_cart` | User's shopping cart items |
| Order | `app_mall_order` | Order header with status, prices, shipping info, aftersale_status, comments count |
| OrderGoods | `app_mall_order_goods` | Per-SKU line items within an order |
| Aftersale | `app_mall_aftersale` | After-sale refund/return records (deferred in MVP) |

## Shopping Cart

### Data Model

- Fields: user_id, goods_id, goods_name, product_id (SKU), price, number, specifications (JSON), checked, pic_url
- One row per user + SKU combination

### Business Rules

- User must be logged in to use cart
- Same user + same SKU: increment quantity (do not create duplicate rows)
- Max quantity per cart item: 999
- New cart items default to checked=1
- Only on-sale goods can be added
- Stock check at add-to-cart time (warning if insufficient, not blocking)
- Hard stock check at order time (blocking)

### Cart Operations

- **Add to cart**: specify goods_id + product_id + quantity; check goods is on sale; check stock; create or increment
- **List cart**: return all items for current user with goods name, pic, price, SKU specs, quantity, checked state; compute cart total
- **Update quantity**: change number for a cart item; check stock
- **Check/uncheck**: toggle checked flag for single item or all items
- **Delete**: remove single item or clear all items
- **Checkout preview**: aggregate checked items, compute prices, show available coupons

## Price Calculation

### Price Components

```
goods_price    = sum(order_goods.number × order_goods.price)    — subtotal of all items
freight_price  = shipping fee (see freight rules)
coupon_price   = coupon discount (0 if no coupon)
groupon_price  = group-buy discount (0 if no groupon)
integral_price = points discount (0 in MVP)
order_price    = goods_price + freight_price - coupon_price - groupon_price
actual_price   = order_price - integral_price
```

### Freight Rules

- Freight amount: system-configurable (default 8 yuan)
- Free-shipping threshold: system-configurable (default 88 yuan)
- Rule: if goods_price >= free-shipping threshold, freight_price = 0; otherwise freight_price = configured freight amount
- These values are stored in system configuration table

### Coupon Discount (Deferred in MVP)

- Coupon reduces goods_price by coupon discount amount
- Must validate coupon applicability (min spend, product restrictions, validity period)
- Coupon discount is capped at goods_price (cannot make order negative)

## Order State Machine

### Order Status Codes

| Code | Name | Description | User Actions | Admin Actions |
|------|------|-------------|--------------|---------------|
| 101 | Unpaid | Order created, awaiting payment | Cancel, Pay | - |
| 102 | Cancelled (user) | User cancelled unpaid order | Delete | - |
| 103 | Cancelled (system) | Auto-cancelled after timeout | Delete | - |
| 201 | Paid | Payment confirmed, awaiting shipment | Request refund | Ship, Refund |
| 202 | Refund requested | User requested refund | - | Approve refund, Reject refund |
| 203 | Refunded | Refund completed | Delete | - |
| 204 | Groupon timeout | Group buy failed due to timeout (deferred: with groupon) | Delete | - |
| 301 | Shipped | Admin shipped, awaiting confirmation | Confirm receipt | - |
| 401 | Received (user) | User confirmed receipt | Comment, After-sale, Delete | - |
| 402 | Received (system) | Auto-confirmed after timeout | Comment, After-sale, Delete | - |

### State Transitions

```
101 (Unpaid)
  ├── user cancel → 102 (Cancelled user)
  ├── timeout → 103 (Cancelled system)
  └── payment success → 201 (Paid)
        ├── user refund request → 202 (Refund requested)
        │     ├── admin approve → 203 (Refunded)
        │     └── admin reject → 201 (Paid)
        ├── groupon timeout → 204 (Groupon timeout) [deferred: with groupon]
        └── admin ship → 301 (Shipped)
              ├── user confirm → 401 (Received user)
              └── auto-confirm → 402 (Received system)
```

### Status Transition Rules

- Only forward transitions allowed (no backward except 202→201 on reject)
- Status check must be atomic with the state change (optimistic locking)
- Each status change should update the corresponding timestamp field
- Order can be deleted (soft) only in terminal states: 102, 103, 203, 204, 401, 402

### Order Header Fields

Key fields beyond status:
- **aftersale_status**: 0=eligible, 1=user applied, 2=admin approved, 3=refund success, 4=admin rejected, 5=user cancelled. Tracks after-sale state independently from order status. Updated when user requests refund (set to 1) and when admin processes refund (set to 3) or rejects (set to 4).
- **comments**: Integer count of order goods eligible for comment. Set during order confirmation, decremented when each order goods is reviewed or when review window expires.
- **Price fields**: goods_price, freight_price, coupon_price, groupon_price, integral_price, order_price, actual_price
- **Shipping fields**: consignee, mobile, address (snapshot), ship_sn, ship_channel, ship_time
- **Timestamps**: pay_time, confirm_time, end_time, refund_time

## Order Creation Flow

### Input Parameters

- cart_id (0 = all checked items)
- address_id (required)
- coupon_id (0 = no coupon, -1 = don't use)
- user_coupon_id (if using coupon)
- message (user note, optional)
- groupon_rules_id (optional, for group buying)
- groupon_link_id (optional, for joining existing group)

### Flow Steps

1. **Validate user**: must be logged in
2. **Load cart items**: get checked items for current user
3. **Validate address**: address must exist and belong to user
4. **Validate stock**: for each cart item, check SKU stock >= quantity
5. **Validate coupon** (if provided): check coupon status, validity, min spend, product restrictions
6. **Validate groupon** (if provided): check rules status, expiry, capacity, user participation
7. **Calculate prices**: compute goods_price, freight_price, coupon_price, groupon_price, order_price, actual_price
8. **Generate order number**: date (8 digits) + random (6 digits), must be unique
9. **Create order record**: insert into order table with all price components and address snapshot
10. **Create order goods records**: one row per cart item with goods snapshot data
11. **Decrease SKU stock**: for each item, decrement stock in goods_product table
12. **Clear cart items**: remove ordered items from cart
13. **Mark coupon used** (if applicable): update coupon_user status to used
14. **Handle zero-amount order**: if actual_price = 0, skip payment, set status to 201 (Paid)
15. **Handle groupon** (if applicable): create groupon record
16. **Schedule auto-cancel**: if actual_price > 0, schedule timeout task for auto-cancellation

### Transaction Boundary

Steps 9-15 must execute within a single transaction. If any step fails, entire order creation rolls back.

## Order Payment

### Payment Flow (Deferred: WeChat Pay in MVP is mocked)

1. User calls pre-pay API with order_id
2. System validates order is in status 101 (Unpaid)
3. System validates order belongs to current user
4. If mocked: return mock payment parameters
5. If real WeChat Pay: call WeChat Pay API, return payment session info
6. User completes payment
7. Payment callback received
8. System validates callback signature and amount
9. Update order: status → 201, pay_time, pay_id
10. Handle groupon status update (if applicable)
11. Send notifications (user SMS, admin email)
12. Cancel auto-cancel scheduled task

### Zero-Amount Orders

- If actual_price = 0 after all discounts, order is immediately set to status 201 (Paid)
- This can happen with 100% coupon discount or group-buy full discount
- No payment callback needed

## Order Cancellation

### User Cancellation

- Only allowed for status 101 (Unpaid)
- Actions within transaction:
  1. Set order status to 102
  2. Set order end_time
  3. Restore SKU stock for all order goods
  4. Restore coupon (set coupon_user status back to unused) if coupon was used

### System Auto-Cancellation

- Triggered by timeout (configurable, default 30 minutes)
- Same actions as user cancellation but sets status to 103
- Scheduled task checks for orders in status 101 older than timeout threshold

## Order Shipping (Admin)

- Only allowed for status 201 (Paid)
- Input: order_id, ship_sn (tracking number), ship_channel (courier company)
- Actions:
  1. Set order status to 301
  2. Set ship_time, ship_sn, ship_channel

## Order Confirmation (User)

- Only allowed for status 301 (Shipped)
- User confirmation:
  1. Set order status to 401
  2. Set confirm_time
  3. Set comments count (number of order goods eligible for comment)
- System auto-confirmation:
  - Triggered by timeout (configurable, default 7 days after shipping)
  - Same actions but sets status to 402

## Order Refund (Admin)

- Only allowed for status 202 (Refund requested)
- Actions within transaction:
  1. Set order status to 203 (Refunded)
  2. Set refund amount, refund time, refund type, refund note
  3. Restore SKU stock
  4. Restore coupon (if used)
  5. Update after-sale status (if applicable)
  6. Execute actual refund via payment platform (deferred in MVP)

## Order Query

### User Order List

- Filter by status group: all, unpaid (101), unshipped (201), shipped (301), uncommented (401/402 with uncommented goods)
- Paginated, sorted by creation time desc
- Include order goods list with snapshot data

### User Order Detail

- Validate order belongs to current user
- Include: order info, address snapshot, order goods list, price breakdown, status with available actions
- If shipped: include tracking info (ship_sn, ship_channel, ship_time)

### Admin Order List

- Filters: user info, order number, status, date range
- Paginated with full order details
- Include user info, address, order goods, all timestamps

### Admin Order Detail

- Full order information including all price components, timestamps, and operation history

## After-Sale (Deferred in MVP)

### Data Model

- Fields: order_id, user_id, type (0=refund without return, 1=refund without return after receipt, 2=return and refund), reason, amount, desc, status, pic_urls

### Status Machine

| Code | Name | Actions |
|------|------|---------|
| 0 | Eligible | User apply |
| 1 | User applied | Admin approve/reject, User cancel |
| 2 | Admin approved | Admin refund |
| 3 | Refund success | Terminal |
| 4 | Admin rejected | Terminal |
| 5 | User cancelled | Terminal |

### Rules

- Only orders in status 401/402 (Received) can apply for after-sale
- One after-sale per order
- Admin approval triggers refund execution and stock restoration

## Data Consistency Rules

### Concurrency Control for Stock (Critical)

Stock decrement during order creation is the highest-risk area for data corruption.

- **Strategy**: Use conditional UPDATE: `UPDATE goods_product SET stock = stock - ? WHERE id = ? AND stock >= ?`. If affected rows = 0, stock is insufficient.
- **Alternative**: Use Nop ORM optimistic locking (version field) with retry (max 3 retries).
- **Stock is NOT reserved at add-to-cart time** — only checked for availability. Reservation happens at order creation time within the order transaction.
- **Race condition handling**: If two users order the last item simultaneously, the first transaction commits and the second gets "stock insufficient" error. The second user receives a clear error message.

### Order Idempotency

- Frontend should generate an idempotency key (UUID) for each order submission.
- Backend checks: within 5 minutes, if an order with the same user_id + idempotency_key exists, return the existing order instead of creating a new one.
- This prevents duplicate orders from double-clicks or network retries.

### Cart Price Handling

- Cart stores price at add-to-cart time for **display purposes only**.
- **Order creation always uses the current live SKU price** from goods_product table, not the cart's stored price.
- If the price has changed since add-to-cart, the order reflects the current price. The checkout preview should show current prices to avoid surprises.

### Order Number Generation

- Format: YYYYMMDD + 6-digit random number
- Must be unique; regenerate on collision
- Alternative (from mall): YYYYMMDD + sequence number with database sequence

### Address Snapshot

- Order records the address snapshot at order creation time (recipient name, phone, full address)
- This ensures order shows correct address even if user later modifies or deletes the address

### Goods Snapshot

- OrderGoods records the goods snapshot at order time (name, pic, price, specifications, goods_sn)
- This ensures order shows correct info even if goods is later modified or deleted

### Price Validation

- On payment callback: verify callback amount matches order actual_price (in cents)
- On order creation: verify all computed prices are consistent
- actual_price must be >= 0

## Nop Platform Implementation Notes

- Order BizModel extends `CrudBizModel<Order>` with custom `@BizMutation` methods for state transitions
- Each state transition method validates current status before updating
- Use `@BizMutation` auto-transaction wrapping; do not add `@Transactional`
- Cart BizModel provides query and mutation endpoints
- After-sale BizModel follows same pattern with its own state machine
- Stock operations use Nop ORM's entity-level update with optimistic locking
- Order number generation uses `CoreMetrics.currentTimeMillis()` and `StringHelper` utilities
- Price calculations use `BigDecimal` for precision; store as decimal(10,2) in database

## Reference Projects Comparison

| Aspect | mall (macrozheng) | litemall | nop-app-mall |
|--------|-------------------|----------|-------------|
| Order states | 6 states (0-5) | 10 states (101-402) | 10 states (101-402, litemall style) |
| After-sale | Separate return apply flow | Unified aftersale entity | Unified aftersale (litemall style) |
| Auto-cancel | RabbitMQ delayed message | Scheduled task | Scheduled task (simpler) |
| Price breakdown | 5 components | 7 components | 7 fields (integral=0 in MVP) |
| Stock locking | Dedicated lock_stock field | Application-level check | Application-level check |
| Order audit trail | oms_order_operate_history table | No dedicated table | No dedicated table (deferred) |
