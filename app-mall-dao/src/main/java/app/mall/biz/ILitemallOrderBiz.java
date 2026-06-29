package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.dto.GoodsStatisticsBean;
import app.mall.dao.dto.OrderStatisticsBean;
import app.mall.dao.dto.UserStatisticsBean;
import app.mall.dao.dto.BatchShipResultBean;
import app.mall.dao.dto.VerifyPickupResultBean;
import app.mall.dao.dto.DashboardMetricsBean;
import app.mall.dao.dto.SalesTrendPointBean;
import app.mall.dao.dto.TodoAggregationBean;
import app.mall.dao.dto.SalesFunnelBean;
import app.mall.dao.dto.ProductAnalysisBean;
import app.mall.dao.dto.UserRetentionPointBean;
import app.mall.dao.dto.RfmSegmentBean;
import app.mall.dao.dto.LifecycleSegmentBean;
import app.mall.dao.dto.RepurchaseRatePointBean;
import app.mall.dao.dto.OrderAnalysisBean;
import app.mall.dao.dto.CouponUsageStatisticsBean;
import app.mall.dao.dto.UserPortraitBean;
import app.mall.dao.dto.SegmentMemberBean;
import app.mall.dao.dto.PayChannelViewBean;
import app.mall.dao.entity.LitemallOrder;

import java.util.Map;

import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.WebContentBean;
import java.math.BigDecimal;
import java.util.List;

public interface ILitemallOrderBiz extends ICrudBiz<LitemallOrder> {

    @BizMutation
    LitemallOrder submit(@Optional @Name("addressId") String addressId,
                         @Optional @Name("message") String message,
                         @Name("freightPrice") BigDecimal freightPrice,
                         @Optional @Name("couponUserId") String couponUserId,
                         @Optional @Name("grouponRulesId") String grouponRulesId,
                         @Optional @Name("grouponId") String grouponId,
                         @Optional @Name("usePoints") Integer usePoints,
                         @Optional @Name("pinTuanActivityId") String pinTuanActivityId,
                         @Optional @Name("pinTuanGroupId") String pinTuanGroupId,
                         @Optional @Name("deliveryType") Integer deliveryType,
                         @Optional @Name("pickupStoreId") String pickupStoreId,
                         IServiceContext context);

    @BizMutation
    LitemallOrder cancel(@Name("orderId") String orderId,
                         IServiceContext context);

    @BizMutation
    Map<String, Object> prepay(@Name("orderId") String orderId,
                               IServiceContext context);

    @BizMutation
    LitemallOrder pay(@Name("orderId") String orderId,
                      IServiceContext context);

    /**
     * Cashier channel catalog (P30). Returns the payment channels available for {@code orderId}
     * at {@code /storefront-pay}: channels whose capability ({@code PayChannel.isEnabled()})
     * AND operator toggle ({@code pay_channels} config) are on, with the calling user's wallet
     * balance attached to the BALANCE channel. Ownership-checked (the order must belong to the
     * caller). Replaces the hardcoded WeChat-only cashier with a dynamic channel list.
     */
    @BizQuery
    List<PayChannelViewBean> getEnabledPayChannels(@Name("orderId") String orderId,
                                                     IServiceContext context);

    /**
     * Balance-payment channel (P30). Confirms a CREATED order with wallet balance:
     * validates ownership + status + balance ≥ actualPrice + confirm credential (login password,
     * Decision B), atomically debits the wallet ({@code WALLET_CHANGE_TYPE_PAY +
     * sourceType=pay}), writes {@code walletPayAmount} + {@code payChannel=20(BALANCE)}, then
     * advances the order to PAID via {@link #markOrderPaidCore}.
     *
     * <p>Double-layer idempotency: the CREATED(101) status guard rejects a repeat call
     * ({@code ERR_ORDER_NOT_ALLOW_PAY}), and {@code debitBalance}'s optimistic lock rejects
     * concurrent double-debit ({@code ERR_WALLET_VERSION_CONFLICT}).
     */
    @BizMutation
    LitemallOrder payByBalance(@Name("orderId") String orderId,
                                @Name("confirmCredential") String confirmCredential,
                                IServiceContext context);

