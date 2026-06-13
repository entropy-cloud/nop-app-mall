package app.mall.service.notification;

import app.mall.service.consts.NotifyType;
import io.nop.commons.util.StringHelper;
import io.nop.integration.api.sms.ISmsSender;
import io.nop.integration.api.sms.SmsMessage;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@jakarta.inject.Named
public class MallNotificationService {
    static final Logger LOG = LoggerFactory.getLogger(MallNotificationService.class);

    @Inject
    @Nullable
    ISmsSender smsSender;

    public void sendOrderPaymentNotification(String orderSn, String mobile) {
        sendSms(mobile, NotifyType.PAY_SUCCEED, Arrays.asList(StringHelper.tail(orderSn, 6)),
                "sendOrderPaymentNotification", orderSn);
    }

    public void sendOrderShipNotification(String orderSn, String mobile) {
        sendSms(mobile, NotifyType.SHIP, Arrays.asList(StringHelper.tail(orderSn, 6)),
                "sendOrderShipNotification", orderSn);
    }

    public void sendAdminOrderNotification(String orderSn) {
        LOG.info("sendAdminOrderNotification: new order created, orderSn={}", orderSn);
    }

    public void sendRefundNotification(String orderSn, String mobile) {
        sendSms(mobile, NotifyType.REFUND, Arrays.asList(StringHelper.tail(orderSn, 6)),
                "sendRefundNotification", orderSn);
    }

    public void sendGrouponFailRefundNotification(String orderSn, String mobile) {
        sendSms(mobile, NotifyType.REFUND, Arrays.asList(StringHelper.tail(orderSn, 6)),
                "sendGrouponFailRefundNotification", orderSn);
    }

    public void sendCaptchaCode(String mobile, String code) {
        sendSms(mobile, NotifyType.CAPTCHA, Arrays.asList(code),
                "sendCaptchaCode", mobile);
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
}
