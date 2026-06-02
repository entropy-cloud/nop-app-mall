
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;
import app.mall.biz.ILitemallNoticeBiz;

import app.mall.dao.entity.LitemallNotice;

@BizModel("LitemallNotice")
public class LitemallNoticeBizModel extends CrudBizModel<LitemallNotice> implements ILitemallNoticeBiz {
    public LitemallNoticeBizModel(){
        setEntityName(LitemallNotice.class.getName());
    }
}
