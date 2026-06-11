package app.mall.service.entity;

import app.mall.dao.entity.LitemallCart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LitemallCartAggregateTest {

    @Test
    public void testIncreaseNumber() {
        LitemallCart cart = new LitemallCart();
        cart.setNumber(1);
        cart.increaseNumber(2);
        assertEquals(3, cart.getNumber());
    }

    @Test
    public void testValidateForCheckout() {
        LitemallCart cart = new LitemallCart();
        cart.setUserId("1");
        cart.setProductId("2");
        cart.setNumber(1);
        assertDoesNotThrow(cart::validateForCheckout);
        cart.setNumber(0);
        assertThrows(IllegalStateException.class, cart::validateForCheckout);
    }
}
