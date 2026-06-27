# P24 秒杀（Flash Sale / Seckill）

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 24；`docs/design/marketing-and-promotions.md`、`docs/design/product-catalog.md`、`docs/design/order-and-cart.md`
> Related: `docs/plans/2026-06-27-2029-2-phase23-time-discount-plan.md`（P23 限时折扣已 done，确立 `mall/promotion-status` 字典 + 原子库存扣减 mapper 模式供本计划复用）；`docs/plans/2026-06-28-0125-2-phase25-pin-tuan-plan.md`（同批次 N=2，拼团）；下游贡献 P22 营销活动管理后台（依赖 P24 完成其「秒杀场次配置」交付项）
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 24（秒杀）

> **执行顺序：** 本计划为 2026-06-28-0125 批次第 1 顺位（N=1）。P24 秒杀优先于 P25 拼团（N=2）：秒杀确立「场次模型 + 并发原子库存扣减 + nop-job 场次状态切换 + 不走购物车直接购买」基础模式，P25 拼团可复用其并发库存/job 调度结论。本计划与 P25 均可能触及 `LitemallOrderBizModel`，但作用点不同（秒杀为独立 `flashSaleBuy` 下单路径或商品单价层，拼团为 actualPrice 减项层 `pinTuanPrice` 接线），须按 N=1→N=2 顺序执行并在各自计划重基线化 submit() 行引用（见 Non-Goals 与协调约定）。

## Current Baseline

> 实读 live repo（working tree）所得，非记忆。

**模型已就绪（脚手架已生成）：**

- `LitemallFlashSale`（秒杀活动表，`app-mall.orm.xml:1543-1586`）：`goodsId`(商品ID,mandatory)、`productId`(SKU ID,null=全部SKU)、`flashPrice`(秒杀价,mandatory decimal)、`totalStock`(活动总库存)、`maxPerUser`(每人限购)、`maxPerOrder`(每单限购)、`status`(dict `mall/promotion-status` DRAFT0/ACTIVE10/FINISHED20/CLOSED30，与 P15/P23 共用)；`goods` to-one、`sessions` to-many(cascadeDelete)。
- `LitemallFlashSaleSession`（秒杀场次表，`app-mall.orm.xml:1587-1621`）：`flashSaleId`(mandatory)、`sessionStart`/`sessionEnd`(mandatory 时间窗)、`sessionStock`(场次库存)、`sessionStatus`(0=未开始/1=进行中/2=已结束，**无字典，纯整数注释语义**)；`flashSale` to-one。
- 复用字典：`mall/promotion-status`（P15 已定义，ACTIVE=10 表示「进行中」）。
- `LitemallOrder`：**无 `flashSaleId`/`flashSaleSessionId` 关联字段**（关键缺口，影响 `maxPerUser` 限购与 P22 报表，见 Phase 1 Decision）。

**BizModel 脚手架已生成但空：**

- `LitemallFlashSaleBizModel`（15 行纯 `CrudBizModel` 空骨架，已确认无业务方法）、`LitemallFlashSaleSessionBizModel`（同空骨架）。Admin view 为空骨架。

**订单价格公式现状（关键交叉点）：**

- `LitemallOrderBizModel.submit()`：`pinTuanPrice` 在 `:232` 初始化为 ZERO 后**再未被使用**（死字段）；`grouponPrice` 在 `:315-335` 计算并 `:354` 从 actualPrice 减去。秒杀若走独立 `flashSaleBuy` 路径则**不进** submit() 价格公式（见 Phase 1 Decision）。
- `orderPrice = goodsPrice + freight - coupon - promotionPrice`（P15 满减在 orderPrice 减项层）；`actualPrice = orderPrice - integralPrice - grouponPrice`（`:354`）。秒杀价为商品单价层（直接以 flashPrice 为成交单价），不与 promotionPrice/coupon 槽位冲突。

**并发库存扣减参考模式（已落地）：**

- P23 `LitemallTimeDiscountMapper.reduceTimeDiscountStock` + `LitemallTimeDiscount.sql-lib.xml`（条件 UPDATE，「原子扣减、售罄返回 0」`@SqlLibMapper` 模式）——秒杀 `sessionStock` 原子扣减直接参照此模式新增 FlashSaleSession mapper。
- `goodsProductMapper.reduceStock`（`LitemallOrderBizModel.java` 既有，作用于 GoodsProduct 商品库存）——秒杀下单仍须扣减商品库存，复用此既有方法。

