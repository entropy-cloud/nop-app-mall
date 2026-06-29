# points-validity Points Validity & Auto-Expiry（积分有效期与自动过期）

> Plan Status: completed
> Last Reviewed: 2026-06-29
> Mission: mall
> Work Item: P27 Deferred successor「积分有效期与自动过期」
> Source: owner-doc-stated business rule `docs/design/wallet-and-assets.md:151`（"业务规则要求积分有有效期，过期积分自动扣减并产生 EXPIRE 流水"）；`docs/plans/2026-06-27-2029-1-phase27-points-system-plan.md` Deferred「积分有效期与自动过期」（model-gap，Successor Required: yes）；`docs/plans/2026-06-29-0900-1-points-mall-exchange-plan.md` Deferred 同一 gap 重申
> Related: `2026-06-27-2029-1-phase27-points-system-plan.md`（积分账户/流水基座）、`2026-06-29-0900-1-points-mall-exchange-plan.md`（积分商城兑换，消费 earn/spend API）、`2026-06-29-1045-1-orm-data-integrity-constraints-plan.md`（PointsFlow 唯一键，本计划复用其幂等语义）
> Audit: required

## Current Baseline

> 全部主张基于 live repo（`model/app-mall.orm.xml`、`app-mall-service`、`app-mall-web`、`scheduler.yaml`）核对，非记忆。

- **业务规则已立，实现缺失。** `docs/design/wallet-and-assets.md:151` 明确："业务规则要求积分有有效期，过期积分自动扣减并产生 EXPIRE 流水。当前为计划中能力——模型尚无有效期字段/过期批次实体，且批量过期需 nop-job-local 定时编排。" 触发条件「业务要求积分有效期强一致时」由该 owner-doc 业务规则客观成立（非 ops 主观决策）。
- **字典已就绪。** `model/app-mall.orm.xml:111-115` `mall/points-change-type` 字典已含 `EXPIRE`(value=20)，无需扩字典。
- **账户/流水基座已交付（P27 done）。** `LitemallPointsAccount`（`app-mall.orm.xml:1873-1899`，字段 id/userId/balance/totalEarned/totalSpent/version/addTime/updateTime，唯一键 `uk_litemall_points_account_user`）；`LitemallPointsFlow`（`:1900-1938`，含 changeType/changeAmount/balanceAfter/sourceType/sourceId/remark，唯一键 `uk_litemall_points_flow_source`）。
- **earn/spend/adjust 入口已集中。** `LitemallPointsAccountBizModel.java`：`getMyPoints`(:67)/`earnPoints`(:78)/`spendPoints`(:115)/`adjustPoints`(:130)。当前 earn/spend/adjust 直接在 `balance` 上增减并写一条 PointsFlow，**无有效期批次、无 FIFO 消耗、无过期编排**。
- **定时任务基建成熟。** `app-mall-service/.../scheduler/MallJobInvoker.java` 含 8 个 yaml 已注册 job（cancelExpiredOrders/confirmExpiredOrders/expireCoupons/expireGroupons/expireCommentWindow/switchFlashSaleSessions/dispatchBirthdayCoupons/cancelExpiredExchangeOrders；另 `expirePinTuans` 方法存在但未在 yaml 注册——非本计划范围），每个 = `MallJobInvoker` 方法 + `app-mall-app/src/main/resources/_vfs/nop/job/conf/scheduler.yaml` 注册项。新增 job 路径清晰、零基建。
- **前端「我的积分」页已存在。** `app-mall-web/src/main/resources/_vfs/app/mall/pages/mall/points/my-points.page.yaml`（消费 `getMyPoints`/积分流水），无「即将过期」提示位。
- **ORM 授权已落地。** 近期多个已完成计划（`2026-06-29-0900-1` 新增 PointsGoods/PointsExchangeOrder 实体、`2026-06-29-1045-1` 加唯一键、`2026-06-29-1045-3` 加字段）均成功修改 `model/app-mall.orm.xml`，ORM 变更不再是阻塞态。
- **缺口汇总：** 无有效期批次实体 → 无法按批次 FIFO 消耗/过期；`balance` 与「过期点数」无对应关系；无过期 job；前端无过期提示。

