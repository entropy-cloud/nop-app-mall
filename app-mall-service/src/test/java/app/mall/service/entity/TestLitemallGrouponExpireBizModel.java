package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallGroupon;
import app.mall.dao.entity.LitemallGrouponRules;
import app.mall.dao.entity.LitemallOrder;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
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
}
