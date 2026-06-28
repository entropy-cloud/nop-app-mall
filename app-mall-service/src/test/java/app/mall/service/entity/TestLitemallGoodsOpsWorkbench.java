package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.autotest.NopTestProperty;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.context.ContextProvider;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.commons.util.IoHelper;
import io.nop.core.resource.impl.FileResource;
import io.nop.dao.api.IDaoProvider;
import io.nop.excel.model.ExcelCell;
import io.nop.excel.model.ExcelSheet;
import io.nop.excel.model.ExcelTable;
import io.nop.excel.model.ExcelWorkbook;
import io.nop.file.core.IFileStore;
import io.nop.file.core.UploadRequestBean;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import io.nop.ooxml.xlsx.util.ExcelHelper;
import io.nop.orm.IOrmTemplate;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 商品运营工作台（P36）IGraphQLEngine 测试：批量改价/改库存/上下架（含部分失败/校验失败）、
 * 导入（xlsx via ExcelHelper）、导出（CSV）、库存预警（per-SKU safeStock + 全局回退 + 空集 + onlyOnSale）。
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
@NopTestProperty(name = "nop.file.store-dir", value = "./target")
public class TestLitemallGoodsOpsWorkbench extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    IFileStore fileStore;

    @Inject
    IOrmTemplate ormTemplate;

    String goodsId;
    String goodsId2;
    String productId;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("admin");

        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("G-P36-001");
        goods.setName("P36 Goods A");
        goods.setRetailPrice(BigDecimal.valueOf(100));
        goods.setCounterPrice(BigDecimal.valueOf(120));
        goods.setIsOnSale(false);
        goods.setPicUrl("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);
        goodsId = goods.getId();

        LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        product.setGoodsId(goodsId);
        product.setNumber(5);
        product.setPrice(BigDecimal.valueOf(100));
        product.setSpecifications("[\"标准\"]");
        product.setUrl("");
        product.setSafeStock(10);
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);
        productId = product.getId();

        LitemallGoods goodsB = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goodsB.setGoodsSn("G-P36-002");
        goodsB.setName("P36 Goods B");
        goodsB.setRetailPrice(BigDecimal.valueOf(50));
        goodsB.setIsOnSale(true);
        goodsB.setPicUrl("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goodsB);
        goodsId2 = goodsB.getId();

        LitemallGoodsProduct productB = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
        productB.setGoodsId(goodsId2);
        productB.setNumber(20);
        productB.setPrice(BigDecimal.valueOf(50));
        productB.setSpecifications("[\"大号\"]");
        productB.setUrl("");
        daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(productB);
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<?> callMutation(String action, Map<String, Object> data) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallGoods__" + action, req);
        return graphQLEngine.executeRpc(ctx);
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<?> callQuery(String action, Map<String, Object> data) {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGoods__" + action, req);
        return graphQLEngine.executeRpc(ctx);
    }

    private List<Map<String, Object>> resultMaps(ApiResponse<?> r) {
        assertEquals(0, r.getStatus(), "call failed: status=" + r.getStatus() + " msg=" + r.getMsg());
        return (List<Map<String, Object>>) r.getData();
    }

    private int successCount(List<Map<String, Object>> results) {
        return (int) results.stream().filter(m -> Boolean.TRUE.equals(m.get("success"))).count();
    }

    // ===== 批量改价 =====

    @Test
    public void testBatchUpdatePriceSuccessAndPartialFailure() {
        ApiResponse<?> r = callMutation("batchUpdatePrice", Map.of(
                "items", List.of(
                        Map.of("goodsId", goodsId, "retailPrice", "88.5"),
                        Map.of("goodsId", "non-existent", "retailPrice", "10"),
                        Map.of("goodsId", goodsId2, "retailPrice", "45")
                )
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(3, results.size());
        assertEquals(2, successCount(results));

        LitemallGoods reloaded = daoProvider.daoFor(LitemallGoods.class).getEntityById(goodsId);
        assertEquals(0, new BigDecimal("88.5").compareTo(reloaded.getRetailPrice()));
    }

    @Test
    public void testBatchUpdatePriceValidationFailure() {
        ApiResponse<?> r = callMutation("batchUpdatePrice", Map.of(
                "items", List.of(
                        Map.of("goodsId", goodsId, "retailPrice", "abc"),
                        Map.of("goodsId", "", "retailPrice", "10")
                )
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(2, results.size());
        assertEquals(0, successCount(results));
    }

    @Test
    public void testBatchUpdatePriceEmptyRejected() {
        ApiResponse<?> r = callMutation("batchUpdatePrice", Map.of("items", List.of()));
        assertEquals(-1, r.getStatus(), "empty items should be rejected: " + r);
    }

    // ===== 批量改库存 =====

    @Test
    public void testBatchUpdateStockSuccessAndPartialFailure() {
        ApiResponse<?> r = callMutation("batchUpdateStock", Map.of(
                "items", List.of(
                        Map.of("productId", productId, "number", "200"),
                        Map.of("productId", "non-existent", "number", "50")
                )
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(2, results.size());
        assertEquals(1, successCount(results));

        LitemallGoodsProduct reloaded = daoProvider.daoFor(LitemallGoodsProduct.class).getEntityById(productId);
        assertEquals(200, reloaded.getNumber());
    }

    @Test
    public void testBatchUpdateStockNegativeRejected() {
        ApiResponse<?> r = callMutation("batchUpdateStock", Map.of(
                "items", List.of(Map.of("productId", productId, "number", "-5"))
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(1, results.size());
        assertEquals(0, successCount(results));
    }

    // ===== 批量上下架 =====

    @Test
    public void testBatchOnSaleSuccessAndPartialFailure() {
        // goodsId 有 SKU 可上架；goodsId2 有 SKU 可上架；non-existent 失败
        ApiResponse<?> r = callMutation("batchOnSale", Map.of(
                "goodsIds", List.of(goodsId, goodsId2, "non-existent")
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(3, results.size());
        assertEquals(2, successCount(results));

        LitemallGoods reloaded = daoProvider.daoFor(LitemallGoods.class).getEntityById(goodsId);
        assertEquals(true, reloaded.getIsOnSale());
    }

    @Test
    public void testBatchOffSaleSuccess() {
        // goodsId2 当前 onSale，下架成功
        ApiResponse<?> r = callMutation("batchOffSale", Map.of(
                "goodsIds", List.of(goodsId2)
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(1, results.size());
        assertEquals(1, successCount(results));

        LitemallGoods reloaded = daoProvider.daoFor(LitemallGoods.class).getEntityById(goodsId2);
        assertEquals(false, reloaded.getIsOnSale());
    }

    @Test
    public void testBatchOnSaleNoSkuRejected() {
        // 无 SKU 商品上架被拒（单行逻辑守卫）
        LitemallGoods noSku = daoProvider.daoFor(LitemallGoods.class).newEntity();
        noSku.setGoodsSn("G-P36-NOSKU");
        noSku.setName("No SKU");
        noSku.setRetailPrice(BigDecimal.valueOf(10));
        noSku.setIsOnSale(false);
        noSku.setPicUrl("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(noSku);

        ApiResponse<?> r = callMutation("batchOnSale", Map.of(
                "goodsIds", List.of(noSku.getId())
        ));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(1, results.size());
        assertEquals(0, successCount(results));
    }

    // ===== 导出 =====

    @SuppressWarnings("unchecked")
    @Test
    public void testExportGoodsWithData() {
        ApiResponse<?> r = callQuery("exportGoods", new HashMap<>());
        assertEquals(0, r.getStatus(), "export failed: " + r);
        Map<String, Object> data = (Map<String, Object>) r.getData();
        assertTrue((int) data.get("rowCount") >= 2, "should export both goods");
        String csv = (String) data.get("csvContent");
        assertNotNull(csv);
        assertTrue(csv.contains("goodsId,goodsSn,name"));
        assertTrue(csv.contains("G-P36-001"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExportGoodsEmpty() {
        // 用一个不匹配的关键词过滤出空集
        ApiResponse<?> r = callQuery("exportGoods", Map.of("keyword", "ZZZ-NOT-EXIST-ZZZ"));
        assertEquals(0, r.getStatus(), "export failed: " + r);
        Map<String, Object> data = (Map<String, Object>) r.getData();
        assertEquals(0, data.get("rowCount"));
    }

    // ===== nop-report 模板化导出（xlsx/pdf）=====

    @Test
    public void testExportGoodsReportXlsx() {
        File f = renderGoodsReport("xlsx");
        assertTrue(f.length() > 0, "xlsx file should be non-empty");
    }

    @Test
    public void testExportGoodsReportPdf() {
        File f = renderGoodsReport("pdf");
        assertTrue(f.length() > 0, "pdf file should be non-empty");
    }

    @Test
    public void testExportGoodsReportEmptyNotError() {
        ApiResponse<?> r = callQuery("exportGoodsReport", Map.of(
                "renderType", "xlsx", "keyword", "ZZZ-NOT-EXIST-ZZZ"));
        assertEquals(0, r.getStatus(), "empty export should not error: " + r);
    }

    @Test
    public void testExportGoodsReportInvalidRenderType() {
        ApiResponse<?> r = callQuery("exportGoodsReport", Map.of("renderType", "docx"));
        assertEquals(-1, r.getStatus(), "invalid renderType should be rejected: " + r);
    }

    private File renderGoodsReport(String renderType) {
        ApiResponse<?> r = callQuery("exportGoodsReport", Map.of("renderType", renderType));
        assertEquals(0, r.getStatus(), "exportGoodsReport failed: " + r);
        Object data = r.getData();
        File file = WebContentBeanFiles.contentFile(data);
        assertNotNull(file, "rendered file should not be null: " + data);
        assertTrue(file.exists(), "rendered file should exist: " + file);
        return file;
    }

    // ===== 导入 =====

    @Test
    public void testImportGoodsUpdateAndInsert() throws Exception {
        ExcelWorkbook wk = new ExcelWorkbook();
        ExcelSheet sheet = new ExcelSheet();
        sheet.setName("Sheet1");
        wk.addSheet(sheet);
        ExcelTable table = sheet.getTable();
        table.setCell(0, 0, headerCell("goodsSn"));
        table.setCell(0, 1, headerCell("name"));
        table.setCell(0, 2, headerCell("retailPrice"));
        table.setCell(0, 3, headerCell("counterPrice"));
        // 更新既有 G-P36-001
        setRow(table, 1, "G-P36-001", "P36 Goods A 改名", "77", "99");
        // 新增 G-P36-NEW
        setRow(table, 2, "G-P36-NEW", "Imported New Goods", "33", "");
        // 校验失败（缺 name）
        setRow(table, 3, "G-P36-FAIL", "", "10", "");
        String excelUpload = saveExcelAndGetLink(wk, "goods-import.xlsx", "LitemallGoods", "importGoods");

        ApiResponse<?> r = callMutation("importGoods", Map.of("excelUpload", excelUpload));
        List<Map<String, Object>> results = resultMaps(r);
        assertEquals(3, results.size());
        int success = successCount(results);
        assertEquals(2, success, "update + insert should succeed; results=" + results);

        LitemallGoods renamed = findBySn("G-P36-001");
        assertEquals("P36 Goods A 改名", renamed.getName());
        assertEquals(0, new BigDecimal("77").compareTo(renamed.getRetailPrice()));

        LitemallGoods inserted = findBySn("G-P36-NEW");
        assertNotNull(inserted, "newly imported goods should exist");
        assertEquals("Imported New Goods", inserted.getName());
    }

    @Test
    public void testImportGoodsEmptyRejected() {
        ApiResponse<?> r = callMutation("importGoods", Map.of("excelUpload", ""));
        assertEquals(-1, r.getStatus(), "empty import should be rejected: " + r);
    }

    // ===== 库存预警 =====

    @SuppressWarnings("unchecked")
    @Test
    public void testGetStockWarningListWithSafeStock() {
        // goodsId 的 SKU: number=5, safeStock=10 -> number(5) <= safeStock(10) -> 预警
        // goodsId2 的 SKU: number=20, safeStock=null -> 用全局阈值(默认10) -> number(20) > 10 -> 不预警
        ApiResponse<?> r = callQuery("getStockWarningList", new HashMap<>());
        assertEquals(0, r.getStatus(), "stock warning failed: " + r);
        List<Map<String, Object>> data = (List<Map<String, Object>>) r.getData();
        assertFalse(data.isEmpty(), "should have at least one warning");
        boolean hasGoodsA = data.stream().anyMatch(m -> "P36 Goods A".equals(m.get("goodsName")));
        assertTrue(hasGoodsA, "Goods A (number=5, safeStock=10) should be in warning list");
        Map<String, Object> goodsAWarn = data.stream()
                .filter(m -> "P36 Goods A".equals(m.get("goodsName")))
                .findFirst().orElseThrow();
        assertEquals("safeStock", goodsAWarn.get("thresholdSource"));
        assertEquals(10, goodsAWarn.get("threshold"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetStockWarningListOnlyOnSale() {
        // goodsId2 onSale=true, number=20, safeStock=null -> 全局阈值默认10 -> 20>10 不预警
        // 但若把 number 调到 3，应预警（全局回退）
        callMutation("batchUpdateStock", Map.of(
                "items", List.of(Map.of("productId", productIdOf(goodsId2), "number", "3"))
        ));

        ApiResponse<?> r = callQuery("getStockWarningList", Map.of("onlyOnSale", true));
        assertEquals(0, r.getStatus(), "stock warning failed: " + r);
        List<Map<String, Object>> data = (List<Map<String, Object>>) r.getData();
        assertFalse(data.isEmpty());
        boolean hasGoodsB = data.stream().anyMatch(m -> "P36 Goods B".equals(m.get("goodsName")));
        assertTrue(hasGoodsB, "Goods B (number=3, global threshold) should be in warning list");
        Map<String, Object> goodsBWarn = data.stream()
                .filter(m -> "P36 Goods B".equals(m.get("goodsName")))
                .findFirst().orElseThrow();
        assertEquals("global", goodsBWarn.get("thresholdSource"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetStockWarningListEmpty() {
        // 将两个 SKU 的库存都改到阈值之上（经 batchUpdateStock 走正确管道）
        callMutation("batchUpdateStock", Map.of(
                "items", List.of(
                        Map.of("productId", productId, "number", "9999"),
                        Map.of("productId", productIdOf(goodsId2), "number", "9999")
                )
        ));
        ApiResponse<?> r = callQuery("getStockWarningList", new HashMap<>());
        assertEquals(0, r.getStatus(), "stock warning failed: " + r);
        List<Map<String, Object>> data = (List<Map<String, Object>>) r.getData();
        assertTrue(data.isEmpty(), "should be empty when all stock above threshold");
    }

    // ===== 三级阈值优先（per-SKU → per-goods → global） =====

    /**
     * 场景：per-SKU safeStock 为空，per-goods safetyStock 命中。
     * Goods B 的 SKU 没有 safeStock，但 goods 设置了 safetyStock=8；
     * 当前 number=20 > 8 不预警；改 number=5 ≤ 8 应预警且 thresholdSource=safetyStock。
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetStockWarningListWithGoodsLevelSafetyStock() {
        // 给 Goods B 设置 per-goods safetyStock=8
        ormTemplate.runInSession(session -> {
            LitemallGoods goodsB = daoProvider.daoFor(LitemallGoods.class).getEntityById(goodsId2);
            goodsB.setSafetyStock(8);
            daoProvider.daoFor(LitemallGoods.class).updateEntity(goodsB);
            return null;
        });
        // 当前 Goods B SKU number=20，safetyStock=8 → 20 > 8 不预警
        ApiResponse<?> r1 = callQuery("getStockWarningList", Map.of("onlyOnSale", true));
        assertEquals(0, r1.getStatus());
        List<Map<String, Object>> data1 = (List<Map<String, Object>>) r1.getData();
        boolean goodsBWarnBefore = data1.stream().anyMatch(m -> "P36 Goods B".equals(m.get("goodsName")));
        assertFalse(goodsBWarnBefore, "Goods B (number=20, safetyStock=8) should NOT be in warning");

        // 调到 number=5 ≤ safetyStock=8 应预警，且 thresholdSource=safetyStock（per-SKU safeStock 为空）
        callMutation("batchUpdateStock", Map.of(
                "items", List.of(Map.of("productId", productIdOf(goodsId2), "number", "5"))
        ));
        ApiResponse<?> r2 = callQuery("getStockWarningList", Map.of("onlyOnSale", true));
        assertEquals(0, r2.getStatus());
        List<Map<String, Object>> data2 = (List<Map<String, Object>>) r2.getData();
        Map<String, Object> goodsBWarn = data2.stream()
                .filter(m -> "P36 Goods B".equals(m.get("goodsName")))
                .findFirst().orElseThrow();
        assertEquals("safetyStock", goodsBWarn.get("thresholdSource"),
                "should hit per-goods tier when per-SKU safeStock is null");
        assertEquals(8, goodsBWarn.get("threshold"));
        assertEquals(8, goodsBWarn.get("safetyStock"));
    }

    /**
     * 场景：per-SKU safeStock 优先于 per-goods safetyStock。
     * Goods A 的 SKU safeStock=10，goods 同时设置 safetyStock=2；
     * 应优先用 safeStock=10 作为阈值（thresholdSource=safeStock）。
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetStockWarningListSkuTierPriorityOverGoodsTier() {
        ormTemplate.runInSession(session -> {
            LitemallGoods goodsA = daoProvider.daoFor(LitemallGoods.class).getEntityById(goodsId);
            goodsA.setSafetyStock(2);
            daoProvider.daoFor(LitemallGoods.class).updateEntity(goodsA);
            return null;
        });

        ApiResponse<?> r = callQuery("getStockWarningList", new HashMap<>());
        assertEquals(0, r.getStatus());
        List<Map<String, Object>> data = (List<Map<String, Object>>) r.getData();
        Map<String, Object> goodsAWarn = data.stream()
                .filter(m -> "P36 Goods A".equals(m.get("goodsName")))
                .findFirst().orElseThrow();
        // per-SKU safeStock=10 优先于 per-goods safetyStock=2
        assertEquals("safeStock", goodsAWarn.get("thresholdSource"));
        assertEquals(10, goodsAWarn.get("threshold"));
        assertEquals(2, goodsAWarn.get("safetyStock"));
    }

    /**
     * 场景：safetyStock=0 应被视为无效（与 null 等价），回退到 global 阈值。
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetStockWarningListSafetyStockZeroFallsBackToGlobal() {
        ormTemplate.runInSession(session -> {
            LitemallGoods goodsB = daoProvider.daoFor(LitemallGoods.class).getEntityById(goodsId2);
            goodsB.setSafetyStock(0);
            daoProvider.daoFor(LitemallGoods.class).updateEntity(goodsB);
            return null;
        });
        // 调到 number=3 ≤ 全局阈值 10 应预警，thresholdSource=global（safetyStock=0 视为无效）
        callMutation("batchUpdateStock", Map.of(
                "items", List.of(Map.of("productId", productIdOf(goodsId2), "number", "3"))
        ));
        ApiResponse<?> r = callQuery("getStockWarningList", Map.of("onlyOnSale", true));
        assertEquals(0, r.getStatus());
        List<Map<String, Object>> data = (List<Map<String, Object>>) r.getData();
        Map<String, Object> goodsBWarn = data.stream()
                .filter(m -> "P36 Goods B".equals(m.get("goodsName")))
                .findFirst().orElseThrow();
        assertEquals("global", goodsBWarn.get("thresholdSource"),
                "safetyStock=0 should be treated as invalid and fall back to global");
    }

    // ===== 辅助 =====

    private LitemallGoods findBySn(String sn) {
        io.nop.api.core.beans.query.QueryBean q = new io.nop.api.core.beans.query.QueryBean();
        q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallGoods.PROP_NAME_goodsSn, sn));
        return daoProvider.daoFor(LitemallGoods.class).findFirstByQuery(q);
    }

    private String productIdOf(String gid) {
        io.nop.api.core.beans.query.QueryBean q = new io.nop.api.core.beans.query.QueryBean();
        q.addFilter(io.nop.api.core.beans.FilterBeans.eq(LitemallGoodsProduct.PROP_NAME_goodsId, gid));
        return daoProvider.daoFor(LitemallGoodsProduct.class).findFirstByQuery(q).orm_idString();
    }

    private static ExcelCell headerCell(String text) {
        ExcelCell c = new ExcelCell();
        c.setValue(text);
        return c;
    }

    private static void setRow(ExcelTable table, int row, String goodsSn, String name,
                               String retailPrice, String counterPrice) {
        table.setCell(row, 0, headerCell(goodsSn));
        table.setCell(row, 1, headerCell(name));
        table.setCell(row, 2, headerCell(retailPrice));
        table.setCell(row, 3, headerCell(counterPrice));
    }

    private String saveExcelAndGetLink(ExcelWorkbook wk, String fileName, String bizObjName,
                                       String fieldName) throws Exception {
        java.io.File tmp = java.io.File.createTempFile(fileName.replace(".xlsx", ""), ".xlsx");
        tmp.deleteOnExit();
        ExcelHelper.saveExcel(new FileResource(tmp), wk);
        byte[] xlsxBytes = java.nio.file.Files.readAllBytes(tmp.toPath());

        InputStream is = new ByteArrayInputStream(xlsxBytes);
        UploadRequestBean upload = new UploadRequestBean();
        upload.setFileName(fileName);
        upload.setBizObjName(bizObjName);
        upload.setFieldName(fieldName);
        upload.setLength(xlsxBytes.length);
        upload.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        upload.setInputStream(is);
        String fileId = fileStore.saveFile(upload, 16 * 1024 * 1024);
        IoHelper.safeCloseObject(is);
        return fileStore.getFileLink(fileId);
    }
}
