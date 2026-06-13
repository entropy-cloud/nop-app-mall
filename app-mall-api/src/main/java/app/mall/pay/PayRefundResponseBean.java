package app.mall.pay;

import io.nop.api.core.annotations.data.DataBean;

@DataBean
public class PayRefundResponseBean {
    private boolean success = true;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
