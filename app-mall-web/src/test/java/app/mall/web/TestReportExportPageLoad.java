package app.mall.web;

import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.web.page.PageProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验 nop-report 导出入口涉及的 page.yaml 可被 PageProvider 加载（YAML 合法性 + 解析无报错）。
 * 对应 plan 2026-06-28-2352-1 Phase 3 Proof「app-mall-web 编译通过 + AMIS YAML 合法性」。
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestReportExportPageLoad extends JunitBaseTestCase {

    @Inject
    PageProvider pageProvider;

    private void assertPageLoads(String path) {
        Map<String, Object> page = pageProvider.getPage(path, "");
        assertNotNull(page, "page loaded null: " + path);
        assertTrue(page.containsKey("body") || page.containsKey("type"),
                "page missing body/type: " + path);
    }

    @Test
    public void testGoodsIoPageLoads() {
        assertPageLoads("/app/mall/pages/mall/goods-ops/goods-io.page.yaml");
    }

    @Test
    public void testStatFunnelPageLoads() {
        assertPageLoads("/app/mall/pages/mall/stat/stat-funnel.page.yaml");
    }

    @Test
    public void testStatProductPageLoads() {
        assertPageLoads("/app/mall/pages/mall/stat/stat-product.page.yaml");
    }

    @Test
    public void testStatOrderPageLoads() {
        assertPageLoads("/app/mall/pages/mall/stat/stat-order.page.yaml");
    }

    @Test
    public void testStatUserPageLoads() {
        assertPageLoads("/app/mall/pages/mall/stat/stat-user.page.yaml");
    }
}
