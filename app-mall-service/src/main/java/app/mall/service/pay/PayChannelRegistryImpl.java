package app.mall.service.pay;

import app.mall.biz.ILitemallSystemBiz;
import app.mall.pay.IPayChannelRegistry;
import app.mall.pay.PayChannel;
import io.nop.core.context.IServiceContext;
import io.nop.core.lang.json.JsonTool;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default {@link IPayChannelRegistry} implementation. Collects every {@link PayChannel} bean
 * via {@code <ioc:collect-beans>} (declared in {@code app-service.beans.xml}) and routes by
 * {@link PayChannel#getCode()}.
 *
 * <p><b>Channel enable/disable model (P30 Decision C):</b>
 * <ul>
 *   <li>{@link PayChannel#isEnabled()} — the channel's own <i>capability</i> flag
 *       (SDK enabled / credentials wired). A channel that is not enabled is never exposed.</li>
 *   <li>{@code pay_channels} JSON in {@code LitemallSystem} — the operator <i>toggle</i>.
 *       Format: {@code [{"code":"WECHAT","enabled":true},{"code":"BALANCE","enabled":false},...]}.
 *       A channel is exposed only when BOTH its capability and its toggle are on.</li>
 *   <li>Default when no config / channel not listed in config: WeChat enabled, all others
 *       disabled (gray rollout per plan; backward compatible with the single-WeChat baseline).</li>
 * </ul>
 *
 * <p>Channel display order is fixed: WeChat, Alipay, Balance (then any unknown channels by
 * registration order), giving the cashier a stable list.
 */
@Named("payChannelRegistry")
public class PayChannelRegistryImpl implements IPayChannelRegistry {

    static final Logger LOG = LoggerFactory.getLogger(PayChannelRegistryImpl.class);

    public static final String CONFIG_PAY_CHANNELS = "pay_channels";

    private static final String CODE_WECHAT = "WECHAT";
    private static final String CODE_ALIPAY = "ALIPAY";
    private static final String CODE_BALANCE = "BALANCE";

    /** Channels injected by IoC (one bean per channel implementation). May be empty in tests. */
    @Inject
    List<PayChannel> channels;

    @Inject
    ILitemallSystemBiz systemBiz;

    private Map<String, PayChannel> channelByCode = Collections.emptyMap();

    @PostConstruct
    public void init() {
        Map<String, PayChannel> map = new LinkedHashMap<>();
        if (channels != null) {
            for (PayChannel ch : channels) {
                if (ch.getCode() == null) {
                    continue;
                }
                map.putIfAbsent(ch.getCode(), ch);
            }
        }
        this.channelByCode = Collections.unmodifiableMap(map);
        LOG.info("PayChannelRegistry initialized with channels: {}", channelByCode.keySet());
    }

    /**
     * Expose a fresh setter so tests / wiring can inject the channel list without relying on
     * field injection ordering.
     */
    public void setChannels(List<PayChannel> channels) {
        this.channels = channels;
    }

    @Override
    public List<PayChannel> getEnabledChannels() {
        Map<String, Boolean> toggleConfig = readToggleConfig(null);
        List<PayChannel> ordered = new ArrayList<>();
        addIfEnabled(ordered, CODE_WECHAT, toggleConfig);
        addIfEnabled(ordered, CODE_ALIPAY, toggleConfig);
        addIfEnabled(ordered, CODE_BALANCE, toggleConfig);
        // any channels beyond the known dict (future channels) — preserve registration order
        for (PayChannel ch : channelByCode.values()) {
            if (CODE_WECHAT.equals(ch.getCode())
                    || CODE_ALIPAY.equals(ch.getCode())
                    || CODE_BALANCE.equals(ch.getCode())) {
                continue;
            }
            addIfEnabled(ordered, ch.getCode(), toggleConfig);
        }
        return ordered;
    }

    @Override
    public PayChannel getChannel(String code) {
        if (code == null) {
            return null;
        }
        return channelByCode.get(code);
    }

    private void addIfEnabled(List<PayChannel> result, String code, Map<String, Boolean> toggleConfig) {
        PayChannel ch = channelByCode.get(code);
        if (ch == null) {
            return;
        }
        if (!ch.isEnabled()) {
            return;
        }
        if (!isToggleEnabled(code, toggleConfig)) {
            return;
        }
        result.add(ch);
    }

    /**
     * Operator toggle. WeChat defaults to enabled (single-channel baseline); all other channels
     * default to disabled (gray rollout). Malformed config rows are ignored.
     */
    private boolean isToggleEnabled(String code, Map<String, Boolean> toggleConfig) {
        Boolean toggled = toggleConfig.get(code);
        if (toggled != null) {
            return toggled;
        }
        return CODE_WECHAT.equals(code);
    }

    /**
     * Read the {@code pay_channels} JSON config into a code→enabled map. Returns an empty map
     * when config is absent or malformed (defaults take over). Runs in a system context since
     * the registry may be queried outside a user request.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Boolean> readToggleConfig(IServiceContext context) {
        String json;
        try {
            json = systemBiz.getConfig(CONFIG_PAY_CHANNELS, context);
        } catch (Exception e) {
            LOG.warn("Failed to read pay_channels config, using defaults", e);
            return new HashMap<>();
        }
        Map<String, Boolean> map = new HashMap<>();
        if (json == null || json.isEmpty()) {
            return map;
        }
        try {
            Object parsed = JsonTool.parse(json);
            if (parsed instanceof List) {
                for (Object item : (List<?>) parsed) {
                    if (!(item instanceof Map)) {
                        continue;
                    }
                    Object codeObj = ((Map<String, Object>) item).get("code");
                    Object enabledObj = ((Map<String, Object>) item).get("enabled");
                    if (codeObj == null) {
                        continue;
                    }
                    String code = codeObj.toString();
                    boolean enabled = enabledObj == null ? false : Boolean.parseBoolean(enabledObj.toString());
                    map.put(code, enabled);
                }
            }
        } catch (Exception e) {
            LOG.warn("Malformed pay_channels config, using defaults: {}", json, e);
        }
        return map;
    }
}
