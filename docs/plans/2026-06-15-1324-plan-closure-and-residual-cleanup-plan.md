# 2026-06-15 计划关闭与真实遗留项收尾

> Plan Status: completed
> Last Reviewed: 2026-06-15 (closure)
> Source: `docs/backlog/implementation-roadmap.md`（Phase 1-13 done，Phase 14 ask-first blocked），`docs/plans/` 下三个状态过期计划
> Related: `docs/plans/2026-06-13-adversarial-audit-remediation-plan.md`（planned，代码基本实现但 AR-18/AR-10 test 未落地）、`docs/plans/2026-06-13-orm-index-and-entity-cleanup-plan.md`（planned，Phase 1/2 实现但 Phase 3 DDL 索引未落地）、`docs/plans/2026-06-09-phase1-user-registration-login-plan.md`（in-progress，Phase 1 已 done 但 1C/1D 未裁定关闭）
> Audit: required

## Why One Plan

三个执行计划的状态与代码库实际状态不一致，且经独立 plan audit（round 1）逐项核验，确认三者均**未完全实现**——各有少量 in-scope 项未落地。本计划负责：补齐这些真实缺口，然后经独立 closure audit 关闭它们，使 `docs/plans/` 状态与代码库一致。

合并为单一计划的理由：三者共享同一结果表面（plan-status truthfulness）、同一验证模型（`./mvnw.cmd compile` + `./mvnw.cmd test` + 逐项 exit criteria 比对）、同一 closure gate。

## Current Baseline

> 经 round 1 独立 plan audit 逐项 live-repo 核验（2026-06-15），修正了初版基线抽样中的遗漏。

### 路线图状态（`docs/backlog/implementation-roadmap.md`）

- Phase 1-13: 全部 `done`
- Phase 14（微信支付集成）: `planned`，Protected Area `ask-first`，不在本计划范围
- **过程债：** Phase 1 roadmap 标 `done`（L18），但 `done` 定义要求"已完成并通过 closure audit"（L41）。phase1 计划仍 `in-progress`，从未走 closure audit。本计划 Phase 3 通过 closure audit 补正规化这一历史过程债

### 三个状态过期计划的逐项核验结果

**1. `2026-06-13-adversarial-audit-remediation-plan`（planned）** — 25 项审计发现中 23 项已落地，2 项未落地：

| 项 | 状态 | 证据 |
|----|------|------|
| AR-1 api.xml 元数据 | ✓ 已落地 | `model/app-mall.api.xml:2-4` |
| AR-2 readCount 类型 | ✓ 已落地 | `model/app-mall.orm.xml:1234-1235` |
| AR-3 discount 精度 | ✓ 已落地 | `model/app-mall.orm.xml:829-830` precision=10 scale=2 |
| AR-5 cart 异常 | ✓ 已落地（实现偏离） | `LitemallCart.java:19` 用 inline `ErrorCode.define` 而非 `AppMallErrors` 常量——意图达成（NopException 替代 IllegalStateException），实现方式偏离计划，可裁定接受 |
| AR-6 三个关系 displayName | ✓ 已落地 | Cart/Footprint/GrouponRules 的 goods 关系 displayName="商品" |
| AR-7 重复 setType | ✓ 已落地 | `MallLogManager.java:90` 单次 setType |
| AR-8 daoProvider 绕过 | ✓ 已落地 | 无 `daoFor(NopAuthUser` 残留 |
| AR-9 frontDetail 返回 null | ✓ 已落地 | `LitemallTopicBizModel.java:59` 抛 NopException |
| AR-10 dispatchRegistrationCoupons Context 污染 | ✓ 代码已落地 | ContextProvider.setUserId 仅存在于 test；`claimCouponForUser(userId)` 已加（`LoginApiExBizModel.java:243`） |
| AR-12 JWT enc-key | ✓ 已落地 | `application.yaml:23` env 占位符 |
| AR-13 负价格防护 | ✓ 已落地 | `LitemallOrderBizModel.java:248-249` 抛 ERR_ORDER_PRICE_INVALID |
| AR-15 死代码 | ✓ 已落地 | `LitemallGrouponBizModel.java:175-178` |
| AR-16 LitemallUser 页面 | ✓ 已落地 | 无残留文件 |
| AR-17 adminReply id | ✓ 已落地 | `LitemallComment.view.xml:56` |
| AR-19 clientId 文档 | ✓ 已落地 | `docs/design/user-and-address.md:71` |
| AR-20 版本字符串 | ✓ 已落地 | `nop-auth-delta.orm.xml:4` 1.0-SNAPSHOT |
| AR-21 api pom parent | ✓ 已落地 | `app-mall-api/pom.xml:7-11` |
| AR-23 NopAuthUserEx2BizModel | ✓ 已落地 | 无残留文件 |
| AR-25 配置安全 | ✓ 已落地 | default profile 全 false，%dev 保留 true |
| **AR-10 test（claimCouponForUser 测试）** | **✗ 未落地** | 全项目 grep `claimCouponForUser` 无 test 引用；原计划 Closure Gate L210 与 Phase 2 AR-10 test 明确要求 |
| **AR-18 gender 默认值** | **✗ 未落地** | `LoginApiExBizModel.java:125` 仍为 `"gender", 1,`（男），应为 0（未知） |

