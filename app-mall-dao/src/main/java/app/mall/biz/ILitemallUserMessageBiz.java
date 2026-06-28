
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallUserMessage;

public interface ILitemallUserMessageBiz extends ICrudBiz<LitemallUserMessage> {

    @BizQuery
    PageBean<LitemallUserMessage> getMyMessages(@Optional @Name("msgType") Integer msgType,
                                                @Optional @Name("page") Integer page,
                                                IServiceContext context);

    @BizQuery
    int getUnreadCount(@Optional @Name("msgType") Integer msgType,
                       IServiceContext context);

    @BizMutation
    void markRead(@Name("messageId") String messageId,
                  IServiceContext context);

    @BizMutation
    int markAllRead(@Optional @Name("msgType") Integer msgType,
                    IServiceContext context);

    @BizMutation
    void deleteMessage(@Name("messageId") String messageId,
                       IServiceContext context);

    @BizQuery
    LitemallUserMessage getMessageDetail(@Name("messageId") String messageId,
                                         IServiceContext context);

    @BizMutation
    @Auth(roles = "admin")
    int broadcastSystemMessage(@Name("title") String title,
                               @Name("content") String content,
                               IServiceContext context);

    @BizMutation
    LitemallUserMessage sendUserMessage(@Name("userId") String userId,
                                        @Name("msgType") Integer msgType,
                                        @Name("title") String title,
                                        @Name("content") String content,
                                        IServiceContext context);
}
