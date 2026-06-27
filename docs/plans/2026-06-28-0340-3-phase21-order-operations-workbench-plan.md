# phase21 订单运营工作台

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Mission: mall
> Work Item: Phase 21 — 订单运营工作台（enhanced-features-roadmap.md P21）
> Source: `docs/backlog/enhanced-features-roadmap.md` §21（订单运营工作台交付范围）
> Related: `2026-06-27-1742-3-phase16-order-item-aftersale-plan.md`（P16 的「退货入库/物流回寄」follow-up 明确要求**开 successor 计划**，本计划不纳入——见 Non-Goals）
> Audit: required

## Current Baseline

**订单管理页功能薄：** `app-mall-web/.../pages/LitemallOrder/LitemallOrder.view.xml`（73 行，bounded-merge on `_gen`）列 orderSn/userId/orderStatus/consignee/mobile/各价格/payTime/shipTime，**唯一行级动作是 `ship`（`visibleOn orderStatus==201`）**。无批量发货/改价/改运费/改地址/标记备注/异常监控/合单拆单/模糊搜索页。无工作台页。`adminRemark` 列（propId 40）**已存在于 ORM（`app-mall.orm.xml:1134`）与 `_gen` 视图，但 customized view 的列清单（`:7-18`）未含**，需 surface。

**订单 BizModel 既有动作：** `LitemallOrderBizModel.java`（1076 行）含 `submit`/`cancel`/`prepay`/`pay`/`confirmPaidByNotify`/`ship(orderId,shipSn,shipChannel,ctx)`（`:632`，唯一发货入口，`@Auth(roles="admin")`）/`confirm`/`deleteOrder`/`myOrders`/`getMyOrder`/`cancelExpiredOrders`/`confirmExpiredOrders`/`createFlashSaleOrder`。**缺**：批量发货、改价/改运费、改地址、订单标记、异常监控查询、合单/拆单。地址仅在 `submit`/`createFlashSaleOrder` 写入（`:248`/`:910`），**无 post-creation changeAddress**。orderSn 仅精确匹配（`_LitemallOrder.xmeta:33-36` queryable 无操作符；`confirmPaidByNotify` 用 `eq`）。

**售后审核工作台已有 MVP（P16）：** `LitemallAftersaleBizModel.java` 含 `batchApprove(ids)`（`:90`）/`batchReject(ids)`（`:106`）/`refund(id)`（`:123`）。视图 `LitemallAftersale.view.xml` 已有 main/wait-approve/wait-refund 三 tab，菜单 `mall-aftersales-manage`(205)。**退货入库/物流回寄 P16 已 DEFERRED 并明确要求开 successor 计划**（`2026-06-27-1742-3-…md:227`），**不在本计划范围**（见 Non-Goals）。`batchApprove`/`batchReject`/`refund` 未声明于 `ILitemallAftersaleBiz` 接口为既有技术债，归同一 P16-successor 处理，不在本计划。

**统计/异常查询：** `getOrderStatistics`/`getGoodsSalesRanking`/`getUserStatistics` 已迁移为原生 SQL 聚合（`LitemallOrder.sql-lib.xml`）——**AR-22 已 RESOLVED**（`2026-06-13-statistics-performance-plan.md` completed，无内存全表聚合）。**无任何异常监控查询**（超期未发货 status=201+shipTime 截止、超期未支付 status=101+addTime 截止）；`cancelExpiredOrders`/`confirmExpiredOrders` 为调度翻转，不暴露「逾期订单列表」供工作台。

**Excel 能力：app-mall-service/dao 未引入 nop-excel，但平台可复用。** app-mall-service/dao 各 pom 无 poi/easyexcel/nop-excel 依赖（注：`app-mall-web`/`app-mall-codegen` 已含 `nop-ooxml-xlsx`）；Nop 平台 `nop-entropy/nop-format/nop-excel/` 模块**已存在**，属**依赖接线 + 平台复用**，非从零自建。注意：`nop-ooxml` 为 parent pom 模块，可直接消费的是 `nop-ooxml-xlsx`（仅当需低层 xlsx 解析）；平台无 `IExcelTransformer` 此名类（实施时按 `nop-excel` 实际 reader API 接线）。

**菜单：** `mall-order-manage`(204)/`mall-aftersales-manage`(205) 现存；`mall-manage`(200) 段有空隙供工作台子项。

## Goals

- 提供批量发货工作台：Excel 导入运单号批量发货（接线平台 `nop-excel`），复用 `ship` 单行逻辑。
- 提供订单运营操作：改价/改运费（带折扣层安全守卫，见 Decision）、改地址（仅发货前）、订单标记（surface 既有 `adminRemark`）、orderSn 模糊搜索。
- 提供异常监控工作台：超期未发货/超期未支付订单列表查询 + 提示。
- 通过 `IGraphQLEngine` 测试所有新增 `@BizMutation`/`@BizQuery`。

