package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallMemberLevel;
import app.mall.dao.entity.LitemallOrder;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.auth.dao.entity.NopAuthUser;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallMemberLevelBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserName("test");
    }

    private LitemallMemberLevel createRule(int level, String name, BigDecimal upgrade, BigDecimal downgrade) {
        LitemallMemberLevel rule = daoProvider.daoFor(LitemallMemberLevel.class).newEntity();
        rule.setLevel(level);
        rule.setName(name);
        rule.setUpgradeThreshold(upgrade);
        rule.setDowngradeThreshold(downgrade);
        rule.setSortOrder(level);
        daoProvider.daoFor(LitemallMemberLevel.class).saveEntity(rule);
        return rule;
    }

    private NopAuthUser signUpAndSetLevel(String username, int level) {
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
        NopAuthUser user = (NopAuthUser) daoProvider.daoFor(NopAuthUser.class).findAllByQuery(q).stream()
                .findFirst().orElse(null);
        assertNotNull(user, "signed-up user must exist");
        // Set userLevel via a committed GraphQL update so subsequent RPCs see the new level.
        // (Test-body edits to loaded entities are not visible to later RPCs which run in their own
        // committed transactions.)
        setUserLevel(user.getUserId(), level);
        return user;
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

    private int readUserLevel(String userId) {
        NopAuthUser user = daoProvider.daoFor(NopAuthUser.class).getEntityById(userId);
        Object value = user.orm_propValueByName("userLevel");
        return value instanceof Integer ? (Integer) value : 0;
    }

    private void createPaidOrder(String userId, String sn, BigDecimal actualPrice) {
        LitemallOrder o = daoProvider.daoFor(LitemallOrder.class).newEntity();
        o.setUserId(userId);
        o.setOrderSn(sn);
        o.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_PAY);
        o.setConsignee("收货人");
        o.setMobile("13800138000");
        o.setAddress("测试地址");
        o.setMessage("spending-order");
        o.setGoodsPrice(actualPrice);
        o.setFreightPrice(BigDecimal.ZERO);
        o.setCouponPrice(BigDecimal.ZERO);
        o.setIntegralPrice(BigDecimal.ZERO);
        o.setGrouponPrice(BigDecimal.ZERO);
        o.setOrderPrice(actualPrice);
        o.setActualPrice(actualPrice);
        o.setPromotionPrice(BigDecimal.ZERO);
        o.setPinTuanPrice(BigDecimal.ZERO);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(o);
    }

    private int evaluateUserLevel(String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallMemberLevel__evaluateUserLevel", ApiRequest.build(data));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "evaluateUserLevel failed: " + result.getMsg());
        return ((Number) result.getData()).intValue();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMyLevelProgress() {
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallMemberLevel__getMyLevelProgress", ApiRequest.build(Map.of()));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getMyLevelProgress failed: " + result.getMsg());
        return (Map<String, Object>) result.getData();
    }

    private int downgradeExpiredLevels(int periodDays) {
        Map<String, Object> data = new HashMap<>();
        data.put("periodDays", periodDays);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallMemberLevel__downgradeExpiredLevels", ApiRequest.build(data));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "downgradeExpiredLevels failed: " + result.getMsg());
        return ((Number) result.getData()).intValue();
    }

    @Test
    public void testEvaluateUpgradeToVip() {
        createRule(1, "VIP", new BigDecimal("100"), new BigDecimal("50"));
        createRule(2, "高级VIP", new BigDecimal("500"), new BigDecimal("300"));

        NopAuthUser user = signUpAndSetLevel("vipup001", 0);
        createPaidOrder(user.getUserId(), "SN-UP-1", new BigDecimal("150"));

        ContextProvider.getOrCreateContext().setUserId(user.getUserId());
        int level = evaluateUserLevel(user.getUserId());
        assertEquals(1, level, "spending 150 should upgrade to VIP(1)");
        assertEquals(1, readUserLevel(user.getUserId()), "userLevel should be persisted as 1");
    }

    @Test
    public void testEvaluateTopLevel() {
        createRule(1, "VIP", new BigDecimal("100"), new BigDecimal("50"));
        createRule(2, "高级VIP", new BigDecimal("500"), new BigDecimal("300"));

        NopAuthUser user = signUpAndSetLevel("vipup002", 0);
        createPaidOrder(user.getUserId(), "SN-UP-2a", new BigDecimal("300"));
        createPaidOrder(user.getUserId(), "SN-UP-2b", new BigDecimal("350"));

        ContextProvider.getOrCreateContext().setUserId(user.getUserId());
        int level = evaluateUserLevel(user.getUserId());
        assertEquals(2, level, "spending 650 should reach top level(2)");
    }

    @Test
    public void testEvaluateNoRulesKeepsZero() {
        NopAuthUser user = signUpAndSetLevel("vipup003", 0);
        createPaidOrder(user.getUserId(), "SN-UP-3", new BigDecimal("9999"));

        ContextProvider.getOrCreateContext().setUserId(user.getUserId());
        int level = evaluateUserLevel(user.getUserId());
        assertEquals(0, level, "no rules configured keeps user at level 0");
    }

    @Test
    public void testGetMyLevelProgress() {
        createRule(1, "VIP", new BigDecimal("100"), new BigDecimal("50"));
        createRule(2, "高级VIP", new BigDecimal("500"), new BigDecimal("300"));

        NopAuthUser user = signUpAndSetLevel("vipprog001", 1);
        createPaidOrder(user.getUserId(), "SN-PR-1", new BigDecimal("200"));

        ContextProvider.getOrCreateContext().setUserId(user.getUserId());
        Map<String, Object> progress = getMyLevelProgress();

        assertEquals(1, ((Number) progress.get("currentLevel")).intValue());
        assertEquals("VIP", progress.get("currentLevelName"));
        assertEquals(2, ((Number) progress.get("nextLevel")).intValue());
        assertEquals(0, ((BigDecimal) progress.get("nextLevelThreshold")).compareTo(new BigDecimal("500")));
        assertEquals(0, ((BigDecimal) progress.get("remaining")).compareTo(new BigDecimal("300")));
    }

    @Test
    public void testDowngradeExpiredLevels() {
        createRule(1, "VIP", new BigDecimal("100"), new BigDecimal("500"));

        NopAuthUser user = signUpAndSetLevel("vipdown001", 1);
        // No paid orders in the evaluation period -> period spending 0 < downgradeThreshold 500.
        ContextProvider.getOrCreateContext().setUserId(user.getUserId());
        assertEquals(1, readUserLevel(user.getUserId()), "precondition: user is VIP(1)");

        int downgraded = downgradeExpiredLevels(365);
        assertEquals(1, downgraded, "one member should be downgraded");
        assertEquals(0, readUserLevel(user.getUserId()), "VIP not meeting threshold should drop to 0");
    }

    @Test
    public void testDowngradeNotTriggeredWhenThresholdMet() {
        createRule(1, "VIP", new BigDecimal("100"), new BigDecimal("500"));

        NopAuthUser user = signUpAndSetLevel("vipdown002", 1);
        createPaidOrder(user.getUserId(), "SN-DN-1", new BigDecimal("600"));

        ContextProvider.getOrCreateContext().setUserId(user.getUserId());
        int downgraded = downgradeExpiredLevels(365);
        assertEquals(0, downgraded, "member meeting threshold should not be downgraded");
        assertEquals(1, readUserLevel(user.getUserId()), "userLevel stays at 1");
    }
}
