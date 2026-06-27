# P26 会员等级体系（Member Level System）

> Plan Status: active
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

Status: planned
Targets: `docs/design/user-and-address.md`、`docs/design/order-and-cart.md`
Required Skill: `none`（docs-only 业务语义合成；模型改动在 Phase 2，本阶段先定语义以指导模型。已确认无 skill 匹配「写设计文档」）

- Item Types: `Decision | Add`
- Prereqs: 无

- [ ] **Skill loading gate:** 扫描 available skills；本阶段 docs-only，无匹配 skill。已读 owner doc：`user-and-address.md`（全文）、`order-and-cart.md`（价格语义）、`domain-design-guidelines.md:47`、`enhanced-features-roadmap.md` Phase 26。
- [ ] **Decision: 升级条件指标。** 备选：累计消费金额 / 累计订单数 / 成长值（growth value）。成长值最灵活（可含签到/评价等多源贡献，与 P27 积分正交互补）但需新字段；累计消费最简。记录抉择 + 理由 + 是否需 `growthValue` 字段（影响 Phase 2 模型）。
- [ ] **Decision: vipPrice 作用层 + OrderGoods 快照机制。** 确认 vipPrice 为 SKU 级会员单价，作用于 goodsPrice 汇总（`min(retail, vip) × number`），非订单级减项。记录与 P15 promotionPrice 的层位区分。**快照机制（必决，影响 Phase 2 模型）：** 备选 A：`LitemallOrderGoods` 现有 `price` 字段直接写会员价（最简，但丢失「是否会员价」标记，退款/等级回算/展示无法区分）；备选 B：OrderGoods 新增 `vipPrice`/`isMemberPrice` 快照字段（保留区分，推荐，支撑退款额与「会员价」标签展示）。记录抉择 + 理由 + 对 Phase 2 模型清单的影响。
- [ ] **Decision: 降级机制（roadmap 必交付项，不可跳过）。** 按 roadmap Phase 26 交付范围，降级机制为**必实现**。本 Decision 确定**机制**而非是否实现：备选 A：周期内（如自然年）累计未达当前等级阈值则降一级；备选 B：仅累计消费回退超阈值时降级。记录抉择 + 触发周期/阈值来源（NopSysVariable）+ 周期重置方式（定时任务，依赖 Phase 11 的 nop-job 调度或手动 BizMutation）。**若评估后认为不应实现降级，则属范围变更，须人工确认 + 更新 `implementation-roadmap.md` Phase 26 交付清单，不得在本 plan 内静默跳过。**
- [ ] **Add:** 将会员等级体系设计（等级规则、权益配置项、升级/降级、vipPrice 价格影响、与订单/积分交接）写入 `user-and-address.md` 与 `order-and-cart.md`。

Exit Criteria:

- [ ] 两 owner doc 含会员等级体系 + vipPrice 完整业务设计
- [ ] 三个 Decision 抉择/备选/理由/残留风险已记录（升级指标 / vipPrice+快照 / 降级机制——降级为必实现，抉择的是机制非是否）
- [ ] Phase 2 模型清单由本阶段 Decision 确定（MemberLevel 字段集 / vipPrice 位置 / OrderGoods 快照字段 / 是否 growthValue）

### Phase 2 — 模型准备（Add-heavy，rule #11 模型须先于业务编码）

