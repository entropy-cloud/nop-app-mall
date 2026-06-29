
package app.mall.service.entity;

import app.mall.biz.ILitemallPointsFlowBiz;
import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.biz.ILitemallPointsExpireBatchBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.biz.ILitemallUserMessageBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallPointsAccount;
import app.mall.dao.entity.LitemallPointsExpireBatch;
import app.mall.dao.entity.LitemallPointsFlow;
import app.mall.dao.entity.LitemallUserMessage;
import app.mall.dao.mapper.LitemallPointsAccountMapper;
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
import io.nop.biz.BizErrors;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.dao.DaoErrors;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    // EXPIRE flow source: sourceId = expired batch id (idempotent via uk_litemall_points_flow_source).
    public static final String SOURCE_TYPE_EXPIRE = "expire";

    public static final String CONFIG_POINTS_EARN_PER_YUAN = "mall_points_earn_per_yuan";
    public static final String CONFIG_POINTS_COMMENT_REWARD = "mall_points_comment_reward";
    public static final String CONFIG_POINTS_TO_YUAN_RATIO = "mall_points_to_yuan_ratio";
    public static final String CONFIG_POINTS_DEDUCT_MAX_RATIO = "mall_points_deduct_max_ratio";
    // 积分有效期（天）：缺失时走 DEFAULT_POINTS_VALIDITY_DAYS（Decision D1）。
    public static final String CONFIG_POINTS_VALIDITY_DAYS = "mall_points_validity_days";
    // 过期预警提前天数：缺失时走 DEFAULT_POINTS_EXPIRY_REMIND_DAYS。
    public static final String CONFIG_POINTS_EXPIRY_REMIND_DAYS = "mall_points_expiry_remind_days";

    public static final int DEFAULT_POINTS_EARN_PER_YUAN = 1;
    public static final int DEFAULT_POINTS_TO_YUAN_RATIO = 100;
    public static final double DEFAULT_POINTS_DEDUCT_MAX_RATIO = 0.3d;
    // Decision D1：默认 730 天（2 年），后台 mall_points_validity_days 可覆盖。
    public static final int DEFAULT_POINTS_VALIDITY_DAYS = 730;
    // 过期预警默认提前 3 天推送（D1 of this successor plan）。
    public static final int DEFAULT_POINTS_EXPIRY_REMIND_DAYS = 3;
    // expirePoints 单轮扫描批次数上限，避免长事务；剩余下轮处理。
    static final int EXPIRE_BATCH_LIMIT = 500;
    // 过期预警单轮扫描批次数上限。
    static final int EXPIRY_REMIND_BATCH_LIMIT = 1000;

    // 站内信事件开关 key 与标题（D2 幂等查询依据：当日同 userId+msgType+title 已存在则跳过）。
    static final String EVENT_KEY_POINTS_EXPIRY_REMIND = "points-expiry-remind";
    static final String EXPIRY_REMIND_TITLE = "积分即将过期";

    @Inject
    ILitemallSystemBiz systemBiz;

    @Inject
    ILitemallPointsFlowBiz flowBiz;

    @Inject
    ILitemallPointsExpireBatchBiz expireBatchBiz;

    @Inject
    LitemallPointsAccountMapper pointsAccountMapper;

    @Inject
    MallNotificationService notificationService;

    @Inject
    ILitemallUserMessageBiz userMessageBiz;

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

    @Override
    @BizMutation
    public int expirePoints(IServiceContext context) {
        // Scan batches whose expireTime has passed and remainingPoints > 0. Per batch, decrement
        // balance via the same optimistic-lock CAS sequence as mutateBalance (flush→detach→
        // updateBalanceIfVersion), write an EXPIRE(20) flow, then zero the batch. Concurrency
        // (job vs job, job vs spend) is serialized by PointsAccount.version: the CAS loser sees
        // affected==0 and defers the batch to the next round, so balance is never double-decremented.
        // Idempotent on re-run: remainingPoints>0 selection guard + EXPIRE flow (sourceType=expire,
        // sourceId=batchId) unique key uk_litemall_points_flow_source. Decision D2 invariant
        // (balance >= SUM(remainingPoints)) guarantees newBalance stays non-negative.
        List<LitemallPointsExpireBatch> batches = expireBatchBiz.findExpiredBatches(EXPIRE_BATCH_LIMIT, context);

        int count = 0;
        for (LitemallPointsExpireBatch batch : batches) {
            int remaining = safeInt(batch.getRemainingPoints());
            if (remaining <= 0) {
                continue;
            }
            String batchUserId = batch.getUserId();
            String batchId = batch.orm_idString();
            LitemallPointsAccount account = findAccountByUserId(batchUserId, context);
            if (account == null) {
                continue;
            }
            int currentBalance = safeInt(account.getBalance());
            int currentVersion = safeInt(account.getVersion());
            int totalEarned = safeInt(account.getTotalEarned());
            int newBalance = currentBalance - remaining;
            int newTotalSpent = safeInt(account.getTotalSpent()) + remaining;
            String accountId = account.orm_idString();

            // Flush pending inserts then detach the session-managed account so the conditional
            // EQL UPDATE executes as real SQL (in-memory routing on a managed entity yields 0).
            dao().flushSession();
            dao().clearEntitySessionCache();
            int affected = pointsAccountMapper.updateBalanceIfVersion(
                    accountId, newBalance, totalEarned, newTotalSpent, currentVersion);
            if (affected == 0) {
                // Lost an optimistic-lock race (concurrent spend or another expire round).
                // Defer this batch; the winner already reconciled balance for this version.
                continue;
            }

            LitemallPointsFlow flow = flowBiz.newEntity();
            flow.setAccountId(accountId);
            flow.setUserId(batchUserId);
            flow.setChangeType(_AppMallDaoConstants.POINTS_CHANGE_TYPE_EXPIRE);
            flow.setChangeAmount(remaining);
            flow.setBalanceAfter(newBalance);
            flow.setSourceType(SOURCE_TYPE_EXPIRE);
            flow.setSourceId(batchId);
            flow.setRemark("积分过期");
            flowBiz.saveEntity(flow, null, context);

            // Reload the batch fresh (clearEntitySessionCache detached the originally selected row)
            // and zero its remaining points.
            LitemallPointsExpireBatch fresh = expireBatchBiz.get(batchId, false, context);
            if (fresh != null) {
                fresh.setRemainingPoints(0);
                expireBatchBiz.updateEntity(fresh, "expirePoints", context);
            }
            count++;
        }
        return count;
    }

    @Override
    @BizQuery
    public Map<String, Object> getMyPointsExpiryHint(IServiceContext context) {
        // Returns the soonest-expiring non-empty batch ({points, expireTime}) for the current user,
        // or null when there is no expirable batch (stock-only balance per Decision D3 → no hint).
        String userId = context.getUserId();
        if (StringHelper.isEmpty(userId)) {
            return null;
        }
        LitemallPointsExpireBatch batch = expireBatchBiz.findSoonestNonExpiredForUser(userId, context);
        if (batch == null) {
            return null;
        }
        Map<String, Object> hint = new HashMap<>();
        hint.put("points", safeInt(batch.getRemainingPoints()));
        hint.put("expireTime", batch.getExpireTime());
        return hint;
    }

    @Override
    @BizMutation
    public int sendPointsExpiryReminders(IServiceContext context) {
        // Successor of getMyPointsExpiryHint: from pull-style hint to proactive daily push.
        // Scan batches whose expireTime is within [now, now+remindDays] (D3 of this plan),
        // aggregate per userId (Σ remainingPoints + earliest expireTime), and push one SYSTEM
        // 站内信 per user. Idempotent per (userId, msgType=SYSTEM, title, addTime=today): if a
        // same-title message already exists today for the user, skip (D2).
        if (!notificationService.isEventMessageEnabled(EVENT_KEY_POINTS_EXPIRY_REMIND, context)) {
            return 0;
        }
        int remindDays = resolveIntConfig(CONFIG_POINTS_EXPIRY_REMIND_DAYS, DEFAULT_POINTS_EXPIRY_REMIND_DAYS, context);
        List<LitemallPointsExpireBatch> batches = expireBatchBiz.findBatchesExpiringWithin(
                remindDays, EXPIRY_REMIND_BATCH_LIMIT, context);

        // Aggregate per userId preserving expireTime ASC order so the earliest expiry becomes the
        // reminder date. LinkedHashMap iteration follows first-seen order.
        Map<String, int[]> sumByUser = new HashMap<>();
        Map<String, LocalDateTime> earliestByUser = new LinkedHashMap<>();
        for (LitemallPointsExpireBatch batch : batches) {
            String uid = batch.getUserId();
            if (StringHelper.isEmpty(uid)) {
                continue;
            }
            int remaining = safeInt(batch.getRemainingPoints());
            if (remaining <= 0) {
                continue;
            }
            sumByUser.computeIfAbsent(uid, k -> new int[]{0})[0] += remaining;
            earliestByUser.putIfAbsent(uid, batch.getExpireTime());
        }

        int pushed = 0;
        for (Map.Entry<String, LocalDateTime> entry : earliestByUser.entrySet()) {
            String uid = entry.getKey();
            LocalDateTime earliest = entry.getValue();
            int sum = sumByUser.get(uid)[0];
            if (sum <= 0) {
                continue;
            }
            if (hasTodayExpiryReminder(uid, context)) {
                continue;
            }
            String dateText = formatDateOnly(earliest);
            String content = "您有 " + sum + " 积分将于 " + dateText + " 过期，请尽快使用";
            notificationService.sendUserMessage(uid, _AppMallDaoConstants.MSG_TYPE_SYSTEM,
                    EXPIRY_REMIND_TITLE, content);
            pushed++;
        }
        return pushed;
    }

    /**
     * Idempotency check (D2): has the user already received an SYSTEM 站内信 with the reminder
     * title today? addTime domain is createTime; the dateTimeBetween filter on addTime is in the
     * xmeta default prop filter whitelist.
     */
    private boolean hasTodayExpiryReminder(String userId, IServiceContext context) {
        LocalDate today = CoreMetrics.currentDateTime().toLocalDate();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_msgType, _AppMallDaoConstants.MSG_TYPE_SYSTEM));
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

        // Synchronize the expire-batch ledger in the same tx (Decision D2 invariant:
        // balance is the truth source, batches track the expirable portion earned after
        // this feature went live). earn/positive-adjust create an expirable batch; spend
        // and negative-adjust consume batches FIFO (earliest-expire first). Stock balance
        // earned before this feature has no batch and is consumed last (Decision D3).
        if (isEarn) {
            createExpireBatch(accountId, userId, amount, sourceType, sourceId, context);
        } else {
            consumeBatchesFifo(userId, amount, context);
        }

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

    private void createExpireBatch(String accountId, String userId, int amount,
                                   String sourceType, String sourceId, IServiceContext context) {
        // One batch per earn; (sourceType, sourceId) mirrors the earn flow, so the batch unique key
        // uk_points_expire_batch_source provides the same dedupe guarantee as the flow unique key.
        int validityDays = resolveValidityDays(context);
        LocalDateTime expireTime = CoreMetrics.currentDateTime().plusDays(validityDays);
        LitemallPointsExpireBatch batch = expireBatchBiz.newEntity();
        batch.setAccountId(accountId);
        batch.setUserId(userId);
        batch.setTotalPoints(amount);
        batch.setRemainingPoints(amount);
        batch.setExpireTime(expireTime);
        batch.setSourceType(sourceType);
        batch.setSourceId(sourceId);
        batch.setVersion(0);
        expireBatchBiz.saveEntity(batch, "earn-batch", context);
    }

    private void consumeBatchesFifo(String userId, int amount, IServiceContext context) {
        // FIFO by expireTime ASC. Only the expirable portion (sum of remaining) is consumed from
        // batches; any remainder came from stock balance (Decision D3) and is left untouched here
        // (balance was already decremented uniformly by the caller's CAS).
        List<LitemallPointsExpireBatch> batches = expireBatchBiz.findExpirableBatchesForUser(userId, context);
        int toCover = amount;
        for (LitemallPointsExpireBatch batch : batches) {
            if (toCover <= 0) {
                break;
            }
            int remaining = safeInt(batch.getRemainingPoints());
            int take = Math.min(remaining, toCover);
            batch.setRemainingPoints(remaining - take);
            expireBatchBiz.updateEntity(batch, "spend-fifo", context);
            toCover -= take;
        }
    }

    private int resolveValidityDays(IServiceContext context) {
        return resolveIntConfig(CONFIG_POINTS_VALIDITY_DAYS, DEFAULT_POINTS_VALIDITY_DAYS, context);
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
