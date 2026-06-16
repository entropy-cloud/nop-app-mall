package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallAftersale;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.pay.PayService;
import app.mall.wx.WxPayServiceImpl;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
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
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        cart.setPicUrl("http://test.com/cart-pic.png");
        cart.setSpecifications("[\"标准\"]");
        daoProvider.daoFor(LitemallCart.class).saveEntity(cart);
    }

    @SuppressWarnings("unchecked")
    private String createAndPayOrder() {
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

    @SuppressWarnings("unchecked")
    @Test
    public void testApplyAndCancel() {
        String orderId = createAndPayOrder();

        ApiRequest<Map<String, Object>> applyReq = ApiRequest.build(Map.of(
                "orderId", orderId,
                "type", 0,
                "reason", "不想要了",
                "amount", BigDecimal.valueOf(198)
        ));
        IGraphQLExecutionContext applyCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAftersale__apply", applyReq);
        ApiResponse<?> applyResult = graphQLEngine.executeRpc(applyCtx);
        assertEquals(0, applyResult.getStatus(),
                "apply failed: " + applyResult);
        Map<String, Object> aftersale = (Map<String, Object>) applyResult.getData();
        assertNotNull(aftersale);
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_REQUEST, aftersale.get("status"));
        String aftersaleId = (String) aftersale.get("id");

        ApiRequest<Map<String, Object>> cancelReq = ApiRequest.build(Map.of(
                "id", aftersaleId
        ));
        IGraphQLExecutionContext cancelCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAftersale__cancel", cancelReq);
        ApiResponse<?> cancelResult = graphQLEngine.executeRpc(cancelCtx);
        assertEquals(0, cancelResult.getStatus(),
                "cancel failed: " + cancelResult);
        Map<String, Object> cancelled = (Map<String, Object>) cancelResult.getData();
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_CANCELLED, cancelled.get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUserListAndDetail() throws Exception {
        String orderId = createAndPayOrder();

        ApiRequest<Map<String, Object>> applyReq = ApiRequest.build(Map.of(
                "orderId", orderId,
                "type", 0,
                "reason", "质量问题",
                "amount", BigDecimal.valueOf(100)
        ));
        IGraphQLExecutionContext applyCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAftersale__apply", applyReq);
        ApiResponse<?> applyResult = graphQLEngine.executeRpc(applyCtx);
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

        ApiRequest<Map<String, Object>> detailReq = ApiRequest.build(Map.of(
                "id", aftersaleId
        ));
        IGraphQLExecutionContext detailCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallAftersale__userDetail", detailReq);
        ApiResponse<?> detailResult = graphQLEngine.executeRpc(detailCtx);
        assertEquals(0, detailResult.getStatus());
        Map<String, Object> detail = (Map<String, Object>) detailResult.getData();
        assertNotNull(detail);
        assertEquals(aftersaleId, detail.get("id"));
    }

    @SuppressWarnings("unchecked")
    private String createApplyAndApprove() {
        String orderId = createAndPayOrder();

        ApiRequest<Map<String, Object>> applyReq = ApiRequest.build(Map.of(
                "orderId", orderId,
                "type", 0,
                "reason", "退款测试",
                "amount", BigDecimal.valueOf(198)
        ));
        IGraphQLExecutionContext applyCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAftersale__apply", applyReq);
        ApiResponse<?> applyResult = graphQLEngine.executeRpc(applyCtx);
        assertEquals(0, applyResult.getStatus(), "apply failed: " + applyResult);
        Map<String, Object> aftersale = (Map<String, Object>) applyResult.getData();
        String aftersaleId = (String) aftersale.get("id");

        ApiRequest<Map<String, Object>> approveReq = ApiRequest.build(Map.of(
                "ids", Set.of(aftersaleId)
        ));
        IGraphQLExecutionContext approveCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAftersale__batchApprove", approveReq);
        ApiResponse<?> approveResult = graphQLEngine.executeRpc(approveCtx);
        assertEquals(0, approveResult.getStatus(), "batchApprove failed: " + approveResult);

        return aftersaleId;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRefundSuccess() {
        String aftersaleId = createApplyAndApprove();

        ApiRequest<Map<String, Object>> refundReq = ApiRequest.build(Map.of("id", aftersaleId));
        IGraphQLExecutionContext refundCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallAftersale__refund", refundReq);
        ApiResponse<?> refundResult = graphQLEngine.executeRpc(refundCtx);
        assertEquals(0, refundResult.getStatus(), "refund failed: " + refundResult);

        LitemallAftersale updated = daoProvider.daoFor(LitemallAftersale.class).getEntityById(aftersaleId);
        assertEquals(_AppMallDaoConstants.AFTERSALE_STATUS_REFUND, updated.getStatus());
    }

    @Test
    public void testRefundFailure() {
        String aftersaleId = createApplyAndApprove();

        WxPayServiceImpl.setForceRefundFailure(true);
        try {
            ApiRequest<Map<String, Object>> refundReq = ApiRequest.build(Map.of("id", aftersaleId));
            IGraphQLExecutionContext refundCtx = graphQLEngine.newRpcContext(
                    GraphQLOperationType.mutation, "LitemallAftersale__refund", refundReq);
            ApiResponse<?> refundResult = graphQLEngine.executeRpc(refundCtx);
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
}
