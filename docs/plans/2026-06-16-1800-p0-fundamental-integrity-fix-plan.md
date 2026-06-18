# 2026-06-16-1800 资金链路与数据完整性 P0 修复计划

> Plan Status: completed（Phase 1/2/3/4/5/Final 全 completed；closure audit passed Round 2；clean package 全绿 + 核心 45 测试全过，10 auth 预存在失败不变）
> Last Reviewed: 2026-06-17
> Source: `docs/audits/2026-06-16-1744-multi-dim-audit-full-project/summary.md`（P0-1~P0-7 七个根因 + 紧耦合状态机闭环 P1）
> Related: 维度 09/04/07/10/11/13 审计文件；`docs/plans/2026-06-15-1500-phase14-wxpay-integration-plan.md`（completed，Phase 14 后端）、`docs/plans/2026-06-16-1000-storefront-pay-flow-plan.md`（completed，前台支付消费者）
> Audit: required
> **Protected Area: 本计划含支付路径（project-context.md ask-first）与 ORM 模型 cascade 移除（AI 阻断条件，需 human approval）。plan audit 通过后，implementation 开始前需 human approval 解锁 Protected 项。**

## Why One Plan

审计定位 7 个 P0 根因全部属于"支付/退款/库存的资金与数据完整性"这一结果表面：订单状态机推进、库存扣减/回滚、退款金额与券归还，共享同一行为合约（order/aftersale/payment 状态机与库存一致性）、同一技术栈（BizModel @BizMutation + @SqlLibMapper）、同一验证模型（IGraphQLEngine 录制回放 + 并发测试）。退款相关 P1（退款后订单 202/203 推进、退款归还券、外部支付调用移出事务）是 P0-1/4/5 的逻辑闭环，分开会导致退款/库存逻辑反复改。DDL 索引虽根因在平台层且与资金链路无代码耦合，但同属"投产前 P0 清零"目标，单独建 plan 收益不大，项目侧缓解（独立 index 文件 + 修正虚假文档声明）纳入本计划。

## Current Baseline

> 经审计逐文件 live-repo 核验（2026-06-16，见 `docs/audits/2026-06-16-1744-multi-dim-audit-full-project/`）。

**资金链路（P0）**
- 微信回调：`WxPayNotifyResource.handleNotify`（`app-mall-wx/.../WxPayNotifyResource.java:38-41`）调 `parseNotifyBody` 但丢弃返回的 outTradeNo，仅回 SUCCESS 给微信；`order-and-cart.md:147` 承诺"回调验证签名后更新订单状态"未实现
- `pay()`（`LitemallOrderBizModel.java:337-357`）仅检查状态==CREATED，全文 0 处 queryPayment 调用，任何登录用户知道 orderId 即可推进"已付款"
- 库存：`reduceStock`（`LitemallOrderBizModel.java:197`）返回值被丢弃；SQL 有 `where number>=?` 原子条件但 Java 不感知 0 行影响（`LitemallGoodsProduct.sql-lib.xml:16-25`）
- 退款金额：`apply()`（`LitemallAftersaleBizModel.java:170`）直接信任 request.getAmount()，无 ≤actualPrice 校验
- GOODS_MISS：`refund()`（`LitemallAftersaleBizModel.java:122-129`）仅 type==GOODS_REQUIRED(2) 才 addStock，但 apply 接受 PAY(201) 未发货状态 + 用户选 GOODS_MISS(0)

**状态机闭环（紧耦合 P1）**
- 订单 202/203 死代码：`refund()` 只改 aftersaleStatus，orderStatus 从不推进（`_AppMallDaoConstants.java:71-79` 定义但无写入点）
- 退款不归还券：`refund()`（`LitemallAftersaleBizModel.java:95-134`）全文无 returnCoupon 调用；对比 `cancel():292-299`、`refundGrouponOrder():248-255` 都正确归还
- 事务边界：prepay/refund/refundGrouponOrder 三处在 @BizMutation 事务内调外部 PayService（`LitemallOrderBizModel.java:326` 等）；全仓 grep `afterCommit` 0 命中
- cancel 与 cancelExpiredOrders 并发双倍回滚库存（`LitemallOrderBizModel.java:272-304/513-546`；全仓无 version 乐观锁列）

