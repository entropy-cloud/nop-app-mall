
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallLog;

@BizModel("LitemallLog")
public class LitemallLogBizModel extends CrudBizModel<LitemallLog>{
    public LitemallLogBizModel(){
        setEntityName(LitemallLog.class.getName());
    }
}
