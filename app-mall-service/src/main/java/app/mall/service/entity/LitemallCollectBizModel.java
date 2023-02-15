
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallCollect;

@BizModel("LitemallCollect")
public class LitemallCollectBizModel extends CrudBizModel<LitemallCollect>{
    public LitemallCollectBizModel(){
        setEntityName(LitemallCollect.class.getName());
    }
}
