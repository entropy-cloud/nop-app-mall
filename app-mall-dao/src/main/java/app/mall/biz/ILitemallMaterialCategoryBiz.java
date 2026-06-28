
package app.mall.biz;

import app.mall.dao.entity.LitemallMaterialCategory;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.util.List;
import java.util.Map;

public interface ILitemallMaterialCategoryBiz extends ICrudBiz<LitemallMaterialCategory>{

    @BizQuery
    @Auth(roles = "admin")
    List<Map<String, Object>> getCategoryTree(IServiceContext context);
}
