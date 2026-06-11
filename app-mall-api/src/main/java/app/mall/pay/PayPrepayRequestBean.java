package app.mall.pay;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

@DataBean
public class PayPrepayRequestBean {
    private String outTradeNo;
    private BigDecimal totalFee;

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }
}
