# P32 优惠券体系增强（Coupon Center & DIY Delivery）

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 32；`docs/design/marketing-and-promotions.md`（优惠券章节）
> Related: `docs/plans/2026-06-27-1742-2-phase26-member-level-system-plan.md`（P26 done，本计划交付「领券入口」+「DIY 投放配置」后可解除 P26 deferred「专属券权益」对 P32 的依赖）；`docs/plans/2026-06-27-2321-1-phase28-check-in-plan.md`、`2026-06-27-2321-2-phase33-structured-comment-plan.md`（同批次）
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 32（优惠券体系增强）

> **执行顺序：** 本计划为 2026-06-27-2321 批次第 3 顺位（N=3）。独立无跨计划依赖；领券中心已交付（见 Baseline），剩余两交付项（商品详情领券入口 + DIY 投放配置）范围较小，列收尾位。

## Current Baseline

> 实读 live repo（HEAD `0cc75d9`）所得。

**领券中心已完整交付（关键，roadmap Entity Coverage「无新增」已对齐）：**

- `app-mall-web/.../pages/mall/coupon/coupon-center.page.yaml`（197 行完整实现）——「可领取优惠券」tab（卡片展示 discount/min/tag/desc/有效期 + 立即领取按钮调 `@mutation:LitemallCouponUser__claimCoupon`）+ 「兑换码领取」（调 `redeemCoupon`）+ 「我的优惠券」tab（未使用/已使用/已过期，调 `listMyCoupons`）。
- 后端 API 齐备：`LitemallCouponBizModel.listAvailableCoupons`（`@BizQuery @Auth(publicAccess=true)`，status=0 且未过期）、`listMyCoupons`（`@BizQuery`）；`LitemallCouponUserBizModel.claimCoupon`/`claimCouponForUser`（`@BizAction`，synchronized 锁 + total/limit 校验 + timeType 有效期处理）、`redeemCoupon`、`selectCouponForOrder`（含 `parseGoodsValue` 商品范围匹配逻辑 line 56-62）。
- Coupon 模型字段齐全（`model/app-mall.orm.xml:492-537`）：`tag`(标签如新人专用)、`total`(数量 0=无限)、`limit`(用户限领)、`goodsType`(`ext:dict=mall/coupon-goods-type`)、`goodsValue`(商品范围值)、`type`(`ext:dict=mall/coupon-type`)、`timeType`/`days`/`startTime`/`endTime`。字典均已定义。

**缺口（本计划交付对象）：**

1. **商品详情页领券入口缺失（关键）：** `goods-detail.page.yaml` 无任何优惠券入口（grep coupon/领券/claimCoupon 零命中）。无「该商品可用券」查询——`listAvailableCoupons` 返回全部公开券，未按 goodsType/goodsValue 对单个商品过滤，也未标记当前用户是否已领。用户在商品详情页看不到可领的券。
2. **DIY 投放配置不完整：** 后台 `LitemallCoupon.view.xml`（admin）grid 已有 12 列（id/name/tag/type/discount/min/total/limit/status/timeType/startTime/endTime，line 7-18），但 **edit/add/view form 均为空骨架**（line 25-27），未暴露 `goodsType`/`goodsValue`（商品范围投放）、`limit`（领券限制）、`tag`（人群标签）、`timeType`/`days`/`startTime`-`endTime`（有效期）；grid 也缺 `goodsType`/`goodsValue` 列。运营无法在后台完成「指定商品范围 + 限领 + 有效期」的 DIY 投放配置。
3. **分类范围券（goodsType=1）兑换路径存在既有缺陷（关键，rule #13 非降级）：** `LitemallCouponUserBizModel.selectCouponForOrder`（`:174-190`）仅以 `goodsType != 0` 作「是否限范围」标志，**未区分 CATEGORY(1) vs GOODS(2)**；`parseGoodsValue`（`:241`）只做逗号分割成 ID 列表，随后将 `goodsId` 与该列表逐项比较（`:182-188`）。对 goodsType=1，`goodsValue` 存的是**分类 ID**，却被当**商品 ID** 比较 → 永不命中 → 抛 `ERR_COUPON_GOODS_NOT_MATCH`。用户在商品详情领了分类券后，结算时会被拒（既有缺陷）。本计划在展示侧启用分类券（goods-detail 入口 + DIY 配置），**必须同步修复兑换侧**，否则展示与兑换不自洽。
4. **业务设计缺失：** `marketing-and-promotions.md` 无「领券入口展示规则」「DIY 投放配置语义」章节（领券中心页虽已实现但设计文档未回写）。
5. **无商品级可用券查询 API**（`listCouponsForGoods`）；**无分类范围匹配逻辑**（coupon 域无 categoryId 匹配，需跨实体经 `ILitemallGoodsBiz` 取商品 categoryId 后判定）。
6. **无测试覆盖商品级领券入口路径与分类范围兑换。**

