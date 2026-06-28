package app.mall.service.entity;

import app.mall.dao.entity.LitemallMaterial;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.autotest.NopTestProperty;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.commons.util.IoHelper;
import io.nop.dao.api.IDaoProvider;
import io.nop.file.core.IFileStore;
import io.nop.file.core.UploadRequestBean;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nop.api.core.beans.FilterBeans.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 素材管理（P37）IGraphQLEngine 测试：
 * uploadMaterial（文件解析 + 记录创建 + fileType 推断 image/video/file）、
 * searchMaterials（keyword/categoryId/fileType/tag 组合筛选）、
 * deleteMaterial（逻辑删除）。
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
@NopTestProperty(name = "nop.file.store-dir", value = "./target")
public class TestLitemallMaterialBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    IFileStore fileStore;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("admin");
    }

    @Test
    public void testUploadMaterialImage() {
        String fileRef = saveFile("logo.png", "image/png", "fake-image-bytes");
        Map<String, Object> data = new HashMap<>();
        data.put("fileUpload", fileRef);
        data.put("tag", "封面");

        Map<String, Object> result = (Map<String, Object>) callMutation("uploadMaterial", data);
        assertNotNull(result.get("id"));
        assertEquals("logo.png", result.get("name"));
        assertEquals("image", result.get("fileType"));
        assertEquals(16, ((Number) result.get("fileSize")).intValue());
        assertEquals("封面", result.get("tag"));
        assertNotNull(result.get("url"));
    }

    @Test
    public void testUploadMaterialVideoByExt() {
        // MIME 为通用 application/octet-stream，按扩展名推断为 video
        String fileRef = saveFile("clip.mp4", "application/octet-stream", "fake-video-content");
        Map<String, Object> data = new HashMap<>();
        data.put("fileUpload", fileRef);

        Map<String, Object> result = (Map<String, Object>) callMutation("uploadMaterial", data);
        assertEquals("video", result.get("fileType"));
    }

    @Test
    public void testUploadMaterialFileFallback() {
        String fileRef = saveFile("readme.pdf", "application/pdf", "pdf-content-here!");
        Map<String, Object> data = new HashMap<>();
        data.put("fileUpload", fileRef);

        Map<String, Object> result = (Map<String, Object>) callMutation("uploadMaterial", data);
        assertEquals("file", result.get("fileType"));
    }

    @Test
    public void testUploadMaterialFileEmpty() {
        Map<String, Object> data = new HashMap<>();
        data.put("fileUpload", "");
        ApiResponse<?> r = callMutationRaw("uploadMaterial", data);
        assertEquals(-1, r.getStatus());
    }

    @Test
    public void testSearchMaterialsByKeyword() {
        saveMaterialViaRpc("summer-banner.png", "image/png", "广告");
        saveMaterialViaRpc("intro-video.mp4", "video/mp4", "教程");

        Map<String, Object> data = new HashMap<>();
        data.put("keyword", "summer");
        data.put("page", 1);
        data.put("pageSize", 10);

        List<Map<String, Object>> items = pageItems(callQueryRaw("searchMaterials", data));
        assertTrue(items.stream().anyMatch(m -> "summer-banner.png".equals(m.get("name"))));
        assertTrue(items.stream().noneMatch(m -> "intro-video.mp4".equals(m.get("name"))));
    }

    @Test
    public void testSearchMaterialsByFileType() {
        saveMaterialViaRpc("ftype-img.png", "image/png", null);
        saveMaterialViaRpc("ftype-vid.mp4", "video/mp4", null);

        Map<String, Object> data = new HashMap<>();
        data.put("fileType", "video");
        data.put("page", 1);
        data.put("pageSize", 10);

        List<Map<String, Object>> items = pageItems(callQueryRaw("searchMaterials", data));
        assertTrue(items.stream().allMatch(m -> "video".equals(m.get("fileType"))));
    }

    @Test
    public void testDeleteMaterialLogicalDelete() {
        String id = saveMaterialViaRpc("del-target.png", "image/png", null);

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        ApiResponse<?> r = callMutationRaw("deleteMaterial", data);
        assertEquals(0, r.getStatus(), "deleteMaterial failed: " + r);

        // 逻辑删除：deleted 标记置位，常规查询（含 deleted=false 过滤）不再可见
        QueryBean q = new QueryBean();
        q.addFilter(eq(LitemallMaterial.PROP_NAME_name, "del-target.png"));
        assertEquals(0, daoProvider.daoFor(LitemallMaterial.class).findAllByQuery(q).size());
    }

    // ===== helpers =====

    // 上传文件并调用 uploadMaterial，返回创建记录的 id。
    // 文件名即为 LitemallMaterial.name（uploadMaterial 从 IFileRecord 取 getFileName），作为断言定位键。
    private String saveMaterialViaRpc(String fileName, String mimeType, String tag) {
        String fileRef = saveFile(fileName, mimeType, "content-" + fileName);
        Map<String, Object> data = new HashMap<>();
        data.put("fileUpload", fileRef);
        data.put("tag", tag);
        Map<String, Object> result = (Map<String, Object>) callMutation("uploadMaterial", data);
        return String.valueOf(result.get("id"));
    }

    private String saveFile(String fileName, String mimeType, String content) {
        byte[] bytes = content.getBytes();
        InputStream is = new ByteArrayInputStream(bytes);
        UploadRequestBean upload = new UploadRequestBean();
        upload.setFileName(fileName);
        upload.setBizObjName("LitemallMaterial");
        upload.setFieldName("url");
        upload.setLength(bytes.length);
        upload.setMimeType(mimeType);
        upload.setInputStream(is);
        String fileId = fileStore.saveFile(upload, 32 * 1024 * 1024);
        IoHelper.safeCloseObject(is);
        return fileStore.getFileLink(fileId);
    }

    private Object callMutation(String action, Map<String, Object> data) {
        ApiResponse<?> r = callMutationRaw(action, data);
        assertEquals(0, r.getStatus(), action + " failed: status=" + r.getStatus() + " msg=" + r.getMsg());
        return r.getData();
    }

    private ApiResponse<?> callMutationRaw(String action, Map<String, Object> data) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallMaterial__" + action, req);
        return graphQLEngine.executeRpc(ctx);
    }

    private ApiResponse<?> callQueryRaw(String action, Map<String, Object> data) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallMaterial__" + action, req);
        return graphQLEngine.executeRpc(ctx);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> pageItems(ApiResponse<?> r) {
        assertEquals(0, r.getStatus(), "query failed: " + r);
        Map<String, Object> page = (Map<String, Object>) r.getData();
        return (List<Map<String, Object>>) page.get("items");
    }
}
