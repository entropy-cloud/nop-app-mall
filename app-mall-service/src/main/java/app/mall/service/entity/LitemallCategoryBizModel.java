
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallCategory;

@BizModel("LitemallCategory")
public class LitemallCategoryBizModel extends CrudBizModel<LitemallCategory>{
    public LitemallCategoryBizModel(){
        setEntityName(LitemallCategory.class.getName());
    }
}
