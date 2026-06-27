# 02 批量过期订单查不到单（count=0）：cutoff 绕开 CoreMetrics 用 LocalDateTime.now()，自外于 ORM 的时间源

## Problem

- `cancelExpiredOrders(timeoutMinutes=0)` 返回 `count=0`，刚创建的待支付订单未被批量取消。
- 影响测试：`TestLitemallOrderBizModel.testCancelExpiredOrders` 失败，断言 `count >= 1` 不满足。
- 严重性：中。这是产品级逻辑 bug（不仅是测试问题）—— 业务代码绕开平台统一时间源 `CoreMetrics`，在生产环境也可能因时钟源不一致导致漏取消/漏确认订单。

## Reproduction

- 环境：`mvn test -pl app-mall-service`，autotest 框架（`CoreMetrics` 注册为 `TestClock`，见下）。
- 触发步骤：`submit` 创建订单（status=CREATED，`addTime` 由 ORM 写入）→ 立即调用 `cancelExpiredOrders(timeoutMinutes=0)`。
- 最小复现：单测 `mvn test -Dtest='TestLitemallOrderBizModel#testCancelExpiredOrders'`，断言失败 `count=0`。
- 实测偏差：订单 `addTime=2026-06-27T16:59:12.085`，而断言时刻 `LocalDateTime.now()=2026-06-27T16:59:08.468`——`addTime` 比 `now()` 晚约 4 秒。

## Diagnostic Method

- 诊断难点：`count=0` 没有异常栈，只能反推查询为什么匹配不到行。
- 第一步排查：读 `cancelExpiredOrders` 实现（`LitemallOrderBizModel.java:598-606`），过滤条件是 `orderStatus=CREATED AND addTime < cutoff`，`cutoff = now() - timeoutMinutes`。直觉怀疑边界条件。
- 第一个被否定的假设：「`<` 应改 `<=`」—— 改成 `le` 后重跑，仍 `count=0`。说明不是 off-by-one，是 `cutoff` 本身小于 `addTime`（订单时间「在未来」）。
- 决定性证据：在测试里加临时诊断，直接读 DB 里的 `order.getAddTime()` 与 `java.time.LocalDateTime.now()` 对比，发现 `addTime` 比 `now()` 晚 4 秒——业务代码读到的「当前时间」比订单的创建时间还早。
- 闭环验证：查平台 `OrmTimestampHelper.onCreate`（`addTime` 经 `CoreMetrics.currentTimeMillis()` 写入）与 `CoreMetrics` 实现（`s_clock`，autotest 下被 `NopTestConfigProcessor` 替换为 `TestClock`），确认 `addTime` 走 `CoreMetrics`；而 `cancelExpiredOrders` 的 `cutoff` 用的是 `LocalDateTime.now()`（裸系统时钟），**两个时间源不同**。

## Root Cause

- **核心机制**：`TestClock`（`nop-autotest-core/.../TestClock.java`）是 autotest 注入到 `CoreMetrics` 的时钟，其设计目的是**保证每次返回的时间戳严格单调递增、永不重复**（同毫秒内 `lastTime++`），从而让测试里多条记录的 `createTime`/`updateTime` 各不相同，避免排序/查询 flaky。**这是特性，不是缺陷。**
- **真正的 bug**：`cancelExpiredOrders` / `confirmExpiredOrders` 用 `LocalDateTime.now()`（直接读系统时钟）算 cutoff，**绕开了 `CoreMetrics` 这个平台唯一时间源**，自外于 `TestClock`（以及 ORM 自动时间戳）的时间线。于是 cutoff 与 `addTime` 不在同一时间线上。
- **4 秒偏差的来源**：`CoreMetrics.s_clock` 是 **static** 字段，surefire 单 fork JVM 内被所有测试类共享（`NopTestConfigProcessor` 每个测试类初始化时 `registerClock(new TestClock())`）。前面若干测试在「系统时钟同一毫秒」内密集调用 `CoreMetrics`，触发 `now <= lastTime` 分支 `lastTime++`，使 `lastTime` **领先**系统时钟；且领先的 `lastTime` 不会回退（仅 `now > lastTime` 时才重置为系统时间）。等跑到 `TestLitemallOrderBizModel` 时，`TestClock.lastTime` 已领先系统时钟数秒，于是 `addTime`（TestClock）> `LocalDateTime.now()`（系统时钟）。
- **为何是产品 bug 而非测试 bug**：AGENTS.md 明确规定「用 `CoreMetrics.currentTimeMillis()` 而非 `System.currentTimeMillis()`」，目的就是统一时间源。TestClock 只是把这个规则的重要性在测试里放大了；即便生产环境不注入 TestClock，任何 `CoreMetrics` 与裸系统时间混用都会埋下时钟不一致隐患。

