
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.dto.UserWorkbenchSummaryBean;
import app.mall.dao.entity.LitemallUserBlacklist;

public interface ILitemallUserBlacklistBiz extends ICrudBiz<LitemallUserBlacklist> {

    @BizMutation
    @Auth(roles = "admin")
    LitemallUserBlacklist banUser(@Name("userId") String userId,
                                  @Optional @Name("reason") String reason,
                                  IServiceContext context);

    @BizMutation
    @Auth(roles = "admin")
    void unbanUser(@Name("userId") String userId,
                   IServiceContext context);

    @BizQuery
    @Auth(roles = "admin")
    UserWorkbenchSummaryBean getUserWorkbenchSummary(@Name("userId") String userId,
                                                     IServiceContext context);
}
