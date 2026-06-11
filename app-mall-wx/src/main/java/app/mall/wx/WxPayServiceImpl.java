package app.mall.wx;

import app.mall.pay.PayRefundRequestBean;
import app.mall.pay.PayRefundResponseBean;
import app.mall.pay.PayService;

public class WxPayServiceImpl implements PayService {

    @Override
    public PayRefundResponseBean refund(PayRefundRequestBean req) {
        return new PayRefundResponseBean();
    }
}
