
package app.mall.service.entity;

import app.mall.biz.ILitemallGoodsBiz;
import app.mall.biz.ILitemallGrouponRulesBiz;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGrouponRules;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallGrouponRules")
public class LitemallGrouponRulesBizModel extends CrudBizModel<LitemallGrouponRules> implements ILitemallGrouponRulesBiz {

    @Inject
    ILitemallGoodsBiz goodsBiz;

    public LitemallGrouponRulesBizModel() {
        setEntityName(LitemallGrouponRules.class.getName());
    }

    @Override
    @BizMutation
    public LitemallGrouponRules publishRules(@Name("id") String id, IServiceContext context) {
        LitemallGrouponRules rules = requireEntity(id, null, context);
        LitemallGoods goods = goodsBiz.get(rules.getGoodsId(), false, context);
        if (goods == null || !Boolean.TRUE.equals(goods.getIsOnSale())) {
            throw new NopException(ERR_GROUPON_RULES_GOODS_NOT_ON_SALE)
                    .param("goodsId", rules.getGoodsId());
        }
        rules.setStatus(0);
        return rules;
    }

    @Override
    @BizMutation
    public LitemallGrouponRules unpublishRules(@Name("id") String id, IServiceContext context) {
        LitemallGrouponRules rules = requireEntity(id, null, context);
        rules.setStatus(2);
        return rules;
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallGrouponRules> listAvailableRules(@Name("page") int page,
                                                              @Name("pageSize") int pageSize,
                                                              IServiceContext context) {
        LocalDateTime now = LocalDateTime.now();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallGrouponRules.PROP_NAME_status, 0));
        query.addFilter(FilterBeans.gt(LitemallGrouponRules.PROP_NAME_expireTime, now));
        query.addFilter(FilterBeans.eq(LitemallGrouponRules.PROP_NAME_deleted, false));

        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallGrouponRules.PROP_NAME_addTime, true);

        return doFindPageByQueryDirectly(query, null, context);
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public LitemallGrouponRules getAvailableRulesById(@Name("id") String id,
                                                       @Optional @Name("strict") Boolean strict,
                                                       IServiceContext context) {
        LitemallGrouponRules rules = get(id, false, context);
        if (rules == null || Boolean.TRUE.equals(rules.getDeleted())) {
            throw new NopException(ERR_GROUPON_RULES_NOT_FOUND)
                    .param("rulesId", id);
        }

        LocalDateTime now = LocalDateTime.now();
        boolean available = rules.getStatus() != null && rules.getStatus() == 0
                && rules.getExpireTime() != null
                && rules.getExpireTime().isAfter(now);
        if (!available && Boolean.TRUE.equals(strict)) {
            throw new NopException(ERR_GROUPON_RULES_NOT_AVAILABLE)
                    .param("rulesId", id);
        }

        return rules;
    }
}
