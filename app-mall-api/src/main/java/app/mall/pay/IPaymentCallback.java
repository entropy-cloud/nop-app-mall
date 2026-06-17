package app.mall.pay;

/**
 * Bridge between the WeChat Pay notify endpoint (app-mall-wx, which cannot depend on the
 * order service/dao layers) and the order state machine (app-mall-service).
 *
 * <p>Implemented in app-mall-service; injected and invoked by {@code WxPayNotifyResource}
 * after the notify body passes signature verification, so that a verified SUCCESS payment
 * drives the order from CREATED to PAY rather than relying on the untrusted client-side
 * {@code pay()} call.
 */
public interface IPaymentCallback {

    /**
     * Invoked after WeChat notify signature verification succeeds and tradeState indicates
     * a successful payment for the given {@code outTradeNo} (== orderSn).
     *
     * @param outTradeNo   the order's orderSn (returned by parseNotifyBody)
     * @param transactionId WeChat transaction id (may be null in demo mode)
     */
    void onPaymentSuccess(String outTradeNo, String transactionId);
}
