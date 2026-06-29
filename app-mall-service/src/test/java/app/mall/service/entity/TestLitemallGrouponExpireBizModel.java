package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallGroupon;
import app.mall.dao.entity.LitemallGrouponRules;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallPromotionActivity;
import app.mall.dao.entity.LitemallPromotionTier;
import app.mall.dao.entity.LitemallPromotionUsage;
import app.mall.dao._AppMallDaoConstants;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallGrouponExpireBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String grouponId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("GE001");
        goods.setName("团购过期测试商品");
        goods.setRetailPrice(new BigDecimal("100.00"));
        goods.setIsOnSale(true);
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        String goodsId = goods.orm_idString();

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goodsId);
        product.setSpecifications("[\"标准\"]");
        product.setPrice(new BigDecimal("100.00"));
        product.setNumber(100);
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);

        LitemallGrouponRules rules = daoProvider.daoFor(LitemallGrouponRules.class).newEntity();
        rules.setGoodsId(goodsId);
        rules.setGoodsName("团购过期测试商品");
        rules.setDiscount(new BigDecimal("20.00"));
        rules.setDiscountMember(3);
        rules.setExpireTime(LocalDateTime.now().minusDays(1));
        rules.setStatus(0);
        daoProvider.daoFor(LitemallGrouponRules.class).saveEntity(rules);
        String rulesId = rules.orm_idString();

        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).newEntity();
        order.setConsignee("测试收货人");
        order.setMobile("13800138000");
        order.setAddress("测试地址");
        order.setMessage("测试");
        order.setDeleted(false);
        order.setOrderSn("GR_EXPIRE_001");
        order.setUserId("1");
        order.setOrderStatus(201);
        order.setGoodsPrice(new BigDecimal("100.00"));
        order.setFreightPrice(BigDecimal.ZERO);
        order.setCouponPrice(BigDecimal.ZERO);
        order.setIntegralPrice(BigDecimal.ZERO);
        order.setGrouponPrice(BigDecimal.ZERO);
        order.setPromotionPrice(BigDecimal.ZERO);
        order.setPinTuanPrice(BigDecimal.ZERO);
        order.setOrderPrice(new BigDecimal("100.00"));
        order.setActualPrice(new BigDecimal("100.00"));
        daoProvider.daoFor(LitemallOrder.class).saveEntity(order);
        String orderId = order.orm_idString();

        LitemallGroupon groupon = daoProvider.daoFor(LitemallGroupon.class).newEntity();
        groupon.setRulesId(rulesId);
        groupon.setOrderId(orderId);
        groupon.setUserId("1");
        groupon.setGrouponId("0");
        groupon.setCreatorUserId("1");
        groupon.setStatus(1);
        daoProvider.daoFor(LitemallGroupon.class).saveEntity(groupon);
        grouponId = groupon.orm_idString();
    }

    @Test
    public void testExpireGroupons() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGroupon__expireGroupons", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "expireGroupons failed: " + result);

        Number expiredCount = (Number) result.getData();
        assertTrue(expiredCount.intValue() >= 1, "Should expire at least 1 groupon");

        LitemallGroupon updated = daoProvider.daoFor(LitemallGroupon.class).requireEntityById(grouponId);
        assertEquals(2, updated.getStatus());
    }

    @Test
    public void testExpireGrouponsReleasesPromotionUsage() {
        // (e) groupon-fail whole-order refund releases a coexisting PromotionUsage (满减 may coexist
        //     with a groupon on the same order). Seed a usage on the @BeforeEach order, run expire,
        //     assert the usage is released.
        LitemallGroupon seeded = daoProvider.daoFor(LitemallGroupon.class).requireEntityById(grouponId);
        String orderId = seeded.getOrderId();
        savePromotionUsage(orderId);
        assertEquals(1, usageCountByOrder(orderId), "usage seeded");

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGroupon__expireGroupons", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "expireGroupons failed: " + result);

        assertEquals(0, usageCountByOrder(orderId),
                "groupon-fail refund should release coexisting PromotionUsage");
    }

    private void savePromotionUsage(String orderId) {
        LitemallPromotionActivity a = daoProvider.daoFor(LitemallPromotionActivity.class).newEntity();
        a.setName("满减团购共存");
        a.setDiscountType(_AppMallDaoConstants.DISCOUNT_TYPE_AMOUNT);
        a.setStatus(_AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        a.setGoodsScope(_AppMallDaoConstants.GOODS_SCOPE_ALL);
        a.setPriority(1);
        a.setStartTime(LocalDateTime.now().minusDays(1));
        a.setEndTime(LocalDateTime.now().plusDays(1));
        daoProvider.daoFor(LitemallPromotionActivity.class).saveEntity(a);

        LitemallPromotionTier t = daoProvider.daoFor(LitemallPromotionTier.class).newEntity();
        t.setActivityId(a.getId());
        t.setMeetAmount(new BigDecimal("100"));
        t.setDiscountValue(new BigDecimal("20"));
        daoProvider.daoFor(LitemallPromotionTier.class).saveEntity(t);

        LitemallPromotionUsage u = daoProvider.daoFor(LitemallPromotionUsage.class).newEntity();
        u.setUserId("1");
        u.setPromotionActivityId(a.orm_idString());
        u.setOrderId(orderId);
        u.setMeetAmount(new BigDecimal("100"));
        u.setDiscountAmount(new BigDecimal("20"));
        daoProvider.daoFor(LitemallPromotionUsage.class).saveEntity(u);
    }

    private long usageCountByOrder(String orderId) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPromotionUsage.PROP_NAME_orderId, orderId));
        return daoProvider.daoFor(LitemallPromotionUsage.class).findAllByQuery(q).size();
    }
}
