# 维度 01：需求正确性与 owner-doc 对齐

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核

## 第 1 轮（初审）发现

### [维度01-1] Phase 11 定时任务缺失调度器装配（nop-job 未引入，5 个"自动"任务均为手动入口）
- **文件**: `app-mall-app/pom.xml`、`app-mall-service/pom.xml`、`app-mall-app/src/main/resources/_vfs/nop/task/test/demo/v1.task.xml:1-16`
- **证据片段**: app-mall-app/pom.xml 依赖列表无 nop-job；全工程 grep `@Scheduled|io.nop.job|IJobScheduler|cron|fixedRate` 仅命中 `@QuarkusMain`；唯一 task.xml 是 demo
- **严重程度**: P1
- **现状**: Phase 11 标 `done`，仅交付 5 个手动 `@BizMutation` 入口（cancelExpiredOrders/confirmExpiredOrders/expireCoupons/expireGroupons/expireCommentWindow），无调度器调用它们
- **风险**: 超时订单永不自动取消、库存/优惠券永久锁定；"自动"语义不存在
- **建议**: 引入 nop-job 依赖并装配 cron，或 Phase 11 改 `partial`
- **信心水平**: 确定
- **误报排除**: grep 全工程确认无调度装配；与 roadmap 第 343-348 行逐条比对 5 个任务名
- **复核状态**: 已保留

### [维度01-2] Phase 13 报表：nop-report 未引入、无后台看板页面、无报表模板
- **文件**: `app-mall-app/pom.xml`、`app-mall-service/pom.xml`、`app-mall-web/.../LitemallOrder/main.page.yaml:1-3`
- **证据片段**: glob `*.report.xml`/`*Report*.java` 0 命中；page.yaml 中无 getOrderStatistics/getGoodsSalesRanking/getUserStatistics 调用
- **严重程度**: P1
- **现状**: Phase 13 标 `done`，但 roadmap 第 379-383 要求的三项（nop-report 依赖、报表模板、后台看板）均未做，只交付了 SQL 数据集
- **风险**: 管理员无法看统计；标 done 误导后续
- **建议**: 真正引入 nop-report 或 Phase 13 改 `partial`
- **信心水平**: 确定
- **误报排除**: glob 全工程扫描三类目标均 0 命中
- **复核状态**: 已保留
- **Remediation（2026-06-29）**: nop-report 引擎+模板部分已由 successor 计划 `docs/plans/2026-06-28-2352-1-nop-report-engine-introduction-plan.md` remediate——引入 `nop-report-core`+`nop-report-pdf` 依赖 + 4 个 `.xpt.xml` 模板（goods-export/sales-funnel/product-analysis/order-analysis）+ `exportGoodsReport`/`exportReport` 两个 `@BizQuery` 经 `IReportEngine` 渲染 xlsx/pdf。**后台看板部分**已由 AMIS chart 关闭（`2026-06-17-1830` Phase 2 Option B + P18 重做）。本维度三条缺口（依赖/模板/看板）至此全部关闭。

### [维度01-3] roadmap "Current Baseline" 文字与 Phase Status 自相矛盾
- **文件**: `docs/backlog/implementation-roadmap.md:14-33` 与 `:61-74`
- **证据片段**: Phase Status 全 done；Current Baseline 却列"核心缺口：用户注册 Delta、地址、优惠券/团购、搜索、定时任务、通知、报表"
- **严重程度**: P1
- **现状**: 同文件内状态索引自相矛盾——Baseline 过期（多数缺口已实现）+ Status 虚标（Phase 11/13 实际部分）
- **风险**: roadmap 核心用途（"AI 读完即知哪些已实现"）失效
- **建议**: 重写 Current Baseline 段；Phase 11/13 改 partial 或在 Phase Details 记录未完成项
- **信心水平**: 确定
- **复核状态**: 已保留

