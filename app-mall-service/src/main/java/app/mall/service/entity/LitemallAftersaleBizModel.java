package app.mall.service.entity;

import app.mall.biz.ILitemallAftersaleBiz;
import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.dao.AppMallDaoConstants;
import app.mall.dao.dto.AftersaleApplyRequest;
import app.mall.dao.entity.LitemallAftersale;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.manager.MallLogManager;
import app.mall.dao.mapper.LitemallGoodsProductMapper;
import app.mall.pay.PayRefundRequestBean;
import app.mall.pay.PayRefundResponseBean;
import app.mall.pay.PayService;
import app.mall.service.notification.MallNotificationService;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.biz.RequestBean;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.DateHelper;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static app.mall.service.AppMallErrors.ERR_AFTERSALE_AMOUNT_EXCEED;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_ALLOW_APPLY;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_ALLOW_CANCEL;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_ALLOW_REFUND;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_REFUND_FAILED;

@BizModel("LitemallAftersale")
public class LitemallAftersaleBizModel extends CrudBizModel<LitemallAftersale> implements ILitemallAftersaleBiz {

    @Inject
    MallNotificationService notificationService;

    @Inject
    PayService payService;

    @Inject
    MallLogManager logManager;

    @Inject
    ILitemallOrderBiz orderBiz;

    @Inject
    ILitemallCouponUserBiz couponUserBiz;

    @Inject
    LitemallGoodsProductMapper goodsProductMapper; // addStock: atomic SQL UPDATE for stock replenishment after refund

    public LitemallAftersaleBizModel() {
        setEntityName(LitemallAftersale.class.getName());
    }

