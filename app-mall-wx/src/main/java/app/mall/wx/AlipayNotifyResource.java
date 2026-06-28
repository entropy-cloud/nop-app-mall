package app.mall.wx;

import app.mall.pay.IPaymentCallback;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Alipay async-notify endpoint (P30). Alipay posts payment/refund result notifications as form
 * parameters to {@code notify_url}. After signature verification (successor SDK), a verified
 * SUCCESS payment drives the order state machine via {@link IPaymentCallback}.
 *
 * <p>In example mode ({@code alipay.enabled=false}) the channel returns {@code null} from
 * {@code parseNotifyBody}, so no order is advanced — the endpoint just replies {@code success}
 * to stop Alipay retries.
 *
 * <p>Real signature verification and the Alipay SDK are a Deferred successor; the wiring point
 * and the reply contract ({@code success}/{@code fail}) are in place here.
 */
@Path("/alipay/notify")
public class AlipayNotifyResource {

    private static final Logger LOG = LoggerFactory.getLogger(AlipayNotifyResource.class);

    @Inject
    AlipayPayChannel alipayPayChannel;

    // Bridge into the order service (app-mall-service), same contract as WxPayNotifyResource.
    @Inject
    IPaymentCallback paymentCallback;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response handleNotify(MultivaluedMap<String, String> form) {
        Map<String, String> params = new HashMap<>();
        if (form != null) {
            for (Map.Entry<String, java.util.List<String>> e : form.entrySet()) {
                if (e.getValue() != null && !e.getValue().isEmpty()) {
                    params.put(e.getKey(), e.getValue().get(0));
                }
            }
        }
        LOG.info("Received alipay notify: params={}", params);

        try {
            String outTradeNo = alipayPayChannel.parseNotifyBody(params);
            // parseNotifyBody returns out_trade_no only for a verified SUCCESS payment (null/empty
            // for non-success states or example mode where there is no real payment).
            if (outTradeNo != null && !outTradeNo.isEmpty() && paymentCallback != null) {
                paymentCallback.onPaymentSuccess(outTradeNo, params.get("trade_no"));
            }
            // Alipay expects the literal string "success" to stop retrying.
            return Response.ok("success").build();
        } catch (Exception e) {
            LOG.error("Failed to process alipay notify", e);
            // Any non-"success" reply makes Alipay retry; onPaymentSuccess is idempotent.
            return Response.serverError().entity("fail").build();
        }
    }
}
