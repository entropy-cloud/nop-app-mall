
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallPinTuanActivity;
import app.mall.dao.entity.LitemallPinTuanGroup;

public interface ILitemallPinTuanActivityBiz extends ICrudBiz<LitemallPinTuanActivity> {

    @BizMutation
    LitemallPinTuanGroup openPinTuan(@Name("pinTuanActivityId") String pinTuanActivityId,
                                     @Name("orderId") String orderId,
                                     IServiceContext context);

    @BizMutation
    LitemallPinTuanGroup joinPinTuan(@Name("groupId") String groupId,
                                     @Name("orderId") String orderId,
                                     IServiceContext context);

    @BizQuery
    PageBean<LitemallPinTuanGroup> myPinTuans(@Name("page") int page,
                                              @Name("pageSize") int pageSize,
                                              IServiceContext context);

    @BizQuery
    LitemallPinTuanGroup pinTuanDetail(@Name("groupId") String groupId, IServiceContext context);

    @BizQuery
    LitemallPinTuanActivity pinTuanForGoods(@Name("goodsId") String goodsId, IServiceContext context);

    @BizMutation
    int expirePinTuans(IServiceContext context);
}
