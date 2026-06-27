package app.mall.service.entity;

import app.mall.biz.ILitemallCouponBiz;
import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallGoodsBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.dto.CouponUsageStatisticsBean;
import app.mall.dao.entity.LitemallCoupon;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.mapper.LitemallMarketingMapper;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@BizModel("LitemallCoupon")
public class LitemallCouponBizModel extends CrudBizModel<LitemallCoupon> implements ILitemallCouponBiz {

    @Inject
    ILitemallCouponUserBiz couponUserBiz;

    @Inject
    ILitemallGoodsBiz goodsBiz;

    // @SqlLibMapper for aggregation SQL that I*Biz/QueryBean cannot express (coupon GMV join). This
    // is the sanctioned cross-entity aggregation path per AGENTS.md cross-entity rule.
    @Inject
    LitemallMarketingMapper marketingMapper;

    private static final Timestamp MIN_TIMESTAMP = Timestamp.valueOf("1970-01-01 00:00:00");
    private static final Timestamp MAX_TIMESTAMP = Timestamp.valueOf("2099-12-31 23:59:59");

    public LitemallCouponBizModel() {
        setEntityName(LitemallCoupon.class.getName());
    }

    @Override
    @BizMutation
    public LitemallCoupon publishCoupon(@Name("id") String id,
                                         IServiceContext context) {
        LitemallCoupon coupon = requireEntity(id, null, context);
        coupon.setStatus(0);
        return coupon;
    }

    @Override
    @BizMutation
    public LitemallCoupon unpublishCoupon(@Name("id") String id,
                                           IServiceContext context) {
        LitemallCoupon coupon = requireEntity(id, null, context);
        coupon.setStatus(2);
        return coupon;
    }

