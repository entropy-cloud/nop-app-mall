
package app.mall.service.entity;

import app.mall.biz.ILitemallUserTagBiz;
import app.mall.dao.entity.LitemallUserTag;
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
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;

import static app.mall.service.AppMallErrors.ERR_USER_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_USER_TAG_DUPLICATE;
import static app.mall.service.AppMallErrors.ERR_USER_TAG_NOT_FOUND;

@BizModel("LitemallUserTag")
public class LitemallUserTagBizModel extends CrudBizModel<LitemallUserTag> implements ILitemallUserTagBiz {

    private static final int DEFAULT_PAGE_SIZE = 20;

    public LitemallUserTagBizModel() {
        setEntityName(LitemallUserTag.class.getName());
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallUserTag addUserTag(@Name("userId") String userId,
                                      @Name("tag") String tag,
                                      @Optional @Name("name") String name,
                                      IServiceContext context) {
        if (StringHelper.isEmpty(userId)) {
            throw new NopException(ERR_USER_NOT_FOUND).param("userId", userId);
        }
        if (StringHelper.isEmpty(tag)) {
            throw new NopException(ERR_USER_TAG_DUPLICATE).param("userId", userId).param("tag", tag);
        }
        // Free-text tag code with explicit dedupe on (userId, tag): a user may carry many tags, but
        // each tag code is recorded at most once per user. Index idx_userTag_userId_tag backs this.
        if (findUserTag(userId, tag, context) != null) {
            throw new NopException(ERR_USER_TAG_DUPLICATE).param("userId", userId).param("tag", tag);
        }
        LitemallUserTag userTag = newEntity();
        userTag.setUserId(userId);
        userTag.setTag(tag);
        userTag.setName(StringHelper.isEmpty(name) ? tag : name);
        saveEntity(userTag, null, context);
        return userTag;
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public void removeUserTag(@Name("userId") String userId,
                              @Name("tag") String tag,
                              IServiceContext context) {
        LitemallUserTag userTag = findUserTag(userId, tag, context);
        if (userTag == null) {
            throw new NopException(ERR_USER_TAG_NOT_FOUND).param("userId", userId).param("tag", tag);
        }
        deleteEntity(userTag, null, context);
    }

    @Override
    @BizQuery
    @Auth(roles = "admin")
    public PageBean<LitemallUserTag> findUsersByTag(@Name("tag") String tag,
                                                    @Optional @Name("page") Integer page,
                                                    IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallUserTag.PROP_NAME_tag, tag));
        query.addOrderField(LitemallUserTag.PROP_NAME_addTime, true);
        int pageNo = page == null || page < 1 ? 1 : page;
        query.setOffset((pageNo - 1) * DEFAULT_PAGE_SIZE);
        query.setLimit(DEFAULT_PAGE_SIZE);
        return findPage(query, null, context);
    }

    private LitemallUserTag findUserTag(String userId, String tag, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallUserTag.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallUserTag.PROP_NAME_tag, tag));
        return findFirst(query, null, context);
    }
}
