package app.mall.dao.mapper;

import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.orm.SqlLibMapper;

@SqlLibMapper("/app/mall/sql/LitemallGoodsProduct.sql-lib.xml")
public interface LitemallGoodsProductMapper {

    int addStock(@Name("productId") String productId, @Name("num") int num);

    int reduceStock(@Name("productId") String productId, @Name("num") int num);
}
