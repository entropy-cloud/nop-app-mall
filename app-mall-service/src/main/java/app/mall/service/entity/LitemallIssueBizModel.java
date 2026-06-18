package app.mall.service.entity;

import app.mall.biz.ILitemallIssueBiz;
import app.mall.dao.entity.LitemallIssue;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

@BizModel("LitemallIssue")
public class LitemallIssueBizModel extends CrudBizModel<LitemallIssue> implements ILitemallIssueBiz {

    public LitemallIssueBizModel() {
        setEntityName(LitemallIssue.class.getName());
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallIssue> listIssues(@Name("page") int page,
                                               @Name("pageSize") int pageSize,
                                               IServiceContext context) {
        QueryBean query = new QueryBean();
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);

        return findPage(query, null, context);
    }
}
