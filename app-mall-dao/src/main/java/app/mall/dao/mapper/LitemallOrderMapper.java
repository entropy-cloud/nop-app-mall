package app.mall.dao.mapper;

import app.mall.dao.dto.CartRankingItemBean;
import app.mall.dao.dto.CouponUsageStatisticsBean;
import app.mall.dao.dto.DashboardGmvBean;
import app.mall.dao.dto.GoodsStatisticsBean;
import app.mall.dao.dto.OrderPointBean;
import app.mall.dao.dto.OrderStatisticsBean;
import app.mall.dao.dto.ReportScalarCountBean;
import app.mall.dao.dto.SalesFunnelBean;
import app.mall.dao.dto.StockWarningItemBean;
import app.mall.dao.dto.UnsalableGoodsBean;
import app.mall.dao.dto.UserPaymentPointBean;
import app.mall.dao.dto.UserPaymentSummaryBean;
import app.mall.dao.dto.UserStatisticsBean;
import app.mall.dao.dto.AovDistributionBean;
import app.mall.dao.dto.PaymentMethodShareBean;
import app.mall.dao.dto.ReturnReasonShareBean;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.orm.SqlLibMapper;

import java.sql.Timestamp;
import java.util.List;

@SqlLibMapper("/app/mall/sql/LitemallOrder.sql-lib.xml")
public interface LitemallOrderMapper {

    // Atomic conditional status transition; returns affected row count (0 = lost race or status mismatch)
    int updateStatusIfMatch(@Name("orderId") String orderId,
                            @Name("newStatus") int newStatus,
                            @Name("expectedStatus") int expectedStatus);

    OrderStatisticsBean getOrderStatistics(@Name("startDate") Timestamp startDate,
                                            @Name("endDate") Timestamp endDate);

    List<GoodsStatisticsBean> getGoodsSalesRanking(@Name("startDate") Timestamp startDate,
                                                    @Name("endDate") Timestamp endDate,
                                                    @Name("limit") int limit);

    UserStatisticsBean getUserStatistics(@Name("startDate") Timestamp startDate,
                                          @Name("endDate") Timestamp endDate,
                                          @Name("todayStart") Timestamp todayStart,
                                          @Name("weekStart") Timestamp weekStart,
                                          @Name("monthStart") Timestamp monthStart);

    // ===== P18 经营看板 =====

    DashboardGmvBean getDashboardGmv(@Name("startDate") Timestamp startDate,
                                      @Name("endDate") Timestamp endDate);

    List<OrderPointBean> getPaidOrderPoints(@Name("startDate") Timestamp startDate,
                                             @Name("endDate") Timestamp endDate);

    List<StockWarningItemBean> getStockWarningList(@Name("threshold") int threshold,
                                                    @Name("limit") int limit);

    // ===== P19 报表体系扩展 =====

    SalesFunnelBean getSalesFunnel(@Name("startDate") Timestamp startDate,
                                    @Name("endDate") Timestamp endDate);

    List<CartRankingItemBean> getCartRanking(@Name("startDate") Timestamp startDate,
                                              @Name("endDate") Timestamp endDate,
                                              @Name("categoryId") String categoryId,
                                              @Name("limit") int limit);

    List<UnsalableGoodsBean> getUnsalableGoods(@Name("startDate") Timestamp startDate,
                                                @Name("endDate") Timestamp endDate,
                                                @Name("categoryId") String categoryId,
                                                @Name("limit") int limit);

    ReportScalarCountBean getSoldGoodsCount(@Name("startDate") Timestamp startDate,
                                             @Name("endDate") Timestamp endDate,
                                             @Name("categoryId") String categoryId);

    ReportScalarCountBean getOnSaleGoodsCount(@Name("categoryId") String categoryId);

    List<GoodsStatisticsBean> getGoodsSalesRankingByCategory(@Name("startDate") Timestamp startDate,
                                                              @Name("endDate") Timestamp endDate,
                                                              @Name("categoryId") String categoryId,
                                                              @Name("limit") int limit);

    // ===== P19 用户分析 =====

    List<UserPaymentSummaryBean> getUserPaymentSummaryInPeriod(@Name("startDate") Timestamp startDate,
                                                                @Name("endDate") Timestamp endDate);

    List<UserPaymentSummaryBean> getUserPaymentSummaryAllTime();

    List<UserPaymentPointBean> getUserPaymentPoints(@Name("startDate") Timestamp startDate,
                                                     @Name("endDate") Timestamp endDate);

    // ===== P19 订单分析 + 营销分析 =====

    List<AovDistributionBean> getAovDistribution(@Name("startDate") Timestamp startDate,
                                                   @Name("endDate") Timestamp endDate);

    List<PaymentMethodShareBean> getPaymentMethodShare(@Name("startDate") Timestamp startDate,
                                                         @Name("endDate") Timestamp endDate);

    List<ReturnReasonShareBean> getReturnReasonShare(@Name("startDate") Timestamp startDate,
                                                       @Name("endDate") Timestamp endDate);

    CouponUsageStatisticsBean getCouponAnalysis(@Name("startDate") Timestamp startDate,
                                                  @Name("endDate") Timestamp endDate);
}
