
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallGoodsProduct;

@BizModel("LitemallGoodsProduct")
public class LitemallGoodsProductBizModel extends CrudBizModel<LitemallGoodsProduct>{
    public LitemallGoodsProductBizModel(){
        setEntityName(LitemallGoodsProduct.class.getName());
    }
}
