package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

@DataBean
public class FlashSaleEffectivenessBean {
    private int dealOrderCount;
    private BigDecimal totalGmv;
    private int participantCount;
    private BigDecimal soldOutRate;
    private int rejectedCount;

    public int getDealOrderCount() {
        return dealOrderCount;
    }

    public void setDealOrderCount(int dealOrderCount) {
        this.dealOrderCount = dealOrderCount;
    }

    public BigDecimal getTotalGmv() {
        return totalGmv;
    }

    public void setTotalGmv(BigDecimal totalGmv) {
        this.totalGmv = totalGmv;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

    public BigDecimal getSoldOutRate() {
        return soldOutRate;
    }

    public void setSoldOutRate(BigDecimal soldOutRate) {
        this.soldOutRate = soldOutRate;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }
}
