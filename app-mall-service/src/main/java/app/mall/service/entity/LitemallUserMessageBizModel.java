package app.mall.service.entity;

import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallUserMessageBiz;
import app.mall.biz.ILitemallUserTagBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.dto.SegmentMemberBean;
import app.mall.dao.entity.LitemallUserMessage;
import app.mall.dao.entity.LitemallUserTag;
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
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static app.mall.service.AppMallErrors.ARG_SEGMENT_TYPE;
import static app.mall.service.AppMallErrors.ARG_SEGMENT_VALUE;
import static app.mall.service.AppMallErrors.ERR_MESSAGE_NOT_BELONG_USER;
import static app.mall.service.AppMallErrors.ERR_MESSAGE_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_MESSAGE_SEGMENT_TITLE_OR_CONTENT_EMPTY;
import static app.mall.service.AppMallErrors.ERR_USER_PORTRAIT_INVALID_SEGMENT_TYPE;
import static app.mall.service.AppMallErrors.ERR_USER_PORTRAIT_INVALID_SEGMENT_VALUE;

@BizModel("LitemallUserMessage")
public class LitemallUserMessageBizModel extends CrudBizModel<LitemallUserMessage> implements ILitemallUserMessageBiz {
    static final Logger LOG = LoggerFactory.getLogger(LitemallUserMessageBizModel.class);

    private static final int DEFAULT_PAGE_SIZE = 20;

    // Platform user status: 0=disabled (banned), 1=enabled (normal/active). broadcast targets active users.
    private static final int USER_STATUS_ENABLED = 1;

    // Segment-directed-marketing successor: full-set segment member resolution goes through
    // ILitemallOrderBiz.collectRfmLifecycleMatches (rfm/lifecycle) and ILitemallUserTagBiz (tag),
    // so the push action and the segment query page share one resolution path (no口径分叉).
    // NopIoC resolves the OrderBizModel→MallNotificationService→this→OrderBizModel cycle via
    // early-singleton reference (same mechanism as the 4 existing Order↔BizModel cycles).
    @Inject
    ILitemallOrderBiz orderBiz;

    @Inject
    ILitemallUserTagBiz userTagBiz;

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
    @Auth(roles = "admin")
    public int sendSegmentMessage(@Name("segmentType") String segmentType,
                                  @Name("segmentValue") String segmentValue,
                                  @Name("title") String title,
                                  @Name("content") String content,
                                  IServiceContext context) {
        if (StringHelper.isEmpty(title) || StringHelper.isEmpty(content)) {
            throw new NopException(ERR_MESSAGE_SEGMENT_TITLE_OR_CONTENT_EMPTY);
        }
        String normalizedType = segmentType == null ? "" : segmentType.trim().toLowerCase();
        boolean byTag = "tag".equals(normalizedType);
        boolean byRfm = "rfm".equals(normalizedType);
        boolean byLifecycle = "lifecycle".equals(normalizedType);
        if (!byTag && !byRfm && !byLifecycle) {
            throw new NopException(ERR_USER_PORTRAIT_INVALID_SEGMENT_TYPE)
                    .param(ARG_SEGMENT_TYPE, segmentType);
        }
        if (StringHelper.isEmpty(segmentValue)) {
            throw new NopException(ERR_USER_PORTRAIT_INVALID_SEGMENT_VALUE)
                    .param(ARG_SEGMENT_VALUE, segmentValue);
        }

        // Resolve the full set of target userIds. tag → manual-tag list (dedup by userId);
        // rfm/lifecycle → algorithmic full-set resolution (same path as getSegmentMembers, only paying
        // users with a payment history are classified). Empty segment → empty set → delivered 0 (D3).
        Set<String> userIds;
        if (byTag) {
            QueryBean tagQuery = new QueryBean();
            tagQuery.addFilter(FilterBeans.eq(LitemallUserTag.PROP_NAME_tag, segmentValue));
            List<LitemallUserTag> tags = userTagBiz.findList(tagQuery, null, context);
            userIds = new LinkedHashSet<>();
            for (LitemallUserTag t : tags) {
                if (!StringHelper.isEmpty(t.getUserId())) {
                    userIds.add(t.getUserId());
                }
            }
        } else {
            List<SegmentMemberBean> members = orderBiz.collectRfmLifecycleMatches(segmentValue, byRfm, context);
            userIds = new LinkedHashSet<>();
            for (SegmentMemberBean m : members) {
                if (!StringHelper.isEmpty(m.getUserId())) {
                    userIds.add(m.getUserId());
                }
            }
        }

        int count = 0;
        for (String uid : userIds) {
            LitemallUserMessage message = newEntity();
            message.setUserId(uid);
            message.setMsgType(_AppMallDaoConstants.MSG_TYPE_MARKETING);
            message.setTitle(title);
            message.setContent(content);
            message.setIsRead(false);
            saveEntity(message, "sendSegmentMessage", context);
            count++;
        }
        LOG.info("sendSegmentMessage: delivered to {} users, segmentType={}, segmentValue={}, title={}",
                count, segmentType, segmentValue, title);
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
