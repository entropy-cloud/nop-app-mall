# P31 配送方式扩展（门店自提 / Store Pickup）

> Plan Status: active
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 31；`docs/design/order-and-cart.md`（自提核销设计待补，feature-inventory/domain-glossary 已登记归属）
> Related: P21 订单运营工作台（`docs/plans/2026-06-28-0340-3-phase21-order-operations-workbench-plan.md`，发货工作台 successor「合单/拆单」等同列 Non-Goals）；P35 站内信（自提核销成功可选触发站内信，依赖 P35 done 后接线）
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 31（配送方式扩展/自提）

> **执行顺序：** 本计划为 2026-06-28-0530 批次第 3 顺位（N=3）。P31 为独立功能切片（不阻塞也不被本批次其他计划阻塞），但需先补业务设计（自提核销章节当前缺失）且触点多（结算/下单/核销/后台），复杂度高于 N=2（P38），故置于批次末位。模型与脚手架均已预置（Order deliveryType/pickupStoreId/pickupCode + PickupStore 实体 + 空 CrudBizModel）。

## Current Baseline

> 实读 live repo 所得，非记忆。

**模型已预置（关键）：**

- `model/app-mall.orm.xml:1126-1129` — `LitemallOrder` 已有：`deliveryType`(propId 32, dict `mall/delivery-type`)、`pickupStoreId`(propId 33)、`pickupCode`(propId 34, 自提核销码)、`pickupTime`(propId 35, 自提核销时间)。
- `model/app-mall.orm.xml:1144-1149` — Order→`pickupStore` to-one 关系已存在（join pickupStoreId→id）。
- `model/app-mall.orm.xml:126-129` — `mall/delivery-type` 字典已定义：`EXPRESS`(0,快递)/`PICKUP`(10,门店自提)。
- `model/app-mall.orm.xml:1938-1969` — `LitemallPickupStore`（自提门店表）已存在：`name`/`address`/`contact`/`phone`/`latitude`/`longitude`/`openingHours`/`status`/`remark` + 审计字段。
- Order 索引仅 `idx_order_userId`/`idx_order_status` + 唯一键 `orderSnKey`（`:1151-1161`）——**`pickupCode` 无索引/唯一键**（核销码反查为应用层 + 唯一性应用层保证，见 Deferred model-gap）。
- roadmap Entity Coverage（`enhanced-features-roadmap.md:578`）将 PickupStore 列为「新增实体」，**与实际不符**——已落地于模型（与 P26/P28/P35 同模式，模型预置）。

**源真相冲突（非降级，须在 Phase 1 处置）：**

- `enhanced-features-roadmap.md:75` 称 `order-and-cart.md`「已包含 配送方式扩展、自提核销…的业务设计」——**不实**（实读 `order-and-cart.md` 全文，自提/核销/pickup 零匹配）。`feature-inventory.md:17`/`domain-glossary.md:53` 亦登记该归属。Phase 1 须记录此 owner-doc 漂移并在 doc-sync 中订正 roadmap 该陈述。

**脚手架已生成但空：**

- `app-mall-service/.../entity/LitemallPickupStoreBizModel.java`（15 行纯 `CrudBizModel`）——无业务方法（无 listActiveStores 等）。Api/InputBean/OutputBean/Dao 已 codegen 生成。

**结算/下单/订单流程已就绪（实读核对）：**

