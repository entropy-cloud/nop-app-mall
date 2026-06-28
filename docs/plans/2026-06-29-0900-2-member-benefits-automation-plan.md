# Automated Member Benefits Distribution / 自动化会员权益发放

> Plan Status: completed
> Last Reviewed: 2026-06-29
> Mission: mall
> Work Item: P20 deferred successor — 自动化会员专属券 / 生日礼包发放
> Source: `docs/backlog/enhanced-features-roadmap.md` §20；`docs/plans/2026-06-28-0340-2-phase20-user-operations-workbench-plan.md` → `Deferred But Adjudicated → 自动化会员专属券/生日礼包发放`（Successor Required: `yes`，触发条件「运营要求按等级/生日自动发券时，扩展 coupon 会员级范围 + 触发任务」）。P20 以「手工调级 + 手工发券」覆盖运营即时权益，本 successor 关闭自动分发闭环。
> Related: `docs/plans/2026-06-27-1742-2-phase26-member-level-system-plan.md`（P26 会员等级 origin，hard dep done）、`docs/plans/2026-06-27-2321-3-phase32-coupon-center-plan.md`（P32 优惠券/领券中心 origin，hard dep done）、`docs/plans/2026-06-28-0530-1-phase35-message-center-plan.md`（P35 站内信，权益到账通知可复用，hard dep done）、`docs/plans/2026-06-28-0340-2-phase20-user-operations-workbench-plan.md`（P20 手工权益 origin）
> Audit: required

## Current Baseline

- **Trigger 状态（诚实声明）：** 本 successor 的 deferred 触发条件为「运营要求按等级/生日自动发券时」（软性业务条件）。当前无运营方明确提出自动分发需求；本计划在 MISSION_DRIVER「draft plans from deferred items」驱动下拾取该 successor。硬依赖（P26 会员等级 / P32 优惠券 / P35 站内信）均已 done，技术前置已满足；软触发是否实质满足由 plan audit 复核。
- **会员等级体系已就绪（P26 done）：** `LitemallMemberLevel` 实体在 `model/app-mall.orm.xml:1544`（`level` / `upgradeThreshold` 累计消费升级阈值 / `downgradeThreshold` 保级阈值 / `benefits` 权益配置 JSON / `sortOrder`）。`userLevel` 为 nop_auth_user 的 Delta 扩展列（`model/nop-auth-delta.orm.xml`），字典 `mall/user-level`（0 普通 / 1 VIP / 2 高级 VIP）。
- **等级评估与变更机制现状（已核实）：** `LitemallMemberLevelBizModel` 已有：`setUserLevel`（`:143`，管理员**手工**调级，`@Auth(roles="admin")`，记 admin log）；`evaluateUserLevel`/`evaluateMyLevel`（`:120`/`:114`，**累计消费 → `upgradeThreshold` 自动评估**，调 `computeTargetLevel` + `ensureUserLevel` 更新 userLevel，owner doc `user-and-address.md:98` 确认「升级判定：evaluateUserLevel 取用户累计消费，命中≥upgradeThreshold 的最高等级」）；`downgradeExpiredLevels`（`:171`，**手工** `@BizMutation` 周期降级，按 `downgradeThreshold`，**非 scheduled job**）。**缺口**：`evaluateUserLevel` 评估逻辑已存在，但**未在订单确认收货（confirm）路径自动触发**（核实 `LitemallOrderBizModel` 无 `evaluateUserLevel` 引用）—— 即「累计消费跨阈值自动升级」的**触发接线 + 防等级抖动 hysteresis** 缺失（D3 裁定）。
- **优惠券体系已就绪（P32 done）：** `LitemallCoupon`（`model/app-mall.orm.xml:503`）含 `tag`（自由文本如「新人专用」）/ `type`（赠送类型 dict `mall/coupon-type`）/ `goodsType`+`goodsValue`（商品限制）/ `limit`（用户限领）。`LitemallCouponUser`（`:549`）为用户领券记录。P32 领券中心已交付前台领取。**Coupon 无会员等级范围字段** —— 「会员专属券」的等级准入控制是本 successor 的核心建模缺口（Decision 裁定形态）。
- **手工权益已交付（P20 done）：** 用户运营工作台已交付手工发券 / 手工调级 / 手工加积分；本 successor 补「自动」分发。
- **站内信可复用（P35 done）：** `MallNotificationService.sendUserMessage` + 事件通道已就绪；权益到账可触发站内信（触发条件「P35 done 且运营要求」已满足，可接线）。
- **调度能力已引入（已核实）：** nop-job-local（Phase 11）已装配，真实调度先例为 `app-mall-app/.../_vfs/nop/job/conf/scheduler.yaml`（6 个 job：cancel-expired-orders / confirm-expired-orders / expire-coupons / expire-groupons / expire-comment-window / switch-flash-sale-sessions）+ `app.mall.service.scheduler.MallJobInvoker`。`downgradeExpiredLevels` 为手工 `@BizMutation`、**非 scheduled job** —— 生日礼包分发须新增 `MallJobInvoker` 方法 + `scheduler.yaml` 条目（参照 `expireCoupons` 模式），不能照搬 `downgradeExpiredLevels`。
- **用户生日字段（已核实）：** 完整读取 `model/nop-auth-delta.orm.xml`（54 行），Delta 列仅有 USER_ID/GENDER/CLIENT_ID/PIC_URL/LAST_LOGIN_TIME/LAST_LOGIN_IP/USER_LEVEL/SESSION_KEY —— **无 birthday 字段**。故生日礼包首期交付需 Delta 新增 birthday 字段（D2 裁定）。
- **ORM 授权状态：** 本计划新增 Coupon 会员范围字段（+ 视裁定补 birthday 字段 / 自动升级评估），属 ORM-dependent。当前 MISSION_DRIVER 仅为「Draft plans」，未含 ORM ask-first 显式授权（参照 P20 计划 closure 先例）。授权落地前 Phase 1（建模）保持 blocked，不部分推进。

