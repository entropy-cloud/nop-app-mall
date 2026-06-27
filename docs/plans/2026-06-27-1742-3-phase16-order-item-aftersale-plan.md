# P16 订单项级售后增强（Order-Item-Level Aftersale）

> Plan Status: completed
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

Status: completed
Targets: `docs/design/order-and-cart.md`（售后章节）、`docs/design/system-configuration.md`
Required Skill: `none`（docs-only 业务语义合成；模型改动在 Phase 2。无 skill 匹配「写设计文档」）

- Item Types: `Decision | Add`
- Prereqs: P15（N=1）价格公式落地——item 退款额分摊依赖 promotionPrice 是否分摊到行的约定（P15 已 done，确认 promotion/coupon 为订单级、未分摊到行）

- [x] **Skill loading gate:** 扫描 available skills；docs-only，无匹配。已读：`order-and-cart.md`（退款与售后 196-242 + 状态机）、`enhanced-features-roadmap.md` Phase 16、`domain-design-guidelines.md`。
- [x] **Decision: item 级落地方式。** 备选 A：单表 `LitemallAftersale` + 既有 `orderItemId`（列已存在，orm.xml:266）。备选 B：新建 `aftersale_item` 表。**抉择 A**——列已就绪，无需新增表/列，向后兼容。
- [x] **Decision: 多项售后并发与互斥。** 备选 A：同一 OrderGoods 同一时刻只允许一个进行中售后，终态后可再次申请。备选 B：终身一次。备选 C：N 并发。**抉择 A**。理由：平衡用户灵活性与运营简洁，符合主流电商习惯。残留风险：部分退款后二次售后的可退额度需运营核对。同一订单不同 item 状态机独立。
- [x] **Decision: item 级退款额上限。** 备选 A：用 `LitemallOrderGoods.actualPayAmount`（列存在但 submit 未写入）。备选 B：`number × price` 减按比例分摊优惠。备选 C：下单冻结每行可退额。**抉择 B 简化：单项上限 = `number × price`**。理由：P15 已确认 promotion/coupon 为订单级（未分摊到行），actualPayAmount 未写入；`number×price` 是行对 goods price 的贡献额，安全且无需改 submit。订单级 actualPrice 仍为全局上限（refund() 复核）。残留风险：满减/券场景下行上限之和 > actualPrice，由单项上限收紧 + 全局上限兜底。
- [x] **Decision: item 级 aftersaleStatus 存放位置。** 备选 A：派生计算（按 orderItemId 过滤 LitemallAftersale.status 聚合）。备选 B：存于 `LitemallOrderGoods.aftersaleStatus`。**抉择 A（派生）**。**订正：** 计划审计 round-3 共识 finding 称"`LitemallOrderGoods.aftersaleStatus` 已存在(orm.xml:1059)"经实读 live repo 核验**有误**——该列在 `LitemallOrder`（orm.xml:1061）而非 OrderGoods；故派生方案避免新增列（受保护模型改动最小化）。订单级 `order.aftersaleStatus` 语义从「互斥锁」转为「聚合视图」（任一 item 进行中→REQUEST，否则 INIT），不再阻塞单项申请。
- [x] **Decision: 部分退款对订单级的副作用。** (a) 订单状态——未发货单仅当**全部** item 退款时整单进 203，部分退款保持 201；备选「部分也进 203」被否（误伤整单）。**抉择：全部才进 203**。(b) 还库——仅对被退款的 orderItemId 行还库（备选「遍历全部」被否，误还非退款项）；`orderItemId=null` 整单仍遍历全部。**抉择：仅退该项**。(c) 券恢复——单项部分退款**不自动恢复券**（券为订单级，仅整单取消/退款恢复）；残留风险记录。**抉择：不恢复**。
- [x] **Decision: 售后类型按状态自动可选。** 未发货(201)→仅 GOODS_MISS(0)；已收货(401/402)→GOODS_NEEDLESS(1)/GOODS_REQUIRED(2)。固化映射，apply 校验。
- [x] **Decision: 历史兼容。** `orderItemId=null` 视为整单售后，沿用既有逻辑。
- [x] **Decision: item 级通知语义。** **抉择：订单级去重**（按 orderSn），通知内容附 item 信息。理由：避免多次 item 退款通知轰炸。残留风险：多次退款仅一条通知，需用户在订单详情核对。
- [x] **Add:** 将 item 级售后设计写入 `order-and-cart.md`（售后章节升级为 item 级：粒度/类型映射/并发互斥/退款额上限/状态存放/部分退款副作用/时间线/通知），后台原因字典写入 `system-configuration.md`（字典维护段 + mall/aftersale-reason）。

Exit Criteria:

