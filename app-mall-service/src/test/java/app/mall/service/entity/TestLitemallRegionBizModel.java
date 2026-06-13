package app.mall.service.entity;

import app.mall.dao.entity.LitemallRegion;
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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallRegionBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    String provinceId;
    String cityId;
    String district1Id;
    String district2Id;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("1");
        ContextProvider.getOrCreateContext().setUserName("test");

        LitemallRegion province = daoProvider.daoFor(LitemallRegion.class).newEntity();
        province.setName("广东省");
        province.setType(1);
        province.setCode(440000);
        province.setPid("0");
        daoProvider.daoFor(LitemallRegion.class).saveEntity(province);
        provinceId = province.getId();

        LitemallRegion city = daoProvider.daoFor(LitemallRegion.class).newEntity();
        city.setName("深圳市");
        city.setType(2);
        city.setCode(440300);
        city.setPid(provinceId);
        daoProvider.daoFor(LitemallRegion.class).saveEntity(city);
        cityId = city.getId();

        LitemallRegion district1 = daoProvider.daoFor(LitemallRegion.class).newEntity();
        district1.setName("南山区");
        district1.setType(3);
        district1.setCode(440305);
        district1.setPid(cityId);
        daoProvider.daoFor(LitemallRegion.class).saveEntity(district1);
        district1Id = district1.getId();

        LitemallRegion district2 = daoProvider.daoFor(LitemallRegion.class).newEntity();
        district2.setName("福田区");
        district2.setType(3);
        district2.setCode(440304);
        district2.setPid(cityId);
        daoProvider.daoFor(LitemallRegion.class).saveEntity(district2);
        district2Id = district2.getId();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetRegionTree() {
        ApiRequest<Map<String, Object>> req = ApiRequest.build(Map.of());
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.query, "LitemallRegion__getRegionTree", req);
        ApiResponse<?> result = graphQLEngine.executeRpc(ctx);
        assertEquals(0, result.getStatus(), "getRegionTree failed: " + result);

        List<Map<String, Object>> tree = (List<Map<String, Object>>) result.getData();
        assertNotNull(tree);
        assertFalse(tree.isEmpty());

        Map<String, Object> provinceNode = tree.stream()
                .filter(n -> provinceId.equals(n.get("id")))
                .findFirst()
                .orElse(null);
        assertNotNull(provinceNode);
        assertEquals("广东省", provinceNode.get("name"));
        assertEquals(1, provinceNode.get("type"));

        List<Map<String, Object>> cities = (List<Map<String, Object>>) provinceNode.get("children");
        assertNotNull(cities);
        assertEquals(1, cities.size());

        Map<String, Object> cityNode = cities.get(0);
        assertEquals("深圳市", cityNode.get("name"));

        List<Map<String, Object>> districts = (List<Map<String, Object>>) cityNode.get("children");
        assertNotNull(districts);
        assertEquals(2, districts.size());
    }
}
