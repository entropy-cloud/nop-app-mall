package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

@DataBean
public class CheckInRewardRuleBean {
    private int daySeq;
    private int pointReward;
    private int resetCycle;

    public int getDaySeq() {
        return daySeq;
    }

    public void setDaySeq(int daySeq) {
        this.daySeq = daySeq;
    }

    public int getPointReward() {
        return pointReward;
    }

    public void setPointReward(int pointReward) {
        this.pointReward = pointReward;
    }

    public int getResetCycle() {
        return resetCycle;
    }

    public void setResetCycle(int resetCycle) {
        this.resetCycle = resetCycle;
    }
}
