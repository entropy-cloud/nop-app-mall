
package app.mall.service.entity;

import app.mall.biz.ILitemallPointsFlowBiz;
import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallPointsAccount;
import app.mall.dao.entity.LitemallPointsFlow;
import app.mall.dao.mapper.LitemallPointsAccountMapper;
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
import io.nop.biz.BizErrors;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.dao.DaoErrors;
import jakarta.inject.Inject;

import static app.mall.service.AppMallErrors.ERR_POINTS_ACCOUNT_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_POINTS_DUPLICATE_EARN;
import static app.mall.service.AppMallErrors.ERR_POINTS_EARN_FAILED;
import static app.mall.service.AppMallErrors.ERR_POINTS_INSUFFICIENT;

@BizModel("LitemallPointsAccount")
public class LitemallPointsAccountBizModel extends CrudBizModel<LitemallPointsAccount> implements ILitemallPointsAccountBiz {
    // changeType strictly maps the 3 dict values; fine-grained origin falls into sourceType.
    // See docs/design/wallet-and-assets.md (changeType × sourceType taxonomy).
    public static final String SOURCE_TYPE_ORDER_CONFIRM_EARN = "order-confirm-earn";
    public static final String SOURCE_TYPE_REFUND_RETURN = "refund-return";
    public static final String SOURCE_TYPE_ORDER_DEDUCT = "order-deduct";
    public static final String SOURCE_TYPE_ADMIN_ADJUST = "admin-adjust";
    public static final String SOURCE_TYPE_CHECK_IN = "check-in";
    public static final String SOURCE_TYPE_COMMENT_REWARD = "comment-reward";

    public static final String CONFIG_POINTS_EARN_PER_YUAN = "mall_points_earn_per_yuan";
    public static final String CONFIG_POINTS_COMMENT_REWARD = "mall_points_comment_reward";
    public static final String CONFIG_POINTS_TO_YUAN_RATIO = "mall_points_to_yuan_ratio";
    public static final String CONFIG_POINTS_DEDUCT_MAX_RATIO = "mall_points_deduct_max_ratio";

    public static final int DEFAULT_POINTS_EARN_PER_YUAN = 1;
    public static final int DEFAULT_POINTS_TO_YUAN_RATIO = 100;
    public static final double DEFAULT_POINTS_DEDUCT_MAX_RATIO = 0.3d;

    @Inject
    ILitemallSystemBiz systemBiz;

    @Inject
    ILitemallPointsFlowBiz flowBiz;

    @Inject
    LitemallPointsAccountMapper pointsAccountMapper;

    public LitemallPointsAccountBizModel() {
        setEntityName(LitemallPointsAccount.class.getName());
    }

    @Override
    @BizQuery
    public int getMyPoints(IServiceContext context) {
        String userId = context.getUserId();
        if (StringHelper.isEmpty(userId)) {
            return 0;
        }
        LitemallPointsAccount account = findAccountByUserId(userId, context);
        return account == null ? 0 : safeInt(account.getBalance());
    }

    @Override
    @BizMutation
    public LitemallPointsAccount earnPoints(@Name("userId") String userId,
                                            @Name("amount") int amount,
                                            @Name("changeType") int changeType,
                                            @Name("sourceType") String sourceType,
                                            @Optional @Name("sourceId") String sourceId,
                                            @Optional @Name("remark") String remark,
                                            IServiceContext context) {
        if (amount <= 0) {
            throw new NopException(ERR_POINTS_EARN_FAILED).param("amount", amount);
        }
        // Idempotency: an earn with the same sourceType+sourceId is rejected as duplicate.
        // App-level fast-path pre-check; the DB unique key uk_litemall_points_flow_source
        // and CrudBizModel.checkUniqueForSaveEntity serve as concurrent-race fallbacks.
        if (!StringHelper.isEmpty(sourceType) && !StringHelper.isEmpty(sourceId)) {
            if (countFlowBySource(sourceType, sourceId, context) > 0) {
                throw new NopException(ERR_POINTS_DUPLICATE_EARN)
                        .param("sourceType", sourceType)
                        .param("sourceId", sourceId);
            }
        }
        try {
            return mutateBalance(userId, amount, changeType, sourceType, sourceId, remark, context, true);
        } catch (NopException e) {
            // Concurrent-race fallback: the pre-check passed but another tx committed the same
            // source first. Translate the pipeline/DB unique-key conflict to the business error
            // code so callers (e.g. CommentBizModel idempotency catch) see a consistent code.
            if (isPointsFlowUniqueKeyConflict(e)) {
                throw new NopException(ERR_POINTS_DUPLICATE_EARN, e)
                        .param("sourceType", sourceType)
                        .param("sourceId", sourceId);
            }
            throw e;
        }
    }

