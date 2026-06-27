package app.mall.service.entity;

import app.mall.dao.entity.LitemallCheckInRecord;
import app.mall.dao.entity.LitemallCheckInRule;
import app.mall.dao.entity.LitemallPointsFlow;
import io.nop.api.core.annotations.autotest.NopTestConfig;
import io.nop.api.core.annotations.core.OptionalBoolean;
import io.nop.api.core.beans.ApiRequest;
import io.nop.api.core.beans.ApiResponse;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.api.core.time.CoreMetrics;
import io.nop.autotest.junit.JunitBaseTestCase;
import io.nop.dao.api.IDaoProvider;
import io.nop.graphql.core.IGraphQLExecutionContext;
import io.nop.graphql.core.ast.GraphQLOperationType;
import io.nop.graphql.core.engine.IGraphQLEngine;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NopTestConfig(localDb = true, initDatabaseSchema = OptionalBoolean.TRUE)
public class TestLitemallCheckInRecordBizModel extends JunitBaseTestCase {

    @Inject
    IGraphQLEngine graphQLEngine;

    @Inject
    IDaoProvider daoProvider;

    private static final String USER_ID = "701";

    @BeforeEach
    void setUp() {
        ContextProvider.getOrCreateContext().setUserId(USER_ID);
        ContextProvider.getOrCreateContext().setUserName("checkin-test");
    }

    private void seedRule(int daySeq, int pointReward, int resetCycle) {
        LitemallCheckInRule rule = daoProvider.daoFor(LitemallCheckInRule.class).newEntity();
        rule.setDaySeq(daySeq);
        rule.setPointReward(pointReward);
        rule.setResetCycle(resetCycle);
        daoProvider.daoFor(LitemallCheckInRule.class).saveEntity(rule);
    }

    private void seedRecord(String userId, LocalDate date, int consecutiveDays) {
        LitemallCheckInRecord rec = daoProvider.daoFor(LitemallCheckInRecord.class).newEntity();
        rec.setUserId(userId);
        rec.setCheckInDate(date);
        rec.setConsecutiveDays(consecutiveDays);
        rec.setPointsEarned(0);
        rec.setDeleted(false);
        daoProvider.daoFor(LitemallCheckInRecord.class).saveEntity(rec);
    }

