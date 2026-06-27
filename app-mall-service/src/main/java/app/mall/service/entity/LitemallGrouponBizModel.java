
package app.mall.service.entity;

import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallGrouponBiz;
import app.mall.biz.ILitemallGrouponRulesBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.biz.ILitemallPointsFlowBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallGroupon;
import app.mall.dao.entity.LitemallGrouponRules;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.entity.LitemallPointsFlow;
import app.mall.dao.mapper.LitemallGoodsProductMapper;
import app.mall.pay.PayRefundRequestBean;
import app.mall.pay.PayRefundResponseBean;
import app.mall.pay.PayService;
import app.mall.service.notification.MallNotificationService;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallGroupon")
public class LitemallGrouponBizModel extends CrudBizModel<LitemallGroupon> implements ILitemallGrouponBiz {

    static final Logger LOG = LoggerFactory.getLogger(LitemallGrouponBizModel.class);

    @Inject
    ILitemallGrouponRulesBiz grouponRulesBiz;

    @Inject
    ILitemallOrderBiz orderBiz;

    @Inject
    ILitemallCouponUserBiz couponUserBiz;

    @Inject
    ILitemallPointsAccountBiz pointsAccountBiz;

    @Inject
    ILitemallPointsFlowBiz pointsFlowBiz;

    @Inject
    LitemallGoodsProductMapper goodsProductMapper;

    @Inject
    PayService payService;

    @Inject
    MallNotificationService notificationService;

    public LitemallGrouponBizModel() {
        setEntityName(LitemallGroupon.class.getName());
    }

    @Override
    @BizMutation
    public LitemallGroupon openGroupon(@Name("rulesId") String rulesId,
                                       @Name("orderId") String orderId,
                                       IServiceContext context) {
        LitemallGrouponRules rules = grouponRulesBiz.requireEntity(rulesId, null, context);
        if (rules.getStatus() != 0) {
            throw new NopException(ERR_GROUPON_RULES_NOT_AVAILABLE)
                    .param("rulesId", rulesId);
        }
        if (rules.getExpireTime() != null && !rules.getExpireTime().isAfter(LocalDateTime.now())) {
            throw new NopException(ERR_GROUPON_RULES_NOT_AVAILABLE)
                    .param("rulesId", rulesId);
        }
        requireOrderContainsRuleGoods(orderId, rules, context);

        String userId = context.getUserId();

        LitemallGroupon groupon = newEntity();
        groupon.setRulesId(rulesId);
        groupon.setOrderId(orderId);
        groupon.setGrouponId("0");
        groupon.setUserId(userId);
        groupon.setCreatorUserId(userId);
        groupon.setCreatorUserTime(LocalDateTime.now());
        groupon.setStatus(1);
        saveEntity(groupon, null, context);
        return groupon;
    }

