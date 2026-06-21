
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallFlashSaleSessionBiz;
import app.mall.dao.entity.LitemallFlashSaleSession;

@BizModel("LitemallFlashSaleSession")
public class LitemallFlashSaleSessionBizModel extends CrudBizModel<LitemallFlashSaleSession> implements ILitemallFlashSaleSessionBiz{
    public LitemallFlashSaleSessionBizModel(){
        setEntityName(LitemallFlashSaleSession.class.getName());
    }
}
