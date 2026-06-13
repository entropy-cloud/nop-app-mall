package app.mall.dao.mapper;

import app.mall.dao.dto.GoodsStatisticsBean;
import app.mall.dao.dto.OrderStatisticsBean;
import app.mall.dao.dto.UserStatisticsBean;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.orm.SqlLibMapper;

import java.sql.Timestamp;
import java.util.List;

@SqlLibMapper("/app/mall/sql/LitemallOrder.sql-lib.xml")
public interface LitemallOrderMapper {

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
}
