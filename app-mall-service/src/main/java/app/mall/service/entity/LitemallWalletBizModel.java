
package app.mall.service.entity;

import app.mall.biz.ILitemallWalletBiz;
import app.mall.biz.ILitemallWalletFlowBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallWallet;
import app.mall.dao.entity.LitemallWalletFlow;
import app.mall.dao.mapper.LitemallWalletMapper;
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
import jakarta.inject.Inject;

import java.math.BigDecimal;

import static app.mall.service.AppMallErrors.ERR_WALLET_INSUFFICIENT;
import static app.mall.service.AppMallErrors.ERR_WALLET_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_WALLET_VERSION_CONFLICT;

@BizModel("LitemallWallet")
public class LitemallWalletBizModel extends CrudBizModel<LitemallWallet> implements ILitemallWalletBiz {
    // changeType strictly maps the 4 dict values (mall/wallet-change-type); fine-grained origin
    // falls into sourceType. See docs/design/wallet-and-assets.md (changeType × sourceType taxonomy).
    public static final String SOURCE_TYPE_RECHARGE = "recharge";
    public static final String SOURCE_TYPE_PAY = "pay";
    public static final String SOURCE_TYPE_ADMIN_ADJUST = "admin-adjust";
    // Recharge-channel refund async reconciliation (P29 deferred successor). Named recharge-refund
    // rather than the owner-doc-reserved bare "refund" so the source remains unambiguously attributable
    // to a recharge reversal (symmetric with SOURCE_TYPE_RECHARGE=recharge) and stays distinguishable
    // from any future "order refund returns to wallet" scenario. Owner doc updated to record this choice.
    public static final String SOURCE_TYPE_RECHARGE_REFUND = "recharge-refund";

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Inject
    ILitemallWalletFlowBiz flowBiz;

    @Inject
    LitemallWalletMapper walletMapper;

    public LitemallWalletBizModel(){
        setEntityName(LitemallWallet.class.getName());
    }

    @Override
    @BizQuery
    public LitemallWallet getMyWallet(IServiceContext context) {
        String userId = context.getUserId();
        if (StringHelper.isEmpty(userId)) {
            return null;
        }
        LitemallWallet wallet = findWalletByUserId(userId, context);
        if (wallet != null) {
            return wallet;
        }
        // Lazy-create policy: a wallet that has never been credited is not persisted. Return an
        // in-memory zero-balance shell so the frontend can render a consistent view. Reconciled in
        // docs/design/wallet-and-assets.md (lazy-create vs register-time pre-create).
        LitemallWallet shell = newEntity();
        shell.setUserId(userId);
        shell.setBalance(ZERO);
        shell.setTotalRecharge(ZERO);
        shell.setTotalSpent(ZERO);
        shell.setVersion(0);
        return shell;
    }

    @Override
    @BizAction
    public LitemallWallet creditBalance(@Name("userId") String userId,
                                        @Name("amount") BigDecimal amount,
                                        @Name("changeType") int changeType,
                                        @Name("sourceType") String sourceType,
                                        @Optional @Name("sourceId") String sourceId,
                                        @Optional @Name("remark") String remark,
                                        IServiceContext context) {
        if (amount == null || amount.compareTo(ZERO) <= 0) {
            throw new NopException(ERR_WALLET_NOT_FOUND).param("reason", "credit amount must be positive");
        }
        return mutateBalance(userId, amount, changeType, sourceType, sourceId, remark, context, true);
    }

