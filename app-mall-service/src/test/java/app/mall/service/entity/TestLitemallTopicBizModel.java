package app.mall.service.entity;

import app.mall.dao.entity.LitemallTopic;
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
public class TestLitemallTopicBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String topicId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        LitemallTopic topic = daoProvider.daoFor(LitemallTopic.class).newEntity();
        topic.setTitle("Summer Sale");
        topic.setSubtitle("Great deals");
        topic.setContent("content here");
        topic.setPrice(BigDecimal.ZERO);
        topic.setSortOrder(1);
        topic.orm_propValueByName("status", 0);
        daoProvider.daoFor(LitemallTopic.class).saveEntity(topic);
        topicId = topic.getId();

        LitemallTopic offShelfTopic = daoProvider.daoFor(LitemallTopic.class).newEntity();
        offShelfTopic.setTitle("Off Shelf Topic");
        offShelfTopic.setSubtitle("Should not appear");
        offShelfTopic.setContent("hidden");
        offShelfTopic.setPrice(BigDecimal.ZERO);
        offShelfTopic.setSortOrder(2);
        offShelfTopic.orm_propValueByName("status", 1);
        daoProvider.daoFor(LitemallTopic.class).saveEntity(offShelfTopic);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFrontList() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallTopic__frontList", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "frontList failed: " + result);

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        assertNotNull(pageData);
        List<Map<String, Object>> items = (List<Map<String, Object>>) pageData.get("items");
        assertFalse(items.isEmpty());

        Map<String, Object> item = items.stream()
                .filter(i -> topicId.equals(i.get("id")))
                .findFirst()
                .orElse(null);
        assertNotNull(item);
        assertEquals("Summer Sale", item.get("title"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFrontListStatusFilter() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallTopic__frontList", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "frontList failed: " + result);

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        List<Map<String, Object>> items = (List<Map<String, Object>>) pageData.get("items");
        assertNotNull(items);

        boolean hasOffShelf = items.stream()
                .anyMatch(i -> "Off Shelf Topic".equals(i.get("title")));
        assertFalse(hasOffShelf, "Off-shelf topic should not appear in frontList");

        boolean hasOnShelf = items.stream()
                .anyMatch(i -> "Summer Sale".equals(i.get("title")));
        assertTrue(hasOnShelf, "On-shelf topic should appear in frontList");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOnShelfOffShelf() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", topicId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallTopic__offShelf", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "offShelf failed: " + result);

        req = ApiRequest.build(Map.of("id", topicId));
        ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallTopic__onShelf", req);
        result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "onShelf failed: " + result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFrontDetail() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", topicId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallTopic__frontDetail", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "frontDetail failed: " + result);

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals("Summer Sale", data.get("title"));
        assertEquals("Great deals", data.get("subtitle"));
        assertEquals("content here", data.get("content"));
    }
}
