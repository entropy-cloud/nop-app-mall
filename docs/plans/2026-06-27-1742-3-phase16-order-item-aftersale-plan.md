# P16 订单项级售后增强（Order-Item-Level Aftersale）

> Plan Status: active
> Last Reviewed: 2026-06-27
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 16；`docs/design/order-and-cart.md`（退款与售后章节）
> Related: `docs/plans/2026-06-27-1742-1-phase15-full-discount-promotion-plan.md`、`docs/plans/2026-06-27-1742-2-phase26-member-level-system-plan.md`（同批次）
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 16（订单项级售后增强）

> **执行顺序：** 本计划为 2026-06-27-1742 批次第 3 顺位（N=3）。P15（N=1）、P26（N=2）优先（解除更多下游阻塞）。本 phase 不解除任何下游 roadmap 阻塞，但属 P0 主链路增强且模型部分就绪，作为本批次收尾的干净 P0 交付。

## Current Baseline

> 实读 live repo（working tree，HEAD `35d90f2`）所得。

**模型部分就绪：**

- `LitemallAftersale` 已有 `orderItemId`（订单商品项ID，propId=15，`app-mall.orm.xml:266`，stdDataType=string/stdSqlType=INTEGER）—— roadmap「售后表新增 order_item_id」**已落地**。
- 售后状态机（REQUEST/APPROVED/REJECT）与 aftersale-type 字典（未收货退款/已收货无需退货退款/退货退款）已存在（`order-and-cart.md:207-208`）。
- `LitemallAftersaleBizModel` 存在，实现**订单整体级**售后（申请/审核/退款/撤回），退款金额以订单 `actualPrice` 为上限（`LitemallAftersaleBizModel:114,124,213,219,223`）。

**缺口（本计划交付对象）：**

1. **业务设计为订单整体级（关键）：** `order-and-cart.md`（196-242）售后设计以**订单**为粒度（「售后覆盖两类：未发货已支付退款 + 已收货退款」，line 207），line 234「售后申请应明确关联原订单、订单商品和售后原因」概念上关联订单商品但**实际粒度为订单整体**（一订单一售后）。需升级为 OrderGoods 项级（一订单多项售后，每项独立状态机）。
2. **orderItemId 未被业务逻辑使用：** 列已存在但 BizModel 仍按订单整体处理；历史记录 `orderItemId=null` 需按订单整体兼容（roadmap line 155）。
3. **售后原因未字典化：** 当前 reason 为文本；roadmap 要求「售后原因字典化（后台维护）」（line 157）。需新增 aftersale-reason 字典（或评估现有 reason 字段改造）。
4. **无售后进度时间线：** 无按状态变更的时间线展示。
5. **退款金额按 item 计算：** item 级售后退款额应受对应 OrderGoods 金额约束，非整单 actualPrice。
6. **无测试覆盖 item 级路径。**

**前置条件已满足：** Phase 5c（退款与售后）`done`。

## Goals

- 将售后从订单整体级升级为 OrderGoods 商品项粒度（一订单可多项独立售后）。
- 历史记录兼容（`orderItemId=null` 按订单整体处理）。
- 售后类型按订单状态自动可选（未发货→仅退款；已收货→仅退款/退货退款）。
- 售后原因字典化（后台维护原因选项）。
- 售后进度时间线展示。
- 前台售后入口 + 后台审核页面适配 item 级；核心路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- 退货入库/物流回寄实现（`order-and-cart.md:230` 明确「退货入库等实现动作属于技术和履约实现，不在本 owner doc 展开」）。
- 售后营销/通知扩展（通知系统 P12 已交付，本计划仅复用既有通知触发点）。
- 移动端前端。

## Task Route

- Type: `app-layer design change` + `implementation-only change`
- Owner Docs: `docs/design/order-and-cart.md`（售后章节 item 级升级）、`docs/design/system-configuration.md`（后台审核/原因字典）
- Skill Selection Basis: BizModel 改造/ErrorCode → `nop-backend-dev`；`@BizMutation`/`@BizQuery` 测试 → `nop-testing`；AMIS 页面 → `nop-frontend-dev`；Phase 2 补 aftersale-reason 字典（+ 可能字段/关系）→ `nop-orm-modeler` + `nop-database-design`

## Infrastructure And Config Prereqs

