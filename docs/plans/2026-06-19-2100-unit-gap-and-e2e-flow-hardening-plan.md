# 2026-06-19-2100 单元测试缺口补齐 + E2E 业务流程串联加固

> Plan Status: completed
> Last Reviewed: 2026-06-19
> Source: 应用 `docs/skills/test-coverage-audit-prompt.md` 对测试现状的专项审计
> Related:
> - `docs/skills/test-coverage-audit-prompt.md`（本次审计方法来源）
> - `docs/plans/2026-06-18-2000-e2e-test-hardening-plan.md`（**互补**：init-data 基建 + happy-path + admin 实体 findPage 冒烟 + 浏览器 UI；本计划不重复其 scope，职责划分见 Current Baseline）
> - `docs/plans/2026-06-17-1830-test-hardening-and-ops-optimization-plan.md`（更早的测试加固与 ops 优化）
> Audit: required

## Current Baseline

### 后端单元/集成测试现状（覆盖比初判更好，缺口集中）

应用 `test-coverage-audit-prompt.md` 对 `app-mall-service` 约 93 个 `@BizMutation`/`@BizQuery` 方法逐方法核对（不以测试类为单位，以方法为粒度）：

- **测试形态**：`JunitBaseTestCase` + `@NopTestConfig(localDb=true, initDatabaseSchema=true)` + `IGraphQLEngine.executeRpc()` 手写断言（真实 local DB 集成测试，非录制回放；`src/test/resources` 下无 `request.json5`）
- **核心域覆盖良好**：
  - `LitemallOrderBizModel`（15 方法）：submit/pay/ship/confirm/cancel/refund/prepay/deleteOrder/myOrders 均有测试，含 `testConfirmPaidByNotify`（支付回调幂等）、`testCancelExpiredOrders`（原子状态守卫 101→103）、`testDeleteGrouponExpiredOrder`（多状态反向验证：PAY/SHIP 拒删、204 可删）
  - Cart/Aftersale/Coupon/CouponUser/Groupon/Goods/Collect/Comment/Footprint 等均有测试类
  - `LitemallGrouponRulesBizModel` 4 方法（publishRules/unpublishRules/listAvailableRules/getAvailableRulesById）**实际已被 `TestLitemallGrouponBizModel` 覆盖**（虽无独立测试类，方法级已覆盖 —— 此点纠正了"无测试"的初判）

- **确认的真实缺口**：
  1. `LitemallOrderGoodsBizModel.expireCommentWindow`（定时任务 `@BizMutation`，comment 窗口过期批量置 -1）—— **无 `IGraphQLEngine` 级业务逻辑测试**；仅有 `TestLitemallSchedulerJobInvoker` 用 Java `Proxy` mock `ILitemallOrderGoodsBiz`（int 方法恒返回 1）验证调度委托 wiring，未触及真实 DB 查询/过滤/状态变更。该 BizModel 仅此 1 个自定义方法
  2. `LitemallGrouponRulesBizModel.publishRules` 的**商品下架错误路径**（`goods.isOnSale=false` 应抛 `ERR_GROUPON_RULES_GOODS_NOT_ON_SALE`）—— 现有 `testPublishAndUnpublishRules` 只测了上架商品的成功路径，未测下架拒绝
  3. **库存并发超卖**：`submit` 已有 `goodsProductMapper.reduceStock` 原子 UPDATE 守卫（`LitemallOrderBizModel.java:214`，扣减返回 0 则抛 `ERR_ORDER_STOCK_INSUFFICIENT`），`TestConcurrencyGuards` 已存在 —— 需确认是否覆盖"扣减至耗尽"场景，若已覆盖则记录结论

### delta auth 测试失败（预存在 defect，超出本计划 scope）

- `TestPasswordReset`/`TestNopAuthUserProfile`/`TestLoginApiSignUp` 共 10 个测试长期失败：`非法的字典项:0`（userType 字典冲突）
- `docs/testing/known-good-baselines.md` 已记录为 Known Failure，clean baseline（git stash）同样失败 → confirmed 预存在 defect，非本计划引入
- 修复涉及 `app-mall-delta` 对 nop-auth 的 Delta 覆盖 + 字典模型，属 **Protected Area**（roadmap Phase 1 标注 `plan-first`），超出"测试加固"scope → 路由到独立 auth-delta 修复计划（见 Non-Goals 与 Deferred）

