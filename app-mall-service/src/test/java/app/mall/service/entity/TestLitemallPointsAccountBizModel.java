package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallPointsAccount;
import app.mall.dao.entity.LitemallPointsFlow;
import app.mall.dao.entity.LitemallSystem;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallPointsAccountBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    private static final String USER_ID = "701";

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId(USER_ID);
        ContextProvider.getOrCreateContext().setUserName("points-test");
    }

    private void setConfig(String key, String value) {
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName", key);
        cfg.orm_propValueByName("keyValue", value);
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
    }

    private ApiResponse<?> rpc(GraphQLOperationType op, String action, Map<String, Object> data) {
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(op, action, ApiRequest.build(data));
        return graphQLEngine.executeRpc(ctx);
    }

    private int earn(String userId, int amount, String sourceType, String sourceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("amount", amount);
        data.put("changeType", _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN);
        data.put("sourceType", sourceType);
        data.put("sourceId", sourceId);
        data.put("remark", "test-earn");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallPointsAccount__earnPoints", data);
        assertEquals(0, r.getStatus(), "earnPoints failed: " + r);
        return ((Number) ((Map<?, ?>) r.getData()).get("balance")).intValue();
    }

    private int spend(String userId, int amount, String sourceType, String sourceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("amount", amount);
        data.put("changeType", _AppMallDaoConstants.POINTS_CHANGE_TYPE_SPEND);
        data.put("sourceType", sourceType);
        data.put("sourceId", sourceId);
        data.put("remark", "test-spend");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallPointsAccount__spendPoints", data);
        assertEquals(0, r.getStatus(), "spendPoints failed: " + r);
        return ((Number) ((Map<?, ?>) r.getData()).get("balance")).intValue();
    }

    private int getMyPoints() {
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallPointsAccount__getMyPoints", new HashMap<>());
        assertEquals(0, r.getStatus(), "getMyPoints failed: " + r);
        return ((Number) r.getData()).intValue();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getMyFlows(Integer changeType, String sourceType) {
        Map<String, Object> data = new HashMap<>();
        if (changeType != null) data.put("changeType", changeType);
        if (sourceType != null) data.put("sourceType", sourceType);
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallPointsFlow__getMyPointsFlows", data);
        assertEquals(0, r.getStatus(), "getMyPointsFlows failed: " + r);
        return (List<Map<String, Object>>) r.getData();
    }

    @Test
    public void testEarnAutoCreatesAccountAndFlowSnapshot() {
        int balance = earn(USER_ID, 100, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "order-1");
        assertEquals(100, balance, "first earn should auto-create account with balance 100");

        // getMyPoints (separate tx) reflects the committed balance
        assertEquals(100, getMyPoints());

        List<Map<String, Object>> flows = getMyFlows(null, null);
        assertEquals(1, flows.size());
        assertEquals(100, ((Number) flows.get(0).get("changeAmount")).intValue());
        assertEquals(100, ((Number) flows.get(0).get("balanceAfter")).intValue(),
                "flow balanceAfter must snapshot the post-change balance");
        assertEquals(_AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN, ((Number) flows.get(0).get("changeType")).intValue());
    }

    @Test
    public void testEarnAccumulatesBalanceAndTotalEarned() {
        earn(USER_ID, 100, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "acc-1");
        int balance = earn(USER_ID, 50, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "acc-2");
        assertEquals(150, balance, "second earn should accumulate");
        assertEquals(150, getMyPoints());

        // totalEarned aggregated on the account
        QueryBean q = new QueryBean();
        LitemallPointsAccount acct = (LitemallPointsAccount) daoProvider.daoFor(LitemallPointsAccount.class).findAllByQuery(q).stream()
                .findFirst().orElse(null);
        assertNotNull(acct);
        assertEquals(150, acct.getTotalEarned());
        assertEquals(0, acct.getTotalSpent());
    }

    @Test
    public void testSpendRejectsNegativeBalance() {
        earn(USER_ID, 30, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "neg-1");

        Map<String, Object> data = new HashMap<>();
        data.put("userId", USER_ID);
        data.put("amount", 100);
        data.put("changeType", _AppMallDaoConstants.POINTS_CHANGE_TYPE_SPEND);
        data.put("sourceType", LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_DEDUCT);
        data.put("sourceId", "neg-order");
        data.put("remark", "overspend");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallPointsAccount__spendPoints", data);
        assertNotEquals(0, r.getStatus(), "spend beyond balance must fail");
        // balance unchanged after failed spend
        assertEquals(30, getMyPoints());
    }

    @Test
    public void testSpendUpdatesTotalSpent() {
        earn(USER_ID, 200, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "sp-1");
        int balance = spend(USER_ID, 80, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_DEDUCT, "sp-order-1");
        assertEquals(120, balance);
        assertEquals(120, getMyPoints());

        QueryBean q = new QueryBean();
        LitemallPointsAccount acct = (LitemallPointsAccount) daoProvider.daoFor(LitemallPointsAccount.class).findAllByQuery(q).stream()
                .findFirst().orElse(null);
        assertNotNull(acct);
        assertEquals(200, acct.getTotalEarned());
        assertEquals(80, acct.getTotalSpent());
    }

    @Test
    public void testEarnIdempotentRejectsDuplicateSource() {
        earn(USER_ID, 100, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "dup-1");

        Map<String, Object> data = new HashMap<>();
        data.put("userId", USER_ID);
        data.put("amount", 100);
        data.put("changeType", _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN);
        data.put("sourceType", LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN);
        data.put("sourceId", "dup-1");
        data.put("remark", "duplicate");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallPointsAccount__earnPoints", data);
        assertNotEquals(0, r.getStatus(), "duplicate earn (same sourceType+sourceId) must be rejected");
        assertEquals(100, getMyPoints(), "balance must not change after rejected duplicate");
    }

    @Test
    public void testAdminAdjustAddsAndDeducts() {
        earn(USER_ID, 100, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "adj-seed");

        Map<String, Object> addData = new HashMap<>();
        addData.put("userId", USER_ID);
        addData.put("amount", 50);
        addData.put("remark", "manual add");
        ApiResponse<?> addR = rpc(GraphQLOperationType.mutation, "LitemallPointsAccount__adjustPoints", addData);
        assertEquals(0, addR.getStatus(), "adjustPoints add failed: " + addR);
        assertEquals(150, getMyPoints());

        Map<String, Object> subData = new HashMap<>();
        subData.put("userId", USER_ID);
        subData.put("amount", -30);
        subData.put("remark", "manual deduct");
        ApiResponse<?> subR = rpc(GraphQLOperationType.mutation, "LitemallPointsAccount__adjustPoints", subData);
        assertEquals(0, subR.getStatus(), "adjustPoints deduct failed: " + subR);
        assertEquals(120, getMyPoints());

        // admin-adjust flows recorded with distinct sourceIds
        List<Map<String, Object>> adjustFlows = getMyFlows(null, LitemallPointsAccountBizModel.SOURCE_TYPE_ADMIN_ADJUST);
        assertEquals(2, adjustFlows.size(), "two admin adjusts recorded");
    }

    @Test
    public void testFlowsFilterByChangeType() {
        earn(USER_ID, 200, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "filt-1");
        spend(USER_ID, 50, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_DEDUCT, "filt-order-1");

        List<Map<String, Object>> earnFlows = getMyFlows(_AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN, null);
        assertTrue(earnFlows.stream().allMatch(f -> ((Number) f.get("changeType")).intValue() == _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN));
        List<Map<String, Object>> spendFlows = getMyFlows(_AppMallDaoConstants.POINTS_CHANGE_TYPE_SPEND, null);
        assertTrue(spendFlows.stream().allMatch(f -> ((Number) f.get("changeType")).intValue() == _AppMallDaoConstants.POINTS_CHANGE_TYPE_SPEND));
    }
}
