
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallWalletFlowBiz;
import app.mall.dao.entity.LitemallWalletFlow;

@BizModel("LitemallWalletFlow")
public class LitemallWalletFlowBizModel extends CrudBizModel<LitemallWalletFlow> implements ILitemallWalletFlowBiz{
    public LitemallWalletFlowBizModel(){
        setEntityName(LitemallWalletFlow.class.getName());
    }
}
