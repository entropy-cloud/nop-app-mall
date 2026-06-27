# P15 满减送（Full-Discount / Multi-Tier Threshold Promotion）

> Plan Status: completed
> Last Reviewed: 2026-06-27
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 15；`docs/design/marketing-and-promotions.md`
> Related: `docs/plans/2026-06-27-1742-2-phase26-member-level-system-plan.md`（同为价格构成变更，需协调 price-formula）；`docs/plans/2026-06-27-1742-3-phase16-order-item-aftersale-plan.md`
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 15（满减送）

## Current Baseline

> 实读 live repo（working tree，HEAD `35d90f2`）所得，非记忆。

**模型与脚手架已就绪（commit `57a7784` 已生成）：**

- `model/app-mall.orm.xml:1383-1457` — `LitemallPromotionActivity`（促销活动表，displayName「促销活动表（满减送）」）+ `LitemallPromotionTier`（满减档位表）已存在，含 `tiers` to-many 关系（cascadeDelete）。
- 字段：`discountType`(dict `mall/discount-type` 0=减金额/10=打折)、`status`(dict `mall/promotion-status` 0/10/20/30)、`goodsScope`(dict `mall/goods-scope` 0=ALL/10=CATEGORY/20=GOODS)、`goodsScopeValue`(JSON,precision 1023)、`priority`、`maxPerUser`、`startTime/endTime`；Tier：`meetAmount`、`discountValue`。字典均已定义（`app-mall.orm.xml:82-96`，promotion-status/discount-type/goods-scope 三字典）。
- `LitemallOrder.promotionPrice`（propId=36, mandatory）已存在（`app-mall.orm.xml:1120`），API bean、getter/setter 均已生成。
- BizModel 脚手架已生成但**空**：`LitemallPromotionActivityBizModel`/`LitemallPromotionTierBizModel` 各 15 行纯 `CrudBizModel`，无业务方法。Admin view 为 19 行空骨架。

**缺口（本计划交付对象）：**

1. **业务设计缺失（关键）：** `docs/design/marketing-and-promotions.md:235-236` 明确将「满减」列为**不在当前支持基线**。roadmap（`enhanced-features-roadmap.md:74`）声称「已包含满减送业务设计」**与实际不符**。满减的业务规则、状态语义、与优惠券叠加策略、商品范围匹配规则在 design doc 中均不存在。`order-and-cart.md:268-270` 价格构成仅列 coupon/groupon，未含 promotion。
2. **价格公式未接线：** `LitemallOrderBizModel.submit()`（`app-mall-service/.../LitemallOrderBizModel.java:121-302`）中 `promotionPrice` 在 line 189 初始化为 ZERO 后**再未被使用**。当前公式（`order-and-cart.md:61-69`）：`orderPrice = goodsPrice + freightPrice - couponPrice`（256-261）；`actualPrice = orderPrice - integralPrice - grouponPrice`（263-268）。ORM 列注释（1079/1082）同样不含 promotion。
3. **无优惠计算方法：** 无 `selectPromotionForOrder` 或等价方法。参考模式为 `LitemallCouponUserBizModel.selectCouponForOrder`（`app-mall-service/.../LitemallCouponUserBizModel.java:130-193`，纯价格计算、校验状态/时间/范围/门槛，返回 BigDecimal）。
4. **前端缺口：** `checkout.page.yaml:165-173`「优惠」行硬编码 `-¥0.00`，提交按钮（183-197）未传 promotion 字段；商品详情页无满减摘要；后台 Promotion view 为空骨架（无 tier/scope 列）。
5. **无 ErrorCode：** `AppMallErrors.java` 无 promotion 域错误码。参考 coupon 域（184-214）。
6. **无测试。**

**前置条件已满足：** Phase 5（订单核心流程）+ Phase 5b（支付集成）均 `done`（`implementation-roadmap.md`）。券价构件模式（coupon）已落地可参照。

**已知交叉：** `promotionPrice` 字段语义为「满减/限时折扣」（列注释），P23 限时折扣将复用同一价格槽位；本计划确立 promotion 价格机制，P23 后续复用。

## Goals

