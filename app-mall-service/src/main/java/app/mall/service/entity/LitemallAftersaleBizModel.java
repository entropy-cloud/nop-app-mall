
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallAftersale;

@BizModel("LitemallAftersale")
public class LitemallAftersaleBizModel extends CrudBizModel<LitemallAftersale>{
    public LitemallAftersaleBizModel(){
        setEntityName(LitemallAftersale.class.getName());
    }
}
