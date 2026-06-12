package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallCart;

import java.util.List;

public interface ILitemallCartBiz extends ICrudBiz<LitemallCart> {

    @BizMutation
    LitemallCart addGoods(@Name("goodsId") String goodsId,
                          @Name("productId") String productId,
                          @Name("number") int number,
                          IServiceContext context);

    @BizMutation
    LitemallCart updateQuantity(@Name("id") String id,
                                @Name("number") int number,
                                IServiceContext context);

    @BizMutation
    LitemallCart check(@Name("id") String id, IServiceContext context);

    @BizMutation
    LitemallCart uncheck(@Name("id") String id, IServiceContext context);

    @BizMutation
    void checkAll(IServiceContext context);

    @BizMutation
    void uncheckAll(IServiceContext context);

    @BizMutation
    void deleteCart(@Name("id") String id, IServiceContext context);

    @BizMutation
    void clear(IServiceContext context);

    @BizQuery
    List<LitemallCart> checkedList(IServiceContext context);
}
