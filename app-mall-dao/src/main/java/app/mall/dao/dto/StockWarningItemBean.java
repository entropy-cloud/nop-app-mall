package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 库存预警明细项（P18）。聚合库存 ≤ 阈值的商品。
 */
@DataBean
public class StockWarningItemBean {
    private String goodsId;
    private String goodsName;
    private int totalStock;

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
}
