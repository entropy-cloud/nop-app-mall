package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallGroupon;
import app.mall.dao.entity.LitemallGrouponRules;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallGrouponBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String goodsId;
    String productId;
    String rulesId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("GR001");
        goods.setName("团购测试商品");
        goods.setRetailPrice(new BigDecimal("100.00"));
        goods.setIsOnSale(true);
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.orm_idString();

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goodsId);
        product.setSpecifications("[\"标准\"]");
        product.setPrice(new BigDecimal("100.00"));
        product.setNumber(100);
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);
        productId = product.orm_idString();

        LitemallGrouponRules rules = daoProvider.daoFor(LitemallGrouponRules.class).newEntity();
        rules.setGoodsId(goodsId);
        rules.setGoodsName("团购测试商品");
        rules.setDiscount(new BigDecimal("20.00"));
        rules.setDiscountMember(3);
        rules.setExpireTime(LocalDateTime.now().plusDays(7));
        rules.setStatus(0);
        daoProvider.daoFor(LitemallGrouponRules.class).saveEntity(rules);
        rulesId = rules.orm_idString();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPublishAndUnpublishRules() {
        LitemallGrouponRules rules = daoProvider.daoFor(LitemallGrouponRules.class).newEntity();
        rules.setGoodsId(goodsId);
        rules.setGoodsName("待上线规则");
        rules.setDiscount(new BigDecimal("10.00"));
        rules.setDiscountMember(2);
        rules.setExpireTime(LocalDateTime.now().plusDays(7));
        rules.setStatus(2);
        daoProvider.daoFor(LitemallGrouponRules.class).saveEntity(rules);
        String newRulesId = rules.orm_idString();

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", newRulesId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGrouponRules__publishRules", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "publishRules failed: " + result);

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0, ((Number) data.get("status")).intValue());

        ApiRequest<Map<String, Object>> unpubReq = ApiRequest.build(Map.of("id", newRulesId));
        IGraphQLExecutionContext unpubCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGrouponRules__unpublishRules", unpubReq);
        ApiResponse<?> unpubResult = graphQLEngine.executeRpc(unpubCtx);
        assertEquals(0, unpubResult.getStatus(), "unpublishRules failed: " + unpubResult);

        Map<String, Object> unpubData = (Map<String, Object>) unpubResult.getData();
        assertEquals(2, ((Number) unpubData.get("status")).intValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListAvailableRules() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGrouponRules__listAvailableRules", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "listAvailableRules failed: " + result);

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        assertNotNull(pageData);
        List<Map<String, Object>> items = (List<Map<String, Object>>) pageData.get("items");
        assertFalse(items.isEmpty());

        boolean found = items.stream().anyMatch(i -> rulesId.equals(i.get("id")));
        assertTrue(found, "Created rules should be in available list");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOpenGroupon() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "rulesId", rulesId,
                "orderId", "1"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGroupon__openGroupon", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "openGroupon failed: " + result);

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals("0", data.get("grouponId"));
        assertEquals("1", data.get("userId"));
        assertEquals("1", data.get("creatorUserId"));
        assertEquals(1, ((Number) data.get("status")).intValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJoinGroupon() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ApiRequest<Map<String, Object>> openReq = ApiRequest.build(Map.of(
                "rulesId", rulesId,
                "orderId", "1"
        ));
        IGraphQLExecutionContext openCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGroupon__openGroupon", openReq);
        ApiResponse<?> openResult = graphQLEngine.executeRpc(openCtx);
        assertEquals(0, openResult.getStatus());
        String grouponId = (String) ((Map<String, Object>) openResult.getData()).get("id");

        ContextProvider.getOrCreateContext().setUserId("2");
        ApiRequest<Map<String, Object>> joinReq = ApiRequest.build(Map.of(
                "grouponId", grouponId,
                "orderId", "2"
        ));
        IGraphQLExecutionContext joinCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGroupon__joinGroupon", joinReq);
        ApiResponse<?> joinResult = graphQLEngine.executeRpc(joinCtx);
        assertEquals(0, joinResult.getStatus(), "joinGroupon failed: " + joinResult);

        Map<String, Object> joinData = (Map<String, Object>) joinResult.getData();
        assertEquals(grouponId, joinData.get("grouponId"));
        assertEquals("2", joinData.get("userId"));

        ContextProvider.getOrCreateContext().setUserId("1");
        ApiRequest<Map<String, Object>> ownReq = ApiRequest.build(Map.of(
                "grouponId", grouponId,
                "orderId", "3"
        ));
        IGraphQLExecutionContext ownCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGroupon__joinGroupon", ownReq);
        ApiResponse<?> ownResult = graphQLEngine.executeRpc(ownCtx);
        assertNotEquals(0, ownResult.getStatus(), "Join own groupon should fail");

        ContextProvider.getOrCreateContext().setUserId("2");
        ApiRequest<Map<String, Object>> againReq = ApiRequest.build(Map.of(
                "grouponId", grouponId,
                "orderId", "4"
        ));
        IGraphQLExecutionContext againCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGroupon__joinGroupon", againReq);
        ApiResponse<?> againResult = graphQLEngine.executeRpc(againCtx);
        assertNotEquals(0, againResult.getStatus(), "Join again should fail");

        ContextProvider.getOrCreateContext().setUserId("3");
        ApiRequest<Map<String, Object>> join2Req = ApiRequest.build(Map.of(
                "grouponId", grouponId,
                "orderId", "5"
        ));
        IGraphQLExecutionContext join2Ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGroupon__joinGroupon", join2Req);
        ApiResponse<?> join2Result = graphQLEngine.executeRpc(join2Ctx);
        assertEquals(0, join2Result.getStatus(), "Second joiner should succeed");

        ContextProvider.getOrCreateContext().setUserId("4");
        ApiRequest<Map<String, Object>> fullReq = ApiRequest.build(Map.of(
                "grouponId", grouponId,
                "orderId", "6"
        ));
        IGraphQLExecutionContext fullCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGroupon__joinGroupon", fullReq);
        ApiResponse<?> fullResult = graphQLEngine.executeRpc(fullCtx);
        assertNotEquals(0, fullResult.getStatus(), "Join when full should fail");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGrouponDetail() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ApiRequest<Map<String, Object>> openReq = ApiRequest.build(Map.of(
                "rulesId", rulesId,
                "orderId", "1"
        ));
        IGraphQLExecutionContext openCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGroupon__openGroupon", openReq);
        ApiResponse<?> openResult = graphQLEngine.executeRpc(openCtx);
        assertEquals(0, openResult.getStatus());
        String grouponId = (String) ((Map<String, Object>) openResult.getData()).get("id");

        ContextProvider.getOrCreateContext().setUserId("2");
        ApiRequest<Map<String, Object>> joinReq = ApiRequest.build(Map.of(
                "grouponId", grouponId,
                "orderId", "2"
        ));
        IGraphQLExecutionContext joinCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGroupon__joinGroupon", joinReq);
        ApiResponse<?> joinResult = graphQLEngine.executeRpc(joinCtx);
        assertEquals(0, joinResult.getStatus());

        ApiRequest<Map<String, Object>> detailReq = ApiRequest.build(Map.of("id", grouponId));
        IGraphQLExecutionContext detailCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGroupon__grouponDetail", detailReq);
        ApiResponse<?> detailResult = graphQLEngine.executeRpc(detailCtx);
        assertEquals(0, detailResult.getStatus(), "grouponDetail failed: " + detailResult);

        Map<String, Object> detailData = (Map<String, Object>) detailResult.getData();
        assertNotNull(detailData);
        assertEquals(grouponId, detailData.get("id"));
    }
}
