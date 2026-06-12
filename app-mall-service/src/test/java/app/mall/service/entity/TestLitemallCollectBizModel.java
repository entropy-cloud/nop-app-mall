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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallCollectBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String goodsId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G001");
        goods.setName("Test Goods");
        goods.setRetailPrice(BigDecimal.valueOf(99));
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.getId();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddCollect() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "type", 0,
                "valueId", goodsId
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCollect__addCollect", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "addCollect failed: " + result);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals(0, ((Number) data.get("type")).intValue());
        assertEquals(goodsId, data.get("valueId"));
    }

    @Test
    public void testDuplicateCollectRejected() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "type", 0,
                "valueId", goodsId
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCollect__addCollect", req);
        graphQLEngine.executeRpc(ctx);

        IGraphQLExecutionContext ctx2 = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCollect__addCollect", req);
        ApiResponse<?> result2 = graphQLEngine.executeRpc(ctx2);
        assertEquals(-1, result2.getStatus());
    }

    @Test
    public void testIsCollect() {
        ApiRequest<Map<String, Object>> addReq = ApiRequest.build(Map.of(
                "type", 0,
                "valueId", goodsId
        ));
        graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCollect__addCollect", addReq);
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCollect__addCollect", addReq));

        ApiRequest<Map<String, Object>> checkReq = ApiRequest.build(Map.of(
                "type", 0,
                "valueId", goodsId
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCollect__isCollect", checkReq);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());
        assertTrue((Boolean) result.getData());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveCollect() {
        ApiRequest<Map<String, Object>> addReq = ApiRequest.build(Map.of(
                "type", 0,
                "valueId", goodsId
        ));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCollect__addCollect", addReq));

        ApiRequest<Map<String, Object>> removeReq = ApiRequest.build(Map.of(
                "type", 0,
                "valueId", goodsId
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCollect__removeCollect", removeReq);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());

        ApiRequest<Map<String, Object>> checkReq = ApiRequest.build(Map.of(
                "type", 0,
                "valueId", goodsId
        ));
        IGraphQLExecutionContext checkCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCollect__isCollect", checkReq);
        ApiResponse<?> checkResult = graphQLEngine.executeRpc(checkCtx);
        assertFalse((Boolean) checkResult.getData());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListByType() {
        ApiRequest<Map<String, Object>> addReq = ApiRequest.build(Map.of(
                "type", 0,
                "valueId", goodsId
        ));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCollect__addCollect", addReq));

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of(
                "type", 0,
                "page", 1,
                "pageSize", 10
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCollect__listByType", listReq);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());
        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        assertNotNull(pageData);
        assertEquals(1, ((Number) ((java.util.List<?>) pageData.get("items")).size()));
    }
}
