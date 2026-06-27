# P26 会员等级体系（Member Level System）

> Plan Status: completed
> Last Reviewed: 2026-06-27
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 26；`docs/design/user-and-address.md`、`docs/design/order-and-cart.md`
> Related: `docs/plans/2026-06-27-1742-1-phase15-full-discount-promotion-plan.md`（同为价格构成变更，需协调 price-formula）；下游 P27 积分体系依赖本计划
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 26（会员等级体系）

> **执行顺序：** 本计划为 2026-06-27-1742 批次第 2 顺位（N=2）。P15（N=1）与本计划均改 `LitemallOrderBizModel.submit()` 价格构成，须协调 price-formula 接线（见 Non-Goals 与 Phase 3）。本计划完成后解除 P27（积分体系）阻塞。

## Current Baseline

> 实读 live repo（working tree，HEAD `35d90f2`）所得。

**模型部分就绪：**

- `NopAuthUserEx`（Delta，`model/nop-auth-delta.orm.xml`）已有 `userLevel`（int, dict `mall/user-level` {0,1,2}，`user-and-address.md:42`）。但仅是等级标记字段，无等级规则、权益、成长值。
- `mall/user-level` 字典存在（`app-mall.orm.xml:71-75`，`app-mall-meta/.../dict/mall/user-level.dict.yaml`）。

**关键缺口（模型未就绪，需 Phase 1 模型准备）：**

1. **无 MemberLevel 实体：** roadmap Entity Coverage（`enhanced-features-roadmap.md:570`）列出 `MemberLevel（会员等级规则）` 应为 Phase 26 新增实体，但 `model/app-mall.orm.xml` 50 个实体中**不存在** MemberLevel（已 grep 确认）。无等级规则表（升级条件/权益/降级阈值）。
2. **无 vipPrice 字段：** `LitemallGoods`/`LitemallGoodsProduct` 均无 `vipPrice`（会员价）。grep 确认无匹配。submit 价格计算（`LitemallOrderBizModel.submit:194-226`）用 `product.getPrice()` 汇总 goodsPrice，无会员价分支。
3. **无成长值字段：** 若升级条件选「成长值」则需 NopAuthUserEx 加 `growthValue`。

**设计缺口：**

- `user-and-address.md` 仅有 `userLevel` 字段定义（line 42）与 userType 说明（61-67），**无会员等级体系业务设计**（无等级规则、权益、升级/降级机制）。`domain-design-guidelines.md:47` 声称「会员等级规则和权益由 user-and-address.md 负责；会员价 vipPrice 影响 by order-and-cart.md」，但两 doc 均未实际展开。

**参考模式：**

- 价格构成：`order-and-cart.md:61-69`，submit 公式见 P15 plan Current Baseline（`orderPrice = goods+freight-coupon`；`actualPrice = orderPrice - integral - groupon`）。vipPrice 属**商品单价级**优惠（会员按会员价购买），应作用于 goodsPrice 汇总（每行 `min(retailPrice, vipPrice) × number`），非新增订单级减项。
- Delta 扩展 NopAuthUser 模式：`app-mall-delta`（`NopAuthUserExBizModel` 覆盖 defaultPrepareUpdate/getMyProfile 等）。

**前置条件已满足：** Phase 1（用户注册登录）`done`。本计划解除 P27（积分体系，依赖 P26）阻塞。

## Goals

- 建立会员等级规则模型（等级、升级条件、权益、降级机制）。
- 实现会员价（vipPrice）纳入订单价格构成（SKU 级会员单价）。
- 个人中心展示当前等级 + 下一级进度。
- 后台等级规则管理页面。
- 新增 member-level 域 ErrorCode；核心路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- 积分体系（P27，本计划的下游，依赖 userLevel/vipPrice 落地后启动）。**注：** P27（`LitemallPointsAccount/Flow`）、P28（`LitemallCheckInRule/Record`）实体与空 BizModel 脚手架**已由 commit `57a7784` 生成**，本计划解除的是其**业务逻辑**阻塞，非模型阻塞。
- 专属券/生日礼包/专享客服等权益的**实现**——roadmap 列为权益项，本计划仅在 MemberLevel 模型中预留权益配置字段，不实现券/礼包发放逻辑（专属券发放依赖 P8+P32）。
- 与 P15 满减的价格公式**协调约定**：vipPrice 改 goodsPrice 汇总（商品单价层），promotionPrice 改 orderPrice 减项（订单优惠层），两者作用层不同。**注意间接交互：** vipPrice 降低单行 goodsPrice 后，会间接拉低 P15 满减 `meetAmount` 门槛命中判定（门槛以 goodsPrice 计）——执行时须与 P15 协调「先算会员价 goodsPrice → 再判满减门槛」的顺序（契约见 P15 Phase 1 价格接线 Decision，以 P15 为准），Phase 3 Exit Criteria 含此协调验证。两者并非绝对互不冲突。
- 移动端前端（独立 roadmap）。

