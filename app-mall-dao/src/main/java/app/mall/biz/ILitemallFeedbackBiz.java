package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallFeedback;

public interface ILitemallFeedbackBiz extends ICrudBiz<LitemallFeedback> {

    @BizMutation
    LitemallFeedback submitFeedback(@Name("feedType") String feedType,
                                    @Name("content") String content,
                                    @Optional @Name("hasPicture") Boolean hasPicture,
                                    @Optional @Name("picUrls") String picUrls,
                                    @Optional @Name("mobile") String mobile,
                                    IServiceContext context);
}
