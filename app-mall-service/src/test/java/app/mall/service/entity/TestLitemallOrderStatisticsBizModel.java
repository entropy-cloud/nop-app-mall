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

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // ===== P19 报表体系扩展 =====

    @SuppressWarnings("unchecked")
    @Test
    public void testGetSalesFunnel() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getSalesFunnel", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getSalesFunnel failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertNotNull(data.get("viewCount"));
        assertNotNull(data.get("cartCount"));
        assertNotNull(data.get("orderCount"));
        assertNotNull(data.get("payCount"));
        assertNotNull(data.get("repurchaseCount"));
        assertNotNull(data.get("payViewRatio"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetProductAnalysis() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getProductAnalysis", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getProductAnalysis failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertNotNull(data.get("salesRanking"));
        assertNotNull(data.get("cartRanking"));
        assertNotNull(data.get("unsalableGoods"));
        assertNotNull(data.get("soldGoodsCount"));
        assertNotNull(data.get("onSaleGoodsCount"));
        assertNotNull(data.get("salabilityRate"));
    }

    // ===== P19 用户分析 =====

    @Test
    public void testGetUserRetention() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getUserRetention", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getUserRetention failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        assertNotNull(result.getData());
    }

    @Test
    public void testGetUserRfm() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getUserRfm", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getUserRfm failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        assertNotNull(result.getData());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetUserLifecycle() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getUserLifecycle", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getUserLifecycle failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        assertNotNull(result.getData());
        List<Map<String, Object>> data = (List<Map<String, Object>>) result.getData();
        assertNotNull(data);
    }

    @Test
    public void testGetRepurchaseRate() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getRepurchaseRate", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getRepurchaseRate failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        assertNotNull(result.getData());
    }

    // ===== P19 订单分析 + 营销分析 =====

    @SuppressWarnings("unchecked")
    @Test
    public void testGetOrderAnalysis() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getOrderAnalysis", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getOrderAnalysis failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertNotNull(data.get("aovDistribution"));
        assertNotNull(data.get("paymentMethodShare"));
        assertNotNull(data.get("returnReasonShare"));
        assertNotNull(data.get("totalPaidOrders"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCouponAnalysis() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getCouponAnalysis", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getCouponAnalysis failed: status=" + result.getStatus() + " msg=" + result.getMsg());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertNotNull(data.get("claimedCount"));
        assertNotNull(data.get("usedCount"));
        assertNotNull(data.get("pulledGmv"));
    }

    // ===== nop-report 报表导出（funnel/product/order × xlsx/pdf + 空集）=====

    private ApiResponse<?> callExportReport(String reportName, String renderType) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "reportName", reportName, "renderType", renderType));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__exportReport", req);
        return graphQLEngine.executeRpc(ctx);
    }

    @Test
    public void testExportReportFunnelXlsx() {
        File f = renderReport("funnel", "xlsx");
        assertTrue(f.length() > 0, "funnel xlsx should be non-empty");
    }

    @Test
    public void testExportReportFunnelPdf() {
        File f = renderReport("funnel", "pdf");
        assertTrue(f.length() > 0, "funnel pdf should be non-empty");
    }

    @Test
    public void testExportReportProductXlsx() {
        File f = renderReport("product", "xlsx");
        assertTrue(f.length() > 0, "product xlsx should be non-empty");
    }

    @Test
    public void testExportReportProductPdf() {
        File f = renderReport("product", "pdf");
        assertTrue(f.length() > 0, "product pdf should be non-empty");
    }

    @Test
    public void testExportReportOrderXlsx() {
        File f = renderReport("order", "xlsx");
        assertTrue(f.length() > 0, "order xlsx should be non-empty");
    }

    @Test
    public void testExportReportOrderPdf() {
        File f = renderReport("order", "pdf");
        assertTrue(f.length() > 0, "order pdf should be non-empty");
    }

    @Test
    public void testExportReportEmptyDataNotError() {
        // 空库：报表数据集为空，渲染不应报错
        ApiResponse<?> r = callExportReport("funnel", "xlsx");
        assertEquals(0, r.getStatus(), "empty funnel export should not error: " + r);
    }

    @Test
    public void testExportReportInvalidName() {
        ApiResponse<?> r = callExportReport("unknown", "xlsx");
        assertEquals(-1, r.getStatus(), "invalid reportName should be rejected: " + r);
    }

    @Test
    public void testExportReportInvalidRenderType() {
        ApiResponse<?> r = callExportReport("funnel", "docx");
        assertEquals(-1, r.getStatus(), "invalid renderType should be rejected: " + r);
    }

    private File renderReport(String reportName, String renderType) {
        ApiResponse<?> r = callExportReport(reportName, renderType);
        assertEquals(0, r.getStatus(), "exportReport " + reportName + "/" + renderType + " failed: " + r);
        File file = WebContentBeanFiles.contentFile(r.getData());
        assertNotNull(file, "rendered file should not be null: " + r.getData());
        assertTrue(file.exists(), "rendered file should exist: " + file);
        return file;
    }

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");
    }
}