**定时任务调度（nop-job-local 已引入）：**

- `MallJobInvoker`（`app-mall-service/.../scheduler/MallJobInvoker.java`）已有 `cancelExpiredOrders`/`expireGroupons`/`expireCoupons` 等方法模式（`new ServiceContextImpl()` + 调 BizModel 方法 + LOG）。秒杀场次状态切换将新增 `switchFlashSaleSessions()` 方法至此处，复用同一调度注册机制。

**业务设计缺失（关键）：**

- `marketing-and-promotions.md:595` 明确「秒杀 / seckill 不属于当前支持基线」（已读全文标题确认无「秒杀」章节）。roadmap（`enhanced-features-roadmap.md:74`）声称「已包含秒杀业务设计」与实际不符——同 P15/P23 文档缺口模式，Phase 1 须先合成业务设计。`domain-glossary.md:42` 定义「秒杀 = 短期、限量、低价的强促销形式，按场次组织，不走购物车」。

**无 ErrorCode：** `AppMallErrors.java` 无 flash-sale 域错误码（参照 promotion/time-discount 域）。

**前置条件已满足：** P5（订单核心流程）+ P5b（支付集成）均 `done`。P23 限时折扣 `done`（原子库存扣减 mapper + promotion-status 字典复用模式可参照）。

## Goals

- 实现按场次组织的限量低价秒杀：场次时间窗内可抢购，含倒计时与抢购进度（已售/总库存）。
- 秒杀不走购物车直接购买（独立下单路径），秒杀订单不支持优惠券。
- 并发安全的场次库存原子扣减（售罄即止）+ 商品库存扣减。
- 限购执行（`maxPerOrder` 每单限购；`maxPerUser` 每人限购，依据 Phase 1 Decision 决定执行路径）。
- 场次状态自动切换（未开始→进行中→已结束）经 nop-job 调度。
- 详情/列表页秒杀横幅（秒杀价 + 倒计时 + 抢购进度条）+ 秒杀活动列表（即将开始/进行中/已结束）。
- 后台秒杀活动管理页面（活动 + 场次子表）。
- 新增 flash-sale 域 ErrorCode；核心路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- 拼团（P25，同批次 N=2，独立 phase，需开团/参团/成团/超时失败退款）。
- 营销活动管理后台统一入口（P22，依赖 P15+P23+P24+P25，本计划仅交付秒杀可被 P22 编排的后端能力）。
- 秒杀 × 满减/限时折扣/会员价叠加：秒杀为「独立直接购买路径 + 秒杀价为成交单价」，不走购物车、不支持券，**默认不与其他促销叠加**（裁决见 Phase 1 Decision）；本计划不实现叠加选择逻辑。
- 移动端前端（独立 roadmap `mobile-frontend-roadmap.md`，共享同一后端 API）。
- **与 P25 拼团的 submit() 协调约定：** 若 Phase 1 Decision 抉择秒杀为独立 `flashSaleBuy` 路径（推荐），则本计划不改 `submit()`，与 P25（改 submit() 接线 pinTuanPrice）无冲突；若抉择扩展 submit()，则须与 P25 按 N=1→N=2 顺序执行并重基线化行引用。Phase 1 将裁决此点。

## Task Route

- Type: `app-layer design change` + `implementation-only change`（业务设计先行——裁决下单路径/限购/场次状态切换/并发库存/促销叠加，随后实现；模型已就绪无需模型准备 phase）
- Owner Docs: `docs/design/marketing-and-promotions.md`（秒杀业务规则/状态/场次/限购/不走购物车）、`docs/design/product-catalog.md`（秒杀横幅/倒计时/进度展示）、`docs/design/order-and-cart.md`（秒杀下单路径与价格层位交接）
- Skill Selection Basis: 后端 BizModel 方法/错误码/并发库存/job → `nop-backend-dev`；新增 `@BizQuery`/`@BizMutation` 方法需 `IGraphQLEngine` 测试 → `nop-testing`（规则 #15）；AMIS 页面 → `nop-frontend-dev`

## Protected Area

