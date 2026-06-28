package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单分析聚合（P19）。包含客单价分布、支付方式占比、退货原因占比。
 * 口径见 docs/design/system-configuration.md「报表与统计」。
 */
@DataBean
public class OrderAnalysisBean {
    private List<AovDistributionBean> aovDistribution = new ArrayList<>();
    private List<PaymentMethodShareBean> paymentMethodShare = new ArrayList<>();
    private List<ReturnReasonShareBean> returnReasonShare = new ArrayList<>();
    private int totalPaidOrders;

    public List<AovDistributionBean> getAovDistribution() {
        return aovDistribution;
    }

    public void setAovDistribution(List<AovDistributionBean> aovDistribution) {
        this.aovDistribution = aovDistribution;
    }

    public List<PaymentMethodShareBean> getPaymentMethodShare() {
        return paymentMethodShare;
    }

    public void setPaymentMethodShare(List<PaymentMethodShareBean> paymentMethodShare) {
        this.paymentMethodShare = paymentMethodShare;
    }

    public List<ReturnReasonShareBean> getReturnReasonShare() {
        return returnReasonShare;
    }

    public void setReturnReasonShare(List<ReturnReasonShareBean> returnReasonShare) {
        this.returnReasonShare = returnReasonShare;
    }

    public int getTotalPaidOrders() {
        return totalPaidOrders;
    }

    public void setTotalPaidOrders(int totalPaidOrders) {
        this.totalPaidOrders = totalPaidOrders;
    }
}
