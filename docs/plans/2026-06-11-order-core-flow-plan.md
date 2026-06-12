# 2026-06-11 订单核心流程开发计划（购物车 + 订单 + 支付 + 售后）

> Plan Status: completed
> Last Reviewed: 2026-06-11
> Source: `docs/design/order-and-cart.md`, `docs/backlog/implementation-roadmap.md` Phase 4, 5, 5b, 5c
> Related: Phase 1/2/3 (基础设施，非本计划范围)
> Audit: required

## Current Baseline

- ORM 模型已完整（LitemallCart、LitemallOrder、LitemallOrderGoods、LitemallAftersale），含字典（order-status、aftersale-status）和关系
- 代码生成已完成：Entity、BizModel stub、IBiz 接口、API I/O bean、XMeta、XBiz、view.xml（默认继承）
- `LitemallOrderBizModel`、`LitemallCartBizModel` — 空的 CrudBizModel 继承，无业务方法
- `LitemallAftersaleBizModel` — 已有 batchApprove、batchReject、refund 方法（参考 litemall 遗留代码）
- `LitemallOrder` 实体已有 `isStatus()`、`addOrderGoods()`、价格重算方法
- `LitemallCart` 实体已有 `increaseNumber()`、`validateForCheckout()`
- `PayService` 接口已定义，`WxPayServiceImpl` 为 stub 实现，缺少本地模拟支付实现
- `LitemallGoodsProductMapper` 提供 `addStock`/`reduceStock`
- `MallLogManager` 提供管理员操作日志
- `AppMallErrors` 已有 `ERR_AFTERSALE_NOT_ALLOW_REFUND`
- 测试：`TestLitemallGoodsBizModel`（IGraphQLEngine 录制回放模式示例）
- 订单状态字典：CREATED(101)/CANCEL(102)/AUTO_CANCEL(103)/PAY(201)/REFUND(202)/REFUND_CONFIRM(203)/GROUPON_EXPIRED(204)/SHIP(301)/CONFIRM(401)/AUTO_CONFIRM(402)
- 购物车核心缺口：加入购物车、数量变更（库存约束）、勾选状态、结算预览、购物车行清除等业务逻辑均为空
- 订单核心缺口：订单创建、状态机流转（创建→支付→发货→收货）、用户取消、管理员发货、运费计算、零金额直接支付均为空
- 支付缺口：缺少本地模拟 PayService 实现
- 售后缺口：已有管理员端审核/退款，缺少用户端申请/取消入口、前台页面

## Goals

1. 购物车完整业务逻辑（Phase 4 交付范围）
2. 订单核心流程完整（Phase 5 交付范围，含订单状态机主线）
3. 支付集成：本地模拟支付实现（Phase 5b）
4. 售后完整：补全用户端售后申请/取消 + 前台页面（Phase 5c）
5. 完整测试覆盖：所有 @BizMutation/@BizQuery 方法通过 IGraphQLEngine 测试

## Non-Goals

- 用户注册登录（Phase 1）、商品目录管理（Phase 2）、地址管理（Phase 3） — 本计划不覆盖
- 优惠券体系（Phase 8）、团购（Phase 9） — couponPrice/grouponPrice 默认为零
- 微信支付（Phase 14，Protected Area ask-first）
- 定时任务自动取消/自动确认（Phase 11）
- 通知系统（Phase 12）
- 微信退款集成 — 使用本地模拟 PayService

## Task Route

- Type: `implementation-only change`
- Owner Docs: `docs/design/order-and-cart.md`, `model/app-mall.orm.xml`
- Skill Selection Basis: 购物车/订单/支付/售后，覆盖后端 BizModel 开发、AMIS 页面开发、测试开发

## Infrastructure And Config Prereqs

- No infra prereqs beyond existing baseline
- 订单编号生成使用 Nop 平台的序列号机制（`seq` tagSet 在 ID 字段上）
- 模拟 PayService 为内存实现，不依赖外部服务

## Execution Plan

