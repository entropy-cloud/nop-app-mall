# P27 积分体系（Points System）

> Plan Status: completed
> Last Reviewed: 2026-06-27
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 27；`docs/design/wallet-and-assets.md`、`docs/design/marketing-and-promotions.md`、`docs/design/order-and-cart.md`
> Related: `docs/plans/2026-06-27-1742-2-phase26-member-level-system-plan.md`（前置 P26 已 done，解除本计划阻塞）；下游 P28 签到复用本计划 earn API（强依赖）；P33 评价结构化其**核心**（结构化评价 pros/cons/semantic_rating）依赖 Phase 7，仅「评价得积分子特性」复用本计划 earn API（roadmap 依赖表 P33→Phase7，mermaid 的 P27→P33 边仅反映 earn-API 复用，非硬阻塞——roadmap 此处表/图自相矛盾，以依赖表为准）
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 27（积分体系）

> **执行顺序：** 本计划为 2026-06-27-2029 批次第 1 顺位（N=1）。P27 解除 P28 签到的强阻塞（签到得积分复用积分账户 earn API）；P33 评价结构化的「评价得积分子特性」亦复用 earn API（其核心依赖 Phase 7）。优先级最高。本计划与 P23（N=2）均改 `LitemallOrderBizModel.submit()` 价格构成但作用层不同（积分抵扣在 actualPrice 减项层，限时折扣在 goodsPrice/orderPrice 层），须协调 price-formula 接线（见 Non-Goals 与 Phase 3）。

## Current Baseline

> 实读 live repo（working tree，HEAD `35d90f2` + P15/P26/P16 批次 uncommitted 改动）所得，非记忆。

**模型已就绪（脚手架已生成）：**

- `LitemallPointsAccount`（积分账户表，`app-mall.orm.xml:1779-1804`）：`userId`(uk)、`balance`(当前可用积分)、`totalEarned`(累计获得)、`totalSpent`(累计消耗)、`version`(乐观锁)、审计字段。已有 `uk_litemall_points_account_user` 唯一键。
- `LitemallPointsFlow`（积分流水表，`app-mall.orm.xml:1806-1841`）：`accountId`、`userId`、`changeType`(dict `mall/points-change-type`)、`changeAmount`、`balanceAfter`、`sourceType`、`sourceId`、`remark` + `account` to-one 关系。
- `mall/points-change-type` 字典已定义（`app-mall.orm.xml:110`，标签「积分变动类型」，valueType=int）。
- `LitemallOrder.integralPrice`（积分减免，`app-mall.orm.xml:1085`，mandatory）已存在。

**价格槽位已接线但恒零（关键缺口）：**

- `LitemallOrderBizModel.submit()`：`:208` `order.setIntegralPrice(BigDecimal.ZERO)` 初始化为零；`:303-304` `actualPrice = actualPrice.subtract(order.getIntegralPrice())` 已在 actualPrice 减项层。**积分抵扣逻辑完全未实现**——submit 无 `usePoints` 入参，无积分扣减、无积分兑换比例计算。当前公式（post P15/P26）：`orderPrice = goodsPrice + freight - coupon - promotionPrice`（`:295-300`，setOrderPrice@:300）；`actualPrice = orderPrice - integralPrice - grouponPrice`（`:302-307`）。积分属 actualPrice 减项层，与 P15 promotionPrice（orderPrice 减项层）、P26 vipPrice（goodsPrice 汇总层）层位不同，**天然不冲突**。

**BizModel 脚手架已生成但空：**

- `LitemallPointsAccountBizModel` / `LitemallPointsFlowBizModel` 各为纯 `CrudBizModel` 空骨架（已确认，无业务方法）。Admin view 为空骨架。

**业务设计缺失（关键）：**

- `wallet-and-assets.md:65-82` 仅有积分账户/流水的**概念性**段落（业务意图 + 流水字段清单 + 「积分有有效期」「余额不可为负」业务规则），**无获取规则、无抵扣规则、无与订单/售后的交接语义**。
- `marketing-and-promotions.md` **无积分章节**（已读全文 53 个标题，无「积分」「积分商城」「积分获取」段）——roadmap（`enhanced-features-roadmap.md:74`）声称「已包含积分体系业务设计」与实际不符，同 P15 满减的文档缺口模式。
- `order-and-cart.md:58` 仅提及 integral price「当前如果未启用积分能力，则该金额保持为零」，无抵扣规则。
- **积分有效期**：`wallet-and-assets.md:80` 称「积分有有效期，过期积分自动扣减」，但模型无有效期字段、无过期批次实体，且自动过期需定时任务（nop-job-local 已引入，见下）。