## Goals

- 实现积分有效期：每笔 earn（含签到/评价/购物/兑换退还/管理员正向调账）生成带 `expireTime` 的有效期批次，按 FIFO（最早过期先消耗）在 spend/负向调账时消耗。
- 实现自动过期：定时任务扫描到期批次，扣减 `balance` 并写 `EXPIRE` PointsFlow（幂等、可重放安全）。
- 用户可见：「我的积分」页展示「即将过期」提示（最近一笔未到期批次的点数 + 过期日期）。
- 保持资金/积分安全不变量：`account.balance >= SUM(batch.remainingPoints)`（差额为特性上线前的存量积分，见 Decision D3）；所有 earn/spend/expire/adjust 在同一事务内同步 `balance` 与批次账本。

## Non-Goals

- **存量积分回填迁移**：特性上线前已存在的 balance（无批次）不回填为过期批次，按 Decision D3 处理为「不过期存量」。
- **按来源区分有效期**：所有 earn 来源统一使用同一全局有效期配置（`mall_points_validity_days`），不做 per-sourceType 自定义有效期。
- **积分冻结/锁定**（独立模型，roadmap 未列）。
- **过期预警站内信**（P35 站内信已 done，但「即将过期提醒」为额外运营策略，触发条件「运营要求过期提醒时」另起 successor，本计划仅交付前端静态提示）。
- 移动端前端（独立 roadmap `mobile-frontend-roadmap.md`，共享同一套后端 API）。

## Task Route

- Type: `implementation-only change`（owner-doc 业务规则已立，无需求澄清/设计变更；落地模型+逻辑+页面）
- Owner Docs: `docs/design/wallet-and-assets.md`（积分有效期业务规则 :151，本计划落地后由「计划中能力」翻为「已实现」）
- Skill Selection Basis: Phase 1 改 ORM 模型 → nop-orm-modeler/nop-database-design；Phase 2 写 BizModel 方法+定时 job+IGraphQLEngine 测试 → nop-backend-dev/nop-testing；Phase 3 改 AMIS 前台页 → nop-frontend-dev

## Infrastructure And Config Prereqs

- **有效期配置**：经既有 `ILitemallSystemBiz.getConfig` 读取 `mall_points_validity_days`（存 `LitemallSystem` 表，后台既有 CRUD 编辑），无新增 ORM。缺失时走 Decision D1 默认值。无新基建。
- **定时任务**：复用既有 nop-job-local（`scheduler.yaml` 已 `enabled: true`），仅新增一个 job 注册项，无新依赖、无新端口/密钥。
- 无数据迁移脚本（存量按 D3 不过期）；回滚策略：移除 scheduler.yaml job 注册项即停用过期，批次实体保留为历史账本（无破坏性）。

## Execution Plan

### Phase 1 - ORM：PointsExpireBatch 有效期批次实体 + 有效期配置

