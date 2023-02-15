
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallAdmin;

@BizModel("LitemallAdmin")
public class LitemallAdminBizModel extends CrudBizModel<LitemallAdmin>{
    public LitemallAdminBizModel(){
        setEntityName(LitemallAdmin.class.getName());
    }
}
