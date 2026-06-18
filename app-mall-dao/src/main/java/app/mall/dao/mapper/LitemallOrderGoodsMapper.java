package app.mall.dao.mapper;

import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.orm.SqlLibMapper;

@SqlLibMapper("/app/mall/sql/LitemallOrderGoods.sql-lib.xml")
public interface LitemallOrderGoodsMapper {

    // Atomic conditional UPDATE: only sets the comment reference if not yet commented (comment=0).
    // Returns affected row count (0 = already commented or lost race).
    int updateCommentFlagIfUnused(@Name("orderGoodsId") String orderGoodsId, @Name("commentId") String commentId);
}
