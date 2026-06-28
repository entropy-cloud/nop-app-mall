package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallPointsAccount;
import app.mall.dao.entity.LitemallPointsExchangeOrder;
import app.mall.dao.entity.LitemallPointsFlow;
import app.mall.dao.entity.LitemallPointsGoods;
import app.mall.dao.entity.LitemallSystem;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallPointsExchangeOrderBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    private static final String USER_ID = "801";

    String goodsId;
    String productId;
    String addressId;
    String pointsGoodsId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId(USER_ID);
        ContextProvider.getOrCreateContext().setUserName("exchange-test");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G-PE");
        goods.setName("Points Exchange Goods");
        goods.setCounterPrice(BigDecimal.valueOf(200));
        goods.setRetailPrice(BigDecimal.valueOf(199));
        goods.setIsOnSale(true);
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.getId();

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goodsId);
        product.setNumber(100);
        product.setPrice(BigDecimal.valueOf(199));
        product.setSpecifications("[\"标准\"]");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);
        productId = product.getId();

        LitemallAddress address = daoProvider.daoFor(LitemallAddress.class).newEntity();
        address.setUserId(USER_ID);
        address.setName("李四");
        address.setTel("13900139000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setCounty("南山区");
        address.setAddressDetail("科技园");
        address.setIsDefault(true);
        daoProvider.daoFor(LitemallAddress.class).saveEntity(address);
        addressId = address.getId();

        // Seed the user's points account with 1000 points.
        seedPoints(USER_ID, 1000);
    }

    private void seedPoints(String userId, int balance) {
        LitemallPointsAccount acct = daoProvider.daoFor(LitemallPointsAccount.class).newEntity();
        acct.setUserId(userId);
        acct.setBalance(balance);
        acct.setTotalEarned(balance);
        acct.setTotalSpent(0);
        acct.setVersion(0);
        daoProvider.daoFor(LitemallPointsAccount.class).saveEntity(acct);
    }

    private String createPointsGoods(int pointsPrice, int exchangeStock, Integer maxPerUser,
                                      LocalDateTime startTime, LocalDateTime endTime, int status,
                                      boolean retailOnSale) {
        // For the off-shelf test case, create a distinct retail goods that is not on sale,
        // rather than mutating the shared setUp goods (which is already managed/flushed).
        String targetGoodsId = goodsId;
        if (!retailOnSale) {
            LitemallGoods offShelf = daoProvider.daoFor(LitemallGoods.class).newEntity();
            offShelf.setGoodsSn("G-PE-OFF");
            offShelf.setName("Off Shelf Goods");
            offShelf.setRetailPrice(BigDecimal.valueOf(199));
            offShelf.setIsOnSale(false);
            daoProvider.daoFor(LitemallGoods.class).saveEntity(offShelf);
            targetGoodsId = offShelf.getId();
        }
        LitemallPointsGoods pg = daoProvider.daoFor(LitemallPointsGoods.class).newEntity();
        pg.setGoodsId(targetGoodsId);
        pg.setProductId(productId);
        pg.setPointsPrice(pointsPrice);
        pg.setExchangeStock(exchangeStock);
        pg.setExchangedCount(0);
        pg.setMaxPerUser(maxPerUser);
        pg.setStartTime(startTime);
        pg.setEndTime(endTime);
        pg.setStatus(status);
        daoProvider.daoFor(LitemallPointsGoods.class).saveEntity(pg);
        return pg.orm_idString();
    }

    private void setConfig(String key, String value) {
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName", key);
        cfg.orm_propValueByName("keyValue", value);
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<Map<String, Object>> rpc(GraphQLOperationType op, String action, Map<String, Object> data) {
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(op, action, ApiRequest.build(data));
        return (ApiResponse<Map<String, Object>>) graphQLEngine.executeRpc(ctx);
    }

    private ApiResponse<?> exchange(String pointsGoodsId, int quantity, String addressId) {
        Map<String, Object> data = new HashMap<>();
        data.put("pointsGoodsId", pointsGoodsId);
        data.put("quantity", quantity);
        if (addressId != null) data.put("addressId", addressId);
        return rpc(GraphQLOperationType.mutation, "LitemallPointsExchangeOrder__exchange", data);
    }

    private int getMyPoints() {
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallPointsAccount__getMyPoints", new HashMap<>());
        assertEquals(0, r.getStatus(), "getMyPoints failed: " + r);
        return ((Number) r.getData()).intValue();
    }

    private LitemallPointsGoods reloadPointsGoods(String id) {
        return daoProvider.daoFor(LitemallPointsGoods.class).getEntityById(id);
    }

    private long countFlows(String sourceType, String sourceId) {
        QueryBean q = new QueryBean();
        q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType, sourceType));
        if (sourceId != null) {
            q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceId, sourceId));
        }
        return daoProvider.daoFor(LitemallPointsFlow.class).findAllByQuery(q).size();
    }

    @Test
    public void testExchangeSuccessDeductsPointsAndStock() {
        pointsGoodsId = createPointsGoods(100, 10, 0, null, null,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, true);

        ApiResponse<?> r = exchange(pointsGoodsId, 2, addressId);
        assertEquals(0, r.getStatus(), "exchange should succeed: " + r);

        // Points deducted: 100 * 2 = 200, leaving 1000 - 200 = 800
        assertEquals(800, getMyPoints());

        // Exchange stock reduced; exchangedCount incremented.
        LitemallPointsGoods pg = reloadPointsGoods(pointsGoodsId);
        assertEquals(8, pg.getExchangeStock());
        assertEquals(2, pg.getExchangedCount());

        // Order is PENDING with denormalized snapshot.
        Map<String, Object> orderData = (Map<String, Object>) r.getData();
        assertEquals(_AppMallDaoConstants.EXCHANGE_STATUS_PENDING, ((Number) orderData.get("exchangeStatus")).intValue());
        assertEquals(200, ((Number) orderData.get("totalPoints")).intValue());
        assertEquals("Points Exchange Goods", orderData.get("goodsName"));
        assertNotNull(orderData.get("consignee"));

        // A single SPEND flow was recorded with sourceType=mall-exchange.
        assertEquals(1, countFlows(LitemallPointsExchangeOrderBizModel.SOURCE_TYPE_MALL_EXCHANGE, null));
    }

    @Test
    public void testExchangeFailsWhenGoodsNotActive() {
        pointsGoodsId = createPointsGoods(100, 10, 0, null, null,
                _AppMallDaoConstants.PROMOTION_STATUS_DRAFT, true);

        ApiResponse<?> r = exchange(pointsGoodsId, 1, addressId);
        assertNotEquals(0, r.getStatus(), "non-ACTIVE goods must not be exchangeable");
        assertEquals(1000, getMyPoints(), "balance must not change on failed exchange");
    }

    @Test
    public void testExchangeFailsWhenSoldOut() {
        pointsGoodsId = createPointsGoods(100, 0, 0, null, null,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, true);

        ApiResponse<?> r = exchange(pointsGoodsId, 1, addressId);
        assertNotEquals(0, r.getStatus(), "zero stock must reject");
        assertEquals(1000, getMyPoints());
    }

    @Test
    public void testExchangeFailsWhenInsufficientPoints() {
        pointsGoodsId = createPointsGoods(2000, 10, 0, null, null,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, true);

        ApiResponse<?> r = exchange(pointsGoodsId, 1, addressId);
        assertNotEquals(0, r.getStatus(), "insufficient points must reject");
        // Balance unchanged, stock unchanged (tx rolled back).
        assertEquals(1000, getMyPoints());
        LitemallPointsGoods pg = reloadPointsGoods(pointsGoodsId);
        assertEquals(10, pg.getExchangeStock());
    }

    @Test
    public void testExchangeFailsWhenOverLimitPerUser() {
        pointsGoodsId = createPointsGoods(100, 50, 1, null, null,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, true);

        // First exchange succeeds (1 unit, max 1).
        ApiResponse<?> r1 = exchange(pointsGoodsId, 1, addressId);
        assertEquals(0, r1.getStatus(), "first exchange within cap should succeed: " + r1);

        // Second exchange exceeds the per-user cap of 1.
        ApiResponse<?> r2 = exchange(pointsGoodsId, 1, addressId);
        assertNotEquals(0, r2.getStatus(), "exchange beyond per-user cap must reject");
    }

    @Test
    public void testExchangeFailsWhenRetailOffShelf() {
        pointsGoodsId = createPointsGoods(100, 10, 0, null, null,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, false);

        ApiResponse<?> r = exchange(pointsGoodsId, 1, addressId);
        assertNotEquals(0, r.getStatus(), "retail goods off-shelf must reject exchange");
        assertEquals(1000, getMyPoints());
    }

    @Test
    public void testExchangeFailsWhenOutsideTimeWindow() {
        LocalDateTime past = LocalDateTime.now().minusDays(2);
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        pointsGoodsId = createPointsGoods(100, 10, 0, past, yesterday,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, true);

        ApiResponse<?> r = exchange(pointsGoodsId, 1, addressId);
        assertNotEquals(0, r.getStatus(), "expired time window must reject");
        assertEquals(1000, getMyPoints());
    }

    @Test
    public void testExchangeFailsWithInvalidQuantity() {
        pointsGoodsId = createPointsGoods(100, 10, 0, null, null,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, true);

        ApiResponse<?> r = exchange(pointsGoodsId, 0, addressId);
        assertNotEquals(0, r.getStatus(), "zero quantity must reject");
    }

    @Test
    public void testCancelRestoresStockAndRefundsPoints() {
        pointsGoodsId = createPointsGoods(100, 10, 0, null, null,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, true);

        ApiResponse<?> exR = exchange(pointsGoodsId, 2, addressId);
        assertEquals(0, exR.getStatus(), "exchange should succeed: " + exR);
        String orderId = ((Map<String, Object>) exR.getData()).get("id").toString();
        assertEquals(800, getMyPoints());

        Map<String, Object> cancelData = new HashMap<>();
        cancelData.put("id", orderId);
        cancelData.put("remark", "user cancelled");
        ApiResponse<?> cancelR = rpc(GraphQLOperationType.mutation,
                "LitemallPointsExchangeOrder__cancelExchangeOrder", cancelData);
        assertEquals(0, cancelR.getStatus(), "cancel should succeed: " + cancelR);

        // Points refunded back to 1000.
        assertEquals(1000, getMyPoints());

        // Stock restored.
        LitemallPointsGoods pg = reloadPointsGoods(pointsGoodsId);
        assertEquals(10, pg.getExchangeStock());

        // Status CANCELLED.
        Map<String, Object> cancelled = (Map<String, Object>) cancelR.getData();
        assertEquals(_AppMallDaoConstants.EXCHANGE_STATUS_CANCELLED,
                ((Number) cancelled.get("exchangeStatus")).intValue());

        // A refund earn flow recorded with sourceType=mall-exchange-refund.
        assertEquals(1, countFlows(LitemallPointsExchangeOrderBizModel.SOURCE_TYPE_MALL_EXCHANGE_REFUND, orderId));
    }

    @Test
    public void testShipAndConfirmFlow() {
        pointsGoodsId = createPointsGoods(100, 10, 0, null, null,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, true);

        ApiResponse<?> exR = exchange(pointsGoodsId, 1, addressId);
        String orderId = ((Map<String, Object>) exR.getData()).get("id").toString();

        // Admin ships the exchange order via the mutation (state machine PENDING→SHIPPED).
        Map<String, Object> shipData = new HashMap<>();
        shipData.put("id", orderId);
        shipData.put("shipCode", "SF-12345");
        ApiResponse<?> shipR = rpc(GraphQLOperationType.mutation,
                "LitemallPointsExchangeOrder__shipExchangeOrder", shipData);
        assertEquals(0, shipR.getStatus(), "ship should succeed: " + shipR);
        assertEquals(_AppMallDaoConstants.EXCHANGE_STATUS_SHIPPED,
                ((Number) ((Map<String, Object>) shipR.getData()).get("exchangeStatus")).intValue());

        // User confirms receipt (state machine SHIPPED→COMPLETED).
        Map<String, Object> confirmData = new HashMap<>();
        confirmData.put("id", orderId);
        ApiResponse<?> confirmR = rpc(GraphQLOperationType.mutation,
                "LitemallPointsExchangeOrder__confirmExchangeOrder", confirmData);
        assertEquals(0, confirmR.getStatus(), "confirm should succeed: " + confirmR);
        Map<String, Object> confirmed = (Map<String, Object>) confirmR.getData();
        assertEquals(_AppMallDaoConstants.EXCHANGE_STATUS_COMPLETED,
                ((Number) confirmed.get("exchangeStatus")).intValue());
    }

    @Test
    public void testMyExchangeOrdersFiltersByStatus() {
        pointsGoodsId = createPointsGoods(100, 50, 0, null, null,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, true);

        exchange(pointsGoodsId, 1, addressId);
        exchange(pointsGoodsId, 1, addressId);

        Map<String, Object> data = new HashMap<>();
        data.put("exchangeStatus", _AppMallDaoConstants.EXCHANGE_STATUS_PENDING);
        ApiResponse<?> r = rpc(GraphQLOperationType.query,
                "LitemallPointsExchangeOrder__myExchangeOrders", data);
        assertEquals(0, r.getStatus());
        Map<String, Object> result = (Map<String, Object>) r.getData();
        assertEquals(2L, ((Number) result.get("total")).longValue());
        List<?> list = (List<?>) result.get("list");
        assertEquals(2, list.size());
    }

    @Test
    public void testPointsAccountReconcilesWithFlowBalanceAfter() {
        pointsGoodsId = createPointsGoods(100, 10, 0, null, null,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE, true);

        exchange(pointsGoodsId, 3, addressId);

        // Verify the SPEND flow's balanceAfter matches the current account balance.
        QueryBean q = new QueryBean();
        q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType,
                LitemallPointsExchangeOrderBizModel.SOURCE_TYPE_MALL_EXCHANGE));
        List<LitemallPointsFlow> flows = daoProvider.daoFor(LitemallPointsFlow.class).findAllByQuery(q);
        assertEquals(1, flows.size());
        assertEquals(700, flows.get(0).getBalanceAfter().intValue(),
                "flow balanceAfter must snapshot the post-spend balance");

        LitemallPointsAccount acct = daoProvider.daoFor(LitemallPointsAccount.class).findAllByQuery(new QueryBean())
                .stream().findFirst().orElse(null);
        assertNotNull(acct);
        assertEquals(700, acct.getBalance());
        assertEquals(300, acct.getTotalSpent());
        assertTrue(acct.getVersion() > 0, "optimistic version must increment on mutation");
    }
}
