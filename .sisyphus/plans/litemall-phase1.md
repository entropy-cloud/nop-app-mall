# Litemall Phase 1: Order + Cart Business Logic Implementation

## TL;DR

> **Quick Summary**: Implement the core e-commerce business logic for Cart and Order modules on Nop platform, following Nop DDD patterns (Entity=read-only helpers, BizModel=mutable logic), using TDD approach and Mock payment.
> 
> **Deliverables**:
> - LitemallOrder Entity: Add read-only helpers (`canBeCancelled()`, `canBeShipped()`, etc.)
> - LitemallCartBizModel: addToCart, updateQuantity, deleteCart, clearCart, checkout
> - LitemallOrderBizModel: submitOrder, cancelOrder, payOrder, shipOrder, confirmReceive
> - TDD test files for all operations
> - Error codes (extend AppMallErrors.java)
> 
> **Estimated Effort**: Large
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Entity Helpers → Cart Module → Order Module

---

## Context

### Original Request
Implement litemall-requirements.md business logic on Nop platform, focusing on Order and Cart modules in Phase 1.

### Interview Summary
**Key Discussions**:
- **Implementation Scope**: Phased approach, Phase 1 = Order + Cart
- **ORM Model**: Use existing model (no modifications)
- **Test Strategy**: TDD with nop-autotest
- **Frontend**: Backend API only
- **Payment**: Mock payment (simple status change)

**Research Findings**:
- **Existing State**: 25+ entities, all BizModels generated as empty stubs
- **Test Infrastructure**: nop-autotest with @EnableSnapshot pattern
- **Pattern Reference**: LitemallAftersaleBizModel shows @BizMutation structure

### Metis Review
**Identified Gaps** (addressed):
- **Stock Concurrency**: Need optimistic locking or explicit stock handling
- **Transaction Strategy**: Use @BizMutation for auto-transaction
- **Order Number Generation**: Implement date(8) + random(6) pattern
- **Error Codes**: Use NopException with specific error codes

### Nop DDD Review (Second Pass)
**Violations Fixed**:
- ~~OrderStateMachine helper class~~ → **Entity read-only helpers** (canBeCancelled, canBeShipped, etc.)
- ~~State validation in helper~~ → **State checks in Entity, transitions in BizModel**
- Added proper data access patterns: `requireEntity()`, `newEntity()`, `updateEntity()`
- Added proper dependency injection: `@Inject @Named("biz_EntityName")`
- Added proper child entity creation: `order.getOrderGoods().add(item)`

---

## Work Objectives

### Core Objective
Implement complete Cart → Order flow business logic with proper state machine, stock management, and price calculation.

### Concrete Deliverables
- `LitemallOrder.java` (Entity): Add read-only helpers (`canBeCancelled()`, `canBeShipped()`, `canBePaid()`, etc.)
- `LitemallCartBizModel.java` with 5 custom methods
- `LitemallOrderBizModel.java` with 6 custom methods
- `TestLitemallCartBizModel.java` with test cases
- `TestLitemallOrderBizModel.java` with test cases
- Extend `AppMallErrors.java` with order/cart error codes

### Definition of Done
- [ ] All Cart operations work: add, update, delete, clear, checkout
- [ ] All Order operations work: submit, cancel, pay, ship, confirm
- [ ] Order state machine enforces valid transitions
- [ ] Stock deducted on order, restored on cancel
- [ ] All tests pass with snapshot verification

### Must Have
- **Entity read-only helpers** for state checks (canBeCancelled, canBeShipped, etc.)
- **BizModel mutation methods** for state transitions (cancel, ship, confirm)
- Stock management (deduct/restore)
- Price calculation (goods + freight)
- TDD test coverage for all methods

### Must NOT Have (Guardrails)
- ❌ No ORM modifications
- ❌ No frontend work
- ❌ No real WeChat payment integration
- ❌ No GroupBuy/Coupon business logic (Phase 2)
- ❌ No Admin-specific APIs
- ❌ No scheduled tasks (auto-cancel, auto-confirm)
- ❌ No external service calls (SMS, email)
- ❌ No DTO classes (use Map/ApiRequest)

