package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallPointsAccount;
import app.mall.dao.entity.LitemallPointsExpireBatch;
import app.mall.dao.entity.LitemallPointsFlow;
import app.mall.dao.entity.LitemallSystem;
import app.mall.dao.entity.LitemallUserMessage;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static app.mall.service.AppMallErrors.ERR_POINTS_DUPLICATE_EARN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    private int adjust(String userId, int amount) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("amount", amount);
        data.put("remark", "test-adjust");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallPointsAccount__adjustPoints", data);
        assertEquals(0, r.getStatus(), "adjustPoints failed: " + r);
        return ((Number) ((Map<?, ?>) r.getData()).get("balance")).intValue();
    }

    private int expirePoints() {
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallPointsAccount__expirePoints", new HashMap<>());
        assertEquals(0, r.getStatus(), "expirePoints failed: " + r);
        return ((Number) r.getData()).intValue();
    }

    private int sendPointsExpiryReminders() {
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallPointsAccount__sendPointsExpiryReminders", new HashMap<>());
        assertEquals(0, r.getStatus(), "sendPointsExpiryReminders failed: " + r);
        return ((Number) r.getData()).intValue();
    }

    private long countExpiryReminders(String userId) {
        QueryBean q = new QueryBean();
        q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallUserMessage.PROP_NAME_userId, userId));
        q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallUserMessage.PROP_NAME_msgType,
                _AppMallDaoConstants.MSG_TYPE_SYSTEM));
        q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallUserMessage.PROP_NAME_title,
                LitemallPointsAccountBizModel.EXPIRY_REMIND_TITLE));
        return daoProvider.daoFor(LitemallUserMessage.class).findAllByQuery(q).size();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getExpiryHint() {
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallPointsAccount__getMyPointsExpiryHint", new HashMap<>());
        assertEquals(0, r.getStatus(), "getMyPointsExpiryHint failed: " + r);
        return (Map<String, Object>) r.getData();
    }

    private LitemallPointsAccount seedAccount(String userId, int balance) {
        LitemallPointsAccount acct = daoProvider.daoFor(LitemallPointsAccount.class).newEntity();
        acct.orm_propValueByName(LitemallPointsAccount.PROP_NAME_userId, userId);
        acct.orm_propValueByName(LitemallPointsAccount.PROP_NAME_balance, balance);
        acct.orm_propValueByName(LitemallPointsAccount.PROP_NAME_totalEarned, balance);
        acct.orm_propValueByName(LitemallPointsAccount.PROP_NAME_totalSpent, 0);
        acct.orm_propValueByName(LitemallPointsAccount.PROP_NAME_version, 0);
        daoProvider.daoFor(LitemallPointsAccount.class).saveEntity(acct);
        return acct;
    }

    private LitemallPointsExpireBatch seedBatch(String accountId, String userId, int total, int remaining,
                                                LocalDateTime expireTime, String sourceId) {
        LitemallPointsExpireBatch b = daoProvider.daoFor(LitemallPointsExpireBatch.class).newEntity();
        b.orm_propValueByName(LitemallPointsExpireBatch.PROP_NAME_accountId, accountId);
        b.orm_propValueByName(LitemallPointsExpireBatch.PROP_NAME_userId, userId);
        b.orm_propValueByName(LitemallPointsExpireBatch.PROP_NAME_totalPoints, total);
        b.orm_propValueByName(LitemallPointsExpireBatch.PROP_NAME_remainingPoints, remaining);
        b.orm_propValueByName(LitemallPointsExpireBatch.PROP_NAME_expireTime, expireTime);
        b.orm_propValueByName(LitemallPointsExpireBatch.PROP_NAME_sourceType, "order-confirm-earn");
        b.orm_propValueByName(LitemallPointsExpireBatch.PROP_NAME_sourceId, sourceId);
        b.orm_propValueByName(LitemallPointsExpireBatch.PROP_NAME_version, 0);
        daoProvider.daoFor(LitemallPointsExpireBatch.class).saveEntity(b);
        return b;
    }

    private List<LitemallPointsExpireBatch> findBatches(String userId) {
        QueryBean q = new QueryBean();
        q.addOrderField(LitemallPointsExpireBatch.PROP_NAME_expireTime, false);
        if (userId != null) {
            q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallPointsExpireBatch.PROP_NAME_userId, userId));
        }
        return daoProvider.daoFor(LitemallPointsExpireBatch.class).findAllByQuery(q);
    }

    private String accountIdOf(String userId) {
        QueryBean q = new QueryBean();
        q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallPointsAccount.PROP_NAME_userId, userId));
        return daoProvider.daoFor(LitemallPointsAccount.class).findFirstByQuery(q).orm_idString();
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
        assertEquals(ERR_POINTS_DUPLICATE_EARN.getErrorCode(), r.getCode(),
                "duplicate earn must surface ERR_POINTS_DUPLICATE_EARN (consistent across app-level pre-check "
                        + "and DB unique-key fallback uk_litemall_points_flow_source): " + r.getCode());
        assertEquals(100, getMyPoints(), "balance must not change after rejected duplicate");
    }

    @Test
    public void testEarnDuplicateRejectedAfterDirectFlowInsert() {
        // Insert a flow directly via dao (bypassing earnPoints), then call earnPoints with the same
        // (sourceType, sourceId). Verifies the app-level countFlowBySource pre-check sees the seeded
        // flow and rejects with ERR_POINTS_DUPLICATE_EARN — same code the concurrent-race DB fallback
        // produces via isPointsFlowUniqueKeyConflict translation.
        earn(USER_ID, 50, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "seed-1");

        Map<String, Object> data = new HashMap<>();
        data.put("userId", USER_ID);
        data.put("amount", 30);
        data.put("changeType", _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN);
        data.put("sourceType", LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN);
        data.put("sourceId", "seed-1");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallPointsAccount__earnPoints", data);
        assertNotEquals(0, r.getStatus(), "duplicate earn after seed insert must be rejected");
        assertEquals(ERR_POINTS_DUPLICATE_EARN.getErrorCode(), r.getCode(),
                "error code must be ERR_POINTS_DUPLICATE_EARN regardless of which layer detected the conflict: "
                        + r.getCode());
        assertEquals(50, getMyPoints(), "balance must not change after rejected duplicate");
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

    // ===== 积分有效期与自动过期（successor）=====

    @Test
    public void testEarnCreatesExpireBatchWithExpireTime() {
        setConfig(LitemallPointsAccountBizModel.CONFIG_POINTS_VALIDITY_DAYS, "10");

        earn(USER_ID, 100, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "eb-1");

        List<LitemallPointsExpireBatch> batches = findBatches(USER_ID);
        assertEquals(1, batches.size(), "one earn must produce one expire batch");
        LitemallPointsExpireBatch batch = batches.get(0);
        assertEquals(100, batch.getTotalPoints());
        assertEquals(100, batch.getRemainingPoints());
        assertEquals("order-confirm-earn", batch.getSourceType());
        assertEquals("eb-1", batch.getSourceId());
        assertEquals(accountIdOf(USER_ID), batch.getAccountId(), "batch accountId must reference the user's points account");

        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(now.toLocalDate(), batch.getExpireTime().toLocalDate());
        assertTrue(days >= 9 && days <= 11, "expireTime should be ~10 days out, got " + days);
    }

    @Test
    public void testPositiveAdjustAlsoCreatesExpirableBatch() {
        earn(USER_ID, 50, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "adj-batch-seed");
        adjust(USER_ID, 30);

        List<LitemallPointsExpireBatch> batches = findBatches(USER_ID);
        assertEquals(2, batches.size(), "earn + positive adjust must each create a batch");
        int sumRemaining = batches.stream().mapToInt(b -> b.getRemainingPoints()).sum();
        assertEquals(80, sumRemaining, "sum(remainingPoints) must equal total earned (50 + 30)");
    }

    @Test
    public void testSpendConsumesBatchesFifoEarliestExpireFirst() {
        String accountId = seedAccount(USER_ID, 250).orm_idString();
        // batch A expires sooner than batch B → FIFO must drain A before B
        seedBatch(accountId, USER_ID, 100, 100, LocalDateTime.now().plusDays(10), "fifo-a");
        seedBatch(accountId, USER_ID, 100, 100, LocalDateTime.now().plusDays(20), "fifo-b");

        int balance = spend(USER_ID, 150, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_DEDUCT, "fifo-order");
        assertEquals(100, balance, "balance = 250 - 150");

        List<LitemallPointsExpireBatch> batches = findBatches(USER_ID);
        LitemallPointsExpireBatch a = batches.stream().filter(x -> "fifo-a".equals(x.getSourceId())).findFirst().orElseThrow();
        LitemallPointsExpireBatch b = batches.stream().filter(x -> "fifo-b".equals(x.getSourceId())).findFirst().orElseThrow();
        assertEquals(0, a.getRemainingPoints(), "earliest-expire batch A must be fully drained first");
        assertEquals(50, b.getRemainingPoints(), "later batch B must supply the remainder");
    }

    @Test
    public void testSpendBeyondBatchSumFallsBackToStockBalance() {
        // Decision D3: balance > sum(remainingPoints) ⇒ excess spend comes from stock (no batch).
        String accountId = seedAccount(USER_ID, 300).orm_idString();
        seedBatch(accountId, USER_ID, 100, 100, LocalDateTime.now().plusDays(10), "stock-1");

        int balance = spend(USER_ID, 250, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_DEDUCT, "stock-order");
        assertEquals(50, balance, "balance = 300 - 250");

        LitemallPointsExpireBatch batch = findBatches(USER_ID).get(0);
        assertEquals(0, batch.getRemainingPoints(), "batch fully drained (100), remaining 150 came from stock");
    }

    @Test
    public void testExpirePointsDecrementsBalanceAndWritesExpireFlow() {
        String accountId = seedAccount(USER_ID, 200).orm_idString();
        LitemallPointsExpireBatch batch = seedBatch(accountId, USER_ID, 100, 100,
                LocalDateTime.now().minusDays(1), "expired-1");

        int affected = expirePoints();
        assertEquals(1, affected, "one batch should have been expired");

        assertEquals(100, getMyPoints(), "balance must drop by remainingPoints (200 - 100)");

        LitemallPointsExpireBatch reloaded = findBatches(USER_ID).get(0);
        assertEquals(0, reloaded.getRemainingPoints(), "batch remainingPoints zeroed after expire");

        List<Map<String, Object>> expireFlows = getMyFlows(_AppMallDaoConstants.POINTS_CHANGE_TYPE_EXPIRE, null);
        assertEquals(1, expireFlows.size(), "exactly one EXPIRE flow");
        Map<String, Object> flow = expireFlows.get(0);
        assertEquals(100, ((Number) flow.get("changeAmount")).intValue());
        assertEquals(LitemallPointsAccountBizModel.SOURCE_TYPE_EXPIRE, flow.get("sourceType"));
        assertEquals(batch.orm_idString(), flow.get("sourceId"), "EXPIRE flow sourceId must be the batch id");
        assertEquals(100, ((Number) flow.get("balanceAfter")).intValue());
    }

    @Test
    public void testExpirePointsIsIdempotentOnReplay() {
        String accountId = seedAccount(USER_ID, 200).orm_idString();
        seedBatch(accountId, USER_ID, 100, 100, LocalDateTime.now().minusDays(1), "expired-replay");

        assertEquals(1, expirePoints());
        int balanceAfterFirst = getMyPoints();

        // Re-run: batch remainingPoints is now 0 ⇒ selection guard skips it ⇒ no double decrement.
        int affected = expirePoints();
        assertEquals(0, affected, "second run must select nothing (remainingPoints>0 guard)");
        assertEquals(balanceAfterFirst, getMyPoints(), "balance must not change on idempotent re-run");

        List<Map<String, Object>> expireFlows = getMyFlows(_AppMallDaoConstants.POINTS_CHANGE_TYPE_EXPIRE, null);
        assertEquals(1, expireFlows.size(), "no duplicate EXPIRE flow on replay");
    }

    @Test
    public void testExpirePointsProcessesMultipleBatchesWithSequentialCas() {
        // Two expired batches on the same account: each iteration re-reads the account version and
        // CAS-increments it (v0→v1→v2). Verifies no double-count and the per-batch CAS sequence.
        String accountId = seedAccount(USER_ID, 300).orm_idString();
        seedBatch(accountId, USER_ID, 100, 100, LocalDateTime.now().minusDays(2), "multi-1");
        seedBatch(accountId, USER_ID, 200, 200, LocalDateTime.now().minusDays(1), "multi-2");

        int affected = expirePoints();
        assertEquals(2, affected, "both batches expired in one round");
        assertEquals(0, getMyPoints(), "balance = 300 - 100 - 200");

        List<LitemallPointsExpireBatch> batches = findBatches(USER_ID);
        assertTrue(batches.stream().allMatch(b -> b.getRemainingPoints() == 0), "all batches zeroed");
        assertEquals(2, getMyFlows(_AppMallDaoConstants.POINTS_CHANGE_TYPE_EXPIRE, null).size());
    }

    @Test
    public void testExpiryHintReturnsSoonestNonEmptyBatch() {
        assertNull(getExpiryHint(), "no batch ⇒ hint is null");

        String accountId = seedAccount(USER_ID, 100).orm_idString();
        seedBatch(accountId, USER_ID, 80, 80, LocalDateTime.now().plusDays(5), "hint-soon");
        seedBatch(accountId, USER_ID, 50, 50, LocalDateTime.now().plusDays(30), "hint-later");

        Map<String, Object> hint = getExpiryHint();
        assertNotNull(hint);
        assertEquals(80, ((Number) hint.get("points")).intValue(), "must return the soonest-expiring batch");
        long days = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(),
                ((LocalDateTime) hint.get("expireTime")).toLocalDate());
        assertTrue(days >= 4 && days <= 6, "expireTime should be ~5 days out, got " + days);
    }

    @Test
    public void testExpiryHintSkipsZeroRemainingBatches() {
        String accountId = seedAccount(USER_ID, 100).orm_idString();
        seedBatch(accountId, USER_ID, 80, 0, LocalDateTime.now().plusDays(5), "hint-empty");

        Map<String, Object> hint = getExpiryHint();
        assertNull(hint, "a batch with remainingPoints=0 must not be reported as expiring soon");
    }

    @Test
    public void testExpiryHintOmitsFutureExpiredAndStockOnlyReturnsNull() {
        // Unexpired batch present but also confirm expirePoints does not touch unexpired batches.
        String accountId = seedAccount(USER_ID, 100).orm_idString();
        seedBatch(accountId, USER_ID, 90, 90, LocalDateTime.now().plusDays(40), "future-1");

        int affected = expirePoints();
        assertEquals(0, affected, "no batch is past expireTime ⇒ expirePoints is a no-op");
        assertEquals(100, getMyPoints(), "balance unchanged");

        Map<String, Object> hint = getExpiryHint();
        assertNotNull(hint, "unexpired non-empty batch should still be reported as the soonest");
        assertEquals(90, ((Number) hint.get("points")).intValue());
    }

    @Test
    public void testEarnThenSpendKeepsBatchLedgerConsistent() {
        // End-to-end through the BizModel (no manual seeding): earn creates a batch, spend consumes
        // part of it, invariant balance == sum(remaining) + stock must hold (stock == 0 here).
        setConfig(LitemallPointsAccountBizModel.CONFIG_POINTS_VALIDITY_DAYS, "10");
        earn(USER_ID, 100, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "e2e-1");
        spend(USER_ID, 40, LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_DEDUCT, "e2e-order-1");

        assertEquals(60, getMyPoints());
        LitemallPointsExpireBatch batch = findBatches(USER_ID).get(0);
        assertEquals(60, batch.getRemainingPoints(), "spend must decrement the batch by the consumed amount");
        assertFalse(batch.getExpireTime().isBefore(LocalDateTime.now()),
                "batch must still be unexpired");
    }

    // ===== 过期预警推送（successor of getMyPointsExpiryHint）=====

    @Test
    public void testSendPointsExpiryRemindersPushesAggregatedMessageForWindowBatch() {
        // Default remindDays=3: a batch expiring in 2 days is in window → user gets ONE aggregated
        // SYSTEM 站内信 even with multiple in-window batches.
        String accountId = seedAccount(USER_ID, 200).orm_idString();
        seedBatch(accountId, USER_ID, 80, 80, LocalDateTime.now().plusDays(2), "remind-a");
        seedBatch(accountId, USER_ID, 50, 50, LocalDateTime.now().plusDays(3), "remind-b");

        int pushed = sendPointsExpiryReminders();
        assertEquals(1, pushed, "one aggregated reminder per user");
        assertEquals(1, countExpiryReminders(USER_ID), "exactly one SYSTEM reminder message persisted");
    }

    @Test
    public void testSendPointsExpiryRemindersSkipsOutOfWindowAndZeroRemaining() {
        // Out-of-window batch (expire in 10 days, remindDays=3) and remainingPoints=0 batch must
        // not trigger a reminder.
        String accountId = seedAccount(USER_ID, 200).orm_idString();
        seedBatch(accountId, USER_ID, 100, 100, LocalDateTime.now().plusDays(10), "remind-far");
        seedBatch(accountId, USER_ID, 100, 0, LocalDateTime.now().plusDays(1), "remind-empty");

        int pushed = sendPointsExpiryReminders();
        assertEquals(0, pushed, "no in-window non-empty batch ⇒ no push");
        assertEquals(0, countExpiryReminders(USER_ID));
    }

    @Test
    public void testSendPointsExpiryRemindersIsIdempotentOnReplaySameDay() {
        String accountId = seedAccount(USER_ID, 100).orm_idString();
        seedBatch(accountId, USER_ID, 80, 80, LocalDateTime.now().plusDays(2), "remind-idem");

        assertEquals(1, sendPointsExpiryReminders(), "first run pushes one reminder");
        // Replay same day: idempotency guard (today same-title SYSTEM 站内信 exists) skips.
        assertEquals(0, sendPointsExpiryReminders(), "re-run same day must skip (idempotent)");
        assertEquals(1, countExpiryReminders(USER_ID), "still only one reminder after replay");
    }

    @Test
    public void testSendPointsExpiryRemindersRespectsEventToggle() {
        // Event toggle mall_message_event_enabled_points-expiry-remind=false ⇒ no push.
        setConfig("mall_message_event_enabled_" + LitemallPointsAccountBizModel.EVENT_KEY_POINTS_EXPIRY_REMIND, "false");
        String accountId = seedAccount(USER_ID, 100).orm_idString();
        seedBatch(accountId, USER_ID, 80, 80, LocalDateTime.now().plusDays(2), "remind-toggle");

        int pushed = sendPointsExpiryReminders();
        assertEquals(0, pushed, "event toggle off ⇒ no push");
        assertEquals(0, countExpiryReminders(USER_ID));
    }

    @Test
    public void testSendPointsExpiryRemindersUsesConfiguredRemindDays() {
        // Override remindDays to 30 ⇒ a batch expiring in 20 days is in window.
        setConfig(LitemallPointsAccountBizModel.CONFIG_POINTS_EXPIRY_REMIND_DAYS, "30");
        String accountId = seedAccount(USER_ID, 100).orm_idString();
        seedBatch(accountId, USER_ID, 80, 80, LocalDateTime.now().plusDays(20), "remind-wide-window");

        int pushed = sendPointsExpiryReminders();
        assertEquals(1, pushed, "remindDays=30 should include batch expiring in 20 days");
        assertEquals(1, countExpiryReminders(USER_ID));
    }
}
