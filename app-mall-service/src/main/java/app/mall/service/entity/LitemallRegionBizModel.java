
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallRegion;

@BizModel("LitemallRegion")
public class LitemallRegionBizModel extends CrudBizModel<LitemallRegion>{
    public LitemallRegionBizModel(){
        setEntityName(LitemallRegion.class.getName());
    }
}
