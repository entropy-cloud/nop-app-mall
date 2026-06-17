# 维度 09：电商域逻辑正确性

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核（含 P0 逐项核验）

## 第 1 轮（初审）发现（按 P0→P1→P2→P3）

### [维度09-01] pay() 无支付凭证校验，任何人可将未支付订单推进到已支付 — P0
- **文件**: `app-mall-service/.../LitemallOrderBizModel.java:337-357`
- **证据片段**: pay() 仅检查状态==CREATED，无 payService.queryPayment 校验，直接 setOrderStatus(PAY)
- **严重程度**: P0
- **现状**: 真实模式任何登录用户知道 orderId 即可零成本推进"已付款"
- **风险**: 商城据此发货但未收到款；资损漏洞
- **建议**: pay() 改由可信触发源调用，先 queryPayment 校验 tradeState==SUCCESS 且金额匹配
- **信心水平**: 确定
- **复核状态**: 已保留（主 agent 复核确认 pay() 无 queryPayment 调用）

### [维度09-02] 微信支付异步回调不更新订单状态，真实模式订单永不自动支付 — P0
- **文件**: `app-mall-wx/.../WxPayNotifyResource.java:26-53`、`WxPayServiceImpl.java:245-272`
- **证据片段**: parseNotifyBody 只验签返回 outTradeNo；handleNotify 仅回 SUCCESS，不调 orderBiz.pay()
- **严重程度**: P0
- **现状**: 回调不推进订单状态；order-and-cart.md:147 明确承诺"回调验证签名后更新订单状态"，代码未实现
- **风险**: 生产环境用户付款后订单停留待支付，被 cancelExpiredOrders 误取消 → 钱货两空
- **建议**: parseNotifyBody 验签后调 orderBiz.pay() 或新增 confirmPaidByNotify
- **信心水平**: 确定
- **复核状态**: 已保留（主 agent 直读 WxPayNotifyResource.java:39-41 确认返回值丢弃）

### [维度09-03] reduceStock 返回值未检查，并发下单可超卖 — P0
- **文件**: `LitemallOrderBizModel.java:138-198`、`LitemallGoodsProduct.sql-lib.xml:16-25`
- **证据片段**: SQL 有 `where number>=${num}` 原子条件；但 Java `goodsProductMapper.reduceStock(...)` 返回值被丢弃
- **严重程度**: P0
- **现状**: 库存=1 时 A、B 并发：都通过 select 检查，A UPDATE 成功，B UPDATE 影响 0 行但代码无感知，照样建单
- **风险**: 超卖，违反 product-catalog.md:78
- **建议**: `int affected = reduceStock(...); if (affected==0) throw ERR_ORDER_STOCK_INSUFFICIENT`
- **信心水平**: 确定
- **复核状态**: 已保留（主 agent 直读 BizModel:197 确认返回值未接收）

### [维度09-04] 售后申请退款金额无校验，可申请超额退款 — P0
- **文件**: `LitemallAftersaleBizModel.java:138-182`、`:96-134`
- **证据片段**: apply() 直接信任 request.getAmount()，无 amount<=actualPrice 校验；refund() 直接传给微信
- **严重程度**: P0
- **现状**: 用户可提交 amount=999999
- **风险**: 资损 + 对账数据失真
- **建议**: apply() 加 amount<=actualPrice 校验 + 新增错误码
- **信心水平**: 确定
- **复核状态**: 已保留

### [维度09-05] GOODS_MISS（未收货退款）不回滚库存，库存永久锁死 — P0
- **文件**: `LitemallAftersaleBizModel.java:122-129`
- **证据片段**: 仅 type==GOODS_REQUIRED(2) 才 addStock；但 apply() 接受 PAY(201) 未发货状态，用户选 GOODS_MISS(0) 走未发货退款
- **严重程度**: P0
- **现状**: 已付款未发货订单走未收货退款，钱退但货还在仓，库存永不回滚
- **风险**: 库存虚减、资金+库存双轨资损
- **建议**: 回滚判断应基于订单履约阶段（未发货售后应回滚），而非用户是否退货
- **信心水平**: 确定
- **复核状态**: 已保留

### [维度09-06] 售后退款成功后订单主状态 orderStatus 不变，202/203 完全死代码 — P1
- **文件**: `LitemallAftersaleBizModel.java:96-134`、`_AppMallDaoConstants.java:71-79`
- **证据片段**: refund() 只改 aftersaleStatus；ORDER_STATUS_REFUND(202)/REFUND_CONFIRM(203) 全代码库无写入点
- **严重程度**: P1
- **建议**: refund 后未发货→203；或回写 design 删除 202/203 死状态
- **复核状态**: 已保留（与 01-5 同源）

### [维度09-07] 优惠券领取竞态 + coupon.total 语义双关 — P1
- **文件**: `LitemallCouponUserBizModel.java:50-93`
- **证据片段**: 先查 findCount 后插；又把 total 当剩余量递减
- **严重程度**: P1
- **建议**: 原子 UPDATE `set total=total-1 where total>0` 校验影响行数；拆分原配额与剩余量
- **复核状态**: 已保留（与 11-P1-3 同源，11 进一步发现 total 递减到 0 翻转为无限券）

