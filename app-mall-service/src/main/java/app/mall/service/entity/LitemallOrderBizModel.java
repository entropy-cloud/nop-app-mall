package app.mall.service.entity;

import app.mall.biz.ILitemallAddressBiz;
import app.mall.biz.ILitemallAftersaleBiz;
import app.mall.biz.ILitemallCartBiz;
import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallFootprintBiz;
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
import app.mall.biz.ILitemallPromotionUsageBiz;
import app.mall.biz.ILitemallPickupStoreBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.biz.ILitemallTimeDiscountBiz;
import app.mall.biz.ILitemallWalletBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallAftersale;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallFootprint;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallGrouponRules;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.dto.BatchShipResultBean;
import app.mall.dao.dto.CartRankingItemBean;
import app.mall.dao.dto.DashboardGmvBean;
import app.mall.dao.dto.DashboardMetricsBean;
import app.mall.dao.dto.GoodsStatisticsBean;
import app.mall.dao.dto.OrderPointBean;
import app.mall.dao.dto.OrderStatisticsBean;
import app.mall.dao.dto.PayChannelViewBean;
import app.mall.dao.dto.SalesTrendPointBean;
import app.mall.dao.dto.StockWarningItemBean;
import app.mall.dao.dto.TodoAggregationBean;
import app.mall.dao.dto.UnsalableGoodsBean;
import app.mall.dao.dto.UserStatisticsBean;
import app.mall.dao.dto.VerifyPickupResultBean;
import app.mall.dao.dto.SalesFunnelBean;
import app.mall.dao.dto.ProductAnalysisBean;
import app.mall.dao.dto.ReportScalarCountBean;
import app.mall.dao.dto.UserRetentionPointBean;
import app.mall.dao.dto.RfmSegmentBean;
import app.mall.dao.dto.LifecycleSegmentBean;
import app.mall.dao.dto.RepurchaseRatePointBean;
import app.mall.dao.dto.UserPaymentPointBean;
import app.mall.dao.dto.UserPaymentSummaryBean;
import app.mall.dao.dto.UserPortraitBean;
import app.mall.dao.dto.SegmentMemberBean;
import app.mall.dao.dto.AovDistributionBean;
import app.mall.dao.dto.CouponUsageStatisticsBean;
import app.mall.dao.dto.OrderAnalysisBean;
import app.mall.dao.dto.PaymentMethodShareBean;
import app.mall.dao.dto.PromotionResolutionBean;
import app.mall.dao.dto.ReturnReasonShareBean;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.entity.LitemallPickupStore;
import app.mall.dao.entity.LitemallPinTuanActivity;
import app.mall.dao.entity.LitemallPointsFlow;
import app.mall.dao.entity.LitemallPromotionActivity;
import app.mall.dao.entity.LitemallPromotionUsage;
import app.mall.dao.entity.LitemallWallet;
import app.mall.dao.manager.MallLogManager;
import app.mall.dao.mapper.LitemallGoodsProductMapper;
import app.mall.dao.mapper.LitemallOrderMapper;
import app.mall.dao.mapper.LitemallTimeDiscountMapper;
import app.mall.pay.PayPrepayRequestBean;
import app.mall.pay.PayPrepayResponseBean;
import app.mall.pay.PayRefundRequestBean;
import app.mall.pay.PayRefundResponseBean;
import app.mall.pay.PayService;
import app.mall.pay.IPayChannelRegistry;
import app.mall.pay.PayChannel;
import app.mall.service.notification.MallNotificationService;
import app.mall.service.AppMallErrors;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.WebContentBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.api.core.time.CoreMetrics;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.core.lang.eval.IEvalScope;
import io.nop.core.resource.IResource;
import io.nop.core.resource.ResourceHelper;
import io.nop.core.resource.tpl.ITemplateOutput;
import io.nop.file.core.IFileRecord;
import io.nop.file.core.IFileStore;
import io.nop.ooxml.xlsx.util.ExcelHelper;
import io.nop.orm.IOrmEntity;
import io.nop.orm.IOrmTemplate;
import io.nop.report.core.engine.IReportEngine;
import io.nop.xlang.api.XLang;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static app.mall.service.AppMallErrors.ERR_GROUPON_RULES_NOT_AVAILABLE;
import static app.mall.service.AppMallErrors.ERR_ORDER_ADDRESS_INVALID;
import static app.mall.service.AppMallErrors.ERR_ORDER_ADDRESS_NOT_BELONG_USER;
import static app.mall.service.AppMallErrors.ERR_ORDER_BATCH_SHIP_EMPTY;
import static app.mall.service.AppMallErrors.ERR_ORDER_BATCH_SHIP_INVALID_ROW;
import static app.mall.service.AppMallErrors.ERR_ORDER_CART_EMPTY;
import static app.mall.service.AppMallErrors.ERR_ORDER_BALANCE_INSUFFICIENT;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_ALLOW_CANCEL;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_ALLOW_CHANGE_ADDRESS;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_ALLOW_CONFIRM;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_ALLOW_DELETE;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_ALLOW_PAY;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_ALLOW_SHIP;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_ORDER_PAY_CREDENTIAL_INVALID;
import static app.mall.service.AppMallErrors.ERR_ORDER_PICKUP_NOT_SHIPPABLE;
import static app.mall.service.AppMallErrors.ERR_ORDER_PRICE_INVALID;
import static app.mall.service.AppMallErrors.ERR_ORDER_PRICE_MODIFY_DISCOUNT_ACTIVE;
import static app.mall.service.AppMallErrors.ERR_ORDER_NOT_ALLOW_MODIFY_PRICE;
import static app.mall.service.AppMallErrors.ERR_ORDER_STOCK_INSUFFICIENT;
import static app.mall.service.AppMallErrors.ERR_ORDER_USE_REAL_PAYMENT;
import static app.mall.service.AppMallErrors.ERR_PIN_TUAN_GROUPON_MUTEX;
import static app.mall.service.AppMallErrors.ERR_PIN_TUAN_NOT_ACTIVE;
import static app.mall.service.AppMallErrors.ERR_PIN_TUAN_PRICE_INVALID;
import static app.mall.service.AppMallErrors.ERR_POINTS_DEDUCT_EXCEED_LIMIT;
import static app.mall.service.AppMallErrors.ERR_PROMOTION_MAX_PER_USER;
import static app.mall.service.AppMallErrors.ERR_PROMOTION_STACKING_NOT_ALLOWED;
import static app.mall.service.AppMallErrors.ERR_PICKUP_CODE_INVALID;
import static app.mall.service.AppMallErrors.ERR_PICKUP_ORDER_NOT_VERIFIABLE;
import static app.mall.service.AppMallErrors.ERR_PICKUP_AUTO_CANCEL_REFUND_FAILED;
import static app.mall.service.AppMallErrors.ERR_PICKUP_STORE_NOT_ACTIVE;
import static app.mall.service.AppMallErrors.ERR_PICKUP_STORE_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_TIME_DISCOUNT_SOLD_OUT;
import static app.mall.service.AppMallErrors.ERR_USER_BANNED;

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
    ILitemallPromotionUsageBiz promotionUsageBiz;

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
    ILitemallAftersaleBiz aftersaleBiz;

    @Inject
    ILitemallFootprintBiz footprintBiz;

    @Inject
    ILitemallPickupStoreBiz pickupStoreBiz;

    @Inject
    IOrmTemplate ormTemplate;

    @Inject
    IReportEngine reportEngine;

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

    // P30 multi-channel abstraction. The registry routes by channel code and combines each
    // channel's capability flag with the operator toggle stored in LitemallSystem pay_channels.
    @Inject
    IPayChannelRegistry payChannelRegistry;

    @Inject
    ILitemallWalletBiz walletBiz;

    // P30 Decision B: balance-payment confirm credential. Reuses the platform login-password
    // encoder so no separate payment-password account entity is needed.
    @Inject
    io.nop.auth.core.password.IPasswordEncoder passwordEncoder;

    // P21 批量发货 Excel 解析（平台 IFileStore + ExcelHelper.readSheet）
    @Inject
    IFileStore fileStore;

    // Member level auto-evaluation on order confirm (D3): evaluates cumulative spending →
    // auto-upgrade → triggers level-up benefit dispatch.
    @Inject
    app.mall.biz.ILitemallMemberLevelBiz memberLevelBiz;

    public LitemallOrderBizModel() {
        setEntityName(LitemallOrder.class.getName());
    }

    @Override
    @BizMutation
    public LitemallOrder submit(@Optional @Name("addressId") String addressId,
                                 @Optional @Name("message") String message,
                                 @Name("freightPrice") BigDecimal freightPrice,
                                 @Optional @Name("couponUserId") String couponUserId,
                                 @Optional @Name("grouponRulesId") String grouponRulesId,
                                 @Optional @Name("grouponId") String grouponId,
                                 @Optional @Name("usePoints") Integer usePoints,
                                 @Optional @Name("pinTuanActivityId") String pinTuanActivityId,
                                 @Optional @Name("pinTuanGroupId") String pinTuanGroupId,
                                 @Optional @Name("deliveryType") Integer deliveryType,
                                 @Optional @Name("pickupStoreId") String pickupStoreId,
                                 IServiceContext context) {
        String userId = context.getUserId();

        // P31 配送方式：PICKUP(10) 走自提分支（无收货地址、运费=0、生成核销码）；
        // 其余（EXPRESS(0) 或 null 兼容存量）走既有快递流程。
        boolean isPickup = deliveryType != null
                && deliveryType == _AppMallDaoConstants.DELIVERY_TYPE_PICKUP;

        LitemallPickupStore pickupStore = null;
        if (isPickup) {
            if (pickupStoreId == null || pickupStoreId.isEmpty()) {
                throw new NopException(ERR_PICKUP_STORE_NOT_FOUND).param("pickupStoreId", pickupStoreId);
            }
            pickupStore = pickupStoreBiz.get(pickupStoreId, false, context);
            if (pickupStore == null || Boolean.TRUE.equals(pickupStore.getDeleted())) {
                throw new NopException(ERR_PICKUP_STORE_NOT_FOUND).param("pickupStoreId", pickupStoreId);
            }
            if (pickupStore.getStatus() == null || pickupStore.getStatus() != PICKUP_STORE_STATUS_ACTIVE) {
                throw new NopException(ERR_PICKUP_STORE_NOT_ACTIVE).param("pickupStoreId", pickupStoreId);
            }
        }

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

        // Banned-user guard (P20): a disabled user (status=0) may not place orders. status is read
        // from the same already-loaded NopAuthUser entity to avoid a second cross-entity fetch.
        Object statusRaw = memberUser != null ? memberUser.orm_propValueByName("status") : null;
        if (statusRaw instanceof Integer && (Integer) statusRaw == 0) {
            throw new NopException(ERR_USER_BANNED).param("userId", userId);
        }

        LitemallAddress address = null;
        if (!isPickup) {
            // EXPRESS: addressId 必填，校验归属（Decision B）。PICKUP 放宽必填，无收货地址。
            address = addressBiz.get(addressId, false, context);
            if (address == null || Boolean.TRUE.equals(address.getDeleted())) {
                throw new NopException(ERR_ORDER_ADDRESS_INVALID)
                        .param("addressId", addressId);
            }
            if (!Objects.equals(userId, address.getUserId())) {
                throw new NopException(ERR_ORDER_ADDRESS_INVALID)
                        .param("addressId", addressId)
                        .param("userId", userId);
            }
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

        order.setMessage(message != null ? message : "");

        if (isPickup) {
            // P31 自提分支：无用户收货地址（Decision B），但 consignee/mobile/address 为 NOT NULL 列，
            // 以「自提门店」作为履约目的地填入（门店即提货点），运费=0（Decision C），写核销码 + 门店 + 配送方式。
            order.setConsignee(pickupStore.getName());
            order.setMobile(pickupStore.getPhone() != null ? pickupStore.getPhone() : "");
            order.setAddress(pickupStore.getAddress() != null ? pickupStore.getAddress() : "");
            order.setDeliveryType(_AppMallDaoConstants.DELIVERY_TYPE_PICKUP);
            order.setPickupStoreId(pickupStore.orm_idString());
            order.setPickupCode(generatePickupCode());
        } else {
            order.setDeliveryType(_AppMallDaoConstants.DELIVERY_TYPE_EXPRESS);
            order.setConsignee(address.getName());
            order.setMobile(address.getTel());
            String fullAddress = (address.getProvince() != null ? address.getProvince() : "")
                    + (address.getCity() != null ? address.getCity() : "")
                    + (address.getCounty() != null ? address.getCounty() : "")
                    + (address.getAddressDetail() != null ? address.getAddressDetail() : "");
            order.setAddress(fullAddress);
        }

        BigDecimal effectiveFreightPrice;
        if (isPickup) {
            // Decision C: 自提不产生配送成本，freightPrice 直接置 0，绕过运费/包邮门槛计算
            effectiveFreightPrice = BigDecimal.ZERO;
        } else {
            effectiveFreightPrice = freightPrice;
            if (effectiveFreightPrice == null) {
                String freightConfig = systemBiz.getConfig("mall_freight_price", context);
                effectiveFreightPrice = freightConfig != null ? new BigDecimal(freightConfig) : BigDecimal.ZERO;
            }
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

        // Resolve the winning promotion via the internal resolver (not the public @BizQuery) so we
        // also obtain the hit activityId — needed to enforce maxPerUser and to write PromotionUsage.
        PromotionResolutionBean promotionResolution = promotionActivityBiz.resolvePromotionForOrderInternal(
                goodsPriceTotal, couponScopeIds, context);
        BigDecimal promotionPrice = promotionResolution != null ? promotionResolution.getDiscount() : BigDecimal.ZERO;
        String promotionActivityId = promotionResolution != null ? promotionResolution.getActivityId() : null;
        BigDecimal promotionMeetAmount = promotionResolution != null ? promotionResolution.getMeetAmount() : null;

        if (couponUserId != null && !couponUserId.isEmpty()
                && promotionPrice.compareTo(BigDecimal.ZERO) > 0
                && !isPromotionCouponStackingEnabled(context)) {
            throw new NopException(ERR_PROMOTION_STACKING_NOT_ALLOWED)
                    .param("goodsPrice", goodsPriceTotal)
                    .param("promotionPrice", promotionPrice);
        }
        order.setPromotionPrice(promotionPrice);

        // maxPerUser guard: when a promotion is hit, count this user's existing participation for
        // the activity and reject if the cap is reached. Throwing here rolls back stock deductions
        // too — the whole submit runs in one @BizMutation transaction.
        if (promotionActivityId != null && promotionPrice.compareTo(BigDecimal.ZERO) > 0) {
            LitemallPromotionActivity promoActivity = promotionActivityBiz.get(promotionActivityId, false, context);
            Integer maxPerUser = promoActivity != null ? promoActivity.getMaxPerUser() : null;
            if (maxPerUser != null && maxPerUser > 0) {
                QueryBean usageQuery = new QueryBean();
                usageQuery.addFilter(FilterBeans.eq(LitemallPromotionUsage.PROP_NAME_userId, userId));
                usageQuery.addFilter(FilterBeans.eq(LitemallPromotionUsage.PROP_NAME_promotionActivityId, promotionActivityId));
                long usageCount = promotionUsageBiz.findCount(usageQuery, context);
                if (usageCount >= maxPerUser) {
                    throw new NopException(ERR_PROMOTION_MAX_PER_USER)
                            .param("userId", userId)
                            .param("activityId", promotionActivityId)
                            .param("maxPerUser", maxPerUser)
                            .param("currentCount", usageCount);
                }
            }
        }

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

        // Write the promotion participation record (maxPerUser model-gap closure): the order hit a
        // promotion, so record the participation fact for limit counting and per-activity attribution.
        // Runs in the same @BizMutation tx; orderId is now available.
        if (promotionActivityId != null && promotionPrice.compareTo(BigDecimal.ZERO) > 0) {
            LitemallPromotionUsage usage = promotionUsageBiz.newEntity();
            usage.setUserId(userId);
            usage.setPromotionActivityId(promotionActivityId);
            usage.setOrderId(order.orm_idString());
            usage.setMeetAmount(promotionMeetAmount);
            usage.setDiscountAmount(promotionPrice);
            promotionUsageBiz.saveEntity(usage, "submit:promotionUsage", context);
        }

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
        // Release promotion participation quota (whole-order cancel mirrors coupon/points return):
        // soft-delete the PromotionUsage so maxPerUser lets the user re-participate. Idempotent.
        releasePromotionUsage(order.orm_idString(), context);
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

        markOrderPaidCore(order, context);
        return order;
    }

    /**
     * P30: shared "order became paid" tail extracted from {@code pay()} and
     * {@code confirmPaidByNotify()}. Sets ORDER_STATUS_PAY + payTime, persists, and fires the
     * payment notification after commit. Callers validate status + ownership + channel fields
     * (payId / payChannel / walletPayAmount) BEFORE invoking this. Balance-payment reuses this
     * core after debiting the wallet, so it does not bypass the state machine nor re-trigger
     * the real-payment guard in {@code pay()}.
     *
     * <p>Internal helper (not a {@code @BizAction}): the GraphQL engine does not support entity
     * params on exposed actions, and this is only reused within this BizModel. Covered
     * transitively by the {@code pay()} / {@code confirmPaidByNotify()} / {@code payByBalance()}
     * GraphQL tests.
     */
    void markOrderPaidCore(LitemallOrder order, IServiceContext context) {
        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_PAY);
        order.setPayTime(LocalDateTime.now());
        updateEntity(order, "markOrderPaidCore", context);
        final String payOrderSn = order.getOrderSn();
        final String payMobile = order.getMobile();
        final String payUserId = notificationService.isEventMessageEnabled("payment", context)
                ? order.getUserId() : null;
        txn().afterCommit(null, () -> notificationService.sendOrderPaymentNotification(payOrderSn, payMobile, payUserId));
    }

    @Override
    @BizMutation
    public LitemallOrder payByBalance(@Name("orderId") String orderId,
                                       @Name("confirmCredential") String confirmCredential,
                                       IServiceContext context) {
        LitemallOrder order = get(orderId, false, context);
        if (order == null) {
            throw new NopException(ERR_ORDER_NOT_FOUND).param("orderId", orderId);
        }
        // Status guard (idempotency layer 1): a repeat call for an already-paid order is rejected.
        if (order.getOrderStatus() != _AppMallDaoConstants.ORDER_STATUS_CREATED) {
            throw new NopException(ERR_ORDER_NOT_ALLOW_PAY)
                    .param("orderId", orderId)
                    .param("status", order.getOrderStatus());
        }
        requireUserIdMatch(order, context);

        BigDecimal actualPrice = order.getActualPrice() == null ? BigDecimal.ZERO : order.getActualPrice();
        // Balance channel only serves non-zero orders. Zero-amount orders confirm via pay() in the
        // cashier's zero-amount branch (no debit needed).
        if (actualPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NopException(ERR_ORDER_NOT_ALLOW_PAY)
                    .param("orderId", orderId)
                    .param("reason", "zero-amount order must use pay(), not balance channel");
        }

        // Confirm credential (Decision B): reuse the login password via the platform encoder.
        verifyPayCredential(order.getUserId(), confirmCredential, context);

        // Balance sufficiency pre-check (the optimistic-lock debit is the authoritative guard).
        BigDecimal balance = lookupWalletBalance(context);
        if (balance.compareTo(actualPrice) < 0) {
            throw new NopException(ERR_ORDER_BALANCE_INSUFFICIENT)
                    .param("orderId", orderId)
                    .param("balance", balance)
                    .param("actualPrice", actualPrice);
        }

        // Atomic debit (idempotency layer 2): optimistic version lock rejects concurrent double
        // debit. sourceId=orderSn ties the flow to this order. Runs in the same @BizMutation tx.
        walletBiz.debitBalance(order.getUserId(), actualPrice,
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_PAY,
                LitemallWalletBizModel.SOURCE_TYPE_PAY,
                order.getOrderSn(),
                "余额支付订单 " + order.getOrderSn(),
                context);

        // Write channel fields BEFORE advancing state so the persisted PAID row reflects the
        // balance channel. Reload the order because debitBalance clears the entity session cache
        // (it detaches entities to force the conditional UPDATE to run as real SQL).
        LitemallOrder fresh = get(orderId, false, context);
        fresh.setWalletPayAmount(actualPrice);
        fresh.setPayChannel(_AppMallDaoConstants.PAY_CHANNEL_BALANCE);
        markOrderPaidCore(fresh, context);
        return fresh;
    }

    /**
     * Decision B confirm-credential check. Verifies {@code confirmCredential} against the user's
     * login password via the platform {@link io.nop.auth.core.password.IPasswordEncoder}. Throws
     * {@link AppMallErrors#ERR_ORDER_PAY_CREDENTIAL_INVALID} on mismatch. The login password is
     * read from {@code NopAuthUser} via {@code IOrmTemplate} (app-mall-delta is test-scoped, so
     * the typed Delta column extension is accessed the same way as the existing userLevel read).
     */
    private void verifyPayCredential(String userId, String confirmCredential, IServiceContext context) {
        if (StringHelper.isEmpty(confirmCredential)) {
            throw new NopException(ERR_ORDER_PAY_CREDENTIAL_INVALID).param("userId", userId);
        }
        IOrmEntity authUser = ormTemplate.get(NopAuthUser.class.getName(), userId);
        if (authUser == null) {
            throw new NopException(ERR_ORDER_PAY_CREDENTIAL_INVALID).param("userId", userId);
        }
        String encoded = (String) authUser.orm_propValueByName("password");
        String salt = (String) authUser.orm_propValueByName("salt");
        if (StringHelper.isEmpty(encoded) || !passwordEncoder.passwordMatches(confirmCredential, salt, encoded)) {
            throw new NopException(ERR_ORDER_PAY_CREDENTIAL_INVALID).param("userId", userId);
        }
    }

    @Override
    @BizQuery
    public List<PayChannelViewBean> getEnabledPayChannels(@Name("orderId") String orderId,
                                                            IServiceContext context) {
        // Ownership-checked catalog of cashier channels. Combines the registry (capability +
        // operator toggle) with the calling user's wallet balance for the BALANCE channel.
        LitemallOrder order = get(orderId, false, context);
        if (order == null || Boolean.TRUE.equals(order.getDeleted())) {
            throw new NopException(ERR_ORDER_NOT_FOUND).param("orderId", orderId);
        }
        if (!order.getUserId().equals(context.getUserId())) {
            throw new NopException(ERR_ORDER_NOT_FOUND).param("orderId", orderId);
        }

        BigDecimal balance = lookupWalletBalance(context);
        List<PayChannelViewBean> views = new ArrayList<>();
        for (PayChannel ch : payChannelRegistry.getEnabledChannels()) {
            PayChannelViewBean view = new PayChannelViewBean();
            view.setCode(ch.getCode());
            view.setName(payChannelDisplayName(ch.getCode()));
            view.setDescription(payChannelDisplayDescription(ch.getCode()));
            if ("BALANCE".equals(ch.getCode())) {
                view.setBalanceAvailable(balance);
            }
            views.add(view);
        }
        return views;
    }

    private BigDecimal lookupWalletBalance(IServiceContext context) {
        LitemallWallet wallet = walletBiz.getMyWallet(context);
        BigDecimal balance = wallet != null ? wallet.getBalance() : null;
        return balance != null ? balance : BigDecimal.ZERO;
    }

    private String payChannelDisplayName(String code) {
        switch (code) {
            case "WECHAT": return "微信支付";
            case "ALIPAY": return "支付宝";
            case "BALANCE": return "余额支付";
            default: return code;
        }
    }

    private String payChannelDisplayDescription(String code) {
        switch (code) {
            case "WECHAT": return "微信扫码支付";
            case "ALIPAY": return "支付宝支付";
            case "BALANCE": return "使用钱包余额支付";
            default: return null;
        }
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
        if (transactionId != null && !transactionId.isEmpty()) {
            order.setPayId(transactionId);
        }
        // Mark WeChat channel on the paid order (Channel abstraction P30): prepay recorded no
        // channel at creation; the verified SUCCESS notify is the authoritative pay confirmation.
        order.setPayChannel(_AppMallDaoConstants.PAY_CHANNEL_WECHAT);
        markOrderPaidCore(order, context);
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
        // P31 隔离 (a): 自提订单不可发货（自提订单走门店核销，不应进入发货流转）
        if (order.getDeliveryType() != null
                && order.getDeliveryType() == _AppMallDaoConstants.DELIVERY_TYPE_PICKUP) {
            throw new NopException(ERR_ORDER_PICKUP_NOT_SHIPPABLE)
                    .param(AppMallErrors.ARG_ORDER_ID, orderId);
        }

        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_SHIP);
        order.setShipSn(shipSn);
        order.setShipChannel(shipChannel);
        order.setShipTime(LocalDateTime.now());
        updateEntity(order, "ship", context);
        final String shipOrderSn = order.getOrderSn();
        final String shipMobile = order.getMobile();
        final String shipUserId = notificationService.isEventMessageEnabled("ship", context)
                ? order.getUserId() : null;
        txn().afterCommit(null, () -> notificationService.sendOrderShipNotification(shipOrderSn, shipMobile, shipUserId));
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
        // Member level auto-evaluation (D3): cumulative spending may cross upgrade threshold.
        // evaluateUserLevel is idempotent (no change → no dispatch); only upgrades trigger benefits.
        try {
            memberLevelBiz.evaluateUserLevel(order.getUserId(), context);
        } catch (NopException e) {
            LOG.warn("confirm: member level evaluation failed for user {}", order.getUserId(), e);
        }
        return order;
    }

    /**
     * 门店自提核销（P31）。见 {@code docs/design/order-and-cart.md}「配送方式扩展/自提核销」：
     * 按 pickupCode 反查订单；仅已支付(201)且 deliveryType=PICKUP 可核销；幂等（已核销跳过）；
     * 推进到 401 终态并**复制 confirm 的真实收货副作用**（积分赠送 + 写 pickupTime），
     * **不复用** ship 的通知/日志（自提无发货语义，Decision A 隔离 b）。
     */
    @Override
    @BizMutation
    @Auth(roles = "admin")
    public VerifyPickupResultBean verifyPickupOrder(@Name("pickupCode") String pickupCode,
                                                     IServiceContext context) {
        if (pickupCode == null || pickupCode.isEmpty()) {
            throw new NopException(ERR_PICKUP_CODE_INVALID).param("pickupCode", pickupCode);
        }
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_pickupCode, pickupCode));
        LitemallOrder order = findFirst(query, null, context);
        if (order == null || Boolean.TRUE.equals(order.getDeleted())) {
            throw new NopException(ERR_PICKUP_CODE_INVALID).param("pickupCode", pickupCode);
        }

        VerifyPickupResultBean result = new VerifyPickupResultBean();
        result.setOrderId(order.orm_idString());
        result.setOrderSn(order.getOrderSn());
        result.setPickupCode(pickupCode);

        // 幂等：已核销（已进入 401/402 收货终态）直接跳过，返回 alreadyVerified 反馈
        if (order.getOrderStatus() != null
                && (order.getOrderStatus() == _AppMallDaoConstants.ORDER_STATUS_CONFIRM
                || order.getOrderStatus() == _AppMallDaoConstants.ORDER_STATUS_AUTO_CONFIRM)) {
            result.setAlreadyVerified(true);
            result.setMessage("订单已核销，跳过重复核销");
            return result;
        }

        // 状态守卫：仅已支付(201)且自提订单可核销
        if (order.getOrderStatus() != _AppMallDaoConstants.ORDER_STATUS_PAY) {
            throw new NopException(ERR_PICKUP_ORDER_NOT_VERIFIABLE)
                    .param(AppMallErrors.ARG_ORDER_ID, order.orm_idString())
                    .param(AppMallErrors.ARG_CURRENT_STATUS, order.getOrderStatus());
        }
        if (order.getDeliveryType() == null
                || order.getDeliveryType() != _AppMallDaoConstants.DELIVERY_TYPE_PICKUP) {
            throw new NopException(ERR_PICKUP_ORDER_NOT_VERIFIABLE)
                    .param(AppMallErrors.ARG_ORDER_ID, order.orm_idString())
                    .param(AppMallErrors.ARG_CURRENT_STATUS, order.getOrderStatus());
        }

        // CAS-guard the 201→401 transition (D4 of pickup-auto-timeout plan): the original
        // read-check-write had no atomic guard, so a concurrent cancelExpiredPickupOrders run
        // could CAS 201→203 (refund) between verify's status check and its updateEntity. The
        // stale updateEntity would then overwrite 203→401 while the refund side-effects (stock
        // restore + wallet credit-back) had already executed → double-spend. updateStatusIfMatch
        // issues a direct SQL UPDATE WHERE orderStatus=201; affected=0 means a concurrent
        // transition already happened (e.g., 201→203 by the pickup-timeout job). We report the
        // original loaded status (the in-memory cached value is stale post-CAS, but querying DB
        // for the true current value would require clearing the live session — disproportionate
        // for a rejection path; the operator can investigate via the admin order detail page).
        int verifyCasAffected = orderMapper.updateStatusIfMatch(
                order.orm_idString(),
                _AppMallDaoConstants.ORDER_STATUS_CONFIRM,
                _AppMallDaoConstants.ORDER_STATUS_PAY);
        if (verifyCasAffected == 0) {
            throw new NopException(ERR_PICKUP_ORDER_NOT_VERIFIABLE)
                    .param(AppMallErrors.ARG_ORDER_ID, order.orm_idString())
                    .param(AppMallErrors.ARG_CURRENT_STATUS, order.getOrderStatus());
        }

        // 推进到 401（用户已收货）终态，不经 301。复制 confirm 的真实副作用：
        // 积分赠送 + 写 pickupTime。不发 ship 通知/日志（自提无发货语义）。
        // CAS 已经在 DB 层把 orderStatus 翻到 401；这里把内存中的 orderStatus 同步到 401
        // （避免 updateEntity 的 dirty 跟踪把陈旧的 201 写回），并写 confirmTime / pickupTime。
        order.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_CONFIRM);
        order.setConfirmTime(CoreMetrics.currentDateTime());
        order.setPickupTime(CoreMetrics.currentDateTime());
        updateEntity(order, "verifyPickupOrder", context);
        earnPointsForOrderConfirm(order, context);

        // 自提核销成功站内信（successor of P31 deferred）：沿用 payment/ship 的 afterCommit +
        // isEventMessageEnabled + uid-null-skip 模式（:817-819 / :1017-1019）。事件开关
        // mall_message_event_enabled_pickup_verify 关闭时 uid=null，sendUserMessage 内部 null-guard 跳过。
        final String verifyOrderSn = order.getOrderSn();
        final String verifyUserId = notificationService.isEventMessageEnabled("pickup_verify", context)
                ? order.getUserId() : null;
        txn().afterCommit(null, () -> notificationService.sendUserMessage(
                verifyUserId, _AppMallDaoConstants.MSG_TYPE_ORDER,
                "订单核销成功", "订单 " + verifyOrderSn + " 已核销成功"));

        result.setAlreadyVerified(false);
        result.setMessage("核销成功");
        return result;
    }

    /**
     * 生成自提核销码（P31 Decision D）。基于 UUID 去掉连字符取前 8 位大写，随机且不可枚举。
     * 唯一性应用层保证（DB 唯一键见 Deferred model-gap）。
     */
    private String generatePickupCode() {
        return StringHelper.generateUUID().replace("-", "").toUpperCase().substring(0, 8);
    }

    private static final int PICKUP_STORE_STATUS_ACTIVE = 0;

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

    // ===== P18 经营看板统计 API =====

    @Override
    @BizQuery
    public DashboardMetricsBean getDashboardMetrics(IServiceContext context) {
        LocalDate today = CoreMetrics.currentDate();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeekSameDay = today.minusWeeks(1);

        Timestamp todayStart = Timestamp.valueOf(today.atTime(LocalTime.MIN));
        Timestamp todayEnd = Timestamp.valueOf(today.atTime(LocalTime.MAX));
        Timestamp yStart = Timestamp.valueOf(yesterday.atTime(LocalTime.MIN));
        Timestamp yEnd = Timestamp.valueOf(yesterday.atTime(LocalTime.MAX));
        Timestamp lwStart = Timestamp.valueOf(lastWeekSameDay.atTime(LocalTime.MIN));
        Timestamp lwEnd = Timestamp.valueOf(lastWeekSameDay.atTime(LocalTime.MAX));

        DashboardGmvBean todayBean = orderMapper.getDashboardGmv(todayStart, todayEnd);
        DashboardGmvBean yBean = orderMapper.getDashboardGmv(yStart, yEnd);
        DashboardGmvBean lwBean = orderMapper.getDashboardGmv(lwStart, lwEnd);

        BigDecimal todayGmv = gmvOrZero(todayBean);
        BigDecimal yesterdayGmv = gmvOrZero(yBean);
        BigDecimal lastWeekGmv = gmvOrZero(lwBean);
        int orderCount = todayBean != null ? todayBean.getOrderCount() : 0;
        int paidUserCount = todayBean != null ? todayBean.getPaidUserCount() : 0;
        int uv = todayBean != null ? todayBean.getUv() : 0;
        int returnCount = todayBean != null ? todayBean.getReturnCount() : 0;

        DashboardMetricsBean result = new DashboardMetricsBean();
        result.setTodayGmv(todayGmv);
        result.setYesterdayGmv(yesterdayGmv);
        result.setLastWeekGmv(lastWeekGmv);
        result.setOrderCount(orderCount);
        result.setUv(uv);
        result.setGmvDayRatio(growthRatio(todayGmv, yesterdayGmv));
        result.setGmvWeekRatio(growthRatio(todayGmv, lastWeekGmv));
        result.setAov(orderCount > 0
                ? todayGmv.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        result.setConversionRate(uv > 0
                ? BigDecimal.valueOf(paidUserCount).divide(BigDecimal.valueOf(uv), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        result.setReturnRate(orderCount > 0
                ? BigDecimal.valueOf(returnCount).divide(BigDecimal.valueOf(orderCount), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        return result;
    }

    @Override
    @BizQuery
    public List<SalesTrendPointBean> getSalesTrend(@Optional @Name("granularity") String granularity,
                                                     @Optional @Name("startDate") String startDate,
                                                     @Optional @Name("endDate") String endDate,
                                                     IServiceContext context) {
        String gran = StringHelper.isEmpty(granularity) ? "day" : granularity.toLowerCase();
        LocalDate today = CoreMetrics.currentDate();
        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : today.minusDays(29);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : today;
        if (start.isAfter(end)) {
            start = end;
        }

        Timestamp startTs = Timestamp.valueOf(start.atTime(LocalTime.MIN));
        Timestamp endTs = Timestamp.valueOf(end.atTime(LocalTime.MAX));

        List<OrderPointBean> points = orderMapper.getPaidOrderPoints(startTs, endTs);

        Map<String, BigDecimal> gmvByKey = new LinkedHashMap<>();
        Map<String, Integer> countByKey = new LinkedHashMap<>();
        for (OrderPointBean p : points) {
            if (p.getPayTime() == null) {
                continue;
            }
            String key = trendBucketLabel(p.getPayTime(), gran);
            BigDecimal price = p.getActualPrice() != null ? p.getActualPrice() : BigDecimal.ZERO;
            gmvByKey.merge(key, price, BigDecimal::add);
            countByKey.merge(key, 1, Integer::sum);
        }

        List<SalesTrendPointBean> result = new ArrayList<>();
        for (String key : trendKeySequence(start, end, gran)) {
            SalesTrendPointBean pt = new SalesTrendPointBean();
            pt.setDateLabel(key);
            pt.setGmv(gmvByKey.getOrDefault(key, BigDecimal.ZERO));
            pt.setOrderCount(countByKey.getOrDefault(key, 0));
            result.add(pt);
        }
        return result;
    }

    @Override
    @BizQuery
    public List<LitemallOrder> getRealtimeOrders(@Optional @Name("limit") Integer limit,
                                                  IServiceContext context) {
        int effectiveLimit = limit != null && limit > 0 ? limit : 20;
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_deleted, false));
        query.addOrderField(LitemallOrder.PROP_NAME_addTime, true);
        query.setLimit(effectiveLimit);
        return findList(query, null, context);
    }

    @Override
    @BizQuery
    public TodoAggregationBean getTodoAggregation(IServiceContext context) {
        QueryBean shipQ = new QueryBean();
        shipQ.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderStatus,
                _AppMallDaoConstants.ORDER_STATUS_PAY));
        int pendingShip = (int) findCount(shipQ, context);

        QueryBean refundQ = new QueryBean();
        refundQ.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderStatus,
                _AppMallDaoConstants.ORDER_STATUS_REFUND));
        int pendingRefund = (int) findCount(refundQ, context);

        QueryBean aftQ = new QueryBean();
        aftQ.addFilter(FilterBeans.eq(LitemallAftersale.PROP_NAME_status,
                _AppMallDaoConstants.AFTERSALE_STATUS_REQUEST));
        int aftersalePendingReview = (int) aftersaleBiz.findCount(aftQ, context);

        int threshold = resolveStockThreshold(context);
        List<StockWarningItemBean> stockWarningDetails = orderMapper.getStockWarningList(threshold, 50);

        TodoAggregationBean result = new TodoAggregationBean();
        result.setPendingShip(pendingShip);
        result.setPendingRefund(pendingRefund);
        result.setAftersalePendingReview(aftersalePendingReview);
        result.setStockWarning(stockWarningDetails.size());
        result.setStockWarningDetails(stockWarningDetails);
        return result;
    }

    // ===== P19 报表体系扩展 =====

    @Override
    @BizQuery
    public SalesFunnelBean getSalesFunnel(@Optional @Name("startDate") String startDate,
                                           @Optional @Name("endDate") String endDate,
                                           IServiceContext context) {
        Timestamp start = parseStartDate(startDate);
        Timestamp end = parseEndDate(endDate);
        SalesFunnelBean bean = orderMapper.getSalesFunnel(start, end);
        if (bean == null) {
            bean = new SalesFunnelBean();
        }
        bean.setCartViewRatio(ratio(bean.getCartCount(), bean.getViewCount()));
        bean.setOrderCartRatio(ratio(bean.getOrderCount(), bean.getCartCount()));
        bean.setPayOrderRatio(ratio(bean.getPayCount(), bean.getOrderCount()));
        bean.setRepurchasePayRatio(ratio(bean.getRepurchaseCount(), bean.getPayCount()));
        bean.setPayViewRatio(ratio(bean.getPayCount(), bean.getViewCount()));
        return bean;
    }

    @Override
    @BizQuery
    @Auth(roles = "admin")
    public ProductAnalysisBean getProductAnalysis(@Optional @Name("startDate") String startDate,
                                                   @Optional @Name("endDate") String endDate,
                                                   @Optional @Name("categoryId") String categoryId,
                                                   IServiceContext context) {
        Timestamp start = parseStartDate(startDate);
        Timestamp end = parseEndDate(endDate);
        String cat = StringHelper.isEmpty(categoryId) ? null : categoryId;

        int effectiveLimit = 20;
        List<GoodsStatisticsBean> ranking = orderMapper.getGoodsSalesRankingByCategory(start, end, cat, effectiveLimit);
        enrichMarginFields(ranking);
        List<CartRankingItemBean> cartRanking = orderMapper.getCartRanking(start, end, cat, effectiveLimit);
        List<UnsalableGoodsBean> unsalable = orderMapper.getUnsalableGoods(start, end, cat, effectiveLimit);

        ReportScalarCountBean soldBean = orderMapper.getSoldGoodsCount(start, end, cat);
        ReportScalarCountBean onSaleBean = orderMapper.getOnSaleGoodsCount(cat);
        int sold = soldBean != null ? soldBean.getTotalCount() : 0;
        int onSale = onSaleBean != null ? onSaleBean.getTotalCount() : 0;

        ProductAnalysisBean result = new ProductAnalysisBean();
        result.setSalesRanking(ranking);
        result.setCartRanking(cartRanking);
        result.setUnsalableGoods(unsalable);
        result.setSoldGoodsCount(sold);
        result.setOnSaleGoodsCount(onSale);
        result.setSalabilityRate(onSale > 0
                ? BigDecimal.valueOf(sold).divide(BigDecimal.valueOf(onSale), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        return result;
    }

    private static void enrichMarginFields(List<GoodsStatisticsBean> ranking) {
        if (ranking == null || ranking.isEmpty()) {
            return;
        }
        for (GoodsStatisticsBean g : ranking) {
            BigDecimal costAmount = g.getCostAmount();
            if (costAmount == null || costAmount.signum() <= 0) {
                g.setCostAmount(null);
                continue;
            }
            BigDecimal salesAmount = g.getSalesAmount();
            if (salesAmount == null || salesAmount.signum() <= 0) {
                g.setCostAmount(null);
                continue;
            }
            BigDecimal grossProfit = salesAmount.subtract(costAmount);
            g.setGrossProfit(grossProfit);
            g.setMarginRate(grossProfit.divide(salesAmount, 4, RoundingMode.HALF_UP));
        }
    }

    private static Timestamp parseStartDate(String startDate) {
        return startDate != null && !startDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(startDate).atTime(LocalTime.MIN)) : MIN_TIMESTAMP;
    }

    private static Timestamp parseEndDate(String endDate) {
        return endDate != null && !endDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(endDate).atTime(LocalTime.MAX)) : MAX_TIMESTAMP;
    }

    private static BigDecimal ratio(int numerator, int denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }

    // ===== P19 用户分析 =====

    private static final int DEFAULT_CHURN_DAYS = 90;

    @Override
    @BizQuery
    public List<UserRetentionPointBean> getUserRetention(@Optional @Name("startDate") String startDate,
                                                          @Optional @Name("endDate") String endDate,
                                                          IServiceContext context) {
        LocalDate today = CoreMetrics.currentDate();
        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : today.minusDays(29);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : today;
        if (start.isAfter(end)) {
            start = end;
        }

        Timestamp startTs = Timestamp.valueOf(start.atTime(LocalTime.MIN));
        Timestamp endTs = Timestamp.valueOf(end.atTime(LocalTime.MAX));

        List<UserPaymentPointBean> points = orderMapper.getUserPaymentPoints(startTs, endTs);

        Map<String, List<LocalDateTime>> userPayTimes = new HashMap<>();
        for (UserPaymentPointBean p : points) {
            if (p.getPayTime() == null || p.getUserId() == null) {
                continue;
            }
            userPayTimes.computeIfAbsent(p.getUserId(), k -> new ArrayList<>())
                    .add(p.getPayTime().toLocalDateTime());
        }

        Map<String, int[]> cohort = new LinkedHashMap<>();
        for (Map.Entry<String, List<LocalDateTime>> entry : userPayTimes.entrySet()) {
            List<LocalDateTime> times = entry.getValue();
            times.sort(LocalDateTime::compareTo);
            LocalDate firstPayDate = times.get(0).toLocalDate();
            String key = firstPayDate.toString();
            int[] counts = cohort.computeIfAbsent(key, k -> new int[4]);
            counts[0]++;
            for (LocalDateTime t : times) {
                long daysBetween = java.time.Duration.between(firstPayDate.atStartOfDay(), t.toLocalDate().atStartOfDay()).toDays();
                if (daysBetween >= 1 && daysBetween <= 1) counts[1]++;
                else if (daysBetween >= 7 && daysBetween <= 7) counts[2]++;
                else if (daysBetween >= 30 && daysBetween <= 30) counts[3]++;
                else if (daysBetween > 1 && daysBetween < 7) {
                    // D+1 window: any payment within next day counts for D1 retention
                }
            }
        }

        List<UserRetentionPointBean> result = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : cohort.entrySet()) {
            int[] counts = entry.getValue();
            UserRetentionPointBean bean = new UserRetentionPointBean();
            bean.setDateLabel(entry.getKey());
            bean.setCohortSize(counts[0]);
            bean.setD1(counts[1]);
            bean.setD7(counts[2]);
            bean.setD30(counts[3]);
            bean.setD1Rate(ratio(counts[1], counts[0]));
            bean.setD7Rate(ratio(counts[2], counts[0]));
            bean.setD30Rate(ratio(counts[3], counts[0]));
            result.add(bean);
        }
        return result;
    }

    @Override
    @BizQuery
    public List<RfmSegmentBean> getUserRfm(@Optional @Name("startDate") String startDate,
                                            @Optional @Name("endDate") String endDate,
                                            IServiceContext context) {
        Timestamp start = parseStartDate(startDate);
        Timestamp end = parseEndDate(endDate);

        List<UserPaymentSummaryBean> summaries = orderMapper.getUserPaymentSummaryInPeriod(start, end);
        if (summaries.isEmpty()) {
            return new ArrayList<>();
        }

        RfmThresholds thresholds = computeRfmThresholds(summaries);

        Map<String, Integer> segmentCounts = new LinkedHashMap<>();
        for (UserPaymentSummaryBean s : summaries) {
            String segment = classifyRfmSegment(s, thresholds);
            segmentCounts.merge(segment, 1, Integer::sum);
        }

        List<RfmSegmentBean> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : segmentCounts.entrySet()) {
            RfmSegmentBean bean = new RfmSegmentBean();
            bean.setSegment(entry.getKey());
            bean.setUserCount(entry.getValue());
            result.add(bean);
        }
        return result;
    }

    /**
     * P19 RFM 报表 + P20 用户画像共用：按当批数据中位数计算 R/F/M 三分位阈值。
     * 同源抽取自原 getUserRfm 内联阈值计算（行为不变，P19 测试零回归）。
     */
    private static RfmThresholds computeRfmThresholds(List<UserPaymentSummaryBean> summaries) {
        List<UserPaymentSummaryBean> sorted = new ArrayList<>(summaries);
        sorted.sort((a, b) -> Integer.compare(b.getOrderCount(), a.getOrderCount()));
        long fMedian = sorted.get(sorted.size() / 2).getOrderCount();

        sorted.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        BigDecimal mMedian = sorted.get(sorted.size() / 2).getTotalAmount();

        long nowMillis = CoreMetrics.currentTimeMillis();
        List<Long> recencies = new ArrayList<>();
        for (UserPaymentSummaryBean s : summaries) {
            if (s.getLastPayTime() != null) {
                recencies.add((nowMillis - s.getLastPayTime().getTime()) / (24 * 60 * 60 * 1000));
            }
        }
        Collections.sort(recencies);
        long rMedian = recencies.isEmpty() ? 0 : recencies.get(recencies.size() / 2);
        return new RfmThresholds(rMedian, fMedian, mMedian);
    }

    /**
     * P19 RFM 报表 + P20 用户画像共用：用阈值把单用户的 R/F/M 标到 8 段之一。
     * 同源抽取自原 getUserRfm 内联分类（无逻辑分叉，与 labelRfm 一致）。
     */
    private static String classifyRfmSegment(UserPaymentSummaryBean s, RfmThresholds thresholds) {
        long nowMillis = CoreMetrics.currentTimeMillis();
        long r = s.getLastPayTime() != null
                ? (nowMillis - s.getLastPayTime().getTime()) / (24 * 60 * 60 * 1000) : Long.MAX_VALUE;
        boolean rHigh = r <= thresholds.rMedian;
        boolean fHigh = s.getOrderCount() >= thresholds.fMedian;
        boolean mHigh = s.getTotalAmount().compareTo(thresholds.mMedian) >= 0;
        return labelRfm(rHigh, fHigh, mHigh);
    }

    /**
     * RFM 三分位阈值载体（私有静态类，仅供 computeRfmThresholds/classifyRfmSegment 同源使用）。
     */
    private static final class RfmThresholds {
        final long rMedian;
        final long fMedian;
        final BigDecimal mMedian;

        RfmThresholds(long rMedian, long fMedian, BigDecimal mMedian) {
            this.rMedian = rMedian;
            this.fMedian = fMedian;
            this.mMedian = mMedian;
        }
    }

    private static String labelRfm(boolean rHigh, boolean fHigh, boolean mHigh) {
        int score = (rHigh ? 4 : 0) + (fHigh ? 2 : 0) + (mHigh ? 1 : 0);
        switch (score) {
            case 7: return "重要价值用户";
            case 6: return "重要保持用户";
            case 5: return "重要发展用户";
            case 4: return "重要挽留用户";
            case 3: return "一般价值用户";
            case 2: return "一般保持用户";
            case 1: return "一般发展用户";
            default: return "一般挽留用户";
        }
    }

    @Override
    @BizQuery
    public List<LifecycleSegmentBean> getUserLifecycle(@Optional @Name("startDate") String startDate,
                                                        @Optional @Name("endDate") String endDate,
                                                        @Optional @Name("churnDays") Integer churnDays,
                                                        IServiceContext context) {
        LocalDate today = CoreMetrics.currentDate();
        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : today.minusDays(29);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : today;
        int effectiveChurnDays = churnDays != null && churnDays > 0 ? churnDays : DEFAULT_CHURN_DAYS;

        Timestamp startTs = Timestamp.valueOf(start.atTime(LocalTime.MIN));
        Timestamp endTs = Timestamp.valueOf(end.atTime(LocalTime.MAX));

        List<UserPaymentSummaryBean> periodSummaries = orderMapper.getUserPaymentSummaryInPeriod(startTs, endTs);
        Set<String> periodUsers = new HashSet<>();
        for (UserPaymentSummaryBean s : periodSummaries) {
            periodUsers.add(s.getUserId());
        }

        List<UserPaymentSummaryBean> allTimeSummaries = orderMapper.getUserPaymentSummaryAllTime();
        int newCount = 0, activeCount = 0, dormantCount = 0, churnedCount = 0;
        for (UserPaymentSummaryBean s : allTimeSummaries) {
            String userId = s.getUserId();
            LocalDate firstAll = s.getFirstPayTime() != null ? s.getFirstPayTime().toLocalDateTime().toLocalDate() : null;
            LocalDate lastAll = s.getLastPayTime() != null ? s.getLastPayTime().toLocalDateTime().toLocalDate() : null;
            if (lastAll == null) continue;

            boolean inPeriod = periodUsers.contains(userId);
            boolean firstInPeriod = firstAll != null && !firstAll.isBefore(start) && !firstAll.isAfter(end);
            String stage = classifyLifecycleStage(firstInPeriod, inPeriod, lastAll, today, effectiveChurnDays);
            switch (stage) {
                case "新客":
                    newCount++;
                    break;
                case "活跃":
                    activeCount++;
                    break;
                case "流失":
                    churnedCount++;
                    break;
                default:
                    dormantCount++;
                    break;
            }
        }

        int total = newCount + activeCount + dormantCount + churnedCount;
        List<LifecycleSegmentBean> result = new ArrayList<>();
        result.add(makeLifecycleSegment("新客", newCount, total));
        result.add(makeLifecycleSegment("活跃", activeCount, total));
        result.add(makeLifecycleSegment("沉睡", dormantCount, total));
        result.add(makeLifecycleSegment("流失", churnedCount, total));
        return result;
    }

    /**
     * P19 生命周期报表 + P20 用户画像共用：按首单/末单时间 + 活跃窗口 + churn 阈值判定当前阶段。
     * 同源抽取自原 getUserLifecycle 内联分类规则（行为不变，P19 测试零回归）。
     * 返回值固定为「新客 / 活跃 / 沉睡 / 流失」四段之一。
     *
     * @param firstInPeriod 该用户首单是否落在活跃窗口内（窗口边界由调用方按报表/画像口径确定）
     * @param inPeriod      该用户末单是否落在活跃窗口内
     * @param lastAllDate   全量历史末单日期（不可为 null）
     * @param today         当前日期
     * @param churnDays     流失阈值天数
     */
    private static String classifyLifecycleStage(boolean firstInPeriod, boolean inPeriod,
                                                  LocalDate lastAllDate, LocalDate today, int churnDays) {
        if (firstInPeriod) {
            return "新客";
        }
        if (inPeriod) {
            return "活跃";
        }
        long daysSinceLast = java.time.Duration.between(lastAllDate.atStartOfDay(), today.atStartOfDay()).toDays();
        if (daysSinceLast >= churnDays) {
            return "流失";
        }
        return "沉睡";
    }

    private static LifecycleSegmentBean makeLifecycleSegment(String segment, int count, int total) {
        LifecycleSegmentBean bean = new LifecycleSegmentBean();
        bean.setSegment(segment);
        bean.setUserCount(count);
        bean.setPercent(total > 0
                ? BigDecimal.valueOf(count).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        return bean;
    }

    @Override
    @BizQuery
    public List<RepurchaseRatePointBean> getRepurchaseRate(@Optional @Name("startDate") String startDate,
                                                            @Optional @Name("endDate") String endDate,
                                                            IServiceContext context) {
        LocalDate today = CoreMetrics.currentDate();
        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : today.minusDays(29);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : today;
        if (start.isAfter(end)) {
            start = end;
        }

        Timestamp startTs = Timestamp.valueOf(start.atTime(LocalTime.MIN));
        Timestamp endTs = Timestamp.valueOf(end.atTime(LocalTime.MAX));

        List<UserPaymentPointBean> points = orderMapper.getUserPaymentPoints(startTs, endTs);
        Map<String, Set<String>> usersByDay = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> ordersByDayUser = new LinkedHashMap<>();
        for (UserPaymentPointBean p : points) {
            if (p.getPayTime() == null || p.getUserId() == null) {
                continue;
            }
            String day = p.getPayTime().toLocalDateTime().toLocalDate().toString();
            usersByDay.computeIfAbsent(day, k -> new HashSet<>()).add(p.getUserId());
            ordersByDayUser.computeIfAbsent(day, k -> new HashMap<>())
                    .merge(p.getUserId(), 1, Integer::sum);
        }

        List<RepurchaseRatePointBean> result = new ArrayList<>();
        LocalDate d = start;
        while (!d.isAfter(end)) {
            String key = d.toString();
            Set<String> users = usersByDay.getOrDefault(key, Collections.emptySet());
            Map<String, Integer> orders = ordersByDayUser.getOrDefault(key, Collections.emptyMap());
            int paidUsers = users.size();
            int repurchaseUsers = 0;
            for (Integer cnt : orders.values()) {
                if (cnt != null && cnt >= 2) repurchaseUsers++;
            }
            RepurchaseRatePointBean bean = new RepurchaseRatePointBean();
            bean.setDateLabel(key);
            bean.setPaidUsers(paidUsers);
            bean.setRepurchaseUsers(repurchaseUsers);
            bean.setRate(ratio(repurchaseUsers, paidUsers));
            result.add(bean);
            d = d.plusDays(1);
        }
        return result;
    }

    // ===== P19 订单分析 + 营销分析 =====

    @Override
    @BizQuery
    public OrderAnalysisBean getOrderAnalysis(@Optional @Name("startDate") String startDate,
                                               @Optional @Name("endDate") String endDate,
                                               IServiceContext context) {
        Timestamp start = parseStartDate(startDate);
        Timestamp end = parseEndDate(endDate);

        List<AovDistributionBean> aov = orderMapper.getAovDistribution(start, end);
        List<PaymentMethodShareBean> payment = orderMapper.getPaymentMethodShare(start, end);
        List<ReturnReasonShareBean> returns = orderMapper.getReturnReasonShare(start, end);

        int totalPaid = 0;
        for (AovDistributionBean b : aov) {
            totalPaid += b.getOrderCount();
        }

        OrderAnalysisBean result = new OrderAnalysisBean();
        result.setAovDistribution(aov);
        result.setPaymentMethodShare(payment);
        result.setReturnReasonShare(returns);
        result.setTotalPaidOrders(totalPaid);
        return result;
    }

    @Override
    @BizQuery
    @Auth(roles = "admin")
    public WebContentBean exportReport(@Name("reportName") String reportName,
                                       @Name("renderType") String renderType,
                                       @Optional @Name("startDate") String startDate,
                                       @Optional @Name("endDate") String endDate,
                                       @Optional @Name("categoryId") String categoryId,
                                       IServiceContext context) {
        String safeType = ReportRenderTypes.validate(renderType);
        String name = normalizeReportName(reportName);
        IEvalScope scope = XLang.newEvalScope();
        switch (name) {
            case "funnel":
                scope.setLocalValue("funnelList", buildFunnelDataSet(
                        getSalesFunnel(startDate, endDate, context)));
                break;
            case "product":
                scope.setLocalValue("productList", buildProductDataSet(
                        getProductAnalysis(startDate, endDate, categoryId, context)));
                break;
            case "order":
                scope.setLocalValue("orderMetrics", buildOrderDataSet(
                        getOrderAnalysis(startDate, endDate, context)));
                break;
            case "user":
                buildUserDataSet(scope, startDate, endDate, context);
                break;
            case "coupon":
                scope.setLocalValue("couponMetrics", buildCouponDataSet(
                        getCouponAnalysis(startDate, endDate, context)));
                break;
            default:
                throw new NopException(AppMallErrors.ERR_REPORT_NAME_INVALID)
                        .param(AppMallErrors.ARG_REPORT_NAME, reportName);
        }

        String path = "/nop/main/report/" + reportTemplate(name) + ".xpt.xml";
        IResource resource = ResourceHelper.getTempResource("rpt");
        try {
            ITemplateOutput output = reportEngine.getRenderer(path, safeType);
            output.generateToResource(resource, scope);
            String fileName = name + "-report." + safeType;
            return new WebContentBean("application/octet-stream", resource.toFile(), fileName);
        } catch (Exception e) {
            resource.delete();
            throw NopException.adapt(e);
        }
    }

    private static String normalizeReportName(String reportName) {
        if (reportName == null) {
            throw new NopException(AppMallErrors.ERR_REPORT_NAME_INVALID)
                    .param(AppMallErrors.ARG_REPORT_NAME, reportName);
        }
        String n = reportName.trim().toLowerCase();
        if (!n.equals("funnel") && !n.equals("product") && !n.equals("order")
                && !n.equals("user") && !n.equals("coupon")) {
            throw new NopException(AppMallErrors.ERR_REPORT_NAME_INVALID)
                    .param(AppMallErrors.ARG_REPORT_NAME, reportName);
        }
        return n;
    }

    private static String reportTemplate(String reportName) {
        switch (reportName) {
            case "funnel":
                return "sales-funnel";
            case "product":
                return "product-analysis";
            case "order":
                return "order-analysis";
            case "user":
                return "user-analysis";
            case "coupon":
                return "coupon-analysis";
            default:
                throw new NopException(AppMallErrors.ERR_REPORT_NAME_INVALID)
                        .param(AppMallErrors.ARG_REPORT_NAME, reportName);
        }
    }

    private static List<Map<String, Object>> buildFunnelDataSet(SalesFunnelBean bean) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(funnelRow("view", bean.getViewCount(), "1.0"));
        list.add(funnelRow("cart", bean.getCartCount(), bean.getCartViewRatio()));
        list.add(funnelRow("order", bean.getOrderCount(), bean.getOrderCartRatio()));
        list.add(funnelRow("pay", bean.getPayCount(), bean.getPayOrderRatio()));
        list.add(funnelRow("repurchase", bean.getRepurchaseCount(), bean.getRepurchasePayRatio()));
        return list;
    }

    private static Map<String, Object> funnelRow(String stage, int count, BigDecimal rate) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("stage", stage);
        m.put("count", count);
        m.put("rate", rate == null ? "0" : rate.toPlainString());
        return m;
    }

    private static Map<String, Object> funnelRow(String stage, int count, String rate) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("stage", stage);
        m.put("count", count);
        m.put("rate", rate);
        return m;
    }

    private static List<Map<String, Object>> buildProductDataSet(ProductAnalysisBean bean) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<GoodsStatisticsBean> ranking = bean.getSalesRanking();
        if (ranking != null) {
            for (GoodsStatisticsBean g : ranking) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("goodsId", g.getGoodsId());
                m.put("goodsName", g.getGoodsName());
                m.put("number", g.getSalesCount());
                m.put("amount", g.getSalesAmount() == null ? "" : g.getSalesAmount().toPlainString());
                m.put("costAmount", g.getCostAmount() == null ? "" : g.getCostAmount().toPlainString());
                m.put("grossProfit", g.getGrossProfit() == null ? "" : g.getGrossProfit().toPlainString());
                m.put("marginRate", g.getMarginRate() == null ? "" : g.getMarginRate().toPlainString());
                list.add(m);
            }
        }
        return list;
    }

    private static List<Map<String, Object>> buildOrderDataSet(OrderAnalysisBean bean) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (bean.getAovDistribution() != null) {
            for (AovDistributionBean b : bean.getAovDistribution()) {
                list.add(orderRow("aov:" + b.getSegment(), b.getOrderCount()));
            }
        }
        if (bean.getPaymentMethodShare() != null) {
            for (PaymentMethodShareBean b : bean.getPaymentMethodShare()) {
                list.add(orderRow("pay:" + b.getMethod(), b.getOrderCount()));
            }
        }
        if (bean.getReturnReasonShare() != null) {
            for (ReturnReasonShareBean b : bean.getReturnReasonShare()) {
                list.add(orderRow("return:" + b.getReason(), b.getAftersaleCount()));
            }
        }
        return list;
    }

    private static Map<String, Object> orderRow(String label, int value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("label", label);
        m.put("value", value);
        return m;
    }

    /**
     * 用户分析导出数据集（多 sheet：retention/rfm/lifecycle/repurchase）。
     * 各子分析同时含绝对计数与比率（见 plan 2026-06-29-0119-1 Decision 选项 A）。
     * 导出适配层将中文段名映射为 Latin 代码：PDFBox 内置 Helvetica 仅 Latin（CJK 字体部署为本计划 Non-Goal，
     * 见 owner doc system-configuration.md），与模板 Latin 表头先例保持一致；原 {@code getXxx} 口径不变。
     */
    private void buildUserDataSet(IEvalScope scope, String startDate, String endDate, IServiceContext context) {
        List<Map<String, Object>> retention = new ArrayList<>();
        for (UserRetentionPointBean b : getUserRetention(startDate, endDate, context)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("dateLabel", b.getDateLabel());
            m.put("cohortSize", b.getCohortSize());
            m.put("d1", b.getD1());
            m.put("d7", b.getD7());
            m.put("d30", b.getD30());
            m.put("d1Rate", b.getD1Rate() == null ? "0" : b.getD1Rate().toPlainString());
            m.put("d7Rate", b.getD7Rate() == null ? "0" : b.getD7Rate().toPlainString());
            m.put("d30Rate", b.getD30Rate() == null ? "0" : b.getD30Rate().toPlainString());
            retention.add(m);
        }

        List<Map<String, Object>> rfm = new ArrayList<>();
        for (RfmSegmentBean b : getUserRfm(startDate, endDate, context)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("segment", latinRfmSegment(b.getSegment()));
            m.put("userCount", b.getUserCount());
            rfm.add(m);
        }

        List<Map<String, Object>> lifecycle = new ArrayList<>();
        for (LifecycleSegmentBean b : getUserLifecycle(startDate, endDate, null, context)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("segment", latinLifecycleSegment(b.getSegment()));
            m.put("userCount", b.getUserCount());
            m.put("percent", b.getPercent() == null ? "0" : b.getPercent().toPlainString());
            lifecycle.add(m);
        }

        List<Map<String, Object>> repurchase = new ArrayList<>();
        for (RepurchaseRatePointBean b : getRepurchaseRate(startDate, endDate, context)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("dateLabel", b.getDateLabel());
            m.put("paidUsers", b.getPaidUsers());
            m.put("repurchaseUsers", b.getRepurchaseUsers());
            m.put("rate", b.getRate() == null ? "0" : b.getRate().toPlainString());
            repurchase.add(m);
        }

        scope.setLocalValue("retentionList", retention);
        scope.setLocalValue("rfmList", rfm);
        scope.setLocalValue("lifecycleList", lifecycle);
        scope.setLocalValue("repurchaseList", repurchase);
    }

    private static String latinRfmSegment(String segment) {
        if (segment == null) return "";
        switch (segment) {
            case "重要价值用户": return "vip-value";
            case "重要保持用户": return "vip-retain";
            case "重要发展用户": return "vip-develop";
            case "重要挽留用户": return "vip-keep";
            case "一般价值用户": return "general-value";
            case "一般保持用户": return "general-retain";
            case "一般发展用户": return "general-develop";
            case "一般挽留用户": return "general-keep";
            default: return segment;
        }
    }

    private static String latinLifecycleSegment(String segment) {
        if (segment == null) return "";
        switch (segment) {
            case "新客": return "new";
            case "活跃": return "active";
            case "沉睡": return "dormant";
            case "流失": return "churned";
            default: return segment;
        }
    }

    /**
     * 优惠券分析导出数据集（单 sheet，claimedCount/usedCount/pulledGmv）。
     * 返回单行 List 以复用既有 `*=^ds!field` 展开模式（与 funnel/order 同构）。
     */
    private static List<Map<String, Object>> buildCouponDataSet(CouponUsageStatisticsBean bean) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("claimedCount", bean.getClaimedCount());
        m.put("usedCount", bean.getUsedCount());
        m.put("pulledGmv", bean.getPulledGmv() == null ? "0" : bean.getPulledGmv().toPlainString());
        return Collections.singletonList(m);
    }

    @Override
    @BizQuery
    public CouponUsageStatisticsBean getCouponAnalysis(@Optional @Name("startDate") String startDate,
                                                         @Optional @Name("endDate") String endDate,
                                                         IServiceContext context) {
        Timestamp start = parseStartDate(startDate);
        Timestamp end = parseEndDate(endDate);
        CouponUsageStatisticsBean bean = orderMapper.getCouponAnalysis(start, end);
        if (bean == null) {
            bean = new CouponUsageStatisticsBean();
            bean.setClaimedCount(0);
            bean.setUsedCount(0);
            bean.setPulledGmv(BigDecimal.ZERO);
        }
        if (bean.getPulledGmv() == null) {
            bean.setPulledGmv(BigDecimal.ZERO);
        }
        return bean;
    }

    // ===== P20 算法化用户画像 successor =====

    /**
     * 活跃窗口默认 30 天（与 P19 报表默认 period 一致），churn 默认 90 天（与 DEFAULT_CHURN_DAYS 一致）。
     * 画像口径见 docs/design/system-configuration.md「报表与统计 - 用户分析」。
     */
    private static final int PORTRAIT_ACTIVE_WINDOW_DAYS = 30;

    @Override
    @BizQuery
    public UserPortraitBean getUserPortrait(@Name("userId") String userId,
                                             IServiceContext context) {
        if (StringHelper.isEmpty(userId)) {
            throw new NopException(AppMallErrors.ERR_USER_NOT_FOUND).param(AppMallErrors.ARG_USER_ID, userId);
        }

        List<UserPaymentSummaryBean> allTimeSummaries = orderMapper.getUserPaymentSummaryAllTime();
        UserPaymentSummaryBean target = null;
        for (UserPaymentSummaryBean s : allTimeSummaries) {
            if (userId.equals(s.getUserId())) {
                target = s;
                break;
            }
        }

        UserPortraitBean portrait = new UserPortraitBean();
        portrait.setUserId(userId);
        if (target == null || target.getLastPayTime() == null) {
            portrait.setFrequency(0);
            portrait.setMonetary(BigDecimal.ZERO);
            return portrait;
        }

        long nowMillis = CoreMetrics.currentTimeMillis();
        long recencyDays = (nowMillis - target.getLastPayTime().getTime()) / (24 * 60 * 60 * 1000);
        portrait.setRecencyDays(recencyDays);
        portrait.setFrequency(target.getOrderCount());
        portrait.setMonetary(target.getTotalAmount() != null ? target.getTotalAmount() : BigDecimal.ZERO);
        portrait.setFirstPayTime(target.getFirstPayTime());
        portrait.setLastPayTime(target.getLastPayTime());

        RfmThresholds thresholds = computeRfmThresholds(allTimeSummaries);
        portrait.setRfmSegment(classifyRfmSegment(target, thresholds));

        LocalDate today = CoreMetrics.currentDate();
        LocalDate activeWindowStart = today.minusDays(PORTRAIT_ACTIVE_WINDOW_DAYS - 1);
        LocalDate firstAll = target.getFirstPayTime().toLocalDateTime().toLocalDate();
        LocalDate lastAll = target.getLastPayTime().toLocalDateTime().toLocalDate();
        boolean inPeriod = !lastAll.isBefore(activeWindowStart) && !lastAll.isAfter(today);
        boolean firstInPeriod = !firstAll.isBefore(activeWindowStart) && !firstAll.isAfter(today);
        portrait.setLifecycleStage(classifyLifecycleStage(firstInPeriod, inPeriod, lastAll, today, DEFAULT_CHURN_DAYS));
        return portrait;
    }

    @Override
    @BizQuery
    public PageBean<SegmentMemberBean> getSegmentMembers(@Name("segmentType") String segmentType,
                                                          @Name("segmentValue") String segmentValue,
                                                          @Optional @Name("page") Integer page,
                                                          @Optional @Name("pageSize") Integer pageSize,
                                                          IServiceContext context) {
        int effectivePage = page != null && page > 0 ? page : 1;
        int effectivePageSize = pageSize != null && pageSize > 0 ? Math.min(pageSize, 500) : 20;

        String normalizedType = segmentType == null ? "" : segmentType.trim().toLowerCase();
        boolean byRfm = "rfm".equals(normalizedType);
        boolean byLifecycle = "lifecycle".equals(normalizedType);
        if (!byRfm && !byLifecycle) {
            throw new NopException(AppMallErrors.ERR_USER_PORTRAIT_INVALID_SEGMENT_TYPE)
                    .param(AppMallErrors.ARG_SEGMENT_TYPE, segmentType);
        }
        if (StringHelper.isEmpty(segmentValue)) {
            throw new NopException(AppMallErrors.ERR_USER_PORTRAIT_INVALID_SEGMENT_VALUE)
                    .param(AppMallErrors.ARG_SEGMENT_VALUE, segmentValue);
        }

        List<UserPaymentSummaryBean> allTimeSummaries = orderMapper.getUserPaymentSummaryAllTime();
        if (allTimeSummaries.isEmpty()) {
            return emptyMemberPage(effectivePage, effectivePageSize);
        }

        RfmThresholds thresholds = computeRfmThresholds(allTimeSummaries);
        LocalDate today = CoreMetrics.currentDate();
        LocalDate activeWindowStart = today.minusDays(PORTRAIT_ACTIVE_WINDOW_DAYS - 1);

        List<SegmentMemberBean> matched = new ArrayList<>();
        for (UserPaymentSummaryBean s : allTimeSummaries) {
            if (s.getLastPayTime() == null) {
                continue;
            }
            String rfmSegment = classifyRfmSegment(s, thresholds);
            LocalDate firstAll = s.getFirstPayTime().toLocalDateTime().toLocalDate();
            LocalDate lastAll = s.getLastPayTime().toLocalDateTime().toLocalDate();
            boolean inPeriod = !lastAll.isBefore(activeWindowStart) && !lastAll.isAfter(today);
            boolean firstInPeriod = !firstAll.isBefore(activeWindowStart) && !firstAll.isAfter(today);
            String lifecycleStage = classifyLifecycleStage(firstInPeriod, inPeriod, lastAll, today, DEFAULT_CHURN_DAYS);

            boolean hit = byRfm ? segmentValue.equals(rfmSegment) : segmentValue.equals(lifecycleStage);
            if (!hit) {
                continue;
            }
            SegmentMemberBean member = new SegmentMemberBean();
            member.setUserId(s.getUserId());
            member.setLastPayTime(s.getLastPayTime());
            member.setOrderCount(s.getOrderCount());
            member.setTotalAmount(s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO);
            member.setRfmSegment(rfmSegment);
            member.setLifecycleStage(lifecycleStage);
            matched.add(member);
        }

        matched.sort((a, b) -> {
            int byCount = Integer.compare(b.getOrderCount(), a.getOrderCount());
            if (byCount != 0) return byCount;
            return b.getTotalAmount().compareTo(a.getTotalAmount());
        });

        return paginateMembers(matched, effectivePage, effectivePageSize);
    }

    private static PageBean<SegmentMemberBean> emptyMemberPage(int page, int pageSize) {
        PageBean<SegmentMemberBean> p = new PageBean<>();
        p.setOffset((long) (page - 1) * pageSize);
        p.setLimit(pageSize);
        p.setTotal(0L);
        p.setItems(new ArrayList<>());
        return p;
    }

    private static PageBean<SegmentMemberBean> paginateMembers(List<SegmentMemberBean> all, int page, int pageSize) {
        int total = all.size();
        int from = Math.min((page - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        List<SegmentMemberBean> slice = new ArrayList<>(all.subList(from, to));
        PageBean<SegmentMemberBean> p = new PageBean<>();
        p.setOffset((long) from);
        p.setLimit(pageSize);
        p.setTotal((long) total);
        p.setItems(slice);
        return p;
    }

    private static BigDecimal gmvOrZero(DashboardGmvBean bean) {
        return bean != null && bean.getGmv() != null ? bean.getGmv() : BigDecimal.ZERO;
    }

    private static BigDecimal growthRatio(BigDecimal current, BigDecimal compare) {
        if (compare == null || compare.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return current.subtract(compare).divide(compare, 4, RoundingMode.HALF_UP);
    }

    private static String trendBucketLabel(LocalDateTime payTime, String granularity) {
        switch (granularity) {
            case "hour":
                return payTime.toLocalDate().toString() + " " + String.format("%02d:00", payTime.getHour());
            case "week": {
                LocalDate monday = payTime.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                return monday.toString();
            }
            case "month":
                return String.format("%04d-%02d", payTime.getYear(), payTime.getMonthValue());
            case "day":
            default:
                return payTime.toLocalDate().toString();
        }
    }

    private static List<String> trendKeySequence(LocalDate start, LocalDate end, String granularity) {
        List<String> keys = new ArrayList<>();
        switch (granularity) {
            case "hour": {
                LocalDate d = start;
                while (!d.isAfter(end)) {
                    for (int h = 0; h < 24; h++) {
                        keys.add(d.toString() + " " + String.format("%02d:00", h));
                    }
                    d = d.plusDays(1);
                }
                break;
            }
            case "week": {
                LocalDate monday = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                while (!monday.isAfter(end)) {
                    keys.add(monday.toString());
                    monday = monday.plusWeeks(1);
                }
                break;
            }
            case "month": {
                LocalDate m = start.withDayOfMonth(1);
                while (!m.isAfter(end)) {
                    keys.add(String.format("%04d-%02d", m.getYear(), m.getMonthValue()));
                    m = m.plusMonths(1);
                }
                break;
            }
            case "day":
            default: {
                LocalDate d = start;
                while (!d.isAfter(end)) {
                    keys.add(d.toString());
                    d = d.plusDays(1);
                }
                break;
            }
        }
        return keys;
    }

    private int resolveStockThreshold(IServiceContext context) {
        String raw = systemBiz.getConfig(LitemallGoodsBizModel.CONFIG_STOCK_THRESHOLD_TIGHT, context);
        if (StringHelper.isEmpty(raw)) {
            return LitemallGoodsBizModel.DEFAULT_STOCK_THRESHOLD_TIGHT;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed >= 0 ? parsed : LitemallGoodsBizModel.DEFAULT_STOCK_THRESHOLD_TIGHT;
        } catch (NumberFormatException e) {
            return LitemallGoodsBizModel.DEFAULT_STOCK_THRESHOLD_TIGHT;
        }
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
            // Release promotion participation quota (mirrors cancel): soft-delete the PromotionUsage.
            releasePromotionUsage(order.orm_idString(), context);
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
            // Member level auto-evaluation (D3): same as manual confirm.
            try {
                memberLevelBiz.evaluateUserLevel(order.getUserId(), context);
            } catch (NopException e) {
                LOG.warn("confirmExpiredOrders: member level evaluation failed for user {}", order.getUserId(), e);
            }
            count++;
        }
        return count;
    }

    /**
     * 自提订单超时自动取消并退款（successor of P31 deferred「已支付未自提订单自动超时取消/退款」）。
     *
     * <p>扫描 {@code orderStatus=201(PAY)} 且 {@code deliveryType=PICKUP(10)} 且 {@code payTime}
     * 早于 {@code now - timeoutDays} 的订单，逐单：
     * <ol>
     *   <li>CAS-guard 翻转 {@code 201→203(REFUND_CONFIRM)}（并发守卫，败者 skip）；</li>
     *   <li>按 {@code order.payChannel} 分流退款（D2）：
     *     <ul>
     *       <li>BALANCE(20) → {@code walletBiz.creditBalance} 原路退回钱包
     *         （{@code WALLET_CHANGE_TYPE_REFUND + sourceType=order-refund}），不调 payService；</li>
     *       <li>WECHAT/ALIPAY/其余 → {@code payService.refund}（in-tx 失败抛
     *         {@code ERR_PICKUP_AUTO_CANCEL_REFUND_FAILED} 回滚）。</li>
     *     </ul>
     *   </li>
     *   <li>还库（{@code goodsProductMapper.addStock}）、还券（{@code couponUserBiz.returnCoupon}）、
     *     还积分（{@code returnDeductedPoints}）、释放满减参与额度（{@code releasePromotionUsage}）；</li>
     *   <li>写 endTime + updateEntity；</li>
     *   <li>afterCommit 推送 ORDER 站内信「订单超时取消并退款」（事件开关
     *     {@code mall_message_event_enabled_pickup_timeout} 关闭时 uid=null，sendUserMessage 内部 null-guard 跳过）；</li>
     *   <li>{@code logManager.logOrderSucceed}。</li>
     * </ol>
     *
     * <p>本方法在单事务内逐单处理；任意一单的 in-tx 外部退款失败将抛 NopException 回滚整批
     * （与 cancelExpiredOrders 一致的事务模型，避免部分成功造成 DB 与退款通道状态错位）。
     */
    @Override
    @BizMutation
    public int cancelExpiredPickupOrders(@Name("timeoutDays") int timeoutDays,
                                          IServiceContext context) {
        // CoreMetrics 与 ORM 自动 createTime/updateTime 同一时间线（与 cancelExpiredOrders 一致）。
        LocalDateTime cutoff = CoreMetrics.currentDateTime().minusDays(timeoutDays);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderStatus, _AppMallDaoConstants.ORDER_STATUS_PAY));
        // deliveryType eq 查询算子受 xmeta 支持（getOverdueUnshippedOrders 注释：仅 eq/in/dateBetween 可用）。
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_deliveryType, _AppMallDaoConstants.DELIVERY_TYPE_PICKUP));
        query.addFilter(FilterBeans.le(LitemallOrder.PROP_NAME_payTime, cutoff));
        query.setLimit(100);

        List<LitemallOrder> orders = doFindListByQueryDirectly(query, context);

        // Collect IDs then detach from session so the conditional CAS UPDATE executes as real SQL
        // (a conditional update on a session-managed entity is routed in-memory and reports 0
        // affected rows — same reason as cancelExpiredOrders:2310).
        List<String> orderIds = new ArrayList<>();
        for (LitemallOrder o : orders) {
            orderIds.add(o.orm_idString());
        }
        dao().clearEntitySessionCache();

        int count = 0;
        for (String orderId : orderIds) {
            // Atomic status guard: skip orders already transitioned by a concurrent path
            // (verifyPickupOrder CAS 201→401, or aftersale GOODS_MISS refund 201→203).
            int affected = orderMapper.updateStatusIfMatch(
                    orderId,
                    _AppMallDaoConstants.ORDER_STATUS_REFUND_CONFIRM,
                    _AppMallDaoConstants.ORDER_STATUS_PAY);
            if (affected == 0) {
                continue;
            }

            LitemallOrder order = get(orderId, false, context);
            BigDecimal refundAmount = order.getActualPrice();
            Integer payChannel = order.getPayChannel();

            if (payChannel != null && payChannel == _AppMallDaoConstants.PAY_CHANNEL_BALANCE) {
                // D2: balance-payment refund must credit the wallet back synchronously —
                // MockPayServiceImpl does not actually touch the wallet, so payService.refund would
                // report success but lose the user's funds. Mirror LitemallPointsExchangeOrderBizModel
                // refundComboCash balance branch (in-tx, no separate notify).
                walletBiz.creditBalance(order.getUserId(), refundAmount,
                        _AppMallDaoConstants.WALLET_CHANGE_TYPE_REFUND,
                        SOURCE_TYPE_ORDER_REFUND,
                        order.getOrderSn(),
                        "自提订单超时取消退还余额 " + order.getOrderSn(),
                        context);
            } else {
                // wxpay/mock/alipay channel: payService facade (in-tx; failure throws → rollback).
                PayRefundRequestBean refundReq = new PayRefundRequestBean();
                refundReq.setOutTradeNo(order.getOrderSn());
                refundReq.setOutRefundNo("refund_" + order.getOrderSn());
                refundReq.setTotalFee(refundAmount);
                refundReq.setRefundFee(refundAmount);
                PayRefundResponseBean refundResp = payService.refund(refundReq);
                if (!refundResp.isSuccess()) {
                    throw new NopException(ERR_PICKUP_AUTO_CANCEL_REFUND_FAILED)
                            .param(AppMallErrors.ARG_ORDER_ID, orderId)
                            .param("orderSn", order.getOrderSn());
                }
            }

            // Restock whole-order (mirrors cancelExpiredOrders / aftersale refund whole-order branch).
            for (LitemallOrderGoods orderGoods : order.getOrderGoods()) {
                goodsProductMapper.addStock(orderGoods.getProductId(), orderGoods.getNumber());
            }

            // Coupon restore (whole-order, mirrors cancelExpiredOrders).
            QueryBean cuQuery = new QueryBean();
            cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_orderId, orderId));
            cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_status, 1));
            List<LitemallCouponUser> usedCoupons = couponUserBiz.findList(cuQuery, null, context);
            for (LitemallCouponUser cu : usedCoupons) {
                couponUserBiz.returnCoupon(cu.orm_idString(), context);
            }

            order.setEndTime(CoreMetrics.currentDateTime());
            updateEntity(order, "cancelExpiredPickupOrders", context);
            // Return deducted points (whole-order, mirrors cancelExpiredOrders).
            returnDeductedPoints(order, context);
            // Release promotion participation quota (whole-order).
            releasePromotionUsage(order.orm_idString(), context);

            // Notification: ORDER 站内信, afterCommit + isEventMessageEnabled + uid-null-skip
            // (mirrors verifyPickupOrder / payment / ship notification pattern).
            final String timeoutOrderSn = order.getOrderSn();
            final String timeoutUserId = notificationService.isEventMessageEnabled("pickup_timeout", context)
                    ? order.getUserId() : null;
            txn().afterCommit(null, () -> notificationService.sendUserMessage(
                    timeoutUserId, _AppMallDaoConstants.MSG_TYPE_ORDER,
                    "订单超时取消并退款", "订单 " + timeoutOrderSn + " 因超时未自提已自动取消并退款"));

            logManager.logOrderSucceed("自提订单超时自动取消退款",
                    "订单编号 " + order.getOrderSn());
            count++;
        }
        return count;
    }

    // sourceType for wallet credit-back on whole-order refund (symmetric with
    // LitemallWalletBizModel.SOURCE_TYPE_PAY used by payByBalance debit).
    private static final String SOURCE_TYPE_ORDER_REFUND = "order-refund";

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
                                               @Optional @Name("flashSaleSessionId") String flashSaleSessionId,
                                               IServiceContext context) {
        // P24 flash-sale direct-buy order creation (independent path, NOT submit()).
        // flashPrice is the unit price (商品单价层); coupon / promotion / integral / groupon /
        // pinTuan slots are all zeroed. Single OrderGoods line. No cart involvement.
        // flashSaleSessionId is written for per-session attribution (maxPerUser counting +
        // getFlashSaleEffectiveness). Non-flash-sale orders keep the column null.
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
        order.setFlashSaleSessionId(flashSaleSessionId);

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

    @Override
    public void releasePromotionUsage(@Name("orderId") String orderId, IServiceContext context) {
        // Mirrors the coupon-return / points-return precedent at all 6 whole-order refund sites.
        // PromotionUsage uses logical delete (useLogicalDelete + deleteFlagProp=deleted), so
        // deleteEntity is a soft delete; the maxPerUser findCount auto-applies deleted=false ->
        // the released quota lets the user re-participate. Idempotent: findList auto-filters
        // deleted=true, so a repeat call finds 0 rows and deletes nothing. Partial-item refunds
        // do NOT reach this helper (promotion is an order-level discount; partial refund keeps the
        // participation, same boundary as partial refunds not returning coupons/points).
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallPromotionUsage.PROP_NAME_orderId, orderId));
        List<LitemallPromotionUsage> usages = promotionUsageBiz.findList(query, null, context);
        for (LitemallPromotionUsage usage : usages) {
            promotionUsageBiz.deleteEntity(usage, null, context);
        }
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

    // ===================== 订单运营工作台（P21） =====================

    /**
     * 改价 / 改运费（P21）。安全策略（见 {@code docs/design/order-and-cart.md}）：
     * 改运费 freightPrice：仅待支付（101）可改，仅重算 orderPrice/actualPrice。
     * 改商品价 goodsPrice：仅当 couponPrice/promotionPrice/integralPrice/grouponPrice/pinTuanPrice 全为 0
     *   （纯商品订单，无任何活动折扣）时允许并重算；任一折扣非 0 拒绝（ERR_ORDER_PRICE_MODIFY_DISCOUNT_ACTIVE）。
     *
     * <p>状态守卫：仅待支付（101）允许改价（管理员未发起收款前可调整）。已支付（201+）订单已固化入账金额，
     * 不允许改价（ERR_ORDER_NOT_ALLOW_MODIFY_PRICE）。
     *
     * <p>本方法接受「增量」参数：freightPriceDelta/goodsPriceDelta 为可正可负的差值。绝对改值场景由前端
     * 先读旧值计算差值传入；以增量形式接收入参更明确地表达「修改」语义，也避免前端并发覆盖。
     */
    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallOrder modifyOrderPrice(@Name("orderId") String orderId,
                                           @Optional @Name("freightPriceDelta") BigDecimal freightPriceDelta,
                                           @Optional @Name("goodsPriceDelta") BigDecimal goodsPriceDelta,
                                           @Optional @Name("remark") String remark,
                                           IServiceContext context) {
        LitemallOrder order = requireEntity(orderId, null, context);

        // 状态守卫：仅待支付允许改价（已支付订单已固化入账金额）
        if (order.getOrderStatus() != _AppMallDaoConstants.ORDER_STATUS_CREATED) {
            throw new NopException(ERR_ORDER_NOT_ALLOW_MODIFY_PRICE)
                    .param(AppMallErrors.ARG_ORDER_ID, orderId)
                    .param(AppMallErrors.ARG_CURRENT_STATUS, order.getOrderStatus());
        }

        BigDecimal freightDelta = freightPriceDelta != null ? freightPriceDelta : BigDecimal.ZERO;
        BigDecimal goodsDelta = goodsPriceDelta != null ? goodsPriceDelta : BigDecimal.ZERO;

        // 改商品价守卫：纯商品订单（无任何活动折扣）才允许
        if (goodsDelta.compareTo(BigDecimal.ZERO) != 0) {
            boolean hasDiscount = isNonZero(order.getCouponPrice())
                    || isNonZero(order.getPromotionPrice())
                    || isNonZero(order.getIntegralPrice())
                    || isNonZero(order.getGrouponPrice())
                    || isNonZero(order.getPinTuanPrice());
            if (hasDiscount) {
                throw new NopException(ERR_ORDER_PRICE_MODIFY_DISCOUNT_ACTIVE)
                        .param(AppMallErrors.ARG_ORDER_ID, orderId);
            }
        }

        // freightPrice / goodsPrice 重算
        BigDecimal newFreightPrice = addNonNull(order.getFreightPrice(), freightDelta);
        BigDecimal newGoodsPrice = addNonNull(order.getGoodsPrice(), goodsDelta);
        // 最低 0：不出现负价
        if (newFreightPrice.compareTo(BigDecimal.ZERO) < 0) {
            newFreightPrice = BigDecimal.ZERO;
        }
        if (newGoodsPrice.compareTo(BigDecimal.ZERO) < 0) {
            newGoodsPrice = BigDecimal.ZERO;
        }

        order.setFreightPrice(newFreightPrice);
        order.setGoodsPrice(newGoodsPrice);

        // orderPrice = goodsPrice + freightPrice − couponPrice − promotionPrice
        BigDecimal orderPrice = newGoodsPrice.add(newFreightPrice)
                .subtract(nonNull(order.getCouponPrice()))
                .subtract(nonNull(order.getPromotionPrice()));
        order.setOrderPrice(orderPrice);

        // actualPrice = orderPrice − integralPrice − grouponPrice − pinTuanPrice
        BigDecimal actualPrice = orderPrice
                .subtract(nonNull(order.getIntegralPrice()))
                .subtract(nonNull(order.getGrouponPrice()))
                .subtract(nonNull(order.getPinTuanPrice()));
        if (actualPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new NopException(ERR_ORDER_PRICE_INVALID).param("actualPrice", actualPrice);
        }
        order.setActualPrice(actualPrice);

        if (remark != null && !remark.isEmpty()) {
            // 运营备注：补丁式追加（不覆盖既有 adminRemark）
            String existing = order.getAdminRemark() != null ? order.getAdminRemark() : "";
            order.setAdminRemark(existing + (existing.isEmpty() ? "" : "\n") + remark);
        }

        updateEntity(order, "modifyOrderPrice", context);
        logManager.logOrderSucceed("订单改价",
                "订单 " + order.getOrderSn() + " freightΔ=" + freightDelta + " goodsΔ=" + goodsDelta);
        return order;
    }

    /**
     * 批量发货（P21）。Excel 经平台 {@link ExcelHelper#readSheet} 解析（orderSn/shipSn/shipChannel），
     * 逐行复用 {@link #ship} 单行逻辑；部分失败不阻断成功行。
     *
     * <p>{@code excelUpload} 为 AMIS 上传返回的 {@code /f/download/{fileId}} 链接。
     *
     * <p>事务边界：直接调本类 {@link #ship}（不经 GraphQL 代理，无独立事务边界）；当前 {@code batchShip}
     * 整体运行在 {@code @BizMutation} 默认事务内。部分失败不阻断：每行失败仅 catch 记入结果列表，
     * 循环继续，已成功行不被回滚（{@link #ship} 的状态守卫 + 本方法的 catch-and-continue 共同保证）。
     */
    @Override
    @BizMutation
    @Auth(roles = "admin")
    public List<BatchShipResultBean> batchShip(@Name("excelUpload") String excelUpload,
                                                IServiceContext context) {
        List<Map<String, Object>> rows = parseBatchShipExcel(excelUpload);
        return doBatchShip(rows, context);
    }

    // package-private for test access (avoids file-store roundtrip in tests)
    List<Map<String, Object>> parseBatchShipExcel(String excelUpload) {
        if (excelUpload == null || excelUpload.isEmpty()) {
            throw new NopException(ERR_ORDER_BATCH_SHIP_EMPTY);
        }
        String fileId = ormEntityFileStore.decodeFileId(excelUpload);
        if (fileId == null || fileId.isEmpty()) {
            fileId = excelUpload;
        }
        IFileRecord fileRecord = fileStore.getFile(fileId);
        IResource xlsx = fileRecord != null ? fileRecord.getResource() : null;
        if (xlsx == null) {
            throw new NopException(ERR_ORDER_BATCH_SHIP_EMPTY);
        }
        try {
            List<Map<String, Object>> rows = ExcelHelper.readSheet(xlsx, null, 0);
            if (rows == null || rows.isEmpty()) {
                throw new NopException(ERR_ORDER_BATCH_SHIP_EMPTY);
            }
            return rows;
        } catch (NopException e) {
            throw e;
        } catch (Exception e) {
            LOG.warn("batchShip: failed to parse xlsx {}", excelUpload, e);
            throw new NopException(ERR_ORDER_BATCH_SHIP_EMPTY);
        }
    }

    // package-private for test access (tests build rows directly to bypass file-store)
    List<BatchShipResultBean> doBatchShip(List<Map<String, Object>> rows, IServiceContext context) {
        List<BatchShipResultBean> results = new ArrayList<>();
        int rowIndex = 1; // 表头是第 1 行，数据从第 2 行起，错误信息中按"数据行号"从 1 计
        for (Map<String, Object> row : rows) {
            rowIndex++;
            String orderSn = stringValue(row, "orderSn");
            String shipSn = stringValue(row, "shipSn");
            String shipChannel = stringValue(row, "shipChannel");

            if (StringHelper.isEmpty(orderSn) || StringHelper.isEmpty(shipSn)
                    || StringHelper.isEmpty(shipChannel)) {
                results.add(new BatchShipResultBean(orderSn, false,
                        "数据行 " + rowIndex + " 字段缺失（orderSn/shipSn/shipChannel 必填）"));
                continue;
            }

            try {
                // 按 orderSn 定位订单
                QueryBean q = new QueryBean();
                q.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderSn, orderSn));
                LitemallOrder order = findFirst(q, null, context);
                if (order == null) {
                    results.add(new BatchShipResultBean(orderSn, false, "订单不存在"));
                    continue;
                }
                // 复用 ship 的状态守卫与发货逻辑（不经 GraphQL，直接调本类方法）
                ship(order.orm_idString(), shipSn, shipChannel, context);
                results.add(new BatchShipResultBean(orderSn, true, null));
            } catch (NopException e) {
                String reason = e.getDescription() != null ? e.getDescription() : e.getMessage();
                results.add(new BatchShipResultBean(orderSn, false, reason));
            } catch (Exception e) {
                results.add(new BatchShipResultBean(orderSn, false, e.getMessage()));
            }
        }
        int success = (int) results.stream().filter(BatchShipResultBean::isSuccess).count();
        logManager.logOrderSucceed("批量发货",
                "共 " + results.size() + " 行，成功 " + success + " 行");
        return results;
    }

    /**
     * 改地址（P21）。仅发货前（待支付 101 / 已支付未发货 201）可改；新地址经 {@code ILitemallAddressBiz}
     * 校验归属同一用户后写入 consignee/mobile/address。
     */
    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallOrder changeOrderAddress(@Name("orderId") String orderId,
                                             @Name("addressId") String addressId,
                                             IServiceContext context) {
        LitemallOrder order = requireEntity(orderId, null, context);

        int status = order.getOrderStatus();
        if (status != _AppMallDaoConstants.ORDER_STATUS_CREATED
                && status != _AppMallDaoConstants.ORDER_STATUS_PAY) {
            throw new NopException(ERR_ORDER_NOT_ALLOW_CHANGE_ADDRESS)
                    .param(AppMallErrors.ARG_ORDER_ID, orderId)
                    .param(AppMallErrors.ARG_CURRENT_STATUS, status);
        }

        LitemallAddress address = addressBiz.get(addressId, false, context);
        if (address == null || Boolean.TRUE.equals(address.getDeleted())) {
            throw new NopException(ERR_ORDER_ADDRESS_INVALID).param("addressId", addressId);
        }
        if (!Objects.equals(order.getUserId(), address.getUserId())) {
            throw new NopException(ERR_ORDER_ADDRESS_NOT_BELONG_USER)
                    .param(AppMallErrors.ARG_ORDER_ID, orderId)
                    .param("addressId", addressId)
                    .param(AppMallErrors.ARG_USER_ID, order.getUserId());
        }

        order.setConsignee(address.getName());
        order.setMobile(address.getTel());
        String fullAddress = (address.getProvince() != null ? address.getProvince() : "")
                + (address.getCity() != null ? address.getCity() : "")
                + (address.getCounty() != null ? address.getCounty() : "")
                + (address.getAddressDetail() != null ? address.getAddressDetail() : "");
        order.setAddress(fullAddress);

        updateEntity(order, "changeOrderAddress", context);
        logManager.logOrderSucceed("订单改地址", "订单 " + order.getOrderSn());
        return order;
    }

    /**
     * 订单标记（P21）。写既有 {@code adminRemark} 字段（surface 既有列，无 ORM 改动）。
     * 整字段覆盖语义：以传入值作为最新运营备注（前端若需追加，自行拼串后传入）。
     */
    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallOrder markOrder(@Name("orderId") String orderId,
                                    @Name("adminRemark") String adminRemark,
                                    IServiceContext context) {
        LitemallOrder order = requireEntity(orderId, null, context);
        order.setAdminRemark(adminRemark != null ? adminRemark : "");
        updateEntity(order, "markOrder", context);
        logManager.logOrderSucceed("订单标记", "订单 " + order.getOrderSn());
        return order;
    }

    /**
     * 超期未发货订单查询（P21 异常监控）。status=201 已支付且 addTime 早于 cutoff；
     * cutoff 默认 168 小时（与系统配置的自动收货时长 7 天对齐）。
     *
     * <p>使用 QueryBean 走管道：异常监控为跨用户聚合查询，{@code @Auth(roles="admin")} 限定仅管理员可达。
     * status=PAY(201) 已隐含 shipTime 未记录（发货即转 SHIP 301），故无需额外 isNull 过滤。
     */
    @Override
    @BizQuery
    @Auth(roles = "admin")
    public List<LitemallOrder> getOverdueUnshippedOrders(@Optional @Name("cutoffHours") Integer cutoffHours,
                                                          IServiceContext context) {
        // 仅 null 时取默认；显式 0 表示「立即视为逾期」（用于工作台人工审视全部未发货订单）
        int hours = cutoffHours != null ? cutoffHours : DEFAULT_OVERDUE_UNSHIPPED_HOURS;
        if (hours < 0) {
            hours = DEFAULT_OVERDUE_UNSHIPPED_HOURS;
        }
        // CoreMetrics 与 ORM 自动 createTime/updateTime 同一时间线（与 cancelExpiredOrders 一致）
        LocalDateTime cutoff = CoreMetrics.currentDateTime().minusHours(hours);
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderStatus,
                _AppMallDaoConstants.ORDER_STATUS_PAY));
        query.addFilter(FilterBeans.le(LitemallOrder.PROP_NAME_addTime, cutoff));
        query.addOrderField(LitemallOrder.PROP_NAME_addTime, false);
        query.setLimit(100);
        // P31 隔离 (c): 自提订单合法停留 201 待核销，不污染逾期未发货列表。
        // deliveryType 的 xmeta 查询算子受限（仅 eq/in/dateBetween），无法用 ne/isNull，
        // 故在 Java 层过滤 PICKUP 订单（null deliveryType 的存量订单视为快递，保留）。
        List<LitemallOrder> orders = findList(query, null, context);
        List<LitemallOrder> result = new ArrayList<>();
        for (LitemallOrder o : orders) {
            if (o.getDeliveryType() == null
                    || o.getDeliveryType() != _AppMallDaoConstants.DELIVERY_TYPE_PICKUP) {
                result.add(o);
            }
        }
        return result;
    }

    /**
     * 超期未支付订单查询（P21 异常监控）。status=101 待支付且 addTime 早于 cutoff；
     * cutoff 默认 30 分钟（与系统配置的订单超时分钟数对齐）。
     */
    @Override
    @BizQuery
    @Auth(roles = "admin")
    public List<LitemallOrder> getOverdueUnpaidOrders(@Optional @Name("cutoffMinutes") Integer cutoffMinutes,
                                                       IServiceContext context) {
        int minutes = cutoffMinutes != null ? cutoffMinutes : DEFAULT_OVERDUE_UNPAID_MINUTES;
        if (minutes < 0) {
            minutes = DEFAULT_OVERDUE_UNPAID_MINUTES;
        }
        LocalDateTime cutoff = CoreMetrics.currentDateTime().minusMinutes(minutes);
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_orderStatus,
                _AppMallDaoConstants.ORDER_STATUS_CREATED));
        query.addFilter(FilterBeans.le(LitemallOrder.PROP_NAME_addTime, cutoff));
        query.addOrderField(LitemallOrder.PROP_NAME_addTime, false);
        query.setLimit(100);
        return findList(query, null, context);
    }

    private static final int DEFAULT_OVERDUE_UNSHIPPED_HOURS = 168;
    private static final int DEFAULT_OVERDUE_UNPAID_MINUTES = 30;

    private static boolean isNonZero(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) != 0;
    }

    private static BigDecimal addNonNull(BigDecimal a, BigDecimal b) {
        return nonNull(a).add(nonNull(b));
    }

    private static BigDecimal nonNull(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static String stringValue(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) {
            return null;
        }
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }
}
