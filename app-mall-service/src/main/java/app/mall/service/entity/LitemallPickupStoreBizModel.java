
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

import app.mall.biz.ILitemallPickupStoreBiz;
import app.mall.dao.entity.LitemallPickupStore;

import java.util.List;

@BizModel("LitemallPickupStore")
public class LitemallPickupStoreBizModel extends CrudBizModel<LitemallPickupStore> implements ILitemallPickupStoreBiz {
    public LitemallPickupStoreBizModel(){
        setEntityName(LitemallPickupStore.class.getName());
    }

    @Override
    @BizQuery
    public List<LitemallPickupStore> listActiveStores(IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(io.nop.api.core.beans.FilterBeans.eq(
                LitemallPickupStore.PROP_NAME_status, STATUS_ACTIVE));
        query.addOrderField(LitemallPickupStore.PROP_NAME_addTime, false);
        return findList(query, null, context);
    }

    private static final int STATUS_ACTIVE = 0;
}
