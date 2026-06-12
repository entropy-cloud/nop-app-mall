package app.mall.service.entity;

import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallSearchHistoryBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");
    }

    @Test
    public void testRecordSearch() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "keyword", "手机", "from", "pc"
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallSearchHistory__recordSearch", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSameKeywordSameDayNoDuplicate() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "keyword", "手机", "from", "pc"
        ));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallSearchHistory__recordSearch", req));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallSearchHistory__recordSearch", req));

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        ApiResponse<?> listResult = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallSearchHistory__listSearchHistory", listReq));
        assertEquals(0, listResult.getStatus());
        Map<String, Object> pageData = (Map<String, Object>) listResult.getData();
        assertEquals(1, ((java.util.List<?>) pageData.get("items")).size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListAndClear() {
        ApiRequest<Map<String, Object>> req1 = ApiRequest.build(Map.of("keyword", "手机", "from", "pc"));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallSearchHistory__recordSearch", req1));

        ApiRequest<Map<String, Object>> req2 = ApiRequest.build(Map.of("keyword", "电脑", "from", "wx"));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallSearchHistory__recordSearch", req2));

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        ApiResponse<?> listResult = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallSearchHistory__listSearchHistory", listReq));
        Map<String, Object> pageData = (Map<String, Object>) listResult.getData();
        assertEquals(2, ((java.util.List<?>) pageData.get("items")).size());

        ApiRequest<Map<String, Object>> clearReq = ApiRequest.build(Map.of());
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallSearchHistory__clearSearchHistory", clearReq));

        ApiResponse<?> afterClear = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallSearchHistory__listSearchHistory", listReq));
        Map<String, Object> afterData = (Map<String, Object>) afterClear.getData();
        assertEquals(0, ((java.util.List<?>) afterData.get("items")).size());
    }
}
