package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 算法化分群成员列表项（P20 算法化用户画像 successor）。由 getSegmentMembers 按算法化维度
 * （RFM 段 / 生命周期阶段）圈选用户后返回。all-time 口径与 {@link UserPortraitBean} 一致。
 * 口径见 docs/design/system-configuration.md「报表与统计」。
 */
@DataBean
public class SegmentMemberBean {
    private String userId;
    private String userName;
    private Timestamp lastPayTime;
    private int orderCount;
    private BigDecimal totalAmount;
    private String rfmSegment;
    private String lifecycleStage;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getLastPayTime() {
        return lastPayTime;
    }

    public void setLastPayTime(Timestamp lastPayTime) {
        this.lastPayTime = lastPayTime;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getRfmSegment() {
        return rfmSegment;
    }

    public void setRfmSegment(String rfmSegment) {
        this.rfmSegment = rfmSegment;
    }

    public String getLifecycleStage() {
        return lifecycleStage;
    }

    public void setLifecycleStage(String lifecycleStage) {
        this.lifecycleStage = lifecycleStage;
    }
}
