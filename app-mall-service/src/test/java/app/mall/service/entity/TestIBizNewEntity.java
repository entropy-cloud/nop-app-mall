package app.mall.service.entity;

import app.mall.biz.ILitemallCartBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallOrderGoodsBiz;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.autotest.junit.JunitBaseTestCase;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestIBizNewEntity extends JunitBaseTestCase {

    @Inject
    ILitemallOrderBiz orderBiz;

    @Inject
    ILitemallOrderGoodsBiz orderGoodsBiz;

    @Inject
    ILitemallCartBiz cartBiz;

    @Test
    void testOrderBizNewEntity() {
        LitemallOrder entity = orderBiz.newEntity();
        assertNotNull(entity);
        assertNull(entity.getOrderStatus(), "new order should have null orderStatus before initialization");
        assertNull(entity.getOrderSn(), "new order should have null orderSn before initialization");
    }

    @Test
    void testOrderGoodsBizNewEntity() {
        LitemallOrderGoods entity = orderGoodsBiz.newEntity();
        assertNotNull(entity);
        assertNull(entity.getOrderId(), "new orderGoods should have null orderId");
        assertNull(entity.getComment(), "new orderGoods should have null comment flag");
    }

    @Test
    void testCartBizNewEntity() {
        LitemallCart entity = cartBiz.newEntity();
        assertNotNull(entity);
        assertNull(entity.getUserId(), "new cart should have null userId");
        assertFalse(Boolean.TRUE.equals(entity.getChecked()), "new cart should not be checked by default");
    }
}
