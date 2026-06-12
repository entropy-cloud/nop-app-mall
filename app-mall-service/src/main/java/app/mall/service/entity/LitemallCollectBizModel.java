package app.mall.service.entity;

import app.mall.biz.ILitemallCollectBiz;
import app.mall.dao.entity.LitemallCollect;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallCollect")
public class LitemallCollectBizModel extends CrudBizModel<LitemallCollect> implements ILitemallCollectBiz {

    public LitemallCollectBizModel() {
        setEntityName(LitemallCollect.class.getName());
    }

    @Override
    @BizMutation
    public LitemallCollect addCollect(@Name("type") int type,
                                       @Name("valueId") String valueId,
                                       IServiceContext context) {
        String userId = context.getUserId();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_type, type));
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_valueId, valueId));
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_deleted, false));
        LitemallCollect existing = findFirst(query, null, context);
        if (existing != null) {
            throw new NopException(ERR_COLLECT_ALREADY_EXISTS)
                    .param("type", type)
                    .param("valueId", valueId);
        }

        LitemallCollect collect = newEntity();
        collect.setUserId(userId);
        collect.setType(type);
        collect.setValueId(valueId);
        saveEntity(collect, null, context);
        return collect;
    }

    @Override
    @BizMutation
    public void removeCollect(@Name("type") int type,
                               @Name("valueId") String valueId,
                               IServiceContext context) {
        String userId = context.getUserId();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_type, type));
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_valueId, valueId));
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_deleted, false));
        LitemallCollect existing = findFirst(query, null, context);
        if (existing == null) {
            throw new NopException(ERR_COLLECT_NOT_FOUND)
                    .param("type", type)
                    .param("valueId", valueId);
        }

        delete(existing.orm_idString(), context);
    }

    @Override
    @BizQuery
    public boolean isCollect(@Name("type") int type,
                              @Name("valueId") String valueId,
                              IServiceContext context) {
        String userId = context.getUserId();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_type, type));
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_valueId, valueId));
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_deleted, false));
        return findCount(query, context) > 0;
    }

    @Override
    @BizQuery
    public PageBean<LitemallCollect> listByType(@Name("type") int type,
                                                 @Name("page") int page,
                                                 @Name("pageSize") int pageSize,
                                                 IServiceContext context) {
        String userId = context.getUserId();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_type, type));
        query.addFilter(FilterBeans.eq(LitemallCollect.PROP_NAME_deleted, false));
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallCollect.PROP_NAME_addTime, true);

        return findPage(query, null, context);
    }
}