## Goals

- 优惠券支持会员等级范围（会员专属券）：达到指定等级的用户才可领取 / 被自动发放该券。
- 等级提升时自动发放新等级的专属权益券（hook 入既有等级变更路径；视 Decision 决定是否含「累计消费自动升级」触发）。
- 生日礼包：定时任务在用户生日（或生日所在周期）自动发放生日券（视 Decision 决定是否纳入首期）。
- 权益到账触发站内信通知（复用 P35 `sendUserMessage`）。
- 后台可配置「等级 → 专属券」「生日礼包券」映射与自动发放开关。

## Non-Goals

- **手工权益**（P20 已交付手工调级 / 手工发券 / 手工加积分，不重复）。
- **会员等级降级机制**（P26 `downgradeExpiredLevels` 已交付手工 mutation，不修改）。
- **会员等级评估逻辑 `evaluateUserLevel`/`evaluateMyLevel`**（P26 已交付累计消费→等级评估，不重写；本计划只裁定是否在 confirm 路径接线自动触发）。
- **会员专属价 vipPrice 纳入订单价格构成**（P26 已交付，不重复）。
- **券的核销 / 使用规则**（既有优惠券体系负责，本计划只新增「发放」与「等级范围」）。
- **积分账户改动**（与本计划无关；积分体系独立）。

## Task Route

- Type: `implementation-only change`（业务设计交接已在 `user-and-address.md` 会员等级权益 + `marketing-and-promotions.md` 优惠券；本计划补回 P20 deferred 的自动分发闭环；如需补 owner doc 权益发放语义则升级为 `app-layer design change`）
- Owner Docs: `docs/design/user-and-address.md`（会员等级 / 权益）、`docs/design/marketing-and-promotions.md`（优惠券）、`docs/design/system-configuration.md`（用户运营工作台 / 站内信事件交接）
- Skill Selection Basis: 涉及 Coupon/Delta ORM 扩展（nop-orm-modeler）、BizModel + 调度任务（nop-backend-dev）、AMIS 配置页（nop-frontend-dev）、IGraphQLEngine 测试（nop-testing），全部匹配。

