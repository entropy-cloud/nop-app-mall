package app.mall.wx;

import app.mall.pay.PayChannel;
import app.mall.pay.PayPrepayRequestBean;
import app.mall.pay.PayPrepayResponseBean;
import app.mall.pay.PayRefundRequestBean;
import app.mall.pay.PayRefundResponseBean;
import app.mall.pay.PayStatusResponseBean;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Alipay payment channel (P30). Implements {@link PayChannel} so the registry can route to it
 * alongside WeChat and the wallet balance channel.
 *
 * <p><b>Example / disabled mode (default):</b> {@code alipay.enabled=false} (the baseline). All
 * channel methods return example/demo responses and {@link #isEnabled()} returns {@code false},
 * so the channel never surfaces in the cashier. This mirrors the Phase 14 {@code WxPayServiceImpl}
 * {@code enabled=false} precedent — clearly marked non-production behavior.
 *
 * <p><b>Real integration is a Deferred successor.</b> The real Alipay SDK (H5 / mini-program),
 * merchant credentials (appId / app private key / alipay public key / gateway / notify URL), and
 * signature verification are NOT wired here. The {@link #prepay}, {@link #query}, {@link #refund},
 * and {@link #parseNotifyBody} methods contain the integration points (marked with clear TODOs)
 * where the successor plan will attach the real SDK calls once merchant credentials are obtained.
 *
 * <p>Non-production status is asserted by {@link #isEnabled()} reflecting {@code alipay.enabled}.
 */
public class AlipayPayChannel implements PayChannel {

    public static final String CHANNEL_CODE = "ALIPAY";

    private static final Logger LOG = LoggerFactory.getLogger(AlipayPayChannel.class);

    private boolean enabled;
    private String appId;
    private String appPrivateKey;
    private String alipayPublicKey;
    private String gateway;
    private String notifyUrl;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setAppPrivateKey(String appPrivateKey) {
        this.appPrivateKey = appPrivateKey;
    }

    public void setAlipayPublicKey(String alipayPublicKey) {
        this.alipayPublicKey = alipayPublicKey;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    @PostConstruct
    public void init() {
        if (!enabled) {
            LOG.info("Alipay channel is disabled (alipay.enabled=false). Using example mode. "
                    + "Real SDK integration is a successor (Deferred).");
            return;
        }
        // Successor integration point: initialize the Alipay SDK client here once merchant
        // credentials (appId / appPrivateKey / alipayPublicKey / gateway) are provided. Until then
        // enabled stays false at baseline, so this branch is only reached when an operator opts in.
        if (appId == null || appId.isEmpty()
                || appPrivateKey == null || appPrivateKey.isEmpty()
                || alipayPublicKey == null || alipayPublicKey.isEmpty()) {
            LOG.warn("Alipay enabled=true but credentials are incomplete; falling back to example mode. "
                    + "Provide appId/appPrivateKey/alipayPublicKey for real integration.");
        } else {
            LOG.info("Alipay channel initialized (enabled=true, appId={})", appId);
        }
    }

    @Override
    public String getCode() {
        return CHANNEL_CODE;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public PayPrepayResponseBean prepay(PayPrepayRequestBean req) {
        if (!enabled) {
            LOG.warn("Alipay disabled, returning example prepay response for outTradeNo={}", req.getOutTradeNo());
            PayPrepayResponseBean resp = new PayPrepayResponseBean();
            resp.setPayId("alipay_sample_" + req.getOutTradeNo());
            // H5/mini-program trade type; codeUrl carries the example pay URL.
            resp.setCodeUrl("https://openapi.alipaydev.com/gateway.do?sample=1&out_trade_no=" + req.getOutTradeNo());
            resp.setTradeType("H5");
            return resp;
        }
        // Successor integration point: invoke Alipay alipay.trade.pay / alipay.trade.wap.pay here.
        PayPrepayResponseBean resp = new PayPrepayResponseBean();
        resp.setPayId("alipay_" + req.getOutTradeNo());
        resp.setTradeType("H5");
        return resp;
    }

    @Override
    public PayStatusResponseBean query(String outTradeNo) {
        if (!enabled) {
            LOG.warn("Alipay disabled, returning example query response for outTradeNo={}", outTradeNo);
            PayStatusResponseBean resp = new PayStatusResponseBean();
            resp.setSuccess(true);
            resp.setTradeState("WAIT_BUYER_PAY");
            resp.setOutTradeNo(outTradeNo);
            return resp;
        }
        // Successor integration point: invoke alipay.trade.query here.
        PayStatusResponseBean resp = new PayStatusResponseBean();
        resp.setOutTradeNo(outTradeNo);
        return resp;
    }

    @Override
    public PayRefundResponseBean refund(PayRefundRequestBean req) {
        if (!enabled) {
            LOG.warn("Alipay disabled, returning example refund response for outRefundNo={}", req.getOutRefundNo());
            PayRefundResponseBean resp = new PayRefundResponseBean();
            resp.setRefundId("alipay_sample_refund_" + req.getOutRefundNo());
            return resp;
        }
        // Successor integration point: invoke alipay.trade.refund here.
        PayRefundResponseBean resp = new PayRefundResponseBean();
        resp.setRefundId("alipay_refund_" + req.getOutRefundNo());
        return resp;
    }

    /**
     * Parse and verify an Alipay async notify body. Returns the {@code out_trade_no} only when the
     * signature verification succeeds AND the trade status indicates success ({@code trade_status ==
     * TRADE_SUCCESS} or {@code TRADE_FINISHED}); returns {@code null} otherwise (so the caller does
     * not advance the order).
     *
     * <p>In example/disabled mode this returns {@code null} (no real payment to confirm), mirroring
     * {@code WxPayServiceImpl.parseNotifyBody}.
     *
     * @param params the parsed Alipay notify form parameters (the successor SDK verifies the
     *               {@code sign} field against the alipay public key; here it is a passthrough)
     */
    public String parseNotifyBody(java.util.Map<String, String> params) {
        if (!enabled) {
            LOG.warn("Alipay disabled, skipping notify verification");
            return null;
        }
        // Successor integration point: verify sign with Alipay SDK (AlipaySignature.rsaCheckV1).
        if (params == null || params.isEmpty()) {
            return null;
        }
        String tradeStatus = params.get("trade_status");
        boolean success = "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
        if (!success) {
            return null;
        }
        return params.get("out_trade_no");
    }
}
