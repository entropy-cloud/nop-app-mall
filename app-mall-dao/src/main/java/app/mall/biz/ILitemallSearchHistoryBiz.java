package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallSearchHistory;

public interface ILitemallSearchHistoryBiz extends ICrudBiz<LitemallSearchHistory> {

    @BizMutation
    void recordSearch(@Name("keyword") String keyword,
                      @Name("from") String from,
                      IServiceContext context);

    @BizQuery
    PageBean<LitemallSearchHistory> listSearchHistory(@Name("page") int page,
                                                      @Name("pageSize") int pageSize,
                                                      IServiceContext context);

    @BizMutation
    void clearSearchHistory(IServiceContext context);
}
