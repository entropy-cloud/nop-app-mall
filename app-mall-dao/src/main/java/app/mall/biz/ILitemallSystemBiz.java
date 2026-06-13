

package app.mall.biz;

import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallSystem;

public interface ILitemallSystemBiz extends ICrudBiz<LitemallSystem> {

    @BizQuery
    String getConfig(@Name("keyName") String keyName, IServiceContext context);
}
