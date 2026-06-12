package app.mall.service.entity;

import app.mall.biz.ILitemallFeedbackBiz;
import app.mall.dao.entity.LitemallFeedback;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.context.ContextProvider;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

@BizModel("LitemallFeedback")
public class LitemallFeedbackBizModel extends CrudBizModel<LitemallFeedback> implements ILitemallFeedbackBiz {

    public LitemallFeedbackBizModel() {
        setEntityName(LitemallFeedback.class.getName());
    }

    @Override
    @BizMutation
    public LitemallFeedback submitFeedback(@Name("feedType") String feedType,
                                            @Name("content") String content,
                                            @Optional @Name("hasPicture") Boolean hasPicture,
                                            @Optional @Name("picUrls") String picUrls,
                                            @Optional @Name("mobile") String mobile,
                                            IServiceContext context) {
        LitemallFeedback feedback = newEntity();
        feedback.setUserId(context.getUserId());
        feedback.setUsername(ContextProvider.getOrCreateContext().getUserName());
        feedback.setFeedType(feedType);
        feedback.setContent(content);
        feedback.setHasPicture(Boolean.TRUE.equals(hasPicture));
        if (picUrls != null) {
            feedback.setPicUrls(picUrls);
        }
        if (mobile != null) {
            feedback.setMobile(mobile);
        }
        feedback.setStatus(0);
        saveEntity(feedback, null, context);
        return feedback;
    }
}
