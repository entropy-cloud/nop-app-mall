
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.dao.entity.LitemallPointsAccount;

@BizModel("LitemallPointsAccount")
public class LitemallPointsAccountBizModel extends CrudBizModel<LitemallPointsAccount> implements ILitemallPointsAccountBiz{
    public LitemallPointsAccountBizModel(){
        setEntityName(LitemallPointsAccount.class.getName());
    }
}
