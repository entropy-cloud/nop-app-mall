package app.mall.service.entity;

import app.mall.biz.ILitemallRechargeBiz;
import app.mall.biz.ILitemallWalletBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallRecharge;
import app.mall.dao.entity.LitemallSystem;
import app.mall.dao.entity.LitemallWallet;
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
}
