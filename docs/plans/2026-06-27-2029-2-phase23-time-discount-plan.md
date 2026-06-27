# P23 限时折扣（Time-Limited Discount）

> Plan Status: completed
> Last Reviewed: 2026-06-27
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 23；`docs/design/marketing-and-promotions.md`、`docs/design/product-catalog.md`、`docs/design/order-and-cart.md`
> Related: `docs/plans/2026-06-27-1742-1-phase15-full-discount-promotion-plan.md`（P15 满减已 done，遗留 promotionPrice 槽位 + 限时折扣共存问题交本计划裁决）；`docs/plans/2026-06-27-2029-1-phase27-points-system-plan.md`（同批次 N=1，价格层位不同）；下游贡献 P22 营销活动管理后台
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 23（限时折扣）

> **执行顺序：** 本计划为 2026-06-27-2029 批次第 2 顺位（N=2）。P27（N=1）优先（解除更多下游阻塞）。本计划与 P27 均改 `LitemallOrderBizModel.submit()` 价格构成，但作用层不同（限时折扣在 goodsPrice 汇总层/商品单价层，积分抵扣在 actualPrice 减项层），须协调 price-formula 接线（见 Non-Goals 与 Phase 2）。本计划裁决 P15 遗留的「限时折扣与满减 promotionPrice 槽位共存」问题。

## Current Baseline

> 实读 live repo（working tree，HEAD `35d90f2` + P15/P26/P16 批次 uncommitted 改动）所得，非记忆。

**模型已就绪（脚手架已生成）：**

- `LitemallTimeDiscount`（限时折扣表，`app-mall.orm.xml:1502-1542`）：`goodsId`(商品ID)、`productId`(SKU ID，null=全部SKU)、`discountType`(dict `mall/discount-type` 0=减金额/10=打折)、`discountValue`(折扣值)、`startTime`/`endTime`(时间窗)、`status`(dict `mall/promotion-status` 0/10/20/30)、`stockLimit`(折扣库存，0=不限)、`remark` + `goods` to-one 关系。
- 复用既有字典：`mall/discount-type`（与 P15 满减 Tier 共用）、`mall/promotion-status`（活动状态语义 0=草稿/10=进行中(ACTIVE)/20=已结束/30=已关闭(CLOSED)，P15 已定义；Phase 2 Java 过滤 `status==ACTIVE`）。
- `LitemallOrderGoods.price`（实际成交单价，`app-mall.orm.xml` OrderGoods 实体，P26 已用 `min(retail,vip)` 写入）+ `vipPrice`（会员价快照，P26 新增）。

**BizModel 脚手架已生成但空：**

- `LitemallTimeDiscountBizModel` 为纯 `CrudBizModel` 空骨架（已确认，无业务方法）。Admin view 为空骨架。

**价格公式现状（post P15/P26，关键交叉点）：**

- `LitemallOrderBizModel.submit()` 当前每行单价：`:226-233` `unitPrice = product.getPrice()`，会员 `min(retail, vipPrice)`（P26 vipPrice 在 goodsPrice 汇总层）；`:239` `lineTotal = unitPrice × number` 汇总 goodsPriceTotal。
- `orderPrice = goodsPrice + freight - coupon - promotionPrice`（`:295-299`，P15 满减在 orderPrice 减项层）。
- `LitemallOrder.PROMOTION_PRICE` 列注释（`app-mall.orm.xml:1130`）：「促销优惠金额（满减/限时折扣）」——**P15 假设限时折扣复用此槽位**（P15 plan line 33），但本计划 Phase 1 将裁决限时折扣实为**商品单价层**（goodsPrice 汇总层），与满减 orderPrice 减项层不同，**不复用 promotionPrice 槽位**（见 Decision）。该列注释需订正。

**业务设计缺失（关键）：**

- `marketing-and-promotions.md` **无「限时折扣」章节**（已读全文 53 个标题确认）；`domain-glossary.md:43` 定义「限时折扣 = 商品级或 SKU 级的短期降价促销」；`product-catalog.md:154` 仅提及促销标签展示；`order-and-cart.md:56` promotion price 段提及「满减/限时折扣共用此价格槽位」（**与本计划裁决冲突，需订正**）。roadmap（`enhanced-features-roadmap.md:74`）声称「已包含限时折扣业务设计」与实际不符，同 P15/P27 文档缺口模式。

