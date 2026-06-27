package app.mall.service.entity;

import app.mall.biz.ILitemallAddressBiz;
import app.mall.biz.ILitemallCartBiz;
import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallGoodsBiz;
import app.mall.biz.ILitemallGoodsProductBiz;
import app.mall.biz.ILitemallGrouponBiz;
import app.mall.biz.ILitemallGrouponRulesBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallOrderGoodsBiz;
import app.mall.biz.ILitemallPinTuanActivityBiz;
import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.biz.ILitemallPointsFlowBiz;
import app.mall.biz.ILitemallPromotionActivityBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.biz.ILitemallTimeDiscountBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallGrouponRules;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.dto.GoodsStatisticsBean;
import app.mall.dao.dto.OrderStatisticsBean;
import app.mall.dao.dto.UserStatisticsBean;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.entity.LitemallPinTuanActivity;
import app.mall.dao.entity.LitemallPointsFlow;
import app.mall.dao.manager.MallLogManager;
import app.mall.dao.mapper.LitemallGoodsProductMapper;
import app.mall.dao.mapper.LitemallOrderMapper;
import app.mall.dao.mapper.LitemallTimeDiscountMapper;
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
import io.nop.api.core.time.CoreMetrics;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.orm.IOrmEntity;
import io.nop.orm.IOrmTemplate;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import static app.mall.service.AppMallErrors.ERR_PIN_TUAN_GROUPON_MUTEX;
import static app.mall.service.AppMallErrors.ERR_PIN_TUAN_NOT_ACTIVE;
import static app.mall.service.AppMallErrors.ERR_PIN_TUAN_PRICE_INVALID;
import static app.mall.service.AppMallErrors.ERR_POINTS_DEDUCT_EXCEED_LIMIT;
import static app.mall.service.AppMallErrors.ERR_PROMOTION_STACKING_NOT_ALLOWED;
import static app.mall.service.AppMallErrors.ERR_TIME_DISCOUNT_SOLD_OUT;

@BizModel("LitemallOrder")
public class LitemallOrderBizModel extends CrudBizModel<LitemallOrder> implements ILitemallOrderBiz {

    static final Logger LOG = LoggerFactory.getLogger(LitemallOrderBizModel.class);

    @Inject
    ILitemallCartBiz cartBiz;

    @Inject
    ILitemallAddressBiz addressBiz;

    @Inject
    ILitemallGoodsBiz goodsBiz;

    @Inject
    ILitemallGoodsProductBiz goodsProductBiz;

    @Inject
    ILitemallOrderGoodsBiz orderGoodsBiz;

    @Inject
    LitemallGoodsProductMapper goodsProductMapper; // reduceStock/addStock: atomic SQL UPDATE for concurrent-safe stock deduction

    @Inject
    ILitemallCouponUserBiz couponUserBiz;

    @Inject
    ILitemallPromotionActivityBiz promotionActivityBiz;

    @Inject
    ILitemallTimeDiscountBiz timeDiscountBiz;

    @Inject
    LitemallTimeDiscountMapper timeDiscountMapper; // reduceTimeDiscountStock: atomic SQL UPDATE for discount stock

    @Inject
    ILitemallPointsAccountBiz pointsAccountBiz;

    @Inject
    ILitemallPointsFlowBiz pointsFlowBiz;

    @Inject
    ILitemallGrouponRulesBiz grouponRulesBiz;

    @Inject
    ILitemallGrouponBiz grouponBiz;

    @Inject
    ILitemallPinTuanActivityBiz pinTuanActivityBiz;

    @Inject
    ILitemallSystemBiz systemBiz;

    @Inject
    IOrmTemplate ormTemplate;

    @Inject
    MallLogManager logManager;

    @Inject
    MallNotificationService notificationService;

    @Inject
    LitemallOrderMapper orderMapper;

