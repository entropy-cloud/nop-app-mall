package app.mall.dao.mapper;

import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.orm.SqlLibMapper;

@SqlLibMapper("/app/mall/sql/LitemallCouponUser.sql-lib.xml")
public interface LitemallCouponUserMapper {

    // Atomic conditional UPDATE: only marks as used if still in unused state (status=0).
    // Returns affected row count (0 = lost race or already used).
    int updateStatusIfUnused(@Name("id") String id, @Name("orderId") String orderId);
}
