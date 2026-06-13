
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallNotice;

public interface ILitemallNoticeBiz extends ICrudBiz<LitemallNotice> {

    @BizQuery
    PageBean<LitemallNotice> listNotices(@Name("page") int page,
                                         @Name("pageSize") int pageSize,
                                         IServiceContext context);
}