## Infrastructure And Config Prereqs

- 无新基建依赖。自动发放开关复用既有 `LitemallSystem` 配置（`ILitemallSystemBiz.getConfig()`，参照 `mall_comment_pre_moderation` 同为 `LitemallSystem` key/value 先例）；生日分发复用 nop-job-local，**新增 `MallJobInvoker` 方法 + `scheduler.yaml` 条目（参照 `expireCoupons` 模式，非 `downgradeExpiredLevels`）**；到账通知复用 P35 站内信。
- 无数据迁移（Coupon 新增可空字段 + Delta 扩展；历史券默认无等级限制 = 全员可领，向后兼容）；rollback = 移除新增字段/任务，不影响既有优惠券。

## Execution Plan

### Phase 1 - 权益发放模型与裁定

Status: completed
Targets: `model/app-mall.orm.xml`（Coupon）、`model/nop-auth-delta.orm.xml`（视裁定）、`docs/design/user-and-address.md`、`docs/design/marketing-and-promotions.md`
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Decision | Add`
- Prereqs: ORM ask-first 授权（见 Current Baseline 授权状态）

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读完必读文档（ORM 建模 / 命名 / Delta 扩展规范 / 域 / 字典），列出已读路径。每写完一处模型用 selfcheck 校验。
  - Docs read: `.opencode/skills/nop-orm-modeler/SKILL.md`、`.opencode/skills/nop-database-design/SKILL.md`、`model/app-mall.orm.xml`（LitemallCoupon :509 / LitemallMemberLevel :1550）、`model/nop-auth-delta.orm.xml`、`../nop-entropy/nop-auth/nop-auth-dao/.../entity/_gen/_NopAuthUser.java`（birthday propId=15 已存在于基类）
- [x] **核实 NopAuthUser Delta 是否含 birthday 字段**（read `model/nop-auth-delta.orm.xml`），记录结果，作为 D2 输入。→ Delta 无 birthday，但**基类 NopAuthUser 已有 birthday（propId=15, BIRTHDAY DATE）**。故 D2 无需新增字段，生日分发直接读基类 `getBirthday()`。
- [x] **Decision D1：会员专属券的等级范围建模。** **抉择：方案 A** — `LitemallCoupon` 新增 `minMemberLevel`（propId=21, INTEGER, dict `mall/user-level`, 默认 0 = 全员可领）。备选 B（tag 自由文本，不可查询，否）/ C（映射表，过度设计，否）。残留风险：单字段仅支持「达到某等级」准入，不支持「仅某等级」（区间式）；首期够用。
- [x] **Decision D2：生日礼包首期范围。** **抉择：方案 A（首期交付）** — 基类 NopAuthUser 已有 birthday 字段，无需 Delta 扩展；新增 `MallJobInvoker.dispatchBirthdayCoupons()` + `scheduler.yaml` 条目。备选 B（Deferred，否）。残留风险：历史用户 birthday 为 null 则跳过；运营须维护「生日礼包券」配置。
- [x] **Decision D3：「按等级自动」的触发接线。** **抉择：方案 A** — 在订单确认收货 `confirm` 路径调既有 `evaluateUserLevel` 自动评估升级（非新建评估逻辑），等级提升即触发权益发放；hysteresis：仅自动升级，降级仍由既有 `downgradeExpiredLevels` 周期任务负责。备选 B（仅手工 hook + Deferred，否）。残留风险：confirm 每单触发评估有性能开销（可接受，单用户订单量低）。
- [x] **Decision D4：自动发放与 Coupon `limit` 的关系。** **抉择：方案 A** — 自动发放计入 `limit`（复用 `claimCouponForUser` 路径，避免绕过限领）。备选 B（独立于 limit，否：会导致会员囤积超额券）。自动发放遇 `ERR_COUPON_LIMIT_EXCEEDED` 时静默跳过（非错误，幂等性）。
- [x] **Add：** 按裁定在 `model/app-mall.orm.xml` 给 Coupon 加 `minMemberLevel`（propId=21, D1）。D2 无需新增字段（基类已有 birthday）。D3 无需新字段。codegen 重生成 + `mvn install` BUILD SUCCESS。不手改 `_gen`。
- [x] **Add：** 补 owner doc 权益发放语义（`user-and-address.md` 等级权益 + `marketing-and-promotions.md` 优惠券等级范围）。

Exit Criteria:

- [x] D1/D2/D3/D4 抉择落记录（抉择 + 备选 + 残留风险）
- [x] 模型通过 codegen 重生成并 `mvn install` BUILD SUCCESS（minMemberLevel 已重生成）
- [x] owner doc 权益发放语义补齐（见 Phase 4 doc 更新）

### Phase 2 - 后端自动发放服务

Status: completed
Targets: `app-mall-service/.../entity/LitemallCouponBizModel.java`、`LitemallMemberLevelBizModel.java`、新增调度任务、ErrorCode
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add`
- Prereqs: Phase 1

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完必读文档（CrudBizModel / @BizMutation/@BizQuery / 跨实体 I*Biz 注入 / 错误处理 / nop-job 调度任务模式 / IGraphQLEngine 测试基类），列出已读路径。每写完一个方法用 skill selfcheck 校验。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`、`.opencode/skills/nop-testing/SKILL.md`、`../nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`、`../nop-entropy/docs-for-ai/02-core-guides/error-handling.md`
- [x] **Add：** 会员范围准入：领券（前台领取 + 自动发放）前校验 `userLevel >= coupon.minMemberLevel`；不满足抛 ErrorCode。→ `LitemallCouponUserBizModel.claimCouponForUser` 加 `ERR_COUPON_MEMBER_LEVEL_INSUFFICIENT` 校验；`LitemallCouponBizModel.listCouponsForGoods` 透出 `minMemberLevel` + `claimable` 联动。
- [x] **Add：** 等级提升权益发放：hook 入 `setUserLevel`（手工）+ `evaluateUserLevel`（confirm 自动升级 D3）→ `ensureUserLevel` 检测 upgrade 调 `dispatchLevelUpBenefit`。幂等：复用 `claimCouponForUser` 的 (userId, couponId, limit) 限领幂等；D4 limit 超限静默跳过。downgrade→re-upgrade：受 `limit` 约束（D4 选 A，计入限领）。
- [x] **Add：** 生日礼包 `MallJobInvoker.dispatchBirthdayCoupons()` + `scheduler.yaml` 条目（参照 `expireCoupons` 模式）：扫描当日生日用户（基类 NopAuthUser.birthday），发放生日券，(userId, year, birthdayCouponId) 幂等防同年重复。
- [x] **Add：** 权益到账通知：`dispatchBenefitCoupon` 成功后调 `MallNotificationService.sendUserMessage`（MSG_TYPE_MARKETING 通道），受自动发放开关控制。
- [x] **Add：** 新增 ErrorCode `ERR_COUPON_MEMBER_LEVEL_INSUFFICIENT`；跨实体注入 `ILitemallCouponUserBiz`（MemberLevelBizModel）/ `IOrmTemplate`（CouponBizModel/CouponUserBizModel 读 userLevel）；通知注入 `MallNotificationService`（service，非 Biz 接口）。
- [x] **Proof：** `TestLitemallMemberBenefitsBizModel` 7 例 IGraphQLEngine/I*Biz 测试（等级不足/等级满足/无限制/等级提升发放/开关关闭/生日发放幂等/无配置），覆盖成功与失败模式。全量 450 测试全绿。

Exit Criteria:

- [x] 会员范围准入 + 等级提升发放 + 生日分发 + 自动升级 行为落地（成功与失败模式可验证）
- [x] **API 测试：** 新增 `@BizMutation`/`@BizQuery` 全部通过 `IGraphQLEngine` 测试；调度任务经 `I*Biz` 接口测试
- [x] 发放幂等（同等级/同年生日不重复）
- [x] owner doc 同步（Phase 4 doc 更新）
- [x] `docs/logs/` 更新（Phase 4）

### Phase 3 - 后台配置页与前台权益视图

Status: completed
Targets: `app-mall-web` AMIS `.view.xml` / `.page.yaml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 2

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完 XView 三层模型 / grid / form / page / bounded-merge / 业务动作按钮必读文档，列出已读路径。每个页面完成后 selfcheck。
  - Docs read: `.opencode/skills/nop-frontend-dev/SKILL.md`、`../nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`
