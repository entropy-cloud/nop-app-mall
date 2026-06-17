# Known-Good Baselines

## Purpose

Record the latest verified project state so future AI sessions can tell whether a failure is new or pre-existing.

This file is lightweight. Record only meaningful baselines, not every local command run.

## Baselines

| Date | Source | Git State | Scope | Commands Passed | Known Failures | Evidence | Notes |
| ---- | ------ | --------- | ----- | --------------- | -------------- | -------- | ----- |
| 2026-06-17 | live repo (Plan 1 Phase 1/2/3/4/5 全 completed) | working tree (uncommitted) | app-mall-service 全量 | `./mvnw test -pl app-mall-service -am`：110 测试，100 过（含新增 testConfirmPaidByNotify + testDeleteGoodsWithOrderHistoryRejected，核心 36 全过）；`./mvnw install -pl app-mall-dao -am` BUILD SUCCESS（_app.orm.xml 重新生成，goods→orderGoods cascadeDelete 已移除） | 10 auth 测试预存在失败（TestPasswordReset/TestNopAuthUserProfile/TestLoginApiSignUp，`非法的字典项:0`，审计 02-01 userType 字典冲突，与本变更无关；git stash 验证 clean baseline 同样失败） | logs/2026/06-17.md | 全量 `./mvnw test -pl app-mall-service -am` 已实跑；e2e 未实跑。 |
| 2026-06-16 | live repo (Plan 1 Phase 2/3/5) | working tree (uncommitted) | app-mall-service core (order/aftersale/cart/groupon/coupon) | `./mvnw package -DskipTests -pl app-mall-app -am` BUILD SUCCESS；`./mvnw test -pl app-mall-service -am -Dtest=TestLitemallOrderBizModel,TestLitemallAftersaleBizModel,TestLitemallCartBizModel,TestLitemallGrouponBizModel,TestLitemallGrouponExpireBizModel,TestLitemallCouponUserBizModel` 33 passed (含新增 testCancelExpiredOrders + testApplyAmountExceedsActualPrice) | 10 auth 测试预存在失败（TestPasswordReset/TestNopAuthUserProfile/TestLoginApiSignUp，`非法的字典项:0`，审计 02-01 userType 字典冲突，与本变更无关；git stash 验证 clean baseline 同样失败） | logs/2026/06-16.md | `./mvnw test`（全量）未实跑（auth 失败为预存在）；e2e 未实跑。下次完整绿时补 row。 |

## When To Update

Update this file when:

- full typecheck/build/lint/test verification passes after a meaningful change
- a previously failing command becomes green and should be remembered
- a team intentionally accepts a known failing command and records it as a known failure, not as a passed command

## Rule

Do not mark a command as passed unless it actually ran in the current repository state.

`Commands Passed` must contain only passing commands. Put accepted failures in `Known Failures` with the reason and evidence.

`full` means all real verification commands configured in `docs/context/project-context.md`. Commands explicitly marked `none` are excluded and should be noted.
