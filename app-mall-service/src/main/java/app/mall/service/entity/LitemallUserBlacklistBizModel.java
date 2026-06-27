
package app.mall.service.entity;

import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallFeedbackBiz;
import app.mall.biz.ILitemallFootprintBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.biz.ILitemallUserBlacklistBiz;
import app.mall.biz.ILitemallUserTagBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.dto.UserWorkbenchSummaryBean;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallPointsAccount;
import app.mall.dao.entity.LitemallUserBlacklist;
import app.mall.dao.entity.LitemallUserTag;
import app.mall.dao.manager.MallLogManager;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.orm.IOrmEntity;
import io.nop.orm.IOrmTemplate;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static app.mall.service.AppMallErrors.ERR_USER_NOT_BANNED;
import static app.mall.service.AppMallErrors.ERR_USER_NOT_FOUND;

@BizModel("LitemallUserBlacklist")
public class LitemallUserBlacklistBizModel extends CrudBizModel<LitemallUserBlacklist> implements ILitemallUserBlacklistBiz {

    // Platform user status: 0=disabled (banned), 1=enabled (normal). The workbench ban/unban levers
    // NopAuthUser.status directly. app-mall-delta's INopAuthUserBiz is test-scoped in app-mall-service,
    // so per AGENTS.md we use the IOrmTemplate fallback (same pattern as LitemallMemberLevelBizModel).
    // This direct write bypasses the platform status-change hook; that tradeoff is accepted because no
    // runtime INopAuthUserBiz entry is available from this module.
    private static final int USER_STATUS_DISABLED = 0;
    private static final int USER_STATUS_ENABLED = 1;
    private static final String NOP_AUTH_USER_NAME = NopAuthUser.class.getName();

    @Inject
    IOrmTemplate ormTemplate;

    @Inject
    ILitemallOrderBiz orderBiz;

    @Inject
    ILitemallPointsAccountBiz pointsAccountBiz;

    @Inject
    ILitemallCouponUserBiz couponUserBiz;

    @Inject
    ILitemallFootprintBiz footprintBiz;

    @Inject
    ILitemallFeedbackBiz feedbackBiz;

    @Inject
    ILitemallUserTagBiz userTagBiz;

    @Inject
    MallLogManager logManager;

