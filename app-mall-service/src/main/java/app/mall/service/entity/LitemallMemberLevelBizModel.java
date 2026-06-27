
package app.mall.service.entity;

import app.mall.biz.ILitemallMemberLevelBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallMemberLevel;
import app.mall.dao.entity.LitemallOrder;
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
import io.nop.dao.api.IEntityDao;
import io.nop.orm.IOrmEntity;
import io.nop.orm.IOrmTemplate;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static app.mall.service.AppMallErrors.ERR_MEMBER_LEVEL_INVALID;
import static app.mall.service.AppMallErrors.ERR_MEMBER_LEVEL_NOT_CONFIGURED;
import static app.mall.service.AppMallErrors.ERR_MEMBER_LEVEL_USER_NOT_FOUND;

@BizModel("LitemallMemberLevel")
public class LitemallMemberLevelBizModel extends CrudBizModel<LitemallMemberLevel> implements ILitemallMemberLevelBiz {
    public static final String CONFIG_MEMBER_EVAL_PERIOD_DAYS = "mall_member_eval_period_days";
    public static final int DEFAULT_EVAL_PERIOD_DAYS = 365;

    // userLevel is a Delta extension column on nop_auth_user (see model/nop-auth-delta.orm.xml).
    // The app's INopAuthUserBiz lives in app-mall-delta, which is test-scoped in app-mall-service,
    // so I*Biz cannot satisfy cross-entity access here. Per AGENTS.md we use the IDaoProvider /
    // IOrmTemplate fallback and access userLevel via IOrmEntity.orm_propValueByName.
    public static final String PROP_USER_LEVEL = "userLevel";
    public static final String NOP_AUTH_USER_NAME = NopAuthUser.class.getName();
    // mall/user-level dict: 0=普通, 1=VIP, 2=高级VIP. Members are levels 1 and 2.
    public static final List<Integer> MEMBER_LEVELS = List.of(1, 2);
    // All valid userLevel values (including ordinary 0) — used to validate manual setUserLevel.
    public static final List<Integer> MEMBER_AND_ORDINARY_LEVELS = List.of(0, 1, 2);

    @Inject
    ILitemallOrderBiz orderBiz;

    @Inject
    ILitemallSystemBiz systemBiz;

    @Inject
    IOrmTemplate ormTemplate;

    @Inject
    MallLogManager logManager;

    public LitemallMemberLevelBizModel() {
        setEntityName(LitemallMemberLevel.class.getName());
    }

    @Override
    @BizQuery
    public List<LitemallMemberLevel> findLevelRules(IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addOrderField(LitemallMemberLevel.PROP_NAME_level, false);
        return findList(query, null, context);
    }

    @Override
    @BizQuery
    public Map<String, Object> getMyLevelProgress(IServiceContext context) {
        String userId = context.getUserId();
        IOrmEntity user = loadUser(userId);

        List<LitemallMemberLevel> sorted = sortedByLevel(findLevelRules(context));

        int currentLevel = readUserLevel(user);
        BigDecimal totalSpending = computeSpending(userId, null, context);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentLevel", currentLevel);
        result.put("currentLevelName", findLevelName(sorted, currentLevel));
        result.put("totalSpending", totalSpending);

        LitemallMemberLevel next = findNextLevel(sorted, currentLevel);
        if (next != null && next.getUpgradeThreshold() != null) {
            result.put("nextLevel", next.getLevel());
            result.put("nextLevelName", next.getName());
            result.put("nextLevelThreshold", next.getUpgradeThreshold());
            BigDecimal remaining = next.getUpgradeThreshold().subtract(totalSpending);
            result.put("remaining", remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO);
        } else {
            result.put("nextLevel", null);
            result.put("nextLevelName", null);
        }
        return result;
    }

    @Override
    @BizMutation
    public int evaluateMyLevel(IServiceContext context) {
        return evaluateUserLevel(context.getUserId(), context);
    }

