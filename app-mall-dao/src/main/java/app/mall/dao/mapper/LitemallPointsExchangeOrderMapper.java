package app.mall.dao.mapper;

import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.orm.SqlLibMapper;

/**
 * SqlLib mapper for {@code LitemallPointsExchangeOrder} atomic status transitions used by the
 * combo-exchange timeout-cancel job (mirrors {@code LitemallOrderMapper.updateStatusIfMatch}).
 */
@SqlLibMapper("/app/mall/sql/LitemallPointsExchangeOrder.sql-lib.xml")
public interface LitemallPointsExchangeOrderMapper {

    // Atomic conditional status transition; returns affected row count
    // (0 = lost race or status mismatch). Used by cancelExpiredExchangeOrders to guard against
    // concurrent double-processing vs a user-initiated cancel.
    int updateStatusIfMatch(@Name("id") String id,
                            @Name("newStatus") int newStatus,
                            @Name("expectedStatus") int expectedStatus);
}
