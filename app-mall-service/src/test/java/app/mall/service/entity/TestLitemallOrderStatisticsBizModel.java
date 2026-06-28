package app.mall.service.entity;

import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallOrderStatisticsBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @SuppressWarnings("unchecked")
    @Test
    public void testGetOrderStatistics() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getOrderStatistics", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getOrderStatistics failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetGoodsSalesRanking() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getGoodsSalesRanking", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getGoodsSalesRanking failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        assertNotNull(result.getData());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetUserStatistics() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getUserStatistics", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getUserStatistics failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetDashboardMetrics() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getDashboardMetrics", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getDashboardMetrics failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertNotNull(data.get("orderCount"));
        assertNotNull(data.get("uv"));
    }

    @Test
    public void testGetSalesTrend() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("granularity", "day"));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getSalesTrend", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getSalesTrend failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        assertNotNull(result.getData());
    }

    @Test
    public void testGetSalesTrendHourly() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("granularity", "hour"));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getSalesTrend", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getSalesTrend(hour) failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        assertNotNull(result.getData());
    }

    @Test
    public void testGetRealtimeOrders() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("limit", 5));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getRealtimeOrders", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getRealtimeOrders failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        assertNotNull(result.getData());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetTodoAggregation() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getTodoAggregation", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getTodoAggregation failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertNotNull(data.get("pendingShip"));
        assertNotNull(data.get("stockWarning"));
    }

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");
    }
}
