package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallPickupStore;
import app.mall.dao.entity.LitemallSystem;
import app.mall.dao.entity.LitemallUserMessage;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.autotest.NopTestProperty;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nop.api.core.beans.FilterBeans.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P31 配送方式扩展/自提核销 IGraphQLEngine 测试。覆盖：
 * - submit 自提分支（freight=0 + pickupCode + addressId 放宽 + 门店未启用/不存在拒绝）
 * - ship() 拒绝自提订单（隔离 a）
 * - getOverdueUnshippedOrders 排除 PICKUP（隔离 c）
 * - verifyPickupOrder 成功推进 401 + pickupTime + 积分赠送 + 幂等 + 无效码拒绝 + 非自提订单拒绝（隔离 b）
 * - listActiveStores 仅返回启用门店
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
@NopTestProperty(name = "nop.file.store-dir", value = "./target")
public class TestLitemallPickupDeliveryBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    io.nop.dao.api.IDaoProvider daoProvider;

    String goodsId;
    String productId;
    String addressId;
    String activeStoreId;
    String inactiveStoreId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("admin");

        io.nop.file.dao.entity.NopFileRecord cartPic = daoProvider.daoFor(io.nop.file.dao.entity.NopFileRecord.class).newEntity();
        cartPic.setFileId("pickup-cart-pic");
        cartPic.setBizObjName("LitemallCart");
        cartPic.setBizObjId("temp");
        cartPic.setFieldName("picUrl");
        cartPic.setOriginFileId("pickup-cart-pic");
        cartPic.setFileName("pickup-cart-pic.png");
        cartPic.setFilePath("/test/pickup-cart-pic.png");
        cartPic.setFileExt("png");
        cartPic.setMimeType("image/png");
        cartPic.setIsPublic(true);
        daoProvider.daoFor(io.nop.file.dao.entity.NopFileRecord.class).saveEntity(cartPic);

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("PICKUP-G001");
        goods.setName("Pickup Test Goods");
        goods.setPicUrl("http://test.com/goods-pic.png");
        goods.setCounterPrice(BigDecimal.valueOf(100));
        goods.setRetailPrice(BigDecimal.valueOf(99));
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.getId();

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goodsId);
        product.setNumber(100);
        product.setPrice(BigDecimal.valueOf(99));
        product.setUrl("http://test.com/product-pic.png");
        product.setSpecifications("[\"标准\"]");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);
        productId = product.getId();

        LitemallAddress address = daoProvider.daoFor(LitemallAddress.class).newEntity();
        address.setUserId("1");
        address.setName("张三");
        address.setTel("13800138000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setCounty("南山区");
        address.setAddressDetail("科技园");
        address.setIsDefault(true);
        daoProvider.daoFor(LitemallAddress.class).saveEntity(address);
        addressId = address.getId();

        LitemallPickupStore active = daoProvider.daoFor(LitemallPickupStore.class).newEntity();
        active.setName("科技园门店");
        active.setAddress("深圳市南山区科技园 1 号");
        active.setContact("店长");
        active.setPhone("0755-10000001");
        active.setLatitude(new BigDecimal("22.540000"));
        active.setLongitude(new BigDecimal("113.940000"));
        active.setOpeningHours("09:00-21:00");
        active.setStatus(0);
        daoProvider.daoFor(LitemallPickupStore.class).saveEntity(active);
        activeStoreId = active.orm_idString();

        LitemallPickupStore inactive = daoProvider.daoFor(LitemallPickupStore.class).newEntity();
        inactive.setName("停业门店");
        inactive.setAddress("北京市朝阳区某街");
        inactive.setContact("店员");
        inactive.setPhone("010-20000002");
        inactive.setOpeningHours("09:00-18:00");
        inactive.setStatus(1);
        daoProvider.daoFor(LitemallPickupStore.class).saveEntity(inactive);
        inactiveStoreId = inactive.orm_idString();
    }

    private void resetCart() {
        QueryBean q = new QueryBean();
        q.addFilter(eq(LitemallCart.PROP_NAME_userId, "1"));
        List<LitemallCart> existing = daoProvider.daoFor(LitemallCart.class).findAllByQuery(q);
        for (LitemallCart c : existing) {
            daoProvider.daoFor(LitemallCart.class).deleteEntity(c);
        }
        LitemallCart cart = daoProvider.daoFor(LitemallCart.class).newEntity();
        cart.setUserId("1");
        cart.setGoodsId(goodsId);
        cart.setProductId(productId);
        cart.setNumber(2);
        cart.setPrice(BigDecimal.valueOf(99));
        cart.setChecked(true);
        cart.setGoodsSn("PICKUP-G001");
        cart.setGoodsName("Pickup Test Goods");
        cart.setPicUrl("/f/download/pickup-cart-pic");
        cart.setSpecifications("[\"标准\"]");
        daoProvider.daoFor(LitemallCart.class).saveEntity(cart);
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<?> callMutation(String obj, String action, Map<String, Object> data) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, obj + "__" + action, req);
        return graphQLEngine.executeRpc(ctx);
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<?> callQuery(String obj, String action, Map<String, Object> data) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, obj + "__" + action, req);
        return graphQLEngine.executeRpc(ctx);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> submitPickupOrder(String storeId) {
        resetCart();
        Map<String, Object> data = new HashMap<>();
        data.put("message", "自提订单测试");
        data.put("freightPrice", BigDecimal.TEN); // 自提分支应忽略并置 0
        data.put("deliveryType", _AppMallDaoConstants.DELIVERY_TYPE_PICKUP);
        data.put("pickupStoreId", storeId);
        ApiResponse<?> r = callMutation("LitemallOrder", "submit", data);
        assertEquals(0, r.getStatus(), "submit pickup failed: " + r);
        return (Map<String, Object>) r.getData();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> submitPickupAndPay(String storeId) {
        Map<String, Object> order = submitPickupOrder(storeId);
        String orderId = (String) order.get("id");
        callMutation("LitemallOrder", "pay", Map.of("orderId", orderId));
        return order;
    }

    private int getMyPoints(String userId) {
        ContextProvider.getOrCreateContext().setUserId(userId);
        ApiResponse<?> r = callQuery("LitemallPointsAccount", "getMyPoints", new HashMap<>());
        assertEquals(0, r.getStatus(), "getMyPoints failed: " + r);
        return ((Number) r.getData()).intValue();
    }

    // ===== listActiveStores =====

    @SuppressWarnings("unchecked")
    @Test
    public void testListActiveStoresReturnsOnlyActive() {
        ApiResponse<?> r = callQuery("LitemallPickupStore", "listActiveStores", new HashMap<>());
        assertEquals(0, r.getStatus(), "listActiveStores failed: " + r);
        List<Map<String, Object>> data = (List<Map<String, Object>>) r.getData();
        assertNotNull(data);
        assertTrue(data.stream().anyMatch(s -> "科技园门店".equals(s.get("name"))),
                "should contain the active store");
        assertFalse(data.stream().anyMatch(s -> "停业门店".equals(s.get("name"))),
                "should NOT contain the inactive store");
    }

    // ===== submit pickup branch =====

    @Test
    public void testSubmitPickupOrderFreightZeroAndCodeGenerated() {
        Map<String, Object> order = submitPickupOrder(activeStoreId);

        assertEquals(0, new BigDecimal("0").compareTo(new BigDecimal(order.get("freightPrice").toString())),
                "pickup order freight must be 0");
        assertEquals(_AppMallDaoConstants.DELIVERY_TYPE_PICKUP, order.get("deliveryType"));
        assertNotNull(order.get("pickupCode"), "pickupCode must be generated");
        assertFalse(order.get("pickupCode").toString().isEmpty(), "pickupCode must not be empty");
        // addressId 放宽：自提订单无用户收货地址，consignee/address 承载自提门店信息（NOT NULL 列）
        assertEquals("科技园门店", order.get("consignee"));
        assertEquals("深圳市南山区科技园 1 号", order.get("address"));
    }

    @Test
    public void testSubmitPickupInactiveStoreRejected() {
        resetCart();
        Map<String, Object> data = new HashMap<>();
        data.put("freightPrice", BigDecimal.ZERO);
        data.put("deliveryType", _AppMallDaoConstants.DELIVERY_TYPE_PICKUP);
        data.put("pickupStoreId", inactiveStoreId);
        ApiResponse<?> r = callMutation("LitemallOrder", "submit", data);
        assertEquals(-1, r.getStatus(), "submit to inactive store should be rejected: " + r);
        assertTrue(r.getMsg().contains("not-active") || r.getMsg().contains("启用"),
                "expected not-active error: " + r.getMsg());
    }

    @Test
    public void testSubmitPickupStoreNotFound() {
        resetCart();
        Map<String, Object> data = new HashMap<>();
        data.put("freightPrice", BigDecimal.ZERO);
        data.put("deliveryType", _AppMallDaoConstants.DELIVERY_TYPE_PICKUP);
        data.put("pickupStoreId", "999999");
        ApiResponse<?> r = callMutation("LitemallOrder", "submit", data);
        assertEquals(-1, r.getStatus(), "submit to non-existent store should be rejected: " + r);
    }

    // ===== ship() rejects pickup (隔离 a) =====

    @Test
    public void testShipRejectsPickupOrder() {
        Map<String, Object> order = submitPickupAndPay(activeStoreId);
        ApiResponse<?> r = callMutation("LitemallOrder", "ship", Map.of(
                "orderId", order.get("id"),
                "shipSn", "SF-PICKUP",
                "shipChannel", "顺丰"));
        assertEquals(-1, r.getStatus(), "ship on pickup order must be rejected: " + r);
        assertTrue(r.getMsg().contains("pickup-not-shippable") || r.getMsg().contains("自提"),
                "expected pickup-not-shippable error: " + r.getMsg());
    }

    // ===== getOverdueUnshippedOrders excludes PICKUP (隔离 c) =====

    @SuppressWarnings("unchecked")
    @Test
    public void testPickupOrderExcludedFromOverdueUnshipped() {
        Map<String, Object> pickupOrder = submitPickupAndPay(activeStoreId); // status=201, PICKUP

        ApiResponse<?> r = callQuery("LitemallOrder", "getOverdueUnshippedOrders", Map.of("cutoffHours", 0));
        assertEquals(0, r.getStatus(), "getOverdueUnshippedOrders failed: " + r);
        List<Map<String, Object>> data = (List<Map<String, Object>>) r.getData();
        assertNotNull(data);
        assertFalse(data.stream().anyMatch(o -> pickupOrder.get("id").equals(o.get("id"))),
                "pickup order must NOT appear in overdue-unshipped list");
    }

    // ===== verifyPickupOrder (隔离 b) =====

    @Test
    public void testVerifyPickupOrderSuccessAdvancesTo401AndEarnsPoints() {
        Map<String, Object> order = submitPickupAndPay(activeStoreId);
        String pickupCode = (String) order.get("pickupCode");
        BigDecimal actualPrice = new BigDecimal(order.get("actualPrice").toString());

        int before = getMyPoints("1");

        ApiResponse<?> r = callMutation("LitemallOrder", "verifyPickupOrder", Map.of("pickupCode", pickupCode));
        assertEquals(0, r.getStatus(), "verifyPickupOrder failed: " + r);

        // DB 状态推进到 401 + pickupTime 写入
        LitemallOrder entity = daoProvider.daoFor(LitemallOrder.class).getEntityById((String) order.get("id"));
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_CONFIRM, entity.getOrderStatus(),
                "verify should advance to 401 (CONFIRM)");
        assertNotNull(entity.getPickupTime(), "pickupTime must be set on verify");

        // 积分赠送（复制 confirm 真实副作用）
        int after = getMyPoints("1");
        int expected = actualPrice.intValue(); // earnPerYuan=1 默认
        assertEquals(expected, after - before,
                "verify should earn actualPrice points (copies confirm side-effect): " + (after - before));

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) r.getData();
        assertFalse(Boolean.TRUE.equals(result.get("alreadyVerified")), "first verify not alreadyVerified");
    }

    @Test
    public void testVerifyPickupOrderIdempotent() {
        Map<String, Object> order = submitPickupAndPay(activeStoreId);
        String pickupCode = (String) order.get("pickupCode");

        ApiResponse<?> r1 = callMutation("LitemallOrder", "verifyPickupOrder", Map.of("pickupCode", pickupCode));
        assertEquals(0, r1.getStatus(), "first verify failed: " + r1);

        int pointsAfterFirst = getMyPoints("1");

        // 重复核销：幂等跳过
        ApiResponse<?> r2 = callMutation("LitemallOrder", "verifyPickupOrder", Map.of("pickupCode", pickupCode));
        assertEquals(0, r2.getStatus(), "idempotent re-verify should succeed (skip): " + r2);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) r2.getData();
        assertTrue(Boolean.TRUE.equals(result.get("alreadyVerified")),
                "re-verify should return alreadyVerified=true");

        // 积分不重复发放
        int pointsAfterSecond = getMyPoints("1");
        assertEquals(pointsAfterFirst, pointsAfterSecond,
                "idempotent re-verify must NOT earn points again");
    }

    @Test
    public void testVerifyPickupOrderInvalidCode() {
        ApiResponse<?> r = callMutation("LitemallOrder", "verifyPickupOrder", Map.of("pickupCode", "NOPE2025"));
        assertEquals(-1, r.getStatus(), "invalid pickupCode should be rejected: " + r);
        assertTrue(r.getMsg().contains("invalid") || r.getMsg().contains("无效"),
                "expected invalid code error: " + r.getMsg());
    }

    @Test
    public void testVerifyPickupOrderRejectsExpressOrder() {
        // 非自提订单核销拒绝：给一笔快递订单手工塞 pickupCode，核销应因 deliveryType != PICKUP 拒绝。
        resetCart();
        Map<String, Object> expressData = new HashMap<>();
        expressData.put("addressId", addressId);
        expressData.put("message", "express");
        expressData.put("freightPrice", BigDecimal.ZERO);
        // 不传 deliveryType → EXPRESS
        ApiResponse<?> submitR = callMutation("LitemallOrder", "submit", expressData);
        assertEquals(0, submitR.getStatus(), "express submit failed: " + submitR);
        @SuppressWarnings("unchecked")
        Map<String, Object> expressOrder = (Map<String, Object>) submitR.getData();
        String orderId = (String) expressOrder.get("id");

        // 手工塞 pickupCode（经 GraphQL update 管道，确保会话上下文）+ 推进到 201（pay）
        ApiRequest<Map<String, Object>> updateReq = ApiRequest.build(Map.of(
                "data", Map.of(
                        "id", orderId,
                        "pickupCode", "EXPRESS1"
                )
        ));
        IGraphQLExecutionContext updateCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__update", updateReq);
        assertEquals(0, graphQLEngine.executeRpc(updateCtx).getStatus(), "update pickupCode failed");

        callMutation("LitemallOrder", "pay", Map.of("orderId", orderId));

        ApiResponse<?> r = callMutation("LitemallOrder", "verifyPickupOrder", Map.of("pickupCode", "EXPRESS1"));
        assertEquals(-1, r.getStatus(), "verify on express order must be rejected: " + r);
        assertTrue(r.getMsg().contains("not-verifiable") || r.getMsg().contains("核销"),
                "expected not-verifiable error: " + r.getMsg());
    }

    // ===== 自提核销成功站内信（successor of P31 deferred）=====

    private void setConfig(String key, String value) {
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName", key);
        cfg.orm_propValueByName("keyValue", value);
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
    }

    private long countPickupVerifyMessages(String userId) {
        QueryBean q = new QueryBean();
        q.addFilter(eq(LitemallUserMessage.PROP_NAME_userId, userId));
        q.addFilter(eq(LitemallUserMessage.PROP_NAME_msgType, _AppMallDaoConstants.MSG_TYPE_ORDER));
        q.addFilter(eq(LitemallUserMessage.PROP_NAME_title, "订单核销成功"));
        return daoProvider.daoFor(LitemallUserMessage.class).findAllByQuery(q).size();
    }

    @Test
    public void testVerifyPickupOrderPushesInAppMessageOnSuccess() {
        Map<String, Object> order = submitPickupAndPay(activeStoreId);
        String pickupCode = (String) order.get("pickupCode");
        String userId = (String) order.get("userId");

        long before = countPickupVerifyMessages(userId);

        ApiResponse<?> r = callMutation("LitemallOrder", "verifyPickupOrder", Map.of("pickupCode", pickupCode));
        assertEquals(0, r.getStatus(), "verify failed: " + r);

        long after = countPickupVerifyMessages(userId);
        assertEquals(before + 1, after, "verify success must push exactly one ORDER 站内信");
    }

    @Test
    public void testVerifyPickupOrderIdempotentReverifyDoesNotDuplicateMessage() {
        Map<String, Object> order = submitPickupAndPay(activeStoreId);
        String pickupCode = (String) order.get("pickupCode");
        String userId = (String) order.get("userId");

        callMutation("LitemallOrder", "verifyPickupOrder", Map.of("pickupCode", pickupCode));
        long afterFirst = countPickupVerifyMessages(userId);

        // Idempotent re-verify: alreadyVerified branch must NOT push a second message.
        ApiResponse<?> r2 = callMutation("LitemallOrder", "verifyPickupOrder", Map.of("pickupCode", pickupCode));
        assertEquals(0, r2.getStatus(), "re-verify should succeed (skip)");
        long afterSecond = countPickupVerifyMessages(userId);
        assertEquals(afterFirst, afterSecond, "idempotent re-verify must not push a duplicate message");
    }

    @Test
    public void testVerifyPickupOrderRespectsEventToggle() {
        // Event toggle mall_message_event_enabled_pickup_verify=false ⇒ no message pushed.
        setConfig("mall_message_event_enabled_pickup_verify", "false");
        Map<String, Object> order = submitPickupAndPay(activeStoreId);
        String pickupCode = (String) order.get("pickupCode");
        String userId = (String) order.get("userId");

        long before = countPickupVerifyMessages(userId);
        ApiResponse<?> r = callMutation("LitemallOrder", "verifyPickupOrder", Map.of("pickupCode", pickupCode));
        assertEquals(0, r.getStatus(), "verify must still succeed with toggle off: " + r);
        long after = countPickupVerifyMessages(userId);
        assertEquals(before, after, "event toggle off ⇒ no 站内信 pushed");
    }
}