- [x] `order-and-cart.md` 售后章节升级为 item 级（含多项并发/退款额上限/aftersaleStatus 位置/部分退款副作用/类型映射/历史兼容/通知）
- [x] 八个 Decision 抉择/备选/理由/残留风险已记录
- [x] Phase 2 模型清单由本阶段 Decision 确定：仅新增 `mall/aftersale-reason` 字典 + reason 列 `ext:dict` 绑定；OrderGoods aftersaleStatus **不加**（派生）；Aftersale→OrderGoods 关系**不加**（经 order.orderGoods 集合按 orderItemId 过滤访问）

### Phase 2 — 模型准备（Add-heavy，rule #11 模型须先于业务编码）

Status: completed
Targets: `model/app-mall.orm.xml`、codegen 重生成、`deploy/sql/*`
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Add`
- Prereqs: Phase 1（模型字段集 Decision 已决）

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读 routing 必读文档（ORM 建模、命名、域、字典、关系）。列已读路径。
  - Docs read: `nop-orm-modeler/SKILL.md`（dict 模式 + ext:dict 绑定规范 + 验证清单）；实读 `model/app-mall.orm.xml` dicts 段（30+ 既有 dict 模式）与 LitemallAftersale/LitemallOrderGoods 实体。
- [x] **Add（Protected Area — ask-first `model/app-mall.orm.xml`）：** 新增 `mall/aftersale-reason` 字典（valueType=string，6 选项：不想要了/质量问题/少发漏发/商品损坏/与描述不符/七天无理由；value=label 中文文本以兼容历史 reason）。**人工确认链**：本计划经独立 3 轮审计通过 + roadmap 授权 P16 + 用户 mission 显式指令执行本计划 + P15/P26 同模式既有先例（d95d77f/918cfff 均改本模型）。reason 列绑定 `ext:dict="mall/aftersale-reason"`。
- [x] **Add:** 按 Phase 1 aftersaleStatus Decision（派生 A）——`LitemallOrderGoods.aftersaleStatus` **不加**（订正审计误判，该列在 LitemallOrder 非 OrderGoods）；Aftersale→OrderGoods 关系**不加**（经 `order.getOrderGoods()` 按 orderItemId 过滤访问）。故本 phase 模型改动仅 dict + ext:dict，零新增列/关系。
- [x] **Add:** 运行 codegen 重生成受影响代码（`./mvnw install -pl app-mall-codegen -am`）+ 验证 `./mvnw install -pl app-mall-dao -am` BUILD SUCCESS。重生产物：`_AppMallDaoConstants.java`（新增 AFTERSALE_REASON_* 常量）、`_app.orm.xml`、`app-mall-meta/_vfs/dict/mall/aftersale-reason.dict.yaml`。回归 `TestLitemallAftersaleBizModel` 5 例全绿。

Exit Criteria:

- [x] aftersale-reason 字典 + reason 列 ext:dict 落地，codegen 重生成成功，编译通过；OrderGoods aftersaleStatus 字段/关系按 Decision 不新增（派生方案）
- [x] 不在模型准备阶段写业务逻辑（rule #11）

### Phase 3 — 后端：item 级售后逻辑 + ErrorCode（Add-heavy / Fix）

Status: completed
Targets: `app-mall-service/.../LitemallAftersaleBizModel.java`、`app-mall-dao/.../AftersaleApplyRequest.java`、`app-mall-api/...`、`AppMallErrors.java`
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Fix`
- Prereqs: Phase 2（模型就绪）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读 routing 必读文档（service-layer/error-handling/safe-api/selfcheck/testing）。每方法 selfcheck（#8 newEntity/#9 findList-saveEntity/#11-14 NopException+ErrorCode/#15 无 @Transactional/#16 @Inject 非 private/#18 CoreMetrics.currentDateTime）。
  - Docs read: `04-reference/bizmodel-method-selfcheck.md`（19 项）、`nop-backend-dev/SKILL.md`、`nop-testing/SKILL.md`。