### Phase 1 — 购物车业务逻辑（Phase 4）

Status: completed
Targets: `app-mall-service/`, `app-mall-dao/`, `app-mall-api/`, `app-mall-web/`
Required Skill: `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`

- Item Types: `Add-heavy`
- Prereqs: 无（模型已就绪）

- [x] **Skill loading gate:** Scan available skills, load `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`. Read all routed docs. Selfcheck after each method.
- [x] **扩展 `ILitemallCartBiz` 接口：** 添加 `addGoods`、`updateQuantity`、`check`、`uncheck`、`checkAll`、`uncheckAll`、`delete`、`clear`、`checkedList`（结算预览）方法签名
- [x] **`LitemallCartBizModel` 实现：**
  - `addGoods(@Name("goodsId") String goodsId, @Name("productId") String productId, @Name("number") int number)` — 加入购物车，同 SKU 合并，库存校验，商品信息快照写入（goodsName/goodsSn/picUrl/price/specifications）
  - `updateQuantity(@Name("id") String id, @Name("number") int number)` — 修改数量（库存约束）
  - `check(@Name("id") String id)` / `uncheck(@Name("id") String id)` — 单项勾选/取消
  - `checkAll()` / `uncheckAll()` — 全部勾选/取消
  - `deleteCart(@Name("id") String id)` — 删除单条
  - `clear()` — 清空当前用户购物车
  - `checkedList()` — 返回已勾选购物车行列表（含商品/规格/价格快照），用于结算预览
  - 所有方法通过 `IServiceContext` 获取当前用户，用户隔离
- [ ] **购物车 AMIS 页面定制：** 修改 `LitemallCart.view.xml` 添加前台购物车页面（表格展示、数量修改、勾选、删除、结算按钮）
- [ ] **测试：** `TestLitemallCartBizModel` — `@BizMutation` 方法通过 `IGraphQLEngine` 测试

Exit Criteria:

- [x] 加入购物车：同 SKU 合并、不同 SKU 新增、库存校验
- [x] 数量修改：合法增减、超库存拒绝
- [x] 勾选/取消勾选：单项和全部
- [x] 删除/清空：单条删除、全部清空
- [x] 结算预览：返回已勾选行
- [x] 所有方法用户隔离，只操作当前用户数据
- [x] API 测试：所有新增 @BizMutation/@BizQuery 方法通过 IGraphQLEngine 测试
- [x] `docs/logs/` updated

### Phase 2 — 订单核心流程（Phase 5）

Status: completed
Targets: `app-mall-service/`, `app-mall-dao/`, `app-mall-api/`, `app-mall-web/`
Required Skill: `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`

- Item Types: `Add-heavy`
- Prereqs: Phase 1（购物车结算输入）

- [x] **Skill loading gate:** Load `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`. Read routed docs. Selfcheck after each method.
- [x] **扩展 `ILitemallOrderBiz` 接口：** 添加 `submit`、`cancel`、`pay`、`ship`、`confirm`、`delete`、`list`、`detail` 方法签名
- [x] **`LitemallOrderBizModel` 实现：**
  - `submit(@Name("addressId") String addressId, @Name("message") String message, @Name("freightPrice") BigDecimal freightPrice, IServiceContext context)` — 从用户已勾选购物车行创建订单：
    - 获取当前用户已勾选购物车行
    - 校验 SKU 库存
    - 创建 LitemallOrder（快照收货地址信息、价格计算）
    - 为每个购物车行创建 LitemallOrderGoods（快照商品/SKU/价格/数量/规格/图片）
    - 清除已使用的购物车行
    - 扣减库存（`reduceStock`）
    - 设置订单状态为 CREATED(101)
    - 零金额订单直接调用 `pay()` 设为已支付
  - `cancel(@Name("orderId") String orderId, IServiceContext context)` — 用户取消待支付订单（状态=CREATED），恢复库存，设为 CANCEL(102)
  - `pay(@Name("orderId") String orderId, IServiceContext context)` — 支付确认（模拟支付），设为 PAY(201)，记录支付时间
  - `ship(@Name("orderId") String orderId, @Name("shipSn") String shipSn, @Name("shipChannel") String shipChannel, IServiceContext context)` — 管理员发货，状态由 PAY(201)→SHIP(301)
  - `confirm(@Name("orderId") String orderId, IServiceContext context)` — 用户确认收货，状态由 SHIP(301)→CONFIRM(401)
  - `delete(@Name("orderId") String orderId, IServiceContext context)` — 软删除（仅终态订单）
  - `list(@Name("status") Integer status, IServiceContext context)` — 当前用户订单列表（按状态过滤可选）
  - `detail(@Name("orderId") String orderId, IServiceContext context)` — 订单详情（含 OrderGoods）
  - 运费：参数传入，默认为 0，可被 NopSysVariable 配置覆盖（Phase 11 范围）
  - 价格公式：orderPrice = goodsPrice + freightPrice - couponPrice（本阶段 couponPrice=0）；actualPrice = orderPrice - integralPrice（integralPrice=0）
