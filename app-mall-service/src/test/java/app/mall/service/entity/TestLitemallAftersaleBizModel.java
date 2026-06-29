package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallAftersale;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.entity.LitemallPromotionActivity;
import app.mall.dao.entity.LitemallPromotionTier;
import app.mall.dao.entity.LitemallPromotionUsage;
import app.mall.pay.PayService;
import app.mall.wx.WxPayServiceImpl;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.file.dao.entity.NopFileRecord;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.nop.api.core.beans.FilterBeans.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallAftersaleBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    PayService payService;

    String goodsId;
    String productId;
    String addressId;

    private void createFileRecord(String fileId, String bizObjName) {
        NopFileRecord record = daoProvider.daoFor(NopFileRecord.class).newEntity();
        record.setFileId(fileId);
        record.setBizObjName(bizObjName);
        record.setBizObjId("temp");
        record.setFieldName("temp");
        record.setOriginFileId(fileId);
        record.setFileName(fileId + ".png");
        record.setFilePath("/test/" + fileId + ".png");
        record.setFileExt("png");
        record.setMimeType("image/png");
        record.setIsPublic(true);
        daoProvider.daoFor(NopFileRecord.class).saveEntity(record);
    }

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        createFileRecord("goods-pic", "LitemallGoods");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G001");
        goods.setName("Test Goods");
        goods.setPicUrl("http://test.com/goods-pic.png");
        goods.setCounterPrice(BigDecimal.valueOf(100));
        goods.setRetailPrice(BigDecimal.valueOf(99));
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.getId();

        createFileRecord("product-pic", "LitemallGoodsProduct");

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goodsId);
        product.setNumber(100);
        product.setPrice(BigDecimal.valueOf(99));
        product.setUrl("http://test.com/product-pic.png");
        product.setSpecifications("[\"标准\"]");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);
        productId = product.getId();

        LitemallAddress address = daoProvider.daoFor(LitemallAddress.class).newEntity();
        address.setUserId("1");
        address.setName("张三");
        address.setTel("13800138000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setCounty("南山区");
        address.setAddressDetail("科技园");
        address.setIsDefault(true);
        daoProvider.daoFor(LitemallAddress.class).saveEntity(address);
        addressId = address.getId();

        createFileRecord("cart-pic", "LitemallCart");

        LitemallCart cart = daoProvider.daoFor(LitemallCart.class).newEntity();
        cart.setUserId("1");
        cart.setGoodsId(goodsId);
        cart.setProductId(productId);
        cart.setNumber(2);
        cart.setPrice(BigDecimal.valueOf(99));
        cart.setChecked(true);
        cart.setGoodsSn(goods.getGoodsSn());
        cart.setGoodsName(goods.getName());
        cart.setPicUrl("/f/download/cart-pic");
        cart.setSpecifications("[\"标准\"]");
        daoProvider.daoFor(LitemallCart.class).saveEntity(cart);
    }

    private void addSecondSkuToCart() {
        createFileRecord("product-pic-2", "LitemallGoodsProduct");
        LitemallGoodsProduct product2 = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product2.setGoodsId(goodsId);
        product2.setNumber(100);
        product2.setPrice(BigDecimal.valueOf(50));
        product2.setUrl("http://test.com/product-pic-2.png");
        product2.setSpecifications("[\"加量\"]");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product2);

        createFileRecord("cart-pic-2", "LitemallCart");
        LitemallCart cart2 = daoProvider.daoFor(LitemallCart.class).newEntity();
        cart2.setUserId("1");
        cart2.setGoodsId(goodsId);
        cart2.setProductId(product2.getId());
        cart2.setNumber(1);
        cart2.setPrice(BigDecimal.valueOf(50));
        cart2.setChecked(true);
        cart2.setGoodsSn("G001");
        cart2.setGoodsName("Test Goods");
        cart2.setPicUrl("/f/download/cart-pic-2");
        cart2.setSpecifications("[\"加量\"]");
        daoProvider.daoFor(LitemallCart.class).saveEntity(cart2);
    }

    @SuppressWarnings("unchecked")
    private String createAndPayOrder() {
        return createAndPayOrder(false);
    }

    @SuppressWarnings("unchecked")
    private String createAndPayOrder(boolean multiItem) {
        if (multiItem) {
            addSecondSkuToCart();
        }
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "test",
                "freightPrice", BigDecimal.ZERO
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "submit failed: " + result.getMsg() + " / " + result.getData());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        String orderId = (String) data.get("id");

        ApiRequest<Map<String, Object>> payReq = ApiRequest.build(Map.of("orderId", orderId));
        IGraphQLExecutionContext payCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__pay", payReq);
        graphQLEngine.executeRpc(payCtx);
        return orderId;
    }

    private List<LitemallOrderGoods> orderGoodsOf(String orderId) {
        QueryBean q = new QueryBean();
        q.addFilter(eq(LitemallOrderGoods.PROP_NAME_orderId, orderId));
        return daoProvider.daoFor(LitemallOrderGoods.class).findAllByQuery(q);
    }

    private ApiResponse<?> apply(Map<String, Object> data) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAftersale__apply", req);
        return graphQLEngine.executeRpc(ctx);
    }

    private void approve(String aftersaleId) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("ids", Set.of(aftersaleId)));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAftersale__batchApprove", req);
        ApiResponse<?> r = graphQLEngine.executeRpc(ctx);
        assertEquals(0, r.getStatus(), "batchApprove failed: " + r);
    }

    private ApiResponse<?> refund(String aftersaleId) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", aftersaleId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAftersale__refund", req);
        return graphQLEngine.executeRpc(ctx);
    }

    // ============ whole-order (orderItemId=null) compatibility path ============

    @SuppressWarnings("unchecked")
    @Test
    public void testApplyAndCancel() {
        String orderId = createAndPayOrder();

        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId,
                "type", 0,
                "reason", "不想要了",
                "amount", BigDecimal.valueOf(198)
        ));
        assertEquals(0, applyResult.getStatus(), "apply failed: " + applyResult);
        Map<String, Object> aftersale = (Map<String, Object>) applyResult.getData();
        assertNotNull(aftersale);
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_REQUEST, aftersale.get("status"));
        String aftersaleId = (String) aftersale.get("id");

        ApiRequest<Map<String, Object>> cancelReq = ApiRequest.build(Map.of("id", aftersaleId));
        IGraphQLExecutionContext cancelCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAftersale__cancel", cancelReq);
        ApiResponse<?> cancelResult = graphQLEngine.executeRpc(cancelCtx);
        assertEquals(0, cancelResult.getStatus(), "cancel failed: " + cancelResult);
        Map<String, Object> cancelled = (Map<String, Object>) cancelResult.getData();
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_CANCELLED, cancelled.get("status"));

        // After cancel, order aggregate aftersaleStatus returns to INIT.
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_INIT, order.getAftersaleStatus());
    }

    @Test
    public void testApplyAmountExceedsActualPrice() {
        String orderId = createAndPayOrder();

        // amount 99999 exceeds the order's actualPrice (~198) -> apply must be rejected (valid reason so
        // the rejection is genuinely about the amount, not the reason).
        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId,
                "type", 0,
                "reason", "质量问题",
                "amount", BigDecimal.valueOf(99999)
        ));
        assertEquals(-1, applyResult.getStatus(),
                "apply with amount exceeding actualPrice should be rejected: " + applyResult);

        // zero / negative amount is also invalid
        ApiResponse<?> zeroResult = apply(Map.of(
                "orderId", orderId,
                "type", 0,
                "reason", "不想要了",
                "amount", BigDecimal.ZERO
        ));
        assertEquals(-1, zeroResult.getStatus(),
                "apply with zero amount should be rejected: " + zeroResult);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUserListAndDetail() throws Exception {
        String orderId = createAndPayOrder();

        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId,
                "type", 0,
                "reason", "质量问题",
                "amount", BigDecimal.valueOf(100)
        ));
        assertEquals(0, applyResult.getStatus());
        Map<String, Object> aftersale = (Map<String, Object>) applyResult.getData();
        String aftersaleId = (String) aftersale.get("id");

        ApiRequest<Map<String, Object>> listReq = ApiRequest.build(Map.of());
        IGraphQLExecutionContext listCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallAftersale__userList", listReq);
        ApiResponse<?> listResult = graphQLEngine.executeRpc(listCtx);
        assertEquals(0, listResult.getStatus());
        java.util.List<Map<String, Object>> list = (java.util.List<Map<String, Object>>) listResult.getData();
        assertNotNull(list);
        assertEquals(1, list.size());

        ApiRequest<Map<String, Object>> detailReq = ApiRequest.build(Map.of("id", aftersaleId));
        IGraphQLExecutionContext detailCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallAftersale__userDetail", detailReq);
        ApiResponse<?> detailResult = graphQLEngine.executeRpc(detailCtx);
        assertEquals(0, detailResult.getStatus());
        Map<String, Object> detail = (Map<String, Object>) detailResult.getData();
        assertNotNull(detail);
        assertEquals(aftersaleId, detail.get("id"));
    }

    @Test
    public void testRefundSuccess() {
        String orderId = createAndPayOrder();
        String aftersaleId = createApplyAndApprove(orderId);

        LitemallGoodsProduct productBefore = daoProvider.daoFor(LitemallGoodsProduct.class).getEntityById(productId);
        int stockBeforeRefund = productBefore.getNumber();

        ApiResponse<?> refundResult = refund(aftersaleId);
        assertEquals(0, refundResult.getStatus(), "refund failed: " + refundResult);

        LitemallAftersale updated = daoProvider.daoFor(LitemallAftersale.class).getEntityById(aftersaleId);
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_REFUND, updated.getStatus());

        // Whole-order refund on an unshipped PAY order pushes the order to REFUND_CONFIRM(203) and restocks.
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_REFUND_CONFIRM, order.getOrderStatus());

        LitemallGoodsProduct productAfter = daoProvider.daoFor(LitemallGoodsProduct.class).getEntityById(productId);
        assertEquals(stockBeforeRefund + 2, productAfter.getNumber(),
                "stock should be restored (+2 units) after whole-order refund: before="
                        + stockBeforeRefund + " after=" + productAfter.getNumber());
    }

    @Test
    public void testRefundFailure() {
        String orderId = createAndPayOrder();
        String aftersaleId = createApplyAndApprove(orderId);

        WxPayServiceImpl.setForceRefundFailure(true);
        try {
            ApiResponse<?> refundResult = refund(aftersaleId);
            assertTrue(refundResult.getStatus() != 0, "refund should fail");
            assertTrue(refundResult.getMsg().contains("退款失败") || refundResult.getMsg().contains("refund-failed"),
                    "error should contain refund-failed: " + refundResult.getMsg());

            LitemallAftersale unchanged = daoProvider.daoFor(LitemallAftersale.class).getEntityById(aftersaleId);
            assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_APPROVED, unchanged.getStatus(),
                    "status should remain APPROVED after refund failure");
        } finally {
            WxPayServiceImpl.setForceRefundFailure(false);
        }
    }

    private String createApplyAndApprove(String orderId) {
        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId,
                "type", 0,
                "reason", "质量问题",
                "amount", BigDecimal.valueOf(198)
        ));
        assertEquals(0, applyResult.getStatus(), "apply failed: " + applyResult);
        Map<String, Object> aftersale = (Map<String, Object>) applyResult.getData();
        String aftersaleId = (String) aftersale.get("id");
        approve(aftersaleId);
        return aftersaleId;
    }

    // ============ item-level (orderItemId != null) path ============

    @Test
    public void testItemLevelApplyAmountExceedsLineCap() {
        String orderId = createAndPayOrder();
        String orderItemId = orderGoodsOf(orderId).get(0).getId();
        // line = 2 x 99 = 198; amount 99999 exceeds the per-item cap.
        ApiResponse<?> r = apply(Map.of(
                "orderId", orderId,
                "orderItemId", orderItemId,
                "type", 0,
                "reason", "质量问题",
                "amount", BigDecimal.valueOf(99999)
        ));
        assertEquals(-1, r.getStatus(), "item apply over line cap should be rejected: " + r);
    }

    @Test
    public void testItemTypeStatusMismatch() {
        String orderId = createAndPayOrder();
        String orderItemId = orderGoodsOf(orderId).get(0).getId();
        // PAY(201) order only allows GOODS_MISS(0); GOODS_REQUIRED(2) must be rejected.
        ApiResponse<?> r = apply(Map.of(
                "orderId", orderId,
                "orderItemId", orderItemId,
                "type", 2,
                "reason", "质量问题",
                "amount", BigDecimal.valueOf(50)
        ));
        assertEquals(-1, r.getStatus(), "type/status mismatch should be rejected: " + r);
    }

    @Test
    public void testItemReasonInvalid() {
        String orderId = createAndPayOrder();
        String orderItemId = orderGoodsOf(orderId).get(0).getId();
        // reason not in mall/aftersale-reason dictionary must be rejected.
        ApiResponse<?> r = apply(Map.of(
                "orderId", orderId,
                "orderItemId", orderItemId,
                "type", 0,
                "reason", "随便编的理由",
                "amount", BigDecimal.valueOf(50)
        ));
        assertEquals(-1, r.getStatus(), "invalid reason should be rejected: " + r);
    }

    @Test
    public void testItemNotInOrder() {
        String orderId = createAndPayOrder();
        // orderItemId that does not belong to this order must be rejected.
        ApiResponse<?> r = apply(Map.of(
                "orderId", orderId,
                "orderItemId", "999999",
                "type", 0,
                "reason", "质量问题",
                "amount", BigDecimal.valueOf(50)
        ));
        assertEquals(-1, r.getStatus(), "item not in order should be rejected: " + r);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testItemInProgressMutex() {
        String orderId = createAndPayOrder();
        String orderItemId = orderGoodsOf(orderId).get(0).getId();

        ApiResponse<?> first = apply(Map.of(
                "orderId", orderId,
                "orderItemId", orderItemId,
                "type", 0,
                "reason", "质量问题",
                "amount", BigDecimal.valueOf(50)
        ));
        assertEquals(0, first.getStatus(), "first item apply should succeed: " + first);

        // Same item still in progress (REQUEST) -> second apply must be rejected.
        ApiResponse<?> second = apply(Map.of(
                "orderId", orderId,
                "orderItemId", orderItemId,
                "type", 0,
                "reason", "质量问题",
                "amount", BigDecimal.valueOf(50)
        ));
        assertEquals(-1, second.getStatus(), "in-progress item mutex should reject second apply: " + second);

        // The order aggregate aftersaleStatus reflects the in-progress item.
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_REQUEST, order.getAftersaleStatus());
    }

    @Test
    public void testItemLevelPartialRefundKeepsOrderPay() {
        // Multi-item order: refunding one item must NOT push the unshipped order to terminal 203,
        // and must restock only the refunded line.
        String orderId = createAndPayOrder(true);
        List<LitemallOrderGoods> goods = orderGoodsOf(orderId);
        assertEquals(2, goods.size(), "multi-item order should have 2 order goods lines");

        // Identify lines by productId (findAllByQuery order is unspecified).
        LitemallOrderGoods line99 = goods.stream()
                .filter(g -> productId.equals(g.getProductId())).findFirst().orElseThrow();
        LitemallOrderGoods line50 = goods.stream()
                .filter(g -> !productId.equals(g.getProductId())).findFirst().orElseThrow();
        String line50ProductId = line50.getProductId();

        LitemallGoodsProduct p99Before = daoProvider.daoFor(LitemallGoodsProduct.class).getEntityById(productId);
        int p99StockBefore = p99Before.getNumber();
        LitemallGoodsProduct p50Before = daoProvider.daoFor(LitemallGoodsProduct.class).getEntityById(line50ProductId);
        int p50StockBefore = p50Before.getNumber();

        String aftersaleId = createApplyAndApprove(orderId, line99.getId(), BigDecimal.valueOf(99));

        ApiResponse<?> refundResult = refund(aftersaleId);
        assertEquals(0, refundResult.getStatus(), "item refund failed: " + refundResult);

        // Partial item refund: unshipped order stays PAY (not all items refunded).
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_PAY, order.getOrderStatus(),
                "partial item refund should keep unshipped order at PAY(201)");

        // Only the refunded line is restocked.
        LitemallGoodsProduct p99After = daoProvider.daoFor(LitemallGoodsProduct.class).getEntityById(productId);
        assertEquals(p99StockBefore + line99.getNumber(), p99After.getNumber(),
                "refunded line should be restocked by its quantity");
        LitemallGoodsProduct p50After = daoProvider.daoFor(LitemallGoodsProduct.class).getEntityById(line50ProductId);
        assertEquals(p50StockBefore, p50After.getNumber(),
                "non-refunded line stock must be unchanged");

        // Aggregate aftersaleStatus: REFUND terminal -> INIT (no in-progress aftersale).
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_INIT, order.getAftersaleStatus());

        // Second item is still eligible for aftersale (independent state machine).
        String aftersale2 = createApplyAndApprove(orderId, line50.getId(), BigDecimal.valueOf(50));
        assertNotEquals(aftersaleId, aftersale2, "second item must get its own aftersale record");
    }

    @Test
    public void testItemLevelAllItemsRefundedTransitionsOrder() {
        // Multi-item order: once every line is refunded, the unshipped order moves to REFUND_CONFIRM(203).
        String orderId = createAndPayOrder(true);
        List<LitemallOrderGoods> goods = orderGoodsOf(orderId);
        LitemallOrderGoods line99 = goods.stream()
                .filter(g -> productId.equals(g.getProductId())).findFirst().orElseThrow();
        LitemallOrderGoods line50 = goods.stream()
                .filter(g -> !productId.equals(g.getProductId())).findFirst().orElseThrow();

        String a1 = createApplyAndApprove(orderId, line99.getId(), BigDecimal.valueOf(99));
        assertEquals(0, refund(a1).getStatus(), "refund item1 failed");

        // After first item refund, order still PAY (one item remains).
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_PAY, order.getOrderStatus());

        String a2 = createApplyAndApprove(orderId, line50.getId(), BigDecimal.valueOf(50));
        assertEquals(0, refund(a2).getStatus(), "refund item2 failed");

        // All items refunded -> unshipped order transitions to terminal REFUND_CONFIRM(203).
        LitemallOrder updated = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_REFUND_CONFIRM, updated.getOrderStatus(),
                "all-items-refunded unshipped order should reach REFUND_CONFIRM(203)");
    }

    private String createApplyAndApprove(String orderId, String orderItemId, BigDecimal amount) {
        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId,
                "orderItemId", orderItemId,
                "type", 0,
                "reason", "质量问题",
                "amount", amount
        ));
        assertEquals(0, applyResult.getStatus(), "item apply failed: " + applyResult);
        Map<String, Object> aftersale = (Map<String, Object>) applyResult.getData();
        String aftersaleId = (String) aftersale.get("id");
        approve(aftersaleId);
        return aftersaleId;
    }

    // ============ Phase 16b: GOODS_REQUIRED return fulfillment (RETURNED substate) ============

    /**
     * Build a CONFIRM-state order so GOODS_REQUIRED (type=2) aftersale is allowed.
     * submit → pay → ship → confirm → returns orderId.
     */
    @SuppressWarnings("unchecked")
    private String createPaidShippedAndConfirmedOrder(boolean multiItem) {
        String orderId = createAndPayOrder(multiItem);

        ApiRequest<Map<String, Object>> shipReq = ApiRequest.build(Map.of(
                "orderId", orderId, "shipSn", "SF-RETURN-001", "shipChannel", "顺丰"));
        IGraphQLExecutionContext shipCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__ship", shipReq);
        ApiResponse<?> shipResult = graphQLEngine.executeRpc(shipCtx);
        assertEquals(0, shipResult.getStatus(), "ship failed: " + shipResult);

        ApiRequest<Map<String, Object>> confirmReq = ApiRequest.build(Map.of("orderId", orderId));
        IGraphQLExecutionContext confirmCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__confirm", confirmReq);
        ApiResponse<?> confirmResult = graphQLEngine.executeRpc(confirmCtx);
        assertEquals(0, confirmResult.getStatus(), "confirm failed: " + confirmResult);
        Map<String, Object> confirmed = (Map<String, Object>) confirmResult.getData();
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_CONFIRM, confirmed.get("orderStatus"));
        return orderId;
    }

    private ApiResponse<?> submitReturnLogistics(String aftersaleId, String channel, String sn) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of(
                "id", aftersaleId, "returnShipChannel", channel, "returnShipSn", sn));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAftersale__submitReturnLogistics", req);
        return graphQLEngine.executeRpc(ctx);
    }

    private ApiResponse<?> confirmReturnReceived(String aftersaleId) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of("id", aftersaleId));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAftersale__confirmReturnReceived", req);
        return graphQLEngine.executeRpc(ctx);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGoodsRequiredFullReturnChain() {
        String orderId = createPaidShippedAndConfirmedOrder(false);
        String orderItemId = null; // whole-order GOODS_REQUIRED

        // apply whole-order GOODS_REQUIRED on CONFIRM(401) order
        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId,
                "type", 2,
                "reason", "质量问题",
                "amount", BigDecimal.valueOf(198)
        ));
        assertEquals(0, applyResult.getStatus(), "apply GOODS_REQUIRED failed: " + applyResult);
        Map<String, Object> aftersale = (Map<String, Object>) applyResult.getData();
        String aftersaleId = (String) aftersale.get("id");

        // approve
        approve(aftersaleId);

        // user submits return logistics: APPROVED -> RETURNED
        ApiResponse<?> submitResult = submitReturnLogistics(aftersaleId, "顺丰", "RETURN-SN-001");
        assertEquals(0, submitResult.getStatus(), "submitReturnLogistics failed: " + submitResult);
        Map<String, Object> submitted = (Map<String, Object>) submitResult.getData();
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_RETURNED, submitted.get("status"));
        assertEquals("RETURN-SN-001", submitted.get("returnShipSn"));
        assertNotNull(submitted.get("returnTime"));

        // While RETURNED is in progress, order-level aftersaleStatus stays REQUEST.
        LitemallOrder orderMid = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_REQUEST, orderMid.getAftersaleStatus(),
                "RETURNED should be treated as in-progress -> order aftersaleStatus=REQUEST");

        // Stock before confirmReturnReceived: should be reduced by the order's quantity (the goods are with the user).
        LitemallGoodsProduct productBefore = daoProvider.daoFor(LitemallGoodsProduct.class).getEntityById(productId);
        int stockBefore = productBefore.getNumber();

        // admin confirms receipt + refund: RETURNED -> REFUND, restock happens here
        ApiResponse<?> confirmResult = confirmReturnReceived(aftersaleId);
        assertEquals(0, confirmResult.getStatus(), "confirmReturnReceived failed: " + confirmResult);

        LitemallAftersale updated = daoProvider.daoFor(LitemallAftersale.class).getEntityById(aftersaleId);
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_REFUND, updated.getStatus());
        assertNotNull(updated.getReceiveConfirmTime(), "receiveConfirmTime must be set on confirm");

        // Restock happened in confirmReturnReceived (+2 units for the whole-order line of quantity 2).
        LitemallGoodsProduct productAfter = daoProvider.daoFor(LitemallGoodsProduct.class).getEntityById(productId);
        assertEquals(stockBefore + 2, productAfter.getNumber(),
                "stock should be restored (+2) on confirmReturnReceived: before=" + stockBefore
                        + " after=" + productAfter.getNumber());

        // Order aggregate aftersaleStatus returns to INIT after REFUND terminal.
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_INIT, order.getAftersaleStatus());
    }

    @Test
    public void testGoodsRequiredRejectRefundPath() {
        String orderId = createPaidShippedAndConfirmedOrder(false);

        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId,
                "type", 2,
                "reason", "质量问题",
                "amount", BigDecimal.valueOf(198)
        ));
        assertEquals(0, applyResult.getStatus());
        String aftersaleId = ((Map<String, Object>) applyResult.getData()).get("id").toString();
        approve(aftersaleId);

        // GOODS_REQUIRED at APPROVED must NOT go through refund() — it must be rejected.
        ApiResponse<?> refundResult = refund(aftersaleId);
        assertTrue(refundResult.getStatus() != 0,
                "GOODS_REQUIRED must not use refund(); should be rejected: " + refundResult);

        // Status remains APPROVED (refund rejected).
        LitemallAftersale unchanged = daoProvider.daoFor(LitemallAftersale.class).getEntityById(aftersaleId);
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_APPROVED, unchanged.getStatus());
    }

    @Test
    public void testSubmitReturnLogisticsGuardRejections() {
        String orderId = createPaidShippedAndConfirmedOrder(false);

        // REQUEST (not yet approved) → submit logistics rejected (status guard)
        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId, "type", 2, "reason", "质量问题",
                "amount", BigDecimal.valueOf(198)));
        assertEquals(0, applyResult.getStatus());
        String aftersaleId = ((Map<String, Object>) applyResult.getData()).get("id").toString();

        ApiResponse<?> r1 = submitReturnLogistics(aftersaleId, "顺丰", "SN-1");
        assertTrue(r1.getStatus() != 0, "submit logistics on REQUEST state should be rejected: " + r1);
    }

    @Test
    public void testSubmitReturnLogisticsTypeGuard() {
        // GOODS_MISS type at APPROVED → submit logistics rejected (type guard: only GOODS_REQUIRED qualifies).
        String orderId = createAndPayOrder(false);
        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId, "type", 0, "reason", "质量问题",
                "amount", BigDecimal.valueOf(50)));
        assertEquals(0, applyResult.getStatus());
        String aftersaleId = ((Map<String, Object>) applyResult.getData()).get("id").toString();
        approve(aftersaleId);

        ApiResponse<?> r2 = submitReturnLogistics(aftersaleId, "顺丰", "SN-2");
        assertTrue(r2.getStatus() != 0,
                "submit logistics on GOODS_MISS type should be rejected: " + r2);
    }

    @Test
    public void testConfirmReturnReceivedGuardRejection() {
        String orderId = createPaidShippedAndConfirmedOrder(false);

        // Apply + approve GOODS_REQUIRED, then try confirm-receive BEFORE submitting logistics (still APPROVED).
        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId, "type", 2, "reason", "质量问题",
                "amount", BigDecimal.valueOf(198)));
        assertEquals(0, applyResult.getStatus());
        String aftersaleId = ((Map<String, Object>) applyResult.getData()).get("id").toString();
        approve(aftersaleId);

        ApiResponse<?> r = confirmReturnReceived(aftersaleId);
        assertTrue(r.getStatus() != 0,
                "confirmReturnReceived on APPROVED (not RETURNED) should be rejected: " + r);
    }

    @Test
    public void testReturnedStateItemMutex() {
        // GOODS_REQUIRED in RETURNED state must keep the item mutex: same item cannot open a second aftersale.
        String orderId = createPaidShippedAndConfirmedOrder(false);
        String orderItemId = orderGoodsOf(orderId).get(0).getId();

        // apply item-level GOODS_REQUIRED
        ApiResponse<?> first = apply(Map.of(
                "orderId", orderId, "orderItemId", orderItemId,
                "type", 2, "reason", "质量问题",
                "amount", BigDecimal.valueOf(99)));
        assertEquals(0, first.getStatus(), "first apply failed: " + first);
        String aftersaleId = ((Map<String, Object>) first.getData()).get("id").toString();
        approve(aftersaleId);
        assertEquals(0, submitReturnLogistics(aftersaleId, "顺丰", "SN-X").getStatus(),
                "submitReturnLogistics should succeed");

        // Second apply on same item while RETURNED in progress → must be rejected.
        ApiResponse<?> second = apply(Map.of(
                "orderId", orderId, "orderItemId", orderItemId,
                "type", 2, "reason", "质量问题",
                "amount", BigDecimal.valueOf(50)));
        assertEquals(-1, second.getStatus(),
                "RETURNED in-progress item mutex should reject second apply: " + second);

        // Order aggregate reflects in-progress (RETURNED counts).
        LitemallOrder order = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_REQUEST, order.getAftersaleStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGoodsNeedlessRefundNoRegression() {
        // GOODS_NEEDLESS (type=1) on CONFIRM order keeps the direct APPROVED -> REFUND path (no RETURNED),
        // and does NOT restock (goods stay with the user).
        String orderId = createPaidShippedAndConfirmedOrder(false);

        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId, "type", 1, "reason", "质量问题",
                "amount", BigDecimal.valueOf(198)));
        assertEquals(0, applyResult.getStatus(), "apply GOODS_NEEDLESS failed: " + applyResult);
        String aftersaleId = ((Map<String, Object>) applyResult.getData()).get("id").toString();
        approve(aftersaleId);

        LitemallGoodsProduct productBefore = daoProvider.daoFor(LitemallGoodsProduct.class).getEntityById(productId);
        int stockBefore = productBefore.getNumber();

        // refund() on GOODS_NEEDLESS/CONFIRM order must work (no RETURNED required).
        ApiResponse<?> refundResult = refund(aftersaleId);
        assertEquals(0, refundResult.getStatus(), "refund GOODS_NEEDLESS failed: " + refundResult);

        LitemallAftersale updated = daoProvider.daoFor(LitemallAftersale.class).getEntityById(aftersaleId);
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_REFUND, updated.getStatus());

        // GOODS_NEEDLESS does NOT restock (goods stay with the user).
        LitemallGoodsProduct productAfter = daoProvider.daoFor(LitemallGoodsProduct.class).getEntityById(productId);
        assertEquals(stockBefore, productAfter.getNumber(),
                "GOODS_NEEDLESS must not restock (goods stay with the user)");
    }

    @Test
    public void testSubmitReturnLogisticsOwnershipGuard() {
        String orderId = createPaidShippedAndConfirmedOrder(false);

        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId, "type", 2, "reason", "质量问题",
                "amount", BigDecimal.valueOf(198)));
        assertEquals(0, applyResult.getStatus());
        String aftersaleId = ((Map<String, Object>) applyResult.getData()).get("id").toString();
        approve(aftersaleId);

        // Switch to a different user — submit logistics must be rejected as not-owner.
        ContextProvider.getOrCreateContext().setUserId("999");
        ApiResponse<?> r = submitReturnLogistics(aftersaleId, "顺丰", "SN-OTHER");
        assertTrue(r.getStatus() != 0, "non-owner submitReturnLogistics should be rejected: " + r);

        // restore user
        ContextProvider.getOrCreateContext().setUserId("1");
    }

    // ============ PromotionUsage refund-rollback (whole-order refund releases, item-level keeps) ============

    private void savePromotionUsage(String orderId) {
        LitemallPromotionActivity a = daoProvider.daoFor(LitemallPromotionActivity.class).newEntity();
        a.setName("满减回滚测试");
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
        u.setMeetAmount(new BigDecimal("198"));
        u.setDiscountAmount(new BigDecimal("20"));
        daoProvider.daoFor(LitemallPromotionUsage.class).saveEntity(u);
    }

    private long usageCountByOrder(String orderId) {
        QueryBean q = new QueryBean();
        q.addFilter(eq(LitemallPromotionUsage.PROP_NAME_orderId, orderId));
        return daoProvider.daoFor(LitemallPromotionUsage.class).findAllByQuery(q).size();
    }

    @Test
    public void testWholeRefundReleasesPromotionUsage() {
        // (c) whole-order aftersale refund releases the PromotionUsage (mirrors coupon/points return).
        String orderId = createAndPayOrder();
        savePromotionUsage(orderId);
        assertEquals(1, usageCountByOrder(orderId), "usage seeded");

        String aftersaleId = createApplyAndApprove(orderId);
        ApiResponse<?> refundResult = refund(aftersaleId);
        assertEquals(0, refundResult.getStatus(), "refund failed: " + refundResult);

        assertEquals(0, usageCountByOrder(orderId), "whole-order refund should release usage");
    }

    @Test
    public void testWholeConfirmReturnReceivedReleasesPromotionUsage() {
        // (d) whole-order return-received refund releases the PromotionUsage (mirrors refund() whole branch).
        String orderId = createPaidShippedAndConfirmedOrder(false);
        savePromotionUsage(orderId);
        assertEquals(1, usageCountByOrder(orderId), "usage seeded");

        // whole-order GOODS_REQUIRED apply + approve + submit return logistics + confirm receipt.
        ApiResponse<?> applyResult = apply(Map.of(
                "orderId", orderId, "type", 2, "reason", "质量问题",
                "amount", BigDecimal.valueOf(198)));
        assertEquals(0, applyResult.getStatus(), "apply GOODS_REQUIRED failed: " + applyResult);
        String aftersaleId = ((Map<String, Object>) applyResult.getData()).get("id").toString();
        approve(aftersaleId);
        assertEquals(0, submitReturnLogistics(aftersaleId, "顺丰", "RETURN-RL-001").getStatus(),
                "submitReturnLogistics failed");

        ApiResponse<?> confirmResult = confirmReturnReceived(aftersaleId);
        assertEquals(0, confirmResult.getStatus(), "confirmReturnReceived failed: " + confirmResult);

        assertEquals(0, usageCountByOrder(orderId), "whole-order return refund should release usage");
    }

    @Test
    public void testItemLevelRefundKeepsPromotionUsage() {
        // (g) partial item-level refund does NOT release usage (promotion is order-level, same boundary
        //     as partial refund not returning coupons/points).
        String orderId = createAndPayOrder(true);
        savePromotionUsage(orderId);
        assertEquals(1, usageCountByOrder(orderId), "usage seeded");

        LitemallOrderGoods line = orderGoodsOf(orderId).get(0);
        String aftersaleId = createApplyAndApprove(orderId, line.getId(),
                line.getPrice().multiply(BigDecimal.valueOf(line.getNumber())));
        assertEquals(0, refund(aftersaleId).getStatus(), "item refund failed");

        assertEquals(1, usageCountByOrder(orderId), "partial item refund must keep usage");
    }
}
