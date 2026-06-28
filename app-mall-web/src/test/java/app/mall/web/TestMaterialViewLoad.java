package app.mall.web;

import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.core.resource.IResource;
import io.nop.core.resource.ResourceHelper;
import io.nop.xlang.xdsl.DslModelParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 校验 P37 素材管理新增/定制的 view.xml 可被 XView schema 加载（解析 x:extends + view-gen + xdef 校验无报错）。
 * 对应 plan Phase 2 「视图 XML 通过 AMIS schema 校验（启动时加载无报错）」。
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestMaterialViewLoad extends JunitBaseTestCase {

    private void assertViewLoads(String path) {
        IResource resource = ResourceHelper.resolveRelativePathResource(path);
        assertNotNull(resource, "view resource not found: " + path);
        Object model = new DslModelParser().parseFromResource(resource);
        assertNotNull(model, "view model parsed null: " + path);
    }

    @Test
    public void testMaterialViewLoads() {
        assertViewLoads("/app/mall/pages/LitemallMaterial/LitemallMaterial.view.xml");
    }

    @Test
    public void testMaterialCategoryViewLoads() {
        assertViewLoads("/app/mall/pages/LitemallMaterialCategory/LitemallMaterialCategory.view.xml");
    }
}
