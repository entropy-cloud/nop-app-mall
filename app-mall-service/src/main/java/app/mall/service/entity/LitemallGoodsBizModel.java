
package app.mall.service.entity;

import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.mapper.LitemallGoodsMapper;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.TreeBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallGoodsBiz;
import io.nop.biz.crud.EntityData;
import io.nop.core.context.IServiceContext;

import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static io.nop.api.core.beans.FilterBeans.contains;
import static io.nop.api.core.beans.FilterBeans.or;
import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallGoods")
public class LitemallGoodsBizModel extends CrudBizModel<LitemallGoods> implements ILitemallGoodsBiz {

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

    @Override
    @BizMutation
    public LitemallGoods onSale(@Name("id") String id) {
        LitemallGoods goods = requireEntity(id, null, null);
        if (goods.getProducts() == null || goods.getProducts().isEmpty()) {
            throw new NopException(ERR_GOODS_NO_SKU)
                    .param("goodsId", id);
        }
        goods.setIsOnSale(true);
        return goods;
    }

    @Override
    @BizMutation
    public LitemallGoods offSale(@Name("id") String id) {
        LitemallGoods goods = requireEntity(id, null, null);
        goods.setIsOnSale(false);
        return goods;
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallGoods> frontList(@Name("categoryId") String categoryId,
                                             @Name("brandId") String brandId,
                                             @Name("page") int page,
                                             @Name("pageSize") int pageSize) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_isOnSale, true));
        query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_deleted, false));

        if (categoryId != null && !categoryId.isEmpty()) {
            query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_categoryId, categoryId));
        }
        if (brandId != null && !brandId.isEmpty()) {
            query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_brandId, brandId));
        }

        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallGoods.PROP_NAME_sortOrder, true);
        query.addOrderField(LitemallGoods.PROP_NAME_addTime, true);

        return findPage(query, null, null);
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public LitemallGoods frontDetail(@Name("id") String id) {
        LitemallGoods goods = get(id, false, null);
        if (goods == null || !Boolean.TRUE.equals(goods.getIsOnSale())) {
            throw new NopException(ERR_GOODS_NOT_ON_SALE)
                    .param("goodsId", id);
        }
        return goods;
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallGoods> search(@Name("keyword") String keyword,
                                          @Name("categoryId") String categoryId,
                                          @Name("brandId") String brandId,
                                          @Name("sortBy") String sortBy,
                                          @Name("page") int page,
                                          @Name("pageSize") int pageSize) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_isOnSale, true));
        query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_deleted, false));

        if (keyword != null && !keyword.isEmpty()) {
            query.addFilter(or(
                    contains(LitemallGoods.PROP_NAME_name, keyword),
                    contains(LitemallGoods.PROP_NAME_goodsSn, keyword)
            ));
        }
        if (categoryId != null && !categoryId.isEmpty()) {
            query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_categoryId, categoryId));
        }
        if (brandId != null && !brandId.isEmpty()) {
            query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_brandId, brandId));
        }

        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);

        if ("price".equals(sortBy)) {
            query.addOrderField(LitemallGoods.PROP_NAME_retailPrice, false);
        } else if ("new".equals(sortBy)) {
            query.addOrderField(LitemallGoods.PROP_NAME_addTime, true);
        } else {
            query.addOrderField(LitemallGoods.PROP_NAME_sortOrder, true);
            query.addOrderField(LitemallGoods.PROP_NAME_addTime, true);
        }

        return findPage(query, null, null);
    }

    @Override
    @BizQuery
    public PageBean<LitemallGoods> adminSearch(@Name("keyword") String keyword,
                                               @Name("categoryId") String categoryId,
                                               @Name("brandId") String brandId,
                                               @Name("sortBy") String sortBy,
                                               @Name("page") int page,
                                               @Name("pageSize") int pageSize) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_deleted, false));

        if (keyword != null && !keyword.isEmpty()) {
            query.addFilter(or(
                    contains(LitemallGoods.PROP_NAME_name, keyword),
                    contains(LitemallGoods.PROP_NAME_goodsSn, keyword)
            ));
        }
        if (categoryId != null && !categoryId.isEmpty()) {
            query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_categoryId, categoryId));
        }
        if (brandId != null && !brandId.isEmpty()) {
            query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_brandId, brandId));
        }

        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);

        if ("price".equals(sortBy)) {
            query.addOrderField(LitemallGoods.PROP_NAME_retailPrice, false);
        } else if ("new".equals(sortBy)) {
            query.addOrderField(LitemallGoods.PROP_NAME_addTime, true);
        } else {
            query.addOrderField(LitemallGoods.PROP_NAME_sortOrder, true);
            query.addOrderField(LitemallGoods.PROP_NAME_addTime, true);
        }

        return findPage(query, null, null);
    }
}