    @Override
    @BizMutation
    public LitemallGroupon joinGroupon(@Name("grouponId") String grouponId,
                                       @Name("orderId") String orderId,
                                       IServiceContext context) {
        LitemallGroupon openGroupon = get(grouponId, false, context);
        if (openGroupon == null) {
            throw new NopException(ERR_GROUPON_NOT_FOUND)
                    .param("grouponId", grouponId);
        }
        if (openGroupon.getStatus() != 1) {
            throw new NopException(ERR_GROUPON_NOT_ACTIVE)
                    .param("grouponId", grouponId);
        }

        String userId = context.getUserId();

        if (userId.equals(openGroupon.getCreatorUserId())) {
            throw new NopException(ERR_GROUPON_CANNOT_JOIN_OWN)
                    .param("grouponId", grouponId);
        }

        QueryBean joinedQuery = new QueryBean();
        joinedQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_grouponId, grouponId));
        joinedQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_userId, userId));
        long joinedCount = findCount(joinedQuery, context);
        if (joinedCount > 0) {
            throw new NopException(ERR_GROUPON_ALREADY_JOINED)
                    .param("grouponId", grouponId);
        }

        LitemallGrouponRules rules = grouponRulesBiz.requireEntity(openGroupon.getRulesId(), null, context);
        requireOrderContainsRuleGoods(orderId, rules, context);

        QueryBean participantsQuery = new QueryBean();
        participantsQuery.addFilter(FilterBeans.or(
                FilterBeans.eq(LitemallGroupon.PROP_NAME_id, grouponId),
                FilterBeans.eq(LitemallGroupon.PROP_NAME_grouponId, grouponId)
        ));
        long participantsCount = findCount(participantsQuery, context);
        if (participantsCount >= rules.getDiscountMember()) {
            throw new NopException(ERR_GROUPON_FULL)
                    .param("grouponId", grouponId);
        }

        LitemallGroupon groupon = newEntity();
        groupon.setRulesId(openGroupon.getRulesId());
        groupon.setOrderId(orderId);
        groupon.setGrouponId(grouponId);
        groupon.setUserId(userId);
        groupon.setCreatorUserId(openGroupon.getCreatorUserId());
        groupon.setCreatorUserTime(openGroupon.getCreatorUserTime());
        groupon.setStatus(1);
        saveEntity(groupon, null, context);

        long totalParticipants = participantsCount + 1;
        if (totalParticipants >= rules.getDiscountMember()) {
            markGrouponSuccess(openGroupon.orm_idString(), context);
        }
        return groupon;
    }

    @Override
    @BizQuery
    public PageBean<LitemallGroupon> myGroupons(@Name("page") int page,
                                                 @Name("pageSize") int pageSize,
                                                 IServiceContext context) {
        String userId = context.getUserId();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_status, 1));

        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallGroupon.PROP_NAME_addTime, true);

        return findPage(query, null, context);
    }

    @Override
    @BizQuery
    public LitemallGroupon grouponDetail(@Name("id") String id, IServiceContext context) {
        LitemallGroupon groupon = requireEntity(id, null, context);
        return groupon;
    }

    @Override
    @BizMutation
    public int expireGroupons(IServiceContext context) {
        LocalDateTime now = LocalDateTime.now();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_status, 1));
        query.setLimit(500);

        List<LitemallGroupon> activeGroupons = doFindListByQueryDirectly(query, context);
        int count = 0;
        Set<String> processedRulesIds = new HashSet<>();
        List<String> refundFailures = new ArrayList<>();
        for (LitemallGroupon groupon : activeGroupons) {
            LitemallGrouponRules rules = grouponRulesBiz.get(groupon.getRulesId(), false, context);
            if (rules != null && rules.getExpireTime() != null && !rules.getExpireTime().isAfter(now)) {
                groupon.setStatus(2);
                updateEntity(groupon, "expireGroupons", context);
                count++;

                refundGrouponOrder(groupon, context, refundFailures);
                processedRulesIds.add(groupon.getRulesId());
            }
        }

        for (String rulesId : processedRulesIds) {
            expireRulesIfAllExpired(rulesId, context);
        }

        // Failed refunds are NOT auto-retried: status is already 2 so expireGroupons won't re-scan them.
        // Surface a summary alert for manual intervention; outRefundNo idempotency guards against double refund on retry.
        if (!refundFailures.isEmpty()) {
            LOG.error("expireGroupons: {} groupon refund(s) failed and require manual intervention: {}",
                    refundFailures.size(), refundFailures);
        }

        return count;
    }

    private void refundGrouponOrder(LitemallGroupon groupon, IServiceContext context, List<String> refundFailures) {
        LitemallOrder order = orderBiz.get(groupon.getOrderId(), false, context);
        if (order == null) {
            LOG.warn("refundGrouponOrder: order not found for groupon {}, orderId={}",
                    groupon.orm_idString(), groupon.getOrderId());
            return;
        }
        if (order.getOrderStatus() != _AppMallDaoConstants.ORDER_STATUS_PAY) {
            LOG.info("refundGrouponOrder: order {} status is {}, skip refund",
                    order.getOrderSn(), order.getOrderStatus());
            return;
        }
        if (order.getAftersaleStatus() != null
                && order.getAftersaleStatus() != _AppMallDaoConstants.AFTERSALE_STATUS_INIT) {
            LOG.info("refundGrouponOrder: order {} aftersaleStatus is {}, skip refund",
                    order.getOrderSn(), order.getAftersaleStatus());
            return;
        }

        try {
            PayRefundRequestBean req = new PayRefundRequestBean();
            req.setOutTradeNo(order.getOrderSn());
            req.setOutRefundNo("refund_" + order.getOrderSn());
            req.setTotalFee(order.getActualPrice());
            req.setRefundFee(order.getActualPrice());
            PayRefundResponseBean resp = payService.refund(req);
            if (!resp.isSuccess()) {
                LOG.error("refundGrouponOrder: refund failed for order {}", order.getOrderSn());
                refundFailures.add(order.getOrderSn());
                return;
            }
        } catch (Exception e) {
            // Do not silently swallow: record for batch summary alert. Batch continues so other groupons still process.
            LOG.error("refundGrouponOrder: refund exception for order {}", order.getOrderSn(), e);
            refundFailures.add(order.getOrderSn());
            return;
        }

        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_GROUPON_EXPIRED);
        order.setEndTime(LocalDateTime.now());

        for (LitemallOrderGoods orderGoods : order.getOrderGoods()) {
            goodsProductMapper.addStock(orderGoods.getProductId(), orderGoods.getNumber());
        }

        QueryBean cuQuery = new QueryBean();
        cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_orderId, order.orm_idString()));
        cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_status, 1));
        List<LitemallCouponUser> usedCoupons = couponUserBiz.findList(cuQuery, null, context);
        for (LitemallCouponUser cu : usedCoupons) {
            couponUserBiz.returnCoupon(cu.orm_idString(), context);
        }

        // Points return (P27): groupon-expire is a whole-order refund, so return the points the
        // user spent deducting on this order, mirroring the coupon return above. Idempotent per orderId.
        returnOrderDeductedPoints(order, context);

        orderBiz.updateEntity(order, "expireGroupons:refundOrder", context);

        // Notification is a fire-and-forget side effect: run after commit so its failure cannot roll back the refund.
        final String orderSn = order.getOrderSn();
        final String mobile = order.getMobile();
        txn().afterCommit(null, () -> notificationService.sendGrouponFailRefundNotification(orderSn, mobile));
    }

    private void returnOrderDeductedPoints(LitemallOrder order, IServiceContext context) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType,
                LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_DEDUCT));
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceId, order.orm_idString()));
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_changeType,
                _AppMallDaoConstants.POINTS_CHANGE_TYPE_SPEND));
        LitemallPointsFlow flow = pointsFlowBiz.findFirst(q, null, context);
        if (flow == null || flow.getChangeAmount() == null || flow.getChangeAmount() <= 0) {
            return;
        }
        pointsAccountBiz.earnPoints(order.getUserId(), flow.getChangeAmount(),
                _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN,
                LitemallPointsAccountBizModel.SOURCE_TYPE_REFUND_RETURN,
                order.orm_idString(),
                "团购失败返还积分 " + order.getOrderSn(), context);
    }

    private void expireRulesIfAllExpired(String rulesId, IServiceContext context) {
        QueryBean activeQuery = new QueryBean();
        activeQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_rulesId, rulesId));
        activeQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_status, 1));
        long activeCount = findCount(activeQuery, context);
        if (activeCount == 0) {
            LitemallGrouponRules rules = grouponRulesBiz.get(rulesId, false, context);
            if (rules != null && rules.getStatus() == 0) {
                rules.setStatus(1);
                grouponRulesBiz.updateEntity(rules, "expireGroupons:expireRules", context);
            }
        }
    }

    private void requireOrderContainsRuleGoods(String orderId, LitemallGrouponRules rules, IServiceContext context) {        LitemallOrder order = orderBiz.requireEntity(orderId, null, context);
        boolean matched = order.getOrderGoods().stream()
                .anyMatch(orderGoods -> Objects.equals(orderGoods.getGoodsId(), rules.getGoodsId()));
        if (!matched) {
            throw new NopException(ERR_GROUPON_RULES_NOT_AVAILABLE)
                    .param("rulesId", rules.orm_idString())
                    .param("goodsId", rules.getGoodsId())
                    .param("orderId", orderId);
        }
    }

    private void markGrouponSuccess(String grouponId, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.or(
                FilterBeans.eq(LitemallGroupon.PROP_NAME_id, grouponId),
                FilterBeans.eq(LitemallGroupon.PROP_NAME_grouponId, grouponId)
        ));
        for (LitemallGroupon item : findList(query, null, context)) {
            item.setStatus(3);
        }
    }
}
