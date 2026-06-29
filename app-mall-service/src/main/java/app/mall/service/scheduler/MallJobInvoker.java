package app.mall.service.scheduler;

import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallFlashSaleSessionBiz;
import app.mall.biz.ILitemallGrouponBiz;
import app.mall.biz.ILitemallMemberLevelBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallOrderGoodsBiz;
import app.mall.biz.ILitemallPinTuanActivityBiz;
import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.biz.ILitemallPointsExchangeOrderBiz;
import app.mall.biz.ILitemallSystemBiz;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.core.context.ServiceContextImpl;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class MallJobInvoker {
    static final Logger LOG = LoggerFactory.getLogger(MallJobInvoker.class);

    private static final int ORDER_CANCEL_TIMEOUT_MINUTES = 30;
    private static final int ORDER_CONFIRM_TIMEOUT_MINUTES = 10080;
    private static final int COMMENT_WINDOW_TIMEOUT_DAYS = 7;
    private static final int EXCHANGE_CANCEL_TIMEOUT_MINUTES = 30;
    private static final int PICKUP_TIMEOUT_DEFAULT_DAYS = 14;

    @Inject
    ILitemallOrderBiz orderBiz;

    @Inject
    ILitemallCouponUserBiz couponUserBiz;

    @Inject
    ILitemallGrouponBiz grouponBiz;

    @Inject
    ILitemallOrderGoodsBiz orderGoodsBiz;

    @Inject
    ILitemallFlashSaleSessionBiz flashSaleSessionBiz;

    @Inject
    ILitemallPinTuanActivityBiz pinTuanActivityBiz;

    @Inject
    ILitemallMemberLevelBiz memberLevelBiz;

    @Inject
    ILitemallPointsExchangeOrderBiz exchangeOrderBiz;

    @Inject
    ILitemallPointsAccountBiz pointsAccountBiz;

    @Inject
    ILitemallSystemBiz systemBiz;

    public void cancelExpiredOrders() {
        IServiceContext context = new ServiceContextImpl();
        int count = orderBiz.cancelExpiredOrders(ORDER_CANCEL_TIMEOUT_MINUTES, context);
        LOG.info("mall-job cancelExpiredOrders finished, affected={}", count);
    }

    public void confirmExpiredOrders() {
        IServiceContext context = new ServiceContextImpl();
        int count = orderBiz.confirmExpiredOrders(ORDER_CONFIRM_TIMEOUT_MINUTES, context);
        LOG.info("mall-job confirmExpiredOrders finished, affected={}", count);
    }

    public void expireCoupons() {
        IServiceContext context = new ServiceContextImpl();
        int count = couponUserBiz.expireCoupons(context);
        LOG.info("mall-job expireCoupons finished, affected={}", count);
    }

    public void expireGroupons() {
        IServiceContext context = new ServiceContextImpl();
        int count = grouponBiz.expireGroupons(context);
        LOG.info("mall-job expireGroupons finished, affected={}", count);
    }

    public void expireCommentWindow() {
        IServiceContext context = new ServiceContextImpl();
        int count = orderGoodsBiz.expireCommentWindow(COMMENT_WINDOW_TIMEOUT_DAYS, context);
        LOG.info("mall-job expireCommentWindow finished, affected={}", count);
    }

    // P24 flash sale: flip sessionStatus 0 -> 1 -> 2 by sessionStart/sessionEnd. See
    // docs/design/marketing-and-promotions.md 秒杀章节 "场次状态切换".
    public void switchFlashSaleSessions() {
        IServiceContext context = new ServiceContextImpl();
        int count = flashSaleSessionBiz.switchFlashSaleSessions(context);
        LOG.info("mall-job switchFlashSaleSessions finished, affected={}", count);
    }

    // P25 pin-tuan: expire ACTIVE groups past their expireTime -> FAILED + refund all members.
    // See docs/design/marketing-and-promotions.md 拼团章节 "超时失败 + 退款语义".
    public void expirePinTuans() {
        IServiceContext context = new ServiceContextImpl();
        int count = pinTuanActivityBiz.expirePinTuans(context);
        LOG.info("mall-job expirePinTuans finished, affected={}", count);
    }

    // Member birthday benefit dispatch: scans users whose birthday matches today (month-day)
    // and dispatches the configured birthday coupon. Idempotent per (userId, year).
    // See docs/design/user-and-address.md 会员等级权益 "生日礼包".
    public void dispatchBirthdayCoupons() {
        IServiceContext context = new ServiceContextImpl();
        int count = memberLevelBiz.dispatchBirthdayCoupons(context);
        LOG.info("mall-job dispatchBirthdayCoupons finished, affected={}", count);
    }

    // Combo-exchange (points+cash) AWAITING_PAYMENT timeout cancel (E3 backstop): scans combo orders
    // past the payment timeout and cancels them (restore stock + refund points; cash never paid).
    // See docs/design/marketing-and-promotions.md 「组合兑换流程」超时取消.
    public void cancelExpiredExchangeOrders() {
        IServiceContext context = new ServiceContextImpl();
        int count = exchangeOrderBiz.cancelExpiredExchangeOrders(EXCHANGE_CANCEL_TIMEOUT_MINUTES, context);
        LOG.info("mall-job cancelExpiredExchangeOrders finished, affected={}", count);
    }

    // Points validity auto-expiry: scans batches whose expireTime has passed, decrements balance and
    // writes an EXPIRE flow. Idempotent + concurrent-safe (PointsAccount optimistic lock). Hourly.
    // See docs/design/wallet-and-assets.md 积分有效期.
    public void expirePoints() {
        IServiceContext context = new ServiceContextImpl();
        int count = pointsAccountBiz.expirePoints(context);
        LOG.info("mall-job expirePoints finished, affected={}", count);
    }

    // Points expiry pre-warning push (successor of getMyPointsExpiryHint): scan batches expiring
    // within N days (mall_points_expiry_remind_days, default 3), aggregate per user, push one
    // SYSTEM 站内信 per user. Idempotent per (userId, title, today). Daily.
    // See docs/design/wallet-and-assets.md 积分有效期 过期预警.
    public void sendPointsExpiryReminders() {
        IServiceContext context = new ServiceContextImpl();
        int count = pointsAccountBiz.sendPointsExpiryReminders(context);
        LOG.info("mall-job sendPointsExpiryReminders finished, pushed={}", count);
    }

    // Pickup-order auto-timeout cancel + refund (successor of P31 deferred「已支付未自提订单自动超时取消/退款」):
    // scan paid(201) PICKUP orders past the configurable pickup timeout (mall_pickup_timeout_days,
    // default 14), CAS 201→203 + route refund by payChannel + 还库/还券/还积分/释放满减/通知/日志.
    // See docs/design/order-and-cart.md 自提核销「已支付未自提订单生命周期」.
    public void cancelExpiredPickupOrders() {
        IServiceContext context = new ServiceContextImpl();
        int timeoutDays = resolvePickupTimeoutDays(context);
        int count = orderBiz.cancelExpiredPickupOrders(timeoutDays, context);
        LOG.info("mall-job cancelExpiredPickupOrders finished, timeoutDays={}, affected={}",
                timeoutDays, count);
    }

    private int resolvePickupTimeoutDays(IServiceContext context) {
        // Config-driven (mirrors sendPointsExpiryReminders pattern). Returns default when key is
        // absent, blank, or non-positive/non-numeric.
        String raw = systemBiz.getConfig("mall_pickup_timeout_days", context);
        if (StringHelper.isBlank(raw)) {
            return PICKUP_TIMEOUT_DEFAULT_DAYS;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : PICKUP_TIMEOUT_DEFAULT_DAYS;
        } catch (NumberFormatException e) {
            return PICKUP_TIMEOUT_DEFAULT_DAYS;
        }
    }
}
