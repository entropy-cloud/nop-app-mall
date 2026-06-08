package app.mall.dao.entity;

import io.nop.api.core.annotations.biz.BizObjName;
import app.mall.dao.entity._gen._LitemallCart;


@BizObjName("LitemallCart")
public class LitemallCart extends _LitemallCart{
    public LitemallCart(){
    }

    public void increaseNumber(short delta) {
        short current = getNumber() != null ? getNumber() : 0;
        setNumber((short) (current + delta));
    }

    public void validateForCheckout() {
        if (getNumber() == null || getNumber() <= 0) {
            throw new IllegalStateException("cart.number is zero");
        }
    }

}
