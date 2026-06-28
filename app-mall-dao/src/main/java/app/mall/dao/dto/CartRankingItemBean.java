package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 加购排行项（P19 商品分析）。
 */
@DataBean
public class CartRankingItemBean {
    private String goodsId;
    private String goodsName;
    private int cartCount;

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

    public int getCartCount() {
        return cartCount;
    }

    public void setCartCount(int cartCount) {
        this.cartCount = cartCount;
    }
}
