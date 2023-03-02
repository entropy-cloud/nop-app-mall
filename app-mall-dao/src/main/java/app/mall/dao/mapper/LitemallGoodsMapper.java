package app.mall.dao.mapper;

import app.mall.dao.entity.LitemallGoodsProduct;
import io.nop.api.core.annotations.orm.SqlLibMapper;

@SqlLibMapper("/app/mall/sql/LitemallGoods.sql-lib.xml")
public interface LitemallGoodsMapper {

    void syncCartProduct(LitemallGoodsProduct product);
}
