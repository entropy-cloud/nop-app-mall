package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

@DataBean
public class UserStatisticsBean {
    private int totalUsers;
    private int newUsersToday;
    private int newUsersThisWeek;
    private int newUsersThisMonth;

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getNewUsersToday() {
        return newUsersToday;
    }

    public void setNewUsersToday(int newUsersToday) {
        this.newUsersToday = newUsersToday;
    }

    public int getNewUsersThisWeek() {
        return newUsersThisWeek;
    }

    public void setNewUsersThisWeek(int newUsersThisWeek) {
        this.newUsersThisWeek = newUsersThisWeek;
    }

    public int getNewUsersThisMonth() {
        return newUsersThisMonth;
    }

    public void setNewUsersThisMonth(int newUsersThisMonth) {
        this.newUsersThisMonth = newUsersThisMonth;
    }
}
