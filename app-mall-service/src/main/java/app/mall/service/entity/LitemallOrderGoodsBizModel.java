
package app.mall.service.entity;

import app.mall.biz.ILitemallOrderGoodsBiz;
import app.mall.dao.entity.LitemallOrderGoods;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;

import java.time.LocalDateTime;
import java.util.List;

@BizModel("LitemallOrderGoods")
public class LitemallOrderGoodsBizModel extends CrudBizModel<LitemallOrderGoods> implements ILitemallOrderGoodsBiz {
    public LitemallOrderGoodsBizModel() {
        setEntityName(LitemallOrderGoods.class.getName());
    }

    @Override
    @BizMutation
    public int expireCommentWindow(@Name("timeoutDays") int timeoutDays,
                                    IServiceContext context) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(timeoutDays);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallOrderGoods.PROP_NAME_comment, 0));
        query.addFilter(FilterBeans.lt(LitemallOrderGoods.PROP_NAME_addTime, cutoff));
        query.setLimit(500);

        List<LitemallOrderGoods> expired = doFindListByQueryDirectly(query, context);
        for (LitemallOrderGoods og : expired) {
            og.setComment(-1);
        }
        return expired.size();
    }
}