> 见 `docs/context/ai-autonomy-policy.md`。本计划触及以下受保护区域，**执行前须取得人工确认**：
>
> - **`model/app-mall.orm.xml`（ask-first，条件性）：** 仅当 Phase 1 Decision 抉择「Alt A：新增 `LitemallOrder.flashSaleSessionId`（nullable）关联列」以强一致执行 `maxPerUser` 限购时触及。该改动驱动 codegen（新增 Order getter/setter/bean），须经人工批准后改 ORM 并重生成受影响代码。若抉择 Alt B（`maxPerUser` 延后为 model-gap，仅执行 `maxPerOrder`），则**不触及** ORM 模型。本计划**不**触及 `app-mall-delta`（秒杀不涉及认证/权限 Delta）。
> - 证据要求：ask-first → 人工确认 →（条件性）改 ORM + 重生成 → 编译/测试通过。

## Infrastructure And Config Prereqs

- **基础基线锁定（前置门）：** 本批次计划建立在当前 working tree 基线（P15/P23/P26/P27/P28/P32/P33/P16/P34 已 done 并提交的稳定 HEAD）之上。执行前须确认 HEAD 为已审计的稳定基线（非中间态），否则行引用会漂移。
- 秒杀全局开关、场次状态切换频率等可配项 → `NopSysVariable`（既有模式）。建议 key：`mall_flash_sale_enabled`（全局开关，默认 true）。
- 场次状态切换依赖 nop-job-local（已引入，`MallJobInvoker` 注册机制）；新增 `switchFlashSaleSessions()` 方法并接入既有调度配置（参照 `expireGroupons` 接线）。
- 无外部服务/端口/密钥依赖。无数据迁移（新表已存在；条件性 Order 新增列若被否决则无 DDL 变更）。

## Execution Plan

### Phase 1 — 秒杀业务设计合成 + 关键裁决（Decision-heavy）

Status: completed
Targets: `docs/design/marketing-and-promotions.md`、`docs/design/order-and-cart.md`、`docs/design/product-catalog.md`
Required Skill: `none`（纯 docs 业务语义合成，不改 ORM 模型与代码；模型已就绪。无 skill 的 description/触发词覆盖「写设计文档」。Phase 2 起的 Nop 实现 phase 才写非 `none` 的 Required Skill——本 phase justify 见 Minimum Rules #14）

- Item Types: `Decision | Add`
- Prereqs: 无（P23 已 done，promotion-status 字典 + 原子库存 mapper 模式上下文已具备）

- [x] **Skill loading gate:** 扫描 available skills；本阶段 docs-only，无匹配。读 owner doc：`marketing-and-promotions.md`（全文，含团购/限时折扣段为参照）、`order-and-cart.md`（价格构成 + 下单流程）、`product-catalog.md`（促销展示）、`domain-glossary.md:42`（秒杀定义）、P23 plan（原子库存 + promotion-status 复用先例）。
  - Docs read: `docs/design/marketing-and-promotions.md`、`docs/design/order-and-cart.md`、`docs/design/product-catalog.md`、`docs/design/domain-glossary.md`、`docs/plans/2026-06-27-2029-2-phase23-time-discount-plan.md`、`docs/context/project-context.md`、`docs/context/ai-autonomy-policy.md`
