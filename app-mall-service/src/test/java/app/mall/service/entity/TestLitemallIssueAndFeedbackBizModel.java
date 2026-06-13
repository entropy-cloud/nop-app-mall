package app.mall.service.entity;

import app.mall.dao.entity.LitemallIssue;
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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallIssueAndFeedbackBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListIssues() {
        LitemallIssue issue = daoProvider.daoFor(LitemallIssue.class).newEntity();
        issue.setQuestion("How to return?");
        issue.setAnswer("Contact support");
        daoProvider.daoFor(LitemallIssue.class).saveEntity(issue);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallIssue__listIssues", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "listIssues failed: " + result);

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        assertNotNull(pageData);
        List<Map<String, Object>> items = (List<Map<String, Object>>) pageData.get("items");
        assertFalse(items.isEmpty());
        assertEquals("How to return?", items.get(0).get("question"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubmitFeedback() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "feedType", "1", "content", "Great app!", "mobile", "13800138000"));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallFeedback__submitFeedback", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "submitFeedback failed: " + result);

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals("1", data.get("feedType"));
        assertEquals("Great app!", data.get("content"));
    }
}
