package app.mall.service.entity;

import app.mall.biz.ILitemallCouponBiz;
import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallGoodsBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.biz.ILitemallUserMessageBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallCoupon;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallUserMessage;
import app.mall.dao.manager.MallLogManager;
import app.mall.dao.mapper.LitemallCouponUserMapper;
import app.mall.service.notification.MallNotificationService;
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
import io.nop.api.core.time.CoreMetrics;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.orm.IOrmEntity;
import io.nop.orm.IOrmTemplate;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallCouponUser")
public class LitemallCouponUserBizModel extends CrudBizModel<LitemallCouponUser> implements ILitemallCouponUserBiz {

    private static final ConcurrentHashMap<String, Object> CLAIM_LOCKS = new ConcurrentHashMap<>();

    // 过期预警提前天数 config key：缺失时走 DEFAULT_COUPON_EXPIRY_REMIND_DAYS（Decision D1）。
    public static final String CONFIG_COUPON_EXPIRY_REMIND_DAYS = "mall_coupon_expiry_remind_days";
    public static final int DEFAULT_COUPON_EXPIRY_REMIND_DAYS = 3;
    // 过期预警单轮扫描券数上限（参照 expireCoupons limit=500）。
    static final int EXPIRY_REMIND_BATCH_LIMIT = 500;

    // 站内信事件开关 key 与标题（D2 幂等查询依据：当日同 userId+msgType(MARKETING)+title 已存在则跳过）。
    static final String EVENT_KEY_COUPON_EXPIRY_REMIND = "coupon-expiry-remind";
    static final String EXPIRY_REMIND_TITLE = "优惠券即将过期";

    @Inject
    ILitemallCouponBiz couponBiz;

    @Inject
    ILitemallGoodsBiz goodsBiz;

    @Inject
    LitemallCouponUserMapper couponUserMapper;

    @Inject
    MallLogManager logManager;

    @Inject
    IOrmTemplate ormTemplate;

    @Inject
    MallNotificationService notificationService;

    @Inject
    ILitemallUserMessageBiz userMessageBiz;

    @Inject
    ILitemallSystemBiz systemBiz;

    // NopAuthUser base entity name for reading userLevel (Delta extension column).
    private static final String NOP_AUTH_USER_NAME = io.nop.auth.dao.entity.NopAuthUser.class.getName();
    private static final String PROP_USER_LEVEL = "userLevel";

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
                // Member-exclusive coupon access control (D1): user must meet minMemberLevel.
                Integer minLevel = coupon.getMinMemberLevel();
                if (minLevel != null && minLevel > 0) {
                    int userLevel = readUserLevel(userId);
                    if (userLevel < minLevel) {
                        throw new NopException(ERR_COUPON_MEMBER_LEVEL_INSUFFICIENT)
                                .param("couponId", couponId)
                                .param("userLevel", userLevel)
                                .param("minMemberLevel", minLevel);
                    }
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

    @Override
    @BizMutation
    public int sendCouponExpiryReminders(IServiceContext context) {
        // 过期前置预警（successor of pull-style「我的优惠券」发现）：每日扫描近 N 天即将过期且
        // 未使用的用户券（status=0 且 now<=endTime<=now+remindDays），按 userId 聚合（Σ 张数 +
        // 最早 endTime），向对应用户推一条聚合 MARKETING 站内信。镜像积分过期预警模式
        // （LitemallPointsAccountBizModel.sendPointsExpiryReminders）。D1 msgType=MARKETING（优惠券
        // 为营销资产，预警促转化）；D2 每日每用户至多一条聚合消息（幂等查当日同 userId+msgType+
        // title 的 UserMessage）；D3 聚合摘要不逐张列券名；D4 窗口查询 endTime ASC。endTime 列非
        // mandatory，NULL 行被 ge/le SQL 语义自动排除（安全）。
        if (!notificationService.isEventMessageEnabled(EVENT_KEY_COUPON_EXPIRY_REMIND, context)) {
            return 0;
        }
        int remindDays = resolveIntConfig(CONFIG_COUPON_EXPIRY_REMIND_DAYS,
                DEFAULT_COUPON_EXPIRY_REMIND_DAYS, context);
        LocalDateTime now = CoreMetrics.currentDateTime();
        LocalDateTime windowEnd = now.plusDays(remindDays);

        // doFindListByQueryDirectly：系统上下文批量扫描 job，不走 per-user 鉴权管道（参照同文件
        // expireCoupons 既有模式）。endTime ASC 保证聚合时取到最早过期时间作为提醒日期。
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_status, 0));
        query.addFilter(FilterBeans.ge(LitemallCouponUser.PROP_NAME_endTime, now));
        query.addFilter(FilterBeans.le(LitemallCouponUser.PROP_NAME_endTime, windowEnd));
        query.addOrderField(LitemallCouponUser.PROP_NAME_endTime, false);
        query.setLimit(EXPIRY_REMIND_BATCH_LIMIT);
        List<LitemallCouponUser> expiring = doFindListByQueryDirectly(query, context);

