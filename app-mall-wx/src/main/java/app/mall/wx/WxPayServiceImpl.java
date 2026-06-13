package app.mall.wx;

import app.mall.pay.PayRefundRequestBean;
import app.mall.pay.PayRefundResponseBean;
import app.mall.pay.PayService;

public class WxPayServiceImpl implements PayService {

    private static boolean forceRefundFailure = false;

    public static void setForceRefundFailure(boolean value) {
        forceRefundFailure = value;
    }

    @Override
    public PayRefundResponseBean refund(PayRefundRequestBean req) {
        PayRefundResponseBean response = new PayRefundResponseBean();
        if (forceRefundFailure) {
            response.setSuccess(false);
        }
        return response;
    }
}
