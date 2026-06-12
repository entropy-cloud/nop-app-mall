
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallCategory;

import java.util.List;
import java.util.Map;

public interface ILitemallCategoryBiz extends ICrudBiz<LitemallCategory>{

    @BizQuery
    List<Map<String, Object>> getCategoryTree();

    @BizQuery
    List<LitemallCategory> getCategoryList();
}
