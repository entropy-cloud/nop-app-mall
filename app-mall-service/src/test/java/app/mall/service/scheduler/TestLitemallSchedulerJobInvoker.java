package app.mall.service.scheduler;

import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.biz.ILitemallGrouponBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallOrderGoodsBiz;
import app.mall.biz.ILitemallResetCodeBiz;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TestLitemallSchedulerJobInvoker {

    @SuppressWarnings("unchecked")
    private static <T> T mockBiz(Class<T> type, Map<String, Integer> callCounter) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            callCounter.merge(method.getName(), 1, Integer::sum);
            if (method.getReturnType() == int.class || method.getReturnType() == Integer.class) {
                return 1;
            }
            if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
                return false;
            }
            return null;
        });
    }

    @Test
    void testInvokeAllJobs() {
        Map<String, Integer> orderCalls = new HashMap<>();
        Map<String, Integer> couponCalls = new HashMap<>();
        Map<String, Integer> grouponCalls = new HashMap<>();
        Map<String, Integer> orderGoodsCalls = new HashMap<>();
        Map<String, Integer> resetCodeCalls = new HashMap<>();

        ILitemallOrderBiz orderBiz = mockBiz(ILitemallOrderBiz.class, orderCalls);
        ILitemallCouponUserBiz couponUserBiz = mockBiz(ILitemallCouponUserBiz.class, couponCalls);
        ILitemallGrouponBiz grouponBiz = mockBiz(ILitemallGrouponBiz.class, grouponCalls);
        ILitemallOrderGoodsBiz orderGoodsBiz = mockBiz(ILitemallOrderGoodsBiz.class, orderGoodsCalls);
        ILitemallResetCodeBiz resetCodeBiz = mockBiz(ILitemallResetCodeBiz.class, resetCodeCalls);

        MallJobInvoker invoker = new MallJobInvoker();
        invoker.orderBiz = orderBiz;
        invoker.couponUserBiz = couponUserBiz;
        invoker.grouponBiz = grouponBiz;
        invoker.orderGoodsBiz = orderGoodsBiz;
        invoker.resetCodeBiz = resetCodeBiz;

        assertDoesNotThrow(invoker::cancelExpiredOrders);
        assertDoesNotThrow(invoker::confirmExpiredOrders);
        assertDoesNotThrow(invoker::expireCoupons);
        assertDoesNotThrow(invoker::expireGroupons);
        assertDoesNotThrow(invoker::expireCommentWindow);
        assertDoesNotThrow(invoker::cleanupExpiredResetCodes);

        org.junit.jupiter.api.Assertions.assertEquals(1, orderCalls.getOrDefault("cancelExpiredOrders", 0));
        org.junit.jupiter.api.Assertions.assertEquals(1, orderCalls.getOrDefault("confirmExpiredOrders", 0));
        org.junit.jupiter.api.Assertions.assertEquals(1, couponCalls.getOrDefault("expireCoupons", 0));
        org.junit.jupiter.api.Assertions.assertEquals(1, grouponCalls.getOrDefault("expireGroupons", 0));
        org.junit.jupiter.api.Assertions.assertEquals(1, orderGoodsCalls.getOrDefault("expireCommentWindow", 0));
        org.junit.jupiter.api.Assertions.assertEquals(1, resetCodeCalls.getOrDefault("cleanupExpiredResetCodes", 0));
    }
}
