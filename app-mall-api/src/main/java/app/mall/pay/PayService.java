package app.mall.pay;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;

@BizModel("PayService")
public interface PayService {
    @BizMutation("refund")
    PayRefundResponseBean refund(PayRefundRequestBean req);
}