    /**
     * Combo-payment channel (successor of P30 deferred「跨通道组合支付」): balance-deduct partial +
     * third-party channel (WeChat) covers the remainder, in one cashier action. Validates ownership +
     * status(101) + combo re-entry guard ({@code walletPayAmount>0} → ERR_ORDER_COMBO_PENDING) +
     * {@code useBalanceAmount>0} + confirm credential (Decision B) + balance ≥ useBalanceAmount,
     * atomically debits {@code debitAmount = min(useBalanceAmount, actualPrice)}, writes
     * {@code walletPayAmount=debitAmount}. If {@code remainder = actualPrice − debitAmount == 0} the
     * order degenerates to a full-balance payment ({@code payChannel=BALANCE} + markOrderPaidCore).
     * Otherwise {@code payService.createPayment(totalFee=remainder)} records {@code payId}, the order
     * stays at 101 until the WeChat async notify (confirmPaidByNotify) advances it to PAID with
     * {@code payChannel=WECHAT} + the persisted {@code walletPayAmount}.
     *
     * <p>Combo re-entry guard (Decision D1): while an order is in the combo pending window (101 and
     * {@code walletPayAmount>0}), {@code pay}/{@code payByBalance}/{@code prepay}/{@code payWithCombo}
     * all reject re-entry, preventing a second debit / second prepay against the same order.
     */
    @BizMutation
    Map<String, Object> payWithCombo(@Name("orderId") String orderId,
                                       @Name("useBalanceAmount") BigDecimal useBalanceAmount,
                                       @Name("confirmCredential") String confirmCredential,
                                       IServiceContext context);

    /**
     * Trusted internal entry: drive a CREATED order to PAY after WeChat Pay async notify
     * signature verification succeeds. Idempotent — already-PAY orders are skipped.
     * Invoked by {@code WxPayNotifyResource} via {@code IPaymentCallback}; not a user-facing
     * confirmation (untrusted client pay() is gated separately).
     */
    @BizMutation
    void confirmPaidByNotify(@Name("outTradeNo") String outTradeNo,
                             @Name("transactionId") String transactionId,
                             IServiceContext context);

    @BizMutation
    LitemallOrder ship(@Name("orderId") String orderId,
                       @Name("shipSn") String shipSn,
                       @Name("shipChannel") String shipChannel,
                       IServiceContext context);

    @BizMutation
    LitemallOrder confirm(@Name("orderId") String orderId,
                          IServiceContext context);

    /**
     * 门店自提核销（P31）。按 pickupCode 反查订单，校验状态（须为已支付且 deliveryType=PICKUP），
     * 幂等（已核销跳过），推进到 401 终态并复制 confirm 的真实收货副作用（积分赠送 + 写 pickupTime），
     * 不复用 ship 的通知/日志（自提无发货语义）。{@code @Auth(roles="admin")} 限定管理员/门店员可达。
     */
    @BizMutation
    VerifyPickupResultBean verifyPickupOrder(@Name("pickupCode") String pickupCode,
                                              IServiceContext context);

    @BizMutation
    void deleteOrder(@Name("orderId") String orderId,
                     IServiceContext context);

    @BizQuery
    List<LitemallOrder> myOrders(@Optional @Name("status") Integer status,
                                 IServiceContext context);

    @BizQuery
    LitemallOrder getMyOrder(@Name("orderId") String orderId,
                             IServiceContext context);

    @BizQuery
    OrderStatisticsBean getOrderStatistics(@Optional @Name("startDate") String startDate,
                                           @Optional @Name("endDate") String endDate,
                                           IServiceContext context);

    @BizQuery
    List<GoodsStatisticsBean> getGoodsSalesRanking(@Optional @Name("startDate") String startDate,
                                                    @Optional @Name("endDate") String endDate,
                                                    @Optional @Name("limit") Integer limit,
                                                    IServiceContext context);

