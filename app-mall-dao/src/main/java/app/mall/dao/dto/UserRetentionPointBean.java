package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

/**
 * 用户留存率时序点（P19 用户分析）。dateLabel 为首次支付日，d1/d7/d30 为对应留存率。
 * 口径见 docs/design/system-configuration.md「报表与统计」。
 */
@DataBean
public class UserRetentionPointBean {
    private String dateLabel;
    private int cohortSize;
    private int d1;
    private int d7;
    private int d30;
    private BigDecimal d1Rate;
    private BigDecimal d7Rate;
    private BigDecimal d30Rate;

    public String getDateLabel() {
        return dateLabel;
    }

    public void setDateLabel(String dateLabel) {
        this.dateLabel = dateLabel;
    }

    public int getCohortSize() {
        return cohortSize;
    }

    public void setCohortSize(int cohortSize) {
        this.cohortSize = cohortSize;
    }

    public int getD1() {
        return d1;
    }

    public void setD1(int d1) {
        this.d1 = d1;
    }

    public int getD7() {
        return d7;
    }

    public void setD7(int d7) {
        this.d7 = d7;
    }

    public int getD30() {
        return d30;
    }

    public void setD30(int d30) {
        this.d30 = d30;
    }

    public BigDecimal getD1Rate() {
        return d1Rate;
    }

    public void setD1Rate(BigDecimal d1Rate) {
        this.d1Rate = d1Rate;
    }

    public BigDecimal getD7Rate() {
        return d7Rate;
    }

    public void setD7Rate(BigDecimal d7Rate) {
        this.d7Rate = d7Rate;
    }

    public BigDecimal getD30Rate() {
        return d30Rate;
    }

    public void setD30Rate(BigDecimal d30Rate) {
        this.d30Rate = d30Rate;
    }
}
