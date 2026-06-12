package app.mall.delta.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.ErrorCode;
import io.nop.api.core.exceptions.NopException;
import io.nop.auth.biz.INopAuthUserBiz;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@BizModel("LoginApi")
public class LoginApiExBizModel {
    static final Logger LOG = LoggerFactory.getLogger(LoginApiExBizModel.class);

    static final String ARG_USERNAME = "username";

    static final ErrorCode ERR_USER_USERNAME_EXISTS =
            ErrorCode.define("nop.err.mall.user.username-exists",
                    "用户名已注册", ARG_USERNAME);

    static final ErrorCode ERR_USER_USERNAME_EMPTY =
            ErrorCode.define("nop.err.mall.user.username-empty",
                    "用户名不能为空");

    static final ErrorCode ERR_USER_PASSWORD_EMPTY =
            ErrorCode.define("nop.err.mall.user.password-empty",
                    "密码不能为空");

    static final ErrorCode ERR_USER_MOBILE_EMPTY =
            ErrorCode.define("nop.err.mall.user.mobile-empty",
                    "手机号不能为空");

    static final ErrorCode ERR_USER_USERNAME_TOO_SHORT =
            ErrorCode.define("nop.err.mall.user.username-too-short",
                    "用户名长度不能少于2个字符");

    static final ErrorCode ERR_USER_USERNAME_TOO_LONG =
            ErrorCode.define("nop.err.mall.user.username-too-long",
                    "用户名长度不能超过63个字符");

    @Inject
    INopAuthUserBiz userBiz;

    @BizMutation
    @Auth(publicAccess = true)
    public NopAuthUser signUp(
            @Name("username") String username,
            @Name("password") String password,
            @Name("mobile") String mobile,
            IServiceContext context) {

        if (StringHelper.isEmpty(username))
            throw new NopException(ERR_USER_USERNAME_EMPTY);
        if (StringHelper.isEmpty(password))
            throw new NopException(ERR_USER_PASSWORD_EMPTY);
        if (StringHelper.isEmpty(mobile))
            throw new NopException(ERR_USER_MOBILE_EMPTY);
        if (username.length() < 2)
            throw new NopException(ERR_USER_USERNAME_TOO_SHORT);
        if (username.length() > 63)
            throw new NopException(ERR_USER_USERNAME_TOO_LONG);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq("userName", username));
        NopAuthUser existing = userBiz.findFirst(query, null, context);
        if (existing != null)
            throw new NopException(ERR_USER_USERNAME_EXISTS).param(ARG_USERNAME, username);

        NopAuthUser user = userBiz.save(Map.of(
                "userName", username,
                "password", password,
                "nickName", username,
                "phone", mobile,
                "gender", 1,
                "userType", 1,
                "lastLoginIp", "",
                "userLevel", 0,
                "sessionKey", ""
        ), context);

        LOG.info("User registered: userId={}, userName={}", user.getUserId(), user.getUserName());
        return user;
    }
}
