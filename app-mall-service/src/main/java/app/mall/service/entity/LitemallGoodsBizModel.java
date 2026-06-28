
package app.mall.service.entity;

import app.mall.biz.ILitemallGoodsBiz;
import app.mall.biz.ILitemallOrderGoodsBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.dao.dto.StockSemanticBean;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.manager.MallLogManager;
import app.mall.dao.mapper.LitemallGoodsMapper;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.TreeBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.biz.crud.EntityData;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.orm.IOrmEntitySet;

import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static io.nop.api.core.beans.FilterBeans.contains;
import static io.nop.api.core.beans.FilterBeans.or;
import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallGoods")
public class LitemallGoodsBizModel extends CrudBizModel<LitemallGoods> implements ILitemallGoodsBiz {

    // P38 库存语义化阈值/文案配置键（存于 LitemallSystem，复用 ILitemallSystemBiz.getConfig + 代码兜底默认）。
    public static final String CONFIG_STOCK_THRESHOLD_TIGHT = "mall_stock_threshold_tight";
    public static final String CONFIG_STOCK_LABEL_SUFFICIENT = "mall_stock_label_sufficient";
    public static final String CONFIG_STOCK_LABEL_TIGHT = "mall_stock_label_tight";
    public static final String CONFIG_STOCK_LABEL_OUT = "mall_stock_label_out";
    public static final String CONFIG_STOCK_COLOR_SUFFICIENT = "mall_stock_color_sufficient";
    public static final String CONFIG_STOCK_COLOR_TIGHT = "mall_stock_color_tight";
    public static final String CONFIG_STOCK_COLOR_OUT = "mall_stock_color_out";

    // 默认：充足 > 10；紧张 1-10（含边界）；缺货 = 0。文案与色值可后台覆盖。
    public static final int DEFAULT_STOCK_THRESHOLD_TIGHT = 10;
    public static final String DEFAULT_STOCK_LABEL_SUFFICIENT = "库存充足";
    public static final String DEFAULT_STOCK_LABEL_TIGHT = "仅剩 {n}";
    public static final String DEFAULT_STOCK_LABEL_OUT = "已售罄";
    public static final String DEFAULT_STOCK_COLOR_SUFFICIENT = "#17a2b8";
    public static final String DEFAULT_STOCK_COLOR_TIGHT = "#dc3545";
    public static final String DEFAULT_STOCK_COLOR_OUT = "#999999";

    @Inject
    LitemallGoodsMapper goodsMapper;

    @Inject
    MallLogManager logManager;

    @Inject
    ILitemallOrderGoodsBiz orderGoodsBiz;

    @Inject
    ILitemallSystemBiz systemBiz;

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
    protected void defaultPrepareDelete(LitemallGoods entity, IServiceContext context) {
        // Goods is shared by orders/carts/collects/footprint/comments. Deleting a goods that has
        // historical order-goods rows would break order snapshots and sales statistics. The
        // goods→orderGoods cascadeDelete has been removed from the ORM model, so this guard is the
        // hard protection: refuse delete if any non-deleted order-goods references this goods.
        // Operators must use offSale() to retire a goods instead.
        QueryBean orderGoodsQuery = new QueryBean();
        orderGoodsQuery.addFilter(FilterBeans.eq(LitemallOrderGoods.PROP_NAME_goodsId, entity.getId()));
        long orderGoodsCount = orderGoodsBiz.findCount(orderGoodsQuery, context);
        if (orderGoodsCount > 0) {
            throw new NopException(ERR_GOODS_HAS_ORDER_HISTORY)
                    .param("goodsId", entity.getId())
                    .param("orderGoodsCount", orderGoodsCount);
        }
    }

    @Override
    @BizMutation
    public LitemallGoods onSale(@Name("id") String id, IServiceContext context) {
        LitemallGoods goods = requireEntity(id, null, context);
        if (goods.getProducts() == null || goods.getProducts().isEmpty()) {
            throw new NopException(ERR_GOODS_NO_SKU)
                    .param("goodsId", id);
        }
        goods.setIsOnSale(true);
        logManager.logGeneralSucceed("商品上架", "商品编号 " + goods.getGoodsSn());
        return goods;
    }

