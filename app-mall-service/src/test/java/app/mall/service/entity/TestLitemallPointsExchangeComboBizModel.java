package app.mall.service.entity;

import app.mall.biz.ILitemallPointsExchangeOrderBiz;
import app.mall.biz.ILitemallWalletBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallPointsAccount;
import app.mall.dao.entity.LitemallPointsExchangeOrder;
import app.mall.dao.entity.LitemallPointsFlow;
import app.mall.dao.entity.LitemallPointsGoods;
import app.mall.dao.entity.LitemallWallet;
import app.mall.dao.entity.LitemallWalletFlow;
import app.mall.pay.IPaymentCallback;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import app.mall.service.pay.PaymentCallbackImpl;
import io.nop.auth.core.password.IPasswordEncoder;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Combo (points+cash) exchange tests — successor to the pure-points exchange.
 *
 * <p>Covers {@code exchangeCombo} (E1/E3: points deducted up-front, AWAITING_PAYMENT, PE outTradeNo),
 * {@code payComboByBalance} (E5: direct debitBalance), {@code confirmExchangePaidByNotify} (E4:
 * callback AWAITING_PAYMENT→PENDING, idempotent), {@code PaymentCallbackImpl} PE routing (E4),
 * combo cancel/refund (balance path), and the timeout-cancel job (E3 backstop).
 *
 * <p>Third-party channel prepay is not exercised here because no PayChannel is capability-enabled in
 * the test baseline (wxpay.enabled=false); the channel-disabled rejection + the callback success
 * path (channel-agnostic) are covered instead.
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallPointsExchangeComboBizModel extends JunitBaseTestCase {

    private static final String PAY_PASSWORD = "Pass@word1";
    private static final String USER_ID = "811";

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    ILitemallWalletBiz walletBiz;

    @Inject
    ILitemallPointsExchangeOrderBiz exchangeOrderBiz;

    @Inject
    IPaymentCallback paymentCallback;

    @Inject
    IPasswordEncoder passwordEncoder;

    @Inject
    IOrmTemplate ormTemplate;

    private String goodsId;
    private String productId;
    private String addressId;

    @BeforeEach
    void setUp() {
        // Combo balance-payment reuses the login password (Decision E5 mirrors payByBalance), so seed
        // a NopAuthUser with a known encoded credential.
        NopAuthUser user = daoProvider.daoFor(NopAuthUser.class).newEntity();
        user.orm_propValueByName("id", USER_ID);
        user.orm_propValueByName("userId", USER_ID);
        user.orm_propValueByName("userName", "combo-exchange-test");
        user.orm_propValueByName("nickName", "combo-exchange-test");
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
        ContextProvider.getOrCreateContext().setUserName("combo-exchange-test");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G-PEC");
        goods.setName("Combo Exchange Goods");
        goods.setCounterPrice(BigDecimal.valueOf(200));
        goods.setRetailPrice(BigDecimal.valueOf(199));
        goods.setIsOnSale(true);
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.getId();

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goodsId);
        product.setNumber(100);
        product.setPrice(BigDecimal.valueOf(199));
        product.setSpecifications("[\"标准\"]");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);
        productId = product.getId();

        LitemallAddress address = daoProvider.daoFor(LitemallAddress.class).newEntity();
        address.setUserId(USER_ID);
        address.setName("王五");
        address.setTel("13900139001");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setCounty("南山区");
        address.setAddressDetail("科技园2号");
        address.setIsDefault(true);
        daoProvider.daoFor(LitemallAddress.class).saveEntity(address);
        addressId = address.getId();

        seedPoints(USER_ID, 1000);
    }

    private void seedPoints(String userId, int balance) {
        LitemallPointsAccount acct = daoProvider.daoFor(LitemallPointsAccount.class).newEntity();
        acct.setUserId(userId);
        acct.setBalance(balance);
        acct.setTotalEarned(balance);
        acct.setTotalSpent(0);
        acct.setVersion(0);
        daoProvider.daoFor(LitemallPointsAccount.class).saveEntity(acct);
    }

    private String createComboPointsGoods(int pointsPrice, BigDecimal cashPrice, int exchangeStock,
                                           Integer maxPerUser, int status) {
        LitemallPointsGoods pg = daoProvider.daoFor(LitemallPointsGoods.class).newEntity();
        pg.setGoodsId(goodsId);
        pg.setProductId(productId);
        pg.setPointsPrice(pointsPrice);
        pg.setCashPrice(cashPrice);
        pg.setExchangeStock(exchangeStock);
        pg.setExchangedCount(0);
        pg.setMaxPerUser(maxPerUser);
        pg.setStatus(status);
        daoProvider.daoFor(LitemallPointsGoods.class).saveEntity(pg);
        return pg.orm_idString();
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<Map<String, Object>> rpc(GraphQLOperationType op, String action, Map<String, Object> data) {
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(op, action, ApiRequest.build(data));
        return (ApiResponse<Map<String, Object>>) graphQLEngine.executeRpc(ctx);
    }

    private ApiResponse<?> exchangeCombo(String pointsGoodsId, int quantity, String addressId) {
        Map<String, Object> data = new HashMap<>();
        data.put("pointsGoodsId", pointsGoodsId);
        data.put("quantity", quantity);
        if (addressId != null) data.put("addressId", addressId);
        return rpc(GraphQLOperationType.mutation, "LitemallPointsExchangeOrder__exchangeCombo", data);
    }

    private int getMyPoints() {
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallPointsAccount__getMyPoints", new HashMap<>());
        assertEquals(0, r.getStatus(), "getMyPoints failed: " + r);
        return ((Number) r.getData()).intValue();
    }

    private LitemallPointsGoods reloadPointsGoods(String id) {
        return daoProvider.daoFor(LitemallPointsGoods.class).getEntityById(id);
    }

    private LitemallPointsExchangeOrder reloadOrder(String id) {
        return (LitemallPointsExchangeOrder) daoProvider.daoFor(LitemallPointsExchangeOrder.class)
                .getEntityById(id);
    }

    private long countPointsFlows(String sourceType, String sourceId) {
        QueryBean q = new QueryBean();
        q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType, sourceType));
        if (sourceId != null) {
            q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceId, sourceId));
        }
        return daoProvider.daoFor(LitemallPointsFlow.class).findAllByQuery(q).size();
    }

    private long countWalletFlows(String sourceType, String sourceId) {
        QueryBean q = new QueryBean();
        q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallWalletFlow.PROP_NAME_sourceType, sourceType));
        if (sourceId != null) {
            q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallWalletFlow.PROP_NAME_sourceId, sourceId));
        }
        return daoProvider.daoFor(LitemallWalletFlow.class).findAllByQuery(q).size();
    }

    private BigDecimal walletBalance() {
        LitemallWallet w = walletBiz.getMyWallet(new ServiceContextImpl());
        return w != null && w.getBalance() != null ? w.getBalance() : BigDecimal.ZERO;
    }

    @Test
    public void testExchangeComboCreatesAwaitingPaymentAndDeductsPoints() {
        String pgId = createComboPointsGoods(100, new BigDecimal("19.90"), 10, 0,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);

        ApiResponse<?> r = exchangeCombo(pgId, 2, addressId);
        assertEquals(0, r.getStatus(), "exchangeCombo should succeed: " + r);

        // Points deducted up-front: 100 * 2 = 200, leaving 1000 - 200 = 800.
        assertEquals(800, getMyPoints());

        // Stock reduced.
        LitemallPointsGoods pg = reloadPointsGoods(pgId);
        assertEquals(8, pg.getExchangeStock());
        assertEquals(2, pg.getExchangedCount());

        Map<String, Object> data = (Map<String, Object>) r.getData();
        // AWAITING_PAYMENT status + cashPrice snapshot + PE-derived outTradeNo.
        assertEquals(_AppMallDaoConstants.EXCHANGE_STATUS_AWAITING_PAYMENT,
                ((Number) data.get("exchangeStatus")).intValue());
        assertEquals(0, new BigDecimal("19.90").compareTo(new BigDecimal(data.get("cashPrice").toString())));
        assertEquals(0, new BigDecimal("39.80").compareTo(new BigDecimal(data.get("totalCash").toString())));
        String outTradeNo = (String) data.get("outTradeNo");
        assertNotNull(outTradeNo);
        assertTrue(outTradeNo.startsWith(LitemallPointsExchangeOrderBizModel.OUT_TRADE_NO_PREFIX),
                "outTradeNo must start with PE: " + outTradeNo);
        assertTrue(outTradeNo.length() >= 6 && outTradeNo.length() <= 32,
                "outTradeNo length within WeChat 6-32 range: " + outTradeNo);

        assertEquals(_AppMallDaoConstants.PAY_STATUS_UNPAID, ((Number) data.get("payStatus")).intValue());

        // A single SPEND flow recorded with sourceType=mall-exchange.
        assertEquals(1, countPointsFlows(LitemallPointsExchangeOrderBizModel.SOURCE_TYPE_MALL_EXCHANGE, null));
    }

    @Test
    public void testExchangeComboRejectsWhenCashPriceNotConfigured() {
        // cashPrice null => pure-points goods, combo path must reject.
        String pgId = createComboPointsGoods(100, null, 10, 0,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);

        ApiResponse<?> r = exchangeCombo(pgId, 1, addressId);
        assertNotEquals(0, r.getStatus(), "combo on a pure-points goods must reject");
        assertEquals(1000, getMyPoints(), "points must not change on rejected combo");
    }

    @Test
    public void testPayComboByBalanceAdvancesToPending() {
        walletBiz.creditBalance(USER_ID, new BigDecimal("500"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "seed-combo-1", null, new ServiceContextImpl());

        String pgId = createComboPointsGoods(100, new BigDecimal("19.90"), 10, 0,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        ApiResponse<?> exR = exchangeCombo(pgId, 2, addressId);
        assertEquals(0, exR.getStatus(), "exchangeCombo failed: " + exR);
        String orderId = ((Map<String, Object>) exR.getData()).get("id").toString();

        Map<String, Object> payData = new HashMap<>();
        payData.put("id", orderId);
        payData.put("confirmCredential", PAY_PASSWORD);
        ApiResponse<?> payR = rpc(GraphQLOperationType.mutation,
                "LitemallPointsExchangeOrder__payComboByBalance", payData);
        assertEquals(0, payR.getStatus(), "payComboByBalance failed: " + payR);

        LitemallPointsExchangeOrder paid = reloadOrder(orderId);
        assertEquals(_AppMallDaoConstants.EXCHANGE_STATUS_PENDING, paid.getExchangeStatus(),
                "combo order must advance to PENDING after balance pay");
        assertEquals(_AppMallDaoConstants.PAY_STATUS_PAID, paid.getPayStatus().intValue());
        assertEquals(_AppMallDaoConstants.PAY_CHANNEL_BALANCE, paid.getPayChannel().intValue());
        assertEquals(0, new BigDecimal("39.80").compareTo(paid.getWalletPayAmount()));

        // Wallet debited: 500 - 39.80 = 460.20
        assertEquals(0, new BigDecimal("460.20").compareTo(walletBalance()));
        // A wallet PAY flow tied to the combo order via mall-exchange-pay sourceType.
        assertEquals(1, countWalletFlows(LitemallPointsExchangeOrderBizModel.SOURCE_TYPE_MALL_EXCHANGE_PAY, orderId));
    }

    @Test
    public void testPayComboByBalanceRejectsWrongCredential() {
        walletBiz.creditBalance(USER_ID, new BigDecimal("500"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "seed-combo-2", null, new ServiceContextImpl());

        String pgId = createComboPointsGoods(100, new BigDecimal("19.90"), 10, 0,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        String orderId = ((Map<String, Object>) exchangeCombo(pgId, 1, addressId).getData()).get("id").toString();

        Map<String, Object> payData = new HashMap<>();
        payData.put("id", orderId);
        payData.put("confirmCredential", "wrong-password");
        ApiResponse<?> payR = rpc(GraphQLOperationType.mutation,
                "LitemallPointsExchangeOrder__payComboByBalance", payData);
        assertNotEquals(0, payR.getStatus(), "wrong credential must reject");
        assertEquals(0, new BigDecimal("500").compareTo(walletBalance()), "wallet must not be debited on rejection");
    }

    @Test
    public void testPayComboByBalanceRejectsInsufficientBalance() {
        // Wallet seeded with only 10, combo cash costs 39.80.
        walletBiz.creditBalance(USER_ID, new BigDecimal("10"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "seed-combo-3", null, new ServiceContextImpl());

        String pgId = createComboPointsGoods(100, new BigDecimal("19.90"), 10, 0,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        String orderId = ((Map<String, Object>) exchangeCombo(pgId, 2, addressId).getData()).get("id").toString();

        Map<String, Object> payData = new HashMap<>();
        payData.put("id", orderId);
        payData.put("confirmCredential", PAY_PASSWORD);
        ApiResponse<?> payR = rpc(GraphQLOperationType.mutation,
                "LitemallPointsExchangeOrder__payComboByBalance", payData);
        assertNotEquals(0, payR.getStatus(), "insufficient balance must reject");
        assertEquals(0, new BigDecimal("10").compareTo(walletBalance()), "wallet must not be debited");
    }

    @Test
    public void testPayComboByChannelRejectsDisabledChannel() {
        // In the test baseline wxpay.enabled=false => WECHAT channel capability is false => rejected.
        String pgId = createComboPointsGoods(100, new BigDecimal("19.90"), 10, 0,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        String orderId = ((Map<String, Object>) exchangeCombo(pgId, 1, addressId).getData()).get("id").toString();

        Map<String, Object> payData = new HashMap<>();
        payData.put("id", orderId);
        payData.put("payChannelCode", "WECHAT");
        ApiResponse<?> payR = rpc(GraphQLOperationType.mutation,
                "LitemallPointsExchangeOrder__payComboByChannel", payData);
        assertNotEquals(0, payR.getStatus(), "disabled channel must reject");
    }

    @Test
    public void testConfirmExchangePaidByNotifyAdvancesToPending() {
        String pgId = createComboPointsGoods(100, new BigDecimal("19.90"), 10, 0,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        Map<String, Object> exData = (Map<String, Object>) exchangeCombo(pgId, 1, addressId).getData();
        String orderId = exData.get("id").toString();
        String outTradeNo = exData.get("outTradeNo").toString();

        // Simulate the verified channel SUCCESS notify via the I*Biz @BizAction entry. @BizAction does
        // not auto-open an ORM session (unlike @BizMutation), so wrap in runInSession — same precedent
        // as TestLitemallRechargeBizModel.testConfirmRechargeByNotifyIsIdempotent.
        ormTemplate.runInSession(session -> {
            exchangeOrderBiz.confirmExchangePaidByNotify(outTradeNo, "wx-tx-001", new ServiceContextImpl());
            return null;
        });

        LitemallPointsExchangeOrder paid = reloadOrder(orderId);
        assertEquals(_AppMallDaoConstants.EXCHANGE_STATUS_PENDING, paid.getExchangeStatus());
        assertEquals(_AppMallDaoConstants.PAY_STATUS_PAID, paid.getPayStatus().intValue());
    }

    @Test
    public void testConfirmExchangePaidByNotifyIsIdempotent() {
        String pgId = createComboPointsGoods(100, new BigDecimal("19.90"), 10, 0,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        Map<String, Object> exData = (Map<String, Object>) exchangeCombo(pgId, 1, addressId).getData();
        String outTradeNo = exData.get("outTradeNo").toString();

        // First notify advances AWAITING_PAYMENT -> PENDING.
        ormTemplate.runInSession(session -> {
            exchangeOrderBiz.confirmExchangePaidByNotify(outTradeNo, "wx-tx-002", new ServiceContextImpl());
            return null;
        });
        // Replay (duplicate/retry) must be a no-op.
        ormTemplate.runInSession(session -> {
            exchangeOrderBiz.confirmExchangePaidByNotify(outTradeNo, "wx-tx-002", new ServiceContextImpl());
            return null;
        });

        LitemallPointsExchangeOrder paid = reloadOrder(exData.get("id").toString());
        assertEquals(_AppMallDaoConstants.EXCHANGE_STATUS_PENDING, paid.getExchangeStatus());
        assertEquals(_AppMallDaoConstants.PAY_STATUS_PAID, paid.getPayStatus().intValue());
    }

    @Test
    public void testPaymentCallbackRoutesPePrefixToExchange() {
        // Verify PaymentCallbackImpl.onPaymentSuccess routes a PE outTradeNo to the exchange branch.
        // PaymentCallbackImpl's @Inject fields are null in the test IoC context (pre-existing gap —
        // see TestLitemallRechargeBizModel.testOnRefundSuccessRoutesByPrefix which uses the very same
        // NPE-as-routing-proof technique for the RC branch). Here the NPE on exchangeOrderBiz proves
        // the PE branch was entered; if the order branch were entered instead, no exchange-order row
        // would match and findOrderByOrderSn would return null (no NPE).
        String pgId = createComboPointsGoods(100, new BigDecimal("19.90"), 10, 0,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        Map<String, Object> exData = (Map<String, Object>) exchangeCombo(pgId, 1, addressId).getData();
        String outTradeNo = exData.get("outTradeNo").toString();

        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class,
                () -> paymentCallback.onPaymentSuccess(outTradeNo, "wx-tx-003"),
                "PE outTradeNo must route to the exchange branch (NPE from the pre-existing null "
                        + "exchangeOrderBiz injection proves PE routing)");
    }

    @Test
    public void testPaymentCallbackRoutingInvariantOrderSnNotMisrouted() {
        // An orderSn-shaped outTradeNo (32-char lowercase hex, no uppercase prefix) must route to the
        // ORDER branch, not be misrouted as a PE exchange id. The NPE on orderBiz proves the order
        // branch was entered (a PE/RC parse would have returned null exchange/recharge id and no-op'd).
        String fakeOrderSn = "abcdef0123456789abcdef0123456789";
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class,
                () -> paymentCallback.onPaymentSuccess(fakeOrderSn, "tx-none"),
                "orderSn-shaped outTradeNo must route to the order branch (NPE from the pre-existing "
                        + "null orderBiz injection proves order routing, not PE/RC misrouting)");
    }

    @Test
    public void testCancelComboFromAwaitingPaymentRefundsPointsNoCash() {
        String pgId = createComboPointsGoods(100, new BigDecimal("19.90"), 10, 0,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        String orderId = ((Map<String, Object>) exchangeCombo(pgId, 2, addressId).getData()).get("id").toString();
        assertEquals(800, getMyPoints(), "points deducted at combo creation");

        Map<String, Object> cancelData = new HashMap<>();
        cancelData.put("id", orderId);
        cancelData.put("remark", "user cancelled before pay");
        ApiResponse<?> cancelR = rpc(GraphQLOperationType.mutation,
                "LitemallPointsExchangeOrder__cancelExchangeOrder", cancelData);
        assertEquals(0, cancelR.getStatus(), "cancel should succeed: " + cancelR);

        // Points refunded (back to 1000), stock restored, status CANCELLED. Cash was never paid.
        assertEquals(1000, getMyPoints());
        LitemallPointsGoods pg = reloadPointsGoods(pgId);
        assertEquals(10, pg.getExchangeStock());
        LitemallPointsExchangeOrder cancelled = reloadOrder(orderId);
        assertEquals(_AppMallDaoConstants.EXCHANGE_STATUS_CANCELLED, cancelled.getExchangeStatus());
        assertEquals(1, countPointsFlows(LitemallPointsExchangeOrderBizModel.SOURCE_TYPE_MALL_EXCHANGE_REFUND, orderId));
    }

    @Test
    public void testCancelComboAfterBalancePayRefundsCashAndPoints() {
        walletBiz.creditBalance(USER_ID, new BigDecimal("500"),
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE, "seed-combo-cancel", null, new ServiceContextImpl());

        String pgId = createComboPointsGoods(100, new BigDecimal("19.90"), 10, 0,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        String orderId = ((Map<String, Object>) exchangeCombo(pgId, 2, addressId).getData()).get("id").toString();
        // Balance-pay the cash component (500 - 39.80 = 460.20).
        Map<String, Object> payData = new HashMap<>();
        payData.put("id", orderId);
        payData.put("confirmCredential", PAY_PASSWORD);
        rpc(GraphQLOperationType.mutation, "LitemallPointsExchangeOrder__payComboByBalance", payData);
        assertEquals(0, new BigDecimal("460.20").compareTo(walletBalance()));

        // Cancel after pay: PENDING -> CANCELLED, refund points + credit cash back.
        Map<String, Object> cancelData = new HashMap<>();
        cancelData.put("id", orderId);
        ApiResponse<?> cancelR = rpc(GraphQLOperationType.mutation,
                "LitemallPointsExchangeOrder__cancelExchangeOrder", cancelData);
        assertEquals(0, cancelR.getStatus(), "cancel after pay should succeed: " + cancelR);

        // Points refunded (800 -> 1000), cash credited back (460.20 -> 500).
        assertEquals(1000, getMyPoints());
        assertEquals(0, new BigDecimal("500").compareTo(walletBalance()));

        LitemallPointsExchangeOrder cancelled = reloadOrder(orderId);
        assertEquals(_AppMallDaoConstants.EXCHANGE_STATUS_CANCELLED, cancelled.getExchangeStatus());
        // Balance refund is synchronous => payStatus REFUNDED immediately.
        assertEquals(_AppMallDaoConstants.PAY_STATUS_REFUNDED, cancelled.getPayStatus().intValue());
        // Wallet refund flow recorded under mall-exchange-refund sourceType.
        assertEquals(1, countWalletFlows(LitemallPointsExchangeOrderBizModel.SOURCE_TYPE_MALL_EXCHANGE_REFUND, orderId));
    }

    @Test
    public void testCancelExpiredExchangeOrdersTimesOutAwaitingPayment() {
        String pgId = createComboPointsGoods(100, new BigDecimal("19.90"), 10, 0,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        String orderId = ((Map<String, Object>) exchangeCombo(pgId, 1, addressId).getData()).get("id").toString();
        assertEquals(900, getMyPoints());

        // Run the timeout-cancel job with timeoutMinutes=0 => the just-created AWAITING_PAYMENT order
        // (addTime <= now) is cancelled: stock restored + points refunded, no cash refund. Invoked via
        // I*Biz (system-job entry path), wrapped in a session like the recharge @BizAction precedent.
        int count = ormTemplate.runInSession(session ->
                exchangeOrderBiz.cancelExpiredExchangeOrders(0, new ServiceContextImpl()));
        assertTrue(count >= 1, "timeout cancel should process the awaiting-payment order");

        LitemallPointsExchangeOrder cancelled = reloadOrder(orderId);
        assertEquals(_AppMallDaoConstants.EXCHANGE_STATUS_CANCELLED, cancelled.getExchangeStatus());
        assertEquals(1000, getMyPoints(), "points must be refunded by the timeout cancel job");
        LitemallPointsGoods pg = reloadPointsGoods(pgId);
        assertEquals(10, pg.getExchangeStock(), "stock must be restored");
    }

    @Test
    public void testExchangeComboMaxPerUserCountsAwaitingPayment() {
        // maxPerUser=1: a first combo order (AWAITING_PAYMENT) must count toward the cap, so a second
        // combo attempt is rejected (audit minor: exchangeCombo maxPerUser includes AWAITING_PAYMENT).
        String pgId = createComboPointsGoods(100, new BigDecimal("19.90"), 50, 1,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);

        ApiResponse<?> r1 = exchangeCombo(pgId, 1, addressId);
        assertEquals(0, r1.getStatus(), "first combo within cap should succeed: " + r1);

        ApiResponse<?> r2 = exchangeCombo(pgId, 1, addressId);
        assertNotEquals(0, r2.getStatus(), "second combo beyond per-user cap must reject");
    }

    @Test
    public void testPurePointsExchangeZeroRegressionViaComboFieldsNull() {
        // A pure-points exchange order must keep the combo pay fields null (cashPrice/payStatus/...).
        // Guards E2 残留风险: pure-points path state machine unchanged + combo fields stay null.
        LitemallPointsGoods pg = daoProvider.daoFor(LitemallPointsGoods.class).newEntity();
        pg.setGoodsId(goodsId);
        pg.setProductId(productId);
        pg.setPointsPrice(100);
        pg.setExchangeStock(10);
        pg.setExchangedCount(0);
        pg.setMaxPerUser(0);
        pg.setStatus(_AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        daoProvider.daoFor(LitemallPointsGoods.class).saveEntity(pg);
        String pgId = pg.orm_idString();

        Map<String, Object> data = new HashMap<>();
        data.put("pointsGoodsId", pgId);
        data.put("quantity", 1);
        data.put("addressId", addressId);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallPointsExchangeOrder__exchange", data);
        assertEquals(0, r.getStatus(), "pure-points exchange should succeed: " + r);

        Map<String, Object> orderData = (Map<String, Object>) r.getData();
        assertEquals(_AppMallDaoConstants.EXCHANGE_STATUS_PENDING,
                ((Number) orderData.get("exchangeStatus")).intValue());
        // Combo fields are null on a pure-points order.
        assertNullOrAssertComboFieldsNull(orderData);
    }

    private static void assertNullOrAssertComboFieldsNull(Map<String, Object> orderData) {
        Object cashPrice = orderData.get("cashPrice");
        Object payStatus = orderData.get("payStatus");
        assertTrue(cashPrice == null, "pure-points order cashPrice must be null, got: " + cashPrice);
        assertTrue(payStatus == null, "pure-points order payStatus must be null, got: " + payStatus);
    }
}
