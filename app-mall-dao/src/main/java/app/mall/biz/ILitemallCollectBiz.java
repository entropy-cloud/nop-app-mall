package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallCollect;

import java.util.List;

public interface ILitemallCollectBiz extends ICrudBiz<LitemallCollect> {

    @BizMutation
    LitemallCollect addCollect(@Name("type") int type,
                               @Name("valueId") String valueId,
                               IServiceContext context);

    @BizMutation
    void removeCollect(@Name("type") int type,
                       @Name("valueId") String valueId,
                       IServiceContext context);

    @BizQuery
    boolean isCollect(@Name("type") int type,
                      @Name("valueId") String valueId,
                      IServiceContext context);

    @BizQuery
    PageBean<LitemallCollect> listByType(@Name("type") int type,
                                         @Name("page") int page,
                                         @Name("pageSize") int pageSize,
                                         IServiceContext context);
}
