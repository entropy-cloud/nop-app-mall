# 订单与购物车业务设计

## 目的

说明 `nop-app-mall` 的购物车行为、结算、订单生命周期、价格语义以及与退款相关的订单流程。

## 边界

- 本文档负责订单与购物车的业务语义、用户/管理员动作和状态迁移。
- 持久化字段、字典和精确存储状态值以 `model/app-mall.orm.xml` 为准。
- 技术事务、锁、调度和集成实现属于 `docs/architecture/`。

## 领域概览

订单领域覆盖以下内容：

- 购买前的购物车管理
- 结算预览与订单提交
- 支付与发货推进
- 收货确认与订单后续动作
- 退款与售后流程

核心业务概念：

- Cart 用于在订单提交前保存用户计划购买的选择。
- Order 是用户与商城之间的商业合同。
- Order goods 保存该订单中被购买商品和 SKU 的快照。
- 售后是在履约之后发生的独立退款或退货流程。

## 购物车

### 业务规则

- 用户必须先完成认证，才能维护购物车。
- 同一用户、同一 SKU 应合并为一条购物车记录，而不是重复新增。
- 只有当前可售商品才能加入购物车。
- 数量变更必须满足当前可用库存约束。
- 勾选状态决定哪些购物车行会参与结算。

### 支持行为

- 按 SKU 和数量加入购物车。
- 查看购物车，展示商品信息、已选规格、数量、勾选状态和小计行为。
- 修改数量。
- 勾选或取消勾选单项或全部项目。
- 删除商品或清空购物车。
- 基于已勾选商品预览结算。

## 价格语义

订单价格模型包含以下业务构成：

- goods price：商品金额小计
- freight price：基于当前运费策略计算出的配送费
- coupon price：订单在满足优惠券条件时形成的优惠金额构件
- promotion price：订单在满足满减活动条件时由系统自动判定形成的优惠金额构件（满减专属此价格槽位；限时折扣裁决为商品单价层，不复用此槽位，见 `marketing-and-promotions.md` 限时折扣）
- groupon price：订单在满足团购条件时形成的优惠金额构件
- pin tuan price：拼团优惠金额；订单按拼团活动下单时，按 `(retailPrice − activity.pinTuanPrice) × number` 汇总形成的减免额。作用于 `actualPrice` 减项层（与 `grouponPrice` 同层），单订单与团购互斥（详见 `marketing-and-promotions.md` 拼团）
- integral price：积分抵扣金额；用户在结算页勾选使用 N 积分时，按 `mall_points_to_yuan_ratio`（X 积分=¥1）换算，受 `orderPrice × mall_points_deduct_max_ratio` 抵扣上限约束（详见 `marketing-and-promotions.md` 积分体系）。作用于 `actualPrice` 减项层
- order price：叠加运费与优惠后的支付前金额
- actual price：用户最终需要支付的金额

### 计算公式

订单提交时按以下公式计算各价格构件：

- `goods price` = 所有结算项 SKU 当前零售价 × 数量的总和（使用提交时的实时价格，不沿用购物车快照）。命中限时折扣的 SKU 行单价取 `min(retailPrice, vipPrice, timeDiscountPrice)`（限时折扣作用于商品单价层，见 `marketing-and-promotions.md` 限时折扣）；会员用户（`userLevel >= 1`）的 SKU 若配置了 `vipPrice`，则该行单价取 `min(retailPrice, vipPrice)`（SKU 单价级会员价，见 `user-and-address.md` 会员等级体系）
- `order price` = `goods price` + `freight price` - `coupon price` - `promotion price`
- `actual price` = `order price` - `integral price` - `groupon price` - `pin tuan price`

其中 `coupon price` 受券范围校验（全场/类目/指定商品）约束，`promotion price` 由满减活动自动判定最优档位生成（订单级优惠，与 coupon 同处 orderPrice 减项层），`groupon price` 仅在团购成功后成立，`pin tuan price` 为拼团减免额（`actualPrice` 减项层，与 groupon/integral 同层；拼团与团购单订单互斥，同时传入则拒绝）。这些公式的定义以 `model/app-mall.orm.xml` 字段注释和 `LitemallOrderBizModel.submit()` 实现为准。

