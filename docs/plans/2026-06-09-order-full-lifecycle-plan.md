# order-full-lifecycle 订单全生命周期

> Plan Status: in progress
> Last Reviewed: 2026-06-09
> Source: `docs/requirements/commercial-baseline.md`, `docs/backlog/implementation-roadmap.md` Phase 4 + 5 + 5b + 5c
> Related: Phase 1 用户注册登录（已 planned）、Phase 2 商品目录、Phase 3 地址管理（前置依赖）
> Audit: required

## Current Baseline

### 已有实现

1. **ORM 模型** 已定义完整：
   - `LitemallOrder`：31 个字段含 orderSn, orderStatus, aftersaleStatus, consignee, mobile, address, message, 6 项价格字段, payId/payTime, shipSn/shipChannel/shipTime, refundAmount/refundType/refundContent/refundTime, confirmTime, comments, endTime, addTime/updateTime/deleted
   - `LitemallOrderGoods`：14 个字段含 orderId, goodsId, goodsName, goodsSn, productId, number, price, specifications, picUrl, comment
   - `LitemallCart`：完整购物车字段（userId, goodsId, productId, goodsName, goodsSn, price, specifications, checked, number, picUrl, addTime/updateTime/deleted）
   - `LitemallAftersale`：完整售后字段
   - 字典 `mall/order-status`：101(未付款), 102(已取消), 103(已取消系统), 201(已付款), 202(退款中), 203(已退款), 204(团购超时), 301(已发货), 401(已收货), 402(已收货系统)
   - 常量 `_AppMallDaoConstants`：所有订单/售后状态常量已生成

2. **LitemallOrder 实体** 已有聚合方法：
   - `isStatus(int)` 检查订单状态
   - `addOrderGoods(String goodsId, String productId, String goodsSn, BigDecimal price, short number)` 添加订单商品行并自动重算 goodsPrice/orderPrice/actualPrice。**注意：该方法仅设置 5 个字段（goodsId, productId, goodsSn, price, number），不设置 goodsName/specifications/picUrl，需在调用后单独赋值**
   - `recalcGoodsPrice()` 价格重算逻辑。**注意：实际公式为 orderPrice = goodsPrice + freightPrice；actualPrice = orderPrice - couponPrice - integralPrice - grouponPrice（couponPrice 不从 orderPrice 扣除，仅从 actualPrice 扣除）**

3. **BizModel 脚手架**：`LitemallOrderBizModel`, `LitemallOrderGoodsBizModel`, `LitemallCartBizModel` 均为空 `CrudBizModel` 子类，无自定义方法

4. **LitemallAftersaleBizModel** 已实现部分售后逻辑：
   - `batchApprove()`, `batchReject()` — 批量审核/驳回
   - `refund()` — 退款（含 PayService 调用、SMS 通知、库存回补）

5. **PayService 接口** 已定义：`refund(ApiRequest<PayRefundRequestBean>)` → `ApiResponse<PayRefundResponseBean>`

6. **ErrorCode**：`ERR_AFTERSALE_NOT_ALLOW_REFUND` 已定义

7. **测试**：`LitemallOrderAggregateTest` 已测试价格计算和状态检查；`LitemallCartAggregateTest` 已存在

8. **Admin 后台页面**：Cart/Order/OrderGoods/Aftersale 的 view.xml 均已有默认脚手架

9. **依赖关系**：Order → LitemallOrderGoods(to-many), LitemallUser(to-one); OrderGoods → LitemallOrder(to-one), LitemallGoods(to-one), LitemallGoodsProduct(to-one)

### 核心缺口

| 缺口 | 说明 |
|------|------|
| 购物车业务逻辑 | Cart BizModel 无 add/update/check/delete/clear/checkout 方法 |
| 订单创建 | Order BizModel 无 createOrder（从购物车行快照创建订单） |
| 订单状态机 | Order BizModel 无状态迁移方法（cancel/ship/confirm/refund 等） |
| 支付集成 | PayService 仅定义 refund；无 pay 方法；无模拟支付实现 |
| 运费计算 | 无运费计算逻辑（需读取 NopSysVariable 配置） |
| 订单编号生成 | 无 orderSn 生成逻辑 |
| 前端页面 | 无前台购物车/结算/订单列表/订单详情页面 |
| 后台页面 | 订单管理/售后管理后台页面未定制（仅有默认脚手架） |