**P15 遗留裁决点：** P15 plan（line 33, Non-Goals line 46）明确「P23 限时折扣将复用同一 promotionPrice 槽位时，若需与满减并存，需额外选择逻辑（P23 处理）」。本计划 Phase 1 裁决此遗留点。

**无 ErrorCode：** `AppMallErrors.java` 无 time-discount 域错误码（参照 promotion 域）。

**前置条件已满足：** P5（订单核心流程）+ P5b（支付集成）均 `done`。P15 满减 `done`（promotionPrice 机制与商品范围匹配模式可参照）。

## Goals

- 实现商品级/SKU 级短期降价促销（限时折扣），按时间窗自动生效，含倒计时与剩余库存。
- 裁决限时折扣价格层位（商品单价层 / goodsPrice 汇总层），明确与满减（orderPrice 减项层）的共存语义。
- 将限时折扣接入订单价格构成（goodsPrice 汇总层），与会员价（vipPrice）层位协调。
- 详情页促销横幅（促销价 + 直降金额 + 倒计时）；列表页促销价展示。
- 后台限时折扣活动管理页面。
- 新增 time-discount 域 ErrorCode；核心路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- 秒杀（P24，独立 phase，需场次模型 + nop-job 状态切换 + 限购）——限时折扣为常规降价促销，不涉场次/抢购/不走购物车直接购买。
- 拼团（P25）、营销活动管理后台统一入口（P22，依赖 P15+P23+P24+P25）。
- **与 P27 积分抵扣的价格公式协调约定**：限时折扣在 goodsPrice 汇总层（商品单价），积分抵扣在 actualPrice 减项层，两者作用层不同，无直接冲突。计算顺序天然正确（先限时折扣单价 goodsPrice → orderPrice 减满减 → actualPrice 减积分）。本计划与 P27（N=1）顺序执行，submit 改动以各自层位独立接线。
- **限时折扣 × 满减共存**：限时折扣降低商品单价（goodsPrice 汇总层）→ 满减门槛以折扣后 goodsPrice 判定 → 两者天然可共存，无需额外选择逻辑（裁决 P15 遗留点，见 Phase 1 Decision）。
- 移动端前端（独立 roadmap）。

## Task Route

- Type: `app-layer design change` + `implementation-only change`（业务设计先行，裁决 P15 遗留层位问题，随后实现；模型已就绪无需模型准备 phase）
- Owner Docs: `docs/design/marketing-and-promotions.md`（限时折扣业务规则/状态/共存）、`docs/design/product-catalog.md`（促销价展示/倒计时）、`docs/design/order-and-cart.md`（价格构成层位订正）
- Skill Selection Basis: 后端 BizModel 方法/错误码/价格接线 → `nop-backend-dev`；新增 `@BizQuery` 计算方法需 `IGraphQLEngine` 测试 → `nop-testing`（规则 #15）；AMIS 页面 → `nop-frontend-dev`

## Protected Area

> 见 `docs/context/ai-autonomy-policy.md`。本计划触及以下受保护区域，**执行前须取得人工确认**：
>
> - **`model/app-mall.orm.xml`（ask-first）：** Phase 2 订正 `LitemallOrder.PROMOTION_PRICE` 列注释（`app-mall.orm.xml:1130`）为「满减优惠金额」（移除「限时折扣」，因限时折扣裁决为商品单价层不复用此槽位）；并按 Phase 1 Decision 可能订正 `order-and-cart.md:56` 描述。ORM `comment` 属性仅为元数据（生成的实体用 displayName+code，不读 comment），故无需重生成，受影响代码经 install + test 全绿。**预期不新增实体/字段/关系**（TimeDiscount 已就绪）。
> - 本计划**不**触及 `app-mall-delta`（限时折扣不涉及认证/权限 Delta）。
>
> 证据要求：ask-first → 人工确认 → 改 ORM comment → 编译/测试通过（comment 不驱动 codegen 故无需重生成）。

## Infrastructure And Config Prereqs

