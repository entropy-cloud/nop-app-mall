package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

/**
 * 复购率时序点（P19 用户分析）。dateLabel 为日期/周/月标签，rate 为复购率。
 */
@DataBean
public class RepurchaseRatePointBean {
    private String dateLabel;
    private int paidUsers;
    private int repurchaseUsers;
    private BigDecimal rate;

    public String getDateLabel() {
        return dateLabel;
    }

    public void setDateLabel(String dateLabel) {
        this.dateLabel = dateLabel;
    }

    public int getPaidUsers() {
        return paidUsers;
    }

    public void setPaidUsers(int paidUsers) {
        this.paidUsers = paidUsers;
    }

    public int getRepurchaseUsers() {
        return repurchaseUsers;
    }

    public void setRepurchaseUsers(int repurchaseUsers) {
        this.repurchaseUsers = repurchaseUsers;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
