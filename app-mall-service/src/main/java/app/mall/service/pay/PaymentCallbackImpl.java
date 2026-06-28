package app.mall.service.pay;

import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallPointsExchangeOrderBiz;
import app.mall.biz.ILitemallRechargeBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallOrder;
import app.mall.pay.IPaymentCallback;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.core.context.IServiceContext;
import io.nop.core.context.ServiceContextImpl;
import io.nop.dao.api.IDaoProvider;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service-layer implementation of {@link IPaymentCallback}. Bridges the WeChat Pay notify
 * endpoint (app-mall-wx, which cannot depend on the order/recharge/exchange services) to the
 * order, wallet-recharge and points-exchange state machines.
 *
 * <p>Routing by outTradeNo prefix:
 * <ul>
 *   <li>{@code RC...} → wallet recharge (derived outTradeNo = "RC" + zero-padded rechargeId).
 *       See {@code LitemallRechargeBizModel.deriveOutTradeNo}.</li>
 *   <li>{@code PE...} → points+cash combo exchange (derived outTradeNo = "PE" + zero-padded
 *       exchangeOrderId). See {@code LitemallPointsExchangeOrderBizModel.deriveOutTradeNo}.</li>
 *   <li>otherwise → order (outTradeNo == order.getOrderSn(), a 32-char lowercase hex UUID that
 *       never starts with the uppercase {@code RC}/{@code PE} prefix — see routing invariant).</li>
 * </ul>
 *
 * <p><b>Routing invariant:</b> if a future {@code generateOrderSn()} introduces an alphabetic
 * prefix, the RC/PE routing split must be re-evaluated. Order outTradeNo is a 32-char lowercase
 * hex UUID; uppercase prefixes {@code RC}/{@code PE} are disjoint from it by case + charset.
 *
 * <p>Runs under a system context (no end-user session) since the trigger is an async WeChat server
 * callback, not a user request.
 */
@Named("paymentCallback")
public class PaymentCallbackImpl implements IPaymentCallback {

    static final Logger LOG = LoggerFactory.getLogger(PaymentCallbackImpl.class);

    static final String RECHARGE_OUT_TRADE_NO_PREFIX = "RC";
    static final String EXCHANGE_OUT_TRADE_NO_PREFIX = "PE";

    @Inject
    ILitemallOrderBiz orderBiz;

    @Inject
    ILitemallRechargeBiz rechargeBiz;

    @Inject
    ILitemallPointsExchangeOrderBiz exchangeOrderBiz;

    // IDaoProvider used for the read-only refund-notify reconciliation lookup. The refund notify
    // only checks the order's current status (idempotency) — it does not drive BizModel mutations
    // (the sync refund path already advanced status). Using the dao directly here avoids the
    // lazy I*Biz proxy round-trip in this async bridge bean. See ai-defaults anti-pattern table:
    // IDaoProvider is permitted when I*Biz only provides a read the dao can serve directly.
    @Inject
    IDaoProvider daoProvider;

    @Override
    public void onPaymentSuccess(String outTradeNo, String transactionId) {
        // System context (no end-user session): the trigger is an async WeChat server callback
        IServiceContext systemContext = new ServiceContextImpl();
        // Route by outTradeNo prefix: RC -> recharge, PE -> combo exchange, otherwise -> order. All
        // confirm*ByNotify entries are idempotent and return quietly for unknown / already-processed
        // records, so WeChat gets a 200 and stops retrying those. A real failure (e.g. DB error)
        // propagates, making WxPayNotifyResource return 5xx so WeChat retries the verified notify.
        if (outTradeNo != null && outTradeNo.startsWith(RECHARGE_OUT_TRADE_NO_PREFIX)) {
            rechargeBiz.confirmRechargeByNotify(outTradeNo, transactionId, systemContext);
            LOG.info("onPaymentSuccess: confirmed recharge for outTradeNo={}, transactionId={}",
                    outTradeNo, transactionId);
        } else if (outTradeNo != null && outTradeNo.startsWith(EXCHANGE_OUT_TRADE_NO_PREFIX)) {
            exchangeOrderBiz.confirmExchangePaidByNotify(outTradeNo, transactionId, systemContext);
            LOG.info("onPaymentSuccess: confirmed combo-exchange payment for outTradeNo={}, transactionId={}",
                    outTradeNo, transactionId);
        } else {
            orderBiz.confirmPaidByNotify(outTradeNo, transactionId, systemContext);
            LOG.info("onPaymentSuccess: confirmed order payment for outTradeNo={}, transactionId={}",
                    outTradeNo, transactionId);
        }
    }