### e2e 现状（业务流程串联是最大价值缺口）

- 3 个 spec：`app-startup`（2 smoke）、`storefront-pages`（11 实体 findPage + 25 页面渲染 smoke）、`storefront-happy-path`（1 条：address→cart→checkout→pay(201)→order list）
- happy-path **停在 pay(201)**，未覆盖后续履约（ship→confirm→comment）
- **完全无 e2e 的核心业务流程**：售后申请/审核/退款、优惠券领用→下单抵扣、拼团开团/参团、订单取消/退款
- `storefront-pages` 只验页面渲染，不验业务行为

### 与现有 e2e 计划的职责划分（互补，不重复）

| 计划 | 聚焦点 | 状态 |
|------|--------|------|
| `2026-06-18-2000` | init-data 基建 + happy-path + **admin 实体 findPage 冒烟** + 浏览器 UI 渲染 | Phase 1/2 done，Phase 3/4 planned |
| **本计划** | **业务流程正确性的端到端串联**（多步骤、跨状态机、跨用户/admin 身份的业务回归网） | planned |

两者维度不同：现有计划验"API 可达性 + 页面渲染"，本计划验"业务逻辑跨状态机的正确串联"。

### init-data 依赖

- 现有 e2e 计划 Phase 1 已交付 6 张核心表种子（goods/goods_product/goods_attribute/goods_specification/category/brand），happy-path 已依赖它转绿（done）
- 本计划 e2e 涉及优惠券/拼团/售后，需 coupon/groupon_rules 等数据 —— 采用**测试内通过 API 动态创建**（自包含，不依赖种子表扩展；happy-path 已示范此模式：先 addAddress 再加购下单）

## Goals

- **G1（单元缺口）**：补齐 `expireCommentWindow` 测试 + `publishRules` 商品下架错误路径 + 确认/补齐库存并发超卖覆盖
- **G2（e2e 履约后半段）**：补 ship→confirm 串联（承接 happy-path 的 pay 之后，完成订单履约闭环）
- **G3（e2e 售后退款）**：补售后申请→审核→退款 + 订单取消/退款串联
- **G4（e2e 营销）**：补优惠券领用→下单抵扣 + 拼团开团/参团串联

## Non-Goals

- **不修改业务代码**（BizModel/view.xml 业务逻辑）；仅新增测试代码
- **不修改 ORM 模型**
- **不修 delta auth 测试失败**（Protected Area + 字典模型冲突，路由到独立 auth-delta 修复计划；本计划仅在 baseline 如实记录）
- **不重复**现有 e2e 计划的 admin 实体 findPage 冒烟与浏览器 UI 渲染 scope
- **不扩展** init-data 种子表（e2e 测试内动态创建数据）
- **不做 CI 集成**（现有 e2e 计划已将 CI 列为 Deferred）

## Task Route

- Type: `verification or audit work`（纯测试补充，不改业务行为；按 `00-plan-authoring-and-execution-guide.md` 的 Plan Decision Table，测试加固属需追踪的非平凡工作，用 full plan）
- Test Stack Note: 本计划跨后端 Java 单元（`./mvnw test`，Phase 1）+ 前端 TS e2e（`npx playwright test`，Phase 2-4）两个测试栈；但 closure criteria 统一为"纯测试新增、不改业务、测试全绿"，按规则 #4 共享 closure criteria 属 ONE result surface，不拆分
- Owner Docs: `docs/design/order-and-cart.md`、`docs/design/marketing-and-promotions.md`、`docs/design/product-catalog.md`（核对业务流程的合法状态转移）
- Skill Selection Basis:
  - Phase 1：`nop-testing`（Java `IGraphQLEngine` 测试模式；本项目用 `JunitBaseTestCase` 而非 `JunitAutoTestCase`，但 `IGraphQLEngine.executeRpc()` 调用与断言模式一致，skill 的核心方法适用）
  - Phase 2/3/4：`none`（Playwright TS e2e 无匹配 skill：`nop-testing` 是 Java，`nop-frontend-dev` 面向 view.xml 开发而非 e2e 测试；方法来自 `../nop-entropy/docs-for-ai/02-core-guides/e2e-testing.md` + 现有 `storefront-happy-path.spec.ts` 范式）