        // 按 userId 聚合（Σ count + 最早 endTime），LinkedHashMap 迭代保留 endTime ASC 首见顺序。
        Map<String, int[]> countByUser = new LinkedHashMap<>();
        Map<String, LocalDateTime> earliestByUser = new LinkedHashMap<>();
        for (LitemallCouponUser cu : expiring) {
            String uid = cu.getUserId();
            if (StringHelper.isEmpty(uid)) {
                continue;
            }
            countByUser.computeIfAbsent(uid, k -> new int[]{0})[0] += 1;
            earliestByUser.putIfAbsent(uid, cu.getEndTime());
        }

        int pushed = 0;
        for (Map.Entry<String, LocalDateTime> entry : earliestByUser.entrySet()) {
            String uid = entry.getKey();
            LocalDateTime earliest = entry.getValue();
            int sum = countByUser.get(uid)[0];
            if (sum <= 0) {
                continue;
            }
            if (hasTodayExpiryReminder(uid, context)) {
                continue;
            }
            String dateText = formatDateOnly(earliest);
            String content = "您有 " + sum + " 张优惠券将于 " + dateText + " 过期，请尽快使用";
            notificationService.sendUserMessage(uid, _AppMallDaoConstants.MSG_TYPE_MARKETING,
                    EXPIRY_REMIND_TITLE, content);
            pushed++;
        }
        return pushed;
    }

    /**
     * Idempotency check (D2)：当日是否已存在同 userId+msgType(MARKETING)+title 的 UserMessage。
     * addTime 为创建时间，dateTimeBetween 过滤走 xmeta 默认 prop filter 白名单（镜像积分过期预警
     * hasTodayExpiryReminder 模式）。
     */
    private boolean hasTodayExpiryReminder(String userId, IServiceContext context) {
        LocalDate today = CoreMetrics.currentDateTime().toLocalDate();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_msgType,
                _AppMallDaoConstants.MSG_TYPE_MARKETING));
        query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_title, EXPIRY_REMIND_TITLE));
        query.addFilter(FilterBeans.dateTimeBetween(LitemallUserMessage.PROP_NAME_addTime, dayStart, dayEnd));
        return userMessageBiz.findCount(query, context) > 0;
    }

    private static String formatDateOnly(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        LocalDate date = time.toLocalDate();
        return date.getYear() + " 年 " + date.getMonthValue() + " 月 " + date.getDayOfMonth() + " 日";
    }

    private int resolveIntConfig(String key, int defaultValue, IServiceContext context) {
        String raw = systemBiz.getConfig(key, context);
        if (StringHelper.isEmpty(raw)) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private List<String> parseGoodsValue(String goodsValue) {
        if (goodsValue == null || goodsValue.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return io.nop.commons.util.StringHelper.split(goodsValue, ',');
    }

    private int readUserLevel(String userId) {
        if (StringHelper.isEmpty(userId)) {
            return 0;
        }
        IOrmEntity user = ormTemplate.get(NOP_AUTH_USER_NAME, userId);
        if (user == null) {
            return 0;
        }
        Object value = user.orm_propValueByName(PROP_USER_LEVEL);
        return value instanceof Integer ? (Integer) value : 0;
    }
}
