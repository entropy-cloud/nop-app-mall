
package app.mall.service.entity;

import app.mall.biz.ILitemallWalletBiz;
import app.mall.biz.ILitemallWalletFlowBiz;
import app.mall.dao.entity.LitemallWallet;
import app.mall.dao.entity.LitemallWalletFlow;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.List;

@BizModel("LitemallWalletFlow")
public class LitemallWalletFlowBizModel extends CrudBizModel<LitemallWalletFlow> implements ILitemallWalletFlowBiz {
    @Inject
    ILitemallWalletBiz walletBiz;

    public LitemallWalletFlowBizModel(){
        setEntityName(LitemallWalletFlow.class.getName());
    }

    @Override
    @BizQuery
    public List<LitemallWalletFlow> getMyWalletFlows(@Optional @Name("changeType") Integer changeType,
                                                      @Optional @Name("sourceType") String sourceType,
                                                      IServiceContext context) {
        String userId = context.getUserId();
        if (StringHelper.isEmpty(userId)) {
            return Collections.emptyList();
        }
        // WalletFlow has no userId column; resolve the current user's wallet and filter by walletId.
        // getMyWallet returns a non-persisted shell (null id) when the user has no wallet yet, in
        // which case there are no flows to list.
        LitemallWallet wallet = walletBiz.getMyWallet(context);
        String walletId = wallet.orm_idString();
        if (StringHelper.isEmpty(walletId)) {
            return Collections.emptyList();
        }
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallWalletFlow.PROP_NAME_walletId, walletId));
        if (changeType != null) {
            query.addFilter(FilterBeans.eq(LitemallWalletFlow.PROP_NAME_changeType, changeType));
        }
        if (!StringHelper.isEmpty(sourceType)) {
            query.addFilter(FilterBeans.eq(LitemallWalletFlow.PROP_NAME_sourceType, sourceType));
        }
        query.addOrderField(LitemallWalletFlow.PROP_NAME_addTime, true);
        return findList(query, null, context);
    }
}
