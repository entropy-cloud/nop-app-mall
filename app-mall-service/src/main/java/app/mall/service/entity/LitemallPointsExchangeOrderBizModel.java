package app.mall.service.entity;

import app.mall.biz.ILitemallAddressBiz;
import app.mall.biz.ILitemallGoodsBiz;
import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.biz.ILitemallPointsExchangeOrderBiz;
import app.mall.biz.ILitemallPointsGoodsBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallPointsExchangeOrder;
import app.mall.dao.entity.LitemallPointsGoods;
import app.mall.dao.mapper.LitemallPointsGoodsMapper;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.api.core.time.CoreMetrics;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static app.mall.service.AppMallErrors.ERR_EXCHANGE_ORDER_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_EXCHANGE_QUANTITY_INVALID;
import static app.mall.service.AppMallErrors.ERR_EXCHANGE_STATUS_TRANSITION_INVALID;
import static app.mall.service.AppMallErrors.ERR_POINTS_GOODS_NOT_ACTIVE;
import static app.mall.service.AppMallErrors.ERR_POINTS_GOODS_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_POINTS_GOODS_NOT_IN_WINDOW;
import static app.mall.service.AppMallErrors.ERR_POINTS_GOODS_OFF_SHELF;
import static app.mall.service.AppMallErrors.ERR_POINTS_GOODS_OVER_LIMIT_PER_USER;
import static app.mall.service.AppMallErrors.ERR_POINTS_GOODS_SOLD_OUT;