Status: planned
Targets: `model/app-mall.orm.xml`、`model/nop-auth-delta.orm.xml`（若加 growthValue）、codegen 重生成、`deploy/sql/*`
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Add`
- Prereqs: Phase 1（模型字段集 Decision 已决）

- [ ] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读 routing 必读文档（ORM 建模规范、命名、域、字典、关系、Delta 模式）。列已读路径。
  - Docs read: <执行时填入>
- [ ] **Add:** `model/app-mall.orm.xml` 新增 `LitemallMemberLevel` 实体（等级 level/name/升级阈值/权益配置 JSON/降级阈值/sort），遵循既有实体命名与通用字段（addTime/deleted 等）规范；按 Phase 1 Decision 确定字段集。
- [ ] **Add:** `LitemallGoodsProduct`（SKU）加 `vipPrice`（DECIMAL，可空，空=无会员价）；按 Decision 决定是否在 `LitemallGoods` 亦加。
- [ ] **Add:** 按 Phase 1 快照机制 Decision，若选备选 B 则 `LitemallOrderGoods` 加会员价快照字段（`vipPrice`/`isMemberPrice`），支撑退款额计算与「会员价」展示标签。
- [ ] **Add:** 按 Decision，若选成长值则 `nop-auth-delta.orm.xml` NopAuthUserEx 加 `growthValue`。
- [ ] **Add:** 运行 codegen 重生成（dao entity / API bean / BizModel 脚手架 / view 骨架 / `deploy/sql/*` 三方言 DDL）。验证 `./mvnw install -pl app-mall-dao -am` BUILD SUCCESS，`_app.orm.xml` 重生成无错。

Exit Criteria:

- [ ] MemberLevel + vipPrice(+growthValue?) 模型落地，codegen 重生成成功，全模块编译通过
- [ ] DDL 三方言（mysql/postgresql/oracle）已重生成
- [ ] 不在模型准备阶段写任何业务逻辑（rule #11）

### Phase 3 — 后端：等级评估 + vipPrice 接线 + ErrorCode（Add-heavy）

Status: planned
Targets: `app-mall-service/.../LitemallMemberLevelBizModel.java`、`app-mall-service/.../LitemallOrderBizModel.java`、`app-mall-delta/.../NopAuthUserExBizModel.java`、`AppMallErrors.java`
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add`
- Prereqs: Phase 2（模型就绪）

- [ ] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读 routing 必读文档。每方法 selfcheck。
  - Docs read: <执行时填入>
- [ ] **Add:** 会员等级评估：依据 Phase 1 升级指标（累计消费/订单数/成长值）计算并更新 `NopAuthUser.userLevel`；提供 `@BizQuery` 查询当前等级 + 下一级进度（个人中心用）。跨实体访问注入 `ILitemallMemberLevelBiz` / `INopAuthUserBiz`（遵循 AGENTS.md 跨实体规则）。
- [ ] **Add:** `LitemallOrderBizModel.submit()` vipPrice 接线：goodsPrice 汇总时对会员用户按 `min(product.getPrice(), product.getVipPrice())`（vipPrice 非空时）计价；快照会员价到 OrderGoods。与 P15 promotionPrice 接线协调（层位不同，见 Non-Goals）。
- [ ] **Add:** 降级机制（按 Phase 1 Decision 实现周期内未达标降级；周期重置走定时任务或手动 BizMutation，依赖 Phase 11 调度装配或本 plan 提供手动入口）。降级为 roadmap 必交付项，**不跳过**。
- [ ] **Add:** `AppMallErrors` 新增 member-level 域 ErrorCode。
- [ ] **Proof:** 等级评估 + vipPrice 接线通过 `IGraphQLEngine`（`JunitAutoTestCase`）测试：会员价生效/非会员价、升级触发、降级触发、下一级进度、vipPrice 缺省回退零售价。
- [ ] **测试基线说明：** submit 接入 vipPrice 会改变 goodsPrice 录制输出；既有 `TestLitemallOrderBizModel` 等订单提交 `IGraphQLEngine` 录制用例若受影响，在本 phase 重录（与 P15 Phase 2 的重录协调，避免冲突）。

Exit Criteria:

- [ ] 会员下单享会员价；goodsPrice 正确反映 vipPrice
- [ ] 等级评估与升级落库；个人中心可查等级 + 进度
- [ ] **API 测试：** `@BizQuery`（等级/进度）+ submit vipPrice 接线通过 `IGraphQLEngine`
- [ ] promotion/vipPrice 层位无冲突（与 P15 协调验证）

### Phase 4 — 前端：个人中心等级展示 + 后台等级管理（Add-heavy）

Status: planned
Targets: 个人中心页、`pages/LitemallMemberLevel/LitemallMemberLevel.view.xml`、商品编辑页 vipPrice
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 3

- [ ] **Skill loading gate:** 加载 `nop-frontend-dev`，读 routing 必读文档。文件完成后 selfcheck。
  - Docs read: <执行时填入>
- [ ] **Add:** 个人中心展示当前等级 + 下一级进度（调等级/进度 `@BizQuery`）。
- [ ] **Add:** 后台 MemberLevel 管理页面（等级规则 CRUD、权益配置、阈值）。
- [ ] **Add:** 商品/SKU 编辑页 vipPrice 录入。

Exit Criteria:

- [ ] 个人中心展示等级 + 进度
- [ ] 后台可管理等级规则
- [ ] 商品可配置会员价

### Phase 5 — 验证、文档同步、日志（Proof）

Status: planned
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-4

- [ ] **Skill loading gate:** 加载 `nop-testing`，读 routing 必读文档。
- [ ] **Proof:** 跑 `docs/context/project-context.md` 真实验证命令，全绿；更新 `known-good-baselines.md`。
- [ ] 更新 `docs/logs/2026/{MM}-{DD}.md`。

Exit Criteria:

- [ ] 全量验证通过（含本计划 IGraphQLEngine 测试）
- [ ] `known-good-baselines.md` 更新
- [ ] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗审计（fresh session），两轮达成共识。
  - Round 1（`ses_0f7854cd1ffeoRL6jlLo8Z7tl5`）：REVISE — 1 BLOCKER（Protected Area 零确认：model ask-first + app-mall-delta plan-first）+ 2 MAJOR（降级机制 escape hatch 疑似规避 roadmap 必交付项、OrderGoods 快照字段未在模型准备中、未按 rule #11）+ 3 MINOR（互不冲突措辞过满、测试重录、P27 脚手架未提）。全部修订：补 Protected Area 门 + 基线锁门；降级改为必实现（机制非是否）；补 vipPrice+OrderGoods 快照 Decision；软化层位冲突措辞。
  - Round 2（`ses_0f77b30d7ffehkErUwdLwyjn1P`）：PASS — 所有 BLOCKER/MAJOR RESOLVED，无新增。
  - Round 3 共识轮（`ses_0f777b1feffeq3bLr24ZVpp5FA`）：PASS — 第 2 个连续 clean round，共识达成。
- Evidence: 实读 live repo 核验（MemberLevel/vipPrice 确实不存在、userLevel @ nop-auth-delta.orm.xml:47、submit:204 用 product.getPrice() 无 vipPrice 分支、降级为 roadmap:316 必交付项）。

## Closure Gates

- [ ] in-scope behavior is complete（等级模型 + 评估 + vipPrice + 前端 + 后台）
- [ ] relevant docs are aligned（`user-and-address.md` / `order-and-cart.md` / ORM）
- [ ] verification has run（`./mvnw test -pl app-mall-service -am` + app-mall-web 编译）
- [ ] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`
- [ ] no in-scope item downgraded to deferred/follow-up
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill` listed（Phase 1 `none` 含 justify）
- [ ] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [ ] text consistency verified: status / phases / gates / log 一致
- [ ] closure audit was performed by a different agent/session than implementation
- [ ] closure evidence exists in files

## Deferred But Adjudicated

> 起草时无可延期项；权益实现（专属券/礼包）已在 Non-Goals 显式移出范围（依赖 P8/P32），非遗留模糊项。

## Closure

<!-- Closure audit MUST be performed by an independent subagent. 留给闭合审计代理。 -->

Status Note: <待闭合>

Closure Audit Evidence:

- Reviewer / Agent: <independent reviewer — MUST NOT be implementing agent>
- Evidence: <task id / 验证记录>

Follow-up:

- 权益发放（专属券/生日礼包/专享客服）触发条件：P8+P32 专属券能力就绪后，开 successor 计划实现权益发放逻辑。
