package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 用户支付汇总（P19 用户分析）。用于 RFM 分层与生命周期判定。
 */
@DataBean
public class UserPaymentSummaryBean {
    private String userId;
    private Timestamp firstPayTime;
    private Timestamp lastPayTime;
    private int orderCount;
    private BigDecimal totalAmount;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getFirstPayTime() {
        return firstPayTime;
    }

    public void setFirstPayTime(Timestamp firstPayTime) {
        this.firstPayTime = firstPayTime;
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
}
