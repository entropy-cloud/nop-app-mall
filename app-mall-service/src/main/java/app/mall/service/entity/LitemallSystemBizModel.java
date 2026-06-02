
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallSystemBiz;

import app.mall.dao.entity.LitemallSystem;

@BizModel("LitemallSystem")
public class LitemallSystemBizModel extends CrudBizModel<LitemallSystem> implements ILitemallSystemBiz {
    public LitemallSystemBizModel(){
        setEntityName(LitemallSystem.class.getName());
    }
}