    @Override
    @BizMutation
    public int evaluateUserLevel(@Name("userId") String userId, IServiceContext context) {
        if (StringHelper.isEmpty(userId)) {
            throw new NopException(ERR_MEMBER_LEVEL_USER_NOT_FOUND).param("userId", userId);
        }
        IOrmEntity user = loadUser(userId);
        if (user == null) {
            throw new NopException(ERR_MEMBER_LEVEL_USER_NOT_FOUND).param("userId", userId);
        }

        List<LitemallMemberLevel> sorted = sortedByLevel(findLevelRules(context));
        if (sorted.isEmpty()) {
            // No level rules configured: keep user at ordinary level.
            return ensureUserLevel(user, 0);
        }

        BigDecimal totalSpending = computeSpending(userId, null, context);
        int target = computeTargetLevel(sorted, totalSpending);
        return ensureUserLevel(user, target);
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public int setUserLevel(@Name("userId") String userId,
                            @Name("targetLevel") int targetLevel,
                            @Optional @Name("remark") String remark,
                            IServiceContext context) {
        // mall/user-level dict values: 0=普通, 1=VIP, 2=高级VIP. Admin may set any of the three;
        // other values are rejected to keep userLevel consistent with the dictionary.
        if (!MEMBER_AND_ORDINARY_LEVELS.contains(targetLevel)) {
            throw new NopException(ERR_MEMBER_LEVEL_INVALID).param("targetLevel", targetLevel);
        }
        if (StringHelper.isEmpty(userId)) {
            throw new NopException(ERR_MEMBER_LEVEL_USER_NOT_FOUND).param("userId", userId);
        }
        IOrmEntity user = loadUser(userId);
        if (user == null) {
            throw new NopException(ERR_MEMBER_LEVEL_USER_NOT_FOUND).param("userId", userId);
        }
        int previous = readUserLevel(user);
        ensureUserLevel(user, targetLevel);
        // Manual level change is an auditable admin operation (not a points flow): record it in the
        // admin operation log (LitemallLog) with before/after so the reason is traceable.
        logManager.logGeneralSucceed("setUserLevel",
                "userId=" + userId + ", " + previous + "->" + targetLevel
                        + (StringHelper.isEmpty(remark) ? "" : ", remark=" + remark));
        return targetLevel;
    }

    @Override
    @BizMutation
    public int downgradeExpiredLevels(@Optional @Name("periodDays") Integer periodDays, IServiceContext context) {
        int period = periodDays != null && periodDays > 0 ? periodDays : resolveEvalPeriodDays(context);
        LocalDateTime since = LocalDateTime.now().minusDays(period);

        List<LitemallMemberLevel> sorted = sortedByLevel(findLevelRules(context));
        if (sorted.isEmpty()) {
            throw new NopException(ERR_MEMBER_LEVEL_NOT_CONFIGURED);
        }
        Map<Integer, LitemallMemberLevel> ruleByLevel = new HashMap<>();
        for (LitemallMemberLevel rule : sorted) {
            if (rule.getLevel() != null) {
                ruleByLevel.put(rule.getLevel(), rule);
            }
        }

        // Only members (userLevel >= 1) are candidates for downgrade. NopAuthUser's app Biz
        // (INopAuthUserBiz) lives in app-mall-delta which is test-scoped here, so per AGENTS.md we
        // use the CrudBizModel.daoProvider() fallback to access the platform entity directly.
        // The xmeta only allows eq/in on userLevel, so we match the member levels explicitly.
        QueryBean userQuery = new QueryBean();
        userQuery.addFilter(FilterBeans.in(PROP_USER_LEVEL, MEMBER_LEVELS));
        IEntityDao<NopAuthUser> userDao = daoProvider().daoFor(NopAuthUser.class);
        List<NopAuthUser> members = userDao.findAllByQuery(userQuery);
        int downgraded = 0;
        for (NopAuthUser user : members) {
            Integer level = readUserLevel(user);
            if (level == null || level <= 0) {
                continue;
            }
            LitemallMemberLevel rule = ruleByLevel.get(level);
            if (rule == null || rule.getDowngradeThreshold() == null) {
                continue;
            }
            BigDecimal periodSpending = computeSpending(user.orm_idString(), since, context);
            if (periodSpending.compareTo(rule.getDowngradeThreshold()) < 0) {
                int newLevel = Math.max(level - 1, 0);
                ensureUserLevel(user, newLevel);
                downgraded++;
            }
        }
        return downgraded;
    }

    private IOrmEntity loadUser(String userId) {
        if (StringHelper.isEmpty(userId)) {
            return null;
        }
        return ormTemplate.get(NOP_AUTH_USER_NAME, userId);
    }

    private int ensureUserLevel(IOrmEntity user, int target) {
        Integer current = readUserLevel(user);
        if (current == null || current != target) {
            // user is MANAGED in the current session (loaded via ormTemplate.get / findAllByQuery),
            // so a dirty property change auto-flushes on transaction commit without an explicit save.
            user.orm_propValueByName(PROP_USER_LEVEL, target);
            ormTemplate.flushSession();
        }
        return target;
    }

    private int readUserLevel(IOrmEntity user) {
        if (user == null) {
            return 0;
        }
        Object value = user.orm_propValueByName(PROP_USER_LEVEL);
        return value instanceof Integer ? (Integer) value : 0;
    }

    private BigDecimal computeSpending(String userId, LocalDateTime since, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrder.PROP_NAME_userId, userId));
        List<Integer> paidStatuses = new ArrayList<>();
        paidStatuses.add(_AppMallDaoConstants.ORDER_STATUS_PAY);
        paidStatuses.add(_AppMallDaoConstants.ORDER_STATUS_SHIP);
        paidStatuses.add(_AppMallDaoConstants.ORDER_STATUS_CONFIRM);
        paidStatuses.add(_AppMallDaoConstants.ORDER_STATUS_AUTO_CONFIRM);
        query.addFilter(FilterBeans.in(LitemallOrder.PROP_NAME_orderStatus, paidStatuses));
        // The xmeta only allows eq/in/dateBetween/dateTimeBetween on time fields, so the period
        // cutoff (addTime >= since) is applied in Java after loading the user's paid orders.
        List<LitemallOrder> orders = orderBiz.findList(query, null, context);
        BigDecimal total = BigDecimal.ZERO;
        for (LitemallOrder order : orders) {
            if (since != null && order.getAddTime() != null && order.getAddTime().isBefore(since)) {
                continue;
            }
            BigDecimal price = order.getActualPrice();
            if (price != null) {
                total = total.add(price);
            }
        }
        return total;
    }

    private int computeTargetLevel(List<LitemallMemberLevel> sortedRules, BigDecimal spending) {
        int target = 0;
        for (LitemallMemberLevel rule : sortedRules) {
            if (rule.getLevel() == null || rule.getUpgradeThreshold() == null) {
                continue;
            }
            if (spending.compareTo(rule.getUpgradeThreshold()) >= 0 && rule.getLevel() > target) {
                target = rule.getLevel();
            }
        }
        return target;
    }

    private LitemallMemberLevel findNextLevel(List<LitemallMemberLevel> sortedRules, int currentLevel) {
        for (LitemallMemberLevel rule : sortedRules) {
            if (rule.getLevel() != null && rule.getLevel() > currentLevel) {
                return rule;
            }
        }
        return null;
    }

    private String findLevelName(List<LitemallMemberLevel> sortedRules, int level) {
        for (LitemallMemberLevel rule : sortedRules) {
            if (rule.getLevel() != null && rule.getLevel() == level) {
                return rule.getName();
            }
        }
        return null;
    }

    private List<LitemallMemberLevel> sortedByLevel(List<LitemallMemberLevel> rules) {
        List<LitemallMemberLevel> sorted = new ArrayList<>(rules);
        sorted.sort(Comparator.comparing(LitemallMemberLevelBizModel::safeLevel));
        return sorted;
    }

    private static int safeLevel(LitemallMemberLevel rule) {
        return rule.getLevel() == null ? Integer.MAX_VALUE : rule.getLevel();
    }

    private int resolveEvalPeriodDays(IServiceContext context) {
        String raw = systemBiz.getConfig(CONFIG_MEMBER_EVAL_PERIOD_DAYS, context);
        if (StringHelper.isEmpty(raw)) {
            return DEFAULT_EVAL_PERIOD_DAYS;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : DEFAULT_EVAL_PERIOD_DAYS;
        } catch (NumberFormatException e) {
            return DEFAULT_EVAL_PERIOD_DAYS;
        }
    }
}