### Nop DDD Guardrails (CRITICAL)
- ❌ **No enum for entity fields** - Use String + Constants (dict from database)
- ❌ **No business logic in Entity** - Entity only has read-only helpers
- ❌ **No dao() direct calls** - Use `this.requireEntity()`, `doFindList()`, etc.
- ❌ **No @Transactional with @BizMutation** - @BizMutation auto-starts transaction
- ❌ **No `new EntityClass()`** - Use `newEntity("EntityName")` for Delta support
- ❌ **No concrete class injection** - Inject interfaces (`ICrudBiz<Entity>`) with `@Named("biz_EntityName")`
- ✅ **Entity = read-only helpers only** - `canBeCancelled()`, `calculateTotal()`
- ✅ **BizModel = mutable business logic** - cancel(), ship(), confirm()
- ✅ **Use CrudBizModel methods** - `requireEntity()`, `doFindList()`, `updateEntity()`
- ✅ **Use NopException only** - With error codes from AppMallErrors

---

## Verification Strategy (MANDATORY)

> **UNIVERSAL RULE: ZERO HUMAN INTERVENTION**
> ALL tasks MUST be verifiable WITHOUT any human action.

### Test Decision
- **Infrastructure exists**: YES (nop-autotest)
- **Automated tests**: YES (TDD)
- **Framework**: nop-autotest with JunitAutoTestCase

### Test Pattern
```java
@NopTestConfig
public class TestLitemallOrderBizModel extends JunitAutoTestCase {
    @Inject
    IGraphQLEngine graphQLEngine;

    @EnableSnapshot
    @Test
    public void testSubmitOrder() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ApiRequest<?> request = input("request.json5", ApiRequest.class);
        IGraphQLExecutionContext context = graphQLEngine.newRpcContext(
            GraphQLOperationType.mutation, "LitemallOrder__submitOrder", request);
        Object result = FutureHelper.syncGet(graphQLEngine.executeRpcAsync(context));
        output("response.json5", result);
    }
}
```

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
├── Task 1: Add read-only helpers to LitemallOrder Entity
├── Task 2: Extend error code constants
└── Task 3: Setup test data files

Wave 2 (After Wave 1):
├── Task 4: Implement Cart - addToCart (TDD)
├── Task 5: Implement Cart - updateQuantity (TDD)
├── Task 6: Implement Cart - deleteCart/clearCart (TDD)
└── Task 7: Implement Cart - checkout (TDD)

Wave 3 (After Wave 2):
├── Task 8: Implement Order - submitOrder (TDD)
├── Task 9: Implement Order - cancelOrder (TDD)
├── Task 10: Implement Order - payOrder (TDD)
├── Task 11: Implement Order - shipOrder (TDD)
└── Task 12: Implement Order - confirmReceive (TDD)

