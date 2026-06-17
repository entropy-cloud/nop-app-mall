package app.mall.service.entity;

import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallOrder;
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
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
        cart.setPicUrl("http://test.com/cart-pic.png");
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
                "submit failed: " + submitResult);
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
}
