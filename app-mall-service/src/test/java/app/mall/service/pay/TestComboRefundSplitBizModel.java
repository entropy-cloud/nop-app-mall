package app.mall.service.pay;

import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallWalletBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallWallet;
import app.mall.wx.WxPayServiceImpl;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.core.context.ServiceContextImpl;
import io.nop.dao.api.IDaoProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 2 tests: combo-aware refund split ({@code refundComboAware}) — the资金安全 helper shared by
 * the five refund sites (aftersale refund / confirmReturnReceived, pickup-timeout non-BALANCE,
 * groupon fail, pintuan fail).
 *
 * <p>Covers the ratio split (Decision D3): for a combo order ({@code walletPayAmount>0} and
 * {@code actualPrice>0}) {@code walletPortion = refundAmount × walletPayAmount / actualPrice} is
 * credited back to the wallet and {@code channelPortion = refundAmount − walletPortion} (remainder归
 * 通道) refunded via payService; non-combo orders fall back to the legacy single-channel refund with
 * zero wallet credit. Whole-order, partial, non-combo, and channel-failure (wallet still credited)
 * paths are all exercised.
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestComboRefundSplitBizModel extends JunitBaseTestCase {

    private static final String USER_ID = "30442";

    @Inject
    ILitemallOrderBiz orderBiz;

    @Inject
    ILitemallWalletBiz walletBiz;

    @Inject
    IDaoProvider daoProvider;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId(USER_ID);
        ContextProvider.getOrCreateContext().setUserName("combo-refund-test");
    }

    private ServiceContextImpl ctx() {
        return new ServiceContextImpl();
    }

    private void seedWallet(String userId, BigDecimal amount, String seedId) {
        walletBiz.creditBalance(userId, amount,
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                app.mall.service.entity.LitemallWalletBizModel.SOURCE_TYPE_RECHARGE,
                seedId, null, ctx());
    }

    /**
     * Create a PAID(201) order with the given combo fields. payChannel=WECHAT + walletPayAmount>0
     * simulates a combo order that completed via the补差 async notify.
     */
    private LitemallOrder createPaidComboOrder(BigDecimal actualPrice, BigDecimal walletPayAmount,
                                                Integer payChannel) {
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).newEntity();
        order.setUserId(USER_ID);
        order.setOrderSn("COMBOREF-" + System.nanoTime());
        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_PAY);
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
        order.setPayChannel(payChannel);
        order.setWalletPayAmount(walletPayAmount);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(order);
        return order;
    }

    private BigDecimal walletBalance() {
        LitemallWallet wallet = walletBiz.getMyWallet(ctx());
        return wallet != null && wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;
    }

    @Test
    public void testRefundComboAwareWholeOrderComboCreditsWalletAndChannel() {
        // Combo order: actualPrice=99, walletPayAmount=30, payChannel=WECHAT. Whole-order refund of 99.
        // walletPortion = 99 × 30 / 99 = 30 (credited to wallet); channelPortion = 99 − 30 = 69.
        seedWallet(USER_ID, new BigDecimal("100"), "seed-rs-1");
        BigDecimal balanceBefore = walletBalance();
        LitemallOrder order = createPaidComboOrder(new BigDecimal("99"), new BigDecimal("30"),
                _AppMallDaoConstants.PAY_CHANNEL_WECHAT);

        boolean ok = orderBiz.refundComboAware(order, new BigDecimal("99"), ctx());

        assertTrue(ok, "channel portion refund should succeed (mock wxpay)");
        // Wallet credited exactly the walletPayAmount (30) on a whole-order refund.
        assertEquals(0, walletBalance().compareTo(balanceBefore.add(new BigDecimal("30"))),
                "wallet must be credited the wallet portion (30) on whole-order combo refund");
    }

    @Test
    public void testRefundComboAwarePartialComboSplitsByRatio() {
        // Combo order: actualPrice=99, walletPayAmount=30. Partial refund of 33.
        // walletPortion = 33 × 30 / 99 = 10 (scale=2 HALF_UP); channelPortion = 33 − 10 = 23.
        seedWallet(USER_ID, new BigDecimal("100"), "seed-rs-2");
        BigDecimal balanceBefore = walletBalance();
        LitemallOrder order = createPaidComboOrder(new BigDecimal("99"), new BigDecimal("30"),
                _AppMallDaoConstants.PAY_CHANNEL_WECHAT);

        boolean ok = orderBiz.refundComboAware(order, new BigDecimal("33"), ctx());

        assertTrue(ok, "channel portion refund should succeed");
        // Wallet credited the ratio-split wallet portion (10).
        assertEquals(0, walletBalance().compareTo(balanceBefore.add(new BigDecimal("10"))),
                "wallet must be credited the ratio-split portion (10) on partial combo refund");
    }

    @Test
    public void testRefundComboAwareNonComboLegacyNoWalletCredit() {
        // Non-combo order: walletPayAmount=0, payChannel=WECHAT. Legacy single-channel refund.
        // Wallet must NOT be credited (no balance portion to return).
        seedWallet(USER_ID, new BigDecimal("100"), "seed-rs-3");
        BigDecimal balanceBefore = walletBalance();
        LitemallOrder order = createPaidComboOrder(new BigDecimal("99"), BigDecimal.ZERO,
                _AppMallDaoConstants.PAY_CHANNEL_WECHAT);

        boolean ok = orderBiz.refundComboAware(order, new BigDecimal("99"), ctx());

        assertTrue(ok, "legacy single-channel refund should succeed");
        assertEquals(0, walletBalance().compareTo(balanceBefore),
                "wallet must NOT be credited on a non-combo order (zero regression)");
    }

    @Test
    public void testRefundComboAwareBalanceChannelOrderNoDoubleCredit() {
        // Full-balance order: walletPayAmount=99 == actualPrice=99, payChannel=BALANCE.
        // The ratio split recovers the full 99 into the wallet and channelPortion=0 (no channel call).
        seedWallet(USER_ID, new BigDecimal("100"), "seed-rs-4");
        BigDecimal balanceBefore = walletBalance();
        LitemallOrder order = createPaidComboOrder(new BigDecimal("99"), new BigDecimal("99"),
                _AppMallDaoConstants.PAY_CHANNEL_BALANCE);

        boolean ok = orderBiz.refundComboAware(order, new BigDecimal("99"), ctx());

        assertTrue(ok, "pure-wallet refund (no channel portion) should report success");
        assertEquals(0, walletBalance().compareTo(balanceBefore.add(new BigDecimal("99"))),
                "wallet must be credited the full amount when channelPortion==0");
    }

    @Test
    public void testRefundComboAwareWalletCreditedEvenWhenChannelFails() {
        // Combo order: when the channel refund fails, the wallet portion must STILL be credited
        // (the wallet credit happens before the channel call, so a channel failure does not lose the
        // user's balance portion). refundComboAware returns false so the caller can throw/record.
        seedWallet(USER_ID, new BigDecimal("100"), "seed-rs-5");
        BigDecimal balanceBefore = walletBalance();
        LitemallOrder order = createPaidComboOrder(new BigDecimal("99"), new BigDecimal("30"),
                _AppMallDaoConstants.PAY_CHANNEL_WECHAT);

        WxPayServiceImpl.setForceRefundFailure(true);
        try {
            boolean ok = orderBiz.refundComboAware(order, new BigDecimal("99"), ctx());
            assertFalse(ok, "channel refund failure must return false");
        } finally {
            WxPayServiceImpl.setForceRefundFailure(false);
        }

        // Wallet credited the 30 even though the channel refund failed — proves the wallet credit
        // is independent of the channel outcome (资金安全: no wallet portion lost on channel failure).
        assertEquals(0, walletBalance().compareTo(balanceBefore.add(new BigDecimal("30"))),
                "wallet portion must be credited even when the channel refund fails");
    }
}