### 价格计算顺序约定

- 满减门槛 `meetAmount` 以 `goods price` 为判定基准。若启用会员等级价（P26），会员价作用于 SKU 单价层（降低 `goods price` 汇总），会间接拉低满减门槛命中判定。限时折扣（P23）同样作用于 SKU 单价层（命中折扣的 SKU 行单价取 `min(retail, vip, timeDiscount)`），会进一步间接拉低满减门槛命中判定。
- 因此价格计算顺序为：**先计算限时折扣 + 会员价后的行单价 → 汇总 `goods price` → 再据 `goods price` 判定满减门槛与最优档位 → 再计算 coupon / orderPrice → 再计算 integral price（积分抵扣，actualPrice 减项层）**。该顺序在 `LitemallOrderBizModel.submit()` 实现中保持。
- **积分抵扣（P27）作用层位：** `integral price` 在 `orderPrice` 汇总完成后的 `actualPrice` 减项层，与满减 `promotionPrice`（`orderPrice` 减项层）、团购 `grouponPrice`（`actualPrice` 减项层，与积分同层）层位不同，天然不冲突。计算顺序天然正确。
- **积分抵扣公式：** 用户结算勾选 N 积分 → `integralPrice = N / mall_points_to_yuan_ratio`，上限 `min(N 对应金额, orderPrice × mall_points_deduct_max_ratio)`；同时调用积分账户 spend API 扣减用户积分（`sourceType=order-deduct`）。

### 秒杀独立下单路径（P24，独立分支）

- 秒杀走独立的 `LitemallFlashSaleBizModel.flashSaleBuy()` `@BizMutation` 下单路径，**不进 `submit()` 主流程**，因而不参与上述价格公式中的任何减项计算。
- 秒杀价 `flashPrice` 直接作为 OrderGoods 行单价（成交单价层，等同于 `goodsPrice` 的单行来源），最终 `actualPrice = flashPrice × number + freightPrice`；`couponPrice` / `promotionPrice` / `integralPrice` / `grouponPrice` / `pinTuanPrice` 全部初始化为 0（不挂券、不判满减、不抵扣积分、不接团购/拼团）。
- 该独立路径由 `marketing-and-promotions.md` 秒杀章节定义业务语义（场次状态、库存扣减、限购、不叠加），具体实现（事务边界、原子扣减、OrderGoods 行写入）由本订单域负责。
- 详见 `marketing-and-promotions.md` 秒杀章节「下单路径（Decision A：独立 flashSaleBuy 路径）」。

### 运费规则

- 运费策略统一配置。
- 包邮门槛统一配置。
- 结算时必须明确展示是否收取运费或已免运费。

## 订单状态机

### 业务状态

产品基线采用以下业务状态。持久化状态码字典由 `model/app-mall.orm.xml` 维护。

| 业务状态 | 状态码 | 含义 | 用户动作 | 管理员动作 |
| -------- | ------ | ---- | -------- | ---------- |
| 待支付 | 101 | 订单已创建，等待支付 | 取消、支付 | 无 |
| 用户取消 | 102 | 用户在支付前主动取消订单 | 删除 | 无 |
| 系统取消 | 103 | 支付超时后系统关闭订单 | 删除 | 无 |
| 已支付 | 201 | 支付已确认，等待发货 | 申请退款 | 发货、退款 |
| 退款中 | 202 | 订单取消，退款处理中 | 无 | 确认退款 |
| 已退款 | 203 | 退款已完成 | 删除 | 无 |
| 已超时团购 | 204 | 团购超时未成团 | 删除 | 无 |
| 已发货 | 301 | 商品已发出，等待确认收货 | 确认收货 | 无 |
| 用户已收货 | 401 | 用户主动确认收货 | 删除 | 无 |
| 系统已收货 | 402 | 系统超时自动确认收货 | 删除 | 无 |

