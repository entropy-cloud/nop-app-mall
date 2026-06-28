
package app.mall.biz;

import app.mall.dao.entity.LitemallPointsExchangeOrder;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.util.Map;

public interface ILitemallPointsExchangeOrderBiz extends ICrudBiz<LitemallPointsExchangeOrder> {

    @BizMutation
    LitemallPointsExchangeOrder exchange(@Name("pointsGoodsId") String pointsGoodsId,
                                         @Name("quantity") Integer quantity,
                                         @Optional @Name("addressId") String addressId,
                                         IServiceContext context);

    @BizQuery
    Map<String, Object> myExchangeOrders(@Optional @Name("exchangeStatus") Integer exchangeStatus,
                                         @Optional @Name("page") Integer page,
                                         @Optional @Name("pageSize") Integer pageSize,
                                         IServiceContext context);

    @BizMutation
    @Auth(roles = "admin")
    LitemallPointsExchangeOrder shipExchangeOrder(@Name("id") String id,
                                                  @Name("shipCode") String shipCode,
                                                  IServiceContext context);

    @BizMutation
    LitemallPointsExchangeOrder confirmExchangeOrder(@Name("id") String id, IServiceContext context);

    @BizMutation
    LitemallPointsExchangeOrder cancelExchangeOrder(@Name("id") String id,
                                                    @Optional @Name("remark") String remark,
                                                    IServiceContext context);
}