## Non-Goals

- **退货入库/物流回寄（P16 successor）：** P16 的 follow-up（`…phase16…md:227`）明确要求「开 successor 计划」，涉及 `LitemallAftersale` 加退货运单字段（ORM ask-first Protected Area）+ APPROVED→REFUND 间子状态机扩展，属独立结果面，不在本计划。`batchApprove/batchReject/refund` 未上 `ILitemallAftersaleBiz` 接口的既有债一并归该 successor。
- 合单/拆单（跨订单合并/拆分需重写价格与库存语义，高复杂度，留 successor）。
- 批量打印快递单/发货单（模板化与浏览器打印入口**均不交付**，需打印基建，整体留 successor——见 Deferred）。
- 第三方物流 API 实时轨迹对接（超出商业基线）。
- 订单级退款审批中间态（202）启用（设计上保留给未来订单级审批路径）。
- 改商品价（goodsPrice）在订单存在任何活动折扣时的支持（见 Decision，会破坏折扣分摊/上限语义，拒绁）。

## Task Route

- Type: `implementation-only change`（订单状态机、价格语义、查询展示设计已在 `order-and-cart.md` 落地；本计划为运营工作台实现，无 ORM/Protected Area 改动）
- 范围对齐：本计划严格对应 `enhanced-features-roadmap.md` §21 交付范围；P16 的退货入库 follow-up 经审计裁定为独立 successor（P16 自身已声明「开 successor 计划」），避免本计划范围漂移与单计划多结果面（plan guide 规则 #4）。
- Owner Docs: `docs/design/order-and-cart.md`（订单状态机、退款与售后、查询与展示规则）、`docs/design/system-configuration.md`（管理员操作日志、定时运营任务）
- Skill Selection Basis: BizModel 新增 @BizMutation/@BizQuery + 平台 Excel 接线（nop-backend-dev）、AMIS 工作台/批量导入页（nop-frontend-dev）、API 测试（nop-testing）。无 ORM 改动，故不需 nop-orm-modeler。

## Infrastructure And Config Prereqs

- **Excel 依赖接线（前置）：** 在 `app-mall-service`（或 `app-mall-dao`）`pom.xml` 新增平台 `nop-excel` 依赖（已验证存在于 `nop-entropy/nop-format/nop-excel/`；如需低层 xlsx 解析另加 `nop-ooxml-xlsx`，注意 `nop-ooxml` 本身是 parent pom 不可直接消费）。批量发货解析经平台 reader API（实施时按 `nop-excel` 实际入口，非 `IExcelTransformer`——该名不存在）。
- 异常监控查询口径依赖系统配置（订单超时/自动收货时长，复用既有 NopSysVariable）。
- 无外部服务依赖；无 ORM/Protected Area 改动。

## Execution Plan

### Phase 1 — 后端订单运营动作 + 异常监控 + Excel 批量发货