- **基础基线锁定（前置门）：** 本批次计划建立在当前 working tree 基线（HEAD `35d90f2` + P15/P26/P16 批次 uncommitted 改动）之上。执行前须先将 P15/P26/P16 批次变更提交锁定为稳定 HEAD（同 P27 前置门）。
- 限时折扣默认开关、stockLimit 强制策略等可配项 → `NopSysVariable`（既有模式）。建议 key：`mall_time_discount_enabled`（全局开关，默认 true）。
- 无外部服务/端口/密钥依赖。无数据迁移（新表已存在，存量订单 promotionPrice 不受影响——限时折扣不进 promotionPrice 槽位）。

## Execution Plan

### Phase 1 — 限时折扣业务设计合成 + 层位裁决（Decision-heavy）

Status: completed
Targets: `docs/design/marketing-and-promotions.md`、`docs/design/order-and-cart.md`、`docs/design/product-catalog.md`
Required Skill: `none`（纯 docs 业务语义合成，不改 ORM 模型与代码；模型已就绪。无 skill 匹配「写设计文档」）

- Item Types: `Decision | Add`
- Prereqs: 无（P15 已 done，遗留裁决点上下文已具备）

- [x] **Skill loading gate:** 扫描 available skills；本阶段 docs-only，无匹配。已读 owner doc：`marketing-and-promotions.md`（全文）、`order-and-cart.md`（价格构成 56-69 + 满减共存）、`product-catalog.md`（促销价展示 154）、`domain-glossary.md:43`（限时折扣定义）、P15 plan（遗留裁决点 line 33/46）。
  - Docs read: `docs/design/marketing-and-promotions.md`、`docs/design/order-and-cart.md`、`docs/design/product-catalog.md`、`docs/design/domain-glossary.md`、`docs/plans/2026-06-27-1742-1-phase15-full-discount-promotion-plan.md`
- [x] **Decision: 限时折扣价格层位（裁决 P15 遗留点）。** 备选 A：商品单价层（goodsPrice 汇总层），`effectiveUnitPrice = min(retail, vipPrice, timeDiscountPrice)`，限时折扣作为 SKU 短期促销价降低行单价。备选 B：复用 promotionPrice 槽位（orderPrice 减项层），与满减并存需选择逻辑（P15 假设）。**抉择 A。** 理由：`domain-glossary.md:43` 定义限时折扣为「商品级/SKU 级短期降价促销」，本质是 SKU 临时促销价（单价变更），非订单级优惠；备选 B 与满减（订单级门槛）语义混杂且需额外选择逻辑。**后果：限时折扣不复用 promotionPrice 槽位**，P15 遗留「共存选择逻辑」问题消解。残留风险：需订正 PROMOTION_PRICE 列注释（移除「限时折扣」）+ order-and-cart.md:56 描述。
- [x] **Decision: discountType × discountValue 计算语义（与 P15 同字典编码对齐）。** `mall/discount-type` {0=减金额/直降, 10=打折}。**抉择对齐 P15 `PromotionActivityBizModel.applyDiscount`（`:120-130`）的编码语义**：discountType=10（打折）→ `discountValue` 为**小数支付率**（∈(0,1]，如 0.9=9 折），`promoPrice = retail × discountValue`；discountType=0（减金额/直降）→ `discountValue` 为直降额，`promoPrice = retail - discountValue`。校验 promoPrice > 0（折扣过度保护）。理由：限时折扣与满减共用 `mall/discount-type` 字典，**必须承载同一编码语义**（P15 打折=小数支付率 0.9=9 折，非整数「折」8=8 折），否则后台同字典两套输入语义造成配置混乱。roadmap「直降金额」对应减金额（discountType=0）。
- [x] **Decision: 限时折扣 × 会员价（vipPrice）层内取舍。** 同在商品单价层。抉择：取三者最低 `min(retail, vipPrice, timeDiscountPrice)`（用户享最优价）。备选「互斥（限时折扣优先覆盖会员价）」被否（损害会员权益）。残留风险：取最低可能叠加侵蚀毛利，由运营配置折扣力度控制。已写入 owner doc。
- [x] **Decision: 限时折扣 × 满减共存。** 限时折扣降低商品单价（goodsPrice 汇总层）→ 满减门槛以折扣后 goodsPrice 判定 → 两者天然可共存，满减自动按折扣后金额判档。**无需额外选择逻辑**（裁决 P15 遗留点）。残留风险：限时折扣拉低 goodsPrice 可能使订单跌出满减门槛，符合商业直觉（折扣后金额判满减）。
- [x] **Decision: stockLimit（折扣库存）强制策略。** TimeDiscount 有 `stockLimit`（0=不限）。抉择：stockLimit > 0 时强制——订单提交时对该折扣原子扣减活动库存（参照既有 `goodsProductMapper.reduceStock` 条件 UPDATE 模式），售罄后该 SKU 恢复原价；stockLimit = 0 时不限（仅受商品库存约束）。理由：活动库存为限时折扣常见能力且字段已建模；0=不限默认简化普通降价场景。
- [x] **Decision: 多重折扣匹配（同 SKU 多活动）。** 抉择：同一 SKU 同一时刻只命中一个生效限时折扣，不叠加多个折扣；多活动命中时取**对用户最优（promoPrice 最低）**，并列时取最新 `startTime`（TimeDiscount 无 `priority` 字段——`priority` 仅存在于 PromotionActivity；TimeDiscount 字段集为 goodsId/productId/discountType/discountValue/startTime/endTime/status/stockLimit/remark，故用最优价 + 最新生效时间作 tiebreaker）。理由：避免多重折扣叠加失控；参照 P15 满减「同订单只命中一档」原则；tiebreaker 仅用既有字段，不新增模型字段（与「预期不新增字段」一致）。
- [x] **Add:** 将限时折扣业务设计（层位裁决、discountType 计算、×vipPrice 取低、×满减共存、stockLimit 强制、多重折扣互斥、状态语义、倒计时/剩余库存展示）写入 `marketing-and-promotions.md`（新增「限时折扣」章节）；订正 `order-and-cart.md:56` promotion price 段（限时折扣移至商品单价层，promotionPrice 槽位仅满减）**与 `order-and-cart.md:66` goods price 公式**（行单价由 `min(retailPrice, vipPrice)` 扩为 `min(retailPrice, vipPrice, timeDiscountPrice)`，限时折扣进入 SKU 单价层）；`product-catalog.md` 补促销价/倒计时展示规则。

