
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallCart;

@BizModel("LitemallCart")
public class LitemallCartBizModel extends CrudBizModel<LitemallCart>{
    public LitemallCartBizModel(){
        setEntityName(LitemallCart.class.getName());
    }
}
