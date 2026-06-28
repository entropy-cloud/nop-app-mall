package app.mall.service.pay;

import app.mall.biz.ILitemallOrderBiz;
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
 * endpoint (app-mall-wx, which cannot depend on the order/recharge services) to the order and
 * recharge state machines.
 *
 * <p>Routing by outTradeNo prefix:
 * <ul>
 *   <li>{@code RC...} → wallet recharge (derived outTradeNo = "RC" + zero-padded rechargeId).
 *       See {@code LitemallRechargeBizModel.deriveOutTradeNo}.</li>
 *   <li>otherwise → order (outTradeNo == order.getOrderSn(), a 32-char lowercase hex UUID that
 *       never starts with the uppercase {@code RC} prefix — see plan RC-routing invariant).</li>
 * </ul>
 *
 * <p><b>RC-routing invariant:</b> if a future {@code generateOrderSn()} introduces an alphabetic
 * prefix, the RC routing split must be re-evaluated.
 *
 * <p>Runs under a system context (no end-user session) since the trigger is an async WeChat server
 * callback, not a user request.
 */
@Named("paymentCallback")
public class PaymentCallbackImpl implements IPaymentCallback {

    static final Logger LOG = LoggerFactory.getLogger(PaymentCallbackImpl.class);

    static final String RECHARGE_OUT_TRADE_NO_PREFIX = "RC";

    @Inject
    ILitemallOrderBiz orderBiz;

    @Inject
    ILitemallRechargeBiz rechargeBiz;

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
        // Route by outTradeNo prefix: RC -> recharge, otherwise -> order. Both confirm*ByNotify
        // entries are idempotent and return quietly for unknown / already-paid records, so WeChat
        // gets a 200 and stops retrying those. A real failure (e.g. DB error) propagates, making
        // WxPayNotifyResource return 5xx so WeChat retries the verified notify.
        if (outTradeNo != null && outTradeNo.startsWith(RECHARGE_OUT_TRADE_NO_PREFIX)) {
            rechargeBiz.confirmRechargeByNotify(outTradeNo, transactionId, systemContext);
            LOG.info("onPaymentSuccess: confirmed recharge for outTradeNo={}, transactionId={}",
                    outTradeNo, transactionId);
        } else {
            orderBiz.confirmPaidByNotify(outTradeNo, transactionId, systemContext);
            LOG.info("onPaymentSuccess: confirmed order payment for outTradeNo={}, transactionId={}",
                    outTradeNo, transactionId);
        }
    }

    /**
     * Refund async-notify reconciliation (P30 order-side + P29 deferred successor recharge-side).
     *
     * <p>The synchronous refund path ({@code LitemallAftersaleBizModel.refund} / groupon / pin-tuan
     * refund) already advances the order and aftersale status on a synchronous channel success.
     * This async notify is the authoritative channel-side confirmation that the money movement
     * actually completed. It is <b>idempotent</b>: if the sync path already advanced the order to a
     * refunded-terminal state, this is a no-op (so channel retries are always safe). If the order is
     * NOT yet in a refunded state, the sync path did not run (or failed) — we log a reconciliation
     * warning for manual intervention rather than silently advancing, because the async notify alone
     * does not carry enough context (which aftersale line, restock, coupon/points return) to drive
     * the full refund side-effects safely.
     *
     * <p><b>Recharge-side reversal (P29 deferred successor):</b> when the refunded outTradeNo is a
     * recharge payment ({@code RC} prefix), the notify is routed to
     * {@code rechargeBiz.refundRechargeByNotify} which debits amount+giftAmount back from the wallet
     * and advances {@code payStatus} PAID → REFUNDED. Idempotent (REFUNDED/UNPAID no-op) and
     * degrades safely on insufficient balance (WARN, no advance). See
     * {@code LitemallRechargeBizModel.refundRechargeByNotify}.
     */
    @Override
    public void onRefundSuccess(String outTradeNo, String outRefundNo) {
        IServiceContext systemContext = new ServiceContextImpl();
        if (outTradeNo == null || outTradeNo.isEmpty()) {
            LOG.warn("onRefundSuccess: empty outTradeNo, ignoring");
            return;
        }
        // Recharge-channel refund: reverse the wallet credit (amount+giftAmount) and advance payStatus.
        if (outTradeNo.startsWith(RECHARGE_OUT_TRADE_NO_PREFIX)) {
            rechargeBiz.refundRechargeByNotify(outTradeNo, outRefundNo, systemContext);
            LOG.info("onRefundSuccess: reconciled recharge-channel refund for outTradeNo={}, outRefundNo={}",
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