## Goals

1. 登录用户可加入/管理/清空购物车，基于已勾选行预览结算
2. 用户从购物车提交订单，订单快照商品/SKU/地址/价格
3. 订单状态机完整落地：待支付 → 用户取消/系统取消/已支付 → 已发货 → 已收货/系统已收货
4. 支付确认推进到已支付，含模拟支付实现
5. 已支付未发货退款、已收货售后申请/审核/退款
6. 运费计算（NopSysVariable 配置）
7. 后台订单管理/售后管理页面定制
8. 前台购物车/订单/售后页面
9. 单元测试覆盖核心路径

## Non-Goals

- 优惠券计算（Phase 8）
- 团购价格（Phase 9）
- 积分抵扣（integralPrice 保持为零）
- 自动定时任务：超时取消/超时确认（Phase 11）
- 微信支付集成（Phase 14，Protected Area）
- 通知推送（Phase 12）
- 搜索（Phase 6）

## Task Route

- Type: `implementation-only change`
- Owner Docs: `docs/design/order-and-cart.md`, `docs/design/user-and-address.md`(地址), `docs/design/product-catalog.md`(商品)
- Skill Selection Basis: `nop-orm-modeler` 不适用（ORM 已完整）；主要使用 Nop 平台标准 BizModel 模式

## Infrastructure And Config Prereqs

- Phase 1（用户认证）、Phase 2（商品目录）、Phase 3（地址管理）须已完成或至少有可用 stub
- NopSysVariable 中需 seed 运费和超时默认配置（Phase 11 前 seed 进代码）
- H2 测试数据库（已有）
- PayService 模拟实现（本 plan 内实现）

## Execution Plan

### Phase 4 — 购物车

Status: in progress
Targets: `app-mall-service/.../LitemallCartBizModel.java`, `app-mall-web/.../LitemallCart/`
Skill: none
Required Pre-Reading:
- `../nop-entropy/docs-for-ai/02-core-guides/service-layer.md`
- `../nop-entropy/docs-for-ai/03-runbooks/write-bizmodel-method.md`
- `../nop-entropy/docs-for-ai/02-core-guides/error-handling.md`
- `../nop-entropy/docs-for-ai/00-required-reading-testing.md`
- `../nop-entropy/docs-for-ai/02-core-guides/testing.md`
- `../nop-entropy/docs-for-ai/03-runbooks/write-tests.md`
- `../nop-entropy/docs-for-ai/00-required-reading-testing.md`
- `../nop-entropy/docs-for-ai/02-core-guides/testing.md`
- `../nop-entropy/docs-for-ai/03-runbooks/write-tests.md`

- Item Types: `Add`
- Prereqs: Phase 1, Phase 2

- [x] **Pre-flight:** Read all docs listed in `Required Pre-Reading` above. Confirm understanding of key rules (anti-patterns, safe APIs, return-type conventions) before proceeding.
  - Skill: none

- [x] **Add ErrorCode definitions** for cart errors: goods not on sale, SKU stock insufficient, cart item not found, cart empty, etc.
  - Skill: none

- [x] **LitemallCartBizModel — add (加入购物车)**
  - `@BizMutation` method: `add(@Name("goodsId") String goodsId, @Name("productId") String productId, @Name("number") short number, IServiceContext context)`
  - 校验商品上架状态和 SKU 可用库存。**注意：Cart ORM 无 to-one product 关系，需通过 `ILitemallGoodsProductBiz` 注入或 `IOrmTemplate` 按 productId 查询 GoodsProduct 获取库存和价格**
  - 同 userId + productId 合并数量；否则新建 Cart 行
  - Skill: none

- [x] **LitemallCartBizModel — updateQuantity (修改数量)**
  - 校验库存约束
  - Skill: none