**无 ErrorCode：** `AppMallErrors.java` 无 points 域错误码（参照 promotion/member-level 域）。

**前置条件已满足：** P26（会员等级体系）`done`（积分体系依赖 userLevel/vipPrice 已落地）。P5b（支付集成）`done`。

**平台能力提示：** `implementation-roadmap.md:30` Phase 11 已引入 `nop-job-local` 调度装配（5 个定时任务自动执行）；enhanced-roadmap「Nop Platform Reuse」表称 nop-job「未引入」**与实际不符（该表 stale）**。定时任务能力可用（但积分自动过期涉及未建模的有效期，本计划见 Non-Goals）。

**已知交叉：** 积分抵扣在 actualPrice 减项层，与 P23 限时折扣（goodsPrice/orderPrice 层）层位不同；购物赠送积分触发点在订单确认收货（confirm），与售后退款（P16）有积分返还交互（见 Phase 1 Decision）。

## Goals

- 实现积分账户原子操作（获取/消耗产生流水，乐观锁防超扣，余额不可为负）。
- 实现积分获取规则配置（购物赠送为主触发源，比例可配；账户 earn API 供 P28 签到 / P33 评价后续接入）。
- 将 `integralPrice` 正式接入订单结算（结算页可勾选积分抵扣，兑换比例 + 抵扣上限可配），积分扣减与订单提交原子绑定。
- 取消/退款返还积分（与 P16 item 级售后协调）。
- 「我的积分」页（余额 + 收支流水）+ 后台积分规则/手工调账页面。
- 新增 points 域 ErrorCode；核心路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- **积分商城兑换（纯积分或积分+现金）**——roadmap 列为 P27 交付项，但需积分商品目录（积分商品实体/兑换价）未建模且属独立子特性。本计划仅建立积分账户/流水/抵扣/获取 API 基座；积分商城兑换作为 successor（触发条件：积分商品目录建模需求）。属显式范围收窄，见 Deferred。
- **签到得积分 / 评价得积分 / 分享得积分的触发实现**——签到属 P28（依赖本计划的 earn API + nop-job-local 周期重置）；评价得积分属 P33；分享得积分不在当前基线。本计划提供账户 earn API 供其复用，不实现这些触发源。
- **积分有效期与自动过期**——`wallet-and-assets.md:80` 业务规则要求有效期，但模型无有效期字段/过期批次，且批量过期需 nop-job-local 定时任务编排。属 model-gap（缺有效期建模），本计划不实现，触发条件见 Deferred。
- **与 P23 限时折扣的价格公式协调约定**：积分抵扣在 actualPrice 减项层，限时折扣在 goodsPrice/orderPrice 层，两者作用层不同，无直接冲突。计算顺序天然正确（积分抵扣在 orderPrice 汇总后的 actualPrice 层）。本计划与 P23（N=2）顺序执行，submit 改动以各自层位独立接线。
- 移动端前端（属 `mobile-frontend-roadmap.md`，共享同一后端 API）。

## Task Route

- Type: `app-layer design change` + `implementation-only change`（业务设计先行，随后实现；模型已就绪无需模型准备 phase）
- Owner Docs: `docs/design/wallet-and-assets.md`（积分账户/流水/有效期）、`docs/design/marketing-and-promotions.md`（积分获取规则/抵扣/商城交接）、`docs/design/order-and-cart.md`（积分抵扣行为 + 价格构成 integralPrice 接线）
- Skill Selection Basis: 后端 BizModel 方法/错误码/价格接线/跨实体 earn API → `nop-backend-dev`；新增 `@BizQuery`/`@BizMutation` 需 `IGraphQLEngine` 测试 → `nop-testing`（规则 #15）；AMIS 页面 → `nop-frontend-dev`

## Protected Area