## Fix

- `cancelExpiredOrders` / `confirmExpiredOrders` 的 cutoff 改用 `CoreMetrics.currentDateTime()`，**回归平台唯一时间源**，与 ORM 自动时间戳（以及 TestClock）同源，符合 AGENTS.md 平台规则。
- 附带：cutoff 过滤由 `lt` 改为 `le`（创建时刻恰等于超时阈值也算过期），对齐平台 job/retry 模块的包含式 cutoff 约定（`nop-job` 的 `JobScheduleStoreImpl`、`nop-retry` 的 `RetryRecordStoreImpl` 均用 `FilterBeans.le(..., now)`）。

## Tests

- `app-mall-service/src/test/java/app/mall/service/entity/TestLitemallOrderBizModel.java::testCancelExpiredOrders` - 复用现有 case 验证 timeoutMinutes=0 下能匹配刚创建订单（level: component/integration）。
- 临时诊断代码（读 addTime/now 对比）已移除，未提交。
- 验证：`mvn test -Dtest='TestLitemallOrderBizModel#testCancelExpiredOrders'` → 通过；全量 `mvn test` → `Tests run: 123, Failures: 0`。

## Affected Artifacts

- `app-mall-service/src/main/java/app/mall/service/entity/LitemallOrderBizModel.java:39` - 新增 `import io.nop.api.core.time.CoreMetrics`
- `app-mall-service/src/main/java/app/mall/service/entity/LitemallOrderBizModel.java:600` - cancelExpiredOrders cutoff 改 `CoreMetrics.currentDateTime()`，`lt`→`le`
- `app-mall-service/src/main/java/app/mall/service/entity/LitemallOrderBizModel.java:654` - confirmExpiredOrders 同上


## Notes For Future Refactors

- **TestClock 的语义必须记住**：autotest 下 `CoreMetrics` 注入的 `TestClock` 保证时间戳**单调递增、永不重复**，目的是让多条记录的 `createTime`/`updateTime` 各不相同，避免排序/查询 flaky——这是特性不是 bug。它通过 `s_clock`（static，单 fork JVM 内跨测试类共享）和 `lastTime++` 实现，正常会领先系统时钟。**不要试图绕开或「修正」TestClock**；要让业务时间与它对齐，而不是反过来。
- **不变量**：任何与 ORM 自动时间戳字段（`domain="createTime"`/`updateTime"` 的 `addTime`/`shipTime`/`createTime` 等）做比较的查询，cutoff 必须用 `CoreMetrics.currentDateTime()`，不能用 `LocalDateTime.now()` / `System.currentTimeMillis()`。若有人把过期/调度类查询改回系统时钟，会**自外于 TestClock 时间线**，在 autotest 下复现 `count=0`，在生产下埋下时钟不一致隐患。
- **易错点**：`LocalDateTime.now()` 编译期不报错，肉眼也难发现混用，是隐蔽的时间源不一致。新建任何「按时间过期/调度」逻辑时，第一反应应是查时间字段由谁赋值，cutoff 跟它同源（一律走 `CoreMetrics`）。
- **联动**：bug 01 修复后，submit 才能跑通，本 case 才第一次被执行到——即本 bug 此前被「submit 失败」掩盖，是层层揭露的第二层问题。

## Prevention Gap

- 项目内无静态规则强制「时间比较用 CoreMetrics」；可考虑在 code review checklist 或 lint 规则里加「禁用 `LocalDateTime.now()`/`System.currentTimeMillis()` 做 DB 时间字段比较」。
