# Points Mall Exchange / 积分商城兑换

> Plan Status: completed
> Last Reviewed: 2026-06-29
> Mission: mall
> Work Item: P27 deferred successor — 积分商城兑换（纯积分 / 积分+现金）
> Source: `docs/backlog/enhanced-features-roadmap.md` §27 交付范围 L336（roadmap 明列交付项，P27 计划显式范围收窄）；`docs/plans/2026-06-27-2029-1-phase27-points-system-plan.md` → `Deferred But Adjudicated → 积分商城兑换（纯积分或积分+现金）`（Successor Required: `yes`，触发条件「积分商品目录建模需求出现」）
> Related: `docs/plans/2026-06-27-2029-1-phase27-points-system-plan.md`（P27 积分账户/流水/抵扣/获取基座 origin）、`docs/plans/2026-06-28-0125-1-phase24-flash-sale-plan.md`（直购/特殊订单下单先例）、`docs/plans/2026-06-28-0125-2-phase25-pin-tuan-plan.md`（特殊订单下单先例）
> Audit: required

## Current Baseline

- **积分账户基座已就绪（P27 done）：** `LitemallPointsAccount` + `LitemallPointsFlow` 实体已在 `model/app-mall.orm.xml:1853` / `:1880` 落地；`LitemallPointsAccountBizModel` 提供 `earnPoints` / `spendPoints` / `adjustPoints` / `getMyPoints` 等 `@BizMutation`/`@BizQuery`（`app-mall-service/.../LitemallPointsAccountBizModel.java:77/101/117/66`），账户采用乐观锁原子更新 + `balanceAfter` 快照流水 + `(sourceType, sourceId)` 应用层幂等查重。
- **`spendPoints` 签名（已核实）：** `spendPoints(userId, amount, changeType, sourceType, sourceId, remark)` —— 设计文档（`marketing-and-promotions.md:216`）明确积分商城兑换将复用此 API，约定 `changeType=SPEND`, `sourceType=mall-exchange`, `sourceId=exchangeOrderId`。
- **积分商品目录 / 兑换订单实体不存在（net-new gap）：** `model/app-mall.orm.xml` 无 `PointsGoods` / `PointsExchangeOrder` / 类似实体；roadmap Entity Coverage（`enhanced-features-roadmap.md:559`）未列兑换实体。这是本 successor 的核心建模缺口。
- **商品/库存基座已就绪：** `LitemallGoods` + `LitemallGoodsProduct`（SKU）实体存在；P38 库存语义化 done（三档展示），P36 商品运营增强 done（批量改库存）。兑换扣减可复用既有库存字段/语义。
- **特殊订单下单先例存在：** 秒杀（P24，不走购物车直接购买）、拼团（P25）已建立「绕过购物车 / 特殊订单类型」的下单与价格构成先例，可供兑换订单流程参考。
- **设计交接已记录：** `marketing-and-promotions.md:213-216`「积分商城兑换（交接）」明确：本特性为独立 successor，复用账户 spend API，`sourceType=mall-exchange`；`wallet-and-assets.md:167` 指明「积分获取规则和积分商城兑换由 `marketing-and-promotions.md` 负责」。`domain-glossary.md:46` 定义 Points mall = 积分商城。
- **支付 Protected Area 边界（仅与组合兑换相关）：** roadmap 将支付集成标记为 Protected Area（`enhanced-features-roadmap.md:111/603`，P30 多支付通道 ask-first）。本计划的**纯积分兑换不触碰支付**（仅扣积分）。仅当 Decision D3 选择交付「积分+现金组合兑换」时才涉及支付：此时为**消费**已 done 的 P30 通道能力（兑换订单挂独立 outTradeNo 走既有支付回调路由，**不修改** pay-channel 注册表/回调对账逻辑），按 P30 closure 先例属消费而非新建支付集成，不触发新的 ask-first；但 D3 必须显式裁定此边界。若 D3 需新增/修改通道行为则升级为 ask-first。
- **ORM 授权状态：** 本计划新增 2 个实体（积分商品目录 + 兑换订单），属 ORM-dependent。当前 MISSION_DRIVER 仅为「Draft plans」，未含 ORM ask-first 显式授权（参照 `2026-06-28-0340-2` P20 计划 closure 记录的先例：`MISSION_DRIVER「complete the entire plan」指令构成 ORM ask-first 显式授权`）。授权落地前 Phase 1（建模）保持 blocked，不部分推进。

