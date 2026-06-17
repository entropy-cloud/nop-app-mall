# 2026-06-16-1800 电商域逻辑加固计划（并发/越权/优惠/状态机/前端呈现）

> Plan Status: planned
> Last Reviewed: 2026-06-16
> Source: `docs/audits/2026-06-16-1744-multi-dim-audit-full-project/summary.md`（P1 域逻辑类发现，items 8-11 + 16 + 前端 P1）
> Related: 维度 02/08/09/11 审计文件；`docs/plans/2026-06-16-1800-p0-fundamental-integrity-fix-plan.md`（前置，资金/库存/状态机闭环）
> Audit: required
> **Protected Area: 本计划含多处 ORM 模型变更（model/app-mall.orm.xml）——Phase 3 改 :1000 ACTUAL_PRICE comment；Phase 4 团购 STATUS ext:dict + 新增字典文件（Decision A）；Phase 1 可能的 version 列/唯一约束（Decision A/B 兜底）。按 project-context.md:55 AI 阻断条件，所有 ORM 变更需 plan audit + human approval 双解锁后方可 implementation。**

## Why One Plan

审计的 P1 域逻辑发现横跨"并发治理 / 越权收口 / 优惠与价格校验 / 状态机对齐 / 前端呈现"五类，但共享同一结果表面——"电商域逻辑正确性"：它们都是"用户操作 + 业务规则 → 正确的状态/价格/库存结果"的保证，前端呈现是这些规则的展示层。全仓无 version 乐观锁是并发类的共同根因；越权与优惠校验都是 submit/order 操作的入参校验缺口；前端金额公式/状态映射/按钮缺失是后端规则的前端对齐。分开会导致 submit/订单方法被多次改。支付/退款/库存的 P0 资损路径归 Plan 1（前置依赖），本计划在其状态机稳定后处理非资损域逻辑。

## Current Baseline

> 经审计核验（见维度 02/08/09/11）。

**并发治理（全仓无 version 乐观锁列）**
- 优惠券领取：`claimCoupon`（`LitemallCouponUserBizModel.java:50-93`）先查 findCount 后插 + 把 total 当剩余量递减；领到第 5 张 total→0 落库，第 6 次起 `total>0` 判定 false 跳过限量校验→限量券翻转为无限券（11-3）
- 优惠券使用：`useCoupon`（`:235-247`）先查 status==0 后改 1，无原子 UPDATE/乐观锁→并发重复核销（09-08）
- 评论/收藏/足迹：`CommentBizModel:65-88`/`CollectBizModel:32-48`/`FootprintBizModel:40-50` read-then-write 去重，ORM 无 (userId,type,valueId) 唯一键（11-10）

**越权（IDOR）**
- 订单操作：`cancel/prepay/pay/ship/confirm`（`LitemallOrderBizModel.java:272-304` 等）不校验本人；`getMyOrder:457` 做了校验（09-11）
- 地址：`submit()`（`:122-165`）addressBiz.get 不校验 address.userId==context.userId→跨用户地址 IDOR/PII 泄露（11-1）

**优惠与价格校验**
- cart 旧价：`submit()`（`:181-198`）orderGoods.setPrice(item.getPrice()) 来自 cart 快照，未用已查的 product.getPrice()（09-10）
- 券范围：`submit()`（`:204`）调 selectCouponForOrder 传 goodsIds=null→类目券/指定商品券范围校验跳过（11-4）
- 团购商品匹配：`submit()`（`:208-246`）应用 grouponPrice 不校验 rules.goodsId 在购物车（11-5）
- 双重退款：售后 apply 不改 orderStatus + refundGrouponOrder 守卫仅判 PAY + outRefundNo 双前缀（09-09/11-8）

**状态机对齐**
- 团购成功：status=3 在 ORM 字典/前端/后端三层缺失（01-4）；`joinGroupon` 达 discountMember 仅抛 GROUPON_FULL

