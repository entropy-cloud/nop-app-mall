package app.mall.service.pay;

import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallRechargeBiz;
import app.mall.pay.IPaymentCallback;
import io.nop.core.context.IServiceContext;
import io.nop.core.context.ServiceContextImpl;
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
}