### 迁移规则

- 待支付订单可以变为用户取消、系统取消或已支付。
- 已支付订单可以进入已退款或已发货。
- 已发货订单可以进入用户已收货或系统已收货。
- 终态订单支持通过软删除语义从用户可见列表中移除。

**退款与售后路径的设计抉择：** 退款进度统一由独立的售后状态机（`aftersaleStatus`）表达，而不是通过订单主状态机的"退款中"中间态流转。原因：售后是独立的后置服务流程，必须保持独立，不得反向篡改订单主状态机已经表达的履约结果。由此约定两条迁移：

- 未发货（已支付）订单的售后退款成功，订单主状态直接进入已退款，不经过退款中。
- 已收货订单的售后退款保持原收货状态（履约已完成的事实不变），仅售后状态反映退款。

退款中（202）作为独立状态位保留给未来的"订单级退款审批"路径，当前售后路径不使用。团购超时未成团时，已支付订单进入团购超时（204）。用户申请退款、审核通过与驳回的动作语义由售后状态机（REQUEST/APPROVED/REJECT）表达，不属于订单主状态迁移，详见退款与售后章节。

### 状态叙述

```text
待支付
  -> 用户取消
  -> 系统取消
  -> 已支付
       -> 已退款 [售后退款成功，见售后状态机]
       -> 团购超时
       -> 已发货
            -> 用户已收货
            -> 系统已收货
```

### 相关状态扩展

- 启用团购的订单可以引入专门的团购超时业务状态。
- 履约后扩展能力：评价限已收货订单；售后资格更宽（见退款与售后章节），但这些扩展不改变支付/发货/收货这条核心履约主线。

## 订单创建

### 业务前置条件

- 用户必须已认证。
- 结算项必须来自用户当前已勾选的购物车选择，或等价的直接购买路径。
- 收货地址必须归属于当前用户。
- 提交时所选 SKU 数量仍然可用。
- 配送相关定价规则以及任何启用中的优惠机制在提交时仍然有效。

### 业务结果

- 订单会固化所购商品、SKU 选择、价格构成和配送信息快照。
- 成功提交后，对应购物车行离开活跃购物车。
- 零金额订单可直接视为已支付，无需等待外部支付步骤。

## 支付、发货与完成

### 支付

- 业务合同上仍然区分待支付与已支付订单。
- 支付确认会把订单推进到已支付状态。
- 生产环境支付行为属于集成能力；微信支付细节应放在对应的集成架构和代码中。
- 开发或本地测试中的支付替代机制必须明确标注为非生产行为，且不能重定义正式商业支付语义。

### 微信支付流程（Native 扫码）

微信支付（Native 扫码）的集成流程如下：

1. **prepay**（`ILitemallOrderBiz.prepay()`）— 订单发货前，用户触发支付。`prepay` 方法校验订单状态为待支付，调用 `PayService.createPayment()`。
2. **createPayment**（`PayService.createPayment()`）— 构造微信 Native 下单请求，调用微信 Native API 获取 `codeUrl`（支付二维码链接），返回给前端。
3. **前端二维码渲染** — 前台支付页根据 `codeUrl` 渲染二维码供用户扫码（详见下方「前台支付消费者」）。
4. **支付回调** — 微信服务器异步通知 `POST /wxpay/notify`，`WxPayNotifyResource` 验签解析后调用 `IPaymentCallback.onPaymentSuccess(outTradeNo, transactionId)`（`PaymentCallbackImpl` 实现），由 `LitemallOrderBizModel.confirmPaidByNotify` 幂等推进订单到已支付（已支付则跳过；系统上下文执行，回调无用户会话）。仅 `tradeState==SUCCESS` 触发推进。
5. **pay()**（`ILitemallOrderBiz.pay()`）— model-level 确认，与外部支付渠道无关。接受边界：**零金额订单（actualPrice==0）任意模式直接确认**；**示例模式（`wxpay.enabled=false`）任意金额由「模拟支付完成」按钮调用**；**真实模式非零金额一律拒绝（`ERR_ORDER_USE_REAL_PAYMENT`），必须经回调 confirmPaidByNotify 推进**。

