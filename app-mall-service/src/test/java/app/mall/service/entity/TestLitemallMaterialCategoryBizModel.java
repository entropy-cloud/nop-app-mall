package app.mall.service.entity;

import app.mall.dao.entity.LitemallMaterialCategory;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 素材分类树（P37）IGraphQLEngine 测试：getCategoryTree 返回根节点列表，
 * 每个节点含 children，所有层级按 sortOrder 升序。
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallMaterialCategoryBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String parentId;
    String child1Id;
    String child2Id;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("admin");

        LitemallMaterialCategory parent = daoProvider.daoFor(LitemallMaterialCategory.class).newEntity();
        parent.setName("图片素材");
        parent.setSortOrder(2);
        daoProvider.daoFor(LitemallMaterialCategory.class).saveEntity(parent);
        parentId = parent.getId();

        LitemallMaterialCategory child1 = daoProvider.daoFor(LitemallMaterialCategory.class).newEntity();
        child1.setName("Banner");
        child1.setParentId(parentId);
        child1.setSortOrder(20);
        daoProvider.daoFor(LitemallMaterialCategory.class).saveEntity(child1);
        child1Id = child1.getId();

        LitemallMaterialCategory child2 = daoProvider.daoFor(LitemallMaterialCategory.class).newEntity();
        child2.setName("图标");
        child2.setParentId(parentId);
        child2.setSortOrder(10);
        daoProvider.daoFor(LitemallMaterialCategory.class).saveEntity(child2);
        child2Id = child2.getId();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCategoryTree() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallMaterialCategory__getCategoryTree", req);
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
        assertEquals("图片素材", parentNode.get("name"));

        List<Map<String, Object>> children = (List<Map<String, Object>>) parentNode.get("children");
        assertNotNull(children);
        assertEquals(2, children.size());
        // 按 sortOrder 升序：图标(10) 在前，Banner(20) 在后
        assertEquals(child2Id, children.get(0).get("id"));
        assertEquals("图标", children.get(0).get("name"));
        assertEquals(child1Id, children.get(1).get("id"));
        assertEquals("Banner", children.get(1).get("name"));
        assertTrue((Integer) children.get(0).get("sortOrder") <= (Integer) children.get(1).get("sortOrder"));
    }
}