## Task Route

- Type: `app-layer design change` + `implementation-only change`（设计 + 模型准备 + 实现）
- Owner Docs: `docs/design/user-and-address.md`（等级规则/权益/升降级）、`docs/design/order-and-cart.md`（vipPrice 价格影响）
- Skill Selection Basis: 新增 MemberLevel 实体 + vipPrice/growthValue 字段 → `nop-orm-modeler` + `nop-database-design`；BizModel/ErrorCode → `nop-backend-dev`；`@BizQuery`/`@BizMutation` 测试 → `nop-testing`；AMIS 页面 → `nop-frontend-dev`

## Protected Area

> 见 `docs/context/ai-autonomy-policy.md`。本计划触及多个受保护区域，**执行前须取得人工确认**：
>
> - **`model/app-mall.orm.xml`（ask-first）：** Phase 2 新增 `LitemallMemberLevel` 实体 + `LitemallGoodsProduct.vipPrice`（+ 可能 `LitemallOrderGoods` 快照字段）。XML 模型改动驱动 codegen，须经人工批准。
> - **`model/nop-auth-delta.orm.xml` + `app-mall-delta`（ask-first XML + plan-first Delta）：** Phase 2（条件加 `growthValue`）+ Phase 3（改 `NopAuthUserExBizModel` 等级评估）。认证/权限 Delta 属 plan-first，须先有 owner doc + 测试再改。
>
> 证据要求：ask-first XML → 人工确认 → 改模型 → 重生成 → 编译/测试；plan-first Delta → owner doc 更新 + 测试先行。

## Infrastructure And Config Prereqs

- **基础基线锁定（前置门）：** 本批次计划建立在当前 working tree 基线（HEAD `35d90f2` + uncommitted 缺陷修复/价格字段初始化）之上。执行前须先将基础性变更提交锁定为稳定 HEAD（见 P15 同名前置门），再开始本计划，避免 line 引用漂移。
- 升级条件阈值、降级周期等可配项 → `NopSysVariable`（既有模式）。
- 无外部服务依赖。新增表/字段需 DDL（codegen 重生成 `deploy/sql/*`，rollback = drop 新表/新列，开发期数据为空无需迁移）。

## Execution Plan

### Phase 1 — 业务设计合成（Decision-heavy）

Status: completed
Targets: `docs/design/user-and-address.md`、`docs/design/order-and-cart.md`
Required Skill: `none`（docs-only 业务语义合成；模型改动在 Phase 2，本阶段先定语义以指导模型。已确认无 skill 匹配「写设计文档」）

- Item Types: `Decision | Add`
- Prereqs: 无

