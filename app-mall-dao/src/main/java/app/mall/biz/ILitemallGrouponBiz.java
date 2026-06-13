
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallGroupon;

public interface ILitemallGrouponBiz extends ICrudBiz<LitemallGroupon> {

    @BizMutation
    LitemallGroupon openGroupon(@Name("rulesId") String rulesId,
                                @Name("orderId") String orderId,
                                IServiceContext context);

    @BizMutation
    LitemallGroupon joinGroupon(@Name("grouponId") String grouponId,
                                @Name("orderId") String orderId,
                                IServiceContext context);

    @BizQuery
    PageBean<LitemallGroupon> myGroupons(@Name("page") int page,
                                          @Name("pageSize") int pageSize,
                                          IServiceContext context);

    @BizQuery
    LitemallGroupon grouponDetail(@Name("id") String id, IServiceContext context);

    @BizMutation
    int expireGroupons(IServiceContext context);
}
