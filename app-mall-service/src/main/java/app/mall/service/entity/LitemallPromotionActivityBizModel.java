
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallPromotionActivityBiz;
import app.mall.dao.entity.LitemallPromotionActivity;

@BizModel("LitemallPromotionActivity")
public class LitemallPromotionActivityBizModel extends CrudBizModel<LitemallPromotionActivity> implements ILitemallPromotionActivityBiz{
    public LitemallPromotionActivityBizModel(){
        setEntityName(LitemallPromotionActivity.class.getName());
    }
}
