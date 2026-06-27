package app.mall.dao.mapper;

import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.orm.SqlLibMapper;

@SqlLibMapper("/app/mall/sql/LitemallPointsAccount.sql-lib.xml")
public interface LitemallPointsAccountMapper {

    // Atomic conditional UPDATE with optimistic version check: only succeeds if the version matches.
    // Returns affected row count (0 = lost race / concurrent modification). Mirrors the
    // LitemallCouponUser.updateStatusIfUnused proven pattern for concurrent-safe balance mutation.
    int updateBalanceIfVersion(@Name("id") String id,
                               @Name("newBalance") int newBalance,
                               @Name("newTotalEarned") int newTotalEarned,
                               @Name("newTotalSpent") int newTotalSpent,
                               @Name("currentVersion") int currentVersion);
}