- 结算页 `app-mall-web/.../pages/mall/checkout/checkout.page.yaml` 存在；下单 `LitemallOrderBizModel.submit()`（`:185`）当前 `addressId` **必填**（校验归属 `:225-234`、写 consignee/mobile/fullAddress `:266-272`）、运费 `:275-280` 解析、价格公式 `:439-457`（coupon/promotion 派生自 `goodsPriceTotal` 而非 freight，integral 受 orderPrice 上限——**freight=0 对价格公式安全**）。当前 `submit()` 无 `deliveryType` 分支。
- 发货 `ship()`（`:658`，守卫 `:667` **仅** `orderStatus==201(已支付)`）在 `:680` 发 `sendOrderShipNotification`、`:681` 写 `logManager.logOrderSucceed("订单发货")`；收货确认 `confirm()`（`:687`，要求 `orderStatus==301(已发货)` `:694`）的副作用**仅** `earnPointsForOrderConfirm`（`:705`）+ confirmTime（`:702`）——**无通知、无 logOrderSucceed**（这两者属 ship）。
- 调度：`confirmExpiredOrders`（`:873`）仅处理 SHIP(301)；`getOverdueUnshippedOrders` 查 `status=201`（`order-and-cart.md:353`）。
- 运费规则在 `system-configuration.md`（运费金额/包邮门槛）。

**业务设计缺口（本计划交付对象）：**

1. `order-and-cart.md` **无自提核销章节**——结算页配送方式切换、自提订单流程、核销码生成与门店核销、运费=0 规则均未定义。
2. 下单 `submit()` 无 `deliveryType` 分支：自提订单应 freightPrice=0、生成 pickupCode、跳过发货流转；且 `addressId` 必填约束需在自提分支放宽（自提无收货地址）。
3. `ship()`/`getOverdueUnshippedOrders` 未排除自提订单（会误发货/误列入逾期未发货）；`verifyPickupOrder` 不能复用 `confirm()`（后者要求 301），须自行复制 confirm 的真实收货副作用（积分赠送/`pickupTime`），**不得**复用 ship 的通知/日志（自提无发货语义）。
4. 无门店核销 API（无 `verifyPickupOrder` mutation）；无已支付未自提订单的超时/取消/退款生命周期（见 Phase 1 Decision）。
5. 前台结算页无快递/自提切换 + 门店选择；自提订单详情无核销码展示；后台无门店管理 + 核销工作台。
6. 无 ErrorCode、无测试。

**前置条件已满足：** Phase 5/5b（订单/结算核心）`done`。

**已知交叉：** 自提订单不走发货 `ship()`，需 Decision 确定状态流转与既有订单作业/调度的隔离（见 Phase 1 Decision A）；运费=0 复用既有 freight 计算前置判定；自提核销成功触发站内信依赖 P35 done（Non-Goal）。

## Goals

- 结算页配送方式切换：快递（EXPRESS，走既有流程）/ 门店自提（PICKUP，选门店）。
- 自提订单下单：`submit()` 识别 `deliveryType=PICKUP` → freightPrice=0、写入 pickupStoreId、生成 pickupCode（核销码）、跳过发货流转进入「待自提」。
- 自提订单状态流转：支付成功 → 待自提 → 门店核销 → 已完成收货（复用既有收货终态语义，不破坏订单主状态机）。
- 门店核销：后台/门店端 `verifyPickupOrder(pickupCode)` 核销自提订单（校验码 + 订单状态），核销成功推进到已完成收货。
- 后台自提门店管理（PickupStore CRUD + 启停）+ 核销工作台。
- 新增 pickup 域 ErrorCode；核心路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- 多门店库存（门店独立库存/调拨）——本基线自提订单仍扣减统一商品库存，门店仅为提货点。
- 自提时段预约/到店时段选择——超出 P31，roadmap 未列。
- 合单/拆单、批量打印快递单/发货单——属 P21 successor（P21 计划 Deferred 已列）；退货入库/物流回寄——属 P16 successor（P16 计划 Deferred 已列）。
- 自提核销成功触发站内信——依赖 P35 done；本计划核销后不主动发站内信（可作 P35 done 后的接线 follow-up）。
- 移动端前端（属 `mobile-frontend-roadmap.md`）。
- 地图选店（LBS 最近门店）——本基线提供门店列表选择 + 经纬度展示，不做地图导航/距离排序。

## Task Route

