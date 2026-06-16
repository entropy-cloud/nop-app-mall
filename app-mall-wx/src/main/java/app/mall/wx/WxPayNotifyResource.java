package app.mall.wx;

import io.nop.api.core.exceptions.NopException;
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
            wxPayService.parseNotifyBody(body, wechatpaySerial, wechatpayNonce,
                    wechatpaySignature, wechatpayTimestamp, wechatpaySignatureType);
            return Response.ok("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>").build();
        } catch (NopException e) {
            LOG.error("Failed to process wxpay notify", e);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[签名验证失败]]></return_msg></xml>")
                    .build();
        } catch (Exception e) {
            LOG.error("Unexpected error processing wxpay notify", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>")
                    .build();
        }
    }
}
