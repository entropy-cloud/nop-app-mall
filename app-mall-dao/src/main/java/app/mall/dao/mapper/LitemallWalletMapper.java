package app.mall.dao.mapper;

import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.orm.SqlLibMapper;

import java.math.BigDecimal;

@SqlLibMapper("/app/mall/sql/LitemallWallet.sql-lib.xml")
public interface LitemallWalletMapper {

    // Atomic conditional UPDATE with optimistic version check: only succeeds if the version matches.
    // Returns affected row count (0 = lost race / concurrent modification). Mirrors the
    // LitemallPointsAccountMapper.updateBalanceIfVersion proven pattern for concurrent-safe balance mutation.
    int updateBalanceIfVersion(@Name("id") String id,
                               @Name("newBalance") BigDecimal newBalance,
                               @Name("newTotalRecharge") BigDecimal newTotalRecharge,
                               @Name("newTotalSpent") BigDecimal newTotalSpent,
                               @Name("currentVersion") int currentVersion);
}
