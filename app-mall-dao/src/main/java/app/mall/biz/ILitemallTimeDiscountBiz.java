package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallTimeDiscount;

import java.util.Map;

public interface ILitemallTimeDiscountBiz extends ICrudBiz<LitemallTimeDiscount> {

    @BizQuery
    Map<String, Object> selectTimeDiscountForProduct(@Name("goodsId") String goodsId,
                                                      @Optional @Name("productId") String productId,
                                                      IServiceContext context);

    @BizMutation
    LitemallTimeDiscount publishActivity(@Name("id") String id, IServiceContext context);

    @BizMutation
    LitemallTimeDiscount unpublishActivity(@Name("id") String id, IServiceContext context);
}