    @BizQuery
    UserStatisticsBean getUserStatistics(@Optional @Name("startDate") String startDate,
                                          @Optional @Name("endDate") String endDate,
                                          IServiceContext context);

    /**
     * 经营看板核心指标卡（P18）。今日 GMV/同环比、订单数、UV、转化率、客单价、退货率。
     * 口径见 docs/design/system-configuration.md「报表与统计」。
     */
    @BizQuery
    DashboardMetricsBean getDashboardMetrics(IServiceContext context);

    /**
     * 销售趋势（P18）。按 granularity(hour/day/week/month) + 时间区间返回时序点。
     */
    @BizQuery
    List<SalesTrendPointBean> getSalesTrend(@Optional @Name("granularity") String granularity,
                                             @Optional @Name("startDate") String startDate,
                                             @Optional @Name("endDate") String endDate,
                                             IServiceContext context);

    /**
     * 实时订单流（P18）。返回最近 N 条订单（默认 20）。
     */
    @BizQuery
    List<LitemallOrder> getRealtimeOrders(@Optional @Name("limit") Integer limit,
                                          IServiceContext context);

    /**
     * 待办聚合（P18）。待发货/待退款/售后待审核/库存预警计数 + 库存预警明细。
     */
    @BizQuery
    TodoAggregationBean getTodoAggregation(IServiceContext context);

    /**
     * 销售漏斗（P19）。浏览→加购→下单→支付→复购 5 段同期对比 + 各段转化率。
     * 口径见 docs/design/system-configuration.md「报表与统计」。
     */
    @BizQuery
    SalesFunnelBean getSalesFunnel(@Optional @Name("startDate") String startDate,
                                    @Optional @Name("endDate") String endDate,
                                    IServiceContext context);

    /**
     * 商品分析（P19）。销量排行、加购排行、滞销品、动销率，支持类目筛选。
     * 成本/毛利为内部数据，admin-only（{@code @Auth(roles="admin")}，与 {@link #exportReport} 同边界）。
     * 口径见 docs/design/system-configuration.md「报表与统计」。
     */
    @BizQuery
    @io.nop.api.core.annotations.directive.Auth(roles = "admin")
    ProductAnalysisBean getProductAnalysis(@Optional @Name("startDate") String startDate,
                                           @Optional @Name("endDate") String endDate,
                                           @Optional @Name("categoryId") String categoryId,
                                           IServiceContext context);

    /**
     * 用户留存时序（P19）。次留/7 留/30 留按首次支付日分组，以支付订单为留存事件。
     * 口径见 docs/design/system-configuration.md「报表与统计」。
     */
    @BizQuery
    List<UserRetentionPointBean> getUserRetention(@Optional @Name("startDate") String startDate,
                                                   @Optional @Name("endDate") String endDate,
                                                   IServiceContext context);

    /**
     * RFM 分层用户分布（P19）。R=最近支付距今天数、F=期间支付订单数、M=期间支付金额，按三分位分段。
     * 口径见 docs/design/system-configuration.md「报表与统计」。
     */
    @BizQuery
    List<RfmSegmentBean> getUserRfm(@Optional @Name("startDate") String startDate,
                                     @Optional @Name("endDate") String endDate,
                                     IServiceContext context);

    /**
     * 生命周期分布（P19）。新客/活跃/沉睡/流失 按支付 recency 派生。
     * 口径见 docs/design/system-configuration.md「报表与统计」。
     */
    @BizQuery
    List<LifecycleSegmentBean> getUserLifecycle(@Optional @Name("startDate") String startDate,
                                                 @Optional @Name("endDate") String endDate,
                                                 @Optional @Name("churnDays") Integer churnDays,
                                                 IServiceContext context);

    /**
     * 复购率时序（P19）。按天分组，复购率=期间≥2单支付用户数/期间支付用户数。
     * 口径见 docs/design/system-configuration.md「报表与统计」。
     */
    @BizQuery
    List<RepurchaseRatePointBean> getRepurchaseRate(@Optional @Name("startDate") String startDate,
                                                     @Optional @Name("endDate") String endDate,
                                                     IServiceContext context);