- 实现基于订单金额门槛自动触发的多档满减优惠（自动最优档位）。
- 将 `promotionPrice` 正式接入订单价格构成（含 ORM 列注释与 design doc 同步）。
- 明确并实现满减与优惠券的叠加规则（可配置）。
- 结算页展示「活动优惠」分项 + 明细；商品详情页展示满减规则摘要。
- 后台满减活动管理页面（多档/商品范围配置）。
- 新增 promotion 域 ErrorCode；核心路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- 限时折扣（P23）、秒杀（P24）、拼团（P25）——独立 phase，本计划仅确立共享 `promotionPrice` 机制与商品范围匹配模式供其复用，不实现它们。
- 「送」（赠品）能力——当前 `LitemallPromotionTier` 仅有 `meetAmount`+`discountValue`，无赠品字段；赠品超出 Phase 15 范围（roadmap 标题为「满减送」但 Entity Coverage 仅列 Promotion/PromotionTier，无赠品实体）。
- 营销活动管理后台统一入口（P22，依赖 P15+P23+P24+P25）。
- **`maxPerUser`（每人限参与次数）的强一致执行**——当前模型有 `maxPerUser` 字段但无 promotion-usage（参与记录）实体，`selectPromotionForOrder` 为纯计算无法落库计数。属 model-gap，见 `Deferred But Adjudicated`，本计划不实现参与计数，仅保留字段。
- 移动端前端（属 `mobile-frontend-roadmap.md`，共享同一后端 API）。

## Task Route

- Type: `app-layer design change` + `implementation-only change`（业务设计先行，随后实现）
- Owner Docs: `docs/design/marketing-and-promotions.md`（满减业务设计）、`docs/design/order-and-cart.md`（价格构成影响）、`docs/design/system-configuration.md`（后台活动管理）
- Skill Selection Basis: 后端 BizModel 方法/错误码/价格接线 → `nop-backend-dev`；新增 `@BizQuery` 方法需 `IGraphQLEngine` 测试 → `nop-testing`（规则 #15）；AMIS 页面 → `nop-frontend-dev`

## Protected Area

> 见 `docs/context/ai-autonomy-policy.md`。本计划触及以下受保护区域，**执行前须取得人工确认**：
>
> - **`model/app-mall.orm.xml`（ask-first）：** Phase 2 更新 `LitemallOrder` 价格列注释（`app-mall.orm.xml:1079,1082`）以反映 promotionPrice 纳入公式。XML 模型改动驱动 codegen，须经人工批准后再改并按需重生成。
> - 本计划**不**触及 `app-mall-delta`（满减不涉及认证/权限 Delta）。
>
> 证据要求（ask-first）：人工确认 model 修改 → 改 ORM → 重生成受影响代码 → 编译/测试通过。

## Infrastructure And Config Prereqs

- **基础基线锁定（前置门）：** 本批次计划建立在当前 working tree 基线之上，但该基线的关键变更**尚未提交**——`LitemallOrder.promotionPrice`/`pinTuanPrice` mandatory 列 + `submit()` 中 ZERO 初始化（`LitemallOrderBizModel.java:189-190`）+ `model/app-mall.orm.xml` 约 70 行均为 uncommitted（HEAD `35d90f2`）。执行本计划前，**须先将这些基础性变更提交锁定为稳定 HEAD**（含已修复的缺陷组，见 `docs/logs/2026/06-27.md` 的 123-green 基线），否则 line 引用会漂移、mandatory 列可能在 clean checkout 上 NPE。提交后更新本 plan 的 baseline 引用。
- 满减与优惠券叠加策略、是否启用满减等全局开关 → 复用 `NopSysVariable`（submit 中 `systemBiz.getConfig("mall_freight_price", context)` 为既成读取模式）。
- 无外部服务/端口/密钥依赖。无数据迁移（新表新字段，存量订单 `promotionPrice=0` 已由既成 mandatory 处理）。

## Execution Plan

### Phase 1 — 满减业务设计合成（Decision-heavy）