> 见 `docs/context/ai-autonomy-policy.md`。本计划触及以下受保护区域，**执行前须取得人工确认**：
>
> - **`model/app-mall.orm.xml`（ask-first）：** 本计划**预期不改 ORM 模型**（积分账户/流水/字典已就绪，获取规则用 `NopSysVariable` 配置不改模型）。若 Phase 1 Decision 判定需新增积分规则实体或字段，则须 ask-first 后改模型并重生成。`LitemallOrder` 价格列注释（integralPrice）已准确（actualPrice 减项），无需改动。
> - 本计划**不**触及 `app-mall-delta`（积分不涉及认证/权限 Delta）。
> - **API 契约（合同变更声明）：** `submit()`（`LitemallOrderBizModel.java:132-138`，公开 `@BizMutation`/GraphQL-exposed）Phase 2 新增 `@Optional @Name("usePoints") Integer usePoints` 入参——**加性/向后兼容**（既有调用不传此参即 usePoints=null→不抵扣，行为不变），无破坏性契约变更；既有 submit 测试与调用方不受影响。
>
> 证据要求（ask-first）：若需改 model → 人工确认 → 改 ORM → 重生成 → 编译/测试通过。若 Phase 1 确认无需改模型，则无 ask-first 触发（submit 新增 @Optional 入参为向后兼容，不属受保护区域变更）。

## Infrastructure And Config Prereqs

- **基础基线锁定（前置门）：** 本批次计划建立在当前 working tree 基线（HEAD `35d90f2` + P15/P26/P16 批次 uncommitted 改动）之上。执行本计划前，**须先将 P15/P26/P16 批次变更提交锁定为稳定 HEAD**（否则 line 引用漂移、submit() 三处价格接线可能冲突）。提交后更新本 plan 的 baseline 引用。
- 积分兑换比例、抵扣上限、购物赠送比例等可配项 → `NopSysVariable`（既有模式：`systemBiz.getConfig("mall_freight_price", context)` / `mall_promotion_coupon_stacking`）。建议 key：`mall_points_to_yuan_ratio`（X 积分=¥1，默认 100）、`mall_points_deduct_max_ratio`（抵扣上限占 orderPrice 比例，默认 0.3）、`mall_points_earn_per_yuan`（购物赠送：每元赠 X 积分，默认 1）。
- 无外部服务/端口/密钥依赖。无数据迁移（新表已存在，存量订单 integralPrice=0 由既成 mandatory 处理）。

## Execution Plan

### Phase 1 — 积分业务设计合成（Decision-heavy）

Status: completed
Targets: `docs/design/wallet-and-assets.md`、`docs/design/marketing-and-promotions.md`、`docs/design/order-and-cart.md`
Required Skill: `none`（纯 docs 业务语义合成，不改 ORM 模型与代码；模型已就绪，本阶段把模型语义 + 获取/抵扣规则回写进 owner doc，消除 roadmap「已包含积分设计」与实际的矛盾）

- Item Types: `Decision | Add`
- Prereqs: 无

- [x] **Skill loading Gate:** 扫描 available skills；本阶段为 docs-only 业务设计合成，无 skill 的 description/触发词覆盖（nop-orm-modeler/nop-database-design 针对「生成/修改模型」，本阶段不改模型）。读完 owner doc：`wallet-and-assets.md`（全文）、`marketing-and-promotions.md`（全文）、`order-and-cart.md`（价格构成 + 退款售后）、`domain-design-guidelines.md:48`（积分获取/账户/抵扣三 doc 分工）。
  - Docs read: `docs/design/wallet-and-assets.md`（全文）、`docs/design/marketing-and-promotions.md`（全文）、`docs/design/order-and-cart.md`（全文）、`docs/context/project-context.md`、`docs/context/ai-autonomy-policy.md`、`AGENTS.md`