    @BizMutation
    public void batchApprove(@Name("ids") Set<String> ids, IServiceContext context) {
        List<LitemallAftersale> list = batchGet(ids, false, context);

        for (LitemallAftersale entity : list) {
            int status = entity.getStatus();
            if (status != AppMallDaoConstants.AFTERSALE_STATUS_REQUEST) {
                continue;
            }
            entity.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_APPROVED);
            entity.setHandleTime(DateHelper.currentDateTime());

            entity.getOrder().setAftersaleStatus(entity.getStatus());
        }
    }

    @BizMutation
    public void batchReject(@Name("ids") Set<String> ids, IServiceContext context) {
        List<LitemallAftersale> list = batchGet(ids, false, context);

        for (LitemallAftersale entity : list) {
            int status = entity.getStatus();
            if (status != AppMallDaoConstants.AFTERSALE_STATUS_REQUEST) {
                continue;
            }

            entity.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_REJECT);
            entity.setHandleTime(DateHelper.currentDateTime());

            entity.getOrder().setAftersaleStatus(entity.getStatus());
        }
    }

    @BizMutation
    public void refund(@Name("id") String id, IServiceContext context) {
        LitemallAftersale entity = get(id, false, context);
        int status = entity.getStatus();
        if (status != AppMallDaoConstants.AFTERSALE_STATUS_APPROVED) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_REFUND);
        }

        LitemallOrder order = entity.getOrder();

        // Defensive amount recheck (apply() already validates, but status may have changed since)
        if (entity.getAmount() == null || entity.getAmount().signum() <= 0
                || entity.getAmount().compareTo(order.getActualPrice()) > 0) {
            throw new NopException(ERR_AFTERSALE_AMOUNT_EXCEED)
                    .param("aftersaleId", id)
                    .param("amount", entity.getAmount())
                    .param("actualPrice", order.getActualPrice());
        }

        PayRefundRequestBean wxPayRefundRequest = new PayRefundRequestBean();
        wxPayRefundRequest.setOutTradeNo(order.getOrderSn());
        wxPayRefundRequest.setOutRefundNo("refund_" + order.getOrderSn());
        wxPayRefundRequest.setTotalFee(order.getActualPrice());
        wxPayRefundRequest.setRefundFee(entity.getAmount());

        // payService.refund stays in-tx: external failure throws → tx rolls back, keeping DB consistent with no-refund
        PayRefundResponseBean refundResult = payService.refund(wxPayRefundRequest);
        if (!refundResult.isSuccess()) {
            throw new NopException(ERR_AFTERSALE_REFUND_FAILED)
                    .param("aftersaleId", id)
                    .param("orderSn", order.getOrderSn());
        }

        entity.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_REFUND);
        entity.setHandleTime(DateHelper.currentDateTime());
        entity.getOrder().setAftersaleStatus(entity.getStatus());

        // Order-level refund state: an unshipped (PAY) order is fully refunded → terminal REFUND_CONFIRM(203).
        // A received (CONFIRM/AUTO_CONFIRM) order keeps its fulfillment status; only aftersaleStatus reflects refund.
        boolean wasUnshipped = order.getOrderStatus() == AppMallDaoConstants.ORDER_STATUS_PAY;
        if (wasUnshipped) {
            order.setOrderStatus(AppMallDaoConstants.ORDER_STATUS_REFUND_CONFIRM);
            order.setEndTime(DateHelper.currentDateTime());
        }

        // Restock when goods are still in the warehouse (unshipped PAY order, regardless of aftersale type)
        // or when the user returns received goods (GOODS_REQUIRED). GOODS_NEEDLESS on received orders keeps goods.
        // NOTE: use wasUnshipped (captured before the status mutation above), not order.getOrderStatus(),
        // which is now REFUND_CONFIRM(203) for unshipped orders.
        boolean shouldRestock = wasUnshipped
                || entity.getType() == AppMallDaoConstants.AFTERSALE_TYPE_GOODS_REQUIRED;
        if (shouldRestock) {
            Set<LitemallOrderGoods> orderGoodsList = entity.getOrder().getOrderGoods();
            for (LitemallOrderGoods orderGoods : orderGoodsList) {
                String productId = orderGoods.getProductId();
                Integer number = orderGoods.getNumber();
                goodsProductMapper.addStock(productId, number);
            }
        }

        // Restore coupons used on this order (mirrors cancel():292-299 / refundGrouponOrder():248-255)
        QueryBean cuQuery = new QueryBean();
        cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_orderId, order.orm_idString()));
        cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_status, 1));
        List<LitemallCouponUser> usedCoupons = couponUserBiz.findList(cuQuery, null, context);
        for (LitemallCouponUser cu : usedCoupons) {
            couponUserBiz.returnCoupon(cu.orm_idString(), context);
        }

        // Notification is a fire-and-forget side effect: run after commit so its failure cannot roll back the refund.
        final String orderSn = order.getOrderSn();
        final String mobile = order.getMobile();
        txn().afterCommit(null, () -> notificationService.sendRefundNotification(orderSn, mobile));

        logManager.logOrderSucceed("退款", "订单编号 " + order.getOrderSn() + " 售后编号 " + entity.getAftersaleSn());
    }

    @Override
    @BizMutation
    public LitemallAftersale apply(@RequestBean AftersaleApplyRequest request,
                                    IServiceContext context) {
        String orderId = request.getOrderId();
        String userId = context.getUserId();

        LitemallOrder order = orderBiz.get(orderId, false, context);
        if (order == null || Boolean.TRUE.equals(order.getDeleted())) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_APPLY)
                    .param("orderId", orderId);
        }

        int orderStatus = order.getOrderStatus();
        if (orderStatus != AppMallDaoConstants.ORDER_STATUS_PAY
                && orderStatus != AppMallDaoConstants.ORDER_STATUS_CONFIRM
                && orderStatus != AppMallDaoConstants.ORDER_STATUS_AUTO_CONFIRM) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_APPLY)
                    .param("orderId", orderId)
                    .param("orderStatus", orderStatus);
        }

        if (order.getAftersaleStatus() != null
                && order.getAftersaleStatus() != AppMallDaoConstants.AFTERSALE_STATUS_INIT) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_APPLY)
                    .param("orderId", orderId);
        }

        LitemallAftersale aftersale = newEntity();
        aftersale.setAftersaleSn(generateAftersaleSn());
        aftersale.setOrderId(orderId);
        aftersale.setUserId(userId);
        aftersale.setType(request.getType());
        aftersale.setReason(request.getReason());
        BigDecimal amount = request.getAmount() != null ? request.getAmount() : order.getActualPrice();
        if (amount.signum() <= 0) {
            throw new NopException(ERR_AFTERSALE_AMOUNT_EXCEED)
                    .param("orderId", orderId)
                    .param("amount", amount);
        }
        if (amount.compareTo(order.getActualPrice()) > 0) {
            throw new NopException(ERR_AFTERSALE_AMOUNT_EXCEED)
                    .param("orderId", orderId)
                    .param("amount", amount)
                    .param("actualPrice", order.getActualPrice());
        }
        aftersale.setAmount(amount);
        aftersale.setPictures(request.getPictures());
        aftersale.setComment(request.getComment());
        aftersale.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_REQUEST);
        aftersale.setAddTime(LocalDateTime.now());
        aftersale.setUpdateTime(LocalDateTime.now());
        aftersale.setDeleted(false);

        order.setAftersaleStatus(AppMallDaoConstants.AFTERSALE_STATUS_REQUEST);

        saveEntity(aftersale, "apply", context);
        return aftersale;
    }

    @Override
    @BizMutation
    public LitemallAftersale cancel(@Name("id") String id,
                                    IServiceContext context) {
        LitemallAftersale aftersale = get(id, false, context);
        if (aftersale == null) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_CANCEL)
                    .param("id", id);
        }

        if (aftersale.getStatus() != AppMallDaoConstants.AFTERSALE_STATUS_REQUEST) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_CANCEL)
                    .param("id", id)
                    .param("status", aftersale.getStatus());
        }

        aftersale.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_CANCELLED);

        LitemallOrder order = aftersale.getOrder();
        if (order != null) {
            order.setAftersaleStatus(AppMallDaoConstants.AFTERSALE_STATUS_INIT);
        }

        updateEntity(aftersale, "cancel", context);
        return aftersale;
    }

    @Override
    @BizQuery
    public List<LitemallAftersale> userList(IServiceContext context) {
        String userId = context.getUserId();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallAftersale.PROP_NAME_userId, userId));
        query.addOrderField(LitemallAftersale.PROP_NAME_addTime, true);
        return findList(query, null, context);
    }

    @Override
    @BizQuery
    public LitemallAftersale userDetail(@Name("id") String id,
                                         IServiceContext context) {
        LitemallAftersale aftersale = get(id, false, context);
        if (aftersale == null || Boolean.TRUE.equals(aftersale.getDeleted())) {
            throw new NopException(ERR_AFTERSALE_NOT_FOUND)
                    .param("id", id);
        }
        if (!aftersale.getUserId().equals(context.getUserId())) {
            throw new NopException(ERR_AFTERSALE_NOT_FOUND)
                    .param("id", id);
        }
        return aftersale;
    }

    private String generateAftersaleSn() {
        return StringHelper.generateUUID();
    }
}