### 前台支付消费者

前台支付页（`/storefront-pay`，`mall/pay/pay.page.yaml`）是 Phase 14 微信支付后端在前台的唯一消费者页面，串起 prepay → 二维码 → 轮询 → pay 的完整前台流程：

1. **订单加载与三分支路由** — 进入页面先 `getMyOrder(orderId)` 加载订单，按 `actualPrice` 与 `orderStatus` 路由三个互斥分支：
   - 零金额且待支付（`actualPrice==0 && orderStatus==101`）：不进入 prepay，直接调用 `pay()` 完成零金额确认并跳转订单详情。
   - 非零金额且待支付（`actualPrice>0 && orderStatus==101`）：进入 prepay + 二维码 + 轮询流程。
   - 异常（`orderStatus!=101`）：展示当前状态文案并正向引导跳转订单详情。
2. **prepay 获取 codeUrl** — 非零分支调用 `prepay(orderId)`，仅取返回的 `codeUrl`（订单摘要复用 orderLoad 的实体字段，实付金额直接用 `actualPrice`，含积分/团购抵扣也准确）。
3. **二维码渲染** — 使用 amis 原生 `qr-code` 渲染器渲染 `codeUrl`（无新依赖、无后端端点）。
4. **轮询支付状态** — 轮询 `queryPayment(outTradeNo=orderSn)`，`tradeState==SUCCESS` 时展示支付成功并跳转订单详情（真实模式路径）。
5. **模拟支付入口（开发测试用）** — 示例模式（`wxpay.enabled=false`）下微信扫码不会真实回调，轮询永不 SUCCESS，因此保留「模拟支付完成」按钮调用 `pay()`，成功后跳转订单详情。

**支付入口接线：** 订单提交结果页、订单详情页、订单列表页（全部 tab 与待付款 tab）共 4 处「立即付款」按钮均跳转 `/storefront-pay?orderId=...`，不再直接调用 `pay()` 模拟确认。

**轮询停止机制：** 真实模式由轮询服务的 `stopAutoRefreshWhen`（`tradeState==SUCCESS`）停止；示例模式由模拟支付成功后页面跳转、组件卸载从而停止 interval。prepay 每次页面加载都会重新发起（微信 Native 预支付单 2 小时过期，危害低），页面接受此行为。

退款流程：

6. **refund**（`PayService.refund()`）— 管理员对已支付订单发起退款，调用微信退款 API 进行原路退款。同步返回退款结果。退款异步通知（微信 → 服务器）预留为后续对账功能。

### 发货

- 只有已支付订单才能发货。
- 发货时记录承运信息和运单信息，便于用户跟踪。

### 收货确认

- 只有已发货订单才能确认收货。
- 收货确认标志着核心履约流程完成，也是评价资格的边界；售后资格更宽，见退款与售后章节。
- 当用户未在规定时间内确认时，系统自动确认作为兜底机制存在。

## 退款与售后

### 售后粒度：订单商品项（Order Goods）级

售后以**订单商品项（OrderGoods）为粒度**：一个订单的多个商品项可以各自独立发起售后，每项拥有独立的状态机。售後记录通过 `LitemallAftersale.orderItemId`（`model/app-mall.orm.xml` propId=15）关联到具体的订单商品项。

- **落地方式抉择：** 备选 A（单表 `LitemallAftersale` + 既有 `orderItemId` 列，列已就绪）与备选 B（新建 `aftersale_item` 表）。抉择 **A**——`orderItemId` 列已存在，无需新增表/列，改动最小且向后兼容历史记录。
- **历史兼容：** `orderItemId = null` 视为**整单售后**，沿用既有订单整体级逻辑（退款额以订单 `actualPrice` 为上限、还库遍历全部订单商品、券恢复按整单）。新增的 item 级语义仅在 `orderItemId` 非空时生效。