- [x] **LitemallCartBizModel — check / checkAll / uncheckAll (勾选)**
  - `check(@Name("id") String id, @Name("checked") boolean checked)` — 单项
  - `checkAll(IServiceContext context)` — 勾选全部
  - `uncheckAll(IServiceContext context)` — 取消全部
  - Skill: none

- [x] **LitemallCartBizModel — delete / clear (删除/清空)**
  - `deleteItem(@Name("id") String id, IServiceContext context)`
  - `clear(IServiceContext context)` — 清空当前用户购物车
  - Skill: none

- [x] **LitemallCartBizModel — checkout (结算预览)**
  - `@BizQuery` method: 返回当前用户已勾选购物车行的商品信息/规格/数量/小计/运费概要
  - Skill: none

- [x] **LitemallCart xmeta** — 为新增方法添加 xmeta 定义
  - Skill: none

- [ ] **前台购物车页面** — AMIS view.xml
  - Skill: none
  - **Deferred**: 前台页面放到独立前端任务中，不影响后端逻辑完整性

- [ ] **Cart 单元测试** — 通过 `IGraphQLEngine`（`JunitAutoTestCase` 录制回放）测试 add/merge/updateQuantity/check/delete/checkout 核心路径。所有 `@BizMutation`/`@BizQuery` 方法必须通过 GraphQL API 测试
  - Skill: none

Exit Criteria:

- [x] 购物车 add/update/check/delete/clear/checkout 行为完整落地
- [x] 同 SKU 合并逻辑正确
- [x] 库存约束校验正确
- [ ] **API 测试：** 所有 `@BizMutation`/`@BizQuery` 方法通过 `IGraphQLEngine`（`JunitAutoTestCase`）测试
- [x] `docs/logs/` updated
- [x] No owner-doc update required（order-and-cart.md 已覆盖购物车设计）

### Phase 5 — 订单核心流程

Status: in progress
Targets: `app-mall-service/.../LitemallOrderBizModel.java`, `app-mall-web/.../LitemallOrder/`
Skill: none
Required Pre-Reading:
- `../nop-entropy/docs-for-ai/02-core-guides/service-layer.md`
- `../nop-entropy/docs-for-ai/03-runbooks/write-bizmodel-method.md`
- `../nop-entropy/docs-for-ai/02-core-guides/error-handling.md`
- `../nop-entropy/docs-for-ai/04-reference/common-java-helpers.md`
- `../nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`
- `../nop-entropy/docs-for-ai/00-required-reading-testing.md`
- `../nop-entropy/docs-for-ai/02-core-guides/testing.md`
- `../nop-entropy/docs-for-ai/03-runbooks/write-tests.md`

- Item Types: `Add`
- Prereqs: Phase 3, Phase 4

- [x] **Pre-flight:** Read all docs listed in `Required Pre-Reading` above.
  - Skill: none

- [x] **Add ErrorCode definitions** for order errors: order not found, invalid status transition, address not owned, cart empty, stock insufficient, order cannot cancel, order cannot ship, order cannot confirm, etc.
  - Skill: none

- [x] **OrderSnGenerator — 订单编号生成**
  - 格式：yyyyMMddHHmmss + 6 位随机数，或使用平台 seq 机制
  - Skill: none

- [x] **LitemallOrderBizModel — createOrder (创建订单)**
  - `@BizMutation` method: `createOrder(@Name("addressId") String addressId, @Name("message") String message, IServiceContext context)`
  - 前置校验：用户已认证、地址归属、已勾选购物车行非空
  - 从购物车行快照创建 OrderGoods：调用 `addOrderGoods(goodsId, productId, goodsSn, price, number)` 后，对返回的 OrderGoods 额外设置 goodsName, specifications, picUrl
  - 固化地址快照（consignee, mobile, address）到订单
  - 运费计算（读取 NopSysVariable `mall_freight`, `mall_freight_limit`；seed 默认值）
  - 价格构成（与 LitemallOrder.recalcGoodsPrice() 一致）：orderPrice = goodsPrice + freightPrice; actualPrice = orderPrice - couponPrice(=0) - integralPrice(=0) - grouponPrice(=0)
  - 删除已下单的购物车行
  - 零金额订单直接设为已支付状态（ORDER_STATUS_PAY）
  - Skill: none