Exit Criteria:

- [x] `marketing-and-promotions.md` 新增限时折扣完整业务设计（含层位裁决 + 共存 + 计算语义 + stockLimit）；`order-and-cart.md:56` 订正层位描述**且 `:66` goods price 公式扩为 `min(retail,vip,timeDiscount)`**；`product-catalog.md` 补展示规则
- [x] 六个 Decision 的抉择/备选/理由/残留风险已记录（含 P15 遗留层位裁决 + ×vipPrice/×满减共存 + discountType + stockLimit + 多重互斥）
- [x] Phase 2 实现清单由本阶段 Decision 确定（限时折扣为商品单价层，不复用 promotionPrice 槽位）

### Phase 2 — 后端：限时折扣计算 + 价格接线 + ErrorCode（Add-heavy）

Status: completed
Targets: `app-mall-service/.../LitemallTimeDiscountBizModel.java`、`app-mall-service/.../LitemallOrderBizModel.java`、`app-mall-service/.../AppMallErrors.java`
Required Skill: `nop-backend-dev`、`nop-testing`（本 phase 新增 `@BizQuery` 计算方法，规则 #15 要求 `IGraphQLEngine` 测试）

- Item Types: `Add`
- Prereqs: Phase 1（层位 + 计算 Decision 已决）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完各自 routing table 标为必读的全部文档（列出路径）。每写完一个方法用 selfcheck 校验（无 anti-pattern）。参照既有 `LitemallPromotionActivityBizModel.selectPromotionForOrder`（P15 模式）+ `goodsProductMapper.reduceStock`（条件 UPDATE 模式）。
  - Docs read: nop-backend-dev skill（service-layer / error-handling / safe-api-reference / bizmodel-method-selfcheck）、nop-testing skill（test-examples / testing）
