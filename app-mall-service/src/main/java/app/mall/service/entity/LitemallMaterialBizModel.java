
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallMaterialBiz;
import app.mall.dao.entity.LitemallMaterial;

@BizModel("LitemallMaterial")
public class LitemallMaterialBizModel extends CrudBizModel<LitemallMaterial> implements ILitemallMaterialBiz{
    public LitemallMaterialBizModel(){
        setEntityName(LitemallMaterial.class.getName());
    }
}