### 退款范围

- 已支付但未发货订单可经售后路径（未收货退款类型）申请退款。
- 管理员可以对符合条件的已支付未发货订单直接退款。
- 管理员也可以审核用户发起的退款申请，执行通过或驳回。
- 退款通过后，被退款项进入终态的已退款状态。

### 售后范围

- 售后覆盖两类业务场景：未发货已支付订单的未收货退款，以及已收货订单的退款/退货退款。
- 售后类型（未收货退款、已收货无需退货退款、退货退款）以 `model/app-mall.orm.xml` aftersale-type 字典为准。

### 售后类型与订单状态的自动映射

售后类型按订单当前履约状态自动限定可选项（固化映射，申请时校验）：

| 订单状态 | 允许的售后类型 | 说明 |
| -------- | -------------- | ---- |
| 已支付未发货（201） | 仅未收货退款（GOODS_MISS=0） | 货未发出，只能退款 |
| 用户已收货（401）/系统已收货（402） | 已收货无需退货退款（GOODS_NEEDLESS=1）、退货退款（GOODS_REQUIRED=2） | 货已收到，按是否需退货选择 |

类型与状态不匹配的申请将被拒绝。

### 多项售后并发与互斥

- 同一订单商品项（同一 `orderItemId`）**同一时刻只允许一个进行中（INIT 之外的未终态）售后**；当该售后进入终态（REFUND/REJECT/CANCELLED）后，可对该项再次发起新的售后（支持部分退款后的二次售后）。
- 同一订单的**不同商品项各自拥有独立状态机**，互不阻塞。
- **退款额递减：** 对同一商品项的二次售后，其退款额上限按"该项行金额 − 已退款额"递减计算，避免超额退款。

抉择理由：备选 A（单项单进行中）在用户灵活性与运营简洁性之间取得平衡，符合主流电商习惯。残留风险：部分退款后二次售后的可退额度需运营人工核对。

### 售后状态与流转

售后是独立于订单主状态机的后置服务流程，依附于原订单和订单商品语义。每条 `LitemallAftersale` 记录独立持有此状态机。

| 售后状态 | 含义 | 用户动作 | 管理员动作 |
| -------- | ---- | -------- | ---------- |
| 可申请 | 该项已满足售后资格，当前尚未提交售后申请 | 发起售后 | 无 |
| 用户已申请 | 用户已提交售后申请，等待处理 | 取消申请 | 审核通过、审核拒绝 |
| 管理员审核通过 | 管理员已同意售后申请，等待执行退款或后续处理 | 无 | 执行退款 |
| 管理员退款成功 | 售后退款已完成 | 查看结果 | 无 |
| 管理员审核拒绝 | 售后申请被拒绝 | 查看结果 | 无 |
| 用户已取消 | 用户主动撤回售后申请 | 无 | 无 |

### item 级售后状态存放（派生计算）

- **抉择：派生计算（备选 A）。** 单项售后状态**派生自**该 `orderItemId` 对应的 `LitemallAftersale.status`（按 orderItemId 过滤聚合当前活跃售后），不新增持久化字段。
- **订正：** 计划审计曾记录"`LitemallOrderGoods.aftersaleStatus` 字段已存在"，经实读 live repo 核验**有误**——该 `aftersaleStatus` 列实际位于 `LitemallOrder`（`app-mall.orm.xml` 行 1061），`LitemallOrderGoods` 并无此列。故采用派生方案，避免为 `LitemallOrderGoods` 新增列（受保护模型改动最小化）。
- **订单级 `order.aftersaleStatus` 语义转换：** 原作为"整单互斥锁"使用（申请时一旦非 INIT 即拒绝）。升级为**聚合视图**：当订单任一商品项存在进行中售后时，订单级字段为 REQUEST；否则为 INIT。该字段不再用于阻塞单项售后申请（单项互斥改由 item 级 active-aftersale 查询判定）。

