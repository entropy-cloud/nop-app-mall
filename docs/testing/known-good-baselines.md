# Known-Good Baselines

## Purpose

Record the latest verified project state so future AI sessions can tell whether a failure is new or pre-existing.

This file is lightweight. Record only meaningful baselines, not every local command run.

## Baselines

| Date | Source | Git State | Scope | Commands Passed | Known Failures | Evidence | Notes |
| ---- | ------ | --------- | ----- | --------------- | -------------- | -------- | ----- |
| 2026-06-27 | enhanced-roadmap Phase 16 订单项级售后增强（计划 `2026-06-27-1742-3`）全量交付 | working tree（uncommitted；含本计划 + Phase 15/26 已提交基线 36ce3f5） | app-mall-service 全量 + app-mall-web 编译 + 全模块 uber-jar | `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` BUILD SUCCESS；`./mvnw test -pl app-mall-service -am` **148 测试全绿**（含 `TestLitemallAftersaleBizModel` 扩展至 12 例：item 级申请/退款/互斥/类型映射/字典校验/部分退款副作用/全部退款转 203 + 整单兼容）；`./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS | 无 | logs/2026/06-27.md；计划 `2026-06-27-1742-3` Phase 5 | Phase 16 全 5 Phase completed。售后升级为 OrderGoods 项级（一订单多项独立售后）；新增 `mall/aftersale-reason` 字典 + reason 列 ext:dict；item 退款额上限=number×price；部分退款副作用（状态/还库/券）按 Decision；order.aftersaleStatus 转 aggregate 视图；前台按项申请售后入口 + 后台 item 审核 + 时间线。 |
| 2026-06-27 | enhanced-roadmap Phase 26 会员等级体系（计划 `2026-06-27-1742-2`）全量交付 | working tree（uncommitted；含本计划 + Phase 15 已提交基线） | app-mall-service 全量 + app-mall-web 编译 + 全模块 uber-jar | `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` BUILD SUCCESS；`./mvnw test -pl app-mall-service -am` **141 测试全绿**（含新增 `TestLitemallMemberLevelBizModel` 6 例 + `TestLitemallOrderBizModel` vipPrice 2 例）；`./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS | 无 | logs/2026/06-27.md；计划 `2026-06-27-1742-2` Phase 5 | Phase 26 全 5 Phase completed。MemberLevel 实体 + vipPrice（GoodsProduct/OrderGoods 快照）模型；等级评估（累计消费）+ 升降级 + 个人中心进度 `@BizQuery`；submit vipPrice 接线（goodsPrice 汇总层）；后台等级管理页 + 个人中心展示 + SKU vipPrice 录入。 |
| 2026-06-27 | enhanced-roadmap Phase 15 满减送（计划 `2026-06-27-1742-1`）全量交付 | working tree（uncommitted；含本计划 + 既有 codegen 基线） | app-mall-service 全量 + app-mall-web 编译 | `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS；`./mvnw test -pl app-mall-service -am` **133 测试全绿**（含新增 `TestLitemallPromotionActivityBizModel` 7 例 + `TestLitemallOrderBizModel` 满减/叠加 3 例）；`./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS | 无 | logs/2026/06-27.md；计划 `2026-06-27-1742-1` Phase 4 | Phase 15 全 4 Phase completed。满减价格接线、叠加配置（`mall_promotion_coupon_stacking` 默认允许）、前端动态预览、后台 tiers 子表均已落地。 |
| 2026-06-19 | 测试加固计划 `2026-06-19-2100` Phase 1 执行 + clean baseline 验证 | working tree（本计划改动）+ `git stash` 验证 HEAD `f98d1ee` clean | app-mall-service 全量 | 新增 3 测试单独跑通过：`TestLitemallOrderGoodsBizModel`（expireCommentWindow 过期/未过期 ×2）+ `TestLitemallGrouponBizModel.testPublishRulesGoodsNotOnSale` ×1 | **21 个预存在失败**（clean baseline `git stash` 同样失败，非本计划引入）：auth 10（`TestPasswordReset`/`TestNopAuthUserProfile`/`TestLoginApiSignUp`，userType 字典 `非法的字典项:0`）+ aftersale 5（`TestLitemallAftersaleBizModel`）+ comment 6（`TestLitemallCommentBizModel`）；后两组根因：image domain picUrl 回归（`submit` 时 orderGoods.picUrl 为 null 违反 NOT NULL） | docs/logs/2026/06-19.md; 计划 `2026-06-19-2100` Deferred 区 | **此前 06-17 记录"核心 36 全过"已过时**：平台 `DataInitInitializer` 改动（e2e 计划 06-19）后 aftersale/comment 测试失效。e2e storefront happy-path 不受影响（种子含 `nop_file_record.csv`，39 passed）。 |
| 2026-06-17 | live repo (Plan 2 completed + Plan 3 in progress) | working tree (uncommitted) | app-mall-service 全量 + app-mall-web 编译 | `./mvnw clean package -DskipTests`：全 10 模块 BUILD SUCCESS；`./mvnw -pl app-mall-service test`：111 跑 / 101 过（含新增 TestMallNotificationService 3 + Plan 2 域逻辑加固后测试）；`./mvnw -pl app-mall-web -DskipTests compile` BUILD SUCCESS | 10 auth 测试预存在失败（TestPasswordReset/TestNopAuthUserProfile/TestLoginApiSignUp，`非法的字典项:0`，审计 02-01 userType 字典冲突） | logs/2026/06-17.md | e2e 未实跑。Plan 2 闭合审计通过。 |
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