**前置条件已满足：** P8（优惠券体系）`done`；领券中心已交付。

## Goals

- 商品详情页领券入口：展示「本商品可用优惠券」+ 当前用户已领状态 + 一键领取。
- 新增 `listCouponsForGoods(goodsId)` 查询：按 goodsType/goodsValue 商品范围过滤可用券（含**分类范围 net-new 匹配逻辑**：经 `ILitemallGoodsBiz` 取商品 categoryId 后与 goodsValue 分类列表判定）+ 标记当前用户已领/可领。
- **修复分类范围券（goodsType=1）兑换缺陷：** `selectCouponForOrder` 增加 CATEGORY 分支（解析商品 categoryId 与 goodsValue 分类列表匹配），使展示侧与兑换侧自洽。
- 后台 DIY 投放配置完善：admin Coupon **edit form** 充分暴露商品范围（goodsType/goodsValue）/限领（limit）/标签（tag）/有效期（timeType/days/startTime-endTime）配置；grid 补 goodsType/goodsValue 列。
- 业务设计回写：领券入口展示规则 + DIY 投放配置语义写入 `marketing-and-promotions.md`。
- 新增/复用 ErrorCode（商品范围校验）；核心路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- 领券中心页（coupon-center.page.yaml）——已交付，本计划不改其主流程（仅在需要时回写设计）。
- 优惠券核销率/ROI 统计（属 P22 营销活动管理后台）。
- 会员专属券自动发放（P26 权益——本计划交付「领券入口 + DIY 配置」为专属券提供通道，但自动发放逻辑属 P26 successor，不在本计划）。
- 营销活动管理后台统一入口（P22）。
- 移动端前端。

## Task Route

- Type: `app-layer design change` + `implementation-only change`
- Owner Docs: `docs/design/marketing-and-promotions.md`（优惠券章节补领券入口 + DIY 投放规则）、`docs/design/system-configuration.md`（后台优惠券投放配置）
- Skill Selection Basis: 后端 BizModel 查询方法 → `nop-backend-dev`；`@BizQuery` 测试 → `nop-testing`（规则 #15）；AMIS 页面（admin form + goods-detail 入口）→ `nop-frontend-dev`；模型字段已就绪，预期不改模型

## Infrastructure And Config Prereqs

- 无外部服务/端口/密钥依赖。无数据迁移。
- 商品范围匹配复用既有 `parseGoodsValue` 逻辑（`selectCouponForOrder` 已用）。

## Protected Area

> 见 `docs/context/ai-autonomy-policy.md`。本计划**预期不**触及受保护区域：
>
> - `model/app-mall.orm.xml`：Coupon 字段与字典均已存在，预期无需改模型。若需补索引则按 ask-first 处理。
> - 不触及 `app-mall-delta`。

## Execution Plan

### Phase 1 — 业务设计合成：领券入口 + DIY 投放规则（Decision-heavy）

Status: completed
Targets: `docs/design/marketing-and-promotions.md`（优惠券章节补领券入口展示 + DIY 投放配置）、`docs/design/system-configuration.md`（后台投放配置）
Required Skill: `none`（纯 docs 业务语义合成，模型已就绪不改）

- Item Types: `Decision | Add`
- Prereqs: P8 done

- [x] **Skill loading gate:** 扫描 available skills；docs-only，无匹配。读 owner doc：`marketing-and-promotions.md`（优惠券章节）、`system-configuration.md`、`enhanced-features-roadmap.md` Phase 32、`domain-design-guidelines.md`。
  - Docs read: `docs/design/marketing-and-promotions.md`、`docs/design/system-configuration.md`、`docs/backlog/enhanced-features-roadmap.md`（Phase 32 行 37/113/402）、`docs/design/domain-design-guidelines.md`
