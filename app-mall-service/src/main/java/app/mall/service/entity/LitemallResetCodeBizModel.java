
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallResetCodeBiz;
import app.mall.dao.entity.LitemallResetCode;

@BizModel("LitemallResetCode")
public class LitemallResetCodeBizModel extends CrudBizModel<LitemallResetCode> implements ILitemallResetCodeBiz{
    public LitemallResetCodeBizModel(){
        setEntityName(LitemallResetCode.class.getName());
    }
}