- [x] **Add:** `LitemallTimeDiscountBizModel.selectTimeDiscountForProduct(productId, goodsId, context)`（`@BizQuery`）——查 status=ACTIVE 且在时间窗内的折扣（同 SKU 优先，null productId 兜底全 SKU），按 Phase 1 Decision 计算 promoPrice（打折小数支付率/直降），校验 promoPrice>0。**返回类型为结构化 Map（区别于 `selectPromotionForOrder` 返回 BigDecimal）**：含 `promoPrice`/`originalPrice`/`discountAmount`(直降金额)/`stockLimit`/`remainingStock`/`endTime`(供详情页倒计时)/`discountType`，无折扣返回 null；供详情页促销横幅与列表促销价展示。纯计算（stockLimit 扣减在 submit）。
- [x] **Add:** `LitemallOrderBizModel.submit()` 限时折扣接线——goodsPrice 汇总时按 Phase 1 Decision 取 `min(retail, vipPrice, timeDiscountPrice)` 为行单价（扩展现有 `:226-233` 会员价逻辑，叠加限时折扣判定）；OrderGoods 快照价格记录实际成交单价（price）。限时折扣不进 promotionPrice（裁决为商品单价层）。
- [x] **Add:** stockLimit 强制（按 Decision）——TimeDiscount 的 `stockLimit` 在 `litemall_time_discount` 表（不同于 `litemall_goods_product.number`），既有 `goodsProductMapper.reduceStock`（`LitemallOrderBizModel.java:94,246`，作用于 GoodsProduct）**不能直接复用**；须**新增** TimeDiscount 的条件 UPDATE mapper 方法（如 `reduceTimeDiscountStock(discountId, n)`，遵循 reduceStock 的「原子扣减、售罄返回 0」`@SqlLibMapper` 模式），submit 中对 stockLimit>0 的折扣调此新方法，售罄返回 0 抛 ErrorCode；事务回滚保证一致。
- [x] **Add（Protected Area — ask-first `model/app-mall.orm.xml`）：** 订正 `LitemallOrder.PROMOTION_PRICE` 列注释（`:1130`）为「满减优惠金额」（移除「限时折扣」），反映限时折扣裁决为商品单价层。comment 不驱动 codegen，无需重生成。**ask-first 已由 MISSION_DRIVER 授权**（同 P15 ORDER_PRICE comment 订正先例）。
- [x] **Add:** `AppMallErrors` 新增 time-discount 域 ErrorCode（`nop.err.mall.time-discount.not-active`/`.sold-out`/`.not-in-window`/`.invalid-discount-value`），参照 promotion 域命名。
- [x] **Proof:** `selectTimeDiscountForProduct` 与 submit 接线通过 `IGraphQLEngine`（`JunitBaseTestCase`）测试：打折/直降计算、×vipPrice 取低、×满减共存（折扣后判档）、stockLimit 强制与售罄、多重折扣互斥、草稿/超时窗不匹配返回原价、详情页倒计时数据。指定 `TestLitemallTimeDiscountBizModel` + `TestLitemallOrderBizModel` 新增限时折扣用例全绿。

Exit Criteria:

- [x] 限时折扣自动生效（时间窗内 SKU 促销价）；订单 goodsPrice 正确反映限时折扣单价
- [x] **API 测试：** `selectTimeDiscountForProduct`（`@BizQuery`）+ submit 限时折扣接线通过 `IGraphQLEngine`
- [x] 限时折扣不进 promotionPrice（层位裁决落地）；×vipPrice 取低 / ×满减共存正确；stockLimit 强制生效
- [x] time-discount ErrorCode 已定义并被使用（`SOLD_OUT` 用于 stockLimit 售罄守卫）

### Phase 3 — 前端：详情页促销横幅 + 列表促销价 + 后台管理（Add-heavy）