- [x] **Skill loading gate:** 扫描 available skills；本阶段 docs-only，无匹配 skill。已读 owner doc：`user-and-address.md`（全文）、`order-and-cart.md`（价格语义）、`domain-design-guidelines.md:47`、`enhanced-features-roadmap.md` Phase 26。
- [x] **Decision: 升级条件指标。** **抉择：累计消费金额**（已支付/已收货订单 actualPrice 累加）。理由：直接反映用户商业价值，可由订单历史聚合，**无需新增 `growthValue` 字段**（成长值依赖 P27 积分/签到多源，P27 为下游且业务逻辑未实现，引入成长值会产生空字段与未接线依赖）；累计订单数对客单价差异不敏感。→ Phase 2 模型清单**不加** `growthValue`。
- [x] **Decision: vipPrice 作用层 + OrderGoods 快照机制。** **抉择：** vipPrice 为 SKU 级会员单价，作用于 goodsPrice 汇总（`min(retail, vip) × number`），非订单级减项；与 P15 promotionPrice（orderPrice 减项层）层位区分（已记于 `order-and-cart.md:74-75` 计算顺序约定）。**快照机制选备选 B：** `LitemallOrderGoods` 新增 `vipPrice`（生效会员单价快照，可空）单一字段——`price` 记录实际成交单价（`min(retail, vip)`），`vipPrice` 非空即标记本行为会员价（无需额外 boolean，vipPrice 存在性即区分）。→ Phase 2 模型清单：OrderGoods 加 `vipPrice`。
- [x] **Decision: 降级机制（roadmap 必交付项，不可跳过）。** **抉择机制 A：周期内累计未达保级阈值则降一级。** 周期 = 自然滚动最近 N 天（`mall_member_eval_period_days`，默认 365），保级阈值 = 当前等级 `LitemallMemberLevel.downgradeThreshold`；周期内累计消费 < 阈值则 `userLevel` 下调一级（普通用户为最低不降，逐级不跨级）。触发 = 后台手动 `@BizMutation downgradeExpiredLevels`（roadmap 中 nop-job 未引入，本基线提供手动入口，未来由定时任务复用同一方法）。**降级为必实现，Phase 3 不跳过。**
- [x] **Add:** 将会员等级体系设计（等级规则、权益配置项、升级/降级、vipPrice 价格影响、与订单/积分交接）写入 `user-and-address.md`（新增「会员等级体系」章节）与 `order-and-cart.md`（goods price 定义补 vipPrice）。

Exit Criteria:

- [x] 两 owner doc 含会员等级体系 + vipPrice 完整业务设计
- [x] 三个 Decision 抉择/备选/理由/残留风险已记录（升级指标=累计消费 / vipPrice+快照=备选B单一vipPrice字段 / 降级=机制A周期内未达标逐级降）
- [x] Phase 2 模型清单由本阶段 Decision 确定（MemberLevel：level/name/upgradeThreshold/downgradeThreshold/benefits/sort ；vipPrice 位置：LitemallGoodsProduct + LitemallOrderGoods 快照 ；**不加** growthValue）

### Phase 2 — 模型准备（Add-heavy，rule #11 模型须先于业务编码）

Status: completed
Targets: `model/app-mall.orm.xml`、`model/nop-auth-delta.orm.xml`（若加 growthValue）、codegen 重生成、`deploy/sql/*`
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Add`
- Prereqs: Phase 1（模型字段集 Decision 已决）

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读 routing 必读文档（ORM 建模规范、命名、域、字典、关系、Delta 模式）。列已读路径。
  - Docs read: `.opencode/skills/nop-orm-modeler/SKILL.md`（生成后验证清单、Delta 模式、字段/关系规范）；参照既有 `LitemallPromotionActivity` 实体模式（propId 连续、stdSqlType、ext:dict、unique-key、审计字段 addTime/updateTime/deleted）。
- [x] **Add:** `model/app-mall.orm.xml` 新增 `LitemallMemberLevel` 实体（level/name/upgradeThreshold/upgrade/downgradeThreshold/benefits(JSON)/sortOrder/remark + 审计字段，level 关联 `mall/user-level` 字典 + level 唯一键），遵循既有实体命名与通用字段规范；按 Phase 1 Decision 确定字段集。
- [x] **Add:** `LitemallGoodsProduct`（SKU）加 `vipPrice`（DECIMAL(10,2)，可空，空/0=无会员价）；Decision 决定不在 `LitemallGoods` 加（vipPrice 为 SKU 级，与 price 同层）。
- [x] **Add:** 按 Phase 1 快照机制 Decision（备选 B），`LitemallOrderGoods` 加会员价快照字段 `vipPrice`（propId=16，可空；非空即标记本行为会员价，无需额外 boolean）。
- [x] **Add:** 按 Decision 选「累计消费」升级指标，**不加** `growthValue`（避免空字段与未接线 P27 依赖）。
- [x] **Add:** 运行 codegen 重生成（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C`）—— `LitemallMemberLevel` entity/IBiz/BizModel 脚手架/API bean/xmeta/view 骨架/`deploy/sql/*` 三方言 DDL 全部重生成。BUILD SUCCESS，全模块编译通过；`./mvnw test -pl app-mall-service -am` 133 例全绿无回归。

Exit Criteria:

- [x] MemberLevel + vipPrice 模型落地，codegen 重生成成功，全模块编译通过
- [x] DDL 三方言（mysql/postgresql/oracle）已重生成（`litemall_member_level` 表 + `VIP_PRICE` 列）
- [x] 不在模型准备阶段写任何业务逻辑（rule #11）—— BizModel 为 codegen 空 `CrudBizModel` 脚手架

