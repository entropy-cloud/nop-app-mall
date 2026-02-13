
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallGrouponRulesBiz;

import app.mall.dao.entity.LitemallGrouponRules;

@BizModel("LitemallGrouponRules")
public class LitemallGrouponRulesBizModel extends CrudBizModel<LitemallGrouponRules> implements ILitemallGrouponRulesBiz{
    public LitemallGrouponRulesBizModel(){
        setEntityName(LitemallGrouponRules.class.getName());
    }
}