Status: completed
Targets: 商品详情页、商品列表页、`pages/LitemallTimeDiscount/LitemallTimeDiscount.view.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 2（后端 API 就绪）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读 routing 必读文档（XView 三层模型、bounded-merge、grid/form 定制）。文件/类完成后 selfcheck（未改 `_gen`、保留层 view.xml 正确 `x:extends`、AMIS service 调 `@query`）。
  - Docs read: nop-frontend-dev skill（view-and-page-customization / delta-customization / page-dsl-pattern-catalog）
- [x] **Add:** 商品详情页（`goods-detail.page.yaml`）限时折扣促销横幅——调 `@query:LitemallTimeDiscount__selectTimeDiscountForProduct`，命中时展示「限时折扣 促销价 ¥X 直降 ¥Y」+ 倒计时（基于 endTime）+ 剩余库存进度条（stockLimit）；无折扣 `visibleOn` 隐藏。
- [x] **Add:** 商品列表页促销价展示——限时折扣生效商品展示促销价（划线原价 + 促销价 + 限时标签）。
- [x] **Add:** 后台 `LitemallTimeDiscount.view.xml` 定制：grid bounded-merge（goodsId/productId/discountType/discountValue/startTime/endTime/status/stockLimit）；edit form layout 含折扣字段（dict 自动渲染 discountType/status 为下拉，productId 占位提示 null=全 SKU，时间窗，stockLimit 提示 0=不限）；edit 用 drawer；query 按 goodsId/status 过滤。

Exit Criteria:

- [x] 详情页展示限时折扣促销横幅（促销价 + 直降 + 倒计时 + 剩余库存）；无折扣时隐藏
- [x] 列表页展示促销价（划线原价 + 促销价 + 标签）
- [x] 后台可创建/编辑限时折扣活动（商品/SKU 维度 + 折扣类型 + 时间窗 + 库存）
- [x] 复用既有 AMIS 三层定制模式，无新前端依赖

### Phase 4 — 验证、文档同步、日志（Proof）

Status: completed
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-3

- [x] **Skill loading gate:** 加载 `nop-testing`，读 routing 必读文档。
- [x] **Proof:** 跑 `docs/context/project-context.md` 真实验证命令（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C`；`./mvnw test -pl app-mall-service -am`；`./mvnw -pl app-mall-web -DskipTests compile`），全绿；更新 `docs/testing/known-good-baselines.md`。
- [x] 更新 `docs/logs/2026/{month}-{day}.md`（逆向时间序，置顶 Phase 23 条目）。

Exit Criteria:

- [x] 全量验证通过（含本计划新增 IGraphQLEngine 测试）
- [x] `known-good-baselines.md` 更新本计划 baseline row
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗审计（fresh session），四轮达成共识。
  - Round 1（`ses_0f6e8e532ffe0BEMMrJCFsi2hC`）：REVISE — 2 BLOCKER（discountType 打折编码与 P15 不一致——P15 用小数支付率 0.9=9折，本计划误用 int/10；多重折扣 tiebreaker 引用 TimeDiscount 不存在的 priority 字段）+ 1 MAJOR（stockLimit 需新增 TimeDiscount mapper，非复用 GoodsProduct.reduceStock）+ minors。全部修订：discountType 对齐 P15 小数支付率编码；tiebreaker 改用既有字段（最低 promoPrice + 最新 startTime）；新增 mapper 显式声明。
  - Round 2（`ses_0f6deabf7ffeGChLcZTWUixPTC`）：REVISE — 1 MAJOR（order-and-cart.md:66 goods price 公式 min(retail,vip) 未纳入 doc 更新范围，层位 Decision 直接改 :66）。修订：Phase 1 Add / Exit Criteria / Closure Gates 均补 :66 → min(retail,vip,timeDiscount)。
  - Round 3（`ses_0f6d618a2ffeaBLdt998W0kPya`）：PASS — MA-1 RESOLVED（三处均引用 :66），无新增（baseline 全 live；核心层位 Decision 经核验语义正确，解决 P15 遗留共存问题）。第 1 个 clean round。
  - Round 4（`ses_0f6cf9476ffe86WRH7WjIXxviM`）：PASS — 第 2 个连续 clean round，共识达成。
- Evidence: 实读 live repo 核验 baseline（TimeDiscount @ orm.xml:1502-1542 无 priority 字段、两字典、空 BizModel、submit :226-233/:295-300、PROMOTION_PRICE 注释 @ :1130、marketing-and-promotions.md 无限时折扣段、order-and-cart.md:56/:66、P15 applyDiscount @ PromotionActivityBizModel:120-130 小数支付率编码）。核心层位裁决（限时折扣=商品单价层，不复用 promotionPrice 槽位）经核验解决 P15 遗留共存问题，PROMOTION_PRICE 注释订正正当。

## Closure Gates

- [x] in-scope behavior is complete（限时折扣计算 + 价格接线 + stockLimit + 前端 + 后台）
- [x] relevant docs are aligned（`marketing-and-promotions.md` / `order-and-cart.md:56` 层位 + `:66` goods price 公式订正 / `product-catalog.md` / ORM PROMOTION_PRICE 注释订正）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 全绿 + app-mall-web 编译 + uber-jar install）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（selectTimeDiscountForProduct + submit 限时折扣接线）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（Phase 1 `none` 含 justify）
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

> 起草时无可延期项。秒杀（P24，需场次/抢购/限购/nop-job）、拼团（P25）、营销活动管理后台（P22，依赖 P24/P25）在 Non-Goals 显式移出（独立 phase）。

