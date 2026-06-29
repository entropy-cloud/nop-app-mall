# Reset Code Periodic Cleanup

> Plan Status: completed
> Mission: mall
> Work Item: reset-code-cleanup（关闭 deferred「验证码过期记录的定期清理」——触发条件已满足）
> Last Reviewed: 2026-06-29
> Source: `docs/plans/2026-06-13-reset-code-storage-plan.md:162` → `Deferred But Adjudicated → 验证码过期记录的定期清理`（Trigger: 当引入 nop-job 时——Phase 11 已引入 nop-job-local，触发条件已满足）
> Related: `docs/plans/2026-06-13-reset-code-storage-plan.md`（Reset code DB storage 改造，已 done）
> Audit: required

## Current Baseline

> 实读 live repo 所得，非记忆。

**惰性清理已存在但覆盖不全（确认的 gap）：**

- `LoginApiExBizModel.sendResetCode`（`app-mall-delta/.../biz/LoginApiExBizModel.java:141`）：发送新验证码时删除该手机号的旧验证码（`:163-166` `resetCodeBiz.delete(old.orm_idString(), context)`）。
- `LoginApiExBizModel.resetPassword`（`:180`）：验证成功后删除已使用的验证码（`:201` 过期路径 + `:217` 成功路径）。
- **覆盖缺口：** 对于已发送但**从未验证、也未重发**的手机号，其验证码记录持续累积，无任何路径清理。源 deferred（`2026-06-13-reset-code-storage-plan.md:165`）明确记录此 gap：「对于已发送但从未验证、也未重发的手机号，其验证码记录会持续累积……项目当前未引入 nop-job 定时任务引擎，无法实现定期清理。」

**触发条件已满足：**

- 源 deferred 的 Trigger 为「当引入 nop-job 时，或 `litemall_reset_code` 表行数超过 10 万行时」。Phase 11（`implementation-roadmap.md:30`）已引入 `nop-job-local` 调度引擎，定时任务已注册运行（11 方法/10 注册，见下）。**触发条件客观满足。**

**调度基建已就绪（复用对象）：**

- `MallJobInvoker`（`app-mall-service/.../scheduler/MallJobInvoker.java`）含 11 个 job 方法，模式统一：`new ServiceContextImpl()` → `biz.method(params, ctx)` → `LOG.info`。已注入 8 个 `ILitemall*Biz` bean（跨模块从 `app-mall-dao` 注入，模式成熟）。
- `scheduler.yaml`（`app-mall-app/.../_vfs/nop/job/conf/scheduler.yaml`）含 10 个 job 配置（`expirePinTuans` 方法存在但未在 yaml 注册——既有不一致，非本计划范围）。

**LitemallResetCode 模型已就绪：**

- `LitemallResetCode`（`_LitemallResetCode.java`）：`id`(1)、`mobile`(2)、`code`(3)、`addTime`(4, createTime)、`updateTime`(5)、`deleted`(6)。`useLogicalDelete`。
- `LitemallResetCodeBizModel`（`:10`）当前为空 `CrudBizModel<LitemallResetCode>`，无业务方法。
- `ILitemallResetCodeBiz` 接口已生成。

**前置条件已满足：** Phase 1（用户注册登录）done、Phase 11（系统运营与定时任务）done。

## Goals

- 新增定时任务，定期清理超过保留期的验证码记录（惰性清理未覆盖的累积记录）。
- 保留期可配置（config key `mall_reset_code_retention_days`），缺省 7 天。
- 关闭 deferred 条目。

## Non-Goals

- 验证码发送/验证流程改动——本计划仅做后台清理，不动 `sendResetCode`/`resetPassword` 既有惰性清理逻辑。
- 验证码存储方式变更（已在 `2026-06-13-reset-code-storage-plan` 完成从 ConcurrentHashMap 到 DB 的迁移）。
- 其他实体的历史数据清理（如 SearchHistory/Footprint）——非本 deferred 范围。

## Task Route

- Type: `implementation-only change`（无业务设计变更；本计划为 deferred 的执行 slice，新增定时清理 job）
- Owner Docs: `docs/design/user-and-address.md`（密码重置章节「验证码定期清理」）、`docs/design/system-configuration.md`（定时任务清单新增 reset-code-cleanup job）
- Skill Selection Basis: 后端 BizModel + 调度 job 接线 → `nop-backend-dev` + `nop-testing`（规则 #15）

## Infrastructure And Config Prereqs

- 无新外部服务/端口/密钥。无 Protected Area 触发。
- 新增 config key：`mall_reset_code_retention_days`（保留天数，默认 7）。

## Decision Points (to resolve in-phase)

