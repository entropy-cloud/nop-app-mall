package app.mall.pay;

import io.nop.api.core.annotations.data.DataBean;

@DataBean
public class PayPrepayResponseBean {
    private String payId;

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId;
    }
}