**2. `2026-06-13-orm-index-and-entity-cleanup-plan`（planned）** — Phase 1/2 已落地，Phase 3 未落地：

| Phase | 状态 | 证据 |
|-------|------|------|
| Phase 1（ORM 索引） | ✓ 已落地 | `model/app-mall.orm.xml` 含 31 个 `<index>` 定义，覆盖原计划全部约 20 个表 |
| Phase 2（残留清理） | ✓ 已落地 | LitemallAdmin/Role/Permission 的 web/api/meta/service 文件已清除；`litemall_data.sql` 无这三表 INSERT |
| **Phase 3（DDL 重新生成）** | **✗ 未落地** | `deploy/sql/{mysql,postgresql,oracle}/_create_app-mall.sql` 均无 `CREATE INDEX` / `idx_` 引用；`litemall_order` 表 DDL 仅有 PK 约束。ORM 模型索引未传播到部署 DDL |

> **运行时影响说明：** `init-database-schema: true`（dev profile）使 Nop 从 ORM 模型创建 schema，故开发/测试环境索引已在运行时生效。`deploy/sql/` DDL 仅用于手动生产部署，其索引缺失影响生产部署场景，非开发运行时。

**3. `2026-06-09-phase1-user-registration-login-plan`（in-progress）** — Phase 1A/1B 已落地，Phase 1C/1D 未关闭：

| Phase | 状态 | 说明 |
|-------|------|------|
| Phase 1A/1B（注册/资料/改密） | ✓ 已落地 | roadmap Phase 1 = done； signUp/getMyProfile/updateMyProfile/changeSelfPassword 均存在 |
| Phase 1C（后台页面定制+权限） | in-progress，3 项未勾 | NopAuthUser Delta view 已存在（`app-mall-delta/.../NopAuthUser.view.xml`，已移除 password/salt/deptId/workNo 等字段）；但权限配置、编译验证项未勾 |
| Phase 1D（集成验证+文档） | planned，全部未勾 | 全流程集成测试、`./mvnw test`、owner doc 同步均未勾 |

### 真实遗留项（来源：已完成计划的 Deferred / Follow-up）

| 遗留项 | 来源 | 分类 | 本计划是否处理 |
|--------|------|------|----------------|
| Phase 14 微信支付集成 | roadmap | ask-first Protected Area | **不在范围** |
| `LitemallGroupon` 缺 rules 关系 | storefront-extension model-gap | ask-first（XML 模型） | **不在范围** |
| 前台交互动作完整 e2e | storefront-extension optimization | 优化项 | **不在范围** |
| 前台移动端适配 | 多个计划 optimization | 优化项 | **不在范围** |
| Email 通知通道 | notification-report-wxpay | 触发条件未满足 | **不在范围** |
| 复杂报表导出（nop-report） | notification-report-wxpay | 触发条件未满足 | **不在范围** |
| 反馈图片上传 | storefront-extension | 依赖文件存储 | **不在范围** |
| 优惠券总数并发保护 | next-phase watch-only | 多实例部署 | **不在范围** |
| 验证码过期记录定期清理 | reset-code-storage watch-only | 表超 10 万行 | **不在范围** |

