package app.mall.service.entity;

import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
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
