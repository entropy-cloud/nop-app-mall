
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallUser;

@BizModel("LitemallUser")
public class LitemallUserBizModel extends CrudBizModel<LitemallUser>{
    public LitemallUserBizModel(){
        setEntityName(LitemallUser.class.getName());
    }
}
