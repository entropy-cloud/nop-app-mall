
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallGoods;

@BizModel("LitemallGoods")
public class LitemallGoodsBizModel extends CrudBizModel<LitemallGoods>{
    public LitemallGoodsBizModel(){
        setEntityName(LitemallGoods.class.getName());
    }
}
