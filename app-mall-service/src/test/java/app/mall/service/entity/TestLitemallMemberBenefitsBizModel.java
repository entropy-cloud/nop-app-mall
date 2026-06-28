package app.mall.service.entity;

import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallMemberLevelBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallCoupon;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallMemberLevel;
import app.mall.dao.entity.LitemallSystem;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.core.context.ServiceContextImpl;
import io.nop.dao.api.IDaoProvider;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import io.nop.orm.IOrmTemplate;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallMemberBenefitsBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    ILitemallMemberLevelBiz memberLevelBiz;

    @Inject
    ILitemallCouponUserBiz couponUserBiz;

    @Inject
    IOrmTemplate ormTemplate;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserName("benefits-test");
    }

    private NopAuthUser signUp(String username) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "username", username,
                "password", "Pass@1234",
                "mobile", "139" + username.substring(username.length() - 4)
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__signUp", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "signUp helper failed: " + result.getMsg());

        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq("userName", username));
        return (NopAuthUser) daoProvider.daoFor(NopAuthUser.class).findAllByQuery(q).stream()
                .findFirst().orElse(null);
    }

    private void setUserLevel(String userId, int level) {
        Map<String, Object> entity = new HashMap<>();
        entity.put("id", userId);
        entity.put("userLevel", level);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "NopAuthUser__update", ApiRequest.build(Map.of("data", entity)));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "NopAuthUser__update userLevel failed: " + result.getMsg());
    }

    private void setUserBirthday(String userId, LocalDate birthday) {
        // Run in a new session so the MANAGED entity change is flushed and committed, making it
        // visible to subsequent RPCs which run in their own transactions.
        ormTemplate.runInNewSession(session -> {
            NopAuthUser user = (NopAuthUser) session.get(NopAuthUser.class.getName(), userId);
            if (user != null) {
                user.setBirthday(birthday);
            }
            session.flush();
            return null;
        });
    }

    private String createCoupon(int minMemberLevel, int limit) {
        LitemallCoupon coupon = daoProvider.daoFor(LitemallCoupon.class).newEntity();
        coupon.setName("会员专属券-L" + minMemberLevel);
        coupon.setTag("会员专属");
        coupon.setTotal(100);
        coupon.setDiscount(BigDecimal.valueOf(50));
        coupon.setMin(BigDecimal.ZERO);
        coupon.setLimit(limit);
        coupon.setType(0);
        coupon.setStatus(0);
        coupon.setGoodsType(0);
        coupon.setGoodsValue("");
        coupon.setCode("");
        coupon.setTimeType(0);
        coupon.setDays(30);
        coupon.setMinMemberLevel(minMemberLevel);
        daoProvider.daoFor(LitemallCoupon.class).saveEntity(coupon);
        return coupon.orm_idString();
    }

    private void setConfig(String key, String value) {
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName", key);
        cfg.orm_propValueByName("keyValue", value);
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
    }

    private long countUserCoupons(String userId, String couponId) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_userId, userId));
        q.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_couponId, couponId));
        return couponUserBiz.findCount(q, new ServiceContextImpl());
    }

    private ApiResponse<?> rpc(GraphQLOperationType op, String action, Map<String, Object> data) {
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(op, action, ApiRequest.build(data));
        return graphQLEngine.executeRpc(ctx);
    }

    @Test
    public void testClaimMemberExclusiveCouponLevelInsufficient() {
        NopAuthUser user = signUp("ben-user-1");
        assertNotNull(user);
        // User defaults to level 0 (ordinary). Coupon requires level 1.
        String couponId = createCoupon(1, 1);

        ContextProvider.getOrCreateContext().setUserId(user.getUserId());
        ApiResponse<?> result = rpc(GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon",
                Map.of("couponId", couponId));
        assertEquals(-1, result.getStatus(), "claim should fail for insufficient member level");
    }

    @Test
    public void testClaimMemberExclusiveCouponLevelOK() {
        NopAuthUser user = signUp("ben-user-2");
        assertNotNull(user);
        setUserLevel(user.getUserId(), 1);

        String couponId = createCoupon(1, 1);
        ContextProvider.getOrCreateContext().setUserId(user.getUserId());
        ApiResponse<?> result = rpc(GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon",
                Map.of("couponId", couponId));
        assertEquals(0, result.getStatus(), "claim should succeed at level 1: " + result);
    }

    @Test
    public void testClaimCouponNoMemberLevelRestriction() {
        NopAuthUser user = signUp("ben-user-3");
        assertNotNull(user);
        // minMemberLevel=0 means all users can claim (backward compatible).
        String couponId = createCoupon(0, 1);
        ContextProvider.getOrCreateContext().setUserId(user.getUserId());
        ApiResponse<?> result = rpc(GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon",
                Map.of("couponId", couponId));
        assertEquals(0, result.getStatus(), "claim should succeed with no level restriction: " + result);
    }

    @Test
    public void testLevelUpBenefitDispatch() {
        NopAuthUser user = signUp("ben-user-4");
        assertNotNull(user);

        String benefitCouponId = createCoupon(0, 1);
        setConfig(LitemallMemberLevelBizModel.CONFIG_BENEFIT_LEVEL_UP_ENABLED, "true");
        setConfig(LitemallMemberLevelBizModel.CONFIG_BENEFIT_LEVEL_COUPON_PREFIX + "1", benefitCouponId);

        // Upgrade user to level 1 via admin setUserLevel → should dispatch benefit coupon.
        ContextProvider.getOrCreateContext().setUserId("0");
        ApiResponse<?> result = rpc(GraphQLOperationType.mutation, "LitemallMemberLevel__setUserLevel",
                Map.of("userId", user.getUserId(), "targetLevel", 1));
        assertEquals(0, result.getStatus(), "setUserLevel failed: " + result);

        // Verify the benefit coupon was dispatched.
        assertEquals(1L, countUserCoupons(user.getUserId(), benefitCouponId),
                "level-up benefit coupon should be dispatched");
    }

    @Test
    public void testLevelUpBenefitDispatchDisabled() {
        NopAuthUser user = signUp("ben-user-5");
        assertNotNull(user);

        String benefitCouponId = createCoupon(0, 1);
        setConfig(LitemallMemberLevelBizModel.CONFIG_BENEFIT_LEVEL_UP_ENABLED, "false");
        setConfig(LitemallMemberLevelBizModel.CONFIG_BENEFIT_LEVEL_COUPON_PREFIX + "1", benefitCouponId);

        ContextProvider.getOrCreateContext().setUserId("0");
        rpc(GraphQLOperationType.mutation, "LitemallMemberLevel__setUserLevel",
                Map.of("userId", user.getUserId(), "targetLevel", 1));

        assertEquals(0L, countUserCoupons(user.getUserId(), benefitCouponId),
                "benefit coupon should NOT be dispatched when disabled");
    }

    @Test
    public void testBirthdayDispatch() {
        NopAuthUser user = signUp("ben-user-6");
        assertNotNull(user);
        // Set birthday to today so the scheduler picks it up.
        setUserBirthday(user.getUserId(), LocalDate.now());

        String birthdayCouponId = createCoupon(0, 0);
        setConfig(LitemallMemberLevelBizModel.CONFIG_BENEFIT_BIRTHDAY_ENABLED, "true");
        setConfig(LitemallMemberLevelBizModel.CONFIG_BENEFIT_BIRTHDAY_COUPON, birthdayCouponId);

        int count = memberLevelBiz.dispatchBirthdayCoupons(new ServiceContextImpl());
        assertTrue(count >= 1, "at least one birthday coupon should be dispatched");
        assertEquals(1L, countUserCoupons(user.getUserId(), birthdayCouponId),
                "birthday coupon should be dispatched to user");

        // Idempotency: second run same year should NOT dispatch again.
        int count2 = memberLevelBiz.dispatchBirthdayCoupons(new ServiceContextImpl());
        assertEquals(1L, countUserCoupons(user.getUserId(), birthdayCouponId),
                "birthday coupon should not be dispatched twice in the same year");
    }

    @Test
    public void testBirthdayDispatchNoConfig() {
        NopAuthUser user = signUp("ben-user-7");
        assertNotNull(user);
        setUserBirthday(user.getUserId(), LocalDate.now());

        // No birthday coupon configured → dispatch returns 0.
        setConfig(LitemallMemberLevelBizModel.CONFIG_BENEFIT_BIRTHDAY_ENABLED, "true");
        int count = memberLevelBiz.dispatchBirthdayCoupons(new ServiceContextImpl());
        assertEquals(0, count, "no dispatch without configured birthday coupon");
    }
}
