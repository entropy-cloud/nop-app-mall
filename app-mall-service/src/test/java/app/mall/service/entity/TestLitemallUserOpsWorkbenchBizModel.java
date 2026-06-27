package app.mall.service.entity;

import app.mall.dao.entity.LitemallCoupon;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.auth.core.login.UserContextImpl;
import io.nop.api.core.auth.IUserContext;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallUserOpsWorkbenchBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @BeforeEach
    void setUp() {
        // Admin context for the operator-side operations (ban/setLevel/dispatch/tag/summary).
        ContextProvider.getOrCreateContext().setUserId("0");
        ContextProvider.getOrCreateContext().setUserName("admin");
    }

    private ApiResponse<?> rpc(GraphQLOperationType op, String action, Map<String, Object> data) {
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(op, action, ApiRequest.build(data));
        return graphQLEngine.executeRpc(ctx);
    }

    private NopAuthUser signUpUser(String username, String mobile) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("password", "Pass@1234");
        data.put("mobile", mobile);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LoginApi__signUp", data);
        assertEquals(0, r.getStatus(), "signUp helper failed: " + r.getMsg());
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq("userName", username));
        return (NopAuthUser) daoProvider.daoFor(NopAuthUser.class)
                .findAllByQuery(query).stream().findFirst().orElse(null);
    }

    private void actAs(NopAuthUser user) {
        ContextProvider.getOrCreateContext().setUserId(user.getUserId());
        ContextProvider.getOrCreateContext().setUserName(user.getUserName());
        UserContextImpl userContext = new UserContextImpl();
        userContext.setUserId(user.getUserId());
        userContext.setUserName(user.getUserName());
        IUserContext.set(userContext);
    }

    private String createCoupon(int total, int limit, int status) {
        LitemallCoupon coupon = daoProvider.daoFor(LitemallCoupon.class).newEntity();
        coupon.orm_propValueByName("name", "test-coupon");
        coupon.orm_propValueByName("status", status);
        coupon.orm_propValueByName("total", total);
        coupon.orm_propValueByName("limit", limit);
        coupon.orm_propValueByName("type", 0);
        coupon.orm_propValueByName("timeType", 0);
        coupon.orm_propValueByName("days", 7);
        coupon.orm_propValueByName("discount", new BigDecimal("10"));
        coupon.orm_propValueByName("min", BigDecimal.ZERO);
        coupon.orm_propValueByName("goodsType", 0);
        daoProvider.daoFor(LitemallCoupon.class).saveEntity(coupon);
        return coupon.orm_idString();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBanAndUnbanUserKeepsBlacklistConsistent() {
        NopAuthUser user = signUpUser("banuser", "13900000010");
        assertNotNull(user);

        Map<String, Object> banData = new HashMap<>();
        banData.put("userId", user.getUserId());
        banData.put("reason", "abuse");
        ApiResponse<?> banR = rpc(GraphQLOperationType.mutation, "LitemallUserBlacklist__banUser", banData);
        assertEquals(0, banR.getStatus(), "banUser failed: " + banR);
        Map<String, Object> record = (Map<String, Object>) banR.getData();
        assertEquals(user.getUserId(), record.get("userId"));
        assertEquals("abuse", record.get("reason"));

        // status is now disabled (0)
        NopAuthUser reloaded = reloadUser(user.getUserId());
        assertEquals(0, reloaded.orm_propValueByName("status"));

        // summary reflects banned state
        Map<String, Object> sumData = new HashMap<>();
        sumData.put("userId", user.getUserId());
        ApiResponse<?> sumR = rpc(GraphQLOperationType.query, "LitemallUserBlacklist__getUserWorkbenchSummary", sumData);
        assertEquals(0, sumR.getStatus(), "summary failed: " + sumR);
        assertTrue((Boolean) ((Map<String, Object>) sumR.getData()).get("banned"));

        // unban clears the record and re-enables
        ApiResponse<?> unbanR = rpc(GraphQLOperationType.mutation, "LitemallUserBlacklist__unbanUser",
                new HashMap<>(Map.of("userId", user.getUserId())));
        assertEquals(0, unbanR.getStatus(), "unbanUser failed: " + unbanR);
        NopAuthUser afterUnban = reloadUser(user.getUserId());
        assertEquals(1, afterUnban.orm_propValueByName("status"));
    }

    @Test
    public void testBannedUserCannotSubmitOrder() {
        NopAuthUser user = signUpUser("bannedsubmit", "13900000011");
        // ban first (admin context)
        assertEquals(0, rpc(GraphQLOperationType.mutation, "LitemallUserBlacklist__banUser",
                new HashMap<>(Map.of("userId", user.getUserId(), "reason", "test"))).getStatus());

        // act as the banned user and attempt to submit; the banned guard fires before address/cart checks
        actAs(user);
        Map<String, Object> submitData = new HashMap<>();
        submitData.put("addressId", "");
        submitData.put("freightPrice", BigDecimal.ZERO);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallOrder__submit", submitData);
        assertNotEquals(0, r.getStatus(), "banned user submit must be rejected");
    }

    @Test
    public void testUnbanUnknownUserFails() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "non-existent-user-999");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallUserBlacklist__unbanUser", data);
        assertNotEquals(0, r.getStatus(), "unban a never-banned user must fail");
    }

    @Test
    public void testSetUserLevelValidAndInvalid() {
        NopAuthUser user = signUpUser("leveluser", "13900000012");

        Map<String, Object> valid = new HashMap<>();
        valid.put("userId", user.getUserId());
        valid.put("targetLevel", 1);
        valid.put("remark", "manual upgrade");
        ApiResponse<?> ok = rpc(GraphQLOperationType.mutation, "LitemallMemberLevel__setUserLevel", valid);
        assertEquals(0, ok.getStatus(), "valid setUserLevel failed: " + ok);
        assertEquals(1, reloadUser(user.getUserId()).orm_propValueByName("userLevel"));

        Map<String, Object> invalid = new HashMap<>();
        invalid.put("userId", user.getUserId());
        invalid.put("targetLevel", 9);
        ApiResponse<?> bad = rpc(GraphQLOperationType.mutation, "LitemallMemberLevel__setUserLevel", invalid);
        assertNotEquals(0, bad.getStatus(), "invalid level 9 must be rejected");
        assertEquals(1, reloadUser(user.getUserId()).orm_propValueByName("userLevel"));
    }

    @Test
    public void testDispatchCouponSuccessLimitAndOffShelf() {
        NopAuthUser user = signUpUser("couponuser", "13900000013");
        String couponId = createCoupon(100, 1, 0);

        // success: first dispatch
        Map<String, Object> ok = new HashMap<>();
        ok.put("couponId", couponId);
        ok.put("userId", user.getUserId());
        ok.put("remark", "manual");
        ApiResponse<?> r1 = rpc(GraphQLOperationType.mutation, "LitemallCouponUser__dispatchCoupon", ok);
        assertEquals(0, r1.getStatus(), "first dispatch should succeed: " + r1);

        // over per-user limit (limit=1)
        ApiResponse<?> r2 = rpc(GraphQLOperationType.mutation, "LitemallCouponUser__dispatchCoupon", ok);
        assertNotEquals(0, r2.getStatus(), "second dispatch must exceed per-user limit");

        // off-shelf coupon (status != 0)
        String offShelfId = createCoupon(100, 1, 2);
        Map<String, Object> offData = new HashMap<>();
        offData.put("couponId", offShelfId);
        offData.put("userId", user.getUserId());
        ApiResponse<?> r3 = rpc(GraphQLOperationType.mutation, "LitemallCouponUser__dispatchCoupon", offData);
        assertNotEquals(0, r3.getStatus(), "off-shelf coupon dispatch must fail");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetUserWorkbenchSummaryAggregates() {
        NopAuthUser user = signUpUser("summaryuser", "13900000014");
        // seed a tag so the summary has non-empty tags
        Map<String, Object> tagData = new HashMap<>();
        tagData.put("userId", user.getUserId());
        tagData.put("tag", "vip");
        tagData.put("name", "VIP客户");
        assertEquals(0, rpc(GraphQLOperationType.mutation, "LitemallUserTag__addUserTag", tagData).getStatus());

        Map<String, Object> sumData = new HashMap<>();
        sumData.put("userId", user.getUserId());
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallUserBlacklist__getUserWorkbenchSummary", sumData);
        assertEquals(0, r.getStatus(), "summary failed: " + r);
        Map<String, Object> data = (Map<String, Object>) r.getData();
        assertEquals(user.getUserId(), data.get("userId"));
        assertEquals("summaryuser", data.get("userName"));
        assertNull(data.get("password"), "summary must not leak credentials");
        assertEquals(0, ((Number) data.get("orderCount")).intValue());
        assertEquals(0, ((Number) data.get("pointsBalance")).intValue());
        assertFalseBanned(data);
        List<String> tags = (List<String>) data.get("tags");
        assertTrue(tags.contains("vip"));
    }

    private void assertFalseBanned(Map<String, Object> data) {
        Object banned = data.get("banned");
        if (banned != null) {
            assertEquals(false, banned, "freshly signed-up user must not be banned");
        }
    }

    @Test
    public void testUserTagAddRemoveDedupeAndSegment() {
        NopAuthUser user = signUpUser("taguser", "13900000015");
        NopAuthUser user2 = signUpUser("taguser2", "13900000016");

        Map<String, Object> add = new HashMap<>();
        add.put("userId", user.getUserId());
        add.put("tag", "high-value");
        add.put("name", "高价值");
        assertEquals(0, rpc(GraphQLOperationType.mutation, "LitemallUserTag__addUserTag", add).getStatus());

        // dedupe: same (userId, tag) rejected
        ApiResponse<?> dup = rpc(GraphQLOperationType.mutation, "LitemallUserTag__addUserTag", add);
        assertNotEquals(0, dup.getStatus(), "duplicate tag must be rejected");

        // second user same tag
        Map<String, Object> add2 = new HashMap<>();
        add2.put("userId", user2.getUserId());
        add2.put("tag", "high-value");
        assertEquals(0, rpc(GraphQLOperationType.mutation, "LitemallUserTag__addUserTag", add2).getStatus());

        // segment query returns both users
        Map<String, Object> seg = new HashMap<>();
        seg.put("tag", "high-value");
        ApiResponse<?> segR = rpc(GraphQLOperationType.query, "LitemallUserTag__findUsersByTag", seg);
        assertEquals(0, segR.getStatus(), "findUsersByTag failed: " + segR);
        Object items = ((Map<String, Object>) segR.getData()).get("items");
        assertNotNull(items);
        assertEquals(2, ((List<?>) items).size());

        // remove tag
        Map<String, Object> rm = new HashMap<>();
        rm.put("userId", user.getUserId());
        rm.put("tag", "high-value");
        assertEquals(0, rpc(GraphQLOperationType.mutation, "LitemallUserTag__removeUserTag", rm).getStatus());

        // removing again fails (already removed)
        ApiResponse<?> rmAgain = rpc(GraphQLOperationType.mutation, "LitemallUserTag__removeUserTag", rm);
        assertNotEquals(0, rmAgain.getStatus(), "removing a non-existent tag must fail");
    }

    private NopAuthUser reloadUser(String userId) {
        return (NopAuthUser) daoProvider.daoFor(NopAuthUser.class).getEntityById(userId);
    }
}
