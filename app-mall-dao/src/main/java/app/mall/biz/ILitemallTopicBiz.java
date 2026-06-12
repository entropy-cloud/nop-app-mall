package app.mall.biz;

import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallTopic;

public interface ILitemallTopicBiz extends ICrudBiz<LitemallTopic> {

    @BizQuery
    PageBean<LitemallTopic> frontList(@Name("page") int page,
                                      @Name("pageSize") int pageSize,
                                      IServiceContext context);

    @BizQuery
    LitemallTopic frontDetail(@Name("id") String id,
                              IServiceContext context);
}
