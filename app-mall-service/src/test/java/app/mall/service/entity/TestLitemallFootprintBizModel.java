package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
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
public class TestLitemallFootprintBizModel extends JunitBaseTestCase {

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

    @Test
    public void testRecordFootprint() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "goodsId", goodsId
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallFootprint__recordFootprint", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "recordFootprint failed: " + result);
    }

    @Test
    public void testSameGoodsSameDayNoDuplicate() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "goodsId", goodsId
        ));

        IGraphQLExecutionContext ctx1 = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallFootprint__recordFootprint", req);
        graphQLEngine.executeRpc(ctx1);

        IGraphQLExecutionContext ctx2 = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallFootprint__recordFootprint", req);
        graphQLEngine.executeRpc(ctx2);

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of(
                "page", 1, "pageSize", 10
        ));
        IGraphQLExecutionContext listCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallFootprint__listFootprints", listReq);
        ApiResponse<?> listResult = graphQLEngine.executeRpc(listCtx);
        assertEquals(0, listResult.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> pageData = (Map<String, Object>) listResult.getData();
        assertEquals(1, ((java.util.List<?>) pageData.get("items")).size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListFootprints() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "goodsId", goodsId
        ));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallFootprint__recordFootprint", req));

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of(
                "page", 1, "pageSize", 10
        ));
        IGraphQLExecutionContext listCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallFootprint__listFootprints", listReq);
        ApiResponse<?> listResult = graphQLEngine.executeRpc(listCtx);
        assertEquals(0, listResult.getStatus());
        Map<String, Object> pageData = (Map<String, Object>) listResult.getData();
        assertNotNull(pageData);
        assertEquals(1, ((java.util.List<?>) pageData.get("items")).size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testClearFootprints() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "goodsId", goodsId
        ));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallFootprint__recordFootprint", req));

        ApiRequest<Map<String, Object>> clearReq = ApiRequest.build(Map.of());
        IGraphQLExecutionContext clearCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallFootprint__clearFootprints", clearReq);
        ApiResponse<?> clearResult = graphQLEngine.executeRpc(clearCtx);
        assertEquals(0, clearResult.getStatus());

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of(
                "page", 1, "pageSize", 10
        ));
        IGraphQLExecutionContext listCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallFootprint__listFootprints", listReq);
        ApiResponse<?> listResult = graphQLEngine.executeRpc(listCtx);
        Map<String, Object> pageData = (Map<String, Object>) listResult.getData();
        assertEquals(0, ((java.util.List<?>) pageData.get("items")).size());
    }
}
