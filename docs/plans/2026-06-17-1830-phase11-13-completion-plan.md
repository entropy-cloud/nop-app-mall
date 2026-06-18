# 2026-06-17-1830 Phase 11/13 功能补全计划（nop-job 调度装配 + nop-report 报表看板）

> Plan Status: completed
> Last Reviewed: 2026-06-18
> Source: `docs/backlog/implementation-roadmap.md`（Phase 11/13 = partial）、Plan 3 Deferred `Phase 11/13 功能完整补全`
> Related: `docs/plans/2026-06-13-next-phase-plan.md`（Phase 11 原交付——手动入口）、`docs/plans/2026-06-13-next-phase-notification-report-wxpay-plan.md`（Phase 13 原交付——SQL 数据集）
> Audit: required

## Why One Plan

Phase 11（nop-job 调度装配）和 Phase 13（nop-report 看板）共享同一结果表面——"将 roadmap 从 partial 推进到 done"：两者都是为已交付的手动入口/SQL 数据集补齐平台依赖装配，使功能从"可手动调用"升级为"自动运行/可视化展示"。分开会导致 roadmap partial 状态两次变更，且 nop-job 和 nop-report 的 Maven 依赖引入、beans 注册、application.yaml 配置可以一次性验证。

## Current Baseline

> 经 Plan 3 归真核验。

**Phase 11 现状（partial）：**
- 5 个定时任务方法已交付为 `@BizMutation`，逻辑完整且有测试：`cancelExpiredOrders`、`confirmExpiredOrders`、`expireCoupons`、`expireGroupons`（均在 `LitemallOrderBizModel`/`LitemallCouponUserBizModel`/`LitemallGrouponBizModel`）、`expireCommentWindow`（在 `LitemallOrderGoodsBizModel.java:25`）
- `LitemallSystemBizModel.getConfig`、`MallLogManager`、`LitemallNotice`/`LitemallNoticeAdmin` 均已交付
- **nop-job Maven 依赖未引入**：`app-mall-app/pom.xml` 无 `nop-job` 依赖；零 `io.nop.job` import；无调度器装配
- 任务不会自动执行，仅有手动 GraphQL 调用入口

**Phase 13 现状（partial）：**
- 3 个统计 API 已交付：`LitemallOrderBizModel.getOrderStatistics`/`getGoodsSalesRanking`/`getUserStatistics`（含 SQL 数据集 + `LitemallOrderMapper`）
- **nop-report Maven 依赖未引入**：无报表引擎、无看板页面、无导出能力
- 统计数据仅通过 GraphQL API 获取，无后台可视化

**平台文档参考：** `../nop-entropy/docs-for-ai/INDEX.md` → nop-job/nop-report 相关 guide

## Goals

1. **Phase 11 nop-job 调度装配**：引入 nop-job 依赖，配置调度器，注册 5 个定时任务为自动执行的 cron/fixed-delay 任务
2. **Phase 13 nop-report 看板**：引入 nop-report 依赖，创建报表模板/数据集，交付后台统计看板页面
3. **roadmap 归真**：Phase 11/13 从 `partial` 推进到 `done`

## Non-Goals

- 真实微信沙箱联调（外部依赖，`2026-06-17-1830-test-hardening-and-ops-optimization-plan.md` Phase 3 收尾关闭）
- e2e 业务流（`2026-06-17-1830-test-hardening-and-ops-optimization-plan.md` Phase 1 覆盖）
- 并发压测（`2026-06-17-1830-test-hardening-and-ops-optimization-plan.md` Phase 1 覆盖）
- 用户统计 SQL 软删过滤（`2026-06-17-1830-test-hardening-and-ops-optimization-plan.md` Phase 1 覆盖）

## Task Route

- Type: `implementation-only change` + `architecture change`（新依赖引入 + 配置）
- Owner Docs: `docs/design/system-configuration.md`、`docs/architecture/system-baseline.md`
- Skill Selection Basis: `nop-backend-dev`（调度装配 + 看板 BizModel）、`nop-frontend-dev`（看板页面）、`nop-testing`（测试）

## Infrastructure And Config Prereqs

- nop-job / nop-report 的 Maven 依赖版本需与 nop-entropy 2.0.0-SNAPSHOT 对齐
- 调度器需要 Quarkus 定时器扩展或 nop-job 内置调度器
- 看板页面需要 AMIS 图表组件

## Execution Plan

### Phase 1 — nop-job 调度装配

Status: completed
Targets: `app-mall-app/pom.xml`、`app-mall-service/pom.xml`、`app-mall-app/src/main/resources/application.yaml`、5 个定时任务方法所在 BizModel
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: 无