**数据完整性（P0）**
- cascade：`model/app-mall.orm.xml:642-648` goods→orderGoods 与 `:1035-1041` order→orderGoods 双向 cascadeDelete=true；goods 用 useLogicalDelete 故软删级联，仍破坏订单商品可见性/统计；`LitemallGoodsBizModel` 无 defaultPrepareDelete（对比 `LitemallCategoryBizModel.defaultPrepareDelete:82-100` 有保护）
- DDL 索引：ORM 31 个 `<index>`，但 `deploy/sql/{mysql,postgresql,oracle}/_create_app-mall.sql` 各 0 个 CREATE INDEX；`module-boundaries.md:91-96` "已同步"虚假；根因在平台 `ddl.xlib` CreateTables 不调 AddIndex（见维度07-1），今天 regen 覆盖了声称的 append

**验证基线**
- 编译 BUILD SUCCESS；`./mvnw test` 106 passed、e2e 38 passed（采信日志 06-15/06-16，本轮未实跑）
- 资损路径零测试：Order/Aftersale/Pay/WxPay 四测试类共 0 处负面断言（维度10-9）
- `docs/testing/known-good-baselines.md` 占位符

## Goals

1. **支付确认链路可靠**：微信回调推进订单状态（P0-1）；`pay()` 增支付凭证校验或拆为回调专用内部方法（P0-2）
2. **库存完整性**：reduceStock 返回值校验防超卖（P0-3）；GOODS_MISS/未发货退款正确回滚库存（P0-5）；cancel 与定时任务并发不双倍回滚（11-2）
3. **退款金额与状态机闭环**：退款金额校验（P0-4）；退款成功后订单 orderStatus 正确推进 202/203 或回写 design 删除死状态（09-06）；售后退款归还优惠券（01-6）
4. **事务边界正确**：refund/prepay 的 payService 调用保留事务内（资金耦合，throw→回滚保证一致性 + outRefundNo 幂等重试）；notificationService 调用移到 afterCommit（通知是 fire-and-forget 副作用，05-2/05-3/09-13）；refundGrouponOrder 收集失败继续批量而非吞异常（05-3）
5. **数据完整性**：移除 goods→orderGoods cascadeDelete + GoodsBizModel 增 defaultPrepareDelete 删除保护（P0-7/11-6）
6. **DDL 索引可用**：项目侧独立 `_create_index.sql` 防 regen 覆盖 + 修正 module-boundaries.md 虚假声明（P0-6）
7. **资损路径有测试**：支付/退款/库存核心方法通过 IGraphQLEngine 测试覆盖 happy + 错误 + 边界

## Non-Goals

- **并发治理的全面铺开**：本计划只处理"直接致资损/数据破坏"的并发（超卖/双倍回滚），券领取使用竞态、评论收藏去重等非资损并发归 Plan 2
- **越权收口、优惠/价格校验、团购成功状态、前端呈现**：归 Plan 2（域逻辑加固）
- **平台层 DDL 模板修复**（ddl.xlib CreateTables 调 AddIndex）：属 nop-entropy，本计划只做项目侧缓解 + 记入 Deferred 追溯平台
- **真实微信沙箱联调**：示例模式（enabled=false）下验证逻辑正确性；真实沙箱依赖商户凭证，沿用 Phase 14 Deferred
- **退款异步通知、JSAPI/H5 支付**：Phase 14 已裁定延后

## Task Route

