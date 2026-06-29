package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.*;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.orm.IOrmTemplate;
import io.nop.file.dao.entity.NopFileRecord;
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

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallCommentBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    IOrmTemplate ormTemplate;

    String orderGoodsId;

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

        createFileRecord("goods-pic-1", "LitemallGoods", "picUrl");
        createFileRecord("share-1", "LitemallGoods", "shareUrl");
        createFileRecord("gallery-1", "LitemallGoods", "gallery");
        createFileRecord("product-pic-1", "LitemallGoodsProduct", "url");
        createFileRecord("cart-pic-1", "LitemallCart", "picUrl");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G001");
        goods.setName("Test Goods");
        goods.setRetailPrice(BigDecimal.valueOf(99));
        goods.setPicUrl("http://test.com/goods-pic-1.png");
        goods.setShareUrl("http://test.com/share-1.png");
        goods.setGallery("http://test.com/gallery-1.png");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goods.getId());
        product.setNumber(100);
        product.setPrice(BigDecimal.valueOf(99));
        product.setSpecifications("[\"标准\"]");
        product.setUrl("http://test.com/product-pic-1.png");
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
        cart.setPicUrl("/f/download/cart-pic-1");
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
        assertEquals(0, submitResult.getStatus(), "submit order failed: " + submitResult);
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
        QueryBean ogQuery = new QueryBean();
        ogQuery.addFilter(FilterBeans.eq(LitemallOrderGoods.PROP_NAME_orderId, orderId));
        LitemallOrderGoods orderGoods = daoProvider.daoFor(LitemallOrderGoods.class)
                .findFirstByQuery(ogQuery);
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
        createFileRecord("goods-pic-2", "LitemallGoods", "picUrl");
        createFileRecord("share-2", "LitemallGoods", "shareUrl");
        createFileRecord("gallery-2", "LitemallGoods", "gallery");
        createFileRecord("product-pic-2", "LitemallGoodsProduct", "url");
        createFileRecord("cart-pic-2", "LitemallCart", "picUrl");

        LitemallGoods goods2 = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods2.setGoodsSn("G002");
        goods2.setName("Goods 2");
        goods2.setRetailPrice(BigDecimal.valueOf(50));
        goods2.setPicUrl("http://test.com/goods-pic-2.png");
        goods2.setShareUrl("http://test.com/share-2.png");
        goods2.setGallery("http://test.com/gallery-2.png");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods2);

        LitemallGoodsProduct prod2 = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        prod2.setGoodsId(goods2.getId());
        prod2.setNumber(10);
        prod2.setPrice(BigDecimal.valueOf(50));
        prod2.setSpecifications("[\"默认\"]");
        prod2.setUrl("http://test.com/product-pic-2.png");
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
        cart2.setPicUrl("/f/download/cart-pic-2");
        daoProvider.daoFor(LitemallCart.class).saveEntity(cart2);

        LitemallAddress addr = daoProvider.daoFor(LitemallAddress.class).newEntity();
        addr.setUserId("1");
        addr.setName("李四");
        addr.setTel("13800138001");
        addr.setProvince("广东省");
        addr.setCity("深圳市");
        addr.setCounty("福田区");
        addr.setAddressDetail("中心区");
        addr.setIsDefault(false);
        daoProvider.daoFor(LitemallAddress.class).saveEntity(addr);

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addr.getId(), "message", "test2", "freightPrice", BigDecimal.ZERO));
        ApiResponse<?> submitRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq));
        assertEquals(0, submitRes.getStatus(), "submit order 2 failed: " + submitRes);
        @SuppressWarnings("unchecked")
        String orderId2 = (String) ((Map<String, Object>) submitRes.getData()).get("id");

        QueryBean ogQuery2 = new QueryBean();
        ogQuery2.addFilter(FilterBeans.eq(LitemallOrderGoods.PROP_NAME_orderId, orderId2));
        LitemallOrderGoods og2 = daoProvider.daoFor(LitemallOrderGoods.class)
                .findFirstByQuery(ogQuery2);
        String ogId2 = og2.orm_idString();

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
        ormTemplate.runInSession(session -> {
            LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class)
                    .getEntityById(orderGoodsId);
            og.setComment(-1);
            daoProvider.daoFor(LitemallOrderGoods.class).updateEntity(og);
            return null;
        });

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

    // ---------- P33 structured-comment tests ----------

    /**
     * Submit a second order on the SAME goods as the setUp orderGoods, run it through to CONFIRM,
     * and return the new orderGoodsId. Used by P33 aggregation/filter tests that need two comments
     * with the same valueId.
     */
    private String seedSecondOrderGoodsForSameGoods() {
        LitemallOrderGoods firstOg = daoProvider.daoFor(LitemallOrderGoods.class)
                .getEntityById(orderGoodsId);
        String goodsId = firstOg.getGoodsId();

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).getEntityById(goodsId);
        // find existing product on this goods
        QueryBean prodQuery = new QueryBean();
        prodQuery.addFilter(FilterBeans.eq(LitemallGoodsProduct.PROP_NAME_goodsId, goodsId));
        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class)
                .findFirstByQuery(prodQuery);

        LitemallCart cart2 = daoProvider.daoFor(LitemallCart.class).newEntity();
        cart2.setUserId("1");
        cart2.setGoodsId(goodsId);
        cart2.setProductId(product.getId());
        cart2.setNumber(1);
        cart2.setPrice(BigDecimal.valueOf(20));
        cart2.setChecked(true);
        cart2.setGoodsSn(goods.getGoodsSn());
        cart2.setGoodsName(goods.getName());
        cart2.setSpecifications(product.getSpecifications());
        cart2.setPicUrl("/f/download/cart-pic-1");
        daoProvider.daoFor(LitemallCart.class).saveEntity(cart2);

        LitemallAddress addr = daoProvider.daoFor(LitemallAddress.class).newEntity();
        addr.setUserId("1");
        addr.setName("P33");
        addr.setTel("13800138099");
        addr.setProvince("广东省");
        addr.setCity("深圳市");
        addr.setCounty("南山区");
        addr.setAddressDetail("P33");
        addr.setIsDefault(false);
        daoProvider.daoFor(LitemallAddress.class).saveEntity(addr);

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addr.getId(), "message", "p33", "freightPrice", BigDecimal.ZERO));
        ApiResponse<?> submitRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq));
        assertEquals(0, submitRes.getStatus(), "submit p33 order failed: " + submitRes);
        @SuppressWarnings("unchecked")
        String orderId2 = (String) ((Map<String, Object>) submitRes.getData()).get("id");

        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(GraphQLOperationType.mutation,
                "LitemallOrder__pay", ApiRequest.build(Map.of("orderId", orderId2))));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(GraphQLOperationType.mutation,
                "LitemallOrder__ship", ApiRequest.build(Map.of(
                        "orderId", orderId2, "shipSn", "P33", "shipChannel", "P33"))));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(GraphQLOperationType.mutation,
                "LitemallOrder__confirm", ApiRequest.build(Map.of("orderId", orderId2))));

        QueryBean ogQuery2 = new QueryBean();
        ogQuery2.addFilter(FilterBeans.eq(LitemallOrderGoods.PROP_NAME_orderId, orderId2));
        LitemallOrderGoods og2 = daoProvider.daoFor(LitemallOrderGoods.class)
                .findFirstByQuery(ogQuery2);
        return og2.orm_idString();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitCommentStructuredFields() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId,
                "content", "结构化评价",
                "star", 5,
                "pros", "[\"质量好\",\"物流快\"]",
                "cons", "[\"包装一般\"]",
                "semanticRating", 5
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "submitComment structured failed: " + result);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("[\"质量好\",\"物流快\"]", data.get("pros"));
        assertEquals("[\"包装一般\"]", data.get("cons"));
        assertEquals(5, ((Number) data.get("semanticRating")).intValue());

        // verify persisted
        String commentId = (String) data.get("id");
        LitemallComment persisted = daoProvider.daoFor(LitemallComment.class).getEntityById(commentId);
        assertNotNull(persisted);
        assertEquals("[\"质量好\",\"物流快\"]", persisted.getPros());
        assertEquals("[\"包装一般\"]", persisted.getCons());
        assertEquals(5, persisted.getSemanticRating());
    }

    @Test
    public void testSemanticRatingOutOfRangeRejected() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId,
                "content", "bad-rating",
                "star", 3,
                "semanticRating", 6
        ));
        ApiResponse<?> result = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req));
        assertEquals(-1, result.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCommentListFilterByShowType() {
        // first comment: star=5 (good), has picture
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment",
                ApiRequest.build(Map.of(
                        "orderGoodsId", orderGoodsId,
                        "content", "first", "star", 5,
                        "hasPicture", true, "picUrls", "http://x"))));
        // second comment on a new orderGoods: star=2 (bad), no picture
        String ogId2 = seedSecondOrderGoodsForSameGoods();
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment",
                ApiRequest.build(Map.of(
                        "orderGoodsId", ogId2, "content", "second", "star", 2))));

        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).getEntityById(orderGoodsId);
        String goodsId = og.getGoodsId();

        // all = 2
        ApiResponse<?> allRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__commentList",
                ApiRequest.build(Map.of("type", 0, "valueId", goodsId,
                        "showType", "all", "page", 1, "pageSize", 10))));
        assertEquals(0, allRes.getStatus());
        assertEquals(2, ((List<?>) ((Map<String, Object>) allRes.getData()).get("items")).size());

        // hasPicture = 1
        ApiResponse<?> picRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__commentList",
                ApiRequest.build(Map.of("type", 0, "valueId", goodsId,
                        "showType", "hasPicture", "page", 1, "pageSize", 10))));
        // note: hasPicture filter on DB requires the value actually persisted; submitComment coerced
        // hasPicture=true via Boolean.TRUE.equals for the first comment.
        assertEquals(0, picRes.getStatus());
        List<?> picItems = (List<?>) ((Map<String, Object>) picRes.getData()).get("items");
        assertEquals(1, picItems.size());

        // good (star>=4) = 1
        ApiResponse<?> goodRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__commentList",
                ApiRequest.build(Map.of("type", 0, "valueId", goodsId,
                        "showType", "good", "page", 1, "pageSize", 10))));
        assertEquals(0, goodRes.getStatus());
        assertEquals(1, ((List<?>) ((Map<String, Object>) goodRes.getData()).get("items")).size());

        // bad (star<=2) = 1
        ApiResponse<?> badRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__commentList",
                ApiRequest.build(Map.of("type", 0, "valueId", goodsId,
                        "showType", "bad", "page", 1, "pageSize", 10))));
        assertEquals(0, badRes.getStatus());
        assertEquals(1, ((List<?>) ((Map<String, Object>) badRes.getData()).get("items")).size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCommentSummaryAggregation() {
        // comment 1: star=5, pros=[质量好, 物流快], cons=[包装一般]
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment",
                ApiRequest.build(Map.of(
                        "orderGoodsId", orderGoodsId,
                        "content", "c1", "star", 5,
                        "pros", "[\"质量好\",\"物流快\"]",
                        "cons", "[\"包装一般\"]"))));
        // comment 2 on new orderGoods: star=2, pros=[], cons=[包装一般]
        String ogId2 = seedSecondOrderGoodsForSameGoods();
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment",
                ApiRequest.build(Map.of(
                        "orderGoodsId", ogId2,
                        "content", "c2", "star", 2,
                        "pros", "[]",
                        "cons", "[\"包装一般\"]"))));

        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).getEntityById(orderGoodsId);
        String goodsId = og.getGoodsId();

        ApiResponse<?> res = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__getCommentSummary",
                ApiRequest.build(Map.of("type", 0, "valueId", goodsId))));
        assertEquals(0, res.getStatus(), "getCommentSummary failed: " + res);
        Map<String, Object> summary = (Map<String, Object>) res.getData();
        assertEquals(2, ((Number) summary.get("totalCount")).intValue());
        // good = star>=4, so 1 of 2 = 50%
        assertEquals(50, ((Number) summary.get("goodRate")).intValue());

        Map<String, Object> starDist = (Map<String, Object>) summary.get("starDistribution");
        assertEquals(1, ((Number) starDist.get("5")).intValue());
        assertEquals(1, ((Number) starDist.get("2")).intValue());
        assertEquals(0, ((Number) starDist.get("3")).intValue());

        List<Map<String, Object>> prosTags = (List<Map<String, Object>>) summary.get("prosTags");
        // comment-1 had 质量好 + 物流快, comment-2 had []; counter has 2 entries each count=1
        assertEquals(2, prosTags.size());
        List<Map<String, Object>> consTags = (List<Map<String, Object>>) summary.get("consTags");
        // 包装一般 appears twice
        assertEquals(1, consTags.size());
        assertEquals("包装一般", consTags.get(0).get("tag"));
        assertEquals(2, ((Number) consTags.get(0).get("count")).intValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCommentSummaryEmpty() {
        // fresh goods with no comments
        LitemallGoods g = daoProvider.daoFor(LitemallGoods.class).newEntity();
        g.setGoodsSn("G-EMPTY");
        g.setName("Empty Goods");
        g.setRetailPrice(BigDecimal.ONE);
        daoProvider.daoFor(LitemallGoods.class).saveEntity(g);

        ApiResponse<?> res = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__getCommentSummary",
                ApiRequest.build(Map.of("type", 0, "valueId", g.getId()))));
        assertEquals(0, res.getStatus());
        Map<String, Object> summary = (Map<String, Object>) res.getData();
        assertEquals(0, ((Number) summary.get("totalCount")).intValue());
        assertEquals(0, ((Number) summary.get("goodRate")).intValue());
        assertTrue(((List<?>) summary.get("prosTags")).isEmpty());
        assertTrue(((List<?>) summary.get("consTags")).isEmpty());
    }

    @Test
    public void testCommentPointsRewardWhenConfigured() {
        // configure reward = 10
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName",
                LitemallPointsAccountBizModel.CONFIG_POINTS_COMMENT_REWARD);
        cfg.orm_propValueByName("keyValue", "10");
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId, "content", "rewarded", "star", 5));
        ApiResponse<?> submitRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req));
        assertEquals(0, submitRes.getStatus(), "submitComment w/ reward failed: " + submitRes);

        // Verify a points flow row with sourceType=comment-reward exists for user "1".
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType,
                LitemallPointsAccountBizModel.SOURCE_TYPE_COMMENT_REWARD));
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_userId, "1"));
        long count = daoProvider.daoFor(LitemallPointsFlow.class).findAllByQuery(q).size();
        assertEquals(1, count, "one comment-reward points flow must be written");
    }

    @Test
    public void testCommentPointsRewardDefaultOff() {
        // No config seeded in setUp; submitComment must NOT emit any comment-reward flow.
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId, "content", "no-reward", "star", 4));
        ApiResponse<?> submitRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req));
        assertEquals(0, submitRes.getStatus());

        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType,
                LitemallPointsAccountBizModel.SOURCE_TYPE_COMMENT_REWARD));
        long count = daoProvider.daoFor(LitemallPointsFlow.class).findAllByQuery(q).size();
        assertEquals(0, count, "default config 0 must NOT earn points");
    }

    // ===== 评价奖励站内信（submit 预审关即时路径，successor）=====

    private long countCommentRewardMessages(String userId) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_userId, userId));
        q.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_msgType,
                _AppMallDaoConstants.MSG_TYPE_SYSTEM));
        q.addFilter(FilterBeans.eq(LitemallUserMessage.PROP_NAME_title, "评价奖励到账"));
        return daoProvider.daoFor(LitemallUserMessage.class).findAllByQuery(q).size();
    }

    @Test
    public void testSubmitCommentRewardPushesMessage() {
        // reward>0 (pre-moderation OFF ⇒ submit 即发) ⇒ one SYSTEM 评价奖励到账 message.
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName",
                LitemallPointsAccountBizModel.CONFIG_POINTS_COMMENT_REWARD);
        cfg.orm_propValueByName("keyValue", "10");
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);

        long before = countCommentRewardMessages("1");
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId, "content", "rewarded-msg", "star", 5));
        ApiResponse<?> submitRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req));
        assertEquals(0, submitRes.getStatus(), "submitComment w/ reward failed: " + submitRes);
        assertEquals(before + 1, countCommentRewardMessages("1"),
                "reward>0 submit must push exactly one SYSTEM 评价奖励到账 message");
    }

    @Test
    public void testSubmitCommentNoMessageWhenRewardZero() {
        // No reward config ⇒ reward=0 ⇒ no message (Decision D3).
        long before = countCommentRewardMessages("1");
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId, "content", "zero-reward", "star", 4));
        ApiResponse<?> submitRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req));
        assertEquals(0, submitRes.getStatus());
        assertEquals(before, countCommentRewardMessages("1"),
                "reward=0 must NOT push any message");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCommentListBackwardCompatNoShowType() {
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment",
                ApiRequest.build(Map.of(
                        "orderGoodsId", orderGoodsId, "content", "compat", "star", 4))));
        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).getEntityById(orderGoodsId);
        // omit showType entirely — must be backward compatible
        ApiResponse<?> res = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__commentList",
                ApiRequest.build(Map.of("type", 0, "valueId", og.getGoodsId(),
                        "page", 1, "pageSize", 10))));
        assertEquals(0, res.getStatus());
        assertEquals(1, ((List<?>) ((Map<String, Object>) res.getData()).get("items")).size());
    }

    // ---------- P36 successor 前置审核状态机 tests ----------

    private void seedPreModerationConfig(String value) {
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName",
                LitemallCommentBizModel.CONFIG_COMMENT_PRE_MODERATION);
        cfg.orm_propValueByName("keyValue", value);
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
    }

    private void seedCommentRewardConfig(String value) {
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName",
                LitemallPointsAccountBizModel.CONFIG_POINTS_COMMENT_REWARD);
        cfg.orm_propValueByName("keyValue", value);
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPreModerationOnSubmitPendingAndNotPublic() {
        seedPreModerationConfig("1");
        // 同时配置奖励 10：预审 ON 时 submit 不应立即发积分
        seedCommentRewardConfig("10");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId, "content", "pending-comment", "star", 5));
        ApiResponse<?> res = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req));
        assertEquals(0, res.getStatus(), "submit failed: " + res);
        String commentId = (String) ((Map<String, Object>) res.getData()).get("id");

        // auditStatus=PENDING
        LitemallComment persisted = daoProvider.daoFor(LitemallComment.class).getEntityById(commentId);
        assertEquals(_AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING,
                persisted.orm_propValueByName(LitemallComment.PROP_NAME_auditStatus));

        // 公共 commentList 应过滤掉 PENDING 评论
        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).getEntityById(orderGoodsId);
        ApiResponse<?> listRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__commentList",
                ApiRequest.build(Map.of("type", 0, "valueId", og.getGoodsId(),
                        "page", 1, "pageSize", 10))));
        assertEquals(0, listRes.getStatus());
        List<?> items = (List<?>) ((Map<String, Object>) listRes.getData()).get("items");
        assertEquals(0, items.size(), "PENDING comment must not be publicly visible");

        // 公共 getCommentSummary 也应过滤掉 PENDING（totalCount=0）
        ApiResponse<?> sumRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__getCommentSummary",
                ApiRequest.build(Map.of("type", 0, "valueId", og.getGoodsId()))));
        assertEquals(0, sumRes.getStatus());
        Map<String, Object> summary = (Map<String, Object>) sumRes.getData();
        assertEquals(0, ((Number) summary.get("totalCount")).intValue(),
                "PENDING comment must not be aggregated in summary");

        // 预审 ON 时 submit 不立即发积分
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType,
                LitemallPointsAccountBizModel.SOURCE_TYPE_COMMENT_REWARD));
        long count = daoProvider.daoFor(LitemallPointsFlow.class).findAllByQuery(q).size();
        assertEquals(0, count, "pre-mod ON must defer points issuance to approve");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPreModerationOffSubmitPublicAndRewardImmediate() {
        // 不设预审开关（默认 OFF）；配置奖励 10
        seedCommentRewardConfig("10");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId, "content", "public-comment", "star", 5));
        ApiResponse<?> res = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", req));
        assertEquals(0, res.getStatus());
        String commentId = (String) ((Map<String, Object>) res.getData()).get("id");

        // auditStatus 为 null（=已通过，公开可见）
        LitemallComment persisted = daoProvider.daoFor(LitemallComment.class).getEntityById(commentId);
        assertNull(persisted.orm_propValueByName(LitemallComment.PROP_NAME_auditStatus));

        // 公共 commentList 可见
        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).getEntityById(orderGoodsId);
        ApiResponse<?> listRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__commentList",
                ApiRequest.build(Map.of("type", 0, "valueId", og.getGoodsId(),
                        "page", 1, "pageSize", 10))));
        assertEquals(0, listRes.getStatus());
        List<?> items = (List<?>) ((Map<String, Object>) listRes.getData()).get("items");
        assertEquals(1, items.size(), "null auditStatus comment must be publicly visible");

        // submit 即发积分（行为同今）
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType,
                LitemallPointsAccountBizModel.SOURCE_TYPE_COMMENT_REWARD));
        long count = daoProvider.daoFor(LitemallPointsFlow.class).findAllByQuery(q).size();
        assertEquals(1, count, "pre-mod OFF must issue points at submit time");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPreModerationOnApprovePublishesAndIssuesPoints() {
        seedPreModerationConfig("1");
        seedCommentRewardConfig("10");

        // submit → PENDING
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId, "content", "to-approve", "star", 5));
        ApiResponse<?> submitRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", submitReq));
        assertEquals(0, submitRes.getStatus());
        String commentId = (String) ((Map<String, Object>) submitRes.getData()).get("id");
        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).getEntityById(orderGoodsId);

        // approve
        ApiResponse<?> approveRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__batchAuditComments",
                ApiRequest.build(Map.of("commentIds", List.of(commentId), "action", "approve"))));
        assertEquals(0, approveRes.getStatus(), "approve failed: " + approveRes);
        List<Map<String, Object>> results = (List<Map<String, Object>>) approveRes.getData();
        assertEquals(1, results.size());
        assertEquals(true, results.get(0).get("success"));

        // APPROVED + 公开可见
        LitemallComment persisted = daoProvider.daoFor(LitemallComment.class).getEntityById(commentId);
        assertEquals(_AppMallDaoConstants.COMMENT_AUDIT_STATUS_APPROVED,
                persisted.orm_propValueByName(LitemallComment.PROP_NAME_auditStatus));
        ApiResponse<?> listRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__commentList",
                ApiRequest.build(Map.of("type", 0, "valueId", og.getGoodsId(),
                        "page", 1, "pageSize", 10))));
        List<?> items = (List<?>) ((Map<String, Object>) listRes.getData()).get("items");
        assertEquals(1, items.size(), "APPROVED comment must be publicly visible");

        // 积分在 approve 时发放（幂等 sourceId=comment.id）
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType,
                LitemallPointsAccountBizModel.SOURCE_TYPE_COMMENT_REWARD));
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceId, commentId));
        long count = daoProvider.daoFor(LitemallPointsFlow.class).findAllByQuery(q).size();
        assertEquals(1, count, "approve must issue deferred points exactly once");

        // 重审（重复 approve 同 comment）应被守卫跳过且积分不重复发放
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__batchAuditComments",
                ApiRequest.build(Map.of("commentIds", List.of(commentId), "action", "approve"))));
        long count2 = daoProvider.daoFor(LitemallPointsFlow.class).findAllByQuery(q).size();
        assertEquals(1, count2, "re-approve must not duplicate points");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPreModerationOnRejectKeepsPrivateAndNoPoints() {
        seedPreModerationConfig("1");
        seedCommentRewardConfig("10");

        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "orderGoodsId", orderGoodsId, "content", "to-reject", "star", 1));
        ApiResponse<?> submitRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__submitComment", submitReq));
        assertEquals(0, submitRes.getStatus());
        String commentId = (String) ((Map<String, Object>) submitRes.getData()).get("id");
        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).getEntityById(orderGoodsId);

        // reject
        ApiResponse<?> rejectRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__batchAuditComments",
                ApiRequest.build(Map.of("commentIds", List.of(commentId), "action", "reject"))));
        assertEquals(0, rejectRes.getStatus());
        assertEquals(true, ((List<Map<String, Object>>) rejectRes.getData()).get(0).get("success"));

        // REJECTED + 公共不可见
        LitemallComment persisted = daoProvider.daoFor(LitemallComment.class).getEntityById(commentId);
        assertEquals(_AppMallDaoConstants.COMMENT_AUDIT_STATUS_REJECTED,
                persisted.orm_propValueByName(LitemallComment.PROP_NAME_auditStatus));
        ApiResponse<?> listRes = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__commentList",
                ApiRequest.build(Map.of("type", 0, "valueId", og.getGoodsId(),
                        "page", 1, "pageSize", 10))));
        List<?> items = (List<?>) ((Map<String, Object>) listRes.getData()).get("items");
        assertEquals(0, items.size(), "REJECTED comment must not be publicly visible");

        // reject 不发积分
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType,
                LitemallPointsAccountBizModel.SOURCE_TYPE_COMMENT_REWARD));
        long count = daoProvider.daoFor(LitemallPointsFlow.class).findAllByQuery(q).size();
        assertEquals(0, count, "reject must not issue points");
    }
}