- Type: `app-layer design change` + `implementation-only change`（业务设计先行）
- Owner Docs: `docs/design/order-and-cart.md`（新增「配送方式扩展/自提核销」章节）、`docs/design/system-configuration.md`（自提门店管理归属）
- Skill Selection Basis: 后端 BizModel 方法/错误码 → `nop-backend-dev`；`@BizMutation`/`@BizQuery` 测试 → `nop-testing`（规则 #15）；AMIS 页面 → `nop-frontend-dev`；模型校验 → `nop-orm-modeler` + `nop-database-design`

## Infrastructure And Config Prereqs

- 无新外部服务/端口/密钥。无破坏性数据迁移（PickupStore 新表 + Order 既有列，存量订单 `deliveryType` 默认 EXPRESS/null，兼容）。
- 核销码生成：应用层生成（如短码/数字码），不依赖外部编码服务。

## Protected Area

> 本计划**预期不**触及受保护区域：
>
> - `model/app-mall.orm.xml`：Order deliveryType/pickupStoreId/pickupCode 列、PickupStore 实体、`mall/delivery-type` 字典均已存在，预期无需改模型。若 Phase 1 Decision 需要新增关系/字典（如自提订单专属状态码），按 ask-first 流程处理并在此记录证据。
> - 不触及 `app-mall-delta`（自提核销不涉及认证；门店管理与核销工作台走既有 RBAC `@Auth(roles="admin")`）。
> - 不触及微信支付/数据删除受保护路径（自提订单仍走既有支付通道；核销为状态推进，非删除）。

## Execution Plan

### Phase 1 — 业务设计合成：自提核销语义（Decision-heavy）

Status: planned
Targets: `docs/design/order-and-cart.md`（新增「配送方式扩展/自提核销」章节）
Required Skill: `none`（纯 docs 业务语义合成，模型已就绪不改；无 skill 匹配「写设计文档」——与 P28/P35 Phase 1 同模式）

- Item Types: `Decision | Add`
- Prereqs: Phase 5/5b done

- [ ] **Skill loading gate:** 扫描 available skills；docs-only，无匹配。读 owner doc：`order-and-cart.md`（全文，订单状态机/下单/发货/收货）、`system-configuration.md`（运费规则）、`enhanced-features-roadmap.md` Phase 31、`LitemallOrderBizModel.submit()`/`ship()`/`confirm()` 既有实现。
  - Docs read: <列出实际读取路径>
- [ ] **Decision A: 自提订单状态流转 + 既有订单作业/调度隔离。** 抉择（自提订单支付后保持「已支付(201)」，以 `deliveryType=PICKUP` + pickupCode 标识「待自提」；核销 `verifyPickupOrder` 推进到 **401（用户已收货）** 终态，**不经 301**）。新增「待自提」独立主状态码（备选）被否——冲击主状态机且需改字典/模型。**须同时落定四项隔离（非可选）**：
  - (a) `ship()` 增加守卫**拒绝** `deliveryType=PICKUP` 订单发货（`ERR_ORDER_PICKUP_NOT_SHIPPABLE`），防止误发货；
  - (b) `verifyPickupOrder` **不复用** `confirm()`（后者要求 301），须**自行复制 confirm 的真实副作用**：仅 `earnPointsForOrderConfirm`（`:705`）+ 写 `pickupTime`(propId 35)；**不得**复用 ship 的 `sendOrderShipNotification`/`logOrderSucceed("订单发货")`（自提无发货语义，强复用会发错误通知），pickup 完成通知 deferred 至 P35；
  - (c) `getOverdueUnshippedOrders` 查询**排除** `deliveryType=PICKUP`（自提订单合法停留在 201，不应污染逾期未发货列表）；
  - (d) 已支付未自提订单生命周期：复用既有 `confirmExpiredOrders` 不触及 201 的事实，**新增显式残留风险**——已支付长期未自提订单无自动超时取消/退款路径（本基线由运营人工在订单运营工作台处理；自动超时取消作为 successor，触发条件见 Deferred）。
  - 终态语义模糊残留风险：401 语义原为「用户主动确认」，门店核销为操作员驱动，复用 401 存在 cause 语义模糊（已记录，无更优既有状态可用）。