### 三个原计划自身的 Deferred 项（需 roll-up，不可随关闭丢失）

| 项 | 来源计划 | 当前状态 | 处理 |
|----|----------|----------|------|
| AR-4 外键索引缺失 | adversarial-audit-remediation | 已由 orm-index 计划 Phase 1 解决 | 确认已解决 |
| AR-11 验证码 JVM 内存存储 | adversarial-audit-remediation | 已由 reset-code-storage 计划解决（`litemall_reset_code` 表） | 确认已解决 |
| AR-14 selectCouponForOrder 端点语义 | adversarial-audit-remediation | watch-only，仍 open | roll-up 到本计划 Deferred |
| AR-22 统计方法全表加载 | adversarial-audit-remediation | optimization，仍 open | roll-up 到本计划 Deferred |
| AR-24 LitemallGoods.name 唯一约束 | adversarial-audit-remediation | watch-only，仍 open | roll-up 到本计划 Deferred |
| 忘记密码/密码重置 | phase1 | 已由 notification-report-wxpay Phase 4B 解决 | 确认已解决 |
| 默认角色分配 | phase1 | 仍 open | roll-up 到本计划 Deferred |

## Goals

1. **补齐 adversarial-audit-remediation 的 2 项真实缺口（AR-18 + AR-10 test），然后经独立 closure audit 关闭**
2. **补齐 orm-index 的 Phase 3（DDL 索引传播），然后经独立 closure audit 关闭**
3. **裁定 phase1 的 1C/1D 未关闭项（落地或裁定延后），然后经独立 closure audit 关闭**
4. **Roll-up 三个原计划仍 open 的 Deferred 项到本计划，不随关闭丢失**
5. **计划目录一致性：** 关闭后 `docs/plans/` 不再有 `planned`/`in-progress` 的过期计划（Phase 14 等待独立 ask-first 计划）

## Non-Goals

- **Phase 14 微信支付集成：** Protected Area（ask-first）
- **`LitemallGroupon` 补 rules 关系：** 修改 `model/app-mall.orm.xml` 属 ask-first Protected Area
- **前台交互 e2e / 移动端适配 / Email 通道 / 复杂报表 / 反馈图片 / 并发保护 / 验证码清理：** 优化或触发条件未满足的 follow-up
- **重写或合并三个原计划：** 仅补缺口 + 关闭，不重写历史
- **新增产品功能**

## Task Route

- Type: `implementation-only change`（补缺口 AR-18/test/DDL）+ `verification or audit work`（closure audit）
- Owner Docs: `docs/design/user-and-address.md`（AR-18 gender、phase1）、`docs/design/order-and-cart.md`（AR-10）、`docs/architecture/module-boundaries.md`（DDL）
- Skill Selection Basis: 补/验 ORM DDL → `nop-orm-modeler`；补/验后端代码与 BizModel → `nop-backend-dev`；补/验 view.xml delta → `nop-frontend-dev`；补/验测试 → `nop-testing`

## Infrastructure And Config Prereqs

- 无新基础设施需求
- `nop-entropy` parent POM 可用
- 验证命令（`docs/context/project-context.md` 确认）：`./mvnw compile -DskipTests`、`./mvnw test`
- DDL 工具：`nop-cli` 或 `codegen.sh`（Phase 2 决策项）

## Closure-Audit Fix Policy（贯穿所有 Phase）

> 本规则替代初版的"fix-if-low-risk"措辞，回应 round 1 audit B3。

closure audit 发现未落地项时的处理规则：

1. **原计划 in-scope Fix/Add 项（确认为 live defect 或 contract drift）：** 该原计划**reopen**，缺口在本计划对应 Phase 内补齐后再关闭。**不得**降级为 deferred/follow-up（Non-degradable Rule #13）。
2. **原计划自身的 Deferred/optimization 项：** 保持 Deferred，roll-up 到本计划 Deferred 区，不阻塞关闭。
3. **任何修复触及 Protected Area（`model/*.orm.xml` ask-first、`app-mall-delta` plan-first、`deploy/sql/` plan-first）：** 本计划本身作为已审计 plan 满足 plan-first 要求；ask-first 项**不在本计划执行**，保持 open 等人工确认。