**前端呈现（AMIS）**
- 实付金额：3 页面用 `goodsPrice+freightPrice-couponPrice` 忽略 groupon/integral（08-3）
- 订单状态映射：order-list/order-detail 只覆盖 101/102/201/301/>=401，103/202/203/204 命中"未知"（08-4）
- 优惠券：checkout.page.yaml labelField:description 指向不存在字段（08-5）
- admin 按钮：订单缺发货、商品缺上下架、团购缺发布、营销管理菜单全注释、团购 view 空壳（08-1/2/7/8）
- 售后：退款按钮 batch+单 id 矛盾（08-6）、类型映射错+缺 type2（08-11）

## Goals

1. **并发安全**：为关键实体引入乐观锁或原子 UPDATE，消除券领取/使用/评论收藏去重的并发竞态（09-07/08、11-3/10）
2. **越权收口**：用户面订单操作校验本人、管理员面加权限、submit 校验地址归属（09-11、11-1）
3. **优惠价格正确**：submit 用当前 product 价、传 goodsIds 校验券范围、校验团购 rules.goodsId 匹配、消除双重退款（09-10、11-4/5、09-09）
4. **状态机对齐**：团购成功 status=3 补全（ORM 字典+后端判定+前端四态）（01-4）
5. **前端呈现正确**：金额公式改 actualPrice、订单状态映射补全、优惠券 labelField、admin 业务按钮补齐、营销菜单取消注释、售后类型/退款按钮修正（08-*）

## Non-Goals

- **P0 资损路径**（支付回调/pay 凭证/超卖/退款金额/GOODS_MISS/cascade/DDL 索引）：归 Plan 1
- **流程合规与文档漂移**（闭合审计回补、技能真值源、project-context/codebase-map/roadmap 刷新、Phase 11/13 状态归真）：归 Plan 3
- **并发治理的非域逻辑部分**（如 system config 缓存）：未在审计 09/11 中被列为发现，且不属本计划结果表面
- **下单以外的团购规则/券规则管理后台细节**：只补发布按钮与状态，规则编辑表单沿用生成默认值
- **评论管理菜单注释（08-15，P3）**：本计划聚焦 P1 前端项；P3 的 08-15 不纳入，留待后续维护
- **delta picUrl 测试字段清理（13-12）**：属模型层清理，非本计划结果表面（若需清理由模型相关计划承接）

## Task Route

- Type: `implementation-only change` + `app-layer design change`（团购 status=3 涉及 ORM 字典）
- Owner Docs: `docs/design/order-and-cart.md`、`marketing-and-promotions.md`、`product-catalog.md`、`app-overview.md`、`feature-inventory.md`
- Skill Selection Basis: `nop-backend-dev`（并发/校验/状态机 BizModel）、`nop-frontend-dev`（AMIS 呈现）、`nop-testing`（IGraphQLEngine + 并发）、`nop-orm-modeler`（团购字典+version 列，若选乐观锁）

## Infrastructure And Config Prereqs

- Plan 1 闭合（资金/库存/状态机稳定）——本计划的 cancel/useCoupon/refundGrouponOrder 改动依赖 Plan 1 的事务边界与状态机闭环
- No new infra beyond existing baseline

## Execution Plan

### Phase 1 — 并发治理（乐观锁 + 原子 UPDATE）

Status: planned
Targets: `model/app-mall.orm.xml`（version 列或唯一约束，若选）、`LitemallCouponUserBizModel.java`、`LitemallCouponBizModel.java`、`LitemallCommentBizModel.java`、`LitemallCollectBizModel.java`、`LitemallFootprintBizModel.java`
Required Skill: `nop-backend-dev`, `nop-orm-modeler`, `nop-testing`

- Item Types: `Fix | Decision | Proof`
- Prereqs: Plan 1 Phase 2/3（库存/退款逻辑稳定）；**若 Decision 选 A（version 列）或 B 的唯一约束兜底路径，触发 ORM 模型变更 AI 阻断条件，需 human approval**