Critical Path: Task 1 (Entity helpers) → Task 4-7 (Cart) → Task 8-12 (Order)
Parallel Speedup: ~50% faster than sequential
```

### Nop DDD Pattern Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ Entity (LitemallOrder)                                          │
│ ┌─────────────────────────────────────────────────────────────┐│
│ │ Read-Only Helpers (Task 1):                                  ││
│ │ • canBeCancelled() → orderStatus == 101                     ││
│ │ • canBeShipped() → orderStatus == 201                       ││
│ │ • canBePaid() → orderStatus == 101                          ││
│ │ • canBeConfirmed() → orderStatus == 301                     ││
│ └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
                              ↓ Used by
┌─────────────────────────────────────────────────────────────────┐
│ BizModel (LitemallOrderBizModel)                                │
│ ┌─────────────────────────────────────────────────────────────┐│
│ │ @BizMutation Methods (Tasks 8-12):                           ││
│ │ • cancelOrder() → if (!order.canBeCancelled()) throw...      ││
│ │ • shipOrder() → if (!order.canBeShipped()) throw...          ││
│ │ • payOrder() → if (!order.canBePaid()) throw...              ││
│ │ • confirmReceive() → if (!order.canBeConfirmed()) throw...   ││
│ │                                                              ││
│ │ Data Access: this.requireEntity(), doFindList(), update()   ││
│ │ Entity Creation: newEntity(), order.getOrderGoods().add()   ││
│ └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 8-12 | 2, 3 |
| 2 | None | 4-12 | 1, 3 |
| 3 | None | 4-12 | 1, 2 |
| 4 | 2, 3 | 8 | 5, 6, 7 |
| 5 | 2, 3 | None | 4, 6, 7 |
| 6 | 2, 3 | None | 4, 5, 7 |
| 7 | 2, 3 | 8 | 4, 5, 6 |
| 8 | 1, 7 | None | 9, 10, 11, 12 |
| 9 | 1 | None | 8, 10, 11, 12 |
| 10 | 1 | None | 8, 9, 11, 12 |
| 11 | 1 | None | 8, 9, 10, 12 |
| 12 | 1 | None | 8, 9, 10, 11 |

---

## TODOs

- [ ] 1. Add Read-Only Helpers to LitemallOrder Entity

  **What to do** (Nop DDD Pattern: Entity = Read-Only Helpers):
  - Extend `LitemallOrder` entity class (in `app-mall-dao`) with read-only helper methods
  - Add `canBeCancelled()` - returns true if orderStatus=101 (unpaid)
  - Add `canBeShipped()` - returns true if orderStatus=201 (paid)
  - Add `canBePaid()` - returns true if orderStatus=101 (unpaid)
  - Add `canBeConfirmed()` - returns true if orderStatus=301 (shipped)
  - Use `MallOrderStatus` constants (already generated from dict)
  - All methods are **pure functions** - no state modification

  **Nop DDD Rules**:
  - ✅ Entity methods must be read-only (no field modification)
  - ✅ Use String constants, NOT enum
  - ✅ Return boolean for state checks
  - ❌ NEVER put mutable business logic in Entity

  **Must NOT do**:
  - Do NOT add any `setXxx()` calls in entity methods
  - Do NOT add complex business logic (that goes in BizModel)
  - Do NOT call external services or databases

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple read-only helper methods
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3)
  - **Blocks**: Tasks 8-12 (Order implementation)
  - **Blocked By**: None

  **References**:
  - `docs/litemall-requirements.md:2490-2523` - Order state definitions
  - `nop-entropy/docs-for-ai/03-development-guide/ddd-in-nop.md:376-440` - Entity read-only helpers pattern
  - `model/app-mall.orm.xml:40-51` - Order status dict definition
  - `app-mall-dao/src/main/java/app/mall/dao/entity/LitemallOrder.java` - Target file

  **Code Example**:
  ```java
  // In LitemallOrder.java
  public boolean canBeCancelled() {
      return MallOrderStatus.CREATED.equals(this.orderStatus);
  }
  
  public boolean canBeShipped() {
      return MallOrderStatus.PAY.equals(this.orderStatus);
  }
  
  public boolean canBePaid() {
      return MallOrderStatus.CREATED.equals(this.orderStatus);
  }
  
  public boolean canBeConfirmed() {
      return MallOrderStatus.SHIP.equals(this.orderStatus);
  }
  ```

  **Acceptance Criteria**:
  - [ ] LitemallOrder has canBeCancelled() method
  - [ ] LitemallOrder has canBeShipped() method
  - [ ] LitemallOrder has canBePaid() method
  - [ ] LitemallOrder has canBeConfirmed() method
  - [ ] All methods use MallOrderStatus constants (NOT hardcoded values)
  - [ ] No field modifications in any method

  **Commit**: YES
  - Message: `feat(entity): add read-only helpers to LitemallOrder`
  - Files: `app-mall-dao/src/main/java/app/mall/dao/entity/LitemallOrder.java`

---

- [ ] 2. Extend Error Code Constants

  **What to do**:
  - Extend existing `AppMallErrors.java` (in `app-mall-biz` module)
  - Add order errors: ERR_ORDER_NOT_FOUND, ERR_ORDER_STATUS_INVALID, ERR_ORDER_NOT_OWNER
  - Add cart errors: ERR_CART_EMPTY, ERR_CART_STOCK_INSUFFICIENT
  - Add goods errors: ERR_GOODS_NOT_FOUND, ERR_GOODS_OFF_SHELF
  - Use NopException pattern with error code constants

  **Must NOT do**:
  - Do not create custom exception classes
  - Do not create a new error file (extend existing)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple constants extension
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3)
  - **Blocks**: Tasks 4-12
  - **Blocked By**: None

  **References**:
  - `docs/litemall-requirements.md:3380-3410` - Error code definitions
  - `app-mall-biz/src/main/java/app/mall/biz/AppMallErrors.java` - Existing error codes to extend

  **Acceptance Criteria**:
  - [ ] AppMallErrors.java extended with order errors
  - [ ] AppMallErrors.java extended with cart errors
  - [ ] All error codes have descriptive messages in Chinese

  **Commit**: YES
  - Message: `feat(core): extend error codes for order and cart`
  - Files: `app-mall-biz/src/main/java/app/mall/biz/AppMallErrors.java`

---

- [ ] 3. Setup Test Data Files

  **What to do**:
  - Create test data CSV files in `_cases/app/mall/service/` directories
  - Include: test user, test goods, test product, test cart items
  - Create baseline snapshot files for tests
  - Follow nop-autotest conventions

  **Must NOT do**:
  - Do not modify production data
  - Do not create overly complex test data

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2)
  - **Blocks**: Tasks 4-12
  - **Blocked By**: None

  **References**:
  - `app-mall-service/src/test/java/app/mall/service/TestLitemallGoodsBizModel.java` - Existing test pattern
  - `nop-entropy/docs-for-ai/11-test-and-debug/autotest-guide.md` - Test conventions

  **Acceptance Criteria**:
  - [ ] Test data CSV files created
  - [ ] Test user with id="1" exists
  - [ ] Test goods with stock exists
  - [ ] Test product linked to goods exists

---

- [ ] 4. Implement Cart - addToCart (TDD)

  **What to do**:
  - Write test first: `testAddToCartNew`, `testAddToCartMerge`, `testAddToCartInsufficientStock`
  - Implement `addToCart(goodsId, productId, number)` in LitemallCartBizModel
  - Use `requireEntity()` to check goods (with `@Inject @Named("biz_LitemallGoods")`)
  - Use `newEntity()` to create cart item (NOT `new LitemallCart()`)
  - Check goods exists and isOnSale=true
  - Check product has sufficient stock
  - If same goodsId+productId exists, merge by adding numbers
  - Create new cart entry with checked=true

  **Nop DDD Pattern**:
  ```java
  @BizMutation
  public Map<String, Object> addToCart(@Name("goodsId") String goodsId,
                                        @Name("productId") String productId,
                                        @Name("number") Integer number,
                                        IServiceContext context) {
      // ✅ Use requireEntity through injected Biz
      LitemallGoods goods = goodsBiz.requireEntity(goodsId, "read", context);
      
      // Check business rules
      if (!goods.getIsOnSale()) {
          throw new NopException(AppMallErrors.ERR_GOODS_OFF_SHELF);
      }
      
      // ✅ Use newEntity() for Delta support
      LitemallCart cart = newEntity();
      cart.setUserId(context.getUserId());
      cart.setGoodsId(goodsId);
      cart.setProductId(productId);
      cart.setNumber(number);
      cart.setChecked(true);
      
      return save(cart, context);
  }
  ```

  **Must NOT do**:
  - ❌ Do NOT use `dao().getEntityById()` - use `requireEntity()`
  - ❌ Do NOT use `new LitemallCart()` - use `newEntity()`
  - Do NOT actually deduct stock (only check)
  - Do NOT create DTOs

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: Business logic with validation
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6, 7)
  - **Blocks**: None
  - **Blocked By**: Tasks 1, 2, 3

  **References**:
  - `docs/litemall-requirements.md:875-897` - addToCart business rules
  - `model/app-mall.orm.xml:277-324` - LitemallCart entity definition
  - `app-mall-service/src/main/java/app/mall/service/entity/LitemallCartBizModel.java` - Target file

  **Acceptance Criteria**:
  - [ ] Test: testAddToCartNew passes
  - [ ] Test: testAddToCartMerge passes (existing item number increases)
  - [ ] Test: testAddToCartInsufficientStock passes (error 461)
  - [ ] Test: testAddToCartGoodsOffShelf passes (error 460)
  - [ ] Cart entry created with checked=true
  - [ ] Returns cartCount and success=true

  **Agent-Executed QA Scenarios**:
  ```
  Scenario: Add new item to cart
    Tool: Bash (curl via GraphQL)
    Preconditions: User logged in (userId=1), goods exists, product stock=10
    Steps:
      1. POST /graphql with mutation LitemallCart__addToCart
         {goodsId: "1", productId: "1", number: 2}
      2. Assert: HTTP 200
      3. Assert: Response.cartCount = 1
      4. Assert: Response.success = true
    Expected Result: Cart entry created
    Evidence: Response JSON

  Scenario: Add duplicate item merges quantity
    Tool: Bash
    Preconditions: Cart has goodsId=1, productId=1, number=2
    Steps:
      1. Add same item with number=3
      2. Assert: Cart entry number=5 (2+3)
      3. Assert: Only 1 cart entry for this goodsId+productId
    Expected Result: Quantities merged
    Evidence: Cart query response

  Scenario: Add item with insufficient stock
    Tool: Bash
    Preconditions: Product stock=2
    Steps:
      1. Add item with number=5
      2. Assert: HTTP 400
      3. Assert: Error code 461 (库存不足)
    Expected Result: Error returned, no cart entry
    Evidence: Error response
  ```

  **Commit**: YES
  - Message: `feat(cart): implement addToCart with TDD`
  - Files: `LitemallCartBizModel.java`, `TestLitemallCartBizModel.java`

---

- [ ] 5. Implement Cart - updateQuantity (TDD)

  **What to do**:
  - Write test: `testUpdateQuantity`, `testUpdateQuantityInsufficientStock`
  - Implement `updateQuantity(cartId, number)` in LitemallCartBizModel
  - Verify cart belongs to current user
  - Check stock if increasing quantity
  - Validate number between 1-999

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 6, 7)

  **References**:
  - `docs/litemall-requirements.md:918-939` - updateQuantity business rules

  **Acceptance Criteria**:
  - [ ] Test: testUpdateQuantity passes
  - [ ] Test: testUpdateQuantityInsufficientStock passes
  - [ ] Test: testUpdateQuantityNotOwner passes (security check)
  - [ ] Test: testUpdateQuantityInvalidRange passes (1-999 validation)

---

- [ ] 6. Implement Cart - deleteCart/clearCart (TDD)

  **What to do**:
  - Write tests for deleteCart and clearCart
  - Implement `deleteCart(cartId)` - single item delete
  - Implement `clearCart()` - delete all user's cart items
  - Verify ownership before delete
  - Use logical delete (deleted=true)

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5, 7)

  **References**:
  - `docs/litemall-requirements.md:940-957` - deleteCart business rules

  **Acceptance Criteria**:
  - [ ] Test: testDeleteCart passes
  - [ ] Test: testClearCart passes
  - [ ] Test: testDeleteCartNotOwner passes
  - [ ] Returns updated cartCount and cartTotal

---

- [ ] 7. Implement Cart - checkout (TDD)

  **What to do**:
  - Write test: `testCheckout`, `testCheckoutInsufficientStock`
  - Implement `checkout(cartIds)` in LitemallCartBizModel
  - Get cart items (only checked=true if cartIds=0)
  - Verify stock availability
  - Calculate goodsPrice (sum of price * number)
  - Calculate freightPrice (free if goodsPrice >= threshold, else fixed)
  - Return preview without creating order

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5, 6)
  - **Blocks**: Task 8
  - **Blocked By**: Tasks 1, 2, 3

  **References**:
  - `docs/litemall-requirements.md:958-981` - checkout business rules
  - `docs/litemall-requirements.md:2412-2419` - freight rules

  **Acceptance Criteria**:
  - [ ] Test: testCheckout passes
  - [ ] Test: testCheckoutEmptyCart passes (error)
  - [ ] Test: testCheckoutInsufficientStock passes
  - [ ] Test: testCheckoutFreeShipping passes
  - [ ] Returns: cartGoods list, goodsPrice, freightPrice, orderPrice

---

- [ ] 8. Implement Order - submitOrder (TDD)

  **What to do**:
  - Write test: `testSubmitOrderSuccess`, `testSubmitOrderInsufficientStock`
  - Implement `submitOrder(cartIds, addressId, couponId, userCouponId, message, grouponRulesId, grouponLinkId)`
  - **Nop DDD Pattern**: Use `@BizMutation` (auto-transaction)
  - Use injected BizModels: `@Inject @Named("biz_LitemallAddress")`, `@Inject @Named("biz_LitemallCart")`
  - Use `requireEntity()` to get address and cart items
  - Use `newEntity()` to create Order and OrderGoods
  - Copy address fields to order (consignee, mobile, address)
  - Create Order with orderStatus=101 (MallOrderStatus.CREATED)
  - Add OrderGoods to order's collection: `order.getOrderGoods().add(orderGoods)`
  - Deduct stock via product entity modification
  - Delete cart items using `cartBiz.deleteEntity(cart, context)`
  - Generate orderSn: date(8) + random(6)

  **Nop DDD Pattern Example**:
  ```java
  @BizMutation  // Auto-transaction, no @Transactional needed
  public Map<String, Object> submitOrder(
          @Name("cartIds") List<String> cartIds,
          @Name("addressId") String addressId,
          IServiceContext context) {
      
      // ✅ Get address with permission check
      LitemallAddress address = addressBiz.requireEntity(addressId, "read", context);
      
      // ✅ Create order using newEntity()
      LitemallOrder order = newEntity();
      order.setUserId(context.getUserId());
      order.setOrderStatus(MallOrderStatus.CREATED);  // Use Constants, not enum
      order.setOrderSn(generateOrderSn());
      order.setConsignee(address.getName());
      order.setMobile(address.getTel());
      order.setAddress(address.getAddressDetail());
      
      // Process cart items
      for (String cartId : cartIds) {
          LitemallCart cart = cartBiz.requireEntity(cartId, "delete", context);
          
          // ✅ Create OrderGoods using newEntity()
          LitemallOrderGoods orderGoods = newEntity("LitemallOrderGoods");
          orderGoods.setGoodsId(cart.getGoodsId());
          orderGoods.setProductId(cart.getProductId());
          orderGoods.setNumber(cart.getNumber());
          orderGoods.setPrice(cart.getPrice());
          
          // ✅ Add to parent's collection (auto-saves on order save)
          order.getOrderGoods().add(orderGoods);
          
          // Deduct stock
          LitemallGoodsProduct product = productBiz.requireEntity(cart.getProductId(), "update", context);
          product.setNumber(product.getNumber() - cart.getNumber());
          
          // Delete cart item
          cartBiz.deleteEntity(cart, context);
      }
      
      // Calculate prices
      order.calculatePrices();  // Entity read-only helper
      
      // Save order (auto-saves OrderGoods via cascade)
      return save(order, context);
  }
  ```

  **Must NOT do**:
  - ❌ Do NOT use `new LitemallOrder()` - use `newEntity()`
  - ❌ Do NOT use `dao().saveEntity()` - use `save()` or `updateEntity()`
  - ❌ Do NOT set collection directly: `order.setOrderGoods(list)` - use `order.getOrderGoods().add(item)`
  - Do NOT handle coupon (Phase 2)
  - Do NOT handle groupon (Phase 2)
  - Do NOT call payment service

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: Complex transactional logic with multiple entities
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 9-12)
  - **Blocks**: None
  - **Blocked By**: Tasks 1, 7

  **References**:
  - `docs/litemall-requirements.md:986-1043` - submitOrder business rules
  - `docs/litemall-requirements.md:341-378` - Order entity definition
  - `model/app-mall.orm.xml:978-1062` - LitemallOrder entity
  - `nop-entropy/docs-for-ai/03-development-guide/ddd-in-nop.md:916-950` - Creating child entities pattern

  **Acceptance Criteria**:
  - [ ] Test: testSubmitOrderSuccess passes
  - [ ] Test: testSubmitOrderInsufficientStock passes (rollback)
  - [ ] Test: testSubmitOrderEmptyCart passes
  - [ ] Test: testSubmitOrderInvalidAddress passes
  - [ ] Order created with orderStatus=101
  - [ ] OrderGoods created for each cart item
  - [ ] Stock decremented
  - [ ] Cart items deleted
  - [ ] Returns orderId and orderSn

  **Agent-Executed QA Scenarios**:
  ```
  Scenario: Submit order successfully
    Tool: Bash (curl via GraphQL)
    Preconditions: User has cart items, valid addressId
    Steps:
      1. POST /graphql with mutation LitemallOrder__submitOrder
         {cartIds: [1,2], addressId: 1, couponId: 0, message: ""}
      2. Assert: HTTP 200
      3. Assert: Response.orderId exists
      4. Assert: Response.orderSn matches pattern YYYYMMDDxxxxxx
    Expected Result: Order created
    Evidence: Order query response

  Scenario: Submit order rolls back on stock error
    Tool: Bash
    Preconditions: Product stock=1, cart has 2 items of same product
    Steps:
      1. Attempt submitOrder
      2. Assert: HTTP 400
      3. Assert: No order created
      4. Assert: Stock unchanged (1)
      5. Assert: Cart items still exist
    Expected Result: Full rollback
    Evidence: Database query
  ```

---

- [ ] 9. Implement Order - cancelOrder (TDD)

  **What to do**:
  - Write tests: `testCancelOrderUnpaid`, `testCancelOrderPaid`
  - Implement `cancelOrder(orderId)` in LitemallOrderBizModel
  - Use `this.requireEntity(orderId, "update", context)` for order access
  - **Use entity's read-only helper**: `order.canBeCancelled()`
  - Update orderStatus to MallOrderStatus.CANCEL
  - Restore stock via productBiz.updateEntity()
  - Set endTime
  - Use `updateEntity(order, context)` to save

  **Nop DDD Pattern Example**:
  ```java
  @BizMutation
  public Map<String, Object> cancelOrder(@Name("orderId") String orderId,
                                          IServiceContext context) {
      // ✅ Get order with permission check
      LitemallOrder order = this.requireEntity(orderId, "update", context);
      
      // ✅ Use entity's read-only helper
      if (!order.canBeCancelled()) {
          throw new NopException(AppMallErrors.ERR_ORDER_STATUS_INVALID)
              .param("status", order.getOrderStatus());
      }
      
      // Update status
      order.setOrderStatus(MallOrderStatus.CANCEL);
      order.setEndTime(LocalDateTime.now());
      
      // Restore stock
      for (LitemallOrderGoods orderGoods : order.getOrderGoods()) {
          LitemallGoodsProduct product = productBiz.requireEntity(
              orderGoods.getProductId(), "update", context);
          product.setNumber(product.getNumber() + orderGoods.getNumber());
          productBiz.updateEntity(product, context);
      }
      
      return update(order, context);
  }
  ```

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 8, 10, 11, 12)

  **References**:
  - `docs/litemall-requirements.md:1102-1131` - cancelOrder business rules
  - `nop-entropy/docs-for-ai/03-development-guide/ddd-in-nop.md:468-487` - Using entity helpers in BizModel

  **Acceptance Criteria**:
  - [ ] Test: testCancelOrderUnpaid passes (101→102)
  - [ ] Test: testCancelOrderPaid passes (error 451)
  - [ ] Test: testCancelOrderNotOwner passes
  - [ ] Uses order.canBeCancelled() for state check
  - [ ] Stock restored to original values

---

- [ ] 10. Implement Order - payOrder (TDD)

  **What to do**:
  - Write tests: `testPayOrderSuccess`, `testPayOrderAlreadyPaid`
  - Implement `payOrder(orderId)` - Mock payment
  - Use `this.requireEntity(orderId, "update", context)`
  - **Use entity's read-only helper**: `order.canBePaid()`
  - Update orderStatus to MallOrderStatus.PAY
  - Set payTime to now()
  - Generate payId (mock: timestamp + random)

  **Nop DDD Pattern Example**:
  ```java
  @BizMutation
  public Map<String, Object> payOrder(@Name("orderId") String orderId,
                                       IServiceContext context) {
      LitemallOrder order = this.requireEntity(orderId, "update", context);
      
      // ✅ Use entity's read-only helper
      if (!order.canBePaid()) {
          throw new NopException(AppMallErrors.ERR_ORDER_STATUS_INVALID);
      }
      
      order.setOrderStatus(MallOrderStatus.PAY);
      order.setPayTime(LocalDateTime.now());
      order.setPayId(generatePayId());
      
      return update(order, context);
  }
  ```

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 8, 9, 11, 12)

  **References**:
  - `docs/litemall-requirements.md:1044-1101` - Payment flow

  **Acceptance Criteria**:
  - [ ] Test: testPayOrderSuccess passes (101→201)
  - [ ] Uses order.canBePaid() for state check
  - [ ] Test: testPayOrderAlreadyPaid passes (error 452)
  - [ ] Test: testPayOrderNotOwner passes
  - [ ] payTime and payId set correctly

---

- [ ] 11. Implement Order - shipOrder (TDD)

  **What to do**:
  - Write tests: `testShipOrderSuccess`, `testShipOrderInvalidStatus`
  - Implement `shipOrder(orderId, shipSn, shipChannel)` - Admin operation
  - Use `this.requireEntity(orderId, "update", context)`
  - **Use entity's read-only helper**: `order.canBeShipped()`
  - Update orderStatus to MallOrderStatus.SHIP
  - Set shipSn, shipChannel, shipTime

  **Nop DDD Pattern Example**:
  ```java
  @BizMutation
  public Map<String, Object> shipOrder(@Name("orderId") String orderId,
                                        @Name("shipSn") String shipSn,
                                        @Name("shipChannel") String shipChannel,
                                        IServiceContext context) {
      LitemallOrder order = this.requireEntity(orderId, "update", context);
      
      if (!order.canBeShipped()) {
          throw new NopException(AppMallErrors.ERR_ORDER_STATUS_INVALID);
      }
      
      order.setOrderStatus(MallOrderStatus.SHIP);
      order.setShipSn(shipSn);
      order.setShipChannel(shipChannel);
      order.setShipTime(LocalDateTime.now());
      
      return update(order, context);
  }
  ```

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 8, 9, 10, 12)

  **References**:
  - `docs/litemall-requirements.md:1189-1214` - shipOrder business rules

  **Acceptance Criteria**:
  - [ ] Test: testShipOrderSuccess passes (201→301)
  - [ ] Test: testShipOrderInvalidStatus passes
  - [ ] Uses order.canBeShipped() for state check
  - [ ] shipSn, shipChannel, shipTime set correctly

---

- [ ] 12. Implement Order - confirmReceive (TDD)

  **What to do**:
  - Write tests: `testConfirmReceiveSuccess`, `testConfirmReceiveInvalidStatus`
  - Implement `confirmReceive(orderId)` in LitemallOrderBizModel
  - Use `this.requireEntity(orderId, "update", context)`
  - **Use entity's read-only helper**: `order.canBeConfirmed()`
  - Update orderStatus to MallOrderStatus.CONFIRM
  - Set confirmTime
  - Set comments = count of OrderGoods (for review tracking)

  **Nop DDD Pattern Example**:
  ```java
  @BizMutation
  public Map<String, Object> confirmReceive(@Name("orderId") String orderId,
                                             IServiceContext context) {
      LitemallOrder order = this.requireEntity(orderId, "update", context);
      
      // ✅ Use entity's read-only helper
      if (!order.canBeConfirmed()) {
          throw new NopException(AppMallErrors.ERR_ORDER_STATUS_INVALID);
      }
      
      order.setOrderStatus(MallOrderStatus.CONFIRM);
      order.setConfirmTime(LocalDateTime.now());
      order.setComments(order.getOrderGoods().size());  // For review tracking
      
      return update(order, context);
  }
  ```

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 8, 9, 10, 11)

  **References**:
  - `docs/litemall-requirements.md:1216-1241` - confirmReceive business rules

  **Acceptance Criteria**:
  - [ ] Test: testConfirmReceiveSuccess passes (301→401)
  - [ ] Test: testConfirmReceiveInvalidStatus passes
  - [ ] Test: testConfirmReceiveNotOwner passes
  - [ ] Uses order.canBeConfirmed() for state check
  - [ ] confirmTime and comments set correctly

---

- [ ] 13. Integration Test - Full Order Flow

  **What to do**:
  - Write integration test: complete order flow
  - Add to cart → Checkout → Submit → Pay → Ship → Confirm
  - Verify state at each step
  - Verify stock changes throughout
  - Test snapshot for full flow

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential (after Wave 3)
  - **Blocked By**: Tasks 1-12

  **Acceptance Criteria**:
  - [ ] Test: testFullOrderFlow passes
  - [ ] All state transitions verified
  - [ ] Stock changes verified at each step

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `feat(entity): add read-only helpers to LitemallOrder` | LitemallOrder.java | compile |
| 2 | `feat(core): extend error codes for order and cart` | AppMallErrors.java | compile |
| 4-7 | `feat(cart): implement Cart operations with Nop DDD` | LitemallCartBizModel.java, tests | mvn test |
| 8-12 | `feat(order): implement Order operations with Nop DDD` | LitemallOrderBizModel.java, tests | mvn test |
| 13 | `test: add integration test for full order flow` | TestFullOrderFlow.java | mvn test |

---

## Nop DDD Compliance Checklist

> **CRITICAL**: These rules MUST be followed. Violation = implementation failure.

### Entity Rules (LitemallOrder, LitemallCart, etc.)
- [ ] **Only read-only helpers** - no `setXxx()` calls in entity methods
- [ ] **Use Constants** (MallOrderStatus.CREATED) not enum
- [ ] **Pure functions** - no side effects, no external calls
- [ ] **State checks return boolean** - `canBeCancelled()`, `canBeShipped()`

### BizModel Rules (LitemallOrderBizModel, LitemallCartBizModel)
- [ ] **Use `this.requireEntity()`** - never `dao().getEntityById()`
- [ ] **Use `doFindList()`** - never `dao().findAllByQuery()`
- [ ] **Use `newEntity()`** - never `new EntityClass()`
- [ ] **Use `updateEntity(entity, context)`** - never `dao().updateEntity()`
- [ ] **@BizMutation only** - no `@Transactional` annotation
- [ ] **Inject interfaces** - `@Inject @Named("biz_EntityName") ICrudBiz<Entity>`

### Child Entity Pattern (OrderGoods)
- [ ] **Create with `newEntity("EntityName")`** - supports Delta customization
- [ ] **Add to parent collection** - `order.getOrderGoods().add(item)`
- [ ] **Don't set collection directly** - ❌ `order.setOrderGoods(list)`

### Transaction Pattern
- [ ] **@BizMutation auto-transactions** - no manual transaction management
- [ ] **NopException auto-rollback** - throw on business rule violation

---

## Success Criteria

### Verification Commands
```bash
# Run all Cart tests
mvn test -pl app-mall-service -Dtest=TestLitemallCartBizModel

# Run all Order tests  
mvn test -pl app-mall-service -Dtest=TestLitemallOrderBizModel

# Run integration test
mvn test -pl app-mall-service -Dtest=TestFullOrderFlow

# Run all tests
mvn test -pl app-mall-service
```

### Final Checklist
- [ ] All "Must Have" present
- [ ] All "Must NOT Have" absent
- [ ] All Nop DDD rules followed
- [ ] Entity has read-only helpers only (canBeXxx, calculateXxx)
- [ ] BizModel uses requireEntity(), newEntity(), updateEntity()
- [ ] No enum used for business fields
- [ ] All tests pass
- [ ] Stock management works correctly
- [ ] Price calculation matches requirements
- [ ] Error codes use NopException pattern
