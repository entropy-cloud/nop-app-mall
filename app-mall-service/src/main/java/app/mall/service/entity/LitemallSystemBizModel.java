
package app.mall.service.entity;

import app.mall.biz.ILitemallSystemBiz;
import app.mall.dao.entity.LitemallSystem;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

import java.math.BigDecimal;

@BizModel("LitemallSystem")
public class LitemallSystemBizModel extends CrudBizModel<LitemallSystem> implements ILitemallSystemBiz {
    public LitemallSystemBizModel() {
        setEntityName(LitemallSystem.class.getName());
    }

    @Override
    @BizQuery
    public String getConfig(@Name("keyName") String keyName, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallSystem.PROP_NAME_keyName, keyName));
        query.addFilter(FilterBeans.eq(LitemallSystem.PROP_NAME_deleted, false));
        LitemallSystem config = findFirst(query, null, context);
        return config != null ? config.getKeyValue() : null;
    }

    @Override
    @BizQuery
    public BigDecimal getFreightPrice(IServiceContext context) {
        String value = getConfig("mall_freight_price", context);
        return value != null ? new BigDecimal(value) : BigDecimal.ZERO;
    }
}
