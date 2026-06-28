package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 评论批量运营（P36）单行结果。一行对应批量回复/批量下架/恢复中的一条评论记录，
 * 失败行带原因；部分失败不阻断成功行（复刻 {@code BatchShipResultBean} 聚合口径）。
 */
@DataBean
public class BatchCommentResultBean {
    private String commentId;
    private boolean success;
    private String reason;

    public BatchCommentResultBean() {
    }

    public BatchCommentResultBean(String commentId, boolean success, String reason) {
        this.commentId = commentId;
        this.success = success;
        this.reason = reason;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