- [ ] **Decision B: `addressId` 在自提分支的处理。** 抉择（`submit()` 自提分支**放宽 `addressId` 必填**——自提无收货地址，`addressId`/consignee/mobile/fullAddress 置空，改以 `pickupStoreId` 记录提货点；`addressId` 必填校验仅在 `deliveryType=EXPRESS`（含 null 兼容存量）时生效）。备选（强制自提也填地址用于记录）被否——徒增用户摩擦且语义冗余。
- [ ] **Decision C: 运费=0 接入点。** 抉择（`submit()` 内 `deliveryType=PICKUP` 时 freightPrice 直接置 0，绕过运费/包邮门槛计算）。备选（统一计算后抵扣）被否——自提不产生配送成本，置 0 语义最清晰；价格公式其余构件（coupon/promotion/integral）派生自 goodsPriceTotal/orderPrice，freight=0 安全。
- [ ] **Decision D: 核销码生成与校验。** 抉择（下单时应用层生成 pickupCode 唯一短码写入 Order；核销按 pickupCode 反查订单 + 状态守卫 + 幂等；唯一性应用层保证，DB 唯一键见 Deferred model-gap）。备选（二维码承载 orderId）被否——短码便于门店手动输入/扫码通用。残留风险：pickupCode 可预测性（生成需避免可枚举，采用随机短码）。
- [ ] **Decision E: 核销权属。** 抉择（门店核销由 `@Auth(roles="admin")` 管理员/门店员执行，本基线不引入门店员独立角色；roadmap 未列门店员 RBAC）。
- [ ] **Decision F: 自提订单售后路径。** 抉择（核销→401 后自提订单具备售后资格，售后类型限定 `GOODS_NEEDLESS`(无需退货退款)——已收货且无物理回寄，见 `order-and-cart.md:240`；不开放退货退款）。在自提核销章节显式记录。
- [ ] **Fix（源真相）:** `enhanced-features-roadmap.md:75` 称自提核销设计已存在于 `order-and-cart.md`——不实。Phase 1 记录此 owner-doc 漂移，并在 doc-sync 中**订正**该 roadmap 陈述为「设计由本计划补齐」。
- [ ] **Add:** 自提核销业务设计写入 `order-and-cart.md` 新增「配送方式扩展/自提核销」章节（含状态流转图、四项隔离、运费规则、核销码语义、核销动作、售后路径、6 个 Decision 抉择/备选/理由/残留风险）；与既有订单状态机/发货/收货章节衔接。

Exit Criteria:

- [ ] `order-and-cart.md` 含自提核销完整业务设计（含状态流转 + 四项隔离 + 6 个 Decision）
- [ ] `enhanced-features-roadmap.md:75` 陈述订正（owner-doc 漂移闭合）
- [ ] Phase 3 模型改动清单由本阶段 Decision 确定（预期零新增列/关系；若需字典则显式列出）—— 结论写入本阶段

### Phase 2 — 模型准备（按 Phase 1 Decision）