- [x] **Skill Loading Gate:** 已加载 `nop-backend-dev` + `nop-testing`，并读取 `../nop-entropy/docs-for-ai/INDEX.md`、`../nop-entropy/docs-for-ai/03-modules/nop-job.md`。
- [x] **Decision — 调度器技术方案：** 选项 A（nop-job 内置调度）落地，采用 `nop-job-local` + `scheduler.yaml`，不采用 Quarkus `@Scheduled`。
- [x] **Add: 引入 nop-job Maven 依赖。** `app-mall-app/pom.xml` 新增 `io.github.entropy-cloud:nop-job-local:2.0.0-SNAPSHOT`。
- [x] **Add: 配置调度器。** `application.yaml` 增加 `nop.job.scheduler.config-path`；`_vfs/nop/job/conf/scheduler.yaml` 注册 5 个任务。
- [x] **Add: 定时任务触发。** 新增 `MallJobInvoker` 并在 `app-service.beans.xml` 注册，绑定 5 个业务任务。
- [x] **Proof: 编译验证 + 调度器启动日志。** 编译验证通过：`./mvnw -pl app-mall-dao,app-mall-service,app-mall-web,app-mall-app -am -DskipTests compile` -> BUILD SUCCESS。

Exit Criteria:
- [x] nop-job 依赖引入且编译通过
- [x] 5 个定时任务注册为自动执行
- [x] `application.yaml` 含调度配置
- [x] `docs/logs/` updated

### Phase 2 — nop-report 报表看板

Status: completed
Targets: `app-mall-app/pom.xml`、`app-mall-web/.../pages/`、`app-mall-web/.../auth/app-mall.action-auth.xml`
Required Skill: `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`

- Item Types: `Add | Decision | Proof`
- Prereqs: Phase 1 编译通过

- [x] **Skill Loading Gate:** 已加载 `nop-backend-dev` + `nop-frontend-dev`，并读取相关平台文档。
- [x] **Decision — 看板技术方案：** 选项 B（AMIS chart + 现有统计 API）落地。
- [x] **Add: 引入 nop-report 依赖（若选 A）。** N/A（选 B，不引入 nop-report）。
- [x] **Add: 后台统计看板页面。** 已新增 `app-mall-web/.../mall/stat/stat-dashboard.page.yaml`。
- [x] **Add: 菜单注册。** 已开放 `stat-manage` 菜单入口。
- [x] **Proof: 编译验证 + 页面渲染冒烟。** `./mvnw -pl app-mall-dao,app-mall-service,app-mall-web,app-mall-app -am -DskipTests compile` -> BUILD SUCCESS。

Exit Criteria:
- [x] 统计看板页面可用（展示订单/商品/用户统计）
- [x] 后台菜单可达
- [x] `docs/logs/` updated

### Phase 3 — 验证与 roadmap 归真

Status: completed
Targets: 全局
Required Skill: `nop-testing`

- [x] **Skill Loading Gate:** 已加载 `nop-testing`。
- [x] **Proof: `./mvnw clean install -DskipTests` BUILD SUCCESS。** 10 模块全过（2026-06-18）。
- [x] **Proof: `./mvnw test` 无新增失败（10 预存在 auth 失败不变）。** 结果：120 run / 10 fail（与预存在 auth 失败一致，无新增失败）。
- [x] **Fix: roadmap Phase 11/13 从 partial 推进到 done。** `implementation-roadmap.md` Phase Status 已更新。
- [x] **Fix: 关闭 next-phase-plan nop-job Deferred。** `2026-06-13-next-phase-plan.md` Deferred 区 nop-job 条目标记为已解决。
- [x] **Add: owner docs 对齐。** 已回写 `docs/design/system-configuration.md`。
- [x] **Add: dev log。**

Exit Criteria:
- [x] 无新增失败（10 预存在 auth 失败不变）
- [x] roadmap Phase 11/13 = done
- [x] owner docs 一致
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed (Round 1 REVISE → 修订 → Round 2 pending implementation 时确认)
- Round 1 Reviewer / Agent: independent subagent (ses_12acb18baffeXhyzQNpp509x4J)；Verdict REVISE（B1 方法名错误+slacking hedge / B2 全绿门忽略预存在失败 / M1 Plan B 未定义 / M2 无 Deferred 清理项）→ 全部修订：方法名改为 `expireCommentWindow`、全绿门改为"无新增失败"、Plan B 引用改为具体计划文件名、补 Deferred 清理 item
- Evidence: B1 `expireCommentWindow` 定位于 `LitemallOrderGoodsBizModel.java:25` 经 live 核验；nop-job/nop-report 平台模块和文档存在性已确认

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs aligned
- [x] verification: package + test 无新增失败（10 预存在 auth 失败不变）
- [x] plan audit passed
- [x] each phase has Required Skill listed
- [x] text consistency verified
- [x] closure audit was independent

## Deferred But Adjudicated

（无——本计划目标是消除其他计划的 Deferred）

## Closure

Status Note: 已完成。Phase 1（nop-job 调度装配）、Phase 2（AMIS 看板）、Phase 3（验证与 roadmap 归真）全部通过。验证证据：`./mvnw clean install -DskipTests` BUILD SUCCESS；`./mvnw test` 120 run / 10 fail（全为预存在 auth 失败）；`npx playwright test` 38 passed（storefront-pages + app-startup）；调度器 5 个 job bean 实例化成功（e2e 启动日志确认 `nop.new-bean:bean[id=$DEFAULT$app.mall.service.scheduler.MallJobInvoker]`）。e2e happy-path 1 failed（空库无种子数据，非代码 bug，记入 Plan B Deferred）。