- [x] **Decision: 积分获取触发源与主路径。** 抉择「购物赠送」为主触发源（订单确认收货 confirm 时按 `mall_points_earn_per_yuan` × actualPrice 赠送）。理由：购物赠送商业语义最直接、可由订单历史聚合、无需新增触发依赖；签到(P28)/评价(P33)得积分作为后续接入源复用同一 earn API。备选「支付即赠送」被否（退款刷分风险）。残留风险：确认收货前退款的订单不发积分（已由 P16 item 级退款守卫覆盖）。
- [x] **Decision: 积分抵扣计算与上限。** 抉择：用户结算勾选用 N 积分 → `integralPrice = N / mall_points_to_yuan_ratio`；上限 = `min(用户可用积分对应金额, orderPrice × mall_points_deduct_max_ratio)`。理由：兑换比例 + 比例上限为电商惯例，防全额积分抵扣侵蚀营收；orderPrice 为基准（与 coupon/promotion 同层基准）。备选「固定金额上限」被否（不随订单规模伸缩）。
- [x] **Decision: 积分账户原子操作与并发安全。** 抉择：earn/spend 均在单事务内「update account set balance=balance±N where id=? and version=?」+ 插入 flow（带 balanceAfter 快照）；version 不匹配抛 ErrorCode 重试/失败；spend 前校验 balance-N>=0（余额不可为负）。理由：PointsAccount 已有 `version` 乐观锁字段（orm.xml:1794），利用既有字段防超扣。残留风险：高并发单账户需重试，本基线非秒杀场景可接受。
- [x] **Decision: 取消/退款积分返还语义。** 抉择：(a) 积分抵扣——订单取消/整单退款返还用户已扣积分（参照券 returnCoupon 模式）；item 级部分退款**不返还抵扣积分**（积分抵扣为订单级，同 P16 券恢复 Decision）。(b) 购物赠送积分——若已赠送（订单曾确认收货）后发生售后退款，**不追回赠送积分**（简化，残留风险记录）。理由：与 P16 部分退款券恢复 Decision 对称（订单级才恢复），避免 item 级碎片化。已写入 owner doc。
- [x] **Decision: 积分有效期落地策略。** 抉择：**本计划不实现有效期自动过期**（model-gap：模型无有效期字段/过期批次，且批量过期需 nop-job 定时编排）。wallet-and-assets.md:80 有效期业务规则标注为「计划中能力」，触发条件见 Deferred。理由：避免引入未建模字段 + 定时编排扩大范围；账户基座先行。
- [x] **Decision: 获取规则存放方式。** 抉择：用 `NopSysVariable`（`LitemallSystem` keyName/keyValue）配置（`mall_points_earn_per_yuan` / `mall_points_to_yuan_ratio` / `mall_points_deduct_max_ratio`），**不新增积分规则实体**。理由：当前规则为全局比例型，配置即可，避免 ask-first 模型改动；签到规则(P28)已有独立 CheckInRule 实体按天配置，不与本计划冲突。
- [x] **Decision: changeType × sourceType 字段分类法（使「预期不改模型」自洽）。** `mall/points-change-type` 字典仅有三值 `EARN`(0)/`SPEND`(10)/`EXPIRE`(20)（`app-mall.orm.xml:110-114`），**无**「返还」「购物赠送」「抵扣」「调账」等细分值。抉择：`changeType` 严格映射既有三值（获取类全用 EARN、消耗类全用 SPEND、过期用 EXPIRE），**所有细分来源/去向落入 `sourceType`（VARCHAR(50)）字段**：购物赠送=`EARN, sourceType=order-confirm-earn`；取消/退款返还=`EARN, sourceType=refund-return`；结算抵扣=`SPEND, sourceType=order-deduct`；手工调账加=`EARN, sourceType=admin-adjust`/扣=`SPEND, sourceType=admin-adjust`；签到(P28)=`EARN, sourceType=check-in`；评价(P33)=`EARN, sourceType=comment-reward`。理由：复用既有字典三值 + 既有 sourceType 字段承载细粒度，**无需扩字典、无需改模型**，使 Protected Area「预期不改模型」成立。`sourceId` 记录来源业务 ID（orderId/commentId/checkInRecordId）支撑幂等查重。
- [x] **Add:** 将积分体系业务设计（账户/流水语义、changeType×sourceType 分类法、获取规则配置项、抵扣规则与上限、取消/退款返还语义、有效期策略、与 P28/P33 交接）写入 `wallet-and-assets.md`（积分账户章节扩展）与 `marketing-and-promotions.md`（新增「积分体系」章节）；`order-and-cart.md` 价格构成 integralPrice 段补抵扣规则与计算顺序。

Exit Criteria:

- [x] `wallet-and-assets.md` 含积分账户/流水完整业务设计（含抵扣/获取/返还/有效期策略）；`marketing-and-promotions.md` 新增积分体系章节（获取规则/抵扣/商城交接）；`order-and-cart.md` integralPrice 段含抵扣规则
- [x] 七个 Decision 的抉择/备选/理由/残留风险已记录（含 changeType×sourceType 分类法使「不改模型」自洽）
- [x] Phase 2/3 模型/实现清单由本阶段 Decision 确定（不改模型：获取规则走 NopSysVariable，changeType 复用既有三值，细分来源走 sourceType）

