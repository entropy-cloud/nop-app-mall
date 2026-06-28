
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallPromotionUsageBiz;
import app.mall.dao.entity.LitemallPromotionUsage;

@BizModel("LitemallPromotionUsage")
public class LitemallPromotionUsageBizModel extends CrudBizModel<LitemallPromotionUsage> implements ILitemallPromotionUsageBiz{
    public LitemallPromotionUsageBizModel(){
        setEntityName(LitemallPromotionUsage.class.getName());
    }
}
