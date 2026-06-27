
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallFlashSaleSession;

public interface ILitemallFlashSaleSessionBiz extends ICrudBiz<LitemallFlashSaleSession>{

    @BizMutation
    int switchFlashSaleSessions(IServiceContext context);
}
