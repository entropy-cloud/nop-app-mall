package app.mall.service.entity;

import app.mall.dao.entity.LitemallCoupon;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallCouponBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String couponId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

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
        couponId = coupon.getId();
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
}
