package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

@DataBean
public class PromotionResolutionBean {
    private String activityId;
    private BigDecimal discount;
    private BigDecimal meetAmount;

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getMeetAmount() {
        return meetAmount;
    }

    public void setMeetAmount(BigDecimal meetAmount) {
        this.meetAmount = meetAmount;
    }
}
