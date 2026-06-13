package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallOrder;

import java.math.BigDecimal;
import java.util.List;

public interface ILitemallOrderBiz extends ICrudBiz<LitemallOrder> {

    @BizMutation
    LitemallOrder submit(@Name("addressId") String addressId,
                         @Optional @Name("message") String message,
                         @Name("freightPrice") BigDecimal freightPrice,
                         @Optional @Name("couponUserId") String couponUserId,
                         @Optional @Name("grouponRulesId") String grouponRulesId,
                         @Optional @Name("grouponId") String grouponId,
                         IServiceContext context);

    @BizMutation
    LitemallOrder cancel(@Name("orderId") String orderId,
                         IServiceContext context);

    @BizMutation
    LitemallOrder pay(@Name("orderId") String orderId,
                      IServiceContext context);

    @BizMutation
    LitemallOrder ship(@Name("orderId") String orderId,
                       @Name("shipSn") String shipSn,
                       @Name("shipChannel") String shipChannel,
                       IServiceContext context);

    @BizMutation
    LitemallOrder confirm(@Name("orderId") String orderId,
                          IServiceContext context);

    @BizMutation
    void deleteOrder(@Name("orderId") String orderId,
                     IServiceContext context);

    @BizQuery
    List<LitemallOrder> myOrders(@Optional @Name("status") Integer status,
                                 IServiceContext context);

    @BizQuery
    LitemallOrder getMyOrder(@Name("orderId") String orderId,
                             IServiceContext context);

    @BizMutation
    int cancelExpiredOrders(@Name("timeoutMinutes") int timeoutMinutes,
                            IServiceContext context);

    @BizMutation
    int confirmExpiredOrders(@Name("timeoutMinutes") int timeoutMinutes,
                             IServiceContext context);
}
