

package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.core.Name;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallOrderGoods;

public interface ILitemallOrderGoodsBiz extends ICrudBiz<LitemallOrderGoods> {

    @BizMutation
    int expireCommentWindow(@Name("timeoutDays") int timeoutDays,
                            IServiceContext context);
}