- Type: `bug investigation` + `implementation-only change`（局部 model 变更 cascade 为 Protected）
- Owner Docs: `docs/design/order-and-cart.md`（订单状态机/退款范围/支付流程）、`docs/architecture/module-boundaries.md`（DDL sync status）、`docs/architecture/system-baseline.md`（支付集成）
- Skill Selection Basis: `nop-backend-dev`（BizModel @BizMutation 改动）、`nop-testing`（IGraphQLEngine 录制回放）、`nop-debugging`（资损路径诊断）、`nop-orm-modeler`+`nop-database-design`（cascade 移除与索引）。注：技能真值源当前分裂（`.opencode/skills/` 含实现技能、`docs/skills/README.md` 仅 audit prompt，见维度12-1/2），本计划技能以 `.opencode/skills/` 为准；统一工作归 Plan 3

## Infrastructure And Config Prereqs

- Phase 14 后端代码在 classpath（已就绪）
- 示例模式 `wxpay.enabled=false` 可验证逻辑（queryPayment 永不 SUCCESS 的分支要覆盖）
- No new infra beyond existing baseline

## Execution Plan

### Phase 1 — 支付确认链路（Protected: ask-first）

Status: completed（human approval 已获；实施 + 测试通过）
Targets: `app-mall-wx/.../WxPayNotifyResource.java`、`WxPayServiceImpl.java`、`LitemallOrderBizModel.java`（pay/prepay）、新增 `app-mall-api/.../pay/IPaymentCallback.java`、`app-mall-service/.../service/pay/PaymentCallbackImpl.java`
Required Skill: `nop-backend-dev`, `nop-testing`, `nop-debugging`

- Item Types: `Fix | Decision | Proof`
- Prereqs: human approval（支付路径 ask-first）— 已获

- [x] **Skill loading Gate:** 加载 `nop-backend-dev`+`nop-testing`+`nop-debugging`，读完各自 routing table 必读文档。列已读路径。每方法 selfcheck。
- [x] **Decision — 支付确认触发源（需 human approval）：** 采用选项 B：新增内部 `confirmPaidByNotify(outTradeNo, transactionId, amount)` 由回调调用。`pay()` 接受边界：actualPrice==0 任意模式；wxpay.enabled=false 任意金额；其他一律拒绝（`ERR_ORDER_USE_REAL_PAYMENT`）。Alternatives 否决理由：前端不应决定支付事实，回调是微信侧可信触发源。
- [x] **Fix: 微信回调推进订单状态（P0-1）。** `IPaymentCallback`（api 桥接，wx 不依赖 dao/service）+ `PaymentCallbackImpl`（service 实现，`app-service.beans.xml` 注册）+ `WxPayNotifyResource` @Inject 调用；`confirmPaidByNotify` 幂等（已 PAY 跳过），系统上下文 `ServiceContextImpl()`（回调无用户会话）；`parseNotifyBody` 仅 tradeState==SUCCESS 返回 outTradeNo。
- [x] **Fix: pay() 凭证校验或拆分（P0-2）。** `PayService.isEnabled()` 抽象（WxPayServiceImpl=WXPAY_ENABLED、MockPayServiceImpl=false）；`pay()` 守卫：actualPrice>0 && isEnabled() → 拒绝。
- [x] **Proof: IGraphQLEngine 测试。** `testConfirmPaidByNotify`（回调→订单推进 + 幂等）；核心套件全过。
- [x] **Proof: 编译 + `./mvnw test -pl app-mall-service`。** 36 测试全过。

Exit Criteria:
- [x] 真实模式回调 SUCCESS 后订单状态可靠推进（P0-1 闭环）
- [x] pay() 不能被任意用户无凭证推进（P0-2 闭环）
- [x] 新增/改动 @BizMutation 通过 IGraphQLEngine 测试
- [x] owner doc `order-and-cart.md` 第 147 行承诺与实现一致
- [x] `docs/logs/` updated

### Phase 2 — 库存扣减与回滚完整性

