
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallPromotionTierBiz;
import app.mall.dao.entity.LitemallPromotionTier;

@BizModel("LitemallPromotionTier")
public class LitemallPromotionTierBizModel extends CrudBizModel<LitemallPromotionTier> implements ILitemallPromotionTierBiz{
    public LitemallPromotionTierBizModel(){
        setEntityName(LitemallPromotionTier.class.getName());
    }
}
