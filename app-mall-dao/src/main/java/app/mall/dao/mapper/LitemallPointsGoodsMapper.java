package app.mall.dao.mapper;

import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.orm.SqlLibMapper;

@SqlLibMapper("/app/mall/sql/LitemallPointsGoods.sql-lib.xml")
public interface LitemallPointsGoodsMapper {

    // Atomic conditional UPDATE: only succeeds if exchangeStock >= num.
    // Returns affected row count (0 = sold out / concurrent modification).
    // Mirrors the LitemallFlashSaleSession.reduceFlashSaleSessionStock pattern.
    int reduceExchangeStock(@Name("id") String id, @Name("num") int num);

    // Restore exchange stock on cancellation. exchangedCount is clamped at 0 to avoid
    // negative counts if a manual data fix has already adjusted it.
    int restoreExchangeStock(@Name("id") String id, @Name("num") int num);
}
