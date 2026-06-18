package app.mall.service.entity;

import app.mall.biz.ILitemallAdBiz;
import app.mall.dao.entity.LitemallAd;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

import java.time.LocalDateTime;

@BizModel("LitemallAd")
public class LitemallAdBizModel extends CrudBizModel<LitemallAd> implements ILitemallAdBiz {

    public LitemallAdBizModel() {
        setEntityName(LitemallAd.class.getName());
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallAd> listActiveAds(@Name("page") int page,
                                               @Name("pageSize") int pageSize,
                                               IServiceContext context) {
        LocalDateTime now = LocalDateTime.now();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallAd.PROP_NAME_enabled, true));
        query.addFilter(FilterBeans.le(LitemallAd.PROP_NAME_startTime, now));
        query.addFilter(FilterBeans.ge(LitemallAd.PROP_NAME_endTime, now));
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);

        return findPage(query, null, context);
    }
}
