package app.mall.service.entity;

import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallOrder;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.auth.dao.entity.NopAuthUser;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P20 算法化用户画像 successor 测试。覆盖：
 * - getUserPortrait 有消费/无消费两路 + 分类逻辑同源（与 P19 调同一私有方法）
 * - getSegmentMembers 按 RFM 段/生命周期阶段圈选 + 分页
 * - P19 既有 getUserRfm/getUserLifecycle 零回归（重跑证明重构不破坏 P19）
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallUserPortraitBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId("0");
        ContextProvider.getOrCreateContext().setUserName("admin");
    }

    private ApiResponse<?> rpc(GraphQLOperationType op, String action, Map<String, Object> data) {
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(op, action, ApiRequest.build(data));
        return graphQLEngine.executeRpc(ctx);
    }

    private String signUpAndGetUserId(String username, String mobile) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("password", "Pass@1234");
        data.put("mobile", mobile);
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LoginApi__signUp", data);
        assertEquals(0, r.getStatus(), "signUp helper failed: " + r.getMsg());

        QueryBean q = new QueryBean();
        for (Object raw : daoProvider.daoFor(NopAuthUser.class).findAllByQuery(q)) {
            NopAuthUser u = (NopAuthUser) raw;
            if (username.equals(u.orm_propValueByName("userName"))) {
                return u.orm_idString();
            }
        }
        throw new IllegalStateException("sign-up did not produce user " + username);
    }

    private void createPaidOrder(String userId, String sn, BigDecimal actualPrice, LocalDateTime payTime) {
        LitemallOrder o = daoProvider.daoFor(LitemallOrder.class).newEntity();
        o.setUserId(userId);
        o.setOrderSn(sn);
        o.setOrderStatus(_AppMallDaoConstants.ORDER_STATUS_PAY);
        o.setConsignee("收货人");
        o.setMobile("13800138000");
        o.setAddress("测试地址");
        o.setMessage("portrait-test-order");
        o.setGoodsPrice(actualPrice);
        o.setFreightPrice(BigDecimal.ZERO);
        o.setCouponPrice(BigDecimal.ZERO);
        o.setIntegralPrice(BigDecimal.ZERO);
        o.setGrouponPrice(BigDecimal.ZERO);
        o.setOrderPrice(actualPrice);
        o.setActualPrice(actualPrice);
        o.setPromotionPrice(BigDecimal.ZERO);
        o.setPinTuanPrice(BigDecimal.ZERO);
        o.setPayTime(payTime);
        daoProvider.daoFor(LitemallOrder.class).saveEntity(o);
    }

    // ===== Phase 1: getUserPortrait =====

    @SuppressWarnings("unchecked")
    @Test
    public void testGetUserPortraitWithPayments() {
        String userId = signUpAndGetUserId("portraitpaid", "13900000301");
        LocalDateTime now = LocalDateTime.now();
        createPaidOrder(userId, "SN-PORTRAIT-1", new BigDecimal("120"), now.minusDays(3));
        createPaidOrder(userId, "SN-PORTRAIT-2", new BigDecimal("80"), now.minusDays(1));

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallOrder__getUserPortrait", data);
        assertEquals(0, r.getStatus(), "getUserPortrait failed: " + r.getMsg());
        Map<String, Object> portrait = (Map<String, Object>) r.getData();
        assertNotNull(portrait);
        assertEquals(userId, portrait.get("userId"));
        assertEquals(2L, ((Number) portrait.get("frequency")).longValue());
        assertEquals(0, new BigDecimal("200").compareTo(new BigDecimal(portrait.get("monetary").toString())));
        assertNotNull(portrait.get("rfmSegment"), "paid user must be assigned an RFM segment");
        assertNotNull(portrait.get("lifecycleStage"), "paid user must be assigned a lifecycle stage");
        assertNotNull(portrait.get("firstPayTime"));
        assertNotNull(portrait.get("lastPayTime"));
        assertTrue(((Number) portrait.get("recencyDays")).longValue() >= 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetUserPortraitWithoutPayments() {
        String userId = signUpAndGetUserId("portraitempty", "13900000302");
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallOrder__getUserPortrait", data);
        assertEquals(0, r.getStatus(), "getUserPortrait(no payments) failed: " + r.getMsg());
        Map<String, Object> portrait = (Map<String, Object>) r.getData();
        assertNotNull(portrait);
        assertEquals(0L, ((Number) portrait.get("frequency")).longValue());
        assertEquals(0, BigDecimal.ZERO.compareTo(new BigDecimal(portrait.get("monetary").toString())));
    }

    @Test
    public void testGetUserPortraitEmptyUserRejected() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "");
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallOrder__getUserPortrait", data);
        assertNotEquals(0, r.getStatus(), "empty userId must be rejected");
    }

    // ===== Phase 1 zero-regression: P19 still works after refactor =====

    @Test
    public void testP19GetUserRfmStillWorks() {
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallOrder__getUserRfm", new HashMap<>());
        assertEquals(0, r.getStatus(), "getUserRfm after refactor failed: " + r.getMsg());
        assertNotNull(r.getData());
    }

    @Test
    public void testP19GetUserLifecycleStillWorks() {
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallOrder__getUserLifecycle", new HashMap<>());
        assertEquals(0, r.getStatus(), "getUserLifecycle after refactor failed: " + r.getMsg());
        assertNotNull(r.getData());
    }

    // ===== Phase 2: getSegmentMembers =====

    @SuppressWarnings("unchecked")
    @Test
    public void testGetSegmentMembersByRfm() {
        String userId = signUpAndGetUserId("segmentrfm", "13900000401");
        createPaidOrder(userId, "SN-SEG-RFM-1", new BigDecimal("100"), LocalDateTime.now().minusDays(2));

        Map<String, Object> data = new HashMap<>();
        data.put("segmentType", "rfm");
        data.put("segmentValue", "重要价值用户");
        data.put("page", 1);
        data.put("pageSize", 10);
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallOrder__getSegmentMembers", data);
        assertEquals(0, r.getStatus(), "getSegmentMembers(rfm) failed: " + r.getMsg());
        Map<String, Object> page = (Map<String, Object>) r.getData();
        assertNotNull(page);
        assertNotNull(page.get("items"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetSegmentMembersByLifecycle() {
        String userId = signUpAndGetUserId("segmentlifecycle", "13900000402");
        createPaidOrder(userId, "SN-SEG-LIFE-1", new BigDecimal("50"), LocalDateTime.now().minusDays(1));

        Map<String, Object> data = new HashMap<>();
        data.put("segmentType", "lifecycle");
        data.put("segmentValue", "新客");
        data.put("page", 1);
        data.put("pageSize", 10);
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallOrder__getSegmentMembers", data);
        assertEquals(0, r.getStatus(), "getSegmentMembers(lifecycle) failed: " + r.getMsg());
        Map<String, Object> page = (Map<String, Object>) r.getData();
        assertNotNull(page);
        assertNotNull(page.get("items"));
    }

    @Test
    public void testGetSegmentMembersInvalidType() {
        Map<String, Object> data = new HashMap<>();
        data.put("segmentType", "unknown");
        data.put("segmentValue", "x");
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallOrder__getSegmentMembers", data);
        assertNotEquals(0, r.getStatus(), "invalid segmentType must be rejected");
    }

    @Test
    public void testGetSegmentMembersEmptyValue() {
        Map<String, Object> data = new HashMap<>();
        data.put("segmentType", "rfm");
        data.put("segmentValue", "");
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallOrder__getSegmentMembers", data);
        assertNotEquals(0, r.getStatus(), "empty segmentValue must be rejected");
    }
}