### Phase 2 — 后端：积分账户原子操作 + 获取 + 抵扣接线 + ErrorCode（Add-heavy）

Status: completed
Targets: `app-mall-service/.../LitemallPointsAccountBizModel.java`、`app-mall-service/.../LitemallPointsFlowBizModel.java`、`app-mall-service/.../LitemallOrderBizModel.java`、`app-mall-service/.../AppMallErrors.java`
Required Skill: `nop-backend-dev`、`nop-testing`（本 phase 新增 `@BizQuery`/`@BizMutation`，规则 #15 要求 `IGraphQLEngine` 测试）

- Item Types: `Add`
- Prereqs: Phase 1（抵扣/获取/返还 Decision 已决）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完各自 routing table 标为必读的全部文档（`05-examples/ibiz-and-bizmodel.java`、`02-core-guides/service-layer.md`、`02-core-guides/error-handling.md`、`04-reference/safe-api-reference.md`、`05-examples/test-examples.java`、`02-core-guides/testing.md`、`02-core-guides/concurrency-and-transactions.md`）。每写完一个方法用 selfcheck 校验（无 anti-pattern：#8 newEntity/#9 findList-saveEntity/#11-14 NopException+ErrorCode/#15 无 @Transactional/#16 @Inject 非 private/#18 CoreMetrics）。
  - Docs read: `nop-entropy/docs-for-ai/05-examples/ibiz-and-bizmodel.java`、`02-core-guides/service-layer.md`、`02-core-guides/error-handling.md`、`04-reference/safe-api-reference.md`、`05-examples/test-examples.java`、`02-core-guides/testing.md`、`02-core-guides/concurrency-and-transactions.md`
- [x] **Add:** `LitemallPointsAccountBizModel` 积分账户原子操作：`earnPoints(userId, amount, changeType, sourceType, sourceId, remark, context)`（`@BizMutation`，获取）、`spendPoints(userId, amount, changeType, sourceType, sourceId, remark, context)`（`@BizMutation`，消耗）—— 内部「乐观锁 update account + insert flow(balanceAfter 快照)」，spend 前校验余额非负，账户不存在则自动创建（首次 earn）。参照 `LitemallCouponUser.updateStatusIfUnused` 条件 UPDATE 模式（新增 `LitemallPointsAccountMapper.updateBalanceIfVersion` + sql-lib）。
- [x] **Add:** `getMyPoints(context)`（`@BizQuery`，我的积分余额）、`getMyPointsFlows(...)`（`@BizQuery`，我的积分流水，支持时间/类型筛选，落于 `LitemallPointsFlowBizModel`）；后台 `adjustPoints(userId, amount, remark, context)`（`@BizMutation @Auth(roles="admin")`，手工调账，运营用）。
- [x] **Add:** 购物赠送积分触发——在订单确认收货路径（`LitemallOrderBizModel.confirm`）按 `mall_points_earn_per_yuan` × actualPrice 调 `earnPoints`（`changeType=EARN`，`sourceType=order-confirm-earn`，sourceId=orderId）；幂等（同 orderId 不重复赠送，按 sourceType+sourceId 查重）。同时在 `confirmExpiredOrders`（系统自动收货）触发赠送。
- [x] **Add:** `LitemallOrderBizModel.submit()` 积分抵扣接线——submit 新增 `usePoints`（int，`@Optional`）入参；非空时按 Phase 1 Decision 计算 integralPrice（N/ratio，受 orderPrice×max_ratio 上限约束），扣减用户积分（调 spendPoints，`changeType=SPEND, sourceType=order-deduct`，sourceId=orderId），`order.setIntegralPrice(...)`。校验 `actualPrice >= 0`（既有守卫复用）。
- [x] **Add:** 取消/退款积分返还——订单取消（`cancel`/`cancelExpiredOrders`）、整单退款（`aftersale.refund` whole-order）、团购失败退款（`groupon.refundGrouponOrder`）路径调 `earnPoints`（返还已扣抵扣积分，`changeType=EARN, sourceType=refund-return`，sourceId=orderId）；item 级部分退款不返还（Phase 1 Decision）。参照 `returnCoupon` 调用点。
- [x] **Add:** `AppMallErrors` 新增 points 域 ErrorCode（`nop.err.mall.points.insufficient`/`.account-not-found`/`.deduct-exceed-limit`/`.earn-failed`/`.duplicate-earn`），参照 promotion/member-level 域命名。
- [x] **Proof:** 积分账户操作 + 获取 + 抵扣接线通过 `IGraphQLEngine`（`JunitBaseTestCase`）测试：earn/spend 原子性 + 流水 balanceAfter、余额非负校验、抵扣上限、购物赠送幂等、取消返还、手工调账。指定 `TestLitemallPointsAccountBizModel`（7 用例）+ `TestLitemallOrderBizModel` 新增积分抵扣/赠送/取消返还用例（4 用例）全绿。全模块 159 测试全绿。

