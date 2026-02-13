
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallCouponUserBiz;

import app.mall.dao.entity.LitemallCouponUser;

@BizModel("LitemallCouponUser")
public class LitemallCouponUserBizModel extends CrudBizModel<LitemallCouponUser> implements ILitemallCouponUserBiz{
    public LitemallCouponUserBizModel(){
        setEntityName(LitemallCouponUser.class.getName());
    }
}
