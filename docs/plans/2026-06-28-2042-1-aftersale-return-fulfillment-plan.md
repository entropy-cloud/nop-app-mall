# phase16b 售后退货履约（退货物流回寄 + 收货确认子状态机 + 售后接口债）

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Mission: mall
> Work Item: P16/P21 Deferred successor — 售后退货履约
> Source: `docs/backlog/enhanced-features-roadmap.md` §16/§21；`2026-06-28-0340-3-phase21-order-operations-workbench-plan.md` → `Deferred But Adjudicated → 退货入库/物流回寄 + 售后接口债（P16 successor）`
> Related: `2026-06-27-1742-3-phase16-order-item-aftersale-plan.md`（P16 item 级售后 origin，退货入库在 Non-Goals）、`docs/design/order-and-cart.md` 退款与售后章节
> Audit: required

## Current Baseline

> 来源：实读 live repo（HEAD 经 grep/read + 子代理基线扫描核验），非旧计划记忆。

**退货退款（GOODS_REQUIRED, type=2）无物理退货阶段：**
- `LitemallAftersale` 实体（`model/app-mall.orm.xml:244-294`）仅 17 列（id…processTime），**无任何退货运单字段**（无 returnShipChannel/returnShipSn/returnTime/receiveConfirmTime）。最后 propId=17（processTime）。
- 售后状态字典 `mall/aftersale-status`（`app-mall.orm.xml:8-15`）6 值：INIT(0)/REQUEST(1)/APPROVED(2)/REFUND(3)/REJECT(4)/CANCELLED(5)。**无"已退货/待收货"中间态**。
- 售后类型字典 `mall/aftersale-type`：GOODS_MISS(0 未收货退款)/GOODS_NEEDLESS(1 已收货无需退货退款)/GOODS_REQUIRED(2 用户退货退款)。GOODS_REQUIRED 仅适用于已收货订单（401/402，见 `order-and-cart.md:353`）。
- `LitemallAftersaleBizModel.refund`（`LitemallAftersaleBizModel.java:123-226`）是唯一退款路径：守卫 `APPROVED`，调用 `payService.refund`，置 `APPROVED→REFUND`，还库/还券/还积分/通知。**GOODS_REQUIRED 的还库在 `refund()` 内 `:181-194` 无条件执行**——管理员点退款即还库，**无"用户回寄→管理员确认收货"门控**。引用：`boolean shouldRestock = wasUnshipped || entity.getType() == AFTERSALE_TYPE_GOODS_REQUIRED;`
- 即 GOODS_REQUIRED 当前与 GOODS_NEEDLESS 流转完全相同（REQUEST→APPROVED→REFUND），仅在 `refund()` 内多还一次库。物理退货阶段是真实缺口。

**售后运营方法未上接口 + 无批量退款：**
- `batchApprove`（`:90-104`，REQUEST→APPROVED）、`batchReject`（`:106-121`，REQUEST→REJECT）、`refund`（`:123`，单 id）**均未声明于 `ILitemallAftersaleBiz`**（`app-mall-dao/.../ILitemallAftersaleBiz.java` 仅声明 apply/cancel/userList/userDetail 4 法）。跨实体无法经 `I*Biz` 程序化调用这些运营动作。
- 无 `batchRefund`；管理员"待退款"页（`LitemallAftersale.view.xml` wait-refund crud）逐行点退款。

**Admin AMIS 视图无退货字段：**
- `app-mall-web/.../pages/LitemallAftersale/LitemallAftersale.view.xml`（111 行）：grid 列含 id/aftersaleSn/orderId/orderItemId/type/reason/amount/status/handleTime/processTime/processNote；3 个 grid（list/wait-approve-list[status=REQUEST]/wait-refund-list[status=APPROVED]）；3 个 crud（main/wait-approve[batchApprove/batchReject]/wait-refund[单行 refund]）；Tabs（全部/待审批/待退款）。**无退货物流列、无"待收货"Tab、无收货确认按钮**。
- Storefront 无 AMIS 售后视图（小程序前端在外部仓库，经 GraphQL API 消费）。本计划仅暴露 GraphQL mutation，不交付小程序 UI。

**ErrorCode：** `AppMallErrors.java` 有 10 个 `ERR_AFTERSALE_*`（not-allow-refund/not-found/not-allow-apply/not-allow-cancel/refund-failed/amount-exceed/item-not-in-order/item-in-progress/type-status-mismatch/reason-invalid），**无退货物流/收货确认相关错误码**。

