package app.mall.service.entity;

import app.mall.biz.ILitemallSearchHistoryBiz;
import app.mall.dao.entity.LitemallSearchHistory;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@BizModel("LitemallSearchHistory")
public class LitemallSearchHistoryBizModel extends CrudBizModel<LitemallSearchHistory> implements ILitemallSearchHistoryBiz {

    public LitemallSearchHistoryBizModel() {
        setEntityName(LitemallSearchHistory.class.getName());
    }

    @Override
    @BizMutation
    public void recordSearch(@Name("keyword") String keyword,
                              @Name("from") String from,
                              IServiceContext context) {
        String userId = context.getUserId();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallSearchHistory.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallSearchHistory.PROP_NAME_keyword, keyword));
        query.addFilter(FilterBeans.dateTimeBetween(LitemallSearchHistory.PROP_NAME_addTime, todayStart, todayEnd));

        LitemallSearchHistory existing = findFirst(query, null, context);
        if (existing != null) {
            existing.setAddTime(LocalDateTime.now());
            saveEntity(existing, null, context);
            return;
        }

        LitemallSearchHistory history = newEntity();
        history.setUserId(userId);
        history.setKeyword(keyword);
        history.setFrom(from);
        saveEntity(history, null, context);
    }

    @Override
    @BizQuery
    public PageBean<LitemallSearchHistory> listSearchHistory(@Name("page") int page,
                                                              @Name("pageSize") int pageSize,
                                                              IServiceContext context) {
        String userId = context.getUserId();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallSearchHistory.PROP_NAME_userId, userId));
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallSearchHistory.PROP_NAME_addTime, true);

        return findPage(query, null, context);
    }

    @Override
    @BizMutation
    public void clearSearchHistory(IServiceContext context) {
        String userId = context.getUserId();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallSearchHistory.PROP_NAME_userId, userId));

        for (LitemallSearchHistory h : findList(query, null, context)) {
            delete(h.orm_idString(), context);
        }
    }
}