### item 级退款额上限

- **抉择：单项退款额上限 = 该 OrderGoods 行金额 `number × price`。**
- **理由：** P15（满减）已落地的价格公式中，`promotion price` 与 `coupon price` 均为**订单级**优惠（未分摊到行），`LitemallOrderGoods.actualPayAmount` 列虽存在但提交时未写入。故以行自身对 `goods price` 的贡献额 `number × price` 作为单项安全上限，无需改动提交逻辑或回填历史。
- **全局上限：** 订单级 `actualPrice` 仍是整单退款的全局上限，由退款执行 `refund()` 复核。满减/券场景下各行上限之和（goods price）可能大于 actualPrice，但单项上限更紧，全局上限兜底。
- 残留风险：满减/券折扣未分摊到行，故部分退款时单项可退额可能略高于其"理论实付分摊额"，由运营审核环节人工把关。

### 部分退款对订单级的副作用

item 级退款时三个订单级副作用策略：

1. **订单状态迁移：** 未发货（201）订单**仅当全部商品项均已退款**时才整体进入已退款（203）；任一商品项部分退款时，订单主状态保持已支付（201）。
2. **还库：** 仅对**被退款的那个 OrderGoods 行**（`orderItemId`）调用还库，不再遍历全部订单商品。`orderItemId=null`（整单）时仍遍历全部订单商品（历史兼容）。
3. **券恢复：** 单项部分退款**不自动恢复券**。券为订单级优惠构件，仅在整单取消/整单退款时恢复。残留风险：部分退款后券仍处于已使用状态，由运营按需人工处理。
4. **积分返还：** 单项部分退款**不返还积分抵扣**（积分抵扣为订单级构件，与券恢复 Decision 对称）；仅在整单取消/整单退款时返还用户已扣的抵扣积分（`sourceType=refund-return`）。**购物赠送积分不追回**（即使订单曾确认收货后发生售后退款，已赠送的积分不回收）。

### 售后流转规则

- 已支付未发货（201）或已完成收货（401/402）的商品项可进入可申请售后状态（与 `LitemallAftersaleBizModel` 申请守卫一致）。
- 用户提交售后申请后，该项进入"用户已申请"的售后处理中语义，但不改写订单主状态机已表达的履约结果（未发货仍为已支付，已收货仍保持收货）。
- 管理员审核通过后，售后进入待退款或待后续处理状态。
- 管理员审核拒绝后，本次售后流程结束；订单仍保持既有履约结果。
- 用户在管理员处理前可以撤回申请；撤回后该次售后流程结束，该项恢复可申请。
- 当售后类型要求退货退款时，退款完成意味着售后流程闭环；退货入库等实现动作属于技术和履约实现，不在本 owner doc 展开。

### 售后申请内容

- 售后申请应明确关联原订单、订单商品（`orderItemId`）和售后原因。
- 售后类型必须在"仅退款"和"退货退款"等支持类型中明确表达，且与订单状态匹配（见类型映射表）。
- 售后金额受该订单商品项行金额约束，不能脱离原交易上下文独立存在。
- 售后原因从后台维护的 `mall/aftersale-reason` 字典选项中选择（见 `system-configuration.md`）。

### 售后进度时间线

- 每条售后记录的状态变更时间点由 `LitemallAftersale` 既有/扩展字段承载：申请时间（`addTime`）、管理员操作时间（`handleTime`）、处理时间（`processTime`）、处理备注（`processNote`）。
- 前台售后详情与后台审核页据此展示按状态变更的时间线（申请 → 审核 → 退款），使用户与运营可追踪售后进度。

### 通知语义