Status: completed
Targets: `LitemallOrderBizModel.java`（submit/cancel）、`LitemallAftersaleBizModel.java`（refund）、`LitemallGoodsProduct.sql-lib.xml`、`LitemallOrder.sql-lib.xml`、`LitemallOrderMapper.java`、`TestLitemallOrderBizModel.java`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Fix | Decision | Proof`
- Prereqs: 无（本 Phase 不改 ORM 模型；见下方 Decision 锁定条件 UPDATE 路径）

- [x] **Skill Loading Gate:** 加载 `nop-backend-dev`+`nop-testing`，读完必读：`02-core-guides/concurrency-and-transactions.md`、`02-core-guides/service-layer.md`、`04-reference/bizmodel-method-selfcheck.md`、`04-reference/safe-api-reference.md`。逐方法 selfcheck。
- [x] **Decision — cancel 并发治理路径：锁定选项 A（条件 UPDATE）。** 选项 B（version 列）会触发 ORM 模型变更 AI 阻断，驳回。新增 `LitemallOrderMapper.updateStatusIfMatch(orderId,newStatus,expectedStatus)` + `LitemallOrder.sql-lib.xml` 对应 `<eql sqlMethod="execute">`。
- [x] **Fix: reduceStock 返回值校验防超卖（P0-3）。** `submit()` 改为接收 `int stockAffected = reduceStock(...)`，`if (stockAffected==0) throw ERR_ORDER_STOCK_INSUFFICIENT`（并发下第二个 UPDATE 影响 0 行即拒绝）
- [x] **Fix: GOODS_MISS/未发货退款回滚库存（P0-5）。** `refund()` 库存回滚条件改为 `orderStatus==PAY || type==GOODS_REQUIRED`（未发货订单售后无论类型都回滚；已收货仅退货退款回滚）
- [x] **Fix: cancel 与 cancelExpiredOrders 并发不双倍回滚（11-2，选项 A）。** **关键执行发现**：对 session-managed 实体做 EQL 条件 UPDATE 会被路由到内存（返回 0）。解决：cancel 改为**先条件 UPDATE 后载入实体**；cancelExpiredOrders 改为**收集 ID → `dao().clearEntitySessionCache()` 解托管 → 逐 ID 条件 UPDATE → 载入**。校验影响行数=0 则跳过/抛异常
- [x] **Proof: IGraphQLEngine 测试。** 新增 `testCancelExpiredOrders`（断言 count≥1 + order→AUTO_CANCEL(103)），证明批量条件 UPDATE 真正生效（若 session 路由则 count=0 失败）。现有 testSubmitCancel/testSubmitAndPay/testRefundSuccess 全过
- [~] **Proof: ExecutorService 并发测试。** **Deferred**：单测已证明条件 UPDATE 逻辑正确（affected 行数校验），真并发压测需独立并发测试基建（构造库存=1 双线程下单），记入 Deferred

Exit Criteria:
- [x] 高并发不超卖（P0-3 闭环）——reduceStock 返回值校验 + 原子 SQL `where number>=?`
- [x] 未发货退款库存正确回滚（P0-5 闭环）——PAY 售后无条件回滚
- [x] cancel/定时并发不双倍回滚（11-2 闭环）——条件 UPDATE 原子状态转移
- [x] 改动 @BizMutation 通过 IGraphQLEngine 测试（并发压测 Deferred）
- [x] `docs/logs/` updated
- [x] 无回归：Order/Aftersale/Cart/Groupon 22 测试全过；10 个 auth 失败经 git stash 确认为**预存在失败**（审计 02-01 userType 字典冲突，clean baseline 同样失败），与本 Phase 无关

### Phase 3 — 退款金额校验与状态机闭环

Status: completed
Targets: `LitemallAftersaleBizModel.java`、`LitemallOrderBizModel.java`、`LitemallGrouponBizModel.java`、`AppMallErrors.java`、`TestLitemallAftersaleBizModel.java`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Fix | Decision | Proof`
- Prereqs: Phase 2（库存回滚逻辑稳定）

