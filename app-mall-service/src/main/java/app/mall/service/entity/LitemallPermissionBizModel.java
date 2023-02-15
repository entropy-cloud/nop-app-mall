
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallPermission;

@BizModel("LitemallPermission")
public class LitemallPermissionBizModel extends CrudBizModel<LitemallPermission>{
    public LitemallPermissionBizModel(){
        setEntityName(LitemallPermission.class.getName());
    }
}
