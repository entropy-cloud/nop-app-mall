package app.mall.service.entity;

import app.mall.biz.ILitemallCouponBiz;
import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallGoodsBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallCoupon;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.manager.MallLogManager;
import app.mall.dao.mapper.LitemallCouponUserMapper;
import io.nop.api.core.annotations.biz.BizAction;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallCouponUser")
public class LitemallCouponUserBizModel extends CrudBizModel<LitemallCouponUser> implements ILitemallCouponUserBiz {

    private static final ConcurrentHashMap<String, Object> CLAIM_LOCKS = new ConcurrentHashMap<>();

    @Inject
    ILitemallCouponBiz couponBiz;

    @Inject
    ILitemallGoodsBiz goodsBiz;

    @Inject
    LitemallCouponUserMapper couponUserMapper;

    @Inject
    MallLogManager logManager;

    public LitemallCouponUserBizModel() {
        setEntityName(LitemallCouponUser.class.getName());
    }

    @Override
    @BizMutation
    public LitemallCouponUser claimCoupon(@Name("couponId") String couponId,
                                            IServiceContext context) {
        String userId = context.getUserId();
        return claimCouponForUser(couponId, userId, context);
    }

    @Override
    @BizAction
    public LitemallCouponUser claimCouponForUser(@Name("couponId") String couponId,
                                                  @Name("userId") String userId,
                                                  IServiceContext context) {
        String lockKey = couponId + "::" + userId;
        Object lock = CLAIM_LOCKS.computeIfAbsent(lockKey, k -> new Object());
        try {
            synchronized (lock) {
                LitemallCoupon coupon = couponBiz.get(couponId, false, context);
                if (coupon == null || Boolean.TRUE.equals(coupon.getDeleted())) {
                    throw new NopException(ERR_COUPON_NOT_FOUND).param("couponId", couponId);
                }
                if (coupon.getStatus() != 0) {
                    throw new NopException(ERR_COUPON_NOT_AVAILABLE).param("couponId", couponId);
                }
                if (coupon.getTotal() != null) {
                    QueryBean usedQuery = new QueryBean();
                    usedQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_couponId, couponId));
                    long claimed = findCount(usedQuery, context);
                    if (claimed >= coupon.getTotal()) {
                        throw new NopException(ERR_COUPON_NOT_AVAILABLE).param("couponId", couponId);
                    }
                }
                if (coupon.getLimit() != null && coupon.getLimit() > 0) {
                    QueryBean userQuery = new QueryBean();
                    userQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_userId, userId));
                    userQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_couponId, couponId));
                    long userClaimed = findCount(userQuery, context);
                    if (userClaimed >= coupon.getLimit()) {
                        throw new NopException(ERR_COUPON_LIMIT_EXCEEDED).param("couponId", couponId);
                    }
                }

                LitemallCouponUser couponUser = newEntity();
                couponUser.setUserId(userId);
                couponUser.setCouponId(couponId);
                couponUser.setStatus(0);
                couponUser.setOrderId("");

                if (coupon.getTimeType() != null && coupon.getTimeType() == 0 && coupon.getDays() != null) {
                    LocalDateTime now = LocalDateTime.now();
                    couponUser.setStartTime(now);
                    couponUser.setEndTime(now.plusDays(coupon.getDays()));
                } else {
                    couponUser.setStartTime(coupon.getStartTime());
                    couponUser.setEndTime(coupon.getEndTime());
                }

                saveEntity(couponUser, null, context);

                return couponUser;
            }
        } finally {
            CLAIM_LOCKS.remove(lockKey, lock);
        }
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallCouponUser dispatchCoupon(@Name("couponId") String couponId,
                                             @Name("userId") String userId,
                                             @Optional @Name("remark") String remark,
                                             IServiceContext context) {
        // Admin manual coupon dispatch: wraps claimCouponForUser so all total/limit/status
        // validations are reused, then records the operation for audit. Closes the P26/P32
        // "member-exclusive coupon auto-dispatch" deferred via an explicit manual path.
        LitemallCouponUser couponUser = claimCouponForUser(couponId, userId, context);
        logManager.logGeneralSucceed("dispatchCoupon",
                "couponId=" + couponId + ", userId=" + userId
                        + (StringHelper.isEmpty(remark) ? "" : ", remark=" + remark));
        return couponUser;
    }

    @Override
    @BizMutation
    public LitemallCouponUser redeemCoupon(@Name("code") String code,
                                            IServiceContext context) {
        String userId = context.getUserId();

        QueryBean couponQuery = new QueryBean();
        couponQuery.addFilter(FilterBeans.eq(LitemallCoupon.PROP_NAME_code, code));
        LitemallCoupon coupon = couponBiz.findFirst(couponQuery, null, context);
        if (coupon == null) {
            throw new NopException(ERR_COUPON_CODE_INVALID)
                    .param("code", code);
        }
        if (coupon.getStatus() != 0) {
            throw new NopException(ERR_COUPON_NOT_AVAILABLE)
                    .param("code", code);
        }

        return claimCoupon(coupon.orm_idString(), context);
    }

    @Override
    @BizQuery
    public BigDecimal selectCouponForOrder(@Name("couponUserId") String couponUserId,
                                            @Name("goodsPrice") BigDecimal goodsPrice,
                                            @Optional @Name("goodsIds") List<String> goodsIds,
                                            IServiceContext context) {
        LitemallCouponUser couponUser = get(couponUserId, false, context);
        if (couponUser == null || Boolean.TRUE.equals(couponUser.getDeleted())) {
            throw new NopException(ERR_COUPON_USER_NOT_FOUND)
                    .param("couponUserId", couponUserId);
        }
        if (couponUser.getStatus() != 0) {
            throw new NopException(ERR_COUPON_NOT_USABLE)
                    .param("couponUserId", couponUserId);
        }

        LocalDateTime now = LocalDateTime.now();
        if (couponUser.getEndTime() != null && now.isAfter(couponUser.getEndTime())) {
            throw new NopException(ERR_COUPON_NOT_USABLE)
                    .param("couponUserId", couponUserId);
        }
        if (couponUser.getStartTime() != null && now.isBefore(couponUser.getStartTime())) {
            throw new NopException(ERR_COUPON_NOT_USABLE)
                    .param("couponUserId", couponUserId);
        }

        if (!context.getUserId().equals(couponUser.getUserId())) {
            throw new NopException(ERR_COUPON_USER_NOT_FOUND)
                    .param("couponUserId", couponUserId);
        }

        LitemallCoupon coupon = couponUser.getCoupon();
        if (coupon == null) {
            throw new NopException(ERR_COUPON_NOT_FOUND)
                    .param("couponUserId", couponUserId);
        }

        BigDecimal min = coupon.getMin();
        if (min != null && goodsPrice.compareTo(min) < 0) {
            throw new NopException(ERR_COUPON_MIN_NOT_MET)
                    .param("goodsPrice", goodsPrice)
                    .param("min", min);
        }

        if (coupon.getGoodsType() != null && coupon.getGoodsType() != _AppMallDaoConstants.COUPON_GOODS_TYPE_ALL) {
            if (goodsIds == null || goodsIds.isEmpty()) {
                throw new NopException(ERR_COUPON_GOODS_NOT_MATCH)
                        .param("couponUserId", couponUserId);
            }

            String goodsValue = coupon.getGoodsValue();
            if (goodsValue != null && !goodsValue.isEmpty()) {
                List<String> allowedIds = parseGoodsValue(goodsValue);
                if (coupon.getGoodsType() == _AppMallDaoConstants.COUPON_GOODS_TYPE_CATEGORY) {
                    Set<String> orderCategoryIds = collectCategoryIds(goodsIds, context);
                    boolean anyMatch = false;
                    for (String catId : orderCategoryIds) {
                        if (allowedIds.contains(catId)) {
                            anyMatch = true;
                            break;
                        }
                    }
                    if (!anyMatch) {
                        throw new NopException(ERR_COUPON_GOODS_NOT_MATCH)
                                .param("couponUserId", couponUserId)
                                .param("goodsType", coupon.getGoodsType());
                    }
                } else {
                    for (String gid : goodsIds) {
                        if (!allowedIds.contains(gid)) {
                            throw new NopException(ERR_COUPON_GOODS_NOT_MATCH)
                                    .param("goodsId", gid);
                        }
                    }
                }
            }
        }

        return coupon.getDiscount() != null ? coupon.getDiscount() : BigDecimal.ZERO;
    }

    private Set<String> collectCategoryIds(List<String> goodsIds, IServiceContext context) {
        Set<String> categoryIds = new HashSet<>();
        for (String gid : goodsIds) {
            LitemallGoods goods = goodsBiz.get(gid, false, context);
            if (goods != null && goods.getCategoryId() != null) {
                categoryIds.add(goods.getCategoryId());
            }
        }
        return categoryIds;
    }

    @Override
    @BizMutation
    public void useCoupon(@Name("couponUserId") String couponUserId,
                           @Name("orderId") String orderId,
                           IServiceContext context) {
        // Atomic conditional UPDATE prevents concurrent double-redemption: only wins if status still 0.
        int affected = couponUserMapper.updateStatusIfUnused(couponUserId, orderId);
        if (affected == 0) {
            // Either not found, or already used by a concurrent redemption
            LitemallCouponUser couponUser = get(couponUserId, false, context);
            if (couponUser == null) {
                throw new NopException(ERR_COUPON_USER_NOT_FOUND)
                        .param("couponUserId", couponUserId);
            }
            throw new NopException(ERR_COUPON_NOT_USABLE)
                    .param("couponUserId", couponUserId);
        }
    }

    @Override
    @BizMutation
    public void returnCoupon(@Name("couponUserId") String couponUserId,
                              IServiceContext context) {
        LitemallCouponUser couponUser = requireEntity(couponUserId, null, context);
        couponUser.setStatus(0);
        couponUser.setUsedTime(null);
        couponUser.setOrderId("");
    }

    @Override
    @BizMutation
    public int expireCoupons(IServiceContext context) {
        LocalDateTime now = LocalDateTime.now();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_status, 0));
        query.addFilter(FilterBeans.lt(LitemallCouponUser.PROP_NAME_endTime, now));
        query.setLimit(500);

        List<LitemallCouponUser> expired = doFindListByQueryDirectly(query, context);
        for (LitemallCouponUser cu : expired) {
            cu.setStatus(2);
        }
        return expired.size();
    }

    private List<String> parseGoodsValue(String goodsValue) {
        if (goodsValue == null || goodsValue.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return io.nop.commons.util.StringHelper.split(goodsValue, ',');
    }
}
