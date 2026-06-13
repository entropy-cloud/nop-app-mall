package app.mall.service.entity;

import app.mall.dao.entity.LitemallCategory;
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
public class TestLitemallCategoryBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String parentId;
    String childId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        LitemallCategory parent = daoProvider.daoFor(LitemallCategory.class).newEntity();
        parent.setName("Electronics");
        parent.setPid("0");
        parent.setLevel("1");
        daoProvider.daoFor(LitemallCategory.class).saveEntity(parent);
        parentId = parent.getId();

        LitemallCategory child = daoProvider.daoFor(LitemallCategory.class).newEntity();
        child.setName("Phones");
        child.setPid(parentId);
        child.setLevel("2");
        daoProvider.daoFor(LitemallCategory.class).saveEntity(child);
        childId = child.getId();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCategoryTree() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCategory__getCategoryTree", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getCategoryTree failed: " + result);

        List<Map<String, Object>> tree = (List<Map<String, Object>>) result.getData();
        assertNotNull(tree);
        assertFalse(tree.isEmpty());

        Map<String, Object> parentNode = tree.stream()
                .filter(n -> parentId.equals(n.get("id")))
                .findFirst()
                .orElse(null);
        assertNotNull(parentNode);
        assertEquals("Electronics", parentNode.get("name"));

        List<Map<String, Object>> children = (List<Map<String, Object>>) parentNode.get("children");
        assertNotNull(children);
        assertFalse(children.isEmpty());
        assertEquals(childId, children.get(0).get("id"));
        assertEquals("Phones", children.get(0).get("name"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCategoryList() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallCategory__getCategoryList", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getCategoryList failed: " + result);

        List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData();
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    public void testDeleteCategoryWithChildren() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", parentId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallCategory__delete", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(-1, result.getStatus());
    }
}