### [维度01-4] 团购成功状态（status=3）在 ORM/前端/后端三层同时缺失
- **文件**: `model/app-mall.orm.xml:789`、`groupon-activity-detail.page.yaml:64`、`LitemallGrouponBizModel.java:67-210`
- **证据片段**: ORM 字典只到 2；前端三态+未知兜底；后端从无 setStatus(3)；input/design 都要求 4 态
- **严重程度**: P1
- **现状**: joinGroupon 达到 discountMember 时仅抛 GROUPON_FULL，无成功判定；团购成团后仍显示"开团中"
- **风险**: 用户无法判断成团；运营报表成功团数为 0
- **建议**: 补 status=3 字典 + joinGroupon 成团判定 + 前端四态
- **信心水平**: 确定
- **复核状态**: 已保留

### [维度01-5] 已支付未发货订单"管理员直接退款"独立路径未交付
- **文件**: `docs/design/order-and-cart.md:184-189`、`implementation-roadmap.md:236-247`、`LitemallOrderBizModel.java`
- **证据片段**: design 区分"退款范围（未发货）"与"售后范围（已收货）"；订单状态机 202/203 字典有定义但代码无 setStatus(202)
- **严重程度**: P1
- **现状**: 退款被合并到 aftersale 单一路径，订单 orderStatus 从不进 202
- **风险**: 订单状态机与设计脱节；后台按 202 过滤退款中订单永远查不到
- **建议**: 增订单级 applyRefund/adminRefund，或回写 design 明示合并
- **信心水平**: 很可能
- **复核状态**: 已保留（与 09-06 同源）

### [维度01-6] 售后退款不归还优惠券（与 input、design 双重违约）
- **文件**: `LitemallAftersaleBizModel.java:95-134`
- **证据片段**: refund() 全文无 couponUserBiz.returnCoupon 调用；对比 cancel():292-299 与 refundGrouponOrder():248-255 都正确归还
- **严重程度**: P1
- **现状**: 售后退款后优惠券永久停留 status=1 已使用
- **风险**: 用户资金已退但券永久丢失；违反 input/design
- **建议**: refund() 复用 cancel() 第 292-299 行归还逻辑 + 补单测
- **信心水平**: 确定
- **复核状态**: 已保留

### [维度01-7] 售后申请接受 ORDER_STATUS_PAY(201)，违反 design "售后范围限定于已收货"
- **文件**: `LitemallAftersaleBizModel.java:136-182`、`order-and-cart.md:184-194`
- **证据片段**: apply() 接受 PAY(201)/CONFIRM(401)/AUTO_CONFIRM(402)；design 售后范围仅 401/402
- **严重程度**: P2
- **现状**: 退款与售后合并到 aftersale 单一路径
- **建议**: 与 01-5 一并修复或回写 design
- **信心水平**: 很可能
- **复核状态**: 已保留

## 维度复核结论

主 agent 独立复核：7 项发现全部保留。01-1/01-2 经 pom.xml + glob 双重核实；01-3 同文件对照确凿；01-4 三层（ORM/前端/后端）交叉确认；01-5/01-6/01-7 与维度 09 的状态机/退款发现相互印证（09-06 订单 202/203 死代码、09-05 GOODS_MISS 不回滚库存、退款不归还优惠券）。无降级或驳回。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 01-1 | P1 | app-mall-app/pom.xml 等 | Phase 11 定时任务无调度器装配 |
| 01-2 | P1 | app-mall-app/pom.xml 等 | Phase 13 报表无 nop-report/看板/模板 |
| 01-3 | P1 | implementation-roadmap.md:14-33,61-74 | roadmap Baseline 与 Phase Status 自相矛盾 |
| 01-4 | P1 | app-mall.orm.xml:789 等 | 团购成功状态 status=3 三层缺失 |
| 01-5 | P1 | LitemallOrderBizModel.java | 已支付未发货退款独立路径未交付 |
| 01-6 | P1 | LitemallAftersaleBizModel.java:95-134 | 售后退款不归还优惠券 |
| 01-7 | P2 | LitemallAftersaleBizModel.java:136-182 | 售后申请接受 PAY(201) 违反 design |

## 维度评级：Moderate

input→requirements→design 链路本身设计良好，但存在 4 条 P1 级"已完成阶段标 done 但功能缺口"（Phase 11 调度、Phase 13 看板、Phase 9 团购成功判定、Phase 5c 退款券归还）+ 1 条 P1 owner-doc 自相矛盾。