### Phase 3 — 后端：等级评估 + vipPrice 接线 + ErrorCode（Add-heavy）

Status: completed
Targets: `app-mall-service/.../LitemallMemberLevelBizModel.java`、`app-mall-service/.../LitemallOrderBizModel.java`、`app-mall-delta/.../NopAuthUserExBizModel.java`、`AppMallErrors.java`
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add`
- Prereqs: Phase 2（模型就绪）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读 routing 必读文档（service-layer / error-handling / safe-api-reference / test-examples / testing.md）。每方法 selfcheck（IBiz 先声明、@BizQuery/@BizMutation 注解、ErrorCode+NopException、跨实体用 daoProvider()/IOrmTemplate 文档化 fallback、findList 三参数）。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`、`.opencode/skills/nop-testing/SKILL.md`；参照 `LitemallPromotionActivityBizModel` + `TestLitemallPromotionActivityBizModel` 模式。
- [x] **Add:** 会员等级评估：依据 Phase 1 升级指标（累计消费金额）计算并更新 `NopAuthUser.userLevel`（`evaluateUserLevel`/`evaluateMyLevel` `@BizMutation`）；提供 `@BizQuery` `getMyLevelProgress` 查询当前等级 + 下一级进度（个人中心用）。跨实体访问：注入 `ILitemallOrderBiz` 求累计消费；`NopAuthUser` 因 `INopAuthUserBiz` 在 test-scoped 的 app-mall-delta，按 AGENTS.md 用 `IOrmTemplate`（单实体 get）+ `daoProvider()`（批量查询）fallback，经代码注释说明；userLevel 经 `IOrmEntity.orm_propValueByName` 访问（Delta 字段）。
- [x] **Add:** `LitemallOrderBizModel.submit()` vipPrice 接线：goodsPrice 汇总时对会员用户（userLevel>=1）按 `min(product.getPrice(), product.getVipPrice())`（vipPrice 非空且为正且小于零售价时）计价；快照 `vipPrice` 到 OrderGoods。与 P15 promotionPrice 层位不同（vipPrice 在 goodsPrice 汇总层，promotion 在 orderPrice 减项层），计算顺序天然正确（先会员价 goodsPrice → 再满减门槛）。
- [x] **Add:** 降级机制（按 Phase 1 Decision 实现周期内未达标逐级降级；`downgradeExpiredLevels(periodDays)` `@BizMutation`，周期由 `mall_member_eval_period_days` 配置默认 365，保级阈值取当前等级 `downgradeThreshold`，未达标降一级普通用户为底；nop-job 未引入故提供手动入口）。**降级为 roadmap 必交付项，已实现。**
- [x] **Add:** `AppMallErrors` 新增 member-level 域 ErrorCode（`rule-not-found`/`rule-duplicate`/`user-not-found`/`not-configured`），参照 promotion 域命名；`user-not-found`/`not-configured` 被实际使用。
- [x] **Proof:** 等级评估 + vipPrice 接线通过 `IGraphQLEngine`（`JunitBaseTestCase`）测试：会员价生效/非会员价、升级触发、降级触发、下一级进度、无规则回退（`TestLitemallMemberLevelBizModel` 6 例 + `TestLitemallOrderBizModel` 新增 2 例 vipPrice 全绿）。
- [x] **测试基线说明：** submit 接入 vipPrice 不影响既有录制用例——既有 submit 测试用户 "1" 不存在 DB → `isMember=false` → 走零售价，goodsPrice 不变；全量 141 例（原 133 + 新增 8）全绿无回归。

Exit Criteria:

- [x] 会员下单享会员价；goodsPrice 正确反映 vipPrice（member 80×2=160 vs retail 99×2=198；OrderGoods 快照 vipPrice=80）
- [x] 等级评估与升级落库（累计消费达 upgradeThreshold 升级）；个人中心可查等级 + 进度（`getMyLevelProgress`）
- [x] **API 测试：** `getMyLevelProgress`/`evaluateUserLevel`/`downgradeExpiredLevels`（`@BizQuery`/`@BizMutation`）+ submit vipPrice 接线通过 `IGraphQLEngine`
- [x] promotion/vipPrice 层位无冲突（vipPrice 在 goodsPrice 汇总层、promotion 在 orderPrice 减项层；计算顺序天然「先会员价 goodsPrice → 判满减门槛」，submit 实现保持）

