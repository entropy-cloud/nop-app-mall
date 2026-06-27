
package app.mall.service.entity;

import app.mall.biz.ILitemallPointsFlowBiz;
import app.mall.dao.entity.LitemallPointsFlow;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;

import java.util.List;

@BizModel("LitemallPointsFlow")
public class LitemallPointsFlowBizModel extends CrudBizModel<LitemallPointsFlow> implements ILitemallPointsFlowBiz {
    public LitemallPointsFlowBizModel(){
        setEntityName(LitemallPointsFlow.class.getName());
    }

    @Override
    @BizQuery
    public List<LitemallPointsFlow> getMyPointsFlows(@Optional @Name("changeType") Integer changeType,
                                                     @Optional @Name("sourceType") String sourceType,
                                                     IServiceContext context) {
        String userId = context.getUserId();
        QueryBean query = new QueryBean();
        if (!StringHelper.isEmpty(userId)) {
            query.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_userId, userId));
        }
        if (changeType != null) {
            query.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_changeType, changeType));
        }
        if (!StringHelper.isEmpty(sourceType)) {
            query.addFilter(FilterBeans.eq(LitemallPointsFlow.PROP_NAME_sourceType, sourceType));
        }
        query.addOrderField(LitemallPointsFlow.PROP_NAME_addTime, true);
        return findList(query, null, context);
    }
}
