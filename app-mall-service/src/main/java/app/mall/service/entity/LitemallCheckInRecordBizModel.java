
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.biz.ILitemallCheckInRecordBiz;
import app.mall.dao.entity.LitemallCheckInRecord;

@BizModel("LitemallCheckInRecord")
public class LitemallCheckInRecordBizModel extends CrudBizModel<LitemallCheckInRecord> implements ILitemallCheckInRecordBiz{
    public LitemallCheckInRecordBizModel(){
        setEntityName(LitemallCheckInRecord.class.getName());
    }
}
