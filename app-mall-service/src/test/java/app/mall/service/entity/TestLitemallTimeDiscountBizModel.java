package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallTimeDiscount;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallTimeDiscountBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String goodsId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G-TD");
        goods.setName("Time Discount Goods");
        goods.setCounterPrice(BigDecimal.valueOf(100));
        goods.setRetailPrice(BigDecimal.valueOf(99));
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.getId();
    }

    private LitemallTimeDiscount createDiscount(int discountType, BigDecimal discountValue, int status,
                                                 String productId, Integer stockLimit,
                                                 LocalDateTime startTime, LocalDateTime endTime) {
        LitemallTimeDiscount d = daoProvider.daoFor(LitemallTimeDiscount.class).newEntity();
        d.setGoodsId(goodsId);
        d.setProductId(productId);
        d.setDiscountType(discountType);
        d.setDiscountValue(discountValue);
        d.setStatus(status);
        d.setStockLimit(stockLimit);
        d.setStartTime(startTime);
        d.setEndTime(endTime);
        daoProvider.daoFor(LitemallTimeDiscount.class).saveEntity(d);
        return d;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> selectTimeDiscount(String productId) {
        Map<String, Object> data = new HashMap<>();
        data.put("goodsId", goodsId);
        if (productId != null) {
            data.put("productId", productId);
        }
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallTimeDiscount__selectTimeDiscountForProduct", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "selectTimeDiscountForProduct failed: " + result);
        return (Map<String, Object>) result.getData();
    }

    private BigDecimal promo(Map<String, Object> m) {
        return m == null ? null : new BigDecimal(m.get("promoPrice").toString());
    }

    @Test
    public void testPercentDiscount() {
        createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_PERCENT, new BigDecimal("0.9"),
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, null, 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        Map<String, Object> m = selectTimeDiscount(null);
        // 99 * 0.9 = 89.10
        assertEquals(0, new BigDecimal("89.10").compareTo(promo(m)));
        assertEquals(0, new BigDecimal("99").compareTo(new BigDecimal(m.get("originalPrice").toString())));
        assertEquals(0, new BigDecimal("9.90").compareTo(new BigDecimal(m.get("discountAmount").toString())));
        assertEquals(10, m.get("discountType"));
    }

    @Test
    public void testAmountDiscount() {
        createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("20"),
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, null, 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        Map<String, Object> m = selectTimeDiscount(null);
        // 99 - 20 = 79
        assertEquals(0, new BigDecimal("79.00").compareTo(promo(m)));
        assertEquals(0, new BigDecimal("20.00").compareTo(new BigDecimal(m.get("discountAmount").toString())));
    }

    @Test
    public void testDraftNotMatched() {
        createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("20"),
                _AppMallDaoConstants.PROMOTION_STATUS_DRAFT, null, 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertNull(selectTimeDiscount(null), "draft discount should not match");
    }

    @Test
    public void testOutOfTimeWindowNotMatched() {
        createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("20"),
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, null, 0,
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1));
        assertNull(selectTimeDiscount(null), "expired discount should not match");
    }

    @Test
    public void testFutureStartNotMatched() {
        createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("20"),
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, null, 0,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(10));
        assertNull(selectTimeDiscount(null), "future discount should not match");
    }

    @Test
    public void testMultiDiscountLowestPriceWins() {
        // discount A: amount 20 -> 79
        createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("20"),
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, null, 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        // discount B: amount 40 -> 59 (better for user)
        createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("40"),
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, null, 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertEquals(0, new BigDecimal("59.00").compareTo(promo(selectTimeDiscount(null))),
                "should pick the lowest promoPrice");
    }

    @Test
    public void testProductIdExactMatchAndFallback() {
        // null-productId discount (all SKU): amount 20 -> 79
        createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("20"),
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, null, 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        // exact-SKU discount: amount 40 -> 59 (better)
        createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("40"),
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, "1", 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        // requesting SKU 1: both match, lowest wins -> 59
        assertEquals(0, new BigDecimal("59.00").compareTo(promo(selectTimeDiscount("1"))));
        // requesting SKU 2: only null-productId matches -> 79
        assertEquals(0, new BigDecimal("79.00").compareTo(promo(selectTimeDiscount("2"))));
    }

    @Test
    public void testExactSkuDiscountNotMatchOtherSku() {
        createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("40"),
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, "1", 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertNull(selectTimeDiscount("2"), "exact-SKU discount must not match a different SKU");
    }

    @Test
    public void testOverDiscountInvalid() {
        // amount exceeds retail -> promoPrice <= 0 -> invalid
        createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("200"),
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, null, 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertNull(selectTimeDiscount(null), "over-discount (promo<=0) should not match");
    }

    @Test
    public void testPercentNotBelowRetailInvalid() {
        // percent 1.0 -> promoPrice == retail -> not a discount -> invalid
        createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_PERCENT, new BigDecimal("1.0"),
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, null, 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertNull(selectTimeDiscount(null), "percent rate >= 1 should not match (not a discount)");
    }

    @Test
    public void testStockLimitAndCountdownReturned() {
        LitemallTimeDiscount d = createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("20"),
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, null, 50,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2));
        Map<String, Object> m = selectTimeDiscount(null);
        assertEquals(50, m.get("stockLimit"), "stockLimit should be returned for countdown/progress");
        assertEquals(50, m.get("remainingStock"));
        assertEquals(d.getEndTime(), m.get("endTime"), "endTime should be returned for countdown");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPublishAndUnpublishActivity() {
        LitemallTimeDiscount draft = createDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("20"),
                _AppMallDaoConstants.PROMOTION_STATUS_DRAFT, null, 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        String id = draft.orm_idString();

        ApiRequest<Map<String, Object>> pubReq = ApiRequest.build(Map.of("id", id));
        ApiResponse<?> pubRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallTimeDiscount__publishActivity", pubReq));
        assertEquals(0, pubRes.getStatus(), "publishActivity failed: " + pubRes);
        assertEquals(_AppMallDaoConstants.PROMOTION_STATUS_ACTIVE,
                ((Number) ((Map<String, Object>) pubRes.getData()).get("status")).intValue());

        // active -> publish again should fail
        ApiResponse<?> republish = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallTimeDiscount__publishActivity", pubReq));
        assertNotEquals(0, republish.getStatus(), "re-publish active should fail");

        // active -> unpublish -> closed
        ApiResponse<?> unpubRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallTimeDiscount__unpublishActivity", pubReq));
        assertEquals(0, unpubRes.getStatus(), "unpublishActivity failed: " + unpubRes);
        assertEquals(_AppMallDaoConstants.PROMOTION_STATUS_CLOSED,
                ((Number) ((Map<String, Object>) unpubRes.getData()).get("status")).intValue());
    }
}
