package app.mall.service.entity;

import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.dao.entity.LitemallCoupon;
import app.mall.dao.entity.LitemallCouponUser;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.api.core.exceptions.NopException;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.core.context.IServiceContext;
import io.nop.core.context.ServiceContextImpl;
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
public class TestLitemallCouponUserBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    ILitemallCouponUserBiz couponUserBiz;

    String couponId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        LitemallCoupon coupon = daoProvider.daoFor(LitemallCoupon.class).newEntity();
        coupon.setName("满100减20");
        coupon.setTag("通用");
        coupon.setTotal(100);
        coupon.setDiscount(BigDecimal.valueOf(20));
        coupon.setMin(BigDecimal.valueOf(100));
        coupon.setLimit(1);
        coupon.setType(0);
        coupon.setStatus(0);
        coupon.setGoodsType(0);
        coupon.setGoodsValue("");
        coupon.setCode("TESTCODE");
        coupon.setTimeType(0);
        coupon.setDays(30);
        coupon.setStartTime(LocalDateTime.now().minusDays(1));
        coupon.setEndTime(LocalDateTime.now().plusDays(30));
        daoProvider.daoFor(LitemallCoupon.class).saveEntity(coupon);
        couponId = coupon.getId();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testClaimCoupon() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("couponId", couponId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "claimCoupon failed: " + result);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals(0, ((Number) data.get("status")).intValue());
        assertEquals(couponId, data.get("couponId"));
    }

    @Test
    public void testClaimLimitExceeded() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("couponId", couponId));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon", req));

        ApiResponse<?> result2 = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon", req));
        assertEquals(-1, result2.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRedeemCoupon() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("code", "TESTCODE"));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__redeemCoupon", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "redeemCoupon failed: " + result);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0, ((Number) data.get("status")).intValue());
    }

    @Test
    public void testSelectCouponForOrder() {
        ApiRequest<Map<String, Object>> claimReq = ApiRequest.build(Map.of("couponId", couponId));
        ApiResponse<?> claimRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon", claimReq));
        String couponUserId = (String) ((Map<String, Object>) claimRes.getData()).get("id");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "couponUserId", couponUserId,
                "goodsPrice", BigDecimal.valueOf(200),
                "goodsIds", List.of()
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCouponUser__selectCouponForOrder", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "selectCouponForOrder failed: " + result);
        BigDecimal discount = (BigDecimal) result.getData();
        assertEquals(0, BigDecimal.valueOf(20).compareTo(discount));
    }

    @Test
    public void testSelectCouponMinNotMet() {
        ApiRequest<Map<String, Object>> claimReq = ApiRequest.build(Map.of("couponId", couponId));
        ApiResponse<?> claimRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon", claimReq));
        String couponUserId = (String) ((Map<String, Object>) claimRes.getData()).get("id");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "couponUserId", couponUserId,
                "goodsPrice", BigDecimal.valueOf(50),
                "goodsIds", List.of()
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCouponUser__selectCouponForOrder", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(-1, result.getStatus());
    }

    @Test
    public void testUseAndReturnCoupon() {
        ApiRequest<Map<String, Object>> claimReq = ApiRequest.build(Map.of("couponId", couponId));
        ApiResponse<?> claimRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon", claimReq));
        String couponUserId = (String) ((Map<String, Object>) claimRes.getData()).get("id");

        ApiRequest<Map<String, Object>> useReq = ApiRequest.build(Map.of(
                "couponUserId", couponUserId, "orderId", "100"));
        IGraphQLExecutionContext useCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__useCoupon", useReq);
        ApiResponse<?> useResult = graphQLEngine.executeRpc(useCtx);
        assertEquals(0, useResult.getStatus());

        ApiRequest<Map<String, Object>> returnReq = ApiRequest.build(Map.of("couponUserId", couponUserId));
        IGraphQLExecutionContext returnCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__returnCoupon", returnReq);
        ApiResponse<?> returnResult = graphQLEngine.executeRpc(returnCtx);
        assertEquals(0, returnResult.getStatus());
    }

    @Test
    public void testClaimCouponForUserSuccess() {
        IServiceContext ctx = new ServiceContextImpl();
        String targetUserId = "user-ar10-1";
        LitemallCouponUser result = couponUserBiz.claimCouponForUser(couponId, targetUserId, ctx);

        assertNotNull(result);
        assertEquals(targetUserId, result.getUserId());
        assertEquals(couponId, result.getCouponId());
        assertEquals(0, result.getStatus());

        QueryBean verifyQuery = new QueryBean();
        verifyQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_userId, targetUserId));
        verifyQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_couponId, couponId));
        verifyQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_deleted, false));
        long count = couponUserBiz.findCount(verifyQuery, ctx);
        assertEquals(1, count, "DB should contain exactly one claim record");
    }

    @Test
    public void testClaimCouponForUserCouponNotFound() {
        IServiceContext ctx = new ServiceContextImpl();
        assertThrows(NopException.class, () ->
                couponUserBiz.claimCouponForUser("non-existent-coupon-id", "user-ar10-2", ctx));
    }

    @Test
    public void testClaimCouponForUserDuplicateRejected() {
        IServiceContext ctx = new ServiceContextImpl();
        String targetUserId = "user-ar10-3";

        LitemallCouponUser first = couponUserBiz.claimCouponForUser(couponId, targetUserId, ctx);
        assertNotNull(first);

        assertThrows(NopException.class, () ->
                couponUserBiz.claimCouponForUser(couponId, targetUserId, ctx));
    }
}