- [x] **Decision: 商品详情页领券入口展示规则。** 备选 A：展示「本商品可用券」列表（按 goodsType/goodsValue 过滤）+ 每券已领/可领状态 + 领取按钮。备选 B：仅展示「有券可领」徽标 + 跳转领券中心。**抉择 A**（减少跳转、提升转化）。**残留风险：** 商品详情页 service 调用增加一次查询；若券量大需分页/限数。领券中心仍保留全量券与兑换码入口，两者分工（详情页=本品可用券，领券中心=全部券）。
- [x] **Decision: 已领状态标记方式 + listCouponsForGoods 鉴权模型。** 备选 A：`listCouponsForGoods` 返回每券附 `claimedByMe`(boolean) + `claimable`(boolean，受 limit 约束)。备选 B：前端二次查询我的券。**抉择 A**（一次查询，性能优）。**鉴权模型：** `listAvailableCoupons` 为 `@Auth(publicAccess=true)`（匿名可浏览）；`listCouponsForGoods` 同样设 `publicAccess=true`（匿名可看商品可用券），但 `claimedByMe` 在 **匿名上下文（userId=null）下恒为 false**（未登录视为未领），登录后才查 CouponUser。**残留风险：** 匿名→登录切换时已领状态需前端刷新；`claimable` 在匿名下按 limit 总量推算（非用户级精确），仅作展示，领取时以 `claimCoupon` 的强校验为准。
- [x] **Decision: DIY 投放配置语义。** 固化 goodsType（0=全部/1=分类/2=指定商品，dict `mall/coupon-goods-type` 语义）、limit（0=不限/默认1）、tag（人群标签如「新人专用」纯展示，不参与会员级路由）、timeType（0=领后N天有效/1=固定时间段）的运营配置语义，写入 design doc。**残留风险：** 当前无会员级（member-level）范围机制，tag 为纯展示，会员专属券仍需 P26 successor；DIY 投放仅支持商品/分类维度。
- [x] **Add:** 将领券入口展示规则 + DIY 投放配置语义写入 `marketing-and-promotions.md`；后台投放配置写入 `system-configuration.md`。

Exit Criteria:

- [x] `marketing-and-promotions.md` 含领券入口展示规则 + DIY 投放配置语义
- [x] 3 个 Decision 抉择/备选/理由/残留风险已记录
- [x] Phase 2 模型改动清单确定（预期零新增列）

### Phase 2 — 模型准备（按 Phase 1 Decision）

