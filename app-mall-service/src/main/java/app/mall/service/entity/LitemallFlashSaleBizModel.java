
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallFlashSaleBiz;
import app.mall.dao.entity.LitemallFlashSale;

@BizModel("LitemallFlashSale")
public class LitemallFlashSaleBizModel extends CrudBizModel<LitemallFlashSale> implements ILitemallFlashSaleBiz{
    public LitemallFlashSaleBizModel(){
        setEntityName(LitemallFlashSale.class.getName());
    }
}
