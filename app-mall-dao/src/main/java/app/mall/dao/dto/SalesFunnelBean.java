package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

/**
 * 销售漏斗（P19）。5 段同口径同期对比：浏览→加购→下单→支付→复购。
 * 口径见 docs/design/system-configuration.md「报表与统计」。
 */
@DataBean
public class SalesFunnelBean {
    private int viewCount;
    private int cartCount;
    private int orderCount;
    private int payCount;
    private int repurchaseCount;
    private BigDecimal cartViewRatio;
    private BigDecimal orderCartRatio;
    private BigDecimal payOrderRatio;
    private BigDecimal repurchasePayRatio;
    private BigDecimal payViewRatio;

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getCartCount() {
        return cartCount;
    }

    public void setCartCount(int cartCount) {
        this.cartCount = cartCount;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getPayCount() {
        return payCount;
    }

    public void setPayCount(int payCount) {
        this.payCount = payCount;
    }

    public int getRepurchaseCount() {
        return repurchaseCount;
    }

    public void setRepurchaseCount(int repurchaseCount) {
        this.repurchaseCount = repurchaseCount;
    }

    public BigDecimal getCartViewRatio() {
        return cartViewRatio;
    }

    public void setCartViewRatio(BigDecimal cartViewRatio) {
        this.cartViewRatio = cartViewRatio;
    }

    public BigDecimal getOrderCartRatio() {
        return orderCartRatio;
    }

    public void setOrderCartRatio(BigDecimal orderCartRatio) {
        this.orderCartRatio = orderCartRatio;
    }

    public BigDecimal getPayOrderRatio() {
        return payOrderRatio;
    }

    public void setPayOrderRatio(BigDecimal payOrderRatio) {
        this.payOrderRatio = payOrderRatio;
    }

    public BigDecimal getRepurchasePayRatio() {
        return repurchasePayRatio;
    }

    public void setRepurchasePayRatio(BigDecimal repurchasePayRatio) {
        this.repurchasePayRatio = repurchasePayRatio;
    }

    public BigDecimal getPayViewRatio() {
        return payViewRatio;
    }

    public void setPayViewRatio(BigDecimal payViewRatio) {
        this.payViewRatio = payViewRatio;
    }
}
