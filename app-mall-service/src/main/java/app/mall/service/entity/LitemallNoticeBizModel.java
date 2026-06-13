
package app.mall.service.entity;

import app.mall.biz.ILitemallNoticeBiz;
import app.mall.dao.entity.LitemallNotice;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

@BizModel("LitemallNotice")
public class LitemallNoticeBizModel extends CrudBizModel<LitemallNotice> implements ILitemallNoticeBiz {
    public LitemallNoticeBizModel() {
        setEntityName(LitemallNotice.class.getName());
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallNotice> listNotices(@Name("page") int page,
                                                 @Name("pageSize") int pageSize,
                                                 IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallNotice.PROP_NAME_deleted, false));
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallNotice.PROP_NAME_addTime, true);

        return findPage(query, null, context);
    }
}
