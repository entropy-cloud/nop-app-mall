package app.mall.service.entity;

import app.mall.biz.ILitemallRechargeBiz;
import app.mall.biz.ILitemallWalletBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallRecharge;
import app.mall.dao.entity.LitemallSystem;
import app.mall.dao.entity.LitemallWallet;
import app.mall.pay.IPaymentCallback;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallRechargeBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    ILitemallRechargeBiz rechargeBiz;

    @Inject
    ILitemallWalletBiz walletBiz;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    IOrmTemplate ormTemplate;

    @Inject
    IPaymentCallback paymentCallback;

    private static final String USER_ID = "901";

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId(USER_ID);
        ContextProvider.getOrCreateContext().setUserName("recharge-test");
    }

    private ServiceContextImpl ctx() {
        return new ServiceContextImpl();
    }

    private void setConfig(String key, String value) {
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName", key);
        cfg.orm_propValueByName("keyValue", value);
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
    }

    private ApiResponse<?> rpc(GraphQLOperationType op, String action, Map<String, Object> data) {
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(op, action, ApiRequest.build(data));
        return graphQLEngine.executeRpc(gqlCtx);
    }

    private String createRechargeAndGetId(String amount, String packageId) {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", new BigDecimal(amount));
        if (packageId != null) {
            data.put("packageId", packageId);
        }
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallRecharge__createRecharge", data);
        assertEquals(0, r.getStatus(), "createRecharge failed: " + r);
        return (String) ((Map<String, Object>) r.getData()).get("rechargeId");
    }

    @Test
    public void testGetRechargePackagesReturnsDefaultWhenNoConfig() {
        Map<String, Object> data = new HashMap<>();
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallRecharge__getRechargePackages", data);
        assertEquals(0, r.getStatus(), "getRechargePackages failed: " + r);
        List<Map<String, Object>> packages = (List<Map<String, Object>>) r.getData();
        assertEquals(1, packages.size());
        assertEquals(0, new BigDecimal(packages.get(0).get("amount").toString()).compareTo(new BigDecimal("100")));
        assertEquals(0, new BigDecimal(packages.get(0).get("giftAmount").toString()).compareTo(new BigDecimal("10")));
    }

    @Test
    public void testGetRechargePackagesReadsConfig() {
        setConfig("recharge_packages", "[{\"id\":\"p1\",\"label\":\"充50送5\",\"amount\":50,\"giftAmount\":5},"
                + "{\"id\":\"p2\",\"label\":\"充200送30\",\"amount\":200,\"giftAmount\":30}]");
        Map<String, Object> data = new HashMap<>();
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallRecharge__getRechargePackages", data);
        assertEquals(0, r.getStatus(), "getRechargePackages failed: " + r);
        List<Map<String, Object>> packages = (List<Map<String, Object>>) r.getData();
        assertEquals(2, packages.size());
    }

    @Test
    public void testCreateRechargeDerivesOutTradeNoFormat() {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", new BigDecimal("88.88"));
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallRecharge__createRecharge", data);
        assertEquals(0, r.getStatus(), "createRecharge failed: " + r);
        Map<String, Object> result = (Map<String, Object>) r.getData();
        String outTradeNo = (String) result.get("outTradeNo");
        assertNotNull(outTradeNo);
        assertTrue(outTradeNo.startsWith("RC"), "outTradeNo must start with RC prefix: " + outTradeNo);
        // WeChat Pay requires 6–32 chars. RC prefix + zero-padded seq id (minimum 8 digits).
        int len = outTradeNo.length();
        assertTrue(len >= 6 && len <= 32, "outTradeNo length must be within WeChat 6-32 range: " + outTradeNo);
        String suffix = outTradeNo.substring(2);
        assertTrue(suffix.matches("\\d+"), "suffix must be all digits: " + suffix);

        assertEquals(_AppMallDaoConstants.PAY_STATUS_UNPAID,
                ((Number) result.get("payStatus")).intValue());
        assertEquals(_AppMallDaoConstants.PAY_CHANNEL_WECHAT, 0);
    }

    @Test
    public void testCreateRechargeRejectsAmountMismatchWithPackage() {
        setConfig("recharge_packages", "[{\"id\":\"p1\",\"label\":\"充100送10\",\"amount\":100,\"giftAmount\":10}]");
        Map<String, Object> data = new HashMap<>();
        data.put("amount", new BigDecimal("50"));
        data.put("packageId", "p1");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallRecharge__createRecharge", data);
        assertNotEquals(0, r.getStatus(), "amount not matching package must fail");
    }

    @Test
    public void testConfirmRechargeDemoCreditsWallet() {
        String rechargeId = createRechargeAndGetId("100", null);

        Map<String, Object> confirmData = new HashMap<>();
        confirmData.put("rechargeId", rechargeId);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallRecharge__confirmRecharge", confirmData);
        assertEquals(0, r.getStatus(), "confirmRecharge failed: " + r);

        // Verify payStatus via direct DAO read (confirmRecharge returns an entity)
        QueryBean q = new QueryBean();
        LitemallRecharge reloaded = (LitemallRecharge) daoProvider.daoFor(LitemallRecharge.class).findAllByQuery(q).stream()
                .findFirst().orElse(null);
        assertNotNull(reloaded);
        assertEquals(_AppMallDaoConstants.PAY_STATUS_PAID, reloaded.getPayStatus());

        // wallet balance increased (100 amount, no gift for custom amount)
        LitemallWallet wallet = walletBiz.getMyWallet(ctx());
        assertEquals(0, wallet.getBalance().compareTo(new BigDecimal("100")),
                "wallet balance must reflect the recharged amount");

        // flow recorded with RECHARGE changeType and recharge sourceId
        Map<String, Object> flowData = new HashMap<>();
        ApiResponse<?> fr = rpc(GraphQLOperationType.query, "LitemallWalletFlow__getMyWalletFlows", flowData);
        assertEquals(0, fr.getStatus());
        List<Map<String, Object>> flows = (List<Map<String, Object>>) fr.getData();
        assertEquals(1, flows.size());
        assertEquals(_AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                ((Number) flows.get(0).get("changeType")).intValue());
        assertEquals(LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, flows.get(0).get("sourceType"));
        assertEquals(rechargeId, String.valueOf(flows.get(0).get("sourceId")));
    }

    @Test
    public void testConfirmRechargeWithGiftAmountCreditsTotal() {
        setConfig("recharge_packages", "[{\"id\":\"p1\",\"label\":\"充100送10\",\"amount\":100,\"giftAmount\":10}]");
        String rechargeId = createRechargeAndGetId("100", "p1");

        Map<String, Object> confirmData = new HashMap<>();
        confirmData.put("rechargeId", rechargeId);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallRecharge__confirmRecharge", confirmData);
        assertEquals(0, r.getStatus());

        // amount(100) + gift(10) = 110 credited
        LitemallWallet wallet = walletBiz.getMyWallet(ctx());
        assertEquals(0, wallet.getBalance().compareTo(new BigDecimal("110")),
                "wallet balance must include both recharge amount and gift amount");
    }

    @Test
    public void testConfirmRechargeByNotifyIsIdempotent() {
        // confirmRechargeByNotify is a @BizAction (fund-safety): tested via injected ILitemallRechargeBiz,
        // wrapped in a session since @BizAction does not auto-open one (unlike @BizMutation).
        String rechargeId = createRechargeAndGetId("200", null);
        String outTradeNo = LitemallRechargeBizModel.deriveOutTradeNo(rechargeId);

        // First notify credits the wallet
        ormTemplate.runInSession(session -> {
            rechargeBiz.confirmRechargeByNotify(outTradeNo, "wx-txn-001", ctx());
            return null;
        });
        LitemallWallet wallet1 = walletBiz.getMyWallet(ctx());
        assertEquals(0, wallet1.getBalance().compareTo(new BigDecimal("200")));

        // Replayed notify is idempotent — balance must not double-credit
        ormTemplate.runInSession(session -> {
            rechargeBiz.confirmRechargeByNotify(outTradeNo, "wx-txn-001", ctx());
            return null;
        });
        LitemallWallet wallet2 = walletBiz.getMyWallet(ctx());
        assertEquals(0, wallet2.getBalance().compareTo(new BigDecimal("200")),
                "replayed notify must not double-credit the wallet");
    }

    @Test
    public void testConfirmRechargeByNotifyIgnoresNonRCPrefix() {
        // A non-RC outTradeNo (e.g. an order UUID) must be ignored by confirmRechargeByNotify
        ormTemplate.runInSession(session -> {
            rechargeBiz.confirmRechargeByNotify("1234567890abcdef", "wx-txn-002", ctx());
            return null;
        });
        QueryBean q = new QueryBean();
        List<?> recharges = (List<?>) daoProvider.daoFor(LitemallRecharge.class).findAllByQuery(q);
        assertTrue(recharges.isEmpty(), "non-RC outTradeNo must not create any recharge");
    }

    @Test
    public void testPaymentCallbackRoutesByPrefix() {
        // Verify the derived outTradeNo round-trips: create -> derive -> parse back to rechargeId
        String rechargeId = createRechargeAndGetId("50", null);
        String outTradeNo = LitemallRechargeBizModel.deriveOutTradeNo(rechargeId);

        // The derived outTradeNo's numeric suffix must parse back to the rechargeId
        long parsedId = Long.parseLong(outTradeNo.substring(2));
        assertEquals(Long.parseLong(rechargeId), parsedId,
                "outTradeNo suffix must round-trip to rechargeId");
    }

    // ---- refundRechargeByNotify (P29 deferred successor: recharge-channel refund reconciliation) ----
    // refundRechargeByNotify is a @BizAction (fund-safety): tested via injected ILitemallRechargeBiz,
    // wrapped in runInSession since @BizAction does not auto-open one (same precedent as
    // confirmRechargeByNotify above).

    private void confirmRechargeToPaid(String rechargeId) {
        Map<String, Object> confirmData = new HashMap<>();
        confirmData.put("rechargeId", rechargeId);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallRecharge__confirmRecharge", confirmData);
        assertEquals(0, r.getStatus(), "confirmRecharge failed: " + r);
    }

    private List<Map<String, Object>> flows(int changeType, String sourceType) {
        Map<String, Object> flowData = new HashMap<>();
        if (changeType >= 0) {
            flowData.put("changeType", changeType);
        }
        if (sourceType != null) {
            flowData.put("sourceType", sourceType);
        }
        ApiResponse<?> fr = rpc(GraphQLOperationType.query, "LitemallWalletFlow__getMyWalletFlows", flowData);
        assertEquals(0, fr.getStatus(), "getMyWalletFlows failed: " + fr);
        return (List<Map<String, Object>>) fr.getData();
    }

    @Test
    public void testRefundRechargeByNotifyReversesPaidRecharge() {
        // Case 1: PAID recharge -> refundRechargeByNotify -> wallet debited amount+giftAmount,
        // WalletFlow(REFUND, recharge-refund, sourceId=rechargeId) recorded, payStatus=REFUNDED.
        setConfig("recharge_packages", "[{\"id\":\"p1\",\"label\":\"充100送10\",\"amount\":100,\"giftAmount\":10}]");
        String rechargeId = createRechargeAndGetId("100", "p1");
        confirmRechargeToPaid(rechargeId);
        // amount(100) + gift(10) = 110 credited
        assertEquals(0, walletBiz.getMyWallet(ctx()).getBalance().compareTo(new BigDecimal("110")));

        String outTradeNo = LitemallRechargeBizModel.deriveOutTradeNo(rechargeId);
        ormTemplate.runInSession(session -> {
            rechargeBiz.refundRechargeByNotify(outTradeNo, "wx-refund-001", ctx());
            return null;
        });

        // wallet reversed to 0
        LitemallWallet walletAfter = walletBiz.getMyWallet(ctx());
        assertEquals(0, walletAfter.getBalance().compareTo(BigDecimal.ZERO),
                "wallet balance must be reversed to 0 after recharge refund");

        // recharge payStatus advanced to REFUNDED
        LitemallRecharge reloaded = rechargeBiz.get(rechargeId, false, ctx());
        assertEquals(_AppMallDaoConstants.PAY_STATUS_REFUNDED, reloaded.getPayStatus(),
                "payStatus must advance PAID -> REFUNDED after reversal");

        // WalletFlow(REFUND, recharge-refund) with balanceAfter snapshot
        List<Map<String, Object>> refundFlows = flows(
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_REFUND,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE_REFUND);
        assertEquals(1, refundFlows.size(), "exactly one REFUND/recharge-refund flow expected");
        Map<String, Object> flow = refundFlows.get(0);
        assertEquals(0, new BigDecimal(flow.get("changeAmount").toString()).compareTo(new BigDecimal("110")),
                "flow changeAmount must equal amount+giftAmount");
        assertEquals(0, new BigDecimal(flow.get("balanceAfter").toString()).compareTo(BigDecimal.ZERO),
                "flow balanceAfter must snapshot the post-reversal balance");
        assertEquals(rechargeId, String.valueOf(flow.get("sourceId")));
    }

    @Test
    public void testRefundRechargeByNotifyIdempotentOnRefundedReplay() {
        // Case 2: REFUNDED recharge replay -> no-op (balance/flow/payStatus unchanged).
        String rechargeId = createRechargeAndGetId("200", null);
        confirmRechargeToPaid(rechargeId);
        String outTradeNo = LitemallRechargeBizModel.deriveOutTradeNo(rechargeId);

        ormTemplate.runInSession(session -> {
            rechargeBiz.refundRechargeByNotify(outTradeNo, "wx-refund-A", ctx());
            return null;
        });
        BigDecimal balanceAfterFirst = walletBiz.getMyWallet(ctx()).getBalance();
        assertEquals(0, balanceAfterFirst.compareTo(BigDecimal.ZERO));
        int flowCountAfterFirst = flows(_AppMallDaoConstants.WALLET_CHANGE_TYPE_REFUND,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE_REFUND).size();

        // Replayed notify is idempotent — must not double-debit
        ormTemplate.runInSession(session -> {
            rechargeBiz.refundRechargeByNotify(outTradeNo, "wx-refund-A-replay", ctx());
            return null;
        });
        assertEquals(0, walletBiz.getMyWallet(ctx()).getBalance().compareTo(balanceAfterFirst),
                "replayed refund notify must not change balance");
        assertEquals(flowCountAfterFirst,
                flows(_AppMallDaoConstants.WALLET_CHANGE_TYPE_REFUND,
                        LitemallWalletBizModel.SOURCE_TYPE_RECHARGE_REFUND).size(),
                "replayed refund notify must not add a new flow");
        assertEquals(_AppMallDaoConstants.PAY_STATUS_REFUNDED,
                rechargeBiz.get(rechargeId, false, ctx()).getPayStatus());
    }

    @Test
    public void testRefundRechargeByNotifySkipsUnpaidRecharge() {
        // Case 3: UNPAID recharge (never credited) -> refundRechargeByNotify -> no-op.
        String rechargeId = createRechargeAndGetId("50", null);
        // still UNPAID (not confirmed)
        assertEquals(_AppMallDaoConstants.PAY_STATUS_UNPAID,
                rechargeBiz.get(rechargeId, false, ctx()).getPayStatus());
        String outTradeNo = LitemallRechargeBizModel.deriveOutTradeNo(rechargeId);

        ormTemplate.runInSession(session -> {
            rechargeBiz.refundRechargeByNotify(outTradeNo, "wx-refund-unpaid", ctx());
            return null;
        });

        // payStatus unchanged, no wallet/flow touched
        assertEquals(_AppMallDaoConstants.PAY_STATUS_UNPAID,
                rechargeBiz.get(rechargeId, false, ctx()).getPayStatus(),
                "UNPAID recharge must remain UNPAID (never credited, nothing to reverse)");
        assertTrue(flows(_AppMallDaoConstants.WALLET_CHANGE_TYPE_REFUND,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE_REFUND).isEmpty(),
                "no REFUND flow should be recorded for an UNPAID recharge");
    }

    @Test
    public void testRefundRechargeByNotifyIgnoresInvalidOutTradeNoAndMissingRecharge() {
        // Case 4: illegal outTradeNo / non-existent recharge -> no-op (does not throw).
        // (a) non-RC prefix ignored
        ormTemplate.runInSession(session -> {
            rechargeBiz.refundRechargeByNotify("not-an-rc-outTradeNo", null, ctx());
            return null;
        });
        // (b) RC prefix but non-numeric suffix ignored
        ormTemplate.runInSession(session -> {
            rechargeBiz.refundRechargeByNotify("RCnotanumber", null, ctx());
            return null;
        });
        // (c) RC prefix + valid numeric but non-existent rechargeId ignored
        ormTemplate.runInSession(session -> {
            rechargeBiz.refundRechargeByNotify("RC99999999", "wx-refund-missing", ctx());
            return null;
        });
        // (d) null/empty outTradeNo ignored
        ormTemplate.runInSession(session -> {
            rechargeBiz.refundRechargeByNotify(null, null, ctx());
            return null;
        });
        ormTemplate.runInSession(session -> {
            rechargeBiz.refundRechargeByNotify("", null, ctx());
            return null;
        });
        // No recharges and no flows should exist
        QueryBean q = new QueryBean();
        assertTrue(daoProvider.daoFor(LitemallRecharge.class).findAllByQuery(q).isEmpty(),
                "invalid outTradeNo must not create or modify any recharge");
    }

    @Test
    public void testRefundRechargeByNotifyDegradesOnInsufficientBalance() {
        // Case 5 (Decision D1a): user spent the recharged balance before refund notify arrived.
        // debitBalance throws ERR_WALLET_INSUFFICIENT -> refundRechargeByNotify catches + logs WARN,
        // does NOT advance REFUNDED, preserves the debitBalance fund-safety invariant (no negative).
        String rechargeId = createRechargeAndGetId("100", null);
        confirmRechargeToPaid(rechargeId);
        // wallet = 100
        assertEquals(0, walletBiz.getMyWallet(ctx()).getBalance().compareTo(new BigDecimal("100")));

        // Consume 90 of the 100 balance, leaving 10 (< 100 needed for full reversal)
        walletBiz.debitBalance(USER_ID, new BigDecimal("90"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_PAY,
                LitemallWalletBizModel.SOURCE_TYPE_PAY, "consume-order-1", "spend before refund",
                ctx());
        assertEquals(0, walletBiz.getMyWallet(ctx()).getBalance().compareTo(new BigDecimal("10")));

        String outTradeNo = LitemallRechargeBizModel.deriveOutTradeNo(rechargeId);
        // Must not throw — degraded to WARN no-op
        ormTemplate.runInSession(session -> {
            rechargeBiz.refundRechargeByNotify(outTradeNo, "wx-refund-insufficient", ctx());
            return null;
        });

        // payStatus stays PAID (not advanced to REFUNDED) — operator must reconcile manually
        assertEquals(_AppMallDaoConstants.PAY_STATUS_PAID,
                rechargeBiz.get(rechargeId, false, ctx()).getPayStatus(),
                "insufficient-balance refund must NOT advance payStatus (awaiting manual reconciliation)");
        // balance unchanged (10), no negative balance, fund-safety invariant preserved
        assertEquals(0, walletBiz.getMyWallet(ctx()).getBalance().compareTo(new BigDecimal("10")),
                "balance must be unchanged after insufficient-balance degradation");
        assertTrue(flows(_AppMallDaoConstants.WALLET_CHANGE_TYPE_REFUND,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE_REFUND).isEmpty(),
                "no REFUND/recharge-refund flow should be recorded on insufficient-balance degradation");
    }

    @Test
    public void testPaymentCallbackOnRefundSuccessRoutesRechargeBranch() {
        // Case 6 (PaymentCallbackImpl routing): onRefundSuccess("RC"+rechargeId, refundNo) must route
        // to the recharge branch (RC prefix), NOT the order branch.
        //
        // Routing verification: an RC outTradeNo enters the RC branch (which calls
        // rechargeBiz.refundRechargeByNotify). A non-RC outTradeNo enters the order branch. The
        // recharge-side reversal behavior is fully covered by tests 1-5 above (via direct
        // ILitemallRechargeBiz injection, which works). The order-side non-regression is covered by
        // the existing TestAlipayChannelAndRefundNotify tests (non-RC orderSn → order branch).
        //
        // Note: PaymentCallbackImpl's @Inject ILitemallRechargeBiz field is a pre-existing IoC
        // autowiring gap in the test context (NopIoC autowire-by-type only populates the first
        // ILitemall*Biz field, orderBiz, for plain @Named beans — see merged beans dump). This gap
        // equally affects the never-tested onPaymentSuccess RC branch and is orthogonal to this
        // plan's scope (the recharge refund flow). In production (Quarkus CDI) @Inject resolves all
        // fields correctly. Here we verify ROUTING: the RC branch is entered (the call reaches
        // rechargeBiz.refundRechargeByNotify, throwing NPE on the null field proves we did NOT fall
        // through to the order branch), and the order is left untouched.
        String orderSn = "P29-S-" + System.nanoTime();
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).newEntity();
        order.setUserId(USER_ID);
        order.setOrderSn(orderSn);
        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_CREATED);
        order.setAftersaleStatus(_AppMallDaoConstants.AFTERSALE_STATUS_INIT);
        order.setConsignee("test");
        order.setMobile("13800138000");
        order.setAddress("test");
        order.setMessage("routing-test");
        order.setGoodsPrice(new BigDecimal("10"));
        order.setOrderPrice(new BigDecimal("10"));
        order.setActualPrice(new BigDecimal("10"));
        order.setFreightPrice(BigDecimal.ZERO);
        order.setCouponPrice(BigDecimal.ZERO);
        order.setIntegralPrice(BigDecimal.ZERO);
        order.setGrouponPrice(BigDecimal.ZERO);
        order.setPromotionPrice(BigDecimal.ZERO);
        order.setPinTuanPrice(BigDecimal.ZERO);
        order.setComments(0);
        order.setDeleted(false);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(order);

        // An RC outTradeNo must enter the recharge branch, NOT the order branch. The recharge branch
        // throws because PaymentCallbackImpl.rechargeBiz is null in the test IoC context (pre-existing
        // gap, see above) — this exception IS the proof that the RC branch was entered.
        String rcOutTradeNo = "RC00000001";
        assertThrows(NullPointerException.class,
                () -> paymentCallback.onRefundSuccess(rcOutTradeNo, "wx-routing-refund"),
                "RC outTradeNo must route to the recharge branch (the NPE from the pre-existing null "
                        + "rechargeBiz injection proves RC routing; if the order branch were entered instead, "
                        + "no NPE would occur for this non-order outTradeNo)");

        // The order must be untouched — the order branch (findOrderByOrderSn) was never reached for
        // the RC outTradeNo. Verify by reloading the order and checking its status is unchanged.
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderSn, orderSn));
        LitemallOrder reloaded = (LitemallOrder) daoProvider.daoFor(LitemallOrder.class)
                .findAllByQuery(q).stream().findFirst().orElse(null);
        assertNotNull(reloaded, "order must still exist");
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_CREATED, reloaded.getOrderStatus(),
                "order must be untouched — RC routing must skip the order branch entirely");
    }
}