Status: completed
Targets: `model/app-mall.orm.xml`（仅当 Decision 要求）、codegen 重生成
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Add`
- Prereqs: Phase 1

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读 routing 必读文档。
  - Docs read: `model/app-mall.orm.xml:492-581`（LitemallCoupon + LitemallCouponUser 实体定义）、字典定义 `:29-47,84-87`。已确认所有字段（goodsType/goodsValue/limit/tag/timeType/days/startTime/endTime）+ 字典（mall/coupon-goods-type / mall/coupon-time-type / mall/coupon-type）均已存在。
- [x] **Add:** 按 Phase 1 Decision——Coupon 字段与字典均已存在；Decision 不需补聚合索引（已有 idx_couponUser_userId/orderId/couponId 覆盖查询路径）；本 phase 无模型改动，仅验证 codegen 产物完整。
- [x] **Proof:** codegen + 编译 BUILD SUCCESS（`./mvnw compile -DskipTests -pl app-mall-service -am` 1.467s SUCCESS）。

Exit Criteria:

- [x] 模型就绪，codegen 通过，编译成功
- [x] 不写业务逻辑（rule #11）

### Phase 3 — 后端：商品级可用券查询 + 分类范围兑换修复 + ErrorCode（Add-heavy / Fix）

Status: completed
Targets: `app-mall-service/.../entity/LitemallCouponBizModel.java`、`LitemallCouponUserBizModel.java`（selectCouponForOrder 分类分支修复）、`AppMallErrors.java`
Required Skill: `nop-backend-dev`、`nop-testing`（新增 `@BizQuery` + 修复 `selectCouponForOrder`，规则 #15）

- Item Types: `Add | Fix`
- Prereqs: Phase 2

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读 routing 必读文档（service-layer/error-handling/safe-api/selfcheck/testing；跨实体访问走 `I*Biz`）。每方法 selfcheck。
  - Docs read: `nop-entropy/docs-for-ai/02-core-guides/service-layer.md`、`05-examples/ibiz-and-bizmodel.java`、`04-reference/safe-api-reference.md`、`04-reference/bizmodel-method-selfcheck.md`、`02-core-guides/testing.md`、`05-examples/test-examples.java`。每方法已对照 selfcheck 19 项校验（@BizQuery/@Auth/@Inject 三参数 findList、requireEntity/get 走管道、ErrorCode + NopException、@Inject 非 private、不 new 实体 newEntity()）。
- [x] **Fix（rule #13 非降级，分类范围兑换缺陷）：** 改造 `LitemallCouponUserBizModel.selectCouponForOrder` 增加 CATEGORY(1) 分支：当 goodsType=1 时，经 `ILitemallGoodsBiz` 取订单商品的 categoryId 集合，与 `parseGoodsValue` 解析出的分类 ID 列表取交集判定范围命中（而非把分类 ID 当商品 ID 比较）。保持 GOODS(2)/ALL(0) 既有逻辑不变。**Net-new 逻辑**（coupon 域无既有 categoryId 匹配，需 `@Inject ILitemallGoodsBiz` 跨实体取 categoryId）。
- [x] **Add:** `LitemallCouponBizModel.listCouponsForGoods(@BizQuery, goodsId, context)` —— `@Auth(publicAccess=true)`（与 listAvailableCoupons 一致）；复用 `listAvailableCoupons` 基础过滤（status=0/未过期/公开 type），再按 goodsType/goodsValue 对 goodsId 过滤：ALL(0) 命中、CATEGORY(1) 经 `ILitemallGoodsBiz` 取 goodsId.categoryId 与 goodsValue 分类列表匹配、GOODS(2) goodsId 在 goodsValue 列表中。每券附 `claimedByMe`（匿名 userId=null 恒 false；登录查 CouponUser）+ `claimable`（受 limit 约束，匿名按总量推算）。返回带状态的结构化列表。
- [x] **Add/Proof:** `AppMallErrors` 复用既有 `ERR_COUPON_GOODS_NOT_MATCH`（分类修复后该错误码语义更准确）；若需商品级领取边界码则补。
- [x] **Proof:** listCouponsForGoods + selectCouponForOrder 分类修复通过 `IGraphQLEngine`：全部商品券(goodsType=0)、**分类券命中/不命中（修复后兑换侧亦通过）**、指定商品券命中/不命中、已领标记 claimedByMe、限领已满 claimable=false、过期/未发布不返回、匿名 userId=null claimedByMe=false、selectCouponForOrder 分类券结算命中。全量回归无失败。

Exit Criteria:

- [x] 商品级可用券查询返回按范围过滤 + 已领/可领状态；匿名态 claimedByMe=false
- [x] **分类范围券兑换缺陷已修复**（selectCouponForOrder CATEGORY 分支），展示侧与兑换侧自洽
- [x] **API 测试：** listCouponsForGoods（`@BizQuery`）+ selectCouponForOrder 分类修复通过 `IGraphQLEngine` 验证（新增 4 + 2 共 6 个测试用例，全 18 个 coupon 测试通过；全量 `./mvnw test` 35.742s SUCCESS）
- [x] 复用既有 claimCoupon 领取（不重复造领取逻辑）

### Phase 4 — 前端：商品详情领券入口 + 后台 DIY 投放配置（Add-heavy）

Status: completed
Targets: `app-mall-web/.../pages/mall/goods/goods-detail.page.yaml`、`pages/LitemallCoupon/LitemallCoupon.view.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 3

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读 routing 必读文档。文件完成后 selfcheck。
  - Docs read: `nop-entropy/docs-for-ai/00-start-here/application-project-defaults.md`、`02-core-guides/view-and-page-customization.md`（bounded-merge + x:prototype 模式）、`03-runbooks/prefer-delta-over-direct-modification.md`、`03-runbooks/add-page-business-action.md`。已对照 frontend selfcheck 反模式表（不直接改 _gen、bounded-merge 白名单、x:prototype 复用 edit form 作为 add form）。
