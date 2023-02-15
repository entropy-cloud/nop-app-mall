
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallIssue;

@BizModel("LitemallIssue")
public class LitemallIssueBizModel extends CrudBizModel<LitemallIssue>{
    public LitemallIssueBizModel(){
        setEntityName(LitemallIssue.class.getName());
    }
}
