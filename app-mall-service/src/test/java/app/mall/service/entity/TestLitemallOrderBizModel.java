package app.mall.service.entity;

import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallCoupon;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.entity.LitemallPinTuanActivity;
import app.mall.dao.entity.LitemallPromotionActivity;
import app.mall.dao.entity.LitemallPromotionTier;
import app.mall.dao.entity.LitemallPromotionUsage;
import app.mall.dao.entity.LitemallSystem;
import app.mall.dao.entity.LitemallTimeDiscount;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.dao._AppMallDaoConstants;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.file.dao.entity.NopFileRecord;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import io.nop.orm.IOrmTemplate;
import io.nop.core.context.ServiceContextImpl;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallOrderBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    IOrmTemplate ormTemplate;

    @Inject
    ILitemallOrderBiz orderBiz;

    String goodsId;
    String productId;
    String addressId;

    private void createFileRecord(String fileId, String bizObjName) {
        NopFileRecord record = daoProvider.daoFor(NopFileRecord.class).newEntity();
        record.setFileId(fileId);
        record.setBizObjName(bizObjName);
        record.setBizObjId("temp");
        record.setFieldName("temp");
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

        createFileRecord("goods-pic", "LitemallGoods");
        createFileRecord("product-pic", "LitemallGoodsProduct");
        createFileRecord("cart-pic", "LitemallCart");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G001");
        goods.setName("Test Goods");
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

        LitemallCart cart = daoProvider.daoFor(LitemallCart.class).newEntity();
        cart.setUserId("1");
        cart.setGoodsId(goodsId);
        cart.setProductId(productId);
        cart.setNumber(2);
        cart.setPrice(BigDecimal.valueOf(99));
        cart.setChecked(true);
        cart.setGoodsSn(goods.getGoodsSn());
        cart.setGoodsName(goods.getName());
        // domain=image stores a file link /f/download/{fileId}, not a plain URL.
        // submit() copies cart.picUrl -> orderGoods.picUrl via OrmFileComponent.copyFrom,
        // which only resolves if the value is a file link matching a NopFileRecord.
        cart.setPicUrl("/f/download/cart-pic");
        cart.setSpecifications("[\"标准\"]");
        daoProvider.daoFor(LitemallCart.class).saveEntity(cart);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitAndPay() {
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "请尽快发货",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(),
                "submit failed: msg=" + submitResult.getMsg() + " data=" + submitResult.getData());
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        assertNotNull(orderData);
        assertEquals(99 * 2, ((Number) orderData.get("goodsPrice")).intValue());
        String orderId = (String) orderData.get("id");
        assertNotNull(orderId);

        ApiRequest<Map<String, Object>> payReq = ApiRequest.build(Map.of(
                "orderId", orderId
        ));
        IGraphQLExecutionContext payCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__pay", payReq);
        ApiResponse<?> payResult = graphQLEngine.executeRpc(payCtx);
        assertEquals(0, payResult.getStatus(),
                "pay failed: " + payResult);
        Map<String, Object> paidOrder = (Map<String, Object>) payResult.getData();
        assertNotNull(paidOrder);
        assertEquals(201, paidOrder.get("orderStatus"));
        assertNotNull(paidOrder.get("payTime"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testShipAndConfirm() throws Exception {
        testSubmitAndPay();

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of());
        IGraphQLExecutionContext listCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__myOrders", listReq);
        ApiResponse<?> listResult = graphQLEngine.executeRpc(listCtx);
        assertEquals(0, listResult.getStatus());
        java.util.List<Map<String, Object>> orders = (java.util.List<Map<String, Object>>) listResult.getData();
        assertNotNull(orders);
        assertTrue(orders.size() > 0);
        String orderId = (String) orders.get(0).get("id");

        ApiRequest<Map<String, Object>> shipReq = ApiRequest.build(Map.of(
                "orderId", orderId,
                "shipSn", "SF1234567890",
                "shipChannel", "顺丰"
        ));
        IGraphQLExecutionContext shipCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__ship", shipReq);
        ApiResponse<?> shipResult = graphQLEngine.executeRpc(shipCtx);
        assertEquals(0, shipResult.getStatus(),
                "ship failed: " + shipResult);
        Map<String, Object> shippedOrder = (Map<String, Object>) shipResult.getData();
        assertEquals(301, shippedOrder.get("orderStatus"));

        ApiRequest<Map<String, Object>> confirmReq = ApiRequest.build(Map.of(
                "orderId", orderId
        ));
        IGraphQLExecutionContext confirmCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__confirm", confirmReq);
        ApiResponse<?> confirmResult = graphQLEngine.executeRpc(confirmCtx);
        assertEquals(0, confirmResult.getStatus(),
                "confirm failed: " + confirmResult);
        Map<String, Object> confirmedOrder = (Map<String, Object>) confirmResult.getData();
        assertEquals(401, confirmedOrder.get("orderStatus"));
    }

    @Test
    public void testConfirmPaidByNotify() {
        // Submit a CREATED order, then simulate a verified WeChat SUCCESS notify driving it to PAY
        // via the trusted internal confirmPaidByNotify entry (P0-1: callback must advance order).
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "notify confirm test",
                "freightPrice", BigDecimal.TEN
        ));
        IGraphQLExecutionContext submitCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(submitCtx);
        assertEquals(0, submitResult.getStatus(), "submit failed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        String orderId = (String) orderData.get("id");
        String orderSn = (String) orderData.get("orderSn");

        // Trusted notify path: outTradeNo == orderSn
        ApiRequest<Map<String, Object>> notifyReq = ApiRequest.build(Map.of(
                "outTradeNo", orderSn,
                "transactionId", "wx-txn-4200000000001"
        ));
        IGraphQLExecutionContext notifyCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__confirmPaidByNotify", notifyReq);
        ApiResponse<?> notifyResult = graphQLEngine.executeRpc(notifyCtx);
        assertEquals(0, notifyResult.getStatus(), "confirmPaidByNotify failed: " + notifyResult);

        // Order advanced CREATED(101) -> PAY(201)
        ApiRequest<Map<String, Object>> getReq = ApiRequest.build(Map.of("orderId", orderId));
        IGraphQLExecutionContext getCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getMyOrder", getReq);
        ApiResponse<?> getResult = graphQLEngine.executeRpc(getCtx);
        assertEquals(0, getResult.getStatus(), "getMyOrder failed: " + getResult);
        Map<String, Object> paid = (Map<String, Object>) getResult.getData();
        assertEquals(201, paid.get("orderStatus"), "order should be PAY after notify confirmation");

        // Idempotency: a replayed notify on an already-PAY order is a no-op (no error)
        ApiResponse<?> replay = graphQLEngine.executeRpc(notifyCtx);
        assertEquals(0, replay.getStatus(), "replayed notify should be idempotent: " + replay);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitCancel() {
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "请取消",
                "freightPrice", BigDecimal.TEN
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(),
                "submit failed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        String orderId = (String) orderData.get("id");

        ApiRequest<Map<String, Object>> cancelReq = ApiRequest.build(Map.of(
                "orderId", orderId
        ));
        IGraphQLExecutionContext cancelCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__cancel", cancelReq);
        ApiResponse<?> cancelResult = graphQLEngine.executeRpc(cancelCtx);
        assertEquals(0, cancelResult.getStatus(),
                "cancel failed: " + cancelResult);
        Map<String, Object> cancelledOrder = (Map<String, Object>) cancelResult.getData();
        assertEquals(102, cancelledOrder.get("orderStatus"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCancelExpiredOrders() {
        // Submit a CREATED order (deducts 2 units: stock 100 -> 98)
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "expire test",
                "freightPrice", BigDecimal.TEN
        ));
        IGraphQLExecutionContext submitCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(submitCtx);
        assertEquals(0, submitResult.getStatus(), "submit failed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        String orderId = (String) orderData.get("id");

        // timeoutMinutes=0 -> cutoff=now, catches the just-created CREATED order
        ApiRequest<Map<String, Object>> cancelReq = ApiRequest.build(Map.of(
                "timeoutMinutes", 0
        ));
        IGraphQLExecutionContext cancelCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__cancelExpiredOrders", cancelReq);
        ApiResponse<?> cancelResult = graphQLEngine.executeRpc(cancelCtx);
        assertEquals(0, cancelResult.getStatus(), "cancelExpiredOrders failed: " + cancelResult);
        int count = ((Number) cancelResult.getData()).intValue();
        assertTrue(count >= 1, "cancelExpiredOrders should process the submitted order, got count=" + count);

        // Order transitioned CREATED(101) -> AUTO_CANCEL(103) via the atomic status guard
        ApiRequest<Map<String, Object>> getReq = ApiRequest.build(Map.of("orderId", orderId));
        IGraphQLExecutionContext getCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getMyOrder", getReq);
        ApiResponse<?> getResult = graphQLEngine.executeRpc(getCtx);
        assertEquals(0, getResult.getStatus(), "getMyOrder failed: " + getResult);
        Map<String, Object> cancelled = (Map<String, Object>) getResult.getData();
        assertEquals(103, cancelled.get("orderStatus"), "order should be AUTO_CANCEL after batch");
    }

    // ============ PromotionUsage refund-rollback (whole-order cancel/refund releases quota) ============

    private void seedCart() {
        LitemallCart cart = daoProvider.daoFor(LitemallCart.class).newEntity();
        cart.setUserId("1");
        cart.setGoodsId(goodsId);
        cart.setProductId(productId);
        cart.setNumber(2);
        cart.setPrice(BigDecimal.valueOf(99));
        cart.setChecked(true);
        cart.setGoodsSn("G001");
        cart.setGoodsName("Test Goods");
        cart.setPicUrl("/f/download/cart-pic");
        cart.setSpecifications("[\"标准\"]");
        daoProvider.daoFor(LitemallCart.class).saveEntity(cart);
    }

    private long usageCountByOrder(String orderId) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPromotionUsage.PROP_NAME_orderId, orderId));
        return daoProvider.daoFor(LitemallPromotionUsage.class).findAllByQuery(q).size();
    }

    private long usageCountByUserActivity(String userId, String activityId) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPromotionUsage.PROP_NAME_userId, userId));
        q.addFilter(FilterBeans.eq(LitemallPromotionUsage.PROP_NAME_promotionActivityId, activityId));
        return daoProvider.daoFor(LitemallPromotionUsage.class).findAllByQuery(q).size();
    }

    private String submitPromotionOrder() {
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId, "message", "满减回滚", "freightPrice", BigDecimal.ZERO));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> res = graphQLEngine.executeRpc(ctx);
        assertEquals(0, res.getStatus(), "submit failed: " + res);
        return (String) ((Map<String, Object>) res.getData()).get("id");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCancelReleasesPromotionUsageAndAllowsReparticipate() {
        // (a) Promotion hit writes usage -> cancel soft-deletes usage -> user can re-participate.
        LitemallPromotionActivity a = createActiveAmountPromotion(new BigDecimal("100"), new BigDecimal("20"), 1);

        String orderId = submitPromotionOrder();
        assertEquals(1, usageCountByOrder(orderId), "usage written on promotion hit");
        assertEquals(1, usageCountByUserActivity("1", a.orm_idString()), "at limit (maxPerUser=1)");

        // cancel the CREATED order -> release usage (mirrors coupon/points return).
        ApiRequest<Map<String, Object>> cancelReq = ApiRequest.build(Map.of("orderId", orderId));
        ApiResponse<?> cancelResult = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__cancel", cancelReq));
        assertEquals(0, cancelResult.getStatus(), "cancel failed: " + cancelResult);

        // usage soft-deleted -> count by order and by (user,activity) both 0 -> quota released.
        assertEquals(0, usageCountByOrder(orderId), "usage released after cancel");
        assertEquals(0, usageCountByUserActivity("1", a.orm_idString()), "quota released -> eligible");

        // Re-participation proof: re-seed cart + submit again under maxPerUser=1 succeeds (was at limit before cancel).
        seedCart();
        String orderId2 = submitPromotionOrder();
        assertEquals(1, usageCountByOrder(orderId2), "new usage written on re-participation");
    }

    @Test
    public void testCancelExpiredOrdersReleasesPromotionUsage() {
        // (b) batch timeout-cancel releases usage (mirrors cancel).
        createActiveAmountPromotion(new BigDecimal("100"), new BigDecimal("20"));
        String orderId = submitPromotionOrder();
        assertEquals(1, usageCountByOrder(orderId), "usage written");

        // timeoutMinutes=0 -> cutoff=now, catches the just-created CREATED order
        ApiResponse<?> cancelResult = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__cancelExpiredOrders",
                ApiRequest.build(Map.of("timeoutMinutes", 0))));
        assertEquals(0, cancelResult.getStatus(), "cancelExpiredOrders failed: " + cancelResult);
        assertEquals(0, usageCountByOrder(orderId), "usage released by batch cancel");
    }

    @Test
    public void testReleasePromotionUsageIdempotent() {
        // (h) releasing usage is idempotent: a repeat call (after the row is already soft-deleted) finds
        //     0 non-deleted rows via findList and is a harmless no-op (D3: 重复调用软删 0 行无副作用).
        //     The first release goes through the real cancel flow (its ambient @BizMutation tx is what
        //     makes findList+deleteEntity share a session); the repeat call then proves the no-op path.
        createActiveAmountPromotion(new BigDecimal("100"), new BigDecimal("20"));
        String orderId = submitPromotionOrder();
        assertEquals(1, usageCountByOrder(orderId), "usage written");

        // First release via cancel (hook 1) — ambient tx, releases the usage.
        ApiResponse<?> cancelResult = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__cancel",
                ApiRequest.build(Map.of("orderId", orderId))));
        assertEquals(0, cancelResult.getStatus(), "cancel failed: " + cancelResult);
        assertEquals(0, usageCountByOrder(orderId), "released by cancel");

        // Repeat release: findList returns 0 non-deleted rows -> loop body never runs -> no-op, no error.
        orderBiz.releasePromotionUsage(orderId, new ServiceContextImpl());
        assertEquals(0, usageCountByOrder(orderId), "repeat release is a no-op");
    }

    @Test
    public void testCancelNonPromotionOrderNoUsageSideEffect() {
        // (i) non-promotion order: no PromotionUsage to delete, cancel has no usage side effect.
        String orderId = submitPromotionOrderNoPromotion();
        assertEquals(0, usageCountByOrder(orderId), "no usage written for non-promotion order");

        ApiRequest<Map<String, Object>> cancelReq = ApiRequest.build(Map.of("orderId", orderId));
        ApiResponse<?> cancelResult = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__cancel", cancelReq));
        assertEquals(0, cancelResult.getStatus(), "cancel should succeed with no usage to release: " + cancelResult);
        assertEquals(0, usageCountByOrder(orderId), "still no usage after cancel");
    }

    private String submitPromotionOrderNoPromotion() {
        // No promotion activity seeded -> submit writes no usage. Cart goodsPrice=198.
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId, "message", "no-promo", "freightPrice", BigDecimal.ZERO));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> res = graphQLEngine.executeRpc(ctx);
        assertEquals(0, res.getStatus(), "submit failed: " + res);
        return (String) ((Map<String, Object>) res.getData()).get("id");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPrepay() {
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "prepay test",
                "freightPrice", BigDecimal.TEN
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(), "submit failed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        String orderId = (String) orderData.get("id");
        assertNotNull(orderId);
        assertEquals(101, orderData.get("orderStatus"));

        ApiRequest<Map<String, Object>> prepayReq = ApiRequest.build(Map.of(
                "orderId", orderId
        ));
        IGraphQLExecutionContext prepayCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__prepay", prepayReq);
        ApiResponse<?> prepayResult = graphQLEngine.executeRpc(prepayCtx);
        assertEquals(0, prepayResult.getStatus(), "prepay failed: " + prepayResult);
        Map<String, Object> prepayData = (Map<String, Object>) prepayResult.getData();
        assertNotNull(prepayData.get("order"), "order should be in response");
        LitemallOrder order = (LitemallOrder) prepayData.get("order");
        assertEquals(101, order.getOrderStatus(),
                "order status should still be CREATED");
        assertNotNull(prepayData.get("codeUrl"), "codeUrl should be present");
        assertNotNull(order.getPayId(), "payId should be written after prepay");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDetail() throws Exception {
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "test",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus());
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        String orderId = (String) orderData.get("id");

        ApiRequest<Map<String, Object>> detailReq = ApiRequest.build(Map.of(
                "orderId", orderId
        ));
        IGraphQLExecutionContext detailCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__getMyOrder", detailReq);
        ApiResponse<?> detailResult = graphQLEngine.executeRpc(detailCtx);
        assertEquals(0, detailResult.getStatus());
        Map<String, Object> detail = (Map<String, Object>) detailResult.getData();
        assertNotNull(detail);
        assertEquals(orderId, detail.get("id"));
    }

    @Test
    public void testDeleteGrouponExpiredOrder() {
        // submit + pay -> order at PAY(201)
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "groupon expired delete test",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext submitCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(submitCtx);
        assertEquals(0, submitResult.getStatus(), "submit failed: " + submitResult);
        String orderId = (String) ((Map<String, Object>) submitResult.getData()).get("id");

        ApiRequest<Map<String, Object>> payReq = ApiRequest.build(Map.of("orderId", orderId));
        IGraphQLExecutionContext payCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__pay", payReq);
        assertEquals(0, graphQLEngine.executeRpc(payCtx).getStatus(), "pay failed");

        ApiRequest<Map<String, Object>> delReq = ApiRequest.build(Map.of("orderId", orderId));
        IGraphQLExecutionContext delCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__deleteOrder", delReq);

        // Reverse 1: PAY(201) — upstream of 204, most likely to be wrongly whitelisted — must be rejected
        ApiResponse<?> delPay = graphQLEngine.executeRpc(delCtx);
        assertTrue(delPay.getStatus() != 0, "PAY(201) order must not be deletable");

        // Reverse 2: SHIP(301) — also non-whitelisted — must be rejected
        setOrderStatusDirect(orderId, _AppMallDaoConstants.ORDER_STATUS_SHIP);
        ApiResponse<?> delShip = graphQLEngine.executeRpc(delCtx);
        assertTrue(delShip.getStatus() != 0, "SHIP(301) order must not be deletable");

        // Forward: GROUPON_EXPIRED(204) now deletable (simulating groupon timeout having occurred)
        setOrderStatusDirect(orderId, _AppMallDaoConstants.ORDER_STATUS_GROUPON_EXPIRED);
        ApiResponse<?> del204 = graphQLEngine.executeRpc(delCtx);
        assertEquals(0, del204.getStatus(), "GROUPON_EXPIRED(204) order should be deletable: " + del204);

        // verify soft-delete via DAO direct; deleteOrder does not change orderStatus
        LitemallOrder deleted = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
        assertNotNull(deleted, "order row must still exist after soft delete");
        assertTrue(deleted.getDeleted(), "order should be soft-deleted");
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_GROUPON_EXPIRED, deleted.getOrderStatus(),
                "orderStatus must stay 204 after delete");
    }

    private void setOrderStatusDirect(String orderId, int status) {
        ormTemplate.runInSession(session -> {
            LitemallOrder o = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
            assertNotNull(o);
            o.setOrderStatus(status);
            daoProvider.daoFor(LitemallOrder.class).updateEntity(o);
            return null;
        });
    }

    private LitemallPromotionActivity createActiveAmountPromotion(BigDecimal meetAmount, BigDecimal discountValue) {
        return createActiveAmountPromotion(meetAmount, discountValue, null);
    }

    private LitemallPromotionActivity createActiveAmountPromotion(BigDecimal meetAmount, BigDecimal discountValue,
                                                                   Integer maxPerUser) {
        LitemallPromotionActivity a = daoProvider.daoFor(LitemallPromotionActivity.class).newEntity();
        a.setName("满减测试");
        a.setDiscountType(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT);
        a.setStatus(_AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        a.setGoodsScope(_AppMallDaoConstants.GOODS_SCOPE_ALL);
        a.setPriority(1);
        a.setMaxPerUser(maxPerUser);
        a.setStartTime(LocalDateTime.now().minusDays(1));
        a.setEndTime(LocalDateTime.now().plusDays(1));
        daoProvider.daoFor(LitemallPromotionActivity.class).saveEntity(a);

        LitemallPromotionTier t = daoProvider.daoFor(LitemallPromotionTier.class).newEntity();
        t.setActivityId(a.getId());
        t.setMeetAmount(meetAmount);
        t.setDiscountValue(discountValue);
        daoProvider.daoFor(LitemallPromotionTier.class).saveEntity(t);
        return a;
    }

    private String claimCouponForTest() {
        LitemallCoupon coupon = daoProvider.daoFor(LitemallCoupon.class).newEntity();
        coupon.setName("满减叠加测试券");
        coupon.setTag("通用");
        coupon.setTotal(100);
        coupon.setDiscount(BigDecimal.valueOf(5));
        coupon.setMin(BigDecimal.ZERO);
        coupon.setLimit(1);
        coupon.setType(0);
        coupon.setStatus(0);
        coupon.setGoodsType(0);
        coupon.setGoodsValue("");
        coupon.setTimeType(0);
        coupon.setDays(30);
        coupon.setStartTime(LocalDateTime.now().minusDays(1));
        coupon.setEndTime(LocalDateTime.now().plusDays(30));
        daoProvider.daoFor(LitemallCoupon.class).saveEntity(coupon);

        ApiRequest<Map<String, Object>> claimReq = ApiRequest.build(Map.of("couponId", coupon.getId()));
        IGraphQLExecutionContext claimCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCouponUser__claimCoupon", claimReq);
        ApiResponse<?> claimRes = graphQLEngine.executeRpc(claimCtx);
        assertEquals(0, claimRes.getStatus(), "claimCoupon failed: " + claimRes);
        return (String) ((Map<String, Object>) claimRes.getData()).get("id");
    }

    private void setStackingConfig(String value) {
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.setKeyName("mall_promotion_coupon_stacking");
        cfg.setKeyValue(value);
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitWithPromotion() {
        // goodsPrice = 99 * 2 = 198; promotion 满100减20 -> promotionPrice=20
        createActiveAmountPromotion(new BigDecimal("100"), new BigDecimal("20"));

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "满减测试",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(), "submit failed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        assertEquals(0, new BigDecimal("20").compareTo(new BigDecimal(orderData.get("promotionPrice").toString())));
        assertEquals(198, ((Number) orderData.get("goodsPrice")).intValue());
        // orderPrice = 198 + 0 - 0 - 20 = 178
        assertEquals(0, new BigDecimal("178").compareTo(new BigDecimal(orderData.get("orderPrice").toString())));
        assertEquals(0, new BigDecimal("178").compareTo(new BigDecimal(orderData.get("actualPrice").toString())));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitPromotionWritesUsage() {
        // Promotion hit must write exactly one PromotionUsage record attributed to the activity.
        LitemallPromotionActivity a = createActiveAmountPromotion(new BigDecimal("100"), new BigDecimal("20"), 0);

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "满减参与记录",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(), "submit failed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        String orderId = (String) orderData.get("id");
        assertEquals(0, new BigDecimal("20").compareTo(new BigDecimal(orderData.get("promotionPrice").toString())));

        QueryBean usageQuery = new QueryBean();
        usageQuery.addFilter(FilterBeans.eq(LitemallPromotionUsage.PROP_NAME_orderId, orderId));
        java.util.List<LitemallPromotionUsage> usages = daoProvider.daoFor(LitemallPromotionUsage.class)
                .findAllByQuery(usageQuery);
        assertEquals(1, usages.size(), "exactly one PromotionUsage should be written on a promotion hit");
        LitemallPromotionUsage usage = usages.get(0);
        assertEquals("1", usage.getUserId());
        assertEquals(a.orm_idString(), usage.getPromotionActivityId());
        assertEquals(0, new BigDecimal("20").compareTo(usage.getDiscountAmount()));
        assertEquals(0, new BigDecimal("198").compareTo(usage.getMeetAmount()));
    }

    @Test
    public void testSubmitPromotionMaxPerUserRejected() {
        // maxPerUser=1 and one prior participation already recorded -> second submit rejected.
        LitemallPromotionActivity a = createActiveAmountPromotion(new BigDecimal("100"), new BigDecimal("20"), 1);

        LitemallPromotionUsage prior = daoProvider.daoFor(LitemallPromotionUsage.class).newEntity();
        prior.setUserId("1");
        prior.setPromotionActivityId(a.orm_idString());
        prior.setOrderId("999999");
        prior.setMeetAmount(new BigDecimal("198"));
        prior.setDiscountAmount(new BigDecimal("20"));
        daoProvider.daoFor(LitemallPromotionUsage.class).saveEntity(prior);

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "超限拒绝",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertTrue(submitResult.getStatus() != 0,
                "submit should be rejected when maxPerUser reached: " + submitResult);
        assertTrue(submitResult.getMsg() != null && submitResult.getMsg().contains("限参与次数"),
                "rejection should surface the max-per-user description: " + submitResult.getMsg());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitPromotionMaxPerUserAllowsUnderLimit() {
        // maxPerUser=5 and no prior participation -> submit succeeds and writes usage.
        LitemallPromotionActivity a = createActiveAmountPromotion(new BigDecimal("100"), new BigDecimal("20"), 5);

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "限购允许",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(), "submit under limit should succeed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        String orderId = (String) orderData.get("id");

        QueryBean usageQuery = new QueryBean();
        usageQuery.addFilter(FilterBeans.eq(LitemallPromotionUsage.PROP_NAME_orderId, orderId));
        assertEquals(1, daoProvider.daoFor(LitemallPromotionUsage.class).findAllByQuery(usageQuery).size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitPromotionAndCouponStackingAllowed() {
        createActiveAmountPromotion(new BigDecimal("100"), new BigDecimal("20"));
        String couponUserId = claimCouponForTest();
        // no stacking config -> default allowed

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "叠加测试",
                "freightPrice", BigDecimal.ZERO,
                "couponUserId", couponUserId
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(), "submit failed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        assertEquals(0, new BigDecimal("5").compareTo(new BigDecimal(orderData.get("couponPrice").toString())));
        assertEquals(0, new BigDecimal("20").compareTo(new BigDecimal(orderData.get("promotionPrice").toString())));
        // orderPrice = 198 - 5 - 20 = 173
        assertEquals(0, new BigDecimal("173").compareTo(new BigDecimal(orderData.get("orderPrice").toString())));
    }

    @Test
    public void testSubmitPromotionCouponStackingDisabled() {
        createActiveAmountPromotion(new BigDecimal("100"), new BigDecimal("20"));
        String couponUserId = claimCouponForTest();
        setStackingConfig("false");

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "禁止叠加测试",
                "freightPrice", BigDecimal.ZERO,
                "couponUserId", couponUserId
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertTrue(submitResult.getStatus() != 0,
                "submit should be rejected when stacking disabled and both promotion and coupon apply: " + submitResult);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitWithVipPriceForMember() {
        String userId = signUpMember("vipmember01", 1);

        // configure vipPrice on the existing SKU (retail 99 -> vip 80) via a committed RPC update,
        // so the submit RPC sees the configured vipPrice.
        setProductVipPrice(productId, new BigDecimal("80"));

        String memberAddress = createMemberAddress(userId);
        createMemberCart(userId);

        ContextProvider.getOrCreateContext().setUserId(userId);

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", memberAddress,
                "message", "会员价订单",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(), "member submit failed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        // member price 80 * 2 = 160 (instead of retail 99 * 2 = 198)
        assertEquals(160, ((Number) orderData.get("goodsPrice")).intValue(),
                "member should pay vipPrice-based goodsPrice");

        String orderId = (String) orderData.get("id");
        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).findAllByQuery(
                queryByOrder(orderId)).get(0);
        assertEquals(0, new BigDecimal("80").compareTo(og.getPrice()), "orderGoods unit price should be vip 80");
        assertEquals(0, new BigDecimal("80").compareTo(og.getVipPrice()), "orderGoods should snapshot vipPrice 80");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitVipPriceIgnoredForNonMember() {
        String userId = signUpMember("normaluser01", 0);

        setProductVipPrice(productId, new BigDecimal("80"));

        String memberAddress = createMemberAddress(userId);
        createMemberCart(userId);

        ContextProvider.getOrCreateContext().setUserId(userId);

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", memberAddress,
                "message", "非会员订单",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(), "non-member submit failed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        // non-member pays retail 99 * 2 = 198, vipPrice ignored
        assertEquals(198, ((Number) orderData.get("goodsPrice")).intValue(),
                "non-member should pay retail goodsPrice");

        String orderId = (String) orderData.get("id");
        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).findAllByQuery(
                queryByOrder(orderId)).get(0);
        assertEquals(0, new BigDecimal("99").compareTo(og.getPrice()), "orderGoods unit price should be retail 99");
        assertNull(og.getVipPrice(), "non-member orderGoods should not snapshot vipPrice");
    }

    private String signUpMember(String username, int level) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "username", username,
                "password", "Pass@1234",
                "mobile", "139" + username.substring(username.length() - 4)
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LoginApi__signUp", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "signUp helper failed: " + result.getMsg());

        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq("userName", username));
        NopAuthUser user = (NopAuthUser) daoProvider.daoFor(NopAuthUser.class).findAllByQuery(q).stream()
                .findFirst().orElse(null);
        assertNotNull(user, "signed-up member must exist");
        // Set userLevel via a committed GraphQL update so the submit RPC sees the member level.
        setUserLevel(user.getUserId(), level);
        return user.getUserId();
    }

    private void setUserLevel(String userId, int level) {
        Map<String, Object> entity = new HashMap<>();
        entity.put("id", userId);
        entity.put("userLevel", level);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "NopAuthUser__update", ApiRequest.build(Map.of("data", entity)));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "NopAuthUser__update userLevel failed: " + result.getMsg());
    }

    private void setProductVipPrice(String prodId, BigDecimal vipPrice) {
        Map<String, Object> entity = new HashMap<>();
        entity.put("id", prodId);
        entity.put("vipPrice", vipPrice);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGoodsProduct__update", ApiRequest.build(Map.of("data", entity)));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "LitemallGoodsProduct__update vipPrice failed: " + result.getMsg());
    }

    private String createMemberAddress(String userId) {
        LitemallAddress address = daoProvider.daoFor(LitemallAddress.class).newEntity();
        address.setUserId(userId);
        address.setName("会员用户");
        address.setTel("13800138000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setCounty("南山区");
        address.setAddressDetail("科技园");
        address.setIsDefault(true);
        daoProvider.daoFor(LitemallAddress.class).saveEntity(address);
        return address.getId();
    }

    private void createMemberCart(String userId) {
        LitemallCart cart = daoProvider.daoFor(LitemallCart.class).newEntity();
        cart.setUserId(userId);
        cart.setGoodsId(goodsId);
        cart.setProductId(productId);
        cart.setNumber(2);
        cart.setPrice(BigDecimal.valueOf(99));
        cart.setChecked(true);
        cart.setGoodsSn("G001");
        cart.setGoodsName("Test Goods");
        cart.setPicUrl("/f/download/cart-pic");
        cart.setSpecifications("[\"标准\"]");
        daoProvider.daoFor(LitemallCart.class).saveEntity(cart);
    }

    private QueryBean queryByOrder(String orderId) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallOrderGoods.PROP_NAME_orderId, orderId));
        return q;
    }

    // ============ Time-Limited Discount (P23) ============

    private LitemallTimeDiscount createActiveTimeDiscount(int discountType, BigDecimal discountValue,
                                                           String productId, Integer stockLimit) {
        LitemallTimeDiscount d = daoProvider.daoFor(LitemallTimeDiscount.class).newEntity();
        d.setGoodsId(goodsId);
        d.setProductId(productId);
        d.setDiscountType(discountType);
        d.setDiscountValue(discountValue);
        d.setStatus(_AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        d.setStockLimit(stockLimit);
        d.setStartTime(LocalDateTime.now().minusDays(1));
        d.setEndTime(LocalDateTime.now().plusDays(1));
        daoProvider.daoFor(LitemallTimeDiscount.class).saveEntity(d);
        return d;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitWithTimeDiscount() {
        // retail 99, percent 0.9 => promoPrice 89.10; goodsPrice = 89.10 * 2 = 178.20
        createActiveTimeDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_PERCENT, new BigDecimal("0.9"), null, 0);

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "限时折扣测试",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(), "submit failed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        // time discount lowers the unit price at the goodsPrice aggregation layer (not promotionPrice)
        assertEquals(0, new BigDecimal("178.20").compareTo(new BigDecimal(orderData.get("goodsPrice").toString())),
                "goodsPrice should reflect the discounted unit price");
        assertEquals(0, BigDecimal.ZERO.compareTo(new BigDecimal(orderData.get("promotionPrice").toString())),
                "time discount must NOT enter promotionPrice (it's a unit-price-layer discount)");

        String orderId = (String) orderData.get("id");
        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).findAllByQuery(
                queryByOrder(orderId)).get(0);
        assertEquals(0, new BigDecimal("89.10").compareTo(og.getPrice()),
                "orderGoods unit price should be the discounted promoPrice");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitTimeDiscountAmountDiscount() {
        // retail 99, amount 20 => promoPrice 79; goodsPrice = 79 * 2 = 158
        createActiveTimeDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT, new BigDecimal("20"), null, 0);

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "直降折扣测试",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(), "submit failed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        assertEquals(0, new BigDecimal("158.00").compareTo(new BigDecimal(orderData.get("goodsPrice").toString())));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitTimeDiscountCoexistsWithPromotion() {
        // time discount percent 0.9 => goodsPrice = 89.10 * 2 = 178.20
        createActiveTimeDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_PERCENT, new BigDecimal("0.9"), null, 0);
        // 满减 meet 100 reduce 20: applies on the discounted goodsPrice 178.20 >= 100 => promotionPrice 20
        createActiveAmountPromotion(new BigDecimal("100"), new BigDecimal("20"));

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "折扣+满减共存",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(), "submit failed: " + submitResult);
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        // goodsPrice = 178.20 (time discount unit-price layer)
        assertEquals(0, new BigDecimal("178.20").compareTo(new BigDecimal(orderData.get("goodsPrice").toString())));
        // promotionPrice = 20 (满减 judges on discounted goodsPrice 178.20 >= 100)
        assertEquals(0, new BigDecimal("20").compareTo(new BigDecimal(orderData.get("promotionPrice").toString())));
        // orderPrice = 178.20 - 20 = 158.20
        assertEquals(0, new BigDecimal("158.20").compareTo(new BigDecimal(orderData.get("orderPrice").toString())));
    }

    @Test
    public void testSubmitTimeDiscountStockLimitSoldOut() {
        // discount limited to 1 unit, but cart requests 2 => sold-out rejection
        createActiveTimeDiscount(_AppMallDaoConstants.DISCOUNT_TYPE_PERCENT, new BigDecimal("0.9"), null, 1);

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "售罄测试",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertTrue(submitResult.getStatus() != 0,
                "submit should be rejected when discount stock is insufficient: " + submitResult);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitTimeDiscountStockLimitDeducted() {
        // discount limited to 10 units, cart requests 2 => succeeds, stockLimit 10 -> 8
        LitemallTimeDiscount d = createActiveTimeDiscount(
                _AppMallDaoConstants.DISCOUNT_TYPE_PERCENT, new BigDecimal("0.9"), null, 10);

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "库存扣减测试",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext gqlCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(gqlCtx);
        assertEquals(0, submitResult.getStatus(), "submit failed: " + submitResult);

        LitemallTimeDiscount reloaded = daoProvider.daoFor(LitemallTimeDiscount.class).getEntityById(d.orm_idString());
        assertEquals(8, reloaded.getStockLimit(), "stockLimit should be decremented by the ordered quantity");
    }

    // ============ Points System (P27) ============

    @SuppressWarnings("unchecked")
    private Map<String, Object> earnPointsForTest(String userId, int amount, String sourceType, String sourceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("amount", amount);
        data.put("changeType", _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN);
        data.put("sourceType", sourceType);
        data.put("sourceId", sourceId);
        data.put("remark", "seed");
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallPointsAccount__earnPoints", ApiRequest.build(data));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "seed earnPoints failed: " + result);
        return (Map<String, Object>) result.getData();
    }

    private int getMyPointsForTest(String userId) {
        ContextProvider.getOrCreateContext().setUserId(userId);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallPointsAccount__getMyPoints", ApiRequest.build(new HashMap<>()));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getMyPoints failed: " + result);
        return ((Number) result.getData()).intValue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitWithPointsDeduction() {
        // Seed 500 points (default ratio 100 points = ¥1 → ¥5 deduction).
        earnPointsForTest("1", 500,
                LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "seed-deduct-1");

        Map<String, Object> data = new HashMap<>();
        data.put("addressId", addressId);
        data.put("message", "积分抵扣测试");
        data.put("freightPrice", BigDecimal.ZERO);
        data.put("usePoints", 500);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", ApiRequest.build(data));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "submit with points failed: " + result);
        Map<String, Object> orderData = (Map<String, Object>) result.getData();
        // orderPrice = 198; integralPrice = 500/100 = 5; actualPrice = 193
        assertEquals(0, new BigDecimal("5.00").compareTo(new BigDecimal(orderData.get("integralPrice").toString())));
        assertEquals(0, new BigDecimal("198").compareTo(new BigDecimal(orderData.get("orderPrice").toString())));
        assertEquals(0, new BigDecimal("193").compareTo(new BigDecimal(orderData.get("actualPrice").toString())));

        // Points fully spent (500 → 0)
        assertEquals(0, getMyPointsForTest("1"), "balance should be 0 after spending all 500 points");
    }

    @Test
    public void testSubmitPointsDeductionExceedingCapRejected() {
        // Seed 100000 points (= ¥1000 with default ratio), far above the 30% cap on orderPrice 198 (= 59.4).
        earnPointsForTest("1", 100000,
                LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "seed-cap-1");

        Map<String, Object> data = new HashMap<>();
        data.put("addressId", addressId);
        data.put("message", "超上限抵扣测试");
        data.put("freightPrice", BigDecimal.ZERO);
        data.put("usePoints", 100000);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", ApiRequest.build(data));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertTrue(result.getStatus() != 0,
                "submit with points exceeding cap must be rejected: " + result);
        // Balance unchanged after rejection
        assertEquals(100000, getMyPointsForTest("1"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConfirmEarnsShoppingReward() {
        // Submit + pay + ship + confirm, then verify shopping-reward points were earned.
        Map<String, Object> submitData = new HashMap<>();
        submitData.put("addressId", addressId);
        submitData.put("message", "购物赠送测试");
        submitData.put("freightPrice", BigDecimal.ZERO);
        IGraphQLExecutionContext submitCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", ApiRequest.build(submitData));
        ApiResponse<?> submitRes = graphQLEngine.executeRpc(submitCtx);
        assertEquals(0, submitRes.getStatus(), "submit failed: " + submitRes);
        Map<String, Object> orderData = (Map<String, Object>) submitRes.getData();
        String orderId = (String) orderData.get("id");
        BigDecimal actualPrice = new BigDecimal(orderData.get("actualPrice").toString());

        // pay
        IGraphQLExecutionContext payCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__pay",
                ApiRequest.build(Map.of("orderId", orderId)));
        assertEquals(0, graphQLEngine.executeRpc(payCtx).getStatus(), "pay failed");

        // ship (admin) — test harness allows admin actions (mirrors testShipAndConfirm)
        IGraphQLExecutionContext shipCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__ship",
                ApiRequest.build(Map.of("orderId", orderId, "shipSn", "SF1", "shipChannel", "顺丰")));
        assertEquals(0, graphQLEngine.executeRpc(shipCtx).getStatus(), "ship failed");

        int before = getMyPointsForTest("1");

        // confirm
        IGraphQLExecutionContext confirmCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__confirm",
                ApiRequest.build(Map.of("orderId", orderId)));
        assertEquals(0, graphQLEngine.executeRpc(confirmCtx).getStatus(), "confirm failed");

        int after = getMyPointsForTest("1");
        int expected = actualPrice.intValue(); // default earnPerYuan=1 → actualPrice points
        assertEquals(expected, after - before,
                "confirm should earn actualPrice points (earnPerYuan=1): expected " + expected);
    }

    @Test
    public void testCancelReturnsDeductedPoints() {
        // Seed points, submit with deduction, cancel the order → points must be returned.
        earnPointsForTest("1", 500,
                LitemallPointsAccountBizModel.SOURCE_TYPE_ORDER_CONFIRM_EARN, "seed-cancel-1");

        Map<String, Object> data = new HashMap<>();
        data.put("addressId", addressId);
        data.put("message", "取消返还积分测试");
        data.put("freightPrice", BigDecimal.ZERO);
        data.put("usePoints", 500);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", ApiRequest.build(data));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "submit failed: " + result);
        String orderId = (String) ((Map<String, Object>) result.getData()).get("id");

        // After submit, 500 points spent → balance 0
        assertEquals(0, getMyPointsForTest("1"));

        // cancel
        IGraphQLExecutionContext cancelCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__cancel",
                ApiRequest.build(Map.of("orderId", orderId)));
        assertEquals(0, graphQLEngine.executeRpc(cancelCtx).getStatus(), "cancel failed");

        // After cancel, the 500 deducted points are returned
        assertEquals(500, getMyPointsForTest("1"), "cancel should return the deducted points");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitWithPinTuan() {
        // P25 pin-tuan: pinTuanPrice = (retailPrice - activity.pinTuanPrice) * number, applied at
        // the actualPrice deduction layer. retail=99, pinTuanPrice=80, number=2 -> discount=38.
        LitemallPinTuanActivity activity = daoProvider.daoFor(LitemallPinTuanActivity.class).newEntity();
        activity.setGoodsId(goodsId);
        activity.setPinTuanPrice(new BigDecimal("80"));
        activity.setMinUserCount(2);
        activity.setExpireHours(24);
        activity.setStatus(10);
        daoProvider.daoFor(LitemallPinTuanActivity.class).saveEntity(activity);
        String pinTuanActivityId = activity.orm_idString();

        Map<String, Object> data = new HashMap<>();
        data.put("addressId", addressId);
        data.put("message", "拼团下单测试");
        data.put("freightPrice", BigDecimal.ZERO);
        data.put("pinTuanActivityId", pinTuanActivityId);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", ApiRequest.build(data));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "submit with pin-tuan failed: " + result);

        Map<String, Object> orderData = (Map<String, Object>) result.getData();
        BigDecimal pinTuanPrice = new BigDecimal(orderData.get("pinTuanPrice").toString());
        BigDecimal actualPrice = new BigDecimal(orderData.get("actualPrice").toString());
        // discount = (99 - 80) * 2 = 38
        assertEquals(0, pinTuanPrice.compareTo(new BigDecimal("38.00")),
                "pinTuanPrice should be (retail - pinTuanPrice) * number = 38, got " + pinTuanPrice);
        // actualPrice = orderPrice(198) - pinTuanPrice(38) = 160
        assertEquals(0, actualPrice.compareTo(new BigDecimal("160.00")),
                "actualPrice should be 160.00, got " + actualPrice);
    }

    @Test
    public void testSubmitPinTuanGrouponMutex() {
        // P25 Decision: pin-tuan × groupon mutex — submitting both must be rejected.
        LitemallPinTuanActivity activity = daoProvider.daoFor(LitemallPinTuanActivity.class).newEntity();
        activity.setGoodsId(goodsId);
        activity.setPinTuanPrice(new BigDecimal("80"));
        activity.setMinUserCount(2);
        activity.setExpireHours(24);
        activity.setStatus(10);
        daoProvider.daoFor(LitemallPinTuanActivity.class).saveEntity(activity);
        String pinTuanActivityId = activity.orm_idString();

        app.mall.dao.entity.LitemallGrouponRules rules =
                daoProvider.daoFor(app.mall.dao.entity.LitemallGrouponRules.class).newEntity();
        rules.setGoodsId(goodsId);
        rules.setGoodsName("Test Goods");
        rules.setDiscount(new BigDecimal("10.00"));
        rules.setDiscountMember(2);
        rules.setExpireTime(LocalDateTime.now().plusDays(7));
        rules.setStatus(0);
        daoProvider.daoFor(app.mall.dao.entity.LitemallGrouponRules.class).saveEntity(rules);
        String grouponRulesId = rules.orm_idString();

        Map<String, Object> data = new HashMap<>();
        data.put("addressId", addressId);
        data.put("message", "拼团团购互斥测试");
        data.put("freightPrice", BigDecimal.ZERO);
        data.put("grouponRulesId", grouponRulesId);
        data.put("pinTuanActivityId", pinTuanActivityId);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", ApiRequest.build(data));
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertFalse(result.getStatus() == 0,
                "submit with both groupon and pin-tuan must be rejected: " + result);
    }
}