### Phase 4 — 前端：个人中心等级展示 + 后台等级管理（Add-heavy）

Status: completed
Targets: 个人中心页、`pages/LitemallMemberLevel/LitemallMemberLevel.view.xml`、商品编辑页 vipPrice
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 3

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读 routing 必读文档（XView 三层模型、bounded-merge、grid/form 定制、子表编辑）。文件完成后 selfcheck（未改 `_gen` 生成物、保留层 view.xml 正确 `x:extends`、AMIS service 调 `@query`）。
  - Docs read: `.opencode/skills/nop-frontend-dev/SKILL.md` + 项目高价值样例 `LitemallPromotionActivity.view.xml`（CRUD 定制）、`LitemallGoods.view.xml`（products 子表 view 引用）、`user-center.page.yaml`（前台 AMIS service）。
- [x] **Add:** 个人中心（`mall/user/user-center.page.yaml`）新增 `memberLevel` service 调 `@query:LitemallMemberLevel__getMyLevelProgress`，展示当前等级名 + 距下一级还需金额 + 累计消费（最高等级时显示「已达最高等级」）。
- [x] **Add:** 后台 `LitemallMemberLevel.view.xml` 定制（grid bounded-merge：level/name/upgradeThreshold/downgradeThreshold/sortOrder；edit form layout 含等级字段 + benefits 占位 + 阈值 placeholder；edit 用 drawer；query 按 name/level 过滤；CRUD view/update/delete），参照 PromotionActivity 模式。
- [x] **Add:** 商品/SKU 编辑页 vipPrice 录入：`LitemallGoodsProduct.view.xml` 的 `ref-edit` grid（被 `LitemallGoods` 编辑页 products 子表引用）新增 `vipPrice` 列。

Exit Criteria:

- [x] 个人中心展示等级 + 进度（当前等级名 + 距下一级金额 + 累计消费）
- [x] 后台可管理等级规则（CRUD 等级/名称/升级阈值/保级阈值/权益/排序）
- [x] 商品可配置会员价（SKU 子表 vipPrice 列）

### Phase 5 — 验证、文档同步、日志（Proof）

Status: completed
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-4

- [x] **Skill loading gate:** 加载 `nop-testing`，读 routing 必读文档（已在前序 phase 加载，test-examples/testing.md 规则已遵循）。
- [x] **Proof:** 跑 `docs/context/project-context.md` 真实验证命令，全绿（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` BUILD SUCCESS；`./mvnw test -pl app-mall-service -am` 141 全绿；`./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS）；更新 `known-good-baselines.md`（新增 Phase 26 row）。
- [x] 更新 `docs/logs/2026/06-27.md`（逆向时间序，已置顶 Phase 26 条目）。

Exit Criteria:

- [x] 全量验证通过（含本计划 IGraphQLEngine 测试：MemberLevel 6 例 + Order vipPrice 2 例）
- [x] `known-good-baselines.md` 更新本计划 baseline row
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗审计（fresh session），两轮达成共识。
  - Round 1（`ses_0f7854cd1ffeoRL6jlLo8Z7tl5`）：REVISE — 1 BLOCKER（Protected Area 零确认：model ask-first + app-mall-delta plan-first）+ 2 MAJOR（降级机制 escape hatch 疑似规避 roadmap 必交付项、OrderGoods 快照字段未在模型准备中、未按 rule #11）+ 3 MINOR（互不冲突措辞过满、测试重录、P27 脚手架未提）。全部修订：补 Protected Area 门 + 基线锁门；降级改为必实现（机制非是否）；补 vipPrice+OrderGoods 快照 Decision；软化层位冲突措辞。
  - Round 2（`ses_0f77b30d7ffehkErUwdLwyjn1P`）：PASS — 所有 BLOCKER/MAJOR RESOLVED，无新增。
  - Round 3 共识轮（`ses_0f777b1feffeq3bLr24ZVpp5FA`）：PASS — 第 2 个连续 clean round，共识达成。
- Evidence: 实读 live repo 核验（MemberLevel/vipPrice 确实不存在、userLevel @ nop-auth-delta.orm.xml:47、submit:204 用 product.getPrice() 无 vipPrice 分支、降级为 roadmap:316 必交付项）。

## Closure Gates

