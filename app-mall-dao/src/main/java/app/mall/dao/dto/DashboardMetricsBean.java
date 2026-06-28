package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

/**
 * 经营看板核心指标卡（P18）。口径见 docs/design/system-configuration.md「报表与统计」。
 */
@DataBean
public class DashboardMetricsBean {
    private BigDecimal todayGmv;
    private BigDecimal yesterdayGmv;
    private BigDecimal lastWeekGmv;
    private BigDecimal gmvDayRatio;
    private BigDecimal gmvWeekRatio;
    private int orderCount;
    private int uv;
    private BigDecimal conversionRate;
    private BigDecimal aov;
    private BigDecimal returnRate;

    public BigDecimal getTodayGmv() {
        return todayGmv;
    }

    public void setTodayGmv(BigDecimal todayGmv) {
        this.todayGmv = todayGmv;
    }

    public BigDecimal getYesterdayGmv() {
        return yesterdayGmv;
    }

    public void setYesterdayGmv(BigDecimal yesterdayGmv) {
        this.yesterdayGmv = yesterdayGmv;
    }

    public BigDecimal getLastWeekGmv() {
        return lastWeekGmv;
    }

    public void setLastWeekGmv(BigDecimal lastWeekGmv) {
        this.lastWeekGmv = lastWeekGmv;
    }

    public BigDecimal getGmvDayRatio() {
        return gmvDayRatio;
    }

    public void setGmvDayRatio(BigDecimal gmvDayRatio) {
        this.gmvDayRatio = gmvDayRatio;
    }

    public BigDecimal getGmvWeekRatio() {
        return gmvWeekRatio;
    }

    public void setGmvWeekRatio(BigDecimal gmvWeekRatio) {
        this.gmvWeekRatio = gmvWeekRatio;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getUv() {
        return uv;
    }

    public void setUv(int uv) {
        this.uv = uv;
    }

    public BigDecimal getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(BigDecimal conversionRate) {
        this.conversionRate = conversionRate;
    }

    public BigDecimal getAov() {
        return aov;
    }

    public void setAov(BigDecimal aov) {
        this.aov = aov;
    }

    public BigDecimal getReturnRate() {
        return returnRate;
    }

    public void setReturnRate(BigDecimal returnRate) {
        this.returnRate = returnRate;
    }
}