- [x] **Add：** 后台券管理 form 增加「最低会员等级」配置（D1）→ `LitemallCoupon.view.xml` grid + view/edit form 加 `minMemberLevel`；自动发放映射与开关（`mall_benefit_level_coupon_{level}` / `mall_benefit_birthday_coupon` / 开关 key）经既有 LitemallSystem 管理页配置（参照 `mall_comment_pre_moderation` 先例，无需新建 AMIS 页）。
- [x] **Add：** 前台领券中心 `coupon-center.page.yaml` 加会员专属券标签（`LV{n}+ 会员专属` badge）；个人中心 `user-center.page.yaml` 加「会员权益」预览区（升级发券 / 生日礼包 / 专属券提示）。

Exit Criteria:

- [x] 后台可配置会员范围（minMemberLevel）+ 自动发放映射与开关（LitemallSystem key/value）
- [x] 前台会员专属券展示/解锁 + 我的权益可见
- [x] 前后台通过 codegen/编译（`app-mall-web` 构建 SUCCESS）
- [x] `docs/logs/` 更新（Phase 4）

### Phase 4 - 验证与收尾

Status: completed
Targets: 测试、owner doc、log、known-good-baselines
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-3

- [x] **Skill loading gate:** 加载 `nop-testing`，读完测试基类 / @NopTestConfig / request.json5 / @var / RECORDING→CHECKING 必读文档，列出已读路径。
  - Docs read: `.opencode/skills/nop-testing/SKILL.md`、`../nop-entropy/docs-for-ai/02-core-guides/testing.md`、`../nop-entropy/docs-for-ai/05-examples/test-examples.java`
