package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallPromotionActivity;
import app.mall.dao.entity.LitemallPromotionTier;
import app.mall.dao.entity.LitemallPromotionUsage;
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

import java.math.BigDecimal;
import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallPromotionActivityBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");
    }

    private LitemallPromotionActivity createActivity(int discountType, int status, int goodsScope,
                                                     String goodsScopeValue, int priority,
                                                     LocalDateTime startTime, LocalDateTime endTime) {
        LitemallPromotionActivity a = daoProvider.daoFor(LitemallPromotionActivity.class).newEntity();
        a.setName("test-act-" + priority);
        a.setDiscountType(discountType);
        a.setStatus(status);
        a.setGoodsScope(goodsScope);
        a.setGoodsScopeValue(goodsScopeValue);
        a.setPriority(priority);
        a.setStartTime(startTime);
        a.setEndTime(endTime);
        daoProvider.daoFor(LitemallPromotionActivity.class).saveEntity(a);
        return a;
    }

    private LitemallPromotionActivity createActiveActivity(int discountType, int goodsScope,
                                                           String goodsScopeValue, int priority) {
        return createActivity(discountType, _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, goodsScope,
                goodsScopeValue, priority, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
    }

    private void addTier(LitemallPromotionActivity a, BigDecimal meetAmount, BigDecimal discountValue) {
        LitemallPromotionTier t = daoProvider.daoFor(LitemallPromotionTier.class).newEntity();
        t.setActivityId(a.getId());
        t.setMeetAmount(meetAmount);
        t.setDiscountValue(discountValue);
        daoProvider.daoFor(LitemallPromotionTier.class).saveEntity(t);
    }

    private BigDecimal selectPromotion(BigDecimal goodsPrice, List<String> goodsScopeIds) {
        Map<String, Object> data = new HashMap<>();
        data.put("goodsPrice", goodsPrice);
        if (goodsScopeIds != null) {
            data.put("goodsScopeIds", goodsScopeIds);
        }
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallPromotionActivity__selectPromotionForOrder", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "selectPromotionForOrder failed: " + result);
        return (BigDecimal) result.getData();
    }

    private ApiResponse<?> callExportMarketingReport(Map<String, Object> data) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallPromotionActivity__exportMarketingReport", req);
        return graphQLEngine.executeRpc(ctx);
    }

    private File renderMarketingReport(Map<String, Object> data) {
        ApiResponse<?> r = callExportMarketingReport(data);
        assertEquals(0, r.getStatus(), "exportMarketingReport failed: " + r);
        File file = WebContentBeanFiles.contentFile(r.getData());
        assertNotNull(file, "rendered file should not be null: " + r.getData());
        assertTrue(file.exists(), "rendered file should exist: " + file);
        return file;
    }

    @Test
    public void testExportMarketingReportXlsx() {
        File f = renderMarketingReport(Map.of("renderType", "xlsx"));
        assertTrue(f.length() > 0, "marketing xlsx should be non-empty");
    }

    @Test
    public void testExportMarketingReportPdf() {
        File f = renderMarketingReport(Map.of("renderType", "pdf"));
        assertTrue(f.length() > 0, "marketing pdf should be non-empty");
    }

    @Test
    public void testExportMarketingReportWithAttribution() {
        LitemallPromotionActivity activity = createActiveActivity(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT,
                _AppMallDaoConstants.GOODS_SCOPE_ALL, null, 1);
        File f = renderMarketingReport(Map.of(
                "renderType", "xlsx",
                "promotionActivityId", activity.orm_idString(),
                "flashSaleId", "non-existent-flash",
                "pinTuanActivityId", "non-existent-pintuan",
                "couponId", "non-existent-coupon"));
        assertTrue(f.length() > 0, "marketing xlsx with attribution params should be non-empty");
    }

    @Test
    public void testExportMarketingReportEmptyDataNotError() {
        ApiResponse<?> r = callExportMarketingReport(Map.of(
                "renderType", "xlsx",
                "startDate", "2099-01-01",
                "endDate", "2099-01-02"));
        assertEquals(0, r.getStatus(), "empty marketing export should not error: " + r);
    }

    @Test
    public void testExportMarketingReportInvalidRenderType() {
        ApiResponse<?> r = callExportMarketingReport(Map.of("renderType", "docx"));
        assertEquals(-1, r.getStatus(), "invalid renderType should be rejected: " + r);
    }

    @Test
    public void testMultiTierBestTier() {
        LitemallPromotionActivity a = createActiveActivity(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT,
                _AppMallDaoConstants.GOODS_SCOPE_ALL, null, 1);
        addTier(a, new BigDecimal("100"), new BigDecimal("10"));
        addTier(a, new BigDecimal("200"), new BigDecimal("30"));

        assertEquals(0, new BigDecimal("30").compareTo(selectPromotion(new BigDecimal("250"), null)));
        assertEquals(0, new BigDecimal("10").compareTo(selectPromotion(new BigDecimal("150"), null)));
        assertEquals(0, BigDecimal.ZERO.compareTo(selectPromotion(new BigDecimal("50"), null)));
    }

    @Test
    public void testPercentDiscount() {
        LitemallPromotionActivity a = createActiveActivity(_AppMallDaoConstants.DISCOUNT_TYPE_PERCENT,
                _AppMallDaoConstants.GOODS_SCOPE_ALL, null, 1);
        addTier(a, new BigDecimal("100"), new BigDecimal("0.9"));

        assertEquals(0, new BigDecimal("20.00").compareTo(selectPromotion(new BigDecimal("200"), null)));
        assertEquals(0, BigDecimal.ZERO.compareTo(selectPromotion(new BigDecimal("50"), null)));
    }

    @Test
    public void testScopeCategoryMatch() {
        LitemallPromotionActivity a = createActiveActivity(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT,
                _AppMallDaoConstants.GOODS_SCOPE_CATEGORY, "[\"cat-1\"]", 1);
        addTier(a, new BigDecimal("100"), new BigDecimal("20"));

        assertEquals(0, new BigDecimal("20").compareTo(
                selectPromotion(new BigDecimal("200"), List.of("goods-1", "cat-1"))));
        assertEquals(0, BigDecimal.ZERO.compareTo(
                selectPromotion(new BigDecimal("200"), List.of("goods-1", "cat-2"))));
    }

    @Test
    public void testScopeGoodsMatch() {
        LitemallPromotionActivity a = createActiveActivity(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT,
                _AppMallDaoConstants.GOODS_SCOPE_GOODS, "[\"goods-1\"]", 1);
        addTier(a, new BigDecimal("100"), new BigDecimal("20"));

        assertEquals(0, new BigDecimal("20").compareTo(
                selectPromotion(new BigDecimal("200"), List.of("goods-1", "cat-1"))));
        assertEquals(0, BigDecimal.ZERO.compareTo(
                selectPromotion(new BigDecimal("200"), List.of("goods-2", "cat-1"))));
    }

    @Test
    public void testDraftActivityNotMatched() {
        createActivity(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, _AppMallDaoConstants.PROMOTION_STATUS_DRAFT,
                _AppMallDaoConstants.GOODS_SCOPE_ALL, null, 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertEquals(0, BigDecimal.ZERO.compareTo(selectPromotion(new BigDecimal("500"), null)));
    }

    @Test
    public void testOutOfTimeWindowNotMatched() {
        createActivity(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE,
                _AppMallDaoConstants.GOODS_SCOPE_ALL, null, 1,
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1));
        assertEquals(0, BigDecimal.ZERO.compareTo(selectPromotion(new BigDecimal("500"), null)));
    }

    @Test
    public void testPriorityWinsOverDiscount() {
        LitemallPromotionActivity low = createActiveActivity(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT,
                _AppMallDaoConstants.GOODS_SCOPE_ALL, null, 1);
        addTier(low, new BigDecimal("100"), new BigDecimal("50"));
        LitemallPromotionActivity high = createActiveActivity(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT,
                _AppMallDaoConstants.GOODS_SCOPE_ALL, null, 9);
        addTier(high, new BigDecimal("100"), new BigDecimal("20"));

        assertEquals(0, new BigDecimal("20").compareTo(selectPromotion(new BigDecimal("200"), null)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPublishAndUnpublishActivity() {
        LitemallPromotionActivity draft = createActivity(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT,
                _AppMallDaoConstants.PROMOTION_STATUS_DRAFT,
                _AppMallDaoConstants.GOODS_SCOPE_ALL, null, 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        String id = draft.orm_idString();

        // draft(0) -> publish -> active(10)
        ApiRequest<Map<String, Object>> pubReq = ApiRequest.build(Map.of("id", id));
        ApiResponse<?> pubRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPromotionActivity__publishActivity", pubReq));
        assertEquals(0, pubRes.getStatus(), "publishActivity failed: " + pubRes);
        assertEquals(_AppMallDaoConstants.PROMOTION_STATUS_ACTIVE,
                ((Number) ((Map<String, Object>) pubRes.getData()).get("status")).intValue());

        // active(10) -> publish again -> fail (invalid transition)
        ApiResponse<?> republish = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPromotionActivity__publishActivity", pubReq));
        assertNotEquals(0, republish.getStatus(), "re-publish active activity should fail");

        // active(10) -> unpublish -> closed(30)
        ApiRequest<Map<String, Object>> unpubReq = ApiRequest.build(Map.of("id", id));
        ApiResponse<?> unpubRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPromotionActivity__unpublishActivity", unpubReq));
        assertEquals(0, unpubRes.getStatus(), "unpublishActivity failed: " + unpubRes);
        assertEquals(_AppMallDaoConstants.PROMOTION_STATUS_CLOSED,
                ((Number) ((Map<String, Object>) unpubRes.getData()).get("status")).intValue());

        // draft(0) -> unpublish -> fail (draft need not be unpublished)
        LitemallPromotionActivity anotherDraft = createActivity(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT,
                _AppMallDaoConstants.PROMOTION_STATUS_DRAFT,
                _AppMallDaoConstants.GOODS_SCOPE_ALL, null, 2,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        ApiRequest<Map<String, Object>> draftUnpub = ApiRequest.build(Map.of("id", anotherDraft.orm_idString()));
        ApiResponse<?> draftUnpubRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPromotionActivity__unpublishActivity", draftUnpub));
        assertNotEquals(0, draftUnpubRes.getStatus(), "unpublish draft activity should fail");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetPromotionEffectiveness() {
        // One order hit by a promotion (promotionPrice>0), one order not (promotionPrice=0).
        LitemallOrder promoted = daoProvider.daoFor(LitemallOrder.class).newEntity();
        promoted.setUserId("1");
        promoted.setOrderSn("PROMO-STAT-001");
        promoted.setOrderStatus(201);
        promoted.setAftersaleStatus(0);
        promoted.setConsignee("测试用户");
        promoted.setMobile("13800138000");
        promoted.setAddress("测试地址");
        promoted.setMessage("promo-stat");
        promoted.setGoodsPrice(new BigDecimal("200"));
        promoted.setFreightPrice(BigDecimal.ZERO);
        promoted.setCouponPrice(BigDecimal.ZERO);
        promoted.setIntegralPrice(BigDecimal.ZERO);
        promoted.setGrouponPrice(BigDecimal.ZERO);
        promoted.setPromotionPrice(new BigDecimal("30"));
        promoted.setPinTuanPrice(BigDecimal.ZERO);
        promoted.setOrderPrice(new BigDecimal("170"));
        promoted.setActualPrice(new BigDecimal("170"));
        promoted.setComments(0);
        promoted.setDeleted(false);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(promoted);

        LitemallOrder plain = daoProvider.daoFor(LitemallOrder.class).newEntity();
        plain.setUserId("1");
        plain.setOrderSn("PROMO-STAT-002");
        plain.setOrderStatus(201);
        plain.setAftersaleStatus(0);
        plain.setConsignee("测试用户");
        plain.setMobile("13800138000");
        plain.setAddress("测试地址");
        plain.setMessage("promo-stat");
        plain.setGoodsPrice(new BigDecimal("50"));
        plain.setFreightPrice(BigDecimal.ZERO);
        plain.setCouponPrice(BigDecimal.ZERO);
        plain.setIntegralPrice(BigDecimal.ZERO);
        plain.setGrouponPrice(BigDecimal.ZERO);
        plain.setPromotionPrice(BigDecimal.ZERO);
        plain.setPinTuanPrice(BigDecimal.ZERO);
        plain.setOrderPrice(new BigDecimal("50"));
        plain.setActualPrice(new BigDecimal("50"));
        plain.setComments(0);
        plain.setDeleted(false);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(plain);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(new HashMap<>());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallPromotionActivity__getPromotionEffectiveness", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getPromotionEffectiveness failed: " + result);

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertTrue(((Number) data.get("promotedOrderCount")).intValue() >= 1,
                "promotedOrderCount should include the promotion-hit order");
        assertEquals(0, new BigDecimal("30").compareTo(new BigDecimal(data.get("totalDiscount").toString())),
                "totalDiscount should equal the promotion discount sum");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetPromotionEffectivenessByActivity() {
        // Per-activity attribution (PromotionUsage-based): two users participated in one activity.
        LitemallPromotionActivity activity = createActiveActivity(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT,
                _AppMallDaoConstants.GOODS_SCOPE_ALL, null, 1);

        LitemallOrder o1 = newPaidOrder("1", "PROMO-BY-ACT-1", new BigDecimal("200"));
        LitemallOrder o2 = newPaidOrder("2", "PROMO-BY-ACT-2", new BigDecimal("300"));
        saveUsage("1", activity.orm_idString(), o1.orm_idString(), new BigDecimal("30"));
        saveUsage("2", activity.orm_idString(), o2.orm_idString(), new BigDecimal("50"));

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("activityId", activity.orm_idString()));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallPromotionActivity__getPromotionEffectiveness", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getPromotionEffectiveness(byActivity) failed: " + result);

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(2, ((Number) data.get("promotedOrderCount")).intValue(), "two participation orders");
        assertEquals(2, ((Number) data.get("participantCount")).intValue(), "two distinct users");
        assertEquals(0, new BigDecimal("80").compareTo(new BigDecimal(data.get("totalDiscount").toString())),
                "totalDiscount = 30 + 50");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetPromotionEffectivenessAggregateWithoutActivityId() {
        // activityId null → time-window aggregate over promotionPrice>0 orders (existing contract).
        createActiveActivity(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT,
                _AppMallDaoConstants.GOODS_SCOPE_ALL, null, 1);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(new HashMap<>());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallPromotionActivity__getPromotionEffectiveness", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "aggregate getPromotionEffectiveness failed: " + result);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("promotedOrderCount"));
    }

    private LitemallOrder newPaidOrder(String userId, String orderSn, BigDecimal orderPrice) {
        LitemallOrder o = daoProvider.daoFor(LitemallOrder.class).newEntity();
        o.setUserId(userId);
        o.setOrderSn(orderSn);
        o.setOrderStatus(201);
        o.setAftersaleStatus(0);
        o.setConsignee("测试");
        o.setMobile("13800138000");
        o.setAddress("测试地址");
        o.setMessage("by-activity");
        o.setGoodsPrice(orderPrice);
        o.setFreightPrice(BigDecimal.ZERO);
        o.setCouponPrice(BigDecimal.ZERO);
        o.setIntegralPrice(BigDecimal.ZERO);
        o.setGrouponPrice(BigDecimal.ZERO);
        o.setPromotionPrice(BigDecimal.ZERO);
        o.setPinTuanPrice(BigDecimal.ZERO);
        o.setOrderPrice(orderPrice);
        o.setActualPrice(orderPrice);
        o.setComments(0);
        o.setDeleted(false);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(o);
        return o;
    }

    private void saveUsage(String userId, String activityId, String orderId, BigDecimal discount) {
        LitemallPromotionUsage u = daoProvider.daoFor(LitemallPromotionUsage.class).newEntity();
        u.setUserId(userId);
        u.setPromotionActivityId(activityId);
        u.setOrderId(orderId);
        u.setMeetAmount(BigDecimal.ZERO);
        u.setDiscountAmount(discount);
        daoProvider.daoFor(LitemallPromotionUsage.class).saveEntity(u);
    }
}