package app.mall.service;

import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.context.ContextProvider;
import io.nop.api.core.util.FutureHelper;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.file.dao.entity.NopFileRecord;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallGoodsBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("0");
        ContextProvider.getOrCreateContext().setUserName("test");

        createFileRecord("8ab2d3287af0cefa2cc539e40600621d", "LitemallGoodsSpecification");
        createFileRecord("spec-pic-001", "LitemallGoodsSpecification");
        createFileRecord("product-pic-001", "LitemallGoodsProduct");
    }

    private void createFileRecord(String fileId, String bizObjName) {
        NopFileRecord record = daoProvider.daoFor(NopFileRecord.class).newEntity();
        record.setFileId(fileId);
        record.setBizObjName(bizObjName);
        record.setBizObjId("temp");
        record.setFieldName("picUrl");
        record.setOriginFileId(fileId);
        record.setFileName(fileId + ".png");
        record.setFilePath("/test/" + fileId + ".png");
        record.setFileExt("png");
        record.setMimeType("image/png");
        record.setIsPublic(true);
        daoProvider.daoFor(NopFileRecord.class).saveEntity(record);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSave() {
        Map<String, Object> data = new HashMap<>();
        data.put("goodsSn", "test-05");
        data.put("name", "测试商品5");
        data.put("counterPrice", 33);
        data.put("isNew", 1);
        data.put("unit", "个");

        Map<String, Object> spec = new HashMap<>();
        spec.put("specification", "规格");
        spec.put("value", "其他");
        spec.put("picUrl", "/f/download/8ab2d3287af0cefa2cc539e40600621d.png");
        data.put("specifications", new ArrayList<>(List.of(spec)));

        Map<String, Object> attr = new HashMap<>();
        attr.put("attribute", "长度");
        attr.put("value", "33");
        data.put("attributes", new ArrayList<>(List.of(attr)));

        Map<String, Object> product = new HashMap<>();
        product.put("specificationsComponent", new ArrayList<>(List.of("其他")));
        product.put("price", 25);
        product.put("number", "30");
        product.put("url", "/f/download/product-pic-001.png");
        data.put("products", new ArrayList<>(List.of(product)));

        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("data", data);
        ApiRequest<Map<String, Object>> request = ApiRequest.build(wrapper);
        IGraphQLExecutionContext context = graphQLEngine.newRpcContext(GraphQLOperationType.mutation,
                "LitemallGoods__save", request);
        ApiResponse<?> result = FutureHelper.syncGet(graphQLEngine.executeRpcAsync(context));

        assertEquals(0, result.getStatus());
        assertNotNull(result.getData());
        Map<String, Object> resultData = (Map<String, Object>) result.getData();
        assertEquals("test-05", resultData.get("goodsSn"));
    }
}
