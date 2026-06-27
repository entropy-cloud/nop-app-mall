
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.exceptions.NopException;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallPromotionActivity;

import java.math.BigDecimal;
import java.util.List;

public interface ILitemallPromotionActivityBiz extends ICrudBiz<LitemallPromotionActivity>{

    @BizQuery
    BigDecimal selectPromotionForOrder(@Name("goodsPrice") BigDecimal goodsPrice,
                                       @Optional @Name("goodsScopeIds") List<String> goodsScopeIds,
                                       IServiceContext context) throws NopException;
}
