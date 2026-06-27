
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallMemberLevel;

import java.util.List;
import java.util.Map;

public interface ILitemallMemberLevelBiz extends ICrudBiz<LitemallMemberLevel> {

    @BizQuery
    Map<String, Object> getMyLevelProgress(IServiceContext context);

    @BizMutation
    int evaluateMyLevel(IServiceContext context);

    @BizMutation
    int evaluateUserLevel(@Name("userId") String userId, IServiceContext context);

    @BizMutation
    @Auth(roles = "admin")
    int setUserLevel(@Name("userId") String userId,
                     @Name("targetLevel") int targetLevel,
                     @Optional @Name("remark") String remark,
                     IServiceContext context);

    @BizMutation
    int downgradeExpiredLevels(@Optional @Name("periodDays") Integer periodDays, IServiceContext context);

    @BizQuery
    List<LitemallMemberLevel> findLevelRules(IServiceContext context);
}
