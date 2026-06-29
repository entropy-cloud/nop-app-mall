package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallComment;
import app.mall.dao.entity.LitemallSystem;
import app.mall.dao.entity.LitemallUserMessage;
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
import static org.junit.jupiter.api.Assertions.assertNull;
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

    private String createCommentWithAuditStatus(String content, int star, String valueId, Integer auditStatus, String userId) {
        LitemallComment comment = daoProvider.daoFor(LitemallComment.class).newEntity();
        comment.setType(0);
        comment.setValueId(valueId);
        comment.setContent(content);
        comment.setUserId(userId);
        comment.setStar(star);
        comment.setHasPicture(false);
        if (auditStatus != null) {
            comment.orm_propValueByName(LitemallComment.PROP_NAME_auditStatus, auditStatus);
        }
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

    // ===== P36 successor 前置审核状态机：batchAuditComments + auditStatus 筛选 =====

    @Test
    public void testBatchAuditApprovePendingToApproved() {
        String c1 = createCommentWithAuditStatus("待审-1", 5, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING, "user-p1");
        String c2 = createCommentWithAuditStatus("待审-2", 4, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING, "user-p2");

        ApiResponse<?> r = callMutation("batchAuditComments", Map.of(
                "commentIds", List.of(c1, c2),
                "action", "approve"
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(2, results.size());
        assertEquals(2, successCount(results));

        assertEquals(_AppMallDaoConstants.COMMENT_AUDIT_STATUS_APPROVED,
                reload(c1).orm_propValueByName(LitemallComment.PROP_NAME_auditStatus));
        assertEquals(_AppMallDaoConstants.COMMENT_AUDIT_STATUS_APPROVED,
                reload(c2).orm_propValueByName(LitemallComment.PROP_NAME_auditStatus));
    }

    @Test
    public void testBatchAuditRejectPendingToRejected() {
        String c1 = createCommentWithAuditStatus("待审-r1", 5, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING, "user-r1");

        ApiResponse<?> r = callMutation("batchAuditComments", Map.of(
                "commentIds", List.of(c1),
                "action", "reject"
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(1, results.size());
        assertEquals(1, successCount(results));
        assertEquals(_AppMallDaoConstants.COMMENT_AUDIT_STATUS_REJECTED,
                reload(c1).orm_propValueByName(LitemallComment.PROP_NAME_auditStatus));
    }

    @Test
    public void testBatchAuditSkipsNonPending() {
        // PENDING 待审；APPROVED/null/REJECTED 应被跳过（非 PENDING 守卫）
        String pending = createCommentWithAuditStatus("p", 5, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING, "u1");
        String approved = createCommentWithAuditStatus("a", 5, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_APPROVED, "u2");
        // commentId1 在 setUp 创建（auditStatus=null），应被跳过

        ApiResponse<?> r = callMutation("batchAuditComments", Map.of(
                "commentIds", List.of(pending, approved, commentId1),
                "action", "approve"
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(3, results.size());
        assertEquals(1, successCount(results));
        // PENDING 推进到 APPROVED；其他保持原状
        assertEquals(_AppMallDaoConstants.COMMENT_AUDIT_STATUS_APPROVED,
                reload(pending).orm_propValueByName(LitemallComment.PROP_NAME_auditStatus));
        assertEquals(_AppMallDaoConstants.COMMENT_AUDIT_STATUS_APPROVED,
                reload(approved).orm_propValueByName(LitemallComment.PROP_NAME_auditStatus));
        assertNull(reload(commentId1).orm_propValueByName(LitemallComment.PROP_NAME_auditStatus));
    }

    @Test
    public void testBatchAuditPartialFailureNonExistent() {
        String pending = createCommentWithAuditStatus("p", 5, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING, "u1");

        ApiResponse<?> r = callMutation("batchAuditComments", Map.of(
                "commentIds", List.of(pending, "non-existent"),
                "action", "approve"
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(2, results.size());
        assertEquals(1, successCount(results));
    }

    @Test
    public void testBatchAuditInvalidActionRejected() {
        ApiResponse<?> r = callMutation("batchAuditComments", Map.of(
                "commentIds", List.of(commentId1),
                "action", "publish"
        ));
        assertEquals(-1, r.getStatus(), "invalid action should be rejected: " + r);
    }

    @Test
    public void testBatchAuditEmptyRejected() {
        ApiResponse<?> r = callMutation("batchAuditComments", Map.of(
                "commentIds", List.of(),
                "action", "approve"
        ));
        assertEquals(-1, r.getStatus(), "empty ids should be rejected: " + r);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCommentReviewListByAuditStatus() {
        createCommentWithAuditStatus("pending-1", 5, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING, "u-p1");
        createCommentWithAuditStatus("rejected-1", 5, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_REJECTED, "u-r1");
        createCommentWithAuditStatus("approved-1", 5, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_APPROVED, "u-a1");

        // 筛选 PENDING
        ApiResponse<?> pendingRes = callQuery("getCommentReviewList", Map.of(
                "auditStatus", _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING,
                "page", 1, "pageSize", 50));
        assertEquals(0, pendingRes.getStatus());
        List<Map<String, Object>> pendingItems = (List<Map<String, Object>>)
                ((Map<String, Object>) pendingRes.getData()).get("items");
        assertEquals(1, pendingItems.size());
        assertEquals("pending-1", pendingItems.get(0).get("content"));

        // 筛选 REJECTED
        ApiResponse<?> rejectedRes = callQuery("getCommentReviewList", Map.of(
                "auditStatus", _AppMallDaoConstants.COMMENT_AUDIT_STATUS_REJECTED,
                "page", 1, "pageSize", 50));
        assertEquals(0, rejectedRes.getStatus());
        List<Map<String, Object>> rejectedItems = (List<Map<String, Object>>)
                ((Map<String, Object>) rejectedRes.getData()).get("items");
        assertEquals(1, rejectedItems.size());
        assertEquals("rejected-1", rejectedItems.get(0).get("content"));

        // 不限定 → 全部（含 PENDING/APPROVED/REJECTED + setUp 的 3 条 auditStatus=null）
        ApiResponse<?> allRes = callQuery("getCommentReviewList", new HashMap<>());
        assertEquals(0, allRes.getStatus());
        List<Map<String, Object>> allItems = (List<Map<String, Object>>)
                ((Map<String, Object>) allRes.getData()).get("items");
        assertEquals(6, allItems.size(), "auditStatus 未指定时应返回全部（含各审核状态）");
    }

    // ===== 评价奖励站内信（approve / reject 路径 + 非聚合语义，successor）=====

    private void seedCommentRewardConfig(String value) {
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName",
                LitemallPointsAccountBizModel.CONFIG_POINTS_COMMENT_REWARD);
        cfg.orm_propValueByName("keyValue", value);
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
    }

    private long countCommentRewardMessages(String userId) {
        QueryBean q = new QueryBean();
        q.addFilter(eq(LitemallUserMessage.PROP_NAME_userId, userId));
        q.addFilter(eq(LitemallUserMessage.PROP_NAME_msgType, _AppMallDaoConstants.MSG_TYPE_SYSTEM));
        q.addFilter(eq(LitemallUserMessage.PROP_NAME_title, "评价奖励到账"));
        return daoProvider.daoFor(LitemallUserMessage.class).findAllByQuery(q).size();
    }

    @Test
    public void testBatchAuditApprovePushesRewardMessage() {
        seedCommentRewardConfig("10");
        // userId must be numeric: litemall_points_account.user_id is an INTEGER-backed column, so a
        // non-numeric id fails type conversion when earnPoints auto-creates the account.
        String c1 = createCommentWithAuditStatus("待审-reward", 5, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING, "1001");

        long before = countCommentRewardMessages("1001");
        ApiResponse<?> r = callMutation("batchAuditComments", Map.of(
                "commentIds", List.of(c1), "action", "approve"));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(1, successCount(results));
        assertEquals(before + 1, countCommentRewardMessages("1001"),
                "approve with reward>0 must push one SYSTEM 评价奖励到账 message");
    }

    @Test
    public void testBatchAuditApproveNoMessageWhenRewardZero() {
        // No reward config ⇒ reward=0 ⇒ approve transitions status but pushes no message (Decision D3).
        String c1 = createCommentWithAuditStatus("待审-zero", 5, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING, "1002");
        long before = countCommentRewardMessages("1002");
        ApiResponse<?> r = callMutation("batchAuditComments", Map.of(
                "commentIds", List.of(c1), "action", "approve"));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(1, successCount(results));
        assertEquals(before, countCommentRewardMessages("1002"),
                "reward=0 approve must NOT push any message");
    }

    @Test
    public void testBatchAuditRejectPushesNoMessage() {
        seedCommentRewardConfig("10");
        String c1 = createCommentWithAuditStatus("待审-reject", 5, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING, "1003");
        long before = countCommentRewardMessages("1003");
        ApiResponse<?> r = callMutation("batchAuditComments", Map.of(
                "commentIds", List.of(c1), "action", "reject"));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(1, successCount(results));
        assertEquals(before, countCommentRewardMessages("1003"),
                "reject must NOT push any reward message");
    }

    @Test
    public void testCommentRewardNonAggregateSameUserTwoComments() {
        // Decision D4 non-aggregate: same user, same day, two distinct comments approved → each earns
        // reward → each receives one message (total 2), NOT deduplicated by a same-day title guard.
        seedCommentRewardConfig("5");
        String c1 = createCommentWithAuditStatus("待审-非聚合-1", 5, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING, "1004");
        String c2 = createCommentWithAuditStatus("待审-非聚合-2", 4, "1",
                _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING, "1004");

        long before = countCommentRewardMessages("1004");
        ApiResponse<?> r = callMutation("batchAuditComments", Map.of(
                "commentIds", List.of(c1, c2), "action", "approve"));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(2, successCount(results), "both comments should be approved");
        assertEquals(before + 2, countCommentRewardMessages("1004"),
                "non-aggregate: two distinct comments each push one message (total 2)");
    }
}