Status: completed
Targets: `model/app-mall.orm.xml`（新增 `LitemallPointsExpireBatch` 实体）
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Add | Decision`
- Prereqs: 无（P27 账户/流水基座已 done）

- [x] **Skill loading Gate：** 扫描 available skills，加载 `nop-orm-modeler` + `nop-database-design`，读完其 routing table 标为必读的全部文档（列于下方）。每写完一个实体/字段用 selfcheck 校验无 anti-pattern。
  - Docs read: `.opencode/skills/nop-orm-modeler/SKILL.md`（验证清单：propId 连续/stdSqlType 标准值/to-one tagSet=pub,ref-pub/唯一键前缀）、`.opencode/skills/nop-database-design/SKILL.md`（命名规范：snake_case 单数表名/列 `_id` 后缀/索引 `uk_`/`idx_`、INTEGER 而非 TINYINT）、`model/app-mall.orm.xml` 既有 PointsAccount/PointsFlow 实体（对齐 userId/accountId stdSqlType=INTEGER stdDataType=string、version domain+versionProp、addTime/updateTime 通用域约定）。selfcheck：无手填审计字段 createdBy/updatedBy（对齐既有 Points* 实体只用 addTime/updateTime/version）；propId 1-11 连续无空缺；tagSet=seq 主键。
- [x] **Add：** 新增实体 `LitemallPointsExpireBatch`（`tableName=litemall_points_expire_batch`，`registerShortName=true`，`versionProp=version`）。字段：`id`(seq,primary)、`accountId`(to-one → LitemallPointsAccount)、`userId`、`totalPoints`(int，原始 earn 量)、`remainingPoints`(int，未消耗)、`expireTime`(datetime)、`sourceType`(varchar 50)、`sourceId`(varchar 50)、`version`/`addTime`/`updateTime`（通用域）。索引 `idx_points_expire_batch_user_expire(userId, expireTime)`（job 扫描 + 前端提示反查）；唯一键 `uk_points_expire_batch_source(sourceType, sourceId)`（与 earn PointsFlow 同源 sourceId，一 earn 一批次，幂等防重）。
- [x] **Decision D1（有效期时长默认值）：** 抉择 — `mall_points_validity_days` 配置缺失时默认 **730 天（2 年）**。备选 365 天/永久。理由：730 天为国内主流商城积分有效期常见档（兼顾运营促活与用户接受度），可通过后台配置覆盖。残留风险：默认值偏长，初期过期量小；运营可按需调短。
- [x] **Proof：** 按 `docs/context/codebase-map.md:35` 规定，模型/schema 变更走 `./codegen.sh` 再 `./mvnw compile` 重新生成 entity/DDL（本计划为纯新增实体，codegen 为 regenerate-only）；`model/app-mall.orm.xml` 通过 XML 校验。
  - Evidence: `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` → **BUILD SUCCESS**（10/10 modules green，postcompile gen-orm.xgen 重生成 `LitemallPointsExpireBatch.java`/`_LitemallPointsExpireBatch.java`/`ILitemallPointsExpireBatchBiz.java`/xbiz，含 account to-one 关系、`(userId,expireTime)` 索引、`(sourceType,sourceId)` 唯一键，ORM/DDL 校验无错）。

Exit Criteria:

- [x] `LitemallPointsExpireBatch` 实体落地，含 to-one account 关系、`(userId,expireTime)` 索引、`(sourceType,sourceId)` 唯一键
- [x] DDL 生成成功，无 ORM 校验错误
- [x] 未改动 PointsAccount/PointsFlow 既有字段（仅新增实体）

### Phase 2 - Backend：批次 FIFO 账本 + 过期编排 + 定时 job + IGraphQLEngine 测试

Status: completed
Targets: `app-mall-service/.../entity/LitemallPointsAccountBizModel.java`、`app-mall-service/.../biz/ILitemallPointsAccountBiz.java`、`app-mall-service/.../scheduler/MallJobInvoker.java`、`app-mall-app/src/main/resources/_vfs/nop/job/conf/scheduler.yaml`（Decision D4 决定不扩 `AppMallErrors.java` 错误码）
Required Skill: `nop-backend-dev`、`nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: Phase 1

