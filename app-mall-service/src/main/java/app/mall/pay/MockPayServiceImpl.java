package app.mall.pay;

import jakarta.inject.Named;

@Named
public class MockPayServiceImpl implements PayService {

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
