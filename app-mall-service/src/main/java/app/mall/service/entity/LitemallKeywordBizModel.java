package app.mall.service.entity;

import app.mall.biz.ILitemallKeywordBiz;
import app.mall.dao.entity.LitemallKeyword;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

import java.util.List;

@BizModel("LitemallKeyword")
public class LitemallKeywordBizModel extends CrudBizModel<LitemallKeyword> implements ILitemallKeywordBiz {
    public LitemallKeywordBizModel() {
        setEntityName(LitemallKeyword.class.getName());
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public List<LitemallKeyword> getHotKeywords(IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallKeyword.PROP_NAME_isHot, true));
        return findList(query, null, context);
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public List<LitemallKeyword> getDefaultKeywords(IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallKeyword.PROP_NAME_isDefault, true));
        return findList(query, null, context);
    }
}