Status: completed
Targets: `app-mall-service/.../entity/LitemallOrderBizModel.java`、`app-mall-dao/.../sql/LitemallOrder.sql-lib.xml`、`LitemallOrderMapper.java`、`app-mall-meta/.../model/LitemallOrder/_LitemallOrder.xmeta`、对应 IBiz 接口、`app-mall-service/pom.xml`（nop-excel 依赖）
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Decision | Add | Proof`
- Prereqs: 无

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完必读文档；列路径。每方法 selfcheck（状态守卫、NopException+ErrorCode、@BizMutation 不叠 @Transactional、@Auth(roles="admin")、批量操作事务边界、平台库先确认再用）。
  - Docs read: `nop-entropy/docs-for-ai/02-core-guides/service-layer.md`、`04-reference/safe-api-reference.md`、`02-core-guides/error-handling.md`、`05-examples/test-examples.java`、`02-core-guides/testing.md`、`03-modules/nop-file.md`（IFileStore/UploadRequestBean 文件存储写入路径）
- [x] **Decision: 改价/改运费安全策略（B1 价格腐蚀修正）。** 抉择：改运费恒允许（仅重算 orderPrice/actualPrice）；改商品价仅纯商品订单（couponPrice/promotionPrice/integralPrice/grouponPrice/pinTuanPrice 全 0）允许，任一折扣非 0 抛 `ERR_ORDER_PRICE_MODIFY_DISCOUNT_ACTIVE`；已支付订单不允许改价。已写入 `order-and-cart.md`「改价/改运费安全策略」。
- [x] **Add:** `modifyOrderPrice(orderId, freightPriceDelta?, goodsPriceDelta?, remark, ctx)` `@BizMutation @Auth(roles="admin")`：状态守卫（仅待支付 101）+ 折扣层守卫，重算并写管理员操作日志。
- [x] **Add:** `batchShip(excelUpload, ctx)` `@BizMutation @Auth(roles="admin")`：经 `nop-excel`（`ExcelHelper.readSheet`）解析，逐行复用 `ship` 逻辑，汇总成功/失败行返回；部分失败不阻断成功行。
- [x] **Add:** `changeOrderAddress(orderId, addressId, ctx)` `@BizMutation @Auth(roles="admin")`：仅发货前（101/201）可改，`ILitemallAddressBiz` 校验归属同一用户。
- [x] **Add:** `markOrder(orderId, adminRemark, ctx)` `@BizMutation @Auth(roles="admin")`：写既有 `adminRemark` 字段。
- [x] **Add:** orderSn 模糊搜索：Nop 默认查询操作符 `filter_orderSn__contains` 已可用（orderSn `queryable="true"`），经 `testOrderSnFuzzySearch` 验证，无需额外 xmeta 配置。
- [x] **Add:** 异常监控 `@BizQuery @Auth(roles="admin")`：`getOverdueUnshippedOrders(cutoffHours)`（status=201 且 addTime 截止）、`getOverdueUnpaidOrders(cutoffMinutes)`（status=101 且 addTime 截止）。cutoff 复用系统配置（默认 168h/30min）。
- [x] **Proof:** `IGraphQLEngine` 覆盖（`TestLitemallOrderOpsWorkbench` 13 例全绿）：modifyOrderPrice（改运费成功/改商品价纯订单成功/带折扣拒绝/非法状态拒绝）、batchShip（成功+部分失败）、changeOrderAddress（发货前+发货后拒）、markOrder、模糊搜索、异常监控两查询（含空集与有集）。

Exit Criteria:

- [x] 批量发货（Excel 经 nop-excel）可用，部分失败正确反馈
- [x] 改价/改运费含折扣层与状态守卫，改地址/标记/模糊搜索可用
- [x] 异常监控两查询返回正确逾期集合
- [x] **API 测试：** 所有新增 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine` 测试
- [x] `docs/design/order-and-cart.md`、`system-configuration.md` 更新（订单运营动作、改价安全策略、异常监控口径）
- [x] `docs/logs/` 更新

### Phase 2 — 前端订单工作台 + 批量发货页 + 异常监控页 + 菜单接线