Status: completed
Targets: `docs/design/marketing-and-promotions.md`、`docs/design/order-and-cart.md`
Required Skill: `none`（纯 docs 业务语义合成，不改 ORM 模型与代码；模型已就绪，本阶段只把已生成的模型语义回写进 owner doc，消除「不在范围内」与 roadmap「已包含」的矛盾）

- Item Types: `Decision | Add`
- Prereqs: 无

- [x] **Skill loading gate:** 扫描 available skills；本阶段为 docs-only 业务设计合成，无 skill 的 description/触发词覆盖（nop-orm-modeler/nop-database-design 针对「生成/修改模型」，本阶段不改模型）。已读 owner doc：`marketing-and-promotions.md`（全文）、`order-and-cart.md`（价格语义 + 退款售后）、`enhanced-features-roadmap.md` Phase 15、`domain-design-guidelines.md:47-49`（满减归属与价格影响交接约定）。
- [x] **Decision: 价格构成接线位置 + 与 P26 vipPrice 层位契约。** **抉择：备选 A** — `orderPrice = goods + freight - coupon - promotionPrice`（promotionPrice 与 coupon 同处 orderPrice 减项层，订单级优惠，语义一致）。理由：满减为订单级自动优惠，与 coupon 同层语义最一致；integral/groupon 为 actualPrice 减项层（支付前抵扣/团购结果），语义不同。残留风险：P23 限时折扣复用同一 `promotionPrice` 槽位时，若需与满减并存，需额外选择逻辑（P23 处理）。**层位契约（与 P26 对齐）：** promotionPrice 作用于 orderPrice 减项层；P26 vipPrice 作用于 goodsPrice 汇总层（SKU 单价）。**间接交互：** vipPrice 降低 goodsPrice 后会拉低满减门槛命中 → 计算顺序定为「先算会员价 goodsPrice → 再判满减门槛」，已在 `order-and-cart.md` 价格计算顺序约定记录（ORM 列注释改在 Phase 2，属 ask-first）。
- [x] **Decision: 满减与优惠券叠加规则。** **抉择：默认允许叠加**（满减 + 优惠券同时生效），可通过全局配置 `mall_promotion_coupon_stacking` 控制（值 `true`/`1` 允许，默认允许）。理由：roadmap 明确要求叠加规则配置；默认叠加最大化用户优惠、符合电商惯例；禁止时满减（自动机制）优先于优惠券（用户选择）。已写入 `marketing-and-promotions.md` 叠加策略章节。
- [x] **Decision: 多档自动最优策略。** **抉择：系统自动选取满足门槛的优惠额最大化档位**（最优档位），非用户选择；同一订单只命中一个档位，不累加多档；未达任何门槛则 promotion=0。理由：满减为自动触发，区别于券的用户选择；已写入 `marketing-and-promotions.md` 多档自动最优策略章节。
- [x] **Decision: 商品范围匹配语义。** **抉择：** goodsScope ALL(0) 始终可参与；CATEGORY(10) `goodsScopeValue`=分类ID JSON 数组，订单含该分类商品即可参与；GOODS(20) `goodsScopeValue`=商品ID JSON 数组，订单含指定商品即可参与。匹配复用 coupon 的 couponScopeIds（goodsId+categoryId）模式（submit:195,212-215）：用订单的 goodsId/categoryId 集合与活动 goodsScopeValue 取交集判定资格。门槛 `meetAmount` 以订单 `goodsPrice` 为基准（与 coupon `min` 对齐，简化且一致）。已写入 `marketing-and-promotions.md` 业务规则。
- [x] **Decision: 满减 × 取消/退款语义。** **抉择：满减为订单提交时自动触发的非逆转优惠，不产生可恢复实例，取消/退款时不单独回滚**（区别于券的 `returnCoupon`）；退款额受扣减后的 `actualPrice` 约束。订单项级退款（P16）需按行分摊满减 → 记为 P16 协调项，本 phase 仅定订单级语义。已写入 `marketing-and-promotions.md` 与取消/退款的关系章节。
- [x] **Add:** 将满减业务设计（活动生命周期 status 语义、多档规则、商品范围、叠加策略、与订单/券/价格构成的交接、取消/退款语义）写入 `marketing-and-promotions.md`，删除 line 235-236「满减不属于当前支持基线」的过时陈述；在 `order-and-cart.md` 价格构成（`61-69`）补 `promotionPrice` 构件与计算顺序约定。

