package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 客单价分布段（P19 订单分析）。
 */
@DataBean
public class AovDistributionBean {
    private String segment;
    private int orderCount;

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
}
