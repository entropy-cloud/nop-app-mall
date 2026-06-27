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
import app.mall.dao.entity.LitemallOrder;

import java.util.Map;

import java.math.BigDecimal;
import java.util.List;

public interface ILitemallOrderBiz extends ICrudBiz<LitemallOrder> {

    @BizMutation
    LitemallOrder submit(@Name("addressId") String addressId,
                         @Optional @Name("message") String message,
                         @Name("freightPrice") BigDecimal freightPrice,
                         @Optional @Name("couponUserId") String couponUserId,
                         @Optional @Name("grouponRulesId") String grouponRulesId,
                         @Optional @Name("grouponId") String grouponId,
                         @Optional @Name("usePoints") Integer usePoints,
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
}
