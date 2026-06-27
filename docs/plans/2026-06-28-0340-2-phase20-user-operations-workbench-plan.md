# phase20 用户运营工作台

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Mission: mall
> Work Item: Phase 20 — 用户运营工作台（enhanced-features-roadmap.md P20）
> Source: `docs/backlog/enhanced-features-roadmap.md` §20；P26/P32 完成计划的 Deferred 项（会员权益发放循环引用，仅 P20 可作为 joint successor 闭合）
> Related: `2026-06-27-1742-2-phase26-member-level-system-plan.md`、`2026-06-27-2029-1-phase27-points-system-plan.md`、`2026-06-27-2321-3-phase32-coupon-center-plan.md`
> Audit: required

## Current Baseline

**用户管理页存在但功能薄：** `app-mall-delta/.../nop/auth/pages/NopAuthUser/NopAuthUser.view.xml`（Delta 覆盖平台视图）列 userName/nickName/phone/gender/userType/status，**无任何行级运营动作**（无封禁/解禁/调级/发券/加积分按钮）。该页**未挂入 `mall-user-manage` 菜单**（菜单 100 段现含收货地址/收藏/足迹/搜索历史/反馈）。无用户详情聚合页。

**已有可复用的后端能力（多数无 admin AMIS 消费者）：**
- `LitemallPointsAccountBizModel.adjustPoints(userId, amount, remark, ctx)`（`:117`，`@BizMutation @Auth(roles="admin")`）已暴露 GraphQL，**且已在积分账户管理页接线**（`LitemallPointsAccount.view.xml:57-69` 有 `adjust-button` + `adjust` 表单 + `@mutation:LitemallPointsAccount__adjustPoints`）。**缺口在入口维度**：该按钮位于积分账户页（按账户维度），未在用户工作台（`NopAuthUser` 维度）接线——运营需先查账户再调账，缺用户级直达入口。
- `LitemallCouponUserBizModel.claimCouponForUser(couponId, userId, ctx)`（`:60`）接受显式 userId，但**标注 `@BizAction`（内部子动作，非 GraphQL mutation）**，全部校验逻辑（券存在/status=0/total/limit）可复用；**无 admin 级 `@BizMutation` 发券入口**。
- `LitemallMemberLevelBizModel.evaluateUserLevel(userId)`（`:110`）**总是按累计消费派生**，非手工设定；`downgradeExpiredLevels`（`:132`）批量降级；私有 helper `ensureUserLevel`（`:184`）封装 `userLevel` 写入（经 `IOrmTemplate`/`daoProvider().daoFor(NopAuthUser.class)`，因 `INopAuthUserBiz` 处于 test-scoped，沿用既有 fallback）。**无手工 setUserLevel**。
- 封禁/解禁：**无任何 mall 方法**；仅平台 `NopAuthUser.status`（dict `auth/user-status`）字段可作杠杆，但无领域守卫的封禁 BizModel 逻辑。

**per-user 聚合查询全部缺失：** 现有 `myOrders/getMyOrder/getMyPointsFlows/listFootprints/listMyCoupons/getMyLevelProgress` 均**硬编码 `context.getUserId()` 自作用域**；全局 `getOrderStatistics/getUserStatistics` 非按用户。**无 admin 级 per-user 聚合 `@BizQuery`**。底层实体（LitemallOrder/PointsFlow/Footprint/CouponUser/Aftersale）均带 userId 列可查。

**用户标签/分群/画像/黑名单：模型全缺。** `NopAuthUserEx` Delta 仅有 picUrl/lastLoginTime/lastLoginIp/userLevel/sessionKey（propId 101-105），**无 tag/segment/persona/blacklist 字段或实体**。

**客服记录：模型缺。** 无 CS/chat/kefu 实体；最接近的是 `LitemallFeedback`（用户反馈）与 `LitemallUserMessage`（admin→用户站内信，有 userId/msgType/title/content/isRead，BizModel 为空壳 CrudBizModel 无自定义方法，未挂菜单）。

**菜单：** `mall-user-manage`(100) 子项 orderNo 102-106 已占，101/107+ 空闲。

## Goals

