package app.mall.dao.mapper;

import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.orm.SqlLibMapper;

@SqlLibMapper("/app/mall/sql/LitemallTimeDiscount.sql-lib.xml")
public interface LitemallTimeDiscountMapper {

    int reduceTimeDiscountStock(@Name("discountId") String discountId, @Name("num") int num);
}
