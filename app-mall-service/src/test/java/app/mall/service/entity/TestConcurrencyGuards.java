package app.mall.service.entity;

import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.dao.entity.LitemallCoupon;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallCouponUser;
import app.mall.dao.mapper.LitemallGoodsProductMapper;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.core.context.ServiceContextImpl;
import io.nop.dao.api.IDaoProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(10)
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestConcurrencyGuards extends JunitBaseTestCase {

    @Inject
    IDaoProvider daoProvider;

    @Inject
    LitemallGoodsProductMapper goodsProductMapper;

    @Inject
    ILitemallCouponUserBiz couponUserBiz;

    @Test
    public void testReduceStockAtomicGuard() throws Exception {
        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("CC-001");
        goods.setName("Concurrency Goods");
        goods.setRetailPrice(BigDecimal.TEN);
        goods.setIsOnSale(true);
        goods.setPicUrl("");
        goods.setShareUrl("");
        goods.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goods.getId());
        product.setNumber(1);
        product.setPrice(BigDecimal.TEN);
        product.setSpecifications("[\"标准\"]");
        product.setUrl("");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> task = () -> {
                start.await(5, TimeUnit.SECONDS);
                return goodsProductMapper.reduceStock(product.getId(), 1);
            };
            Future<Integer> f1 = pool.submit(task);
            Future<Integer> f2 = pool.submit(task);
            start.countDown();

            int r1 = f1.get(5, TimeUnit.SECONDS);
            int r2 = f2.get(5, TimeUnit.SECONDS);
            assertEquals(1, r1 + r2, "exactly one concurrent stock deduction should succeed");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void testClaimCouponConcurrentSingleUser() throws Exception {
        LitemallCoupon coupon = daoProvider.daoFor(LitemallCoupon.class).newEntity();
        coupon.setName("Concurrency Coupon");
        coupon.setTag("test");
        coupon.setTotal(1);
        coupon.setDiscount(BigDecimal.ONE);
        coupon.setMin(BigDecimal.ZERO);
        coupon.setLimit(1);
        coupon.setType(0);
        coupon.setStatus(0);
        coupon.setGoodsType(0);
        coupon.setGoodsValue("");
        coupon.setCode("CC-CODE-1");
        coupon.setTimeType(0);
        coupon.setDays(30);
        coupon.setStartTime(LocalDateTime.now().minusDays(1));
        coupon.setEndTime(LocalDateTime.now().plusDays(30));
        daoProvider.daoFor(LitemallCoupon.class).saveEntity(coupon);

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> task = () -> {
                start.await(5, TimeUnit.SECONDS);
                try {
                    couponUserBiz.claimCouponForUser(coupon.getId(), "concurrent-user", new ServiceContextImpl());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            };
            Future<Boolean> f1 = pool.submit(task);
            Future<Boolean> f2 = pool.submit(task);
            start.countDown();

            int success = 0;
            if (Boolean.TRUE.equals(f1.get(5, TimeUnit.SECONDS))) {
                success++;
            }
            if (Boolean.TRUE.equals(f2.get(5, TimeUnit.SECONDS))) {
                success++;
            }

            QueryBean query = new QueryBean();
            query.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_couponId, coupon.getId()));
            query.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_userId, "concurrent-user"));
            query.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_deleted, false));
            long count = couponUserBiz.findCount(query, new ServiceContextImpl());

            assertEquals(1, success, "only one concurrent claim should succeed");
            assertEquals(1L, count, "DB should contain exactly one coupon claim for the same user");
        } finally {
            pool.shutdownNow();
        }
    }
}
