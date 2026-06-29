package app.mall.service.entity;

import app.mall.biz.ILitemallOrderBiz;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.file.dao.entity.NopFileRecord;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import io.nop.orm.IOrmTemplate;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    // ===== 商品成本价与毛利分析（D1/D3 口径）=====

    private void seedMarginOrderLine(String sn, String name, BigDecimal retailPrice,
                                     BigDecimal costPrice, String categoryId,
                                     int number, BigDecimal sellPrice, String orderSn) {
        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn(sn);
        goods.setName(name);
        goods.setRetailPrice(retailPrice);
        if (costPrice != null) {
            goods.setCostPrice(costPrice);
        }
        goods.setIsOnSale(true);
        goods.setPicUrl("");
        if (categoryId != null) {
            goods.setCategoryId(categoryId);
        }
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goods.getId());
        product.setNumber(100);
        product.setPrice(retailPrice);
        product.setSpecifications("[\"标准\"]");
        product.setUrl("");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);

        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).newEntity();
        order.setUserId("1");
        order.setOrderSn(orderSn);
        order.setOrderStatus(201);
        order.setAftersaleStatus(0);
        order.setConsignee("test");
        order.setMobile("13800138000");
        order.setAddress("addr");
        order.setMessage("margin-test");
        BigDecimal lineTotal = sellPrice.multiply(BigDecimal.valueOf(number));
        order.setGoodsPrice(lineTotal);
        order.setFreightPrice(BigDecimal.ZERO);
        order.setCouponPrice(BigDecimal.ZERO);
        order.setIntegralPrice(BigDecimal.ZERO);
        order.setGrouponPrice(BigDecimal.ZERO);
        order.setPromotionPrice(BigDecimal.ZERO);
        order.setPinTuanPrice(BigDecimal.ZERO);
        order.setOrderPrice(lineTotal);
        order.setActualPrice(lineTotal);
        order.setComments(0);
        order.setDeleted(false);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(order);

        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).newEntity();
        og.setOrderId(order.orm_idString());
        og.setGoodsId(goods.getId());
        og.setGoodsName(name);
        og.setGoodsSn(sn);
        og.setProductId(product.getId());
        og.setNumber(number);
        og.setPrice(sellPrice);
        og.setSpecifications("[\"标准\"]");
        og.setPicUrl("/f/download/margin-test-pic");
        og.setComment(0);
        daoProvider.daoFor(LitemallOrderGoods.class).saveEntity(og);
    }

    private static BigDecimal asDecimal(Object v) {
        if (v == null) {
            return null;
        }
        return new BigDecimal(v.toString());
    }

    /**
     * D1/D3 毛利口径：
     * (a) 有 costPrice 的商品毛利正确（costAmount=120, grossProfit=80, marginRate=0.4）；
     * (b) costPrice 为空 → 三列均 null；
     * (c) costPrice=0（D3 视为未维护）→ 三列均 null；
     * 多商品聚合 + 销量排行同口径。
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetProductAnalysisMarginWithCostPrice() {
        seedMarginOrderLine("MG-A", "毛利商品A", new BigDecimal("100"), new BigDecimal("60"),
                null, 2, new BigDecimal("100"), "MO-A-001");
        seedMarginOrderLine("MG-B", "未维护成本B", new BigDecimal("50"), null,
                null, 1, new BigDecimal("50"), "MO-B-001");
        seedMarginOrderLine("MG-C", "零成本C", new BigDecimal("30"), BigDecimal.ZERO,
                null, 3, new BigDecimal("30"), "MO-C-001");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getProductAnalysis", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getProductAnalysis failed: " + result);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        List<Map<String, Object>> ranking = (List<Map<String, Object>>) data.get("salesRanking");
        assertNotNull(ranking);
        assertFalse(ranking.isEmpty(), "salesRanking should contain seeded goods");

        Map<String, Object> a = ranking.stream()
                .filter(m -> "毛利商品A".equals(m.get("goodsName"))).findFirst()
                .orElseThrow(() -> new AssertionError("goods A missing from ranking"));
        assertEquals(0, new BigDecimal("120").compareTo(asDecimal(a.get("costAmount"))),
                "goods A costAmount = 2 * 60 = 120");
        assertEquals(0, new BigDecimal("80").compareTo(asDecimal(a.get("grossProfit"))),
                "goods A grossProfit = 200 - 120 = 80");
        assertEquals(0, new BigDecimal("0.4").compareTo(asDecimal(a.get("marginRate"))),
                "goods A marginRate = 80 / 200 = 0.4");

        Map<String, Object> b = ranking.stream()
                .filter(m -> "未维护成本B".equals(m.get("goodsName"))).findFirst()
                .orElseThrow(() -> new AssertionError("goods B missing from ranking"));
        assertNull(b.get("costAmount"), "unmaintained costPrice(null) → costAmount null");
        assertNull(b.get("grossProfit"), "unmaintained costPrice(null) → grossProfit null");
        assertNull(b.get("marginRate"), "unmaintained costPrice(null) → marginRate null");

        Map<String, Object> c = ranking.stream()
                .filter(m -> "零成本C".equals(m.get("goodsName"))).findFirst()
                .orElseThrow(() -> new AssertionError("goods C missing from ranking"));
        assertNull(c.get("costAmount"), "D3: costPrice=0 (unmaintained) → costAmount null");
        assertNull(c.get("grossProfit"), "D3: costPrice=0 (unmaintained) → grossProfit null");
        assertNull(c.get("marginRate"), "D3: costPrice=0 (unmaintained) → marginRate null");
    }

    /**
     * Proof(d)：categoryId 过滤从 INNER JOIN 降级为 WHERE 后，结果一致——
     * 按 categoryId=1001 过滤只返回该类目商品，不返回类目 1002 商品。
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetProductAnalysisCategoryFilterDowngradedToWhere() {
        seedMarginOrderLine("CF-A", "类目过滤A", new BigDecimal("100"), new BigDecimal("60"),
                "1001", 1, new BigDecimal("100"), "CF-A-O1");
        seedMarginOrderLine("CF-C", "类目过滤C", new BigDecimal("40"), new BigDecimal("20"),
                "1002", 1, new BigDecimal("40"), "CF-C-O1");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("categoryId", "1001"));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getProductAnalysis", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getProductAnalysis(categoryId) failed: " + result);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        List<Map<String, Object>> ranking = (List<Map<String, Object>>) data.get("salesRanking");
        assertNotNull(ranking);
        boolean hasA = ranking.stream().anyMatch(m -> "类目过滤A".equals(m.get("goodsName")));
        boolean hasC = ranking.stream().anyMatch(m -> "类目过滤C".equals(m.get("goodsName")));
        assertTrue(hasA, "categoryId=1001 filter should include goods A (cat 1001)");
        assertFalse(hasC, "categoryId=1001 filter should exclude goods C (cat 1002)");
    }

    /**
     * product 主题导出含毛利列（D2 扩展主题，非新 case）：seeded 毛利数据下 xlsx/pdf 不报错且非空。
     * 商品名用 Latin（PDFBox Helvetica 仅 Latin，见基线 product-analysis.xpt.xml 注记）。
     */
    @Test
    public void testExportReportProductWithMarginData() {
        seedMarginOrderLine("EX-A", "MarginGoodsA", new BigDecimal("100"), new BigDecimal("60"),
                null, 2, new BigDecimal("100"), "EX-A-O1");

        File xlsx = renderReport("product", "xlsx");
        assertTrue(xlsx.length() > 0, "product xlsx with margin data should be non-empty");
        File pdf = renderReport("product", "pdf");
        assertTrue(pdf.length() > 0, "product pdf with margin data should be non-empty");
    }

    /**
     * M1 鉴权回归：getProductAnalysis 必须标注 @Auth(roles="admin")，杜绝成本/毛利经 GraphQL 泄漏给非 admin。
     * 运行时强制由平台 enableActionAuth 控制（生产开启），此处以元数据级反射守卫确保注解不被移除。
     */
    @Test
    public void testGetProductAnalysisRequiresAdminAuth() throws NoSuchMethodException {
        Method ifaceMethod = ILitemallOrderBiz.class.getMethod(
                "getProductAnalysis", String.class, String.class, String.class,
                io.nop.core.context.IServiceContext.class);
        Auth ifaceAuth = ifaceMethod.getAnnotation(Auth.class);
        assertNotNull(ifaceAuth, "ILitemallOrderBiz.getProductAnalysis must declare @Auth");
        assertEquals("admin", ifaceAuth.roles(),
                "getProductAnalysis interface must require admin role");

        Method implMethod = LitemallOrderBizModel.class.getMethod(
                "getProductAnalysis", String.class, String.class, String.class,
                io.nop.core.context.IServiceContext.class);
        Auth implAuth = implMethod.getAnnotation(Auth.class);
        assertNotNull(implAuth, "LitemallOrderBizModel.getProductAnalysis must declare @Auth");
        assertEquals("admin", implAuth.roles(),
                "getProductAnalysis impl must require admin role");
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
        createFileRecord("margin-test-pic", "LitemallOrderGoods", "picUrl");
    }

    private void createFileRecord(String fileId, String bizObjName, String fieldName) {
        NopFileRecord record = daoProvider.daoFor(NopFileRecord.class).newEntity();
        record.setFileId(fileId);
        record.setBizObjName(bizObjName);
        record.setBizObjId("temp");
        record.setFieldName(fieldName);
        record.setOriginFileId(fileId);
        record.setFileName(fileId + ".png");
        record.setFilePath("/test/" + fileId + ".png");
        record.setFileExt("png");
        record.setMimeType("image/png");
        record.setIsPublic(true);
        daoProvider.daoFor(NopFileRecord.class).saveEntity(record);
    }
}
