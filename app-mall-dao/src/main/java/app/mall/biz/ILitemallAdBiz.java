package app.mall.biz;

import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallAd;

public interface ILitemallAdBiz extends ICrudBiz<LitemallAd> {

    @BizQuery
    PageBean<LitemallAd> listActiveAds(@Name("page") int page,
                                       @Name("pageSize") int pageSize,
                                       IServiceContext context);
}
