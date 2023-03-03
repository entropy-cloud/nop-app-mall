package app.mall.dao.entity;

import app.mall.dao.entity._gen._LitemallGoods;
import io.nop.api.core.annotations.biz.BizObjName;
import io.nop.commons.util.CollectionHelper;
import io.nop.core.lang.json.JsonTool;
import io.nop.core.lang.utils.Underscore;

import java.math.BigDecimal;
import java.util.List;


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

    public List<String> getKeywordsList() {
        return (List<String>) JsonTool.parseNonStrict(getKeywords());
    }

    public void setKeywordsList(List<String> list) {
        if (CollectionHelper.isEmpty(list)) {
            setKeywords(null);
        } else {
            setKeywords(JsonTool.stringify(list));
        }
    }
}