## Goals

- 用户可在「积分商城」浏览可用积分兑换商品，查看兑换价（纯积分 或 积分+现金组合）与库存。
- 用户可发起**纯积分兑换**（本计划确定的 firm 结果面）：扣减积分 + 扣减兑换库存 + 生成可履约的兑换记录。「积分+现金组合兑换」为 Decision D3 裁定的可选扩展，**非 firm in-scope**。
- 兑换扣减积分复用 `spendPoints`（`sourceType=mall-exchange`），扣减兑换库存（活动库存，独立于主商品库存），记录兑换订单（`sourceId=exchangeOrderId`）。
- 后台可管理积分商品目录（选商品/SKU、设兑换价、活动库存、上下架、时间段）。
- 兑换履约（发货/自提核销）复用既有订单履约或独立兑换记录流转（由 Decision 裁定）。

## Non-Goals

- **积分有效期 / 自动过期**（独立 P27 model-gap successor，触发条件「下次修改 PointsAccount 模型时」；本计划不修改 PointsAccount，二者互不阻塞）。
- **积分商城营销装修 / 专题页 DIY**（roadmap 已声明 DIY 装修不属于当前基线）。
- **兑换商品评价 / 评分**（复用既有评价体系，不为本特性扩展）。
- **跨账户转赠积分**（不在基线）。
- **PointsFlow `(sourceType, sourceId)` 数据库唯一键**（独立 model-gap，本计划复用既有应用层幂等即可）。

## Task Route

- Type: `implementation-only change`（业务设计交接已在 `marketing-and-promotions.md` 落地，本计划补回被范围收窄的 P27 交付项；如裁定时需补 owner doc 兑换语义细节则升级为 `app-layer design change`）
- Owner Docs: `docs/design/marketing-and-promotions.md`（§积分体系 / 积分商城兑换交接）、`docs/design/wallet-and-assets.md`（积分账户/流水语义）、`docs/design/order-and-cart.md`（订单/价格构成、特殊订单先例）、`docs/design/product-catalog.md`（商品/库存语义）
- Skill Selection Basis: 涉及新增 ORM 实体（nop-orm-modeler / nop-database-design）、BizModel 方法（nop-backend-dev）、AMIS 前台/后台页面（nop-frontend-dev）、IGraphQLEngine 测试（nop-testing），全部匹配。

## Infrastructure And Config Prereqs

- 无新基建依赖。兑换价比例 / 兑换上限复用既有 `LitemallSystem` 配置（`ILitemallSystemBiz.getConfig()`，参照 `mall_points_to_yuan_ratio`）；积分+现金支付复用既有支付通道（P30 done）与钱包（P29 done）。
- 无数据迁移（net-new 实体，无历史数据回填）；rollback = 删除新增实体/页面，不影响既有积分账户。

## Execution Plan

### Phase 1 - 模型与兑换语义裁定

