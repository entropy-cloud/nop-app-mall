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
import java.util.Set;

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

    @BizMutation
    void batchApprove(@Name("ids") Set<String> ids, IServiceContext context);

    @BizMutation
    void batchReject(@Name("ids") Set<String> ids, IServiceContext context);

    @BizMutation
    void refund(@Name("id") String id, IServiceContext context);

    @BizMutation
    LitemallAftersale submitReturnLogistics(@Name("id") String id,
                                            @Name("returnShipChannel") String returnShipChannel,
                                            @Name("returnShipSn") String returnShipSn,
                                            IServiceContext context);

    @BizMutation
    LitemallAftersale confirmReturnReceived(@Name("id") String id, IServiceContext context);
}
