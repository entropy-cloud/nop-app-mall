
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallAd;

@BizModel("LitemallAd")
public class LitemallAdBizModel extends CrudBizModel<LitemallAd>{
    public LitemallAdBizModel(){
        setEntityName(LitemallAd.class.getName());
    }
}