    private ApiResponse<?> rpc(GraphQLOperationType op, String action, Map<String, Object> data) {
        IGraphQLExecutionContext ctx = graphQLEngine.newRpcContext(op, action, ApiRequest.build(data));
        return graphQLEngine.executeRpc(ctx);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> checkInToday() {
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallCheckInRecord__checkInToday", new HashMap<>());
        assertEquals(0, r.getStatus(), "checkInToday failed: " + r);
        return (Map<String, Object>) r.getData();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMyCheckInStatus() {
        ApiResponse<?> r = rpc(GraphQLOperationType.query, "LitemallCheckInRecord__getMyCheckInStatus", new HashMap<>());
        assertEquals(0, r.getStatus(), "getMyCheckInStatus failed: " + r);
        return (Map<String, Object>) r.getData();
    }

    private long countCheckInFlows() {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType, LitemallPointsAccountBizModel.SOURCE_TYPE_CHECK_IN));
        q.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_userId, USER_ID));
        return daoProvider.daoFor(LitemallPointsFlow.class).findAllByQuery(q).size();
    }

    @Test
    public void testFirstCheckInEarnsTierOne() {
        seedRule(1, 5, 0);
        seedRule(3, 15, 0);
        seedRule(7, 50, 0);

        Map<String, Object> result = checkInToday();

        assertEquals(1, ((Number) result.get("consecutiveDays")).intValue());
        assertEquals(5, ((Number) result.get("pointsEarned")).intValue(), "first day should match daySeq=1 tier");
        assertEquals(5, ((Number) result.get("accountBalance")).intValue());
        assertTrue((Boolean) result.get("todayChecked"));
        assertEquals(1, countCheckInFlows(), "one check-in points flow must be written");
    }

    @Test
    public void testConsecutiveIncrementAndStepwiseTier() {
        seedRule(1, 5, 0);
        seedRule(3, 15, 0);
        seedRule(7, 50, 0);
        seedRecord(USER_ID, CoreMetrics.currentDate().minusDays(1), 2);

        Map<String, Object> result = checkInToday();

        assertEquals(3, ((Number) result.get("consecutiveDays")).intValue(), "yesterday=2 -> today=3");
        assertEquals(15, ((Number) result.get("pointsEarned")).intValue(), "daySeq=3 tier matches at consecutive=3");
        assertEquals(15, ((Number) result.get("accountBalance")).intValue());
    }

    @Test
    public void testBrokenStreakResetsToOne() {
        seedRule(1, 5, 0);
        seedRule(7, 50, 0);
        // record 2 days ago but NOT yesterday -> broken streak
        seedRecord(USER_ID, CoreMetrics.currentDate().minusDays(2), 5);

        Map<String, Object> result = checkInToday();

        assertEquals(1, ((Number) result.get("consecutiveDays")).intValue(), "broken streak resets to 1");
        assertEquals(5, ((Number) result.get("pointsEarned")).intValue());
    }

    @Test
    public void testDuplicateSameDayRejected() {
        seedRule(1, 5, 0);

        checkInToday();
        ApiResponse<?> second = rpc(GraphQLOperationType.mutation, "LitemallCheckInRecord__checkInToday", new HashMap<>());
        assertNotEquals(0, second.getStatus(), "second same-day check-in must be rejected");
    }

    @Test
    public void testCycleResetWhenExceedingResetCycle() {
        seedRule(1, 5, 3);
        seedRule(2, 10, 3);
        seedRule(3, 15, 3);
        // yesterday was day 3 of a 3-day cycle -> candidate=4 > resetCycle=3 -> reset to 1
        seedRecord(USER_ID, CoreMetrics.currentDate().minusDays(1), 3);

        Map<String, Object> result = checkInToday();

        assertEquals(1, ((Number) result.get("consecutiveDays")).intValue(), "cycle reset to 1 after exceeding resetCycle");
        assertEquals(5, ((Number) result.get("pointsEarned")).intValue());
    }

    @Test
    public void testNoCycleCapsAtTopTier() {
        seedRule(1, 5, 0);
        seedRule(3, 15, 0);
        // resetCycle=0 (no cycle); yesterday=3 already at tier daySeq=3, today=4 still tier daySeq=3
        seedRecord(USER_ID, CoreMetrics.currentDate().minusDays(1), 3);

        Map<String, Object> result = checkInToday();

        assertEquals(4, ((Number) result.get("consecutiveDays")).intValue(), "no cycle -> keeps growing");
        assertEquals(15, ((Number) result.get("pointsEarned")).intValue(), "stays at top tier daySeq=3");
    }

    @Test
    public void testNoRulesRejected() {
        ApiResponse<?> r = rpc(GraphQLOperationType.mutation, "LitemallCheckInRecord__checkInToday", new HashMap<>());
        assertNotEquals(0, r.getStatus(), "check-in with no rules configured must fail");
    }

    @Test
    public void testZeroRewardWhenNoMatchingTier() {
        // rules start at daySeq=3, user consecutive=1 -> no tier match -> 0 points, record still written
        seedRule(3, 15, 0);
        seedRule(7, 50, 0);

        Map<String, Object> result = checkInToday();

        assertEquals(1, ((Number) result.get("consecutiveDays")).intValue());
        assertEquals(0, ((Number) result.get("pointsEarned")).intValue(), "no matching tier -> 0 points");
        assertEquals(0, countCheckInFlows(), "zero-reward check-in must not write points flow");
    }

    @Test
    public void testGetMyCheckInStatusAfterCheckIn() {
        seedRule(1, 5, 0);
        seedRule(3, 15, 0);

        checkInToday();
        Map<String, Object> status = getMyCheckInStatus();

        assertTrue((Boolean) status.get("todayChecked"));
        assertEquals(1, ((Number) status.get("consecutiveDays")).intValue());
        assertEquals(1, ((Number) status.get("totalDays")).intValue());
        assertEquals(5, ((Number) status.get("accountBalance")).intValue());
        assertEquals(2, ((java.util.List<?>) status.get("rewardRules")).size(), "reward preview lists all rules");
    }

    @Test
    public void testGetMyCheckInStatusStreakFromYesterday() {
        seedRule(1, 5, 0);
        seedRecord(USER_ID, CoreMetrics.currentDate().minusDays(1), 4);

        Map<String, Object> status = getMyCheckInStatus();

        assertEquals(false, status.get("todayChecked"));
        assertEquals(4, ((Number) status.get("consecutiveDays")).intValue(),
                "before today's check-in, streak is carried from yesterday");
        assertEquals(1, ((Number) status.get("totalDays")).intValue());
    }
}