Status: completed
Targets: `app-mall-web/.../pages/LitemallOrder/LitemallOrder.view.xml`（surface adminRemark + 加动作）、`app-mall-web/.../pages/mall/order-ops/*.page.yaml`（新增）、`app-mall-web/.../auth/app-mall.action-auth.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 1

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完必读文档；列路径。每页 selfcheck。
  - Docs read: `nop-entropy/docs-for-ai/00-start-here/application-project-defaults.md`、`02-core-guides/view-and-page-customization.md`、`03-runbooks/add-page-business-action.md`、`03-modules/nop-file.md`（AMIS upload receiver `/f/upload`）；`std-domain.dict.yaml` 验证 `decimal`/`string` 为合法 std-domain
- [x] **Add:** `LitemallOrder.view.xml` surface `adminRemark` 列 + 加行级动作（改价/改地址/标记，按状态 `visibleOn`）；grid query orderSn 加模糊操作符（默认操作符 `filter_orderSn__contains` 已可用）。
- [x] **Add:** 批量发货页 `mall/order-ops/batch-ship.page.yaml`：AMIS upload/excel 导入 → 调 `batchShip` → 结果表格（成功/失败行）。
- [x] **Add:** 异常监控页 `mall/order-ops/order-exception.page.yaml`：两 tab 消费 `getOverdueUnshippedOrders`/`getOverdueUnpaidOrders`。
- [x] **Add:** `app-mall.action-auth.xml`：`mall-manage`(200) 下新增 `mall-order-batch-ship`(208)/`mall-order-exception`(209) 菜单。

Exit Criteria:

- [x] 批量发货/异常监控/改价改地址标记页可渲染并正确消费后端
- [x] 菜单接线完成
- [x] `docs/design/system-configuration.md` 更新（订单运营工作台菜单与运营动作）
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent（fresh session），两轮
- Evidence:
  - Round-1（task `ses_0f55cf47bffe`）：`revise` — B1 `modifyOrderPrice` 价格腐蚀缺口、B2 Excel 依赖未定且误名 `IExcelTransformer`、M1 范围过宽（P16 successor 应独立）、M3 greenfield 误述 + minors。
  - 已修订：改价 Decision（改运费恒允许/改商品价仅纯订单）、Excel 改 commit 平台 `nop-excel`+`nop-ooxml-xlsx`（已验证存在）并声明 pom 依赖、退货入库+接口债移出至 P16-successor（计划现 ORM-free、2 phase）、greenfield 改述为平台复用。
  - Round-2（task `ses_0f550fa75ffe`）：`pass` — 全部 blocker/major 经 live repo 核验已解决；无 Protected Area 暴露；剩余 notes 为基线着色细节（ExcelReportHelper 位置、nop-ooxml-xlsx 等）已修。计划审计洁净，可 `active`。

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs are aligned（`order-and-cart.md`、`system-configuration.md`）
- [x] `enhanced-features-roadmap.md` Phase 21 状态：计划通过审计时已由 `todo` 翻为 `planned`；闭合审计通过后翻为 `done`
- [x] verification has run（`mvn` 编译 + `IGraphQLEngine` 测试全绿；AMIS 页面经 `app-mall-web compile` 通过）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed, and Nop-platform phases do not write `none` without justification
- [x] skill loading verification: each phase scanned available skills, loaded all matching skills, read ALL mandatory docs listed in skill routing tables（路径列于 skill loading gate），selfcheck 无 anti-pattern
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 退货入库/物流回寄 + 售后接口债（P16 successor）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: P16 follow-up（`…phase16…md:227`）明确要求「开 successor 计划」；涉及 ORM ask-first Protected Area（`LitemallAftersale` 加退货运单字段）+ APPROVED→REFUND 子状态机扩展 + `batchApprove/batchReject/refund` 上 `ILitemallAftersaleBiz` 接口债，属独立结果面（plan guide 规则 #4），不在订单运营工作台范围。
- Successor Required: `yes`（触发条件：业务需要实物退货履约时，开 P16-return-successor 计划）

### 合单/拆单

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 跨订单合并/拆分需重写价格/库存/优惠分摊语义，高复杂度。
- Successor Required: `yes`（触发条件：业务出现高频合单/拆单需求时）

### 批量打印快递单/发货单

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 需打印模板/label 基建，本计划不交付任何打印入口（模板化与浏览器打印均不交付）。
- Successor Required: `yes`（触发条件：运营需要规范快递面单/发货单模板时）

### 第三方物流实时轨迹对接

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 超出商业基线；本计划运单号仅作记录与展示。
- Successor Required: `no`（触发条件：业务需要实时轨迹时引入物流 API 集成）

## Closure

Status Note: closed — 独立 subagent（fresh session `ses_0f48e0fc3ffe`）执行闭合审计，9 维全部 PASS，无 blocker。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure auditor（explore subagent，不同 session/context 于实现 agent）
- Evidence: task `ses_0f48e0fc3ffe`（2026-06-28）。重新核验所有 closure gate（不信实现 agent 自填的勾）。结论 `pass`：
  - 后端 6 方法（`LitemallOrderBizModel.java:1114-1387`）均带 `@BizMutation`/`@BizQuery` + `@Auth(roles="admin")`，零 `@Transactional`；接口 6 声明、6 ErrorCode、`BatchShipResultBean` DTO 齐备。
  - 改价安全策略：CREATED-only 状态守卫 + 5 折扣字段 goods 守卫 + 已支付拒（`:1125-1145`）。
  - `batchShip` per-row catch-and-continue + 结果收集（`:1236-1275`）。
  - `nop-ooxml-xlsx` 接线（`pom.xml:88-91`）；`TestLitemallOrderOpsWorkbench` 13 例覆盖 Proof 项 1:1。
  - 前端：view adminRemark surface + 3 行动作（状态 visibleOn）+ batch-ship page + order-exception page + action-auth 菜单 208/209 齐备。
  - 文档 `order-and-cart.md`/`system-configuration.md`/`docs/logs/2026/06-28.md` 对齐；roadmap Phase 21 = `done`。
  - 反模式检查通过（无 `@Inject private`/`dao().getEntityById`/`new Entity()`/`@Transactional` on @BizMutation/裸 RuntimeException）。
  - 闭合审计后修正两处 comment imprecision（modifyOrderPrice Javadoc 发货前态措辞、batchShip 事务边界注释），code 行为与权威设计文档本就一致。

Follow-up:

- 无阻断 follow-up。合单/拆单、批量打印快递单/发货单、退货入库/物流回寄（P16 successor）已按 Non-Goals / Deferred But Adjudicated 归独立 successor 计划。