    @Override
    @BizMutation
    public LitemallGoods offSale(@Name("id") String id, IServiceContext context) {
        LitemallGoods goods = requireEntity(id, null, context);
        goods.setIsOnSale(false);
        logManager.logGeneralSucceed("商品下架", "商品编号 " + goods.getGoodsSn());
        return goods;
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallGoods> frontList(@Optional @Name("categoryId") String categoryId,
                                              @Optional @Name("brandId") String brandId,
                                              @Name("page") int page,
                                              @Name("pageSize") int pageSize,
                                              IServiceContext context) {
        return frontListByFlags(null, null, null, categoryId, brandId, page, pageSize, context);
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallGoods> frontListByFlags(@Optional @Name("isHot") Boolean isHot,
                                                     @Optional @Name("isNew") Boolean isNew,
                                                     @Optional @Name("isRecommend") Boolean isRecommend,
                                                     @Optional @Name("categoryId") String categoryId,
                                                     @Optional @Name("brandId") String brandId,
                                                     @Name("page") int page,
                                                     @Name("pageSize") int pageSize,
                                                     IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_isOnSale, true));

        if (isHot != null) {
            query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_isHot, isHot));
        }
        if (isNew != null) {
            query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_isNew, isNew));
        }
        if (isRecommend != null) {
            query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_isRecommend, isRecommend));
        }

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

        return findPage(query, null, context);
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public LitemallGoods frontDetail(@Name("id") String id, IServiceContext context) {
        LitemallGoods goods = get(id, false, context);
        if (goods == null || !Boolean.TRUE.equals(goods.getIsOnSale())) {
            throw new NopException(ERR_GOODS_NOT_ON_SALE)
                    .param("goodsId", id);
        }
        return goods;
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public StockSemanticBean getStockSemantic(@Name("goodsId") String goodsId, IServiceContext context) {
        LitemallGoods goods = get(goodsId, false, context);
        if (goods == null) {
            throw new NopException(ERR_GOODS_NOT_FOUND).param("goodsId", goodsId);
        }

        // 聚合商品下全部 SKU 的可用库存（求和，Decision A：求和更贴合「该商品是否还可买」的用户心智）。
        IOrmEntitySet<LitemallGoodsProduct> products = goods.getProducts();
        int totalStock = 0;
        if (products != null) {
            for (LitemallGoodsProduct product : products) {
                if (product.getNumber() != null) {
                    totalStock += product.getNumber();
                }
            }
        }

        int tightThreshold = resolveStockThreshold(context);

        StockSemanticBean result = new StockSemanticBean();
        result.setStockNumber(totalStock);
        if (totalStock <= 0) {
            result.setLevel(StockSemanticBean.LEVEL_OUT);
            result.setLabel(resolveStockText(CONFIG_STOCK_LABEL_OUT, DEFAULT_STOCK_LABEL_OUT, totalStock, context));
            result.setColor(resolveStockText(CONFIG_STOCK_COLOR_OUT, DEFAULT_STOCK_COLOR_OUT, totalStock, context));
        } else if (totalStock <= tightThreshold) {
            result.setLevel(StockSemanticBean.LEVEL_TIGHT);
            result.setLabel(resolveStockText(CONFIG_STOCK_LABEL_TIGHT, DEFAULT_STOCK_LABEL_TIGHT, totalStock, context));
            result.setColor(resolveStockText(CONFIG_STOCK_COLOR_TIGHT, DEFAULT_STOCK_COLOR_TIGHT, totalStock, context));
        } else {
            result.setLevel(StockSemanticBean.LEVEL_SUFFICIENT);
            result.setLabel(resolveStockText(CONFIG_STOCK_LABEL_SUFFICIENT, DEFAULT_STOCK_LABEL_SUFFICIENT, totalStock, context));
            result.setColor(resolveStockText(CONFIG_STOCK_COLOR_SUFFICIENT, DEFAULT_STOCK_COLOR_SUFFICIENT, totalStock, context));
        }
        return result;
    }

    private int resolveStockThreshold(IServiceContext context) {
        String raw = systemBiz.getConfig(CONFIG_STOCK_THRESHOLD_TIGHT, context);
        if (StringHelper.isEmpty(raw)) {
            return DEFAULT_STOCK_THRESHOLD_TIGHT;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed >= 0 ? parsed : DEFAULT_STOCK_THRESHOLD_TIGHT;
        } catch (NumberFormatException e) {
            return DEFAULT_STOCK_THRESHOLD_TIGHT;
        }
    }

    private String resolveStockText(String key, String defaultValue, int stockNumber, IServiceContext context) {
        String raw = systemBiz.getConfig(key, context);
        String tpl = StringHelper.isEmpty(raw) ? defaultValue : raw;
        // 紧张档位文案支持 {n} 占位符替换为实际聚合库存（充足/缺货档位通常不含 {n}，替换无副作用）。
        return tpl.replace("{n}", String.valueOf(stockNumber));
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallGoods> search(@Optional @Name("keyword") String keyword,
                                          @Optional @Name("categoryId") String categoryId,
                                          @Optional @Name("brandId") String brandId,
                                          @Optional @Name("sortBy") String sortBy,
                                          @Name("page") int page,
                                          @Name("pageSize") int pageSize,
                                          IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_isOnSale, true));

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

        return findPage(query, null, context);
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
