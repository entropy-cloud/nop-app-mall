package app.mall.service.entity;

import app.mall.dao.entity.LitemallComment;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nop.api.core.beans.FilterBeans.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 评论运营工作台（P36）IGraphQLEngine 测试：批量回复（含部分失败/校验失败）、
 * 批量审核下架/恢复（含非法动作）、工作台列表筛选（star/hasPicture/关键字/时间）。
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallCommentOpsWorkbench extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String commentId1;
    String commentId2;
    String commentId3;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("admin");

        commentId1 = createComment("质量很好，推荐购买", 5, true, "1");
        commentId2 = createComment("一般般", 3, false, "1");
        commentId3 = createComment("太差了", 1, false, "2");
    }

    private String createComment(String content, int star, boolean hasPicture, String valueId) {
        LitemallComment comment = daoProvider.daoFor(LitemallComment.class).newEntity();
        comment.setType(0);
        comment.setValueId(valueId);
        comment.setContent(content);
        comment.setUserId("user-" + star);
        comment.setStar(star);
        comment.setHasPicture(hasPicture);
        daoProvider.daoFor(LitemallComment.class).saveEntity(comment);
        return comment.orm_idString();
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<?> callMutation(String action, Map<String, Object> data) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallComment__" + action, req);
        return graphQLEngine.executeRpc(ctx);
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<?> callQuery(String action, Map<String, Object> data) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallComment__" + action, req);
        return graphQLEngine.executeRpc(ctx);
    }

    private List<Map<String, Object>> resultMaps(ApiResponse<?> r) {
        assertEquals(0, r.getStatus(), "call failed: status=" + r.getStatus() + " msg=" + r.getMsg());
        return (List<Map<String, Object>>) r.getData();
    }

    private int successCount(List<Map<String, Object>> results) {
        return (int) results.stream().filter(m -> Boolean.TRUE.equals(m.get("success"))).count();
    }

    private LitemallComment reload(String id) {
        return daoProvider.daoFor(LitemallComment.class).getEntityById(id);
    }

    // ===== 批量回复 =====

    @Test
    public void testBatchAdminReplySuccessAndPartialFailure() {
        ApiResponse<?> r = callMutation("batchAdminReply", Map.of(
                "items", List.of(
                        Map.of("commentId", commentId1, "adminContent", "感谢好评"),
                        Map.of("commentId", "non-existent", "adminContent", "回复"),
                        Map.of("commentId", commentId2, "adminContent", "感谢评价")
                )
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(3, results.size());
        assertEquals(2, successCount(results));

        assertEquals("感谢好评", reload(commentId1).getAdminContent());
        assertEquals("感谢评价", reload(commentId2).getAdminContent());
    }

    @Test
    public void testBatchAdminReplyValidationFailure() {
        ApiResponse<?> r = callMutation("batchAdminReply", Map.of(
                "items", List.of(
                        Map.of("commentId", commentId1, "adminContent", ""),
                        Map.of("commentId", "", "adminContent", "回复")
                )
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(2, results.size());
        assertEquals(0, successCount(results));
    }

    @Test
    public void testBatchAdminReplyEmptyRejected() {
        ApiResponse<?> r = callMutation("batchAdminReply", Map.of("items", List.of()));
        assertEquals(-1, r.getStatus(), "empty items should be rejected: " + r);
    }

    // ===== 批量审核（后置 Moderation）=====

    @Test
    public void testBatchModerateHideSuccess() {
        ApiResponse<?> r = callMutation("batchModerateComments", Map.of(
                "commentIds", List.of(commentId1, commentId3),
                "action", "hide"
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(2, results.size());
        assertEquals(2, successCount(results));

        assertTrue(reload(commentId1).getDeleted());
        assertTrue(reload(commentId3).getDeleted());
    }

    @Test
    public void testBatchModerateRestoreSuccess() {
        // 先下架
        callMutation("batchModerateComments", Map.of(
                "commentIds", List.of(commentId2), "action", "hide"));
        assertTrue(reload(commentId2).getDeleted());

        // 再恢复
        ApiResponse<?> r = callMutation("batchModerateComments", Map.of(
                "commentIds", List.of(commentId2), "action", "restore"
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(1, results.size());
        assertEquals(1, successCount(results));
        assertFalse(reload(commentId2).getDeleted());
    }

    @Test
    public void testBatchModeratePartialFailure() {
        ApiResponse<?> r = callMutation("batchModerateComments", Map.of(
                "commentIds", List.of(commentId1, "non-existent"),
                "action", "hide"
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(2, results.size());
        assertEquals(1, successCount(results));
    }

    @Test
    public void testBatchModerateInvalidActionRejected() {
        ApiResponse<?> r = callMutation("batchModerateComments", Map.of(
                "commentIds", List.of(commentId1),
                "action", "delete"
        ));
        assertEquals(-1, r.getStatus(), "invalid action should be rejected: " + r);
    }

    @Test
    public void testBatchModerateEmptyRejected() {
        ApiResponse<?> r = callMutation("batchModerateComments", Map.of(
                "commentIds", List.of(),
                "action", "hide"
        ));
        assertEquals(-1, r.getStatus(), "empty ids should be rejected: " + r);
    }

    // ===== 工作台列表筛选 =====

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCommentReviewListByStar() {
        ApiResponse<?> r = callQuery("getCommentReviewList", Map.of(
                "star", 5,
                "page", 1,
                "pageSize", 10
        ));
        assertEquals(0, r.getStatus(), "query failed: " + r);
        Map<String, Object> data = (Map<String, Object>) r.getData();
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        assertEquals(1, items.size());
        assertEquals("质量很好，推荐购买", items.get(0).get("content"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCommentReviewListByHasPicture() {
        ApiResponse<?> r = callQuery("getCommentReviewList", Map.of(
                "hasPicture", true,
                "page", 1,
                "pageSize", 10
        ));
        assertEquals(0, r.getStatus(), "query failed: " + r);
        Map<String, Object> data = (Map<String, Object>) r.getData();
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        assertEquals(1, items.size());
        assertEquals(5, items.get(0).get("star"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCommentReviewListByKeyword() {
        ApiResponse<?> r = callQuery("getCommentReviewList", Map.of(
                "keyword", "质量",
                "page", 1,
                "pageSize", 10
        ));
        assertEquals(0, r.getStatus(), "query failed: " + r);
        Map<String, Object> data = (Map<String, Object>) r.getData();
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        assertEquals(1, items.size());
        assertTrue(items.get(0).get("content").toString().contains("质量"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCommentReviewListAll() {
        ApiResponse<?> r = callQuery("getCommentReviewList", new HashMap<>());
        assertEquals(0, r.getStatus(), "query failed: " + r);
        Map<String, Object> data = (Map<String, Object>) r.getData();
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        assertEquals(3, items.size(), "should return all 3 comments");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCommentReviewListExcludeHidden() {
        // 下架 commentId3 后，默认工作台查询仍含未删除评论；此处验证下架后 total 减少
        callMutation("batchModerateComments", Map.of(
                "commentIds", List.of(commentId3), "action", "hide"));

        ApiResponse<?> r = callQuery("getCommentReviewList", new HashMap<>());
        assertEquals(0, r.getStatus(), "query failed: " + r);
        Map<String, Object> data = (Map<String, Object>) r.getData();
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        // 下架的评论 deleted=true，CrudBizModel 默认查询过滤 deleted（取决于 defaultPrepareQuery）
        // 工作台应仅返回未删除评论
        assertEquals(2, items.size(), "hidden comment should be excluded from review list");
    }
}
