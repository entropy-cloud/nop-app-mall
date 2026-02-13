
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallBrandBiz;

import app.mall.dao.entity.LitemallBrand;

@BizModel("LitemallBrand")
public class LitemallBrandBizModel extends CrudBizModel<LitemallBrand> implements ILitemallBrandBiz{
    public LitemallBrandBizModel(){
        setEntityName(LitemallBrand.class.getName());
    }
}
