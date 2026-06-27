
package app.mall.biz;

import app.mall.dao.entity.LitemallFlashSale;
import app.mall.dao.entity.LitemallOrder;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.util.Map;

public interface ILitemallFlashSaleBiz extends ICrudBiz<LitemallFlashSale> {

    @BizMutation
    LitemallOrder flashSaleBuy(@Name("flashSaleSessionId") String flashSaleSessionId,
                                @Name("addressId") String addressId,
                                @Optional @Name("productId") String productId,
                                @Name("number") Integer number,
                                IServiceContext context);

    @BizQuery
    Map<String, Object> activeFlashSales(@Optional @Name("page") Integer page,
                                          @Optional @Name("pageSize") Integer pageSize,
                                          IServiceContext context);

    @BizQuery
    Map<String, Object> flashSaleDetail(@Name("id") String id,
                                         IServiceContext context);

    @BizQuery
    Map<String, Object> flashSaleForGoods(@Name("goodsId") String goodsId,
                                           @Optional @Name("productId") String productId,
                                           IServiceContext context);
}
