package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

@DataBean
public class PromotionEffectivenessBean {
    private int promotedOrderCount;
    private BigDecimal totalGmv;
    private BigDecimal totalDiscount;
    private int participantCount;

    public int getPromotedOrderCount() {
        return promotedOrderCount;
    }

    public void setPromotedOrderCount(int promotedOrderCount) {
        this.promotedOrderCount = promotedOrderCount;
    }

    public BigDecimal getTotalGmv() {
        return totalGmv;
    }

    public void setTotalGmv(BigDecimal totalGmv) {
        this.totalGmv = totalGmv;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }
}