- 退款通知维持**订单级去重**：同一订单的多次 item 级退款按 `orderSn` 触发，通知内容附带被退款的商品项信息（商品名/规格/退款额）。
- 抉择理由：避免多次 item 退款对用户造成通知轰炸；订单级聚合通知更符合用户心智。残留风险：多次退款仅一条通知，需用户在订单详情核对明细。

### 角色与可见性

- 商城用户可以查看自己订单各商品项的售后申请状态与处理结果。
- 管理员可以查看售后申请、执行审核与退款，并保留必要的处理记录。
- 售后流程中的审核、退款与结果通知不应改变订单原始快照和履约完成事实。

## 订单运营工作台（P21）

后台运营工作台为管理员提供订单批量发货、改价/改运费、改地址、订单标记、orderSn 模糊搜索与异常监控能力。所有运营动作标注 `@Auth(roles="admin")`，仅管理员可达。

### 改价 / 改运费安全策略

订单价格含 6 层构件（见「价格语义」）。改价动作按构件层分级守卫，避免破坏既有折扣分摊与上限语义：

- **改运费（freightPrice）**：仅待支付（101）允许改。freight 在 orderPrice 加项层，不触碰折扣层，重算 `orderPrice/actualPrice` 后安全落库。
- **改商品价（goodsPrice）**：仅待支付（101）且 `couponPrice=promotionPrice=integralPrice=grouponPrice=pinTuanPrice` 全为 0（纯商品订单，无任何活动折扣）时允许并重算；任一折扣非 0 则拒绝（`ERR_ORDER_PRICE_MODIFY_DISCOUNT_ACTIVE`）。理由：goodsPrice 变更会使已派生的满减门槛/券门槛/积分上限失效，重算将破坏既有折扣分摊与上限语义，存在负 orderPrice / 积分超额风险。残留风险：带折扣订单的 goodsPrice 改价需求由运营先取消订单重建满足。
- 已支付订单已固化入账金额，不允许改价（`ERR_ORDER_NOT_ALLOW_MODIFY_PRICE`）。

`modifyOrderPrice` 接受增量参数 `freightPriceDelta`/`goodsPriceDelta`（可正可负），最低 0 不出现负价；可选 `remark` 补丁式追加到 `adminRemark`。

### 改地址

- 仅发货前（待支付 101 / 已支付未发货 201）允许改地址（`ERR_ORDER_NOT_ALLOW_CHANGE_ADDRESS`）。
- 新地址经 `ILitemallAddressBiz` 校验归属同一用户后写入 consignee/mobile/address（`ERR_ORDER_ADDRESS_NOT_BELONG_USER`）。

### 订单标记

- 写既有 `adminRemark` 字段（surface 既有列，无 ORM 改动）。整字段覆盖语义：以传入值作为最新运营备注。

### orderSn 模糊搜索

- 订单管理页 grid 查询支持 orderSn 模糊搜索（`filter_orderSn__contains`），覆盖 Nop 默认查询操作符，无需额外 xmeta 配置。

### 批量发货

- Excel 导入运单号批量发货（orderSn/shipSn/shipChannel 三列），经平台 `nop-excel`（`ExcelHelper.readSheet`）解析，逐行复用 `ship` 单行逻辑（状态守卫 + 事务）。
- 部分失败不阻断成功行，失败行带原因返回（`BatchShipResultBean`）。每行独立提交：某行失败仅记录在结果列表中，不回滚已成功行。

### 异常监控

- `getOverdueUnshippedOrders(cutoffHours)`：`status=201` 已支付未发货且 `addTime` 早于 cutoff 的订单。cutoff 默认 168 小时（与系统配置的自动收货时长 7 天对齐）；显式 0 表示「立即视为逾期」用于人工审视全部未发货订单。
- `getOverdueUnpaidOrders(cutoffMinutes)`：`status=101` 待支付且 `addTime` 早于 cutoff 的订单。cutoff 默认 30 分钟（与系统配置的订单超时分钟数对齐）。
- cutoff 复用系统配置（见 `system-configuration.md` 订单超时/自动收货时长）。
- 异常监控为跨用户聚合查询，`@Auth(roles="admin")` 限定仅管理员可达，与 `cancelExpiredOrders`/`confirmExpiredOrders` 的调度翻转职责互补：调度负责翻转状态，工作台负责暴露逾期集合供运营审视。

