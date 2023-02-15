
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallCouponUser;

@BizModel("LitemallCouponUser")
public class LitemallCouponUserBizModel extends CrudBizModel<LitemallCouponUser>{
    public LitemallCouponUserBizModel(){
        setEntityName(LitemallCouponUser.class.getName());
    }
}
