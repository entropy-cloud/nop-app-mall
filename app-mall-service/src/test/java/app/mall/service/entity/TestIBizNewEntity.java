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
    }

    @Test
    void testOrderGoodsBizNewEntity() {
        LitemallOrderGoods entity = orderGoodsBiz.newEntity();
        assertNotNull(entity);
    }

    @Test
    void testCartBizNewEntity() {
        LitemallCart entity = cartBiz.newEntity();
        assertNotNull(entity);
    }
}
