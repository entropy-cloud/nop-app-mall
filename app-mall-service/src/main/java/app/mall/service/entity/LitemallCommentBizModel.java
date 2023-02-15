
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallComment;

@BizModel("LitemallComment")
public class LitemallCommentBizModel extends CrudBizModel<LitemallComment>{
    public LitemallCommentBizModel(){
        setEntityName(LitemallComment.class.getName());
    }
}
