
package app.mall.service.entity;

import app.mall.biz.ILitemallUserMessageBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallUserMessage;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

import static app.mall.service.AppMallErrors.ERR_MESSAGE_NOT_BELONG_USER;
import static app.mall.service.AppMallErrors.ERR_MESSAGE_NOT_FOUND;

@BizModel("LitemallUserMessage")
public class LitemallUserMessageBizModel extends CrudBizModel<LitemallUserMessage> implements ILitemallUserMessageBiz {
    static final Logger LOG = LoggerFactory.getLogger(LitemallUserMessageBizModel.class);

    private static final int DEFAULT_PAGE_SIZE = 20;

    // Platform user status: 0=disabled (banned), 1=enabled (normal/active). broadcast targets active users.
    private static final int USER_STATUS_ENABLED = 1;

    public LitemallUserMessageBizModel() {
        setEntityName(LitemallUserMessage.class.getName());
    }

    @Override
    @BizQuery
    public PageBean<LitemallUserMessage> getMyMessages(@Optional @Name("msgType") Integer msgType,
                                                       @Optional @Name("page") Integer page,
                                                       IServiceContext context) {
        String userId = context.getUserId();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_userId, userId));
        if (msgType != null) {
            query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_msgType, msgType));
        }
        query.addOrderField(LitemallUserMessage.PROP_NAME_addTime, true);
        int pageNo = page == null || page < 1 ? 1 : page;
        query.setOffset((pageNo - 1) * DEFAULT_PAGE_SIZE);
        query.setLimit(DEFAULT_PAGE_SIZE);
        return findPage(query, null, context);
    }

    @Override
    @BizQuery
    public int getUnreadCount(@Optional @Name("msgType") Integer msgType,
                              IServiceContext context) {
        String userId = context.getUserId();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_isRead, false));
        if (msgType != null) {
            query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_msgType, msgType));
        }
        return (int) findCount(query, context);
    }

    @Override
    @BizMutation
    public void markRead(@Name("messageId") String messageId,
                         IServiceContext context) {
        LitemallUserMessage message = requireOwnMessage(messageId, context);
        if (Boolean.TRUE.equals(message.getIsRead())) {
            return;
        }
        message.setIsRead(true);
        message.setReadTime(LocalDateTime.now());
        updateEntity(message, "markRead", context);
    }

    @Override
    @BizMutation
    public int markAllRead(@Optional @Name("msgType") Integer msgType,
                           IServiceContext context) {
        String userId = context.getUserId();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_isRead, false));
        if (msgType != null) {
            query.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_msgType, msgType));
        }
        List<LitemallUserMessage> unread = findList(query, null, context);
        LocalDateTime now = LocalDateTime.now();
        for (LitemallUserMessage message : unread) {
            message.setIsRead(true);
            message.setReadTime(now);
            updateEntity(message, "markAllRead", context);
        }
        return unread.size();
    }

    @Override
    @BizMutation
    public void deleteMessage(@Name("messageId") String messageId,
                              IServiceContext context) {
        LitemallUserMessage message = requireOwnMessage(messageId, context);
        deleteEntity(message, "deleteMessage", context);
    }

    @Override
    @BizQuery
    public LitemallUserMessage getMessageDetail(@Name("messageId") String messageId,
                                                IServiceContext context) {
        LitemallUserMessage message = requireOwnMessage(messageId, context);
        // First view of an unread message marks it read in the same transaction.
        if (!Boolean.TRUE.equals(message.getIsRead())) {
            message.setIsRead(true);
            message.setReadTime(LocalDateTime.now());
            updateEntity(message, "getMessageDetail:markRead", context);
        }
        return message;
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public int broadcastSystemMessage(@Name("title") String title,
                                      @Name("content") String content,
                                      IServiceContext context) {
        if (StringHelper.isEmpty(title)) {
            throw new NopException(ERR_MESSAGE_NOT_FOUND).param("userId", context.getUserId());
        }
        // Phase 1 Decision 4: write one SYSTEM message per active user. NopAuthUser is a platform entity
        // with no runtime app I*Biz in app-mall-service, so per AGENTS.md we use the CrudBizModel
        // daoProvider() fallback (same pattern as LitemallMemberLevelBizModel) to list active users.
        QueryBean userQuery = new QueryBean();
        userQuery.addFilter(FilterBeans.eq(NopAuthUser.PROP_NAME_status, USER_STATUS_ENABLED));
        List<NopAuthUser> activeUsers = daoProvider().daoFor(NopAuthUser.class).findAllByQuery(userQuery);
        int count = 0;
        for (NopAuthUser user : activeUsers) {
            String uid = user.getUserId();
            if (StringHelper.isEmpty(uid)) {
                continue;
            }
            LitemallUserMessage message = newEntity();
            message.setUserId(uid);
            message.setMsgType(_AppMallDaoConstants.MSG_TYPE_SYSTEM);
            message.setTitle(title);
            message.setContent(content);
            message.setIsRead(false);
            saveEntity(message, "broadcastSystemMessage", context);
            count++;
        }
        LOG.info("broadcastSystemMessage: delivered to {} active users, title={}", count, title);
        return count;
    }

    @Override
    @BizMutation
    public LitemallUserMessage sendUserMessage(@Name("userId") String userId,
                                               @Name("msgType") Integer msgType,
                                               @Name("title") String title,
                                               @Name("content") String content,
                                               IServiceContext context) {
        if (StringHelper.isEmpty(userId)) {
            return null;
        }
        LitemallUserMessage message = newEntity();
        message.setUserId(userId);
        message.setMsgType(msgType != null ? msgType : _AppMallDaoConstants.MSG_TYPE_ORDER);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(false);
        saveEntity(message, "sendUserMessage", context);
        return message;
    }

    private LitemallUserMessage requireOwnMessage(String messageId, IServiceContext context) {
        if (StringHelper.isEmpty(messageId)) {
            throw new NopException(ERR_MESSAGE_NOT_FOUND).param("userId", context.getUserId());
        }
        LitemallUserMessage message = get(messageId, false, context);
        if (message == null || Boolean.TRUE.equals(message.getDeleted())) {
            throw new NopException(ERR_MESSAGE_NOT_FOUND).param("userId", context.getUserId());
        }
        if (!String.valueOf(context.getUserId()).equals(message.getUserId())) {
            throw new NopException(ERR_MESSAGE_NOT_BELONG_USER).param("userId", context.getUserId());
        }
        return message;
    }
}
