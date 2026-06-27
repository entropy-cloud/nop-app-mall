package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

@DataBean
public class CouponUsageStatisticsBean {
    private int claimedCount;
    private int usedCount;
    private BigDecimal pulledGmv;

    public int getClaimedCount() {
        return claimedCount;
    }

    public void setClaimedCount(int claimedCount) {
        this.claimedCount = claimedCount;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public BigDecimal getPulledGmv() {
        return pulledGmv;
    }

    public void setPulledGmv(BigDecimal pulledGmv) {
        this.pulledGmv = pulledGmv;
    }
}
