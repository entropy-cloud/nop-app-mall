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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 1 tests: combo-payment (balance deduction + third-party channel补差) core + re-entry guard +
 * CREATED cancel/expire balance credit-back.
 *
 * <p>Covers {@code payWithCombo} via {@link IGraphQLEngine} for success (remainder>0 stays 101 with
 * walletPayAmount + debited wallet) + degenerate full-balance path (advances to PAY) + all failure
 * modes (re-entry, insufficient balance, invalid amount, wrong credential), the re-entry guard on
 * {@code payByBalance}/{@code prepay} against a combo-pending order, and the symmetric wallet
 * credit-back when a combo-pending CREATED order is canceled / auto-expired.
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestComboPaymentBizModel extends JunitBaseTestCase {

    private static final String PAY_PASSWORD = "Pass@word1";
    private static final String USER_ID = "3044";

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
        NopAuthUser user = daoProvider.daoFor(NopAuthUser.class).newEntity();
        user.orm_propValueByName("id", USER_ID);
        user.orm_propValueByName("userId", USER_ID);
        user.orm_propValueByName("userName", "combo-test");
        user.orm_propValueByName("nickName", "combo-test");
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
        ContextProvider.getOrCreateContext().setUserName("combo-test");
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
        order.setOrderSn("COMBO-" + System.nanoTime());
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

    private void seedWallet(String userId, BigDecimal amount, String seedId) {
        walletBiz.creditBalance(userId, amount,
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, seedId, null, ctx());
    }

    private Map<String, Object> comboData(String orderId, BigDecimal useBalanceAmount) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("useBalanceAmount", useBalanceAmount);
        data.put("confirmCredential", PAY_PASSWORD);
        return data;
    }

    @Test
    public void testPayWithComboSuccessRemainderStaysCreated() {
        seedWallet(USER_ID, new BigDecimal("500"), "seed-combo-1");
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));
        BigDecimal balanceBefore = walletBiz.getMyWallet(ctx()).getBalance();

        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallOrder__payWithCombo",
                comboData(orderId, new BigDecimal("30")));
        assertEquals(0, r.getStatus(), "payWithCombo failed: " + r);

        LitemallOrder order = reloadOrder(orderId);
        // Order stays at CREATED(101) waiting for the third-party补差 async notify.
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_CREATED, order.getOrderStatus(),
                "combo order must stay at 101 until the补差 notify advances it");
        assertEquals(0, order.getWalletPayAmount().compareTo(new BigDecimal("30")),
                "walletPayAmount must equal the deducted balance portion");
        assertNotNull(order.getPayId(), "payId must be recorded by createPayment for the补差 portion");

        // Wallet debited exactly the balance portion (500 -> 470).
        BigDecimal balanceAfter = walletBiz.getMyWallet(ctx()).getBalance();
        assertEquals(0, balanceAfter.compareTo(balanceBefore.subtract(new BigDecimal("30"))),
                "wallet must be debited by the balance portion only");
    }

    @Test
    public void testPayWithComboDegenerateFullBalanceAdvancesToPaid() {
        seedWallet(USER_ID, new BigDecimal("500"), "seed-combo-2");
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        // useBalanceAmount == actualPrice degenerates to a full-balance payment.
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallOrder__payWithCombo",
                comboData(orderId, new BigDecimal("99")));
        assertEquals(0, r.getStatus(), "degenerate combo failed: " + r);

        LitemallOrder order = reloadOrder(orderId);
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_PAY, order.getOrderStatus(),
                "full-balance combo must advance to PAY(201)");
        assertEquals(_AppMallDaoConstants.PAY_CHANNEL_BALANCE, order.getPayChannel().intValue(),
                "payChannel must be BALANCE on degenerate full-balance path");
        assertEquals(0, order.getWalletPayAmount().compareTo(new BigDecimal("99")),
                "walletPayAmount must equal actualPrice");
    }

    @Test
    public void testPayWithComboRejectsInsufficientBalance() {
        seedWallet(USER_ID, new BigDecimal("20"), "seed-combo-3");
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallOrder__payWithCombo",
                comboData(orderId, new BigDecimal("30")));
        assertNotEquals(0, r.getStatus(), "insufficient balance must be rejected");
        assertTrue(r.getMsg().contains("balance-insufficient") || r.getMsg().contains("余额不足")
                        || r.getMsg().contains("insufficient") || r.getMsg().contains("version-conflict"),
                "expected insufficient-balance error, got: " + r.getMsg());
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_CREATED, reloadOrder(orderId).getOrderStatus());
    }

    @Test
    public void testPayWithComboRejectsInvalidAmount() {
        seedWallet(USER_ID, new BigDecimal("500"), "seed-combo-4");
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        // zero balance amount
        ApiResponse<?> r0 = rpc(GraphQLOperationType.mutation, "LitemallOrder__payWithCombo",
                comboData(orderId, BigDecimal.ZERO));
        assertNotEquals(0, r0.getStatus(), "zero balance amount must be rejected");
        assertTrue(r0.getMsg().contains("combo-amount-invalid") || r0.getMsg().contains("金额非法"),
                "expected combo-amount-invalid error, got: " + r0.getMsg());

        // amount exceeding actualPrice
        ApiResponse<?> rOver = rpc(GraphQLOperationType.mutation, "LitemallOrder__payWithCombo",
                comboData(orderId, new BigDecimal("100")));
        assertNotEquals(0, rOver.getStatus(), "amount > actualPrice must be rejected");
        assertTrue(rOver.getMsg().contains("combo-amount-invalid") || rOver.getMsg().contains("金额非法"),
                "expected combo-amount-invalid error, got: " + rOver.getMsg());

        assertEquals(_AppMallDaoConstants.ORDER_STATUS_CREATED, reloadOrder(orderId).getOrderStatus());
        assertEquals(0, walletBiz.getMyWallet(ctx()).getBalance().compareTo(new BigDecimal("500")),
                "wallet must be untouched when amount is rejected");
    }

    @Test
    public void testPayWithComboRejectsWrongCredential() {
        seedWallet(USER_ID, new BigDecimal("500"), "seed-combo-5");
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        Map<String, Object> data = comboData(orderId, new BigDecimal("30"));
        data.put("confirmCredential", "wrong-password");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallOrder__payWithCombo", data);
        assertNotEquals(0, r.getStatus(), "wrong credential must be rejected");
        assertTrue(r.getMsg().contains("pay-credential-invalid") || r.getMsg().contains("凭证不正确"),
                "expected credential-invalid error, got: " + r.getMsg());
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_CREATED, reloadOrder(orderId).getOrderStatus());
    }

    @Test
    public void testPayWithComboRejectsReentryOnComboPendingOrder() {
        seedWallet(USER_ID, new BigDecimal("500"), "seed-combo-6");
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        // First combo call: deduct 30, order stays 101 with walletPayAmount=30.
        ApiResponse<?> r1 = rpc(GraphQLOperationType.mutation, "LitemallOrder__payWithCombo",
                comboData(orderId, new BigDecimal("30")));
        assertEquals(0, r1.getStatus(), "first combo should succeed: " + r1);

        // Second combo entry on the combo-pending order must be rejected by the re-entry guard.
        ApiResponse<?> r2 = rpc(GraphQLOperationType.mutation, "LitemallOrder__payWithCombo",
                comboData(orderId, new BigDecimal("30")));
        assertNotEquals(0, r2.getStatus(), "re-entry on combo-pending order must be rejected");
        assertTrue(r2.getMsg().contains("combo-pending") || r2.getMsg().contains("组合支付"),
                "expected combo-pending error, got: " + r2.getMsg());

        // Wallet debited only once (500 -> 470, not 440).
        assertEquals(0, walletBiz.getMyWallet(ctx()).getBalance().compareTo(new BigDecimal("470")),
                "wallet must be debited exactly once");
    }

    @Test
    public void testPayByBalanceRejectsReentryOnComboPendingOrder() {
        seedWallet(USER_ID, new BigDecimal("500"), "seed-combo-7");
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        // Combo: deduct 30, order stays 101 with walletPayAmount=30.
        ApiResponse<?> rCombo = rpc(GraphQLOperationType.mutation, "LitemallOrder__payWithCombo",
                comboData(orderId, new BigDecimal("30")));
        assertEquals(0, rCombo.getStatus(), "combo setup failed: " + rCombo);

        // payByBalance on the combo-pending order must be rejected by the re-entry guard, not
        // double-debit the wallet for the full actualPrice.
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("confirmCredential", PAY_PASSWORD);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallOrder__payByBalance", data);
        assertNotEquals(0, r.getStatus(), "payByBalance on combo-pending order must be rejected");
        assertTrue(r.getMsg().contains("combo-pending") || r.getMsg().contains("组合支付"),
                "expected combo-pending error, got: " + r.getMsg());
        assertEquals(0, walletBiz.getMyWallet(ctx()).getBalance().compareTo(new BigDecimal("470")),
                "wallet must NOT be double-debited");
    }

    @Test
    public void testPrepayRejectsReentryOnComboPendingOrder() {
        seedWallet(USER_ID, new BigDecimal("500"), "seed-combo-8");
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        // Combo: deduct 30, order stays 101 with walletPayAmount=30.
        ApiResponse<?> rCombo = rpc(GraphQLOperationType.mutation, "LitemallOrder__payWithCombo",
                comboData(orderId, new BigDecimal("30")));
        assertEquals(0, rCombo.getStatus(), "combo setup failed: " + rCombo);

        // prepay on the combo-pending order must be rejected by the re-entry guard, not create a
        // second prepay for the full amount.
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallOrder__prepay", data);
        assertNotEquals(0, r.getStatus(), "prepay on combo-pending order must be rejected");
        assertTrue(r.getMsg().contains("combo-pending") || r.getMsg().contains("组合支付"),
                "expected combo-pending error, got: " + r.getMsg());
    }

    @Test
    public void testCancelComboPendingOrderCreditsWalletBack() {
        seedWallet(USER_ID, new BigDecimal("500"), "seed-combo-9");
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        // Combo: deduct 30, wallet 500 -> 470.
        ApiResponse<?> rCombo = rpc(GraphQLOperationType.mutation, "LitemallOrder__payWithCombo",
                comboData(orderId, new BigDecimal("30")));
        assertEquals(0, rCombo.getStatus(), "combo setup failed: " + rCombo);
        assertEquals(0, walletBiz.getMyWallet(ctx()).getBalance().compareTo(new BigDecimal("470")),
                "wallet must be 470 after combo debit");

        // Cancel the combo-pending CREATED order: the debited 30 must be credited back.
        Map<String, Object> cancelData = new HashMap<>();
        cancelData.put("orderId", orderId);
        ApiResponse<?> rCancel = rpc(GraphQLOperationType.mutation, "LitemallOrder__cancel", cancelData);
        assertEquals(0, rCancel.getStatus(), "cancel failed: " + rCancel);

        assertEquals(_AppMallDaoConstants.ORDER_STATUS_CANCEL, reloadOrder(orderId).getOrderStatus(),
                "order must advance to CANCEL(102)");

        // Wallet restored to 500 (470 + 30 credit-back).
        assertEquals(0, walletBiz.getMyWallet(ctx()).getBalance().compareTo(new BigDecimal("500")),
                "wallet must be restored to 500 after combo cancel credit-back");

        // A REFUND / sourceType=order-refund flow must exist tying the credit-back to this order.
        LitemallOrder order = reloadOrder(orderId);
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallWalletFlow.PROP_NAME_changeType,
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_REFUND));
        List<LitemallWalletFlow> flows = flowBiz.findList(q, null, ctx());
        boolean hasComboRefundFlow = flows.stream().anyMatch(f ->
                "order-refund".equals(f.getSourceType())
                        && order.getOrderSn().equals(f.getSourceId()));
        assertTrue(hasComboRefundFlow, "a REFUND/order-refund flow tied to the orderSn must exist");
    }

    @Test
    public void testCancelExpiredOrdersCreditsComboWalletBack() {
        seedWallet(USER_ID, new BigDecimal("500"), "seed-combo-10");
        String orderId = createOrderForUser(USER_ID, new BigDecimal("99"));

        // Combo: deduct 30, wallet 500 -> 470.
        ApiResponse<?> rCombo = rpc(GraphQLOperationType.mutation, "LitemallOrder__payWithCombo",
                comboData(orderId, new BigDecimal("30")));
        assertEquals(0, rCombo.getStatus(), "combo setup failed: " + rCombo);

        // Force the addTime into the past so cancelExpiredOrders picks it up (timeout 0 minutes).
        // Use the GraphQL update path so the change runs inside an ORM session (direct dao update
        // outside a session is rejected by the platform).
        Map<String, Object> updateData = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();
        fields.put("id", orderId);
        fields.put("addTime", java.time.LocalDateTime.now().minusMinutes(30).toString());
        updateData.put("data", fields);
        ApiResponse<?> rUp = rpc(GraphQLOperationType.mutation, "LitemallOrder__update", updateData);
        assertEquals(0, rUp.getStatus(), "backdate addTime failed: " + rUp);

        Map<String, Object> expireData = new HashMap<>();
        expireData.put("timeoutMinutes", 0);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallOrder__cancelExpiredOrders", expireData);
        assertEquals(0, r.getStatus(), "cancelExpiredOrders failed: " + r);

        assertEquals(_AppMallDaoConstants.ORDER_STATUS_AUTO_CANCEL, reloadOrder(orderId).getOrderStatus(),
                "order must advance to AUTO_CANCEL(103)");
        // Wallet restored to 500 (470 + 30 credit-back).
        assertEquals(0, walletBiz.getMyWallet(ctx()).getBalance().compareTo(new BigDecimal("500")),
                "wallet must be restored to 500 after combo auto-cancel credit-back");
    }
}
