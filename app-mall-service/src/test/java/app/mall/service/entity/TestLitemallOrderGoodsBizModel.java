package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallOrderGoods;
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
import io.nop.orm.IOrmTemplate;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallOrderGoodsBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    IOrmTemplate ormTemplate;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");
    }

    private String createOrderGoodsAtTime(LocalDateTime addTime) {
        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("OG-001");
        goods.setName("Comment Window Goods");
        goods.setRetailPrice(BigDecimal.TEN);
        goods.setIsOnSale(true);
        goods.setPicUrl("");
        goods.setShareUrl("");
        goods.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goods.getId());
        product.setSpecifications("[\"标准\"]");
        product.setPrice(BigDecimal.TEN);
        product.setNumber(100);
        product.setUrl("");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);

        LitemallOrderGoods og = daoProvider.daoFor(LitemallOrderGoods.class).newEntity();
        og.setOrderId("1");
        og.setGoodsId(goods.getId());
        og.setProductId(product.getId());
        og.setGoodsName("Comment Window Goods");
        og.setGoodsSn("OG-001");
        og.setNumber(1);
        og.setPrice(BigDecimal.TEN);
        og.setSpecifications("[\"标准\"]");
        og.setComment(0);
        og.setPicUrl("http://test.com/og-pic.png");
        daoProvider.daoFor(LitemallOrderGoods.class).saveEntity(og);

        String id = og.orm_idString();
        ormTemplate.runInSession(session -> {
            LitemallOrderGoods managed = daoProvider.daoFor(LitemallOrderGoods.class).getEntityById(id);
            managed.setAddTime(addTime);
            daoProvider.daoFor(LitemallOrderGoods.class).updateEntity(managed);
            return null;
        });
        return id;
    }

    @Test
    public void testExpireCommentWindow_expired() {
        String id = createOrderGoodsAtTime(LocalDateTime.now().minusDays(30));

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("timeoutDays", 1));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrderGoods__expireCommentWindow", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "expireCommentWindow failed: " + result);

        int count = ((Number) result.getData()).intValue();
        assertTrue(count >= 1, "expired order-goods should be processed");

        LitemallOrderGoods updated = daoProvider.daoFor(LitemallOrderGoods.class).getEntityById(id);
        assertEquals(-1, updated.getComment(), "comment should be marked expired (-1) after window passed");
    }

    @Test
    public void testExpireCommentWindow_notExpired() {
        String id = createOrderGoodsAtTime(LocalDateTime.now());

        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("timeoutDays", 1));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrderGoods__expireCommentWindow", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "expireCommentWindow failed: " + result);

        LitemallOrderGoods updated = daoProvider.daoFor(LitemallOrderGoods.class).getEntityById(id);
        assertEquals(0, updated.getComment(), "comment should remain 0 when within window");
    }
}
