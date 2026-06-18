package app.mall.service.entity;

import app.mall.biz.ILitemallAddressBiz;
import app.mall.biz.ILitemallCartBiz;
import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallGoodsProductBiz;
import app.mall.biz.ILitemallGrouponBiz;
import app.mall.biz.ILitemallGrouponRulesBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallOrderGoodsBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallGrouponRules;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.dto.GoodsStatisticsBean;
import app.mall.dao.dto.OrderStatisticsBean;
import app.mall.dao.dto.UserStatisticsBean;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.manager.MallLogManager;
import app.mall.dao.mapper.LitemallGoodsProductMapper;
import app.mall.dao.mapper.LitemallOrderMapper;
import app.mall.pay.PayPrepayRequestBean;
import app.mall.pay.PayPrepayResponseBean;
import app.mall.pay.PayService;
import app.mall.service.notification.MallNotificationService;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static app.mall.service.AppMallErrors.ERR_GROUPON_RULES_NOT_AVAILABLE;
import static app.mall.service.AppMallErrors.ERR_ORDER_ADDRESS_INVALID;
import static app.mall.service.AppMallErrors.ERR_ORDER_CART_EMPTY;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_ALLOW_CANCEL;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_ALLOW_CONFIRM;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_ALLOW_DELETE;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_ALLOW_PAY;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_ALLOW_SHIP;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_ORDER_PRICE_INVALID;
import static app.mall.service.AppMallErrors.ERR_ORDER_STOCK_INSUFFICIENT;
import static app.mall.service.AppMallErrors.ERR_ORDER_USE_REAL_PAYMENT;

@BizModel("LitemallOrder")
public class LitemallOrderBizModel extends CrudBizModel<LitemallOrder> implements ILitemallOrderBiz {

    static final Logger LOG = LoggerFactory.getLogger(LitemallOrderBizModel.class);

    @Inject
    ILitemallCartBiz cartBiz;

    @Inject
    ILitemallAddressBiz addressBiz;

    @Inject
    ILitemallGoodsProductBiz goodsProductBiz;

    @Inject
    ILitemallOrderGoodsBiz orderGoodsBiz;

    @Inject
    LitemallGoodsProductMapper goodsProductMapper; // reduceStock/addStock: atomic SQL UPDATE for concurrent-safe stock deduction

    @Inject
    ILitemallCouponUserBiz couponUserBiz;

    @Inject
    ILitemallGrouponRulesBiz grouponRulesBiz;

    @Inject
    ILitemallGrouponBiz grouponBiz;

    @Inject
    ILitemallSystemBiz systemBiz;

    @Inject
    MallLogManager logManager;

    @Inject
    MallNotificationService notificationService;

    @Inject
    LitemallOrderMapper orderMapper;

    @Inject
    PayService payService;

    public LitemallOrderBizModel() {
        setEntityName(LitemallOrder.class.getName());
    }

