package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.biz.RequestBean;
import io.nop.api.core.annotations.core.Name;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.dto.AftersaleApplyRequest;
import app.mall.dao.entity.LitemallAftersale;

import java.util.List;

public interface ILitemallAftersaleBiz extends ICrudBiz<LitemallAftersale> {

    @BizMutation
    LitemallAftersale apply(@RequestBean AftersaleApplyRequest request,
                            IServiceContext context);

    @BizMutation
    LitemallAftersale cancel(@Name("id") String id,
                             IServiceContext context);

    @BizQuery
    List<LitemallAftersale> userList(IServiceContext context);

    @BizQuery
    LitemallAftersale userDetail(@Name("id") String id,
                                 IServiceContext context);
}
