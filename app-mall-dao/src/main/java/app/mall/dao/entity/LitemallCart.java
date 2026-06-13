package app.mall.dao.entity;

import io.nop.api.core.annotations.biz.BizObjName;
import app.mall.dao.entity._gen._LitemallCart;


@BizObjName("LitemallCart")
public class LitemallCart extends _LitemallCart{
    public LitemallCart(){
    }

    public void increaseNumber(int delta) {
        int current = getNumber() != null ? getNumber() : 0;
        setNumber(current + delta);
    }

    public void validateForCheckout() {
        if (getNumber() == null || getNumber() <= 0) {
            throw new io.nop.api.core.exceptions.NopException(
                    io.nop.api.core.exceptions.ErrorCode.define("nop.err.mall.cart.number-zero", "购物车商品数量为零"));
        }
    }

}
