
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallPinTuanActivityBiz;
import app.mall.dao.entity.LitemallPinTuanActivity;

@BizModel("LitemallPinTuanActivity")
public class LitemallPinTuanActivityBizModel extends CrudBizModel<LitemallPinTuanActivity> implements ILitemallPinTuanActivityBiz{
    public LitemallPinTuanActivityBizModel(){
        setEntityName(LitemallPinTuanActivity.class.getName());
    }
}