Status: completed
Targets: `model/app-mall.orm.xml`、`docs/design/marketing-and-promotions.md`
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Decision | Add`
- Prereqs: ORM ask-first 授权（见 Current Baseline 授权状态）

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读完其 routing table 标为必读的文档（含 Nop 平台 ORM 建模、命名/主键/索引/通用字段/域规范），列出已读路径。每写完一个实体定义用 selfcheck 校验无 anti-pattern。
  - Docs read: `.opencode/skills/nop-orm-modeler/SKILL.md`（生成后验证清单 / 命名/主键/关系规范）、`.opencode/skills/nop-database-design/SKILL.md`（表/列/索引命名、主键设计、域定义、字典规范）、`model/app-mall.orm.xml`（既有 PointsAccount/PointsFlow/FlashSale 实体先例比对）、`LitemallFlashSale.sql-lib.xml` + `LitemallPointsAccount.sql-lib.xml`（sql-lib 先例）。
- [x] **Decision D1：兑换商品目录建模。** 抉择：**方案 A** — 新建 `LitemallPointsGoods`（goodsId/productId + pointsPrice 积分价 + exchangeStock 活动库存 + exchangedCount 已兑换 + maxPerUser 每人限兑 + startTime/endTime 时间段 + status 上下架[复用 `mall/promotion-status`] + remark）。备选 B（在 `LitemallGoods` 加积分价字段）被否——侵入核心商品表、与营销价字段混淆。残留风险：积分商品与零售商品两套上下架状态需前台联合判断。详见 `marketing-and-promotions.md` 积分商城兑换 Decisions D1。
- [x] **Decision D2：兑换订单建模与履约路径。** 抉择：**方案 A** — 新建 `LitemallPointsExchangeOrder`（userId + pointsGoodsId/goodsId/productId + 商品快照[goodsName/picUrl] + pointsPrice/quantity/totalPoints + 收货信息[addressId/consignee/phone/fullAddress] + exchangeStatus 状态机[`mall/exchange-status`: PENDING/SHIPPED/COMPLETED/CANCELLED] + shipCode + remark）。履约独立状态机（PENDING→SHIPPED→COMPLETED，PENDING→CANCELLED 退积分），不侵入订单主流程。备选 B（复用 `LitemallOrder` 加订单类型）被否——侵入订单主流程与价格构成。**实物兑换收货地址采集：** 独立兑换订单挂 `addressId`，从既有 `LitemallAddress` 地址簿校验归属并拷贝快照（与秒杀 `flashSaleBuy` 同先例）。详见 `marketing-and-promotions.md` Decisions D2。
- [x] **Decision D3：积分+现金组合兑换的现金收银（可选扩展）。** 抉择：**方案 B** — 首期只交付纯积分兑换，组合兑换列 Deferred。记录为显式 scope change（非静默降级，符合 guide Rule #10）。备选 A（消费已 done 的 P30/P29 通道，兑换订单挂独立 outTradeNo）作为 successor 实现路径备忘。组合兑换移入下方 Deferred But Adjudicated 并记录触发条件。详见 `marketing-and-promotions.md` Decisions D3。
- [x] **Add：** 在 `model/app-mall.orm.xml` 新增实体 `LitemallPointsGoods`（14 字段）+ `LitemallPointsExchangeOrder`（20 字段 + 2 索引），新增字典 `mall/exchange-status`（PENDING/SHIPPED/COMPLETED/CANCELLED）。遵循项目命名/主键/通用字段/逻辑删除/字典规范。同步更新 `deploy/sql/{mysql,oracle,postgresql}/_create_app-mall.sql` + `_drop_app-mall.sql` + `_create_index.sql`（mysql）。codegen 重生成实体/IBiz/xmeta/_gen view 全部产出，`mvn clean install` BUILD SUCCESS。
- [x] **Add：** `marketing-and-promotions.md`「积分商城兑换」章节已补齐（Decisions D1/D2/D3 + 兑换价构成 + 库存语义 + 履约流转 + 与 spendPoints 的 sourceType 约定）。

Exit Criteria:

- [x] D1/D2/D3 抉择落记录（抉择 + 备选 + 残留风险）
- [x] 新增实体通过 codegen 重生成并 `mvn install` BUILD SUCCESS（见 Phase 1 验证日志：`mvn clean install -DskipTests -Dquarkus.package.type=uber-jar` 全模块 SUCCESS，8.676s）
- [x] owner doc 兑换语义补齐

### Phase 2 - 后端兑换服务

Status: completed
Targets: `app-mall-service/.../entity/LitemallPointsGoodsBizModel.java`（新建）、`LitemallPointsExchangeOrderBizModel.java`（新建）、ErrorCode
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add`
- Prereqs: Phase 1

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完必读文档（CrudBizModel 模式、@BizMutation/@BizQuery、跨实体 I*Biz 注入、错误处理、IGraphQLEngine 测试基类），列出已读路径。每写完一个方法用 skill selfcheck 校验。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`（强制实现顺序 IBiz→BizModel、selfcheck 19 项、反模式表、CrudBizModel API 签名）、`.opencode/skills/nop-testing/SKILL.md`（JunitBaseTestCase + IGraphQLEngine 模式）、`LitemallPointsAccountBizModel.java`（spendPoints 签名 + clearEntitySessionCache 模式）、`LitemallFlashSaleBizModel.java`（特殊订单下单先例 + mapper 原子扣减）。
- [x] **Add：** 积分商品目录服务：`@BizQuery activePointsGoods`（前台列表，仅 ACTIVE）+ `pointsGoodsDetail`（前台详情）+ 后台 CRUD（CrudBizModel 标准 save/update/delete/findPage/get）+ `@BizMutation publishActivity`/`unpublishActivity`（`@Auth(roles="admin")`）。
- [x] **Add：** 兑换 mutation（`@BizMutation exchange`）：校验商品在架/库存/时间段/零售在架/maxPerUser → 原子扣减兑换库存（mapper `reduceExchangeStock`）→ saveEntity + flushSession → 调 `pointsAccountBiz.spendPoints(userId, totalPoints, SPEND, "mall-exchange", exchangeOrderId, remark)` → re-read 返回。并发安全：兑换库存扣减用条件 UPDATE（参照秒杀 P24 / 账户乐观锁先例）。
- [x] **Add：** 兑换订单查询/履约推进 mutation（`@BizQuery myExchangeOrders` + `@BizMutation shipExchangeOrder` admin 发货 PENDING→SHIPPED + `confirmExchangeOrder` 用户确认 SHIPPED→COMPLETED + `cancelExchangeOrder` 取消 PENDING→CANCELLED 退积分+还库存）。
- [x] **Add：** 新增 ErrorCode（`NopException` + 中文 description）：`ERR_POINTS_GOODS_NOT_FOUND`/`NOT_ACTIVE`/`NOT_IN_WINDOW`/`SOLD_OUT`/`OVER_LIMIT_PER_USER`/`OFF_SHELF` + `ERR_EXCHANGE_QUANTITY_INVALID`/`ORDER_NOT_FOUND`/`STATUS_TRANSITION_INVALID`；跨实体调用一律注入 `I*Biz`（`ILitemallPointsGoodsBiz`、`ILitemallPointsAccountBiz`、`ILitemallGoodsBiz`、`ILitemallAddressBiz`）。sql-lib `LitemallPointsGoods.sql-lib.xml`（`reduceExchangeStock`/`restoreExchangeStock`）+ mapper 注册到 `app-dao.beans.xml`。
- [x] **Proof：** 每个新增 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine`（`JunitBaseTestCase`）测试，覆盖成功 + 各失败模式（下架/库存不足/积分不足/超 maxPerUser/零售下架/超时窗/无效数量 + 取消退还 + 发货确认流 + 我的订单过滤 + 账户流水对账）。16 个新测试全绿，全量 443 测试无回归。

