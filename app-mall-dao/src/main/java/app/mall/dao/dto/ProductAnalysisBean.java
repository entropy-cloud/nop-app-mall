package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品分析聚合（P19）。包含销量排行、加购排行、滞销品与动销率。
 * 口径见 docs/design/system-configuration.md「报表与统计」。
 */
@DataBean
public class ProductAnalysisBean {
    private List<GoodsStatisticsBean> salesRanking = new ArrayList<>();
    private List<CartRankingItemBean> cartRanking = new ArrayList<>();
    private List<UnsalableGoodsBean> unsalableGoods = new ArrayList<>();
    private int soldGoodsCount;
    private int onSaleGoodsCount;
    private BigDecimal salabilityRate;

    public List<GoodsStatisticsBean> getSalesRanking() {
        return salesRanking;
    }

    public void setSalesRanking(List<GoodsStatisticsBean> salesRanking) {
        this.salesRanking = salesRanking;
    }

    public List<CartRankingItemBean> getCartRanking() {
        return cartRanking;
    }

    public void setCartRanking(List<CartRankingItemBean> cartRanking) {
        this.cartRanking = cartRanking;
    }

    public List<UnsalableGoodsBean> getUnsalableGoods() {
        return unsalableGoods;
    }

    public void setUnsalableGoods(List<UnsalableGoodsBean> unsalableGoods) {
        this.unsalableGoods = unsalableGoods;
    }

    public int getSoldGoodsCount() {
        return soldGoodsCount;
    }

    public void setSoldGoodsCount(int soldGoodsCount) {
        this.soldGoodsCount = soldGoodsCount;
    }

    public int getOnSaleGoodsCount() {
        return onSaleGoodsCount;
    }

    public void setOnSaleGoodsCount(int onSaleGoodsCount) {
        this.onSaleGoodsCount = onSaleGoodsCount;
    }

    public BigDecimal getSalabilityRate() {
        return salabilityRate;
    }

    public void setSalabilityRate(BigDecimal salabilityRate) {
        this.salabilityRate = salabilityRate;
    }
}
