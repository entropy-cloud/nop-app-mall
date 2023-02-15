
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallOrderGoods;

@BizModel("LitemallOrderGoods")
public class LitemallOrderGoodsBizModel extends CrudBizModel<LitemallOrderGoods>{
    public LitemallOrderGoodsBizModel(){
        setEntityName(LitemallOrderGoods.class.getName());
    }
}