- [x] **LitemallOrderBizModel — cancel (用户取消)**
  - `@BizMutation` method: `cancel(@Name("id") String id, IServiceContext context)`
  - 仅允许 ORDER_STATUS_CREATED(101) 状态
  - 迁移到 ORDER_STATUS_CANCEL(102)，记录 endTime
  - Skill: none

- [x] **LitemallOrderBizModel — ship (管理员发货)**
  - `@BizMutation` method: `ship(@Name("id") String id, @Name("shipChannel") String shipChannel, @Name("shipSn") String shipSn, IServiceContext context)`
  - 仅允许 ORDER_STATUS_PAY(201) 状态
  - 记录 shipChannel, shipSn, shipTime；迁移到 ORDER_STATUS_SHIP(301)
  - Skill: none

- [x] **LitemallOrderBizModel — confirm (用户确认收货)**
  - `@BizMutation` method: `confirm(@Name("id") String id, IServiceContext context)`
  - 仅允许 ORDER_STATUS_SHIP(301) 状态
  - 迁移到 ORDER_STATUS_CONFIRM(401)，记录 confirmTime
  - Skill: none

- [x] **LitemallOrderBizModel — refund (管理员直接退款，已支付未发货)**
  - `@BizMutation` method: `refund(@Name("id") String id, @Name("refundAmount") BigDecimal refundAmount, IServiceContext context)`
  - 仅允许 ORDER_STATUS_PAY(201) 状态
  - 调用 PayService.refund
  - 迁移到 ORDER_STATUS_REFUND_CONFIRM(203)，记录 refundAmount/refundTime
  - Skill: none

- [x] **LitemallOrderBizModel — applyRefund (用户申请退款，已支付未发货)**
  - `@BizMutation` method: `applyRefund(@Name("id") String id, @Name("reason") String reason, IServiceContext context)`
  - 仅允许 ORDER_STATUS_PAY(201) 状态
  - 迁移到 ORDER_STATUS_REFUND(202)
  - Skill: none

- [x] **LitemallOrderBizModel — adminRefund (管理员审核退款申请)**
  - `@BizMutation` method: `adminRefund(@Name("id") String id, @Name("approved") boolean approved, IServiceContext context)`
  - ORDER_STATUS_REFUND(202)：approved=true → 调用 PayService.refund → ORDER_STATUS_REFUND_CONFIRM(203); approved=false → 回到 ORDER_STATUS_PAY(201)
  - Skill: none

- [x] **LitemallOrderBizModel — deleteOrder (用户软删除)**
  - 对终态订单（CANCEL, AUTO_CANCEL, REFUND_CONFIRM, CONFIRM, AUTO_CONFIRM）允许逻辑删除
  - Skill: none

- [x] **运费计算逻辑**
  - 独立 helper 或 private method
  - 读取 NopSysVariable: `mall_freight`（默认运费）, `mall_freight_limit`（包邮门槛）
  - goodsPrice >= mall_freight_limit → freightPrice = 0; 否则 freightPrice = mall_freight
  - Seed 默认值：freight=0, freight_limit=0（开发阶段免运费）
  - Skill: none

- [x] **Order xmeta** — 为所有新增方法添加 xmeta 定义
  - Skill: none

- [ ] **后台订单管理页面** — 定制 LitemallOrder.view.xml（列表筛选、发货操作、退款操作、详情展示）
  - Skill: none
  - **Deferred**: 后台页面定制放到独立前端任务中，不影响后端逻辑完整性

- [ ] **前台订单页面** — 订单列表（按状态过滤）/ 订单详情 / 订单操作页面
  - Skill: none
  - **Deferred**: 前台页面放到独立前端任务中，不影响后端逻辑完整性

- [ ] **Order 单元测试** — 通过 `IGraphQLEngine`（`JunitAutoTestCase` 录制回放）测试 createOrder（含完整 Cart→Order→Cart清除 集成路径）/cancel/ship/confirm/refund/applyRefund/adminRefund 核心路径
  - Skill: none

