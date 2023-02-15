
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallAddress;

@BizModel("LitemallAddress")
public class LitemallAddressBizModel extends CrudBizModel<LitemallAddress>{
    public LitemallAddressBizModel(){
        setEntityName(LitemallAddress.class.getName());
    }
}