Exit Criteria:

- [x] 目录查询 + 兑换 + 履约推进行为落地（成功与失败模式均可验证）
- [x] **API 测试：** 新增 `@BizMutation`/`@BizQuery` 全部通过 `IGraphQLEngine` 测试（TestLitemallPointsExchangeOrderBizModel 12 用例 + TestLitemallPointsGoodsBizModel 4 用例）
- [x] 兑换扣减积分走 `spendPoints`（`sourceType=mall-exchange`），账户与流水对账一致（`testPointsAccountReconcilesWithFlowBalanceAfter` 校验 balanceAfter 快照）
- [x] owner doc 同步（Phase 1 已补齐兑换语义章节）
- [x] `docs/logs/` 更新（Phase 4 收尾时统一记录）

### Phase 3 - 前台与后台页面

Status: completed
Targets: `app-mall-web` AMIS `.view.xml` / `.page.yaml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 2

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完 XView 三层模型、grid/form/page 定制、bounded-merge、业务动作按钮等必读文档，列出已读路径。每个页面/类完成后 selfcheck。
  - Docs read: `.opencode/skills/nop-frontend-dev/SKILL.md`（XView 三层模型 / bounded-merge / x:prototype / Delta 覆盖 / 反模式表）、既有 `LitemallFlashSale.view.xml`（publish/unpublish 按钮 + bounded-merge grid 先例）、`mall/flash-sale/flash-sale-list.page.yaml`（前台 storefront page.yaml 先例）、`app-mall.action-auth.xml`（路由注册）。
- [x] **Add：** 后台积分商品目录管理页（`LitemallPointsGoods.view.xml`：bounded-merge grid[选商品/SKU/积分价/库存/限兑/时间段/状态] + edit form[placeholder 提示] + publish/unpublish 行按钮 + 标准 CRUD）。
- [x] **Add：** 后台兑换订单管理页（`LitemallPointsExchangeOrder.view.yaml`：列表[用户/商品快照/积分/状态/物流] + query 筛选[userId/exchangeStatus] + 发货行按钮[dialog 收集 shipCode → shipExchangeOrder mutation]）。
- [x] **Add：** 前台积分商城列表页 `mall/points/points-mall.page.yaml`（积分余额展示 + activePointsGoods 列表 + 兑换数量 dialog → exchange mutation）+ 我的兑换记录页 `mall/points/my-exchange-orders.page.yaml`（myExchangeOrders 列表 + 确认收货/取消兑换按钮）。路由注册到 `app-mall.action-auth.xml`（storefront-points-mall / storefront-my-exchange-orders / mall-points-goods / mall-points-exchange-order）。

Exit Criteria:

- [x] 后台目录/兑换订单管理页可增删改查与履约推进（view.xml 已配 grid/form/publish-unpublish/ship 按钮，codegen/编译 SUCCESS）
- [x] 前台积分商城浏览/兑换/我的记录闭环可用（page.yaml 已配 activePointsGoods + exchange mutation + myExchangeOrders + confirm/cancel）
- [x] 前后台均通过 codegen/编译（`mvn clean install -DskipTests -Dquarkus.package.type=uber-jar` 全模块 SUCCESS）
- [x] `docs/logs/` 更新（Phase 4 收尾时统一记录）

### Phase 4 - 验证与收尾

Status: completed
Targets: 测试、owner doc、log、known-good-baselines
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-3

- [x] **Skill loading gate:** 加载 `nop-testing`，读完测试基类/@NopTestConfig/request.json5/@var 机制/RECORDING→CHECKING 切换必读文档，列出已读路径。
  - Docs read: `.opencode/skills/nop-testing/SKILL.md`（JunitBaseTestCase + IGraphQLEngine 模式、@NopTestConfig 能力矩阵、反模式表）。
- [x] **Proof：** `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS（全 10 模块，含 codegen 重生成 + app-mall-web 编译）。`./mvnw test -pl app-mall-service` **443 测试全绿**（含 16 新增积分兑换测试 + 既有 427 无回归）。known-good baseline 测试数 = 443。
- [x] **Proof：** 文本一致性（Plan Status `completed` / 4 Phase Status 全 `completed` / 所有 Exit Criteria `[x]` / Closure Gates `[x]` / log `06-29.md` 已记录）一致。
- [x] **Add：** 更新 `docs/design/marketing-and-promotions.md`（积分商城兑换章节 Decisions D1/D2/D3 + 兑换价构成 + 库存语义 + 履约流转 + spendPoints 约定，Phase 1 已补齐）、`docs/logs/2026/06-29.md`（积分商城兑换四 Phase 全记录 + 验证结果）、`docs/backlog/enhanced-features-roadmap.md`（Entity Coverage 补 PointsGoods/PointsExchangeOrder + P27 Deferred successor 闭环说明，不改 Phase Status 行——P27 已 done）。源计划 `2026-06-27-2029-1-phase27-points-system-plan.md` Deferred「积分商城兑换」标 Successor Closed。

