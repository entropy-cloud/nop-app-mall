
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallUserMessageBiz;
import app.mall.dao.entity.LitemallUserMessage;

@BizModel("LitemallUserMessage")
public class LitemallUserMessageBizModel extends CrudBizModel<LitemallUserMessage> implements ILitemallUserMessageBiz{
    public LitemallUserMessageBizModel(){
        setEntityName(LitemallUserMessage.class.getName());
    }
}