Status: planned
Targets: `model/app-mall.orm.xml`（仅当 Decision 要求）、codegen 重生成
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Add`
- Prereqs: Phase 1

- [ ] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读 routing 必读文档。复核 Order deliveryType/pickupStoreId/pickupCode + PickupStore + delivery-type 字典已就绪。
  - Docs read: <列出实际读取路径>
- [ ] **Add:** 按 Phase 1 Decision —— 预期零模型改动。仅验证 codegen 产物完整（`_gen/_LitemallPickupStore.java`、Order pick 相关字段 getter/setter）。
- [ ] **Proof:** `./mvnw install -pl app-mall-codegen -am -DskipTests` + `./mvnw install -pl app-mall-dao -am -DskipTests` BUILD SUCCESS。

Exit Criteria:

- [ ] 模型就绪（零改动或 Decision 要求的改动落地），codegen 通过，编译成功
- [ ] 不在模型准备阶段写业务逻辑（rule #11）

### Phase 3 — 后端：自提下单分支 + 核销 + 门店查询 + ErrorCode（Add-heavy）

Status: planned
Targets: `app-mall-service/.../entity/LitemallOrderBizModel.java`（submit 自提分支、ship 守卫、verifyPickupOrder、getOverdueUnshippedOrders 排除）、`LitemallPickupStoreBizModel.java`（listActiveStores）、`AppMallErrors.java`
Required Skill: `nop-backend-dev`、`nop-testing`（新增 `@BizMutation`/`@BizQuery`，规则 #15）

- Item Types: `Add | Fix`
- Prereqs: Phase 2

- [ ] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读 routing 必读文档（bizmodel-method-selfcheck、error-handling、safe-api-reference、test-examples）。每方法 selfcheck。
  - Docs read: <列出实际读取路径>
- [ ] **Add:** `submit()` 内 `deliveryType=PICKUP` 分支：校验 pickupStoreId 对应门店启用（拒 `ERR_PICKUP_STORE_NOT_ACTIVE`）、**放宽 `addressId` 必填**（Decision B，自提不写 consignee/mobile/fullAddress）、freightPrice=0、生成 pickupCode（随机短码）、写入 pickupStoreId/deliveryType。
- [ ] **Fix（隔离 a）:** `ship()` 增加守卫——`deliveryType=PICKUP` 订单拒绝发货（`ERR_ORDER_PICKUP_NOT_SHIPPABLE`），防止误发货。
- [ ] **Fix（隔离 c）:** `getOverdueUnshippedOrders` 查询排除 `deliveryType=PICKUP`（自提订单合法停留 201，不污染逾期未发货列表）。
- [ ] **Add:** `LitemallPickupStoreBizModel.listActiveStores(context)` —— `@BizQuery`：返回 status=启用的门店列表（含经纬度/营业时间）。
- [ ] **Add（隔离 b）:** `LitemallOrderBizModel.verifyPickupOrder(pickupCode,context)` —— `@BizMutation` `@Auth(roles="admin")`：按 pickupCode 反查订单，校验状态（须为已支付且 deliveryType=PICKUP，拒 `ERR_PICKUP_ORDER_NOT_VERIFIABLE`）、幂等（已核销跳过）、推进到 **401** 终态并**复制 confirm 的真实副作用**：`earnPointsForOrderConfirm`（`:705`）+ 写 `pickupTime`(propId 35)；**不复用** ship 的 `sendOrderShipNotification`/`logOrderSucceed("订单发货")`（自提无发货语义）。跨实体门店查询经 `ILitemallPickupStoreBiz`。
- [ ] **Add:** `AppMallErrors` 新增 pickup 域 ErrorCode（`ERR_PICKUP_STORE_NOT_ACTIVE`、`ERR_PICKUP_STORE_NOT_FOUND`、`ERR_PICKUP_ORDER_NOT_VERIFIABLE`、`ERR_PICKUP_CODE_INVALID`、`ERR_ORDER_PICKUP_NOT_SHIPPABLE`，中文描述）。DTO：`PickupStoreListBean`/`VerifyPickupResultBean`（`app-mall-dao/.../dto/`，@DataBean）。接口声明。
- [ ] **Proof:** submit 自提分支 + ship 拒绝 PICKUP + getOverdueUnshippedOrders 排除 + listActiveStores + verifyPickupOrder 通过 `IGraphQLEngine`（`JunitBaseTestCase`）：覆盖自提下单 freightPrice=0/pickupCode 生成/addressId 放宽、门店未启用拒绝、**ship() 拒绝自提订单**、**自提订单不进逾期未发货列表**、核销成功推进 401 且触发积分赠送 + 写 pickupTime（不发 ship 通知）、重复核销幂等、非自提订单核销拒绝、无效码拒绝。全量回归无失败。

Exit Criteria:

- [ ] 自提下单（freight=0 + pickupCode + 门店校验 + addressId 放宽）与核销（推进 401 + 复制副作用 + 幂等 + 拒绝路径）按设计工作
- [ ] 四项隔离全部落地（ship 拒绝、overdue 排除、副作用复制、abandoned 残留风险记录）
- [ ] **API 测试：** listActiveStores（`@BizQuery`）+ verifyPickupOrder（`@BizMutation`）通过 `IGraphQLEngine` 验证；submit 自提分支 + ship/overdue 隔离通过 IGraphQLEngine 验证

### Phase 4 — 前端：结算切换 + 自提订单详情 + 后台门店管理/核销（Add-heavy）

Status: planned
Targets: `app-mall-web/.../pages/mall/checkout/checkout.page.yaml`（配送方式切换 + 门店选择）、订单详情（核销码展示）、后台 PickupStore 管理 + 核销工作台
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 3

- [ ] **Skill loading gate:** 加载 `nop-frontend-dev`，读 routing 必读文档（XView 三层、bounded-merge、page-dsl）。文件完成后 selfcheck。
  - Docs read: <列出实际读取路径>
- [ ] **Add:** 结算页配送方式切换：快递（既有地址 + 运费展示）/ 门店自提（调 `listActiveStores` 选门店、运费=0 提示）；submit 时透传 `deliveryType`/`pickupStoreId`。
- [ ] **Add:** 自提订单详情：展示 pickupCode（核销码，支持二维码/短码）+ 「待自提」状态标识；与快递订单详情区分。
- [ ] **Add:** 后台 `LitemallPickupStore.view.xml` 管理（grid bounded-merge：name/address/phone/status；edit drawer 全字段）；核销工作台页（输入/扫码 pickupCode → 调 `verifyPickupOrder` → 结果反馈）。

Exit Criteria:

- [ ] 结算页可切换快递/自提并选门店，自提订单运费=0
- [ ] 自提订单详情展示核销码，后台可管理门店与核销
- [ ] 复用既有 AMIS 三层定制模式，无新前端依赖（`./mvnw compile -pl app-mall-web` BUILD SUCCESS）

### Phase 5 — 验证、文档同步、日志（Proof）

Status: planned
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-4

- [ ] **Skill loading gate:** 加载 `nop-testing`（Phase 3 已读，复用）。
- [ ] **Proof:** `./mvnw test -pl app-mall-service -am` 全绿（含新增 IGraphQLEngine 测试）；`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS；更新 `docs/testing/known-good-baselines.md`。
- [ ] **Proof:** 前端 view 编译（`./mvnw -pl app-mall-web -DskipTests compile`）BUILD SUCCESS。
- [ ] 更新 `docs/design/system-configuration.md`（自提门店管理归属）+ `docs/logs/2026/{month}-{day}.md`。