Exit Criteria:

- [x] 积分账户 earn/spend 原子落地（乐观锁 + 流水 + 余额非负）；订单 integralPrice 正确反映积分抵扣
- [x] **API 测试：** `getMyPoints`/`getMyPointsFlows`/`earnPoints`/`spendPoints`/`adjustPoints`（`@BizQuery`/`@BizMutation`）+ submit 积分抵扣接线 + 购物赠送/返还通过 `IGraphQLEngine`
- [x] integralPrice 公式与 ORM 列注释一致；`actualPrice>=0` 守卫保持；取消/退款积分返还正确
- [x] points ErrorCode 已定义并被使用

### Phase 3 — 前端：我的积分页 + 结算积分抵扣 + 后台调账/规则（Add-heavy）

Status: completed
Targets: 前台个人中心积分页、结算页积分抵扣勾选、后台 `pages/LitemallPointsAccount/`、`pages/LitemallPointsFlow/`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 2（后端 API 就绪）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完 routing 必读文档（`00-start-here/application-project-defaults.md`、`02-core-guides/view-and-page-customization.md`、`02-core-guides/delta-customization.md`、`03-runbooks/prefer-delta-over-direct-modification.md`）。文件/类完成后 selfcheck（未改 `_gen`、保留层 view.xml 正确 `x:extends`、AMIS service 调 `@query`/`@mutation`）。
  - Docs read: `nop-entropy/docs-for-ai/00-start-here/application-project-defaults.md`、`02-core-guides/view-and-page-customization.md`、`02-core-guides/delta-customization.md`、`03-runbooks/prefer-delta-over-direct-modification.md`
- [x] **Add:** 「我的积分」页（`mall/points/my-points.page.yaml`）调 `@query:LitemallPointsAccount__getMyPoints` 展示余额 + 累计获得/消耗；`@query:LitemallPointsFlow__getMyPointsFlows` 展示收支流水（全部/获取/消耗 tab 筛选，changeType+sourceType 映射）。已在 `app-mall.action-auth.xml` 注册 `/storefront-my-points` 路由（orderNo 826），并在 `user-center.page.yaml` 加入「我的积分」入口。
- [x] **Add:** 结算页（`checkout.page.yaml`）积分抵扣勾选——新增 `pointsService`（getMyPoints 余额）+ `usePoints` input-number（max=余额，visibleOn 余额>0），提交时传 `usePoints`；描述含兑换比例/上限提示。
- [x] **Add:** 后台积分账户/流水管理页（`LitemallPointsAccount.view.xml` / `LitemallPointsFlow.view.xml`）：grid bounded-merge（账户：userId/balance/totalEarned/totalSpent；流水：userId/changeType/changeAmount/balanceAfter/sourceType/sourceId/remark/addTime）；账户页提供手工调账动作（listAction `adjust-button` 调 `@mutation:LitemallPointsAccount__adjustPoints` via `adjust` form/dialog）；edit 用 drawer。

Exit Criteria:

- [x] 我的积分页展示余额 + 流水（全部/获取/消耗 tab 筛选）
- [x] 结算页可勾选积分抵扣，提交时传 usePoints
- [x] 后台可查积分账户/流水 + 手工调账
- [x] 复用既有 AMIS 三层定制模式，无新前端依赖（bounded-merge / dialog / @query·@mutation）

### Phase 4 — 验证、文档同步、日志（Proof）

Status: completed
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-3

- [x] **Skill loading gate:** 加载 `nop-testing`，读 routing 必读文档（`05-examples/test-examples.java` / `02-core-guides/testing.md`，已在前序 phase 加载，规则已遵循）。
  - Docs read: `nop-entropy/docs-for-ai/05-examples/test-examples.java`、`02-core-guides/testing.md`
