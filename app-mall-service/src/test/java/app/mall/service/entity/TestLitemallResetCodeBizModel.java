package app.mall.service.entity;

import app.mall.dao.entity.LitemallResetCode;
import app.mall.dao.entity.LitemallSystem;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import io.nop.orm.IOrmTemplate;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;

import static io.nop.api.core.beans.FilterBeans.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证码定期清理（successor of deferred「验证码过期记录的定期清理」）IGraphQLEngine 测试：
 * 超过保留期逻辑删除、保留期内保留、已删除不重复处理、config 缺省/覆盖、单轮 limit 上限。
 */
@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallResetCodeBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    @Inject
    IOrmTemplate ormTemplate;

    private void setConfig(String key, String value) {
        LitemallSystem cfg = daoProvider.daoFor(LitemallSystem.class).newEntity();
        cfg.orm_propValueByName("keyName", key);
        cfg.orm_propValueByName("keyValue", value);
        daoProvider.daoFor(LitemallSystem.class).saveEntity(cfg);
    }

    private int cleanupExpiredResetCodes() {
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(
                GraphQLOperationType.mutation, "LitemallResetCode__cleanupExpiredResetCodes",
                io.nop.api.core.beans.ApiRequest.build(new HashMap<>()));
        ApiResponse<?> r = graphQLEngine.executeRpc(ctx);
        assertEquals(0, r.getStatus(), "cleanupExpiredResetCodes failed: " + r);
        return ((Number) r.getData()).intValue();
    }

    private String seedResetCode(String mobile, LocalDateTime addTime, boolean deleted) {
        // addTime is the entity's createTimeProp, auto-set to now() on insert. Insert, flush so the
        // entity becomes MANAGED, then backdate via update (createTime is auto-set only on insert, so
        // an update preserves the backdated value). runInSession keeps the session open across calls.
        String[] idHolder = new String[1];
        ormTemplate.runInSession(session -> {
            LitemallResetCode code = daoProvider.daoFor(LitemallResetCode.class).newEntity();
            code.orm_propValueByName(LitemallResetCode.PROP_NAME_mobile, mobile);
            code.orm_propValueByName(LitemallResetCode.PROP_NAME_code, "123456");
            code.orm_propValueByName(LitemallResetCode.PROP_NAME_deleted, deleted);
            daoProvider.daoFor(LitemallResetCode.class).saveEntity(code);
            session.flush();
            code.orm_propValueByName(LitemallResetCode.PROP_NAME_addTime, addTime);
            daoProvider.daoFor(LitemallResetCode.class).updateEntity(code);
            session.flush();
            idHolder[0] = code.orm_idString();
            return null;
        });
        return idHolder[0];
    }

    private boolean isDeleted(String id) {
        return daoProvider.daoFor(LitemallResetCode.class).getEntityById(id).getDeleted();
    }

    private long countActiveByMobile(String mobile) {
        QueryBean q = new QueryBean();
        q.addFilter(eq(LitemallResetCode.PROP_NAME_mobile, mobile));
        return daoProvider.daoFor(LitemallResetCode.class).findAllByQuery(q).size();
    }

    @Test
    public void testCleanupLogicallyDeletesExpired() {
        String expiredId = seedResetCode("13800000001", LocalDateTime.now().minusDays(8), false);

        int affected = cleanupExpiredResetCodes();

        assertEquals(1, affected, "one expired code should be logically deleted");
        assertTrue(isDeleted(expiredId), "expired record must be marked deleted=true");
    }

    @Test
    public void testCleanupKeepsRecordsWithinRetention() {
        // Default retention is 7 days; a 6-day-old record is within the window.
        String recentId = seedResetCode("13800000002", LocalDateTime.now().minusDays(6), false);

        int affected = cleanupExpiredResetCodes();

        assertEquals(0, affected, "within-retention record must not be touched");
        assertFalse(isDeleted(recentId), "recent record must stay deleted=false");
    }

    @Test
    public void testCleanupSkipsAlreadyDeletedRecords() {
        // One expired+active and one expired+already-deleted for the same mobile. Only the active
        // one should be processed; the already-deleted row must not be double-counted.
        String activeExpiredId = seedResetCode("13800000003", LocalDateTime.now().minusDays(10), false);
        String alreadyDeletedId = seedResetCode("13800000003", LocalDateTime.now().minusDays(10), true);

        int affected = cleanupExpiredResetCodes();

        assertEquals(1, affected, "only the active expired record is processed");
        assertTrue(isDeleted(activeExpiredId));
        assertTrue(isDeleted(alreadyDeletedId), "pre-deleted record stays deleted");
    }

    @Test
    public void testCleanupRespectsConfigOverride() {
        setConfig(LitemallResetCodeBizModel.CONFIG_RESET_CODE_RETENTION_DAYS, "1");
        // retention=1 day: a 2-day-old record is expired, a half-day record is within window.
        String expiredId = seedResetCode("13800000004", LocalDateTime.now().minusDays(2), false);
        String keptId = seedResetCode("13800000004", LocalDateTime.now().minusHours(12), false);

        int affected = cleanupExpiredResetCodes();

        assertEquals(1, affected, "config override narrows the window to 1 day");
        assertTrue(isDeleted(expiredId));
        assertFalse(isDeleted(keptId), "half-day-old record is within the 1-day window");
    }

    @Test
    public void testCleanupBatchLimitCapsSingleRun() {
        // Seed more than CLEANUP_BATCH_LIMIT (500) expired records; a single run must cap at 500.
        for (int i = 0; i < 600; i++) {
            seedResetCode("13800000005", LocalDateTime.now().minusDays(30), false);
        }

        int affected = cleanupExpiredResetCodes();

        assertEquals(LitemallResetCodeBizModel.CLEANUP_BATCH_LIMIT, affected,
                "single run is capped at the batch limit; leftover is deferred to the next round");

        // Remaining active records for this mobile = 600 - 500 = 100.
        assertEquals(100, countActiveByMobile("13800000005"),
                "100 leftover records remain for the next cleanup round");
    }
}
