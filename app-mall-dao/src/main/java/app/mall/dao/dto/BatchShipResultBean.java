package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量发货（P21）单行结果。一行对应 Excel 中一条 orderSn/shipSn/shipChannel 记录，
 * 失败行带原因；部分失败不阻断成功行。
 */
@DataBean
public class BatchShipResultBean {
    private String orderSn;
    private boolean success;
    private String reason;

    public BatchShipResultBean() {
    }

    public BatchShipResultBean(String orderSn, boolean success, String reason) {
        this.orderSn = orderSn;
        this.success = success;
        this.reason = reason;
    }

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public static List<BatchShipResultBean> wrap() {
        return new ArrayList<>();
    }
}
