
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallFootprintBiz;

import app.mall.dao.entity.LitemallFootprint;

@BizModel("LitemallFootprint")
public class LitemallFootprintBizModel extends CrudBizModel<LitemallFootprint> implements ILitemallFootprintBiz{
    public LitemallFootprintBizModel(){
        setEntityName(LitemallFootprint.class.getName());
    }
}
