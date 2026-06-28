package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

/**
 * 销售趋势时序点（P18）。dateLabel 为按时/天/周/月分组的标签，gmv/orderCount 为该分组聚合值。
 */
@DataBean
public class SalesTrendPointBean {
    private String dateLabel;
    private BigDecimal gmv;
    private int orderCount;

    public String getDateLabel() {
        return dateLabel;
    }

    public void setDateLabel(String dateLabel) {
        this.dateLabel = dateLabel;
    }

    public BigDecimal getGmv() {
        return gmv;
    }

    public void setGmv(BigDecimal gmv) {
        this.gmv = gmv;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
}