## 查询与展示规则

- 用户可以按待支付、待发货、已发货等业务状态组过滤订单列表。
- 订单详情必须展示价格构成、商品快照、配送快照、当前业务状态以及可执行的下一步动作。
- 后台订单视图应支持按用户、订单号、状态和时间范围进行查询。
- 用户侧在支持售后的订单上，应能看到售后资格、售后状态和当前可执行动作。
- 后台售后视图应支持按售后状态、订单号、用户和时间范围进行查询。

## 一致性规则

- 结算和订单提交必须基于当前可售价格和库存，而不是沿用购物车中的过期假设。
- 即使后续商品目录或地址记录发生变化，订单详情也必须保留已购买商品、已选规格和配送数据的原始含义。
- 状态迁移必须遵循允许的业务流转，不得静默跳过本应存在的中间业务含义。
- 售后状态流转不得反向篡改订单主状态机已经表达的履约结果，只能在其基础上补充后置服务结果。

## 跨域引用

订单域是商城主链路的枢纽，与多个域存在交接：

| 交接点 | 方向 | 目标文档 | 说明 |
|--------|------|---------|------|
| 认证前置 | ← 入 | `user-and-address.md` | 购物车、结算、订单、售后均要求已认证用户 |
| 收货地址 | ← 入 | `user-and-address.md` | 结算要求地址归属于当前用户 |
| SKU 可售性与库存 | ← 入 | `product-catalog.md` | 结算/下单基于当前可售价格与库存；订单保留商品快照 |
| 优惠券价格构件 | ← 入 | `marketing-and-promotions.md` | 结算校验可用券，影响 coupon price；取消/退款后恢复 |
| 满减价格构件 | ← 入 | `marketing-and-promotions.md` | 结算自动判定满减最优档位，影响 promotion price（orderPrice 减项层）；自动触发、不可恢复 |
| 限时折扣价格构件 | ← 入 | `marketing-and-promotions.md` | 命中折扣的 SKU 行单价取 min(retail,vip,timeDiscount)，作用于商品单价层（降低 goodsPrice 汇总），不进 promotion price；自动触发、不可恢复 |
| 秒杀独立下单路径 | ← 入 | `marketing-and-promotions.md` | 秒杀走独立 `flashSaleBuy` `@BizMutation` 路径（不进 `submit()`），秒杀价（flashPrice）为成交单价（商品单价层）；不走购物车、不挂券、不与满减/限时折扣/会员价/积分/团购叠加；场次状态由 nop-job 翻转 |
| 积分抵扣构件 | ← 入 | `marketing-and-promotions.md` | 结算勾选积分抵扣，影响 integral price（actualPrice 减项层）；取消/整单退款返还积分，item 级部分退款不返还 |
| 团购上下文 | ← 入 | `marketing-and-promotions.md` | grouponRulesId/grouponId 透传到 submit；团购超时触发 204 |
| 拼团上下文 | ← 入 | `marketing-and-promotions.md` | pinTuanActivityId/pinTuanGroupId 透传到 submit；拼团超时失败全单退款；拼团×团购单订单互斥（同时传则拒绝） |
| 评价资格 | → 出 | `marketing-and-promotions.md` | 收货完成(401/402)是评价资格边界 |
| 售后资格 | → 出 | 本文件（退款与售后章节） | 已支付未发货(201)或已完成收货(401/402)+售后状态可申请(INIT) |
| 运费/超时配置 | ← 入 | `system-configuration.md` | 运费策略、包邮门槛、订单超时、自动收货时长 |
| 支付开关 | ← 入 | `system-configuration.md` | 决定真实模式与示例模式分流 |

全局流程视图见 `flow-overview.md`。
