
package app.mall.service.entity;

import app.mall.biz.ILitemallCouponBiz;
import app.mall.biz.ILitemallFlashSaleBiz;
import app.mall.biz.ILitemallPinTuanActivityBiz;
import app.mall.biz.ILitemallPromotionActivityBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.dto.CouponUsageStatisticsBean;
import app.mall.dao.dto.FlashSaleEffectivenessBean;
import app.mall.dao.dto.MarketingReportRequest;
import app.mall.dao.dto.PinTuanEffectivenessBean;
import app.mall.dao.dto.PromotionEffectivenessBean;
import app.mall.dao.dto.PromotionResolutionBean;
import app.mall.dao.entity.LitemallPromotionActivity;
import app.mall.dao.entity.LitemallPromotionTier;
import app.mall.dao.mapper.LitemallMarketingMapper;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.biz.RequestBean;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.WebContentBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.api.core.json.JSON;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import io.nop.core.lang.eval.IEvalScope;
import io.nop.core.resource.IResource;
import io.nop.core.resource.ResourceHelper;
import io.nop.core.resource.tpl.ITemplateOutput;
import io.nop.orm.IOrmEntitySet;
import io.nop.report.core.engine.IReportEngine;
import io.nop.xlang.api.XLang;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static app.mall.service.AppMallErrors.ERR_PROMOTION_STATUS_TRANSITION_INVALID;

@BizModel("LitemallPromotionActivity")
public class LitemallPromotionActivityBizModel extends CrudBizModel<LitemallPromotionActivity> implements ILitemallPromotionActivityBiz {
    public LitemallPromotionActivityBizModel(){
        setEntityName(LitemallPromotionActivity.class.getName());
    }

    // @SqlLibMapper for aggregation SQL (promotion GMV/discount) that QueryBean cannot express.
    @Inject
    LitemallMarketingMapper marketingMapper;

    @Inject
    IReportEngine reportEngine;

    @Inject
    ILitemallFlashSaleBiz flashSaleBiz;

    @Inject
    ILitemallPinTuanActivityBiz pinTuanActivityBiz;

    @Inject
    ILitemallCouponBiz couponBiz;

    private static final Timestamp MIN_TIMESTAMP = Timestamp.valueOf("1970-01-01 00:00:00");
    private static final Timestamp MAX_TIMESTAMP = Timestamp.valueOf("2099-12-31 23:59:59");

    @Override
    @BizMutation
    public LitemallPromotionActivity publishActivity(@Name("id") String id, IServiceContext context) {
        LitemallPromotionActivity activity = requireEntity(id, null, context);
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
    public LitemallPromotionActivity unpublishActivity(@Name("id") String id, IServiceContext context) {
        LitemallPromotionActivity activity = requireEntity(id, null, context);
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
    public PromotionEffectivenessBean getPromotionEffectiveness(@Optional @Name("activityId") String activityId,
                                                                @Optional @Name("startDate") String startDate,
                                                                @Optional @Name("endDate") String endDate,
                                                                IServiceContext context) {
        // Per-activity attribution (PromotionUsage model-gap closure): when activityId is provided,
        // aggregate via PromotionUsage (distinct participants, per-activity GMV/discount/order count).
        // When activityId is null, keep the time-window aggregate over promotionPrice>0 orders.
        Timestamp start = startDate != null && !startDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(startDate).atTime(LocalTime.MIN)) : MIN_TIMESTAMP;
        Timestamp end = endDate != null && !endDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(endDate).atTime(LocalTime.MAX)) : MAX_TIMESTAMP;
        if (activityId != null && !activityId.isEmpty()) {
            return marketingMapper.getPromotionEffectivenessByActivity(activityId, start, end);
        }
        return marketingMapper.getPromotionEffectiveness(start, end);
    }

