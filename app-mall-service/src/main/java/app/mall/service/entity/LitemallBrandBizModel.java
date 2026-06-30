
package app.mall.service.entity;

import app.mall.biz.ILitemallBrandBiz;
import app.mall.dao.entity.LitemallBrand;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

@BizModel("LitemallBrand")
public class LitemallBrandBizModel extends CrudBizModel<LitemallBrand> implements ILitemallBrandBiz {
    public LitemallBrandBizModel() {
        setEntityName(LitemallBrand.class.getName());
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallBrand> frontList(@Name("page") int page,
                                             @Name("pageSize") int pageSize,
                                             IServiceContext context) {
        QueryBean query = new QueryBean();
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallBrand.PROP_NAME_sortOrder, true);

        return findPage(query, null, context);
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public LitemallBrand frontDetail(@Name("id") String id,
                                     IServiceContext context) {
        LitemallBrand brand = get(id, false, context);
        if (brand == null || Boolean.TRUE.equals(brand.getDeleted())) {
            throw new NopException(app.mall.service.AppMallErrors.ERR_BRAND_NOT_FOUND).param("id", id);
        }
        return brand;
    }
}
