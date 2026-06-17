# 维度 11：回归与跨特性风险

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核

## 第 1 轮（初审）发现（深挖新回归面，标注 *deepens known* 为对基线的深化）

### [维度11-1] submit() 未校验收货地址归属 — 跨用户地址 IDOR/PII 泄露 — P1（新发现）
- **文件**: `LitemallOrderBizModel.java:122-165`
- **证据**: addressBiz.get(addressId) 是 Crud 直查，全 submit 无 address.getUserId()==context.getUserId() 比对；同仓 getMyOrder/aftersale.userDetail/selectCouponForOrder/address 都做了 owner 校验
- **风险**: PII 泄露（姓名/手机/详细地址）+ 欺诈履约；与已知"cancel/pay/ship/confirm 不校验本人"叠加成完整越权链
- **建议**: submit 增 `if(!userId.equals(address.getUserId())) throw`

### [维度11-2] cancel() 与定时任务 cancelExpiredOrders() 并发 → 双倍回滚库存（无乐观锁） — P1
- **文件**: `LitemallOrderBizModel.java:272-304`（cancel）、`:513-546`（cancelExpiredOrders）
- **证据**: ORM grep 确认 `version` domain 虽定义但**无任何 mall 实体列引用**；addStock EQL 无条件守卫
- **风险**: 用户取消与定时任务并发，两事务都过 status 校验，addStock 各执行一次→库存加两倍→后续超卖
- **建议**: 引入 version 乐观锁或条件 UPDATE 校验影响行数

### [维度11-3] claimCoupon 内存递减 total → 把"限量券"翻转为"无限券" — P1（深化 09-07）
- **文件**: `LitemallCouponUserBizModel.java:50-93`
- **证据**: 字典注释 total=0 为无限量；领到第 5 张时 total 内存递减到 0 落库；第 6 次起 `total>0` 判定为 false 整段限量校验跳过，且不再递减→永久无限发放
- **风险**: 限量促销券/注册赠券被领完即无限发行，可被薅羊毛；不自愈（total 已持久化为 0）
- **建议**: 不递减 total（语义是发行上限）；以 count(coupon_user) 为准；原子 UPDATE

### [维度11-4] submit() 调 selectCouponForOrder 传 goodsIds=null → 跳过券适用范围校验 — P1（新发现）
- **文件**: `LitemallOrderBizModel.java:204`、`LitemallCouponUserBizModel.java:176-232`
- **证据**: `selectCouponForOrder(couponUserId, goodsPriceTotal, null, context)`；goodsIds==null→整块类目券/指定商品券校验跳过
- **风险**: 可用"仅商品 A 可用"高额券抵扣只含商品 B 的订单
- **建议**: submit 计算 goodsIds 传入；goodsIds 空时拒绝受限券

### [维度11-5] submit() 应用团购优惠却不校验 rules.goodsId 是否在购物车 — P1（新发现）
- **文件**: `LitemallOrderBizModel.java:208-246`
- **证据**: rules.goodsId mandatory 但 submit 全程未读，未与 checkedItems 比对；openGroupon/joinGroupon 也只校验规则状态/参团资格
- **风险**: 低价商品+高价商品团购规则→超额抵扣，actualPrice 被打到接近 0
- **建议**: submit 校验 checkedItems 含 rules.goodsId

### [维度11-6] 商品软删会级联软删订单商品 → 订单历史被破坏 — P1（深化 04-01）
- **文件**: `app-mall.orm.xml:642-648`、`LitemallGoodsBizModel.java`（无 defaultPrepareDelete）
- **证据**: 双向 cascadeDelete=true；GoodsBizModel 无删除守卫（对比 CategoryBizModel.defaultPrepareDelete 有子分类保护）；getGoodsSalesRanking SQL 过滤 og.DELETED=0
- **风险**: 删除曾被购买的商品→所有历史订单 orderGoods 软删→订单详情/统计丢失
- **建议**: GoodsBizModel 增 defaultPrepareDelete 拒绝有 orderGoods 引用的删除；或移除 goods→orderGoods cascadeDelete

### [维度11-7] *deepens known* 微信回调不更新订单状态 — 直接违反 owner doc 契约 — P1
- **文件**: `WxPayNotifyResource.java:38-41`、`order-and-cart.md:147`
- **复核状态**: 已保留（与 09-02/10-1 同源）

