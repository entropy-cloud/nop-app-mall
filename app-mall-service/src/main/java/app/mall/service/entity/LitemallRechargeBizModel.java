
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallRechargeBiz;
import app.mall.dao.entity.LitemallRecharge;

@BizModel("LitemallRecharge")
public class LitemallRechargeBizModel extends CrudBizModel<LitemallRecharge> implements ILitemallRechargeBiz{
    public LitemallRechargeBizModel(){
        setEntityName(LitemallRecharge.class.getName());
    }
}
