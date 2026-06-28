package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 退货原因占比项（P19 订单分析）。
 */
@DataBean
public class ReturnReasonShareBean {
    private String reason;
    private int aftersaleCount;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getAftersaleCount() {
        return aftersaleCount;
    }

    public void setAftersaleCount(int aftersaleCount) {
        this.aftersaleCount = aftersaleCount;
    }
}
