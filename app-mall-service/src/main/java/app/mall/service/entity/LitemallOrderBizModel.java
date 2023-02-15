
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallOrder;

@BizModel("LitemallOrder")
public class LitemallOrderBizModel extends CrudBizModel<LitemallOrder>{
    public LitemallOrderBizModel(){
        setEntityName(LitemallOrder.class.getName());
    }
}