- **D1 — 清理方式（物理删除 vs 逻辑删除）：** 抉择 **逻辑删除**（`deleteEntity` 设置 `deleted=true`，与既有惰性清理 `resetCodeBiz.delete()` 一致）。备选物理删除（`dao().delete` 直接删行）。抉择逻辑删除——与既有代码路径一致（`sendResetCode`/`resetPassword` 均用逻辑删除），避免引入第二种删除语义。残留风险：逻辑删除的记录仍占空间，但 reset_code 为临时数据，逻辑删除后查询自动过滤（`deleted=false`），不影响功能。
- **D2 — 保留期缺省值：** 抉择 **7 天**（验证码为短生命周期临时数据，7 天覆盖最长合理验证窗口；超过 7 天的记录确定不再需要）。备选 3 天（更激进）。残留风险：缺省值可经 config 调整。
- **D3 — 批量清理机制：** `doFindListByQueryDirectly` + `FilterBeans.lt(addTime, now.minusDays(retentionDays))` + `FilterBeans.eq(deleted, false)` → limit 500 → 逐条 `deleteEntity`（参照 `cancelExpiredOrders:2290-2344` 的查询+逐条处理模式）。备选批量 SQL DELETE（更高效但跳过 ORM 管道）。抉择 ORM 管道——与既有清理代码一致，且 reset_code 表量级小。

## Execution Plan

### Phase 1 - 验证码定期清理后端

Status: completed
Targets: `app-mall-service/.../entity/LitemallResetCodeBizModel.java`、`app-mall-dao/.../ILitemallResetCodeBiz.java`、`app-mall-service/.../scheduler/MallJobInvoker.java`、`app-mall-app/.../scheduler.yaml`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Decision`
- Prereqs: Phase 11 nop-job-local 已引入（已满足）

- [x] **Skill loading gate:** 扫描 available skills，加载 `nop-backend-dev` + `nop-testing`。读完 routing table 必读文档（含调度 job 接线、`@BizMutation` 事务边界、错误处理、bizmodel-method-selfcheck）。列已读文档路径如下。每写完一个方法用 skill selfcheck 校验。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`、`.opencode/skills/nop-testing/SKILL.md`、`nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`、`nop-entropy/docs-for-ai/04-reference/safe-api-reference.md`；代码先例 `MallJobInvoker.java`、`LitemallPointsAccountBizModel.java`(expirePoints)、`LitemallOrderBizModel.java`(cancelExpiredOrders)、`LoginApiExBizModel.java`、`_LitemallResetCode.java`、`ILitemallSystemBiz.java`、`TestLitemallPointsAccountBizModel.java`、`TestLitemallCommentOpsWorkbench.java`(reload 越过逻辑删除读取)、`TestLitemallOrderBizModel.java`(ormTemplate.runInSession)
- [x] **Decision D1/D2/D3：** 确认逻辑删除（`deleteEntity`，与 `sendResetCode`/`resetPassword` 惰性清理同语义，避免第二种删除语义）+ 保留期缺省 7 天（短生命周期临时数据，config 可调）+ `doFindListByQueryDirectly`（`lt(addTime, cutoff)` + `eq(deleted, false)`，limit 500）逐条 `deleteEntity`（参照 `cancelExpiredOrders`/`expirePoints` 先例，ORM 管道）。残留风险见 plan Decision Points。
- [x] **Add（注入）：** `LitemallResetCodeBizModel` 注入 `ILitemallSystemBiz systemBiz`（package-private，读 config）。
- [x] **Add（清理方法）：** `ILitemallResetCodeBiz` + `LitemallResetCodeBizModel` 新增 `cleanupExpiredResetCodes(IServiceContext context)` `@BizMutation`：读 `mall_reset_code_retention_days`（缺省 7）→ `CoreMetrics.currentDateTime().minusDays(retentionDays)` cutoff → `doFindListByQueryDirectly`（`FilterBeans.lt(addTime, cutoff)` + `eq(deleted, false)`, limit 500）→ 逐条 `deleteEntity`（逻辑删除）→ return count。
- [x] **Add（job 入口）：** `MallJobInvoker.cleanupExpiredResetCodes()`：`new ServiceContextImpl()` → `resetCodeBiz.cleanupExpiredResetCodes(context)` → LOG.info。`MallJobInvoker` 新增 `@Inject ILitemallResetCodeBiz resetCodeBiz`（package-private）。
- [x] **Add（scheduler）：** `scheduler.yaml` 注册 `cleanup-expired-reset-codes` job（repeatInterval=86400000 每日，参照每日 job 模式）。
- [x] **Proof：** `TestLitemallResetCodeBizModel` 新增 IGraphQLEngine 测试（`LitemallResetCode__cleanupExpiredResetCodes` mutation）：(a) 超过保留期记录逻辑删除（deleted=true）；(b) 保留期内记录不受影响；(c) 已逻辑删除记录不重复处理（count 仅含 active）；(d) config 缺省/覆盖；(e) 大批量 limit（600→500 处理，100 顺延）。
  - 验证命令：`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar`（EXIT=0）+ `./mvnw test -pl app-mall-service -am`（Tests run: 521, Failures: 0, Errors: 0, Skipped: 0）