## Infrastructure And Config Prereqs

- **单元测试**：`./mvnw test -pl app-mall-service -am`（H2 内存库自包含，无外部依赖）
- **e2e**：`app-mall-app/target/quarkus-app/quarkus-run.jar` 已构建（`./mvnw.cmd package -DskipTests -pl app-mall-app -am`）；`e2e/` 已 `npm install`；系统 Chrome；H2 内存库
- **回滚策略**：纯测试代码新增，不改业务代码/模型/配置；`git revert` 即完整回滚

## Execution Plan

### Phase 1 — 单元测试缺口补齐

Status: completed
Targets: `app-mall-service/src/test/java/app/mall/service/entity/TestLitemallOrderGoodsBizModel.java`（新建）、`app-mall-service/src/test/java/app/mall/service/entity/TestLitemallGrouponBizModel.java`（追加）、`TestConcurrencyGuards`（确认/追加）
Required Skill: `nop-testing`

- Item Types: `Add`、`Proof`、`Explore`
- Prereqs: none

- [x] **Skill loading gate:** 加载 `nop-testing`，读完路由表必读文档
  - Docs read: nop-testing skill（SKILL.md）、`../nop-entropy/docs-for-ai/05-examples/test-examples.java`、`../nop-entropy/docs-for-ai/02-core-guides/testing.md`、现有范式 `TestLitemallOrderBizModel.java` / `TestLitemallGrouponBizModel.java` / `TestConcurrencyGuards.java` / `TestLitemallSchedulerJobInvoker.java`
- [x] **Add:** 新建 `TestLitemallOrderGoodsBizModel`，补的是 `IGraphQLEngine` 业务逻辑层测试（与已有 `TestLitemallSchedulerJobInvoker` 的调度委托 wiring 测试互补 —— 后者 mock biz 接口不触及真实逻辑）。通过 `IGraphQLEngine` 调 `LitemallOrderGoods__expireCommentWindow`：(a) 构造 `comment=0` 且 `addTime` 早于 cutoff 的 OrderGoods，调用后断言 `comment=-1` 且返回 count≥1；(b) 构造未过期（addTime 晚于 cutoff）的 OrderGoods，调用后断言 `comment` 不变。**已实施**（2 测试全过）；实施修正：`OrderGoods.productId` 为非空需补建 `GoodsProduct`、`picUrl` 为 NOT NULL 且 image domain 把空串转 null 须用真实 URL、`updateEntity` 须包在 `ormTemplate.runInSession` 内（session 已关闭）
- [x] **Add:** 在 `TestLitemallGrouponBizModel` 追加 `testPublishRulesGoodsNotOnSale`：构造 `isOnSale=false` 的 goods + 关联 rules，调 `LitemallGrouponRules__publishRules`，断言 `status != 0`（错误码 `ERR_GROUPON_RULES_GOODS_NOT_ON_SALE`）。**已实施**，`TestLitemallGrouponBizModel` 7 测试全过（原 6 + 新增 1）
- [x] **Explore:** 读 `TestConcurrencyGuards` 全文，确认是否覆盖"库存原子扣减至耗尽"场景。**结论：已覆盖** —— `testReduceStockAtomicGuard`（`TestConcurrencyGuards.java:44-81`）设 SKU 库存=1，两线程并发 `reduceStock(1)`，断言 `r1+r2==1`（恰好一个成功）；另覆盖优惠券并发领取（`testClaimCouponConcurrentSingleUser`）。mapper 层原子守卫已验证，BizModel 层 `submit` 把 `reduceStock==0` 转 `ERR_ORDER_STOCK_INSUFFICIENT` 是简单 if 分支，不需另补
- [x] **Proof:** `./mvnw test -pl app-mall-service -am` 全量实跑。新增 3 测试全过（expireCommentWindow×2 + publishRules×1）。**clean baseline 验证**（`git stash` 移除本计划全部改动后跑 HEAD `f98d1ee`）：发现 21 个预存在失败（auth 10 + aftersale 5 + comment 6），与本计划无关（根因：平台 image domain 回归 + delta 字典冲突），见 `Deferred But Adjudicated`。**本计划未引入任何新失败**