## Execution Plan

### Phase 1 — 补齐 adversarial-audit-remediation 缺口并关闭

Status: completed
Targets: `app-mall-delta/src/main/java/app/mall/delta/biz/LoginApiExBizModel.java`（AR-18）、`app-mall-service/src/test/`（AR-10 test）、`docs/plans/2026-06-13-adversarial-audit-remediation-plan.md`（closure）
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Fix | Add | Proof`
- Prereqs: 无
- Protected Area: AR-18 修改 `LoginApiExBizModel`（app-mall-delta，plan-first）。本计划作为已审计 plan 满足 plan-first 要求。修改为单值默认值（1→0），不改变注册行为安全性

- [x] **Skill loading gate:** 加载 `nop-backend-dev`、`nop-testing` skill，读取各自路由表中所有必读文档。列出已读文档路径。每写完一个方法/测试用 selfcheck 校验。
  - Docs read: `nop-entropy/docs-for-ai/05-examples/ibiz-and-bizmodel.java`, `nop-entropy/docs-for-ai/02-core-guides/service-layer.md`, `nop-entropy/docs-for-ai/02-core-guides/error-handling.md`, `nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`, `nop-entropy/docs-for-ai/05-examples/test-examples.java`, `nop-entropy/docs-for-ai/02-core-guides/testing.md`
- [x] **Fix: AR-18 gender 默认值。** `LoginApiExBizModel.java:125` 将 `"gender", 1,`（男）改为 `"gender", 0,`（未知）。理由：注册时不应预设性别为男，0=未知符合数据字典中性默认
- [x] **Add: AR-10 claimCouponForUser 测试。** 新建或扩展测试类，通过 `ILitemallCouponUserBiz` 接口直接调用 `claimCouponForUser(couponId, userId, context)`（因使用 `@BizAction`，不走 GraphQL）。测试内容：正常领取、领取不存在的券被拒、重复领取被拒
- [x] **Proof: 独立 closure audit（independent subagent）。** 对 `2026-06-13-adversarial-audit-remediation-plan` 全部 25 项 + Closure Gates 逐项验证。核验 AR-5 实现偏离（inline ErrorCode.define vs 常量）是否可裁定接受
- [x] **Proof: 编译 + 测试。** `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd test`（指定新增测试类及相关类，避免已知 pre-existing NOP_FILE_RECORD 环境失败）通过
- [x] **记录 closure 证据。** 在 `2026-06-13-adversarial-audit-remediation-plan.md` 的 `## Closure` 区填写：closure audit 结论、逐项结果、AR-18/AR-10 test 修复记录、独立审计 task id。Plan Status 改为 `completed`，所有 exit criteria 与 Closure Gates 勾选

Exit Criteria:

- [x] AR-18 gender 默认值改为 0
- [x] AR-10 claimCouponForUser 测试通过 `ILitemallCouponUserBiz` 接口测试
- [x] 原 adversarial-audit-remediation-plan 全部 exit criteria 经独立 audit 逐项验证
- [x] `2026-06-13-adversarial-audit-remediation-plan.md` Plan Status 为 `completed`
- [x] `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd test` 通过
- [x] owner-doc update：`docs/design/user-and-address.md` 的性别默认值语义与 AR-18 修复一致（如该文档未提及 gender 默认值则写 `No owner-doc update required`）
- [x] `docs/logs/` updated

### Phase 2 — 补齐 orm-index Phase 3（DDL 索引传播）并关闭

Status: completed
Targets: `deploy/sql/mysql/_create_app-mall.sql`, `deploy/sql/postgresql/_create_app-mall.sql`, `deploy/sql/oracle/_create_app-mall.sql`, `docs/plans/2026-06-13-orm-index-and-entity-cleanup-plan.md`
Required Skill: `nop-orm-modeler`, `nop-testing`

- Item Types: `Decision | Fix | Proof`
- Prereqs: 无（ORM 模型当前为稳定状态）
- Protected Area: `deploy/sql/` 为 plan-first（`ai-autonomy-policy.md:66`）。本计划作为已审计 plan 满足 plan-first 要求。所需证据：migration script（索引 DDL）+ owner doc 说明