    /**
     * 订单分析（P19）。客单价分布 + 支付方式占比 + 退货原因占比。
     * 口径见 docs/design/system-configuration.md「报表与统计」。
     */
    @BizQuery
    OrderAnalysisBean getOrderAnalysis(@Optional @Name("startDate") String startDate,
                                        @Optional @Name("endDate") String endDate,
                                        IServiceContext context);

    /**
     * 优惠券分析（P19 营销分析）。领取数/核销数/核销率/拉动 GMV。
     * 口径见 docs/design/system-configuration.md「报表与统计」。
     */
    @BizQuery
    CouponUsageStatisticsBean getCouponAnalysis(@Optional @Name("startDate") String startDate,
                                                  @Optional @Name("endDate") String endDate,
                                                  IServiceContext context);

    /**
     * 单用户算法画像（P20 算法化用户画像 successor）。all-time / 当前快照口径：
     * 返回该用户的 R/F/M 原始值 + RFM 段（对 all-time R/F/M 用全量阈值分类，复用 labelRfm）+
     * 生命周期阶段（活跃窗口=近 30 天、churn=90 天判定，复用 classifyLifecycleStage）+ 首末单时间。
     * 无消费用户返回「未消费」画像（rfmSegment/lifecycleStage 空 + 计数 0）。
     * 口径见 docs/design/system-configuration.md「报表与统计 - 用户分析」。
     */
    @BizQuery
    UserPortraitBean getUserPortrait(@Name("userId") String userId,
                                      IServiceContext context);

    /**
     * 算法化分群成员列表（P20 算法化用户画像 successor）。按 RFM 段（segmentType=rfm）或
     * 生命周期阶段（segmentType=lifecycle）圈选命中用户。all-time 口径与 {@link #getUserPortrait} 一致。
     * 全量用户逐个分类后 Java 端过滤命中段并分页（admin 上下文报表场景，沿用既有报表权限边界）。
     * 口径见 docs/design/system-configuration.md「报表与统计 - 用户分析」。
     */
    @BizQuery
    PageBean<SegmentMemberBean> getSegmentMembers(@Name("segmentType") String segmentType,
                                                   @Name("segmentValue") String segmentValue,
                                                   @Optional @Name("page") Integer page,
                                                   @Optional @Name("pageSize") Integer pageSize,
                                                   IServiceContext context);

    /**
     * Shared full-set segment member resolution (successor of P20 user-portrait). Loads all-time
     * payment summaries, computes RFM thresholds, classifies every paying user, and returns the
     * FULL sorted list matching {@code segmentValue} (no paging). {@code byRfm=true} matches the
     * RFM segment, {@code false} matches the lifecycle stage. Only users with a payment history
     * ({@code lastPayTime != null}) are classified — same guard as {@link #getSegmentMembers}.
     * Same all-time口径 as {@link #getUserPortrait}.
     *
     * <p>Internal helper (no {@code @BizQuery}/@BizMutation}/{@code @BizAction}, not GraphQL-exposed),
     * called by {@code getSegmentMembers} (paging wrapper) and by the segment-directed-marketing
     * successor ({@code LitemallUserMessageBizModel.sendSegmentMessage}, injected via
     * {@code ILitemallOrderBiz}) so the push action and the query page share one resolution path.
     * The caller is responsible for segmentType/segmentValue validation.
     */
    List<SegmentMemberBean> collectRfmLifecycleMatches(@Name("segmentValue") String segmentValue,
                                                        @Name("byRfm") boolean byRfm,
                                                        IServiceContext context);

    @BizMutation
    int cancelExpiredOrders(@Name("timeoutMinutes") int timeoutMinutes,
                            IServiceContext context);

