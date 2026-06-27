
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.core.Name;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.util.List;

import app.mall.dao.entity.LitemallPointsFlow;

public interface ILitemallPointsFlowBiz extends ICrudBiz<LitemallPointsFlow>{

    @BizQuery
    List<LitemallPointsFlow> getMyPointsFlows(@Optional @Name("changeType") Integer changeType,
                                              @Optional @Name("sourceType") String sourceType,
                                              IServiceContext context);
}
