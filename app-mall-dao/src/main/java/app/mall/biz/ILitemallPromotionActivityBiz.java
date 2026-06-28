
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.exceptions.NopException;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.dto.PromotionEffectivenessBean;
import app.mall.dao.dto.PromotionResolutionBean;
import app.mall.dao.entity.LitemallPromotionActivity;

import java.math.BigDecimal;
import java.util.List;

public interface ILitemallPromotionActivityBiz extends ICrudBiz<LitemallPromotionActivity> {

    @BizQuery
    BigDecimal selectPromotionForOrder(@Name("goodsPrice") BigDecimal goodsPrice,
                                       @Optional @Name("goodsScopeIds") List<String> goodsScopeIds,
                                       IServiceContext context) throws NopException;

    /**
     * 内部解析命中满减活动的完整结果（activityId/discount/meetAmount）。不带 {@code @BizQuery}，
     * 不经 GraphQL 暴露，仅供 submit 等内部路径写参与记录使用。返回 null 表示无命中。
     * {@link #selectPromotionForOrder} 委托本方法取 discount，保持既有 GraphQL 契约不变。
     */
    PromotionResolutionBean resolvePromotionForOrderInternal(@Name("goodsPrice") BigDecimal goodsPrice,
                                                              @Optional @Name("goodsScopeIds") List<String> goodsScopeIds,
                                                              IServiceContext context);

    @BizMutation
    LitemallPromotionActivity publishActivity(@Name("id") String id, IServiceContext context);

    @BizMutation
    LitemallPromotionActivity unpublishActivity(@Name("id") String id, IServiceContext context);

    @BizQuery
    PromotionEffectivenessBean getPromotionEffectiveness(@Optional @Name("activityId") String activityId,
                                                         @Optional @Name("startDate") String startDate,
                                                         @Optional @Name("endDate") String endDate,
                                                         IServiceContext context);
}

