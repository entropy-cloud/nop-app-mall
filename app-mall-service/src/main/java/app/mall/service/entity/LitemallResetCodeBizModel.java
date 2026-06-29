
package app.mall.service.entity;

import app.mall.biz.ILitemallResetCodeBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.dao.entity.LitemallResetCode;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.time.CoreMetrics;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;

@BizModel("LitemallResetCode")
public class LitemallResetCodeBizModel extends CrudBizModel<LitemallResetCode> implements ILitemallResetCodeBiz {
    // 验证码保留期（天）：缺失或非法时走 DEFAULT_RESET_CODE_RETENTION_DAYS（Decision D2）。
    public static final String CONFIG_RESET_CODE_RETENTION_DAYS = "mall_reset_code_retention_days";
    public static final int DEFAULT_RESET_CODE_RETENTION_DAYS = 7;
    // cleanupExpiredResetCodes 单轮扫描上限，避免长事务；剩余下轮 job 处理（Decision D3）。
    static final int CLEANUP_BATCH_LIMIT = 500;

    @Inject
    ILitemallSystemBiz systemBiz;

    public LitemallResetCodeBizModel() {
        setEntityName(LitemallResetCode.class.getName());
    }

    @Override
    @BizMutation
    public int cleanupExpiredResetCodes(IServiceContext context) {
        int retentionDays = resolveRetentionDays(context);
        // CoreMetrics 与 ORM 自动 createTime/updateTime 同源（Decision D1，逻辑删除）。
        LocalDateTime cutoff = CoreMetrics.currentDateTime().minusDays(retentionDays);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.lt(LitemallResetCode.PROP_NAME_addTime, cutoff));
        query.addFilter(FilterBeans.eq(LitemallResetCode.PROP_NAME_deleted, false));
        query.setLimit(CLEANUP_BATCH_LIMIT);

        List<LitemallResetCode> expired = doFindListByQueryDirectly(query, context);
        int count = 0;
        for (LitemallResetCode code : expired) {
            deleteEntity(code, "cleanupExpiredResetCodes", context);
            count++;
        }
        return count;
    }

    private int resolveRetentionDays(IServiceContext context) {
        String raw = systemBiz.getConfig(CONFIG_RESET_CODE_RETENTION_DAYS, context);
        if (StringHelper.isEmpty(raw)) {
            return DEFAULT_RESET_CODE_RETENTION_DAYS;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : DEFAULT_RESET_CODE_RETENTION_DAYS;
        } catch (NumberFormatException e) {
            return DEFAULT_RESET_CODE_RETENTION_DAYS;
        }
    }
}
