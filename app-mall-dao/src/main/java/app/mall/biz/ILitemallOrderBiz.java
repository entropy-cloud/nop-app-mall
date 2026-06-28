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
import app.mall.dao.entity.LitemallOrder;

import java.util.Map;

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

    @BizMutation
    int cancelExpiredOrders(@Name("timeoutMinutes") int timeoutMinutes,
                            IServiceContext context);

    @BizMutation
    int confirmExpiredOrders(@Name("timeoutMinutes") int timeoutMinutes,
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
}