    public LitemallUserBlacklistBizModel() {
        setEntityName(LitemallUserBlacklist.class.getName());
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallUserBlacklist banUser(@Name("userId") String userId,
                                         @Optional @Name("reason") String reason,
                                         IServiceContext context) {
        requireExistingUser(userId);
        setUserStatus(userId, USER_STATUS_DISABLED);

        // Keep blacklist record consistent with status: upsert so re-ban refreshes the reason/operator
        // instead of creating a duplicate (unique key on userId enforces at-most-one row anyway).
        LitemallUserBlacklist record = findBlacklistByUser(userId, context);
        if (record == null) {
            record = newEntity();
            record.setUserId(userId);
        }
        record.setReason(reason);
        record.setOperatorId(context.getUserId());
        saveEntity(record, null, context);

        logManager.logGeneralSucceed("banUser",
                "userId=" + userId + (StringHelper.isEmpty(reason) ? "" : ", reason=" + reason));
        return record;
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public void unbanUser(@Name("userId") String userId,
                          IServiceContext context) {
        LitemallUserBlacklist record = findBlacklistByUser(userId, context);
        if (record == null) {
            throw new NopException(ERR_USER_NOT_BANNED).param("userId", userId);
        }
        setUserStatus(userId, USER_STATUS_ENABLED);
        deleteEntity(record, null, context);
        logManager.logGeneralSucceed("unbanUser", "userId=" + userId);
    }

    @Override
    @BizQuery
    @Auth(roles = "admin")
    public UserWorkbenchSummaryBean getUserWorkbenchSummary(@Name("userId") String userId,
                                                            IServiceContext context) {
        IOrmEntity user = requireExistingUser(userId);

        UserWorkbenchSummaryBean summary = new UserWorkbenchSummaryBean();
        summary.setUserId(userId);
        summary.setUserName(readStringProp(user, "userName"));
        summary.setNickName(readStringProp(user, "nickName"));
        summary.setMobile(readStringProp(user, "phone"));
        summary.setStatus(readIntProp(user, "status"));
        summary.setUserLevel(readIntProp(user, "userLevel"));

        // Order aggregation: total orders of this user + cumulative spending over paid orders.
        QueryBean orderQuery = new QueryBean();
        orderQuery.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_userId, userId));
        List<LitemallOrder> orders = orderBiz.findList(orderQuery, null, context);
        summary.setOrderCount(orders == null ? 0 : orders.size());
        summary.setTotalSpending(sumPaidSpending(orders));

        // Points balance (account may not exist yet for a fresh user).
        QueryBean pointsQuery = new QueryBean();
        pointsQuery.addFilter(FilterBeans.eq(LitemallPointsAccount.PROP_NAME_userId, userId));
        LitemallPointsAccount account = pointsAccountBiz.findFirst(pointsQuery, null, context);
        summary.setPointsBalance(account != null && account.getBalance() != null ? account.getBalance() : 0);

        // Coupons grouped by status: 0=unused, 1=used, 2=expired.
        QueryBean couponQuery = new QueryBean();
        couponQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_userId, userId));
        List<LitemallCouponUser> coupons = couponUserBiz.findList(couponQuery, null, context);
        int unused = 0, used = 0, expired = 0;
        if (coupons != null) {
            for (LitemallCouponUser cu : coupons) {
                Integer st = cu.getStatus();
                if (st == null || st == 0) {
                    unused++;
                } else if (st == 1) {
                    used++;
                } else {
                    expired++;
                }
            }
        }
        summary.setCouponUnusedCount(unused);
        summary.setCouponUsedCount(used);
        summary.setCouponExpiredCount(expired);

        QueryBean footprintQuery = new QueryBean();
        footprintQuery.addFilter(FilterBeans.eq("userId", userId));
        summary.setFootprintCount((int) footprintBiz.findCount(footprintQuery, context));

        QueryBean feedbackQuery = new QueryBean();
        feedbackQuery.addFilter(FilterBeans.eq("userId", userId));
        summary.setFeedbackCount((int) feedbackBiz.findCount(feedbackQuery, context));

        // Tags: list the user's tag codes for display on the workbench.
        QueryBean tagQuery = new QueryBean();
        tagQuery.addFilter(FilterBeans.eq(LitemallUserTag.PROP_NAME_userId, userId));
        List<LitemallUserTag> tags = userTagBiz.findList(tagQuery, null, context);
        List<String> tagCodes = new ArrayList<>();
        if (tags != null) {
            for (LitemallUserTag t : tags) {
                tagCodes.add(t.getTag());
            }
        }
        summary.setTags(tagCodes);

        // Blacklist state: banned flag + reason come from the blacklist record (single source of truth
        // for the ban audit trail), kept in sync with status by banUser/unbanUser.
        LitemallUserBlacklist blacklist = findBlacklistByUser(userId, context);
        summary.setBanned(blacklist != null);
        summary.setBanReason(blacklist != null ? blacklist.getReason() : null);

        return summary;
    }

    private IOrmEntity requireExistingUser(String userId) {
        if (StringHelper.isEmpty(userId)) {
            throw new NopException(ERR_USER_NOT_FOUND).param("userId", userId);
        }
        IOrmEntity user = ormTemplate.get(NOP_AUTH_USER_NAME, userId);
        if (user == null) {
            throw new NopException(ERR_USER_NOT_FOUND).param("userId", userId);
        }
        return user;
    }

    private void setUserStatus(String userId, int status) {
        IOrmEntity user = ormTemplate.get(NOP_AUTH_USER_NAME, userId);
        if (user == null) {
            throw new NopException(ERR_USER_NOT_FOUND).param("userId", userId);
        }
        user.orm_propValueByName("status", status);
        ormTemplate.flushSession();
    }

    private LitemallUserBlacklist findBlacklistByUser(String userId, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallUserBlacklist.PROP_NAME_userId, userId));
        return findFirst(query, null, context);
    }

    private BigDecimal sumPaidSpending(List<LitemallOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<Integer> paidStatuses = new ArrayList<>();
        paidStatuses.add(_AppMallDaoConstants.ORDER_STATUS_PAY);
        paidStatuses.add(_AppMallDaoConstants.ORDER_STATUS_SHIP);
        paidStatuses.add(_AppMallDaoConstants.ORDER_STATUS_CONFIRM);
        paidStatuses.add(_AppMallDaoConstants.ORDER_STATUS_AUTO_CONFIRM);
        BigDecimal total = BigDecimal.ZERO;
        for (LitemallOrder order : orders) {
            if (order.getOrderStatus() != null && paidStatuses.contains(order.getOrderStatus())) {
                BigDecimal price = order.getActualPrice();
                if (price != null) {
                    total = total.add(price);
                }
            }
        }
        return total;
    }

    private static String readStringProp(IOrmEntity user, String propName) {
        Object value = user.orm_propValueByName(propName);
        return value != null ? value.toString() : null;
    }

    private static Integer readIntProp(IOrmEntity user, String propName) {
        Object value = user.orm_propValueByName(propName);
        return value instanceof Integer ? (Integer) value : null;
    }
}