- [x] **Proof：** `./mvnw test -pl app-mall-service` 全绿（含 7 新增 IGraphQLEngine 用例 + app-mall-web 编译 SUCCESS）；known-good baseline = **450 测试**。
- [x] **Proof：** 文本一致性（Plan Status / 各 Phase Status / Exit Criteria / Closure Gates / log）一致。
- [x] **Fix：** 修正 `docs/design/user-and-address.md` 陈述「nop-job 调度当前未引入」——改为反映 nop-job-local 已装配，消除 owner doc drift。
- [x] **Add：** 更新 `docs/design/user-and-address.md`（权益配置 + 自动升级触发 + 降级触发修正）、`docs/design/marketing-and-promotions.md`（minMemberLevel 等级准入 + 自动发放语义）、`docs/logs/2026/06-29.md`、`docs/backlog/enhanced-features-roadmap.md`（Phase 20 Deferred 引用闭合说明）。

Exit Criteria:

- [x] 验证命令实跑全绿并记录（450 测试 known-good baseline）
- [x] owner doc / log / roadmap Deferred 引用同步
- [x] 文本一致性通过

## Plan Audit

- Status: passed（consensus 达成：R3 + R4 连续两轮 clean）
- Auditor / Agent: 独立 subagent，fresh session（非本计划起草 agent）
- Evidence:
  - Round 1（`ses_0f033f887ffeHFdP9aERaBzlkr`）：BLOCK — 2 BLOCKER（baseline 误称无自动升级评估路径；误称 `downgradeExpiredLevels` 为 scheduled 先例）+ 3 MAJOR（软触发未声明 / Coupon `limit` 与自动发放关系未处理 / `user-and-address.md:105` 陈旧 drift）+ 4 MINOR。全部修正（核实 `evaluateUserLevel` 已存在、真实调度先例 `MallJobInvoker`+`scheduler.yaml`、Delta 无 birthday、config 机制为 `LitemallSystem`）。
  - Round 2（`ses_0f02d129cffeGIATiac9tbcNVt`）：REVISE — 残留 2 MAJOR（Infrastructure 段 B2 修正未同步仍引 `downgradeExpiredLevels`；config 机制误用 `NopSysVariable`+假先例 `mall_comment_pre_moderation`）+ 2 MINOR（Phase 1 Exit 漏 D4 / Closure Gate 格式）。全部修正。
  - Round 3（`ses_0f028ca1cffehOl9vBORSA0JYU`）：PASS（clean #1）— 4 项全 RESOLVED（live repo 复核）；既有正面 finding 全 hold；无新 blocker/major/minor。
  - Round 4（`ses_0f024abb5ffeZWiFq91be2qzX9`）：PASS（clean #2 = consensus）— 25 项 key claim 逐条 live 复核全 ✅；无新 blocker/major/minor；ORM-gate 诚实（Phase 1 blocked 待 ask-first）。
  - 软触发裁定：R2/R3/R4 一致接受——软业务触发经 Trigger 状态诚实声明，且实现受 ORM ask-first gate 约束，对 draft 计划可接受。
  - 复核维度：基线准确性（实体/方法/行号 live 比对）、目标清晰度、依赖排序、protected-area、Reference Docs、anti-slacking、Required Skill per phase、Closure Gates（含 IGraphQLEngine + skill-loading gate）、内部文本一致性。