- [x] **Decision: 秒杀下单路径（核心裁决）。** 备选 A：独立 `flashSaleBuy()` `@BizMutation`（不走购物车，单 SKU、数量在 `maxPerOrder` 内、秒杀价为成交单价、跳过券选择）。备选 B：扩展 `submit()` 增加 optional `flashSaleSessionId` 参数并分支（复用下单机器但污染 submit() 并与 P25 耦合）。**抉择 A。** 理由：roadmap 明确「秒杀不走购物车直接购买」「秒杀订单不支持优惠券」——独立路径最干净表达此语义，且与 P25 拼团的 submit() 改动解耦（无跨计划耦合）。残留风险：独立路径重复部分下单步骤（地址/运单号生成/商品库存扣减）——通过复用既有 helper（`goodsProductMapper.reduceStock`、订单号生成）控制，订单创建步骤较 cart submit 简单（单行、已知地址）。
- [x] **Decision: 限购（`maxPerUser`/`maxPerOrder`）执行路径。** `maxPerOrder`（每单限购）：在 `flashSaleBuy` 中以请求数量上限校验，**无需模型改动**。`maxPerUser`（每人限购）：需按用户累计计数其对该场次的秒杀成交——备选 Alt A（新增 `LitemallOrder.flashSaleSessionId` nullable 关联列，ask-first ORM）可强一致查询计数 + 服务 P22 报表；备选 Alt B（`maxPerUser` 延后为 model-gap，仅执行 `maxPerOrder` + 场次库存原子扣减，参照 P15 `maxPerUser` 先例）。**抉择：** 采纳 **Alt B**（执行环境无 ORM Protected Area 人工批准，按计划既定 fallback 执行；限购为运营增强项，秒杀「限量」已由场次库存原子扣减保证）。`maxPerUser` 强一致由 successor 计划在拿到 ORM 批准后落地关联列实现，触发条件：限购强一致需求或 P22 秒杀效果报表。残留风险：本计划交付的 `maxPerUser` 字段在后台仍可配置，但服务端不强制执行（仅校验 `maxPerOrder`），运营需理解该字段当前为「展示 + 后台配置就绪、执行待 successor」状态。
- [x] **Decision: 场次状态（`sessionStatus` 0/1/2）切换机制。** 备选 A：nop-job 定时扫描切换（新增 `MallJobInvoker.switchFlashSaleSessions()`，按 `sessionStart`/`sessionEnd` 翻转 0→1→2，参照 `expireGroupons` 模式）。备选 B：读取时实时计算（不持久化翻转）。**抉择 A。** 理由：roadmap 明确「秒杀场次状态切换」依赖 nop-job；持久化状态使列表查询/前台展示/库存守卫一致（进行中才允许扣库存），避免每次读取重复计算时间窗。残留风险：job 频率与场次边界的精度差（可接受，秒级误差不影响业务正确性）。
- [x] **Decision: 并发库存扣减语义 + `totalStock`/`sessionStock` 关系。** `sessionStock`（场次库存，**权威扣减单位**）通过新增 `LitemallFlashSaleSessionMapper.reduceFlashSaleSessionStock(sessionId, n)` 条件 UPDATE 原子扣减（参照 P23 `reduceTimeDiscountStock`：`UPDATE ... SET session_stock=session_stock-n WHERE id=? AND session_stock>=n`，affectedRows=0 视为售罄抛 ErrorCode），事务回滚保证一致；同时复用既有 `goodsProductMapper.reduceStock` 扣减商品库存。`sessionStock=0`(或 null) 语义**抉择 null/0=不限**（与 P23 stockLimit=0 语义一致）。`LitemallFlashSale.totalStock`（活动总库存）与 `sessionStock`（场次库存）关系**抉择：totalStock 为非规范化元数据（≈ 各 sessionStock 之和，由后台配置/维护，仅展示用），不作为跨场次硬上限原子扣减**——理由：秒杀抢购的并发竞争发生在单场次内，跨场次硬上限会引入分布式计数复杂度且无业务必要；单场次 sessionStock 原子扣减已保证并发安全。取消/退款库存回流语义**抉择：秒杀场次库存不随订单取消回流**（继承 P23 限时折扣先例——促销库存为活动资源，取消回流会破坏「限量」语义并引发超卖风险），写入 owner doc。
- [x] **Decision: 秒杀 × 其他促销/价格叠加。** 秒杀为独立直接购买路径，秒杀价（flashPrice）即成交单价（商品单价层），**默认不与满减/限时折扣/会员价/优惠券叠加**（不走购物车 + 不支持券）。理由：秒杀本质为「限量低价强促销」，叠加会侵蚀毛利且违背「不支持优惠券」要求；用户享秒杀价即最优。残留风险：会员价（vipPrice）不叠加可能引发会员感知问题——裁决秒杀价优于会员价（秒杀场景例外），写入 owner doc。
- [x] **Add:** 将秒杀业务设计（场次生命周期 sessionStatus 语义、不走购物车直接购买、限购规则、并发库存语义、不叠加策略、与订单/价格构成的交接、场次状态切换 job）写入 `marketing-and-promotions.md`（新增「秒杀」章节），删除 line 595「秒杀不属于当前支持基线」过时陈述；在 `order-and-cart.md` 补秒杀下单路径与价格层位交接（秒杀价为成交单价，不进 promotionPrice/coupon 槽位）；`product-catalog.md` 补秒杀横幅/倒计时/抢购进度展示规则。

Exit Criteria:

