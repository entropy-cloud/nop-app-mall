package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * RFM 分层用户计数（P19 用户分析）。segment 为三分位标签，userCount 为该分层用户数。
 * 口径见 docs/design/system-configuration.md「报表与统计」。
 */
@DataBean
public class RfmSegmentBean {
    private String segment;
    private int userCount;

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
}
