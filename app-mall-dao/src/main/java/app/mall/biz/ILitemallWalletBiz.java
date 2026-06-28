
package app.mall.biz;

import app.mall.dao.entity.LitemallWallet;
import io.nop.api.core.annotations.biz.BizAction;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.math.BigDecimal;

public interface ILitemallWalletBiz extends ICrudBiz<LitemallWallet>{

    @BizQuery
    LitemallWallet getMyWallet(IServiceContext context);

    /**
     * Internal entry: credit the wallet by {@code amount} (must be &gt; 0). Not exposed as a
     * GraphQL mutation — only trusted BizModels may invoke it via injection, since a public
     * userId+amount mutation would let any logged-in user credit any wallet (fund theft).
     */
    @BizAction
    LitemallWallet creditBalance(@Name("userId") String userId,
                                 @Name("amount") BigDecimal amount,
                                 @Name("changeType") int changeType,
                                 @Name("sourceType") String sourceType,
                                 @Optional @Name("sourceId") String sourceId,
                                 @Optional @Name("remark") String remark,
                                 IServiceContext context);

    /**
     * Internal entry: debit the wallet by {@code amount} (must be &gt; 0). Rejects when the
     * resulting balance would go negative. Not exposed as a GraphQL mutation — same fund-safety
     * reason as {@link #creditBalance}. Intended for P30 balance-payment checkout wiring.
     */
    @BizAction
    LitemallWallet debitBalance(@Name("userId") String userId,
                                @Name("amount") BigDecimal amount,
                                @Name("changeType") int changeType,
                                @Name("sourceType") String sourceType,
                                @Optional @Name("sourceId") String sourceId,
                                @Optional @Name("remark") String remark,
                                IServiceContext context);

    /**
     * Internal entry: ensure the wallet for {@code userId} exists (lazy-create with zero balance if
     * not yet persisted) and return it. Not exposed as a GraphQL mutation. Used by trusted BizModels
     * that need to record a walletId reference before the first credit (e.g. recharge record creation,
     * where walletId is a mandatory column).
     */
    @BizAction
    LitemallWallet ensureWallet(@Name("userId") String userId,
                                IServiceContext context);

    @BizMutation
    @Auth(roles = "admin")
    LitemallWallet adminAdjust(@Name("userId") String userId,
                               @Name("amount") BigDecimal amount,
                               @Optional @Name("remark") String remark,
                               IServiceContext context);
}
