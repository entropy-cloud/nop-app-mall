package app.mall.pay;

import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;

public interface PayService {
    ApiResponse<PayRefundResponseBean> refund(ApiRequest<PayRefundRequestBean> req);
}