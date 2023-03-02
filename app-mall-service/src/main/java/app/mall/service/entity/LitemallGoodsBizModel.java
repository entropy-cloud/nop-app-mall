
package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.mapper.LitemallGoodsMapper;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.beans.TreeBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.biz.crud.EntityData;
import io.nop.core.context.IServiceContext;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static io.nop.api.core.beans.FilterBeans.contains;
import static io.nop.api.core.beans.FilterBeans.or;

@BizModel("LitemallGoods")
public class LitemallGoodsBizModel extends CrudBizModel<LitemallGoods> {

    @Inject
    LitemallGoodsMapper goodsMapper;

    public LitemallGoodsBizModel() {
        setEntityName(LitemallGoods.class.getName());
    }

    @Override
    protected void defaultPrepareQuery(QueryBean query, IServiceContext context) {
        TreeBean filter = query.getFilter();
        if (filter != null) {
            TreeBean keywordsFilter = filter.childWithAttr("name", LitemallGoods.PROP_NAME_keywords);
            if (keywordsFilter != null) {
                Object value = keywordsFilter.getAttr("value");
                TreeBean orCond = or(contains(LitemallGoods.PROP_NAME_name, value), contains(LitemallGoods.PROP_NAME_keywords, value));
                filter.replaceChild(keywordsFilter, orCond);
            }
        }
    }

    @Override
    protected void defaultPrepareSave(EntityData<LitemallGoods> entityData, IServiceContext context) {
        entityData.getEntity().syncRetailPrice();
    }

    @Override
    protected void defaultPrepareUpdate(EntityData<LitemallGoods> entityData, IServiceContext context) {

        LitemallGoods goods = entityData.getEntity();
        goods.syncRetailPrice();

        // 收集发生变化的product列表
        List<LitemallGoodsProduct> changedProducts = goods.getProducts().stream().filter(LitemallGoodsProduct::orm_dirty)
                .collect(Collectors.toList());

        // 提交修改数据到数据库中
        orm().flushSession();

        // 调用sql语句同步购物车中冗余的产品属性

        // 这里需要注意的是购物车litemall_cart有些字段是拷贝商品的一些字段，因此需要及时更新
        // 目前这些字段是goods_sn, goods_name, price, pic_url
        for (LitemallGoodsProduct product : changedProducts) {
            goodsMapper.syncCartProduct(product);
        }
    }
}