- [x] `marketing-and-promotions.md` 新增秒杀完整业务设计（含下单路径裁决 + 限购 + 场次状态 + 并发库存 + 不叠加 + sessionStatus 语义）；`order-and-cart.md` 补秒杀下单路径/价格层位交接；`product-catalog.md` 补展示规则；line 595 过时陈述已删除
- [x] 五个 Decision 的抉择/备选/理由/残留风险已记录（含下单路径 + 限购执行路径 + 场次状态切换 + 并发库存 + 不叠加）
- [x] Phase 2 实现清单由本阶段 Decision 确定（含限购 Alt A/B 分支条件、独立 flashSaleBuy 路径）

### Phase 2 — 后端：秒杀下单 + 场次状态切换 + 限购 + ErrorCode（Add-heavy）

Status: completed
Targets: `app-mall-service/.../LitemallFlashSaleBizModel.java`、`app-mall-service/.../LitemallFlashSaleSessionBizModel.java`、`app-mall-service/.../scheduler/MallJobInvoker.java`、`app-mall-dao/.../mapper/LitemallFlashSaleSessionMapper.java` + `LitemallFlashSaleSession.sql-lib.xml`、`app-mall-service/.../AppMallErrors.java`、`app-mall-service/.../LitemallOrderBizModel.java`（createFlashSaleOrder 委托方法）

Required Skill: `nop-backend-dev`、`nop-testing`（本 phase 新增 `@BizMutation` flashSaleBuy 与 `@BizQuery` 列表/详情方法，规则 #15 要求 `IGraphQLEngine` 测试）

- Item Types: `Add | Decision`
- Prereqs: Phase 1（下单路径 + 限购 + 场次状态 + 并发库存 Decision 已决）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完各自 routing table 标为必读的全部文档。每写完一个方法用 selfcheck 校验。参照 P23 `reduceTimeDiscountStock` + Groupon `expireGroupons` 模式。
  - Docs read: nop-backend-dev skill（service-layer / error-handling / safe-api-reference）、nop-testing skill（test-examples / testing）
- [x] **Add:** `LitemallFlashSaleSessionMapper.reduceFlashSaleSessionStock(sessionId, n)` + `LitemallFlashSaleSession.sql-lib.xml` 条件 UPDATE（参照 `LitemallTimeDiscount.sql-lib.xml`，affectedRows=0 返回售罄信号）。Mapper bean 注册于 `app-dao.beans.xml`。
- [x] **Add:** `LitemallFlashSaleBizModel.flashSaleBuy(@Name flashSaleSessionId, @Name addressId, @Name number, context)`（`@BizMutation`，按 Phase 1 Decision A 独立路径）：校验场次 sessionStatus=进行中 且在时间窗内、activity.status=ACTIVE、商品在售；`maxPerOrder` 数量上限校验；原子扣减 `sessionStock`（售罄抛 ErrorCode）+ 复用 `goodsProductMapper.reduceStock` 扣商品库存；委托 `orderBiz.createFlashSaleOrder(...)` 以 flashPrice 为成交单价创建订单（单行 OrderGoods、不走 cart、不挂券 couponPrice=0）。`@BizMutation` 自动事务包裹。
- [x] **Add:** `LitemallFlashSaleBizModel` 列表/详情 `@BizQuery` 方法：`activeFlashSales(page,pageSize,context)`（按 sessionStatus 分组返回即将开始/进行中/已结束 + 倒计时 endTime + 抢购进度 soldStock/totalStock）；`flashSaleDetail(id,context)`（含场次列表 + 进度）；`flashSaleForGoods(goodsId,productId,context)`（供详情页秒杀横幅）。供前台秒杀活动列表与详情。
- [x] **Add:** `LitemallFlashSaleSessionBizModel.switchFlashSaleSessions()`：扫描所有场次按 `sessionStart`/`sessionEnd` 翻转 sessionStatus 0→1→2；`MallJobInvoker.switchFlashSaleSessions()` 包装调用并注册到 `scheduler.yaml`（repeatInterval=60000ms）。
- [x] **Add（条件性 Protected Area — ask-first `model/app-mall.orm.xml`）：** Phase 1 抉择 **Alt B**（不触及 ORM）：`maxPerUser` 延后为 model-gap，本计划仅执行 `maxPerOrder` + 场次库存原子扣减。无需 ORM 重生成。`maxPerUser` 强一致由 successor 计划落地关联列后实现。
- [x] **Add:** `AppMallErrors` 新增 flash-sale 域 ErrorCode（`nop.err.mall.flash-sale.not-active`/`.session-not-in-window`/`.sold-out`/`.over-limit-per-order`/`.over-limit-per-user`/`.goods-off-shelf`/`.goods-product-not-found`/`.product-not-in-activity`/`.session-not-found`），参照 time-discount/promotion 域命名。
- [x] **Proof:** `flashSaleBuy` + 列表/详情方法通过 `IGraphQLEngine`（`JunitBaseTestCase`）测试：场次进行中验证、未开始/已结束拒绝、售罄原子扣减失败、`maxPerOrder` 超限拒绝、草稿活动拒绝、商品下架拒绝、不限库存（sessionStock=0）验证、列表三态分组、详情含场次、flashSaleForGoods 横幅、job 翻转场次状态。指定 `TestLitemallFlashSaleBizModel` 14 例全绿。

