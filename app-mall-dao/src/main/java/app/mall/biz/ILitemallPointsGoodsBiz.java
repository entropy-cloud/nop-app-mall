
package app.mall.biz;

import app.mall.dao.entity.LitemallPointsGoods;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.util.Map;

public interface ILitemallPointsGoodsBiz extends ICrudBiz<LitemallPointsGoods> {

    @BizQuery
    Map<String, Object> activePointsGoods(@Optional @Name("page") Integer page,
                                          @Optional @Name("pageSize") Integer pageSize,
                                          IServiceContext context);

    @BizQuery
    Map<String, Object> pointsGoodsDetail(@Name("id") String id, IServiceContext context);

    @BizMutation
    @Auth(roles = "admin")
    LitemallPointsGoods publishActivity(@Name("id") String id, IServiceContext context);

    @BizMutation
    @Auth(roles = "admin")
    LitemallPointsGoods unpublishActivity(@Name("id") String id, IServiceContext context);
}
