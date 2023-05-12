package app.mall.delta.biz;

import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Description;
import io.nop.api.core.annotations.core.Name;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.auth.service.entity.NopAuthUserBizModel;
import io.nop.biz.crud.EntityData;
import io.nop.core.context.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NopAuthUserExBizModel extends NopAuthUserBizModel {
    static final Logger LOG = LoggerFactory.getLogger(NopAuthUserExBizModel.class);

    @Override
    protected void defaultPrepareUpdate(EntityData<NopAuthUser> entityData, IServiceContext context) {
        super.defaultPrepareUpdate(entityData, context);

        LOG.info("prepare update user: {}", entityData.getEntity().getUserId());
    }

    @BizQuery
    @Description("定义在NopAuthUserExBizModel中的扩展方法")
    public String extAction1(@Name("myArg") String myArg) {
        return "result:" + myArg;
    }
}