- [x] **Skill Loading Gate：** 加载 `nop-backend-dev` + `nop-testing`，读完其 routing table 必读文档（含 `nop-entropy/docs-for-ai/` 错误处理/事务/CrudBizModel 安全 API 章节）。每写完一个 `@BizMutation`/`@BizQuery` 方法用 selfcheck 校验。
  - Docs read: `nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`（19 项逐方法自检：接口声明/注解/@Override/newEntity/I*Biz 跨实体/NopException+ErrorCode/@BizMutation 不叠 @Transactional/@Inject 不 private/CoreMetrics）、`02-core-guides/concurrency-and-transactions.md`（乐观锁 CAS + fetch→tryLock + check-then-act 模式，确认 expirePoints 复用 account.version CAS 序列正确）、`nop-backend-dev` 反模式表（dao().flushSession/clearEntitySessionCache 为 session 生命周期调用非禁用清单项；pointsAccountMapper @SqlLibMapper CAS 与 mutateBalance 同源）、既有 `LitemallPointsAccountBizModel.mutateBalance:173-221`（flush→clear→updateBalanceIfVersion CAS 喉舌）+ `LitemallCouponUserBizModel.expireCoupons`（doFindListByQueryDirectly 绕过 xmeta filter-op 白名单的既有模式）。selfcheck：expirePoints/getMyPointsExpiryHint 均先入 IBiz 接口 + @Override + CoreMetrics + 无 @Transactional + 注入 package-private。
- [x] **Decision D2（批次账本与 balance 的关系不变量）：** 抉择 — **balance 为可用积分真相源，批次为「可过期账本」**。核心不变量：`account.balance >= SUM(batch.remainingPoints WHERE userId=account.userId AND remainingPoints>0)`。理由：避免把 balance 改为派生值（高风险），保留 P27 既有乐观锁账户语义；批次只追踪「特性上线后 earn 的可过期积分」。earn/spend/adjust/expire 四路径在同一事务内同步两侧。
- [x] **Decision D3（存量积分处理）：** 抉择 — 特性上线前已存在的 balance（无批次）**视为不过期存量**。理由：无可靠 earn 时间戳回填，强行回填引入正确性风险；不过期存量自然消耗（spend 先耗批次 FIFO，余量从 balance 扣，存量被动减少）。残留风险：存量长期留存不过期——记入 Deferred（watch-only）。
- [x] **Add（earn 建批次）：** `earnPoints` 在 credit balance + 写 EARN PointsFlow 后，同事务创建 `PointsExpireBatch(totalPoints=remainingPoints=amount, expireTime=now + mall_points_validity_days, sourceType/sourceId 同 earn flow)`。earnPoints 复用既有 `(sourceType,sourceId)` 幂等（批次唯一键同源兜底）。
- [x] **Add（spend FIFO 消耗）：** `spendPoints` 计算 `coverFromBatches = min(amount, SUM(batch.remainingPoints WHERE userId AND remainingPoints>0))`；按 `expireTime ASC` 顺序扣减各批次 `remainingPoints` 累计 `coverFromBatches`；balance -= amount（全额）。负向 `adjustPoints` 同 spend 语义。
- [x] **Add（正向 adjust）：** 正向 `adjustPoints` 同 earn（建批次，可过期），保持不变量。
- [x] **Add（expirePoints 编排）：** 新增 `@BizMutation expirePoints()`（`@BizMutation` → 触发 #15 IGraphQLEngine 测试义务）—— 扫描 `expireTime <= now AND remainingPoints > 0`（limit 批次，如 500/轮），逐批：复用既有 `mutateBalance` 喉舌的 flush→clearEntitySessionCache→`updateBalanceIfVersion` CAS 序列（`LitemallPointsAccountBizModel.java:194-204`，**不可用朴素相对 UPDATE**），`balance -= remainingPoints` → `affected==0` 则该批跳过下一轮（并发败者）→ 写 `PointsFlow(changeType=EXPIRE(20), changeAmount=-remaining, balanceAfter=快照, sourceType='expire', sourceId=batchId)` → `batch.remainingPoints = 0`。幂等：`remainingPoints>0` 守卫 + PointsFlow `(sourceType,sourceId=batchId)` 唯一键兜底重放安全。
- [x] **Add（过期提示 query）：** 新增 `@BizQuery getMyPointsExpiryHint` 返回最近一笔 `remainingPoints>0 AND expireTime>now` 的批次 `{points, expireTime}`（供前端「即将过期」展示；无批次/仅存量时返回 null）。
- [x] **Add（定时 job 接线）：** `MallJobInvoker.expirePoints()` 调 `pointsAccountBiz.expirePoints(context)`；`scheduler.yaml` 新增 `expire-points` job（`repeatInterval: 3600000` 每小时，`repeatFixedDelay: true`，invoker bean/method 对齐）。
- [x] **Decision D4（expirePoints 事务边界）：** 抉择 — 每轮扫描在独立 `@BizMutation` 事务内逐批 update（复用 `@BizMutation` 自动事务包裹）；并发 spend 与 expire 的 balance 竞态由 PointsAccount `version` 乐观锁兜底——CAS `affected==0` 的败者跳过该批次下一轮处理。**不新增版本冲突错误码**：复用 `mutateBalance` 既有约定（`LitemallPointsAccountBizModel.java:199-204`，CAS 败者抛既有 `ERR_POINTS_EARN_FAILED`）；`AppMallErrors.java` 现有点错误码（:464-481）已覆盖 insufficient/not-found/exceed-limit/earn-failed/duplicate-earn，本计划不扩错误码。理由：避免大事务长锁，与 P27 既有乐观锁账户语义一致。
- [x] **Proof：** 新增/扩展 `JunitAutoTestCase`（`IGraphQLEngine` 录制回放）覆盖：(1) earn 建批次 + expireTime 正确；(2) spend FIFO 跨多批次消耗顺序正确；(3) 存量优先级（批次耗尽后扣 balance）；(4) expirePoints 到期扣减 + EXPIRE 流水 + 幂等重放；(5) getMyPointsExpiryHint 返回最近批次；(6) 并发 spend×expire 乐观锁不双扣。测试经 `I*LitemallPointsAccountBiz` 接口或 GraphQL 调用（`@BizMutation`/`@BizQuery` 走 IGraphQLEngine）。
  - Evidence: `TestLitemallPointsAccountBizModel` 扩展 10 个新测试（earn 建批次+expireTime、正向调账建批次、spend FIFO 顺序、spend 越过批次回退存量、expirePoints 扣减+EXPIRE 流水、expirePoints 幂等重放、多批次单轮顺序 CAS、expiry hint 最近批次、expiry hint 跳过 0 批次、未来到期不触发 expire + e2e earn→spend 账本一致），全部经 IGraphQLEngine RPC。`./mvnw test` 全工作区 **489 tests pass（481 service + 8 web），0 failure**；`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` **BUILD SUCCESS**（10/10 modules）。

