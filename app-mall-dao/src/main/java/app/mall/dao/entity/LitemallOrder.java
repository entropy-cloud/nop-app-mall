package app.mall.dao.entity;

import io.nop.api.core.annotations.biz.BizObjName;
import app.mall.dao.entity._gen._LitemallOrder;

import java.math.BigDecimal;


@BizObjName("LitemallOrder")
public class LitemallOrder extends _LitemallOrder{
    public LitemallOrder(){
    }

    public boolean isStatus(int status) {
        return getOrderStatus() != null && getOrderStatus() == status;
    }

    public LitemallOrderGoods addOrderGoods(String goodsId, String productId, String goodsSn,
                                            BigDecimal price, int number) {
        LitemallOrderGoods orderGoods = new LitemallOrderGoods();
        orderGoods.setGoodsId(goodsId);
        orderGoods.setProductId(productId);
        orderGoods.setGoodsSn(goodsSn);
        orderGoods.setPrice(price);
        orderGoods.setNumber(number);
        getOrderGoods().add(orderGoods);

        recalcGoodsPrice();
        return orderGoods;
    }

    private void recalcGoodsPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (LitemallOrderGoods item : getOrderGoods()) {
            if (item.getPrice() != null && item.getNumber() != null) {
                total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getNumber())));
            }
        }
        setGoodsPrice(total);

        BigDecimal orderPrice = total;
        if (getFreightPrice() != null) {
            orderPrice = orderPrice.add(getFreightPrice());
        }
        setOrderPrice(orderPrice);

        BigDecimal actualPrice = orderPrice;
        if (getCouponPrice() != null) {
            actualPrice = actualPrice.subtract(getCouponPrice());
        }
        if (getIntegralPrice() != null) {
            actualPrice = actualPrice.subtract(getIntegralPrice());
        }
        if (getGrouponPrice() != null) {
            actualPrice = actualPrice.subtract(getGrouponPrice());
        }
        setActualPrice(actualPrice);
    }

}
