package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 库存预警明细项（P18）。聚合库存 ≤ 阈值的商品。
 * 阈值解析三级优先：per-goods {@code safetyStock}（非空且 >0）→ 全局 {@code mall_stock_threshold_tight}。
 * （聚合层无 per-SKU 档，与 SKU 粒度工作台 {@link StockWarningSkuBean} 的口径在 goods→global 两档对齐。）
 */
@DataBean
public class StockWarningItemBean {
    private String goodsId;
    private String goodsName;
    private int totalStock;
    private Integer safetyStock;
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

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public Integer getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(Integer safetyStock) {
        this.safetyStock = safetyStock;
    }

    public String getThresholdSource() {
        return thresholdSource;
    }

    public void setThresholdSource(String thresholdSource) {
        this.thresholdSource = thresholdSource;
    }
}
