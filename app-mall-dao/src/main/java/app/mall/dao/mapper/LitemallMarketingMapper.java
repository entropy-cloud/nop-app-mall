package app.mall.dao.mapper;

import app.mall.dao.dto.CouponUsageStatisticsBean;
import app.mall.dao.dto.FlashSaleEffectivenessBean;
import app.mall.dao.dto.PinTuanEffectivenessBean;
import app.mall.dao.dto.PromotionEffectivenessBean;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.orm.SqlLibMapper;

import java.sql.Timestamp;

@SqlLibMapper("/app/mall/sql/LitemallMarketing.sql-lib.xml")
public interface LitemallMarketingMapper {

    CouponUsageStatisticsBean getCouponUsageStatistics(@Name("couponId") String couponId,
                                                       @Name("startDate") Timestamp startDate,
                                                       @Name("endDate") Timestamp endDate);

    PromotionEffectivenessBean getPromotionEffectiveness(@Name("startDate") Timestamp startDate,
                                                         @Name("endDate") Timestamp endDate);

    PromotionEffectivenessBean getPromotionEffectivenessByActivity(@Name("activityId") String activityId,
                                                                    @Name("startDate") Timestamp startDate,
                                                                    @Name("endDate") Timestamp endDate);

    FlashSaleEffectivenessBean getFlashSaleEffectiveness(@Name("flashSaleId") String flashSaleId,
                                                          @Name("startDate") Timestamp startDate,
                                                          @Name("endDate") Timestamp endDate);

    PinTuanEffectivenessBean getPinTuanEffectiveness(@Name("activityId") String activityId,
                                                     @Name("startDate") Timestamp startDate,
                                                     @Name("endDate") Timestamp endDate);
}

