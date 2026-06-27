
package app.mall.service.entity;

import app.mall.biz.ILitemallCheckInRecordBiz;
import app.mall.biz.ILitemallCheckInRuleBiz;
import app.mall.biz.ILitemallPointsAccountBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.dto.CheckInResultBean;
import app.mall.dao.dto.CheckInRewardRuleBean;
import app.mall.dao.dto.CheckInStatusBean;
import app.mall.dao.entity.LitemallCheckInRecord;
import app.mall.dao.entity.LitemallCheckInRule;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.api.core.time.CoreMetrics;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static app.mall.service.AppMallErrors.ERR_CHECK_IN_ALREADY_TODAY;
import static app.mall.service.AppMallErrors.ERR_CHECK_IN_RULE_MISSING;

@BizModel("LitemallCheckInRecord")
public class LitemallCheckInRecordBizModel extends CrudBizModel<LitemallCheckInRecord> implements ILitemallCheckInRecordBiz {
    @Inject
    ILitemallPointsAccountBiz pointsAccountBiz;

    @Inject
    ILitemallCheckInRuleBiz checkInRuleBiz;

    public LitemallCheckInRecordBizModel() {
        setEntityName(LitemallCheckInRecord.class.getName());
    }

    @Override
    @BizMutation
    public CheckInResultBean checkInToday(IServiceContext context) {
        String userId = context.getUserId();
        LocalDate today = CoreMetrics.currentDate();

        LitemallCheckInRecord existing = findRecord(userId, today, context);
        if (existing != null) {
            throw new NopException(ERR_CHECK_IN_ALREADY_TODAY)
                    .param("userId", userId)
                    .param("checkInDate", today.toString());
        }

        List<LitemallCheckInRule> rules = findRules(context);
        if (rules.isEmpty()) {
            throw new NopException(ERR_CHECK_IN_RULE_MISSING);
        }

        int resetCycle = resolveResetCycle(rules);
        LitemallCheckInRecord yesterdayRecord = findRecord(userId, today.minusDays(1), context);
        int prevConsecutive = yesterdayRecord != null && yesterdayRecord.getConsecutiveDays() != null
                ? yesterdayRecord.getConsecutiveDays() : 0;
        int candidate = prevConsecutive + 1;
        int consecutiveDays = (resetCycle > 0 && candidate > resetCycle) ? 1 : candidate;

        int pointReward = matchTier(rules, consecutiveDays);

        LitemallCheckInRecord record = newEntity();
        record.setUserId(userId);
        record.setCheckInDate(today);
        record.setConsecutiveDays(consecutiveDays);
        record.setPointsEarned(pointReward);
        saveEntity(record, "checkInToday", context);

        if (pointReward > 0) {
            pointsAccountBiz.earnPoints(userId, pointReward,
                    _AppMallDaoConstants.POINTS_CHANGE_TYPE_EARN,
                    LitemallPointsAccountBizModel.SOURCE_TYPE_CHECK_IN,
                    record.orm_idString(),
                    "每日签到奖励",
                    context);
        }

        CheckInResultBean result = new CheckInResultBean();
        result.setTodayChecked(true);
        result.setConsecutiveDays(consecutiveDays);
        result.setPointsEarned(pointReward);
        result.setAccountBalance(pointsAccountBiz.getMyPoints(context));
        return result;
    }

    @Override
    @BizQuery
    public CheckInStatusBean getMyCheckInStatus(IServiceContext context) {
        CheckInStatusBean status = new CheckInStatusBean();
        List<LitemallCheckInRule> rules = findRules(context);
        status.setRewardRules(toRewardRuleBeans(rules));

        String userId = context.getUserId();
        if (StringHelper.isEmpty(userId)) {
            return status;
        }

        LocalDate today = CoreMetrics.currentDate();
        LitemallCheckInRecord todayRecord = findRecord(userId, today, context);
        status.setTodayChecked(todayRecord != null);

        int consecutive;
        if (todayRecord != null && todayRecord.getConsecutiveDays() != null) {
            consecutive = todayRecord.getConsecutiveDays();
        } else {
            LitemallCheckInRecord yesterdayRecord = findRecord(userId, today.minusDays(1), context);
            consecutive = yesterdayRecord != null && yesterdayRecord.getConsecutiveDays() != null
                    ? yesterdayRecord.getConsecutiveDays() : 0;
        }
        status.setConsecutiveDays(consecutive);

        QueryBean countQuery = new QueryBean();
        countQuery.addFilter(FilterBeans.eq(LitemallCheckInRecord.PROP_NAME_userId, userId));
        status.setTotalDays((int) findCount(countQuery, context));

        status.setAccountBalance(pointsAccountBiz.getMyPoints(context));
        return status;
    }

    private LitemallCheckInRecord findRecord(String userId, LocalDate date, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCheckInRecord.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallCheckInRecord.PROP_NAME_checkInDate, date));
        query.setLimit(1);
        return findFirst(query, null, context);
    }

    private List<LitemallCheckInRule> findRules(IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addOrderField(LitemallCheckInRule.PROP_NAME_daySeq, false);
        return checkInRuleBiz.findList(query, null, context);
    }

    private int resolveResetCycle(List<LitemallCheckInRule> rules) {
        int max = 0;
        for (LitemallCheckInRule rule : rules) {
            Integer cycle = rule.getResetCycle();
            if (cycle != null && cycle > max) {
                max = cycle;
            }
        }
        return max;
    }

    private int matchTier(List<LitemallCheckInRule> rules, int consecutiveDays) {
        int reward = 0;
        for (LitemallCheckInRule rule : rules) {
            Integer daySeq = rule.getDaySeq();
            if (daySeq != null && daySeq <= consecutiveDays) {
                Integer pointReward = rule.getPointReward();
                if (pointReward != null && pointReward > reward) {
                    reward = pointReward;
                }
            }
        }
        return reward;
    }

    private List<CheckInRewardRuleBean> toRewardRuleBeans(List<LitemallCheckInRule> rules) {
        List<CheckInRewardRuleBean> beans = new ArrayList<>();
        for (LitemallCheckInRule rule : rules) {
            CheckInRewardRuleBean bean = new CheckInRewardRuleBean();
            bean.setDaySeq(rule.getDaySeq() == null ? 0 : rule.getDaySeq());
            bean.setPointReward(rule.getPointReward() == null ? 0 : rule.getPointReward());
            bean.setResetCycle(rule.getResetCycle() == null ? 0 : rule.getResetCycle());
            beans.add(bean);
        }
        return beans;
    }
}