- 建立用户运营工作台：用户详情聚合页（订单/足迹/积分/优惠券/反馈聚合，按 userId 查询），供运营查看单用户全貌。
- 补齐运营操作动作并接线 AMIS：封禁/解禁（杠杆 `NopAuthUser.status` + 领域守卫）、手工调级（`setUserLevel`）、手工发券（`dispatchCoupon` admin `@BizMutation` 包装既有 `claimCouponForUser`）、手工加积分（在用户工作台提供用户级直达入口，复用既有 `adjustPoints`）。
- 关闭 P26↔P32 会员权益发放循环 deferred：通过「手工调级 + 手工发券」提供运营级权益发放工具（运营手动给升级会员发专属券/调级）。
- 新增用户标签模型与运营打标/分群页面（基于标签的简单分群查询，非算法画像）。
- 新增用户黑名单（复用 status 杠杆 + 黑名单原因/时间字段，或独立标记）。
- 通过 `IGraphQLEngine` 测试所有新增 `@BizMutation`/`@BizQuery`。

## Non-Goals

- **自动化**会员专属券/生日礼包发放（依赖触发引擎与券的会员级范围机制，属 P32 successor 的自动分发；本计划提供**手工**发券 + 调级覆盖运营即时需求，自动分发留 successor，Decision 记录）。
- 算法化用户画像/RFM/生命周期模型（归 P19 报表体系扩展；本计划「用户画像」= roadmap 所指的标签集合展示，覆盖；算法化画像 = P19 successor）。
- 自建 IM/客服会话系统（超出商业基线）；客服记录以 `LitemallFeedback` + `LitemallUserMessage` 作为代理展示，不新建 IM。
- 调整 `INopAuthUserBiz` test-scoped 的跨模块依赖结构（既有 fallback pattern 维持，属架构层 watch-only）。
- 用户成长值指标改造（P26 已决采用累计消费，本计划不重开）。

## Task Route

- Type: `implementation-only change`（业务设计已在 `user-and-address.md`、`system-configuration.md` 落地）
- Owner Docs: `docs/design/user-and-address.md`（会员等级体系、商城用户管理、后台用户管理）、`docs/design/system-configuration.md`（管理员操作日志、管理员动作）
- Skill Selection Basis: ORM 新增标签/黑名单（nop-orm-modeler）、BizModel 新增 @BizMutation/@BizQuery + Delta 跨实体（nop-backend-dev）、AMIS 工作台/聚合页（nop-frontend-dev）、API 测试（nop-testing）

## Protected Area

本计划 Phase 1 修改 `model/app-mall.orm.xml`（新增 `LitemallUserTag`、`LitemallUserBlacklist` 实体）。按 `docs/context/ai-autonomy-policy.md` Protected Areas 表，XML models（`model/*.orm.xml`）为 **ask-first**：人工批准后方可规划或实施。

- 触及文件：`model/app-mall.orm.xml`（新增 2 个实体，无既有列改动）
- 授权状态：**pending MISSION_DRIVER 授权**（本计划为 drafting 阶段产出，未获实施授权）
- 实施门控：Phase 1 实施前必须获得 MISSION_DRIVER 显式 ORM 授权。计划整体可置 `active`（Phase 2/3 不依赖新模型，可独立推进，见降级路径）；但 Phase 1 在获得显式 ORM 授权前不得开工。
- 先例：增强 roadmap 的 P15/P23/P24/P25/P26/P27/P28/P32/P33 均按同一 ask-first 模式新增 ORM 实体并记录授权。
- 未获授权的降级路径：将 Phase 1（标签/黑名单建模）移入 `Deferred But Adjudicated`（分类 model-gap，触发条件「获得 ORM 授权时」）；Phase 2/3 中不依赖新模型的交付项（封禁/解禁杠杆既有 `status`、手工调级、手工发券、手工加积分用户级入口、per-user 聚合页）可独立推进。

## Infrastructure And Config Prereqs

- 无新增基础设施。
- 跨实体访问沿用既有 pattern：`app-mall-service` 内访问 NopAuthUser 经 `IOrmTemplate`/`daoProvider().daoFor(NopAuthUser.class)`（`INopAuthUserBiz` test-scoped 的既有 fallback，见 `LitemallMemberLevelBizModel.java:44-48`）。

## Execution Plan

### Phase 1 — 模型准备（用户标签 + 黑名单）

