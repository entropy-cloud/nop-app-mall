package app.mall.pay;

import jakarta.inject.Named;

@Named
public class MockPayServiceImpl implements PayService {

    @Override
    public PayRefundResponseBean refund(PayRefundRequestBean req) {
        return new PayRefundResponseBean();
    }
}
