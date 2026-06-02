
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallNoticeAdminBiz;

import app.mall.dao.entity.LitemallNoticeAdmin;

@BizModel("LitemallNoticeAdmin")
public class LitemallNoticeAdminBizModel extends CrudBizModel<LitemallNoticeAdmin> implements ILitemallNoticeAdminBiz {
    public LitemallNoticeAdminBizModel(){
        setEntityName(LitemallNoticeAdmin.class.getName());
    }
}
