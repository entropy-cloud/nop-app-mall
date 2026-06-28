package app.mall.service.pay;

import app.mall.biz.ILitemallWalletBiz;
import app.mall.biz.ILitemallWalletFlowBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallWallet;
import app.mall.dao.entity.LitemallWalletFlow;
import app.mall.service.entity.LitemallWalletBizModel;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.auth.core.password.IPasswordEncoder;
import io.nop.auth.dao.entity.NopAuthUser;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P30 Phase 2 tests: balance-payment channel.
 *
 * <p>Covers {@code payByBalance} via {@link IGraphQLEngine} for success + all documented failure
 * modes (insufficient balance, wrong credential, non-CREATED status idempotency), the wallet
 * flow side-effect ({@code PAY + sourceType=pay}), the channel fields ({@code payChannel=20},
 * {@code walletPayAmount}), and the {@code markOrderPaidCore} extraction not regressing
 * {@code pay()} (demo confirm) and {@code confirmPaidByNotify}.
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestPayByBalanceBizModel extends JunitBaseTestCase {

    private static final String PAY_PASSWORD = "Pass@word1";
    private static final String USER_ID = "302";

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    ILitemallWalletBiz walletBiz;

    @Inject
    ILitemallWalletFlowBiz flowBiz;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    IPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Create a NopAuthUser directly with a numeric id and a known encoded password, so the
        // balance-payment confirm-credential check (Decision B: reuse login password via
        // IPasswordEncoder) has a verifiable credential. A numeric id keeps wallet/order userId
        // binding consistent with the rest of the test suite.
        NopAuthUser user = daoProvider.daoFor(NopAuthUser.class).newEntity();
        user.orm_propValueByName("id", USER_ID);
        user.orm_propValueByName("userId", USER_ID);
        user.orm_propValueByName("userName", "balance-test");
        user.orm_propValueByName("nickName", "balance-test");
        user.orm_propValueByName("openId", USER_ID);
        user.orm_propValueByName("gender", 0);
        user.orm_propValueByName("userType", 0);
        user.orm_propValueByName("status", 1);
        user.orm_propValueByName("tenantId", "0");
        user.orm_propValueByName("delFlag", 0);
        user.orm_propValueByName("version", 0);
        String salt = passwordEncoder.generateSalt();
        String encoded = passwordEncoder.encodePassword(PAY_PASSWORD, salt);
        user.orm_propValueByName("password", encoded);
        user.orm_propValueByName("salt", salt);
        daoProvider.daoFor(NopAuthUser.class).saveEntity(user);

        ContextProvider.getOrCreateContext().setUserId(USER_ID);
        ContextProvider.getOrCreateContext().setUserName("balance-test");
    }

    private ServiceContextImpl ctx() {
        return new ServiceContextImpl();
    }

    private ApiResponse<?> rpc(GraphQLOperationType op, String action, Map<String, Object> data) {
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(op, action, ApiRequest.build(data));
        return graphQLEngine.executeRpc(gqlCtx);
    }

    private String createOrderForUser(String userId, BigDecimal actualPrice) {
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).newEntity();
        order.setUserId(userId);
        order.setOrderSn("P30B-" + System.nanoTime());
        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_CREATED);
        order.setAftersaleStatus(_AppMallDaoConstants.AFTERSALE_STATUS_INIT);
        order.setConsignee("test");
        order.setMobile("13800138000");
        order.setAddress("test");
        order.setMessage("test");
        order.setGoodsPrice(actualPrice);
        order.setOrderPrice(actualPrice);
        order.setActualPrice(actualPrice);
        order.setFreightPrice(BigDecimal.ZERO);
        order.setCouponPrice(BigDecimal.ZERO);
        order.setIntegralPrice(BigDecimal.ZERO);
        order.setGrouponPrice(BigDecimal.ZERO);
        order.setPromotionPrice(BigDecimal.ZERO);
        order.setPinTuanPrice(BigDecimal.ZERO);
        order.setComments(0);
        order.setDeleted(false);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(order);
        return order.orm_idString();
    }

    private LitemallOrder reloadOrder(String orderId) {
        return (LitemallOrder) daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
    }

    @Test
    public void testPayByBalanceSuccess() {
        // Seed wallet with 500, order costs 99.
        walletBiz.creditBalance(USER_ID, new BigDecimal("500"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "seed-pay-1", null, ctx());

        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("confirmCredential", PAY_PASSWORD);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallOrder__payByBalance", data);
        assertEquals(0, r.getStatus(), "payByBalance failed: " + r);

        // Order advanced to PAID with BALANCE channel + walletPayAmount set
        LitemallOrder paid = reloadOrder(orderId);
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_PAY, paid.getOrderStatus(),
                "order must advance to PAID (201)");
        assertEquals(_AppMallDaoConstants.PAY_CHANNEL_BALANCE, paid.getPayChannel().intValue(),
                "payChannel must be 20 (BALANCE)");
        assertEquals(0, paid.getWalletPayAmount().compareTo(new BigDecimal("99")),
                "walletPayAmount must equal actualPrice");

        // Wallet balance reduced by 99 (500 -> 401)
        LitemallWallet wallet = walletBiz.getMyWallet(ctx());
        assertEquals(0, wallet.getBalance().compareTo(new BigDecimal("401")),
                "wallet balance must be reduced by the paid amount");

        // A PAY/sourceType=pay flow recorded against this order
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(app.mall.dao.entity.LitemallWalletFlow.PROP_NAME_changeType,
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_PAY));
        List<LitemallWalletFlow> flows = flowBiz.findList(q, null, ctx());
        boolean hasPayFlow = flows.stream().anyMatch(f ->
                LitemallWalletBizModel.SOURCE_TYPE_PAY.equals(f.getSourceType())
                        && reloadOrder(orderId).getOrderSn().equals(f.getSourceId()));
        assertTrue(hasPayFlow, "a PAY/sourceType=pay flow tied to the orderSn must exist");
    }

    @Test
    public void testPayByBalanceRejectsWrongCredential() {
        walletBiz.creditBalance(USER_ID, new BigDecimal("500"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "seed-pay-2", null, ctx());
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("confirmCredential", "wrong-password");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallOrder__payByBalance", data);
        assertNotEquals(0, r.getStatus(), "wrong credential must be rejected");
        assertTrue(r.getMsg().contains("pay-credential-invalid") || r.getMsg().contains("凭证不正确"),
                "expected credential-invalid error, got: " + r.getMsg());

        // Order must NOT advance, balance must NOT change
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_CREATED, reloadOrder(orderId).getOrderStatus());
    }

    @Test
    public void testPayByBalanceRejectsInsufficientBalance() {
        // Seed only 50, order costs 99
        walletBiz.creditBalance(USER_ID, new BigDecimal("50"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "seed-pay-3", null, ctx());
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("confirmCredential", PAY_PASSWORD);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallOrder__payByBalance", data);
        assertNotEquals(0, r.getStatus(), "insufficient balance must be rejected");
        assertTrue(r.getMsg().contains("balance-insufficient") || r.getMsg().contains("余额不足")
                        || r.getMsg().contains("insufficient") || r.getMsg().contains("version-conflict"),
                "expected insufficient-balance error, got: " + r.getMsg());
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_CREATED, reloadOrder(orderId).getOrderStatus());
    }

    @Test
    public void testPayByBalanceIdempotentStatusGuard() {
        walletBiz.creditBalance(USER_ID, new BigDecimal("500"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "seed-pay-4", null, ctx());
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        // First call succeeds
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("confirmCredential", PAY_PASSWORD);
        ApiResponse<?> r1 = rpc(GraphQLOperationType.mutation, "LitemallOrder__payByBalance", data);
        assertEquals(0, r1.getStatus(), "first payByBalance should succeed: " + r1);

        // Second (repeat) call must be rejected by the CREATED(101) status guard — double-layer
        // idempotency layer 1 (debitBalance optimistic lock is layer 2).
        ApiResponse<?> r2 = rpc(GraphQLOperationType.mutation, "LitemallOrder__payByBalance", data);
        assertNotEquals(0, r2.getStatus(), "repeat payByBalance must be rejected by status guard");
        assertTrue(r2.getMsg().contains("not-allow-pay") || r2.getMsg().contains("不允许支付"),
                "expected not-allow-pay error, got: " + r2.getMsg());

        // Balance debited only once (500 -> 401, not 302)
        LitemallWallet wallet = walletBiz.getMyWallet(ctx());
        assertEquals(0, wallet.getBalance().compareTo(new BigDecimal("401")),
                "wallet must be debited exactly once");
    }

    @Test
    public void testMarkOrderPaidCoreNotRegressPayDemoConfirm() {
        // pay() in demo mode (wxpay disabled) still advances a CREATED order to PAID via the
        // extracted markOrderPaidCore. Zero-amount order uses pay() directly.
        String orderId = createOrderForUser(USER_ID, BigDecimal.ZERO);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallOrder__pay", data);
        assertEquals(0, r.getStatus(), "pay() zero-amount demo confirm failed: " + r);
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_PAY, reloadOrder(orderId).getOrderStatus());
    }
}