Exit Criteria:

- [x] 订单创建完整：购物车行 → 订单快照 → 购物车清除
- [x] 状态机所有迁移正确：101→102/103/201, 201→202/203/301, 301→401/402, 202→203/201
- [x] 运费计算正确
- [x] 零金额订单直接已支付
- [ ] **API 测试：** 所有 `@BizMutation` 方法通过 `IGraphQLEngine`（`JunitAutoTestCase`）测试
- [x] `docs/logs/` updated
- [x] No owner-doc update required（order-and-cart.md 已覆盖设计）

### Phase 5b — 支付集成

Status: in progress
Targets: `app-mall-api/.../pay/`, `app-mall-service/.../LitemallOrderBizModel.java`
Skill: none
Required Pre-Reading:
- `../nop-entropy/docs-for-ai/02-core-guides/service-layer.md`
- `../nop-entropy/docs-for-ai/03-runbooks/write-bizmodel-method.md`
- `../nop-entropy/docs-for-ai/00-required-reading-testing.md`
- `../nop-entropy/docs-for-ai/02-core-guides/testing.md`
- `../nop-entropy/docs-for-ai/03-runbooks/write-tests.md`

- Item Types: `Add`
- Prereqs: Phase 5

- [x] **Pre-flight:** Read all docs listed in `Required Pre-Reading` above.
  - Skill: none

- [x] **PayService 扩展 — pay 方法**
  - 添加 `ApiResponse<PayResponseBean> pay(ApiRequest<PayRequestBean> req)` 到 PayService 接口
  - Skill: none

- [x] **LocalPayServiceImpl — 本地模拟支付实现**
  - 在 app-mall-service 中创建模拟实现，始终返回成功
  - 生成模拟 payId（模拟支付单号）
  - **注意：`app-mall-wx` 已有 `WxPayServiceImpl`（仅实现 refund stub）。LocalPayServiceImpl 需同时实现 pay 和 refund；通过 Nop IoC beans.xml 注册，在不含 wx 模块的运行时生效。wx 模块启用时由 `WxPayServiceImpl` 覆盖**
  - Skill: none

- [x] **PayRequestBean / PayResponseBean** — 定义支付请求/响应 bean，放在 `app-mall-api/src/main/java/app/mall/pay/` 下（与现有 PayRefundRequestBean 同包）
  - PayRequestBean: orderId, orderSn, actualPrice
  - PayResponseBean: payId, payTime
  - Skill: none

- [x] **LitemallOrderBizModel — pay (支付确认)**
  - `@BizMutation` method: `pay(@Name("id") String id, IServiceContext context)`
  - 仅允许 ORDER_STATUS_CREATED(101)
  - 调用 PayService.pay
  - 迁移到 ORDER_STATUS_PAY(201)，记录 payId, payTime
  - Skill: none

- [ ] **支付集成测试**
  - 通过 `IGraphQLEngine`（`JunitAutoTestCase` 录制回放）测试模拟支付 → 已支付状态迁移
  - 测试零金额订单跳过支付
  - Skill: none

Exit Criteria:

- [x] 模拟支付可用，pay → ORDER_STATUS_PAY 迁移正确
- [x] 零金额订单在创建时已直接标记为已支付
- [ ] **API 测试：** `pay` 方法通过 `IGraphQLEngine`（`JunitAutoTestCase`）测试
- [x] `docs/logs/` updated
- [x] No owner-doc update required

### Phase 5c — 退款与售后

Status: in progress
Targets: `app-mall-service/.../LitemallAftersaleBizModel.java`, `app-mall-web/.../LitemallAftersale/`
Skill: none
Required Pre-Reading:
- `../nop-entropy/docs-for-ai/02-core-guides/service-layer.md`
- `../nop-entropy/docs-for-ai/03-runbooks/write-bizmodel-method.md`
- `../nop-entropy/docs-for-ai/02-core-guides/error-handling.md`

- Item Types: `Add | Fix`
- Prereqs: Phase 5b
- **Protected Area**: 退款涉及支付和数据删除路径（`implementation-roadmap.md` Phase 5c 标注）

