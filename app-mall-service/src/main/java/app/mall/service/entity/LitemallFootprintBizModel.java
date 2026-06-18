package app.mall.service.entity;

import app.mall.biz.ILitemallFootprintBiz;
import app.mall.dao.entity.LitemallFootprint;
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

@BizModel("LitemallFootprint")
public class LitemallFootprintBizModel extends CrudBizModel<LitemallFootprint> implements ILitemallFootprintBiz {

    public LitemallFootprintBizModel() {
        setEntityName(LitemallFootprint.class.getName());
    }

    @Override
    @BizMutation
    public void recordFootprint(@Name("goodsId") String goodsId,
                                 IServiceContext context) {
        String userId = context.getUserId();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallFootprint.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallFootprint.PROP_NAME_goodsId, goodsId));
        query.addFilter(FilterBeans.dateTimeBetween(LitemallFootprint.PROP_NAME_addTime, todayStart, todayEnd));

        LitemallFootprint existing = findFirst(query, null, context);
        if (existing != null) {
            existing.setAddTime(LocalDateTime.now());
            saveEntity(existing, null, context);
            return;
        }

        LitemallFootprint footprint = newEntity();
        footprint.setUserId(userId);
        footprint.setGoodsId(goodsId);
        saveEntity(footprint, null, context);
    }

    @Override
    @BizQuery
    public PageBean<LitemallFootprint> listFootprints(@Name("page") int page,
                                                       @Name("pageSize") int pageSize,
                                                       IServiceContext context) {
        String userId = context.getUserId();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallFootprint.PROP_NAME_userId, userId));
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallFootprint.PROP_NAME_addTime, true);

        return findPage(query, null, context);
    }

    @Override
    @BizMutation
    public void clearFootprints(IServiceContext context) {
        String userId = context.getUserId();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallFootprint.PROP_NAME_userId, userId));

        for (LitemallFootprint fp : findList(query, null, context)) {
            delete(fp.orm_idString(), context);
        }
    }
}
