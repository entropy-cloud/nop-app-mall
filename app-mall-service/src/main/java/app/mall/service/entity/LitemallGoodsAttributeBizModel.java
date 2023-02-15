
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallGoodsAttribute;

@BizModel("LitemallGoodsAttribute")
public class LitemallGoodsAttributeBizModel extends CrudBizModel<LitemallGoodsAttribute>{
    public LitemallGoodsAttributeBizModel(){
        setEntityName(LitemallGoodsAttribute.class.getName());
    }
}