@BizModel("LitemallPointsExchangeOrder")
public class LitemallPointsExchangeOrderBizModel extends CrudBizModel<LitemallPointsExchangeOrder>
        implements ILitemallPointsExchangeOrderBiz {

    // sourceType taxonomy: spend on exchange vs earn-back on cancellation.
    // See docs/design/marketing-and-promotions.md (积分商城兑换 与 spendPoints 约定).
    public static final String SOURCE_TYPE_MALL_EXCHANGE = "mall-exchange";
    public static final String SOURCE_TYPE_MALL_EXCHANGE_REFUND = "mall-exchange-refund";

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 100;

    @Inject
    ILitemallPointsGoodsBiz pointsGoodsBiz;

    @Inject
    ILitemallPointsAccountBiz pointsAccountBiz;

    @Inject
    ILitemallGoodsBiz goodsBiz;

    @Inject
    ILitemallAddressBiz addressBiz;

    @Inject
    LitemallPointsGoodsMapper pointsGoodsMapper;

    public LitemallPointsExchangeOrderBizModel() {
        setEntityName(LitemallPointsExchangeOrder.class.getName());
    }

    @Override
    @BizMutation
    public LitemallPointsExchangeOrder exchange(@Name("pointsGoodsId") String pointsGoodsId,
                                                @Name("quantity") Integer quantity,
                                                @Optional @Name("addressId") String addressId,
                                                IServiceContext context) {
        // Decision A (Phase 2): independent exchange path. Does NOT enter the order submit()
        // pipeline — therefore it does not interact with coupon / promotion / integral / groupon.
        // Points deduction reuses the P27 account spend API (sourceType=mall-exchange).
        // See docs/design/marketing-and-promotions.md 积分商城兑换.
        String userId = context.getUserId();
        if (StringHelper.isEmpty(userId)) {
            throw new NopException(ERR_EXCHANGE_ORDER_NOT_FOUND).param("reason", "no authenticated user");
        }
        if (quantity == null || quantity <= 0) {
            throw new NopException(ERR_EXCHANGE_QUANTITY_INVALID).param("quantity", quantity);
        }

        LitemallPointsGoods goods = requirePointsGoods(pointsGoodsId, context);

        // Activity status guard: only ACTIVE goods are exchangeable.
        if (goods.getStatus() == null
                || goods.getStatus() != _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE) {
            throw new NopException(ERR_POINTS_GOODS_NOT_ACTIVE)
                    .param("id", pointsGoodsId)
                    .param("status", goods.getStatus());
        }

        // Time-window guard (authoritative — the storefront list only filters by status).
        LocalDateTime now = CoreMetrics.currentDateTime();
        if (!LitemallPointsGoodsBizModel.isInWindow(goods, now)) {
            throw new NopException(ERR_POINTS_GOODS_NOT_IN_WINDOW)
                    .param("id", pointsGoodsId)
                    .param("startTime", goods.getStartTime())
                    .param("endTime", goods.getEndTime());
        }

        // Retail goods must still be on sale (defense in depth: even if the points goods is ACTIVE,
        // a pulled-off-shelf retail goods cannot be fulfilled).
        LitemallGoods retail = goodsBiz.get(goods.getGoodsId(), false, context);
        if (retail == null || Boolean.TRUE.equals(retail.getDeleted())) {
            throw new NopException(ERR_POINTS_GOODS_OFF_SHELF).param("goodsId", goods.getGoodsId());
        }
        if (!Boolean.TRUE.equals(retail.getIsOnSale())) {
            throw new NopException(ERR_POINTS_GOODS_OFF_SHELF).param("goodsId", goods.getGoodsId());
        }

        // maxPerUser guard: count this user's non-cancelled exchange orders for THIS points goods.
        // Rejection is placed before any stock/points side effects so a rejection leaves no trace.
        // The GraphQL filter whitelist only allows {eq, in, dateBetween, dateTimeBetween}, so the
        // "not cancelled" condition is expressed as `in` over the non-final statuses (mirrors the
        // flash-sale sessionStatus pattern in LitemallFlashSaleSessionBizModel.switchFlashSaleSessions).
        Integer maxPerUser = goods.getMaxPerUser();
        if (maxPerUser != null && maxPerUser > 0) {
            QueryBean userCountQuery = new QueryBean();
            userCountQuery.addFilter(FilterBeans.eq(LitemallPointsExchangeOrder.PROP_NAME_pointsGoodsId, pointsGoodsId));
            userCountQuery.addFilter(FilterBeans.eq(LitemallPointsExchangeOrder.PROP_NAME_userId, userId));
            userCountQuery.addFilter(FilterBeans.in(LitemallPointsExchangeOrder.PROP_NAME_exchangeStatus,
                    List.of(_AppMallDaoConstants.EXCHANGE_STATUS_PENDING,
                            _AppMallDaoConstants.EXCHANGE_STATUS_SHIPPED,
                            _AppMallDaoConstants.EXCHANGE_STATUS_COMPLETED)));
            long exchanged = findCount(userCountQuery, context);
            if (exchanged + quantity > maxPerUser) {
                throw new NopException(ERR_POINTS_GOODS_OVER_LIMIT_PER_USER)
                        .param("userId", userId)
                        .param("pointsGoodsId", pointsGoodsId)
                        .param("maxPerUser", maxPerUser)
                        .param("exchanged", exchanged);
            }
        }

        // Address snapshot for physical fulfillment. addressId may be null for virtual goods;
        // when provided it must belong to the current user (mirrors submit()/flashSaleBuy rule).
        String consignee = null;
        String phone = null;
        String fullAddress = null;
        if (!StringHelper.isEmpty(addressId)) {
            LitemallAddress address = addressBiz.get(addressId, false, context);
            if (address == null || Boolean.TRUE.equals(address.getDeleted())) {
                throw new NopException(ERR_EXCHANGE_ORDER_NOT_FOUND)
                        .param("reason", "address not found").param("addressId", addressId);
            }
            if (!Objects.equals(userId, address.getUserId())) {
                throw new NopException(ERR_EXCHANGE_ORDER_NOT_FOUND)
                        .param("reason", "address not owner").param("addressId", addressId);
            }
            consignee = address.getName();
            phone = address.getTel();
            fullAddress = (address.getProvince() != null ? address.getProvince() : "")
                    + (address.getCity() != null ? address.getCity() : "")
                    + (address.getCounty() != null ? address.getCounty() : "")
                    + (address.getAddressDetail() != null ? address.getAddressDetail() : "");
        }

        // Atomic exchange stock deduction (mapper). null/0 exchangeStock => 0 stock (sold out),
        // consistent with the activity-stock semantics. Affected=0 means sold out or lost a race.
        Integer exchangeStock = goods.getExchangeStock();
        if (exchangeStock == null || exchangeStock <= 0) {
            throw new NopException(ERR_POINTS_GOODS_SOLD_OUT)
                    .param("id", pointsGoodsId)
                    .param("exchangeStock", exchangeStock);
        }
        int affected = pointsGoodsMapper.reduceExchangeStock(pointsGoodsId, quantity);
        if (affected == 0) {
            throw new NopException(ERR_POINTS_GOODS_SOLD_OUT)
                    .param("id", pointsGoodsId)
                    .param("requested", quantity)
                    .param("exchangeStock", exchangeStock);
        }

        // Build the exchange order with denormalized goods snapshot so historical orders remain
        // readable even if the retail goods is later renamed or removed.
        int pointsPrice = safeInt(goods.getPointsPrice());
        int totalPoints = pointsPrice * quantity;
        LitemallPointsExchangeOrder order = newEntity();
        order.setUserId(userId);
        order.setPointsGoodsId(pointsGoodsId);
        order.setGoodsId(goods.getGoodsId());
        order.setProductId(goods.getProductId());
        order.setGoodsName(retail.getName());
        order.setPicUrl(retail.getPicUrl());
        order.setPointsPrice(pointsPrice);
        order.setQuantity(quantity);
        order.setTotalPoints(totalPoints);
        order.setAddressId(addressId);
        order.setConsignee(consignee);
        order.setPhone(phone);
        order.setFullAddress(fullAddress);
        order.setExchangeStatus(_AppMallDaoConstants.EXCHANGE_STATUS_PENDING);
        order.setRemark("积分兑换: " + retail.getName() + " x" + quantity);
        saveEntity(order, "exchange", context);

        // Flush so the PENDING order row exists before spendPoints runs. spendPoints internally
        // calls dao().clearEntitySessionCache() (see LitemallPointsAccountBizModel.mutateBalance),
        // which would otherwise evict this unflushed entity and cause a duplicate INSERT at commit.
        // After flush the row is persisted; clearSessionCache only detaches the in-memory object.
        dao().flushSession();

        // Deduct points via the P27 account spend API. sourceId = exchange order id enables
        // (sourceType, sourceId) idempotency: a duplicate exchange for the same order cannot double-spend.
        // If the user has insufficient points, spendPoints throws INSUFFICIENT and the surrounding
        // @BizMutation transaction rolls back the stock deduction + order INSERT automatically.
        pointsAccountBiz.spendPoints(userId, totalPoints,
                _AppMallDaoConstants.POINTS_CHANGE_TYPE_SPEND,
                SOURCE_TYPE_MALL_EXCHANGE, order.orm_idString(),
                "积分商城兑换: " + retail.getName() + " x" + quantity, context);

        // Re-read fresh so the returned managed entity survives the session-clear inside spendPoints.
        LitemallPointsExchangeOrder refreshed = get(order.orm_idString(), false, context);
        return refreshed != null ? refreshed : order;
    }

    @Override
    @BizQuery
    public Map<String, Object> myExchangeOrders(@Optional @Name("exchangeStatus") Integer exchangeStatus,
                                                 @Optional @Name("page") Integer page,
                                                 @Optional @Name("pageSize") Integer pageSize,
                                                 IServiceContext context) {
        String userId = context.getUserId();
        int pageNum = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallPointsExchangeOrder.PROP_NAME_userId, userId));
        if (exchangeStatus != null) {
            query.addFilter(FilterBeans.eq(LitemallPointsExchangeOrder.PROP_NAME_exchangeStatus, exchangeStatus));
        }
        query.addOrderField(LitemallPointsExchangeOrder.PROP_NAME_addTime, true);
        query.setOffset((pageNum - 1) * size);
        query.setLimit(size);

        // Count separately because CrudBizModel.findPage may report total=-1 when it skips the
        // count query for performance. findCount always executes a real COUNT query.
        QueryBean countQuery = new QueryBean();
        countQuery.addFilter(FilterBeans.eq(LitemallPointsExchangeOrder.PROP_NAME_userId, userId));
        if (exchangeStatus != null) {
            countQuery.addFilter(FilterBeans.eq(LitemallPointsExchangeOrder.PROP_NAME_exchangeStatus, exchangeStatus));
        }
        long total = findCount(countQuery, context);

        PageBean<LitemallPointsExchangeOrder> result = findPage(query, null, context);
        List<Map<String, Object>> list = new ArrayList<>();
        if (result != null && result.getItems() != null) {
            for (LitemallPointsExchangeOrder order : result.getItems()) {
                list.add(toOrderMap(order));
            }
        }

        Map<String, Object> out = new HashMap<>();
        out.put("page", pageNum);
        out.put("pageSize", size);
        out.put("total", total);
        out.put("list", list);
        return out;
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallPointsExchangeOrder shipExchangeOrder(@Name("id") String id,
                                                         @Name("shipCode") String shipCode,
                                                         IServiceContext context) {
        LitemallPointsExchangeOrder order = requireEntity(id, null, context);
        Integer status = order.getExchangeStatus();
        if (status == null || status != _AppMallDaoConstants.EXCHANGE_STATUS_PENDING) {
            throw new NopException(ERR_EXCHANGE_STATUS_TRANSITION_INVALID)
                    .param("id", id)
                    .param("currentStatus", status)
                    .param("targetStatus", _AppMallDaoConstants.EXCHANGE_STATUS_SHIPPED);
        }
        if (StringHelper.isEmpty(shipCode)) {
            throw new NopException(ERR_EXCHANGE_STATUS_TRANSITION_INVALID)
                    .param("id", id).param("reason", "shipCode is empty");
        }
        order.setExchangeStatus(_AppMallDaoConstants.EXCHANGE_STATUS_SHIPPED);
        order.setShipCode(shipCode);
        return order;
    }

    @Override
    @BizMutation
    public LitemallPointsExchangeOrder confirmExchangeOrder(@Name("id") String id, IServiceContext context) {
        LitemallPointsExchangeOrder order = requireOwner(id, context);
        Integer status = order.getExchangeStatus();
        if (status == null || status != _AppMallDaoConstants.EXCHANGE_STATUS_SHIPPED) {
            throw new NopException(ERR_EXCHANGE_STATUS_TRANSITION_INVALID)
                    .param("id", id)
                    .param("currentStatus", status)
                    .param("targetStatus", _AppMallDaoConstants.EXCHANGE_STATUS_COMPLETED);
        }
        order.setExchangeStatus(_AppMallDaoConstants.EXCHANGE_STATUS_COMPLETED);
        return order;
    }

    @Override
    @BizMutation
    public LitemallPointsExchangeOrder cancelExchangeOrder(@Name("id") String id,
                                                           @Optional @Name("remark") String remark,
                                                           IServiceContext context) {
        // PENDING orders can be cancelled by the owner; the stock is restored and points refunded.
        // COMPLETED orders cannot be cancelled (fulfillment already done — would need an after-sale flow).
        LitemallPointsExchangeOrder order = requireOwner(id, context);
        Integer status = order.getExchangeStatus();
        if (status == null || status != _AppMallDaoConstants.EXCHANGE_STATUS_PENDING) {
            throw new NopException(ERR_EXCHANGE_STATUS_TRANSITION_INVALID)
                    .param("id", id)
                    .param("currentStatus", status)
                    .param("targetStatus", _AppMallDaoConstants.EXCHANGE_STATUS_CANCELLED);
        }

        // Restore exchange stock (mapper). exchangedCount is clamped at 0 to survive manual fixes.
        int qty = safeInt(order.getQuantity());
        if (qty > 0) {
            pointsGoodsMapper.restoreExchangeStock(order.getPointsGoodsId(), qty);
        }

        // Refund points via the P27 account earn API. sourceId = exchange order id, distinct
        // sourceType (mall-exchange-refund) so it does not collide with the spend's idempotency key
        // and the earn's (sourceType, sourceId) idempotency check. If this somehow throws, the
        // @BizMutation tx rolls back the stock restore + status change.
        int totalPoints = safeInt(order.getTotalPoints());
        if (totalPoints > 0) {
            pointsAccountBiz.earnPoints(order.getUserId(), totalPoints,
                    _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN,
                    SOURCE_TYPE_MALL_EXCHANGE_REFUND, order.orm_idString(),
                    "积分兑换取消退还", context);
        }

        order.setExchangeStatus(_AppMallDaoConstants.EXCHANGE_STATUS_CANCELLED);
        if (!StringHelper.isEmpty(remark)) {
            order.setRemark((order.getRemark() != null ? order.getRemark() + " | " : "") + "取消: " + remark);
        }
        return order;
    }

    private LitemallPointsGoods requirePointsGoods(String id, IServiceContext context) {
        LitemallPointsGoods goods = pointsGoodsBiz.get(id, false, context);
        if (goods == null || Boolean.TRUE.equals(goods.getDeleted())) {
            throw new NopException(ERR_POINTS_GOODS_NOT_FOUND).param("id", id);
        }
        return goods;
    }

    private LitemallPointsExchangeOrder requireOwner(String id, IServiceContext context) {
        LitemallPointsExchangeOrder order = requireEntity(id, null, context);
        String userId = context.getUserId();
        if (StringHelper.isEmpty(userId) || !Objects.equals(userId, order.getUserId())) {
            throw new NopException(ERR_EXCHANGE_ORDER_NOT_FOUND)
                    .param("id", id).param("reason", "not owner");
        }
        return order;
    }

    private Map<String, Object> toOrderMap(LitemallPointsExchangeOrder order) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", order.orm_idString());
        m.put("pointsGoodsId", order.getPointsGoodsId());
        m.put("goodsId", order.getGoodsId());
        m.put("productId", order.getProductId());
        m.put("goodsName", order.getGoodsName());
        m.put("picUrl", order.getPicUrl());
        m.put("pointsPrice", order.getPointsPrice());
        m.put("quantity", order.getQuantity());
        m.put("totalPoints", order.getTotalPoints());
        m.put("consignee", order.getConsignee());
        m.put("phone", order.getPhone());
        m.put("fullAddress", order.getFullAddress());
        m.put("exchangeStatus", order.getExchangeStatus());
        m.put("shipCode", order.getShipCode());
        m.put("remark", order.getRemark());
        m.put("addTime", order.getAddTime());
        return m;
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