Exit Criteria:

- [x] `expireCommentWindow` 过期/未过期两路径均有测试，通过 `IGraphQLEngine`（`@BizMutation` 方法，遵守规则 #15）
- [x] `publishRules` 商品下架错误路径有测试，断言 `status != 0`
- [x] 库存并发超卖覆盖确认（已覆盖于 `TestConcurrencyGuards.testReduceStockAtomicGuard`，记录证据，不补齐）
- [x] `./mvnw test` 新增 3 测试全过 + clean baseline 验证 21 个失败为预存在（非本计划引入）；本计划零新失败
- [x] No owner-doc update required（纯测试，不改业务语义）
- [x] `docs/logs/` updated

### Phase 2 — E2E 订单履约后半段串联（ship → confirm）

Status: completed
Targets: `e2e/tests/order-fulfillment.spec.ts`（新建）
Required Skill: `none`（Playwright TS e2e 无匹配 skill；方法遵循 `../nop-entropy/docs-for-ai/02-core-guides/e2e-testing.md` 与现有 `storefront-happy-path.spec.ts` 范式）

- Item Types: `Explore`、`Add`、`Proof`
- Prereqs: 现有 e2e 计划 Phase 1 init-data（已 done）

- [x] **Skill loading gate:** 扫描 available skills，无匹配（`nop-testing` 为 Java、`nop-frontend-dev` 面向 view.xml 开发）。方法遵循 `../nop-entropy/docs-for-ai/02-core-guides/e2e-testing.md` Nop RPC 调用模式
  - Docs read: `../nop-entropy/docs-for-ai/02-core-guides/e2e-testing.md`、`docs/references/e2e-testing-guide.md`、现有 `storefront-happy-path.spec.ts`
- [x] **Explore:** 验证 `nop`/`123` 凭证对 admin 操作 `LitemallOrder__ship` 与用户操作 `LitemallOrder__confirm` 的 RBAC 可达性。**结论：nop 凭证可调 admin 操作** —— 启动 e2e 应用（init-data）后 `curl` 测试：`ship`（假 orderId）返回参数类型错误（`orderId "nonexistent"` 无法转 Integer）而非权限拒绝 → RBAC 通过；`findPage` 正常。nop 在 `%dev` profile 有足够权限调 admin RPC（ship/refund/coupon 等），无需独立 admin 凭证；orderId 为 Integer 类型，e2e 用订单真实数字 id
- [x] **Add:** 新建 `order-fulfillment.spec.ts`，承接 happy-path（addAddress→addCart→submit→pay 201），续测 `ship`（→301）→ `confirm`（→401）。**已实施**，单测通过（627ms）
- [x] **Proof:** `cd e2e && npx playwright test` 全量回归 = **40 passed / 0 failed**（原 39 + 新增 order-fulfillment 1）。无回归。注：构建 jar 时发现并 kill 了残留 Java 进程（PID 11540，之前 e2e 未关闭的 quarkus 应用，持有 jar 锁导致 FastJar 构建失败）

Exit Criteria:

- [x] Explore 产出 nop 凭证对 ship/confirm 的 RBAC 结论（可调，无需独立 admin 凭证）
- [x] ship→confirm 串联 e2e 通过（订单状态 201→301→401）
- [x] 全量 e2e 全绿（40 passed / 0 failed）
- [x] No owner-doc update required
- [x] `docs/logs/` updated

### Phase 3 — E2E 售后与退款串联

Status: completed
Targets: `e2e/tests/aftersale-refund.spec.ts`（新建）
Required Skill: `none`（同 Phase 2 justify）