    /**
     * Release the promotion participation quota an order occupies on a whole-order cancel/refund.
     * Internal helper (no {@code @BizMutation}/@BizQuery}/{@code @BizAction}, not GraphQL-exposed),
     * called from the 6 whole-order refund sites (cancel / cancelExpiredOrders / aftersale refund /
     * aftersale confirmReturnReceived / groupon refundGrouponOrder / pintuan refundMemberOrder).
     * Soft-deletes the PromotionUsage row(s) for this order so the user can re-participate within
     * maxPerUser. Idempotent: a repeat call finds 0 non-deleted rows. Partial-item refunds do NOT
     * call this (promotion is an order-level discount).
     */
    void releasePromotionUsage(@Name("orderId") String orderId, IServiceContext context);

    /**
     * Combo-aware refund split (successor of P30 deferred「跨通道组合支付」资金安全面). Shared helper
     * for the five whole-order/partial refund sites that previously called {@code payService.refund}
     * directly. For a combo order ({@code walletPayAmount>0} and {@code actualPrice>0}) the refund
     * amount is split by ratio: {@code walletPortion = refundAmount × walletPayAmount / actualPrice},
     * {@code channelPortion = refundAmount − walletPortion} (Decision D3, remainder归通道), with the
     * wallet portion credited back via {@code walletBiz.creditBalance(REFUND, sourceType=order-refund)}
     * and the channel portion via {@code payService.refund(totalFee=actualPrice, refundFee=channelPortion)}.
     * For a non-combo order ({@code walletPayAmount==0/null} or {@code actualPrice==0}) it falls back to
     * the legacy single-channel {@code payService.refund(totalFee=actualPrice, refundFee=refundAmount)}.
     *
     * <p>Returns {@code true} when the channel portion refunded successfully OR there was no channel
     * portion (pure-wallet refund). Returns {@code false} when the channel {@code payService.refund}
     * reported failure — callers retain their own domain-specific failure handling (aftersale throws
     * {@code ERR_AFTERSALE_REFUND_FAILED}, groupon/pintuan record to refundFailures, pickup-timeout
     * throws {@code ERR_PICKUP_AUTO_CANCEL_REFUND_FAILED}). The wallet {@code creditBalance} portion
     * throws {@code NopException} on optimistic-lock conflict (propagates to caller).
     *
     * <p>Internal helper (no {@code @BizMutation}/@BizQuery}/{@code @BizAction}, not GraphQL-exposed),
     * called from the five refund sites: aftersale refund / aftersale confirmReturnReceived /
     * cancelExpiredPickupOrders (non-BALANCE branch) / groupon refundGrouponOrder /
     * pintuan refundMemberOrder.
     */
    boolean refundComboAware(@Name("order") LitemallOrder order,
                              @Name("refundAmount") BigDecimal refundAmount,
                              IServiceContext context);

    @BizMutation
    int confirmExpiredOrders(@Name("timeoutMinutes") int timeoutMinutes,
                             IServiceContext context);

    /**
     * 自提订单超时自动取消并退款（successor of P31 deferred「已支付未自提订单自动超时取消/退款」）。
     * 扫描已支付(201) 且 deliveryType=PICKUP 且 payTime 早于 cutoff（{@code now - timeoutDays}）的订单，
     * CAS-guard 翻转 201→203(REFUND_CONFIRM)，按 {@code order.payChannel} 分流退款
     * （balance→wallet credit-back / 其余→payService.refund），并复用既有完整副作用链
     * （还库 / 还券 / 还积分 / 释放满减参与额度 / 通知 / 日志）。
     *
     * <p>{@code timeoutDays} 由调度层（{@code MallJobInvoker}）从 {@code mall_pickup_timeout_days}
     * config（缺省 14 天）读取后显式传入，便于本方法直接 API 直调与单元测试。
     */
    @BizMutation
    int cancelExpiredPickupOrders(@Name("timeoutDays") int timeoutDays,
                                   IServiceContext context);