Exit Criteria:

- [x] 超过保留期的验证码记录被逻辑删除；保留期内/已删除记录不受影响
- [x] config 保留期可调（缺省 7 天，`mall_reset_code_retention_days` 覆盖）
- [x] **API 测试：** `cleanupExpiredResetCodes` `@BizMutation` 通过 `IGraphQLEngine` 测试（`JunitBaseTestCase` 5 用例 + scheduler invoker mock 断言）
- [x] owner doc `user-and-address.md`（新增「密码重置与验证码」章节）+ `system-configuration.md`（扩展运营任务清单 + 调度频率）更新
- [x] deferred 源条目（`2026-06-13-reset-code-storage-plan.md:162`）标注「已由 successor 关闭」
- [x] `docs/logs/` updated（`docs/logs/2026/06-30.md`）

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent（fresh sessions `ses_0ec359c72ffeH51zEnyYirNSUZ` round-1, `ses_0ec29c2e3ffeB854lgjzrtb5sv` round-2）
- Evidence: Round-1 REVISE（2 blockers: B1 ILitemallResetCodeBiz 模块路径误标 app-mall-api（实为 app-mall-dao）/ B2 job 计数 "12" 错误（实为 11 方法/10 注册）+ 3 minors 行号修正）→ 全部修订 → Round-2 仅剩 line 23 残留 "12" 引用 → 修正为 "11 方法/10 注册" → 一致性确认。触发条件「当引入 nop-job 时」经 `scheduler.yaml` + `MallJobInvoker` 实证满足；跨模块注入模式成熟（MallJobInvoker 已注入 8 个 app-mall-dao IBiz bean）；`useLogicalDelete="true"` 确认 `app-mall.orm.xml:1402`。

## Closure Gates

