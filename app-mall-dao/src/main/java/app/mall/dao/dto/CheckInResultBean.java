package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

@DataBean
public class CheckInResultBean {
    private boolean todayChecked;
    private int consecutiveDays;
    private int pointsEarned;
    private int accountBalance;

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

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public int getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(int accountBalance) {
        this.accountBalance = accountBalance;
    }
}
