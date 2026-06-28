package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * GMV 与订单数聚合（P18 看板内部复用）。GMV = status&ge;201 订单 actualPrice 之和；
 * orderCount = 该区间支付订单数；paidUserCount = 该区间去重支付用户数。
 */
@DataBean
public class DashboardGmvBean {
    private java.math.BigDecimal gmv;
    private int orderCount;
    private int paidUserCount;
    private int uv;
    private int returnCount;

    public java.math.BigDecimal getGmv() {
        return gmv;
    }

    public void setGmv(java.math.BigDecimal gmv) {
        this.gmv = gmv;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getPaidUserCount() {
        return paidUserCount;
    }

    public void setPaidUserCount(int paidUserCount) {
        this.paidUserCount = paidUserCount;
    }

    public int getUv() {
        return uv;
    }

    public void setUv(int uv) {
        this.uv = uv;
    }

    public int getReturnCount() {
        return returnCount;
    }

    public void setReturnCount(int returnCount) {
        this.returnCount = returnCount;
    }
}