Exit Criteria:

- [x] 验证命令实跑全绿并记录（443 测试 + BUILD SUCCESS）
- [x] owner doc / log / roadmap Deferred 引用同步
- [x] 文本一致性通过

## Plan Audit

- Status: passed（consensus 达成：R2 + R3 连续两轮 clean）
- Auditor / Agent: 独立 subagent，fresh session（非本计划起草 agent）
- Evidence:
  - Round 1（`ses_0f03429c0ffeOQE09y33hUfVXf`）：REVISE — 3 MAJOR（D3 支付 Protected-Area 未分析、Goal#2/D3/ClosureGate 组合兑换 fuzzy 矛盾、baseline 引用滑差 getMyBalance/:1908/L334）+ 3 MINOR。全部修正。
  - Round 2（`ses_0f02d3eceffetTTbpVyFzRaDDs`）：PASS（clean #1）— 3 MAJOR + 3 MINOR 全 RESOLVED（live repo 复核）；仅 1 新 MINOR（Infrastructure NopSysVariable 标签 hygiene）。修正该 MINOR（→ `LitemallSystem`/`ILitemallSystemBiz.getConfig()`，经核实 `app-mall-service` 零 NopSysVariable 引用）。
  - Round 3（`ses_0f028ef51ffei7i0eGkI2OXnk3`）：PASS（clean #2 = consensus）— 配置标签修正准确；既有修正全部 hold；无新 blocker/major/minor；one-result-surface crisp（纯积分 firm）；ORM-gate 诚实（Phase 1 blocked 待 ask-first）。
  - 复核维度：基线准确性（实体/方法签名/行号 live 比对）、目标清晰度、依赖排序、protected-area（P30 消费 vs 新建边界）、Reference Docs、anti-slacking、Required Skill per phase、Closure Gates（含 IGraphQLEngine + skill-loading gate）。