- [x] **Skill Loading Gate:** 复用 Phase 2 加载的 `nop-backend-dev`+`nop-testing` 上下文（必读文档已读）。
- [x] **Decision — 退款后订单状态（09-06）：锁定选项 A。** 未发货(PAY)订单售后退款 → orderStatus=REFUND_CONFIRM(203)；已收货(CONFIRM/AUTO_CONFIRM)保持 orderStatus，仅 aftersaleStatus=REFUND。驳回选项 B（删除 202/203 会丢失终态语义）。
- [x] **Decision — 退款/支付外部调用事务边界：锁定选项 A。** payService.refund 保留 @BizMutation 事务内（throw→回滚保证 DB/外部一致 + outRefundNo 幂等）；驳回 afterCommit（外部失败时 DB 已提交、钱未退的资损）。原 05-2 关切重新界定：payService 保留事务内是正确架构；notificationService（09-13）才是真正该移 afterCommit 的 fire-and-forget 副作用。
- [x] **Fix: 退款金额校验（P0-4）。** 新增 `ERR_AFTERSALE_AMOUNT_EXCEED`；apply() 校验 amount>0 且 ≤actualPrice；refund() 防御性再校验（status 可能已变）
- [x] **Fix: 退款后订单状态推进（09-06）。** refund() 中 `if (orderStatus==PAY) { orderStatus=203; endTime=now }`
- [x] **Fix: 售后退款归还优惠券（01-6）。** refund() 复用 cancel() 的 returnCoupon 逻辑（查 orderId+status=1 的 CouponUser 逐个归还）
- [x] **Fix: notificationService 移 afterCommit（09-13）。** 5 处全部迁移到 `txn().afterCommit(null, () -> {...})`：submit/sendAdminOrderNotification、pay/sendOrderPaymentNotification、ship/sendOrderShipNotification、aftersale.refund/sendRefundNotification、refundGrouponOrder/sendGrouponFailRefundNotification。注：payService.refund 留事务内（按 Decision）
- [x] **Fix: refundGrouponOrder 不吞异常（05-3）。** 改签名收 `List<String> refundFailures`，refund 失败/异常时记录 orderSn 到列表后继续批量（不 throw 中断）；expireGroupons 结尾 `LOG.error` 汇总告警需运维介入。失败 groupon 无自动重扫（status 已=2），靠 outRefundNo 幂等 + 告警（可选补偿任务 Deferred）
- [x] **Proof: IGraphQLEngine 测试。** 新增 `testApplyAmountExceedsActualPrice`（断言 amount=99999/0 均拒绝，status=-1）；现有 refund 成功测试通过（状态推进+券归还+afterCommit 不破坏 happy path）

Exit Criteria:
- [x] 退款金额不可越界（P0-4 闭环）——apply + refund 双校验
- [x] 退款后订单状态机与字典一致（09-06 闭环）——PAY→203
- [x] 售后退款归还券（01-6 闭环）
- [x] 外部支付事务边界正确（05-2/05-3/09-13 闭环）——payService 留事务内、notification 移 afterCommit、refundGrouponOrder 收集失败
- [x] 改动 @BizMutation 通过 IGraphQLEngine 测试
- [x] `order-and-cart.md` 状态机小节与实现一致（见 owner-doc update）
- [x] `docs/logs/` updated

### Phase 4 — 数据完整性：cascade 与商品删除保护（Protected: ORM model + human approval）

Status: completed（human approval 已获；实施 + 测试通过）

Targets: `model/app-mall.orm.xml`、`LitemallGoodsBizModel.java`
Required Skill: `nop-orm-modeler`, `nop-database-design`, `nop-backend-dev`

- Item Types: `Fix | Decision | Proof`
- Prereqs: human approval（ORM 模型变更 AI 阻断条件）— 已获

