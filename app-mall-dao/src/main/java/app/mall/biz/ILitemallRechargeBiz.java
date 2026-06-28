
package app.mall.biz;

import app.mall.dao.entity.LitemallRecharge;
import io.nop.api.core.annotations.biz.BizAction;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ILitemallRechargeBiz extends ICrudBiz<LitemallRecharge>{

    @BizQuery
    List<Map<String, Object>> getRechargePackages(IServiceContext context);

    @BizMutation
    Map<String, Object> createRecharge(@Name("amount") BigDecimal amount,
                                       @Optional @Name("packageId") String packageId,
                                       IServiceContext context);

    @BizMutation
    LitemallRecharge confirmRecharge(@Name("rechargeId") String rechargeId,
                                    IServiceContext context);

    /**
     * Trusted internal entry: drive an UNPAID recharge to PAID after WeChat Pay async notify
     * signature verification succeeds. Idempotent — already-PAID recharges are skipped. Not
     * exposed as a GraphQL mutation (fund-safety: a public userId/rechargeId-confirm would let
     * any attacker credit a wallet without real payment). Invoked by {@code PaymentCallbackImpl}
     * via injection when the outTradeNo starts with the {@code RC} prefix.
     */
    @BizAction
    void confirmRechargeByNotify(@Name("outTradeNo") String outTradeNo,
                                 @Name("transactionId") String transactionId,
                                 IServiceContext context);

    /**
     * Trusted internal entry: reverse a PAID recharge (debit amount+giftAmount) after a channel-side
     * refund async notify (WeChat/Alipay merchant-backend refund of the original recharge payment).
     * Idempotent — already-REFUNDED recharges are skipped, UNPAID recharges (never credited) are
     * skipped, and optimistic-version conflicts on the wallet are caught + logged as a no-op (the
     * winner of the concurrent retry already completed the reversal). Insufficient balance degrades
     * safely: logs a WARN for manual reconciliation without advancing REFUNDED, preserving the
     * debitBalance fund-safety invariant (no negative balance). Not exposed as GraphQL (same
     * fund-safety model as {@code confirmRechargeByNotify}). Invoked by {@code PaymentCallbackImpl}
     * via injection when an {@code onRefundSuccess} notify's outTradeNo starts with {@code RC}.
     */
    @BizAction
    void refundRechargeByNotify(@Name("outTradeNo") String outTradeNo,
                                @Optional @Name("outRefundNo") String outRefundNo,
                                IServiceContext context);
}