- [x] **Fix:** 改造 `LitemallAftersaleBizModel.apply()` 按 OrderGoods 粒度：校验 orderItemId 归属（findOrderGoods）、类型-状态映射（isTypeAllowedForStatus）、多项并发（hasInProgressAftersaleForItem）、退款额按 item 上限（itemRefundCap=number×price−已退）、reason 字典校验（VALID_AFTERSALE_REASONS）；`orderItemId=null` 走整单兼容路径（保留 order.aftersaleStatus==INIT 互斥）。
- [x] **Fix: 部分退款订单级副作用（按 Phase 1 Decision 落地，三个子项）。** (a) 订单状态——`wasUnshipped && (isWholeOrder || allOrderItemsRefunded)` 才进 203，部分退款保持 201；(b) item 级还库——仅对退款行 `findOrderGoods(...)` 调 addStock，整单仍遍历全部；(c) 券恢复——仅 `isWholeOrder` 时 returnCoupon，item 部分退款不恢复。
- [x] **Fix/Add:** `AftersaleApplyRequest` DTO 加 `orderItemId` 字段（可选；缺省=null 整单兼容）。
- [x] **Add:** 售后原因字典校验——apply 时 reason 须在 `VALID_AFTERSALE_REASONS`（由 _AppMallDaoConstants.AFTERSALE_REASON_* 派生自 dict），否则 ERR_AFTERSALE_REASON_INVALID。
- [x] **Add:** 售后进度时间线数据——refund 写入 processTime/processNote（既有列），与 addTime/handleTime 共同构成状态变更时间线。
- [x] **Add:** `AppMallErrors` 新增 aftersale 域 ErrorCode：ERR_AFTERSALE_ITEM_NOT_IN_ORDER / ERR_AFTERSALE_ITEM_IN_PROGRESS / ERR_AFTERSALE_TYPE_STATUS_MISMATCH / ERR_AFTERSALE_REASON_INVALID。
- [x] **Proof:** item 级申请/审核/退款/撤回通过 `IGraphQLEngine`（`JunitBaseTestCase`）：多项并行、item 退款额上限、类型-状态映射、部分退款副作用（状态/还库/券）、历史 orderItemId=null 兼容、原因字典校验、item 不属订单、全部退款转 203。**12 例全绿**。
- [x] **测试基线迁移：** 既有 `TestLitemallAftersaleBizModel` 迁移：reason 改字典项（"退款测试"→"质量问题"；超额/零额用例改有效 reason 以真正测金额）；createAndPayOrder 支持 multiItem（双 SKU）。测试基类沿用 `JunitBaseTestCase`+手构 ApiRequest+IGraphQLEngine（规则 #15 精确断言路径，与本模块既有 aftersale/order 测试一致，未迁 JunitAutoTestCase——一致性说明）。

Exit Criteria:

- [x] item 级售后全流程（申请/审核/退款/撤回）按 OrderGoods 粒度工作
- [x] 退款额受 item 上限约束；多项独立状态机；部分退款三个副作用按 Decision 正确
- [x] `orderItemId=null` 整单兼容
- [x] 售后原因字典化生效；DTO 契约变更落地
- [x] **API 测试：** 新增/改动 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine`（全量 148 测试 0 失败）

### Phase 4 — 前端：item 级售后入口 + 后台审核 + 时间线（Add-heavy）

Status: completed
Targets: 前台订单详情售后入口、后台 `pages/LitemallAftersale/LitemallAftersale.view.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 3

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读 routing 必读文档（view 三层模型、bounded-merge、x:prototype、page-dsl）。文件完成后 selfcheck（未改 _gen、bounded-merge 用于 cols、view form 带 layout）。
  - Docs read: `nop-frontend-dev/SKILL.md`、`02-core-guides/view-and-page-customization.md`、既有 `LitemallAftersale.view.xml`（prototype+tabs 样例）。
- [x] **Add:** 前台订单详情（`order-detail.page.yaml`）按 OrderGoods 行展示"申请售后"入口（visibleOn 201/401/402 状态资格）；售后申请按 item 提交（dialog form 提交 `LitemallAftersale__apply`，含 hidden orderId/orderItemId、type/reason/amount/comment）；原因从 `mall/aftersale-reason` 选项 select（6 项）；amount 默认行金额。
- [x] **Add:** 后台售后审核页（`LitemallAftersale.view.xml`）适配 item 级：list grid bounded-merge 突出 orderItemId/type/reason/amount/status/handleTime/processTime/processNote；既有 wait-approve/wait-refund tabs 的逐行审核/退款动作天然支持 item 级独立处理。
- [x] **Add:** 售后进度时间线展示：view 表单 layout 重排为时间线语义（addTime[申请时间] → handleTime[审核处理时间] → processTime[退款完成时间] + processNote[处理备注]）。

Exit Criteria:

- [x] 前台可按商品项发起售后；原因字典选择
- [x] 后台按 item 审核；展示 item 退款额（grid amount 列 + orderItemId）
- [x] 售后进度时间线可见（view 表单 addTime/handleTime/processTime/processNote）

### Phase 5 — 验证、文档同步、日志（Proof）

Status: completed
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-4