Exit Criteria:

- [ ] 全量验证命令通过（含本计划新增 IGraphQLEngine 测试）
- [ ] `system-configuration.md` 含自提门店管理归属
- [ ] `known-good-baselines.md` 与 `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗审计（fresh session），三轮达成共识（round 3 PASS）。
- Evidence:
  - Round 1（`ses_0f47dec4cffeUfJp8KpPbHYs64`）：REVISE — 3 MAJOR（Decision A 状态流转与既有 ship/confirm/overdue 作业冲突、submit() 必填 addressId、roadmap:75 谎称自提设计已存在）+ 5 MINOR（pickupCode 无唯一键、PICKUP_TIME propId35 遗漏、401/402 语义模糊、P21/P16 successor 误归、自提售后路径）。
  - Round 2（`ses_0f474146dffeCOrzFjd17lLbwF`）：REVISE — 3 MAJOR + 5 MINOR 全部结构解决，但修订引入 1 MAJOR（confirm() 副作用误归：通知/logOrderSucceed 属 ship() 非 confirm()；earnPointsForOrderConfirm 在 :705 非 :892）。
  - Round 3（`ses_0f46f8aecffeicvf5da3g5DN00`）：PASS — confirm/ship 副作用归属已在 baseline:37/Decision A(b):106/Phase3:158/Proof:160 四处订正（verifyPickupOrder 仅复制 earnPointsForOrderConfirm(:705)+pickupTime，不复用 ship 通知/日志），无新 blocker/major（line 45 残留 prose 已订正）。
  - 关键修正：四项隔离（ship 拒绝 PICKUP/overdue 排除 PICKUP/verifyPickupOrder 复制真实副作用/abandoned 残留风险）、addressId 在 PICKUP 放宽（Decision B）、roadmap:75 owner-doc 漂移 Fix、pickupCode model-gap deferred、PICKUP_TIME 写入、401 选择 + 残留风险、P21/P16 归属订正、自提售后 GOODS_NEEDLESS。

## Closure Gates

- [ ] in-scope behavior is complete（自提下单 + 四项隔离 + 核销 + 门店管理 + 前后台）
- [ ] relevant docs are aligned（`order-and-cart.md` 自提核销章节 / `system-configuration.md` 门店管理归属 / `enhanced-features-roadmap.md:75` 陈述订正）
- [ ] verification has run（`./mvnw test -pl app-mall-service -am` 全绿 + app-mall-web 编译）
- [ ] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（listActiveStores/verifyPickupOrder + submit 自提分支 + ship/overdue 隔离）
- [ ] no in-scope item downgraded to deferred/follow-up
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill` listed，Nop-platform phases 不写 `none` 无 justify（Phase 1 `none` 含 justify）
- [ ] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [ ] text consistency verified: status / phases / gates / log 一致
- [ ] closure audit was performed by a different agent/session than implementation
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### pickupCode 唯一键/索引（model-gap）

