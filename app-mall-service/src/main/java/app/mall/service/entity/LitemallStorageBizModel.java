
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallStorage;

@BizModel("LitemallStorage")
public class LitemallStorageBizModel extends CrudBizModel<LitemallStorage>{
    public LitemallStorageBizModel(){
        setEntityName(LitemallStorage.class.getName());
    }
}
