
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallGrouponRules;

@BizModel("LitemallGrouponRules")
public class LitemallGrouponRulesBizModel extends CrudBizModel<LitemallGrouponRules>{
    public LitemallGrouponRulesBizModel(){
        setEntityName(LitemallGrouponRules.class.getName());
    }
}
