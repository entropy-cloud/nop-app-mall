
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallRole;

@BizModel("LitemallRole")
public class LitemallRoleBizModel extends CrudBizModel<LitemallRole>{
    public LitemallRoleBizModel(){
        setEntityName(LitemallRole.class.getName());
    }
}
