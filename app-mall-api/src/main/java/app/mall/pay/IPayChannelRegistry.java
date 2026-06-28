package app.mall.pay;

import java.util.List;

/**
 * Registry of all {@link PayChannel} beans. Routes by channel code and filters by the
 * operator enable/disable config stored in {@code LitemallSystem} {@code pay_channels}.
 *
 * <p>Implementation lives in {@code app-mall-service} so it can read {@code LitemallSystem}
 * config via {@code ILitemallSystemBiz}. Channel implementations (WeChat in {@code app-mall-wx},
 * balance/alipay) are discovered by IoC as {@link PayChannel} beans.
 */
public interface IPayChannelRegistry {

    /**
     * All channels whose {@link PayChannel#isEnabled()} is true AND whose operator toggle is
     * enabled in {@code pay_channels} config (WeChat enabled by default, others disabled).
     * Ordered by a stable display order. Does NOT filter by order context.
     */
    List<PayChannel> getEnabledChannels();

    /**
     * Resolve a channel by code. Returns {@code null} when the code is unknown or the channel
     * bean is not registered (callers decide whether to treat that as an error).
     */
    PayChannel getChannel(String code);
}
