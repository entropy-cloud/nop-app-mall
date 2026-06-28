package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 自提核销结果（P31）。区分「本次新核销」与「幂等跳过（已核销）」，
 * 便于门店端 UI 给出明确反馈（不复用 ship 通知/日志，仅返回结构化结果）。
 */
@DataBean
public class VerifyPickupResultBean {
    private String orderId;
    private String orderSn;
    private String pickupCode;
    private boolean alreadyVerified;
    private String message;

    public VerifyPickupResultBean() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public boolean isAlreadyVerified() {
        return alreadyVerified;
    }

    public void setAlreadyVerified(boolean alreadyVerified) {
        this.alreadyVerified = alreadyVerified;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
