package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
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
import io.nop.orm.IOrmTemplate;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallOrderStatisticsBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    IOrmTemplate ormTemplate;

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

    /**
     * Path B（Dashboard 聚合）三级阈值优先验证：
     * 在售商品 SKU 总库存 = 15 > 全局阈值 10，默认不预警；
     * 设置 per-goods safetyStock=20 后 → 15 ≤ 20 应进入预警明细，thresholdSource=safetyStock。
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetTodoAggregationStockWarningWithGoodsLevelSafetyStock() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("admin");

        // 在售商品，总库存 15（> 全局阈值 10，默认不预警）
        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G-P18-SAFE-001");
        goods.setName("P18 SafetyStock Goods");
        goods.setRetailPrice(BigDecimal.valueOf(100));
        goods.setIsOnSale(true);
        goods.setPicUrl("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goods.getId());
        product.setNumber(15);
        product.setPrice(BigDecimal.valueOf(100));
        product.setSpecifications("[\"标准\"]");
        product.setUrl("");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);

        // 不设 safetyStock：聚合库存 15 > 全局阈值 10，不应出现在预警明细
        ApiRequest<Map<String, Object>> req1 = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx1 = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getTodoAggregation", req1);
        ApiResponse<?> r1 = graphQLEngine.executeRpc(ctx1);
        assertEquals(0, r1.getStatus());
        Map<String, Object> data1 = (Map<String, Object>) r1.getData();
        List<Map<String, Object>> details1 = (List<Map<String, Object>>) data1.get("stockWarningDetails");
        if (details1 != null) {
            boolean presentBefore = details1.stream()
                    .anyMatch(m -> "P18 SafetyStock Goods".equals(m.get("goodsName")));
            assertFalse(presentBefore, "Goods (totalStock=15, no safetyStock) should NOT trigger warning under global threshold 10");
        }

        // 设置 per-goods safetyStock=20：聚合库存 15 ≤ 20 应预警，thresholdSource=safetyStock
        String goodsIdLocal = goods.getId();
        ormTemplate.runInSession(session -> {
            LitemallGoods loaded = daoProvider.daoFor(LitemallGoods.class).getEntityById(goodsIdLocal);
            loaded.setSafetyStock(20);
            daoProvider.daoFor(LitemallGoods.class).updateEntity(loaded);
            return null;
        });

        ApiRequest<Map<String, Object>> req2 = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx2 = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getTodoAggregation", req2);
        ApiResponse<?> r2 = graphQLEngine.executeRpc(ctx2);
        assertEquals(0, r2.getStatus());
        Map<String, Object> data2 = (Map<String, Object>) r2.getData();
        List<Map<String, Object>> details2 = (List<Map<String, Object>>) data2.get("stockWarningDetails");
        assertNotNull(details2);
        assertFalse(details2.isEmpty(), "stockWarningDetails should not be empty after setting safetyStock=20");
        Map<String, Object> warn = details2.stream()
                .filter(m -> "P18 SafetyStock Goods".equals(m.get("goodsName")))
                .findFirst().orElseThrow(() -> new AssertionError("Goods should be in warning list after safetyStock=20"));
        assertEquals("safetyStock", warn.get("thresholdSource"),
                "per-goods safetyStock should drive Dashboard warning source");
        assertEquals(20, warn.get("safetyStock"));
        assertEquals(15, warn.get("totalStock"));
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
    public void testExportReportUserXlsx() {
        File f = renderReport("user", "xlsx");
        assertTrue(f.length() > 0, "user xlsx should be non-empty");
    }

    @Test
    public void testExportReportUserPdf() {
        File f = renderReport("user", "pdf");
        assertTrue(f.length() > 0, "user pdf should be non-empty");
    }

    @Test
    public void testExportReportCouponXlsx() {
        File f = renderReport("coupon", "xlsx");
        assertTrue(f.length() > 0, "coupon xlsx should be non-empty");
    }

    @Test
    public void testExportReportCouponPdf() {
        File f = renderReport("coupon", "pdf");
        assertTrue(f.length() > 0, "coupon pdf should be non-empty");
    }

    @Test
    public void testExportReportEmptyDataNotError() {
        // 空库：报表数据集为空，渲染不应报错
        ApiResponse<?> r = callExportReport("funnel", "xlsx");
        assertEquals(0, r.getStatus(), "empty funnel export should not error: " + r);
    }

    @Test
    public void testExportReportUserEmptyDataNotError() {
        // 空库：用户分析多 sheet 导出不应报错
        ApiResponse<?> r = callExportReport("user", "xlsx");
        assertEquals(0, r.getStatus(), "empty user export should not error: " + r);
    }

    @Test
    public void testExportReportCouponEmptyDataNotError() {
        // 空库：优惠券分析导出不应报错
        ApiResponse<?> r = callExportReport("coupon", "pdf");
        assertEquals(0, r.getStatus(), "empty coupon export should not error: " + r);
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
