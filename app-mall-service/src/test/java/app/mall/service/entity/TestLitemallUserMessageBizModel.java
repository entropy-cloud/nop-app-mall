package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallSystem;
import app.mall.dao.entity.LitemallUserMessage;
import app.mall.service.notification.MallNotificationService;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.auth.core.login.UserContextImpl;
import io.nop.api.core.auth.IUserContext;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.core.context.ServiceContextImpl;
import io.nop.dao.api.IDaoProvider;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallUserMessageBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    MallNotificationService notificationService;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("0");
        ContextProvider.getOrCreateContext().setUserName("admin");
    }

    private ApiResponse<?> rpc(GraphQLOperationType op, String action, Map<String, Object> data) {
        return graphQLEngine.executeRpc(
                graphQLEngine.newRpcContext(op, action, ApiRequest.build(data)));
    }

    private NopAuthUser signUpUser(String username, String mobile) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("password", "Pass@1234");
        data.put("mobile", mobile);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LoginApi__signUp", data);
        assertEquals(0, r.getStatus(), "signUp helper failed: " + r.getMsg());
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq("userName", username));
        return (NopAuthUser) daoProvider.daoFor(NopAuthUser.class)
                .findAllByQuery(query).stream().findFirst().orElse(null);
    }

    private void actAs(NopAuthUser user) {
        ContextProvider.getOrCreateContext().setUserId(user.getUserId());
        ContextProvider.getOrCreateContext().setUserName(user.getUserName());
        UserContextImpl userContext = new UserContextImpl();
        userContext.setUserId(user.getUserId());
        userContext.setUserName(user.getUserName());
        IUserContext.set(userContext);
    }

    private String seedMessage(String userId, int msgType, String title, boolean read) {
        LitemallUserMessage msg = daoProvider.daoFor(LitemallUserMessage.class).newEntity();
        msg.orm_propValueByName(LitemallUserMessage.PROP_NAME_userId, userId);
        msg.orm_propValueByName(LitemallUserMessage.PROP_NAME_msgType, msgType);
        msg.orm_propValueByName(LitemallUserMessage.PROP_NAME_title, title);
        msg.orm_propValueByName(LitemallUserMessage.PROP_NAME_content, title + "-content");
        msg.orm_propValueByName(LitemallUserMessage.PROP_NAME_isRead, read);
        daoProvider.daoFor(LitemallUserMessage.class).saveEntity(msg);
        return msg.orm_idString();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> myList(Integer msgType) {
        Map<String, Object> data = new HashMap<>();
        if (msgType != null) {
            data.put("msgType", msgType);
        }
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallUserMessage__getMyMessages", data);
        assertEquals(0, r.getStatus(), "getMyMessages failed: " + r);
        return (List<Map<String, Object>>) ((Map<String, Object>) r.getData()).get("items");
    }

    private int unreadCount(Integer msgType) {
        Map<String, Object> data = new HashMap<>();
        if (msgType != null) {
            data.put("msgType", msgType);
        }
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallUserMessage__getUnreadCount", data);
        assertEquals(0, r.getStatus(), "getUnreadCount failed: " + r);
        return ((Number) r.getData()).intValue();
    }

    // Paid-order seeder (same shape as TestLitemallUserPortraitBizModel) so RFM/lifecycle
    // algorithmic segments are non-empty for the segment-directed-marketing push tests.
    private void createPaidOrder(String userId, String sn, BigDecimal actualPrice, LocalDateTime payTime) {
        LitemallOrder o = daoProvider.daoFor(LitemallOrder.class).newEntity();
        o.setUserId(userId);
        o.setOrderSn(sn);
        o.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_PAY);
        o.setConsignee("收货人");
        o.setMobile("13800138000");
        o.setAddress("测试地址");
        o.setMessage("segment-push-test-order");
        o.setGoodsPrice(actualPrice);
        o.setFreightPrice(BigDecimal.ZERO);
        o.setCouponPrice(BigDecimal.ZERO);
        o.setIntegralPrice(BigDecimal.ZERO);
        o.setGrouponPrice(BigDecimal.ZERO);
        o.setOrderPrice(actualPrice);
        o.setActualPrice(actualPrice);
        o.setPromotionPrice(BigDecimal.ZERO);
        o.setPinTuanPrice(BigDecimal.ZERO);
        o.setPayTime(payTime);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(o);
    }

    private void actAsAdmin() {
        ContextProvider.getOrCreateContext().setUserId("0");
        ContextProvider.getOrCreateContext().setUserName("admin");
    }

    @Test
    public void testMyMessagesUnreadAndDetailAutoRead() {
        NopAuthUser user = signUpUser("msguser1", "13900001001");
        actAs(user);
        seedMessage(user.getUserId(), _AppMallDaoConstants.MSG_TYPE_ORDER, "订单-已读", true);
        seedMessage(user.getUserId(), _AppMallDaoConstants.MSG_TYPE_ORDER, "订单-未读", false);
        seedMessage(user.getUserId(), _AppMallDaoConstants.MSG_TYPE_SYSTEM, "系统-未读", false);

        // unread across all types = 2; ORDER-only unread = 1
        assertEquals(2, unreadCount(null));
        assertEquals(1, unreadCount(_AppMallDaoConstants.MSG_TYPE_ORDER));

        // list returns all 3, newest first
        List<Map<String, Object>> all = myList(null);
        assertEquals(3, all.size());

        // ORDER filter returns 2
        assertEquals(2, myList(_AppMallDaoConstants.MSG_TYPE_ORDER).size());

        // detail of an unread message marks it read
        String unreadId = all.stream()
                .filter(m -> "系统-未读".equals(m.get("title")))
                .map(m -> String.valueOf(m.get("id")))
                .findFirst().orElse(null);
        assertNotNull(unreadId);
        Map<String, Object> detailData = new HashMap<>();
        detailData.put("messageId", unreadId);
        ApiResponse<?> detail = rpc(GraphQLOperationType.query, "LitemallUserMessage__getMessageDetail", detailData);
        assertEquals(0, detail.getStatus(), "getMessageDetail failed: " + detail);
        assertEquals(true, ((Map<String, Object>) detail.getData()).get("isRead"));
        // unread SYSTEM now 0
        assertEquals(0, unreadCount(_AppMallDaoConstants.MSG_TYPE_SYSTEM));
    }

    @Test
    public void testMarkReadOwnershipGuard() {
        NopAuthUser owner = signUpUser("msgowner", "13900001002");
        NopAuthUser intruder = signUpUser("msgintruder", "13900001003");
        String messageId = seedMessage(owner.getUserId(), _AppMallDaoConstants.MSG_TYPE_ORDER, "属于owner", false);

        // intruder cannot markRead owner's message
        actAs(intruder);
        Map<String, Object> data = new HashMap<>();
        data.put("messageId", messageId);
        ApiResponse<?> denied = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__markRead", data);
        assertNotEquals(0, denied.getStatus(), "non-owner markRead must be rejected");

        // owner can markRead
        actAs(owner);
        ApiResponse<?> ok = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__markRead", data);
        assertEquals(0, ok.getStatus(), "owner markRead failed: " + ok);
        assertEquals(0, unreadCount(null));
    }

    @Test
    public void testMarkAllRead() {
        NopAuthUser user = signUpUser("msgallread", "13900001004");
        actAs(user);
        seedMessage(user.getUserId(), _AppMallDaoConstants.MSG_TYPE_ORDER, "a", false);
        seedMessage(user.getUserId(), _AppMallDaoConstants.MSG_TYPE_ORDER, "b", false);
        seedMessage(user.getUserId(), _AppMallDaoConstants.MSG_TYPE_SYSTEM, "c", false);
        assertEquals(3, unreadCount(null));

        // markAllRead scoped to ORDER clears only ORDER unread (2), leaves SYSTEM (1)
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("msgType", _AppMallDaoConstants.MSG_TYPE_ORDER);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__markAllRead", orderData);
        assertEquals(0, r.getStatus(), "markAllRead failed: " + r);
        assertEquals(2, ((Number) r.getData()).intValue());
        assertEquals(1, unreadCount(null));

        // markAllRead all clears the rest
        ApiResponse<?> r2 = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__markAllRead", new HashMap<>());
        assertEquals(0, r2.getStatus());
        assertEquals(0, unreadCount(null));
    }

    @Test
    public void testDeleteMessageOwnership() {
        NopAuthUser owner = signUpUser("msgdelowner", "13900001005");
        NopAuthUser intruder = signUpUser("msgdelintruder", "13900001006");
        String messageId = seedMessage(owner.getUserId(), _AppMallDaoConstants.MSG_TYPE_ORDER, "to-delete", false);

        actAs(intruder);
        Map<String, Object> data = new HashMap<>();
        data.put("messageId", messageId);
        ApiResponse<?> denied = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__deleteMessage", data);
        assertNotEquals(0, denied.getStatus(), "non-owner delete must be rejected");

        actAs(owner);
        ApiResponse<?> ok = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__deleteMessage", data);
        assertEquals(0, ok.getStatus(), "owner delete failed: " + ok);
        assertEquals(0, myList(null).size(), "deleted message must not appear in list");
    }

    @Test
    public void testBroadcastSystemMessageDeliversToActiveUsers() {
        NopAuthUser user = signUpUser("msgbcast", "13900001007");
        // admin context (setUp). broadcast must deliver to the active signed-up user.
        Map<String, Object> data = new HashMap<>();
        data.put("title", "系统公告-开业大促");
        data.put("content", "全场满减，不容错过");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__broadcastSystemMessage", data);
        assertEquals(0, r.getStatus(), "broadcastSystemMessage failed: " + r);
        int delivered = ((Number) r.getData()).intValue();
        assertTrue(delivered >= 1, "broadcast should deliver to at least one active user");

        // the active user sees the SYSTEM message
        actAs(user);
        List<Map<String, Object>> systemMsgs = myList(_AppMallDaoConstants.MSG_TYPE_SYSTEM);
        assertTrue(systemMsgs.stream().anyMatch(m -> "系统公告-开业大促".equals(m.get("title"))),
                "active user should receive the broadcast SYSTEM message");
    }

    @Test
    public void testEventChannelWritesUserMessage() {
        NopAuthUser user = signUpUser("msgevent", "13900001009");
        // Simulate the payment-success event channel: the notification method writes an ORDER站内信.
        // (afterCommit timing is infrastructure; calling the method directly validates the write path.)
        notificationService.sendOrderPaymentNotification("ORD123456789", "13900001009", user.getUserId());

        actAs(user);
        List<Map<String, Object>> orderMsgs = myList(_AppMallDaoConstants.MSG_TYPE_ORDER);
        assertTrue(orderMsgs.stream().anyMatch(m -> "支付成功".equals(m.get("title"))),
                "payment event should produce an ORDER站内信");
        assertTrue(unreadCount(_AppMallDaoConstants.MSG_TYPE_ORDER) >= 1);
    }

    @Test
    public void testEventToggleDefaultEnabledAndDisabled() {
        NopAuthUser user = signUpUser("msgtoggle", "13900001010");
        // default: no config → enabled
        assertEquals(true, notificationService.isEventMessageEnabled("payment", new ServiceContextImpl()));

        // set config = false → disabled
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName(LitemallSystem.PROP_NAME_keyName, "mall_message_event_enabled_payment");
        cfg.orm_propValueByName(LitemallSystem.PROP_NAME_keyValue, "false");
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
        daoProvider.daoFor(LitemallSystem.class).flushSession();
        assertEquals(false, notificationService.isEventMessageEnabled("payment", new ServiceContextImpl()));
    }

    // ===== 分群定向营销推送（successor of P35 deferred「MARKETING 定向投放」） =====

    @Test
    public void testSendSegmentMessageByTag() {
        NopAuthUser user = signUpUser("segpushtag", "13900002002");
        actAsAdmin();
        // Seed a manual tag on the user via the admin addUserTag mutation.
        Map<String, Object> tagData = new HashMap<>();
        tagData.put("userId", user.getUserId());
        tagData.put("tag", "vip-customer");
        tagData.put("name", "VIP客户");
        ApiResponse<?> tr = rpc(GraphQLOperationType.mutation, "LitemallUserTag__addUserTag", tagData);
        assertEquals(0, tr.getStatus(), "addUserTag helper failed: " + tr);

        Map<String, Object> data = new HashMap<>();
        data.put("segmentType", "tag");
        data.put("segmentValue", "vip-customer");
        data.put("title", "标签定向推送");
        data.put("content", "会员专享优惠");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__sendSegmentMessage", data);
        assertEquals(0, r.getStatus(), "sendSegmentMessage(tag) failed: " + r);
        assertEquals(1, ((Number) r.getData()).intValue(),
                "tag push should deliver to exactly the 1 tagged user");

        // The tagged user sees a MARKETING message.
        actAs(user);
        List<Map<String, Object>> mktMsgs = myList(_AppMallDaoConstants.MSG_TYPE_MARKETING);
        assertTrue(mktMsgs.stream().anyMatch(m -> "标签定向推送".equals(m.get("title"))),
                "tagged user should receive the MARKETING message");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSendSegmentMessageByRfm() {
        NopAuthUser user = signUpUser("segpushrfm", "13900002001");
        actAsAdmin();
        createPaidOrder(user.getUserId(), "SN-SEG-PUSH-RFM", new BigDecimal("100"),
                LocalDateTime.now().minusDays(1));

        // Determine the actual RFM segment the seeded paying user falls into (threshold-based, so
        // query the portrait rather than hard-coding a segment label).
        Map<String, Object> portraitData = new HashMap<>();
        portraitData.put("userId", user.getUserId());
        ApiResponse<?> pr = rpc(GraphQLOperationType.query, "LitemallOrder__getUserPortrait", portraitData);
        assertEquals(0, pr.getStatus(), "getUserPortrait helper failed: " + pr);
        String segment = (String) ((Map<String, Object>) pr.getData()).get("rfmSegment");
        assertNotNull(segment, "paid user must be assigned an RFM segment");

        Map<String, Object> data = new HashMap<>();
        data.put("segmentType", "rfm");
        data.put("segmentValue", segment);
        data.put("title", "RFM定向推送");
        data.put("content", "专享回归优惠");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__sendSegmentMessage", data);
        assertEquals(0, r.getStatus(), "sendSegmentMessage(rfm) failed: " + r);
        assertTrue(((Number) r.getData()).intValue() >= 1,
                "rfm push should deliver to at least the seeded paying user");

        actAs(user);
        List<Map<String, Object>> mktMsgs = myList(_AppMallDaoConstants.MSG_TYPE_MARKETING);
        assertTrue(mktMsgs.stream().anyMatch(m -> "RFM定向推送".equals(m.get("title"))),
                "rfm-segment user should receive the MARKETING message");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSendSegmentMessageByLifecycle() {
        NopAuthUser user = signUpUser("segpushlife", "13900002003");
        actAsAdmin();
        createPaidOrder(user.getUserId(), "SN-SEG-PUSH-LIFE", new BigDecimal("50"),
                LocalDateTime.now().minusDays(1));

        Map<String, Object> portraitData = new HashMap<>();
        portraitData.put("userId", user.getUserId());
        ApiResponse<?> pr = rpc(GraphQLOperationType.query, "LitemallOrder__getUserPortrait", portraitData);
        assertEquals(0, pr.getStatus(), "getUserPortrait helper failed: " + pr);
        String stage = (String) ((Map<String, Object>) pr.getData()).get("lifecycleStage");
        assertNotNull(stage, "paid user must be assigned a lifecycle stage");

        Map<String, Object> data = new HashMap<>();
        data.put("segmentType", "lifecycle");
        data.put("segmentValue", stage);
        data.put("title", "生命周期定向推送");
        data.put("content", "专属唤醒礼");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__sendSegmentMessage", data);
        assertEquals(0, r.getStatus(), "sendSegmentMessage(lifecycle) failed: " + r);
        assertTrue(((Number) r.getData()).intValue() >= 1,
                "lifecycle push should deliver to at least the seeded paying user");

        actAs(user);
        List<Map<String, Object>> mktMsgs = myList(_AppMallDaoConstants.MSG_TYPE_MARKETING);
        assertTrue(mktMsgs.stream().anyMatch(m -> "生命周期定向推送".equals(m.get("title"))),
                "lifecycle-segment user should receive the MARKETING message");
    }

    @Test
    public void testSendSegmentMessageEmptySegmentReturnsZero() {
        actAsAdmin();
        // A tag nobody carries → empty segment → delivered 0, not an error (Decision D3).
        Map<String, Object> data = new HashMap<>();
        data.put("segmentType", "tag");
        data.put("segmentValue", "nonexistent-tag-xyz");
        data.put("title", "空分群推送");
        data.put("content", "无人送达");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__sendSegmentMessage", data);
        assertEquals(0, r.getStatus(), "empty segment push must succeed");
        assertEquals(0, ((Number) r.getData()).intValue(), "empty segment must deliver 0");
    }

    @Test
    public void testSendSegmentMessageInvalidTypeRejected() {
        actAsAdmin();
        Map<String, Object> data = new HashMap<>();
        data.put("segmentType", "unknown");
        data.put("segmentValue", "x");
        data.put("title", "T");
        data.put("content", "C");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__sendSegmentMessage", data);
        assertNotEquals(0, r.getStatus(), "invalid segmentType must be rejected");
    }

    @Test
    public void testSendSegmentMessageEmptyTitleRejected() {
        actAsAdmin();
        Map<String, Object> data = new HashMap<>();
        data.put("segmentType", "tag");
        data.put("segmentValue", "vip");
        data.put("title", "");
        data.put("content", "C");
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallUserMessage__sendSegmentMessage", data);
        assertNotEquals(0, r.getStatus(), "empty title must be rejected");
    }
}
