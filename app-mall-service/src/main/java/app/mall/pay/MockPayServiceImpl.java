package app.mall.pay;

public class MockPayServiceImpl implements PayService {

    private static boolean forceRefundFailure = false;

    public static void setForceRefundFailure(boolean value) {
        forceRefundFailure = value;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public PayPrepayResponseBean createPayment(PayPrepayRequestBean req) {
        PayPrepayResponseBean resp = new PayPrepayResponseBean();
        resp.setPayId("mock-" + req.getOutTradeNo());
        return resp;
    }

    @Override
    public PayStatusResponseBean queryPayment(String outTradeNo) {
        PayStatusResponseBean resp = new PayStatusResponseBean();
        resp.setSuccess(true);
        resp.setTradeState("SUCCESS");
        resp.setOutTradeNo(outTradeNo);
        return resp;
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
