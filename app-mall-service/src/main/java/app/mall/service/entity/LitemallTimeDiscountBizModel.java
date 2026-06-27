package app.mall.service.entity;

import app.mall.biz.ILitemallGoodsBiz;
import app.mall.biz.ILitemallTimeDiscountBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallTimeDiscount;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.api.core.time.CoreMetrics;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static app.mall.service.AppMallErrors.ERR_PROMOTION_STATUS_TRANSITION_INVALID;
import static app.mall.service.AppMallErrors.ERR_TIME_DISCOUNT_INVALID_VALUE;

@BizModel("LitemallTimeDiscount")
public class LitemallTimeDiscountBizModel extends CrudBizModel<LitemallTimeDiscount> implements ILitemallTimeDiscountBiz {
    public LitemallTimeDiscountBizModel() {
        setEntityName(LitemallTimeDiscount.class.getName());
    }

    @Inject
    ILitemallGoodsBiz goodsBiz;

    @Override
    @BizMutation
    public LitemallTimeDiscount publishActivity(@Name("id") String id, IServiceContext context) {
        LitemallTimeDiscount discount = requireEntity(id, null, context);
        Integer status = discount.getStatus();
        if (status != null && (status == _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE
                || status == _AppMallDaoConstants.PROMOTION_STATUS_FINISHED)) {
            throw new NopException(ERR_PROMOTION_STATUS_TRANSITION_INVALID)
                    .param("activityId", id)
                    .param("currentStatus", status)
                    .param("targetStatus", _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        }
        discount.setStatus(_AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        return discount;
    }

    @Override
    @BizMutation
    public LitemallTimeDiscount unpublishActivity(@Name("id") String id, IServiceContext context) {
        LitemallTimeDiscount discount = requireEntity(id, null, context);
        Integer status = discount.getStatus();
        if (status != null && (status == _AppMallDaoConstants.PROMOTION_STATUS_CLOSED
                || status == _AppMallDaoConstants.PROMOTION_STATUS_DRAFT)) {
            throw new NopException(ERR_PROMOTION_STATUS_TRANSITION_INVALID)
                    .param("activityId", id)
                    .param("currentStatus", status)
                    .param("targetStatus", _AppMallDaoConstants.PROMOTION_STATUS_CLOSED);
        }
        discount.setStatus(_AppMallDaoConstants.PROMOTION_STATUS_CLOSED);
        return discount;
    }

    @Override
    @BizQuery
    public Map<String, Object> selectTimeDiscountForProduct(@Name("goodsId") String goodsId,
                                                             @Optional @Name("productId") String productId,
                                                             IServiceContext context) {
        if (goodsId == null || goodsId.isEmpty()) {
            return null;
        }
        LitemallGoods goods = goodsBiz.get(goodsId, false, context);
        if (goods == null || Boolean.TRUE.equals(goods.getDeleted())) {
            return null;
        }
        BigDecimal retailPrice = goods.getRetailPrice();
        if (retailPrice == null || retailPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        LitemallTimeDiscount best = findBestTimeDiscount(goodsId, productId, retailPrice, context);
        if (best == null) {
            return null;
        }
        return toDiscountMap(best, retailPrice);
    }

    public LitemallTimeDiscount findBestTimeDiscount(String goodsId, String productId,
                                                     BigDecimal retailPrice, IServiceContext context) {
        if (goodsId == null || retailPrice == null) {
            return null;
        }
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallTimeDiscount.PROP_NAME_goodsId, goodsId));
        query.addFilter(FilterBeans.eq(LitemallTimeDiscount.PROP_NAME_status, _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE));

        List<LitemallTimeDiscount> candidates = findList(query, null, context);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        LocalDateTime now = CoreMetrics.currentDateTime();

        LitemallTimeDiscount best = null;
        BigDecimal bestPrice = null;
        for (LitemallTimeDiscount d : candidates) {
            if (!isInTimeWindow(d, now)) {
                continue;
            }
            // Match target: discount.productId == null means all SKUs of the goods; otherwise exact SKU match.
            String discountProductId = d.getProductId();
            if (discountProductId != null && !discountProductId.equals(productId)) {
                continue;
            }
            BigDecimal promo = computePromoPrice(d, retailPrice);
            if (promo == null) {
                continue;
            }
            if (bestPrice == null
                    || promo.compareTo(bestPrice) < 0
                    || (promo.compareTo(bestPrice) == 0 && isNewerStart(d, best))) {
                best = d;
                bestPrice = promo;
            }
        }
        return best;
    }

    public BigDecimal computePromoPrice(LitemallTimeDiscount discount, BigDecimal retailPrice) {
        if (discount == null || discount.getDiscountValue() == null || retailPrice == null) {
            return null;
        }
        BigDecimal value = discount.getDiscountValue();
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new NopException(ERR_TIME_DISCOUNT_INVALID_VALUE)
                    .param("discountId", discount.orm_idString())
                    .param("discountValue", value);
        }
        BigDecimal promo;
        if (discount.getDiscountType() != null
                && discount.getDiscountType() == _AppMallDaoConstants.DISCOUNT_TYPE_PERCENT) {
            // Aligned with P15: discountValue is a decimal pay-rate (0.9 => 90% of price = 9-fold).
            promo = retailPrice.multiply(value);
        } else {
            // discountType=0 (减金额/直降): discountValue is a direct cut.
            promo = retailPrice.subtract(value);
        }
        promo = promo.setScale(2, RoundingMode.HALF_UP);
        // Over-discount guard: promoPrice must be positive and actually below retail.
        if (promo.compareTo(BigDecimal.ZERO) <= 0 || promo.compareTo(retailPrice) >= 0) {
            return null;
        }
        return promo;
    }

    private boolean isInTimeWindow(LitemallTimeDiscount discount, LocalDateTime now) {
        LocalDateTime start = discount.getStartTime();
        LocalDateTime end = discount.getEndTime();
        if (start != null && now.isBefore(start)) {
            return false;
        }
        return end == null || !now.isAfter(end);
    }

    private boolean isNewerStart(LitemallTimeDiscount candidate, LitemallTimeDiscount current) {
        if (current == null) {
            return true;
        }
        LocalDateTime cs = candidate.getStartTime();
        LocalDateTime bs = current.getStartTime();
        if (cs == null) {
            return false;
        }
        return bs == null || cs.isAfter(bs);
    }

    private Map<String, Object> toDiscountMap(LitemallTimeDiscount discount, BigDecimal retailPrice) {
        BigDecimal promoPrice = computePromoPrice(discount, retailPrice);
        Map<String, Object> result = new HashMap<>();
        result.put("id", discount.orm_idString());
        result.put("promoPrice", promoPrice);
        result.put("originalPrice", retailPrice);
        result.put("discountAmount", retailPrice.subtract(promoPrice));
        result.put("discountType", discount.getDiscountType());
        result.put("endTime", discount.getEndTime());
        result.put("stockLimit", discount.getStockLimit());
        result.put("remainingStock", discount.getStockLimit());
        return result;
    }
}