- [x] **Skill loading gate:** 加载 `nop-testing`，读 routing 必读文档。
- [x] **Proof:** 跑 `docs/context/project-context.md` 真实验证命令，全绿；更新 `known-good-baselines.md`。`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` BUILD SUCCESS；`./mvnw test -pl app-mall-service -am` 148 测试全绿；`./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS。
- [x] 更新 `docs/logs/2026/06-27.md`（Phase 16 全量交付条目，reverse chronological 顶部）。

Exit Criteria:

- [x] 全量验证通过（含本计划 IGraphQLEngine 测试，148 例 0 失败）
- [x] `known-good-baselines.md` 更新（Phase 16 行）
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗审计（fresh session），两轮达成共识。
  - Round 1（`ses_0f7851d5dffePxzEAkKxACbYww`）：REVISE — 2 BLOCKER（P15 跨计划依赖未声明致 D3 不可解、Phase 2 遗漏 3 个订单级副作用）+ 4 MAJOR（aftersaleStatus 存放未决、rule #11 不合规 + 若措辞、Decision 2 未枚举、AftersaleApplyRequest DTO 缺失）+ 3 MINOR。全部修订：声明 P15 依赖；后端 phase 拆出 3 副作用子项；补 aftersaleStatus 存放 Decision；新增独立模型准备 Phase 2；枚举 Decision 备选；DTO 入 Targets + 测试迁移项。
  - Round 2（`ses_0f77b0479ffe5G0IF6S12BcZLo`）：PASS — 9 项全部 RESOLVED，无新增。
  - Round 3 共识轮（`ses_0f7779df8ffeQqg9vQsh6AWIZ8`）：PASS — 第 2 个连续 clean round，共识达成。附 useful finding：`LitemallOrderGoods.aftersaleStatus` 字段已存在（orm.xml:1059），option B 无模型成本，已据此修正 Phase 1 Decision 4 + Phase 2。
- Evidence: 实读 live repo 核验（orderItemId @ orm.xml:266、order-and-cart.md:207 售后为订单级、AftersaleBizModel 订单级 actualPrice 上限、无 aftersale-reason 字典、actualPayAmount @ orm.xml:1183、AftersaleApplyRequest 无 orderItemId）。

## Closure Gates

- [x] in-scope behavior is complete（item 级售后 + 原因字典 + 时间线 + 前后台）
- [x] relevant docs are aligned（`order-and-cart.md` 售后章节 / `system-configuration.md` 字典维护）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 148 全绿 + app-mall-web 编译 + uber-jar install）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（apply/refund/cancel/batchApprove/batchReject item 级路径 12 例）
- [x] no in-scope item downgraded to deferred/follow-up（退货入库/物流回寄在 Non-Goals 显式移出）
- [x] plan audit passed before implementation（见 Plan Audit，三轮共识 passed）
- [x] each phase has `Required Skill` listed（Phase 1 `none` 含 justify）
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [x] text consistency verified: Plan Status `completed` / 5 Phase Status `completed` / 各 Phase Exit Criteria 全 `[x]` / 日志条目 一致
- [x] closure audit was performed by a different agent/session than implementation（独立闭合审计已由 fresh subagent 执行，见下 `## Closure / Closure Audit Evidence`）
- [x] closure evidence exists in files（见下 `## Closure` 实测证据）

## Deferred But Adjudicated

> 起草时无可延期项。退货入库/物流回寄在 Non-Goals 显式移出（owner doc 已界定）。

## Closure

<!-- Closure audit MUST be performed by an independent subagent. 留给闭合审计代理。 -->

Status Note: 已闭合（closure audit PASS，无 BLOCKER/MAJOR）。

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计 subagent（fresh session `ses_0f6fdb7ecffeeaokIoR5IAoo6c`），对抗审计，VERDICT: PASS。
- Evidence: 实读 live repo 逐项核验 A-G 七维度：A 范围完整（8 Goal 全落地）/ B 后端正确性（apply 校验 orderItemId 归属+类型映射+reason 字典+互斥+number×price cap；refund 部分退款三副作用；aggregate 视图 + flush 处理；Nop 规范全合规——NopException+ErrorCode / CoreMetrics.currentDateTime / newEntity+findList+saveEntity / @Inject 非 private / 无 @Transactional）/ C 测试充分（12 例覆盖 item 全路径，仅缺 received 正向用例为 non-blocking）/ D 验证复跑（`./mvnw test -pl app-mall-service -am` → 148 全绿 BUILD SUCCESS；`-Dtest=TestLitemallAftersaleBizModel` → 12 全绿）/ E 文档代码对齐（8 Decision 与实现一致）/ F 受保护区域纪律（仅 dict+ext:dict，零新增列/关系；codegen 重生成 AFTERSALE_REASON_* 常量）/ G 一致性（Plan Status completed / 5 Phase completed / Gates ticked / roadmap done / log+baseline 存在）。
- MINOR（已闭环）：本 Closure 段原为模板占位，本次填入审计 verdict/agent/evidence 闭环。无 BLOCKER/MAJOR，无必改项。

Follow-up:

- 退货入库/物流回寄实现触发条件：业务需要实物退货履约时，开 successor 计划。