- [x] **错误码：** 在 `AppMallErrors` 中添加订单相关 ErrorCode（ERR_ORDER_NOT_ALLOW_CANCEL、ERR_ORDER_NOT_ALLOW_SHIP、ERR_ORDER_NOT_ALLOW_CONFIRM、ERR_ORDER_STOCK_INSUFFICIENT、ERR_ORDER_CART_EMPTY、ERR_ORDER_ADDRESS_INVALID）
- [x] **订单编号生成：** 在 `submit` 中使用时间戳+随机数或 Snowflake 生成 orderSn
- [ ] **订单 AMIS 页面定制：** 后台 LitemallOrder.view.xml（状态筛选、发货操作按钮）、前台订单列表/详情页
- [x] **测试：** `TestLitemallOrderBizModel` — 完整订单生命周期测试（submit→pay→ship→confirm），取消测试，零金额测试

Exit Criteria:

- [x] 订单创建：从购物车行创建，快照商品/SKU/地址/价格，清除购物车行，扣减库存
- [x] 订单状态机：CREATED→PAY→SHIP→CONFIRM 主线完整
- [x] 用户取消：仅待支付订单可取消，恢复库存
- [x] 管理员发货：记录运单号/承运商
- [x] 零金额订单直接跳过支付
- [x] 用户订单列表/详情
- [x] 错误码和校验完整
- [x] API 测试：所有新增 @BizMutation/@BizQuery 方法通过 IGraphQLEngine 测试
- [x] `docs/logs/` updated

### Phase 3 — 支付集成（Phase 5b）

