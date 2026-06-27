
package app.mall.biz;

import app.mall.dao.dto.CheckInResultBean;
import app.mall.dao.dto.CheckInStatusBean;
import app.mall.dao.entity.LitemallCheckInRecord;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

public interface ILitemallCheckInRecordBiz extends ICrudBiz<LitemallCheckInRecord>{

    @BizMutation
    CheckInResultBean checkInToday(IServiceContext context);

    @BizQuery
    CheckInStatusBean getMyCheckInStatus(IServiceContext context);
}