Exit Criteria:

- [x] 秒杀场次进行中验证通过（原子扣场次库存 + 商品库存逻辑就绪），售罄/未开始/已结束/超限被拒；秒杀价为成交单价、不挂券
- [x] **API 测试：** `flashSaleBuy`（`@BizMutation`）+ 列表/详情（`@BizQuery`）通过 `IGraphQLEngine` 测试；场次状态切换 job 经 `LitemallFlashSaleSession__switchFlashSaleSessions` GraphQL mutation 验证
- [x] flash-sale ErrorCode 已定义并被使用（`SOLD_OUT`/`OVER_LIMIT_PER_ORDER` 为守卫 throw-site）
- [x] 限购执行路径落地（Alt B：maxPerOrder + model-gap 记录于 Deferred）

### Phase 3 — 前端：秒杀列表 + 详情横幅 + 后台管理（Add-heavy）

Status: completed
Targets: 秒杀活动列表页（`mall/flash-sale/flash-sale-list.page.yaml`）、商品详情页秒杀横幅（`mall/goods/goods-detail.page.yaml`）、`pages/LitemallFlashSale/LitemallFlashSale.view.xml`、`pages/LitemallFlashSaleSession/LitemallFlashSaleSession.view.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 2（后端 API 就绪）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读 routing 必读文档（XView 三层模型、bounded-merge、grid/form 定制、子表编辑 input-table）。文件完成后 selfcheck（未改 `_gen`、保留层 view.xml 正确 `x:extends`、AMIS service 调 `@query`/`@mutation`）。
  - Docs read: nop-frontend-dev skill（view-and-page-customization / delta-customization / page-dsl-pattern-catalog）
- [x] **Add:** 秒杀活动列表页（`mall/flash-sale/flash-sale-list.page.yaml`）——调 `@query:LitemallFlashSale__activeFlashSales`，按 sessionStatus 分区展示（即将开始/进行中/已结束）+ 倒计时（基于 sessionEnd）+ 抢购进度条（sessionStock）+ 进行中商品「立即抢购」入口（调 `@mutation:LitemallFlashSale__flashSaleBuy`）。
- [x] **Add:** 商品详情页秒杀横幅（`goods-detail.page.yaml`）——调 `@query:LitemallFlashSale__flashSaleForGoods`，命中时展示「秒杀价 ¥X 倒计时 抢购进度」+ 直接抢购按钮（不走加购）；无秒杀 `visibleOn` 隐藏。
- [x] **Add:** 后台 `LitemallFlashSale.view.xml` 定制：grid bounded-merge（goodsId/flashPrice/totalStock/maxPerUser/maxPerOrder/status）；edit form layout 含活动字段 + dict 自动渲染 status；edit 用 drawer；query 按 goodsId/status 过滤。`LitemallFlashSaleSession.view.xml` 同样定制（场次开始/结束时间/库存/状态）。

Exit Criteria:

- [x] 秒杀活动列表展示三态分区 + 倒计时 + 抢购进度 + 抢购入口；商品详情页秒杀横幅（无秒杀时隐藏）
- [x] 后台可创建/编辑秒杀活动（活动字段 + 限购 + 库存 + 状态）；场次管理页同样可编辑
- [x] 复用既有 AMIS 三层定制模式，无新前端依赖

### Phase 4 — 验证、文档同步、日志（Proof）

Status: completed
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-3

- [x] **Skill loading gate:** 加载 `nop-testing`，读 routing 必读文档。
- [x] **Proof:** 跑 `docs/context/project-context.md` 真实验证命令（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C`；`./mvnw test -pl app-mall-service -am`；`./mvnw -pl app-mall-web -DskipTests compile`），全绿；更新 `docs/testing/known-good-baselines.md`。
- [x] **Add:** 订正 `docs/backlog/enhanced-features-roadmap.md` Phase 24 行 Platform Reuse 列「nop-job（需引入…）」为「nop-job-local（已引入，Phase 11）」——消除文档漂移。
- [x] 更新 `docs/logs/2026/06-28.md`（逆向时间序，置顶 Phase 24 条目）。

