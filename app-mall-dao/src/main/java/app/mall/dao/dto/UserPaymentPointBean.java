package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.sql.Timestamp;

/**
 * 用户支付时序点（P19 用户分析）。用于留存与复购率计算。
 */
@DataBean
public class UserPaymentPointBean {
    private String userId;
    private Timestamp payTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getPayTime() {
        return payTime;
    }

    public void setPayTime(Timestamp payTime) {
        this.payTime = payTime;
    }
}
