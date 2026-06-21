
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallWalletBiz;
import app.mall.dao.entity.LitemallWallet;

@BizModel("LitemallWallet")
public class LitemallWalletBizModel extends CrudBizModel<LitemallWallet> implements ILitemallWalletBiz{
    public LitemallWalletBizModel(){
        setEntityName(LitemallWallet.class.getName());
    }
}
