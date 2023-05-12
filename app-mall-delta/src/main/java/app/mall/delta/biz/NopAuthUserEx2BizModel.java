package app.mall.delta.biz;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Description;

@BizModel("NopAuthUser")
public class NopAuthUserEx2BizModel {

    @BizQuery
    @Description("定义在NopAuthUserEx2BizModel中的扩展方法")
    public String extAction2() {
        return "result2";
    }
}