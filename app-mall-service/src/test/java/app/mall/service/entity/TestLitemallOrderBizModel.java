package app.mall.service.entity;

import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallCoupon;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallPromotionActivity;
import app.mall.dao.entity.LitemallPromotionTier;
import app.mall.dao.entity.LitemallSystem;
import app.mall.dao._AppMallDaoConstants;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.file.dao.entity.NopFileRecord;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import io.nop.orm.IOrmTemplate;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallOrderBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    IOrmTemplate ormTemplate;

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
        LitemallPromotionActivity a = daoProvider.daoFor(LitemallPromotionActivity.class).newEntity();
        a.setName("满减测试");
        a.setDiscountType(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT);
        a.setStatus(_AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        a.setGoodsScope(_AppMallDaoConstants.GOODS_SCOPE_ALL);
        a.setPriority(1);
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
}
