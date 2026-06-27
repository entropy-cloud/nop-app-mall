package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.util.ArrayList;
import java.util.List;

@DataBean
public class CheckInStatusBean {
    private boolean todayChecked;
    private int consecutiveDays;
    private int totalDays;
    private int accountBalance;
    private List<CheckInRewardRuleBean> rewardRules = new ArrayList<>();

    public boolean isTodayChecked() {
        return todayChecked;
    }

    public void setTodayChecked(boolean todayChecked) {
        this.todayChecked = todayChecked;
    }

    public int getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(int consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public int getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(int accountBalance) {
        this.accountBalance = accountBalance;
    }

    public List<CheckInRewardRuleBean> getRewardRules() {
        return rewardRules;
    }

    public void setRewardRules(List<CheckInRewardRuleBean> rewardRules) {
        this.rewardRules = rewardRules;
    }
}
