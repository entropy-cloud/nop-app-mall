
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallRegionBiz;

import app.mall.dao.entity.LitemallRegion;

@BizModel("LitemallRegion")
public class LitemallRegionBizModel extends CrudBizModel<LitemallRegion> implements ILitemallRegionBiz {
    public LitemallRegionBizModel(){
        setEntityName(LitemallRegion.class.getName());
    }
}
