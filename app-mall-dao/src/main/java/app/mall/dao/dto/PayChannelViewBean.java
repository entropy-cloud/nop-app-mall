package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;

/**
 * Cashier-facing view of one enabled payment channel (P30). Returned by
 * {@code LitemallOrder__getEnabledPayChannels(orderId)} so the storefront cashier
 * ({@code /storefront-pay}) can render a dynamic channel list instead of the hardcoded
 * WeChat flow.
 */
@DataBean
public class PayChannelViewBean {
    private String code;
    private String name;
    private String description;
    /** Wallet balance available for the current user (only set for the BALANCE channel). */
    private BigDecimal balanceAvailable;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBalanceAvailable() {
        return balanceAvailable;
    }

    public void setBalanceAvailable(BigDecimal balanceAvailable) {
        this.balanceAvailable = balanceAvailable;
    }
}
