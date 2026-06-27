package app.mall.service.entity;

import app.mall.dao.entity.LitemallCoupon;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallOrder;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallCouponBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String couponId;
    String goodsScopedCouponId;
    String categoryScopedCouponId;
    String goodsId;
    String categoryId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("COUPON-G-001");
        goods.setName("Coupon Test Goods");
        goods.setRetailPrice(BigDecimal.valueOf(100));
        goods.setIsOnSale(true);
        goods.setPicUrl("");
        goods.setShareUrl("");
        goods.setGallery("");
        categoryId = "100100";
        goods.setCategoryId(categoryId);
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.orm_idString();

        LitemallCoupon coupon = daoProvider.daoFor(LitemallCoupon.class).newEntity();
        coupon.setName("满100减20");
        coupon.setDesc("满100减20优惠券");
        coupon.setTag("通用");
        coupon.setTotal(100);
        coupon.setDiscount(BigDecimal.valueOf(20));
        coupon.setMin(BigDecimal.valueOf(100));
        coupon.setLimit(1);
        coupon.setType(0);
        coupon.setStatus(0);
        coupon.setGoodsType(0);
        coupon.setGoodsValue("");
        coupon.setCode("");
        coupon.setTimeType(0);
        coupon.setDays(30);
        coupon.setStartTime(LocalDateTime.now().minusDays(1));
        coupon.setEndTime(LocalDateTime.now().plusDays(30));
        daoProvider.daoFor(LitemallCoupon.class).saveEntity(coupon);
        couponId = coupon.orm_idString();

        LitemallCoupon goodsScoped = daoProvider.daoFor(LitemallCoupon.class).newEntity();
        goodsScoped.setName("指定商品券");
        goodsScoped.setTag("商品限定");
        goodsScoped.setTotal(50);
        goodsScoped.setDiscount(BigDecimal.valueOf(10));
        goodsScoped.setMin(BigDecimal.valueOf(50));
        goodsScoped.setLimit(1);
        goodsScoped.setType(0);
        goodsScoped.setStatus(0);
        goodsScoped.setGoodsType(2);
        goodsScoped.setGoodsValue(goodsId);
        goodsScoped.setCode("");
        goodsScoped.setTimeType(0);
        goodsScoped.setDays(15);
        goodsScoped.setStartTime(LocalDateTime.now().minusDays(1));
        goodsScoped.setEndTime(LocalDateTime.now().plusDays(15));
        daoProvider.daoFor(LitemallCoupon.class).saveEntity(goodsScoped);
        goodsScopedCouponId = goodsScoped.orm_idString();

        LitemallCoupon categoryScoped = daoProvider.daoFor(LitemallCoupon.class).newEntity();
        categoryScoped.setName("分类券");
        categoryScoped.setTag("分类限定");
        categoryScoped.setTotal(50);
        categoryScoped.setDiscount(BigDecimal.valueOf(15));
        categoryScoped.setMin(BigDecimal.valueOf(30));
        categoryScoped.setLimit(1);
        categoryScoped.setType(0);
        categoryScoped.setStatus(0);
        categoryScoped.setGoodsType(1);
        categoryScoped.setGoodsValue(categoryId);
        categoryScoped.setCode("");
        categoryScoped.setTimeType(0);
        categoryScoped.setDays(15);
        categoryScoped.setStartTime(LocalDateTime.now().minusDays(1));
        categoryScoped.setEndTime(LocalDateTime.now().plusDays(15));
        daoProvider.daoFor(LitemallCoupon.class).saveEntity(categoryScoped);
        categoryScopedCouponId = categoryScoped.orm_idString();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPublishAndUnpublish() {
        ApiRequest<Map<String, Object>> unpublishReq = ApiRequest.build(Map.of("id", couponId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCoupon__unpublishCoupon", unpublishReq);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());
        assertEquals(2, ((Number) ((Map<String, Object>) result.getData()).get("status")).intValue());

        ApiRequest<Map<String, Object>> publishReq = ApiRequest.build(Map.of("id", couponId));
        IGraphQLExecutionContext pubCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCoupon__publishCoupon", publishReq);
        ApiResponse<?> pubResult = graphQLEngine.executeRpc(pubCtx);
        assertEquals(0, pubResult.getStatus());
        assertEquals(0, ((Number) ((Map<String, Object>) pubResult.getData()).get("status")).intValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListAvailableCoupons() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCoupon__listAvailableCoupons", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());
        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        assertTrue(((java.util.List<?>) pageData.get("items")).size() >= 1);
    }

    @Test
    public void testListMyCoupons() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCoupon__listMyCoupons", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListCouponsForGoods_includesAllGoodsTypeCoupon() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("goodsId", goodsId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCoupon__listCouponsForGoods", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "listCouponsForGoods failed: " + result);

        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData();
        assertNotNull(items);
        assertTrue(items.size() >= 3, "should include ALL/GOODS/CATEGORY scoped coupons");

        boolean hasAllScoped = false;
        boolean hasGoodsScoped = false;
        boolean hasCategoryScoped = false;
        for (Map<String, Object> item : items) {
            String id = String.valueOf(item.get("id"));
            if (couponId.equals(id)) {
                hasAllScoped = true;
            }
            if (goodsScopedCouponId.equals(id)) {
                hasGoodsScoped = true;
            }
            if (categoryScopedCouponId.equals(id)) {
                hasCategoryScoped = true;
            }
            assertEquals(false, item.get("claimedByMe"), "fresh user should not have claimed");
            assertEquals(true, item.get("claimable"), "fresh user should be able to claim");
        }
        assertTrue(hasAllScoped, "goodsType=0 (ALL) coupon should match any goods");
        assertTrue(hasGoodsScoped, "goodsType=2 (GOODS) coupon matching goodsId should be returned");
        assertTrue(hasCategoryScoped, "goodsType=1 (CATEGORY) coupon matching goods.categoryId should be returned");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListCouponsForGoods_excludesNonMatchingGoodsScopedCoupon() {
        LitemallGoods otherGoods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        otherGoods.setGoodsSn("COUPON-G-002");
        otherGoods.setName("Other Goods");
        otherGoods.setRetailPrice(BigDecimal.valueOf(100));
        otherGoods.setIsOnSale(true);
        otherGoods.setPicUrl("");
        otherGoods.setShareUrl("");
        otherGoods.setGallery("");
        otherGoods.setCategoryId("999999");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(otherGoods);
        String otherGoodsId = otherGoods.orm_idString();

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("goodsId", otherGoodsId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCoupon__listCouponsForGoods", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());

        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData();
        for (Map<String, Object> item : items) {
            String id = String.valueOf(item.get("id"));
            assertNotEquals(goodsScopedCouponId, id, "GOODS-scoped coupon should NOT match other goods");
            assertNotEquals(categoryScopedCouponId, id, "CATEGORY-scoped coupon should NOT match other category");
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListCouponsForGoods_marksClaimedByMeAfterClaim() {
        ApiRequest<Map<String, Object>> claimReq = ApiRequest.build(Map.of("couponId", goodsScopedCouponId));
        ApiResponse<?> claimRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon", claimReq));
        assertEquals(0, claimRes.getStatus(), "claim setup failed");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("goodsId", goodsId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCoupon__listCouponsForGoods", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());

        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData();
        for (Map<String, Object> item : items) {
            String id = String.valueOf(item.get("id"));
            if (goodsScopedCouponId.equals(id)) {
                assertEquals(true, item.get("claimedByMe"), "claimed coupon should be marked claimedByMe=true");
                assertEquals(false, item.get("claimable"), "limit=1 and already claimed → claimable=false");
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListCouponsForGoods_anonymousClaimedByMeIsFalse() {
        ContextProvider.getOrCreateContext().setUserId(null);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("goodsId", goodsId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCoupon__listCouponsForGoods", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());

        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData();
        for (Map<String, Object> item : items) {
            assertEquals(false, item.get("claimedByMe"),
                    "anonymous user (userId=null) should always see claimedByMe=false");
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCouponUsageStatistics() {
        // Build an order that the used coupon pulls GMV from.
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).newEntity();
        order.setUserId("1");
        order.setOrderSn("COUPON-STAT-001");
        order.setOrderStatus(201);
        order.setAftersaleStatus(0);
        order.setConsignee("测试用户");
        order.setMobile("13800138000");
        order.setAddress("测试地址");
        order.setMessage("coupon-stat");
        order.setGoodsPrice(new BigDecimal("100"));
        order.setFreightPrice(BigDecimal.ZERO);
        order.setCouponPrice(BigDecimal.ZERO);
        order.setIntegralPrice(BigDecimal.ZERO);
        order.setGrouponPrice(BigDecimal.ZERO);
        order.setPromotionPrice(BigDecimal.ZERO);
        order.setPinTuanPrice(BigDecimal.ZERO);
        order.setOrderPrice(new BigDecimal("100"));
        order.setActualPrice(new BigDecimal("100"));
        order.setComments(0);
        order.setDeleted(false);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(order);
        String orderId = order.orm_idString();

        // 2 claimed-but-unused + 1 used (status=1) → claimed=3, used=1, pulledGmv=100
        for (int i = 0; i < 2; i++) {
            LitemallCouponUser unused = daoProvider.daoFor(LitemallCouponUser.class).newEntity();
            unused.setUserId("1");
            unused.setCouponId(couponId);
            unused.setStatus(0);
            unused.setStartTime(LocalDateTime.now().minusDays(1));
            unused.setEndTime(LocalDateTime.now().plusDays(30));
            daoProvider.daoFor(LitemallCouponUser.class).saveEntity(unused);
        }
        LitemallCouponUser used = daoProvider.daoFor(LitemallCouponUser.class).newEntity();
        used.setUserId("1");
        used.setCouponId(couponId);
        used.setStatus(1);
        used.setOrderId(orderId);
        used.setUsedTime(LocalDateTime.now());
        used.setStartTime(LocalDateTime.now().minusDays(1));
        used.setEndTime(LocalDateTime.now().plusDays(30));
        daoProvider.daoFor(LitemallCouponUser.class).saveEntity(used);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("couponId", couponId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCoupon__getCouponUsageStatistics", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getCouponUsageStatistics failed: " + result);

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(3, ((Number) data.get("claimedCount")).intValue(), "claimedCount");
        assertEquals(1, ((Number) data.get("usedCount")).intValue(), "usedCount");
        assertEquals(0, new BigDecimal("100").compareTo(new BigDecimal(data.get("pulledGmv").toString())),
                "pulledGmv should equal the used order actualPrice");
    }
}
