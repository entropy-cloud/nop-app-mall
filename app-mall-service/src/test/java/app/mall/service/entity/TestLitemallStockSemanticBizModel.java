package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallSystem;
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

/**
 * P38 库存语义化 getStockSemantic (@BizQuery) 通过 IGraphQLEngine 验证：
 * 三档判定、阈值边界归属、自定义文案生效、多 SKU 求和聚合、全 SKU 缺货。
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallStockSemanticBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("0");
        ContextProvider.getOrCreateContext().setUserName("test");
    }

    private void setConfig(String key, String value) {
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName", key);
        cfg.orm_propValueByName("keyValue", value);
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
    }

    private String createGoodsWithStock(List<Integer> stockNumbers) {
        LitemallGoods goods = daoProvider.daoFor(LitemallGoods.class).newEntity();
        goods.setGoodsSn("STOCK-" + System.nanoTime());
        goods.setName("Stock Test Goods");
        goods.setRetailPrice(BigDecimal.TEN);
        goods.setIsOnSale(true);
        goods.setPicUrl("");
        goods.setShareUrl("");
        goods.setGallery("");
        daoProvider.daoFor(LitemallGoods.class).saveEntity(goods);

        for (Integer stock : stockNumbers) {
            LitemallGoodsProduct product = daoProvider.daoFor(LitemallGoodsProduct.class).newEntity();
            product.setGoodsId(goods.getId());
            product.setSpecifications("[\"标准\"]");
            product.setPrice(BigDecimal.TEN);
            product.setNumber(stock);
            product.setUrl("");
            daoProvider.daoFor(LitemallGoodsProduct.class).saveEntity(product);
        }
        return goods.orm_idString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getStockSemantic(String goodsId) {
        Map<String, Object> data = new HashMap<>();
        data.put("goodsId", goodsId);
        ApiRequest<Map<String, Object>> request = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGoods__getStockSemantic", request);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getStockSemantic failed: " + result);
        return (Map<String, Object>) result.getData();
    }

    @Test
    public void testSufficientLevel() {
        String id = createGoodsWithStock(List.of(100));
        Map<String, Object> sem = getStockSemantic(id);
        assertEquals("sufficient", sem.get("level"));
        assertEquals(100, ((Number) sem.get("stockNumber")).intValue());
        assertEquals(LitemallGoodsBizModel.DEFAULT_STOCK_LABEL_SUFFICIENT, sem.get("label"));
        assertEquals(LitemallGoodsBizModel.DEFAULT_STOCK_COLOR_SUFFICIENT, sem.get("color"));
    }

    @Test
    public void testTightLevel() {
        // 5 <= default threshold 10 → tight，紧张文案含 {n} 占位替换
        String id = createGoodsWithStock(List.of(5));
        Map<String, Object> sem = getStockSemantic(id);
        assertEquals("tight", sem.get("level"));
        assertEquals("仅剩 5", sem.get("label"));
        assertEquals(LitemallGoodsBizModel.DEFAULT_STOCK_COLOR_TIGHT, sem.get("color"));
    }

    @Test
    public void testOutLevel() {
        String id = createGoodsWithStock(List.of(0));
        Map<String, Object> sem = getStockSemantic(id);
        assertEquals("out", sem.get("level"));
        assertEquals(LitemallGoodsBizModel.DEFAULT_STOCK_LABEL_OUT, sem.get("label"));
        assertEquals(0, ((Number) sem.get("stockNumber")).intValue());
    }

    @Test
    public void testThresholdBoundary() {
        // = 阈值 (默认 10) 归属 tight
        String idAt = createGoodsWithStock(List.of(10));
        assertEquals("tight", getStockSemantic(idAt).get("level"));
        // 阈值 + 1 (11) 归属 sufficient
        String idAbove = createGoodsWithStock(List.of(11));
        assertEquals("sufficient", getStockSemantic(idAbove).get("level"));
    }

    @Test
    public void testCustomThresholdAndText() {
        setConfig(LitemallGoodsBizModel.CONFIG_STOCK_THRESHOLD_TIGHT, "20");
        setConfig(LitemallGoodsBizModel.CONFIG_STOCK_LABEL_SUFFICIENT, "现货充足");
        setConfig(LitemallGoodsBizModel.CONFIG_STOCK_LABEL_TIGHT, "抓紧，剩 {n} 件");

        // 15 <= 自定义阈值 20 → tight（默认阈值下 15 本应是 sufficient，验证配置生效）
        String id = createGoodsWithStock(List.of(15));
        Map<String, Object> sem = getStockSemantic(id);
        assertEquals("tight", sem.get("level"));
        assertEquals("抓紧，剩 15 件", sem.get("label"));

        // 充足档位自定义文案生效
        String idSufficient = createGoodsWithStock(List.of(50));
        Map<String, Object> sem2 = getStockSemantic(idSufficient);
        assertEquals("sufficient", sem2.get("level"));
        assertEquals("现货充足", sem2.get("label"));
    }

    @Test
    public void testMultiSkuAggregationSum() {
        // 3 + 4 = 7 → tight（默认阈值 10）
        String id = createGoodsWithStock(List.of(3, 4));
        Map<String, Object> sem = getStockSemantic(id);
        assertEquals("tight", sem.get("level"));
        assertEquals(7, ((Number) sem.get("stockNumber")).intValue());
        assertEquals("仅剩 7", sem.get("label"));
    }

    @Test
    public void testAllSkuOutAggregatesToOut() {
        // 0 + 0 = 0 → out；部分缺货 + 总量充足仍为 sufficient
        String allOut = createGoodsWithStock(List.of(0, 0));
        Map<String, Object> sem = getStockSemantic(allOut);
        assertEquals("out", sem.get("level"));
        assertEquals(0, ((Number) sem.get("stockNumber")).intValue());

        // 部分缺货但总量充足 → sufficient（商品级求和，单 SKU 缺货由下拉单独标识）
        String mixed = createGoodsWithStock(List.of(0, 30));
        Map<String, Object> sem2 = getStockSemantic(mixed);
        assertEquals("sufficient", sem2.get("level"));
        assertEquals(30, ((Number) sem2.get("stockNumber")).intValue());
    }

    @Test
    public void testGoodsNotFound() {
        Map<String, Object> data = new HashMap<>();
        data.put("goodsId", "99999999");
        ApiRequest<Map<String, Object>> request = ApiRequest.build(data);
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallGoods__getStockSemantic", request);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertNotEquals(0, result.getStatus(), "non-existent goods must error");
    }
}