### [维度11-8] *deepens known* outRefundNo 双前缀 + refundGrouponOrder 守卫漏掉 APPROVED 售后 — P2
- **文件**: `LitemallAftersaleBizModel.java:96-134`、`LitemallGrouponBizModel.java:212-260`
- **证据**: 售后 apply/batchApprove 只改 aftersaleStatus 不改 orderStatus；refundGrouponOrder 守卫仅判 orderStatus==PAY；售后退款用 refund_ 前缀，团购用 groupon_refund_→双倍退款
- **复核状态**: 已保留（深化 09-09）

### [维度11-9] 实付价公式与 ORM 注释不一致：grouponPrice 只减 actualPrice 不减 orderPrice — P2（新发现）
- **文件**: `LitemallOrderBizModel.java:223-235`；ORM `:997-1002`
- **建议**: 同步 ORM 注释为 `actualPrice = order_price - integral_price - groupon_price`

### [维度11-10] 评论/收藏/足迹 并发去重靠 read-then-write，且无唯一约束兜底 — P2
- **文件**: `CommentBizModel.java:65-88`、`CollectBizModel.java:32-48`、`FootprintBizModel.java:40-50`
- **现状**: 三实体 ORM 仅有非唯一 idx_*_userId，无 (userId,type,valueId) 唯一键

### [维度11-11] getUserStatistics SQL 未过滤已删除用户 — P3
- **文件**: `LitemallOrder.sql-lib.xml:47-66`
- **现状**: 对比 getOrderStatistics/getGoodsSalesRanking 都过滤 DELETED=0，唯独用户统计无

### [维度11-12] 大量 BizModel 方法依赖 dirty auto-flush 无显式 save — P3
- **文件**: CommentBizModel.adminReply、GoodsBizModel.onSale/offSale、CouponUserBizModel.expireCoupons 等
- **风险**: 模式不一致+脆弱（session 策略变更静默不落库）

## 维度复核结论
主 agent 复核：11-1 地址 IDOR 经 grep 确认 submit 无 userId 比对；11-2 version 列经 ORM grep 确认无 mall 实体引用；11-3 total 翻转逻辑经字典注释+代码确认；11-4/11-5 经调用点确认恒传 null/不校验。12 项保留。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 11-1 | P1 | LitemallOrderBizModel.java:122 | submit 不校验地址归属 IDOR |
| 11-2 | P1 | LitemallOrderBizModel.java:272/513 | cancel 与定时任务并发双倍回滚库存 |
| 11-3 | P1 | LitemallCouponUserBizModel.java:50 | claimCoupon 递减 total 致限量变无限 |
| 11-4 | P1 | LitemallOrderBizModel.java:204 | submit 传 goodsIds=null 跳过券范围校验 |
| 11-5 | P1 | LitemallOrderBizModel.java:208 | submit 不校验团购规则商品匹配 |
| 11-6 | P1 | app-mall.orm.xml:642 | 商品软删级联破坏订单历史 |
| 11-7 | P1 | WxPayNotifyResource.java:38 | 微信回调不推进订单（违反 owner doc） |
| 11-8 | P2 | Aftersale/GrouponBizModel | 双退款竞态 |
| 11-9 | P2 | LitemallOrderBizModel.java:223 | grouponPrice 公式与 ORM 注释不符 |
| 11-10 | P2 | Comment/Collect/Footprint | 并发去重无唯一约束 |
| 11-11 | P3 | LitemallOrder.sql-lib.xml:47 | 用户统计未过滤软删 |
| 11-12 | P3 | 多 BizModel | dirty-flush 无显式 save |

## 维度评级：Moderate（偏 Poor）

资损面密集（地址 IDOR/限量券翻转/券范围绕过/团购不匹配/回调不推进 5 条可叠加资损路径）；数据完整性硬伤（库存双倍回滚/订单历史破坏）；并发治理缺位（全仓无 version 乐观锁，库存/券/团购/评论/收藏/足迹并发全靠 read-then-write）；契约一致性差。骨架健全（14 阶段结构完成、事务边界整体正确、错误码统一）但并发/授权/优惠校验三大面普遍薄弱。
