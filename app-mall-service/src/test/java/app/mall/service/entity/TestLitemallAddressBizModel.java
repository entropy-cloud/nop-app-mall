package app.mall.service.entity;

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
public class TestLitemallAddressBizModel extends JunitBaseTestCase {

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
    public void testAddAndList() {
        ApiRequest<Map<String, Object>> addReq = ApiRequest.build(Map.of(
                "name", "张三", "phone", "13800138000",
                "province", "广东省", "city", "深圳市",
                "county", "南山区", "addressDetail", "科技园",
                "areaCode", "440305", "isDefault", false));
        IGraphQLExecutionContext addCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAddress__add", addReq);
        ApiResponse<?> addResult = graphQLEngine.executeRpc(addCtx);
        assertEquals(0, addResult.getStatus(), "add failed: " + addResult);

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of());
        IGraphQLExecutionContext listCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallAddress__list", listReq);
        ApiResponse<?> listResult = graphQLEngine.executeRpc(listCtx);
        assertEquals(0, listResult.getStatus(), "list failed: " + listResult);

        List<Map<String, Object>> list = (List<Map<String, Object>>) listResult.getData();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("张三", list.get(0).get("name"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDefaultAddress() {
        ApiRequest<Map<String, Object>> addReq1 = ApiRequest.build(Map.of(
                "name", "张三", "phone", "13800138000",
                "province", "广东省", "city", "深圳市",
                "county", "南山区", "addressDetail", "科技园",
                "areaCode", "440305", "isDefault", false));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAddress__add", addReq1));

        ApiRequest<Map<String, Object>> addReq2 = ApiRequest.build(Map.of(
                "name", "李四", "phone", "13900139000",
                "province", "北京市", "city", "北京市",
                "county", "朝阳区", "addressDetail", "望京",
                "areaCode", "110105", "isDefault", true));
        graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAddress__add", addReq2));

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of());
        IGraphQLExecutionContext listCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallAddress__list", listReq);
        ApiResponse<?> listResult = graphQLEngine.executeRpc(listCtx);
        assertEquals(0, listResult.getStatus());

        List<Map<String, Object>> list = (List<Map<String, Object>>) listResult.getData();
        assertEquals(2, list.size());

        long defaultCount = list.stream()
                .filter(a -> Boolean.TRUE.equals(a.get("isDefault")))
                .count();
        assertEquals(1, defaultCount);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetDefault() {
        ApiRequest<Map<String, Object>> addReq1 = ApiRequest.build(Map.of(
                "name", "张三", "phone", "13800138000",
                "province", "广东省", "city", "深圳市",
                "county", "南山区", "addressDetail", "科技园",
                "areaCode", "440305", "isDefault", false));
        ApiResponse<?> r1 = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAddress__add", addReq1));
        Map<String, Object> addr1 = (Map<String, Object>) r1.getData();

        ApiRequest<Map<String, Object>> addReq2 = ApiRequest.build(Map.of(
                "name", "李四", "phone", "13900139000",
                "province", "北京市", "city", "北京市",
                "county", "朝阳区", "addressDetail", "望京",
                "areaCode", "110105", "isDefault", false));
        ApiResponse<?> r2 = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAddress__add", addReq2));
        Map<String, Object> addr2 = (Map<String, Object>) r2.getData();

        ApiRequest<Map<String, Object>> setDefReq = ApiRequest.build(Map.of("id", addr2.get("id")));
        IGraphQLExecutionContext setDefCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAddress__setDefault", setDefReq);
        ApiResponse<?> setDefResult = graphQLEngine.executeRpc(setDefCtx);
        assertEquals(0, setDefResult.getStatus(), "setDefault failed: " + setDefResult);

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of());
        ApiResponse<?> listResult = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallAddress__list", listReq));
        List<Map<String, Object>> list = (List<Map<String, Object>>) listResult.getData();
        long defaultCount = list.stream()
                .filter(a -> Boolean.TRUE.equals(a.get("isDefault")))
                .count();
        assertEquals(1, defaultCount);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteAddress() {
        ApiRequest<Map<String, Object>> addReq = ApiRequest.build(Map.of(
                "name", "张三", "phone", "13800138000",
                "province", "广东省", "city", "深圳市",
                "county", "南山区", "addressDetail", "科技园",
                "areaCode", "440305", "isDefault", false));
        ApiResponse<?> addResult = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAddress__add", addReq));
        Map<String, Object> addr = (Map<String, Object>) addResult.getData();
        String addressId = (String) addr.get("id");

        ApiRequest<Map<String, Object>> delReq = ApiRequest.build(Map.of("id", addressId));
        IGraphQLExecutionContext delCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAddress__deleteAddress", delReq);
        ApiResponse<?> delResult = graphQLEngine.executeRpc(delCtx);
        assertEquals(0, delResult.getStatus(), "deleteAddress failed: " + delResult);

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of());
        ApiResponse<?> listResult = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallAddress__list", listReq));
        List<Map<String, Object>> list = (List<Map<String, Object>>) listResult.getData();
        assertTrue(list.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateAddress() {
        ApiRequest<Map<String, Object>> addReq = ApiRequest.build(Map.of(
                "name", "张三", "phone", "13800138000",
                "province", "广东省", "city", "深圳市",
                "county", "南山区", "addressDetail", "科技园",
                "areaCode", "440305", "isDefault", false));
        ApiResponse<?> addResult = graphQLEngine.executeRpc(graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAddress__add", addReq));
        Map<String, Object> addr = (Map<String, Object>) addResult.getData();
        String addressId = (String) addr.get("id");

        ApiRequest<Map<String, Object>> updateReq = ApiRequest.build(Map.of(
                "id", addressId,
                "name", "李四", "phone", "13900139000",
                "province", "北京市", "city", "北京市",
                "county", "朝阳区", "addressDetail", "望京",
                "areaCode", "110105", "isDefault", false));
        IGraphQLExecutionContext updateCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAddress__updateAddress", updateReq);
        ApiResponse<?> updateResult = graphQLEngine.executeRpc(updateCtx);
        assertEquals(0, updateResult.getStatus(), "updateAddress failed: " + updateResult);

        ApiRequest<Map<String, Object>> detailReq = ApiRequest.build(Map.of("id", addressId));
        IGraphQLExecutionContext detailCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallAddress__detail", detailReq);
        ApiResponse<?> detailResult = graphQLEngine.executeRpc(detailCtx);
        assertEquals(0, detailResult.getStatus(), "detail failed: " + detailResult);

        Map<String, Object> data = (Map<String, Object>) detailResult.getData();
        assertEquals("李四", data.get("name"));
        assertEquals("13900139000", data.get("tel"));
    }
}