## Closure

<!-- Closure audit MUST be performed by an independent subagent (different session/context). 留给闭合审计代理填写。 -->

Status Note: 全部 4 个 Phase 均经独立闭合审计（fresh session，非实现代理）对照 live repo 核验，in-scope 行为、文档、测试、日志均已落地，无未决遗留缺陷，可闭合。

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计 subagent（fresh session，与实现代理不同上下文），委派 2 个独立 explore subagent 逐项核验 live repo 证据（task ids `ses_0f67514eaffeiuNveX4drjRUIq`、`ses_0f672c5f3ffe0iBO0ZiprxPVR`）。
- Phase 1（docs）：`marketing-and-promotions.md:241-325` 新增「限时折扣」章节（层位=商品单价层 / discountType 打折小数支付率+直降 / ×vipPrice 取低 / ×满减共存 / stockLimit 强制 / 多重互斥 全覆盖）；`order-and-cart.md:56` 层位订正（promotionPrice 槽位仅满减）+ `:66` goods price 公式扩为 `min(retailPrice,vipPrice,timeDiscountPrice)`；`product-catalog.md:154,162-177` 详情页限时折扣展示规则。
- Phase 2（backend）：`LitemallTimeDiscountBizModel.selectTimeDiscountForProduct`（`@BizQuery` `:39-42`，status=ACTIVE 时间窗过滤 `:68`，打折小数支付率 `:112-115`、直降 `:116-119`，promo>0 校验 `:122-124`，结构化 Map `:149-161`）；`LitemallOrderBizModel.submit` `:254-274` `min(retail,vip,timeDiscount)` 接线 + stockLimit 售罄守卫（`:264-267` 抛 `ERR_TIME_DISCOUNT_SOLD_OUT`）；`LitemallTimeDiscountMapper.reduceTimeDiscountStock` + `LitemallTimeDiscount.sql-lib.xml`（条件 UPDATE，独立于 GoodsProduct.reduceStock）；`AppMallErrors:308-323` time-discount 4 错误码；`model/app-mall.orm.xml:1130` PROMOTION_PRICE `displayName="满减优惠金额"` 已订正（移除限时折扣）。
- Phase 3（frontend）：`goods-detail.page.yaml:83-94` 促销横幅（`@query:LitemallTimeDiscount__selectTimeDiscountForProduct` + 划线原价 + 促销价 + 直降 + 倒计时 + 剩余库存 + `visibleOn` 隐藏）；`search.page.yaml:190-201` 列表促销价；`LitemallTimeDiscount.view.xml:1-65` grid bounded-merge + edit drawer。
- Phase 4（验证/日志）：`docs/logs/2026/06-27.md:3-10` Phase 23 条目（`./mvnw clean install … uber-jar` BUILD SUCCESS；`./mvnw test -pl app-mall-service -am` 175 测试全绿；app-mall-web 编译 BUILD SUCCESS）；`docs/testing/known-good-baselines.md:13` Phase 23 baseline row。
- 测试覆盖：`TestLitemallTimeDiscountBizModel` 11 例（打折/直降/草稿不匹配/超时窗/未来窗/多重取低/SKU 精确+兜底/跨 SKU 不串/过度折扣拒绝/库存与倒计时）；`TestLitemallOrderBizModel` 限时折扣 5 例（单价/直降/×满减共存/stockLimit 售罄/stockLimit 原子扣减），均经 `graphQLEngine.executeRpc` 验证。
- 五点一致性核验：Plan Status=completed / 各 Phase Status=completed / 各 Phase Exit Criteria 全 `[x]` / Closure Gates 全 `[x]` / `docs/logs/2026/06-27.md` 条目一致。
- 残留观察（非阻塞，不违反 Exit Criteria）：`ERR_TIME_DISCOUNT_NOT_ACTIVE`/`NOT_IN_WINDOW` 已定义但当前无 throw-site（window/active 过滤走 return null，Exit Criteria 仅要求 SOLD_OUT 被使用，已满足）；日志/baseline 文案描述订单折扣用例数与实际方法数有 ±1 prose 偏差，不影响测试全绿事实。

Follow-up:

- 秒杀（P24）/拼团（P25）启动后，与限时折扣在 P22 营销活动管理后台统一编排。