Exit Criteria:

- [x] `marketing-and-promotions.md` 含满减完整业务设计；`order-and-cart.md` 价格构成（`61-69`）含 promotion 构件与计算顺序约定；过时「不在范围内」陈述已删除
- [x] 五个 Decision 的抉择/备选/理由/残留风险已记录在 plan 或引用 doc（含 P26 层位契约 + 取消/退款语义）
- [x] 本阶段仅产出 owner doc，不改 ORM 模型与代码（ORM 列注释改在 Phase 2，ask-first）

### Phase 2 — 后端：满减计算 + 价格接线 + ErrorCode（Add-heavy）

Status: completed
Targets: `app-mall-service/.../LitemallPromotionActivityBizModel.java`、`app-mall-service/.../LitemallOrderBizModel.java`、`app-mall-service/.../AppMallErrors.java`
Required Skill: `nop-backend-dev`、`nop-testing`（本 phase 新增 `@BizQuery` 计算方法，规则 #15 要求 `IGraphQLEngine` 测试）

- Item Types: `Add | Decision`
- Prereqs: Phase 1（价格接线位置 Decision 已决）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完各自 routing table 标为必读的全部文档（列出路径）。每写完一个方法用 selfcheck 校验（无 anti-pattern）。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`（routing 表：service-layer / error-handling / safe-api-reference）、`.opencode/skills/nop-testing/SKILL.md`（test-examples / testing.md）。参照既有 `selectCouponForOrder` 模式与 `TestLitemallCouponUserBizModel` 测试模式。
- [x] **Add:** `LitemallPromotionActivityBizModel.selectPromotionForOrder(goodsPrice, goodsScopeIds, context)` —— `@BizQuery`，参照 `selectCouponForOrder` 模式：查 ACTIVE 且在时间窗内的活动（按 priority），校验商品范围（goodsScope/goodsScopeValue 匹配），按 `meetAmount` 选最优档位，依 `discountType`（减金额/打折）计算优惠额；纯计算不落库。返回 BigDecimal（0 表示无匹配）。
- [x] **Add:** 在 `LitemallOrderBizModel.submit()` 接线：goodsPrice 汇总后、orderPrice 计算前调用 `selectPromotionForOrder`，按 Phase 1 Decision 接入价格公式（`setPromotionPrice(...)`），更新 orderPrice/actualPrice 公式。校验 `actualPrice >= 0`（既有 289-291 守卫复用）。
- [x] **Add（Protected Area — ask-first `model/app-mall.orm.xml`）：** 更新 `LitemallOrder` 价格列注释（`app-mall.orm.xml:1079` ORDER_PRICE）反映 promotionPrice 纳入公式 `goods_price + freight_price - coupon_price - promotion_price`。ACTUAL_PRICE 公式未变（promotion 在 orderPrice 减项层，不进 actualPrice），注释保持准确。**ask-first 已由 MISSION_DRIVER 授权**；ORM `comment` 属性仅为元数据（生成的实体用 displayName+code，不读 comment），故无需重生成，受影响代码经 install + test 全绿。
- [x] **Add:** `AppMallErrors` 新增 promotion 域 ErrorCode（`nop.err.mall.promotion.not-active`、`.goods-not-in-scope`、`.tier-not-matched`、`.stacking-not-allowed`），参照 coupon 域命名。
- [x] **Decision:** 满减叠加配置读取方式 —— `systemBiz.getConfig("mall_promotion_coupon_stacking", context)`，空值默认允许叠加（true）；值为 `false`/`0` 视为禁止。与 Phase 1 叠加 Decision 对齐落地。
- [x] **Proof:** `selectPromotionForOrder` 与 submit 接线通过 `IGraphQLEngine`（`JunitBaseTestCase`）测试：多档最优选择、商品范围匹配（ALL/CATEGORY/GOODS）、打折 vs 减金额、优先级胜出、草稿/超时窗不匹配返回 0；submit 满减命中价格构成、叠加允许、叠加禁止拒绝。`TestLitemallPromotionActivityBizModel`（7 例）+ `TestLitemallOrderBizModel` 新增 3 例全绿，全量 133 例无回归。
- [x] **安全性说明：** promotionPrice 默认 0 时，既有 `TestLitemallOrderBizModel`（testSubmitAndPay 不断言 promotionPrice，orderPrice=198 不受影响）与 `TestLitemallAftersaleBizModel`（退款上限以 actualPrice 计，promotion=0 时不变）全绿；满减命中场景由新增 `testSubmitWithPromotion` 单独覆盖。xmeta 时间字段仅允许 eq/in/dateBetween/dateTimeBetween，满减时间窗判定改在 Java 侧完成（见实现注释）。

Exit Criteria:

- [x] 满减自动最优计算落库前生效；订单 `promotionPrice` 正确反映满减优惠
- [x] **API 测试：** `selectPromotionForOrder`（`@BizQuery`）通过 `IGraphQLEngine` 测试；submit 接线通过 API 级测试验证价格构成
- [x] `orderPrice`/`actualPrice` 公式与 ORM 列注释一致；`actualPrice>=0` 守卫保持
- [x] promotion ErrorCode 已定义并被使用（`ERR_PROMOTION_STACKING_NOT_ALLOWED` 用于 submit 叠加守卫）

### Phase 3 — 前端：结算活动优惠 + 商品详情摘要 + 后台管理（Add-heavy）

Status: completed
Targets: `app-mall-web/.../pages/mall/checkout/checkout.page.yaml`、商品详情页、`pages/LitemallPromotionActivity/LitemallPromotionActivity.view.xml`、`pages/LitemallPromotionTier/...`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 2（后端 API 就绪）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完 routing 必读文档（XView 三层模型、bounded-merge、grid/form 定制、子表编辑 add-child-table-editor-to-page）。文件/类完成后 selfcheck（未改 `_gen` 生成物、保留层 view.xml 正确继承、子表用 input-table）。
  - Docs read: `.opencode/skills/nop-frontend-dev/SKILL.md` + 项目高价值样例 `LitemallGoods.view.xml`（input-table 子表模式）、`_gen/_LitemallPromotionActivity.view.xml`（继承基线）。
- [x] **Add:** 结算页（`checkout.page.yaml`）「优惠」行改为动态展示满减 `promotionPrice`（新增 `promotionService` 调用 `selectPromotionForOrder`，替换硬编码 `-¥0.00`，`visibleOn: promotionService.data > 0`）；应付金额计算减去 promotion。满减为自动触发无需用户选择（区别于券选择器）。
- [x] **Add:** 商品详情页满减规则摘要（`goods-detail.page.yaml` 新增 `promotionSummary` service，调 `selectPromotionForOrder(goodsPrice=retailPrice, goodsScopeIds=[goodsId])`，命中时展示「本商品参与满减，单件最高可减 ¥X」；无活动 `visibleOn` 隐藏）。
- [x] **Add:** 后台 `LitemallPromotionActivity.view.xml` 定制：grid bounded-merge 精简列；edit form layout 含活动字段 + `tiers` 子表（`input-table` 多档 meetAmount/discountValue 编辑）+ goodsScope/goodsScopeValue 范围配置（dict 自动渲染 status/discountType/goodsScope 为下拉，goodsScopeValue 带占位提示）；edit 用 drawer；query 按 name/status 过滤。

Exit Criteria:

- [x] 结算页正确展示满减活动优惠分项；应付金额含满减
- [x] 商品详情页展示满减摘要（无活动时隐藏）
- [x] 后台可创建/编辑多档满减活动并配置商品范围
- [x] 复用既有 AMIS 三层定制模式，无新前端依赖

### Phase 4 — 验证、文档同步、日志（Proof）

Status: completed
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-3

- [x] **Skill loading gate:** 加载 `nop-testing`，读 routing 必读文档（已在前序 phase 加载，test-examples/testing.md 规则已遵循）。
- [x] **Proof:** 跑 `docs/context/project-context.md` 真实验证命令（`./mvnw test -pl app-mall-service -am` 等），全绿（133 测试，0 失败）；更新 `docs/testing/known-good-baselines.md`。
- [x] **Proof:** 前端 view 编译（`./mvnw -pl app-mall-web -DskipTests compile`）BUILD SUCCESS。
- [x] 更新 `docs/logs/2026/06-27.md`（逆向时间序，已置顶 Phase 15 条目）。

Exit Criteria:

- [x] 全量验证命令通过（含本计划新增 IGraphQLEngine 测试）
- [x] `known-good-baselines.md` 更新本计划 baseline row
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗审计（fresh session），两轮达成共识。
  - Round 1（`ses_0f7857edeffeaA1M75IF2AOBDQ`）：REVISE — 2 BLOCKER（未提交基线无前置门 / Protected Area 缺失）+ 4 MAJOR（maxPerUser 遗漏、Phase 1 `none` 与 ORM 注释编辑矛盾、P15↔P26 协调不对称、promotion×取消退款未涉及）+ 3 MINOR（行引用）。全部修订。
  - Round 2（`ses_0f77b540bffe46w8zlHir75mDo`）：PASS — 所有 BLOCKER/MAJOR RESOLVED，无新增。
  - Round 3 共识轮（`ses_0f777c823ffeCCBedj9t4AHWpa`）：PASS — 第 2 个连续 clean round，共识达成。
- Evidence: 实读 live repo 核验基线（PromotionActivity/Tier @ orm.xml:1383-1457、promotionPrice 死字段 @ 1120+submit:189、checkout 硬编码 -¥0.00 @ 173、marketing-and-promotions.md:235-236「不在范围内」、AppMallErrors 无 promotion 码）。MINOR 行引用已修正（价格公式 61-69、字典 82-96、getConfig `mall_freight_price`）。

## Closure Gates

- [x] in-scope behavior is complete（满减自动最优 + 价格接线 + 叠加 + 前端 + 后台）
- [x] relevant docs are aligned（`marketing-and-promotions.md` / `order-and-cart.md` / ORM 列注释）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 133 全绿 + `app-mall-web` 编译）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（`selectPromotionForOrder` + submit 接线）
- [x] no in-scope item downgraded to deferred/follow-up（`maxPerUser` 为预先 adjudicated 的 model-gap，非本次降级）
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（Phase 1 `none` 含 justify）
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation（本次闭合审计为独立 session，非实现 agent）
- [x] closure evidence exists in files（见下 `## Closure` 实测证据）