    @Override
    @BizMutation
    public LitemallPointsAccount spendPoints(@Name("userId") String userId,
                                             @Name("amount") int amount,
                                             @Name("changeType") int changeType,
                                             @Name("sourceType") String sourceType,
                                             @Optional @Name("sourceId") String sourceId,
                                             @Optional @Name("remark") String remark,
                                             IServiceContext context) {
        if (amount <= 0) {
            throw new NopException(ERR_POINTS_INSUFFICIENT).param("amount", amount);
        }
        return mutateBalance(userId, amount, changeType, sourceType, sourceId, remark, context, false);
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallPointsAccount adjustPoints(@Name("userId") String userId,
                                              @Name("amount") int amount,
                                              @Optional @Name("remark") String remark,
                                              IServiceContext context) {
        if (amount == 0) {
            return findOrCreateAccount(userId, context);
        }
        // Admin adjust records each call with a unique sourceId (no idempotency dedupe).
        boolean isEarn = amount > 0;
        int changeType = isEarn ? _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN : _AppMallDaoConstants.POINTS_CHANGE_TYPE_SPEND;
        String sourceId = "adjust-" + CoreMetrics.currentTimeMillis();
        return mutateBalance(userId, Math.abs(amount), changeType, SOURCE_TYPE_ADMIN_ADJUST, sourceId, remark, context, isEarn);
    }

    /**
     * Resolve the points-per-yuan earn ratio from system config, with default fallback.
     * Exposed as package API so LitemallOrderBizModel.confirm can compute shopping rewards.
     */
    @Override
    public int resolveEarnPerYuan(IServiceContext context) {
        return resolveIntConfig(CONFIG_POINTS_EARN_PER_YUAN, DEFAULT_POINTS_EARN_PER_YUAN, context);
    }

    @Override
    public int resolveToYuanRatio(IServiceContext context) {
        return resolveIntConfig(CONFIG_POINTS_TO_YUAN_RATIO, DEFAULT_POINTS_TO_YUAN_RATIO, context);
    }

    @Override
    public double resolveDeductMaxRatio(IServiceContext context) {
        String raw = systemBiz.getConfig(CONFIG_POINTS_DEDUCT_MAX_RATIO, context);
        if (StringHelper.isEmpty(raw)) {
            return DEFAULT_POINTS_DEDUCT_MAX_RATIO;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_POINTS_DEDUCT_MAX_RATIO;
        }
    }

    private LitemallPointsAccount mutateBalance(String userId, int amount, int changeType,
                                                String sourceType, String sourceId, String remark,
                                                IServiceContext context, boolean isEarn) {
        LitemallPointsAccount account = findOrCreateAccount(userId, context);
        int currentBalance = safeInt(account.getBalance());
        int newBalance = isEarn ? currentBalance + amount : currentBalance - amount;
        if (!isEarn && newBalance < 0) {
            throw new NopException(ERR_POINTS_INSUFFICIENT)
                    .param("userId", userId)
                    .param("balance", currentBalance)
                    .param("requested", amount);
        }
        int newTotalEarned = isEarn ? safeInt(account.getTotalEarned()) + amount : safeInt(account.getTotalEarned());
        int newTotalSpent = !isEarn ? safeInt(account.getTotalSpent()) + amount : safeInt(account.getTotalSpent());
        int currentVersion = safeInt(account.getVersion());
        String accountId = account.orm_idString();

        // Flush the pending insert (first-earn auto-create) so the row exists in DB, then detach
        // the PointsAccount class from the session. A conditional EQL UPDATE on a session-managed
        // entity is routed in-memory and reports 0 affected rows (see cancelExpiredOrders for the
        // same pattern). Detaching forces the mapper UPDATE to execute as real SQL.
        dao().flushSession();
        dao().clearEntitySessionCache();

        int affected = pointsAccountMapper.updateBalanceIfVersion(
                accountId, newBalance, newTotalEarned, newTotalSpent, currentVersion);
        if (affected == 0) {
            // Lost an optimistic-lock race or the row was concurrently modified.
            throw new NopException(ERR_POINTS_EARN_FAILED)
                    .param("userId", userId)
                    .param("reason", "optimistic version conflict");
        }

        // Record the flow with balanceAfter snapshot so account and flow tables reconcile.
        LitemallPointsFlow flow = flowBiz.newEntity();
        flow.setAccountId(accountId);
        flow.setUserId(userId);
        flow.setChangeType(changeType);
        flow.setChangeAmount(amount);
        flow.setBalanceAfter(newBalance);
        flow.setSourceType(sourceType);
        flow.setSourceId(sourceId);
        flow.setRemark(remark);
        flowBiz.saveEntity(flow, null, context);

        // Re-read fresh so the returned managed entity reflects the committed balance.
        LitemallPointsAccount updated = findAccountByUserId(userId, context);
        return updated != null ? updated : account;
    }

    private LitemallPointsAccount findOrCreateAccount(String userId, IServiceContext context) {
        if (StringHelper.isEmpty(userId)) {
            throw new NopException(ERR_POINTS_ACCOUNT_NOT_FOUND).param("userId", userId);
        }
        LitemallPointsAccount account = findAccountByUserId(userId, context);
        if (account != null) {
            return account;
        }
        account = newEntity();
        account.setUserId(userId);
        account.setBalance(0);
        account.setTotalEarned(0);
        account.setTotalSpent(0);
        account.setVersion(0);
        saveEntity(account, "earnPoints:autoCreate", context);
        return account;
    }

    private LitemallPointsAccount findAccountByUserId(String userId, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallPointsAccount.PROP_NAME_userId, userId));
        return findFirst(query, null, context);
    }

    private long countFlowBySource(String sourceType, String sourceId, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType, sourceType));
        query.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceId, sourceId));
        return flowBiz.findCount(query, context);
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

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static boolean isPointsFlowUniqueKeyConflict(NopException e) {
        String code = e.getErrorCode();
        if (code == null)
            return false;
        return code.equals(BizErrors.ERR_BIZ_ENTITY_WITH_SAME_KEY_ALREADY_EXISTS.getErrorCode())
                || code.equals(DaoErrors.ERR_SQL_DATA_INTEGRITY_VIOLATION.getErrorCode());
    }
}