- [ ] **Skill Loading Gate:** 加载 `nop-backend-dev`+`nop-orm-modeler`+`nop-testing`，读必读文档，列路径。
- [ ] **Decision — 并发控制策略：** 选项 A：为关键实体（CouponUser/Comment/Collect/Footprint）ORM 加 version 列走平台乐观锁（**触发 ORM 模型变更，需 human approval**）；选项 B：纯原子条件 UPDATE（`where id=? and status=0`）+ 影响行数校验（首选，不涉模型迁移）；CouponUser 总量控制另算。注：Order 的并发治理（cancel 与定时任务）已归 Plan 1 Phase 2，不在本 Phase。记录 alternatives + 残留风险
- [ ] **Fix: 优惠券领取竞态 + total 翻转（09-07/11-3）。** 不递减 total（语义是发行上限，以 count(coupon_user) 为准）；原子 UPDATE 或唯一约束兜底；限量校验改 `total!=null` 且 count<total
- [ ] **Fix: 优惠券使用并发重复核销（09-08）。** useCoupon 改原子条件 UPDATE `where id=? and status=0`，影响行数=0 抛异常
- [ ] **Fix: 评论/收藏/足迹并发去重（11-10）。** 加唯一约束（(userId,type,valueId,deleted)）或条件 UPDATE；Comment 的 orderGoods.comment 用条件 UPDATE `where comment=0`
- [ ] **Proof: 并发测试。** ExecutorService 构造券并发领取/使用、评论重复提交，断言不超发/不重复
- [ ] **Proof: IGraphQLEngine 测试。**

Exit Criteria:
- [ ] 券并发不超发/不重复核销（09-07/08、11-3 闭环）
- [ ] 评论/收藏/足迹并发不重复（11-10 闭环）
- [ ] 改动 @BizMutation 通过 IGraphQLEngine + 并发测试
- [ ] `marketing-and-promotions.md` 券规则小节与实现一致
- [ ] `docs/logs/` updated

### Phase 2 — 越权收口