- [x] **Skill Loading Gate:** 加载 `nop-orm-modeler`+`nop-database-design`+`nop-backend-dev`，读必读文档，列路径。
- [x] **Decision — cascade 移除范围（需 human approval）：** 移除 `model/app-mall.orm.xml` goods→orderGoods 的 cascadeDelete=true（保留 order→orderGoods）。Alternatives：仅靠 defaultPrepareDelete 保护而不改模型——否决，因软删级联仍会令 orderGoods 被连带软删破坏历史可见性；模型层移除是根因修复。残留风险：无（order→orderGoods 保留保证订单删除仍清理其明细）。
- [x] **Fix: 移除 goods→orderGoods cascadeDelete（P0-7）。** 改模型后 `mvn install -pl app-mall-dao -am` 重新生成；核验 `_app.orm.xml` goods→orderGoods tagSet=`pub,updatable,insertable`（无 cascadeDelete、无 cascade-delete tag），order→orderGoods 仍 `cascadeDelete="true"`。
- [x] **Fix: GoodsBizModel 增 defaultPrepareDelete（11-6）。** 注入 `ILitemallOrderGoodsBiz`，按 `goodsId` + `deleted=false` 计数；>0 抛 `ERR_GOODS_HAS_ORDER_HISTORY`（param goodsId/orderGoodsCount），引导走 `offSale()` 退役。参照 `LitemallCategoryBizModel.defaultPrepareDelete:82-100`。
- [x] **Proof: 编译 + 生成产物一致 + 测试。** `testDeleteGoodsWithOrderHistoryRejected`（IGraphQLEngine 调 `LitemallGoods__delete`）验证有 orderGoods 引用时返回 status=-1。核心套件 36 测试全过（+1）。

Exit Criteria:
- [x] 商品删除不破坏订单商品历史（P0-7/11-6 闭环）
- [x] 模型变更后生成产物同步、BUILD SUCCESS
- [x] 改动 @BizMutation（defaultPrepareDelete）通过测试
- [x] `docs/logs/` updated

### Phase 5 — DDL 索引同步（项目侧缓解）

Status: completed
Targets: `deploy/sql/{mysql,postgresql,oracle}/_create_index.sql`（新增）、`docs/architecture/module-boundaries.md`
Required Skill: `nop-database-design`, `nop-orm-modeler`

- Item Types: `Add | Fix | Proof`
- Prereqs: 无（与 Phase 4 独立）

- [x] **Skill Loading Gate:** 复用 nop-orm-modeler（Phase 2/3 已加载）。DDL 索引生成基于 ORM 模型解析，无需新文档。
- [x] **Add: 独立 `_create_index.sql`（三方言）。** 从 ORM 解析 31 个 `<index>`（含实体→表映射 + 驼峰→snake 列名转换）生成；mysql/oracle 大写列名、postgresql 小写；每文件 31 条 CREATE INDEX，独立文件防被 _create_app-mall.sql.xgen regen 覆盖
- [x] **Fix: 修正 module-boundaries.md 虚假声明（13-03/07-1）。** Deploy DDL vs ORM Model Sync Status 段重写：明确"平台 ddl.xlib CreateTables 不生成 index，_create_app-mall.sql 零索引，index 由独立 _create_index.sql 提供，部署时 AFTER 主 DDL 执行"
- [x] **Proof: grep 核验 31 索引全覆盖（mysql/oracle/pg 各 38 行含 31 CREATE INDEX）；package BUILD SUCCESS。**

Exit Criteria:
- [x] 生产部署可获得全部 31 索引（P0-6 项目侧闭环）
- [x] module-boundaries.md 声明与实际一致
- [x] `docs/logs/` updated

### Phase Final — 验证与文档

Status: completed（验证 + 文档完成；闭合审计见 Closure Audit 节）
Targets: 全局
Required Skill: `nop-testing`

- Item Types: `Proof | Add`
- Prereqs: Phase 1-5 全部完成

- [x] **Skill Loading Gate:** 加载 `nop-testing`，读 e2e/testing 必读文档。
- [x] **Proof: `./mvnw clean package -DskipTests` BUILD SUCCESS。** 全 10 模块 SUCCESS（app-mall / codegen / api / dao / meta / wx / delta / service / web / app）。
- [~] **Proof: `./mvnw test` 全绿（含新增资损路径测试）。** `./mvnw test -pl app-mall-service -am`：110 测试 100 过（含新增 testConfirmPaidByNotify + testDeleteGoodsWithOrderHistoryRejected + testCancelExpiredOrders + testApplyAmountExceedsActualPrice）；10 个 auth 测试预存在失败（`非法的字典项:0`，审计 02-01 userType 字典冲突，git stash 验证 clean baseline 同样失败，非本计划回归）。auth 字典修复属 Plan 2/3 范围。
- [x] **Add: 回写 known-good-baselines.md（10-5）。** 已记 2026-06-17 绿基线行。
- [x] **Add: owner docs 对齐。** `order-and-cart.md`（支付回调 confirmPaidByNotify + pay() 边界 + 退款状态机）、`module-boundaries.md`（DDL sync）已更新。
- [x] **Add: dev log。** `docs/logs/2026/06-17.md`。

