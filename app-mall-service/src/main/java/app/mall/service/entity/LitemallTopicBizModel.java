
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.biz.crud.CrudBizModel;

import app.mall.dao.entity.LitemallTopic;

@BizModel("LitemallTopic")
public class LitemallTopicBizModel extends CrudBizModel<LitemallTopic>{
    public LitemallTopicBizModel(){
        setEntityName(LitemallTopic.class.getName());
    }
}