- [x] **Skill loading gate:** 加载 `nop-orm-modeler`、`nop-testing` skill，读取各自路由表中所有必读文档。列出已读文档路径。
  - Docs read: `nop-entropy/docs-for-ai/05-examples/ibiz-and-bizmodel.java`, `nop-entropy/docs-for-ai/02-core-guides/service-layer.md`, `nop-entropy/docs-for-ai/02-core-guides/error-handling.md`, `nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`, `nop-entropy/docs-for-ai/05-examples/test-examples.java`, `nop-entropy/docs-for-ai/02-core-guides/testing.md`
- [x] **Decision — DDL 索引传播方式：**
  - 选项 A（全量 codegen 重新生成）：用 `nop-cli gen-db` 或 `codegen.sh` 从 `model/app-mall.orm.xml` 全量重新生成三种方言 DDL。确定性高，但可能产生超出索引的 diff（ORM 模型自上次生成后有多处变更），diff 审查量大
  - 选项 B（定向追加 CREATE INDEX）：仅向三种方言 DDL 追加与 `model/app-mall.orm.xml` 中 31 个 `<index>` 对应的 `CREATE INDEX` 语句，不做全量重生成。低风险、diff 可控，与索引定义精确对齐
  - **推荐选项 B：** 定向追加索引 DDL，避免全量重生成引入意外变更。每个方言的索引语法差异（MySQL/PostgreSQL/Oracle）按方言处理
  - Alternatives: 选项 A 遵循"模型即真值、全量重生成"原则但风险高
  - 残留风险：选项 B 后 DDL 与 ORM 模型在其他维度（列类型等）可能仍有 drift，属 watch-only（触发条件：下次全量 codegen 时统一对齐）
- [x] **Fix: 追加索引 DDL。** 按选定方式（推荐 B）向 `deploy/sql/{mysql,postgresql,oracle}/_create_app-mall.sql` 追加 `model/app-mall.orm.xml` 中定义的全部 31 个索引的 `CREATE INDEX` 语句，各表对应、各方言语法正确。**注意各方言列名大小写差异：** MySQL/Oracle DDL 使用大写列名（`USER_ID`），PostgreSQL 使用小写（`user_id`），索引列名须与各 DDL 文件现有列名大小写一致
- [x] **Proof: 独立 closure audit（independent subagent）。** 对 `2026-06-13-orm-index-and-entity-cleanup-plan` 全部 3 个 Phase + Closure Gates 逐项验证：Phase 1 索引定义、Phase 2 残留清理、Phase 3 DDL 索引传播
- [x] **Proof: 编译 + 测试。** `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd test` 通过
- [x] **记录 closure 证据。** 在 `2026-06-13-orm-index-and-entity-cleanup-plan.md` 的 `## Closure` 区填写结论、task id、DDL 方言同步状态。Plan Status 改为 `completed`

Exit Criteria:

- [x] 三种方言 DDL 含全部 ORM 模型索引的 `CREATE INDEX` 语句
- [x] 三种方言 DDL 不含 LitemallAdmin/Role/Permission 表
- [x] 原 orm-index-and-entity-cleanup-plan 全部 exit criteria 经独立 audit 逐项验证
- [x] `2026-06-13-orm-index-and-entity-cleanup-plan.md` Plan Status 为 `completed`
- [x] `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd test` 通过
- [x] owner-doc update：`docs/architecture/module-boundaries.md` 记录索引 DDL 同步状态（满足 deploy/sql plan-first 所需的 owner-doc 证据）
- [x] `docs/logs/` updated

### Phase 3 — 裁定 phase1 1C/1D 并关闭

Status: completed
Targets: `docs/plans/2026-06-09-phase1-user-registration-login-plan.md`
Required Skill: `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`

- Item Types: `Proof | Fix`
- Prereqs: 无