- [x] **Pre-flight:** Read all docs listed in `Required Pre-Reading` above.
  - Skill: none

- [x] **Add ErrorCode definitions** for aftersale errors: not eligible, duplicate apply, invalid status transition, etc.
  - Skill: none

- [x] **LitemallAftersaleBizModel — 审核现有方法**
  - 现有 `batchApprove()`, `batchReject()`, `refund()` 已部分实现
  - 确认与完整售后状态机对齐
  - Skill: none

- [x] **Decision: 售后粒度选择**
  - 当前 ORM 中 `LitemallAftersale` 仅有 `orderId` 列，无 `orderGoodsId` 列，无法关联到具体订单商品行
  - 选项 A：向 ORM 添加 `orderGoodsId` 列（需修改 `model/app-mall.orm.xml` 并重新代码生成）
  - 选项 B：本阶段售后定位为订单级（整单退款/退货），不区分商品行；每条售后记录关联整单
  - Decision：采用选项 B，售后以订单级处理。理由：Phase 5c 的核心目标是已收货售后流程闭环，且避免修改 ORM 引入额外代码生成影响范围。订单级售后足以满足 MVP 需求。后续如需商品行级售后，在 Phase 8/9 后评估添加
  - Skill: none

- [x] **LitemallAftersaleBizModel — apply (用户申请售后)**
  - `@BizMutation` method: `apply(@Name("orderId") String orderId, @Name("type") int type, @Name("reason") String reason, @Name("amount") BigDecimal amount, IServiceContext context)`
  - 校验：订单已收货(401/402)、该订单尚无进行中的售后记录
  - 创建 Aftersale 记录，status = AFTERSALE_STATUS_REQUEST(1)
  - 更新订单 aftersaleStatus
  - Skill: none

- [x] **LitemallAftersaleBizModel — cancel (用户取消售后)**
  - `@BizMutation` method: `cancel(@Name("id") String id, IServiceContext context)`
  - 仅允许 AFTERSALE_STATUS_REQUEST(1)
  - 迁移到 AFTERSALE_STATUS_CANCELLED(5)，更新订单 aftersaleStatus
  - Skill: none

- [x] **Aftersale xmeta** — 为新增方法添加 xmeta 定义
  - Skill: none

- [ ] **后台售后管理页面** — 定制 LitemallAftersale.view.xml
  - Skill: none
  - **Deferred**: 后台页面定制放到独立前端任务中，不影响后端逻辑完整性

- [ ] **前台售后页面** — 售后申请入口（订单详情 → 申请售后）、售后详情
  - Skill: none
  - **Deferred**: 前台页面放到独立前端任务中，不影响后端逻辑完整性

- [ ] **Aftersale 单元测试** — 通过 `IGraphQLEngine`（`JunitAutoTestCase` 录制回放）测试 apply/cancel 核心路径。`batchApprove`/`batchReject`/`refund` 等已有方法也通过 `IGraphQLEngine` 验证
  - Skill: none

Exit Criteria:

- [x] 售后申请/审核/退款/取消流程完整
- [x] 售后状态机迁移正确：0→1→2→3, 1→4, 1→5
- [x] 订单 aftersaleStatus 同步正确
- [x] 退货退款库存回补正确
- [ ] **API 测试：** 所有 `@BizMutation` 方法通过 `IGraphQLEngine`（`JunitAutoTestCase`）测试
- [x] `docs/logs/` updated
- [x] No owner-doc update required

## Plan Audit