    @Override
    @BizQuery
    @Auth(roles = "admin")
    public WebContentBean exportMarketingReport(@RequestBean MarketingReportRequest request,
                                                IServiceContext context) {
        String safeType = ReportRenderTypes.validate(request.getRenderType());
        IEvalScope scope = XLang.newEvalScope();
        scope.setLocalValue("promotionEffect", buildPromotionEffectDataSet(
                getPromotionEffectiveness(request.getPromotionActivityId(),
                        request.getStartDate(), request.getEndDate(), context)));
        scope.setLocalValue("flashSaleEffect", buildFlashSaleEffectDataSet(
                flashSaleBiz.getFlashSaleEffectiveness(request.getFlashSaleId(),
                        request.getStartDate(), request.getEndDate(), context)));
        scope.setLocalValue("pinTuanEffect", buildPinTuanEffectDataSet(
                pinTuanActivityBiz.getPinTuanEffectiveness(request.getPinTuanActivityId(),
                        request.getStartDate(), request.getEndDate(), context)));
        scope.setLocalValue("couponUsage", buildCouponUsageDataSet(
                couponBiz.getCouponUsageStatistics(request.getCouponId(),
                        request.getStartDate(), request.getEndDate(), context)));

        IResource resource = ResourceHelper.getTempResource("rpt");
        try {
            ITemplateOutput output = reportEngine.getRenderer(
                    "/nop/main/report/marketing-effect.xpt.xml", safeType);
            output.generateToResource(resource, scope);
            String fileName = "marketing-effect." + safeType;
            return new WebContentBean("application/octet-stream", resource.toFile(), fileName);
        } catch (Exception e) {
            resource.delete();
            throw NopException.adapt(e);
        }
    }

