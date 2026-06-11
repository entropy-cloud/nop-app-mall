
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallUserRoleBiz;

import app.mall.dao.entity.LitemallUserRole;

@BizModel("LitemallUserRole")
public class LitemallUserRoleBizModel extends CrudBizModel<LitemallUserRole> implements ILitemallUserRoleBiz {
    public LitemallUserRoleBizModel(){
        setEntityName(LitemallUserRole.class.getName());
    }
}
