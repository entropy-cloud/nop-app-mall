
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallPinTuanGroupBiz;
import app.mall.dao.entity.LitemallPinTuanGroup;

@BizModel("LitemallPinTuanGroup")
public class LitemallPinTuanGroupBizModel extends CrudBizModel<LitemallPinTuanGroup> implements ILitemallPinTuanGroupBiz{
    public LitemallPinTuanGroupBizModel(){
        setEntityName(LitemallPinTuanGroup.class.getName());
    }
}
