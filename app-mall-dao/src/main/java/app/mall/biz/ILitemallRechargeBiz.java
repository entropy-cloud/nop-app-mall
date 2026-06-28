
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
}
