package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

/**
 * 生命周期分层用户计数（P19 用户分析）。segment 为 new/active/dormant/churned，userCount/percent 为该分层占比。
 */
@DataBean
public class LifecycleSegmentBean {
    private String segment;
    private int userCount;
    private BigDecimal percent;

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public void setPercent(BigDecimal percent) {
        this.percent = percent;
    }
}