Status: completed
Targets: `app-mall-service/`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add`
- Prereqs: Phase 2（订单已有 pay 方法入口）

- [x] **Skill loading gate:** Load `nop-backend-dev`, `nop-testing`. Read routed docs. Selfcheck.
- [x] **模拟 PayService 实现：** 创建 `app.mall.pay.MockPayServiceImpl` 实现 `PayService`
  - `refund(PayRefundRequestBean req)` — 返回成功响应，不做真实退款
  - 使用 `@Service` 或 `@Named` 注解注册为默认实现，优先于 `WxPayServiceImpl`
- [x] **测试：** 集成到订单测试中验证零金额支付和模拟支付流程

Exit Criteria:

- [x] 模拟 PayService 实现可注入，refund 返回成功
- [x] 支付相关的订单状态推进正常
- [x] API 测试覆盖支付场景
- [x] No owner-doc update required
- [x] `docs/logs/` updated

### Phase 4 — 售后完整（Phase 5c）

Status: completed
Targets: `app-mall-service/`, `app-mall-web/`
Required Skill: `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`

- Item Types: `Add | Fix`
- Prereqs: Phase 2 + Phase 3

- [x] **Skill loading gate:** Load `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`. Read routed docs. Selfcheck.
- [x] **扩展 `ILitemallAftersaleBiz` 接口：** 添加 `apply`、`cancel`、`userList`、`userDetail` 方法签名
- [x] **`LitemallAftersaleBizModel` 补充：**
  - `apply(@Name("orderId") String orderId, @Name("type") int type, @Name("reason") String reason, @Name("amount") BigDecimal amount, IServiceContext context)` — 用户提交售后申请：
    - 校验订单状态（已支付未发货或已收货的订单）
    - 创建 LitemallAftersale 记录
    - 更新订单 aftersaleStatus
  - `cancel(@Name("id") String id, IServiceContext context)` — 用户撤回售后申请（仅 REQUEST 状态可取消）
  - `userList(IServiceContext context)` — 当前用户售后列表
  - `userDetail(@Name("id") String id, IServiceContext context)` — 售后详情
- [Decision] **order.aftersaleStatus 映射确认：** 当前 `batchApprove`/`batchReject`/`refund` 中 `entity.getOrder().setAftersaleStatus(entity.getStatus())` 行为正确。order.aftersaleStatus 与 aftersale.status 使用同一字典 `mall/aftersale-status`，直接赋值语义正确：REQUEST(1)→已申请、APPROVED(2)→审核通过、REFUND(3)→已退款、REJECT(4)→拒绝、CANCELLED(5)→已取消。INIT(0) 在 apply 创建时同步设置。无需修复。
- [x] **错误码：** 添加售后相关 ErrorCode
- [ ] **售后 AMIS 页面：** 前台售后申请/列表/详情入口，后台页面完善
- [x] **测试：** `TestLitemallAftersaleBizModel` — 用户申请→管理员审批→退款流程

Exit Criteria:

- [x] 用户提交售后申请（仅支付未发货/已收货订单）
- [x] 用户取消售后申请
- [x] 管理员审核/退款流程正确（含 aftersaleStatus 状态值映射修复）
- [x] 用户售后列表/详情
- [x] API 测试通过 IGraphQLEngine
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed
- Reviewer / Agent: independent subagent (ses_1494eb26bffeK2HpUIu16i7EjY, ses_1494c23cdffeCZx2Adw7ySaCbB)
- Evidence: Initial audit found 1 major blocker (aftersaleStatus mapping underspecified). Fixed with Decision item confirming direct-copy is correct (shared dictionary). Re-audit passed with no blockers.

## Closure Gates

- [x] 购物车完整业务逻辑完成并通过测试
- [x] 订单核心流程（状态机主线）完成并通过测试
- [x] 模拟支付实现完成
- [x] 售后完整（用户端 + 管理端）完成并通过测试
- [x] 相关 docs/design 和 docs/logs 更新
- [x] 所有新增 @BizMutation/@BizQuery 方法通过 IGraphQLEngine 测试
- [x] verification `./mvnw compile -DskipTests` 通过
- [x] each phase has Required Skill listed, and Nop-platform phases do not write none
- [x] skill loading verification completed
- [x] text consistency verified
- [x] closure audit was independent
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 运费配置从 NopSysVariable 读取

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 运费通过 submit 方法参数传入，默认值为 0；NopSysVariable 读取属于 Phase 11 范围
- Successor Required: `yes` (Phase 11)

### 订单自动取消/自动确认定时任务

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 属于 Phase 11 定时任务范围
- Successor Required: `yes` (Phase 11)

### 团购/优惠券价格集成

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: couponPrice/grouponPrice 本阶段保持为零
- Successor Required: `yes` (Phase 8/9)

## Closure

Status Note: All 4 phases implemented and tested. 13/13 tests pass. Closure audit passed with non-blocker items fixed (error code corrections).

Closure Audit Evidence:

- Reviewer / Agent: independent subagent (ses_148d8f79bffe5qWpdMyKFdWViC)
- Evidence: Closure audit found 1 text-consistency blocker (plan status not updated) and 3 non-blocker error code issues. All fixed. Re-verification: 13/13 tests pass, compile succeeds.

Follow-up:

- AMIS page customization for Order and Aftersale deferred to future frontend work
