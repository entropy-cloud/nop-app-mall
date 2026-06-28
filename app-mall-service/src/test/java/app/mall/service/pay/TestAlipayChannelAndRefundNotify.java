package app.mall.service.pay;

import app.mall.biz.ILitemallOrderBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallOrder;
import app.mall.pay.IPaymentCallback;
import app.mall.wx.AlipayPayChannel;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.core.context.ServiceContextImpl;
import io.nop.dao.api.IDaoProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * P30 Phase 3 tests: Alipay channel skeleton + refund async-notify reconciliation.
 *
 * <p>Covers the Alipay {@link app.mall.pay.PayChannel} strategy methods (example-mode prepay/query/
 * refund fallback, parseNotifyBody trade_status routing) by direct injection (no new GraphQL entry
 * — {@code PayChannel} methods are strategy-interface methods, not {@code @BizMutation}/
 * {@code @BizQuery}, so they do not trigger the GraphQL-test rule). Also covers
 * {@link IPaymentCallback#onRefundSuccess} idempotency (already-refunded orders are a no-op).
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestAlipayChannelAndRefundNotify extends JunitBaseTestCase {

    @Inject
    AlipayPayChannel alipayPayChannel;

    @Inject
    IPaymentCallback paymentCallback;

    @Inject
    IDaoProvider daoProvider;

    private static final String USER_ID = "303";

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId(USER_ID);
        ContextProvider.getOrCreateContext().setUserName("alipay-test");
    }

    private ServiceContextImpl ctx() {
        return new ServiceContextImpl();
    }

    private String createOrderForUser(String userId, int status, BigDecimal actualPrice) {
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).newEntity();
        order.setUserId(userId);
        order.setOrderSn("P30A-" + System.nanoTime());
        order.setOrderStatus(status);
        order.setAftersaleStatus(_AppMallDaoConstants.AFTERSALE_STATUS_INIT);
        order.setConsignee("test");
        order.setMobile("13800138000");
        order.setAddress("test");
        order.setMessage("test");
        order.setGoodsPrice(actualPrice);
        order.setOrderPrice(actualPrice);
        order.setActualPrice(actualPrice);
        order.setFreightPrice(BigDecimal.ZERO);
        order.setCouponPrice(BigDecimal.ZERO);
        order.setIntegralPrice(BigDecimal.ZERO);
        order.setGrouponPrice(BigDecimal.ZERO);
        order.setPromotionPrice(BigDecimal.ZERO);
        order.setPinTuanPrice(BigDecimal.ZERO);
        order.setComments(0);
        order.setDeleted(false);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(order);
        return order.getOrderSn();
    }

    @Test
    public void testAlipayChannelCodeAndDisabledByDefault() {
        assertEquals("ALIPAY", alipayPayChannel.getCode());
        // Baseline: alipay.enabled=false => capability false => not surfaced in cashier.
        assertEquals(false, alipayPayChannel.isEnabled());
    }

    @Test
    public void testAlipayExamplePrepayFallback() {
        app.mall.pay.PayPrepayRequestBean req = new app.mall.pay.PayPrepayRequestBean();
        req.setOutTradeNo("alipay-001");
        req.setTotalFee(BigDecimal.valueOf(9.9));
        app.mall.pay.PayPrepayResponseBean resp = alipayPayChannel.prepay(req);
        assertNotNull(resp);
        assertNotNull(resp.getPayId(), "example prepay must return a payId");
        assertEquals("H5", resp.getTradeType());
    }

    @Test
    public void testAlipayExampleQueryFallback() {
        app.mall.pay.PayStatusResponseBean resp = alipayPayChannel.query("alipay-001");
        assertNotNull(resp);
        assertEquals("alipay-001", resp.getOutTradeNo());
    }

    @Test
    public void testAlipayExampleRefundFallback() {
        app.mall.pay.PayRefundRequestBean req = new app.mall.pay.PayRefundRequestBean();
        req.setOutTradeNo("alipay-001");
        req.setOutRefundNo("alipay-refund-001");
        req.setTotalFee(BigDecimal.valueOf(9.9));
        req.setRefundFee(BigDecimal.valueOf(9.9));
        app.mall.pay.PayRefundResponseBean resp = alipayPayChannel.refund(req);
        assertNotNull(resp);
        assertNotNull(resp.getRefundId(), "example refund must return a refundId");
    }

    @Test
    public void testAlipayParseNotifyReturnsNullWhenDisabled() {
        // Example mode: parseNotifyBody returns null (no real payment to confirm).
        Map<String, String> params = new HashMap<>();
        params.put("trade_status", "TRADE_SUCCESS");
        params.put("out_trade_no", "alipay-001");
        assertNull(alipayPayChannel.parseNotifyBody(params),
                "disabled channel must not surface a verified outTradeNo");
    }

    @Test
    public void testOnRefundSuccessIdempotentForAlreadyRefundedOrder() {
        // Order already in REFUND_CONFIRM (sync refund path advanced it) — async notify is a no-op.
        String orderSn = createOrderForUser(USER_ID, _AppMallDaoConstants.ORDER_STATUS_REFUND_CONFIRM,
                new BigDecimal("99"));
        // Must not throw and must not change the status.
        paymentCallback.onRefundSuccess(orderSn, "refund_" + orderSn);
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderSn, orderSn));
        LitemallOrder reloaded = (LitemallOrder) daoProvider.daoFor(LitemallOrder.class)
                .findAllByQuery(q).stream().findFirst().orElse(null);
        assertNotNull(reloaded);
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_REFUND_CONFIRM, reloaded.getOrderStatus(),
                "idempotent refund notify must not change an already-refunded order");
    }

    @Test
    public void testOnRefundSuccessIgnoresUnknownOrder() {
        // Unknown orderSn: must not throw.
        paymentCallback.onRefundSuccess("unknown-order-sn", null);
    }

    @Test
    public void testOnRefundSuccessIgnoresEmptyOutTradeNo() {
        paymentCallback.onRefundSuccess("", null);
        paymentCallback.onRefundSuccess(null, null);
    }
}
