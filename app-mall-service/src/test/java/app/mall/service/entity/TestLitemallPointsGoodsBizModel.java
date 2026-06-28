package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallPointsGoods;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallPointsGoodsBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String goodsId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("pg-test");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G-PG");
        goods.setName("Points Catalog Goods");
        goods.setRetailPrice(BigDecimal.valueOf(199));
        goods.setIsOnSale(true);
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.getId();
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<Map<String, Object>> rpc(GraphQLOperationType op, String action, Map<String, Object> data) {
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(op, action, ApiRequest.build(data));
        return (ApiResponse<Map<String, Object>>) graphQLEngine.executeRpc(ctx);
    }

    private String createPointsGoods(int pointsPrice, int status) {
        LitemallPointsGoods pg = daoProvider.daoFor(LitemallPointsGoods.class).newEntity();
        pg.setGoodsId(goodsId);
        pg.setPointsPrice(pointsPrice);
        pg.setExchangeStock(10);
        pg.setExchangedCount(0);
        pg.setStatus(status);
        daoProvider.daoFor(LitemallPointsGoods.class).saveEntity(pg);
        return pg.orm_idString();
    }

    @Test
    public void testActivePointsGoodsListsOnlyActive() {
        createPointsGoods(100, _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        createPointsGoods(200, _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        createPointsGoods(300, _AppMallDaoConstants.PROMOTION_STATUS_DRAFT);

        ApiResponse<?> r = rpc(GraphQLOperationType.query,
                "LitemallPointsGoods__activePointsGoods", new HashMap<>());
        assertEquals(0, r.getStatus(), r.toString());
        Map<String, Object> result = (Map<String, Object>) r.getData();
        List<?> list = (List<?>) result.get("list");
        assertEquals(2, list.size(), "only ACTIVE goods should be listed");
        Map<String, Object> card = (Map<String, Object>) list.get(0);
        assertNotNull(card.get("goodsName"), "card must include retail goods snapshot");
        assertNotNull(card.get("inWindow"), "card must report time-window status");
    }

    @Test
    public void testPointsGoodsDetailReturnsSnapshot() {
        String id = createPointsGoods(150, _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        ApiResponse<?> r = rpc(GraphQLOperationType.query,
                "LitemallPointsGoods__pointsGoodsDetail", data);
        assertEquals(0, r.getStatus(), r.toString());
        Map<String, Object> detail = (Map<String, Object>) r.getData();
        assertEquals(150, ((Number) detail.get("pointsPrice")).intValue());
        assertEquals("Points Catalog Goods", detail.get("goodsName"));
    }

    @Test
    public void testPublishUnpublishTogglesStatus() {
        String id = createPointsGoods(100, _AppMallDaoConstants.PROMOTION_STATUS_DRAFT);

        Map<String, Object> pub = new HashMap<>();
        pub.put("id", id);
        ApiResponse<?> pubR = rpc(GraphQLOperationType.mutation,
                "LitemallPointsGoods__publishActivity", pub);
        assertEquals(0, pubR.getStatus(), "publish should succeed: " + pubR);
        assertEquals(_AppMallDaoConstants.PROMOTION_STATUS_ACTIVE,
                ((Number) ((Map<String, Object>) pubR.getData()).get("status")).intValue());

        Map<String, Object> unp = new HashMap<>();
        unp.put("id", id);
        ApiResponse<?> unpR = rpc(GraphQLOperationType.mutation,
                "LitemallPointsGoods__unpublishActivity", unp);
        assertEquals(0, unpR.getStatus(), "unpublish should succeed: " + unpR);
        assertEquals(_AppMallDaoConstants.PROMOTION_STATUS_CLOSED,
                ((Number) ((Map<String, Object>) unpR.getData()).get("status")).intValue());
    }

    @Test
    public void testPublishRejectsInvalidTransition() {
        String id = createPointsGoods(100, _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);

        Map<String, Object> pub = new HashMap<>();
        pub.put("id", id);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation,
                "LitemallPointsGoods__publishActivity", pub);
        assertNotEquals(0, r.getStatus(), "publishing an already-ACTIVE goods must reject");
    }
}