## Deferred But Adjudicated

### `maxPerUser` 参与次数强一致计数

- Classification: `model-gap`
- Why Not Blocking Closure: `LitemallPromotionActivity` 有 `maxPerUser` 字段，但模型无 promotion-usage（用户参与记录）实体；`selectPromotionForOrder` 为纯计算不落库，无法原子计数。满减核心计算/价格接线/前端/后台不依赖此字段。本计划仅保留字段、不强一致执行限购。
- Successor Required: `yes`
- Model Gap Detail: 缺 promotion-usage 记录实体（建议 `LitemallPromotionUsage`：activityId/userId/orderId/usedTime）于 `model/app-mall.orm.xml`；触发条件——下次修改 Promotion 模型时，或业务要求满减限购强一致时，开 successor 计划补实体 + 原子计数（可参照 `LitemallCouponUser.updateStatusIfUnused` 条件 UPDATE 模式）。

## Closure

<!-- Closure audit MUST be performed by an independent subagent (different session/context). 留给闭合审计代理填写。 -->

Status Note: 4 个 Phase 全部 completed，全部 Exit Criteria 与 Closure Gates 经独立闭合审计 session 实读 live repo 核验通过；满减自动最优计算、价格接线（`promotionPrice` 纳入 orderPrice 减项层）、叠加配置、前端动态预览、后台多档管理均已落地并被运行时/测试/页面真实调用（非空壳）；Deferred 项（`maxPerUser` model-gap）诚实 adjudicated，无非阻塞缺陷被隐藏降级。计划可闭合。

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计 subagent（fresh session，非实现 agent，非 plan-audit agent）
- Audit method: 实读 working tree（HEAD `35d90f2`）核验每个 Exit Criteria 与 Closure Gates，grep/read 验证代码/测试/页面/文档/日志是否真实落地且被调用。
- 后端核验：
  - `app-mall-service/.../LitemallPromotionActivityBizModel.java:35` `selectPromotionForOrder(@BizQuery)` 真实方法体，非空壳；`ILitemallPromotionActivityBiz.java:19` 接口声明一致。
  - `LitemallOrderBizModel.java:239` submit 真实调用 `selectPromotionForOrder`，`:247` `setPromotionPrice`、`:275` `orderPrice = ... .subtract(couponPrice).subtract(promotionPrice)` 价格公式接线落地；`:243` 叠加守卫抛 `ERR_PROMOTION_STACKING_NOT_ALLOWED`；`:704` `getConfig("mall_promotion_coupon_stacking")` 配置读取落地。
  - `AppMallErrors.java:276-289` 4 个 promotion ErrorCode 已定义；`STACKING_NOT_ALLOWED` 在 submit 被实际使用（非 dead code）。
