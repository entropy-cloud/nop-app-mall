
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallGroupon;

@BizModel("LitemallGroupon")
public class LitemallGrouponBizModel extends CrudBizModel<LitemallGroupon>{
    public LitemallGrouponBizModel(){
        setEntityName(LitemallGroupon.class.getName());
    }
}
