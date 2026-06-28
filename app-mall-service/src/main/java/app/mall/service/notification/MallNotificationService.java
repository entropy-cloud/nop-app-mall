package app.mall.service.notification;

import app.mall.biz.ILitemallSystemBiz;
import app.mall.biz.ILitemallUserMessageBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.service.consts.NotifyType;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.core.context.ServiceContextImpl;
import io.nop.integration.api.email.EmailMessage;
import io.nop.integration.api.email.IEmailSender;
import io.nop.integration.api.sms.ISmsSender;
import io.nop.integration.api.sms.SmsMessage;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;

@jakarta.inject.Named
public class MallNotificationService {
    static final Logger LOG = LoggerFactory.getLogger(MallNotificationService.class);

    @Inject
    @Nullable
    ISmsSender smsSender;

    @Inject
    @Nullable
    IEmailSender emailSender;

    // User-message (站内信) channel. Required beans (always present at runtime). The plain
    // `new MallNotificationService()` in unit tests leaves these null, so sendUserMessage guards on null.
    @Inject
    ILitemallUserMessageBiz userMessageBiz;

    @Inject
    ILitemallSystemBiz systemBiz;

    public void sendOrderPaymentNotification(String orderSn, String mobile, String userId) {
        sendSms(mobile, NotifyType.PAY_SUCCEED, Arrays.asList(StringHelper.tail(orderSn, 6)),
                "sendOrderPaymentNotification", orderSn);
        if (userId != null) {
            String tail = StringHelper.tail(orderSn, 6);
            sendUserMessage(userId, _AppMallDaoConstants.MSG_TYPE_ORDER,
                    "支付成功", "您的订单 " + tail + " 已支付成功");
        }
    }

    public void sendOrderShipNotification(String orderSn, String mobile, String userId) {
        sendSms(mobile, NotifyType.SHIP, Arrays.asList(StringHelper.tail(orderSn, 6)),
                "sendOrderShipNotification", orderSn);
        if (userId != null) {
            String tail = StringHelper.tail(orderSn, 6);
            sendUserMessage(userId, _AppMallDaoConstants.MSG_TYPE_ORDER,
                    "订单已发货", "您的订单 " + tail + " 已发货，请注意查收");
        }
    }

    public void sendAdminOrderNotification(String orderSn) {
        sendAdminEmail(orderSn);
        LOG.info("sendAdminOrderNotification: new order created, orderSn={}", orderSn);
    }

    public void sendRefundNotification(String orderSn, String mobile, String userId) {
        sendSms(mobile, NotifyType.REFUND, Arrays.asList(StringHelper.tail(orderSn, 6)),
                "sendRefundNotification", orderSn);
        if (userId != null) {
            String tail = StringHelper.tail(orderSn, 6);
            sendUserMessage(userId, _AppMallDaoConstants.MSG_TYPE_ORDER,
                    "退款成功", "您的订单 " + tail + " 已退款");
        }
    }

    public void sendGrouponFailRefundNotification(String orderSn, String mobile, String userId) {
        sendSms(mobile, NotifyType.REFUND, Arrays.asList(StringHelper.tail(orderSn, 6)),
                "sendGrouponFailRefundNotification", orderSn);
        if (userId != null) {
            String tail = StringHelper.tail(orderSn, 6);
            sendUserMessage(userId, _AppMallDaoConstants.MSG_TYPE_ORDER,
                    "团购失败退款", "您的订单 " + tail + " 因团购未成团已退款");
        }
    }

    public void sendPinTuanFailRefundNotification(String orderSn, String mobile, String userId) {
        sendSms(mobile, NotifyType.REFUND, Arrays.asList(StringHelper.tail(orderSn, 6)),
                "sendPinTuanFailRefundNotification", orderSn);
        if (userId != null) {
            String tail = StringHelper.tail(orderSn, 6);
            sendUserMessage(userId, _AppMallDaoConstants.MSG_TYPE_ORDER,
                    "拼团失败退款", "您的订单 " + tail + " 因拼团未成团已退款");
        }
    }

    public void sendCaptchaCode(String mobile, String code) {
        sendSms(mobile, NotifyType.CAPTCHA, Arrays.asList(code),
                "sendCaptchaCode", mobile);
    }

    /**
     * Event-toggle gate read by host BizModels in the main transaction (where a context is available).
     * Defaults to enabled when the config key is absent or the read fails, so站内信 is opt-out.
     */
    public boolean isEventMessageEnabled(String eventKey, IServiceContext context) {
        if (systemBiz == null) {
            return true;
        }
        try {
            String value = systemBiz.getConfig("mall_message_event_enabled_" + eventKey, context);
            return value == null || value.isEmpty() || "true".equalsIgnoreCase(value.trim()) || "1".equals(value.trim());
        } catch (Exception e) {
            LOG.warn("isEventMessageEnabled: read failed for {}, defaulting enabled", eventKey, e);
            return true;
        }
    }

    /**
     * Persist a站内信 record. Called from event hooks (txn().afterCommit). The CrudBizModel save queues
     * the entity into the request-scoped ORM session, which flushes at request end, so the write
     * persists after the business transaction commits. Failures are logged and swallowed: 站内信 is a
     * side channel and must never roll back the core business fact.
     */
    public void sendUserMessage(String userId, int msgType, String title, String content) {
        if (StringHelper.isEmpty(userId) || userMessageBiz == null) {
            return;
        }
        try {
            // System context: event hooks run in txn().afterCommit with no user session available
            // (mirrors PaymentCallbackImpl's WeChat-callback handling).
            IServiceContext systemContext = new ServiceContextImpl();
            userMessageBiz.sendUserMessage(userId, msgType, title, content, systemContext);
        } catch (Exception e) {
            LOG.error("sendUserMessage: 站内信写入失败，已忽略。userId={}, title={}", userId, title, e);
        }
    }

    private void sendSms(String mobile, NotifyType type, java.util.List<String> params,
                         String methodName, String contextKey) {
        if (smsSender == null) {
            LOG.info("{}: ISmsSender is null, skip. context={}", methodName, contextKey);
            return;
        }
        try {
            SmsMessage smsMessage = new SmsMessage();
            smsMessage.setMobile(mobile);
            smsMessage.setType(type.ordinal());
            smsMessage.setParams(params);
            smsSender.sendMessage(smsMessage);
            LOG.info("{}: context={}", methodName, contextKey);
        } catch (Exception e) {
            LOG.error("{}: SMS send failed. context={}", methodName, contextKey, e);
        }
    }

    private void sendAdminEmail(String orderSn) {
        if (emailSender == null) {
            LOG.info("sendAdminOrderNotification: IEmailSender is null, skip email. orderSn={}", orderSn);
            return;
        }
        try {
            EmailMessage email = new EmailMessage();
            email.setTo(Collections.singletonList("ops@example.com"));
            email.setSubject("Mall new order " + StringHelper.tail(orderSn, 6));
            email.setText("A new order is submitted. orderSn=" + orderSn);
            email.setHtml(false);
            emailSender.sendEmail(email);
            LOG.info("sendAdminOrderNotification: email sent. orderSn={}", orderSn);
        } catch (Exception e) {
            LOG.error("sendAdminOrderNotification: email send failed. orderSn={}", orderSn, e);
        }
    }
}
