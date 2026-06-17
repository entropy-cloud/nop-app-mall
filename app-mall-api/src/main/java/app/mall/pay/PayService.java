package app.mall.pay;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;

@BizModel("PayService")
public interface PayService {

    /**
     * Whether real WeChat Pay is enabled (wxpay.enabled=true).
     * BizModel code uses this to decide whether manual pay() confirmation is allowed
     * (only when disabled / demo mode or zero-amount orders).
     */
    @BizQuery("isEnabled")
    boolean isEnabled();

    @BizMutation("createPayment")
    PayPrepayResponseBean createPayment(PayPrepayRequestBean req);

    @BizQuery("queryPayment")
    PayStatusResponseBean queryPayment(@Name("outTradeNo") String outTradeNo);

    @BizMutation("refund")
    PayRefundResponseBean refund(PayRefundRequestBean req);
}
