package app.mall.service.scheduler;

import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallFlashSaleSessionBiz;
import app.mall.biz.ILitemallGrouponBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallOrderGoodsBiz;
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
}