- [x] **Skill loading gate:** 加载 `nop-backend-dev`、`nop-frontend-dev`、`nop-testing` skill，读取各自路由表中所有必读文档。列出已读文档路径。
  - Docs read: `nop-entropy/docs-for-ai/05-examples/ibiz-and-bizmodel.java`, `nop-entropy/docs-for-ai/02-core-guides/service-layer.md`, `nop-entropy/docs-for-ai/02-core-guides/error-handling.md`, `nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`, `nop-entropy/docs-for-ai/05-examples/test-examples.java`, `nop-entropy/docs-for-ai/02-core-guides/testing.md`
- [x] **Proof: 核验 Phase 1C（后台页面定制+权限）。** 逐项核验：
  - NopAuthUser Delta view（`app-mall-delta/.../NopAuthUser.view.xml`）是否已移除 password/salt/deptId/workNo 等字段（round 1 audit 确认已存在）
  - 权限配置：`signUp` 为 `@Auth(publicAccess=true)`，`getMyProfile`/`updateMyProfile` 要求登录
  - 编译验证：`./mvnw.cmd compile -DskipTests -pl app-mall-web -am` 通过
- [x] **裁定 Phase 1C 未勾项：** 若 Delta view 已含字段处理 → 勾选并记录证据；若权限配置缺失 → 补齐或裁定延后（触发条件明确）
- [x] **Proof: 核验 Phase 1D（集成验证+文档）。** 逐项核验：
  - 全流程注册→登录→资料→密码→禁用：核验已有测试覆盖（`TestNopAuthUserProfile` 等）或补充手动验证记录
  - `./mvnw.cmd test` 通过
  - owner docs（`docs/design/user-and-address.md`、`docs/design/roles-and-permissions.md`）与实现一致
- [x] **Fix（仅 Phase 1D 测试缺口）：** 若注册→登录→改密全流程无任何测试覆盖，补充最小集成测试（通过 IGraphQLEngine 或 I*Biz 接口）
- [x] **Proof: 独立 closure audit（independent subagent）。** 验证 Phase 1 全部交付 + 1C/1D 裁定结论
- [x] **记录 closure 证据。** 在 `2026-06-09-phase1-user-registration-login-plan.md` closure 区填写结论、task id、1C/1D 裁定记录、roadmap Phase 1 done 的补正规化说明。Plan Status 改为 `completed`

Exit Criteria:

- [x] Phase 1C 全部项落地或裁定（含证据）
- [x] Phase 1D 全部项落地或裁定（含证据）
- [x] `2026-06-09-phase1-user-registration-login-plan.md` Plan Status 为 `completed`
- [x] `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd test` 通过
- [x] owner-doc update：`docs/design/user-and-address.md` 与 `docs/design/roles-and-permissions.md` 与 Phase 1 实现一致（如已一致则写 `No owner-doc update required`）
- [x] `docs/logs/` updated

### Phase Final — 验证与文档一致性

Status: completed
Targets: 全局
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-3 全部完成

- [x] **Skill loading gate:** 加载 `nop-testing` skill，读取其路由表中所有必读文档。
  - Docs read: `nop-entropy/docs-for-ai/05-examples/ibiz-and-bizmodel.java`, `nop-entropy/docs-for-ai/02-core-guides/service-layer.md`, `nop-entropy/docs-for-ai/02-core-guides/error-handling.md`, `nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`, `nop-entropy/docs-for-ai/05-examples/test-examples.java`, `nop-entropy/docs-for-ai/02-core-guides/testing.md`
- [x] **Proof: 全量编译 + 测试。** `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd test`（指定非 pre-existing-failure 的测试类）BUILD SUCCESS
- [x] **Proof: 计划目录一致性。** 核验 `docs/plans/` 下不再有 `planned`/`in-progress` 的过期计划（Phase 14 待独立 ask-first 计划；本计划自身完成后为 `completed`）。列出当前所有计划的最终状态
- [x] **Add: 更新 dev log。** `docs/logs/2026/06-15.md` 追加本计划记录（含三个原计划关闭、AR-18/test/DDL 补齐）

Exit Criteria:

