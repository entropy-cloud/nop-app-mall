
package app.mall.biz;

import app.mall.dao.entity.LitemallPointsAccount;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

public interface ILitemallPointsAccountBiz extends ICrudBiz<LitemallPointsAccount>{

    @BizQuery
    int getMyPoints(IServiceContext context);

    @BizMutation
    LitemallPointsAccount earnPoints(@Name("userId") String userId,
                                     @Name("amount") int amount,
                                     @Name("changeType") int changeType,
                                     @Name("sourceType") String sourceType,
                                     @Optional @Name("sourceId") String sourceId,
                                     @Optional @Name("remark") String remark,
                                     IServiceContext context);

    @BizMutation
    LitemallPointsAccount spendPoints(@Name("userId") String userId,
                                      @Name("amount") int amount,
                                      @Name("changeType") int changeType,
                                      @Name("sourceType") String sourceType,
                                      @Optional @Name("sourceId") String sourceId,
                                      @Optional @Name("remark") String remark,
                                      IServiceContext context);

    @BizMutation
    @Auth(roles = "admin")
    LitemallPointsAccount adjustPoints(@Name("userId") String userId,
                                       @Name("amount") int amount,
                                       @Optional @Name("remark") String remark,
                                       IServiceContext context);

    // Internal config-resolution helpers (not GraphQL-exposed): read NopSysVariable defaults
    // for points ratios. Used by LitemallOrderBizModel to compute integralPrice / shopping rewards.
    int resolveEarnPerYuan(IServiceContext context);

    int resolveToYuanRatio(IServiceContext context);

    double resolveDeductMaxRatio(IServiceContext context);
}