    @Override
    @BizAction
    public LitemallWallet debitBalance(@Name("userId") String userId,
                                       @Name("amount") BigDecimal amount,
                                       @Name("changeType") int changeType,
                                       @Name("sourceType") String sourceType,
                                       @Optional @Name("sourceId") String sourceId,
                                       @Optional @Name("remark") String remark,
                                       IServiceContext context) {
        if (amount == null || amount.compareTo(ZERO) <= 0) {
            throw new NopException(ERR_WALLET_INSUFFICIENT).param("reason", "debit amount must be positive");
        }
        return mutateBalance(userId, amount, changeType, sourceType, sourceId, remark, context, false);
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallWallet adminAdjust(@Name("userId") String userId,
                                      @Name("amount") BigDecimal amount,
                                      @Optional @Name("remark") String remark,
                                      IServiceContext context) {
        if (amount == null || amount.compareTo(ZERO) == 0) {
            return findOrCreateWallet(userId, context);
        }
        // Admin adjust records each call with a unique sourceId (no idempotency dedupe), mirroring
        // LitemallPointsAccountBizModel.adjustPoints.
        boolean isCredit = amount.compareTo(ZERO) > 0;
        int changeType = _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE;
        String sourceId = "adjust-" + CoreMetrics.currentTimeMillis();
        return mutateBalance(userId, amount.abs(), changeType, SOURCE_TYPE_ADMIN_ADJUST, sourceId, remark,
                context, isCredit);
    }

    @Override
    @BizAction
    public LitemallWallet ensureWallet(@Name("userId") String userId,
                                       IServiceContext context) {
        return findOrCreateWallet(userId, context);
    }

    private LitemallWallet mutateBalance(String userId, BigDecimal amount, int changeType,
                                         String sourceType, String sourceId, String remark,
                                         IServiceContext context, boolean isCredit) {
        LitemallWallet wallet = findOrCreateWallet(userId, context);
        BigDecimal currentBalance = nvl(wallet.getBalance());
        BigDecimal newBalance = isCredit ? currentBalance.add(amount) : currentBalance.subtract(amount);
        if (!isCredit && newBalance.compareTo(ZERO) < 0) {
            throw new NopException(ERR_WALLET_INSUFFICIENT)
                    .param(ARG_USER_ID, userId)
                    .param("balance", currentBalance)
                    .param("requested", amount);
        }
        BigDecimal currentRecharge = nvl(wallet.getTotalRecharge());
        BigDecimal currentSpent = nvl(wallet.getTotalSpent());
        // totalRecharge accumulates all credit operations (recharge + admin-adjust credit) and
        // totalSpent accumulates all debit operations, so the account table reconciles with flows.
        BigDecimal newTotalRecharge = isCredit ? currentRecharge.add(amount) : currentRecharge;
        BigDecimal newTotalSpent = !isCredit ? currentSpent.add(amount) : currentSpent;
        int currentVersion = nvlInt(wallet.getVersion());
        String walletId = wallet.orm_idString();

        // Flush the pending insert (first-credit auto-create) so the row exists in DB, then detach
        // the Wallet from the session. A conditional EQL UPDATE on a session-managed entity is
        // routed in-memory and reports 0 affected rows (see LitemallPointsAccountBizModel for the
        // same pattern). Detaching forces the mapper UPDATE to execute as real SQL.
        dao().flushSession();
        dao().clearEntitySessionCache();

        int affected = walletMapper.updateBalanceIfVersion(
                walletId, newBalance, newTotalRecharge, newTotalSpent, currentVersion);
        if (affected == 0) {
            throw new NopException(ERR_WALLET_VERSION_CONFLICT)
                    .param(ARG_USER_ID, userId)
                    .param("reason", "optimistic version conflict");
        }

        // Record the flow with balanceAfter snapshot so account and flow tables reconcile.
        LitemallWalletFlow flow = flowBiz.newEntity();
        flow.setWalletId(walletId);
        flow.setChangeType(changeType);
        flow.setChangeAmount(amount);
        flow.setBalanceAfter(newBalance);
        flow.setSourceType(sourceType);
        flow.setSourceId(sourceId);
        flow.setRemark(remark);
        flowBiz.saveEntity(flow, null, context);

        // Re-read fresh so the returned managed entity reflects the committed balance.
        LitemallWallet updated = findWalletByUserId(userId, context);
        return updated != null ? updated : wallet;
    }

    LitemallWallet findOrCreateWallet(String userId, IServiceContext context) {
        if (StringHelper.isEmpty(userId)) {
            throw new NopException(ERR_WALLET_NOT_FOUND).param(ARG_USER_ID, userId);
        }
        LitemallWallet wallet = findWalletByUserId(userId, context);
        if (wallet != null) {
            return wallet;
        }
        wallet = newEntity();
        wallet.setUserId(userId);
        wallet.setBalance(ZERO);
        wallet.setTotalRecharge(ZERO);
        wallet.setTotalSpent(ZERO);
        wallet.setVersion(0);
        saveEntity(wallet, "creditBalance:autoCreate", context);
        return wallet;
    }

    LitemallWallet findWalletByUserId(String userId, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallWallet.PROP_NAME_userId, userId));
        return findFirst(query, null, context);
    }

    private static final String ARG_USER_ID = "userId";

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private static int nvlInt(Integer value) {
        return value == null ? 0 : value;
    }
}