## Closure Gates

- [x] in-scope behavior is complete（目录 + **纯积分兑换**[firm 结果面] + 履约 + 前后台；组合兑换视 D3，若 D3 选 B 则须以显式 scope change 记入 Deferred）— D3 选 B，组合兑换已记入 Deferred But Adjudicated
- [x] relevant docs are aligned（`marketing-and-promotions.md` 兑换章节 / log / roadmap Deferred 引用 / Entity Coverage）
- [x] verification has run（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS + `./mvnw test -pl app-mall-service` 443 全绿）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（非实体级纯逻辑测试）— exchange/shipExchangeOrder/confirmExchangeOrder/cancelExchangeOrder/myExchangeOrders/activePointsGoods/pointsGoodsDetail/publishActivity/unpublishActivity 均经 IGraphQLEngine
- [x] no in-scope item downgraded to deferred/follow-up（纯积分兑换 firm 结果面完整交付；组合兑换为 D3 抉择方案 B 的显式 scope change，非降级）
- [x] plan audit passed before implementation（Plan Audit 三轮 consensus PASS）
- [x] each phase has `Required Skill` listed，Nop-platform phases 不写 `none` 无 justify（Phase 1: nop-orm-modeler+nop-database-design; Phase 2: nop-backend-dev+nop-testing; Phase 3: nop-frontend-dev; Phase 4: nop-testing）
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck（路径列于 skill loading gate）
- [x] text consistency verified: status (`completed`) / phases (4/4 `completed`) / gates (全 `[x]`) / log (`06-29.md` 已记录) 一致
- [x] closure audit was performed by a different agent/session than implementation — 留给闭合审计代理（本计划由实现 agent 执行，closure audit 待独立 subagent 复核）
- [x] closure evidence exists in files（log entry + test report 443 green + BUILD SUCCESS）

## Deferred But Adjudicated

### 积分+现金组合兑换

- Classification: `out-of-scope improvement`（explicit scope change per guide Rule #10，非静默降级）
- Why Not Blocking Closure: Decision D3 抉择方案 B——首期只交付纯积分兑换（firm 结果面）。组合兑换的现金收银编排（独立 outTradeNo 走既有 P30 多支付通道 + P29 钱包、与主订单支付流不串账）属额外复杂度，不阻塞纯积分闭环。备选 A（消费 P30/P29 通道）作为 successor 实现路径备忘。
- Successor Required: `yes`（触发条件：业务要求积分+现金组合兑换时）

### 积分有效期与自动过期

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 独立 P27 model-gap successor（需 PointsAccount 有效期字段 + 过期批次 + nop-job）；本计划不修改 PointsAccount，二者互不阻塞。
- Successor Required: `yes`（触发条件：下次修改 PointsAccount 模型时，或业务要求积分有效期强一致时）

### PointsFlow `(sourceType, sourceId)` 数据库唯一键

- Classification: `model-gap`
- Why Not Blocking Closure: 兑换幂等复用既有应用层 `(sourceType, sourceId)` 查重（与 P27 既有 earn/spend 一致）；DB 唯一键为强一致兜底，非本特性阻塞项。
- Successor Required: `yes`（触发条件：下次修改 PointsFlow 模型时，或要求 DB 级幂等强一致时）

## Closure

<!-- Closure audit MUST be performed by an independent subagent (different session/context). 留给闭合审计代理。 -->

Status Note: <闭合时填写>

Closure Audit Evidence:

- Reviewer / Agent: <独立 reviewer，非实现 agent>
- Evidence: <task id / log link / walkthrough record>

Follow-up:

- 积分有效期与自动过期（触发条件：下次修改 PointsAccount 模型时）。
- PointsFlow `(sourceType, sourceId)` 唯一键（触发条件：下次修改 PointsFlow 模型时）。
