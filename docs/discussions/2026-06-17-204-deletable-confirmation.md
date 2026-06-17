# 团购超时(204)订单可删除性 — 行为变更确认

- 日期：2026-06-17
- 状态：已确认
- 关联：`docs/analysis/2026-06-17-design-doc-consistency-audit.md`（F-doc-code-3）、`docs/plans/2026-06-17-1700-design-code-consistency-fix-plan.md`（Phase 2）

## 背景

审计发现 `LitemallOrderBizModel.deleteOrder` 删除白名单（`:478-482`）含 `REFUND_CONFIRM(203)` 不含 `GROUPON_EXPIRED(204)`，而两者都是已退款终态。文档（`order-and-cart.md` 状态叙述、`flow-overview.md` mermaid）把 204 当作可删终态。代码与文档不一致。

source-of-truth 规则（`source-of-truth-and-precedence.md:146`）要求：解决冲突若改变用户可见行为，必须停下确认。将 204 纳入白名单会使原本不可被用户软删的 204 订单变为可软删——属用户可见行为变更。

## 决策选项

1. 改代码：204 加入删除白名单（使 203/204 一致可删，对齐文档）
2. 改文档：204 标注为例外不可删（不改行为）
3. 暂不处理，列为 Deferred

## 确认结果

**选项 1（改代码）**，由 human 在计划拟制阶段经交互确认。

确认渠道：plan 拟制交互（question 工具），human 选择"改代码：204加入删除白名单 (推荐)"。

## 理由

- 203 已退款与 204 团购超时（已退款）在业务上同类，都是资金已退的终态订单
- 203 可删而 204 不可删是代码内部不一致，最可能是 204（团购特性后加）未被同步加入既有白名单
- 对齐方向选"改代码"使代码内部一致，并消除文档-代码漂移

## 残留风险

- 用户现可软删 204 订单（用户可见行为变化）。软删为逻辑删除，不影响数据完整性或对账。
