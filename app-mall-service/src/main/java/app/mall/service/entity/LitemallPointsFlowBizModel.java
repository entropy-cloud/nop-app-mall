
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallPointsFlowBiz;
import app.mall.dao.entity.LitemallPointsFlow;

@BizModel("LitemallPointsFlow")
public class LitemallPointsFlowBizModel extends CrudBizModel<LitemallPointsFlow> implements ILitemallPointsFlowBiz{
    public LitemallPointsFlowBizModel(){
        setEntityName(LitemallPointsFlow.class.getName());
    }
}
