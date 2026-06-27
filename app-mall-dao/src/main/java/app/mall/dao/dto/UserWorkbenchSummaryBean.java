package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;
import java.util.List;

@DataBean
public class UserWorkbenchSummaryBean {
    private String userId;
    private String userName;
    private String nickName;
    private String mobile;
    private Integer status;
    private Integer userLevel;
    private boolean banned;

    private int orderCount;
    private BigDecimal totalSpending;

    private Integer pointsBalance;

    private int couponUnusedCount;
    private int couponUsedCount;
    private int couponExpiredCount;

    private int footprintCount;
    private int feedbackCount;

    private List<String> tags;
    private String banReason;

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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(Integer userLevel) {
        this.userLevel = userLevel;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getTotalSpending() {
        return totalSpending;
    }

    public void setTotalSpending(BigDecimal totalSpending) {
        this.totalSpending = totalSpending;
    }

    public Integer getPointsBalance() {
        return pointsBalance;
    }

    public void setPointsBalance(Integer pointsBalance) {
        this.pointsBalance = pointsBalance;
    }

    public int getCouponUnusedCount() {
        return couponUnusedCount;
    }

    public void setCouponUnusedCount(int couponUnusedCount) {
        this.couponUnusedCount = couponUnusedCount;
    }

    public int getCouponUsedCount() {
        return couponUsedCount;
    }

    public void setCouponUsedCount(int couponUsedCount) {
        this.couponUsedCount = couponUsedCount;
    }

    public int getCouponExpiredCount() {
        return couponExpiredCount;
    }

    public void setCouponExpiredCount(int couponExpiredCount) {
        this.couponExpiredCount = couponExpiredCount;
    }

    public int getFootprintCount() {
        return footprintCount;
    }

    public void setFootprintCount(int footprintCount) {
        this.footprintCount = footprintCount;
    }

    public int getFeedbackCount() {
        return feedbackCount;
    }

    public void setFeedbackCount(int feedbackCount) {
        this.feedbackCount = feedbackCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getBanReason() {
        return banReason;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }
}