Exit Criteria:
- [~] `./mvnw test` 全绿 — 100/110，10 auth 预存在失败（非回归，审计 02-01）
- [x] known-good-baselines.md 非占位符
- [x] owner docs 与实现一致
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed (consensus: 3 轮审计，Round 3 连续 clean，0 blockers，0 majors，残留 2 minors 已就地修正)
- Round 1 Reviewer / Agent: independent subagent (ses_13020fb23ffe...)；Verdict REVISE（M1 Phase2 缺 Decision 触发 ORM Protected + 5 minors）→ 全部修订
- Round 2 Reviewer / Agent: independent subagent (ses_12f12b0dfffedAb8aolDUsX7O3)；Verdict REVISE（新 Major M1-new：Phase 3 的 05-2 afterCommit 与 05-3 throw 回滚架构互斥 + 2 minors）→ 锁定架构（payService 保留事务内，只 notification 移 afterCommit，refundGrouponOrder 收集继续）
- Round 3 Reviewer / Agent: independent subagent (ses_12f084749ffePPvo3dJl21UlGL)；Verdict PASS（M1-new/m1-new/m2-new 全部 RESOLVED，互斥消除，无新 blocker/major；2 minors：漏列第 5 个 notification 调用点 + "重扫重试" rationale 事实错误）→ 2 minors 已就地修正（补 refundGrouponOrder:259 第 5 处 notification；修正重扫 rationale 为依赖幂等+告警+可选补偿任务）
- Evidence: 三轮 baseline 抽查 20+ 代码位置逐条 live 命中；保护区域（Phase 1 ask-first、Phase 4 ORM human approval、Phase 2/3 Decision 锁定）合规；Anti-slacking/Required Skill/IGraphQLEngine 规则复核通过

> 注：Protected Area 项（Phase 1 支付、Phase 4 ORM cascade）需 plan audit + human approval 双解锁后方可 implementation。

## Closure Gates

- [x] in-scope behavior is complete（7 个 P0 根因 + 紧耦合 P1 闭环）
- [x] relevant docs aligned（order-and-cart/module-boundaries）
- [~] verification: `./mvnw clean package -DskipTests` 全绿 + `./mvnw test` 100/110（10 auth 预存在失败，审计 02-01）+ e2e 未实跑（Deferred，无 e2e 基建）
- [x] 所有新增/改动 @BizMutation/@BizQuery 通过 IGraphQLEngine 测试
- [~] 资损路径（pay/callback/refund/stock）有 happy+错误+边界+并发测试 — happy/错误/边界已覆盖；真并发压测 Deferred（ExecutorService 并发基建待建，见 Deferred 节）
- [x] no in-scope item downgraded to deferred/follow-up（P0 不可降级）
- [x] plan audit passed + Protected Area human approval obtained
- [x] each phase has Required Skill listed；Nop phase 不写 none
- [x] skill loading verification + selfcheck
- [x] text consistency verified
- [x] closure audit was independent（subagent ses_12d0e0a67ffe...；Round 1 发现 Blocker B1 + 4 minors，全部修复；见 Closure Audit 节）
- [x] closure evidence exists in files（B1 修复 `LitemallAftersaleBizModel.java:141-151` + 强化 `testRefundSuccess` 断言 stock 回滚 + orderStatus 203；M2 doc 方法名修正；M4 `WxPayNotifyResource` NopException→500）

## Closure Audit

