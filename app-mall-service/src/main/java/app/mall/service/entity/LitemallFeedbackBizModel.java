
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallFeedbackBiz;

import app.mall.dao.entity.LitemallFeedback;

@BizModel("LitemallFeedback")
public class LitemallFeedbackBizModel extends CrudBizModel<LitemallFeedback> implements ILitemallFeedbackBiz {
    public LitemallFeedbackBizModel(){
        setEntityName(LitemallFeedback.class.getName());
    }
}
