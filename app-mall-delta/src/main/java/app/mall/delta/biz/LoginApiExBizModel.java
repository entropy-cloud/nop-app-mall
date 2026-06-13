package app.mall.delta.biz;

import app.mall.biz.ILitemallCouponBiz;
import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.dao.entity.LitemallCoupon;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.context.ContextProvider;
import io.nop.api.core.exceptions.ErrorCode;
import io.nop.api.core.exceptions.NopException;
import io.nop.api.core.time.CoreMetrics;
import io.nop.auth.biz.INopAuthUserBiz;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.integration.api.sms.ISmsSender;
import io.nop.integration.api.sms.SmsMessage;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@BizModel("LoginApi")
public class LoginApiExBizModel {
    static final Logger LOG = LoggerFactory.getLogger(LoginApiExBizModel.class);

    static final String ARG_USERNAME = "username";
    static final String ARG_MOBILE = "mobile";
    static final long CODE_EXPIRE_MS = 5 * 60_000L;
    static final long CODE_INTERVAL_MS = 60_000L;

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

    static final ErrorCode ERR_RESET_CODE_INVALID =
            ErrorCode.define("nop.err.mall.reset-code.invalid",
                    "验证码无效或已过期");

    static final ErrorCode ERR_RESET_CODE_SEND_TOO_FREQUENT =
            ErrorCode.define("nop.err.mall.reset-code.send-too-frequent",
                    "发送太频繁");

    static final ErrorCode ERR_USER_MOBILE_NOT_FOUND =
            ErrorCode.define("nop.err.mall.user.mobile-not-found",
                    "手机号未注册");

    static final class ResetCodeEntry {
        final String code;
        final long createdAt;

        ResetCodeEntry(String code, long createdAt) {
            this.code = code;
            this.createdAt = createdAt;
        }
    }

    static final ConcurrentHashMap<String, ResetCodeEntry> resetCodeStore = new ConcurrentHashMap<>();

    @Inject
    INopAuthUserBiz userBiz;

    @Inject
    ILitemallCouponBiz couponBiz;

    @Inject
    ILitemallCouponUserBiz couponUserBiz;

    @Inject
    @Nullable
    ISmsSender smsSender;

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

        dispatchRegistrationCoupons(user, context);

        return user;
    }

    @BizMutation
    @Auth(publicAccess = true)
    public void sendResetCode(@Name("mobile") String mobile, IServiceContext context) {
        if (StringHelper.isEmpty(mobile))
            throw new NopException(ERR_USER_MOBILE_EMPTY);

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq("phone", mobile));
        NopAuthUser user = userBiz.findFirst(query, null, context);
        if (user == null)
            throw new NopException(ERR_USER_MOBILE_NOT_FOUND)
                    .param(ARG_MOBILE, mobile);

        long now = CoreMetrics.currentTimeMillis();
        ResetCodeEntry existing = resetCodeStore.get(mobile);
        if (existing != null && (now - existing.createdAt) < CODE_INTERVAL_MS)
            throw new NopException(ERR_RESET_CODE_SEND_TOO_FREQUENT);

        String code = StringHelper.generateUUID().substring(0, 6);
        resetCodeStore.put(mobile, new ResetCodeEntry(code, now));

        sendCaptchaSms(mobile, code);
        LOG.info("sendResetCode: mobile={}", mobile);
    }

    @BizMutation
    @Auth(publicAccess = true)
    public void resetPassword(
            @Name("mobile") String mobile,
            @Name("code") String code,
            @Name("newPassword") String newPassword,
            IServiceContext context) {

        if (StringHelper.isEmpty(mobile))
            throw new NopException(ERR_USER_MOBILE_EMPTY);
        if (StringHelper.isEmpty(newPassword))
            throw new NopException(ERR_USER_PASSWORD_EMPTY);

        ResetCodeEntry entry = resetCodeStore.get(mobile);
        if (entry == null || !entry.code.equals(code))
            throw new NopException(ERR_RESET_CODE_INVALID);

        long now = CoreMetrics.currentTimeMillis();
        if (now - entry.createdAt > CODE_EXPIRE_MS) {
            resetCodeStore.remove(mobile);
            throw new NopException(ERR_RESET_CODE_INVALID);
        }

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq("phone", mobile));
        NopAuthUser user = userBiz.findFirst(query, null, context);
        if (user == null)
            throw new NopException(ERR_USER_MOBILE_NOT_FOUND)
                    .param(ARG_MOBILE, mobile);

        userBiz.update(Map.of(
                "id", user.orm_idString(),
                "password", newPassword
        ), context);

        resetCodeStore.remove(mobile);
        LOG.info("resetPassword: userId={}", user.getUserId());
    }

    private void sendCaptchaSms(String mobile, String code) {
        if (smsSender == null) {
            LOG.info("sendCaptchaSms: ISmsSender is null, skip. mobile={}", mobile);
            return;
        }
        SmsMessage smsMessage = new SmsMessage();
        smsMessage.setMobile(mobile);
        smsMessage.setType(3);
        smsMessage.setParams(Arrays.asList(code));
        smsSender.sendMessage(smsMessage);
    }

    private void dispatchRegistrationCoupons(NopAuthUser user, IServiceContext context) {
        try {
            QueryBean couponQuery = new QueryBean();
            couponQuery.addFilter(FilterBeans.eq(LitemallCoupon.PROP_NAME_type, 1));
            couponQuery.addFilter(FilterBeans.eq(LitemallCoupon.PROP_NAME_status, 0));
            couponQuery.addFilter(FilterBeans.eq(LitemallCoupon.PROP_NAME_deleted, false));
            List<LitemallCoupon> regCoupons = couponBiz.findList(couponQuery, null, context);

            ContextProvider.getOrCreateContext().setUserId(user.getUserId());

            for (LitemallCoupon coupon : regCoupons) {
                try {
                    couponUserBiz.claimCoupon(coupon.orm_idString(), context);
                } catch (Exception e) {
                    LOG.error("Failed to auto-dispatch registration coupon: couponId={}, userId={}",
                            coupon.orm_idString(), user.getUserId(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to query registration coupons for userId={}", user.getUserId(), e);
        }
    }
}
