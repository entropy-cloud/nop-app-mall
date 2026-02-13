
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallCouponBiz;

import app.mall.dao.entity.LitemallCoupon;

@BizModel("LitemallCoupon")
public class LitemallCouponBizModel extends CrudBizModel<LitemallCoupon> implements ILitemallCouponBiz{
    public LitemallCouponBizModel(){
        setEntityName(LitemallCoupon.class.getName());
    }
}
