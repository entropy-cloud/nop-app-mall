package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.*;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallCommentBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String orderGoodsId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G001");
        goods.setName("Test Goods");
        goods.setRetailPrice(BigDecimal.valueOf(99));
        goods.setPicUrl("");
        goods.setShareUrl("");
        goods.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goods.getId());
        product.setNumber(100);
        product.setPrice(BigDecimal.valueOf(99));
        product.setSpecifications("[\"标准\"]");
        product.setUrl("");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);

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

        LitemallCart cart = daoProvider.daoFor(LitemallCart.class).newEntity();
        cart.setUserId("1");
        cart.setGoodsId(goods.getId());
        cart.setProductId(product.getId());
        cart.setNumber(1);
        cart.setPrice(BigDecimal.valueOf(99));
        cart.setChecked(true);
        cart.setGoodsSn(goods.getGoodsSn());
        cart.setGoodsName(goods.getName());
        cart.setSpecifications("[\"标准\"]");
        cart.setPicUrl("");
        daoProvider.daoFor(LitemallCart.class).saveEntity(cart);

        // submit order
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", address.getId(),
                "message", "test",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext submitCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> submitResult = graphQLEngine.executeRpc(submitCtx);
        assertEquals(0, submitResult.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> orderData = (Map<String, Object>) submitResult.getData();
        String orderId = (String) orderData.get("id");

        // pay
        ApiRequest<Map<String, Object>> payReq = ApiRequest.build(Map.of("orderId", orderId));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__pay", payReq));

        // ship
        ApiRequest<Map<String, Object>> shipReq = ApiRequest.build(Map.of(
                "orderId", orderId, "shipSn", "SF123", "shipChannel", "顺丰"));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__ship", shipReq));

        // confirm
        ApiRequest<Map<String, Object>> confirmReq = ApiRequest.build(Map.of("orderId", orderId));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__confirm", confirmReq));

        // get order goods id
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
        LitemallOrderGoods orderGoods = order.getOrderGoods().stream().findFirst().orElse(null);
        orderGoodsId = orderGoods.orm_idString();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitComment() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId,
                "content", "非常好的商品",
                "star", 5
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "submitComment failed: " + result);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals("非常好的商品", data.get("content"));
        assertEquals(5, ((Number) data.get("star")).intValue());
    }

    @Test
    public void testNotReceivedOrderRejected() {
        // Create a new order that is NOT confirmed
        LitemallGoods goods2 = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods2.setGoodsSn("G002");
        goods2.setName("Goods 2");
        goods2.setRetailPrice(BigDecimal.valueOf(50));
        goods2.setPicUrl("");
        goods2.setShareUrl("");
        goods2.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods2);

        LitemallGoodsProduct prod2 = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        prod2.setGoodsId(goods2.getId());
        prod2.setNumber(10);
        prod2.setPrice(BigDecimal.valueOf(50));
        prod2.setSpecifications("[\"默认\"]");
        prod2.setUrl("");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(prod2);

        LitemallCart cart2 = daoProvider.daoFor(LitemallCart.class).newEntity();
        cart2.setUserId("1");
        cart2.setGoodsId(goods2.getId());
        cart2.setProductId(prod2.getId());
        cart2.setNumber(1);
        cart2.setPrice(BigDecimal.valueOf(50));
        cart2.setChecked(true);
        cart2.setGoodsSn(goods2.getGoodsSn());
        cart2.setGoodsName(goods2.getName());
        cart2.setSpecifications("[\"默认\"]");
        cart2.setPicUrl("");
        daoProvider.daoFor(LitemallCart.class).saveEntity(cart2);

        LitemallAddress addr = daoProvider.daoFor(LitemallAddress.class).newEntity();
        addr.setUserId("1");
        addr.setName("李四");
        addr.setTel("13800138001");
        addr.setProvince("广东省");
        addr.setCity("深圳市");
        addr.setCounty("福田区");
        addr.setAddressDetail("中心区");
        daoProvider.daoFor(LitemallAddress.class).saveEntity(addr);

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addr.getId(), "message", "", "freightPrice", BigDecimal.ZERO));
        ApiResponse<?> submitRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq));
        assertEquals(0, submitRes.getStatus());
        @SuppressWarnings("unchecked")
        String orderId2 = (String) ((Map<String, Object>) submitRes.getData()).get("id");

        LitemallOrder order2 = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId2);
        String ogId2 = order2.getOrderGoods().stream().findFirst().map(g -> g.orm_idString()).orElse(null);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "orderGoodsId", ogId2, "content", "test", "star", 3));
        ApiResponse<?> result = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req));
        assertEquals(-1, result.getStatus());
    }

    @Test
    public void testDuplicateCommentRejected() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId, "content", "good", "star", 5));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req));

        ApiResponse<?> result2 = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req));
        assertEquals(-1, result2.getStatus());
    }

    @Test
    public void testExpiredCommentRejected() {
        // Mark orderGoods as expired comment
        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).getEntityById(orderGoodsId);
        og.setComment(-1);
        daoProvider.daoFor(LitemallOrderGoods.class).flushSession();

        // Need a fresh orderGoods for this - let's use a different approach
        // The entity is cached, so let's just test with a non-existent but valid scenario
        // Actually, let's reset
        og.setComment(0);
        daoProvider.daoFor(LitemallOrderGoods.class).flushSession();

        // Now mark as expired
        og.setComment(-1);
        daoProvider.daoFor(LitemallOrderGoods.class).flushSession();

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId, "content", "test", "star", 3));
        ApiResponse<?> result = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req));
        assertEquals(-1, result.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCommentList() {
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId, "content", "nice", "star", 4));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", submitReq));

        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).getEntityById(orderGoodsId);
        String goodsId = og.getGoodsId();

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of(
                "type", 0, "valueId", goodsId, "page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__commentList", listReq);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());
        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        assertEquals(1, ((java.util.List<?>) pageData.get("items")).size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAdminReply() {
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId, "content", "question", "star", 3));
        ApiResponse<?> submitRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", submitReq));
        String commentId = (String) ((Map<String, Object>) submitRes.getData()).get("id");

        ApiRequest<Map<String, Object>> replyReq = ApiRequest.build(Map.of(
                "id", commentId, "adminContent", "感谢您的评价"));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__adminReply", replyReq);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("感谢您的评价", data.get("adminContent"));
    }
}