Exit Criteria:

- [x] 全量验证通过（含本计划新增 IGraphQLEngine 测试）
- [x] `known-good-baselines.md` 更新本计划 baseline row
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: MISSION_DRIVER 执行（同会话执行 + 验证；独立闭合审计待 fresh session subagent）
- Evidence: 实读 live repo 核验 baseline（FlashSale/FlashSaleSession 模型已就绪、空 BizModel 脚手架、MallJobInvoker 模式、P23 reduceTimeDiscountStock 先例、promotion-status 字典复用）。Phase 1 五 Decision 全部裁决并写入 owner doc。Phase 2 后端实现 214 测试全绿。Phase 3 前端三页面交付。Phase 4 全量验证 BUILD SUCCESS。

## Closure Gates

- [x] in-scope behavior is complete（秒杀下单 + 场次状态切换 + 并发库存 + 限购 + 前端 + 后台）
- [x] relevant docs are aligned（`marketing-and-promotions.md` 秒杀章节 / `order-and-cart.md` 秒杀独立下单路径 / `product-catalog.md` 秒杀横幅与列表；条件性 ORM flashSaleSessionId 列未触及——Alt B 抉择）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 全绿 + app-mall-web 编译 + uber-jar install）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（flashSaleBuy + activeFlashSales + flashSaleDetail + flashSaleForGoods）；场次状态切换经 `IGraphQLEngine`（`LitemallFlashSaleSession__switchFlashSaleSessions` mutation）验证
- [x] no in-scope item downgraded to deferred/follow-up（`maxPerUser` 为 Phase 1 预先 adjudicated 的 model-gap Alt B 抉择，非 closure 时降级）
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（Phase 1 `none` 含 justify；Phase 2/3/4 非 `none`）
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

> Phase 1 限购 Decision 抉择 **Alt B**（不触及 ORM ask-first），`maxPerUser` 延后为 model-gap：
> - **缺口：** 缺 `LitemallOrder.flashSaleSessionId`（nullable）关联列，无法按用户累计计数其对某场次的秒杀成交订单数。
> - **本计划交付：** `maxPerOrder`（每单限购）在 `flashSaleBuy` 中以请求数量上限校验。`maxPerUser` 字段在模型/后台/owner doc 中保留（可配置），但服务端不强制执行。
> - **Successor：** 待 ORM ask-first 人工批准后，新增关联列（驱动 codegen）→ `flashSaleBuy` 写入该列 → maxPerUser 按 `(userId, flashSaleSessionId)` 计数查询校验。同时服务 P22 秒杀效果报表（按场次/商品维度聚合秒杀订单）。
> - **触发条件：** 限购强一致需求出现（如黄牛刷秒杀），或 P22 营销活动管理后台启动秒杀效果分析。
> - **参照先例：** P15 满减 `maxPerUser` 同样的 model-gap 先例（`marketing-and-promotions.md` 满减章节「已知约束」）。

## Closure

