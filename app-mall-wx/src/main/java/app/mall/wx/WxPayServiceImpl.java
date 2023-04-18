package app.mall.wx;

import app.mall.pay.PayRefundRequestBean;
import app.mall.pay.PayRefundResponseBean;
import app.mall.pay.PayService;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;

public class WxPayServiceImpl implements PayService {

    @Override
    public ApiResponse<PayRefundResponseBean> refund(ApiRequest<PayRefundRequestBean> req) {
        return ApiResponse.buildSuccess(new PayRefundResponseBean());
    }

}
