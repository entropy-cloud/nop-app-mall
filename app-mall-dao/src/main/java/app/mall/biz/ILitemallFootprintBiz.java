package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallFootprint;

public interface ILitemallFootprintBiz extends ICrudBiz<LitemallFootprint> {

    @BizMutation
    void recordFootprint(@Name("goodsId") String goodsId,
                         IServiceContext context);

    @BizQuery
    PageBean<LitemallFootprint> listFootprints(@Name("page") int page,
                                               @Name("pageSize") int pageSize,
                                               IServiceContext context);

    @BizMutation
    void clearFootprints(IServiceContext context);
}