Status Note: 全部 4 个 Phase 均已执行完成。`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` BUILD SUCCESS；`./mvnw test -pl app-mall-service -am` 214 测试全绿；`./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS。`maxPerUser` model-gap 已记录于 Deferred But Adjudicated（Alt B，不触及 ORM ask-first）。独立闭合审计由独立 subagent 执行。

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计 subagent（fresh session，非实现 agent）。任务入口：MISSION_DRIVER closure-audit step，SCRIPT_CHECK_RESULT=FAIL（11 unchecked Closure Gates 项）→ 修复路径。
- Audit Method: 实读 live repo working tree 全量核验，非信任 plan 自述。逐项核验 11 个 Closure Gates 与各 Phase Exit Criteria 对应代码/文档/测试是否真落地。
- Evidence (live repo verified):
  - 后端核心：`LitemallFlashSaleBizModel.flashSaleBuy`（@BizMutation，`:104-260`，含场次/活动/商品/maxPerOrder 守卫 + 原子扣 sessionStock + 扣商品库存 + 委托 `orderBiz.createFlashSaleOrder`）；`activeFlashSales`/`flashSaleDetail`/`flashSaleForGoods`（@BizQuery，`:263-360`）。
  - 场次状态切换：`LitemallFlashSaleSessionBizModel.switchFlashSaleSessions`（@BizMutation，`:31-57`）+ `MallJobInvoker.switchFlashSaleSessions`（`:70-73`）+ `scheduler.yaml:58-67` 注册 `switch-flash-sale-sessions` job（repeatInterval=60000ms）。
  - 原子库存扣减：`LitemallFlashSaleSessionMapper.reduceFlashSaleSessionStock` + `LitemallFlashSaleSession.sql-lib.xml` 条件 UPDATE（`sessionStock >= num` 守卫，售罄 affectedRows=0）。
  - 订单委托：`LitemallOrderBizModel.createFlashSaleOrder`（`:799-866`，单行 OrderGoods + flashPrice 成交单价 + 全 ZERO 价格槽）。
  - ErrorCode：`AppMallErrors.java:334-368` 定义 9 个 flash-sale 域错误码（not-active/session-not-in-window/sold-out/over-limit-per-order/over-limit-per-user/goods-off-shelf/goods-product-not-found/product-not-in-activity/session-not-found）。
  - 测试：`TestLitemallFlashSaleBizModel.java`（503 行）14 个 `@Test` 方法全经 `IGraphQLEngine`（JunitBaseTestCase，localDb=true），覆盖进行中/未开始/已结束/售罄/超限/草稿/下架/不限库存/列表三态/详情/横幅/job 翻转/订单持久化。
  - 前端：`flash-sale-list.page.yaml`（三态分区 + 抢购按钮调 `@mutation:LitemallFlashSale__flashSaleBuy`）；`goods-detail.page.yaml:71-117` 秒杀横幅（调 `@query:LitemallFlashSale__flashSaleForGoods`，无秒杀时 `visibleOn` 隐藏）；`LitemallFlashSale.view.xml` + `LitemallFlashSaleSession.view.xml` 后台 bounded-merge 定制。
  - 文档：`marketing-and-promotions.md:594-664` 秒杀完整章节（5 Decision + 下单路径/限购/场次状态/并发库存/不叠加语义）；`order-and-cart.md:79-84` 秒杀独立下单路径交接；`product-catalog.md:179-204` 秒杀横幅与列表展示规则；line 595 过时陈述已删除。
  - 日志：`docs/logs/2026/06-28.md` Phase 24 条目（含 5 Decision 摘要 + maxPerUser model-gap + H2 测试已知约束说明）。
  - 验证基线：`docs/testing/known-good-baselines.md:13` Phase 24 row 记录 214 测试全绿 + uber-jar install + web 编译 BUILD SUCCESS。
  - 路线图：`enhanced-features-roadmap.md:29` Phase 24 状态 `done`；Platform Reuse 列订正为 `nop-job-local（已引入，Phase 11）`。
- maxPerUser model-gap 复核：Phase 1 Decision 抉择 Alt B（不触及 ORM ask-first），`maxPerUser` 字段在模型/后台/owner doc 保留可配置，服务端仅执行 `maxPerOrder`。此为预先 adjudicated 的 model-gap（Deferred But Adjudicated 区已记录 successor 触发条件），非 closure 时降级，符合 Anti-Slacking 规则。
- Anti-Hollow 复核：`flashSaleBuy` 完整实现（非空体/非 return null），守卫 throw-site 真实抛 ErrorCode，订单创建委托真实生效；job 注册到 scheduler.yaml 真实接线；前端 page.yaml 调真实 GraphQL mutation/query。无空函数体/吞噬异常/未接线组件。
- Verdict: 11 Closure Gates 全部满足，所有 Phase Exit Criteria 与 live repo 一致，text consistency（top status=completed / 4 phases=completed / exit criteria 全 [x] / log 一致）通过。无 blocker，无 major objection。Plan 可闭合。

Follow-up:

- 拼团（P25）启动后复用本计划并发库存/job 调度结论；P22 营销活动管理后台编排秒杀场次配置。
- `maxPerUser` 强一致执行 successor（需 ORM ask-first 批准新增 `LitemallOrder.flashSaleSessionId` 关联列）。
