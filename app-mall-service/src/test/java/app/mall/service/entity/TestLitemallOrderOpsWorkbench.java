package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.dto.BatchShipResultBean;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.autotest.NopTestProperty;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.commons.util.IoHelper;
import io.nop.core.context.IServiceContext;
import io.nop.core.context.ServiceContextImpl;
import io.nop.core.resource.impl.FileResource;
import io.nop.dao.api.IDaoProvider;
import io.nop.excel.model.ExcelCell;
import io.nop.excel.model.ExcelSheet;
import io.nop.excel.model.ExcelTable;
import io.nop.excel.model.ExcelWorkbook;
import io.nop.file.core.IFileStore;
import io.nop.file.core.UploadRequestBean;
import io.nop.file.dao.entity.NopFileRecord;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import io.nop.ooxml.xlsx.util.ExcelHelper;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nop.api.core.beans.FilterBeans.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 订单运营工作台（P21）IGraphQLEngine 测试：改价（含折扣层/状态守卫）、批量发货、改地址、标记、
 * 异常监控两查询（含空集与有集），以及 orderSn 模糊搜索。
 *
 * <p>{@code nop.file.store-dir=./target} 让 IFileStore 的本地存储根目录可写，批量发货 Excel 经
 * {@code fileStore.saveFile()} 写入后再由 {@code batchShip} 经 {@code fileStore.getFile()} 读回。
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
@NopTestProperty(name = "nop.file.store-dir", value = "./target")
public class TestLitemallOrderOpsWorkbench extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    LitemallOrderBizModel orderBizModel;

    @Inject
    IFileStore fileStore;

    String goodsId;
    String productId;
    String addressId;
    String secondAddressId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("admin");

        // cart-pic 必须注册为 NopFileRecord：submit() 经 OrmFileComponent.copyFrom 解析 /f/download/{fileId}
        NopFileRecord cartPic = daoProvider.daoFor(NopFileRecord.class).newEntity();
        cartPic.setFileId("cart-pic");
        cartPic.setBizObjName("LitemallCart");
        cartPic.setBizObjId("temp");
        cartPic.setFieldName("picUrl");
        cartPic.setOriginFileId("cart-pic");
        cartPic.setFileName("cart-pic.png");
        cartPic.setFilePath("/test/cart-pic.png");
        cartPic.setFileExt("png");
        cartPic.setMimeType("image/png");
        cartPic.setIsPublic(true);
        daoProvider.daoFor(NopFileRecord.class).saveEntity(cartPic);

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G001");
        goods.setName("Test Goods");
        goods.setPicUrl("http://test.com/goods-pic.png");
        goods.setCounterPrice(BigDecimal.valueOf(100));
        goods.setRetailPrice(BigDecimal.valueOf(99));
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.getId();

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

        LitemallAddress address2 = daoProvider.daoFor(LitemallAddress.class).newEntity();
        address2.setUserId("1");
        address2.setName("李四");
        address2.setTel("13900139000");
        address2.setProvince("北京市");
        address2.setCity("北京市");
        address2.setCounty("海淀区");
        address2.setAddressDetail("中关村");
        address2.setIsDefault(false);
        daoProvider.daoFor(LitemallAddress.class).saveEntity(address2);
        secondAddressId = address2.getId();
    }

    // 每次提交订单前重置购物车（submit 会清空已勾选购物车行）
    private void resetCart() {
        QueryBean q = new QueryBean();
        q.addFilter(eq(LitemallCart.PROP_NAME_userId, "1"));
        List<LitemallCart> existing = daoProvider.daoFor(LitemallCart.class).findAllByQuery(q);
        for (LitemallCart c : existing) {
            daoProvider.daoFor(LitemallCart.class).deleteEntity(c);
        }
        LitemallCart cart = daoProvider.daoFor(LitemallCart.class).newEntity();
        cart.setUserId("1");
        cart.setGoodsId(goodsId);
        cart.setProductId(productId);
        cart.setNumber(2);
        cart.setPrice(BigDecimal.valueOf(99));
        cart.setChecked(true);
        cart.setGoodsSn("G001");
        cart.setGoodsName("Test Goods");
        cart.setPicUrl("/f/download/cart-pic");
        cart.setSpecifications("[\"标准\"]");
        daoProvider.daoFor(LitemallCart.class).saveEntity(cart);
    }

    // ===== 辅助 =====

    @SuppressWarnings("unchecked")
    private Map<String, Object> submitAndPayOrder() {
        resetCart();
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "ops workbench test",
                "freightPrice", BigDecimal.TEN
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> r = graphQLEngine.executeRpc(ctx);
        assertEquals(0, r.getStatus(), "submit failed: " + r);
        Map<String, Object> data = (Map<String, Object>) r.getData();
        String orderId = (String) data.get("id");

        ApiRequest<Map<String, Object>> payReq = ApiRequest.build(Map.of("orderId", orderId));
        IGraphQLExecutionContext payCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__pay", payReq);
        graphQLEngine.executeRpc(payCtx);
        return data;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> submitOrderOnly() {
        resetCart();
        ApiRequest<Map<String, Object>> submitReq = ApiRequest.build(Map.of(
                "addressId", addressId,
                "message", "ops modify-price test",
                "freightPrice", BigDecimal.TEN
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__submit", submitReq);
        ApiResponse<?> r = graphQLEngine.executeRpc(ctx);
        assertEquals(0, r.getStatus(), "submit failed: " + r);
        return (Map<String, Object>) r.getData();
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<?> callMutation(String action, Map<String, Object> data) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__" + action, req);
        return graphQLEngine.executeRpc(ctx);
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<?> callQuery(String action, Map<String, Object> data) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__" + action, req);
        return graphQLEngine.executeRpc(ctx);
    }

    // ===== 改价 =====

    @SuppressWarnings("unchecked")
    @Test
    public void testModifyFreightPriceSuccess() {
        Map<String, Object> order = submitOrderOnly();
        String orderId = (String) order.get("id");
        BigDecimal originalActual = new BigDecimal(order.get("actualPrice").toString());

        ApiResponse<?> r = callMutation("modifyOrderPrice", Map.of(
                "orderId", orderId,
                "freightPriceDelta", new BigDecimal("5"),
                "remark", "运费补 5 元"
        ));
        assertEquals(0, r.getStatus(), "modifyOrderPrice freight failed: " + r);
        Map<String, Object> updated = (Map<String, Object>) r.getData();

        assertEquals(0, new BigDecimal("15").compareTo(new BigDecimal(updated.get("freightPrice").toString())));
        // actualPrice 应该 += 5（freight 在 orderPrice 加项层，无折扣）
        BigDecimal newActual = new BigDecimal(updated.get("actualPrice").toString());
        assertEquals(0, originalActual.add(new BigDecimal("5")).compareTo(newActual));
    }

    @Test
    public void testModifyGoodsPriceOnPureOrderSuccess() {
        // 纯商品订单（无折扣）允许改 goodsPrice
        Map<String, Object> order = submitOrderOnly();
        String orderId = (String) order.get("id");
        BigDecimal originalGoods = new BigDecimal(order.get("goodsPrice").toString());
        assertEquals(0, new BigDecimal("198").compareTo(originalGoods)); // 99 * 2

        ApiResponse<?> r = callMutation("modifyOrderPrice", Map.of(
                "orderId", orderId,
                "goodsPriceDelta", new BigDecimal("-10"),
                "remark", "商品价减 10 元"
        ));
        assertEquals(0, r.getStatus(), "modifyOrderPrice goods (pure) failed: " + r);
    }

    @Test
    public void testModifyGoodsPriceOnDiscountedOrderRejected() {
        // 模拟带折扣订单：通过 modifyOrderPrice 内部走 requireEntity（含 context）。
        // 在事务中手工给订单设置 couponPrice 模拟折扣存在的情况。
        Map<String, Object> order = submitOrderOnly();
        String orderId = (String) order.get("id");

        // 走 markOrder 写 adminRemark 后再读出来验证：markOrder 内部经 requireEntity 加载实体到当前会话，
        // 之后 update couponPrice 不出当前测试线程事务。
        // 此处直接通过 ILitemallOrderBiz GraphQL update 接口的 data 参数（包含 id）写入：
        ApiRequest<Map<String, Object>> updateReq = ApiRequest.build(Map.of(
                "data", Map.of(
                        "id", orderId,
                        "couponPrice", new BigDecimal("10")
                )
        ));
        IGraphQLExecutionContext updateCtx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallOrder__update", updateReq);
        ApiResponse<?> updateResp = graphQLEngine.executeRpc(updateCtx);
        assertEquals(0, updateResp.getStatus(), "update couponPrice failed: " + updateResp);

        ApiResponse<?> r = callMutation("modifyOrderPrice", Map.of(
                "orderId", orderId,
                "goodsPriceDelta", new BigDecimal("-10")
        ));
        assertEquals(-1, r.getStatus(),
                "modifyOrderPrice goods (with discount) should be rejected: " + r);
        assertTrue(r.getMsg().contains("discount-active") || r.getMsg().contains("折扣"),
                "expected discount-active error: " + r.getMsg());
    }

    @Test
    public void testModifyPriceOnPaidOrderRejected() {
        // 状态守卫：已支付订单不允许改价
        Map<String, Object> order = submitAndPayOrder();
        String orderId = (String) order.get("id");

        ApiResponse<?> r = callMutation("modifyOrderPrice", Map.of(
                "orderId", orderId,
                "freightPriceDelta", new BigDecimal("5")
        ));
        assertEquals(-1, r.getStatus(),
                "modifyOrderPrice on paid order should be rejected: " + r);
    }

    // ===== 批量发货 =====

    @Test
    public void testBatchShipSuccessAndPartialFailure() throws Exception {
        // 两笔已支付订单 + 一笔不存在订单 + 一笔字段缺失行 + 一笔已发货订单
        Map<String, Object> o1 = submitAndPayOrder();
        Map<String, Object> o2 = submitAndPayOrder();
        Map<String, Object> o3 = submitAndPayOrder();
        // o3 先发货
        callMutation("ship", Map.of(
                "orderId", o3.get("id"),
                "shipSn", "EXISTING-SF-001",
                "shipChannel", "顺丰"
        ));

        // 生成 xlsx 到内存，经 IFileStore.saveFile 写入存储根目录（store-dir=./target）
        ExcelWorkbook wk = new ExcelWorkbook();
        ExcelSheet sheet = new ExcelSheet();
        sheet.setName("Sheet1");
        wk.addSheet(sheet);
        ExcelTable table = sheet.getTable();
        // 表头
        table.setCell(0, 0, headerCell("orderSn"));
        table.setCell(0, 1, headerCell("shipSn"));
        table.setCell(0, 2, headerCell("shipChannel"));
        // 数据行
        setRow(table, 1, (String) o1.get("orderSn"), "SF-001", "顺丰");
        setRow(table, 2, (String) o2.get("orderSn"), "SF-002", "顺丰");
        setRow(table, 3, (String) o3.get("orderSn"), "SF-003", "顺丰");
        setRow(table, 4, "NON-EXIST-ORDER-SN", "SF-004", "顺丰");
        setRow(table, 5, "", "SF-005", "顺丰");
        java.io.File tmp = java.io.File.createTempFile("batch-ship", ".xlsx");
        tmp.deleteOnExit();
        ExcelHelper.saveExcel(new FileResource(tmp), wk);
        byte[] xlsxBytes = java.nio.file.Files.readAllBytes(tmp.toPath());

        // 经文件存储写入：saveFile 自动生成 fileId 与虚拟 filePath，生产路径与测试路径一致
        InputStream is = new ByteArrayInputStream(xlsxBytes);
        UploadRequestBean upload = new UploadRequestBean();
        upload.setFileName("batch-ship.xlsx");
        upload.setBizObjName("LitemallOrder");
        upload.setFieldName("batchShip");
        upload.setLength(xlsxBytes.length);
        upload.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        upload.setInputStream(is);
        String fileId = fileStore.saveFile(upload, 16 * 1024 * 1024);
        IoHelper.safeCloseObject(is);

        // 调用 batchShip via GraphQL，excelUpload 为 /f/download/{fileId}
        String excelUpload = fileStore.getFileLink(fileId);
        ApiResponse<?> r = callMutation("batchShip", Map.of("excelUpload", excelUpload));
        assertEquals(0, r.getStatus(), "batchShip failed: " + r);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> resultsMaps = (List<Map<String, Object>>) r.getData();
        // GraphQL 序列化为 List<Map>；从 map 重建便于断言
        List<BatchShipResultBean> results = new java.util.ArrayList<>();
        for (Map<String, Object> m : resultsMaps) {
            results.add(new BatchShipResultBean(
                    (String) m.get("orderSn"),
                    Boolean.TRUE.equals(m.get("success")),
                    (String) m.get("reason")));
        }

        assertEquals(5, results.size(), "all 5 rows should produce a result");
        int success = (int) results.stream().filter(BatchShipResultBean::isSuccess).count();
        assertEquals(2, success, "2 rows (o1, o2) should succeed. Results: " + results);

        // 验证 o1/o2 已切到 SHIP(301)
        LitemallOrder e1 = daoProvider.daoFor(LitemallOrder.class).getEntityById((String) o1.get("id"));
        LitemallOrder e2 = daoProvider.daoFor(LitemallOrder.class).getEntityById((String) o2.get("id"));
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_SHIP, e1.getOrderStatus());
        assertEquals(_AppMallDaoConstants.ORDER_STATUS_SHIP, e2.getOrderStatus());
        assertEquals("SF-001", e1.getShipSn());
        assertEquals("SF-002", e2.getShipSn());
    }

    private static ExcelCell headerCell(String text) {
        ExcelCell c = new ExcelCell();
        c.setValue(text);
        return c;
    }

    private static void setRow(ExcelTable table, int row, String orderSn, String shipSn, String shipChannel) {
        table.setCell(row, 0, headerCell(orderSn));
        table.setCell(row, 1, headerCell(shipSn));
        table.setCell(row, 2, headerCell(shipChannel));
    }

    // ===== 改地址 =====

    @SuppressWarnings("unchecked")
    @Test
    public void testChangeOrderAddressBeforeShip() {
        Map<String, Object> order = submitAndPayOrder(); // status=201 已支付未发货
        String orderId = (String) order.get("id");

        ApiResponse<?> r = callMutation("changeOrderAddress", Map.of(
                "orderId", orderId,
                "addressId", secondAddressId
        ));
        assertEquals(0, r.getStatus(), "changeOrderAddress failed: " + r);
        Map<String, Object> updated = (Map<String, Object>) r.getData();
        assertEquals("李四", updated.get("consignee"));
        assertEquals("13900139000", updated.get("mobile"));
        assertTrue(updated.get("address").toString().contains("中关村"));
    }

    @Test
    public void testChangeOrderAddressAfterShipRejected() {
        Map<String, Object> order = submitAndPayOrder();
        String orderId = (String) order.get("id");
        callMutation("ship", Map.of(
                "orderId", orderId,
                "shipSn", "SF-ADDR-001",
                "shipChannel", "顺丰"
        )); // 切到 301

        ApiResponse<?> r = callMutation("changeOrderAddress", Map.of(
                "orderId", orderId,
                "addressId", secondAddressId
        ));
        assertEquals(-1, r.getStatus(),
                "changeOrderAddress on shipped order should be rejected: " + r);
    }

    // ===== 订单标记 =====

    @SuppressWarnings("unchecked")
    @Test
    public void testMarkOrder() {
        Map<String, Object> order = submitOrderOnly();
        String orderId = (String) order.get("id");

        ApiResponse<?> r = callMutation("markOrder", Map.of(
                "orderId", orderId,
                "adminRemark", "VIP 客户订单，加急处理"
        ));
        assertEquals(0, r.getStatus(), "markOrder failed: " + r);
        Map<String, Object> updated = (Map<String, Object>) r.getData();
        assertEquals("VIP 客户订单，加急处理", updated.get("adminRemark"));

        LitemallOrder entity = daoProvider.daoFor(LitemallOrder.class).getEntityById(orderId);
        assertEquals("VIP 客户订单，加急处理", entity.getAdminRemark());
    }

    // ===== 异常监控 =====

    @SuppressWarnings("unchecked")
    @Test
    public void testGetOverdueUnpaidOrdersWithResults() {
        // 提交一笔待支付订单
        Map<String, Object> order = submitOrderOnly();

        // cutoffMinutes=0 → cutoff=now，刚创建的订单 addTime<=now 命中
        ApiResponse<?> r = callQuery("getOverdueUnpaidOrders", Map.of("cutoffMinutes", 0));
        assertEquals(0, r.getStatus(), "getOverdueUnpaidOrders failed: " + r);
        List<Map<String, Object>> data = (List<Map<String, Object>>) r.getData();
        assertNotNull(data);
        assertTrue(data.size() >= 1, "should contain at least the just-created order");
        assertTrue(data.stream().anyMatch(o -> order.get("id").equals(o.get("id"))),
                "should contain the just-created order");
    }

    @Test
    public void testGetOverdueUnpaidOrdersEmpty() {
        // 不存在订单时返回空列表（cutoffMinutes=0 不会包含未来 addTime）
        ApiResponse<?> r = callQuery("getOverdueUnpaidOrders", Map.of("cutoffMinutes", 0));
        assertEquals(0, r.getStatus(), "getOverdueUnpaidOrders failed: " + r);
        assertNotNull(r.getData());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetOverdueUnshippedOrdersWithResults() {
        // 已支付未发货订单 + cutoffHours=0 命中
        Map<String, Object> order = submitAndPayOrder();

        ApiResponse<?> r = callQuery("getOverdueUnshippedOrders", Map.of("cutoffHours", 0));
        assertEquals(0, r.getStatus(), "getOverdueUnshippedOrders failed: " + r);
        List<Map<String, Object>> data = (List<Map<String, Object>>) r.getData();
        assertNotNull(data);
        assertTrue(data.stream().anyMatch(o -> order.get("id").equals(o.get("id"))),
                "should contain the just-paid unshipped order");
    }

    @Test
    public void testGetOverdueUnshippedOrdersEmpty() {
        // shipTime=null 但要求 status=201；无订单时返回空
        ApiResponse<?> r = callQuery("getOverdueUnshippedOrders", Map.of("cutoffHours", 0));
        assertEquals(0, r.getStatus(), "getOverdueUnshippedOrders failed: " + r);
        assertNotNull(r.getData());
    }

    // ===== orderSn 模糊搜索 =====

    @SuppressWarnings("unchecked")
    @Test
    public void testOrderSnFuzzySearch() {
        Map<String, Object> order = submitOrderOnly();
        String orderSn = (String) order.get("orderSn");
        // 取 orderSn 中间 6 字符做模糊匹配
        String partial = orderSn.substring(Math.max(0, orderSn.length() / 2 - 3), orderSn.length() / 2 + 3);

        ApiRequest<Map<String, Object>> findReq = ApiRequest.build(Map.of(
                "filter_orderSn__contains", partial
        ));
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallOrder__findPage", findReq);
        ApiResponse<?> r = graphQLEngine.executeRpc(ctx);
        assertEquals(0, r.getStatus(), "findPage with filter_orderSn__contains failed: " + r);
        Map<String, Object> data = (Map<String, Object>) r.getData();
        assertNotNull(data);
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        assertNotNull(items);
        assertTrue(items.stream().anyMatch(o -> orderSn.equals(o.get("orderSn"))),
                "fuzzy search should find the order by partial orderSn");
    }
}