    @Inject
    io.nop.orm.IOrmEntityFileStore ormEntityFileStore;

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
                                 @Optional @Name("usePoints") Integer usePoints,
                                 @Optional @Name("pinTuanActivityId") String pinTuanActivityId,
                                 @Optional @Name("pinTuanGroupId") String pinTuanGroupId,
                                 IServiceContext context) {
        String userId = context.getUserId();

        // Pin-tuan × Groupon mutex (P25 Decision): a single order may use groupon OR pin-tuan,
        // never both. They occupy the same actualPrice deduction layer (grouponPrice /
        // pinTuanPrice); allowing both would double-count social-buy discounts.
        boolean hasGroupon = grouponRulesId != null && !grouponRulesId.isEmpty();
        boolean hasPinTuan = pinTuanActivityId != null && !pinTuanActivityId.isEmpty();
        if (hasGroupon && hasPinTuan) {
            throw new NopException(ERR_PIN_TUAN_GROUPON_MUTEX)
                    .param("grouponRulesId", grouponRulesId)
                    .param("pinTuanActivityId", pinTuanActivityId);
        }

        // Member-level pricing: vipPrice is a SKU unit-price discount applied at the goodsPrice
        // aggregation layer (min(retail, vip) * number), not an order-level deduction. It therefore
        // indirectly lowers the promotion (P15) meetAmount base — see order-and-cart.md calc order.
        // Only users with userLevel >= 1 (VIP / VIP_SENIOR) are eligible. userLevel is a Delta
        // extension column accessed via IOrmTemplate (app-mall-delta is test-scoped here, so I*Biz
        // is unavailable and we use the documented IOrmTemplate fallback).
        IOrmEntity memberUser = ormTemplate.get(NopAuthUser.class.getName(), userId);
        Object levelRaw = memberUser != null ? memberUser.orm_propValueByName("userLevel") : null;
        boolean isMember = levelRaw instanceof Integer && (Integer) levelRaw >= 1;

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
        order.setPromotionPrice(BigDecimal.ZERO);
        order.setPinTuanPrice(BigDecimal.ZERO);
        order.setComments(0);
        order.setDeleted(false);

        // Pin-tuan activity resolution (P25): load + validate the activity up front so an invalid
        // activity fails before any stock deduction. pinTuanPrice (order) is a deduction amount:
        // (retailPrice - activity.pinTuanPrice) * number, aggregated over matching SKU lines, and
        // applied at the actualPrice deduction layer (same layer as grouponPrice). See
        // docs/design/order-and-cart.md price composition.
        LitemallPinTuanActivity pinTuanActivity = null;
        if (hasPinTuan) {
            pinTuanActivity = pinTuanActivityBiz.get(pinTuanActivityId, false, context);
            if (pinTuanActivity == null || Boolean.TRUE.equals(pinTuanActivity.getDeleted())) {
                throw new NopException(ERR_PIN_TUAN_NOT_ACTIVE)
                        .param("pinTuanActivityId", pinTuanActivityId);
            }
            if (pinTuanActivity.getStatus() == null
                    || pinTuanActivity.getStatus() != _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE) {
                throw new NopException(ERR_PIN_TUAN_NOT_ACTIVE)
                        .param("pinTuanActivityId", pinTuanActivityId)
                        .param("status", pinTuanActivity.getStatus());
            }
            if (pinTuanActivity.getPinTuanPrice() == null) {
                throw new NopException(ERR_PIN_TUAN_PRICE_INVALID)
                        .param("pinTuanActivityId", pinTuanActivityId);
            }
        }

        BigDecimal goodsPriceTotal = BigDecimal.ZERO;
        BigDecimal pinTuanPriceTotal = BigDecimal.ZERO;
        List<String> couponScopeIds = new ArrayList<>();
        for (LitemallCart item : checkedItems) {
            LitemallOrderGoods orderGoods = orderGoodsBiz.newEntity();
            LitemallGoodsProduct product = goodsProductBiz.requireEntity(item.getProductId(), null, context);
            orderGoods.setGoodsId(item.getGoodsId());
            orderGoods.setGoodsName(item.getGoodsName());
            orderGoods.setGoodsSn(item.getGoodsSn());
            orderGoods.setProductId(item.getProductId());
            orderGoods.setNumber(item.getNumber());
            // Effective unit price: members pay min(retail, vip) when a positive vipPrice is configured.
            BigDecimal unitPrice = product.getPrice();
            if (isMember && product.getVipPrice() != null
                    && product.getVipPrice().compareTo(BigDecimal.ZERO) > 0
                    && product.getVipPrice().compareTo(unitPrice) < 0) {
                unitPrice = product.getVipPrice();
                orderGoods.setVipPrice(product.getVipPrice());
            }
            // Time-limited discount (P23): SKU-level promo price at the goodsPrice aggregation layer.
            // effectiveUnitPrice = min(retail, vip, timeDiscountPrice). When the discount wins and has
            // a limited stock (stockLimit > 0), atomically decrement the discount stock; sold-out aborts.
            Map<String, Object> timeDiscount = timeDiscountBiz.selectTimeDiscountForProduct(
                    item.getGoodsId(), item.getProductId(), context);
            if (timeDiscount != null && timeDiscount.get("promoPrice") != null) {
                BigDecimal timeDiscountPrice = new BigDecimal(timeDiscount.get("promoPrice").toString());
                if (timeDiscountPrice.compareTo(unitPrice) < 0) {
                    Integer stockLimit = (Integer) timeDiscount.get("stockLimit");
                    if (stockLimit != null && stockLimit > 0) {
                        int affected = timeDiscountMapper.reduceTimeDiscountStock(
                                (String) timeDiscount.get("id"), item.getNumber());
                        if (affected == 0) {
                            throw new NopException(ERR_TIME_DISCOUNT_SOLD_OUT)
                                    .param("discountId", timeDiscount.get("id"))
                                    .param("requested", item.getNumber());
                        }
                    }
                    unitPrice = timeDiscountPrice;
                }
            }
            orderGoods.setPrice(unitPrice);
            orderGoods.setSpecifications(item.getSpecifications());
            orderGoods.getPicUrlComponent().copyFrom(item.getPicUrlComponent());
            orderGoods.setComment(0);
            order.getOrderGoods().add(orderGoods);

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getNumber()));
            goodsPriceTotal = goodsPriceTotal.add(lineTotal);

            // Pin-tuan discount (P25): for SKU lines matching the pin-tuan activity, the deduction
            // amount is (retailPrice - activity.pinTuanPrice) * number, computed from the base retail
            // price (product.getPrice()). Applied at the actualPrice deduction layer.
            if (pinTuanActivity != null) {
                boolean skuMatch = pinTuanActivity.getProductId() != null
                        ? Objects.equals(item.getProductId(), pinTuanActivity.getProductId())
                        : Objects.equals(item.getGoodsId(), pinTuanActivity.getGoodsId());
                if (skuMatch) {
                    BigDecimal retailPrice = product.getPrice();
                    if (pinTuanActivity.getPinTuanPrice().compareTo(retailPrice) >= 0) {
                        throw new NopException(ERR_PIN_TUAN_PRICE_INVALID)
                                .param("pinTuanActivityId", pinTuanActivity.orm_idString())
                                .param("pinTuanPrice", pinTuanActivity.getPinTuanPrice())
                                .param("retailPrice", retailPrice);
                    }
                    BigDecimal linePinTuanDiscount = retailPrice
                            .subtract(pinTuanActivity.getPinTuanPrice())
                            .multiply(BigDecimal.valueOf(item.getNumber()));
                    pinTuanPriceTotal = pinTuanPriceTotal.add(linePinTuanDiscount);
                }
            }

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

        BigDecimal promotionPrice = promotionActivityBiz.selectPromotionForOrder(goodsPriceTotal, couponScopeIds, context);
        if (couponUserId != null && !couponUserId.isEmpty()
                && promotionPrice.compareTo(BigDecimal.ZERO) > 0
                && !isPromotionCouponStackingEnabled(context)) {
            throw new NopException(ERR_PROMOTION_STACKING_NOT_ALLOWED)
                    .param("goodsPrice", goodsPriceTotal)
                    .param("promotionPrice", promotionPrice);
        }
        order.setPromotionPrice(promotionPrice);

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
        order.setPinTuanPrice(pinTuanPriceTotal);

        BigDecimal orderPrice = goodsPriceTotal;
        if (order.getFreightPrice() != null) {
            orderPrice = orderPrice.add(order.getFreightPrice());
        }
        orderPrice = orderPrice.subtract(couponPrice).subtract(promotionPrice);
        order.setOrderPrice(orderPrice);

        // Points deduction (P27): integralPrice is an actualPrice deduction layer, computed from
        // the user-supplied usePoints and capped by orderPrice × mall_points_deduct_max_ratio.
        // See docs/design/order-and-cart.md (价格计算顺序) and marketing-and-promotions.md (积分体系).
        BigDecimal integralPrice = computeIntegralPrice(usePoints, orderPrice, context);
        order.setIntegralPrice(integralPrice);

        BigDecimal actualPrice = orderPrice;
        if (order.getIntegralPrice() != null) {
            actualPrice = actualPrice.subtract(order.getIntegralPrice());
        }
        actualPrice = actualPrice.subtract(grouponPrice).subtract(pinTuanPriceTotal);
        order.setActualPrice(actualPrice);

        saveEntity(order, "submit", context);

        // Spend the user's points after the order is saved (orderId available as sourceId).
        // A failed spend (e.g. insufficient balance) throws inside the same @BizMutation tx,
        // rolling back the order save too — atomic with the price computation above.
        if (integralPrice.compareTo(BigDecimal.ZERO) > 0 && usePoints != null && usePoints > 0) {
            pointsAccountBiz.spendPoints(userId, usePoints,
                    _AppMallDaoConstants.POINTS_CHANGE_TYPE_SPEND,
                    LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_DEDUCT,
                    order.orm_idString(),
                    "订单抵扣 " + order.getOrderSn(), context);
        }

        if (grouponRulesId != null && !grouponRulesId.isEmpty()) {
            String orderId = order.orm_idString();
            if (grouponId != null && !grouponId.isEmpty()) {
                grouponBiz.joinGroupon(grouponId, orderId, context);
            } else {
                grouponBiz.openGroupon(grouponRulesId, orderId, context);
            }
        }

        // Pin-tuan open/join (P25): mirror the groupon wiring above. The creator opens a new group;
        // a joiner (pinTuanGroupId provided) joins an existing group.
        if (pinTuanActivityId != null && !pinTuanActivityId.isEmpty()) {
            String orderId = order.orm_idString();
            if (pinTuanGroupId != null && !pinTuanGroupId.isEmpty()) {
                pinTuanActivityBiz.joinPinTuan(pinTuanGroupId, orderId, context);
            } else {
                pinTuanActivityBiz.openPinTuan(pinTuanActivityId, orderId, context);
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
        List<LitemallCouponUser> usedCoupons = couponUserBiz.findList(cuQuery, null, context);
        for (LitemallCouponUser cu : usedCoupons) {
            couponUserBiz.returnCoupon(cu.orm_idString(), context);
        }

        updateEntity(order, "cancel", context);
        // Return deducted points (P27): order-level cancel returns the points the user spent
        // deducting on this order. Idempotent per orderId (sourceType=refund-return).
        returnDeductedPoints(order, context);
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
        // Shopping reward (P27): earn points on receipt confirmation. Idempotent per orderId.
        earnPointsForOrderConfirm(order, context);
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
        // Use CoreMetrics (same clock the ORM uses for auto createTime/updateTime) so the
        // expiry cutoff and the entity's addTime/shipTime stay on one monotonic timeline.
        LocalDateTime cutoff = CoreMetrics.currentDateTime().minusMinutes(timeoutMinutes);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderStatus, _AppMallDaoConstants.ORDER_STATUS_CREATED));
        // Inclusive cutoff: an order created exactly N minutes ago has "timed out".
        query.addFilter(FilterBeans.le(LitemallOrder.PROP_NAME_addTime, cutoff));
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
            List<LitemallCouponUser> usedCoupons = couponUserBiz.findList(cuQuery, null, context);
            for (LitemallCouponUser cu : usedCoupons) {
                couponUserBiz.returnCoupon(cu.orm_idString(), context);
            }

            updateEntity(order, "cancelExpiredOrders", context);
            // Return deducted points (P27): whole-order cancel mirrors coupon return.
            returnDeductedPoints(order, context);
            count++;
        }
        return count;
    }

    @Override
    @BizMutation
    public int confirmExpiredOrders(@Name("timeoutMinutes") int timeoutMinutes,
                                     IServiceContext context) {
        // Use CoreMetrics (same clock the ORM uses for auto createTime/updateTime) so the
        // expiry cutoff and the entity's addTime/shipTime stay on one monotonic timeline.
        LocalDateTime cutoff = CoreMetrics.currentDateTime().minusMinutes(timeoutMinutes);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderStatus, _AppMallDaoConstants.ORDER_STATUS_SHIP));
        query.addFilter(FilterBeans.le(LitemallOrder.PROP_NAME_shipTime, cutoff));
        query.setLimit(100);

        List<LitemallOrder> orders = doFindListByQueryDirectly(query, context);
        int count = 0;
        for (LitemallOrder order : orders) {
            order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_AUTO_CONFIRM);
            order.setConfirmTime(LocalDateTime.now());
            updateEntity(order, "confirmExpiredOrders", context);
            // Auto-confirm is also a receipt completion (P27): earns the same shopping reward as
            // manual confirm. Idempotent per orderId.
            earnPointsForOrderConfirm(order, context);
            count++;
        }
        return count;
    }

    private String generateOrderSn() {
        return StringHelper.generateUUID();
    }

    @Override
    @BizMutation
    public LitemallOrder createFlashSaleOrder(@Name("userId") String userId,
                                               @Name("goodsId") String goodsId,
                                               @Name("productId") String productId,
                                               @Name("goodsName") String goodsName,
                                               @Name("goodsSn") String goodsSn,
                                               @Name("specifications") String specifications,
                                               @Name("picUrl") String picUrl,
                                               @Name("flashPrice") BigDecimal flashPrice,
                                               @Name("number") int number,
                                               @Name("consignee") String consignee,
                                               @Name("mobile") String mobile,
                                               @Name("address") String address,
                                               @Name("freightPrice") BigDecimal freightPrice,
                                               IServiceContext context) {
        // P24 flash-sale direct-buy order creation (independent path, NOT submit()).
        // flashPrice is the unit price (商品单价层); coupon / promotion / integral / groupon /
        // pinTuan slots are all zeroed. Single OrderGoods line. No cart involvement.
        // See docs/design/marketing-and-promotions.md 秒杀章节 "下单路径".
        LocalDateTime now = LocalDateTime.now();
        BigDecimal lineTotal = flashPrice.multiply(BigDecimal.valueOf(number));
        BigDecimal effectiveFreight = freightPrice != null ? freightPrice : BigDecimal.ZERO;
        BigDecimal orderPrice = lineTotal.add(effectiveFreight);

        LitemallOrder order = newEntity();
        order.setUserId(userId);
        order.setOrderSn(generateOrderSn());
        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_CREATED);
        order.setAftersaleStatus(_AppMallDaoConstants.AFTERSALE_STATUS_INIT);
        order.setConsignee(consignee);
        order.setMobile(mobile);
        order.setAddress(address);
        order.setMessage("");
        order.setFreightPrice(effectiveFreight);
        order.setCouponPrice(BigDecimal.ZERO);
        order.setIntegralPrice(BigDecimal.ZERO);
        order.setGrouponPrice(BigDecimal.ZERO);
        order.setPromotionPrice(BigDecimal.ZERO);
        order.setPinTuanPrice(BigDecimal.ZERO);
        order.setGoodsPrice(lineTotal);
        order.setOrderPrice(orderPrice);
        order.setActualPrice(orderPrice);
        order.setComments(0);
        order.setDeleted(false);

        LitemallOrderGoods orderGoods = orderGoodsBiz.newEntity();
        orderGoods.setGoodsId(goodsId);
        orderGoods.setGoodsName(goodsName);
        orderGoods.setGoodsSn(goodsSn);
        orderGoods.setProductId(productId);
        orderGoods.setNumber(number);
        orderGoods.setPrice(flashPrice);
        orderGoods.setSpecifications(specifications);
        // LitemallOrderGoods.picUrl has stdDomain="file" and requires a /f/download/{fileId}
        // reference registered in NopFileRecord with bizObjName=LitemallOrderGoods. We use
        // OrmFileComponent.copyFrom on the TARGET to create a new file record from the goods
        // image, but since the goods entity may be loaded in a different session scope (causing
        // its component enhancer to be null), we extract the fileId from the picUrl string and
        // call the file store directly on the target component.
        copyGoodsPicToOrderGoods(orderGoods, picUrl);
        orderGoods.setComment(0);
        order.getOrderGoods().add(orderGoods);

        saveEntity(order, "createFlashSaleOrder", context);

        if (orderPrice.compareTo(BigDecimal.ZERO) == 0) {
            order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_PAY);
            order.setPayTime(now);
            updateEntity(order, "createFlashSaleOrder:zeroPay", context);
        }

        final String adminNotifyOrderSn = order.getOrderSn();
        txn().afterCommit(null, () -> notificationService.sendAdminOrderNotification(adminNotifyOrderSn));

        return order;
    }

    private void requireUserIdMatch(LitemallOrder order, IServiceContext context) {
        if (!Objects.equals(order.getUserId(), context.getUserId())) {
            throw new NopException(ERR_ORDER_NOT_FOUND)
                    .param("orderId", order.orm_idString());
        }
    }

    private boolean isPromotionCouponStackingEnabled(IServiceContext context) {
        String value = systemBiz.getConfig("mall_promotion_coupon_stacking", context);
        if (value == null || value.isEmpty()) {
            return true;
        }
        return !"false".equalsIgnoreCase(value.trim()) && !"0".equals(value.trim());
    }

    private BigDecimal computeIntegralPrice(Integer usePoints, BigDecimal orderPrice, IServiceContext context) {
        if (usePoints == null || usePoints <= 0 || orderPrice == null || orderPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        int ratio = pointsAccountBiz.resolveToYuanRatio(context);
        if (ratio <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal pointsValue = BigDecimal.valueOf(usePoints.longValue())
                .divide(BigDecimal.valueOf(ratio), 2, RoundingMode.HALF_UP);
        double maxRatio = pointsAccountBiz.resolveDeductMaxRatio(context);
        BigDecimal cap = orderPrice.multiply(BigDecimal.valueOf(maxRatio));
        if (pointsValue.compareTo(cap) > 0) {
            throw new NopException(ERR_POINTS_DEDUCT_EXCEED_LIMIT)
                    .param("requested", pointsValue)
                    .param("cap", cap)
                    .param("maxRatio", maxRatio);
        }
        return pointsValue;
    }

    /**
     * Award shopping-reward points on receipt confirmation (P27). Idempotent: earnPoints rejects
     * a duplicate (sourceType=order-confirm-earn, sourceId=orderId), and confirm() is already
     * guarded by the SHIP→CONFIRM status transition so it succeeds once per order.
     */
    private void earnPointsForOrderConfirm(LitemallOrder order, IServiceContext context) {
        int earnPerYuan = pointsAccountBiz.resolveEarnPerYuan(context);
        if (earnPerYuan <= 0) {
            return;
        }
        BigDecimal actualPrice = order.getActualPrice();
        if (actualPrice == null || actualPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        int points = actualPrice.multiply(BigDecimal.valueOf(earnPerYuan)).intValue();
        if (points <= 0) {
            return;
        }
        pointsAccountBiz.earnPoints(order.getUserId(), points,
                _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN,
                LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN,
                order.orm_idString(),
                "购物赠送 " + order.getOrderSn(), context);
    }

    /**
     * Return the points a user spent deducting on an order (for cancel/refund return).
     * Looks up the SPEND flow (sourceType=order-deduct, sourceId=orderId).
     */
    private int findDeductedPoints(String orderId, IServiceContext context) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType,
                LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_DEDUCT));
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceId, orderId));
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_changeType,
                _AppMallDaoConstants.POINTS_CHANGE_TYPE_SPEND));
        LitemallPointsFlow flow = pointsFlowBiz.findFirst(q, null, context);
        return flow != null && flow.getChangeAmount() != null ? flow.getChangeAmount() : 0;
    }

    private void returnDeductedPoints(LitemallOrder order, IServiceContext context) {
        int deducted = findDeductedPoints(order.orm_idString(), context);
        if (deducted <= 0) {
            return;
        }
        // Idempotent: earnPoints rejects a duplicate (sourceType=refund-return, sourceId=orderId).
        pointsAccountBiz.earnPoints(order.getUserId(), deducted,
                _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN,
                LitemallPointsAccountBizModel.SOURCE_TYPE_REFUND_RETURN,
                order.orm_idString(),
                "取消/退款返还积分 " + order.getOrderSn(), context);
    }

    private void copyGoodsPicToOrderGoods(LitemallOrderGoods orderGoods, String goodsPicUrl) {
        if (goodsPicUrl == null || goodsPicUrl.isEmpty()) {
            return;
        }
        // Use the injected file store to create a new file record from the goods image.
        // This bypasses the source OrmFileComponent (which may have a null enhancer when the
        // goods entity is loaded through a different session scope in cross-BizModel calls).
        // We decode the fileId from the /f/download/{fileId} link and call fileStore.copyFile
        // on the TARGET entity, which creates a new NopFileRecord with correct bizObjName.
        try {
            io.nop.orm.component.OrmFileComponent target = orderGoods.getPicUrlComponent();
            if (target == null || ormEntityFileStore == null) {
                orderGoods.setPicUrl(goodsPicUrl);
                return;
            }
            String sourceFileId = ormEntityFileStore.decodeFileId(goodsPicUrl);
            if (sourceFileId == null || sourceFileId.isEmpty()) {
                target.setFilePath(goodsPicUrl);
                return;
            }
            if (!orderGoods.orm_hasId()) {
                orderGoods.orm_enhancer().initEntityId(orderGoods);
            }
            String newFileId = ormEntityFileStore.copyFile(sourceFileId,
                    target.getBizObjName(), orderGoods.orm_idString(), "picUrl");
            target.setFilePath(ormEntityFileStore.getFileLink(newFileId));
        } catch (Exception e) {
            LOG.warn("copyGoodsPicToOrderGoods failed ({}), falling back to raw picUrl", e.getMessage());
            orderGoods.setPicUrl(goodsPicUrl);
        }
    }
}