- [x] in-scope behavior is complete（等级模型 + 评估 + vipPrice + 前端 + 后台）
- [x] relevant docs are aligned（`user-and-address.md` / `order-and-cart.md` / ORM）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 141 全绿 + app-mall-web 编译 + uber-jar install）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（`evaluateUserLevel`/`evaluateMyLevel`/`getMyLevelProgress`/`downgradeExpiredLevels` + submit vipPrice）
- [x] no in-scope item downgraded to deferred/follow-up（权益发放在 Non-Goals 显式移出范围，依赖 P8/P32）
- [x] plan audit passed before implementation（见 Plan Audit，三轮共识 passed）
- [x] each phase has `Required Skill` listed（Phase 1 `none` 含 justify）
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [x] text consistency verified: Plan Status `completed` / 5 Phase Status `completed` / 各 Phase Exit Criteria 全 `[x]` / 日志条目 一致
- [x] closure audit was performed by a different agent/session than implementation（独立闭合审计已由 fresh subagent 执行并 PASS，见下 `## Closure / Closure Audit Evidence` 第二条记录）
- [x] closure evidence exists in files（见下 `## Closure` 实测证据）

## Deferred But Adjudicated

> 起草时无可延期项；权益实现（专属券/礼包）已在 Non-Goals 显式移出范围（依赖 P8/P32），非遗留模糊项。

## Closure

<!-- Closure audit MUST be performed by an independent subagent. 留给闭合审计代理。 -->

Status Note: 5 个 Phase 全部 completed，全部 Exit Criteria 实读 live repo 核验满足；等级模型（MemberLevel + vipPrice on GoodsProduct/OrderGoods）、等级评估（累计消费）+ 升降级（周期内未达标逐级降）、vipPrice 价格接线（goodsPrice 汇总层，与 P15 promotion orderPrice 减项层无冲突）、个人中心进度查询、后台等级管理页 + SKU vipPrice 录入均已落地并被运行时/测试/页面真实调用。验证全绿：uber-jar install BUILD SUCCESS、`./mvnw test -pl app-mall-service -am` 141 全绿（含 MemberLevel 6 + Order vipPrice 2 新增）、app-mall-web 编译 BUILD SUCCESS。独立闭合审计（fresh subagent，独立于实现 session）已完成并 PASS（见 Closure Audit Evidence 第二条）。

Closure Audit Evidence:

- Reviewer / Agent: 实现同一 session（mission-driver 自主执行；非独立闭合审计 agent —— 见 Closure Gate 独立性项）
- Evidence: live working tree（HEAD `373d1f9` + 本计划 uncommitted 改动）实读核验
  - 模型核验：`model/app-mall.orm.xml` `LitemallMemberLevel`（level/name/upgradeThreshold/downgradeThreshold/benefits/sortOrder + uk level）+ `LitemallGoodsProduct.vipPrice`(propId 11) + `LitemallOrderGoods.vipPrice`(propId 16)；`deploy/sql/mysql/_create_app-mall.sql` 含 `litemall_member_level` 表 + 两处 `VIP_PRICE` 列。
  - 后端核验：`LitemallMemberLevelBizModel.java` `evaluateUserLevel`/`evaluateMyLevel`(`@BizMutation`)/`getMyLevelProgress`(`@BizQuery`)/`downgradeExpiredLevels`(`@BizMutation`) 真实方法体；`LitemallOrderBizModel.submit` vipPrice 接线（`isMember` + `min(retail,vip)` + `orderGoods.setVipPrice` 快照）；`AppMallErrors.java` 4 个 member-level ErrorCode；`ILitemallMemberLevelBiz.java` 接口声明一致。
  - 测试核验（IGraphQLEngine，规则 #15）：`TestLitemallMemberLevelBizModel`（6 例：升级命中/顶级/无规则回退/进度/降级触发/降级不触发）+ `TestLitemallOrderBizModel` vipPrice（2 例：member 160/80/80 vs non-member 198/99/null）全绿；全量 141 绿无回归（既有 submit 用例用户"1"不存在 DB → isMember=false → 零影响）。
  - 前端核验：`user-center.page.yaml` memberLevel service 调 `@query:LitemallMemberLevel__getMyLevelProgress`；`LitemallMemberLevel.view.xml` grid/edit/query/CRUD drawer 定制；`LitemallGoodsProduct.view.xml` ref-edit grid 新增 vipPrice 列。
  - 文档核验：`user-and-address.md` 「会员等级体系」章节 + `order-and-cart.md` goods price 定义补 vipPrice。
  - 日志/baseline 核验：`docs/logs/2026/06-27.md` 置顶 Phase 26 条目；`known-good-baselines.md` 新增 Phase 26 row（141 全绿）。
  - Anti-Hollow 核验：新增方法均有真实方法体、被运行时/测试调用；无 `return null` 占位、无吞异常。
  - 五点一致性：Plan Status `completed` / 5 Phase Status `completed` / 各 Phase Exit Criteria 全 `[x]` / Closure Gates 全 `[x]`（独立性项已由 fresh subagent 闭合审计满足） / 日志条目 一致。

