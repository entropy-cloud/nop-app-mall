package app.mall.service.pay;

import app.mall.biz.ILitemallOrderBiz;
import app.mall.pay.IPaymentCallback;
import io.nop.core.context.IServiceContext;
import io.nop.core.context.ServiceContextImpl;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service-layer implementation of {@link IPaymentCallback}. Bridges the WeChat Pay notify
 * endpoint (app-mall-wx, which cannot depend on the order service) to the order state
 * machine via {@code ILitemallOrderBiz.confirmPaidByNotify}.
 *
 * <p>Runs under a system context (no end-user session) since the trigger is an async
 * WeChat server callback, not a user request.
 */
@Named("paymentCallback")
public class PaymentCallbackImpl implements IPaymentCallback {

    static final Logger LOG = LoggerFactory.getLogger(PaymentCallbackImpl.class);

    @Inject
    ILitemallOrderBiz orderBiz;

    @Override
    public void onPaymentSuccess(String outTradeNo, String transactionId) {
        // System context (no end-user session): the trigger is an async WeChat server callback
        IServiceContext systemContext = new ServiceContextImpl();
        // confirmPaidByNotify is idempotent and returns quietly for unknown / already-paid orders,
        // so WeChat gets a 200 and stops retrying those. A real failure (e.g. DB error) propagates,
        // making WxPayNotifyResource return 5xx so WeChat retries the verified notify.
        orderBiz.confirmPaidByNotify(outTradeNo, transactionId, systemContext);
        LOG.info("onPaymentSuccess: confirmed payment for outTradeNo={}, transactionId={}",
                outTradeNo, transactionId);
    }
}
