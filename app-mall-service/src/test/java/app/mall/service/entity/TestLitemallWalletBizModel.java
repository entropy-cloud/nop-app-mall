package app.mall.service.entity;

import app.mall.biz.ILitemallWalletBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallWallet;
import app.mall.dao.entity.LitemallWalletFlow;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.core.context.ServiceContextImpl;
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
public class TestLitemallWalletBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    ILitemallWalletBiz walletBiz;

    @Inject
    IDaoProvider daoProvider;

    private static final String USER_ID = "801";

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId(USER_ID);
        ContextProvider.getOrCreateContext().setUserName("wallet-test");
    }

    private ServiceContextImpl ctx() {
        return new ServiceContextImpl();
    }

    private ApiResponse<?> rpc(GraphQLOperationType op, String action, Map<String, Object> data) {
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(op, action, ApiRequest.build(data));
        return graphQLEngine.executeRpc(ctx);
    }

    private BigDecimal balanceOf(Map<String, Object> walletMap) {
        return new BigDecimal(((Number) walletMap.get("balance")).toString());
    }

    @Test
    public void testGetMyWalletReturnsZeroShellForNewUser() {
        Map<String, Object> data = new HashMap<>();
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallWallet__getMyWallet", data);
        assertEquals(0, r.getStatus(), "getMyWallet failed: " + r);
        Map<String, Object> wallet = (Map<String, Object>) r.getData();
        assertNotNull(wallet);
        assertEquals(0, new BigDecimal(wallet.get("balance").toString()).compareTo(BigDecimal.ZERO),
                "new user wallet shell should have zero balance");
        assertEquals(0, new BigDecimal(wallet.get("totalRecharge").toString()).compareTo(BigDecimal.ZERO));
        assertEquals(0, new BigDecimal(wallet.get("totalSpent").toString()).compareTo(BigDecimal.ZERO));
        // Lazy-create policy: getMyWallet must NOT persist a wallet for a user that has never been credited
        assertNull(wallet.get("id"), "shell wallet must not be persisted (null id)");
    }

    @Test
    public void testCreditBalanceAutoCreatesWalletAndFlowSnapshot() {
        // creditBalance is a @BizAction (internal, fund-safety): tested via injected ILitemallWalletBiz
        LitemallWallet wallet = walletBiz.creditBalance(USER_ID, new BigDecimal("100.50"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "recharge-1", "test-credit",
                ctx());
        assertEquals(0, wallet.getBalance().compareTo(new BigDecimal("100.50")));
        assertEquals(0, wallet.getTotalRecharge().compareTo(new BigDecimal("100.50")));
        assertEquals(0, wallet.getTotalSpent().compareTo(BigDecimal.ZERO));
        assertNotNull(wallet.orm_idString(), "wallet must be persisted on first credit");

        // getMyWallet (separate tx) reflects the committed balance
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallWallet__getMyWallet", new HashMap<>());
        assertEquals(0, r.getStatus());
        assertEquals(0, balanceOf((Map<String, Object>) r.getData()).compareTo(new BigDecimal("100.50")));

        // flow with balanceAfter snapshot
        Map<String, Object> flowData = new HashMap<>();
        ApiResponse<?> fr = rpc(GraphQLOperationType.query, "LitemallWalletFlow__getMyWalletFlows", flowData);
        assertEquals(0, fr.getStatus(), "getMyWalletFlows failed: " + fr);
        List<Map<String, Object>> flows = (List<Map<String, Object>>) fr.getData();
        assertEquals(1, flows.size());
        assertEquals(0, new BigDecimal(flows.get(0).get("changeAmount").toString()).compareTo(new BigDecimal("100.50")));
        assertEquals(0, new BigDecimal(flows.get(0).get("balanceAfter").toString()).compareTo(new BigDecimal("100.50")),
                "flow balanceAfter must snapshot the post-change balance");
        assertEquals(_AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                ((Number) flows.get(0).get("changeType")).intValue());
    }

    @Test
    public void testCreditBalanceAccumulatesBalanceAndTotalRecharge() {
        walletBiz.creditBalance(USER_ID, new BigDecimal("100"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "acc-1", null,
                ctx());
        LitemallWallet wallet = walletBiz.creditBalance(USER_ID, new BigDecimal("50"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "acc-2", null,
                ctx());
        assertEquals(0, wallet.getBalance().compareTo(new BigDecimal("150")));
        assertEquals(0, wallet.getTotalRecharge().compareTo(new BigDecimal("150")));
        assertEquals(0, wallet.getTotalSpent().compareTo(BigDecimal.ZERO));

        QueryBean q = new QueryBean();
        LitemallWallet acct = (LitemallWallet) daoProvider.daoFor(LitemallWallet.class).findAllByQuery(q).stream()
                .findFirst().orElse(null);
        assertNotNull(acct);
        assertEquals(0, acct.getTotalRecharge().compareTo(new BigDecimal("150")));
    }

    @Test
    public void testDebitBalanceRejectsInsufficient() {
        walletBiz.creditBalance(USER_ID, new BigDecimal("30"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "neg-1", null,
                ctx());

        try {
            walletBiz.debitBalance(USER_ID, new BigDecimal("100"),
                    _AppMallDaoConstants.WALLET_CHANGE_TYPE_PAY,
                    LitemallWalletBizModel.SOURCE_TYPE_PAY, "neg-order", "overspend",
                    ctx());
            org.junit.jupiter.api.Assertions.fail("debit beyond balance must throw");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("wallet.insufficient") || e.getMessage().contains("余额不足"),
                    "expected insufficient-balance error, got: " + e.getMessage());
        }

        // balance unchanged after failed debit
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallWallet__getMyWallet", new HashMap<>());
        assertEquals(0, balanceOf((Map<String, Object>) r.getData()).compareTo(new BigDecimal("30")));
    }

    @Test
    public void testDebitBalanceUpdatesTotalSpent() {
        walletBiz.creditBalance(USER_ID, new BigDecimal("200"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "sp-1", null,
                ctx());
        LitemallWallet wallet = walletBiz.debitBalance(USER_ID, new BigDecimal("80"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_PAY,
                LitemallWalletBizModel.SOURCE_TYPE_PAY, "sp-order-1", null,
                ctx());
        assertEquals(0, wallet.getBalance().compareTo(new BigDecimal("120")));
        assertEquals(0, wallet.getTotalSpent().compareTo(new BigDecimal("80")));

        QueryBean q = new QueryBean();
        LitemallWallet acct = (LitemallWallet) daoProvider.daoFor(LitemallWallet.class).findAllByQuery(q).stream()
                .findFirst().orElse(null);
        assertNotNull(acct);
        assertEquals(0, acct.getTotalRecharge().compareTo(new BigDecimal("200")));
        assertEquals(0, acct.getTotalSpent().compareTo(new BigDecimal("80")));
    }

    @Test
    public void testAdminAdjustAddsAndDeducts() {
        walletBiz.creditBalance(USER_ID, new BigDecimal("100"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "adj-seed", null,
                ctx());

        // adminAdjust is a @BizMutation: tested via IGraphQLEngine
        Map<String, Object> addData = new HashMap<>();
        addData.put("userId", USER_ID);
        addData.put("amount", new BigDecimal("50"));
        addData.put("remark", "manual add");
        ApiResponse<?> addR = rpc(GraphQLOperationType.mutation, "LitemallWallet__adminAdjust", addData);
        assertEquals(0, addR.getStatus(), "adminAdjust add failed: " + addR);
        assertEquals(0, balanceOf((Map<String, Object>) addR.getData()).compareTo(new BigDecimal("150")));

        Map<String, Object> subData = new HashMap<>();
        subData.put("userId", USER_ID);
        subData.put("amount", new BigDecimal("-30"));
        subData.put("remark", "manual deduct");
        ApiResponse<?> subR = rpc(GraphQLOperationType.mutation, "LitemallWallet__adminAdjust", subData);
        assertEquals(0, subR.getStatus(), "adminAdjust deduct failed: " + subR);
        assertEquals(0, balanceOf((Map<String, Object>) subR.getData()).compareTo(new BigDecimal("120")));

        // admin-adjust flows recorded with distinct sourceIds
        Map<String, Object> flowData = new HashMap<>();
        flowData.put("sourceType", LitemallWalletBizModel.SOURCE_TYPE_ADMIN_ADJUST);
        ApiResponse<?> fr = rpc(GraphQLOperationType.query, "LitemallWalletFlow__getMyWalletFlows", flowData);
        assertEquals(0, fr.getStatus());
        assertEquals(2, ((List<?>) fr.getData()).size(), "two admin adjusts recorded");
    }

    @Test
    public void testFlowsFilterByChangeType() {
        walletBiz.creditBalance(USER_ID, new BigDecimal("200"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "filt-1", null,
                ctx());
        walletBiz.debitBalance(USER_ID, new BigDecimal("50"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_PAY,
                LitemallWalletBizModel.SOURCE_TYPE_PAY, "filt-order-1", null,
                ctx());

        Map<String, Object> rechargeFlowsData = new HashMap<>();
        rechargeFlowsData.put("changeType", _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE);
        ApiResponse<?> rr = rpc(GraphQLOperationType.query, "LitemallWalletFlow__getMyWalletFlows", rechargeFlowsData);
        assertEquals(0, rr.getStatus());
        List<Map<String, Object>> rechargeFlows = (List<Map<String, Object>>) rr.getData();
        assertTrue(rechargeFlows.stream().allMatch(
                f -> ((Number) f.get("changeType")).intValue() == _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE));

        Map<String, Object> payFlowsData = new HashMap<>();
        payFlowsData.put("changeType", _AppMallDaoConstants.WALLET_CHANGE_TYPE_PAY);
        ApiResponse<?> pr = rpc(GraphQLOperationType.query, "LitemallWalletFlow__getMyWalletFlows", payFlowsData);
        assertEquals(0, pr.getStatus());
        List<Map<String, Object>> payFlows = (List<Map<String, Object>>) pr.getData();
        assertTrue(payFlows.stream().allMatch(
                f -> ((Number) f.get("changeType")).intValue() == _AppMallDaoConstants.WALLET_CHANGE_TYPE_PAY));

        assertNotEquals(0, rechargeFlows.size());
        assertNotEquals(0, payFlows.size());
    }

    @Test
    public void testVersionAdvancesAfterMutation() {
        walletBiz.creditBalance(USER_ID, new BigDecimal("100"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "v-1", null,
                ctx());

        QueryBean q = new QueryBean();
        LitemallWallet acct = (LitemallWallet) daoProvider.daoFor(LitemallWallet.class).findAllByQuery(q).stream()
                .findFirst().orElse(null);
        assertNotNull(acct);
        assertEquals(1, acct.getVersion(), "version must advance to 1 after one successful mutation");
    }
}
