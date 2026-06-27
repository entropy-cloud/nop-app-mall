package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallOrderGoods;
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

    @Test
    public void testDeleteGoodsWithOrderHistoryRejected() {
        NopFileRecord file = daoProvider.daoFor(NopFileRecord.class).newEntity();
        file.setFileId("og-pic");
        file.setBizObjName("LitemallOrderGoods");
        file.setBizObjId("temp");
        file.setFieldName("temp");
        file.setOriginFileId("og-pic");
        file.setFileName("og-pic.png");
        file.setFilePath("/test/og-pic.png");
        file.setFileExt("png");
        file.setMimeType("image/png");
        file.setIsPublic(true);
        daoProvider.daoFor(NopFileRecord.class).saveEntity(file);

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G_DEL");
        goods.setName("Delete Me");
        goods.setRetailPrice(BigDecimal.valueOf(99));
        goods.setIsOnSale(true);
        goods.setPicUrl("");
        goods.setShareUrl("");
        goods.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);

        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).newEntity();
        og.setOrderId("1");
        og.setGoodsId(goods.getId());
        og.setGoodsSn("G_DEL");
        og.setGoodsName("Delete Me");
        og.setProductId("1");
        og.setNumber(1);
        og.setPrice(BigDecimal.valueOf(99));
        og.setSpecifications("[]");
        og.setPicUrl("og-pic");
        og.setComment(0);
        daoProvider.daoFor(LitemallOrderGoods.class).saveEntity(og);

        ApiRequest<Map<String, Object>> delReq = ApiRequest.build(Map.of("id", goods.getId()));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGoods__delete", delReq);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(-1, result.getStatus(),
                "delete of goods with order history should be rejected: " + result);
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

    @SuppressWarnings("unchecked")
    @Test
    public void testFrontListByFlags() {
        LitemallGoods hotAndNew = daoProvider.daoFor(LitemallGoods.class).newEntity();
        hotAndNew.setGoodsSn("G007");
        hotAndNew.setName("Hot New Goods");
        hotAndNew.setRetailPrice(BigDecimal.valueOf(199));
        hotAndNew.setIsOnSale(true);
        hotAndNew.setIsHot(true);
        hotAndNew.setIsNew(true);
        hotAndNew.setPicUrl("");
        hotAndNew.setShareUrl("");
        hotAndNew.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(hotAndNew);

        LitemallGoods normal = daoProvider.daoFor(LitemallGoods.class).newEntity();
        normal.setGoodsSn("G008");
        normal.setName("Normal Goods");
        normal.setRetailPrice(BigDecimal.valueOf(299));
        normal.setIsOnSale(true);
        normal.setIsHot(false);
        normal.setIsNew(false);
        normal.setPicUrl("");
        normal.setShareUrl("");
        normal.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(normal);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "isHot", true,
                "isNew", true,
                "page", 1,
                "pageSize", 20
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGoods__frontListByFlags", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "frontListByFlags failed: " + result);

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        List<Map<String, Object>> items = (List<Map<String, Object>>) pageData.get("items");
        assertTrue(items.stream().anyMatch(i -> hotAndNew.getId().equals(i.get("id"))));
        assertFalse(items.stream().anyMatch(i -> normal.getId().equals(i.get("id"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFrontListByFlagsRecommend() {
        LitemallGoods recommendGoods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        recommendGoods.setGoodsSn("G009");
        recommendGoods.setName("Recommend Goods");
        recommendGoods.setRetailPrice(BigDecimal.valueOf(399));
        recommendGoods.setIsOnSale(true);
        recommendGoods.setIsRecommend(true);
        recommendGoods.setPicUrl("");
        recommendGoods.setShareUrl("");
        recommendGoods.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(recommendGoods);

        LitemallGoods recommendAndHot = daoProvider.daoFor(LitemallGoods.class).newEntity();
        recommendAndHot.setGoodsSn("G011");
        recommendAndHot.setName("Recommend and Hot Goods");
        recommendAndHot.setRetailPrice(BigDecimal.valueOf(459));
        recommendAndHot.setIsOnSale(true);
        recommendAndHot.setIsRecommend(true);
        recommendAndHot.setIsHot(true);
        recommendAndHot.setPicUrl("");
        recommendAndHot.setShareUrl("");
        recommendAndHot.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(recommendAndHot);

        LitemallGoods normal = daoProvider.daoFor(LitemallGoods.class).newEntity();
        normal.setGoodsSn("G010");
        normal.setName("Not Recommend Goods");
        normal.setRetailPrice(BigDecimal.valueOf(499));
        normal.setIsOnSale(true);
        normal.setIsRecommend(false);
        normal.setPicUrl("");
        normal.setShareUrl("");
        normal.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(normal);

        // isRecommend=true 命中推荐商品（含同时具备 isHot 的），不命中非推荐商品
        ApiRequest<Map<String, Object>> reqTrue = ApiRequest.build(Map.of(
                "isRecommend", true,
                "page", 1,
                "pageSize", 20
        ));
        IGraphQLExecutionContext ctxTrue = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGoods__frontListByFlags", reqTrue);
        ApiResponse<?> resultTrue = graphQLEngine.executeRpc(ctxTrue);
        assertEquals(0, resultTrue.getStatus(), "frontListByFlags isRecommend=true failed: " + resultTrue);

        Map<String, Object> pageTrue = (Map<String, Object>) resultTrue.getData();
        List<Map<String, Object>> itemsTrue = (List<Map<String, Object>>) pageTrue.get("items");
        assertTrue(itemsTrue.stream().anyMatch(i -> recommendGoods.getId().equals(i.get("id"))));
        assertTrue(itemsTrue.stream().anyMatch(i -> recommendAndHot.getId().equals(i.get("id"))));
        assertFalse(itemsTrue.stream().anyMatch(i -> normal.getId().equals(i.get("id"))));

        // 向后兼容：省略 isRecommend 时推荐商品仍出现在列表（与既有 frontList/frontListByFlags 行为一致）
        ApiRequest<Map<String, Object>> reqAll = ApiRequest.build(Map.of(
                "page", 1,
                "pageSize", 20
        ));
        IGraphQLExecutionContext ctxAll = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGoods__frontListByFlags", reqAll);
        ApiResponse<?> resultAll = graphQLEngine.executeRpc(ctxAll);
        assertEquals(0, resultAll.getStatus(), "frontListByFlags backward-compat failed: " + resultAll);

        Map<String, Object> pageAll = (Map<String, Object>) resultAll.getData();
        List<Map<String, Object>> itemsAll = (List<Map<String, Object>>) pageAll.get("items");
        assertTrue(itemsAll.stream().anyMatch(i -> recommendGoods.getId().equals(i.get("id"))));

        // 组合过滤：isRecommend=true 且 isHot=true 仅命中同时具备两标记的商品，排除仅 isRecommend 的与无标记的
        ApiRequest<Map<String, Object>> reqCombo = ApiRequest.build(Map.of(
                "isRecommend", true,
                "isHot", true,
                "page", 1,
                "pageSize", 20
        ));
        IGraphQLExecutionContext ctxCombo = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGoods__frontListByFlags", reqCombo);
        ApiResponse<?> resultCombo = graphQLEngine.executeRpc(ctxCombo);
        assertEquals(0, resultCombo.getStatus(), "frontListByFlags combo failed: " + resultCombo);

        Map<String, Object> pageCombo = (Map<String, Object>) resultCombo.getData();
        List<Map<String, Object>> itemsCombo = (List<Map<String, Object>>) pageCombo.get("items");
        assertTrue(itemsCombo.stream().anyMatch(i -> recommendAndHot.getId().equals(i.get("id"))));
        assertFalse(itemsCombo.stream().anyMatch(i -> recommendGoods.getId().equals(i.get("id"))));
        assertFalse(itemsCombo.stream().anyMatch(i -> normal.getId().equals(i.get("id"))));
    }
}