- **基础基线锁定（前置门）：** 本批次计划建立在当前 working tree 基线（HEAD `35d90f2` + uncommitted 缺陷修复/价格字段初始化）之上。执行前须先将基础性变更提交锁定为稳定 HEAD（见 P15 同名前置门）。
- **跨计划依赖：** Phase 1 退款额/分摊 Decision 依赖 P15（N=1）确定的价格公式（promotionPrice 是否分摊到行）。本计划执行顺序为 N=3，须在 P15（及 P26）价格公式落地后定 item 退款额计算；Prereqs 显式声明 P15。
- 售后原因字典选项后台维护 → `nop-sys` 字典管理（已引入）。
- 无外部服务依赖。无破坏性数据迁移（orderItemId 列已存在；存量 reason 文本保留兼容）。

## Protected Area

> 见 `docs/context/ai-autonomy-policy.md`。本计划触及受保护区域，**执行前须取得人工确认**：
>
> - **`model/app-mall.orm.xml`（ask-first）：** Phase 2 新增 `mall/aftersale-reason` 字典 +（按 Phase 1 Decision）可能新增 OrderGoods aftersaleStatus 字段 / Aftersale→OrderGoods 关系。XML 模型改动须经人工批准后改并重生成。
>
> 证据要求：ask-first → 人工确认 → 改模型 → 重生成 → 编译/测试。

## Execution Plan

### Phase 1 — 业务设计合成：售后 item 级升级（Decision-heavy）

Status: planned
Targets: `docs/design/order-and-cart.md`（售后章节）、`docs/design/system-configuration.md`
Required Skill: `none`（docs-only 业务语义合成；模型改动在 Phase 2。无 skill 匹配「写设计文档」）

- Item Types: `Decision | Add`
- Prereqs: P15（N=1）价格公式落地——item 退款额分摊依赖 promotionPrice 是否分摊到行的约定

- [ ] **Skill loading gate:** 扫描 available skills；docs-only，无匹配。已读：`order-and-cart.md`（退款与售后 196-242 + 状态机）、`enhanced-features-roadmap.md` Phase 16、`domain-design-guidelines.md`。
- [ ] **Decision: item 级落地方式。** 备选 A：单表 `LitemallAftersale` + 既有 `orderItemId`（orderItemId 已存在，推荐，改动小）。备选 B：新建 `aftersale_item` 表（roadmap line 154 备选）。记录抉择 + 理由（倾向 A，列已就绪）。
- [ ] **Decision: 多项售后并发与互斥（枚举备选）。** 备选 A：同一 OrderGoods 同一时刻只允许一个进行中售后，终态后可再次申请（部分退款后二次）；备选 B：同一 OrderGoods 终身只允许一次售后；备选 C：允许 N 个并发。抉择 + 理由 + 残留风险，并明确同一订单多项并行的状态机独立性。
- [ ] **Decision: item 级退款额上限（枚举备选）。** 备选 A：用既有 `LitemallOrderGoods.actualPayAmount`（实付金额列，`app-mall.orm.xml:1183`，最简）；备选 B：`number × price` 减按比例分摊的 coupon/promotion；备选 C：下单时冻结每行可退金额。抉择 + 理由。分摊规则依赖 P15 promotionPrice 分摊约定。
- [ ] **Decision: item 级 aftersaleStatus 存放位置。** 备选 A：派生计算（按 `LitemallAftersale.status` 过滤 `orderItemId` 聚合，不加字段）；备选 B：存于 `LitemallOrderGoods.aftersaleStatus`——**该字段已存在**（`app-mall.orm.xml:1059-1060`，dict `mall/aftersale-status`，当前未用），选 B 无需 Phase 2 加字段，仅需启用/写入语义。抉择 + 理由。**并明确** 多项并行时既有 `order.aftersaleStatus`（订单级互斥锁，`LitemallAftersaleBizModel:201,233`）的语义：3 项中 1 项 REQUEST + 2 项 INIT 时订单级字段如何表达（建议改为聚合视图或弃用为互斥锁）。
- [ ] **Decision: 部分退款对订单级的副作用（枚举）。** item 退款时三个订单级副作用须定策略：(a) 订单状态迁移——未发货单仅退 1 项时不应整单进 REFUND 终态（`LitemallAftersaleBizModel:143`）；(b) 还库——仅对退款的 OrderGoods 行还库（当前 `:154-159` 遍历全部 orderGoods）；(c) 券恢复——item 退款需按比例/部分恢复券（当前 `:162-169` 整单 returnCoupon）。每个副作用给备选 + 抉择 + 理由。
- [ ] **Decision: 售后类型按状态自动可选。** 未发货(201)→仅退款；已收货(401/402)→仅退款/退货退款。固化映射规则。
- [ ] **Decision: 历史兼容。** `orderItemId=null` 视为整单售后，沿用既有逻辑。
- [ ] **Decision: item 级通知语义。** 当前 `:174` 按 orderSn 触发退款通知；item 级多次退款会重复通知。抉择：按 item 维度通知（含 item 信息）或保持订单级去重。记录抉择（影响前端 UX）。
- [ ] **Add:** 将 item 级售后设计写入 `order-and-cart.md`（升级售后章节），后台审核/原因字典写入 `system-configuration.md`。