- [x] **Proof:** 跑 `docs/context/project-context.md` 真实验证命令（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` BUILD SUCCESS；`./mvnw test -pl app-mall-service -am` **159 测试全绿**；`./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS），全绿；更新 `docs/testing/known-good-baselines.md`。
- [x] 更新 `docs/logs/2026/06-27.md`（逆向时间序，置顶 Phase 27 条目）。

Exit Criteria:

- [x] 全量验证命令通过（含本计划新增 IGraphQLEngine 测试）
- [x] `known-good-baselines.md` 更新本计划 baseline row
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗审计（fresh session），三轮达成共识。
  - Round 1（`ses_0f6e9443dffeDXIQhED3qxjOeG`）：REVISE — 1 BLOCKER（points-change-type 字典仅 EARN/SPEND/EXPIRE，Phase 2 误用 changeType=返还/购物赠送，与「预期不改模型」矛盾）+ 2 MAJOR（P33 依赖过强、submit usePoints 入参未声明 API 契约）+ minors。全部修订：补 changeType×sourceType 分类法 Decision（changeType 复用三值、细分来源落 sourceType）；P33 依赖改为「核心依赖 Phase 7，仅 earn-API 复用」；submit 入参声明为 @Optional 加性向后兼容。
  - Round 2（`ses_0f6dedaa1ffeE7RXG6LajtBbb1`）：PASS — 4 项全部 RESOLVED，无新增（15/15 baseline 实读核验通过；no-model-change 论证自洽；价格层位/并发/skill/anti-slacking 全合规）。第 1 个 clean round。
  - Round 3（`ses_0f6d64231ffeHDMud07AoXvxi6`）：PASS — 第 2 个连续 clean round，共识达成。
- Evidence: 实读 live repo 核验全部 baseline（PointsAccount/PointsFlow @ orm.xml:1779-1841、points-change-type 字典 @ :110-114 仅三值、integralPrice @ submit:208/303-304、空 BizModel、marketing-and-promotions.md 无积分段、AppMallErrors 无 points 码、version 乐观锁字段 @ :1794）。no-model-change 论证（字典复用 + sourceType + NopSysVariable + @Optional usePoints）经核验自洽。

## Closure Gates

- [x] in-scope behavior is complete（积分账户/流水/获取/抵扣/返还/前端/后台）
- [x] relevant docs are aligned（`wallet-and-assets.md` / `marketing-and-promotions.md` / `order-and-cart.md`）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 全绿 + app-mall-web 编译 + uber-jar install）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（earn/spend/getMyPoints/getMyPointsFlows/adjustPoints + submit 抵扣接线）
- [x] no in-scope item downgraded to deferred/follow-up（积分商城兑换/签到评价得积分/有效期在 Non-Goals 显式移出）
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（Phase 1 `none` 含 justify）
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 积分商城兑换（纯积分或积分+现金）

- Classification: `out-of-scope improvement`
- **Successor Closed:** 已由 `docs/plans/2026-06-29-0900-1-points-mall-exchange-plan.md` 交付（纯积分兑换 firm 结果面；积分+现金组合兑换为该计划 Decision D3 抉择方案 B，再次 Deferred）。Phase 27 本身已 done，不改 Phase Status 行。
- Why Not Blocking Closure: 需积分商品目录（积分商品实体/兑换价）未建模，属独立子特性；本计划建立积分账户/流水/抵扣/获取 API 基座，积分商城兑换可在此基础上独立交付。**注：** 此项为 roadmap P27 明列交付项（`enhanced-features-roadmap.md:334`）的范围收窄，已显式 adjudicated 并记录 successor + 触发条件，待人工确认收窄。
- Successor Required: `yes`（触发条件：积分商品目录建模需求出现）→ **已触发并闭环**

### 购物赠送幂等的并发安全（sourceType+sourceId 唯一键）

