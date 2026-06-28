
package app.mall.service.entity;

import app.mall.biz.ILitemallGoodsBiz;
import app.mall.biz.ILitemallGoodsProductBiz;
import app.mall.biz.ILitemallOrderGoodsBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.dao.dto.BatchGoodsResultBean;
import app.mall.dao.dto.GoodsExportResultBean;
import app.mall.dao.dto.StockSemanticBean;
import app.mall.dao.dto.StockWarningSkuBean;
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
import io.nop.api.core.beans.WebContentBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.biz.crud.EntityData;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.core.lang.eval.IEvalScope;
import io.nop.core.resource.IResource;
import io.nop.core.resource.ResourceHelper;
import io.nop.core.resource.tpl.ITemplateOutput;
import io.nop.file.core.IFileRecord;
import io.nop.file.core.IFileStore;
import io.nop.orm.IOrmEntityFileStore;
import io.nop.orm.IOrmEntitySet;
import io.nop.ooxml.xlsx.util.ExcelHelper;
import io.nop.report.core.engine.IReportEngine;
import io.nop.xlang.api.XLang;

import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Inject
    ILitemallGoodsProductBiz goodsProductBiz;

    @Inject
    IFileStore fileStore;

    @Inject
    IOrmEntityFileStore ormEntityFileStore;

    @Inject
    IReportEngine reportEngine;

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

    // ===== P36 商品运营工作台：批量运营 + 导入导出 + 库存预警 =====

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public List<BatchGoodsResultBean> batchUpdatePrice(@Name("items") List<Map<String, Object>> items,
                                                        IServiceContext context) {
        if (items == null || items.isEmpty()) {
            throw new NopException(ERR_GOODS_BATCH_EMPTY);
        }
        List<BatchGoodsResultBean> results = new ArrayList<>();
        int rowIndex = 0;
        for (Map<String, Object> item : items) {
            rowIndex++;
            String goodsId = stringValue(item, "goodsId");
            String rawPrice = stringValue(item, "retailPrice");

            if (StringHelper.isEmpty(goodsId) || StringHelper.isEmpty(rawPrice)) {
                results.add(new BatchGoodsResultBean(goodsId, null, false,
                        "数据行 " + rowIndex + " 字段缺失（goodsId/retailPrice 必填）"));
                continue;
            }
            BigDecimal price;
            try {
                price = new BigDecimal(rawPrice.trim());
            } catch (NumberFormatException e) {
                results.add(new BatchGoodsResultBean(goodsId, null, false,
                        "数据行 " + rowIndex + " 价格格式无效"));
                continue;
            }
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                results.add(new BatchGoodsResultBean(goodsId, null, false,
                        "数据行 " + rowIndex + " 价格不可为负"));
                continue;
            }
            try {
                LitemallGoods goods = requireEntity(goodsId, null, context);
                // 直接设置 retailPrice：不经 update() 管道，defaultPrepareUpdate 的 syncRetailPrice 不触发，
                // 运营批量改价语义为「直接改 goods.retailPrice」（与单条 CRUD 的 syncRetailPrice 派生口径隔离）。
                goods.setRetailPrice(price);
                results.add(new BatchGoodsResultBean(goodsId, goods.getGoodsSn(), true, null));
            } catch (NopException e) {
                results.add(new BatchGoodsResultBean(goodsId, null, false,
                        e.getDescription() != null ? e.getDescription() : e.getMessage()));
            } catch (Exception e) {
                results.add(new BatchGoodsResultBean(goodsId, null, false, e.getMessage()));
            }
        }
        logBatchResult("批量改价", results);
        return results;
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public List<BatchGoodsResultBean> batchUpdateStock(@Name("items") List<Map<String, Object>> items,
                                                        IServiceContext context) {
        if (items == null || items.isEmpty()) {
            throw new NopException(ERR_GOODS_BATCH_EMPTY);
        }
        List<BatchGoodsResultBean> results = new ArrayList<>();
        int rowIndex = 0;
        for (Map<String, Object> item : items) {
            rowIndex++;
            String productId = stringValue(item, "productId");
            String rawNumber = stringValue(item, "number");

            if (StringHelper.isEmpty(productId) || StringHelper.isEmpty(rawNumber)) {
                results.add(new BatchGoodsResultBean(null, null, false,
                        "数据行 " + rowIndex + " 字段缺失（productId/number 必填）"));
                continue;
            }
            int number;
            try {
                number = Integer.parseInt(rawNumber.trim());
            } catch (NumberFormatException e) {
                results.add(new BatchGoodsResultBean(null, null, false,
                        "数据行 " + rowIndex + " 库存格式无效"));
                continue;
            }
            if (number < 0) {
                results.add(new BatchGoodsResultBean(null, null, false,
                        "数据行 " + rowIndex + " 库存不可为负"));
                continue;
            }
            try {
                // 跨实体经 ILitemallGoodsProductBiz（既有先例，禁止 daoProvider().daoFor() 绕过）
                LitemallGoodsProduct product = goodsProductBiz.requireEntity(productId, null, context);
                product.setNumber(number);
                results.add(new BatchGoodsResultBean(
                        product.orm_idString(), product.getGoodsId(), true, null));
            } catch (NopException e) {
                results.add(new BatchGoodsResultBean(null, null, false,
                        e.getDescription() != null ? e.getDescription() : e.getMessage()));
            } catch (Exception e) {
                results.add(new BatchGoodsResultBean(null, null, false, e.getMessage()));
            }
        }
        logBatchResult("批量改库存", results);
        return results;
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public List<BatchGoodsResultBean> batchOnSale(@Name("goodsIds") List<String> goodsIds,
                                                   IServiceContext context) {
        return doBatchSale(goodsIds, true, context);
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public List<BatchGoodsResultBean> batchOffSale(@Name("goodsIds") List<String> goodsIds,
                                                    IServiceContext context) {
        return doBatchSale(goodsIds, false, context);
    }

    private List<BatchGoodsResultBean> doBatchSale(List<String> goodsIds, boolean onSale,
                                                   IServiceContext context) {
        if (goodsIds == null || goodsIds.isEmpty()) {
            throw new NopException(ERR_GOODS_BATCH_EMPTY);
        }
        List<BatchGoodsResultBean> results = new ArrayList<>();
        for (String goodsId : goodsIds) {
            try {
                // 复用 onSale/offSale 单行逻辑（含上架 SKU 守卫）
                LitemallGoods goods = onSale
                        ? onSale(goodsId, context)
                        : offSale(goodsId, context);
                results.add(new BatchGoodsResultBean(goodsId, goods.getGoodsSn(), true, null));
            } catch (NopException e) {
                results.add(new BatchGoodsResultBean(goodsId, null, false,
                        e.getDescription() != null ? e.getDescription() : e.getMessage()));
            } catch (Exception e) {
                results.add(new BatchGoodsResultBean(goodsId, null, false, e.getMessage()));
            }
        }
        logBatchResult(onSale ? "批量上架" : "批量下架", results);
        return results;
    }

    @Override
    @BizQuery
    @Auth(roles = "admin")
    public GoodsExportResultBean exportGoods(@Optional @Name("keyword") String keyword,
                                             @Optional @Name("categoryId") String categoryId,
                                             @Optional @Name("brandId") String brandId,
                                             @Optional @Name("isOnSale") Boolean isOnSale,
                                             IServiceContext context) {
        QueryBean query = new QueryBean();
        applyAdminExportFilters(query, keyword, categoryId, brandId, isOnSale);
        query.addOrderField(LitemallGoods.PROP_NAME_addTime, true);
        List<LitemallGoods> list = findList(query, null, context);

        StringBuilder sb = new StringBuilder();
        sb.append("goodsId,goodsSn,name,retailPrice,counterPrice,isOnSale\n");
        for (LitemallGoods g : list) {
            sb.append(csv(g.getId())).append(',')
                    .append(csv(g.getGoodsSn())).append(',')
                    .append(csv(g.getName())).append(',')
                    .append(g.getRetailPrice() != null ? g.getRetailPrice().toPlainString() : "").append(',')
                    .append(g.getCounterPrice() != null ? g.getCounterPrice().toPlainString() : "").append(',')
                    .append(Boolean.TRUE.equals(g.getIsOnSale()) ? "1" : "0").append('\n');
        }
        GoodsExportResultBean result = new GoodsExportResultBean();
        result.setFileName("goods-export.csv");
        result.setCsvContent(sb.toString());
        result.setRowCount(list.size());
        return result;
    }

    @Override
    @BizQuery
    @Auth(roles = "admin")
    public WebContentBean exportGoodsReport(@Name("renderType") String renderType,
                                            @Optional @Name("keyword") String keyword,
                                            @Optional @Name("categoryId") String categoryId,
                                            @Optional @Name("brandId") String brandId,
                                            @Optional @Name("isOnSale") Boolean isOnSale,
                                            IServiceContext context) {
        String safeType = ReportRenderTypes.validate(renderType);
        QueryBean query = new QueryBean();
        applyAdminExportFilters(query, keyword, categoryId, brandId, isOnSale);
        query.addOrderField(LitemallGoods.PROP_NAME_addTime, true);
        List<LitemallGoods> list = findList(query, null, context);

        IEvalScope scope = XLang.newEvalScope();
        scope.setLocalValue("goodsList", list);

        IResource resource = ResourceHelper.getTempResource("rpt");
        try {
            ITemplateOutput output = reportEngine.getRenderer(
                    "/nop/main/report/goods-export.xpt.xml", safeType);
            output.generateToResource(resource, scope);
            String fileName = "goods-export." + safeType;
            return new WebContentBean("application/octet-stream", resource.toFile(), fileName);
        } catch (Exception e) {
            resource.delete();
            throw NopException.adapt(e);
        }
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public List<BatchGoodsResultBean> importGoods(@Name("excelUpload") String excelUpload,
                                                   IServiceContext context) {
        List<Map<String, Object>> rows = parseGoodsImportExcel(excelUpload);
        List<BatchGoodsResultBean> results = new ArrayList<>();
        int rowIndex = 1; // 表头是第 1 行，数据从第 2 行起
        for (Map<String, Object> row : rows) {
            rowIndex++;
            String goodsSn = stringValue(row, "goodsSn");
            String name = stringValue(row, "name");
            String rawRetail = stringValue(row, "retailPrice");

            if (StringHelper.isEmpty(goodsSn) || StringHelper.isEmpty(name)) {
                results.add(new BatchGoodsResultBean(null, goodsSn, false,
                        "数据行 " + rowIndex + " 字段缺失（goodsSn/name 必填）"));
                continue;
            }
            BigDecimal retailPrice = null;
            if (!StringHelper.isEmpty(rawRetail)) {
                try {
                    retailPrice = new BigDecimal(rawRetail.trim());
                } catch (NumberFormatException e) {
                    results.add(new BatchGoodsResultBean(null, goodsSn, false,
                            "数据行 " + rowIndex + " 零售价格式无效"));
                    continue;
                }
            }
            String rawCounter = stringValue(row, "counterPrice");
            BigDecimal counterPrice = null;
            if (!StringHelper.isEmpty(rawCounter)) {
                try {
                    counterPrice = new BigDecimal(rawCounter.trim());
                } catch (NumberFormatException e) {
                    results.add(new BatchGoodsResultBean(null, goodsSn, false,
                            "数据行 " + rowIndex + " 专柜价格式无效"));
                    continue;
                }
            }
            try {
                // 按 goodsSn 定位，存在则更新，不存在则新增
                QueryBean q = new QueryBean();
                q.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_goodsSn, goodsSn));
                LitemallGoods goods = findFirst(q, null, context);
                if (goods == null) {
                    goods = newEntity();
                    goods.setGoodsSn(goodsSn);
                    goods.setName(name);
                    if (retailPrice != null) {
                        goods.setRetailPrice(retailPrice);
                    }
                    if (counterPrice != null) {
                        goods.setCounterPrice(counterPrice);
                    }
                    saveEntity(goods, null, context);
                } else {
                    goods.setName(name);
                    if (retailPrice != null) {
                        goods.setRetailPrice(retailPrice);
                    }
                    if (counterPrice != null) {
                        goods.setCounterPrice(counterPrice);
                    }
                }
                results.add(new BatchGoodsResultBean(goods.orm_idString(), goodsSn, true, null));
            } catch (NopException e) {
                results.add(new BatchGoodsResultBean(null, goodsSn, false,
                        e.getDescription() != null ? e.getDescription() : e.getMessage()));
            } catch (Exception e) {
                results.add(new BatchGoodsResultBean(null, goodsSn, false, e.getMessage()));
            }
        }
        logBatchResult("商品导入", results);
        return results;
    }

    @Override
    @BizQuery
    @Auth(roles = "admin")
    public List<StockWarningSkuBean> getStockWarningList(@Optional @Name("onlyOnSale") Boolean onlyOnSale,
                                                          IServiceContext context) {
        int globalThreshold = resolveStockThreshold(context);
        QueryBean query = new QueryBean();
        if (Boolean.TRUE.equals(onlyOnSale)) {
            query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_isOnSale, true));
        }
        query.addOrderField(LitemallGoods.PROP_NAME_addTime, true);
        List<LitemallGoods> goodsList = findList(query, null, context);

        List<StockWarningSkuBean> result = new ArrayList<>();
        for (LitemallGoods goods : goodsList) {
            IOrmEntitySet<LitemallGoodsProduct> products = goods.getProducts();
            if (products == null) {
                continue;
            }
            Integer goodsSafetyStock = goods.getSafetyStock();
            for (LitemallGoodsProduct product : products) {
                int number = product.getNumber() != null ? product.getNumber() : 0;
                Integer safeStock = product.getSafeStock();
                int threshold;
                String thresholdSource;
                if (safeStock != null && safeStock > 0) {
                    threshold = safeStock;
                    thresholdSource = "safeStock";
                } else if (goodsSafetyStock != null && goodsSafetyStock > 0) {
                    threshold = goodsSafetyStock;
                    thresholdSource = "safetyStock";
                } else {
                    threshold = globalThreshold;
                    thresholdSource = "global";
                }
                if (number <= threshold) {
                    StockWarningSkuBean bean = new StockWarningSkuBean();
                    bean.setGoodsId(goods.getId());
                    bean.setGoodsName(goods.getName());
                    bean.setProductId(product.orm_idString());
                    bean.setSpecifications(product.getSpecifications());
                    bean.setNumber(number);
                    bean.setSafeStock(safeStock);
                    bean.setSafetyStock(goodsSafetyStock);
                    bean.setThreshold(threshold);
                    bean.setThresholdSource(thresholdSource);
                    result.add(bean);
                }
            }
        }
        // 按库存升序
        result.sort(java.util.Comparator.comparingInt(StockWarningSkuBean::getNumber));
        return result;
    }

    // package-private for test access (avoids file-store roundtrip in tests)
    List<Map<String, Object>> parseGoodsImportExcel(String excelUpload) {
        if (excelUpload == null || excelUpload.isEmpty()) {
            throw new NopException(ERR_GOODS_IMPORT_EMPTY);
        }
        String fileId = ormEntityFileStore.decodeFileId(excelUpload);
        if (fileId == null || fileId.isEmpty()) {
            fileId = excelUpload;
        }
        IFileRecord fileRecord = fileStore.getFile(fileId);
        IResource xlsx = fileRecord != null ? fileRecord.getResource() : null;
        if (xlsx == null) {
            throw new NopException(ERR_GOODS_IMPORT_EMPTY);
        }
        try {
            List<Map<String, Object>> rows = ExcelHelper.readSheet(xlsx, null, 0);
            if (rows == null || rows.isEmpty()) {
                throw new NopException(ERR_GOODS_IMPORT_EMPTY);
            }
            return rows;
        } catch (NopException e) {
            throw e;
        } catch (Exception e) {
            throw new NopException(ERR_GOODS_IMPORT_EMPTY);
        }
    }

    private void applyAdminExportFilters(QueryBean query, String keyword, String categoryId,
                                         String brandId, Boolean isOnSale) {
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
        if (isOnSale != null) {
            query.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_isOnSale, isOnSale));
        }
    }

    private void logBatchResult(String action, List<BatchGoodsResultBean> results) {
        int success = (int) results.stream().filter(BatchGoodsResultBean::isSuccess).count();
        logManager.logGeneralSucceed(action,
                "共 " + results.size() + " 行，成功 " + success + " 行");
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String stringValue(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) {
            return null;
        }
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }
}
