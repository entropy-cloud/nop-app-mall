package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 滞销品项（P19 商品分析）。在售但期间零销量的商品。
 */
@DataBean
public class UnsalableGoodsBean {
    private String goodsId;
    private String goodsName;

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
}