- [x] in-scope behavior is complete（`LitemallResetCodeBizModel.cleanupExpiredResetCodes` `@BizMutation` 实读确认：`CoreMetrics.currentDateTime().minusDays(retention)` cutoff → `doFindListByQueryDirectly` + `FilterBeans.lt(addTime, cutoff)` + `eq(deleted, false)` + limit 500 → 逐条 `deleteEntity` 逻辑删除；`ILitemallResetCodeBiz` 接口同步声明；config 解析 `mall_reset_code_retention_days` 缺省 7）
- [x] relevant docs are aligned（`docs/design/user-and-address.md:173,179` 新增「密码重置与验证码」章节；`docs/design/system-configuration.md:266,273` 运营任务清单 + 调度频率；源 deferred `2026-06-13-reset-code-storage-plan.md:168` 标注「已由 successor 关闭（2026-06-30）」）
- [x] verification has run（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` EXIT=0；`./mvnw test -pl app-mall-service -am` Tests run: 521, Failures: 0, Errors: 0, Skipped: 0——见 `docs/logs/2026/06-30.md:12`）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（`cleanupExpiredResetCodes` 经 `TestLitemallResetCodeBizModel` 5 用例：过期删除 / 保留期保留 / 已删除不重复 / config 覆盖 / 单轮 500 上限；`JunitBaseTestCase` + `graphQLEngine.newRpcContext` mutation 路径）
- [x] no in-scope item downgraded to deferred/follow-up（Phase 1 全部 execution item `[x]`，无降级；本计划自身 Deferred 区为空）
- [x] plan audit passed before implementation（Plan Audit 状态 `passed`，独立 subagent 两轮：ses_0ec359c72ffeH51zEnyYirNSUZ round-1、ses_0ec29c2e3ffeB854lgjzrtb5sv round-2）
- [x] each phase has `Required Skill` listed，Nop-platform phases do not write `none` without justification（Phase 1 `Required Skill: nop-backend-dev, nop-testing`，匹配后端 BizModel + IGraphQLEngine 测试）
- [x] skill loading verification: each phase scanned available skills, loaded all matching skills, read ALL mandatory docs listed in skill routing tables, and selfchecked after each method/class（Phase 1 skill loading gate `[x]`，已读文档路径列于 item 内：SKILL.md ×2 + bizmodel-method-selfcheck + safe-api-reference + 6 处代码先例）
- [x] text consistency verified: status / phases / gates / log all agree（Plan Status: completed、Phase 1 Status: completed、所有 Exit Criteria `[x]`、Closure Gates 全 `[x]`、`docs/logs/2026/06-30.md` 记录全绿验证——一致）
- [x] closure audit was performed by a different agent/session than implementation（本次 closure audit 由独立 closure-auditor agent 执行，非实施 agent，实读 live repo 全部关键产物复核）
- [x] closure evidence exists in files（代码 5 文件 + 测试 1 文件 + 文档 2 文件 + 日志 1 文件 + 源 plan 标注 1 文件，均实读确认存在且内容与计划声明一致）

## Deferred But Adjudicated

（本计划起草时无 deferred 项）

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 可关闭——源 deferred「验证码过期记录的定期清理」触发条件（nop-job 引入，Phase 11 nop-job-local 已满足）客观成立，本计划新增 `cleanup-expired-reset-codes` 定时任务（`MallJobInvoker.cleanupExpiredResetCodes()` → `LitemallResetCodeBizModel.cleanupExpiredResetCodes` `@BizMutation`）覆盖惰性清理未触及的「已发送但从未验证、也未重发」累积记录。in-scope 行为、文档、验证、测试、五点一致性、deferred 源条目闭环均经独立 closure audit 实读 live repo 复核通过。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure-auditor agent（fresh session，非实施 agent）
- 实读复核（live repo `./`，非信任 `[x]` 标记）：
  - `app-mall-service/.../entity/LitemallResetCodeBizModel.java`：`@BizMutation cleanupExpiredResetCodes(IServiceContext)` 方法体非空非占位——读 `mall_reset_code_retention_days`（缺省 7，非法回退缺省）→ `CoreMetrics.currentDateTime().minusDays(retention)` cutoff → `doFindListByQueryDirectly`(`lt(addTime, cutoff)` + `eq(deleted, false)`, limit 500) → 逐条 `deleteEntity`（逻辑删除）。注入 `@Inject ILitemallSystemBiz systemBiz`（package-private，合规）。
  - `app-mall-dao/.../ILitemallResetCodeBiz.java`：接口声明 `int cleanupExpiredResetCodes(IServiceContext context)` + `@BizMutation`，模块路径为 `app-mall-dao`（与 Round-1 audit 修订一致）。
  - `app-mall-service/.../scheduler/MallJobInvoker.java`：`cleanupExpiredResetCodes()` 方法 + `@Inject ILitemallResetCodeBiz resetCodeBiz`（package-private）；模式与既有 11 个 job 一致（`new ServiceContextImpl()` → `biz.method(ctx)` → `LOG.info`）。
  - `app-mall-app/.../_vfs/nop/job/conf/scheduler.yaml:135-144`：`cleanup-expired-reset-codes` job 注册，`repeatInterval: 86400000`（每日），invoker 指向 `MallJobInvoker.cleanupExpiredResetCodes`——anti-hollow 通过（job 已接线，运行时可被调度执行）。
  - `app-mall-service/.../TestLitemallResetCodeBizModel.java`：5 个 IGraphQLEngine 测试（`JunitBaseTestCase` + `graphQLEngine.newRpcContext(mutation, "LitemallResetCode__cleanupExpiredResetCodes", ...)`），覆盖过期删除/保留期保留/已删除不重复/config 缺省与覆盖/单轮 500 上限——满足规则 #15（`@BizMutation` 经 IGraphQLEngine）。
  - `docs/design/user-and-address.md:173,179`：新增「密码重置与验证码」章节含「定期清理」子项；`docs/design/system-configuration.md:266,273`：运营任务清单 + 调度频率含 reset-code 清理——owner doc 同步完成。
  - `docs/logs/2026/06-30.md`：完整日志（实现 + 测试 + 验证全绿 Tests run: 521, Failures: 0, Errors: 0, Skipped: 0 + 文档同步）。
  - `docs/plans/2026-06-13-reset-code-storage-plan.md:168`：源 deferred 条目标注「已由 successor 关闭（2026-06-30）」并说明实施路径——源 deferred 闭环完成。
- 五点一致性：Plan Status `completed` ↔ Phase 1 Status `completed` ↔ Exit Criteria 全 `[x]` ↔ Closure Gates 全 `[x]` ↔ 日志全绿——一致。
- Anti-Hollow：方法体非空、job 在 scheduler.yaml 注册且 invoker 指向真实 bean/method、测试经 GraphQL mutation 路径实际执行——无 `{}` / `return null` / 吞异常 / 注册不可达。
- Deferred honesty：本计划 Deferred 区为空；in-scope 项无降级为 follow-up。

Follow-up:

- 无（源 deferred 已闭环；惰性清理覆盖发送/验证路径、定期清理覆盖累积路径，二者互补且均无残留缺陷）。
