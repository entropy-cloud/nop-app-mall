package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

@DataBean
public class PinTuanEffectivenessBean {
    private int openedGroups;
    private int successGroups;
    private int participantCount;
    private BigDecimal totalGmv;

    public int getOpenedGroups() {
        return openedGroups;
    }

    public void setOpenedGroups(int openedGroups) {
        this.openedGroups = openedGroups;
    }

    public int getSuccessGroups() {
        return successGroups;
    }

    public void setSuccessGroups(int successGroups) {
        this.successGroups = successGroups;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

    public BigDecimal getTotalGmv() {
        return totalGmv;
    }

    public void setTotalGmv(BigDecimal totalGmv) {
        this.totalGmv = totalGmv;
    }
}
