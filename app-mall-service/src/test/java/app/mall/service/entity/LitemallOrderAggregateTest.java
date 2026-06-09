package app.mall.service.entity;

import app.mall.dao.entity.LitemallOrder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LitemallOrderAggregateTest {

    @Test
    public void testRecalcPricesFromLines() {
        LitemallOrder order = new LitemallOrder();
        order.setFreightPrice(new BigDecimal("10"));
        order.setCouponPrice(new BigDecimal("2"));
        order.setIntegralPrice(BigDecimal.ZERO);
        order.setGrouponPrice(BigDecimal.ZERO);
        order.addOrderGoods("1", "100", "G1", new BigDecimal("20"), (short) 2);
        assertEquals(new BigDecimal("40"), order.getGoodsPrice());
        assertEquals(new BigDecimal("50"), order.getOrderPrice());
        assertEquals(new BigDecimal("48"), order.getActualPrice());
    }

    @Test
    public void testIsStatus() {
        LitemallOrder order = new LitemallOrder();
        order.setOrderStatus((short) 101);
        assertTrue(order.isStatus(101));
    }
}