    @Override
    @BizQuery
    public CouponUsageStatisticsBean getCouponUsageStatistics(@Optional @Name("couponId") String couponId,
                                                                @Optional @Name("startDate") String startDate,
                                                                @Optional @Name("endDate") String endDate,
                                                                IServiceContext context) {
        Timestamp start = startDate != null && !startDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(startDate).atTime(LocalTime.MIN)) : MIN_TIMESTAMP;
        Timestamp end = endDate != null && !endDate.isEmpty()
                ? Timestamp.valueOf(LocalDate.parse(endDate).atTime(LocalTime.MAX)) : MAX_TIMESTAMP;
        return marketingMapper.getCouponUsageStatistics(couponId, start, end);
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallCoupon> listAvailableCoupons(@Name("page") int page,
                                                           @Name("pageSize") int pageSize,
                                                           IServiceContext context) {
        LocalDateTime now = LocalDateTime.now();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCoupon.PROP_NAME_status, 0));
        query.addFilter(FilterBeans.eq(LitemallCoupon.PROP_NAME_type, 0));

        query.addFilter(FilterBeans.or(
                FilterBeans.isNull(LitemallCoupon.PROP_NAME_endTime),
                FilterBeans.gt(LitemallCoupon.PROP_NAME_endTime, now)
        ));

        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallCoupon.PROP_NAME_addTime, true);

        return findPage(query, null, context);
    }

    @Override
    @BizQuery
    public PageBean<LitemallCouponUser> listMyCoupons(@Optional @Name("status") Integer status,
                                                        @Name("page") int page,
                                                        @Name("pageSize") int pageSize,
                                                        IServiceContext context) {
        String userId = context.getUserId();

        QueryBean cuQuery = new QueryBean();
        cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_userId, userId));
        if (status != null) {
            cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_status, status));
        }

        return couponUserBiz.findPage(cuQuery, null, context);
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public List<Map<String, Object>> listCouponsForGoods(@Name("goodsId") String goodsId,
                                                          IServiceContext context) {
        LitemallGoods goods = goodsBiz.get(goodsId, false, context);
        String goodsCategoryId = goods != null ? goods.getCategoryId() : null;

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCoupon.PROP_NAME_status, _AppMallDaoConstants.COUPON_STATUS_NORMAL));
        query.addFilter(FilterBeans.eq(LitemallCoupon.PROP_NAME_type, 0));
        query.addFilter(FilterBeans.or(
                FilterBeans.isNull(LitemallCoupon.PROP_NAME_endTime),
                FilterBeans.gt(LitemallCoupon.PROP_NAME_endTime, LocalDateTime.now())
        ));
        query.addOrderField(LitemallCoupon.PROP_NAME_addTime, true);

        List<LitemallCoupon> candidates = findList(query, null, context);

        String userId = context.getUserId();
        Map<String, Long> claimedCounts = collectClaimedCounts(userId, candidates, context);

        List<Map<String, Object>> result = new ArrayList<>();
        for (LitemallCoupon coupon : candidates) {
            if (!matchesGoodsScope(coupon, goodsId, goodsCategoryId, context)) {
                continue;
            }

            long claimedCount = claimedCounts.getOrDefault(coupon.getId(), 0L);
            boolean claimedByMe = claimedCount > 0;
            boolean claimable = computeClaimable(coupon, claimedCount, userId != null);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", coupon.orm_idString());
            entry.put("name", coupon.getName());
            entry.put("desc", coupon.getDesc());
            entry.put("tag", coupon.getTag());
            entry.put("discount", coupon.getDiscount());
            entry.put("min", coupon.getMin());
            entry.put("goodsType", coupon.getGoodsType());
            entry.put("goodsValue", coupon.getGoodsValue());
            entry.put("limit", coupon.getLimit());
            entry.put("total", coupon.getTotal());
            entry.put("timeType", coupon.getTimeType());
            entry.put("days", coupon.getDays());
            entry.put("startTime", coupon.getStartTime());
            entry.put("endTime", coupon.getEndTime());
            entry.put("claimedByMe", claimedByMe);
            entry.put("claimable", claimable);
            result.add(entry);
        }
        return result;
    }

    private Map<String, Long> collectClaimedCounts(String userId, List<LitemallCoupon> candidates, IServiceContext context) {
        Map<String, Long> result = new HashMap<>();
        if (userId == null || userId.isEmpty() || candidates.isEmpty()) {
            return result;
        }
        List<String> candidateIds = new ArrayList<>();
        for (LitemallCoupon c : candidates) {
            candidateIds.add(c.orm_idString());
        }

        QueryBean cuQuery = new QueryBean();
        cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_userId, userId));
        cuQuery.addFilter(FilterBeans.in(LitemallCouponUser.PROP_NAME_couponId, candidateIds));

        List<LitemallCouponUser> claimed = couponUserBiz.findList(cuQuery, null, context);
        for (LitemallCouponUser cu : claimed) {
            result.merge(cu.getCouponId(), 1L, Long::sum);
        }
        return result;
    }

    private boolean computeClaimable(LitemallCoupon coupon, long userClaimedCount, boolean isLoggedIn) {
        if (isLoggedIn) {
            Integer limit = coupon.getLimit();
            if (limit != null && limit > 0 && userClaimedCount >= limit) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesGoodsScope(LitemallCoupon coupon, String goodsId, String goodsCategoryId, IServiceContext context) {
        Integer goodsType = coupon.getGoodsType();
        if (goodsType == null || goodsType == _AppMallDaoConstants.COUPON_GOODS_TYPE_ALL) {
            return true;
        }
        List<String> allowedIds = parseGoodsValue(coupon.getGoodsValue());
        if (allowedIds.isEmpty()) {
            return false;
        }
        if (goodsType == _AppMallDaoConstants.COUPON_GOODS_TYPE_CATEGORY) {
            if (goodsCategoryId == null) {
                return false;
            }
            return allowedIds.contains(goodsCategoryId);
        }
        if (goodsType == _AppMallDaoConstants.COUPON_GOODS_TYPE_GOODS) {
            return allowedIds.contains(goodsId);
        }
        return false;
    }

    private List<String> parseGoodsValue(String goodsValue) {
        if (goodsValue == null || goodsValue.isEmpty()) {
            return new ArrayList<>();
        }
        return io.nop.commons.util.StringHelper.split(goodsValue, ',');
    }
}
