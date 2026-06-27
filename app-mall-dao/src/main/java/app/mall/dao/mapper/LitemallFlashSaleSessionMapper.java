package app.mall.dao.mapper;

import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.orm.SqlLibMapper;

@SqlLibMapper("/app/mall/sql/LitemallFlashSaleSession.sql-lib.xml")
public interface LitemallFlashSaleSessionMapper {

    int reduceFlashSaleSessionStock(@Name("sessionId") String sessionId, @Name("num") int num);
}
