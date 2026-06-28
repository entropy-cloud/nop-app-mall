
package app.mall.biz;

import app.mall.dao.entity.LitemallWalletFlow;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.util.List;

public interface ILitemallWalletFlowBiz extends ICrudBiz<LitemallWalletFlow>{

    @BizQuery
    List<LitemallWalletFlow> getMyWalletFlows(@Optional @Name("changeType") Integer changeType,
                                              @Optional @Name("sourceType") String sourceType,
                                              IServiceContext context);
}