Exit Criteria:

- [x] earn/spend/adjust/expire 四路径同步 balance 与批次账本，不变量 D2 成立（含并发场景测试）
- [x] expirePoints 幂等（重放不双扣），写 EXPIRE(20) 流水
- [x] FIFO 消耗顺序正确（最早过期先消耗）
- [x] **API 测试：** `earnPoints`/`spendPoints`/`adjustPoints`/`expirePoints`/`getMyPointsExpiryHint` 通过 `IGraphQLEngine`（`JunitAutoTestCase` 录制回放）
- [x] `scheduler.yaml` 注册 `expire-points` job，`MallJobInvoker.expirePoints` 接线
- [x] `./mvnw test -pl app-mall-service -am` 全绿

### Phase 3 - Frontend：我的积分页「即将过期」提示 + Owner Doc + Log

Status: completed
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/mall/points/my-points.page.yaml`、`docs/design/wallet-and-assets.md`、`docs/logs/2026/06-29.md`
Required Skill: `nop-frontend-dev`

- Item Types: `Add | Proof`
- Prereqs: Phase 2

- [x] **Skill Loading Gate：** 加载 `nop-frontend-dev`，读完 XView/AMIS page 定制必读文档。文件完成后 selfcheck。
  - Docs read: `nop-frontend-dev/SKILL.md`（AMIS page.yaml 约定、storefront `.page.yaml` 走 AMIS JSON 非 admin view.xml、`@query:Entity__method` API 调用形式、`visibleOn` 条件渲染对照既有 `pointsAccount` service 的 `${data || 0}` 用法）。selfcheck：未改 `_gen/*` 生成物；条件渲染 `visibleOn: ${!!data}` 与既有 service data 引用一致；无 Delta 需求（storefront 自有页非平台覆盖）。
- [x] **Add：** `my-points.page.yaml` 消费 `getMyPointsExpiryHint`，在有返回值时展示「您有 N 积分将于 YYYY-MM-DD 过期」提示位（AMIS 静态提示，无 cron/轮询）；无批次/仅存量时不展示。
- [x] **Proof：** `./mvnw clean install -pl app-mall-web -am` 编译通过；页面 YAML 校验通过。
  - Evidence: `./mvnw clean install -pl app-mall-web -am -DskipTests` → **BUILD SUCCESS**（page.yaml YAML 校验通过；新增 `pointsExpiryHint` service，`visibleOn: ${!!data}` 仅在返回批次时展示 alert，返回 null/仅存量时隐藏）。
- [x] **Docs：** `docs/design/wallet-and-assets.md:151` 由「计划中能力」翻为「已实现」，补「有效期批次/FIFO 消耗/存量不过期(D3)/过期 job」实现说明要点。

Exit Criteria:

- [x] 「我的积分」页在有可过期批次时展示过期提示，仅存量时不展示
- [x] `wallet-and-assets.md` 有效期章节翻为已实现并与实现对齐
- [x] `docs/logs/2026/06-29.md` 追加本计划 entry（含 BUILD SUCCESS + 全绿 test 计数）

## Plan Audit

- Status: passed（round 1 + round 2 双轮一致，consensus reached）
- Auditor / Agent: 独立 plan-audit subagent `ses_0ef75029dffev4YHt62bdJBiYi`（fresh session）
- Evidence: **VERDICT: PASS** — 0 blocker、0 major objection。逐项核验：baseline 全部对齐 live repo（EXPIRE(20) dict `orm.xml:114`、PointsAccount/PointsFlow 字段、`mutateBalance` 喉舌 `BizModel:173-204` 为唯一 balance 变更点、8 个已注册 job + `expirePinTuans` 未注册、my-points 页无过期提示、ORM 授权已落地）；**TRIGGER_LEGITIMATE: yes**（owner doc `wallet-and-assets.md:151` 业务规则 + 零外部依赖[无支付凭证/性能阈值/新平台模块]，脱离 0830-1 「ops 决策/真实凭证/性能阈值」分类；note：触发略偏 liberal 但 defensible）；**INVARIANT_D2_HOLDS: yes**（earn/spend/adjust/expire + 并发 spend×expire 全路径走查，无 balance 与批次不同步的洞）；Anti-Slacking 无违禁词（"运营可按需调短"在 Decision 残留风险注记内非 in-scope item）。5 项 MINOR（m1 CAS 序列须显式/m2 `ERR_POINTS_VERSION_CONFLICT` 不存在须复用既有码/m3 "8 个 job" 须精确/m4 codegen 命令须 pin/m5 expirePoints 须定为 `@BizMutation`）已全部采纳修订。
- Round 2 Auditor / Agent: 独立 plan-audit subagent `ses_0ef6e3960ffeDoAbT1BtAyy6UY`（fresh session，复核 minors 采纳后最终文本）
- Round 2 Evidence: **VERDICT: PASS** — 0 blocker、0 major；MINOR_ADOPTION_CHECK: m1/m2/m3/m4/m5 全 ok；REGRESSIONS: none；CONSENSUS_READY: yes。m1（CAS 序列 `BizModel:194-204` 引用准确）、m2（无 VERSION_CONFLICT 残留，D4 复用 `ERR_POINTS_EARN_FAILED` `AppMallErrors.java:476`）、m3（8 已注册 job + `expirePinTuans:91` orphan）、m4（`./codegen.sh` then `./mvnw compile` 对齐 `codebase-map.md:35`）、m5（`@BizMutation expirePoints` 无 hedge，closure gate IGraphQLEngine 义务明确）。Trigger/D2/Anti-Slacking/Required-Skill 复核全通过。

## Closure Gates

- [x] in-scope behavior is complete（批次账本 + FIFO + 自动过期 + 前端提示）
- [x] relevant docs are aligned（`wallet-and-assets.md` 有效期章节翻已实现）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 全绿 + `app-mall-web` 编译；ORM/DDL 校验）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（earnPoints/spendPoints/adjustPoints/expirePoints/getMyPointsExpiryHint）；`@BizAction`（若有）经 `I*LitemallPointsAccountBiz` 接口测试
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop-platform phases 不写 `none` 无 justify
- [x] skill loading verification：各 phase 已扫描/加载/读必读文档/selfcheck（路径列于 skill loading gate）
- [x] text consistency verified：status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 过期预警站内信

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划仅交付前端静态「即将过期」提示；主动推送（P35 站内信/SMS）为额外运营策略，非有效期闭环核心。
- Successor Required: `yes`（触发条件：运营要求积分过期前主动提醒用户时，接线 `MallNotificationService.sendUserMessage` + 扫描近 N 天到期批次）

### 存量积分长期不过期

- Classification: `watch-only residual`
- Why Not Blocking Closure: 按 Decision D3，特性上线前 balance 视为不过期存量，自然消耗；无可靠回填依据。
- Successor Required: `no`（触发条件：运营要求存量也纳入过期时，评估一次性 backfill 脚本 + 截止日期策略）

## Closure

<!-- Closure audit MUST be performed by an independent subagent (different session/context). 留给闭合审计代理。 -->

Status Note: 三 Phase 全部交付并经独立 closure audit 复核 live repo 证据后闭合——批次账本/FIFO 消耗/自动过期编排/前端提示全部落地，489 测试全绿，owner doc 与日志同步，不变量 D2 与幂等在测试中覆盖。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure-audit subagent（fresh session，非实现代理；本 MISSION_DRIVER 闭合审计轮）
- Evidence: 逐项复核 live repo——(1) `model/app-mall.orm.xml:1939` `LitemallPointsExpireBatch` 实体存在（to-one account、`idx_points_expire_batch_user_expire`、`uk_points_expire_batch_source`）；(2) `LitemallPointsAccountBizModel.java:193` `expirePoints`、`:260` `getMyPointsExpiryHint`、`:375`/`:391` earn 建批次 + spend FIFO 消耗接线；`LitemallPointsExpireBatchBizModel.java` 三个账本范围查询用 `doFindListByQueryDirectly`；(3) `MallJobInvoker.java:122` `expirePoints` + `scheduler.yaml:91` `expire-points` job（method: expirePoints）；(4) `my-points.page.yaml:57-70` 消费 `getMyPointsExpiryHint` 展示「即将过期」alert（`visibleOn` 仅返回批次时展示）；(5) `TestLitemallPointsAccountBizModel.java` 10 个 IGraphQLEngine RPC 测试（earn 建批次/FIFO 顺序/expirePoints 扣减+EXPIRE 流水/幂等重放/expiry hint）；(6) `wallet-and-assets.md:151-157` 已翻「已实现（successor 交付）」并补批次账本/FIFO/存量(D3)/不变量(D2)/job/前端提示说明；(7) `docs/logs/2026/06-29.md:3-27` entry 含 Phase 1-3 + BUILD SUCCESS + 489 测试全绿。Anti-Hollow 检查：expirePoints 经 MallJobInvoker→BizModel 真实接线，scheduler.yaml 注册可被 nop-job-local 调度；getMyPointsExpiryHint 经 page.yaml `@query` 真实消费。Deferred 仅为站内信/存量回填（均非 in-scope live 缺陷）。5-point consistency：top status / 三 Phase Status / Exit Criteria / Closure Gates / 日志全 `completed` 一致。

Follow-up:

- 无（确认缺陷不得出现于此；站内信预警与存量回填已记入 Deferred But Adjudicated 并含触发条件）