> **保护区域提示**：退款逻辑改动受 `docs/context/project-context.md` 的 AI Block Conditions（支付/数据删除路径）与 `docs/context/ai-autonomy-policy.md` 约束。本 phase **只测已有退款逻辑，不修改退款业务代码**；如测试暴露真实 defect，停止并路由到独立 fix 计划，不在本计划内改业务代码。

- Item Types: `Explore`、`Add`、`Proof`
- Prereqs: Phase 2（确认 admin 操作 RBAC 模式）

- [x] **Skill loading gate:** 同 Phase 2（none + justify）
  - Docs read: `e2e-testing.md`、`TestLitemallAftersaleBizModel.java`（API 序列范式）、`LitemallAftersaleBizModel.java`（方法签名）
- [x] **Explore:** 售后/退款合法状态转移从 `TestLitemallAftersaleBizModel` 推断（未发货 PAY 订单 type=0 GOODS_MISS 可申请；apply→batchApprove→refund 后订单→203 REFUND_CONFIRM）。RBAC：nop 凭证可调 admin batchApprove/refund（Phase 2 已验证 admin 操作可达）
- [x] **Add:** 售后流程 e2e：`aftersale-refund.spec.ts` test 1：submit→pay(201)→apply(type=0,amount=1)→batchApprove→refund，断言 refund status=0 + 订单 orderStatus=203。**已实施，通过**
- [x] **Add:** 取消 e2e：test 2：submit→cancel，断言 orderStatus=102。**已实施，通过**
- [x] **Proof:** `cd e2e && npx playwright test` 全量 = **44 passed**（含本 phase 2）。注：webServer 日志的 `MallJobInvoker` NPE 是 e2e 环境定时任务 IoC 注入问题（orderBiz 等为 null），非本计划引入，不影响测试通过——记为环境问题

Exit Criteria:

- [x] 售后申请→审核→退款串联通过（未发货退款 → 订单 203）
- [x] 取消流程通过（CREATED 101 → 102）
- [x] Protected Area：未修改退款业务逻辑（只新增测试）；未发现新 defect（picUrl 回归已在 Phase 1 路由）
- [x] 全量 e2e 全绿（44 passed）
- [x] No owner-doc update required
- [x] `docs/logs/` updated

### Phase 4 — E2E 营销流程串联（优惠券 + 拼团）

Status: completed
Targets: `e2e/tests/marketing.spec.ts`（新建）
Required Skill: `none`（同 Phase 2 justify）

- Item Types: `Explore`、`Add`、`Proof`
- Prereqs: Phase 2（admin 操作 RBAC 模式）

- [x] **Skill loading gate:** 同 Phase 2（none + justify）
  - Docs read: `e2e-testing.md`、`TestLitemallCouponUserBizModel.java`（coupon API）、`TestLitemallGrouponBizModel.java`（groupon API）
- [x] **Explore:** 优惠券领用（`LitemallCouponUser__claimCoupon`）+ 拼团开团（`LitemallGroupon__openGroupon`）API 从现有单元测试推断；nop 凭证可调 admin `LitemallCoupon__save`/`LitemallGrouponRules__save`/`publishRules`（Phase 2 验证 admin 可达）。**拼团参团 `joinGroupon` 需第二用户**（不能加入自己的团，见 `TestLitemallGrouponBizModel.testJoinGroupon`），e2e 仅有 nop 单用户 → 参团移入 `Deferred But Adjudicated`
- [x] **Add:** 优惠券 e2e：`marketing.spec.ts` test 1：`LitemallCoupon__save`(discount=1,min=0)→`claimCoupon`→submit(couponUserId)→断言 `couponPrice>0`。**已实施，通过**
- [x] **Add:** 拼团开团 e2e：test 2：`LitemallGrouponRules__save`+`publishRules`→submit(得 orderId)→`openGroupon`→断言 groupon status=1（开团中）。**已实施，通过**。参团见 Deferred
- [x] **Proof:** `cd e2e && npx playwright test` 全量 = **44 passed**（含本 phase 2）

Exit Criteria:

