
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallGrouponRules;

public interface ILitemallGrouponRulesBiz extends ICrudBiz<LitemallGrouponRules> {

    @BizMutation
    LitemallGrouponRules publishRules(@Name("id") String id, IServiceContext context);

    @BizMutation
    LitemallGrouponRules unpublishRules(@Name("id") String id, IServiceContext context);

    @BizQuery
    PageBean<LitemallGrouponRules> listAvailableRules(@Name("page") int page,
                                                       @Name("pageSize") int pageSize,
                                                       IServiceContext context);
}
