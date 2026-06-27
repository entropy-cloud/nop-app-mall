package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.entity.LitemallPinTuanActivity;
import app.mall.dao.entity.LitemallPinTuanGroup;
import app.mall.dao.entity.LitemallPinTuanMember;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.file.dao.entity.NopFileRecord;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallPinTuanActivityBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String goodsId;
    String productId;
    String activityId;

    private void createFileRecord(String fileId, String bizObjName, String fieldName) {
        NopFileRecord record = daoProvider.daoFor(NopFileRecord.class).newEntity();
        record.setFileId(fileId);
        record.setBizObjName(bizObjName);
        record.setBizObjId("temp");
        record.setFieldName(fieldName);
        record.setOriginFileId(fileId);
        record.setFileName(fileId + ".png");
        record.setFilePath("/test/" + fileId + ".png");
        record.setFileExt("png");
        record.setMimeType("image/png");
        record.setIsPublic(true);
        daoProvider.daoFor(NopFileRecord.class).saveEntity(record);
    }

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        createFileRecord("pintuan-goods-pic", "LitemallOrderGoods", "picUrl");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("PT001");
        goods.setName("拼团测试商品");
        goods.setRetailPrice(new BigDecimal("100.00"));
        goods.setIsOnSale(true);
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.orm_idString();

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goodsId);
        product.setSpecifications("[\"标准\"]");
        product.setPrice(new BigDecimal("100.00"));
        product.setNumber(100);
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);
        productId = product.orm_idString();

        LitemallPinTuanActivity activity = daoProvider.daoFor(LitemallPinTuanActivity.class).newEntity();
        activity.setGoodsId(goodsId);
        activity.setPinTuanPrice(new BigDecimal("80.00"));
        activity.setMinUserCount(3);
        activity.setMaxUserCount(3);
        activity.setExpireHours(24);
        activity.setStatus(10);
        daoProvider.daoFor(LitemallPinTuanActivity.class).saveEntity(activity);
        activityId = activity.orm_idString();
    }

    private String createOrderWithGoods(String userId, String orderSn) {
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).newEntity();
        order.setUserId(userId);
        order.setOrderSn(orderSn);
        order.setOrderStatus(101);
        order.setAftersaleStatus(0);
        order.setConsignee("测试用户" + userId);
        order.setMobile("1380013800" + userId);
        order.setAddress("测试地址" + userId);
        order.setMessage("pintuan-test");
        order.setGoodsPrice(new BigDecimal("100.00"));
        order.setFreightPrice(BigDecimal.ZERO);
        order.setCouponPrice(BigDecimal.ZERO);
        order.setIntegralPrice(BigDecimal.ZERO);
        order.setGrouponPrice(BigDecimal.ZERO);
        order.setPromotionPrice(BigDecimal.ZERO);
        order.setPinTuanPrice(BigDecimal.ZERO);
        order.setOrderPrice(new BigDecimal("100.00"));
        order.setActualPrice(new BigDecimal("100.00"));
        order.setComments(0);
        order.setDeleted(false);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(order);

        LitemallOrderGoods orderGoods = daoProvider.daoFor(LitemallOrderGoods.class).newEntity();
        orderGoods.setOrderId(order.orm_idString());
        orderGoods.setGoodsId(goodsId);
        orderGoods.setGoodsName("拼团测试商品");
        orderGoods.setGoodsSn("PT001");
        orderGoods.setProductId(productId);
        orderGoods.setNumber(1);
        orderGoods.setPrice(new BigDecimal("100.00"));
        orderGoods.setSpecifications("[\"标准\"]");
        orderGoods.setPicUrl("/f/download/pintuan-goods-pic");
        orderGoods.setComment(0);
        daoProvider.daoFor(LitemallOrderGoods.class).saveEntity(orderGoods);

        return order.orm_idString();
    }

    @SuppressWarnings("unchecked")
    private String openGroup(String userId, String orderId) {
        ContextProvider.getOrCreateContext().setUserId(userId);
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "pinTuanActivityId", activityId,
                "orderId", orderId
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__openPinTuan", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "openPinTuan failed: " + result);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0, ((Number) data.get("status")).intValue());
        assertEquals(userId, data.get("creatorUserId"));
        return (String) data.get("id");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOpenPinTuan() {
        String orderId = createOrderWithGoods("1", "PT-ORDER-1");
        String groupId = openGroup("1", orderId);

        QueryBean mq = new QueryBean();
        mq.addFilter(FilterBeans.eq(LitemallPinTuanMember.PROP_NAME_groupId, groupId));
        List<?> members = daoProvider.daoFor(LitemallPinTuanMember.class).findAllByQuery(mq);
        assertEquals(1, members.size(), "creator member should be recorded");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJoinPinTuanAndAutoSuccess() {
        String openOrderId = createOrderWithGoods("1", "PT-ORDER-OPEN");
        String groupId = openGroup("1", openOrderId);

        ContextProvider.getOrCreateContext().setUserId("2");
        String joinOrderId = createOrderWithGoods("2", "PT-ORDER-JOIN-1");
        ApiRequest<Map<String, Object>> joinReq = ApiRequest.build(Map.of(
                "groupId", groupId,
                "orderId", joinOrderId
        ));
        IGraphQLExecutionContext joinCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__joinPinTuan", joinReq);
        ApiResponse<?> joinResult = graphQLEngine.executeRpc(joinCtx);
        assertEquals(0, joinResult.getStatus(), "joinPinTuan failed: " + joinResult);

        ContextProvider.getOrCreateContext().setUserId("3");
        String join2OrderId = createOrderWithGoods("3", "PT-ORDER-JOIN-2");
        ApiRequest<Map<String, Object>> join2Req = ApiRequest.build(Map.of(
                "groupId", groupId,
                "orderId", join2OrderId
        ));
        IGraphQLExecutionContext join2Ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__joinPinTuan", join2Req);
        ApiResponse<?> join2Result = graphQLEngine.executeRpc(join2Ctx);
        assertEquals(0, join2Result.getStatus(), "second joinPinTuan should succeed: " + join2Result);

        LitemallPinTuanGroup updated = daoProvider.daoFor(LitemallPinTuanGroup.class)
                .requireEntityById(groupId);
        assertEquals(10, updated.getStatus(), "group should auto-promote to SUCCESS at minUserCount");
    }

    @Test
    public void testCannotJoinOwn() {
        String openOrderId = createOrderWithGoods("1", "PT-ORDER-OWN-1");
        String groupId = openGroup("1", openOrderId);

        ContextProvider.getOrCreateContext().setUserId("1");
        String ownOrderId = createOrderWithGoods("1", "PT-ORDER-OWN-2");
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "groupId", groupId,
                "orderId", ownOrderId
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__joinPinTuan", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus(), "join own group should fail");
    }

    @Test
    public void testAlreadyJoined() {
        String openOrderId = createOrderWithGoods("1", "PT-ORDER-AJ-1");
        String groupId = openGroup("1", openOrderId);

        ContextProvider.getOrCreateContext().setUserId("2");
        String joinOrderId = createOrderWithGoods("2", "PT-ORDER-AJ-2");
        ApiRequest<Map<String, Object>> joinReq = ApiRequest.build(Map.of(
                "groupId", groupId,
                "orderId", joinOrderId
        ));
        IGraphQLExecutionContext joinCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__joinPinTuan", joinReq);
        assertEquals(0, graphQLEngine.executeRpc(joinCtx).getStatus());

        String againOrderId = createOrderWithGoods("2", "PT-ORDER-AJ-3");
        ApiRequest<Map<String, Object>> againReq = ApiRequest.build(Map.of(
                "groupId", groupId,
                "orderId", againOrderId
        ));
        IGraphQLExecutionContext againCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__joinPinTuan", againReq);
        assertNotEquals(0, graphQLEngine.executeRpc(againCtx).getStatus(),
                "joining again should fail");
    }

    @Test
    public void testFullMaxUserCount() {
        String openOrderId = createOrderWithGoods("1", "PT-ORDER-FULL-1");
        String groupId = openGroup("1", openOrderId);

        ContextProvider.getOrCreateContext().setUserId("2");
        String joinOrderId = createOrderWithGoods("2", "PT-ORDER-FULL-2");
        ApiRequest<Map<String, Object>> joinReq = ApiRequest.build(Map.of(
                "groupId", groupId,
                "orderId", joinOrderId
        ));
        IGraphQLExecutionContext joinCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__joinPinTuan", joinReq);
        assertEquals(0, graphQLEngine.executeRpc(joinCtx).getStatus());

        ContextProvider.getOrCreateContext().setUserId("3");
        String join2OrderId = createOrderWithGoods("3", "PT-ORDER-FULL-3");
        ApiRequest<Map<String, Object>> join2Req = ApiRequest.build(Map.of(
                "groupId", groupId,
                "orderId", join2OrderId
        ));
        IGraphQLExecutionContext join2Ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__joinPinTuan", join2Req);
        assertEquals(0, graphQLEngine.executeRpc(join2Ctx).getStatus(),
                "3rd member reaches minUserCount(3) -> SUCCESS, but maxUserCount(3) also reached");

        ContextProvider.getOrCreateContext().setUserId("4");
        String fullOrderId = createOrderWithGoods("4", "PT-ORDER-FULL-4");
        ApiRequest<Map<String, Object>> fullReq = ApiRequest.build(Map.of(
                "groupId", groupId,
                "orderId", fullOrderId
        ));
        IGraphQLExecutionContext fullCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__joinPinTuan", fullReq);
        assertNotEquals(0, graphQLEngine.executeRpc(fullCtx).getStatus(),
                "4th member should be rejected (maxUserCount=3 reached)");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPinTuanDetail() {
        String openOrderId = createOrderWithGoods("1", "PT-ORDER-DETAIL-OPEN");
        String groupId = openGroup("1", openOrderId);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("groupId", groupId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallPinTuanActivity__pinTuanDetail", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "pinTuanDetail failed: " + result);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals(groupId, data.get("id"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMyPinTuans() {
        String openOrderId = createOrderWithGoods("1", "PT-ORDER-MY-OPEN");
        openGroup("1", openOrderId);

        ContextProvider.getOrCreateContext().setUserId("1");
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallPinTuanActivity__myPinTuans", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "myPinTuans failed: " + result);

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        List<Map<String, Object>> items = (List<Map<String, Object>>) pageData.get("items");
        assertNotNull(items);
        assertFalse(items.isEmpty(), "user should see their opened group");
    }

    @Test
    public void testExpirePinTuans() {
        ContextProvider.getOrCreateContext().setUserId("1");
        // Seed a paid order + an ACTIVE group whose expireTime is already in the past, then run the
        // expire job. Mirrors the groupon expire test setup (direct dao seed, past expiry).
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).newEntity();
        order.setUserId("1");
        order.setOrderSn("PT_EXPIRE_001");
        order.setOrderStatus(201);
        order.setAftersaleStatus(0);
        order.setConsignee("测试收货人");
        order.setMobile("13800138000");
        order.setAddress("测试地址");
        order.setMessage("expire-test");
        order.setGoodsPrice(new BigDecimal("100.00"));
        order.setFreightPrice(BigDecimal.ZERO);
        order.setCouponPrice(BigDecimal.ZERO);
        order.setIntegralPrice(BigDecimal.ZERO);
        order.setGrouponPrice(BigDecimal.ZERO);
        order.setPromotionPrice(BigDecimal.ZERO);
        order.setPinTuanPrice(BigDecimal.ZERO);
        order.setOrderPrice(new BigDecimal("100.00"));
        order.setActualPrice(new BigDecimal("100.00"));
        order.setDeleted(false);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(order);
        String orderId = order.orm_idString();

        LitemallPinTuanGroup group = daoProvider.daoFor(LitemallPinTuanGroup.class).newEntity();
        group.setActivityId(activityId);
        group.setCreatorUserId("1");
        group.setOrderId(orderId);
        group.setStatus(0);
        group.setExpireTime(LocalDateTime.now().minusHours(1));
        daoProvider.daoFor(LitemallPinTuanGroup.class).saveEntity(group);
        String groupId = group.orm_idString();

        LitemallPinTuanMember member = daoProvider.daoFor(LitemallPinTuanMember.class).newEntity();
        member.setGroupId(groupId);
        member.setUserId("1");
        member.setOrderId(orderId);
        daoProvider.daoFor(LitemallPinTuanMember.class).saveEntity(member);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__expirePinTuans", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "expirePinTuans failed: " + result);

        Number expiredCount = (Number) result.getData();
        assertTrue(expiredCount.intValue() >= 1, "should expire at least 1 group");

        LitemallPinTuanGroup updated = daoProvider.daoFor(LitemallPinTuanGroup.class)
                .requireEntityById(groupId);
        assertEquals(20, updated.getStatus(), "group should be FAILED after expire");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetPinTuanEffectiveness() {
        String order1 = createOrderWithGoods("1", "PT-STAT-1");
        String order2 = createOrderWithGoods("2", "PT-STAT-2");
        String order3 = createOrderWithGoods("3", "PT-STAT-3");

        // success group (status=10) with 2 members
        LitemallPinTuanGroup successGroup = daoProvider.daoFor(LitemallPinTuanGroup.class).newEntity();
        successGroup.setActivityId(activityId);
        successGroup.setCreatorUserId("1");
        successGroup.setOrderId(order1);
        successGroup.setStatus(10);
        daoProvider.daoFor(LitemallPinTuanGroup.class).saveEntity(successGroup);
        String successGroupId = successGroup.orm_idString();

        LitemallPinTuanMember m1 = daoProvider.daoFor(LitemallPinTuanMember.class).newEntity();
        m1.setGroupId(successGroupId);
        m1.setUserId("1");
        m1.setOrderId(order1);
        daoProvider.daoFor(LitemallPinTuanMember.class).saveEntity(m1);
        LitemallPinTuanMember m2 = daoProvider.daoFor(LitemallPinTuanMember.class).newEntity();
        m2.setGroupId(successGroupId);
        m2.setUserId("2");
        m2.setOrderId(order2);
        daoProvider.daoFor(LitemallPinTuanMember.class).saveEntity(m2);

        // active group (status=0) with 1 member
        LitemallPinTuanGroup activeGroup = daoProvider.daoFor(LitemallPinTuanGroup.class).newEntity();
        activeGroup.setActivityId(activityId);
        activeGroup.setCreatorUserId("3");
        activeGroup.setOrderId(order3);
        activeGroup.setStatus(0);
        daoProvider.daoFor(LitemallPinTuanGroup.class).saveEntity(activeGroup);
        LitemallPinTuanMember m3 = daoProvider.daoFor(LitemallPinTuanMember.class).newEntity();
        m3.setGroupId(activeGroup.orm_idString());
        m3.setUserId("3");
        m3.setOrderId(order3);
        daoProvider.daoFor(LitemallPinTuanMember.class).saveEntity(m3);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("activityId", activityId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallPinTuanActivity__getPinTuanEffectiveness", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getPinTuanEffectiveness failed: " + result);

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(2, ((Number) data.get("openedGroups")).intValue(), "openedGroups");
        assertEquals(1, ((Number) data.get("successGroups")).intValue(), "successGroups");
        assertEquals(3, ((Number) data.get("participantCount")).intValue(), "participantCount");
        assertEquals(0, new BigDecimal("300").compareTo(new BigDecimal(data.get("totalGmv").toString())),
                "totalGmv should be sum of the 3 member orders' actualPrice");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPublishAndUnpublishActivity() {
        LitemallPinTuanActivity draft = daoProvider.daoFor(LitemallPinTuanActivity.class).newEntity();
        draft.setGoodsId(goodsId);
        draft.setPinTuanPrice(new BigDecimal("80.00"));
        draft.setMinUserCount(3);
        draft.setMaxUserCount(3);
        draft.setExpireHours(24);
        draft.setStatus(0);
        daoProvider.daoFor(LitemallPinTuanActivity.class).saveEntity(draft);
        String id = draft.orm_idString();

        // draft(0) -> publish -> active(10)
        ApiRequest<Map<String, Object>> pubReq = ApiRequest.build(Map.of("id", id));
        ApiResponse<?> pubRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__publishActivity", pubReq));
        assertEquals(0, pubRes.getStatus(), "publishActivity failed: " + pubRes);
        assertEquals(10, ((Number) ((Map<String, Object>) pubRes.getData()).get("status")).intValue());

        // active(10) -> publish again should fail
        ApiResponse<?> republish = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__publishActivity", pubReq));
        assertNotEquals(0, republish.getStatus(), "re-publish active should fail");

        // active(10) -> unpublish -> closed(30)
        ApiResponse<?> unpubRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPinTuanActivity__unpublishActivity", pubReq));
        assertEquals(0, unpubRes.getStatus(), "unpublishActivity failed: " + unpubRes);
        assertEquals(30, ((Number) ((Map<String, Object>) unpubRes.getData()).get("status")).intValue());
    }
}
