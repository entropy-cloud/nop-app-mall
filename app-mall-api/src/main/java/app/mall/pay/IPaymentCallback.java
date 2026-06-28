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

    /**
     * Invoked after a refund async notify (WeChat/Alipay → server) signature verification
     * succeeds and the refund status indicates the refund completed at the channel/bank level.
     *
     * <p>Reconciliation layer (P30, closes the P29 deferred "退款异步通知流程"): the synchronous
     * refund path ({@code LitemallAftersaleBizModel.refund} / groupon/pin-tuan refund) already
     * advances status on a synchronous success; this async notify is the authoritative
     * channel-side confirmation. Implementations MUST be idempotent — a refund already marked
     * complete is a no-op so channel retries are safe.
     *
     * @param outTradeNo  the order's orderSn the refund was issued against
     * @param outRefundNo the refund identifier (may be null in some channel modes)
     */
    void onRefundSuccess(String outTradeNo, String outRefundNo);
}