### [维度09-08] 优惠券使用竞态，可被并发重复核销 — P1
- **文件**: `LitemallCouponUserBizModel.java:235-247`
- **证据片段**: useCoupon 先查 status==0 后改 1，无乐观锁/原子 UPDATE
- **严重程度**: P1
- **建议**: 原子条件 UPDATE `where id=? and status=0`
- **复核状态**: 已保留

### [维度09-09] 团购过期退款与售后退款时序竞态，可能双重退款 — P1
- **文件**: `LitemallGrouponBizModel.java:182-260`、`LitemallAftersaleBizModel.java:138-156`
- **证据片段**: 售后 apply 不改 orderStatus；expireGroupons 只看 orderStatus==PAY，两条退款路径 outRefundNo 不复用
- **严重程度**: P1
- **复核状态**: 已保留（与 11-P2-8 同源并深化）

### [维度09-10] submit 直接用 cart.price 而非当前 product.price — P1
- **文件**: `LitemallOrderBizModel.java:181-198`
- **证据片段**: orderGoods.setPrice(item.getPrice()) 来自 cart 快照；违反 product-catalog.md:121/order-and-cart.md:240
- **严重程度**: P1
- **建议**: 循环里已查 product，直接用 product.getPrice()
- **复核状态**: 已保留

### [维度09-11] cancel/prepay/pay/ship/confirm 不校验订单本人或管理员身份 — P1
- **文件**: `LitemallOrderBizModel.java:272-304` 等
- **证据片段**: getMyOrder 做了本人校验，但 cancel 等无；cancel 触发库存回滚/券归还副作用
- **严重程度**: P1
- **建议**: 用户面 mutation 加 requireUserIdMatch；管理员面加权限注解
- **复核状态**: 已保留

### [维度09-12] aftersale.refund 全量回滚 orderGoods 库存，部分退货多退 — P2
### [维度09-13] notificationService 在 @BizMutation 事务内调用（外部 IO） — P2
### [维度09-14] prepay intValue() 与 refund longValue() 金额处理不一致 — P2
### [维度09-15] myOrders 魔法值 -1 表示待支付 — P3
### [维度09-16] myOrders 无分页 — P3

（P2/P3 完整证据见各对应文件行号，保留）

## 维度复核结论

主 agent 对 5 个 P0 全部独立复核（直读源码）：
- 09-01：确认 pay() 无 queryPayment 调用 ✓
- 09-02：确认 WxPayNotifyResource:39-41 返回值丢弃 ✓
- 09-03：确认 BizModel:197 reduceStock 返回值未接收 ✓
- 09-04/09-05：直读 refund()/apply() 确认无金额校验、GOODS_MISS 不回滚 ✓
6 个 P1 与维度 01/11 多处交叉印证。全部保留，无降级驳回。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 09-01 | P0 | LitemallOrderBizModel.java:337-357 | pay() 无支付凭证校验 |
| 09-02 | P0 | WxPayNotifyResource.java:26-53 | 微信回调不更新订单状态 |
| 09-03 | P0 | LitemallOrderBizModel.java:197 | reduceStock 返回值未检查致超卖 |
| 09-04 | P0 | LitemallAftersaleBizModel.java:138-182 | 售后退款金额无校验 |
| 09-05 | P0 | LitemallAftersaleBizModel.java:122-129 | GOODS_MISS 未收货退款不回滚库存 |
| 09-06 | P1 | LitemallAftersaleBizModel.java:96-134 | 订单 202/203 死代码 |
| 09-07 | P1 | LitemallCouponUserBizModel.java:50-93 | 优惠券领取竞态+total 语义双关 |
| 09-08 | P1 | LitemallCouponUserBizModel.java:235-247 | 优惠券使用并发重复核销 |
| 09-09 | P1 | LitemallGrouponBizModel.java:182-260 | 团购/售后双重退款竞态 |
| 09-10 | P1 | LitemallOrderBizModel.java:181-198 | submit 用 cart 旧价非当前价 |
| 09-11 | P1 | LitemallOrderBizModel.java:272-304 | 订单操作不校验本人/管理员 |
| 09-12 | P2 | LitemallAftersaleBizModel.java:122-129 | refund 全量回滚库存 |
| 09-13 | P2 | LitemallOrderBizModel.java:265 等 | 通知在事务内调用 |
| 09-14 | P2 | WxPayServiceImpl.java:153,221 | prepay/refund 金额类型不一致 |
| 09-15 | P3 | LitemallOrderBizModel.java:437-443 | myOrders 魔法值 -1 |
| 09-16 | P3 | LitemallOrderBizModel.java:429-446 | myOrders 无分页 |

## 维度评级：Poor

电商域逻辑存在 5 个 P0 级资金/超卖/状态机缺陷，叠加 6 个 P1 契约漂移与并发竞态。状态机、并发、支付回调链路三个核心维度都有系统性问题，距离"商业级电商"基线有明显差距，投产前必须修复。
