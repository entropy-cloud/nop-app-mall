package app.mall.service.entity;

import app.mall.biz.ILitemallAftersaleBiz;
import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.biz.ILitemallPointsFlowBiz;
import app.mall.dao.AppMallDaoConstants;
import app.mall.dao.dto.AftersaleApplyRequest;
import app.mall.dao.entity.LitemallAftersale;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.entity.LitemallPointsFlow;
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
import io.nop.api.core.time.CoreMetrics;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static app.mall.service.AppMallErrors.ERR_AFTERSALE_AMOUNT_EXCEED;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_ITEM_IN_PROGRESS;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_ITEM_NOT_IN_ORDER;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_ALLOW_APPLY;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_ALLOW_CANCEL;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_ALLOW_CONFIRM_RETURN;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_ALLOW_REFUND;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_ALLOW_SUBMIT_RETURN;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_REASON_INVALID;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_REFUND_FAILED;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_TYPE_STATUS_MISMATCH;

@BizModel("LitemallAftersale")
public class LitemallAftersaleBizModel extends CrudBizModel<LitemallAftersale> implements ILitemallAftersaleBiz {

    private static final Set<String> VALID_AFTERSALE_REASONS = Set.of(
            AppMallDaoConstants.AFTERSALE_REASON_UNWANTED,
            AppMallDaoConstants.AFTERSALE_REASON_QUALITY,
            AppMallDaoConstants.AFTERSALE_REASON_MISSING,
            AppMallDaoConstants.AFTERSALE_REASON_DAMAGED,
            AppMallDaoConstants.AFTERSALE_REASON_NOT_AS_DESCRIBED,
            AppMallDaoConstants.AFTERSALE_REASON_SEVEN_DAY);

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
    ILitemallPointsAccountBiz pointsAccountBiz;

    @Inject
    ILitemallPointsFlowBiz pointsFlowBiz;

    @Inject
    LitemallGoodsProductMapper goodsProductMapper; // addStock: atomic SQL UPDATE for stock replenishment after refund

    public LitemallAftersaleBizModel() {
        setEntityName(LitemallAftersale.class.getName());
    }

