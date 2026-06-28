package app.mall.pay;

/**
 * Pluggable payment channel abstraction (P30). Each concrete channel (WeChat Native,
 * Alipay H5/mini-program, wallet balance, ...) implements this strategy interface and is
 * registered in {@link IPayChannelRegistry}, which routes by {@link #getCode()}.
 *
 * <p>The {@code PayService} interface remains as a WeChat-specific compatibility facade so
 * existing injection points (order {@code prepay}, recharge {@code createRecharge},
 * aftersale {@code refund}) keep working unchanged. New multi-channel routing goes through
 * the registry.
 *
 * <p>{@code getCode()} returns the dict code string (e.g. {@code "WECHAT"}, {@code "ALIPAY"},
 * {@code "BALANCE"}), aligned with the {@code mall/pay-channel} dictionary in
 * {@code model/app-mall.orm.xml}. Callers that persist the channel on an entity map the
 * string code to the dict int value via {@code _AppMallDaoConstants.PAY_CHANNEL_*}.
 */
public interface PayChannel {

    /**
     * Dict code of this channel (e.g. {@code "WECHAT"}). Stable identifier used by the
     * registry for routing and by {@code getEnabledPayChannels} for filtering.
     */
    String getCode();

    /**
     * Whether this channel's integration is usable at runtime. For third-party channels
     * (WeChat, Alipay) this reflects the SDK {@code enabled} flag / credential availability
     * (example/demo fallback returns {@code false}). For internal channels (balance) it
     * reflects whether the channel is wired.
     *
     * <p>Note: this is the <b>capability</b> flag, independent of the operator enable/disable
     * toggle stored in {@code LitemallSystem} {@code pay_channels} config. The registry
     * combines both (a channel is exposed only when {@code isEnabled()==true} AND the
     * operator toggle is on, with WeChat enabled-by-default and others disabled-by-default).
     */
    boolean isEnabled();

    /**
     * Create a prepay order on this channel (returns codeUrl / payUrl / tradeType etc.).
     * Mirrors {@link PayService#createPayment} so the WeChat facade and the channel strategy
     * stay structurally aligned.
     */
    PayPrepayResponseBean prepay(PayPrepayRequestBean req);

    /**
     * Query the payment status of {@code outTradeNo} on this channel.
     */
    PayStatusResponseBean query(String outTradeNo);

    /**
     * Refund via this channel. Returns the refund result; failure is reported via
     * {@link PayRefundResponseBean#isSuccess()} (does not throw for ordinary refund declines).
     */
    PayRefundResponseBean refund(PayRefundRequestBean req);
}
