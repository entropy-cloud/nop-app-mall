
package app.mall.biz;

import app.mall.dao.entity.LitemallPointsExchangeOrder;
import io.nop.api.core.annotations.biz.BizAction;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.util.Map;

public interface ILitemallPointsExchangeOrderBiz extends ICrudBiz<LitemallPointsExchangeOrder> {

    @BizMutation
    LitemallPointsExchangeOrder exchange(@Name("pointsGoodsId") String pointsGoodsId,
                                         @Name("quantity") Integer quantity,
                                         @Optional @Name("addressId") String addressId,
                                         IServiceContext context);

    @BizQuery
    Map<String, Object> myExchangeOrders(@Optional @Name("exchangeStatus") Integer exchangeStatus,
                                         @Optional @Name("page") Integer page,
                                         @Optional @Name("pageSize") Integer pageSize,
                                         IServiceContext context);

    @BizMutation
    @Auth(roles = "admin")
    LitemallPointsExchangeOrder shipExchangeOrder(@Name("id") String id,
                                                  @Name("shipCode") String shipCode,
                                                  IServiceContext context);

    @BizMutation
    LitemallPointsExchangeOrder confirmExchangeOrder(@Name("id") String id, IServiceContext context);

    @BizMutation
    LitemallPointsExchangeOrder cancelExchangeOrder(@Name("id") String id,
                                                    @Optional @Name("remark") String remark,
                                                    IServiceContext context);

    // ===== 积分+现金组合兑换（successor） =====

    /**
     * 组合兑换建单（E3：积分先扣 + 现金后付）。校验 PointsGoods.cashPrice>0 → 扣积分（spendPoints）
     * → 扣 exchangeStock → 建单 exchangeStatus=AWAITING_PAYMENT + cashPrice 快照 → 派生 outTradeNo
     * （PE 前缀）→ 返回收银参数（outTradeNo / cashPrice / totalCash）。现金支付由后续
     * {@link #payComboByChannel} / {@link #payComboByBalance} 完成。
     */
    @BizMutation
    Map<String, Object> exchangeCombo(@Name("pointsGoodsId") String pointsGoodsId,
                                      @Name("quantity") Integer quantity,
                                      @Optional @Name("addressId") String addressId,
                                      IServiceContext context);

    /**
     * 组合兑换现金部分——第三方通道（微信/支付宝）预下单。调 payChannel.prepay 返回 codeUrl，
     * 订单保持 AWAITING_PAYMENT 直到支付回调 {@link #confirmExchangePaidByNotify} 推进到 PENDING。
     */
    @BizMutation
    Map<String, Object> payComboByChannel(@Name("id") String id,
                                          @Name("payChannelCode") String payChannelCode,
                                          IServiceContext context);

    /**
     * 组合兑换现金部分——余额支付（E5：直接 debitBalance，不经 BalancePayChannel）。同步扣余额、
     * 设 payChannel=BALANCE / walletPayAmount，推进 AWAITING_PAYMENT→PENDING。需登录密码确认凭证。
     */
    @BizMutation
    LitemallPointsExchangeOrder payComboByBalance(@Name("id") String id,
                                                  @Name("confirmCredential") String confirmCredential,
                                                  IServiceContext context);

    /**
     * 支付回调（E4）：第三方通道支付成功后由 PaymentCallbackImpl 按 PE outTradeNo 前缀路由调用。
     * 幂等推进 AWAITING_PAYMENT→PENDING，设 payChannel（微信/支付宝）+ payStatus=PAID。资金安全内部入口，
     * 非 GraphQL 暴露。
     */
    @BizAction
    void confirmExchangePaidByNotify(@Name("outTradeNo") String outTradeNo,
                                     @Name("transactionId") String transactionId,
                                     IServiceContext context);

    /**
     * 退款回调（E4）：第三方通道退款成功后由 PaymentCallbackImpl 按 PE 前缀路由调用。幂等（REFUNDED/UNPAID
     * no-op，仅 PAID 推进），advance payStatus PAID→REFUNDED。资金安全内部入口，非 GraphQL 暴露。
     */
    @BizAction
    void refundExchangeOrderByNotify(@Name("outTradeNo") String outTradeNo,
                                     @Optional @Name("outRefundNo") String outRefundNo,
                                     IServiceContext context);

    /**
     * 超时取消未支付组合单（E3 兜底）：扫描 AWAITING_PAYMENT 且超过 timeoutMinutes 的单，还库存 + 退积分
     * （现金未付无需退款）。系统定时任务入口（MallJobInvoker）。
     */
    @BizMutation
    int cancelExpiredExchangeOrders(@Name("timeoutMinutes") int timeoutMinutes,
                                    IServiceContext context);
}
