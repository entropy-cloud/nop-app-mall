
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallTimeDiscountBiz;
import app.mall.dao.entity.LitemallTimeDiscount;

@BizModel("LitemallTimeDiscount")
public class LitemallTimeDiscountBizModel extends CrudBizModel<LitemallTimeDiscount> implements ILitemallTimeDiscountBiz{
    public LitemallTimeDiscountBizModel(){
        setEntityName(LitemallTimeDiscount.class.getName());
    }
}
