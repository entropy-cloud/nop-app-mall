
package app.mall.biz;

import app.mall.dao.entity.LitemallPointsAccount;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.util.Map;

public interface ILitemallPointsAccountBiz extends ICrudBiz<LitemallPointsAccount>{

    @BizQuery
    int getMyPoints(IServiceContext context);

    @BizMutation
    LitemallPointsAccount earnPoints(@Name("userId") String userId,
                                     @Name("amount") int amount,
                                     @Name("changeType") int changeType,
                                     @Name("sourceType") String sourceType,
                                     @Optional @Name("sourceId") String sourceId,
                                     @Optional @Name("remark") String remark,
                                     IServiceContext context);

    @BizMutation
    LitemallPointsAccount spendPoints(@Name("userId") String userId,
                                      @Name("amount") int amount,
                                      @Name("changeType") int changeType,
                                      @Name("sourceType") String sourceType,
                                      @Optional @Name("sourceId") String sourceId,
                                      @Optional @Name("remark") String remark,
                                      IServiceContext context);

    @BizMutation
    @Auth(roles = "admin")
    LitemallPointsAccount adjustPoints(@Name("userId") String userId,
                                       @Name("amount") int amount,
                                       @Optional @Name("remark") String remark,
                                       IServiceContext context);

    // 积分有效期与自动过期（successor）：扫描到期批次，扣减 balance 并写 EXPIRE 流水。
    // 系统定时任务入口（MallJobInvoker.expirePoints），亦可通过 GraphQL 触发用于测试。
    // 幂等：remainingPoints>0 选择守卫 + PointsAccount version 乐观锁 CAS + EXPIRE 流水
    // (sourceType=expire, sourceId=batchId) 唯一键兜底重放安全。返回本轮处理的批次数。
    @BizMutation
    int expirePoints(IServiceContext context);

    // 「即将过期」提示：返回当前用户最近一笔未到期且 remainingPoints>0 批次的
    // {points, expireTime}；无批次（仅存量积分）时返回 null。
    @BizQuery
    Map<String, Object> getMyPointsExpiryHint(IServiceContext context);

    // 过期预警推送（successor of getMyPointsExpiryHint，从拉取式升级为主动推送）：扫描近 N 天
    // （mall_points_expiry_remind_days，缺省 3）到期的非空批次，按 userId 聚合为一条 SYSTEM 站内信，
    // 幂等（当日同标题已存在则跳过）。系统定时任务入口（MallJobInvoker.sendPointsExpiryReminders），
    // 亦可通过 GraphQL 触发用于测试。返回本轮推送的用户数。
    @BizMutation
    int sendPointsExpiryReminders(IServiceContext context);

    // Internal config-resolution helpers (not GraphQL-exposed): read NopSysVariable defaults
    // for points ratios. Used by LitemallOrderBizModel to compute integralPrice / shopping rewards.
    int resolveEarnPerYuan(IServiceContext context);

    int resolveToYuanRatio(IServiceContext context);

    double resolveDeductMaxRatio(IServiceContext context);
}
