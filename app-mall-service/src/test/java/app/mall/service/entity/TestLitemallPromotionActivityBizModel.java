package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallPromotionActivity;
import app.mall.dao.entity.LitemallPromotionTier;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
