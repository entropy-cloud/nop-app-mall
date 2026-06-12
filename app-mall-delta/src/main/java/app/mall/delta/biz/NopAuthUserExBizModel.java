package app.mall.delta.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.auth.service.entity.NopAuthUserBizModel;
import io.nop.biz.crud.EntityData;
import io.nop.core.context.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class NopAuthUserExBizModel extends NopAuthUserBizModel implements INopAuthUserBiz {
    static final Logger LOG = LoggerFactory.getLogger(NopAuthUserExBizModel.class);

    @Override
    protected void defaultPrepareUpdate(EntityData<NopAuthUser> entityData, IServiceContext context) {
        super.defaultPrepareUpdate(entityData, context);

        LOG.info("prepare update user: {}", entityData.getEntity().getUserId());
    }

    @Override
    @BizQuery
    public NopAuthUser getMyProfile(IServiceContext context) {
        String userId = context.getUserId();
        return requireEntity(userId, "getMyProfile", context);
    }

    @Override
    @BizMutation
    public NopAuthUser updateMyProfile(
            @Name("nickName") @Optional String nickName,
            @Name("gender") @Optional Integer gender,
            @Name("avatar") @Optional String avatar,
            @Name("phone") @Optional String phone,
            @Name("email") @Optional String email,
            @Name("birthday") @Optional LocalDate birthday,
            IServiceContext context) {
        String userId = context.getUserId();
        NopAuthUser user = requireEntity(userId, "updateMyProfile", context);

        if (nickName != null) user.setNickName(nickName);
        if (gender != null) user.setGender(gender);
        if (avatar != null) user.setAvatar(avatar);
        if (phone != null) user.setPhone(phone);
        if (email != null) user.setEmail(email);
        if (birthday != null) user.setBirthday(birthday);

        updateEntity(user, "updateMyProfile", context);
        return user;
    }
}
