package app.mall.service.entity;

import app.mall.dao.entity.LitemallAd;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.file.dao.entity.NopFileRecord;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallAdBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        NopFileRecord record = daoProvider.daoFor(NopFileRecord.class).newEntity();
        record.setFileId("ad-pic");
        record.setBizObjName("LitemallAd");
        record.setBizObjId("temp");
        record.setFieldName("url");
        record.setOriginFileId("ad-pic");
        record.setFileName("ad-pic.png");
        record.setFilePath("/test/ad-pic.png");
        record.setFileExt("png");
        record.setMimeType("image/png");
        record.setIsPublic(true);
        daoProvider.daoFor(NopFileRecord.class).saveEntity(record);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListActiveAds() {
        LitemallAd ad = daoProvider.daoFor(LitemallAd.class).newEntity();
        ad.setName("Banner");
        ad.setUrl("http://test.com/ad-pic.png");
        ad.setLink("about:blank");
        ad.setPosition(1);
        ad.setContent("Ad content");
        ad.setEnabled(true);
        ad.setStartTime(LocalDateTime.now().minusDays(1));
        ad.setEndTime(LocalDateTime.now().plusDays(1));
        daoProvider.daoFor(LitemallAd.class).saveEntity(ad);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallAd__listActiveAds", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "listActiveAds failed: " + result);

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        assertNotNull(pageData);
        List<Map<String, Object>> items = (List<Map<String, Object>>) pageData.get("items");
        assertFalse(items.isEmpty());
        assertEquals("Banner", items.get(0).get("name"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDisabledAdNotListed() {
        LitemallAd ad = daoProvider.daoFor(LitemallAd.class).newEntity();
        ad.setName("Disabled Banner");
        ad.setUrl("http://test.com/ad-pic.png");
        ad.setLink("about:blank");
        ad.setPosition(1);
        ad.setContent("Ad content");
        ad.setEnabled(false);
        ad.setStartTime(LocalDateTime.now().minusDays(1));
        ad.setEndTime(LocalDateTime.now().plusDays(1));
        daoProvider.daoFor(LitemallAd.class).saveEntity(ad);

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("page", 1, "pageSize", 10));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallAd__listActiveAds", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus());

        Map<String, Object> pageData = (Map<String, Object>) result.getData();
        List<Map<String, Object>> items = (List<Map<String, Object>>) pageData.get("items");
        assertTrue(items.isEmpty());
    }
}
