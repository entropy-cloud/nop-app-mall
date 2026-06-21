
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallPickupStoreBiz;
import app.mall.dao.entity.LitemallPickupStore;

@BizModel("LitemallPickupStore")
public class LitemallPickupStoreBizModel extends CrudBizModel<LitemallPickupStore> implements ILitemallPickupStoreBiz{
    public LitemallPickupStoreBizModel(){
        setEntityName(LitemallPickupStore.class.getName());
    }
}
