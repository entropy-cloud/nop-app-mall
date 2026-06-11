
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallGoodsAttributeBiz;

import app.mall.dao.entity.LitemallGoodsAttribute;

@BizModel("LitemallGoodsAttribute")
public class LitemallGoodsAttributeBizModel extends CrudBizModel<LitemallGoodsAttribute> implements ILitemallGoodsAttributeBiz {
    public LitemallGoodsAttributeBizModel(){
        setEntityName(LitemallGoodsAttribute.class.getName());
    }
}
