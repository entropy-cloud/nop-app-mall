package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 库存预警（P36）SKU 粒度明细项。当某 SKU {@code number} ≤ 有效阈值时返回。
 * 有效阈值优先用 per-SKU {@code safeStock}（非空且 >0），否则回退全局配置 {@code mall_stock_threshold_tight}。
 */
@DataBean
public class StockWarningSkuBean {
    private String goodsId;
    private String goodsName;
    private String productId;
    private String specifications;
    private int number;
    private Integer safeStock;
    private int threshold;
    private String thresholdSource;

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Integer getSafeStock() {
        return safeStock;
    }

    public void setSafeStock(Integer safeStock) {
        this.safeStock = safeStock;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String getThresholdSource() {
        return thresholdSource;
    }

    public void setThresholdSource(String thresholdSource) {
        this.thresholdSource = thresholdSource;
    }
}