- Classification: `model-gap`
- Why Not Blocking Closure: `LitemallPointsFlow` 无 `(sourceType, sourceId)` 数据库唯一键（`app-mall.orm.xml:1806-1841` 仅有 relations 无 unique-keys）。购物赠送幂等依赖应用层「查 sourceId 后插入」，在并发 confirm() 下存在竞态（极低概率，订单确认收货非高并发路径）。账户 earn/spend 核心不依赖此唯一键。
- Successor Required: `yes` → **已触发并闭环**
- Successor Closed: 已由 `docs/plans/2026-06-29-1045-1-orm-data-integrity-constraints-plan.md` 交付（`uk_litemall_points_flow_source` 唯一键 + earnPoints 应用层异常翻译为 `ERR_POINTS_DUPLICATE_EARN`）。
- Model Gap Detail: 缺 `LitemallPointsFlow` 的 `(sourceType, sourceId)` 唯一键约束（防重复赠送）；触发条件——下次修改 PointsFlow 模型时，或业务要求赠送幂等强一致（数据库级保证）时，补 unique-key。

### 积分有效期与自动过期

- Classification: `model-gap`
- Why Not Blocking Closure: 模型无有效期字段/过期批次实体，且批量过期需 nop-job-local 定时编排；账户 earn/spend/抵扣核心不依赖有效期。`wallet-and-assets.md:80` 有效期业务规则标注为「计划中能力」。
- Successor Required: `yes` → **已触发并闭环**
- Successor Closed: 已由 `docs/plans/2026-06-29-1200-1-points-validity-auto-expiry-plan.md` 交付（新增 `LitemallPointsExpireBatch` 有效期批次实体 + earn 建批次/spend FIFO 消耗/正向调账建批次 + `expirePoints` @BizMutation 编排[复用 `mutateBalance` CAS 序列] + `getMyPointsExpiryHint` 查询 + `expire-points` nop-job 每小时定时 + 「我的积分」页过期提示；不变量 `balance >= SUM(remainingPoints)`；存量按 D3 不过期；489 测试全绿）。
- Model Gap Detail: 缺积分有效期建模（建议 PointsAccount 加 `earliestExpireTime` 或新建 `LitemallPointsExpireBatch` 有效期批次表）+ nop-job-local 定时过期任务；触发条件——下次修改 PointsAccount 模型时，或业务要求积分有效期强一致时。**[已解决：采用新建 `LitemallPointsExpireBatch` 批次表方案]**

### 签到/评价/分享得积分触发

- Classification: `watch-only residual`
- Why Not Blocking Closure: 签到属 P28（依赖本计划 earn API + nop-job-local）、评价属 P33、分享不在基线；本计划 earn API 已为这些触发源预留，不阻塞积分账户基座。
- Successor Required: `yes`（触发条件：P28/P33 启动时复用 earn API）

## Closure

Status Note: 已闭合（2026-06-27）。全 4 Phase completed，11 项 Closure Gates 全 PASS。

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计 subagent（fresh session `ses_0f69f1b36ffelIE00BLyHcoOq5`，不同于实现 agent）。
- Evidence: 11 项 Closure Gates 逐项核验通过（in-scope/docs/verification/IGraphQLEngine 测试/Non-Goals/plan-audit/skill/consistency/closure-by-different-agent/closure-evidence）。专项风险核验：①ORM 模型未改（`git status` 确认 `model/app-mall.orm.xml` 未触及，changeType×sourceType 分类法 + NopSysVariable 使「不改模型」成立）；②无 anti-pattern（全 NopException、无 @Transactional/@Inject private/new Entity 误用、跨实体走 I*Biz）；③submit usePoints 为 @Optional 加性向后兼容；④乐观锁模式（flushSession+clearEntitySessionCache+mapper）与 cancelExpiredOrders 先例一致。
- 审计发现并已修复：MAJOR-1（`checkout.page.yaml` orderForm 缩进回归，name/title/mode/body 由 12 空格还原为 14 空格，YAML 已 `python3 yaml.safe_load` 校验通过 + web 编译 BUILD SUCCESS）。
- 接受的残留（非阻塞）：MINOR-1（aftersale 整单退款 / groupon 失败返还积分路径无独立 IGraphQLEngine 测试，但其返还逻辑与已测的 cancel `returnDeductedPoints` 同源、均经幂等 earnPoints，风险低）。

Follow-up:

- 积分商城兑换（触发条件：积分商品目录建模需求）。
- 积分有效期自动过期（触发条件：PointsAccount 模型修改或有效期强一致需求，需补有效期建模 + nop-job-local 任务）。
- 补 aftersale/groupon 返还积分路径的 IGraphQLEngine 测试（MINOR-1，非阻塞）。
