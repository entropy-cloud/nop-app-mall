
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallUserTag;

public interface ILitemallUserTagBiz extends ICrudBiz<LitemallUserTag> {

    @BizMutation
    @Auth(roles = "admin")
    LitemallUserTag addUserTag(@Name("userId") String userId,
                               @Name("tag") String tag,
                               @Optional @Name("name") String name,
                               IServiceContext context);

    @BizMutation
    @Auth(roles = "admin")
    void removeUserTag(@Name("userId") String userId,
                       @Name("tag") String tag,
                       IServiceContext context);

    @BizQuery
    @Auth(roles = "admin")
    PageBean<LitemallUserTag> findUsersByTag(@Name("tag") String tag,
                                             @Optional @Name("page") Integer page,
                                             IServiceContext context);
}
