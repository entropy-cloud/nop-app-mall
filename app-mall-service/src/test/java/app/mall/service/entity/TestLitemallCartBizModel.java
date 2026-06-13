package app.mall.service.entity;

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

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallCartBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String goodsId;
    String productId;

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
    void setup() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        createFileRecord("goods-pic", "LitemallGoods");
        createFileRecord("product-pic", "LitemallGoodsProduct");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("TEST001");
        goods.setName("测试商品");
        goods.setPicUrl("http://test.com/goods-pic.png");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.orm_idString();

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goodsId);
        product.setPrice(new BigDecimal("99.00"));
        product.setNumber(100);
        product.setSpecifications("[\"标准\"]");
        product.setUrl("http://test.com/product-pic.png");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);
        productId = product.orm_idString();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddGoods() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "goodsId", goodsId,
                "productId", productId,
                "number", 2
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCart__addGoods", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals(2, ((Number) data.get("number")).intValue());
        assertEquals(productId, data.get("productId"));
        assertTrue((Boolean) data.get("checked"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddGoodsMerge() {
        ApiRequest<Map<String, Object>> req1 = ApiRequest.build(Map.of(
                "goodsId", goodsId,
                "productId", productId,
                "number", 2
        ));
        IGraphQLExecutionContext ctx1 = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCart__addGoods", req1);
        graphQLEngine.executeRpc(ctx1);

        ApiRequest<Map<String, Object>> req2 = ApiRequest.build(Map.of(
                "goodsId", goodsId,
                "productId", productId,
                "number", 3
        ));
        IGraphQLExecutionContext ctx2 = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCart__addGoods", req2);
        ApiResponse<?> result2 = graphQLEngine.executeRpc(ctx2);
        assertEquals(0, result2.getStatus());

        Map<String, Object> data = (Map<String, Object>) result2.getData();
        assertEquals(5, data.get("number"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateQuantity() throws Exception {
        String cartId = addToCartAndReturnId(2);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "id", cartId,
                "number", 5
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCart__updateQuantity", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(5, data.get("number"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCheckUncheck() throws Exception {
        String cartId = addToCartAndReturnId(1);

        ApiRequest<Map<String, Object>> uncheckReq = ApiRequest.build(Map.of("id", cartId));
        IGraphQLExecutionContext ctx1 = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCart__uncheck", uncheckReq);
        ApiResponse<?> result1 = graphQLEngine.executeRpc(ctx1);
        assertEquals(0, result1.getStatus());
        assertFalse((Boolean) ((Map<String, Object>) result1.getData()).get("checked"));

        ApiRequest<Map<String, Object>> checkReq = ApiRequest.build(Map.of("id", cartId));
        IGraphQLExecutionContext ctx2 = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCart__check", checkReq);
        ApiResponse<?> result2 = graphQLEngine.executeRpc(ctx2);
        assertEquals(0, result2.getStatus());
        assertTrue((Boolean) ((Map<String, Object>) result2.getData()).get("checked"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteCart() throws Exception {
        String cartId = addToCartAndReturnId(1);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", cartId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCart__deleteCart", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());

        LitemallCart cart = daoProvider.daoFor(LitemallCart.class).getEntityById(cartId);
        assertNotNull(cart);
        assertTrue(Boolean.TRUE.equals(cart.getDeleted()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCheckedList() throws Exception {
        addToCartAndReturnId(2);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCart__checkedList", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCheckAllUncheckAll() throws Exception {
        addToCartAndReturnId(2);

        ApiRequest<Map<String, Object>> uncheckAllReq = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx1 = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCart__uncheckAll", uncheckAllReq);
        graphQLEngine.executeRpc(ctx1);

        long unchecked = daoProvider.daoFor(LitemallCart.class).findAll().stream()
                .filter(c -> !Boolean.TRUE.equals(c.getChecked()))
                .count();
        assertTrue(unchecked > 0);

        ApiRequest<Map<String, Object>> checkAllReq = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx2 = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCart__checkAll", checkAllReq);
        graphQLEngine.executeRpc(ctx2);

        long checked = daoProvider.daoFor(LitemallCart.class).findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getChecked()))
                .count();
        assertTrue(checked > 0);
    }

    private String addToCartAndReturnId(int number) throws Exception {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "goodsId", goodsId,
                "productId", productId,
                "number", number
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCart__addGoods", req);
        ApiResponse<Map<String, Object>> result = (ApiResponse<Map<String, Object>>) graphQLEngine.executeRpc(ctx);
        assertNotNull(result.getData());
        return (String) result.getData().get("id");
    }
}
