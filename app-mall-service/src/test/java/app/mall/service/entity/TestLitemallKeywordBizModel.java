package app.mall.service.entity;

import app.mall.dao.entity.LitemallKeyword;
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
public class TestLitemallKeywordBizModel extends JunitBaseTestCase {

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
    public void testGetHotKeywords() {
        LitemallKeyword kw = daoProvider.daoFor(LitemallKeyword.class).newEntity();
        kw.setKeyword("手机");
        kw.setIsHot(true);
        kw.setIsDefault(true);
        kw.setUrl("about:blank");
        kw.setSortOrder(0);
        daoProvider.daoFor(LitemallKeyword.class).saveEntity(kw);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallKeyword__getHotKeywords", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getHotKeywords failed: " + result);

        List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData();
        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertEquals("手机", list.get(0).get("keyword"));
        assertEquals(true, list.get(0).get("isHot"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetDefaultKeywords() {
        LitemallKeyword kw = daoProvider.daoFor(LitemallKeyword.class).newEntity();
        kw.setKeyword("手机");
        kw.setIsHot(true);
        kw.setIsDefault(true);
        kw.setUrl("about:blank");
        kw.setSortOrder(0);
        daoProvider.daoFor(LitemallKeyword.class).saveEntity(kw);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallKeyword__getDefaultKeywords", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getDefaultKeywords failed: " + result);

        List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData();
        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertEquals("手机", list.get(0).get("keyword"));
        assertEquals(true, list.get(0).get("isDefault"));
    }
}
