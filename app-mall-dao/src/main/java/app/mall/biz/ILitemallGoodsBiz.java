
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.PageBean;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallGoods;

public interface ILitemallGoodsBiz extends ICrudBiz<LitemallGoods>{

    @BizMutation
    LitemallGoods onSale(@Name("id") String id);

    @BizMutation
    LitemallGoods offSale(@Name("id") String id);

    @BizQuery
    PageBean<LitemallGoods> frontList(@Name("categoryId") String categoryId,
                                      @Name("brandId") String brandId,
                                      @Name("page") int page,
                                      @Name("pageSize") int pageSize);

    @BizQuery
    LitemallGoods frontDetail(@Name("id") String id);

    @BizQuery
    PageBean<LitemallGoods> search(@Name("keyword") String keyword,
                                   @Name("categoryId") String categoryId,
                                   @Name("brandId") String brandId,
                                   @Name("sortBy") String sortBy,
                                   @Name("page") int page,
                                   @Name("pageSize") int pageSize);

    @BizQuery
    PageBean<LitemallGoods> adminSearch(@Name("keyword") String keyword,
                                        @Name("categoryId") String categoryId,
                                        @Name("brandId") String brandId,
                                        @Name("sortBy") String sortBy,
                                        @Name("page") int page,
                                        @Name("pageSize") int pageSize);
}
