package app.mall.service.entity;

import app.mall.biz.ILitemallFlashSaleSessionBiz;
import app.mall.dao.entity.LitemallFlashSaleSession;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.time.CoreMetrics;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@BizModel("LitemallFlashSaleSession")
public class LitemallFlashSaleSessionBizModel extends CrudBizModel<LitemallFlashSaleSession>
        implements ILitemallFlashSaleSessionBiz {

    static final int SESSION_STATUS_NOT_STARTED = 0;
    static final int SESSION_STATUS_IN_PROGRESS = 1;
    static final int SESSION_STATUS_FINISHED = 2;

    public LitemallFlashSaleSessionBizModel() {
        setEntityName(LitemallFlashSaleSession.class.getName());
    }

    @Override
    @BizMutation
    public int switchFlashSaleSessions(IServiceContext context) {
        // Decision A (Phase 1): nop-job flips sessionStatus 0 -> 1 -> 2 by time window.
        // Boundary precision is best-effort here; flashSaleBuy still enforces the time window
        // authoritatively. Registered in scheduler.yaml as switch-flash-sale-sessions.
        LocalDateTime now = CoreMetrics.currentDateTime();

        QueryBean query = new QueryBean();
        // Skip sessions already in FINISHED state — they cannot transition further. The Nop
        // GraphQL query layer restricts the allowed operators to {eq, in, dateBetween,
        // dateTimeBetween}, so we use an inclusive `in` over the non-final states.
        query.addFilter(FilterBeans.in(LitemallFlashSaleSession.PROP_NAME_sessionStatus,
                Arrays.asList(SESSION_STATUS_NOT_STARTED, SESSION_STATUS_IN_PROGRESS)));
        query.setLimit(500);

        List<LitemallFlashSaleSession> sessions = findList(query, null, context);

        int count = 0;
        for (LitemallFlashSaleSession session : sessions) {
            int target = computeSessionStatus(session, now);
            if (session.getSessionStatus() == null || session.getSessionStatus() != target) {
                session.setSessionStatus(target);
                updateEntity(session, "switchFlashSaleSessions", context);
                count++;
            }
        }
        return count;
    }

    private int computeSessionStatus(LitemallFlashSaleSession session, LocalDateTime now) {
        LocalDateTime start = session.getSessionStart();
        LocalDateTime end = session.getSessionEnd();
        if (start != null && now.isBefore(start)) {
            return SESSION_STATUS_NOT_STARTED;
        }
        if (end != null && now.isAfter(end)) {
            return SESSION_STATUS_FINISHED;
        }
        return SESSION_STATUS_IN_PROGRESS;
    }
}