- [x] **Add:** 商品详情页领券入口区块：调 `@query:LitemallCoupon__listCouponsForGoods`（传当前 goodsId），展示可用券卡片（discount/min/tag）+ 已领置灰/可领按钮（调 `@mutation:LitemallCouponUser__claimCoupon`，领取后刷新）；无券 `visibleOn` 隐藏。
- [x] **Add:** 后台 `LitemallCoupon.view.xml` DIY 投放配置完善：**edit/add/view form（当前空骨架）** 充分暴露 goodsType（dict 下拉）/goodsValue（商品范围配置）/limit（领券限制）/tag（人群标签）/timeType（dict）/days/startTime-endTime（有效期）；grid bounded-merge **补 goodsType/goodsValue 列**（tag/limit/timeType/startTime/endTime 已在 grid，无需重复添加）。

Exit Criteria:

- [x] 商品详情页展示本商品可用券 + 领取入口；已领置灰
- [x] 后台可完成 DIY 投放配置（商品范围/限领/标签/有效期）
- [x] 复用既有 AMIS 模式，无新前端依赖（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` 8.017s BUILD SUCCESS，含 app-mall-web 编译）

### Phase 5 — 验证、文档同步、日志（Proof）

Status: completed
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-4

- [x] **Skill loading gate:** 加载 `nop-testing`。
- [x] **Proof:** 跑真实验证命令，全绿；更新 `known-good-baselines.md`。(`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` 8.017s BUILD SUCCESS；`./mvnw test` 35.742s SUCCESS 含新增 6 测试)
- [x] **Proof:** 前端 view 编译 BUILD SUCCESS。（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` 含 app-mall-web 模块编译通过）
- [x] 更新 `docs/logs/2026/06-27.md`。（顶部新增「2026-06-28 — Phase 32 优惠券体系增强」完整 entry）

Exit Criteria:

- [x] 全量验证通过（含本计划 IGraphQLEngine 测试）
- [x] `known-good-baselines.md` 更新
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗审计（fresh session），三轮达成共识（round 1 REVISE → round 2 PASS → round 3 PASS，两个连续 clean round post-revision）。
  - Round 1（`ses_0f64f01c1ffeOHwjoLE4mAqOc1`）：REVISE — 1 BLOCKER（分类范围券 goodsType=1 兑换缺陷：`selectCouponForOrder`(@CouponUserBizModel.java:174-190) 不区分 CATEGORY/GOODS，把分类 ID 当商品 ID 比较永不命中，rule #13 非降级）+ 3 MAJOR（admin grid 基线失实——grid 已有 12 列仅 form 空骨架、「复用 parseGoodsValue+categoryId」实为 net-new 跨实体逻辑、3 个 Decision 缺残留风险且 listCouponsForGoods 鉴权未决）+ 4 MINOR。全部修订：Phase 3 加 Fix 项修复 CATEGORY 分支（经 ILitemallGoodsBiz 取 categoryId）；基线订正 grid 12 列 + form 空骨架；net-new 逻辑如实标注；3 Decision 补残留风险 + listCouponsForGoods 设 publicAccess + 匿名 claimedByMe 恒 false。
  - Round 2（`ses_0f6471482ffezxTpKmNoO0f7Bt`）：PASS — BLOCKER + 3 MAJOR 全 RESOLVED，1 非阻塞 MINOR（错误码软措辞）。
  - Round 3 共识轮（`ses_0f643bce9ffewh7nS3BSxr366C`）：PASS — 第 2 个连续 clean round post-revision，共识达成。实读核验分类兑换缺陷 @ CouponUserBizModel.java:174-190,241 完整、Fix 项 @ plan:123、Decisions 残留风险齐、Closure Gate 覆盖 rule #15。
- Evidence: 实读 live repo（HEAD `0cc75d9`）核验。关键发现：领券中心页（coupon-center.page.yaml 197 行）+ claimCoupon/listAvailableCoupons API 已完整交付，P32 实际缺口为商品详情领券入口 + DIY 投放 form + 分类范围兑换缺陷修复；roadmap Entity Coverage「无新增」已对齐。

