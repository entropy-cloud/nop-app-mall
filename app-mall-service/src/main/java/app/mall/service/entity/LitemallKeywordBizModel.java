
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallKeyword;

@BizModel("LitemallKeyword")
public class LitemallKeywordBizModel extends CrudBizModel<LitemallKeyword>{
    public LitemallKeywordBizModel(){
        setEntityName(LitemallKeyword.class.getName());
    }
}
