package app.mall.dao.entity;

import app.mall.dao.entity._gen._LitemallGoods;
import io.nop.api.core.annotations.biz.BizObjName;
import io.nop.core.lang.utils.Underscore;

import java.math.BigDecimal;


@BizObjName("LitemallGoods")
public class LitemallGoods extends _LitemallGoods {
    public LitemallGoods() {
    }

    /**
     * retailPrice记录当前商品的最低价
     */
    public void syncRetailPrice() {
        LitemallGoodsProduct minProduct = Underscore.min(getProducts(), LitemallGoodsProduct::getPrice);
        BigDecimal retailPrice = minProduct == null ? minProduct.getPrice() : new BigDecimal(Integer.MAX_VALUE);
        setRetailPrice(retailPrice);
    }
}
