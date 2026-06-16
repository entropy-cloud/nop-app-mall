package app.mall.service.pay;

import app.mall.pay.PayService;
import app.mall.wx.WxPayServiceImpl;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestPayServiceBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    PayService payService;

    @AfterEach
    void tearDown() {
        WxPayServiceImpl.setForceRefundFailure(false);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testCreatePayment() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "outTradeNo", "test-pre-001",
                "totalFee", BigDecimal.valueOf(0.01),
                "description", "Test Order test-pre-001"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "PayService__createPayment", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "createPayment failed: " + result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("payId"), "payId should not be null");
        assertNotNull(data.get("codeUrl"), "codeUrl should not be null");
        assertEquals("NATIVE", data.get("tradeType"));
        assertTrue(((String) data.get("payId")).contains("test-pre-001"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testQueryPayment() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "outTradeNo", "test-qry-001"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "PayService__queryPayment", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "queryPayment failed: " + result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("tradeState"));
        assertEquals("test-qry-001", data.get("outTradeNo"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testRefundSuccess() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "outTradeNo", "test-ref-001",
                "outRefundNo", "test-refund-001",
                "totalFee", BigDecimal.valueOf(100),
                "refundFee", BigDecimal.valueOf(50)
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "PayService__refund", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "refund should succeed: " + result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertTrue((Boolean) data.get("success"));
        assertNotNull(data.get("refundId"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testRefundFailure() {
        WxPayServiceImpl.setForceRefundFailure(true);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "outTradeNo", "test-ref-002",
                "outRefundNo", "test-refund-002",
                "totalFee", BigDecimal.valueOf(100),
                "refundFee", BigDecimal.valueOf(50)
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "PayService__refund", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "refund should still return 200 even when failed");
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(false, data.get("success"));
    }
}