    /**
     * Flash-sale direct-buy order creation (P24 Decision A — independent path, NOT submit()).
     * Creates a single-line order with flashPrice as the unit price. No coupon / promotion /
     * integral / groupon slots are wired (all zeroed). Intended for internal trusted invocation
     * from {@code LitemallFlashSaleBizModel.flashSaleBuy} after it validates session/activity/
     * stock and performs the atomic stock deductions.
     */
    @BizMutation
    LitemallOrder createFlashSaleOrder(@Name("userId") String userId,
                                        @Name("goodsId") String goodsId,
                                        @Name("productId") String productId,
                                        @Name("goodsName") String goodsName,
                                        @Name("goodsSn") String goodsSn,
                                        @Name("specifications") String specifications,
                                        @Name("picUrl") String picUrl,
                                        @Name("flashPrice") BigDecimal flashPrice,
                                        @Name("number") int number,
                                        @Name("consignee") String consignee,
                                        @Name("mobile") String mobile,
                                        @Name("address") String address,
                                        @Name("freightPrice") BigDecimal freightPrice,
                                        @Optional @Name("flashSaleSessionId") String flashSaleSessionId,
                                        IServiceContext context);

    // ===== 订单运营工作台（P21） =====

    /**
     * 改价 / 改运费（P21）。安全策略见 {@code docs/design/order-and-cart.md}：
     * 改运费（freightPrice）发货前任意态可改、仅重算 orderPrice/actualPrice；
     * 改商品价（goodsPrice）仅当 couponPrice/promotionPrice/integralPrice/grouponPrice/pinTuanPrice 全为 0
     * （纯商品订单，无任何活动折扣）时允许并重算；任一折扣非 0 拒绝。
     */
    @BizMutation
    LitemallOrder modifyOrderPrice(@Name("orderId") String orderId,
                                   @Optional @Name("freightPriceDelta") BigDecimal freightPriceDelta,
                                   @Optional @Name("goodsPriceDelta") BigDecimal goodsPriceDelta,
                                   @Optional @Name("remark") String remark,
                                   IServiceContext context);

    /**
     * 批量发货（P21）。Excel 经平台 {@code ExcelHelper.readSheet} 解析（orderSn/shipSn/shipChannel），
     * 逐行复用 {@link #ship} 单行逻辑（状态守卫 + 事务）；部分失败不阻断成功行。
     * {@code excelUpload} 为前端上传的 xlsx 文件路径（{@code /f/download/{fileId}}）。
     */
    @BizMutation
    List<BatchShipResultBean> batchShip(@Name("excelUpload") String excelUpload,
                                         IServiceContext context);

    /**
     * 改地址（P21）。仅发货前（待支付 101 或已支付未发货 201）可改；新地址经
     * {@code ILitemallAddressBiz} 校验归属同一用户后写入 consignee/mobile/address。
     */
    @BizMutation
    LitemallOrder changeOrderAddress(@Name("orderId") String orderId,
                                      @Name("addressId") String addressId,
                                      IServiceContext context);

    /**
     * 订单标记（P21）。写既有 {@code adminRemark} 字段（surface 既有列，无 ORM 改动）。
     */
    @BizMutation
    LitemallOrder markOrder(@Name("orderId") String orderId,
                             @Name("adminRemark") String adminRemark,
                             IServiceContext context);

    /**
     * 超期未发货订单查询（P21 异常监控）。{@code status=201} 且 {@code shipTime} 截止未发；
     * cutoff 复用系统配置（自动收货时长）。
     */
    @BizQuery
    List<LitemallOrder> getOverdueUnshippedOrders(@Optional @Name("cutoffHours") Integer cutoffHours,
                                                   IServiceContext context);

    /**
     * 超期未支付订单查询（P21 异常监控）。{@code status=101} 且 {@code addTime} 截止未付；
     * cutoff 复用系统配置（订单超时分钟数）。
     */
    @BizQuery
    List<LitemallOrder> getOverdueUnpaidOrders(@Optional @Name("cutoffMinutes") Integer cutoffMinutes,
                                                IServiceContext context);

    @BizQuery
    WebContentBean exportReport(@Name("reportName") String reportName,
                                @Name("renderType") String renderType,
                                @Optional @Name("startDate") String startDate,
                                @Optional @Name("endDate") String endDate,
                                @Optional @Name("categoryId") String categoryId,
                                IServiceContext context);
}