    @Override
    @BizMutation
    public LitemallOrder submit(@Name("addressId") String addressId,
                                 @Optional @Name("message") String message,
                                 @Name("freightPrice") BigDecimal freightPrice,
                                 @Optional @Name("couponUserId") String couponUserId,
                                 @Optional @Name("grouponRulesId") String grouponRulesId,
                                 @Optional @Name("grouponId") String grouponId,
                                 IServiceContext context) {
        String userId = context.getUserId();

        LitemallAddress address = addressBiz.get(addressId, false, context);
        if (address == null || Boolean.TRUE.equals(address.getDeleted())) {
            throw new NopException(ERR_ORDER_ADDRESS_INVALID)
                    .param("addressId", addressId);
        }
        if (!Objects.equals(userId, address.getUserId())) {
            throw new NopException(ERR_ORDER_ADDRESS_INVALID)
                    .param("addressId", addressId)
                    .param("userId", userId);
        }

        QueryBean cartQuery = new QueryBean();
        cartQuery.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_userId, userId));
        cartQuery.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_checked, true));
        cartQuery.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_deleted, false));
        List<LitemallCart> checkedItems = cartBiz.findList(cartQuery, null, context);

        if (checkedItems == null || checkedItems.isEmpty()) {
            throw new NopException(ERR_ORDER_CART_EMPTY);
        }

        for (LitemallCart item : checkedItems) {
            LitemallGoodsProduct product = goodsProductBiz.get(item.getProductId(), false, context);
            if (product == null || Boolean.TRUE.equals(product.getDeleted())) {
                throw new NopException(ERR_ORDER_STOCK_INSUFFICIENT)
                        .param("productId", item.getProductId());
            }
            if (product.getNumber() == null || item.getNumber() > product.getNumber()) {
                throw new NopException(ERR_ORDER_STOCK_INSUFFICIENT)
                        .param("productId", item.getProductId())
                        .param("stock", product.getNumber());
            }
        }

        LocalDateTime now = LocalDateTime.now();

        LitemallOrder order = newEntity();
        order.setUserId(userId);
        order.setOrderSn(generateOrderSn());
        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_CREATED);
        order.setAftersaleStatus(_AppMallDaoConstants.AFTERSALE_STATUS_INIT);

        order.setConsignee(address.getName());
        order.setMobile(address.getTel());
        String fullAddress = (address.getProvince() != null ? address.getProvince() : "")
                + (address.getCity() != null ? address.getCity() : "")
                + (address.getCounty() != null ? address.getCounty() : "")
                + (address.getAddressDetail() != null ? address.getAddressDetail() : "");
        order.setAddress(fullAddress);

        order.setMessage(message != null ? message : "");
        BigDecimal effectiveFreightPrice = freightPrice;
        if (effectiveFreightPrice == null) {
            String freightConfig = systemBiz.getConfig("mall_freight_price", context);
            effectiveFreightPrice = freightConfig != null ? new BigDecimal(freightConfig) : BigDecimal.ZERO;
        }
        order.setFreightPrice(effectiveFreightPrice);
        order.setCouponPrice(BigDecimal.ZERO);
        order.setIntegralPrice(BigDecimal.ZERO);
        order.setGrouponPrice(BigDecimal.ZERO);
        order.setComments(0);
        order.setDeleted(false);

        BigDecimal goodsPriceTotal = BigDecimal.ZERO;
        List<String> couponScopeIds = new ArrayList<>();
        for (LitemallCart item : checkedItems) {
            LitemallOrderGoods orderGoods = orderGoodsBiz.newEntity();
            LitemallGoodsProduct product = goodsProductBiz.requireEntity(item.getProductId(), null, context);
            orderGoods.setGoodsId(item.getGoodsId());
            orderGoods.setGoodsName(item.getGoodsName());
            orderGoods.setGoodsSn(item.getGoodsSn());
            orderGoods.setProductId(item.getProductId());
            orderGoods.setNumber(item.getNumber());
            orderGoods.setPrice(product.getPrice());
            orderGoods.setSpecifications(item.getSpecifications());
            orderGoods.getPicUrlComponent().copyFrom(item.getPicUrlComponent());
            orderGoods.setComment(0);
            order.getOrderGoods().add(orderGoods);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getNumber()));
            goodsPriceTotal = goodsPriceTotal.add(lineTotal);
            couponScopeIds.add(item.getGoodsId());
            if (product.getGoods() != null && product.getGoods().getCategoryId() != null) {
                couponScopeIds.add(product.getGoods().getCategoryId());
            }

            int stockAffected = goodsProductMapper.reduceStock(item.getProductId(), item.getNumber());
            if (stockAffected == 0) {
                // Concurrent deduction exhausted stock between pre-check and atomic UPDATE
                throw new NopException(ERR_ORDER_STOCK_INSUFFICIENT)
                        .param("productId", item.getProductId())
                        .param("requested", item.getNumber());
            }
        }

        order.setGoodsPrice(goodsPriceTotal);

        BigDecimal couponPrice = BigDecimal.ZERO;
        if (couponUserId != null && !couponUserId.isEmpty()) {
            couponPrice = couponUserBiz.selectCouponForOrder(couponUserId, goodsPriceTotal, couponScopeIds, context);
        }
        order.setCouponPrice(couponPrice);

        BigDecimal grouponPrice = BigDecimal.ZERO;
        if (grouponRulesId != null && !grouponRulesId.isEmpty()) {
            LitemallGrouponRules rules = grouponRulesBiz.requireEntity(grouponRulesId, null, context);
            if (rules.getStatus() != 0) {
                throw new NopException(ERR_GROUPON_RULES_NOT_AVAILABLE)
                        .param("grouponRulesId", grouponRulesId);
            }
            if (rules.getExpireTime() != null && !rules.getExpireTime().isAfter(now)) {
                throw new NopException(ERR_GROUPON_RULES_NOT_AVAILABLE)
                        .param("grouponRulesId", grouponRulesId);
            }
            boolean matchedGoods = checkedItems.stream()
                    .anyMatch(item -> Objects.equals(item.getGoodsId(), rules.getGoodsId()));
            if (!matchedGoods) {
                throw new NopException(ERR_GROUPON_RULES_NOT_AVAILABLE)
                        .param("grouponRulesId", grouponRulesId)
                        .param("goodsId", rules.getGoodsId());
            }
            grouponPrice = rules.getDiscount() != null ? rules.getDiscount() : BigDecimal.ZERO;
        }
        order.setGrouponPrice(grouponPrice);

        BigDecimal orderPrice = goodsPriceTotal;
        if (order.getFreightPrice() != null) {
            orderPrice = orderPrice.add(order.getFreightPrice());
        }
        orderPrice = orderPrice.subtract(couponPrice);
        order.setOrderPrice(orderPrice);

        BigDecimal actualPrice = orderPrice;
        if (order.getIntegralPrice() != null) {
            actualPrice = actualPrice.subtract(order.getIntegralPrice());
        }
        actualPrice = actualPrice.subtract(grouponPrice);
        order.setActualPrice(actualPrice);

        saveEntity(order, "submit", context);

        if (grouponRulesId != null && !grouponRulesId.isEmpty()) {
            String orderId = order.orm_idString();
            if (grouponId != null && !grouponId.isEmpty()) {
                grouponBiz.joinGroupon(grouponId, orderId, context);
            } else {
                grouponBiz.openGroupon(grouponRulesId, orderId, context);
            }
        }

        if (couponUserId != null && !couponUserId.isEmpty() && couponPrice.compareTo(BigDecimal.ZERO) > 0) {
            couponUserBiz.useCoupon(couponUserId, order.orm_idString(), context);
        }

        for (LitemallCart item : checkedItems) {
            cartBiz.delete(item.orm_idString(), context);
        }

        if (actualPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new NopException(ERR_ORDER_PRICE_INVALID).param("actualPrice", actualPrice);
        }
        if (actualPrice.compareTo(BigDecimal.ZERO) == 0) {
            order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_PAY);
            order.setPayTime(now);
            updateEntity(order, "submit:zeroPay", context);
        }

        final String adminNotifyOrderSn = order.getOrderSn();
        txn().afterCommit(null, () -> notificationService.sendAdminOrderNotification(adminNotifyOrderSn));

        return order;
    }

    @Override
    @BizMutation
    public LitemallOrder cancel(@Name("orderId") String orderId,
                                 IServiceContext context) {
        // Atomic status guard: win the CREATED→CANCEL transition before loading the entity into the session,
        // preventing concurrent double stock-rollback and double coupon-return (cancel vs cancelExpiredOrders).
        int affected = orderMapper.updateStatusIfMatch(
                orderId, _AppMallDaoConstants.ORDER_STATUS_CANCEL, _AppMallDaoConstants.ORDER_STATUS_CREATED);
        if (affected == 0) {
            LitemallOrder existing = get(orderId, false, context);
            if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
                throw new NopException(ERR_ORDER_NOT_FOUND)
                        .param("orderId", orderId);
            }
            throw new NopException(ERR_ORDER_NOT_ALLOW_CANCEL)
                    .param("orderId", orderId)
                    .param("status", existing.getOrderStatus());
        }

        LitemallOrder order = get(orderId, false, context);
        requireUserIdMatch(order, context);
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

        updateEntity(order, "cancel", context);
        logManager.logOrderSucceed("订单取消", "订单编号 " + order.getOrderSn());
        return order;
    }

    @Override
    @BizMutation
    public Map<String, Object> prepay(@Name("orderId") String orderId,
                                       IServiceContext context) {
        LitemallOrder order = get(orderId, false, context);
        if (order == null) {
            throw new NopException(ERR_ORDER_NOT_FOUND)
                    .param("orderId", orderId);
        }
        if (order.getOrderStatus() != _AppMallDaoConstants.ORDER_STATUS_CREATED) {
            throw new NopException(ERR_ORDER_NOT_ALLOW_PAY)
                    .param("orderId", orderId)
                    .param("status", order.getOrderStatus());
        }
        requireUserIdMatch(order, context);

        PayPrepayRequestBean payReq = new PayPrepayRequestBean();
        payReq.setOutTradeNo(order.getOrderSn());
        payReq.setTotalFee(order.getActualPrice());
        payReq.setDescription("商城订单 " + order.getOrderSn());

        PayPrepayResponseBean payResp = payService.createPayment(payReq);

        order.setPayId(payResp.getPayId());
        updateEntity(order, "prepay", context);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("codeUrl", payResp.getCodeUrl());
        return result;
    }

    @Override
    @BizMutation
    public LitemallOrder pay(@Name("orderId") String orderId,
                               IServiceContext context) {
        LitemallOrder order = get(orderId, false, context);
        if (order == null) {
            throw new NopException(ERR_ORDER_NOT_FOUND)
                    .param("orderId", orderId);
        }
        if (order.getOrderStatus() != _AppMallDaoConstants.ORDER_STATUS_CREATED) {
            throw new NopException(ERR_ORDER_NOT_ALLOW_PAY)
                    .param("orderId", orderId)
                    .param("status", order.getOrderStatus());
        }
        requireUserIdMatch(order, context);
        // pay() is the manual/demo confirmation path. In real WeChat Pay mode, non-zero orders
        // MUST go through prepay -> WeChat scan -> async notify -> confirmPaidByNotify.
        if (order.getActualPrice() != null
                && order.getActualPrice().compareTo(BigDecimal.ZERO) > 0
                && payService.isEnabled()) {
            throw new NopException(ERR_ORDER_USE_REAL_PAYMENT)
                    .param("orderId", orderId)
                    .param("actualPrice", order.getActualPrice());
        }

        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_PAY);
        order.setPayTime(LocalDateTime.now());
        updateEntity(order, "pay", context);
        final String payOrderSn = order.getOrderSn();
        final String payMobile = order.getMobile();
        txn().afterCommit(null, () -> notificationService.sendOrderPaymentNotification(payOrderSn, payMobile));
        return order;
    }

    @Override
    @BizMutation
    public void confirmPaidByNotify(@Name("outTradeNo") String outTradeNo,
                                     @Name("transactionId") String transactionId,
                                     IServiceContext context) {
        // Trusted entry invoked by WxPayNotifyResource after signature verification.
        // Find order by orderSn (== outTradeNo); orderSn is unique per order.
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderSn, outTradeNo));
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_deleted, false));
        LitemallOrder order = findFirst(query, null, context);
        if (order == null) {
            LOG.warn("confirmPaidByNotify: no order found for outTradeNo={}", outTradeNo);
            return;
        }
        // Idempotent: duplicate/replayed WeChat notifies for an already-paid order are a no-op
        if (order.getOrderStatus() != _AppMallDaoConstants.ORDER_STATUS_CREATED) {
            LOG.info("confirmPaidByNotify: order {} already in status {}, skip",
                    order.getOrderSn(), order.getOrderStatus());
            return;
        }
        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_PAY);
        order.setPayTime(LocalDateTime.now());
        if (transactionId != null && !transactionId.isEmpty()) {
            order.setPayId(transactionId);
        }
        updateEntity(order, "confirmPaidByNotify", context);
        final String orderSn = order.getOrderSn();
        final String mobile = order.getMobile();
        txn().afterCommit(null, () -> notificationService.sendOrderPaymentNotification(orderSn, mobile));
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallOrder ship(@Name("orderId") String orderId,
                                @Name("shipSn") String shipSn,
                                @Name("shipChannel") String shipChannel,
                               IServiceContext context) {
        LitemallOrder order = get(orderId, false, context);
        if (order == null) {
            throw new NopException(ERR_ORDER_NOT_FOUND)
                    .param("orderId", orderId);
        }
        if (order.getOrderStatus() != _AppMallDaoConstants.ORDER_STATUS_PAY) {
            throw new NopException(ERR_ORDER_NOT_ALLOW_SHIP)
                    .param("orderId", orderId)
                    .param("status", order.getOrderStatus());
        }

        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_SHIP);
        order.setShipSn(shipSn);
        order.setShipChannel(shipChannel);
        order.setShipTime(LocalDateTime.now());
        updateEntity(order, "ship", context);
        final String shipOrderSn = order.getOrderSn();
        final String shipMobile = order.getMobile();
        txn().afterCommit(null, () -> notificationService.sendOrderShipNotification(shipOrderSn, shipMobile));
        logManager.logOrderSucceed("订单发货", "订单编号 " + order.getOrderSn());
        return order;
    }

    @Override
    @BizMutation
    public LitemallOrder confirm(@Name("orderId") String orderId,
                                   IServiceContext context) {
        LitemallOrder order = get(orderId, false, context);
        if (order == null) {
            throw new NopException(ERR_ORDER_NOT_FOUND)
                    .param("orderId", orderId);
        }
        if (order.getOrderStatus() != _AppMallDaoConstants.ORDER_STATUS_SHIP) {
            throw new NopException(ERR_ORDER_NOT_ALLOW_CONFIRM)
                    .param("orderId", orderId)
                    .param("status", order.getOrderStatus());
        }
        requireUserIdMatch(order, context);

        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_CONFIRM);
        order.setConfirmTime(LocalDateTime.now());
        updateEntity(order, "confirm", context);
        return order;
    }

    @Override
    @BizMutation
    public void deleteOrder(@Name("orderId") String orderId,
                             IServiceContext context) {
        LitemallOrder order = get(orderId, false, context);
        if (order == null) {
            throw new NopException(ERR_ORDER_NOT_FOUND)
                    .param("orderId", orderId);
        }
        int status = order.getOrderStatus();
        if (status != _AppMallDaoConstants.ORDER_STATUS_CANCEL
                && status != _AppMallDaoConstants.ORDER_STATUS_AUTO_CANCEL
                && status != _AppMallDaoConstants.ORDER_STATUS_CONFIRM
                && status != _AppMallDaoConstants.ORDER_STATUS_AUTO_CONFIRM
                && status != _AppMallDaoConstants.ORDER_STATUS_REFUND_CONFIRM
                && status != _AppMallDaoConstants.ORDER_STATUS_GROUPON_EXPIRED) {
            throw new NopException(ERR_ORDER_NOT_ALLOW_DELETE)
                    .param("orderId", orderId)
                    .param("status", status);
        }
        delete(orderId, context);
    }

    @Override
    @BizQuery
    public List<LitemallOrder> myOrders(@Optional @Name("status") Integer status,
                                         IServiceContext context) {
        String userId = context.getUserId();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_deleted, false));
        if (status != null) {
            if (status == -1) {
                query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderStatus, _AppMallDaoConstants.ORDER_STATUS_CREATED));
            } else {
                query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderStatus, status));
            }
        }
        query.addOrderField(LitemallOrder.PROP_NAME_addTime, true);
        return findList(query, null, context);
    }

    @Override
    @BizQuery
    public LitemallOrder getMyOrder(@Name("orderId") String orderId,
                                     IServiceContext context) {
        LitemallOrder order = get(orderId, false, context);
        if (order == null || Boolean.TRUE.equals(order.getDeleted())) {
            throw new NopException(ERR_ORDER_NOT_FOUND)
                    .param("orderId", orderId);
        }
        if (!order.getUserId().equals(context.getUserId())) {
            throw new NopException(ERR_ORDER_NOT_FOUND)
                    .param("orderId", orderId);
        }
        return order;
    }

    private static final Timestamp MIN_TIMESTAMP = Timestamp.valueOf("1970-01-01 00:00:00");
    private static final Timestamp MAX_TIMESTAMP = Timestamp.valueOf("2099-12-31 23:59:59");

    @Override
    @BizQuery
    public OrderStatisticsBean getOrderStatistics(@Optional @Name("startDate") String startDate,
                                                   @Optional @Name("endDate") String endDate,
                                                   IServiceContext context) {
        Timestamp start = startDate != null && !startDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(startDate).atTime(LocalTime.MIN)) : MIN_TIMESTAMP;
        Timestamp end = endDate != null && !endDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(endDate).atTime(LocalTime.MAX)) : MAX_TIMESTAMP;
        return orderMapper.getOrderStatistics(start, end);
    }

    @Override
    @BizQuery
    public List<GoodsStatisticsBean> getGoodsSalesRanking(@Optional @Name("startDate") String startDate,
                                                          @Optional @Name("endDate") String endDate,
                                                          @Optional @Name("limit") Integer limit,
                                                          IServiceContext context) {
        int effectiveLimit = limit != null && limit > 0 ? limit : 10;
        Timestamp start = startDate != null && !startDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(startDate).atTime(LocalTime.MIN)) : MIN_TIMESTAMP;
        Timestamp end = endDate != null && !endDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(endDate).atTime(LocalTime.MAX)) : MAX_TIMESTAMP;
        return orderMapper.getGoodsSalesRanking(start, end, effectiveLimit);
    }

    @Override
    @BizQuery
    public UserStatisticsBean getUserStatistics(@Optional @Name("startDate") String startDate,
                                                 @Optional @Name("endDate") String endDate,
                                                 IServiceContext context) {
        Timestamp start = startDate != null && !startDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(startDate).atTime(LocalTime.MIN)) : MIN_TIMESTAMP;
        Timestamp end = endDate != null && !endDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(endDate).atTime(LocalTime.MAX)) : MAX_TIMESTAMP;

        LocalDateTime now = LocalDateTime.now();
        Timestamp todayStart = Timestamp.valueOf(now.toLocalDate().atTime(LocalTime.MIN));
        Timestamp weekStart = Timestamp.valueOf(now.minusWeeks(1));
        Timestamp monthStart = Timestamp.valueOf(now.minusMonths(1));

        return orderMapper.getUserStatistics(start, end, todayStart, weekStart, monthStart);
    }

    @Override
    @BizMutation
    public int cancelExpiredOrders(@Name("timeoutMinutes") int timeoutMinutes,
                                    IServiceContext context) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderStatus, _AppMallDaoConstants.ORDER_STATUS_CREATED));
        query.addFilter(FilterBeans.lt(LitemallOrder.PROP_NAME_addTime, cutoff));
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_deleted, false));
        query.setLimit(100);

        List<LitemallOrder> orders = doFindListByQueryDirectly(query, context);

        // Collect IDs then detach from session so the conditional UPDATE executes as real SQL
        // (a conditional update on a session-managed entity is routed in-memory and reports 0).
        List<String> orderIds = new ArrayList<>();
        for (LitemallOrder o : orders) {
            orderIds.add(o.orm_idString());
        }
        dao().clearEntitySessionCache();

        int count = 0;
        for (String orderId : orderIds) {
            // Atomic status guard: skip orders already transitioned by a concurrent cancel/refund path
            int affected = orderMapper.updateStatusIfMatch(
                    orderId,
                    _AppMallDaoConstants.ORDER_STATUS_AUTO_CANCEL,
                    _AppMallDaoConstants.ORDER_STATUS_CREATED);
            if (affected == 0) {
                continue;
            }

            LitemallOrder order = get(orderId, false, context);
            order.setEndTime(LocalDateTime.now());

            for (LitemallOrderGoods orderGoods : order.getOrderGoods()) {
                goodsProductMapper.addStock(orderGoods.getProductId(), orderGoods.getNumber());
            }

            QueryBean cuQuery = new QueryBean();
            cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_orderId, orderId));
            cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_status, 1));
            cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_deleted, false));
            List<LitemallCouponUser> usedCoupons = couponUserBiz.findList(cuQuery, null, context);
            for (LitemallCouponUser cu : usedCoupons) {
                couponUserBiz.returnCoupon(cu.orm_idString(), context);
            }

            updateEntity(order, "cancelExpiredOrders", context);
            count++;
        }
        return count;
    }

    @Override
    @BizMutation
    public int confirmExpiredOrders(@Name("timeoutMinutes") int timeoutMinutes,
                                     IServiceContext context) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderStatus, _AppMallDaoConstants.ORDER_STATUS_SHIP));
        query.addFilter(FilterBeans.lt(LitemallOrder.PROP_NAME_shipTime, cutoff));
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_deleted, false));
        query.setLimit(100);

        List<LitemallOrder> orders = doFindListByQueryDirectly(query, context);
        int count = 0;
        for (LitemallOrder order : orders) {
            order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_AUTO_CONFIRM);
            order.setConfirmTime(LocalDateTime.now());
            updateEntity(order, "confirmExpiredOrders", context);
            count++;
        }
        return count;
    }

    private String generateOrderSn() {
        return StringHelper.generateUUID();
    }

    private void requireUserIdMatch(LitemallOrder order, IServiceContext context) {
        if (!Objects.equals(order.getUserId(), context.getUserId())) {
            throw new NopException(ERR_ORDER_NOT_FOUND)
                    .param("orderId", order.orm_idString());
        }
    }
}
