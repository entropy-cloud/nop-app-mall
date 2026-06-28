
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.util.List;
import java.util.Map;

import app.mall.dao.dto.BatchGoodsResultBean;
import app.mall.dao.dto.GoodsExportResultBean;
import app.mall.dao.dto.StockSemanticBean;
import app.mall.dao.dto.StockWarningSkuBean;
import app.mall.dao.entity.LitemallGoods;

public interface ILitemallGoodsBiz extends ICrudBiz<LitemallGoods>{

    @BizMutation
    LitemallGoods onSale(@Name("id") String id, IServiceContext context);

    @BizMutation
    LitemallGoods offSale(@Name("id") String id, IServiceContext context);

    @BizQuery
    PageBean<LitemallGoods> frontList(@Optional @Name("categoryId") String categoryId,
                                      @Optional @Name("brandId") String brandId,
                                      @Name("page") int page,
                                      @Name("pageSize") int pageSize,
                                      IServiceContext context);

    @BizQuery
    PageBean<LitemallGoods> frontListByFlags(@Optional @Name("isHot") Boolean isHot,
                                             @Optional @Name("isNew") Boolean isNew,
                                             @Optional @Name("isRecommend") Boolean isRecommend,
                                             @Optional @Name("categoryId") String categoryId,
                                             @Optional @Name("brandId") String brandId,
                                             @Name("page") int page,
                                             @Name("pageSize") int pageSize,
                                             IServiceContext context);

    @BizQuery
    LitemallGoods frontDetail(@Name("id") String id, IServiceContext context);

    @BizQuery
    StockSemanticBean getStockSemantic(@Name("goodsId") String goodsId, IServiceContext context);

    @BizQuery
    PageBean<LitemallGoods> search(@Optional @Name("keyword") String keyword,
                                   @Optional @Name("categoryId") String categoryId,
                                   @Optional @Name("brandId") String brandId,
                                   @Optional @Name("sortBy") String sortBy,
                                   @Name("page") int page,
                                   @Name("pageSize") int pageSize,
                                   IServiceContext context);

    @BizQuery
    PageBean<LitemallGoods> adminSearch(@Name("keyword") String keyword,
                                        @Name("categoryId") String categoryId,
                                        @Name("brandId") String brandId,
                                        @Name("sortBy") String sortBy,
                                        @Name("page") int page,
                                        @Name("pageSize") int pageSize);

    @BizMutation
    List<BatchGoodsResultBean> batchUpdatePrice(@Name("items") List<Map<String, Object>> items,
                                                IServiceContext context);

    @BizMutation
    List<BatchGoodsResultBean> batchUpdateStock(@Name("items") List<Map<String, Object>> items,
                                                IServiceContext context);

    @BizMutation
    List<BatchGoodsResultBean> batchOnSale(@Name("goodsIds") List<String> goodsIds,
                                           IServiceContext context);

    @BizMutation
    List<BatchGoodsResultBean> batchOffSale(@Name("goodsIds") List<String> goodsIds,
                                            IServiceContext context);

    @BizQuery
    GoodsExportResultBean exportGoods(@Optional @Name("keyword") String keyword,
                                      @Optional @Name("categoryId") String categoryId,
                                      @Optional @Name("brandId") String brandId,
                                      @Optional @Name("isOnSale") Boolean isOnSale,
                                      IServiceContext context);

    @BizMutation
    List<BatchGoodsResultBean> importGoods(@Name("excelUpload") String excelUpload,
                                           IServiceContext context);

    @BizQuery
    List<StockWarningSkuBean> getStockWarningList(@Optional @Name("onlyOnSale") Boolean onlyOnSale,
                                                  IServiceContext context);
}

