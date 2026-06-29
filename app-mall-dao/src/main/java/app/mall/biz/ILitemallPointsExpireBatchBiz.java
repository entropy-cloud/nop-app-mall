
package app.mall.biz;

import app.mall.dao.entity.LitemallPointsExpireBatch;
import io.nop.api.core.annotations.core.Name;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.util.List;

public interface ILitemallPointsExpireBatchBiz extends ICrudBiz<LitemallPointsExpireBatch>{

    // ===== 积分有效期账本内部查询（无 @BizQuery/@BizMutation → 非 GraphQL 暴露）=====
    // 由 LitemallPointsAccountBizModel 调用。使用 doFindListByQueryDirectly 绕过 xmeta
    // prop 默认 filter-op 白名单（gt/le 不在默认 [eq,in,dateBetween,dateTimeBetween] 内），
    // 与 LitemallCouponUserBizModel.expireCoupons 同一模式。非用户输入过滤条件。

    /** FIFO 消耗用：指定用户所有 remainingPoints>0 的批次，按 expireTime ASC。 */
    List<LitemallPointsExpireBatch> findExpirableBatchesForUser(@Name("userId") String userId,
                                                                IServiceContext context);

    /** 自动过期用：所有到期（expireTime<=now）且 remainingPoints>0 的批次，按 expireTime ASC，限 limit 条。 */
    List<LitemallPointsExpireBatch> findExpiredBatches(@Name("limit") int limit,
                                                       IServiceContext context);

    /** 「即将过期」提示用：指定用户最近一笔未到期且 remainingPoints>0 的批次（无则 null）。 */
    LitemallPointsExpireBatch findSoonestNonExpiredForUser(@Name("userId") String userId,
                                                           IServiceContext context);
}