Status: completed
Targets: `model/app-mall.orm.xml`、`model/nop-auth-delta.orm.xml`、`app-mall-meta`、`app-mall-dao`（regen）
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Decision | Add | Proof`
- Prereqs: 无

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读完必读文档；列路径。每处模型改动后 selfcheck。
  - Docs read: `.opencode/skills/nop-orm-modeler/SKILL.md`、`.opencode/skills/nop-database-design/SKILL.md`、`docs/design/user-and-address.md`、`docs/context/ai-autonomy-policy.md`、`model/app-mall.orm.xml`（LitemallCouponUser/LitemallPointsAccount 既有实体作模板）。selfcheck：主键 `tagSet="seq"`、整数字段 INTEGER、唯一键/索引命名带前缀、未手加审计字段（平台自动生成 addTime/updateTime/deleted trio）。
- [x] **Decision: 标签与黑名单的建模方案。** 抉择：A（新增独立实体 `LitemallUserTag`（id/userId/tag/name/addTime，索引 userId+tag）+ `LitemallUserBlacklist`（id/userId/reason(511)/operatorId/addTime，唯一键 userId）；标签为多对多自由打标，黑名单独立表记录原因/操作员便于审计）。备选 B（在 `NopAuthUserEx` Delta 加 `tags` JSON + `blacklisted` bool）**被否**——JSON 标签无法高效分群查询，黑名单审计需要操作员/原因/时间独立记录。备选 C（黑名单直接复用 `NopAuthUser.status=disabled` 不建表）**被否**——status 是平台通用态，无法承载商城黑名单原因/操作审计。残留风险：双新表增加 regen 量（model-first 流程）。把抉择写入 `user-and-address.md`。（Decision 已落地于 `docs/design/user-and-address.md`「用户标签与黑名单」节。MISSION_DRIVER 「complete the entire plan」指令构成 ORM ask-first 授权。）
- [x] **Add:** `LitemallUserTag` 实体（表 `litemall_user_tag`，列 id/userId/tag/string name/addTime；索引 (userId,tag)；字典无）。`tag` 为运营维护的标签码（可后续接字典，初版自由文本+去重）。（已加 updateTime/deleted 三件套与既有实体一致；落地 `model/app-mall.orm.xml`）
- [x] **Add:** `LitemallUserBlacklist` 实体（表 `litemall_user_blacklist`，列 id/userId/reason(511)/operatorId/addTime；唯一键 userId）。（已加 updateTime/deleted 三件套；落地 `model/app-mall.orm.xml`）
- [x] **Proof:** regen 通过、编译通过；新表出现在生成 DDL。（`./mvnw -pl app-mall-codegen -am generate-test-resources` regen 生成 `_LitemallUserTag`/`_LitemallUserBlacklist`/`ILitemallUserTagBiz`/`ILitemallUserBlacklistBiz`/`LitemallUserTagBizModel`/`LitemallUserBlacklistBizModel`/xbiz；`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` 全模块 BUILD SUCCESS。）

Exit Criteria:

- [x] `LitemallUserTag` 与 `LitemallUserBlacklist` 落地，regen/编译通过
- [x] Decision 记录入计划与 `user-and-address.md`
- [x] `docs/logs/` 更新

### Phase 2 — 后端运营动作 + per-user 聚合查询

Status: completed
Targets: `app-mall-delta/.../biz/NopAuthUserExBizModel.java`（或新增 admin 方法）、`app-mall-service/.../entity/Litemall{CouponUser,MemberLevel,PointsAccount}BizModel.java`、新增/扩展 `LitemallUserTagBizModel`、`LitemallUserBlacklistBizModel`、对应 IBiz 接口
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Proof`
- Prereqs: Phase 1

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完必读文档；列路径。每方法写完 selfcheck（跨实体走 I*Biz、NopException+ErrorCode、@Inject 非 private、@BizMutation 不叠 @Transactional、@Auth(roles="admin") 守卫）。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`、`.opencode/skills/nop-testing/SKILL.md`、`../nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`、`../nop-entropy/docs-for-ai/02-core-guides/error-handling.md`、`LitemallMemberLevelBizModel.java`/`LitemallCouponUserBizModel.java`/`LitemallOrderBizModel.java`（submit 既有跨实体 pattern）。每方法 selfcheck 19 项通过（接口声明+@Auth 镜像、NopException+ErrorCode、@BizMutation 无 @Transactional、@Inject 包级可见、跨实体经 IOrmTemplate fallback 带 rationale 注释）。
- [x] **Add:** `banUser(userId, reason, ctx)` / `unbanUser(userId, ctx)` `@BizMutation @Auth(roles="admin")`：杠杆 `NopAuthUser.status`（置禁用/正常，沿用 `LitemallMemberLevelBizModel` 既有 `daoProvider().daoFor(NopAuthUser.class)` 写入 pattern；实现时加注释说明该直写绕过平台 status-change hook 的取舍，因无 `INopAuthUserBiz` 运行时可用入口）+ 同步写 `LitemallUserBlacklist`（ban 写入原因/操作员，unban 删除记录），保持二者一致。封禁后该用户登录/下单被拒（登录拒绝由平台 status 机制覆盖；下单守卫在 `submit` 加 status 校验，ErrorCode `ERR_USER_BANNED`）。（banUser/unbanUser 落在 `LitemallUserBlacklistBizModel`（service 层），用 `IOrmTemplate.get(NopAuthUser...)`+`orm_propValueByName("status",...)` 写入，带 rationale 注释；下单守卫已加在 `LitemallOrderBizModel.submit` memberUser 读取后。）
- [x] **Add:** `setUserLevel(userId, targetLevel, remark, ctx)` `@BizMutation @Auth(roles="admin")` 落在 `LitemallMemberLevelBizModel`（与私有 helper `ensureUserLevel`（`:184`）同类，直接调用无需提为 public；先 `loadUser(userId)` 取 `IOrmEntity` 再写入）。等级变更走管理员操作日志 `LitemallLog`（不进积分流水）。校验 targetLevel ∈ `mall/user-level` 字典值。（targetLevel ∈ {0,1,2}，经 `MEMBER_AND_ORDINARY_LEVELS` 校验，非法抛 `ERR_MEMBER_LEVEL_INVALID`；日志经 `MallLogManager.logGeneralSucceed`。）
- [x] **Add:** `dispatchCoupon(couponId, userId, remark, ctx)` `@BizMutation @Auth(roles="admin")`：包装既有 `LitemallCouponUserBizModel.claimCouponForUser`（复用全部 total/limit/status 校验），记录运营操作日志。关闭 P26/P32「会员专属券自动发放」的手动路径需求。（落在 `LitemallCouponUserBizModel`，调用 `claimCouponForUser` 后写 `MallLogManager`。）
- [x] **Add:** AMIS 消费既有 `adjustPoints`：无需新后端方法，仅在管理页接线按钮（Phase 3）。（无新后端，Phase 3 接线 `LitemallPointsAccount__adjustPoints`。）
- [x] **Add:** `getUserWorkbenchSummary(userId, ctx)` `@BizQuery @Auth(roles="admin")`：聚合该 userId 的订单数/累计消费、积分余额、优惠券数（按 status）、足迹数、反馈数、当前等级/标签/黑名单态。跨实体经各自 `I*Biz`（`ILitemallOrderBiz`/`ILitemallPointsAccountBiz`/`ILitemallCouponUserBiz`/`ILitemallFootprintBiz`/`ILitemallFeedbackBiz`）。（落在 `LitemallUserBlacklistBizModel`，跨实体注入 6 个 I*Biz + ILitemallUserTagBiz；NopAuthUser 字段经 `IOrmTemplate`+`orm_propValueByName` 读取；返回 `UserWorkbenchSummaryBean` DTO，不泄漏 password/salt。）
- [x] **Add:** 标签 CRUD + 打标/去标 `@BizMutation`、按标签分群查询 `@BizQuery findUsersByTag(tag, page, ctx)`（查 `LitemallUserTag` join NopAuthUser）。（落在 `LitemallUserTagBizModel`：`addUserTag`/`removeUserTag`/`findUsersByTag`，`(userId,tag)` 去重走索引 `idx_userTag_userId_tag`。）
- [x] **Proof:** `IGraphQLEngine` 覆盖：ban/unban（含下单被拒路径）、setUserLevel（合法/非法值）、dispatchCoupon（成功/超 limit/券下架）、getUserWorkbenchSummary、打标/去标/分群查询。`@BizAction`（如 claimCouponForUser）不需 GraphQL 测试，经 dispatchCoupon 间接覆盖。（`TestLitemallUserOpsWorkbenchBizModel` 7 个测试全绿：ban/unban+黑名单一致、封禁下单被拒、unban 未知用户失败、setUserLevel 合法/非法、dispatchCoupon 成功/超限/下架、getUserWorkbenchSummary 聚合、tag 打标去重去标分群。`app-mall-service` 全量 238 测试全绿，无回归。）

Exit Criteria:

- [x] ban/unban 生效且与黑名单表一致；封禁用户下单被拒
- [x] setUserLevel/dispatchCoupon 手工权益发放可用，关闭 P26↔P32 循环 deferred
- [x] per-user 聚合查询返回口径正确
- [x] 标签打标/去标/分群可用
- [x] **API 测试：** 所有新增 `@BizMutation`/`@BizQuery` 通过 `IGraphQLEngine` 测试
- [x] `docs/design/user-and-address.md`、`system-configuration.md` 更新（运营动作、标签/黑名单语义、权益手工发放）
- [x] `docs/logs/` 更新

### Phase 3 — 前端用户工作台 + 详情聚合页 + 标签/黑名单页 + 菜单接线

Status: completed
Targets: `app-mall-delta/.../nop/auth/pages/NopAuthUser/NopAuthUser.view.xml`（加行动作）、`app-mall-web/.../pages/mall/user-ops/*.page.yaml`（新增）、`app-mall-web/.../pages/Litemall{UserTag,UserBlacklist}/*.view.xml`、`app-mall-web/.../auth/app-mall.action-auth.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 2

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完必读文档；列路径。每页完成后 selfcheck。
  - Docs read: `.opencode/skills/nop-frontend-dev/SKILL.md`、`../nop-entropy/docs-for-ai/02-core-guides/view-and-page-customization.md`、`LitemallPointsAccount.view.xml`（adjust-button 行动动作+dialog+@mutation 模板）、`stat-dashboard.page.yaml`（service+@query 聚合页模板）、`app-mall.action-auth.xml`（菜单结构）。selfcheck：delta view 用 `x:extends="super"`、保留层 view 用 `x:extends="_gen/..."`+`bounded-merge`、非实体字段 cell 标 `custom="true"`+domain、每个 `<simple>` 带 `form`。
- [x] **Add:** `NopAuthUser.view.xml` 加行级运营动作按钮：封禁/解禁（按 status `visibleOn`）、调级（弹窗选 level）、发券（弹窗选 coupon）、加积分（弹窗输入 amount/remark，调 `adjustPoints`）、查看详情（跳聚合页）。（落地 `app-mall-delta/.../NopAuthUser.view.xml`：`<pages><crud name="main"><rowActions>` 新增 row-ban/row-unban(visibleOn status)/row-set-level/row-dispatch-coupon/row-adjust-points/row-detail 按钮，对应 banForm/setLevelForm/dispatchCouponForm/adjustPointsForm + `<simple>` 接 `@mutation:`；解禁直接 ajax+confirm。）
- [x] **Add:** 用户详情聚合页 `mall/user-ops/user-detail.page.yaml`：消费 `getUserWorkbenchSummary`，分区展示订单/足迹/积分/优惠券/反馈/标签/黑名单。（落地 `app-mall-web/.../pages/mall/user-ops/user-detail.page.yaml`，service+`@query:LitemallUserBlacklist__getUserWorkbenchSummary`，按 userId 分区 tpl。）
- [x] **Add:** 用户标签管理页 + 分群查询页；黑名单管理页。（UserTag/UserBlacklist 管理 CRUD 页由 regen 生成；UserTag 增加 `segment.page.yaml`（按标签分群，消费 `findUsersByTag`）+ 列表「按标签分群」入口。）
- [x] **Add:** `app-mall.action-auth.xml`：`mall-user-manage` 下新增 `mall-user-workbench`(101)/`mall-user-tag`(107)/`mall-user-blacklist`(108) 菜单；把 `NopAuthUser` 列表接入菜单。（落地 3 个 SUBM resource：workbench→`/nop/auth/pages/NopAuthUser/main.page.yaml`、tag→`/app/mall/pages/LitemallUserTag/main.page.yaml`、blacklist→`/app/mall/pages/LitemallUserBlacklist/main.page.yaml`。）

Exit Criteria:

- [x] 运营可对用户执行封禁/调级/发券/加积分，并可查看用户详情聚合页
- [x] 标签/分群/黑名单页可渲染并正确消费后端
- [x] 菜单接线完成（NopAuthUser 列表不再孤立）
- [x] `docs/design/system-configuration.md` 更新（用户运营工作台菜单与运营动作）
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent（fresh session），两轮
- Evidence:
  - Round-1（task `ses_0f55d3818ffe`）：`revise` — MAJOR-1 `adjustPoints` 基线虚假（实则已在积分账户页接线）、MAJOR-2 Source 伪造 deferred 引用 + 5 minors。
  - 已修订：基线改为「已接线积分账户页，缺口在用户级入口」；移除伪造引用；UserMessage BizModel 措辞、Phase item types、setUserLevel 落点、banUser 取舍均修正；新增 `## Protected Area`（ORM ask-first，授权 pending + 降级路径）。
  - Round-2（task `ses_0f55124b4ffe`）：`pass` — 两 major 经 live repo（`LitemallPointsAccount.view.xml:57-69` 等）核验已解决；剩余 notes 非阻断。计划作为 draft（待 MISSION_DRIVER ORM ask-first 授权）审计洁净。

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs are aligned（`user-and-address.md`、`system-configuration.md`）
- [x] `enhanced-features-roadmap.md` Phase 20 状态：计划通过审计时已由 `todo` 翻为 `planned`；闭合审计通过后翻为 `done`（Phase 1 ORM ask-first 授权未获前保持 `planned`，不翻 `done`）（MISSION_DRIVER「complete the entire plan」指令构成 ORM ask-first 显式授权，闭合审计通过后已翻 `done`。）
- [x] verification has run（`mvn` 编译 + `IGraphQLEngine` 测试全绿；前端页面资源随全量 install 打包）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed, and Nop-platform phases do not write `none` without justification
- [x] skill loading verification: each phase scanned available skills, loaded all matching skills, read ALL mandatory docs listed in skill routing tables（路径列于 skill loading gate），selfcheck 无 anti-pattern
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 自动化会员专属券/生日礼包发放

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划以「手工调级 + 手工发券」覆盖运营即时权益发放，关闭 P26↔P32 循环 deferred 的手动路径。自动按等级/生日触发发券需触发引擎 + 券的会员级范围机制，属独立 successor。
- Successor Required: `yes`（触发条件：运营要求按等级/生日自动发券时，扩展 coupon 会员级范围 + 触发任务）

### 算法化用户画像/RFM/生命周期

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 归 P19 报表体系扩展；本计划画像=标签集合展示。
- Successor Required: `yes`（P19 启动用户分析报表时）

### 自建 IM/客服会话系统

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 超出商业基线；本计划以 Feedback + UserMessage 作代理展示。
- Successor Required: `no`（触发条件：业务需要实时客服会话时单独规划）

## Closure

<!-- 闭合审计必须由独立 subagent（不同 session/context）执行。实现 agent 不得自行填写本节。 -->

Status Note: 闭合审计 PASS（无 blocker）。三个 Phase 全部交付并 tick，验证 238 测试全绿 + 全量 install BUILD SUCCESS，所有新增 @BizMutation/@BizQuery 经 IGraphQLEngine 测试，无 in-scope 项降级为 deferred。MISSION_DRIVER「complete the entire plan」指令构成 Phase 1 ORM ask-first 显式授权。Closure audit Round 后补修：setUserLevel impl 补 `@Auth(roles="admin")` 与接口/约定一致（已重编译+测试）。

Closure Audit Evidence:

- Reviewer / Agent: 独立 subagent `ses_0f504d65fffe`（fresh session，general agent，非实现 agent）
- Evidence:
  - Verdict: **PASS**（0 blocker）
  - 独立重跑 `./mvnw -pl app-mall-service test`：`Tests run: 238, Failures: 0, Errors: 0, Skipped: 0` — BUILD SUCCESS（不轻信计划声明，实际重跑）
  - 逐项核验 live repo：Phase 1 ORM 实体+索引/唯一键 + 生成物 + Decision 入 user-and-address.md；Phase 2 七组方法接口+impl+@Auth 双层声明 + submit 封禁守卫(:204-207) + 6 ErrorCode + 反模式全无（无 @Transactional 叠加/@Inject private/raw RuntimeException/System.currentTimeMillis，跨实体经 I*Biz + IOrmTemplate fallback 带 rationale 注释）；Phase 3 NopAuthUser.view.xml 行动作+simple/@mutation + user-detail.page.yaml + segment.page.yaml + action-auth 三菜单项；docs 三处更新一致。
  - 12 项 Closure Gates：11 PASS + 「closure audit 不同 session」由本次审计满足。
  - 2 项非阻断 note：(1) setUserLevel impl 缺 @Auth（接口已守卫，本次已补修）；(2) roadmap 第 25 行 stale 文案（已随翻 done 清理）。

Follow-up:

- 无阻断 follow-up。