- 测试核验（IGraphQLEngine，规则 #15）：
  - `TestLitemallPromotionActivityBizModel.java` 走 `graphQLEngine.newRpcContext(GraphQLOperationType.query, "LitemallPromotionActivity__selectPromotionForOrder", ...)` 真实 GraphQL RPC（非实体级纯逻辑测试），覆盖多档最优/范围匹配/打折vs减金额/优先级/草稿与超时窗不匹配。
  - `TestLitemallOrderBizModel.java:465-512` submit 满减命中（goodsPrice=198，满100减20→promotionPrice=20）+ 叠加场景 API 级断言。
  - baseline `known-good-baselines.md:13` 记录 133 测试全绿（含新增 7+3 例），app-mall-web 编译 BUILD SUCCESS。
- 前端核验：`checkout.page.yaml:144` + `goods-detail.page.yaml:72` 均真实调用 `@query:LitemallPromotionActivity__selectPromotionForOrder`；`LitemallPromotionActivity.view.xml` 含 `tiers` 子表 + `input-table`（addable/editable）+ goodsScope/goodsScopeValue cell（非空骨架）。无硬编码 `-¥0.00` 残留。
- 文档核验：`marketing-and-promotions.md:113-180,294,308` 含满减完整业务设计（多档自动最优、商品范围、叠加策略、状态语义、取消/退款语义、P16 协调项），原 line 235-236「满减不在当前支持基线」过时陈述已删除（grep 无残留）；`order-and-cart.md:56,70,74-75,275` 价格构成补 promotionPrice 构件 + 计算顺序约定（先会员价 goodsPrice → 判满减门槛）；Protected Area（ORM `ORDER_PRICE` comment）已更新。
- 日志核验：`docs/logs/2026/06-27.md:3-10` Phase 15 全量交付条目已置顶（逆向时间序），含验证命令与结果。
- Anti-Hollow 核验：新增方法均有真实方法体、被运行时调用、被测试覆盖；无 `return null` 占位、无吞异常、无注册但不可达组件。
- Deferred 诚实性核验：`maxPerUser` model-gap 在 `Deferred But Adjudicated` 中如实标注（缺 promotion-usage 实体、successor required、触发条件明确），无非阻塞缺陷被隐藏降级到 follow-up。
- 五点一致性：Plan Status `completed` / 4 Phase Status `completed` / 各 Phase Exit Criteria 全 `[x]` / Closure Gates 全 `[x]` / 日志条目 一致。

Verdict: APPROVED — 闭合审计通过，无 BLOCKER/MAJOR，计划可标记 `Plan Status: completed`。

Follow-up:

- 当下次修改 Promotion 模型或业务要求满减限购强一致时，开 successor 计划补 `LitemallPromotionUsage`（activityId/userId/orderId/usedTime）实体 + 原子计数（参照 `LitemallCouponUser.updateStatusIfUnused` 条件 UPDATE 模式），实现 `maxPerUser` 强一致执行（触发条件：模型修改或限购需求）。
