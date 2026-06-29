
package app.mall.service.entity;

import app.mall.biz.ILitemallPointsExpireBatchBiz;
import app.mall.dao.entity.LitemallPointsExpireBatch;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.time.CoreMetrics;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

import java.time.LocalDateTime;
import java.util.List;

// 积分有效期批次账本：每笔 earn（含正向调账）生成一条带 expireTime 的批次，spend/负向调账
// 按 expireTime ASC FIFO 消耗 remainingPoints，过期由 LitemallPointsAccountBizModel.expirePoints
// 编排。本 BizModel 承载标准 CRUD + 账本范围查询（后者供 PointsAccountBizModel 跨实体调用），
// 无自定义 GraphQL 动作。
@BizModel("LitemallPointsExpireBatch")
public class LitemallPointsExpireBatchBizModel extends CrudBizModel<LitemallPointsExpireBatch>
        implements ILitemallPointsExpireBatchBiz {
    public LitemallPointsExpireBatchBizModel() {
        setEntityName(LitemallPointsExpireBatch.class.getName());
    }

    // 这些范围查询使用 doFindListByQueryDirectly 绕过 xmeta prop 默认 filter-op 白名单
    // （gt/le 不在默认 [eq,in,dateBetween,dateTimeBetween] 内），与 expireCoupons 同模式。
    // 查询条件均为系统内部账本逻辑，非用户输入过滤，故绕过管道校验安全。

    @Override
    public List<LitemallPointsExpireBatch> findExpirableBatchesForUser(String userId, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallPointsExpireBatch.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.gt(LitemallPointsExpireBatch.PROP_NAME_remainingPoints, 0));
        query.addOrderField(LitemallPointsExpireBatch.PROP_NAME_expireTime, false);
        return doFindListByQueryDirectly(query, context);
    }

    @Override
    public List<LitemallPointsExpireBatch> findExpiredBatches(int limit, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.gt(LitemallPointsExpireBatch.PROP_NAME_remainingPoints, 0));
        query.addFilter(FilterBeans.le(LitemallPointsExpireBatch.PROP_NAME_expireTime, CoreMetrics.currentDateTime()));
        query.addOrderField(LitemallPointsExpireBatch.PROP_NAME_expireTime, false);
        query.setLimit(limit);
        return doFindListByQueryDirectly(query, context);
    }

    @Override
    public LitemallPointsExpireBatch findSoonestNonExpiredForUser(String userId, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallPointsExpireBatch.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.gt(LitemallPointsExpireBatch.PROP_NAME_remainingPoints, 0));
        query.addFilter(FilterBeans.gt(LitemallPointsExpireBatch.PROP_NAME_expireTime, CoreMetrics.currentDateTime()));
        query.addOrderField(LitemallPointsExpireBatch.PROP_NAME_expireTime, false);
        query.setLimit(1);
        List<LitemallPointsExpireBatch> list = doFindListByQueryDirectly(query, context);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<LitemallPointsExpireBatch> findBatchesExpiringWithin(int days, int limit, IServiceContext context) {
        LocalDateTime now = CoreMetrics.currentDateTime();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.gt(LitemallPointsExpireBatch.PROP_NAME_remainingPoints, 0));
        query.addFilter(FilterBeans.ge(LitemallPointsExpireBatch.PROP_NAME_expireTime, now));
        query.addFilter(FilterBeans.le(LitemallPointsExpireBatch.PROP_NAME_expireTime, now.plusDays(days)));
        query.addOrderField(LitemallPointsExpireBatch.PROP_NAME_expireTime, false);
        query.setLimit(limit);
        return doFindListByQueryDirectly(query, context);
    }
}
