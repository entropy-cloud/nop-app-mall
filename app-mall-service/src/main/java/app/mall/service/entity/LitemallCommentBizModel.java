package app.mall.service.entity;

import app.mall.biz.ILitemallCommentBiz;
import app.mall.biz.ILitemallOrderGoodsBiz;
import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.dto.BatchCommentResultBean;
import app.mall.dao.entity.LitemallComment;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.mapper.LitemallOrderGoodsMapper;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.core.lang.json.JsonTool;
import jakarta.inject.Inject;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallComment")
public class LitemallCommentBizModel extends CrudBizModel<LitemallComment> implements ILitemallCommentBiz {

    // Filter enum values for commentList (P33 Decision: single-value, mutually exclusive).
    static final String SHOW_TYPE_ALL = "all";
    static final String SHOW_TYPE_HAS_PICTURE = "hasPicture";
    static final String SHOW_TYPE_GOOD = "good";
    static final String SHOW_TYPE_BAD = "bad";

    // Star thresholds matching P33 Decision A (good rate uses star >= 4).
    static final int GOOD_STAR_THRESHOLD = 4;
    static final int BAD_STAR_THRESHOLD = 2;

    // Top-N tags returned by getCommentSummary (P33 Decision A: app-layer aggregation).
    static final int TAG_TOP_N = 10;

    @Inject
    ILitemallOrderGoodsBiz orderGoodsBiz;

    @Inject
    LitemallOrderGoodsMapper orderGoodsMapper;

    @Inject
    ILitemallPointsAccountBiz pointsAccountBiz;

    @Inject
    ILitemallSystemBiz systemBiz;

    public LitemallCommentBizModel() {
        setEntityName(LitemallComment.class.getName());
    }