    private static List<Map<String, Object>> buildPromotionEffectDataSet(PromotionEffectivenessBean bean) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("promotedOrderCount", bean != null ? bean.getPromotedOrderCount() : 0);
        m.put("totalGmv", bean != null && bean.getTotalGmv() != null ? bean.getTotalGmv().toPlainString() : "0");
        m.put("totalDiscount", bean != null && bean.getTotalDiscount() != null ? bean.getTotalDiscount().toPlainString() : "0");
        m.put("participantCount", bean != null ? bean.getParticipantCount() : 0);
        return Collections.singletonList(m);
    }

    private static List<Map<String, Object>> buildFlashSaleEffectDataSet(FlashSaleEffectivenessBean bean) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("dealOrderCount", bean != null ? bean.getDealOrderCount() : 0);
        m.put("totalGmv", bean != null && bean.getTotalGmv() != null ? bean.getTotalGmv().toPlainString() : "0");
        m.put("participantCount", bean != null ? bean.getParticipantCount() : 0);
        m.put("soldOutRate", bean != null && bean.getSoldOutRate() != null ? bean.getSoldOutRate().toPlainString() : "0");
        m.put("rejectedCount", bean != null ? bean.getRejectedCount() : 0);
        return Collections.singletonList(m);
    }

    private static List<Map<String, Object>> buildPinTuanEffectDataSet(PinTuanEffectivenessBean bean) {
        int openedGroups = bean != null ? bean.getOpenedGroups() : 0;
        int successGroups = bean != null ? bean.getSuccessGroups() : 0;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("openedGroups", openedGroups);
        m.put("successGroups", successGroups);
        m.put("participantCount", bean != null ? bean.getParticipantCount() : 0);
        m.put("totalGmv", bean != null && bean.getTotalGmv() != null ? bean.getTotalGmv().toPlainString() : "0");
        m.put("successRate", ratio(successGroups, openedGroups));
        return Collections.singletonList(m);
    }

    private static List<Map<String, Object>> buildCouponUsageDataSet(CouponUsageStatisticsBean bean) {
        int claimedCount = bean != null ? bean.getClaimedCount() : 0;
        int usedCount = bean != null ? bean.getUsedCount() : 0;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("claimedCount", claimedCount);
        m.put("usedCount", usedCount);
        m.put("pulledGmv", bean != null && bean.getPulledGmv() != null ? bean.getPulledGmv().toPlainString() : "0");
        m.put("usedRate", ratio(usedCount, claimedCount));
        return Collections.singletonList(m);
    }

    private static BigDecimal ratio(int numerator, int denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }

    @Override
    @BizQuery
    public BigDecimal selectPromotionForOrder(@Name("goodsPrice") BigDecimal goodsPrice,
                                              @Optional @Name("goodsScopeIds") List<String> goodsScopeIds,
                                              IServiceContext context) {
        // Public GraphQL preview contract preserved: delegates to the internal resolver and
        // returns only the discount. submit() uses resolvePromotionForOrderInternal directly to
        // also obtain the hit activityId (needed to write PromotionUsage). Single source of truth
        // lives in resolvePromotionForOrderInternal.
        PromotionResolutionBean resolved = resolvePromotionForOrderInternal(goodsPrice, goodsScopeIds, context);
        return resolved != null ? resolved.getDiscount() : BigDecimal.ZERO;
    }

    @Override
    public PromotionResolutionBean resolvePromotionForOrderInternal(BigDecimal goodsPrice,
                                                                     List<String> goodsScopeIds,
                                                                     IServiceContext context) {
        BigDecimal effectiveGoodsPrice = goodsPrice != null ? goodsPrice : BigDecimal.ZERO;
        List<String> scopeIds = goodsScopeIds != null ? goodsScopeIds : Collections.emptyList();

        LocalDateTime now = LocalDateTime.now();

        // The xmeta only allows eq/in/dateBetween/dateTimeBetween on the time fields, so the active
        // time-window (startTime <= now <= endTime) is enforced in Java after loading candidates.
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallPromotionActivity.PROP_NAME_status, _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE));
        query.addOrderField(LitemallPromotionActivity.PROP_NAME_priority, true);

        List<LitemallPromotionActivity> activities = findList(query, null, context);

        // Among eligible active activities within their time window, the highest-priority activity
        // that both matches the goods scope AND has a tier whose threshold is met wins. Ties on
        // priority are broken by the larger discount.
        Integer bestPriority = null;
        BigDecimal bestDiscount = BigDecimal.ZERO;
        String bestActivityId = null;
        for (LitemallPromotionActivity activity : activities) {
            if (!isInTimeWindow(activity, now) || !isScopeEligible(activity, scopeIds)) {
                continue;
            }
            BigDecimal tierDiscount = computeBestTierDiscount(activity, effectiveGoodsPrice);
            if (tierDiscount == null) {
                continue;
            }
            Integer priority = activity.getPriority();
            if (bestPriority == null
                    || (priority != null && priority > bestPriority)
                    || (priority != null && priority.equals(bestPriority) && tierDiscount.compareTo(bestDiscount) > 0)) {
                bestPriority = priority != null ? priority : 0;
                bestDiscount = tierDiscount;
                bestActivityId = activity.orm_idString();
            }
        }
        if (bestDiscount.compareTo(BigDecimal.ZERO) <= 0 || bestActivityId == null) {
            return null;
        }
        PromotionResolutionBean result = new PromotionResolutionBean();
        result.setActivityId(bestActivityId);
        result.setDiscount(bestDiscount);
        // meetAmount = the order goodsPrice at the moment the tier threshold was met.
        result.setMeetAmount(effectiveGoodsPrice);
        return result;
    }

    private boolean isInTimeWindow(LitemallPromotionActivity activity, LocalDateTime now) {
        LocalDateTime start = activity.getStartTime();
        LocalDateTime end = activity.getEndTime();
        if (start != null && now.isBefore(start)) {
            return false;
        }
        return end == null || !now.isAfter(end);
    }

    private boolean isScopeEligible(LitemallPromotionActivity activity, List<String> scopeIds) {
        Integer scope = activity.getGoodsScope();
        if (scope == null || scope == _AppMallDaoConstants.GOODS_SCOPE_ALL) {
            return true;
        }
        List<String> allowed = parseScopeValue(activity.getGoodsScopeValue());
        if (allowed.isEmpty()) {
            return false;
        }
        for (String id : allowed) {
            if (scopeIds.contains(id)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal computeBestTierDiscount(LitemallPromotionActivity activity, BigDecimal goodsPrice) {
        IOrmEntitySet<LitemallPromotionTier> tiers = activity.getTiers();
        if (tiers == null || tiers.isEmpty()) {
            return null;
        }
        BigDecimal best = null;
        for (LitemallPromotionTier tier : tiers) {
            BigDecimal meetAmount = tier.getMeetAmount();
            if (meetAmount == null || goodsPrice.compareTo(meetAmount) < 0) {
                continue;
            }
            BigDecimal discount = applyDiscount(activity.getDiscountType(), tier.getDiscountValue(), goodsPrice);
            if (discount != null && (best == null || discount.compareTo(best) > 0)) {
                best = discount;
            }
        }
        return best;
    }

    private BigDecimal applyDiscount(Integer discountType, BigDecimal discountValue, BigDecimal goodsPrice) {
        if (discountValue == null) {
            return null;
        }
        if (discountType != null && discountType == _AppMallDaoConstants.DISCOUNT_TYPE_PERCENT) {
            // discountValue is a discount rate (e.g. 0.9 => 90% of price). Discount = price * (1 - rate).
            BigDecimal pay = goodsPrice.multiply(discountValue);
            return goodsPrice.subtract(pay).setScale(2, RoundingMode.HALF_UP);
        }
        return discountValue;
    }

    @SuppressWarnings("unchecked")
    private List<String> parseScopeValue(String goodsScopeValue) {
        if (goodsScopeValue == null || goodsScopeValue.isEmpty()) {
            return Collections.emptyList();
        }
        Object parsed = JSON.parse(goodsScopeValue);
        if (parsed instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object o : (List<Object>) parsed) {
                if (o != null) {
                    result.add(String.valueOf(o));
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
}