- Classification: `model-gap`
- Why Not Blocking Closure: 核销码反查与唯一性当前由应用层保证（基线订单量非超大，应用层足够）；DB 唯一键为强一致兜底。
- Successor Required: `yes`
- Model Gap Detail: 缺 `LitemallOrder.pickupCode` 唯一键/索引；触发条件——下次修改 Order 模型时，或核销码反查成为热点/要求 DB 级唯一强一致时，补 unique-key/index。

### 已支付未自提订单自动超时取消/退款

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本基线自提订单合法停留在 201 待用户到店核销，无自然超时语义；长期未自提订单由运营在订单运营工作台人工处理。自动超时取消需引入「最大待自提时长」配置 + 调度任务 + 退款编排。
- Successor Required: `yes`（触发条件：运营要求自提订单自动超时取消退款时）

### 自提核销成功触发站内信

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 依赖 P35 站内信中心 done；本计划核销后不主动发站内信，核销结果由订单详情/后台反馈呈现。
- Successor Required: `yes`（触发条件：P35 done 且运营要求核销成功通知用户时，接线 `MallNotificationService.sendUserMessage`）

### 多门店独立库存/调拨

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本基线自提订单扣减统一商品库存，门店仅为提货点；roadmap 未列门店库存。
- Successor Required: `no`（除非后续引入多仓库存模型）

## Closure

<!-- Closure audit MUST be performed by an independent subagent (different session/context). 留给闭合审计代理。 -->

Status Note: <待实施与闭合审计后填写>

Closure Audit Evidence:

- Reviewer / Agent: <independent reviewer — MUST NOT be the implementing agent>
- Evidence: <task id / walkthrough record>

Follow-up:

- 自提核销成功触发站内信（触发条件：P35 done 且运营要求通知）。
- 地图选店/LBS 排序（触发条件：门店数量增长至需按距离导航时）。
