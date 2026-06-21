
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallMaterialCategoryBiz;
import app.mall.dao.entity.LitemallMaterialCategory;

@BizModel("LitemallMaterialCategory")
public class LitemallMaterialCategoryBizModel extends CrudBizModel<LitemallMaterialCategory> implements ILitemallMaterialCategoryBiz{
    public LitemallMaterialCategoryBizModel(){
        setEntityName(LitemallMaterialCategory.class.getName());
    }
}