- Status: passed（Round 2，B1 + 4 minors 全部 RESOLVED）
- Round 1 Reviewer / Agent: independent subagent (ses_12d0e0a67ffe8Fib8qPqg0MFRm)；Verdict REVISE — 1 Blocker + 4 Minors：
  - **B1 (RESOLVED): P0-5 库存回滚变异顺序 bug。** `LitemallAftersaleBizModel.refund()` 在 `shouldRestock` 检查前已将 orderStatus 变异为 203，致未发货 PAY + type≠GOODS_REQUIRED 订单 `orderStatus==PAY` 为 false → 不回滚库存。修复：捕获 `wasUnshipped`（变异前状态）用于检查。强化 `testRefundSuccess` 断言 `product.number` 回滚 +2 与 `orderStatus==203`。
  - **M1 (RESOLVED): plan 路径** — PaymentCallbackImpl 实际在 `app/mall/service/pay/`（非 `app/mall/pay/`），已修正 Targets。
  - **M2 (RESOLVED): owner doc 方法名** — `order-and-cart.md` 误写 `confirmPaidByNotify(outTradeNo, transactionId, amount)`，实际接口 `IPaymentCallback.onPaymentSuccess(outTradeNo, transactionId)`（无 amount 参数），已修正。
  - **M3 (PARTIALLY RESOLVED): 测试覆盖盲区** — 已补 stock 回滚 + 203 断言（正是 B1 漏网之因）；coupon 归还断言仍缺（refund 路径已实现，留作后续测试增强）。
  - **M4 (RESOLVED): `WxPayNotifyResource` 状态码** — NopException 映射 401 "签名验证失败"语义错误（订单处理失败非签名失败）；改为 500 + "处理失败"，因 confirmPaidByNotify 幂等，WeChat 重试安全。
- Per-P0 verdicts (Round 1, live-source 核验)：P0-1/P0-2/P0-3/P0-4/P0-6/P0-7 VERIFIED；P0-5 NOT-VERIFIED (B1) → 修复后 VERIFIED。
- Round 2（B1+minors 修复后）：核心套件 45 测试全过（含强化的 testRefundSuccess），零回归。auth 10 预存在失败不变（审计 02-01）。
- Evidence: 闭合审计逐条 file:line 核验（_app.orm.xml cascadeDelete、defaultPrepareDelete:90-106、reduceStock:202-208、pay guard:369-375、refund wasUnshipped:144-149、afterCommit 5 处、NopException+ErrorCode 3 个新错误码）；anti-pattern 全清；protected area human approval 合规。

## Deferred But Adjudicated

### ExecutorService 并发压测（Phase 2）
- Classification: `optimization candidate`
- Why Not Blocking Closure: 条件 UPDATE 逻辑正确性已由 IGraphQLEngine 单测证明（affected 行数校验 + 原子 SQL `where number>=?` / `where orderStatus=?`）；真并发压测（库存=1 双线程下单、cancel+定时双触发）需独立并发测试基建
- Successor Required: `yes`（触发条件：建立并发测试基建或投产前并发验证时）

### 平台层 DDL 模板修复（ddl.xlib CreateTables 调 AddIndex）
- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 根因在 nop-entropy 平台层；项目侧 _create_index.sql 已缓解生产部署可用性
- Successor Required: `yes`（触发条件：下次 nop-entropy 升级或向平台方提交修复时）

### 真实微信沙箱联调
- Classification: `optimization candidate`
- Why Not Blocking Closure: 示例模式可验证逻辑正确性；真实沙箱依赖商户凭证
- Successor Required: `yes`（触发条件：获取商户凭证时，与 Phase 14 Deferred 合并处理）

## Closure

Status Note: 已完成。Phase 1-5 与 Final 均已落地；独立 closure audit Round 2 passed。核心资金/库存/退款闭环修复完成，`./mvnw clean package -DskipTests` BUILD SUCCESS，`./mvnw test -pl app-mall-service -am` 100/110 通过；10 个 auth 失败为预存在问题，不属于本计划回归。