Exit Criteria:

- [ ] `order-and-cart.md` 售后章节升级为 item 级（含多项并发/退款额上限/aftersaleStatus 位置/部分退款副作用/类型映射/历史兼容/通知）
- [ ] 八个 Decision 抉择/备选/理由/残留风险已记录
- [ ] Phase 2 模型清单由本阶段 Decision 确定（aftersale-reason 字典 / 是否 OrderGoods aftersaleStatus / 是否 Aftersale→OrderGoods 关系）

### Phase 2 — 模型准备（Add-heavy，rule #11 模型须先于业务编码）

Status: planned
Targets: `model/app-mall.orm.xml`、codegen 重生成、`deploy/sql/*`
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Add`
- Prereqs: Phase 1（模型字段集 Decision 已决）

- [ ] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读 routing 必读文档（ORM 建模、命名、域、字典、关系）。列已读路径。
  - Docs read: <执行时填入>
- [ ] **Add（Protected Area — ask-first `model/app-mall.orm.xml`）：** 新增 `mall/aftersale-reason` 字典（后台维护售后原因选项；已确认现状无此字典，仅有 aftersale-type/aftersale-status）。**须先取得人工确认**再改 XML。
- [ ] **Add:** 按 Phase 1 aftersaleStatus Decision——`LitemallOrderGoods.aftersaleStatus` 字段已存在（`app-mall.orm.xml:1059`），选「存于 OrderGoods」时无需新增、仅启用写入语义；按 item 落地方式 Decision 补 Aftersale→OrderGoods 关系（若需要）。
- [ ] **Add:** 运行 codegen 重生成受影响代码 + `deploy/sql/*` 三方言。验证 `./mvnw install -pl app-mall-dao -am` BUILD SUCCESS。

Exit Criteria:

- [ ] aftersale-reason 字典 +（按 Decision）OrderGoods aftersaleStatus 字段/关系落地，codegen 重生成成功，编译通过
- [ ] 不在模型准备阶段写业务逻辑（rule #11）

### Phase 3 — 后端：item 级售后逻辑 + ErrorCode（Add-heavy / Fix）

Status: planned
Targets: `app-mall-service/.../LitemallAftersaleBizModel.java`、`app-mall-dao/.../AftersaleApplyRequest.java`、`app-mall-api/...`、`AppMallErrors.java`
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Fix`
- Prereqs: Phase 2（模型就绪）

- [ ] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读 routing 必读文档。每方法 selfcheck。
  - Docs read: <执行时填入>
- [ ] **Fix:** 改造 `LitemallAftersaleBizModel` 申请路径：按 OrderGoods 粒度（校验 orderItemId 归属订单、状态资格、类型映射、多项并发 Decision），退款额按 item 上限 Decision，aftersaleStatus 按 Phase 1 存放 Decision；`orderItemId=null` 走整单兼容路径。
- [ ] **Fix: 部分退款订单级副作用（按 Phase 1 Decision 落地，三个子项）。** (a) 订单状态迁移策略——未发货单部分 item 退款不整单进 REFUND；(b) item 级还库——仅对退款 OrderGoods 行调用还库；(c) 券恢复——按比例/部分恢复券。
- [ ] **Fix/Add:** `AftersaleApplyRequest` DTO 加 `orderItemId` 字段（**公开 API 契约变更**；`orderItemId` 可选，缺省=整单兼容）。
- [ ] **Add:** 售后原因字典校验（申请时 reason 须在 aftersale-reason 字典内）。
- [ ] **Add:** 售后进度时间线数据（按状态变更记录时间点）。
- [ ] **Add:** `AppMallErrors` 新增/补充 aftersale 域 ErrorCode（item 不属于订单、item 已售后、类型与状态不匹配、退款额超 item 上限、reason 非字典项等）。
- [ ] **Proof:** item 级申请/审核/退款/撤回通过 `IGraphQLEngine`（`JunitAutoTestCase`）测试：多项并行、item 退款额上限、类型-状态映射、部分退款副作用（状态/还库/券）、历史 `orderItemId=null` 兼容、原因字典校验。
- [ ] **测试基线迁移：** 既有 `TestLitemallAftersaleBizModel`（`:154,188,201,219,257` 调 `LitemallAftersale__apply`，`:286,312` 调 `LitemallAftersale__refund`，均用旧 payload）须迁移到 item 级 payload（或靠 `orderItemId` 可选 + 整单回退保持兼容）。测试基类选 `JunitAutoTestCase`（规则 #15）；与模块现有 `JunitBaseTestCase`+手构 ApiRequest 模式的一致性/迁移在本 phase 说明。

Exit Criteria:

- [ ] item 级售后全流程（申请/审核/退款/撤回）按 OrderGoods 粒度工作
- [ ] 退款额受 item 上限约束；多项独立状态机；部分退款三个副作用按 Decision 正确
- [ ] `orderItemId=null` 整单兼容
- [ ] 售后原因字典化生效；DTO 契约变更落地
- [ ] **API 测试：** 新增/改动 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine`

### Phase 4 — 前端：item 级售后入口 + 后台审核 + 时间线（Add-heavy）

Status: planned
Targets: 前台订单详情售后入口、后台 `pages/LitemallAftersale/LitemallAftersale.view.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 3

- [ ] **Skill loading gate:** 加载 `nop-frontend-dev`，读 routing 必读文档。文件完成后 selfcheck。
  - Docs read: <执行时填入>
- [ ] **Add:** 前台订单详情：按 OrderGoods 行展示售后入口（依据行状态资格与类型映射），售后申请按 item 提交；原因从字典选项选择。
- [ ] **Add:** 后台售后审核页适配 item 级（展示 orderItemId/商品快照、item 退款额、独立审核动作）。
- [ ] **Add:** 售后进度时间线展示（状态变更时间点）。

Exit Criteria:

- [ ] 前台可按商品项发起售后；原因字典选择
- [ ] 后台按 item 审核；展示 item 退款额
- [ ] 售后进度时间线可见

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
  - Round 1（`ses_0f7851d5dffePxzEAkKxACbYww`）：REVISE — 2 BLOCKER（P15 跨计划依赖未声明致 D3 不可解、Phase 2 遗漏 3 个订单级副作用）+ 4 MAJOR（aftersaleStatus 存放未决、rule #11 不合规 + 若措辞、Decision 2 未枚举、AftersaleApplyRequest DTO 缺失）+ 3 MINOR。全部修订：声明 P15 依赖；后端 phase 拆出 3 副作用子项；补 aftersaleStatus 存放 Decision；新增独立模型准备 Phase 2；枚举 Decision 备选；DTO 入 Targets + 测试迁移项。
  - Round 2（`ses_0f77b0479ffe5G0IF6S12BcZLo`）：PASS — 9 项全部 RESOLVED，无新增。
  - Round 3 共识轮（`ses_0f7779df8ffeQqg9vQsh6AWIZ8`）：PASS — 第 2 个连续 clean round，共识达成。附 useful finding：`LitemallOrderGoods.aftersaleStatus` 字段已存在（orm.xml:1059），option B 无模型成本，已据此修正 Phase 1 Decision 4 + Phase 2。
- Evidence: 实读 live repo 核验（orderItemId @ orm.xml:266、order-and-cart.md:207 售后为订单级、AftersaleBizModel 订单级 actualPrice 上限、无 aftersale-reason 字典、actualPayAmount @ orm.xml:1183、AftersaleApplyRequest 无 orderItemId）。

## Closure Gates

- [ ] in-scope behavior is complete（item 级售后 + 原因字典 + 时间线 + 前后台）
- [ ] relevant docs are aligned（`order-and-cart.md` 售后章节 / `system-configuration.md`）
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

> 起草时无可延期项。退货入库/物流回寄在 Non-Goals 显式移出（owner doc 已界定）。

## Closure

<!-- Closure audit MUST be performed by an independent subagent. 留给闭合审计代理。 -->

Status Note: <待闭合>

Closure Audit Evidence:

- Reviewer / Agent: <independent reviewer — MUST NOT be implementing agent>
- Evidence: <task id / 验证记录>

Follow-up:

- 退货入库/物流回寄实现触发条件：业务需要实物退货履约时，开 successor 计划。