    /**
     * Refund async-notify reconciliation (P30 order-side + P29 recharge-side + combo-exchange-side).
     *
     * <p>The synchronous refund path already advances the order/exchange status on a synchronous
     * channel success. This async notify is the authoritative channel-side confirmation that the
     * money movement actually completed. It is <b>idempotent</b>.
     *
     * <p><b>Combo-exchange reversal:</b> when the refunded outTradeNo is a combo-exchange payment
     * ({@code PE} prefix), the notify is routed to {@code exchangeOrderBiz.refundExchangeOrderByNotify}
     * which advances {@code payStatus} PAID → REFUNDED. The points/stock restoration already ran in
     * the synchronous cancel path ({@code cancelExchangeOrder}); this notify only reconciles the cash
     * refund status.
     */
    @Override
    public void onRefundSuccess(String outTradeNo, String outRefundNo) {
        IServiceContext systemContext = new ServiceContextImpl();
        if (outTradeNo == null || outTradeNo.isEmpty()) {
            LOG.warn("onRefundSuccess: empty outTradeNo, ignoring");
            return;
        }
        // Recharge-channel refund: reverse the wallet credit and advance payStatus.
        if (outTradeNo.startsWith(RECHARGE_OUT_TRADE_NO_PREFIX)) {
            rechargeBiz.refundRechargeByNotify(outTradeNo, outRefundNo, systemContext);
            LOG.info("onRefundSuccess: reconciled recharge-channel refund for outTradeNo={}, outRefundNo={}",
                    outTradeNo, outRefundNo);
            return;
        }
        // Combo-exchange refund: reconcile the cash refund (payStatus PAID → REFUNDED).
        if (outTradeNo.startsWith(EXCHANGE_OUT_TRADE_NO_PREFIX)) {
            exchangeOrderBiz.refundExchangeOrderByNotify(outTradeNo, outRefundNo, systemContext);
            LOG.info("onRefundSuccess: reconciled combo-exchange refund for outTradeNo={}, outRefundNo={}",
                    outTradeNo, outRefundNo);
            return;
        }
        LitemallOrder order = findOrderByOrderSn(outTradeNo, systemContext);
        if (order == null) {
            LOG.warn("onRefundSuccess: no order found for outTradeNo={}, ignoring", outTradeNo);
            return;
        }
        Integer status = order.getOrderStatus();
        if (status != null
                && (status == _AppMallDaoConstants.ORDER_STATUS_REFUND
                || status == _AppMallDaoConstants.ORDER_STATUS_REFUND_CONFIRM)) {
            LOG.info("onRefundSuccess: order {} already in refunded state {}, refund notify is a no-op (idempotent)",
                    order.getOrderSn(), status);
            return;
        }
        // Sync refund path did not advance the order. The async notify alone cannot safely drive
        // the full refund side-effects (restock / coupon / points / notification). Surface for manual
        // reconciliation — operator re-runs the refund through the aftersale flow or investigates.
        LOG.warn("onRefundSuccess: order {} is in state {} (not refunded-terminal); sync refund path did not advance. "
                + "outRefundNo={}. Manual reconciliation required.", order.getOrderSn(), status, outRefundNo);
    }

    private LitemallOrder findOrderByOrderSn(String orderSn, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderSn, orderSn));
        return (LitemallOrder) daoProvider.daoFor(LitemallOrder.class).findAllByQuery(query).stream()
                .findFirst().orElse(null);
    }
}