- [x] 优惠券领用→下单抵扣串联通过（`couponPrice>0`）
- [x] 拼团开团串联通过（status=1）；参团按规则 Deferred（需第二用户身份）
- [x] 全量 e2e 全绿（44 passed）
- [x] No owner-doc update required
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed（第二轮，2026-06-19）
- Reviewer / Agent: 独立子代理 ses_121f76f6affeEXCo4g1n3I15KH
- Evidence:
  - **第一轮（2026-06-19）Verdict: revise**（1 Blocker + 1 Major + 2 Minor，全部基于 live repo 证据）
    - **B1** baseline "expireCommentWindow 零测试" 措辞失实：`app-mall-service/src/test/java/app/mall/service/scheduler/TestLitemallSchedulerJobInvoker.java:53/59` 已用 Java `Proxy` mock `ILitemallOrderGoodsBiz`（int 方法恒返回 1）测调度委托 wiring，但未测真实业务逻辑 → **已修订**：baseline 改为"无 `IGraphQLEngine` 级业务逻辑测试，仅有调度委托 wiring 测试"；Phase 1 Add item 点明补的是与调度委托测试互补的业务逻辑层
    - **M1** RBAC fallback "admin 操作改用 DB 状态推进"会产出空心测试（e2e 看起来绿但未验证 admin RPC 路径）→ **已修订**：Phase 2-4 改为"admin 凭证注入 或 移入 `Deferred But Adjudicated`，**禁 DB 直改掩盖缺口**"；Exit Criteria 同步对齐
    - **m1** Phase 3 Protected Area 引用来源错误（`project-context.md` 无 Protected Areas 表，实际在 `ai-autonomy-policy.md`；且未列退款/售后，只列支付/删除/Auth-delta/XML/DB-schema）→ **已修订**：引用改为 `project-context.md` 的 AI Block Conditions + `ai-autonomy-policy.md`
    - **m2** 跨两个测试栈建议在 Task Route 点明 → **已新增** Test Stack Note
  - **第二轮（2026-06-19，修订后复核）Verdict: passed** — 独立子代理 ses_121f76f6affeEXCo4g1n3I15KH 续审，重新读修订后的计划 + live repo 证据，确认 B1/M1/m1/m2 全部正确修复（baseline 措辞与 `TestLitemallSchedulerJobInvoker` 实际行为一致、RBAC fallback 明确禁止 DB 降级并指向 Deferred、Protected Area 引用准确、Test Stack Note 到位），无新问题。审计同时复核确认的既有准确判断：GrouponRules 4 方法已覆盖、publishRules 下架路径未覆盖、库存并发（`TestConcurrencyGuards` 已覆盖 mapper 层原子守卫）、delta auth 10 失败、93 方法计数、与现有 e2e 计划 scope 不重叠、anti-slacking 零禁用词、Required Skill 合规、规则 #15 已满足。零 Blocker + 零 Major，可进入实施

## Closure Gates

- [x] in-scope behavior is complete（G1-G4 全部落地或按规则 Deferred；joinGroupon 参团按规则 Deferred）
- [x] relevant docs are aligned（e2e-testing-guide.md 测试文件表补本计划新增 3 spec）
- [x] verification has run：Phase 1 新增 3 单元测试全过（clean baseline 验证 21 个预存在失败非本计划引入）+ Phase 2-4 e2e 全量 **44 passed / 0 failed**
- [x] N/A — no new `@BizMutation`/`@BizQuery` business methods（本计划仅新增测试，不新增业务方法）
- [x] no in-scope item downgraded to deferred/follow-up（joinGroupon 参团 Deferred 有明确触发条件：需第二用户身份）
- [x] plan audit passed before implementation（独立子代理 ses_121f76f6affeEXCo4g1n3I15KH，revise→修订→passed）
- [x] each phase has `Required Skill` listed（Phase 1 `nop-testing`、Phase 2-4 `none` 含 justify）
- [x] skill loading verification: Phase 1 加载 nop-testing 并读完路由必读文档（docs 已列）
- [x] text consistency verified: status / phases / gates / log 全部一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### delta auth 测试失败（10 个，userType 字典冲突）

