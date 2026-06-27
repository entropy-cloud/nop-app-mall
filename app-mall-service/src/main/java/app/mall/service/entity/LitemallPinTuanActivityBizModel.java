
package app.mall.service.entity;

import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallPinTuanActivityBiz;
import app.mall.biz.ILitemallPinTuanGroupBiz;
import app.mall.biz.ILitemallPinTuanMemberBiz;
import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.biz.ILitemallPointsFlowBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.dto.PinTuanEffectivenessBean;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.entity.LitemallPinTuanActivity;
import app.mall.dao.entity.LitemallPinTuanGroup;
import app.mall.dao.entity.LitemallPinTuanMember;
import app.mall.dao.entity.LitemallPointsFlow;
import app.mall.dao.mapper.LitemallGoodsProductMapper;
import app.mall.dao.mapper.LitemallMarketingMapper;
import app.mall.pay.PayRefundRequestBean;
import app.mall.pay.PayRefundResponseBean;
import app.mall.pay.PayService;
import app.mall.service.notification.MallNotificationService;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallPinTuanActivity")
public class LitemallPinTuanActivityBizModel extends CrudBizModel<LitemallPinTuanActivity>
        implements ILitemallPinTuanActivityBiz {

    static final Logger LOG = LoggerFactory.getLogger(LitemallPinTuanActivityBizModel.class);

    @Inject
    ILitemallPinTuanGroupBiz pinTuanGroupBiz;

    @Inject
    ILitemallPinTuanMemberBiz pinTuanMemberBiz;

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

    // @SqlLibMapper for aggregation SQL (pintuan effectiveness) that I*Biz/QueryBean cannot express.
    @Inject
    LitemallMarketingMapper marketingMapper;

    private static final Timestamp MIN_TIMESTAMP = Timestamp.valueOf("1970-01-01 00:00:00");
    private static final Timestamp MAX_TIMESTAMP = Timestamp.valueOf("2099-12-31 23:59:59");

    public LitemallPinTuanActivityBizModel() {
        setEntityName(LitemallPinTuanActivity.class.getName());
    }

    @Override
    @BizMutation
    public LitemallPinTuanActivity publishActivity(@Name("id") String id, IServiceContext context) {
        LitemallPinTuanActivity activity = requireEntity(id, null, context);
        Integer status = activity.getStatus();
        if (status != null && (status == _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE
                || status == _AppMallDaoConstants.PROMOTION_STATUS_FINISHED)) {
            throw new NopException(ERR_PROMOTION_STATUS_TRANSITION_INVALID)
                    .param("activityId", id)
                    .param("currentStatus", status)
                    .param("targetStatus", _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        }
        activity.setStatus(_AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        return activity;
    }

    @Override
    @BizMutation
    public LitemallPinTuanActivity unpublishActivity(@Name("id") String id, IServiceContext context) {
        LitemallPinTuanActivity activity = requireEntity(id, null, context);
        Integer status = activity.getStatus();
        if (status != null && (status == _AppMallDaoConstants.PROMOTION_STATUS_CLOSED
                || status == _AppMallDaoConstants.PROMOTION_STATUS_DRAFT)) {
            throw new NopException(ERR_PROMOTION_STATUS_TRANSITION_INVALID)
                    .param("activityId", id)
                    .param("currentStatus", status)
                    .param("targetStatus", _AppMallDaoConstants.PROMOTION_STATUS_CLOSED);
        }
        activity.setStatus(_AppMallDaoConstants.PROMOTION_STATUS_CLOSED);
        return activity;
    }

    @Override
    @BizQuery
    public PinTuanEffectivenessBean getPinTuanEffectiveness(@Optional @Name("activityId") String activityId,
                                                            @Optional @Name("startDate") String startDate,
                                                            @Optional @Name("endDate") String endDate,
                                                            IServiceContext context) {
        Timestamp start = startDate != null && !startDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(startDate).atTime(LocalTime.MIN)) : MIN_TIMESTAMP;
        Timestamp end = endDate != null && !endDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(endDate).atTime(LocalTime.MAX)) : MAX_TIMESTAMP;
        return marketingMapper.getPinTuanEffectiveness(activityId, start, end);
    }

    @Override
    @BizMutation
    public LitemallPinTuanGroup openPinTuan(@Name("pinTuanActivityId") String pinTuanActivityId,
                                            @Name("orderId") String orderId,
                                            IServiceContext context) {
        LitemallPinTuanActivity activity = requireActiveActivity(pinTuanActivityId, context);
        requireOrderContainsActivityGoods(orderId, activity, context);

        String userId = context.getUserId();
        LocalDateTime now = LocalDateTime.now();

        LitemallPinTuanGroup group = pinTuanGroupBiz.newEntity();
        group.setActivityId(pinTuanActivityId);
        group.setCreatorUserId(userId);
        group.setOrderId(orderId);
        group.setStatus(_AppMallDaoConstants.PIN_TUAN_GROUP_STATUS_ACTIVE);
        if (activity.getExpireHours() != null && activity.getExpireHours() > 0) {
            group.setExpireTime(now.plusHours(activity.getExpireHours()));
        }
        pinTuanGroupBiz.saveEntity(group, "openPinTuan", context);

        LitemallPinTuanMember creatorMember = pinTuanMemberBiz.newEntity();
        creatorMember.setGroupId(group.orm_idString());
        creatorMember.setUserId(userId);
        creatorMember.setOrderId(orderId);
        pinTuanMemberBiz.saveEntity(creatorMember, "openPinTuan", context);

        // minUserCount == 1 means the creator alone satisfies the group.
        if (activity.getMinUserCount() != null && activity.getMinUserCount() <= 1) {
            markPinTuanSuccess(group.orm_idString(), context);
        }
        return group;
    }

    @Override
    @BizMutation
    public LitemallPinTuanGroup joinPinTuan(@Name("groupId") String groupId,
                                            @Name("orderId") String orderId,
                                            IServiceContext context) {
        LitemallPinTuanGroup group = pinTuanGroupBiz.get(groupId, false, context);
        if (group == null) {
            throw new NopException(ERR_PIN_TUAN_NOT_FOUND).param("groupId", groupId);
        }
        if (group.getStatus() == null
                || group.getStatus() != _AppMallDaoConstants.PIN_TUAN_GROUP_STATUS_ACTIVE) {
            throw new NopException(ERR_PIN_TUAN_NOT_ACTIVE).param("groupId", groupId);
        }
        LocalDateTime now = LocalDateTime.now();
        if (group.getExpireTime() != null && !group.getExpireTime().isAfter(now)) {
            throw new NopException(ERR_PIN_TUAN_EXPIRED).param("groupId", groupId);
        }

        String userId = context.getUserId();
        if (userId.equals(group.getCreatorUserId())) {
            throw new NopException(ERR_PIN_TUAN_CANNOT_JOIN_OWN).param("groupId", groupId);
        }

        QueryBean joinedQuery = new QueryBean();
        joinedQuery.addFilter(FilterBeans.eq(LitemallPinTuanMember.PROP_NAME_groupId, groupId));
        joinedQuery.addFilter(FilterBeans.eq(LitemallPinTuanMember.PROP_NAME_userId, userId));
        long joinedCount = pinTuanMemberBiz.findCount(joinedQuery, context);
        if (joinedCount > 0) {
            throw new NopException(ERR_PIN_TUAN_ALREADY_JOINED).param("groupId", groupId);
        }

        LitemallPinTuanActivity activity = requireActiveActivity(group.getActivityId(), context);
        requireOrderContainsActivityGoods(orderId, activity, context);

        long memberCount = countMembers(groupId, context);
        if (activity.getMaxUserCount() != null && activity.getMaxUserCount() > 0
                && memberCount >= activity.getMaxUserCount()) {
            throw new NopException(ERR_PIN_TUAN_FULL).param("groupId", groupId);
        }

        LitemallPinTuanMember member = pinTuanMemberBiz.newEntity();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setOrderId(orderId);
        pinTuanMemberBiz.saveEntity(member, "joinPinTuan", context);

        long totalCount = memberCount + 1;
        if (activity.getMinUserCount() != null && totalCount >= activity.getMinUserCount()) {
            markPinTuanSuccess(groupId, context);
        }
        return group;
    }

    @Override
    @BizQuery
    public PageBean<LitemallPinTuanGroup> myPinTuans(@Name("page") int page,
                                                      @Name("pageSize") int pageSize,
                                                      IServiceContext context) {
        String userId = context.getUserId();

        QueryBean memberQuery = new QueryBean();
        memberQuery.addFilter(FilterBeans.eq(LitemallPinTuanMember.PROP_NAME_userId, userId));
        memberQuery.setLimit(500);
        List<LitemallPinTuanMember> myMembers = pinTuanMemberBiz.findList(memberQuery, null, context);

        if (myMembers == null || myMembers.isEmpty()) {
            QueryBean empty = new QueryBean();
            empty.addFilter(FilterBeans.eq(LitemallPinTuanGroup.PROP_NAME_id, "0"));
            empty.setOffset(page > 0 ? (page - 1) * pageSize : 0);
            empty.setLimit(pageSize > 0 ? pageSize : 10);
            return pinTuanGroupBiz.findPage(empty, null, context);
        }

        List<String> groupIds = new ArrayList<>();
        for (LitemallPinTuanMember m : myMembers) {
            groupIds.add(m.getGroupId());
        }

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.in(LitemallPinTuanGroup.PROP_NAME_id, groupIds));
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallPinTuanGroup.PROP_NAME_addTime, true);
        return pinTuanGroupBiz.findPage(query, null, context);
    }

    @Override
    @BizQuery
    public LitemallPinTuanGroup pinTuanDetail(@Name("groupId") String groupId, IServiceContext context) {
        return pinTuanGroupBiz.requireEntity(groupId, null, context);
    }

    @Override
    @BizQuery
    public LitemallPinTuanActivity pinTuanForGoods(@Name("goodsId") String goodsId, IServiceContext context) {
        // Storefront goods-detail entry: return the first ACTIVE pin-tuan activity for the goods
        // (productId=null activities cover all SKUs of the goods). Returns null when none active.
        if (goodsId == null || goodsId.isEmpty()) {
            return null;
        }
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallPinTuanActivity.PROP_NAME_goodsId, goodsId));
        query.addFilter(FilterBeans.eq(LitemallPinTuanActivity.PROP_NAME_status,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE));
        query.setLimit(1);
        return findFirst(query, null, context);
    }

    @Override
    @BizMutation
    public int expirePinTuans(IServiceContext context) {
        LocalDateTime now = LocalDateTime.now();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallPinTuanGroup.PROP_NAME_status,
                _AppMallDaoConstants.PIN_TUAN_GROUP_STATUS_ACTIVE));
        query.setLimit(500);

        List<LitemallPinTuanGroup> activeGroups = pinTuanGroupBiz.findList(query, null, context);
        int count = 0;
        List<String> refundFailures = new ArrayList<>();
        for (LitemallPinTuanGroup group : activeGroups) {
            if (group.getExpireTime() != null && !group.getExpireTime().isAfter(now)) {
                group.setStatus(_AppMallDaoConstants.PIN_TUAN_GROUP_STATUS_FAILED);
                pinTuanGroupBiz.updateEntity(group, "expirePinTuans", context);
                count++;

                refundGroupMembers(group, context, refundFailures);
            }
        }

        if (!refundFailures.isEmpty()) {
            LOG.error("expirePinTuans: {} pin-tuan refund(s) failed and require manual intervention: {}",
                    refundFailures.size(), refundFailures);
        }
        return count;
    }

    private void markPinTuanSuccess(String groupId, IServiceContext context) {
        // Unlike Groupon's markGrouponSuccess which only mutates in-memory entities, here we
        // explicitly persist the status transition so SUCCESS is durable in the database.
        LitemallPinTuanGroup group = pinTuanGroupBiz.get(groupId, false, context);
        if (group != null && group.getStatus() != null
                && group.getStatus() == _AppMallDaoConstants.PIN_TUAN_GROUP_STATUS_ACTIVE) {
            group.setStatus(_AppMallDaoConstants.PIN_TUAN_GROUP_STATUS_SUCCESS);
            pinTuanGroupBiz.updateEntity(group, "markPinTuanSuccess", context);
        }
    }

    private long countMembers(String groupId, IServiceContext context) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPinTuanMember.PROP_NAME_groupId, groupId));
        return pinTuanMemberBiz.findCount(q, context);
    }

    private void refundGroupMembers(LitemallPinTuanGroup group, IServiceContext context,
                                    List<String> refundFailures) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPinTuanMember.PROP_NAME_groupId, group.orm_idString()));
        List<LitemallPinTuanMember> members = pinTuanMemberBiz.findList(q, null, context);
        if (members == null) {
            return;
        }
        for (LitemallPinTuanMember member : members) {
            refundMemberOrder(member, context, refundFailures);
        }
    }

    private void refundMemberOrder(LitemallPinTuanMember member, IServiceContext context,
                                   List<String> refundFailures) {
        LitemallOrder order = orderBiz.get(member.getOrderId(), false, context);
        if (order == null) {
            LOG.warn("refundMemberOrder: order not found for member {}, orderId={}",
                    member.orm_idString(), member.getOrderId());
            return;
        }
        if (order.getOrderStatus() != _AppMallDaoConstants.ORDER_STATUS_PAY) {
            LOG.info("refundMemberOrder: order {} status is {}, skip refund",
                    order.getOrderSn(), order.getOrderStatus());
            return;
        }
        if (order.getAftersaleStatus() != null
                && order.getAftersaleStatus() != _AppMallDaoConstants.AFTERSALE_STATUS_INIT) {
            LOG.info("refundMemberOrder: order {} aftersaleStatus is {}, skip refund",
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
                LOG.error("refundMemberOrder: refund failed for order {}", order.getOrderSn());
                refundFailures.add(order.getOrderSn());
                return;
            }
        } catch (Exception e) {
            LOG.error("refundMemberOrder: refund exception for order {}", order.getOrderSn(), e);
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

        returnOrderDeductedPoints(order, context);

        orderBiz.updateEntity(order, "expirePinTuans:refundOrder", context);

        final String orderSn = order.getOrderSn();
        final String mobile = order.getMobile();
        txn().afterCommit(null, () -> notificationService.sendPinTuanFailRefundNotification(orderSn, mobile));
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
                "拼团失败返还积分 " + order.getOrderSn(), context);
    }

    private LitemallPinTuanActivity requireActiveActivity(String activityId, IServiceContext context) {
        LitemallPinTuanActivity activity = get(activityId, false, context);
        if (activity == null || Boolean.TRUE.equals(activity.getDeleted())) {
            throw new NopException(ERR_PIN_TUAN_NOT_ACTIVE).param("activityId", activityId);
        }
        if (activity.getStatus() == null
                || activity.getStatus() != _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE) {
            throw new NopException(ERR_PIN_TUAN_NOT_ACTIVE)
                    .param("activityId", activityId)
                    .param("status", activity.getStatus());
        }
        return activity;
    }

    private void requireOrderContainsActivityGoods(String orderId, LitemallPinTuanActivity activity,
                                                   IServiceContext context) {
        LitemallOrder order = orderBiz.requireEntity(orderId, null, context);
        boolean matched;
        if (activity.getProductId() != null) {
            matched = order.getOrderGoods().stream()
                    .anyMatch(og -> Objects.equals(og.getProductId(), activity.getProductId()));
        } else {
            matched = order.getOrderGoods().stream()
                    .anyMatch(og -> Objects.equals(og.getGoodsId(), activity.getGoodsId()));
        }
        if (!matched) {
            throw new NopException(ERR_PIN_TUAN_NOT_ACTIVE)
                    .param("activityId", activity.orm_idString())
                    .param("goodsId", activity.getGoodsId())
                    .param("orderId", orderId);
        }
    }
}
