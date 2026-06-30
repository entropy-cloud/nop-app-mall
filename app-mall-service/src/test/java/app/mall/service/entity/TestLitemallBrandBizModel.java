package app.mall.service.entity;

import app.mall.dao.entity.LitemallBrand;
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
public class TestLitemallBrandBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String brandId;
    String deletedBrandId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        LitemallBrand brand = daoProvider.daoFor(LitemallBrand.class).newEntity();
        brand.orm_propValueByName("name", "Acme");
        brand.orm_propValueByName("desc", "Acme official brand");
        brand.orm_propValueByName("picUrl", "http://acme.png");
        brand.orm_propValueByName("sortOrder", 1);
        daoProvider.daoFor(LitemallBrand.class).saveEntity(brand);
        brandId = brand.getId();

        LitemallBrand deletedBrand = daoProvider.daoFor(LitemallBrand.class).newEntity();
        deletedBrand.orm_propValueByName("name", "Deleted Brand");
        deletedBrand.orm_propValueByName("desc", "should not appear");
        deletedBrand.orm_propValueByName("picUrl", "http://deleted.png");
        deletedBrand.orm_propValueByName("sortOrder", 2);
        deletedBrand.orm_propValueByName("deleted", true);
        daoProvider.daoFor(LitemallBrand.class).saveEntity(deletedBrand);
        deletedBrandId = deletedBrand.getId();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFrontList() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallBrand__frontList", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "frontList failed: " + result);

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        assertNotNull(pageData);
        List<Map<String, Object>> items = (List<Map<String, Object>>) pageData.get("items");
        assertNotNull(items);
        assertFalse(items.isEmpty());

        Map<String, Object> item = items.stream()
                .filter(i -> brandId.equals(i.get("id")))
                .findFirst()
                .orElse(null);
        assertNotNull(item);
        assertEquals("Acme", item.get("name"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFrontListExcludesDeleted() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallBrand__frontList", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        List<Map<String, Object>> items = (List<Map<String, Object>>) pageData.get("items");
        assertNotNull(items);

        boolean hasDeleted = items.stream()
                .anyMatch(i -> deletedBrandId.equals(i.get("id")));
        assertFalse(hasDeleted, "Deleted brand should not appear in frontList");

        boolean hasActive = items.stream()
                .anyMatch(i -> brandId.equals(i.get("id")));
        assertTrue(hasActive, "Active brand should appear in frontList");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFrontDetail() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", brandId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallBrand__frontDetail", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "frontDetail failed: " + result);

        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals("Acme", data.get("name"));
        assertEquals("Acme official brand", data.get("desc"));
    }

    @Test
    public void testFrontDetailNotFound() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", "non-existent-brand"));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallBrand__frontDetail", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus(), "frontDetail should fail for missing brand");
    }

    @Test
    public void testFrontDetailDeletedThrows() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", deletedBrandId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallBrand__frontDetail", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus(), "frontDetail should fail for deleted brand");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAnonymousAccess() {
        ContextProvider.getOrCreateContext().setUserId(null);
        ContextProvider.getOrCreateContext().setUserName(null);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallBrand__frontList", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "frontList must be callable anonymously (publicAccess)");

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        assertNotNull(pageData);
    }
}