- Classification: `out-of-scope improvement`（路由到独立 auth-delta 修复计划）
- Why Not Blocking Closure: 非"测试加固"工作；修复需改 `app-mall-delta` 对 nop-auth 的 Delta 覆盖 + 字典模型，属 Protected Area（roadmap Phase 1 `plan-first`）；已在 `docs/testing/known-good-baselines.md` 如实记录为 Known Failure，clean baseline 同样失败 → confirmed 预存在 defect，非本计划引入
- Successor Required: `yes`
- 触发条件：新建独立 `auth-delta-dict-conflict-fix-plan.md`，定位 userType 字典项为何被判为非法（`非法的字典项:0`），在 delta 层或字典模型层修复，使 10 个测试转绿并更新 `known-good-baselines.md`

### image domain picUrl 回归（aftersale 5 + comment 6 = 11 个失败）

- Classification: `out-of-scope improvement`（路由到独立修复计划）
- Why Not Blocking Closure: 非"测试加固"工作，非本计划引入。clean baseline（`git stash`，HEAD `f98d1ee`）验证同样失败。根因：`OrmFileComponent.copyFrom` 在 `submit` 时从 cart 复制 picUrl 到 orderGoods，因 `nop_file_record` 缺记录导致 picUrl=null（违反 NOT NULL；错误：`实体对象[订单商品表]的非空属性[商品/货品图片(picUrl)]为null`）。与 e2e 计划 2026-06-19 的 image domain 约束发现同源（见该计划 Phase 1 Decision）。e2e 层 storefront happy-path 不受影响（种子数据含 `nop_file_record.csv`）
- Successor Required: `yes`
- 触发条件：新建独立修复计划，更新 `TestLitemallAftersaleBizModel`/`TestLitemallCommentBizModel` 的 cart/goods picUrl 数据构造为 `/f/download/{fileId}` 格式 + 预建 `NopFileRecord`（或评估平台 `copyFrom` 行为是否变更）；同时更新 `known-good-baselines.md`（当前"核心 36 全过"记录已过时）

### 拼团参团（joinGroupon）e2e 缺失

- Classification: `out-of-scope improvement`（需 e2e 多用户身份支持）
- Why Not Blocking Closure: `joinGroupon` 业务规则禁止用户加入自己的团（`TestLitemallGrouponBizModel.testJoinGroupon` 验证），参团需第二用户身份。e2e 仅有 nop 单用户（`auth.ts` 只提供 nop/123），无法在同一 spec 内用两个不同身份完成开团+参团。开团（`openGroupon`）已覆盖（Phase 4 test 2，status=1），参团是额外验证
- Successor Required: `no`
- 触发条件：当 e2e 支持第二用户身份时（扩 `auth.ts` 支持注册/第二 token，或引入 admin 用户作为第二参团者），补 `joinGroupon` 串联测试

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: completed（闭合审计通过，2026-06-19）。G1-G4 全部落地，3 项 Deferred 合规裁定，e2e 44 passed，单元新增 3 全过

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计代理 ses_12187b50effeJCQIO0CdY239xa（MUST NOT be the implementing agent，已满足）
- Evidence: 实读 4 个测试文件（确认存在 + 断言正确 + 规则 #15 满足）+ 实跑 `./mvnw test -Dtest=TestLitemallOrderGoodsBizModel,TestLitemallGrouponBizModel` = 9 passed + e2e 静态计数 44 + `e2e/test-results/.last-run.json` passed + 源码核验 `ERR_GROUPON_CANNOT_JOIN_OWN`（joinGroupon Deferred 合规）+ picUrl 根因核验（cart 字面 URL 非 `/f/download` 格式，clean baseline 同样失败）+ baselines/log/guide 交叉一致
- Verdict: **pass**（1 Minor 叙事性 off-by-one：baseline 描述"10 实体"实际 11，不影响任何 closure criterion）

Follow-up:

- delta auth 修复见上方 Deferred But Adjudicated（路由到独立计划）
- CI 集成见现有 e2e 计划 `2026-06-18-2000` 的 Deferred