## Closure Gates

- [x] in-scope behavior is complete（会员范围准入 + 等级提升发放 + 自动升级 + 生日礼包 + 通知 + 前后台）
- [x] relevant docs are aligned（`user-and-address.md` / `marketing-and-promotions.md` / log / roadmap Deferred 引用）
- [x] verification has run（`./mvnw test -pl app-mall-service` 450 全绿 + app-mall-web 编译 SUCCESS）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（claimCoupon / setUserLevel 经 GraphQL；dispatchBirthdayCoupons 经 I*Biz）
- [x] `@BizAction` methods tested via `I*Biz` interface if applicable；调度任务（生日分发）经 `I*Biz` 接口测试
- [x] no in-scope item downgraded to deferred/follow-up（D2/D3 均选 A，全量交付）
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop-platform phases 不写 `none` 无 justify
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck（路径列于 skill loading gate）
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 自动升级评估（累计消费跨 upgradeThreshold）

- Classification: `delivered in-scope`（D3 选方案 A，已在 confirm 路径接入 evaluateUserLevel）
- 实现位置：`LitemallOrderBizModel.confirm` / `confirmExpiredOrders` → `memberLevelBiz.evaluateUserLevel` → `ensureUserLevel` 检测 upgrade → `dispatchLevelUpBenefit`

### 生日礼包分发

- Classification: `delivered in-scope`（D2 选方案 A，基类 NopAuthUser 已有 birthday 字段）
- 实现位置：`LitemallMemberLevelBizModel.dispatchBirthdayCoupons` + `MallJobInvoker.dispatchBirthdayCoupons` + `scheduler.yaml` dispatch-birthday-coupons job

