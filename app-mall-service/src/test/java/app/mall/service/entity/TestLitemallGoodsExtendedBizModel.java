package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallGoodsExtendedBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String goodsId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOnSaleOffSale() {
        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G001");
        goods.setName("Test Phone");
        goods.setRetailPrice(BigDecimal.valueOf(999));
        goods.setIsOnSale(false);
        goods.setPicUrl("");
        goods.setShareUrl("");
        goods.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.getId();

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goodsId);
        product.setNumber(100);
        product.setPrice(BigDecimal.valueOf(999));
        product.setSpecifications("[\"标准\"]");
        product.setUrl("");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);

        ApiRequest<Map<String, Object>> onSaleReq = ApiRequest.build(Map.of("id", goodsId));
        IGraphQLExecutionContext onSaleCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGoods__onSale", onSaleReq);
        ApiResponse<?> onSaleResult = graphQLEngine.executeRpc(onSaleCtx);
        assertEquals(0, onSaleResult.getStatus(), "onSale failed: " + onSaleResult);
        Map<String, Object> onSaleData = (Map<String, Object>) onSaleResult.getData();
        assertEquals(true, onSaleData.get("isOnSale"));

        ApiRequest<Map<String, Object>> offSaleReq = ApiRequest.build(Map.of("id", goodsId));
        IGraphQLExecutionContext offSaleCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGoods__offSale", offSaleReq);
        ApiResponse<?> offSaleResult = graphQLEngine.executeRpc(offSaleCtx);
        assertEquals(0, offSaleResult.getStatus(), "offSale failed: " + offSaleResult);
        Map<String, Object> offSaleData = (Map<String, Object>) offSaleResult.getData();
        assertEquals(false, offSaleData.get("isOnSale"));
    }

    @Test
    public void testOnSaleNoSkuRejected() {
        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G002");
        goods.setName("No SKU Product");
        goods.setRetailPrice(BigDecimal.valueOf(100));
        goods.setIsOnSale(false);
        goods.setPicUrl("");
        goods.setShareUrl("");
        goods.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", goods.getId()));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGoods__onSale", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(-1, result.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFrontList() {
        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G003");
        goods.setName("Test Phone");
        goods.setRetailPrice(BigDecimal.valueOf(999));
        goods.setIsOnSale(true);
        goods.setPicUrl("");
        goods.setShareUrl("");
        goods.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.getId();

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goodsId);
        product.setNumber(100);
        product.setPrice(BigDecimal.valueOf(999));
        product.setSpecifications("[\"标准\"]");
        product.setUrl("");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGoods__frontList", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "frontList failed: " + result);

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        assertNotNull(pageData);
        List<Map<String, Object>> items = (List<Map<String, Object>>) pageData.get("items");
        assertFalse(items.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFrontDetail() {
        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G004");
        goods.setName("Detail Phone");
        goods.setRetailPrice(BigDecimal.valueOf(999));
        goods.setIsOnSale(true);
        goods.setPicUrl("");
        goods.setShareUrl("");
        goods.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.getId();

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goodsId);
        product.setNumber(100);
        product.setPrice(BigDecimal.valueOf(999));
        product.setSpecifications("[\"标准\"]");
        product.setUrl("");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", goodsId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGoods__frontDetail", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "frontDetail failed: " + result);

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals(goodsId, data.get("id"));
    }

    @Test
    public void testFrontDetailNotOnSale() {
        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G005");
        goods.setName("Not On Sale Phone");
        goods.setRetailPrice(BigDecimal.valueOf(999));
        goods.setIsOnSale(false);
        goods.setPicUrl("");
        goods.setShareUrl("");
        goods.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", goods.getId()));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGoods__frontDetail", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(-1, result.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch() {
        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G006");
        goods.setName("Smart Phone");
        goods.setRetailPrice(BigDecimal.valueOf(999));
        goods.setIsOnSale(true);
        goods.setPicUrl("");
        goods.setShareUrl("");
        goods.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goods.getId());
        product.setNumber(100);
        product.setPrice(BigDecimal.valueOf(999));
        product.setSpecifications("[\"标准\"]");
        product.setUrl("");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "keyword", "Phone", "page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGoods__search", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "search failed: " + result);

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        assertNotNull(pageData);
        List<Map<String, Object>> items = (List<Map<String, Object>>) pageData.get("items");
        assertFalse(items.isEmpty());
    }
}