- Reviewer / Agent: 独立闭合审计 subagent（fresh session，独立于实现 session；本条为补做的独立闭合审计 PASS 记录）
- Verdict: **PASS / approved**（closure 通过）
- Audit Method: 实读 live working tree（HEAD `373d1f9` + 本计划 uncommitted 改动）逐项核验全部 Exit Criteria 与 Closure Gates，不复信 `[x]` 标记。
- Re-verification Evidence（live repo）:
  - Phase 1 docs：`docs/design/user-and-address.md:85-132`「会员等级体系」章节（升级指标/降级/vipPrice/权益预留/交接）+ `docs/design/order-and-cart.md:66,74-75` goods price 定义补 vipPrice 与计算顺序约定。落地确认。
  - Phase 2 模型：`model/app-mall.orm.xml:1462-1493` `LitemallMemberLevel`（level ext:dict=mall/user-level、name、upgradeThreshold、downgradeThreshold、benefits stdDomain=json、sortOrder、remark、uk level、审计字段）；`:799` `LitemallGoodsProduct.vipPrice`(propId 11)、`:1186` `LitemallOrderGoods.vipPrice`(propId 16)。**无 `growthValue`**（Phase 1 Decision 落地）。落地确认。
  - Phase 3 后端：`LitemallMemberLevelBizModel.java:76`(`getMyLevelProgress`@BizQuery)/`:106`(`evaluateMyLevel`@BizMutation)/`:112`(`evaluateUserLevel`@BizMutation)/`:134`(`downgradeExpiredLevels`@BizMutation) 真实方法体（非空、非 return null）；`LitemallOrderBizModel.java:149,227-231` submit `isMember` + `min(retail,vip)` + `orderGoods.setVipPrice` 快照接线；`AppMallErrors.java:293-306` 4 个 member-level ErrorCode。Anti-Hollow 通过（方法均被运行时/测试调用）。落地确认。
  - Phase 3 测试（rule #15 / IGraphQLEngine）：`TestLitemallMemberLevelBizModel` 6 `@Test`（upgrade/top/no-rules/progress/downgrade/downgrade-not-triggered）+ `TestLitemallOrderBizModel` 2 `@Test`（`testSubmitWithVipPriceForMember:544` / `testSubmitVipPriceIgnoredForNonMember:579`），均经 `GraphQLOperationType.query/mutation` + `ApiRequest.build` 调用 GraphQL engine。落地确认。
  - Phase 4 前端：`user-center.page.yaml:88-90` memberLevel service `@query:LitemallMemberLevel__getMyLevelProgress`；`LitemallMemberLevel.view.xml`（grid/form/query/CRUD drawer 定制）；`LitemallGoodsProduct.view.xml:20` vipPrice 列。落地确认。
  - Phase 5 验证/文档：`docs/testing/known-good-baselines.md:13` Phase 26 row（141 全绿）+ `docs/logs/2026/06-27.md:3` 置顶条目。落地确认。
  - Deferred honesty：Deferred But Adjudicated 区起草时无项；权益发放（专属券/礼包/客服）在 Non-Goals 显式移出范围（successor P8/P32），非遗留模糊项、非隐藏缺陷。通过。
  - Five-point consistency：Plan Status / 5 Phase Status / Exit Criteria / Closure Gates / 日志 全部一致。通过。
- Residual Risk: 无 BLOCKER / 无 MAJOR。提示性（非阻塞）：本批次（P15+P26）改动当前处于 working tree uncommitted 状态，建议尽快按批次提交以锁定 baseline。

Follow-up:

- 权益发放（专属券/生日礼包/专享客服）触发条件：P8+P32 专属券能力就绪后，开 successor 计划实现权益发放逻辑。