## Closure Gates

- [x] in-scope behavior is complete（商品详情领券入口 + DIY 投放配置 + 分类范围兑换修复）
- [x] relevant docs are aligned（`marketing-and-promotions.md` / `system-configuration.md`）
- [x] verification has run（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` 全绿 + `./mvnw test` 全绿 35.742s + `app-mall-web` 编译，见 `docs/context/project-context.md`）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（listCouponsForGoods + selectCouponForOrder 分类修复，6 个新测试用例）
- [x] no in-scope item downgraded to deferred/follow-up（分类兑换缺陷为 Fix 非 Follow-up）
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（Phase 1 `none` 含 justify）
- [x] skill loading verification
- [x] text consistency verified
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

> 起草时预期无可延期项。优惠券核销率/ROI 统计在 Non-Goals 显式移出（属 P22）；会员专属券自动发放在 Non-Goals 移出（属 P26 successor）。

## Closure

<!-- Closure audit MUST be performed by an independent subagent. 留给闭合审计代理。 -->

Status Note: 全部 5 个 Phase Status=completed、所有 Phase Exit Criteria 全 `[x]`、所有 Closure Gates 全 `[x]`、Plan Status=completed 一致。实读 live repo（HEAD `0cc75d9`）逐项核验，所有交付物均已落盘且通过验证命令，无可延期项隐藏在 Deferred。

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计 subagent（fresh session，非实施 agent）。
- Evidence:
  - **Phase 3 后端核验（Exit Criteria vs live repo）：** `app-mall-service/.../entity/LitemallCouponBizModel.java:105` 存在 `listCouponsForGoods(@BizQuery)`；`LitemallCouponUserBizModel.java:192` 存在 CATEGORY 分支（`COUPON_GOODS_TYPE_CATEGORY`），`:221-228` 经 `ILitemallGoodsBiz` 取 categoryId 集合做交集匹配，展示侧与兑换侧自洽。非空壳：方法体完整，无 `return null` 占位。
  - **Phase 4 前端核验：** `goods-detail.page.yaml:96-140` 存在「本商品可用优惠券」service 区块（调 `@query:LitemallCoupon__listCouponsForGoods` + 已领置灰/可领按钮调 `@mutation:LitemallCouponUser__claimCoupon`）；`LitemallCoupon.view.xml:15-16` grid 补 `goodsType`/`goodsValue` 列，`:27-58` edit/view/add form（add `x:prototype="edit"`）显式 layout 暴露全部 DIY 字段。
  - **API 测试核验（rule #15）：** `TestLitemallCouponBizModel.java:162,189,209,231,252` 通过 `IGraphQLEngine` 验证 `listCouponsForGoods`（ALL/GOODS/CATEGORY 范围命中与排除、claimedByMe、匿名态）；`TestLitemallCouponUserBizModel.java:76-77` 验证 CATEGORY 券结算命中/不匹配拒。
  - **Docs 同步核验：** `marketing-and-promotions.md:60,66-67,84,92-97,105` 含领券入口展示规则 + DIY 投放配置语义 + 分类兑换自洽；`system-configuration.md:85,89,95-99,107-120` 含后台 DIY 投放配置；`docs/logs/2026/06-27.md:3-10` 含完整 Phase 32 entry。
  - **Anti-Hollow 检查：** 新代码均在运行时被调用——listCouponsForGoods 经 GraphQL schema 暴露并被 goods-detail.page.yaml 调用；selectCouponForOrder CATEGORY 分支在结算链路被调用；admin form 字段经 AMIS 渲染。无注册但不可达组件。
  - **验证命令：** `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS + `./mvnw test` 全量 35.742s SUCCESS（含 6 个新测试 + 18 个 coupon 测试全绿）。
  - **Verdict: APPROVED.** 无 BLOCKER / MAJOR，文本一致性全通过，Plan 可闭合。

Follow-up:

- 优惠券核销率/ROI 统计（触发条件：P22 营销活动管理后台启动）。
- 会员专属券自动发放（触发条件：P26 权益 successor）。
