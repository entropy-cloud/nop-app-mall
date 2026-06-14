package app.mall.service.entity;

import app.mall.biz.ILitemallGoodsBiz;
import app.mall.biz.ILitemallTopicBiz;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallTopic;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static app.mall.service.AppMallErrors.ERR_GOODS_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_TOPIC_NOT_FOUND;

@BizModel("LitemallTopic")
public class LitemallTopicBizModel extends CrudBizModel<LitemallTopic> implements ILitemallTopicBiz {

    @Inject
    ILitemallGoodsBiz goodsBiz;

    public LitemallTopicBizModel() {
        setEntityName(LitemallTopic.class.getName());
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallTopic> frontList(@Name("page") int page,
                                              @Name("pageSize") int pageSize,
                                              IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallTopic.PROP_NAME_deleted, false));
        query.addFilter(FilterBeans.eq("status", 0));
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallTopic.PROP_NAME_sortOrder, true);

        return findPage(query, null, context);
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public LitemallTopic frontDetail(@Name("id") String id,
                                      IServiceContext context) {
        LitemallTopic topic = get(id, false, context);
        if (topic == null || Boolean.TRUE.equals(topic.getDeleted())) {
            throw new NopException(ERR_TOPIC_NOT_FOUND).param("id", id);
        }
        return topic;
    }

    @Override
    @BizMutation
    public LitemallTopic onShelf(@Name("id") String id, IServiceContext context) {
        LitemallTopic topic = requireEntity(id, null, context);
        topic.orm_propValueByName("status", 0);
        updateEntity(topic, "onShelf", context);
        return topic;
    }

    @Override
    @BizMutation
    public LitemallTopic offShelf(@Name("id") String id, IServiceContext context) {
        LitemallTopic topic = requireEntity(id, null, context);
        topic.orm_propValueByName("status", 1);
        updateEntity(topic, "offShelf", context);
        return topic;
    }
}
