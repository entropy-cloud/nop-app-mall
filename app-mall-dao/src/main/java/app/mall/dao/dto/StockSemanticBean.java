package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 库存语义化结果（P38）。聚合商品下所有 SKU 的可用库存（number）后，
 * 按后台可配阈值映射为三档语义：充足 / 紧张 / 缺货，附带文案与色值。
 */
@DataBean
public class StockSemanticBean {
    public static final String LEVEL_SUFFICIENT = "sufficient";
    public static final String LEVEL_TIGHT = "tight";
    public static final String LEVEL_OUT = "out";

    private String level;
    private String label;
    private String color;
    private Integer stockNumber;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getStockNumber() {
        return stockNumber;
    }

    public void setStockNumber(Integer stockNumber) {
        this.stockNumber = stockNumber;
    }
}