    @Override
    @BizMutation
    public void batchApprove(@Name("ids") Set<String> ids, IServiceContext context) {
        List<LitemallAftersale> list = batchGet(ids, false, context);

        for (LitemallAftersale entity : list) {
            int status = entity.getStatus();
            if (status != AppMallDaoConstants.AFTERSALE_STATUS_REQUEST) {
                continue;
            }
            entity.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_APPROVED);
            entity.setHandleTime(CoreMetrics.currentDateTime());

            recomputeOrderAftersaleStatus(entity.getOrder(), context);
        }
    }

    @Override
    @BizMutation
    public void batchReject(@Name("ids") Set<String> ids, IServiceContext context) {
        List<LitemallAftersale> list = batchGet(ids, false, context);

        for (LitemallAftersale entity : list) {
            int status = entity.getStatus();
            if (status != AppMallDaoConstants.AFTERSALE_STATUS_REQUEST) {
                continue;
            }

            entity.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_REJECT);
            entity.setHandleTime(CoreMetrics.currentDateTime());

            recomputeOrderAftersaleStatus(entity.getOrder(), context);
        }
    }

    @Override
    @BizMutation
    public void refund(@Name("id") String id, IServiceContext context) {
        LitemallAftersale entity = get(id, false, context);
        int status = entity.getStatus();
        if (status != AppMallDaoConstants.AFTERSALE_STATUS_APPROVED) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_REFUND);
        }
        // GOODS_REQUIRED must go through confirmReturnReceived (RETURNED -> REFUND with restock), not refund().
        if (entity.getType() == AppMallDaoConstants.AFTERSALE_TYPE_GOODS_REQUIRED) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_REFUND)
                    .param("id", id)
                    .param("type", entity.getType());
        }

        LitemallOrder order = entity.getOrder();
        boolean isWholeOrder = StringHelper.isEmpty(entity.getOrderItemId());

        // Defensive amount recheck (apply() already validates, but status may have changed since).
        // Item-level cap = number x price of that line (minus already-refunded for the line);
        // whole-order cap = order.actualPrice.
        BigDecimal cap = isWholeOrder ? order.getActualPrice() : itemRefundCap(order, entity.getOrderItemId(), context);
        if (entity.getAmount() == null || entity.getAmount().signum() <= 0
                || entity.getAmount().compareTo(cap) > 0) {
            throw new NopException(ERR_AFTERSALE_AMOUNT_EXCEED)
                    .param("aftersaleId", id)
                    .param("amount", entity.getAmount())
                    .param("cap", cap);
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

        LocalDateTime now = CoreMetrics.currentDateTime();
        entity.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_REFUND);
        entity.setHandleTime(now);
        entity.setProcessTime(now);
        entity.setProcessNote("退款成功");
        // Flush the terminal status so the order-level decision queries below observe REFUND rather than
        // the pre-mutation APPROVED value.
        orm().flushSession();

        // Order-level refund state (Decision 5a): a whole-order refund, or an item-level refund that
        // exhausts every order goods line, pushes an unshipped (PAY) order to terminal REFUND_CONFIRM(203).
        // A partial item refund on an unshipped order keeps the order at PAY.
        boolean wasUnshipped = order.getOrderStatus() == AppMallDaoConstants.ORDER_STATUS_PAY;
        if (wasUnshipped && (isWholeOrder || allOrderItemsRefunded(order, context))) {
            order.setOrderStatus(AppMallDaoConstants.ORDER_STATUS_REFUND_CONFIRM);
            order.setEndTime(now);
        }

        // Restock (Decision 5b): refund() now only handles GOODS_MISS (unshipped, restock) and
        // GOODS_NEEDLESS (received, no restock). GOODS_REQUIRED restock moved to confirmReturnReceived.
        // wasUnshipped covers GOODS_MISS on PAY orders (the only type allowed on PAY per isTypeAllowedForStatus).
        boolean shouldRestock = wasUnshipped;
        if (shouldRestock) {
            if (isWholeOrder) {
                for (LitemallOrderGoods orderGoods : order.getOrderGoods()) {
                    goodsProductMapper.addStock(orderGoods.getProductId(), orderGoods.getNumber());
                }
            } else {
                LitemallOrderGoods line = findOrderGoods(order, entity.getOrderItemId());
                if (line != null) {
                    goodsProductMapper.addStock(line.getProductId(), line.getNumber());
                }
            }
        }

        // Coupon restore (Decision 5c): only on whole-order refund. Item-level partial refund does not
        // restore the order-level coupon (coupon is an order-level price component; restored only on
        // full order cancel/refund). Mirrors cancel()/refundGrouponOrder() coupon-return pattern.
        if (isWholeOrder) {
            QueryBean cuQuery = new QueryBean();
            cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_orderId, order.orm_idString()));
            cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_status, 1));
            List<LitemallCouponUser> usedCoupons = couponUserBiz.findList(cuQuery, null, context);
            for (LitemallCouponUser cu : usedCoupons) {
                couponUserBiz.returnCoupon(cu.orm_idString(), context);
            }

            // Points return (P27, mirrors coupon restore): a whole-order refund returns the points
            // the user spent deducting on this order. Item-level partial refund does NOT return points
            // (deduction is an order-level component, symmetric with coupon restore). Idempotent per
            // orderId via sourceType=refund-return dedupe in earnPoints.
            returnOrderDeductedPoints(order, context);

            // Release promotion participation quota (mirrors coupon/points restore): whole-order
            // refund soft-deletes the PromotionUsage so maxPerUser lets the user re-participate.
            // Item-level partial refund does NOT release (promotion is order-level, same boundary).
            orderBiz.releasePromotionUsage(order.orm_idString(), context);
        }

        // Notification (Decision 8): order-level dedupe by orderSn. Item-level refunds reuse the same
        // order-level notification to avoid spamming the user across multiple item refunds.
        final String orderSn = order.getOrderSn();
        final String mobile = order.getMobile();
        final String refundUserId = notificationService.isEventMessageEnabled("refund", context)
                ? order.getUserId() : null;
        txn().afterCommit(null, () -> notificationService.sendRefundNotification(orderSn, mobile, refundUserId));

        recomputeOrderAftersaleStatus(order, context);

        logManager.logOrderSucceed("退款", "订单编号 " + order.getOrderSn() + " 售后编号 " + entity.getAftersaleSn());
    }

    @Override
    @BizMutation
    public LitemallAftersale submitReturnLogistics(@Name("id") String id,
                                                    @Name("returnShipChannel") String returnShipChannel,
                                                    @Name("returnShipSn") String returnShipSn,
                                                    IServiceContext context) {
        LitemallAftersale entity = get(id, false, context);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new NopException(ERR_AFTERSALE_NOT_FOUND)
                    .param("id", id);
        }
        // Storefront ownership: only the aftersale owner can submit return logistics.
        if (!entity.getUserId().equals(context.getUserId())) {
            throw new NopException(ERR_AFTERSALE_NOT_FOUND)
                    .param("id", id);
        }
        int status = entity.getStatus();
        int type = entity.getType();
        if (status != AppMallDaoConstants.AFTERSALE_STATUS_APPROVED
                || type != AppMallDaoConstants.AFTERSALE_TYPE_GOODS_REQUIRED) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_SUBMIT_RETURN)
                    .param("id", id)
                    .param("status", status)
                    .param("type", type);
        }
        if (StringHelper.isBlank(returnShipChannel) || StringHelper.isBlank(returnShipSn)) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_SUBMIT_RETURN)
                    .param("id", id);
        }

        entity.setReturnShipChannel(returnShipChannel);
        entity.setReturnShipSn(returnShipSn);
        entity.setReturnTime(CoreMetrics.currentDateTime());
        entity.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_RETURNED);

        updateEntity(entity, "submitReturnLogistics", context);

        recomputeOrderAftersaleStatus(entity.getOrder(), context);

        return entity;
    }

    @Override
    @BizMutation
    public LitemallAftersale confirmReturnReceived(@Name("id") String id, IServiceContext context) {
        LitemallAftersale entity = get(id, false, context);
        int status = entity.getStatus();
        if (status != AppMallDaoConstants.AFTERSALE_STATUS_RETURNED) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_CONFIRM_RETURN)
                    .param("id", id)
                    .param("status", status);
        }

        LitemallOrder order = entity.getOrder();
        boolean isWholeOrder = StringHelper.isEmpty(entity.getOrderItemId());

        // Defensive amount recheck (same as refund()).
        BigDecimal cap = isWholeOrder ? order.getActualPrice() : itemRefundCap(order, entity.getOrderItemId(), context);
        if (entity.getAmount() == null || entity.getAmount().signum() <= 0
                || entity.getAmount().compareTo(cap) > 0) {
            throw new NopException(ERR_AFTERSALE_AMOUNT_EXCEED)
                    .param("aftersaleId", id)
                    .param("amount", entity.getAmount())
                    .param("cap", cap);
        }

        PayRefundRequestBean wxPayRefundRequest = new PayRefundRequestBean();
        wxPayRefundRequest.setOutTradeNo(order.getOrderSn());
        wxPayRefundRequest.setOutRefundNo("refund_" + order.getOrderSn());
        wxPayRefundRequest.setTotalFee(order.getActualPrice());
        wxPayRefundRequest.setRefundFee(entity.getAmount());

        PayRefundResponseBean refundResult = payService.refund(wxPayRefundRequest);
        if (!refundResult.isSuccess()) {
            throw new NopException(ERR_AFTERSALE_REFUND_FAILED)
                    .param("aftersaleId", id)
                    .param("orderSn", order.getOrderSn());
        }

        LocalDateTime now = CoreMetrics.currentDateTime();
        entity.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_REFUND);
        entity.setReceiveConfirmTime(now);
        entity.setHandleTime(now);
        entity.setProcessTime(now);
        entity.setProcessNote("确认收货并退款成功");
        orm().flushSession();

        // Restock the returned goods (moved from refund() GOODS_REQUIRED branch). On a received order
        // (CONFIRM/AUTO_CONFIRM) the goods are with the user, so we restock upon return confirmation.
        if (isWholeOrder) {
            for (LitemallOrderGoods orderGoods : order.getOrderGoods()) {
                goodsProductMapper.addStock(orderGoods.getProductId(), orderGoods.getNumber());
            }
        } else {
            LitemallOrderGoods line = findOrderGoods(order, entity.getOrderItemId());
            if (line != null) {
                goodsProductMapper.addStock(line.getProductId(), line.getNumber());
            }
        }

        // Coupon restore + points return (mirrors refund() whole-order branch).
        if (isWholeOrder) {
            QueryBean cuQuery = new QueryBean();
            cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_orderId, order.orm_idString()));
            cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_status, 1));
            List<LitemallCouponUser> usedCoupons = couponUserBiz.findList(cuQuery, null, context);
            for (LitemallCouponUser cu : usedCoupons) {
                couponUserBiz.returnCoupon(cu.orm_idString(), context);
            }
            returnOrderDeductedPoints(order, context);

            // Release promotion participation quota (mirrors refund() whole-order branch): soft-delete
            // the PromotionUsage so maxPerUser lets the user re-participate. Partial-item return does
            // NOT release (promotion is order-level).
            orderBiz.releasePromotionUsage(order.orm_idString(), context);
        }

        final String orderSn = order.getOrderSn();
        final String mobile = order.getMobile();
        final String refundUserId = notificationService.isEventMessageEnabled("refund", context)
                ? order.getUserId() : null;
        txn().afterCommit(null, () -> notificationService.sendRefundNotification(orderSn, mobile, refundUserId));

        recomputeOrderAftersaleStatus(order, context);

        logManager.logOrderSucceed("退货确认退款",
                "订单编号 " + order.getOrderSn() + " 售后编号 " + entity.getAftersaleSn());

        return entity;
    }

    private void returnOrderDeductedPoints(LitemallOrder order, IServiceContext context) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType,
                LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_DEDUCT));
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceId, order.orm_idString()));
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_changeType,
                AppMallDaoConstants.POINTS_CHANGE_TYPE_SPEND));
        LitemallPointsFlow flow = pointsFlowBiz.findFirst(q, null, context);
        if (flow == null || flow.getChangeAmount() == null || flow.getChangeAmount() <= 0) {
            return;
        }
        pointsAccountBiz.earnPoints(order.getUserId(), flow.getChangeAmount(),
                AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN,
                LitemallPointsAccountBizModel.SOURCE_TYPE_REFUND_RETURN,
                order.orm_idString(),
                "退款返还积分 " + order.getOrderSn(), context);
    }

    @Override
    @BizMutation
    public LitemallAftersale apply(@RequestBean AftersaleApplyRequest request,
                                    IServiceContext context) {
        String orderId = request.getOrderId();
        String userId = context.getUserId();
        String orderItemId = request.getOrderItemId();
        boolean isWholeOrder = StringHelper.isEmpty(orderItemId);

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

        // Decision 6: aftersale type must match order fulfillment status.
        int type = request.getType();
        if (!isTypeAllowedForStatus(type, orderStatus)) {
            throw new NopException(ERR_AFTERSALE_TYPE_STATUS_MISMATCH)
                    .param("type", type)
                    .param("orderStatus", orderStatus);
        }

        // Reason must be a defined mall/aftersale-reason dictionary option.
        String reason = request.getReason();
        if (!VALID_AFTERSALE_REASONS.contains(reason)) {
            throw new NopException(ERR_AFTERSALE_REASON_INVALID)
                    .param("reason", reason);
        }

        BigDecimal cap;
        LitemallOrderGoods line = null;
        if (isWholeOrder) {
            // Whole-order compat (Decision 7): keep the existing order-level mutex.
            if (order.getAftersaleStatus() != null
                    && order.getAftersaleStatus() != AppMallDaoConstants.AFTERSALE_STATUS_INIT) {
                throw new NopException(ERR_AFTERSALE_NOT_ALLOW_APPLY)
                        .param("orderId", orderId);
            }
            cap = order.getActualPrice();
        } else {
            // Item-level: the line must belong to this order, and have no in-progress aftersale.
            line = findOrderGoods(order, orderItemId);
            if (line == null) {
                throw new NopException(ERR_AFTERSALE_ITEM_NOT_IN_ORDER)
                        .param("orderId", orderId)
                        .param("orderItemId", orderItemId);
            }
            if (hasInProgressAftersaleForItem(orderItemId, context)) {
                throw new NopException(ERR_AFTERSALE_ITEM_IN_PROGRESS)
                        .param("orderItemId", orderItemId);
            }
            // Decision 3: per-item cap = number x price, minus already-refunded for the line.
            cap = itemRefundCap(order, orderItemId, context);
        }

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : cap;
        if (amount.signum() <= 0) {
            throw new NopException(ERR_AFTERSALE_AMOUNT_EXCEED)
                    .param("orderId", orderId)
                    .param("amount", amount);
        }
        if (amount.compareTo(cap) > 0) {
            throw new NopException(ERR_AFTERSALE_AMOUNT_EXCEED)
                    .param("orderId", orderId)
                    .param("amount", amount)
                    .param("cap", cap);
        }

        LitemallAftersale aftersale = newEntity();
        aftersale.setAftersaleSn(generateAftersaleSn());
        aftersale.setOrderId(orderId);
        aftersale.setUserId(userId);
        aftersale.setOrderItemId(isWholeOrder ? null : orderItemId);
        aftersale.setType(type);
        aftersale.setReason(reason);
        aftersale.setAmount(amount);
        aftersale.setPictures(request.getPictures());
        aftersale.setComment(request.getComment());
        aftersale.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_REQUEST);
        LocalDateTime now = CoreMetrics.currentDateTime();
        aftersale.setAddTime(now);
        aftersale.setUpdateTime(now);
        aftersale.setDeleted(false);

        // Order-level field is an aggregate view (Decision 4): a freshly requested aftersale makes the
        // order show REQUEST regardless of item/whole granularity.
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
            recomputeOrderAftersaleStatus(order, context);
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

    // ---- item-level helpers ----

    private boolean isTypeAllowedForStatus(int type, int orderStatus) {
        if (orderStatus == AppMallDaoConstants.ORDER_STATUS_PAY) {
            return type == AppMallDaoConstants.AFTERSALE_TYPE_GOODS_MISS;
        }
        if (orderStatus == AppMallDaoConstants.ORDER_STATUS_CONFIRM
                || orderStatus == AppMallDaoConstants.ORDER_STATUS_AUTO_CONFIRM) {
            return type == AppMallDaoConstants.AFTERSALE_TYPE_GOODS_NEEDLESS
                    || type == AppMallDaoConstants.AFTERSALE_TYPE_GOODS_REQUIRED;
        }
        return false;
    }

    private LitemallOrderGoods findOrderGoods(LitemallOrder order, String orderItemId) {
        if (orderItemId == null) {
            return null;
        }
        Set<LitemallOrderGoods> goods = order.getOrderGoods();
        if (goods == null) {
            return null;
        }
        for (LitemallOrderGoods og : goods) {
            if (orderItemId.equals(og.orm_idString())) {
                return og;
            }
        }
        return null;
    }

    // Decision 3: per-item refund cap = line amount (number x price) minus already-refunded for the line.
    private BigDecimal itemRefundCap(LitemallOrder order, String orderItemId, IServiceContext context) {
        LitemallOrderGoods line = findOrderGoods(order, orderItemId);
        if (line == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal lineTotal = line.getPrice().multiply(BigDecimal.valueOf(line.getNumber()));
        return lineTotal.subtract(sumRefundedAmountForItem(orderItemId, context));
    }

    private boolean hasInProgressAftersaleForItem(String orderItemId, IServiceContext context) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallAftersale.PROP_NAME_orderItemId, orderItemId));
        q.addFilter(FilterBeans.in(LitemallAftersale.PROP_NAME_status, List.of(
                AppMallDaoConstants.AFTERSALE_STATUS_REQUEST,
                AppMallDaoConstants.AFTERSALE_STATUS_APPROVED,
                AppMallDaoConstants.AFTERSALE_STATUS_RETURNED)));
        return !findList(q, null, context).isEmpty();
    }

    private BigDecimal sumRefundedAmountForItem(String orderItemId, IServiceContext context) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallAftersale.PROP_NAME_orderItemId, orderItemId));
        q.addFilter(FilterBeans.eq(LitemallAftersale.PROP_NAME_status, AppMallDaoConstants.AFTERSALE_STATUS_REFUND));
        List<LitemallAftersale> list = findList(q, null, context);
        return list.stream()
                .map(a -> a.getAmount() == null ? BigDecimal.ZERO : a.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean hasRefundedAftersaleForItem(String orderItemId, IServiceContext context) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallAftersale.PROP_NAME_orderItemId, orderItemId));
        q.addFilter(FilterBeans.eq(LitemallAftersale.PROP_NAME_status, AppMallDaoConstants.AFTERSALE_STATUS_REFUND));
        return !findList(q, null, context).isEmpty();
    }

    // Decision 5a: an unshipped order is fully refunded only when every order goods line has reached REFUND.
    private boolean allOrderItemsRefunded(LitemallOrder order, IServiceContext context) {
        Set<LitemallOrderGoods> goods = order.getOrderGoods();
        if (goods == null || goods.isEmpty()) {
            return false;
        }
        for (LitemallOrderGoods og : goods) {
            if (!hasRefundedAftersaleForItem(og.orm_idString(), context)) {
                return false;
            }
        }
        return true;
    }

    // Decision 4: order.aftersaleStatus is an aggregate view — REQUEST while any aftersale is in progress,
    // INIT otherwise. Computed from LitemallAftersale records rather than a persisted per-item column.
    private void recomputeOrderAftersaleStatus(LitemallOrder order, IServiceContext context) {
        if (order == null) {
            return;
        }
        // Flush in-memory status mutations (e.g. REQUEST -> CANCELLED/REFUND on the calling entity) so the
        // in-progress query observes the up-to-date status rather than the pre-mutation persisted value.
        orm().flushSession();
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallAftersale.PROP_NAME_orderId, order.orm_idString()));
        q.addFilter(FilterBeans.in(LitemallAftersale.PROP_NAME_status, List.of(
                AppMallDaoConstants.AFTERSALE_STATUS_REQUEST,
                AppMallDaoConstants.AFTERSALE_STATUS_APPROVED,
                AppMallDaoConstants.AFTERSALE_STATUS_RETURNED)));
        boolean inProgress = !findList(q, null, context).isEmpty();
        order.setAftersaleStatus(inProgress
                ? AppMallDaoConstants.AFTERSALE_STATUS_REQUEST
                : AppMallDaoConstants.AFTERSALE_STATUS_INIT);
    }

    private String generateAftersaleSn() {
        return StringHelper.generateUUID();
    }
}