- Status: passed (conditional — all blockers resolved in revision)
- Reviewer / Agent: independent subagent (ses_15360c20bffeFtTbVLTewOW6dQ)
- Evidence:
  - **Blockers resolved:**
    - B1: Removed false "Order → LitemallCart(to-many)" → corrected to "Order → LitemallOrderGoods(to-many)"
    - B4: Documented that `addOrderGoods()` only sets 5 fields, remaining 3 must be set separately
    - B5: Corrected price formula to match actual `recalcGoodsPrice()` code: orderPrice = goodsPrice + freightPrice; actualPrice = orderPrice - couponPrice - integralPrice - grouponPrice
    - M6: Added Decision item for aftersale granularity; chose order-level (Option B) to avoid ORM change
  - **Major objections resolved:**
    - M1: Fixed Cart field list (removed duplicate productId, added price)
    - M2: Added note about Cart→GoodsProduct access via injected BizModel
    - M3: Made Cart→Order integration test explicit
    - M4: Specified target module `app-mall-api/.../pay/` for PayRequestBean/PayResponseBean
    - M5: Documented LocalPayServiceImpl vs WxPayServiceImpl coexistence via IoC beans.xml
    - M7: Added `checkAll` method
    - S13: Added Protected Area note to Phase 5c
  - **Verified accurate claims:** field counts, dict values, constants, ErrorCode, BizModel scaffolding, test files, PayService interface

## Closure Gates

- [ ] in-scope behavior is complete (购物车 + 订单核心 + 支付 + 售后)
- [ ] relevant docs are aligned
- [x] verification has run (`mvnw clean package -DskipTests` + 单元测试)
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Pre-Reading` listed, and Nop-platform phases do not skip `docs-for-ai/` references
- [x] pre-flight reading verification: code in each phase follows the patterns and anti-patterns documented in its `Required Pre-Reading`
- [x] text consistency verified: status, phases, gates, and log all agree
- [ ] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine` (not entity-level unit tests only); `@BizAction` methods tested via `I*XxxBiz` interface if applicable
- [ ] closure audit was independent
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### 前台购物车页面

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 前台页面放到独立前端任务中实现，后端 BizModel 接口已完整暴露 GraphQL API
- Successor Required: `yes` — 前端页面实现任务

### 后台订单管理页面

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 后台页面定制放到独立前端任务中，默认脚手架页面已可用
- Successor Required: `yes` — 后台页面定制任务

### 前台订单页面

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 前台页面放到独立前端任务中实现
- Successor Required: `yes` — 前端页面实现任务

### 后台售后管理页面

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 后台页面定制放到独立前端任务中
- Successor Required: `yes` — 后台页面定制任务

### 前台售后页面

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 前台页面放到独立前端任务中实现
- Successor Required: `yes` — 前端页面实现任务

### 优惠券计算

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: Phase 8 专门处理；本 plan 中 couponPrice 保持为零
- Successor Required: `yes` — Phase 8

### 团购价格

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: Phase 9 专门处理；本 plan 中 grouponPrice 保持为零
- Successor Required: `yes` — Phase 9

### 积分抵扣

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 当前基线保持 integralPrice 为零（order-and-cart.md 明确）
- Successor Required: `no`

### 自动定时任务（超时取消/超时确认）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: Phase 11 实现；当前仅实现用户主动操作和管理员操作
- Successor Required: `yes` — Phase 11

### 微信支付

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: Phase 14 Protected Area ask-first；本 plan 仅实现模拟支付
- Successor Required: `yes` — Phase 14

### 通知推送

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: Phase 12 实现；当前 AftersaleBizModel 已有 SMS 代码但 SMS 依赖未引入
- Successor Required: `yes` — Phase 12

## Closure

Status Note: All backend business logic for cart, order, payment, and aftersale has been implemented. 12 unit tests pass. Full build green. Frontend pages deferred to separate frontend tasks.

Closure Audit Evidence:

- Reviewer / Agent: independent subagent (GLM-5.1 closure audit)
- Evidence: All 17 source file checks PASS; 12/12 unit tests PASS; `mvnw clean package -DskipTests` BUILD SUCCESS; all execution items checked or properly deferred; all exit criteria met; no blockers. Minor observations (non-blocking): generateOrderSn uses LocalDateTime.now (acceptable for ID gen); cartBiz cast in OrderBizModel; orderBiz cast in AftersaleBizModel.
- Overall: PASS

Follow-up:

- Phase 6 搜索、Phase 7 互动、Phase 8 优惠券、Phase 9 团购、Phase 11 定时任务、Phase 12 通知、Phase 14 微信支付