**Owner doc 明确将退货履约移出：** `order-and-cart.md:407` "当售后类型要求退货退款时，退款完成意味着售后流程闭环；退货入库等实现动作属于技术和履约实现，不在本 owner doc 展开。" —— 本 successor 需把退货履约语义补回 owner doc。

**roadmap 状态：** Phase 16/21 均 `done`；本计划为 P21 Deferred successor，不在 roadmap Phase 列表内（roadmap 顶层无对应 phase 行），完成后更新本计划 Deferred 来源引用。

## Goals

- 为 GOODS_REQUIRED（退货退款）补齐物理退货履约阶段：用户回寄 → 管理员确认收货 → 退款。
- `LitemallAftersale` 新增退货运单字段（物流公司/运单号/发货时间/收货确认时间）。
- 售后状态机为 GOODS_REQUIRED 扩展 `APPROVED → RETURNED → REFUND` 子路径（RETURNED = 用户已回寄、待管理员确认收货）。
- 新增用户 mutation `submitReturnLogistics`（APPROVED→RETURNED）与管理员 mutation `confirmReturnReceived`（RETURNED→还库+退款→REFUND）；将 GOODS_REQUIRED 的还库从 `refund()` 拆到 `confirmReturnReceived`。
- 售后运营方法（batchApprove/batchReject/refund/submitReturnLogistics/confirmReturnReceived）上 `ILitemallAftersaleBiz` 接口（关闭接口债）。
- Admin 视图增退货物流列 + "待收货"Tab + 收货确认按钮。
- owner doc `order-and-cart.md` 补退货履约语义（状态流转/字段/角色可见性）。
- 所有新增 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine` 测试。

## Non-Goals

- 退货入库的**库存估值/质检/二次上架**流程（本计划仅还库 `addStock`，与既有 `refund()` 还库口径一致）。
- 第三方物流实时轨迹对接（运单号仅记录与展示）。
- **小程序前端 UI**（外部仓库；本计划仅暴露 GraphQL mutation 供其调用）。
- GOODS_MISS / GOODS_NEEDLESS 流转改动（无物理退货，保持 APPROVED→REFUND 直达）。
- `batchRefund` / `batchConfirmReturnReceived`（不在 P21 Deferred 范围；单 id 与既有 `refund()` 一致）。
- 退货超时自动提醒/关闭（需调度任务，为 successor）。
- **审核通过后通知用户回寄**（APPROVED→通知"请退货"属 P35 通知中心的新事件投递，是独立结果面；当前 `batchApprove` 不发通知，退货地址/说明由运营在售后详情 processNote 记录、用户在售后详情可见 APPROVED 态与退货说明，不另起通知编排）。
- **Dashboard 待办计数新增"待收货"桶**（`getTodoAggregation` 现仅计 REQUEST 为 aftersalePendingReview；RETURNED 不进待办计数——RETURNED 售后在 Admin"待收货"Tab 暴露供运营审视，即该态的操作面，无需 Dashboard 重复计数）。

## Task Route

- Type: `implementation-only change`（业务设计将在本计划补入 `order-and-cart.md`；退货履约为 P16/P21 显式 deferred successor，无 net-new 业务域）
- Owner Docs: `docs/design/order-and-cart.md`（退款与售后章节：补退货履约子状态机/字段/流转规则）、`docs/design/flow-overview.md`（售后状态机图，按需同步 RETURNED 态）
- Skill Selection Basis: ORM 新增列 + 字典值（nop-orm-modeler、nop-database-design）、BizModel 新增 @BizMutation + 跨实体走 I*Biz + 还库拆分（nop-backend-dev）、AMIS 视图增 Tab/列/按钮（nop-frontend-dev）、API 测试 IGraphQLEngine（nop-testing）

## Protected Area

本计划 Phase 1 修改 `model/app-mall.orm.xml`（`LitemallAftersale` 新增 4 可空列 + `mall/aftersale-status` 字典新增 RETURNED 值）。按 `docs/context/ai-autonomy-policy.md` Protected Areas 表，XML models（`model/*.orm.xml`）为 **ask-first**。

- 触及文件：`model/app-mall.orm.xml`（1 实体加 4 可空列 + 1 字典加 1 值）
- 授权状态：**pending MISSION_DRIVER ORM 授权**（plan 已 active 并通过 4 轮审计；Phase 1 ORM 实施待显式 ORM 授权开工）
- 实施门控：Phase 1（ORM 实施）在获得 MISSION_DRIVER 显式 ORM 授权前不得开工。先例：P15/P20/P22(1610)/P24/P26/P27/P28/P32/P33 均按同一 ask-first 模式新增 ORM 列/实体，授权由 MISSION_DRIVER「execute the entire plan」类指令构成（见 `2026-06-28-1610-1` Phase 1 先例，已成功闭合）。
- 未获授权的降级路径：将 Phase 1 ORM 实施 + 依赖它的 Phase 2/3/4 整体移入 `Deferred But Adjudicated`（分类 model-gap，触发条件「获 ORM 授权时」）。本计划无 ORM-independent 切片（退货字段为子状态机前置依赖），故授权前保持 `draft`/blocked，不部分推进。

## Infrastructure And Config Prereqs

- 无新增基础设施。退款继续走既有 `PayService.refund`（微信支付通道，app-mall-wx）；还库继续走既有 `goodsProductMapper.addStock`。
- 无新增调度任务（退货超时提醒为 Non-Goal）。

## Execution Plan

### Phase 1 — 模型准备（Aftersale 退货字段 + RETURNED 状态值 + 接口声明）

Status: completed
Targets: `model/app-mall.orm.xml`、`app-mall-meta`、`app-mall-dao`（regen + 接口声明）、`_AppMallDaoConstants.java`
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Add | Decision | Proof`
- Prereqs: 无（Phase 1 受 `## Protected Area` ask-first 门控）

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读完其 routing table 标为必读的文档；列出已读路径。每处模型改动后用 skill selfcheck 校验命名/索引/域/字典规范。
  - Docs read: `.opencode/skills/nop-orm-modeler/SKILL.md`（propId 续排/stdSqlType 标准值/dict valueType=int/option code 全大写下划线 等校验清单）、`.opencode/skills/nop-database-design/SKILL.md`（列名 snake_case、INTEGER 统一、布尔 BOOLEAN 等规范）
- [x] **Decision: 退货阶段状态建模。** 抉择 A（推荐）：`mall/aftersale-status` 新增 `RETURNED`(6, "用户已退货待收货")，GOODS_REQUIRED 走 `APPROVED→RETURNED→REFUND`；GOODS_MISS/GOODS_NEEDLESS 保持 `APPROVED→REFUND` 直达（RETURNED 仅 GOODS_REQUIRED 经过）。备选 B（独立 `returnStatus` 标志位 + returnReceiveTime，主状态机不变）被否——主状态机已表达售后生命周期，新增平行标志位使前端 Tab 过滤/状态时间线双源更复杂，与既有"售后状态机独立持有"设计（`order-and-cart.md:367`）相悖。残留风险：RETURNED 仅对 GOODS_REQUIRED 有意义，refund() 守卫需按 type 区分（GOODS_REQUIRED 要求 RETURNED，其余要求 APPROVED）——在 Phase 2 落地。抉择/备选/残留风险写入 `order-and-cart.md`。
- [x] **Add:** `LitemallAftersale` 新增 4 可空列（propId 续排 18-21）：`returnShipChannel`(varchar 63, "退货物流公司")、`returnShipSn`(varchar 63, "退货运单号")、`returnTime`(datetime, "用户退货发货时间")、`receiveConfirmTime`(datetime, "管理员确认收货时间")。落地 `model/app-mall.orm.xml`。
- [x] **Add:** `mall/aftersale-status` 字典新增 `RETURNED` 值 6（"用户已退货待收货"）。落地 `model/app-mall.orm.xml`。
- [x] **Add:** `ILitemallAftersaleBiz` 接口声明**既有 3 运营方法**：`batchApprove`、`batchReject`、`refund`（关闭 P21 接口债；此 3 法已在 `LitemallAftersaleBizModel` 实现，声明上接口不破坏编译）。新增的 `submitReturnLogistics`/`confirmReturnReceived` 在 Phase 2 随实现一同声明上接口（避免 Phase 1 声明未实现抽象方法导致 `app-mall-service` 编译失败）。
- [x] **Add:** `mall/aftersale-status` 字典新增 `RETURNED` 值 6 由 regen 自动生成 `AFTERSALE_STATUS_RETURNED` 常量（按既有 `AFTERSALE_STATUS_*` 先例，已存于生成文件 `_AppMallDaoConstants.java:9-34`）；BizModel 引用 regen 产物，**不手改 `_` 前缀生成文件**。
- [x] **Proof:** regen 通过、编译通过；新列/字典值出现在生成 DDL 与 `_gen` 代码。验证命令：`./mvnw -pl app-mall-codegen -am generate-test-resources` + `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C`。

Exit Criteria:

- [x] 4 退货字段 + RETURNED 字典值落地 `model/app-mall.orm.xml`，regen + 全模块编译通过
- [x] `ILitemallAftersaleBiz` 声明 3 既有运营方法（batchApprove/batchReject/refund），`app-mall-service` 编译通过（新 2 法在 Phase 2 随实现声明，不在 Phase 1）
- [x] `docs/logs/` 更新

### Phase 2 — 后端（退货物流提交 + 收货确认 + 还库拆分）

Status: completed
Targets: `LitemallAftersaleBizModel.java`、`ILitemallAftersaleBiz.java`、`AppMallErrors.java`（若新增聚合 SQL 需要 `LitemallAftersale.sql-lib.xml`，本计划目前不引入新 SQL，复用既有 `recomputeOrderAftersaleStatus`/`hasInProgressAftersaleForItem`）
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Fix | Proof`
- Prereqs: Phase 1（ORM 字段 + RETURNED 状态 + 接口声明）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完 routing table 必读文档；列出已读路径。每个方法写完用 nop-backend-dev selfcheck 校验（NopException/ErrorCode、跨实体 I*Biz、@Inject 非 private、@BizMutation 事务）。
  - Docs read: nop-backend-dev SKILL.md（强制实现顺序/反模式表/CrudBizModel API 速查）、nop-testing SKILL.md（IGraphQLEngine 模式/@NopTestConfig/request() 规范/反模式表）。Mandatory 平台 docs：service-layer.md、error-handling.md、bizmodel-method-selfcheck.md（19 项逐方法校验）、testing.md。
- [x] **Add:** 用户 mutation `submitReturnLogistics(id, returnShipChannel, returnShipSn, ctx)`：守卫 `status==APPROVED && type==GOODS_REQUIRED`（否则 `ERR_AFTERSALE_NOT_ALLOW_SUBMIT_RETURN`）；写 returnShipChannel/returnShipSn/returnTime(now)；置 `APPROVED→RETURNED`；调用 `recomputeOrderAftersaleStatus(entity.getOrder(), ctx)` 保持订单级聚合正确（RETURNED 为进行中态；签名对齐既有调用点）。storefront 归属校验（entity.userId == ctx.userId）。声明 `submitReturnLogistics` 上 `ILitemallAftersaleBiz` 接口。
- [x] **Add:** 管理员 mutation `confirmReturnReceived(id, ctx)` `@Auth(roles="admin")`：守卫 `status==RETURNED`（否则 `ERR_AFTERSALE_NOT_ALLOW_CONFIRM_RETURN`）；写 receiveConfirmTime(now)；**执行还库**（GOODS_REQUIRED 的 addStock 从 refund 拆到此）；调用 `payService.refund` + 还券 + 还积分 + 通知（复用既有 refund 编排）；置 `RETURNED→REFUND`；调用 `recomputeOrderAftersaleStatus(entity.getOrder(), ctx)`。事务由 `@BizMutation` 包裹。声明 `confirmReturnReceived` 上 `ILitemallAftersaleBiz` 接口。
- [x] **Fix:** 进行中售后状态集纳入 RETURNED——`recomputeOrderAftersaleStatus`（`:492-494`，`FilterBeans.in(status,[REQUEST,APPROVED])`）与 `hasInProgressAftersaleForItem`（`:444-446`，同集合）均改为 `[REQUEST,APPROVED,RETURNED]`。否则 RETURNED 态会被误判为非进行中：(1) 订单级 `order.aftersaleStatus` 错误翻回 INIT（违反 `order-and-cart.md:382`）；(2) `apply()` 的 item 互斥失效，允许同一项并发二开售后（违反 `order-and-cart.md:359`）。
- [x] **Fix:** `refund()` 还库拆分——将 GOODS_REQUIRED 的 `shouldRestock` 还库分支从 `refund()` 移除（GOODS_REQUIRED 不再经 `refund()` 直达，改走 `confirmReturnReceived`）。`refund()` 仅保留 GOODS_MISS(未发货还库)/GOODS_NEEDLESS(不还库) 语义。refund() 守卫收紧：GOODS_REQUIRED 不允许经 refund()（要求经 confirmReturnReceived）。确保 GOODS_MISS/GOODS_NEEDLESS 的 APPROVED→REFUND 直达路径无回归。
- [x] **Add:** ErrorCode 新增 `ERR_AFTERSALE_NOT_ALLOW_SUBMIT_RETURN`（"当前售后状态不允许提交退货物流"）、`ERR_AFTERSALE_NOT_ALLOW_CONFIRM_RETURN`（"当前售后状态不允许确认收货"）于 `AppMallErrors.java`。
- [x] **Proof:** 新增/改动 `@BizMutation`（submitReturnLogistics/confirmReturnReceived）+ 守卫改动（refund）+ 进行中状态集改动通过 `IGraphQLEngine` 测试（`JunitAutoTestCase` 录制回放）：覆盖 GOODS_REQUIRED 全链（apply→approve→submitReturnLogistics→confirmReturnReceived→REFUND + 还库断言 + 订单级 aftersaleStatus 保持 REQUEST 断言）、GOODS_MISS/GOODS_NEEDLESS 回归（approve→refund→REFUND 无回归）、守卫拒绝（非 APPROVED 提交物流、非 RETURNED 确认收货、GOODS_REQUIRED 走 refund 被拒）、item 互斥（RETURNED 态同项不可二开售后）。验证命令：`./mvnw test -pl app-mall-service -am`。
- [x] **Note（in-flight 数据）:** 上线时已处于 APPROVED 的 GOODS_REQUIRED 售后会"搁浅"（refund() 守卫收紧后拒绝 GOODS_REQUIRED，confirmReturnReceived 要求 RETURNED）。参考应用基线无生产数据，不做数据迁移；若存在过渡数据，运营对搁浅单手工置 RETURNED 后走正常收货确认。

Exit Criteria:

- [x] GOODS_REQUIRED 走 apply→approve→submitReturnLogistics→confirmReturnReceived→REFUND，还库发生在 confirmReturnReceived（非 refund）
- [x] `ILitemallAftersaleBiz` 接口累计声明 5 运营方法（Phase 1 的 3 既有 + Phase 2 新增 submitReturnLogistics/confirmReturnReceived）
- [x] GOODS_MISS/GOODS_NEEDLESS 保持 approve→refund→REFUND 直达，无回归
- [x] RETURNED 纳入进行中状态集，订单级 aftersaleStatus 与 item 互斥无回归
- [x] 守卫拒绝路径正确（4 个新 ErrorCode + 既有 refund 守卫收紧）
- [x] **API 测试：** submitReturnLogistics/confirmReturnReceived 通过 `IGraphQLEngine`；refund 改动通过既有录制回放无回归
- [x] `docs/logs/` 更新

### Phase 3 — 前端（Admin 视图退货字段 + 待收货 Tab + 收货确认按钮）

Status: completed
Targets: `app-mall-web/.../pages/LitemallAftersale/LitemallAftersale.view.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add | Proof`
- Prereqs: Phase 2（后端 mutation 就绪）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完 routing table 必读文档；列出已读路径。视图改完用 skill selfcheck 校验（XView 三层/bounded-merge/x:prototype）。
  - Docs read: nop-frontend-dev SKILL.md（XView 三层模型/x:override 合并算子/bounded-merge/x:prototype/x:prototype-override/反模式表）。Mandatory 平台 docs：view-and-page-customization.md、delta-customization.md、add-page-business-action.md。
- [x] **Add:** grid `list` 增退货物流列（returnShipChannel/returnShipSn/returnTime/receiveConfirmTime，status=RETURNED/REFUND 时可见）。
- [x] **Add:** 新 grid `wait-receive-list`（filter status=RETURNED）+ crud `wait-receive`（单行"确认收货"按钮调 `confirmReturnReceived`），Tabs 增"待收货"页（全部/待审批/待收货/待退款）。
- [x] **Add:** 售后详情/表单展示退货物流字段（RETURNED/REFUND 态只读回显 returnShipChannel/returnShipSn/returnTime/receiveConfirmTime + 状态时间线增 RETURNED 节点）。
- [x] **Proof:** `./mvnw -pl app-mall-web -DskipTests compile` 通过；视图 XML 合法（bounded-merge 不破坏 _gen 基线）。

Exit Criteria:

- [x] Admin 视图含退货物流列 + 待收货 Tab + 收货确认按钮，编译通过
- [x] 状态时间线含 RETURNED 节点
- [x] `docs/logs/` 更新

### Phase 4 — Owner Doc 同步 + 闭合

Status: completed
Targets: `docs/design/order-and-cart.md`、`docs/design/flow-overview.md`（按需）
Required Skill: `none`（文档同步，无平台技能匹配）

- Item Types: `Add | Proof`
- Prereqs: Phase 1-3

- [x] **Add:** `order-and-cart.md` 退款与售后章节补"退货履约（GOODS_REQUIRED）子状态机"段：RETURNED 态语义、APPROVED→RETURNED→REFUND 流转、退货字段、还库发生在 confirmReturnReceived、Decision A 抉择/备选/残留风险。替换 `:407` "退货入库等实现动作不在本 owner doc 展开"为引用本段。
- [x] **Add:** `flow-overview.md` 售后状态机图：若该图穷举售后状态值则补 RETURNED 态（经实读该图确认后再改；若图仅为高层语义不列具体值则不改并在日志记录原因）。
- [x] **Proof:** owner doc 与 live 代码一致（状态值/字段名/方法名核对）。

Exit Criteria:

- [x] `order-and-cart.md` 含退货履约子状态机段，与代码一致
- [x] `docs/logs/` 更新（聚合闭合日志）

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent（fresh session），4 轮对抗式审计
- Evidence:
  - R1 `ses_0f1bc23f1f`：revise — MAJOR-1（接口声明 2 新法在 Phase 1 致编译断）/ MAJOR-2（RETURNED 未纳入进行中状态集 `recomputeOrderAftersaleStatus:492-494` + `hasInProgressAftersaleForItem:444-446`，致订单聚合 + item 互斥回归）。baseline 准确、Protected Area 合规。
  - 修订：接口声明拆分（Phase 1 仅声明 3 既有法，2 新法随 Phase 2 实现）；新增 Phase 2 Fix 项把 RETURNED 纳入两个进行中集合；通知/Dashboard/in-flight 数据在 Non-Goal/Note 显式裁定。
  - R2 `ses_0f1b30652f`：revise — 残留 1 Major（Phase 1 Exit Criteria 误写"5 运营方法"，与自身 Item 的 3 法矛盾）。MAJOR-2 已修。
  - 修订：Phase 1 Exit Criteria 改 3 法 + 新增 Phase 2"接口累计 5 法"Exit Criteria；sql-lib 按需 Targets 收紧 + 增 `ILitemallAftersaleBiz.java` Target；调用点签名对齐 `recomputeOrderAftersaleStatus(entity.getOrder(), ctx)`。
  - R3 `ses_0f1ac8584f`：revise — 1 pre-existing Major（line 95 指示手改生成文件 `_AppMallDaoConstants.java`，违 AGENTS.md）。
  - 修订：改为 regen 自动生成常量、显式禁手改 `_` 前缀文件。
  - R4 `ses_0f1a67783f`：**pass** — 无 blocker、无 major；生成文件禁手改已落地；Required Skill/API 测试/Closure Gates 齐全；baseline 抽验准确。
  - 共识：4 轮渐进收敛，末轮 clean（无 blocker/major）。实施仍受 ORM ask-first 门控（Phase 1 获授权前不得开工）。

## Closure Gates

- [x] in-scope behavior is complete（退货履约全链 + 接口债 + Admin 视图 + owner doc）
- [x] relevant docs are aligned（order-and-cart.md / flow-overview.md）
- [x] verification has run（regen + 全模块编译 + service 测试 + web 编译）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（submitReturnLogistics/confirmReturnReceived）；refund 改动无回归
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop-platform phases 不写 `none`（Phase 4 文档同步为非平台 phase，`none` 有正当理由）
- [x] skill loading verification: each phase scanned + loaded + read mandatory docs + selfchecked
- [x] text consistency verified: status, phases, gates, log all agree
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

> 起草时无可延期项。退货超时自动提醒、batchRefund/batchConfirmReturnReceived、第三方物流轨迹、小程序 UI、审核通过通知用户回寄、Dashboard 待收货桶 在 Non-Goals 显式移出（含理由）。

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: <待实施 + 闭合审计后填写>

Closure Audit Evidence:

- Reviewer / Agent: <independent reviewer — MUST NOT be the implementing agent>
- Evidence: <task id / log link / walkthrough record>
