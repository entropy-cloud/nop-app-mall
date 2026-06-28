package app.mall.wx;

import app.mall.pay.IPaymentCallback;
import io.nop.api.core.exceptions.NopException;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@Path("/wxpay/notify")
public class WxPayNotifyResource {

    private static final Logger LOG = LoggerFactory.getLogger(WxPayNotifyResource.class);

    private WxPayServiceImpl wxPayService;

    // Bridge into the order service (app-mall-service) so a signature-verified SUCCESS payment
    // drives the order state machine. app-mall-wx cannot depend on the order service/dao directly,
    // so this goes through the api-layer IPaymentCallback contract, injected by type.
    @Inject
    IPaymentCallback paymentCallback;

    public void setWxPayService(WxPayServiceImpl wxPayService) {
        this.wxPayService = wxPayService;
    }

    @POST
    @Consumes({"application/json", "application/xml", "text/plain"})
    public Response handleNotify(String body, @Context HttpHeaders headers) {
        String wechatpaySerial = headers.getHeaderString("Wechatpay-Serial");
        String wechatpayNonce = headers.getHeaderString("Wechatpay-Nonce");
        String wechatpaySignature = headers.getHeaderString("Wechatpay-Signature");
        String wechatpayTimestamp = headers.getHeaderString("Wechatpay-Timestamp");
        String wechatpaySignatureType = headers.getHeaderString("Wechatpay-Signature-Type");

        LOG.info("Received wxpay notify: timestamp={}, nonce={}, serial={}",
                wechatpayTimestamp, wechatpayNonce, wechatpaySerial);

        try {
            String outTradeNo = wxPayService.parseNotifyBody(body, wechatpaySerial, wechatpayNonce,
                    wechatpaySignature, wechatpayTimestamp, wechatpaySignatureType);
            // parseNotifyBody returns the order's outTradeNo only for a verified SUCCESS payment
            // (null/empty for non-success states or demo mode where there is no real payment).
            if (outTradeNo != null && !outTradeNo.isEmpty() && paymentCallback != null) {
                paymentCallback.onPaymentSuccess(outTradeNo, null);
            }
            return Response.ok("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>").build();
        } catch (NopException e) {
            // A NopException here means either signature verification failed or order processing
            // (confirmPaidByNotify) failed. Return 500 (not 401) so WeChat retries the notify:
            // confirmPaidByNotify is idempotent, so a retry is always safe. A genuinely tampered
            // notify will keep failing verification and WeChat will give up after its retry budget.
            LOG.error("Failed to process wxpay notify (errorCode={}, params={})",
                    e.getErrorCode(), e.getParams(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>")
                    .build();
        } catch (Exception e) {
            LOG.error("Unexpected error processing wxpay notify", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>")
                    .build();
        }
    }

    /**
     * WeChat Pay refund async-notify endpoint (P30, closes the P29 deferred "退款异步通知流程").
     * WeChat posts the refund result here after {@code refund()} sets {@code notify_url}. After
     * signature verification, a verified SUCCESS refund drives the refund reconciliation via
     * {@link IPaymentCallback#onRefundSuccess}, which is idempotent (already-refunded orders are
     * a no-op), so WeChat retries are always safe.
     */
    @POST
    @Path("/refund")
    @Consumes({"application/json", "application/xml", "text/plain"})
    public Response handleRefundNotify(String body, @Context HttpHeaders headers) {
        String wechatpaySerial = headers.getHeaderString("Wechatpay-Serial");
        String wechatpayNonce = headers.getHeaderString("Wechatpay-Nonce");
        String wechatpaySignature = headers.getHeaderString("Wechatpay-Signature");
        String wechatpayTimestamp = headers.getHeaderString("Wechatpay-Timestamp");
        String wechatpaySignatureType = headers.getHeaderString("Wechatpay-Signature-Type");

        LOG.info("Received wxpay refund notify: timestamp={}, nonce={}, serial={}",
                wechatpayTimestamp, wechatpayNonce, wechatpaySerial);

        try {
            String outTradeNo = wxPayService.parseRefundNotifyBody(body, wechatpaySerial, wechatpayNonce,
                    wechatpaySignature, wechatpayTimestamp, wechatpaySignatureType);
            if (outTradeNo != null && !outTradeNo.isEmpty() && paymentCallback != null) {
                paymentCallback.onRefundSuccess(outTradeNo, null);
            }
            return Response.ok("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>").build();
        } catch (NopException e) {
            LOG.error("Failed to process wxpay refund notify (errorCode={}, params={})",
                    e.getErrorCode(), e.getParams(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>")
                    .build();
        } catch (Exception e) {
            LOG.error("Unexpected error processing wxpay refund notify", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>")
                    .build();
        }
    }
}
