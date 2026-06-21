
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallCheckInRuleBiz;
import app.mall.dao.entity.LitemallCheckInRule;

@BizModel("LitemallCheckInRule")
public class LitemallCheckInRuleBizModel extends CrudBizModel<LitemallCheckInRule> implements ILitemallCheckInRuleBiz{
    public LitemallCheckInRuleBizModel(){
        setEntityName(LitemallCheckInRule.class.getName());
    }
}
