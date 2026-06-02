# Order and Cart Business Design

## Purpose

Describe cart behavior, checkout, order lifecycle, price semantics, and refund-related order flows for `nop-app-mall`.

## Boundary

- This document owns order and cart business semantics, user/admin actions, and state transitions.
- Persisted fields, dictionaries, and exact stored status values are defined in `model/app-mall.orm.xml`.
- Technical transaction, locking, scheduling, and integration implementation belongs in `docs/architecture/`.

## Domain Overview

The order domain covers:

- cart management before purchase
- checkout preview and order submission
- payment and shipment progression
- receipt confirmation and post-order actions
- refund and after-sale flows

Core business concepts:

- Cart stores the user's intended purchase selections before order submission.
- Order is the commercial contract between user and mall.
- Order goods captures the goods and SKU snapshot purchased in that order.
- After-sale is a separate refund or return process after fulfillment events.

## Shopping Cart

### Business Rules

- Users must be authenticated to maintain a cart.
- The same user and the same SKU should merge into one cart line instead of creating duplicates.
- Only currently sellable goods may be added to cart.
- Quantity changes must respect current availability.
- Checked state determines which cart lines participate in checkout.

### Supported Behavior

- Add goods to cart by SKU and quantity.
- View cart with goods information, selected specifications, quantity, checked state, and subtotal behavior.
- Change quantity.
- Check or uncheck individual or all items.
- Remove items or clear cart.
- Preview checkout from checked items.

## Price Semantics

The order price model contains these business components:

- goods price: subtotal of ordered items
- freight price: shipping fee based on current freight policy
- coupon price: reserved price component for future coupon support
- groupon price: reserved price component for future group-buy support
- integral price: points discount, reserved for future support
- order price: pre-payment amount after shipping and discounts
- actual price: final amount the user needs to pay

### Freight Rules

- Freight policy is centrally configured.
- Free-shipping threshold is centrally configured.
- Checkout must show whether freight is charged or waived.

## Order State Machine

### Business States

The product baseline uses these business states. The persisted status-code dictionary is maintained in `model/app-mall.orm.xml`.

| Business State | Meaning | User Actions | Admin Actions |
| -------------- | ------- | ------------ | ------------- |
| Unpaid | Order created and waiting for payment | cancel, pay | none |
| Cancelled by user | User cancelled before payment | delete | none |
| Cancelled by system | Payment timeout closed the order | delete | none |
| Paid | Payment confirmed and waiting for shipment | request refund | ship, refund |
| Refund requested | User requested refund before shipment | none | approve refund, reject refund, direct refund |
| Refunded | Refund completed | delete | none |
| Shipped | Goods shipped and waiting for receipt confirmation | confirm receipt | none |
| Received by user | User confirmed receipt | delete | none |
| Received by system | System auto-confirmed receipt | delete | none |

### Transition Rules

- Unpaid orders can become user-cancelled, system-cancelled, or paid.
- Paid orders can move to refund-requested, refunded, or shipped.
- Refund-requested orders can return to paid on rejection or move to refunded on approval or direct admin refund.
- Shipped orders can move to received by user or received by system.
- Terminal states support deletion from the user's visible list through soft-delete semantics.

### State Narrative

```text
Unpaid
  -> Cancelled by user
  -> Cancelled by system
  -> Paid
       -> Refund requested
            -> Refunded
            -> Paid
       -> Refunded [direct admin refund]
       -> Shipped
            -> Received by user
            -> Received by system
```

### Related State Extensions

- Groupon-enabled orders may introduce a dedicated groupon-timeout business state.
- Comment/review and after-sale for received orders extend the allowed user actions after receipt without changing the core payment/shipment/receipt progression.

## Order Creation

### Business Preconditions

- User must be authenticated.
- Checkout items must come from the user's current checked cart selection or an equivalent direct-buy path.
- Delivery address must belong to the user.
- Ordered SKU quantity must still be available at submission time.
- Delivery-related pricing rules and any enabled discount mechanism must still be valid at submission time.

### Business Outcome

- The order captures a snapshot of the purchased goods, SKU selection, price composition, and delivery information.
- Ordered cart lines leave the active cart after successful submission.
- A zero-amount order is treated as paid without waiting for an external payment step.

## Payment, Shipping, And Completion

### Payment

- The business contract still distinguishes unpaid and paid orders.
- Payment confirmation moves the order into the paid state.
- Production payment behavior is an integration-owned capability; WeChat Pay details belong in the relevant integration architecture and code.
- Development or local-test payment substitutes must be explicitly marked non-production and must not redefine commercial payment behavior.

### Shipping

- Only paid orders can be shipped.
- Shipping records carrier and tracking information for user follow-up.

### Receipt Confirmation

- Only shipped orders can be confirmed as received.
- Receipt confirmation completes the core fulfillment lifecycle and becomes the boundary for comment/review and after-sale eligibility.
- System auto-confirmation exists as a fallback when the user does not confirm in time.

## Refund And After-Sale

### Refund Scope

- Paid but unshipped orders may enter refund-requested state.
- Admin users may directly refund eligible paid but unshipped orders.
- Admin users may also approve or reject a user-initiated refund request.
- Approved refunds restore the order to a terminal refunded state.

### After-Sale Scope

- After-sale for received orders covers refund-only and return-and-refund scenarios after receipt.
- After-sale eligibility and approval rules must preserve the meaning of the completed fulfillment lifecycle.

## Query And Presentation Rules

- Users can filter order lists by business-relevant status groups such as unpaid, unshipped, and shipped.
- Order detail must show price composition, goods snapshot, delivery snapshot, current business state, and available next actions.
- Admin order views must support search by user, order number, state, and time range.

## Consistency Rules

- Checkout and order submission must use current sellable price and availability, not stale cart assumptions.
- Order detail must preserve the meaning of purchased goods, selected specifications, and delivery data even if the underlying catalog or address records later change.
- State transitions must follow the allowed business flow and must not silently skip required intermediate meaning.
