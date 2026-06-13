
package app.mall.service.entity;

import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallGrouponBiz;
import app.mall.biz.ILitemallGrouponRulesBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallGroupon;
import app.mall.dao.entity.LitemallGrouponRules;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
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
import java.util.HashSet;
import java.util.List;
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
        joinedQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_deleted, false));
        long joinedCount = findCount(joinedQuery, context);
        if (joinedCount > 0) {
            throw new NopException(ERR_GROUPON_ALREADY_JOINED)
                    .param("grouponId", grouponId);
        }

        LitemallGrouponRules rules = grouponRulesBiz.requireEntity(openGroupon.getRulesId(), null, context);

        QueryBean participantsQuery = new QueryBean();
        participantsQuery.addFilter(FilterBeans.or(
                FilterBeans.eq(LitemallGroupon.PROP_NAME_id, grouponId),
                FilterBeans.eq(LitemallGroupon.PROP_NAME_grouponId, grouponId)
        ));
        participantsQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_deleted, false));
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
        query.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_deleted, false));

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
        query.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_deleted, false));
        query.setLimit(500);

        List<LitemallGroupon> activeGroupons = doFindListByQueryDirectly(query, context);
        int count = 0;
        Set<String> processedRulesIds = new HashSet<>();
        for (LitemallGroupon groupon : activeGroupons) {
            LitemallGrouponRules rules = grouponRulesBiz.get(groupon.getRulesId(), false, context);
            if (rules != null && rules.getExpireTime() != null && !rules.getExpireTime().isAfter(now)) {
                groupon.setStatus(2);
                updateEntity(groupon, "expireGroupons", context);
                count++;

                refundGrouponOrder(groupon, context);
                processedRulesIds.add(groupon.getRulesId());
            }
        }

        for (String rulesId : processedRulesIds) {
            expireRulesIfAllExpired(rulesId, context);
        }

        return count;
    }

    private void refundGrouponOrder(LitemallGroupon groupon, IServiceContext context) {
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

        try {
            PayRefundRequestBean req = new PayRefundRequestBean();
            req.setOutTradeNo(order.getOrderSn());
            req.setOutRefundNo("groupon_refund_" + order.getOrderSn());
            req.setTotalFee(order.getActualPrice());
            req.setRefundFee(order.getActualPrice());
            PayRefundResponseBean resp = payService.refund(req);
            if (!resp.isSuccess()) {
                LOG.error("refundGrouponOrder: refund failed for order {}", order.getOrderSn());
                return;
            }
        } catch (Exception e) {
            LOG.error("refundGrouponOrder: refund exception for order {}", order.getOrderSn(), e);
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
        cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_deleted, false));
        List<LitemallCouponUser> usedCoupons = couponUserBiz.findList(cuQuery, null, context);
        for (LitemallCouponUser cu : usedCoupons) {
            couponUserBiz.returnCoupon(cu.orm_idString(), context);
        }

        orderBiz.updateEntity(order, "expireGroupons:refundOrder", context);

        notificationService.sendGrouponFailRefundNotification(order.getOrderSn(), order.getMobile());
    }

    private void expireRulesIfAllExpired(String rulesId, IServiceContext context) {
        QueryBean activeQuery = new QueryBean();
        activeQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_rulesId, rulesId));
        activeQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_status, 1));
        activeQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_deleted, false));
        long activeCount = findCount(activeQuery, context);
        if (activeCount == 0) {
            LitemallGrouponRules rules = grouponRulesBiz.get(rulesId, false, context);
            if (rules != null && rules.getStatus() == 0) {
                rules.setStatus(1);
                grouponRulesBiz.updateEntity(rules, "expireGroupons:expireRules", context);
            }
        }
    }
}