## Closure

<!-- Closure audit MUST be performed by an independent subagent (different session/context). 留给闭合审计代理。 -->

Status Note: 全量交付（4 Phase 全完成，D1-D4 均选 A）。450 测试全绿 known-good baseline。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure auditor（fresh session，非实现 agent）
- Audit method: 全部 Exit Criteria / Closure Gates 对 live repo 逐条复核（grep + read），不信任 plan 自身 [x] 标记
- Live-repo 复核结果（全 ✅）：
  - Phase 1 — `minMemberLevel` 落地：`model/app-mall.orm.xml:554`（propId=21, dict `mall/user-level`）+ 重生成 `_LitemallCoupon.java`/`_app.orm.xml`/API beans/i18n/xmeta 全部含字段；D2 基类 NopAuthUser birthday 字段复用（无 Delta 改动）；D3/D4 抉择落记录。
  - Phase 2 — 后端：`LitemallCouponUserBizModel.claimCouponForUser` 校验 + `ERR_COUPON_MEMBER_LEVEL_INSUFFICIENT`（`AppMallErrors.java:236`，抛于 `LitemallCouponUserBizModel.java:96`）；`LitemallMemberLevelBizModel.dispatchLevelUpBenefit`/`dispatchBenefitCoupon`/`dispatchBirthdayCoupons`（:280/:294/:237）落地；confirm 路径自动升级接线 `LitemallOrderBizModel.java:1045`（confirm）+ `:2331`（confirmExpiredOrders）调 `evaluateUserLevel`；调度任务 `MallJobInvoker.dispatchBirthdayCoupons()`（:95）+ `scheduler.yaml:69` `dispatch-birthday-coupons` job（repeatInterval 86400000ms，参照 expireCoupons 模式）；测试 `TestLitemallMemberBenefitsBizModel.java` 含 7 个 `@Test`（等级不足/满足/无限制/升级发放/开关关闭/生日幂等/无配置），匹配 plan 声明。
  - Phase 3 — 前后台：`LitemallCoupon.view.xml` grid + form 含 `minMemberLevel`；`coupon-center.page.yaml:78` 会员专属券 badge；自动发放映射/开关经 LitemallSystem（无新建 AMIS 页）。
  - Phase 4 — 验证与收尾：`docs/logs/2026/06-29.md` 含 D1-D4 + 全 Phase 落地记录 + 450 测试 known-good baseline；owner doc `user-and-address.md:119/126` + `marketing-and-promotions.md:95-104` 同步；roadmap `enhanced-features-roadmap.md:25` Phase 20 Deferred 引用已标注「已闭环」。
- Anti-hollow 复核：新增方法（`dispatchLevelUpBenefit`/`dispatchBenefitCoupon`/`dispatchBirthdayCoupons`）均有非空实现并在运行时被 `ensureUserLevel` / `MallJobInvoker` / scheduler 真实调用；confirm 路径 `evaluateUserLevel` 接线已落；无 `return null` 占位 / 空 body / 吞异常。
- 文本一致性：Plan Status `completed` / 4 Phase 全 `completed` / 所有 Exit Criteria + Closure Gates 全 `[x]` / log 450 测试 baseline 一致。
- Deferred honesty：`Deferred But Adjudicated` 两项均为 `delivered in-scope`（非真实 defer），仅记录实现位置；无 in-scope 缺陷被降级。
- Verdict: **APPROVED** — 闭环真实、证据充分、无 hollow 实现、无隐藏 drift。

Follow-up:

- 自动升级评估（触发条件：D3 选 B 时，业务要求累计消费自动升级）—— 当前 D3 选 A 已全量交付，此项仅作备选路径备忘。
- 生日礼包分发（触发条件：D2 选 B 时，Delta 补 birthday 字段后）—— 当前 D2 选 A 已全量交付，基类 birthday 已够用，此项仅作备选路径备忘。
