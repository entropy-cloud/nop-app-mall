package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售趋势原始数据点（P18 内部复用）。一条已支付订单的 payTime/actualPrice，供 Java 侧按时/天/周/月分组聚合。
 */
@DataBean
public class OrderPointBean {
    private LocalDateTime payTime;
    private BigDecimal actualPrice;

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public BigDecimal getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(BigDecimal actualPrice) {
        this.actualPrice = actualPrice;
    }
}