    @Override
    @BizMutation
    public LitemallComment submitComment(@Name("orderGoodsId") String orderGoodsId,
                                          @Name("content") String content,
                                          @Name("star") int star,
                                          @Optional @Name("hasPicture") Boolean hasPicture,
                                          @Optional @Name("picUrls") String picUrls,
                                          @Optional @Name("pros") String pros,
                                          @Optional @Name("cons") String cons,
                                          @Optional @Name("semanticRating") Integer semanticRating,
                                          IServiceContext context) {
        String userId = context.getUserId();

        if (semanticRating != null && (semanticRating < 1 || semanticRating > 5)) {
            throw new NopException(ERR_COMMENT_SEMANTIC_RATING_INVALID)
                    .param("semanticRating", semanticRating);
        }

        LitemallOrderGoods orderGoods = orderGoodsBiz.get(orderGoodsId, false, context);
        if (orderGoods == null) {
            throw new NopException(ERR_COMMENT_ORDER_GOODS_NOT_FOUND)
                    .param("orderGoodsId", orderGoodsId);
        }

        LitemallOrder order = orderGoods.getOrder();
        int orderStatus = order.getOrderStatus();
        if (orderStatus != _AppMallDaoConstants.ORDER_STATUS_CONFIRM
                && orderStatus != _AppMallDaoConstants.ORDER_STATUS_AUTO_CONFIRM) {
            throw new NopException(ERR_COMMENT_ORDER_NOT_RECEIVED)
                    .param("orderGoodsId", orderGoodsId)
                    .param("orderStatus", orderStatus);
        }

        if (!order.getUserId().equals(userId)) {
            throw new NopException(ERR_COMMENT_NOT_OWNER)
                    .param("orderGoodsId", orderGoodsId);
        }

        Integer commentFlag = orderGoods.getComment();
        if (commentFlag != null && commentFlag == -1) {
            throw new NopException(ERR_COMMENT_EXPIRED)
                    .param("orderGoodsId", orderGoodsId);
        }
        if (commentFlag != null && commentFlag > 0) {
            throw new NopException(ERR_COMMENT_ALREADY_EXISTS)
                    .param("orderGoodsId", orderGoodsId);
        }

        LitemallComment comment = newEntity();
        comment.setType(0);
        comment.setValueId(orderGoods.getGoodsId());
        comment.setContent(content);
        comment.setUserId(userId);
        comment.setStar(star);
        comment.setHasPicture(Boolean.TRUE.equals(hasPicture));
        if (picUrls != null) {
            comment.setPicUrls(picUrls);
        }
        if (pros != null) {
            comment.setPros(pros);
        }
        if (cons != null) {
            comment.setCons(cons);
        }
        if (semanticRating != null) {
            comment.setSemanticRating(semanticRating);
        }
        // P36 successor 前置审核：预审 ON 时新评论置 PENDING（公开不可见，待管理员审核），
        // 且积分延迟至审核通过发放（见 batchAuditComments approve 分支）。
        // 预审 OFF 时 auditStatus 保持 null（=已通过，公开可见，行为同今），积分 submit 即发。
        boolean preModeration = isPreModerationEnabled(context);
        if (preModeration) {
            comment.setAuditStatus(_AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING);
        }
        saveEntity(comment, null, context);

        // Atomic conditional UPDATE: win the "first comment" race only if orderGoods.comment is still 0.
        // Affects 0 means a concurrent submission already claimed this slot.
        int affected = orderGoodsMapper.updateCommentFlagIfUnused(orderGoodsId, comment.orm_idString());
        if (affected == 0) {
            throw new NopException(ERR_COMMENT_ALREADY_EXISTS)
                    .param("orderGoodsId", orderGoodsId);
        }

        if (order.getComments() != null && order.getComments() > 0) {
            order.setComments(order.getComments() - 1);
        }

        // P33 comment-reward points integration (gated by mall_points_comment_reward, default 0=off).
        // Idempotency is inherited from P27 (sourceType=comment-reward, sourceId=comment.id).
        // P36 successor: 预审 ON 时积分延迟至 batchAuditComments approve 分支发放；OFF 时维持 submit 即发。
        if (!preModeration) {
            int reward = resolveCommentReward(context);
            if (reward > 0) {
                pointsAccountBiz.earnPoints(userId, reward,
                        _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN,
                        LitemallPointsAccountBizModel.SOURCE_TYPE_COMMENT_REWARD,
                        comment.orm_idString(),
                        "商品评价奖励",
                        context);
            }
        }

        return comment;
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallComment> commentList(@Name("type") int type,
                                                   @Name("valueId") String valueId,
                                                   @Optional @Name("showType") String showType,
                                                   @Name("page") int page,
                                                   @Name("pageSize") int pageSize,
                                                   IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallComment.PROP_NAME_type, type));
        query.addFilter(FilterBeans.eq(LitemallComment.PROP_NAME_valueId, valueId));
        applyShowTypeFilter(query, showType);
        // P36 successor 前置审核：公共查询仅返回 APPROVED 或 auditStatus 为空（=已通过）的评论，
        // PENDING/REJECTED 不公开。auditStatus 的 OR(isNull, eq APPROVED) 复合过滤超出 xmeta 公共端
        // 允许的操作符面（与本文件 applyShowTypeFilter/getCommentReviewList 同类约束），故走
        // doFindPageByQueryDirectly（绕过 xmeta 操作符校验，不绕过 @Auth(publicAccess=true) 公开语义；
        // 查询仍由 BizModel 显式构造，仅暴露 valueId/type 维度 + auditStatus 过滤，与既有 getCommentReviewList
        // 同一安全模型）。
        applyPublicAuditFilter(query);
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallComment.PROP_NAME_addTime, true);

        return doFindPageByQueryDirectly(query, null, context);
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public Map<String, Object> getCommentSummary(@Name("type") int type,
                                                   @Name("valueId") String valueId,
                                                   IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallComment.PROP_NAME_type, type));
        query.addFilter(FilterBeans.eq(LitemallComment.PROP_NAME_valueId, valueId));
        // P36 successor 前置审核：汇总口径与 commentList 一致——仅聚合公开可见集合（APPROVED/null），
        // 避免未公开 PENDING 拉偏 goodRate/starDistribution/标签云。同样走 doFindListByQueryDirectly。
        applyPublicAuditFilter(query);

        List<LitemallComment> all = doFindListByQueryDirectly(query, context);

        Map<String, Object> summary = new LinkedHashMap<>();
        int totalCount = all.size();
        summary.put("totalCount", totalCount);

        int goodCount = 0;
        int[] starBuckets = new int[5];
        Map<String, Integer> prosCounter = new HashMap<>();
        Map<String, Integer> consCounter = new HashMap<>();
        for (LitemallComment c : all) {
            Integer star = c.getStar();
            int starVal = star == null ? 0 : star;
            if (starVal >= 1 && starVal <= 5) {
                starBuckets[starVal - 1]++;
            }
            if (starVal >= GOOD_STAR_THRESHOLD) {
                goodCount++;
            }
            aggregateTags(c.getPros(), prosCounter);
            aggregateTags(c.getCons(), consCounter);
        }
        int goodRate = totalCount == 0 ? 0 : Math.round((float) goodCount * 100f / totalCount);
        summary.put("goodRate", goodRate);

        Map<String, Integer> starDistribution = new LinkedHashMap<>();
        for (int s = 1; s <= 5; s++) {
            starDistribution.put(String.valueOf(s), starBuckets[s - 1]);
        }
        summary.put("starDistribution", starDistribution);

        summary.put("prosTags", topTags(prosCounter, TAG_TOP_N));
        summary.put("consTags", topTags(consCounter, TAG_TOP_N));
        return summary;
    }

    @Override
    @BizQuery
    public PageBean<LitemallComment> myComments(@Name("page") int page,
                                                  @Name("pageSize") int pageSize,
                                                  IServiceContext context) {
        String userId = context.getUserId();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallComment.PROP_NAME_userId, userId));
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallComment.PROP_NAME_addTime, true);

        return findPage(query, null, context);
    }

    @Override
    @BizMutation
    public LitemallComment adminReply(@Name("id") String id,
                                       @Name("adminContent") String adminContent,
                                       IServiceContext context) {
        LitemallComment comment = requireEntity(id, null, context);
        comment.setAdminContent(adminContent);
        return comment;
    }

    // ===== P36 评论运营工作台：批量回复 + 后置 Moderation + 工作台列表 =====

    static final String MODERATION_ACTION_HIDE = "hide";
    static final String MODERATION_ACTION_RESTORE = "restore";

    // ===== P36 successor 评论前置审核状态机：预审开关 + batchAuditComments =====

    static final String AUDIT_ACTION_APPROVE = "approve";
    static final String AUDIT_ACTION_REJECT = "reject";

    static final String CONFIG_COMMENT_PRE_MODERATION = "mall_comment_pre_moderation";

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public List<BatchCommentResultBean> batchAdminReply(@Name("items") List<Map<String, Object>> items,
                                                         IServiceContext context) {
        if (items == null || items.isEmpty()) {
            throw new NopException(ERR_COMMENT_BATCH_EMPTY);
        }
        List<BatchCommentResultBean> results = new ArrayList<>();
        int rowIndex = 0;
        for (Map<String, Object> item : items) {
            rowIndex++;
            String commentId = stringValue(item, "commentId");
            String adminContent = stringValue(item, "adminContent");

            if (StringHelper.isEmpty(commentId) || StringHelper.isEmpty(adminContent)) {
                results.add(new BatchCommentResultBean(commentId, false,
                        "数据行 " + rowIndex + " 字段缺失（commentId/adminContent 必填）"));
                continue;
            }
            try {
                LitemallComment comment = requireEntity(commentId, null, context);
                comment.setAdminContent(adminContent);
                results.add(new BatchCommentResultBean(commentId, true, null));
            } catch (NopException e) {
                results.add(new BatchCommentResultBean(commentId, false,
                        e.getDescription() != null ? e.getDescription() : e.getMessage()));
            } catch (Exception e) {
                results.add(new BatchCommentResultBean(commentId, false, e.getMessage()));
            }
        }
        return results;
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public List<BatchCommentResultBean> batchModerateComments(@Name("commentIds") List<String> commentIds,
                                                               @Name("action") String action,
                                                               IServiceContext context) {
        if (commentIds == null || commentIds.isEmpty()) {
            throw new NopException(ERR_COMMENT_BATCH_EMPTY);
        }
        boolean hide;
        if (MODERATION_ACTION_HIDE.equals(action)) {
            hide = true;
        } else if (MODERATION_ACTION_RESTORE.equals(action)) {
            hide = false;
        } else {
            throw new NopException(ERR_COMMENT_MODERATION_ACTION_INVALID).param("action", action);
        }
        List<BatchCommentResultBean> results = new ArrayList<>();
        for (String commentId : commentIds) {
            try {
                // 后置 Moderation 需操作已删除（deleted=true）的评论：requireEntity 走管道会被 deleted 过滤，
                // 此处用 dao().getEntityById() 按 PK 直取以支持 restore（合理例外，与 hide/restore 语义匹配）。
                LitemallComment comment = dao().getEntityById(commentId);
                if (comment == null) {
                    results.add(new BatchCommentResultBean(commentId, false, "评论不存在"));
                    continue;
                }
                comment.setDeleted(hide);
                results.add(new BatchCommentResultBean(commentId, true, null));
            } catch (Exception e) {
                results.add(new BatchCommentResultBean(commentId, false, e.getMessage()));
            }
        }
        return results;
    }

    // ===== P36 successor 前置审核状态机：batchAuditComments（PENDING→APPROVED/REJECTED）=====

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public List<BatchCommentResultBean> batchAuditComments(@Name("commentIds") List<String> commentIds,
                                                            @Name("action") String action,
                                                            IServiceContext context) {
        if (commentIds == null || commentIds.isEmpty()) {
            throw new NopException(ERR_COMMENT_BATCH_EMPTY);
        }
        boolean approve;
        if (AUDIT_ACTION_APPROVE.equals(action)) {
            approve = true;
        } else if (AUDIT_ACTION_REJECT.equals(action)) {
            approve = false;
        } else {
            throw new NopException(ERR_COMMENT_AUDIT_ACTION_INVALID).param("action", action);
        }
        // 预审 ON 时积分延迟至此发放；预审 OFF 时此处必为 null→PENDING 守卫跳过，不会重复发放。
        // reward=0（未配置）时跳过积分发放，仅置状态。
        int reward = approve ? resolveCommentReward(context) : 0;

        List<BatchCommentResultBean> results = new ArrayList<>();
        for (String commentId : commentIds) {
            try {
                // 审核动作需操作 PENDING 评论；requireEntity 走管道不会被 deleted 过滤（审核评论不应已删除），
                // 但与 batchModerateComments 同一调查路径便于一致性，仍用 dao().getEntityById() 直查。
                LitemallComment comment = dao().getEntityById(commentId);
                if (comment == null) {
                    results.add(new BatchCommentResultBean(commentId, false, "评论不存在"));
                    continue;
                }
                Integer current = comment.getAuditStatus();
                if (current == null || current != _AppMallDaoConstants.COMMENT_AUDIT_STATUS_PENDING) {
                    results.add(new BatchCommentResultBean(commentId, false,
                            "评论非待审核状态，跳过（当前 auditStatus=" + current + "）"));
                    continue;
                }
                if (approve) {
                    comment.setAuditStatus(_AppMallDaoConstants.COMMENT_AUDIT_STATUS_APPROVED);
                    // 预审 ON 延迟发放分支：经 I*LitemallPointsAccountBiz 发放积分，sourceId=comment.id 保幂等。
                    // earnPoints 在 (sourceType, sourceId) 已存在时抛 ERR_POINTS_DUPLICATE_EARN——视为已发放，
                    // 与状态推进幂等一致（重审不会重复发积分）。
                    if (reward > 0) {
                        try {
                            pointsAccountBiz.earnPoints(comment.getUserId(), reward,
                                    _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN,
                                    LitemallPointsAccountBizModel.SOURCE_TYPE_COMMENT_REWARD,
                                    comment.orm_idString(),
                                    "商品评价奖励",
                                    context);
                        } catch (NopException e) {
                            if (!ERR_POINTS_DUPLICATE_EARN.getErrorCode().equals(e.getErrorCode())) {
                                throw e;
                            }
                            // 幂等：积分已发放过，视为成功（状态推进仍继续）
                        }
                    }
                } else {
                    comment.setAuditStatus(_AppMallDaoConstants.COMMENT_AUDIT_STATUS_REJECTED);
                }
                results.add(new BatchCommentResultBean(commentId, true, null));
            } catch (NopException e) {
                results.add(new BatchCommentResultBean(commentId, false,
                        e.getDescription() != null ? e.getDescription() : e.getMessage()));
            } catch (Exception e) {
                results.add(new BatchCommentResultBean(commentId, false, e.getMessage()));
            }
        }
        return results;
    }

    @Override
    @BizQuery
    @Auth(roles = "admin")
    public PageBean<LitemallComment> getCommentReviewList(@Optional @Name("keyword") String keyword,
                                                           @Optional @Name("star") Integer star,
                                                           @Optional @Name("hasPicture") Boolean hasPicture,
                                                           @Optional @Name("auditStatus") Integer auditStatus,
                                                           @Optional @Name("startTime") String startTime,
                                                           @Optional @Name("endTime") String endTime,
                                                           @Optional @Name("page") Integer page,
                                                           @Optional @Name("pageSize") Integer pageSize,
                                                           IServiceContext context) {
        QueryBean query = new QueryBean();
        // content 字段 xmeta 仅允许 eq/in，contains 需绕过管道校验，故用 doFindPageByQueryDirectly。
        if (!StringHelper.isEmpty(keyword)) {
            query.addFilter(FilterBeans.contains(LitemallComment.PROP_NAME_content, keyword));
        }
        if (star != null) {
            query.addFilter(FilterBeans.eq(LitemallComment.PROP_NAME_star, star));
        }
        if (hasPicture != null) {
            query.addFilter(FilterBeans.eq(LitemallComment.PROP_NAME_hasPicture, hasPicture));
        }
        if (auditStatus != null) {
            query.addFilter(FilterBeans.eq(LitemallComment.PROP_NAME_auditStatus, auditStatus));
        }
        if (!StringHelper.isEmpty(startTime)) {
            Timestamp startTs = Timestamp.valueOf(LocalDate.parse(startTime).atTime(0, 0));
            query.addFilter(FilterBeans.ge(LitemallComment.PROP_NAME_addTime, startTs));
        }
        if (!StringHelper.isEmpty(endTime)) {
            Timestamp endTs = Timestamp.valueOf(LocalDate.parse(endTime).atTime(23, 59, 59));
            query.addFilter(FilterBeans.le(LitemallComment.PROP_NAME_addTime, endTs));
        }
        query.setOffset(page != null && page > 0 ? (page - 1) * (pageSize != null ? pageSize : 10) : 0);
        query.setLimit(pageSize != null && pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallComment.PROP_NAME_addTime, true);
        return doFindPageByQueryDirectly(query, null, context);
    }

    private static String stringValue(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) {
            return null;
        }
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private void applyShowTypeFilter(QueryBean query, String showType) {
        if (StringHelper.isEmpty(showType) || SHOW_TYPE_ALL.equals(showType)) {
            return;
        }
        switch (showType) {
            case SHOW_TYPE_HAS_PICTURE:
                query.addFilter(FilterBeans.eq(LitemallComment.PROP_NAME_hasPicture, Boolean.TRUE));
                break;
            case SHOW_TYPE_GOOD:
                // star xmeta only allows eq/in by default; emulate star>=4 via explicit list.
                query.addFilter(FilterBeans.in(LitemallComment.PROP_NAME_star,
                        Arrays.asList(GOOD_STAR_THRESHOLD, GOOD_STAR_THRESHOLD + 1)));
                break;
            case SHOW_TYPE_BAD:
                query.addFilter(FilterBeans.in(LitemallComment.PROP_NAME_star,
                        Arrays.asList(1, BAD_STAR_THRESHOLD)));
                break;
            default:
                throw new NopException(ERR_COMMENT_SHOW_TYPE_INVALID)
                        .param("showType", showType);
        }
    }

    private void aggregateTags(String json, Map<String, Integer> counter) {
        if (StringHelper.isEmpty(json)) {
            return;
        }
        Object parsed;
        try {
            parsed = JsonTool.parse(json);
        } catch (Exception ignored) {
            return;
        }
        if (parsed instanceof List) {
            for (Object item : (List<?>) parsed) {
                if (item == null) {
                    continue;
                }
                String tag = String.valueOf(item).trim();
                if (!tag.isEmpty()) {
                    counter.merge(tag, 1, Integer::sum);
                }
            }
        }
    }

    private List<Map<String, Object>> topTags(Map<String, Integer> counter, int topN) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (counter.isEmpty()) {
            return result;
        }
        TreeMap<String, Integer> sorted = new TreeMap<>(String::compareTo);
        sorted.putAll(counter);
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(sorted.entrySet());
        entries.sort(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed());
        int limit = Math.min(topN, entries.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> e = entries.get(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("tag", e.getKey());
            row.put("count", e.getValue());
            result.add(row);
        }
        return result;
    }

    private int resolveCommentReward(IServiceContext context) {
        String raw = systemBiz.getConfig(
                LitemallPointsAccountBizModel.CONFIG_POINTS_COMMENT_REWARD, context);
        if (StringHelper.isEmpty(raw)) {
            return 0;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ===== P36 successor 前置审核：公共查询过滤 + 预审开关读取 =====

    /**
     * 公共查询仅返回 APPROVED 或 auditStatus 为空（历史评论，null=已通过）。
     * PENDING/REJECTED 不公开。OR/isNull 复合操作符超出公共端 xmeta 允许面，
     * 故 commentList/getCommentSummary 走 doFind*ByQueryDirectly 后由本方法构造过滤。
     */
    private void applyPublicAuditFilter(QueryBean query) {
        query.addFilter(FilterBeans.or(
                FilterBeans.isNull(LitemallComment.PROP_NAME_auditStatus),
                FilterBeans.eq(LitemallComment.PROP_NAME_auditStatus,
                        _AppMallDaoConstants.COMMENT_AUDIT_STATUS_APPROVED)));
    }

    /**
     * 预审开关读取（复刻 resolveCommentReward 防御解析模式）：
     * empty/null/"0"/"false"/解析失败=OFF；"1"/"true"=ON。
     */
    private boolean isPreModerationEnabled(IServiceContext context) {
        String raw = systemBiz.getConfig(CONFIG_COMMENT_PRE_MODERATION, context);
        if (StringHelper.isEmpty(raw)) {
            return false;
        }
        String v = raw.trim().toLowerCase();
        if ("1".equals(v) || "true".equals(v)) {
            return true;
        }
        return false;
    }
}
