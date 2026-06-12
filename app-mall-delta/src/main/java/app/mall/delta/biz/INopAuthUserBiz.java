package app.mall.delta.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.time.LocalDate;

public interface INopAuthUserBiz extends ICrudBiz<NopAuthUser> {

    @BizQuery
    NopAuthUser getMyProfile(IServiceContext context);

    @BizMutation
    NopAuthUser updateMyProfile(
            @Name("nickName") @Optional String nickName,
            @Name("gender") @Optional Integer gender,
            @Name("avatar") @Optional String avatar,
            @Name("phone") @Optional String phone,
            @Name("email") @Optional String email,
            @Name("birthday") @Optional LocalDate birthday,
            IServiceContext context);
}
