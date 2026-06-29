
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

    /**
     * 分群定向营销推送（successor of P35 deferred「MARKETING 定向投放」）。选定一个分群
     * （{@code segmentType=tag|rfm|lifecycle}）+ 段值后，向该分群的**全部**匹配成员逐人写入一条
     * MARKETING 站内信（{@code MSG_TYPE_MARKETING}），返回送达人数。空分群返回 0 不抛错；
     * 非法 segmentType / 空 title-content 拒绝。成员解析复用既有能力（手工标签走
     * {@code LitemallUserTag}、RFM/生命周期走算法化分类），与分群查询页同源口径。
     * 口径见 docs/design/system-configuration.md「消息中心」与 docs/design/marketing-and-promotions.md。
     */
    @BizMutation
    @Auth(roles = "admin")
    int sendSegmentMessage(@Name("segmentType") String segmentType,
                           @Name("segmentValue") String segmentValue,
                           @Name("title") String title,
                           @Name("content") String content,
                           IServiceContext context);

    @BizMutation
    LitemallUserMessage sendUserMessage(@Name("userId") String userId,
                                        @Name("msgType") Integer msgType,
                                        @Name("title") String title,
                                        @Name("content") String content,
                                        IServiceContext context);
}
