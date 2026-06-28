package app.mall.service.entity;

import app.mall.biz.ILitemallAddressBiz;
import app.mall.biz.ILitemallGoodsBiz;
import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.biz.ILitemallPointsExchangeOrderBiz;
import app.mall.biz.ILitemallPointsGoodsBiz;
import app.mall.biz.ILitemallWalletBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallPointsExchangeOrder;
import app.mall.dao.entity.LitemallPointsGoods;
import app.mall.dao.entity.LitemallWallet;
import app.mall.dao.mapper.LitemallPointsExchangeOrderMapper;
import app.mall.dao.mapper.LitemallPointsGoodsMapper;
import app.mall.pay.IPayChannelRegistry;
import app.mall.pay.PayChannel;
import app.mall.pay.PayPrepayRequestBean;
import app.mall.pay.PayPrepayResponseBean;
import app.mall.pay.PayRefundRequestBean;
import app.mall.pay.PayRefundResponseBean;
import io.nop.api.core.annotations.biz.BizAction;
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
import io.nop.auth.core.password.IPasswordEncoder;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.orm.IOrmEntity;
import io.nop.orm.IOrmTemplate;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static app.mall.service.AppMallErrors.ERR_EXCHANGE_BALANCE_INSUFFICIENT;
import static app.mall.service.AppMallErrors.ERR_EXCHANGE_CASH_PRICE_REQUIRED;
import static app.mall.service.AppMallErrors.ERR_EXCHANGE_NOT_ALLOW_PAY;
import static app.mall.service.AppMallErrors.ERR_EXCHANGE_ORDER_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_EXCHANGE_PAY_CHANNEL_DISABLED;
import static app.mall.service.AppMallErrors.ERR_EXCHANGE_PAY_CREDENTIAL_INVALID;
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

    private static final Logger LOG = LoggerFactory.getLogger(LitemallPointsExchangeOrderBizModel.class);

    // sourceType taxonomy: spend on exchange vs earn-back on cancellation.
    // See docs/design/marketing-and-promotions.md (积分商城兑换 与 spendPoints 约定).
    public static final String SOURCE_TYPE_MALL_EXCHANGE = "mall-exchange";
    public static final String SOURCE_TYPE_MALL_EXCHANGE_REFUND = "mall-exchange-refund";
    // Combo-exchange cash component: wallet debit on balance pay / wallet credit on refund.
    public static final String SOURCE_TYPE_MALL_EXCHANGE_PAY = "mall-exchange-pay";

    // Combo-exchange outTradeNo is derived (not stored): "PE" + zero-padded exchangeOrderId,
    // e.g. PE00000001. Mirrors the recharge RC derivation (LitemallRechargeBizModel). 6–32 chars,
    // within WeChat's limit. Strip "PE" + parseInt to recover the exchange order id.
    static final String OUT_TRADE_NO_PREFIX = "PE";

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

    @Inject
    ILitemallWalletBiz walletBiz;

    @Inject
    IPayChannelRegistry payChannelRegistry;

    @Inject
    IPasswordEncoder passwordEncoder;

    @Inject
    IOrmTemplate ormTemplate;

    @Inject
    LitemallPointsExchangeOrderMapper exchangeOrderMapper;

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
        // PENDING orders (pure-points or combo already paid) and AWAITING_PAYMENT combo orders can be
        // cancelled by the owner. COMPLETED orders cannot be cancelled (fulfillment already done).
        LitemallPointsExchangeOrder order = requireOwner(id, context);
        Integer status = order.getExchangeStatus();
        if (status == null
                || (status != _AppMallDaoConstants.EXCHANGE_STATUS_PENDING
                && status != _AppMallDaoConstants.EXCHANGE_STATUS_AWAITING_PAYMENT)) {
            throw new NopException(ERR_EXCHANGE_STATUS_TRANSITION_INVALID)
                    .param("id", id)
                    .param("currentStatus", status)
                    .param("targetStatus", _AppMallDaoConstants.EXCHANGE_STATUS_CANCELLED);
        }
        cancelComboInternal(order, context);
        if (!StringHelper.isEmpty(remark)) {
            order.setRemark((order.getRemark() != null ? order.getRemark() + " | " : "") + "取消: " + remark);
        }
        return order;
    }

    // ===== 积分+现金组合兑换（successor） =====
    // See docs/design/marketing-and-promotions.md 「组合兑换流程」(Decisions E1-E5).

    @Override
    @BizMutation
    public Map<String, Object> exchangeCombo(@Name("pointsGoodsId") String pointsGoodsId,
                                              @Name("quantity") Integer quantity,
                                              @Optional @Name("addressId") String addressId,
                                              IServiceContext context) {
        // Decision E3: points deducted up-front (same as pure-points exchange), cash paid afterwards.
        // Combo requires PointsGoods.cashPrice > 0 (E1). The order is created in AWAITING_PAYMENT
        // (points already spent, stock already reduced, cash pending); a timeout-cancel job restores
        // stock + refunds points if the user never pays (E3 backstop).
        String userId = context.getUserId();
        if (StringHelper.isEmpty(userId)) {
            throw new NopException(ERR_EXCHANGE_ORDER_NOT_FOUND).param("reason", "no authenticated user");
        }
        if (quantity == null || quantity <= 0) {
            throw new NopException(ERR_EXCHANGE_QUANTITY_INVALID).param("quantity", quantity);
        }

        LitemallPointsGoods goods = requirePointsGoods(pointsGoodsId, context);

        // E1: cashPrice must be configured (>0) for the combo path.
        BigDecimal unitCash = goods.getCashPrice();
        if (unitCash == null || unitCash.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NopException(ERR_EXCHANGE_CASH_PRICE_REQUIRED)
                    .param("id", pointsGoodsId)
                    .param("cashPrice", unitCash);
        }

        // Activity status + time window + retail on-sale guards (same rule as pure-points).
        if (goods.getStatus() == null
                || goods.getStatus() != _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE) {
            throw new NopException(ERR_POINTS_GOODS_NOT_ACTIVE)
                    .param("id", pointsGoodsId).param("status", goods.getStatus());
        }
        LocalDateTime now = CoreMetrics.currentDateTime();
        if (!LitemallPointsGoodsBizModel.isInWindow(goods, now)) {
            throw new NopException(ERR_POINTS_GOODS_NOT_IN_WINDOW)
                    .param("id", pointsGoodsId)
                    .param("startTime", goods.getStartTime())
                    .param("endTime", goods.getEndTime());
        }
        LitemallGoods retail = goodsBiz.get(goods.getGoodsId(), false, context);
        if (retail == null || Boolean.TRUE.equals(retail.getDeleted())
                || !Boolean.TRUE.equals(retail.getIsOnSale())) {
            throw new NopException(ERR_POINTS_GOODS_OFF_SHELF).param("goodsId", goods.getGoodsId());
        }

        // maxPerUser guard. Combo counts AWAITING_PAYMENT too (an unpaid combo order still reserves
        // the user's quota — plan audit minor). Expressed as `in` over non-final statuses because the
        // GraphQL filter whitelist only allows {eq, in, dateBetween, dateTimeBetween}.
        Integer maxPerUser = goods.getMaxPerUser();
        if (maxPerUser != null && maxPerUser > 0) {
            QueryBean userCountQuery = new QueryBean();
            userCountQuery.addFilter(FilterBeans.eq(LitemallPointsExchangeOrder.PROP_NAME_pointsGoodsId, pointsGoodsId));
            userCountQuery.addFilter(FilterBeans.eq(LitemallPointsExchangeOrder.PROP_NAME_userId, userId));
            userCountQuery.addFilter(FilterBeans.in(LitemallPointsExchangeOrder.PROP_NAME_exchangeStatus,
                    List.of(_AppMallDaoConstants.EXCHANGE_STATUS_AWAITING_PAYMENT,
                            _AppMallDaoConstants.EXCHANGE_STATUS_PENDING,
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

        // Address snapshot for physical fulfillment (same rule as pure-points).
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

        // Atomic exchange stock deduction (mapper). Rejection before any points side effect.
        Integer exchangeStock = goods.getExchangeStock();
        if (exchangeStock == null || exchangeStock <= 0) {
            throw new NopException(ERR_POINTS_GOODS_SOLD_OUT)
                    .param("id", pointsGoodsId).param("exchangeStock", exchangeStock);
        }
        int affected = pointsGoodsMapper.reduceExchangeStock(pointsGoodsId, quantity);
        if (affected == 0) {
            throw new NopException(ERR_POINTS_GOODS_SOLD_OUT)
                    .param("id", pointsGoodsId)
                    .param("requested", quantity)
                    .param("exchangeStock", exchangeStock);
        }

        int pointsPrice = safeInt(goods.getPointsPrice());
        int totalPoints = pointsPrice * quantity;
        BigDecimal totalCash = unitCash.multiply(BigDecimal.valueOf(quantity));

        // Build the combo order: AWAITING_PAYMENT (cash pending), payStatus=UNPAID, cashPrice snapshot.
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
        order.setExchangeStatus(_AppMallDaoConstants.EXCHANGE_STATUS_AWAITING_PAYMENT);
        order.setPayStatus(_AppMallDaoConstants.PAY_STATUS_UNPAID);
        order.setCashPrice(unitCash);
        order.setRemark("组合兑换: " + retail.getName() + " x" + quantity);
        saveEntity(order, "exchangeCombo", context);

        // Flush so the AWAITING_PAYMENT row exists before spendPoints runs (spendPoints clears the
        // entity session cache internally — same reason as the pure-points exchange path).
        dao().flushSession();

        // Deduct points up-front (E3). sourceId = exchange order id => (sourceType, sourceId) idempotency.
        pointsAccountBiz.spendPoints(userId, totalPoints,
                _AppMallDaoConstants.POINTS_CHANGE_TYPE_SPEND,
                SOURCE_TYPE_MALL_EXCHANGE, order.orm_idString(),
                "积分商城组合兑换: " + retail.getName() + " x" + quantity, context);

        // Derive outTradeNo (E4): "PE" + zero-padded id. Returned to the cashier so the frontend can
        // drive payComboByChannel / payComboByBalance.
        String outTradeNo = deriveOutTradeNo(order.orm_idString());

        LitemallPointsExchangeOrder refreshed = get(order.orm_idString(), false, context);
        LitemallPointsExchangeOrder result = refreshed != null ? refreshed : order;
        Map<String, Object> out = toComboCashierMap(result, outTradeNo, totalCash);
        return out;
    }

    @Override
    @BizMutation
    public Map<String, Object> payComboByChannel(@Name("id") String id,
                                                  @Name("payChannelCode") String payChannelCode,
                                                  IServiceContext context) {
        // Third-party channel prepay (E4: consumes the P30 PayChannel strategy, no new integration).
        // Records the chosen channel on the order up-front; the verified SUCCESS notify
        // (confirmExchangePaidByNotify) is the authoritative pay confirmation that advances status.
        LitemallPointsExchangeOrder order = requireComboOwner(id, context);
        if (order.getExchangeStatus() == null
                || order.getExchangeStatus() != _AppMallDaoConstants.EXCHANGE_STATUS_AWAITING_PAYMENT) {
            throw new NopException(ERR_EXCHANGE_NOT_ALLOW_PAY)
                    .param("id", id)
                    .param("status", order.getExchangeStatus());
        }
        PayChannel channel = payChannelRegistry.getChannel(payChannelCode);
        // Only enabled third-party channels (WECHAT/ALIPAY) are accepted here. BALANCE has no
        // PayChannel bean (E5) and must go through payComboByBalance.
        if (channel == null || !channel.isEnabled() || "BALANCE".equals(payChannelCode)) {
            throw new NopException(ERR_EXCHANGE_PAY_CHANNEL_DISABLED)
                    .param("payChannelCode", payChannelCode);
        }

        BigDecimal totalCash = totalCashOf(order);
        String outTradeNo = deriveOutTradeNo(id);
        PayPrepayRequestBean req = new PayPrepayRequestBean();
        req.setOutTradeNo(outTradeNo);
        req.setTotalFee(totalCash);
        req.setDescription("积分组合兑换 " + outTradeNo);
        PayPrepayResponseBean resp = channel.prepay(req);

        // Record the channel on the order so the callback need not infer it (routing is by outTradeNo
        // prefix only). payStatus stays UNPAID until the verified notify arrives.
        order.setPayChannel(payChannelIntCode(payChannelCode));
        updateEntity(order, "payComboByChannel", context);

        Map<String, Object> out = new HashMap<>();
        out.put("id", id);
        out.put("outTradeNo", outTradeNo);
        out.put("payChannelCode", payChannelCode);
        out.put("codeUrl", resp.getCodeUrl());
        out.put("tradeType", resp.getTradeType());
        return out;
    }

    @Override
    @BizMutation
    public LitemallPointsExchangeOrder payComboByBalance(@Name("id") String id,
                                                          @Name("confirmCredential") String confirmCredential,
                                                          IServiceContext context) {
        // E5: balance component goes through direct debitBalance (mimic payByBalance), NOT a
        // BalancePayChannel bean. Synchronous: debits the wallet, sets channel=BALANCE +
        // walletPayAmount, and advances AWAITING_PAYMENT→PENDING in one @BizMutation tx.
        LitemallPointsExchangeOrder order = requireComboOwner(id, context);
        if (order.getExchangeStatus() == null
                || order.getExchangeStatus() != _AppMallDaoConstants.EXCHANGE_STATUS_AWAITING_PAYMENT) {
            throw new NopException(ERR_EXCHANGE_NOT_ALLOW_PAY)
                    .param("id", id)
                    .param("status", order.getExchangeStatus());
        }
        BigDecimal totalCash = totalCashOf(order);
        if (totalCash.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NopException(ERR_EXCHANGE_NOT_ALLOW_PAY)
                    .param("id", id).param("reason", "non-positive cash amount");
        }

        String userId = order.getUserId();
        verifyPayCredential(userId, confirmCredential, context);

        // Balance sufficiency pre-check (the optimistic-lock debit is the authoritative guard).
        BigDecimal balance = lookupWalletBalance(userId, context);
        if (balance.compareTo(totalCash) < 0) {
            throw new NopException(ERR_EXCHANGE_BALANCE_INSUFFICIENT)
                    .param("id", id)
                    .param("balance", balance)
                    .param("totalCash", totalCash);
        }

        // Atomic debit. sourceId = exchange order id ties the wallet flow to this combo order.
        walletBiz.debitBalance(userId, totalCash,
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_PAY,
                SOURCE_TYPE_MALL_EXCHANGE_PAY,
                id,
                "组合兑换余额支付 " + id,
                context);

        // Reload (debitBalance clears the entity session cache) before advancing state.
        LitemallPointsExchangeOrder fresh = get(id, false, context);
        fresh.setWalletPayAmount(totalCash);
        fresh.setPayChannel(_AppMallDaoConstants.PAY_CHANNEL_BALANCE);
        fresh.setPayStatus(_AppMallDaoConstants.PAY_STATUS_PAID);
        fresh.setExchangeStatus(_AppMallDaoConstants.EXCHANGE_STATUS_PENDING);
        updateEntity(fresh, "payComboByBalance", context);
        return fresh;
    }

    @Override
    @BizAction
    public void confirmExchangePaidByNotify(@Name("outTradeNo") String outTradeNo,
                                             @Name("transactionId") String transactionId,
                                             IServiceContext context) {
        // Trusted entry invoked by PaymentCallbackImpl after channel signature verification, when
        // outTradeNo starts with the PE prefix. Idempotent: a duplicate/replayed notify for an order
        // that already left AWAITING_PAYMENT is a no-op.
        String id = parseExchangeId(outTradeNo);
        if (id == null) {
            return;
        }
        LitemallPointsExchangeOrder order = get(id, false, context);
        if (order == null) {
            return;
        }
        if (order.getExchangeStatus() == null
                || order.getExchangeStatus() != _AppMallDaoConstants.EXCHANGE_STATUS_AWAITING_PAYMENT) {
            return;
        }
        // payChannel was recorded at payComboByChannel time; payStatus advances UNPAID→PAID here as the
        // authoritative channel-side confirmation, and the order becomes PENDING (ready to ship).
        order.setPayStatus(_AppMallDaoConstants.PAY_STATUS_PAID);
        order.setExchangeStatus(_AppMallDaoConstants.EXCHANGE_STATUS_PENDING);
        updateEntity(order, "confirmExchangePaidByNotify", context);
    }

    @Override
    @BizAction
    public void refundExchangeOrderByNotify(@Name("outTradeNo") String outTradeNo,
                                            @Optional @Name("outRefundNo") String outRefundNo,
                                            IServiceContext context) {
        // Trusted entry invoked by PaymentCallbackImpl.onRefundSuccess after channel-side refund
        // signature verification, when outTradeNo starts with PE. Symmetric to recharge-side
        // refundRechargeByNotify: idempotent, advances payStatus PAID→REFUNDED only.
        String id = parseExchangeId(outTradeNo);
        if (id == null) {
            return;
        }
        LitemallPointsExchangeOrder order = get(id, true, context);
        if (order == null) {
            return;
        }
        Integer payStatus = order.getPayStatus();
        if (payStatus == null
                || payStatus == _AppMallDaoConstants.PAY_STATUS_UNPAID
                || payStatus == _AppMallDaoConstants.PAY_STATUS_REFUNDED) {
            return;
        }
        if (payStatus != _AppMallDaoConstants.PAY_STATUS_PAID) {
            LOG.warn("refundExchangeOrderByNotify: order {} in unexpected payStatus {}, skipping. outRefundNo={}",
                    id, payStatus, outRefundNo);
            return;
        }
        // Reload fresh (the sync refund path in cancelComboInternal may have cleared the session cache)
        // before advancing status.
        LitemallPointsExchangeOrder fresh = get(id, false, context);
        fresh.setPayStatus(_AppMallDaoConstants.PAY_STATUS_REFUNDED);
        updateEntity(fresh, "refundExchangeOrderByNotify", context);
        LOG.info("refundExchangeOrderByNotify: reconciled combo-exchange refund for id={}, outRefundNo={}",
                id, outRefundNo);
    }

    @Override
    @BizMutation
    public int cancelExpiredExchangeOrders(@Name("timeoutMinutes") int timeoutMinutes,
                                            IServiceContext context) {
        // E3 backstop: scan AWAITING_PAYMENT combo orders past the payment timeout and cancel them
        // (restore stock + refund points; cash was never paid so no cash refund). Mirrors
        // LitemallOrderBizModel.cancelExpiredOrders, incl. the atomic status guard against a concurrent
        // user-initiated cancel.
        LocalDateTime cutoff = CoreMetrics.currentDateTime().minusMinutes(timeoutMinutes);
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallPointsExchangeOrder.PROP_NAME_exchangeStatus,
                _AppMallDaoConstants.EXCHANGE_STATUS_AWAITING_PAYMENT));
        query.addFilter(FilterBeans.le(LitemallPointsExchangeOrder.PROP_NAME_addTime, cutoff));
        query.setLimit(100);

        List<LitemallPointsExchangeOrder> orders = doFindListByQueryDirectly(query, context);
        List<String> ids = new ArrayList<>();
        for (LitemallPointsExchangeOrder o : orders) {
            ids.add(o.orm_idString());
        }
        dao().clearEntitySessionCache();

        int count = 0;
        for (String id : ids) {
            int affected = exchangeOrderMapper.updateStatusIfMatch(
                    id,
                    _AppMallDaoConstants.EXCHANGE_STATUS_CANCELLED,
                    _AppMallDaoConstants.EXCHANGE_STATUS_AWAITING_PAYMENT);
            if (affected == 0) {
                continue;
            }
            LitemallPointsExchangeOrder order = get(id, false, context);
            cancelComboInternal(order, context);
            count++;
        }
        return count;
    }

    /**
     * Shared combo cancellation body: restore exchange stock, refund points, and — if cash was paid —
     * initiate a cash refund (balance: synchronous creditBalance + payStatus=REFUNDED; third-party
     * channel: initiate {@code payChannel.refund}, payStatus stays PAID until the refund notify
     * {@link #refundExchangeOrderByNotify} reconciles it to REFUNDED). The caller is responsible for
     * the status guard + ownership check; this method does NOT advance exchangeStatus (the caller's
     * atomic guard / state transition already did, or the order is reloaded afterwards).
     */
    private void cancelComboInternal(LitemallPointsExchangeOrder order, IServiceContext context) {
        int qty = safeInt(order.getQuantity());
        if (qty > 0) {
            pointsGoodsMapper.restoreExchangeStock(order.getPointsGoodsId(), qty);
        }

        // Refund points (sourceType=mall-exchange-refund keeps the earn's idempotency key distinct
        // from the spend's mall-exchange key).
        int totalPoints = safeInt(order.getTotalPoints());
        if (totalPoints > 0) {
            pointsAccountBiz.earnPoints(order.getUserId(), totalPoints,
                    _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN,
                    SOURCE_TYPE_MALL_EXCHANGE_REFUND, order.orm_idString(),
                    "积分兑换取消退还", context);
        }

        // Cash refund only when cash was actually paid. A combo order in AWAITING_PAYMENT that was
        // never paid (payStatus UNPAID/null) has no cash to return.
        Integer payStatus = order.getPayStatus();
        BigDecimal totalCash = totalCashOf(order);
        if (payStatus != null && payStatus == _AppMallDaoConstants.PAY_STATUS_PAID
                && totalCash.compareTo(BigDecimal.ZERO) > 0) {
            refundComboCash(order, totalCash, context);
        }

        order.setExchangeStatus(_AppMallDaoConstants.EXCHANGE_STATUS_CANCELLED);
        updateEntity(order, "cancelComboInternal", context);
    }

    /**
     * Refund the cash component of a paid combo order. Balance path is synchronous (creditBalance +
     * payStatus=REFUNDED immediately). Third-party channel path initiates an async refund; on sync
     * success payStatus stays PAID (the refund notify advances it to REFUNDED), on failure the cancel
     * still proceeds (points/stock restored) and the failed refund is surfaced for manual retry.
     */
    private void refundComboCash(LitemallPointsExchangeOrder order, BigDecimal totalCash,
                                 IServiceContext context) {
        Integer channel = order.getPayChannel();
        if (channel != null && channel == _AppMallDaoConstants.PAY_CHANNEL_BALANCE) {
            walletBiz.creditBalance(order.getUserId(), totalCash,
                    _AppMallDaoConstants.WALLET_CHANGE_TYPE_REFUND,
                    SOURCE_TYPE_MALL_EXCHANGE_REFUND,
                    order.orm_idString(),
                    "组合兑换取消退还余额 " + order.orm_idString(),
                    context);
            order.setPayStatus(_AppMallDaoConstants.PAY_STATUS_REFUNDED);
            return;
        }
        // Third-party channel (WECHAT/ALIPAY): initiate async refund via the channel strategy.
        PayChannel ch = channel != null ? payChannelRegistry.getChannel(payChannelCodeString(channel)) : null;
        if (ch == null) {
            LOG.warn("refundComboCash: order {} has unknown payChannel {}, cannot initiate refund. "
                    + "Manual reconciliation required.", order.orm_idString(), channel);
            return;
        }
        PayRefundRequestBean req = new PayRefundRequestBean();
        req.setOutTradeNo(deriveOutTradeNo(order.orm_idString()));
        req.setOutRefundNo("PER" + deriveOutTradeNo(order.orm_idString()).substring(OUT_TRADE_NO_PREFIX.length()));
        req.setTotalFee(totalCash);
        req.setRefundFee(totalCash);
        PayRefundResponseBean resp = ch.refund(req);
        if (!resp.isSuccess()) {
            LOG.warn("refundComboCash: channel refund failed for order {} (channel={}, errorCode={}, message={}). "
                            + "Points/stock already restored; cash refund needs manual retry.",
                    order.orm_idString(), ch.getCode(), resp.getErrorCode(), resp.getErrorMessage());
        }
        // payStatus intentionally left PAID: the authoritative REFUNDED advance happens in the
        // refundExchangeOrderByNotify reconciliation once the channel confirms the refund completed.
    }

    private LitemallPointsExchangeOrder requireComboOwner(String id, IServiceContext context) {
        LitemallPointsExchangeOrder order = requireOwner(id, context);
        // Combo orders carry a non-null cashPrice snapshot. Defend against routing a pure-points order
        // (cashPrice null) into the combo cashier.
        if (order.getCashPrice() == null) {
            throw new NopException(ERR_EXCHANGE_CASH_PRICE_REQUIRED)
                    .param("id", id).param("reason", "not a combo exchange order");
        }
        return order;
    }

    private void verifyPayCredential(String userId, String confirmCredential, IServiceContext context) {
        // Mirrors LitemallOrderBizModel.verifyPayCredential: reuse the login password via the platform
        // IPasswordEncoder for the balance-payment fund-safety check.
        if (StringHelper.isEmpty(confirmCredential)) {
            throw new NopException(ERR_EXCHANGE_PAY_CREDENTIAL_INVALID).param("userId", userId);
        }
        IOrmEntity authUser = ormTemplate.get(io.nop.auth.dao.entity.NopAuthUser.class.getName(), userId);
        if (authUser == null) {
            throw new NopException(ERR_EXCHANGE_PAY_CREDENTIAL_INVALID).param("userId", userId);
        }
        String encoded = (String) authUser.orm_propValueByName("password");
        String salt = (String) authUser.orm_propValueByName("salt");
        if (StringHelper.isEmpty(encoded)
                || !passwordEncoder.passwordMatches(confirmCredential, salt, encoded)) {
            throw new NopException(ERR_EXCHANGE_PAY_CREDENTIAL_INVALID).param("userId", userId);
        }
    }

    private BigDecimal lookupWalletBalance(String userId, IServiceContext context) {
        LitemallWallet wallet = walletBiz.getMyWallet(context);
        BigDecimal balance = wallet != null ? wallet.getBalance() : null;
        return balance != null ? balance : BigDecimal.ZERO;
    }

    static String deriveOutTradeNo(String exchangeOrderId) {
        long id = Long.parseLong(exchangeOrderId);
        return OUT_TRADE_NO_PREFIX + String.format("%08d", id);
    }

    static String parseExchangeId(String outTradeNo) {
        if (StringHelper.isEmpty(outTradeNo) || !outTradeNo.startsWith(OUT_TRADE_NO_PREFIX)) {
            return null;
        }
        try {
            return Long.toString(Long.parseLong(outTradeNo.substring(OUT_TRADE_NO_PREFIX.length())));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal totalCashOf(LitemallPointsExchangeOrder order) {
        BigDecimal unitCash = order.getCashPrice();
        if (unitCash == null) {
            return BigDecimal.ZERO;
        }
        int qty = safeInt(order.getQuantity());
        return unitCash.multiply(BigDecimal.valueOf(qty));
    }

    private int payChannelIntCode(String code) {
        switch (code) {
            case "WECHAT":
                return _AppMallDaoConstants.PAY_CHANNEL_WECHAT;
            case "ALIPAY":
                return _AppMallDaoConstants.PAY_CHANNEL_ALIPAY;
            case "BALANCE":
                return _AppMallDaoConstants.PAY_CHANNEL_BALANCE;
            default:
                return _AppMallDaoConstants.PAY_CHANNEL_WECHAT;
        }
    }

    private String payChannelCodeString(int code) {
        if (code == _AppMallDaoConstants.PAY_CHANNEL_ALIPAY) {
            return "ALIPAY";
        }
        if (code == _AppMallDaoConstants.PAY_CHANNEL_BALANCE) {
            return "BALANCE";
        }
        return "WECHAT";
    }

    private Map<String, Object> toComboCashierMap(LitemallPointsExchangeOrder order, String outTradeNo,
                                                   BigDecimal totalCash) {
        Map<String, Object> m = toOrderMap(order);
        m.put("outTradeNo", outTradeNo);
        m.put("cashPrice", order.getCashPrice());
        m.put("totalCash", totalCash);
        m.put("payStatus", order.getPayStatus());
        return m;
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
        m.put("cashPrice", order.getCashPrice());
        m.put("payStatus", order.getPayStatus());
        m.put("payChannel", order.getPayChannel());
        m.put("walletPayAmount", order.getWalletPayAmount());
        m.put("addTime", order.getAddTime());
        return m;
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
