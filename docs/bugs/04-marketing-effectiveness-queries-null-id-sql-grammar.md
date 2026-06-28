# 04 Nop xpl-sql `${arg}` 对 null 输出空文本，`(${id} IS NULL OR col=${id})` 惯用法生成非法 SQL

## Problem

- `LitemallMarketing.sql-lib.xml` 中 `getCouponUsageStatistics` / `getPinTuanEffectiveness` / `getFlashSaleEffectiveness` 3 条 query 用 `(${id} IS NULL OR col = ${id})` 处理「可选 ID 过滤」（id 空 = 全量聚合）。
- 实际：Nop xpl-sql 模板里 `${arg}` 当值为 null 时输出**空文本**（不是 `?` 绑定参数），故 id=null 时生成 `( IS NULL OR col = )` —— 语法非法，抛 `nop.err.dao.sql.bad-sql-grammar`。
- 影响范围：上述 3 条 query 在 **id 为 null** 时必崩。**既有** `marketing-effect.page.yaml` 的 coupon/pinTuan/flashSale 3 个面板均不带 id 调用（生产 latent bug，因既有单测一律传非 null id，未覆盖 null 路径）。营销效果导出（plan `2026-06-29-0830-1`）同样在 null-id 全量口径下触发。
- 严重性：中-高（生产功能损坏，但仅限营销效果 3 面板的默认全量口径；非 null 路径正常）。

## Reproduction

- 环境：任意调用上述 3 query 且 id 参数为 null 的路径（H2 测试库 / MySQL 生产库均复现，SQL 语法非法与数据库无关）。
- 触发：`marketingMapper.getCouponUsageStatistics(null, start, end)` → `JdbcSQLSyntaxErrorException: Syntax error in SQL statement "...AND ( [*]IS NULL OR cu.COUPON_ID = )..."`。
- 决定性证据：H2 抛出的 SQL 文本里 `${couponId}` 两处都变成空（`[*]` 为 H2 语法错误定位标记）。

## Diagnostic Method

- 诊断方法：新增临时 diag 测试，对 4 条效果 query 分别以空参经 `IGraphQLEngine` 调用，定位「promotion(null) OK / flashSale/pinTuan/coupon(null) 全 bad-sql-grammar」；再注入 `LitemallMarketingMapper` 直调并打印 cause 链，拿到 H2 原始 SQL 文本确认 `${arg}`→空。
- 关键转折：GraphQL 层把 DAO 异常包成 `ApiResponse[status=-1]`，cause 不在 surefire 默认报告里；须绕过 GraphQL 直调 mapper 才看到 H2 原始 SQL（受 bug 03「toString 吞错误」同源影响）。

## Root Cause

- **机制**：Nop sql-lib 的 `<source>` 是 xpl-sql 模板（`xpl:outputMode="sql"`），`${expr}` 对**非 null** 值生成 JDBC `?` 绑定，但对 **null** 值输出空文本（设计如此，便于可选片段）。因此 `(${id} IS NULL OR col = ${id})` 这种「靠 `${id}` 自身判空」的 MyBatis 风格惯用法在 Nop 里从根本上无效。
- **正确惯用法**：用 XPL 条件块 `<c:if test="${id != null and !id.isEmpty()}"> AND col = ${id} </c:if>`（本项目 `LitemallOrder.sql-lib.xml:199` 已有先例，处理可选 `categoryId`）。null 时整个 `AND` 子句不生成 → 全量聚合；非 null 时生成 `AND col = ?` → 按 id 过滤。

## Fix

- `app-mall-dao/src/main/resources/_vfs/app/mall/sql/LitemallMarketing.sql-lib.xml`：
  - 根元素加 `xmlns:c="c"`。
  - `getCouponUsageStatistics`：`AND (${couponId} IS NULL OR cu.COUPON_ID = ${couponId})` → `<c:if test="${couponId != null and !couponId.isEmpty()}"> AND cu.COUPON_ID = ${couponId} </c:if>`。
  - `getPinTuanEffectiveness`：3 处 `(${activityId} IS NULL OR gN.ACTIVITY_ID = ${activityId})`（外层 WHERE + 2 个标量子查询）各替换为同名 `<c:if>`。
  - `getFlashSaleEffectiveness`：`AND (${flashSaleId} IS NULL OR EXISTS(...))` → `<c:if test="${flashSaleId != null and !flashSaleId.isEmpty()}"> AND EXISTS(...) </c:if>`。
- **口径不变**：null=全量聚合、非 null=按 id 过滤，与 owner doc（`marketing-and-promotions.md` 效果分析口径表）记载一致；非 null 路径生成的 SQL 与原先逐字等价。

## Tests

- 既有非 null 用例全绿（`TestLitemallFlashSaleBizModel` 19、`TestLitemallPinTuanActivityBizModel` 10、`TestLitemallCouponBizModel` 8）——证明非 null 路径未回归。
- 新增 null 路径覆盖：`TestLitemallPromotionActivityBizModel.testExportMarketingReportXlsx/Pdf/EmptyDataNotError`（导出以 null-id 全量口径调 4 query）全绿。
- 全服务套件 `./mvnw test -pl app-mall-service -am` → 427 通过。

## Affected Artifacts

- `app-mall-dao/src/main/resources/_vfs/app/mall/sql/LitemallMarketing.sql-lib.xml` - 3 query 的可选 ID 过滤改 `<c:if>` 条件块 + 根元素加 `xmlns:c="c"`。

## Notes For Future Refactors

- **不变量**：Nop sql-lib/eql 的 `<source>` 里，**禁止**用 `${arg} IS NULL` 或 `col = ${arg}` 之类「靠 `${arg}` 绑定 null」的写法处理可选过滤。可选过滤一律用 `<c:if test="${arg != null and !arg.isEmpty()}">`（字符串）或 `<c:if test="${arg != null}">`（非字符串）条件块。
- **审计线索**：审计 SQL-lib 时，grep `IS NULL OR` 命中且该 `${arg}` 可能为 null → 几乎必为此 bug。本项目其它 sql-lib（LitemallOrder 等）已用 `<c:if>` 正确模式，可作参照。
- **范围**：本 bug 不影响 promotion 聚合 query（无 id 参数，天然不受影响）与 byActivity query（activityId 必填，无 null 路径）。

## Prevention Gap

- 可考虑在 SQL-lib 加载期或 codegen 增加静态检查：`<source>` 内出现 `${arg} IS NULL` 或裸 `= ${arg}` 且对应 `<arg>` 未声明 `nullable=false`/必填时告警。当前靠 review + 本 bug 笔记提醒。
