
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallSearchHistory;

@BizModel("LitemallSearchHistory")
public class LitemallSearchHistoryBizModel extends CrudBizModel<LitemallSearchHistory>{
    public LitemallSearchHistoryBizModel(){
        setEntityName(LitemallSearchHistory.class.getName());
    }
}
