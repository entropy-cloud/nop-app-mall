
package app.mall.service.entity;

import app.mall.biz.ILitemallGrouponBiz;
import app.mall.biz.ILitemallGrouponRulesBiz;
import app.mall.dao.entity.LitemallGroupon;
import app.mall.dao.entity.LitemallGrouponRules;
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
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;

import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallGroupon")
public class LitemallGrouponBizModel extends CrudBizModel<LitemallGroupon> implements ILitemallGrouponBiz {

    @Inject
    ILitemallGrouponRulesBiz grouponRulesBiz;

    public LitemallGrouponBizModel() {
        setEntityName(LitemallGroupon.class.getName());
    }

    @Override
    @BizMutation
    public LitemallGroupon openGroupon(@Name("rulesId") String rulesId,
                                       @Name("orderId") String orderId,
                                       IServiceContext context) {
        LitemallGrouponRules rules = grouponRulesBiz.requireEntity(rulesId, null, context);
        if (rules.getStatus() != 0) {
            throw new NopException(ERR_GROUPON_RULES_NOT_AVAILABLE)
                    .param("rulesId", rulesId);
        }
        if (rules.getExpireTime() != null && !rules.getExpireTime().isAfter(LocalDateTime.now())) {
            throw new NopException(ERR_GROUPON_RULES_NOT_AVAILABLE)
                    .param("rulesId", rulesId);
        }

        String userId = context.getUserId();

        LitemallGroupon groupon = newEntity();
        groupon.setRulesId(rulesId);
        groupon.setOrderId(orderId);
        groupon.setGrouponId("0");
        groupon.setUserId(userId);
        groupon.setCreatorUserId(userId);
        groupon.setCreatorUserTime(LocalDateTime.now());
        groupon.setStatus(1);
        saveEntity(groupon, null, context);
        return groupon;
    }

    @Override
    @BizMutation
    public LitemallGroupon joinGroupon(@Name("grouponId") String grouponId,
                                       @Name("orderId") String orderId,
                                       IServiceContext context) {
        LitemallGroupon openGroupon = get(grouponId, false, context);
        if (openGroupon == null) {
            throw new NopException(ERR_GROUPON_NOT_FOUND)
                    .param("grouponId", grouponId);
        }
        if (openGroupon.getStatus() != 1) {
            throw new NopException(ERR_GROUPON_NOT_ACTIVE)
                    .param("grouponId", grouponId);
        }

        String userId = context.getUserId();

        if (userId.equals(openGroupon.getCreatorUserId())) {
            throw new NopException(ERR_GROUPON_CANNOT_JOIN_OWN)
                    .param("grouponId", grouponId);
        }

        QueryBean joinedQuery = new QueryBean();
        joinedQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_grouponId, grouponId));
        joinedQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_userId, userId));
        joinedQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_deleted, false));
        long joinedCount = findCount(joinedQuery, context);
        if (joinedCount > 0) {
            throw new NopException(ERR_GROUPON_ALREADY_JOINED)
                    .param("grouponId", grouponId);
        }

        LitemallGrouponRules rules = grouponRulesBiz.requireEntity(openGroupon.getRulesId(), null, context);

        QueryBean participantsQuery = new QueryBean();
        participantsQuery.addFilter(FilterBeans.or(
                FilterBeans.eq(LitemallGroupon.PROP_NAME_id, grouponId),
                FilterBeans.eq(LitemallGroupon.PROP_NAME_grouponId, grouponId)
        ));
        participantsQuery.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_deleted, false));
        long participantsCount = findCount(participantsQuery, context);
        if (participantsCount >= rules.getDiscountMember()) {
            throw new NopException(ERR_GROUPON_FULL)
                    .param("grouponId", grouponId);
        }

        LitemallGroupon groupon = newEntity();
        groupon.setRulesId(openGroupon.getRulesId());
        groupon.setOrderId(orderId);
        groupon.setGrouponId(grouponId);
        groupon.setUserId(userId);
        groupon.setCreatorUserId(openGroupon.getCreatorUserId());
        groupon.setCreatorUserTime(openGroupon.getCreatorUserTime());
        groupon.setStatus(1);
        saveEntity(groupon, null, context);
        return groupon;
    }

    @Override
    @BizQuery
    public PageBean<LitemallGroupon> myGroupons(@Name("page") int page,
                                                 @Name("pageSize") int pageSize,
                                                 IServiceContext context) {
        String userId = context.getUserId();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_status, 1));
        query.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_deleted, false));

        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallGroupon.PROP_NAME_addTime, true);

        return findPage(query, null, context);
    }

    @Override
    @BizQuery
    public LitemallGroupon grouponDetail(@Name("id") String id, IServiceContext context) {
        LitemallGroupon groupon = requireEntity(id, null, context);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.or(
                FilterBeans.eq(LitemallGroupon.PROP_NAME_id, id),
                FilterBeans.eq(LitemallGroupon.PROP_NAME_grouponId, id)
        ));
        query.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_deleted, false));
        findList(query, null, context);

        return groupon;
    }

    @Override
    @BizMutation
    public int expireGroupons(IServiceContext context) {
        LocalDateTime now = LocalDateTime.now();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_status, 1));
        query.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_deleted, false));
        query.setLimit(500);

        List<LitemallGroupon> activeGroupons = doFindListByQueryDirectly(query, context);
        int count = 0;
        for (LitemallGroupon groupon : activeGroupons) {
            LitemallGrouponRules rules = grouponRulesBiz.get(groupon.getRulesId(), false, context);
            if (rules != null && rules.getExpireTime() != null && !rules.getExpireTime().isAfter(now)) {
                groupon.setStatus(2);
                count++;
            }
        }
        return count;
    }
}