Status: planned
Targets: `LitemallOrderBizModel.java`、`LitemallAftersaleBizModel.java`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Fix | Proof`
- Prereqs: **Plan 1 Phase 1（pay/prepay 拆分稳定）+ Phase 2（cancel 守卫稳定）**——cancel/prepay/pay 是 Plan 1 与本 Phase 共同触碰方法，必须 Plan 1 落地后再加 userId 校验，避免改动冲突

- [ ] **Skill Loading Gate:** 加载 `nop-backend-dev`+`nop-testing`，读必读文档，列路径。
- [ ] **Fix: 订单操作本人校验（09-11）。** cancel/prepay/pay/confirm 增 `requireUserIdMatch(order, context)`（在 Plan 1 改动后的方法签名上叠加）；ship/refund 管理员面加 @Auth 管理员角色注解
- [ ] **Fix: submit 地址归属校验（11-1）。** `submit()` 增 `if(!userId.equals(address.getUserId())) throw ERR_ORDER_ADDRESS_INVALID`
- [ ] **Proof: IGraphQLEngine 测试。** 越权 cancel/submit 他人地址应拒绝

Exit Criteria:
- [ ] 订单操作不可越权（09-11 闭环）
- [ ] submit 不可 IDOR 他人地址（11-1 闭环）
- [ ] 改动 @BizMutation 通过 IGraphQLEngine 测试
- [ ] `docs/logs/` updated

### Phase 3 — 优惠与价格校验 + 双重退款消除

Status: planned
Targets: `LitemallOrderBizModel.java`、`LitemallCouponUserBizModel.java`、`LitemallGrouponBizModel.java`、`LitemallAftersaleBizModel.java`、`model/app-mall.orm.xml`（注释）
Required Skill: `nop-backend-dev`, `nop-testing`, `nop-orm-modeler`

- Item Types: `Fix | Proof`
- Prereqs: Plan 1 Phase 3（退款状态机闭环 + 退款累计上限 API 落地）；**改 ORM `:1000` comment 触发 AI 阻断条件，需 human approval**

- [ ] **Skill Loading Gate:** 加载 `nop-backend-dev`+`nop-testing`+`nop-orm-modeler`，读必读文档，列路径。
- [ ] **Fix: submit 用当前 product 价（09-10）。** 循环已查 product，直接 `orderGoods.setPrice(product.getPrice())` 并用 product 价算 lineTotal
- [ ] **Fix: submit 传 goodsIds 校验券范围（11-4）。** 计算 goodsIds（含 categoryId）传入 selectCouponForOrder；goodsType=1 类目匹配、=2 商品集合校验；goodsIds 空时拒绝受限券
- [ ] **Fix: submit 校验团购 rules.goodsId 匹配（11-5）。** 校验 checkedItems 含 rules.goodsId；openGroupon/joinGroupon 复用同校验
- [ ] **Fix: 双重退款消除（09-09/11-8）。** refundGrouponOrder 守卫扩展为 `orderStatus==PAY && aftersaleStatus==INIT`；统一 outRefundNo 策略；**退款累计上限（≤actualPrice）校验复用 Plan 1 Phase 3 落地的累计 API/字段，本计划不重复实现**（所有权归 Plan 1，紧耦合 P0-4）
- [ ] **Fix: grouponPrice 公式与 ORM 注释一致（11-9）。** 同步 `model/app-mall.orm.xml:1000` 注释为 `actualPrice = order_price - integral_price - groupon_price`；**改 comment 属性触发 _app.orm.xml 重生成，按 ORM 模型变更走 codegen（mvn install 重新生成产物），禁止手改 _gen 文件**
- [ ] **Proof: IGraphQLEngine 测试。** cart 旧价不生效、受限券越界拒绝、团购商品不匹配拒绝、双重退款场景阻断

Exit Criteria:
- [ ] 下单用当前价（09-10 闭环）
- [ ] 券范围/团购商品校验生效（11-4/5 闭环）
- [ ] 双重退款消除（09-09 闭环）
- [ ] 改动 @BizMutation 通过 IGraphQLEngine 测试
- [ ] ORM `:1000` 注释改动后生成产物同步（_app.orm.xml 重生成，无手改 _gen）、BUILD SUCCESS
- [ ] `order-and-cart.md`/`product-catalog.md` 价格小节一致
- [ ] `docs/logs/` updated

### Phase 4 — 团购成功状态对齐（ORM 字典 + 后端 + 前端）

Status: planned
Targets: `model/app-mall.orm.xml`、`LitemallGrouponBizModel.java`、`groupon-activity-detail.page.yaml`
Required Skill: `nop-orm-modeler`, `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`

- Item Types: `Fix | Decision | Proof`
- Prereqs: **human approval（ORM 模型变更 AI 阻断条件，Decision A 涉及 ext:dict + 新增字典文件）**

- [ ] **Skill Loading Gate:** 加载 `nop-orm-modeler`+`nop-backend-dev`+`nop-frontend-dev`，读必读文档，列路径。
- [ ] **Decision — 团购状态建模路径：** Live `model/app-mall.orm.xml:789` 团购 STATUS 列**无 `ext:dict` 引用**，状态码仅在 displayName 自由文本描述（对比 Coupon 列 `:429 ext:dict="mall/coupon-status"`）。选项 A：新增 `mall/groupon-status.dict.yaml`（含 0/1/2/3）+ 列加 `ext:dict` 引用（推荐，与其他 status 字段一致，前端可字典渲染）；选项 B：仅改 displayName 文本加"开团成功则3"。记录 alternatives
- [ ] **Fix: 团购状态建模补 status=3 开团成功（01-4，按 Decision）。** 选 A 则新增字典文件 + `model/app-mall.orm.xml:789` 加 ext:dict + 字典补 value=3；选 B 则只改 displayName。重新生成产物
- [ ] **Fix: joinGroupon 成团判定（01-4）。** participantsCount>=discountMember 时把相关 groupon 记录置 status=3（事务内）
- [ ] **Fix: 前端四态（01-4/08-12）。** `groupon-activity-detail.page.yaml:64` tpl 补 status=3 分支（选 A 则前端可走字典映射）；visibleOn 嵌入 tpl 提升为组件属性（08-12）
- [ ] **Proof: IGraphQLEngine + e2e。** 成团判定、前端四态渲染

Exit Criteria:
- [ ] 团购成功状态三层一致（01-4 闭环）
- [ ] model 变更后生成产物同步、BUILD SUCCESS
- [ ] `marketing-and-promotions.md` 团购状态小节一致
- [ ] `docs/logs/` updated

### Phase 5 — 前端呈现正确性（admin 按钮 + storefront 公式/映射）

Status: planned
Targets: `app-mall-web/.../LitemallOrder/LitemallOrder.view.xml`、`LitemallGoods/LitemallGoods.view.xml`、`LitemallGrouponRules/LitemallGroupon.view.xml`、`mall/user/order-*.page.yaml`、`mall/checkout/checkout.page.yaml`、`mall/checkout/order-result.page.yaml`、`LitemallAftersale.view.xml`、`auth/app-mall.action-auth.xml`、`mall/user/aftersale-*.page.yaml`
Required Skill: `nop-frontend-dev`

- Item Types: `Fix | Add`
- Prereqs: Phase 3/4（后端状态/价格稳定）+ **Plan 1 Phase 3（订单 202/203 写入稳定，前端状态映射 202/203 分支依赖此后端落地）**

- [ ] **Skill Loading Gate:** 加载 `nop-frontend-dev`，读 view-and-page-customization/delta-customization 必读，列路径。每文件 selfcheck。
- [ ] **Fix: storefront 实付金额改 actualPrice（08-3）。** order-detail:177、order-list:85/150/192、order-result:47 四处 `goodsPrice+freightPrice-couponPrice` 改 `${data.actualPrice}`
- [ ] **Fix: 订单状态映射补全（08-4）。** order-list:58、order-detail:44 补 103/202/203/204（202/203 分支在 Plan 1 Phase 3 闭合后单独验证）
- [ ] **Decision — 优惠券 labelField 修复路径（08-5）：** 选项 A：后端 `listMyCoupons` 增 couponDescription 计算字段（改 @BizQuery，需 IGraphQLEngine 测试 + 加 nop-testing 技能）；选项 B：前端改 labelTpl 用 `${item.coupon.name}/${item.coupon.discount}`（纯前端，不改后端）。**推荐 B**（避免改 @BizQuery，与 coupon-center.page.yaml 的 `${item.coupon.name}` 模式一致）。选定后在 item 明确路径
- [ ] **Fix: 优惠券 labelField（08-5，按 Decision）。** 按 Decision B 改 checkout.page.yaml:111 labelTpl（纯前端）或按 A 改后端（需加测试）
- [ ] **Add: admin 订单发货按钮+状态分组（08-1）。** LitemallOrder.view.xml 增 ship-button 调 @mutation:LitemallOrder__ship + orderStatus tab
- [ ] **Add: admin 商品上下架按钮（08-2）。** LitemallGoods.view.xml 增 on-sale/off-sale 按钮 visibleOn 切 isOnSale
- [ ] **Add: admin 团购发布按钮+状态 tab（08-8）。** GrouponRules/Groupon.view.xml 增 publishRules/unpublishRules + status 筛选
- [ ] **Fix: 营销管理菜单取消注释（08-7）。** action-auth.xml:80-101 promotion-manage 及子项
- [ ] **Fix: 售后退款按钮 batch 矛盾（08-6）。** 移除 batch="true" 或改 batchRefund(Set ids)
- [ ] **Fix: 售后类型映射+补 type2（08-11）。** aftersale-apply/aftersale-list 补 type=2 退货退款；type1 标签改"已收货无需退货"（与 Plan 1 Phase 2 GOODS_MISS 后端协调）
- [ ] **Fix: 意见反馈菜单指向（08-10）。** action-auth mall-feedback URL 改 LitemallFeedback；新增 FAQ 菜单指向 LitemallIssue
- [ ] **Proof: e2e 页面渲染 + 编译。** admin 按钮 storefront 渲染冒烟

Exit Criteria:
- [ ] storefront 金额/状态/优惠券显示正确（08-3/4/5 闭环）
- [ ] admin 发货/上下架/团购发布按钮可用（08-1/2/8 闭环）
- [ ] 营销菜单可达、售后退款按钮/类型正确（08-6/7/11 闭环）
- [ ] e2e 全绿（38+ 基线不退化）
- [ ] `docs/logs/` updated

### Phase Final — 验证与文档

Status: planned
Targets: 全局
Required Skill: `nop-testing`

- [ ] **Skill Loading Gate:** 加载 `nop-testing`，读必读。
- [ ] **Proof: `./mvnw clean package -DskipTests` BUILD SUCCESS。**
- [ ] **Proof: `./mvnw test` + e2e 全绿。**
- [ ] **Add: owner docs 对齐。** order-and-cart/marketing-and-promotions/product-catalog/app-overview/feature-inventory
- [ ] **Add: dev log。**

Exit Criteria:
- [ ] 全绿
- [ ] owner docs 一致
- [ ] `docs/logs/` updated

## Plan Audit

- Status: passed (consensus: 3 轮审计，Round 3 连续 clean，0 blockers，0 majors，残留 2 minors 非阻塞)
- Round 1 Reviewer / Agent: independent subagent (ses_12f21fc59ffe...)；Verdict REVISE（B1 跨计划依赖 + M1/M2/M3 + 5 minors）→ 全部修订
- Round 2 Reviewer / Agent: independent subagent (ses_12f1297abffeiOerM7pnq77ubW)；Verdict REVISE（新 Blocker B2：ORM 变更漏标 Protected Area，违反 project-context.md:55 + 4 minors）→ 顶部加 Protected 头部 + Phase 1/3/4 Prereqs human approval + Plan Audit 双解锁注
- Round 3 Reviewer / Agent: independent subagent (ses_12f082aa8ffePcPs8Qi67UruX8)；Verdict PASS（B2 + n1/n2/n4 RESOLVED，n3 partial；2 minors：08-15 trigger 措辞偏弱 + Phase 5 优惠券 Decision 潜在技能缺口——条件性，仅偏离推荐选 A 时实例化）
- Evidence: ORM 三触点（Phase 3 :1000 comment、Phase 4 :789 ext:dict、Phase 1 version/唯一约束）全部 Protected 标注经 live 模型文件核验；与 Plan 1 Protected 范式对齐

> 注：Protected Area 项（Phase 1 version 列/唯一约束、Phase 3 ORM comment、Phase 4 ext:dict+字典）需 plan audit + human approval 双解锁后方可 implementation。

## Closure Gates

- [ ] in-scope behavior is complete（并发/越权/优惠/状态机/前端 P1）
- [ ] relevant docs aligned
- [ ] verification: package + test + e2e 全绿
- [ ] 所有新增/改动 @BizMutation/@BizQuery 通过 IGraphQLEngine 测试
- [ ] no in-scope item downgraded to deferred/follow-up
- [ ] plan audit passed
- [ ] each phase has Required Skill listed；Nop phase 不写 none
- [ ] skill loading verification + selfcheck
- [ ] text consistency verified
- [ ] closure audit was independent
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### 团购规则详情页客户端过滤（08-13）
- Classification: `optimization candidate`
- Why Not Blocking Closure: 功能可用，仅效率低（加载 100 条展示 1 条）
- Successor Required: `yes`（触发条件：团购规则数据量增长或性能投诉时改用 get(id)）

### 首页人气推荐/新品首发按 isHot/isNew 过滤（08-14）
- Classification: `optimization candidate`
- Why Not Blocking Closure: 展示的是在售商品，非资损；运营可调整 sortOrder 缓解
- Successor Required: `yes`（触发条件：运营要求精准热门推荐时）

### 用户统计 SQL 未过滤软删用户（11-11）
- Classification: `watch-only residual`
- Why Not Blocking Closure: 报表口径偏差，非资损
- Successor Required: `yes`（触发条件：Phase 13 报表真正上线时，与 Plan 3 协调）

## Closure

Status Note: <待 implementation + closure audit 后填写>
