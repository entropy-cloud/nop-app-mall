
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallGoodsSpecification;

@BizModel("LitemallGoodsSpecification")
public class LitemallGoodsSpecificationBizModel extends CrudBizModel<LitemallGoodsSpecification>{
    public LitemallGoodsSpecificationBizModel(){
        setEntityName(LitemallGoodsSpecification.class.getName());
    }
}
