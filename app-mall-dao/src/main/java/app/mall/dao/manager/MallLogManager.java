package app.mall.dao.manager;

import app.mall.dao.entity.LitemallLog;
import io.nop.api.core.auth.IUserContext;
import io.nop.api.core.context.ContextProvider;
import io.nop.api.core.context.IContext;
import io.nop.dao.api.IDaoProvider;
import io.nop.dao.api.IEntityDao;

import javax.inject.Inject;

public class MallLogManager {
    public static final Integer LOG_TYPE_GENERAL = 0;
    public static final Integer LOG_TYPE_AUTH = 1;
    public static final Integer LOG_TYPE_ORDER = 2;
    public static final Integer LOG_TYPE_OTHER = 3;

    @Inject
    IDaoProvider daoProvider;

    IEntityDao<LitemallLog> dao() {
        return daoProvider.daoFor(LitemallLog.class);
    }

    public void logGeneralSucceed(String action) {
        logAdmin(LOG_TYPE_GENERAL, action, true, "", "");
    }

    public void logGeneralSucceed(String action, String result) {
        logAdmin(LOG_TYPE_GENERAL, action, true, result, "");
    }

    public void logGeneralFail(String action, String error) {
        logAdmin(LOG_TYPE_GENERAL, action, false, error, "");
    }

    public void logAuthSucceed(String action) {
        logAdmin(LOG_TYPE_AUTH, action, true, "", "");
    }

    public void logAuthSucceed(String action, String result) {
        logAdmin(LOG_TYPE_AUTH, action, true, result, "");
    }

    public void logAuthFail(String action, String error) {
        logAdmin(LOG_TYPE_AUTH, action, false, error, "");
    }

    public void logOrderSucceed(String action) {
        logAdmin(LOG_TYPE_ORDER, action, true, "", "");
    }

    public void logOrderSucceed(String action, String result) {
        logAdmin(LOG_TYPE_ORDER, action, true, result, "");
    }

    public void logOrderFail(String action, String error) {
        logAdmin(LOG_TYPE_ORDER, action, false, error, "");
    }

    public void logOtherSucceed(String action) {
        logAdmin(LOG_TYPE_OTHER, action, true, "", "");
    }

    public void logOtherSucceed(String action, String result) {
        logAdmin(LOG_TYPE_OTHER, action, true, result, "");
    }


    public void logOtherFail(String action, String error) {
        logAdmin(LOG_TYPE_OTHER, action, false, error, "");
    }

    public void logAdmin(Integer type, String action, Boolean succeed, String result, String comment) {
        LitemallLog log = new LitemallLog();

        IUserContext currentUser = IUserContext.get();
        if (currentUser != null) {
            log.setAdmin(currentUser.getUserName());
        } else {
            log.setAdmin("匿名用户");
        }

        IContext ctx = ContextProvider.currentContext();
        if (ctx != null) {
            String ip = ctx.getCallIp();
            log.setIp(ip);
        }

        log.setType(type);
        log.setType(type);
        log.setAction(action);
        log.setStatus(succeed);
        log.setResult(result);
        log.setComment(comment);

        dao().saveEntity(log);
    }
}