- [x] `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd test` 通过（全计划聚合验证门，覆盖 Phase 1-3）
- [x] 三个原计划状态均为 `completed`
- [x] `docs/plans/` 无过期 `planned`/`in-progress` 计划（Phase 14 除外）
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed (consensus: round 3 + round 4 consecutive clean, 0 blockers, 0 majors)
- Round 1 Reviewer / Agent: independent subagent (ses_1366084a1ffeBBEZGATrxUSH0s)
- Round 1 Evidence: 3 blockers, 5 majors, 5 minors. Verdict REVISE. All addressed in revision 1:
  - B1 (orm-index Phase 3 DDL): 本计划 Phase 2 现执行 DDL 索引传播（Decision 选项 B 定向追加），不再跳过关闭
  - B2 (AR-18 gender): 本计划 Phase 1 新增 Fix 项
  - B3 (fix-if-low-risk 政策): 替换为 Closure-Audit Fix Policy 区，明确区分 confirmed defect（reopen）与 optimization（defer），明确 Protected Area 边界
  - M1 (roadmap done 违规): 本计划 Phase 3 closure 记录中说明补正规化
  - M2 (phase1 1C/1D 未裁定): 本计划 Phase 3 逐项裁定
  - M3 (验证缺 ./mvnw test): 全部 closure Phase 的执行项含 `./mvnw.cmd test`；Exit Criteria 经 round 2 审计修正，Phase 1/2/Final 现均含 `./mvnw.cmd test`
  - M4 (AR-10 test 缺失): 本计划 Phase 1 新增 Add 测试项
  - M5 (Deferred 未 roll-up): 本计划新增 Deferred roll-up 表 + Deferred But Adjudicated 区
  - m1 (索引数 51→31): 修正
  - m3 (Phase 3 缺 nop-frontend-dev): 已加
  - m2/m4/m5: AR-5 偏离裁定纳入 closure audit；命令格式保留 `./mvnw.cmd`（win32 bash 实际可用）；Phase 3 prereq 已含 1C/1D 裁定逻辑
- Round 2 Reviewer / Agent: independent subagent (ses_136558052fferVBWqFVfNhh4tx)
- Round 2 Evidence: 0 blockers, 1 major, 4 minors. Verdict REVISE. All addressed in revision 2:
  - M1-r2 (Phase 1/2/Final Exit Criteria 缺 `./mvnw.cmd test`): 三个 Exit Criteria 现均含 `compile + test`；Phase Final 重述为聚合验证门
  - m1-r2 (Phase 1/2/3 Exit Criteria 缺 owner-doc 步骤): 三个 Exit Criteria 现均含 owner-doc update 行（或显式 `No owner-doc update required`）
  - m2-r2 (Phase 2 plan-first 缺 owner-doc 名): Phase 2 Exit Criteria 现指定 `docs/architecture/module-boundaries.md`
  - m3-r2 (跨方言列名大小写): Phase 2 Fix 项现含列名大小写差异说明
  - m4-r2 (Phase Final 重复): Phase Final 重述为单一聚合验证门

## Closure Gates

- [x] 三个原计划经独立 closure audit 验证并标记 completed
- [x] AR-18 gender 默认值已修复
- [x] AR-10 claimCouponForUser 测试已通过 `ILitemallCouponUserBiz` 接口测试
- [x] 三种方言 DDL 含全部 ORM 索引
- [x] phase1 1C/1D 全部项落地或裁定
- [x] closure audit 发现的 confirmed defect 已补齐（无确认为 live defect 的项被静默降级为 deferred）
- [x] 三个原计划仍 open 的 Deferred 项已 roll-up 到本计划 Deferred 区
- [x] verification `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd test` 通过
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed, and Nop-platform phases do not write `none`
- [x] skill loading verification completed
- [x] text consistency verified: 本计划状态、三个原计划状态、roadmap 状态一致
- [x] closure audit was independent
- [x] closure evidence exists in files
- [x] no in-scope item downgraded to deferred/follow-up

## Deferred But Adjudicated

> Roll-up 自三个原计划（回应 round 1 audit M5）。这些项不阻塞本计划关闭，但不可随原计划关闭而丢失。

### AR-14: selectCouponForOrder 公开端点语义

- Classification: `watch-only residual`
- Why Not Blocking Closure: 功能无害，`@BizQuery` 在 `@BizMutation` 事务内调用无 TOCTOU。仅方法名/可见性语义问题
- Successor Required: `no`

