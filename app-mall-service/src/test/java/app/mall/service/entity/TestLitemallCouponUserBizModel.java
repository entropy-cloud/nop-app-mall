package app.mall.service.entity;

import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallCoupon;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallSystem;
import app.mall.dao.entity.LitemallUserMessage;
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
import java.util.HashMap;
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
    String categoryScopedCouponId;
    String categoryMatchGoodsId;
    String categoryMismatchGoodsId;
    String categoryIdMatch = "100100";

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
        couponId = coupon.orm_idString();

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
        categoryScoped.setGoodsValue(categoryIdMatch);
        categoryScoped.setCode("");
        categoryScoped.setTimeType(0);
        categoryScoped.setDays(15);
        categoryScoped.setStartTime(LocalDateTime.now().minusDays(1));
        categoryScoped.setEndTime(LocalDateTime.now().plusDays(15));
        daoProvider.daoFor(LitemallCoupon.class).saveEntity(categoryScoped);
        categoryScopedCouponId = categoryScoped.orm_idString();

        LitemallGoods matchGoods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        matchGoods.setGoodsSn("CAT-MATCH-001");
        matchGoods.setName("Category Match Goods");
        matchGoods.setRetailPrice(BigDecimal.valueOf(100));
        matchGoods.setIsOnSale(true);
        matchGoods.setPicUrl("");
        matchGoods.setShareUrl("");
        matchGoods.setGallery("");
        matchGoods.setCategoryId(categoryIdMatch);
        daoProvider.daoFor(LitemallGoods.class).saveEntity(matchGoods);
        categoryMatchGoodsId = matchGoods.orm_idString();

        LitemallGoods mismatchGoods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        mismatchGoods.setGoodsSn("CAT-MISMATCH-002");
        mismatchGoods.setName("Category Mismatch Goods");
        mismatchGoods.setRetailPrice(BigDecimal.valueOf(100));
        mismatchGoods.setIsOnSale(true);
        mismatchGoods.setPicUrl("");
        mismatchGoods.setShareUrl("");
        mismatchGoods.setGallery("");
        mismatchGoods.setCategoryId("999999");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(mismatchGoods);
        categoryMismatchGoodsId = mismatchGoods.orm_idString();
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

    @Test
    public void testSelectCouponForOrder_categoryScopedMatch() {
        ApiRequest<Map<String, Object>> claimReq = ApiRequest.build(Map.of("couponId", categoryScopedCouponId));
        ApiResponse<?> claimRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon", claimReq));
        assertEquals(0, claimRes.getStatus(), "claim setup failed: " + claimRes);
        String couponUserId = (String) ((Map<String, Object>) claimRes.getData()).get("id");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "couponUserId", couponUserId,
                "goodsPrice", BigDecimal.valueOf(100),
                "goodsIds", List.of(categoryMatchGoodsId)
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCouponUser__selectCouponForOrder", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(),
                "category-scoped coupon should match when goods.categoryId is in coupon.goodsValue: " + result);
        BigDecimal discount = (BigDecimal) result.getData();
        assertEquals(0, BigDecimal.valueOf(15).compareTo(discount));
    }

    @Test
    public void testSelectCouponForOrder_categoryScopedMismatch() {
        ApiRequest<Map<String, Object>> claimReq = ApiRequest.build(Map.of("couponId", categoryScopedCouponId));
        ApiResponse<?> claimRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon", claimReq));
        assertEquals(0, claimRes.getStatus());
        String couponUserId = (String) ((Map<String, Object>) claimRes.getData()).get("id");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "couponUserId", couponUserId,
                "goodsPrice", BigDecimal.valueOf(100),
                "goodsIds", List.of(categoryMismatchGoodsId)
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCouponUser__selectCouponForOrder", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(-1, result.getStatus(),
                "category-scoped coupon should NOT match when goods.categoryId not in coupon.goodsValue");
    }

    // ===== 优惠券过期前置预警推送（deferred successor）=====

    private LitemallCouponUser seedCouponUser(String userId, int status, LocalDateTime endTime) {
        LitemallCouponUser cu = daoProvider.daoFor(LitemallCouponUser.class).newEntity();
        cu.setUserId(userId);
        cu.setCouponId(couponId);
        cu.setStatus(status);
        cu.setOrderId("");
        cu.setStartTime(LocalDateTime.now().minusDays(1));
        cu.setEndTime(endTime);
        daoProvider.daoFor(LitemallCouponUser.class).saveEntity(cu);
        return cu;
    }

    private void setSystemConfig(String key, String value) {
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName", key);
        cfg.orm_propValueByName("keyValue", value);
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
    }

    private int sendCouponExpiryReminders() {
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__sendCouponExpiryReminders",
                ApiRequest.build(new HashMap<>()));
        ApiResponse<?> r = graphQLEngine.executeRpc(ctx);
        assertEquals(0, r.getStatus(), "sendCouponExpiryReminders failed: " + r);
        return ((Number) r.getData()).intValue();
    }

    private long countCouponExpiryReminders(String userId) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_userId, userId));
        q.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_msgType,
                _AppMallDaoConstants.MSG_TYPE_MARKETING));
        q.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_title,
                LitemallCouponUserBizModel.EXPIRY_REMIND_TITLE));
        return daoProvider.daoFor(LitemallUserMessage.class).findAllByQuery(q).size();
    }

    @Test
    public void testSendCouponExpiryRemindersPushesAggregatedMessageForWindowCoupons() {
        // Default remindDays=3: multiple unused in-window coupons for one user aggregate to ONE
        // MARKETING 站内信 (covers aggregation D3 + per-user dedupe D2).
        String uid = "coupon-remind-agg";
        seedCouponUser(uid, 0, LocalDateTime.now().plusDays(2));
        seedCouponUser(uid, 0, LocalDateTime.now().plusDays(3));

        int pushed = sendCouponExpiryReminders();
        assertEquals(1, pushed, "multiple in-window coupons aggregate to one reminder per user");
        assertEquals(1, countCouponExpiryReminders(uid), "exactly one MARKETING reminder persisted");
    }

    @Test
    public void testSendCouponExpiryRemindersSkipsOutOfWindowUsedAndExpired() {
        // Out-of-window (endTime now+10, remindDays=3), used (status=1), and already-expired
        // (status=2) coupons must not trigger a reminder.
        String uid = "coupon-remind-skip";
        seedCouponUser(uid, 0, LocalDateTime.now().plusDays(10));
        seedCouponUser(uid, 1, LocalDateTime.now().plusDays(2));
        seedCouponUser(uid, 2, LocalDateTime.now().plusDays(2));

        int pushed = sendCouponExpiryReminders();
        assertEquals(0, pushed, "out-of-window/used/expired coupons must not push");
        assertEquals(0, countCouponExpiryReminders(uid));
    }

    @Test
    public void testSendCouponExpiryRemindersIsIdempotentOnReplaySameDay() {
        String uid = "coupon-remind-idem";
        seedCouponUser(uid, 0, LocalDateTime.now().plusDays(2));

        assertEquals(1, sendCouponExpiryReminders(), "first run pushes one reminder");
        // Replay same day: idempotency guard (today same-title MARKETING 站内信 exists) skips.
        assertEquals(0, sendCouponExpiryReminders(), "re-run same day must skip (idempotent)");
        assertEquals(1, countCouponExpiryReminders(uid), "still only one reminder after replay");
    }

    @Test
    public void testSendCouponExpiryRemindersRespectsEventToggle() {
        // Event toggle mall_message_event_enabled_coupon-expiry-remind=false ⇒ no push.
        setSystemConfig("mall_message_event_enabled_"
                + LitemallCouponUserBizModel.EVENT_KEY_COUPON_EXPIRY_REMIND, "false");
        String uid = "coupon-remind-toggle";
        seedCouponUser(uid, 0, LocalDateTime.now().plusDays(2));

        int pushed = sendCouponExpiryReminders();
        assertEquals(0, pushed, "event toggle off ⇒ no push");
        assertEquals(0, countCouponExpiryReminders(uid));
    }

    @Test
    public void testSendCouponExpiryRemindersUsesConfiguredRemindDays() {
        // Override remindDays to 30 ⇒ a coupon expiring in 20 days is in window.
        setSystemConfig(LitemallCouponUserBizModel.CONFIG_COUPON_EXPIRY_REMIND_DAYS, "30");
        String uid = "coupon-remind-window";
        seedCouponUser(uid, 0, LocalDateTime.now().plusDays(20));

        int pushed = sendCouponExpiryReminders();
        assertEquals(1, pushed, "remindDays=30 should include coupon expiring in 20 days");
        assertEquals(1, countCouponExpiryReminders(uid));
    }
}
