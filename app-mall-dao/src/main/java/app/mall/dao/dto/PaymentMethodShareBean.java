package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 支付方式占比项（P19 订单分析）。
 */
@DataBean
public class PaymentMethodShareBean {
    private String method;
    private int orderCount;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
}