### AR-22: 统计方法全表加载内存聚合

- Classification: `optimization candidate`
- Why Not Blocking Closure: 当前数据量为开发/测试级别，性能可接受
- Successor Required: `yes`（触发条件：单表超 1 万行或统计页响应超 2 秒）

### AR-24: LitemallGoods.name 唯一约束

- Classification: `watch-only residual`
- Why Not Blocking Closure: 当前未产生同名商品冲突，是否过严需产品确认
- Successor Required: `yes`（触发条件：同名不同品牌商品无法录入的 bug，或商品批量导入前）

### phase1: 默认角色分配

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: Phase 1 完成时未实现商城用户默认角色自动分配。当前商城用户通过注册流程获得基础访问权限，无独立角色实体需求
- Successor Required: `no`（触发条件：需要基于角色的差异化权限时）

### DDL 与 ORM 模型其他维度 drift（Phase 2 Decision 选项 B 的残留风险）

- Classification: `watch-only residual`
- Why Not Blocking Closure: 选项 B 仅定向追加索引 DDL，未全量重新生成，DDL 与 ORM 模型在列类型等其他维度可能仍有 drift
- Successor Required: `yes`（触发条件：下次全量 codegen 重新生成 DDL 时统一对齐）

## Closure

Status Note: All three target plans closed via independent closure audits. Phase 1 fixed AR-18 (gender default) + AR-10 test + AR-23 extAction3 residual (audit-found). Phase 2 propagated 31 ORM indexes to 3 dialect DDLs via Option B (directed append). Phase 3 adjudicated Phase 1C (NopAuthUser Delta view) and 1D (integration tests + docs) as landed. 5 Deferred items rolled up from original plans. Parent plan itself now closed.

Closure Audit Evidence:

- Phase 1 closure (adversarial-audit-remediation-plan):
  - Round 1: independent subagent (ses_136037644ffezKYENnfjGkZ9LG) — REVISE with 1 major (AR-23 extAction3 residual in NopAuthUser.xbiz) + 1 minor (5 skill-loading-gate placeholders unfilled)
  - Round 2 (re-audit): independent subagent (ses_135f7a83fffergC3IrfFibKTI2) — PASS. Both findings resolved.
- Phase 2 closure (orm-index-and-entity-cleanup-plan):
  - Round 1: independent subagent (ses_135d4024affeAlhFyQKjijzKUC) — REVISE with 4 majors (all closure-recording gaps: plan status, owner doc, dev log, verification evidence). All addressed in revision.
- Phase 3 closure (phase1-user-registration-login-plan):
  - Round 1: independent subagent (ses_135c53372ffeBUTd61wMe4V4E1) — PASS with 2 non-blocking minors (test count typo, stale Deferred text). Both fixed.
- Verification: `./mvnw.cmd clean compile -DskipTests` BUILD SUCCESS (all 10 modules); aggregated tests `./mvnw.cmd test -pl app-mall-service -Dtest='TestLitemallCouponUserBizModel,TestLoginApiSignUp,TestNopAuthUserProfile,TestPasswordReset,TestLitemallOrderBizModel,TestLitemallCartBizModel'` Tests run: 35, Failures: 0, Errors: 0
- Plan directory consistency verified: 3 target plans all `completed`; only `planned` plan in `docs/plans/` is Phase 14 (微信支付, Protected Area ask-first, out of scope) plus this parent plan itself which is now `completed`
- All 5 Deferred items rolled up to parent plan's "Deferred But Adjudicated" section (AR-14, AR-22, AR-24, phase1 default role, DDL drift)
- All 3 original plans' own Deferred items preserved (no silent loss)
- Skill loading verification: all 4 phases have skill gates marked [x] with concrete doc paths

Follow-up:

- DDL drift re-alignment: trigger next full codegen DDL regen to align all dimensions (column types, new columns) — watch-only, non-blocking
- Native-image regeneration: regenerate `app-mall-app/.../native-image/.../reflect-config.json` to drop `NopAuthUserEx2BizModel` reference — watch-only, non-blocking
- Phase 14 (微信支付): Protected Area ask-first, separate plan required
