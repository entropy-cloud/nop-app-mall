package app.mall.service.notification;

import io.nop.integration.api.email.EmailMessage;
import io.nop.integration.api.email.IEmailSender;
import io.nop.integration.api.sms.ISmsSender;
import io.nop.integration.api.sms.SmsMessage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestMallNotificationService {

    private static class CapturingSmsSender implements ISmsSender {
        final List<SmsMessage> sent = new ArrayList<>();
        RuntimeException throwOnSend = null;

        @Override
        public void sendMessage(SmsMessage message) {
            if (throwOnSend != null) {
                throw throwOnSend;
            }
            sent.add(message);
        }
    }

    private static class CapturingEmailSender implements IEmailSender {
        final List<EmailMessage> sent = new ArrayList<>();
        RuntimeException throwOnSend = null;

        @Override
        public void sendEmail(EmailMessage mail) {
            if (throwOnSend != null) {
                throw throwOnSend;
            }
            sent.add(mail);
        }
    }

    private void setSmsSender(MallNotificationService svc, ISmsSender sender) throws Exception {
        Field f = MallNotificationService.class.getDeclaredField("smsSender");
        f.setAccessible(true);
        f.set(svc, sender);
    }

    private void setEmailSender(MallNotificationService svc, IEmailSender sender) throws Exception {
        Field f = MallNotificationService.class.getDeclaredField("emailSender");
        f.setAccessible(true);
        f.set(svc, sender);
    }

    @Test
    void testNullSenderSkipsSilently() {
        MallNotificationService svc = new MallNotificationService();
        assertDoesNotThrow(() -> svc.sendOrderPaymentNotification("ORD123456", "13800138000"));
        assertDoesNotThrow(() -> svc.sendAdminOrderNotification("ORD123456"));
        assertDoesNotThrow(() -> svc.sendCaptchaCode("13800138000", "1234"));
    }

    @Test
    void testNormalSendCapturesMessage() throws Exception {
        MallNotificationService svc = new MallNotificationService();
        CapturingSmsSender mock = new CapturingSmsSender();
        setSmsSender(svc, mock);

        svc.sendOrderPaymentNotification("ORD123456", "13800138000");

        assertEquals(1, mock.sent.size());
        assertEquals("13800138000", mock.sent.get(0).getMobile());
    }

    @Test
    void testSenderExceptionIsSwallowed() throws Exception {
        MallNotificationService svc = new MallNotificationService();
        CapturingSmsSender mock = new CapturingSmsSender();
        mock.throwOnSend = new RuntimeException("network down");
        setSmsSender(svc, mock);

        assertDoesNotThrow(() -> svc.sendRefundNotification("ORD123456", "13800138000"));
        assertTrue(mock.sent.isEmpty());
    }

    @Test
    void testAdminNotificationWithEmail() throws Exception {
        MallNotificationService svc = new MallNotificationService();
        CapturingEmailSender emailSender = new CapturingEmailSender();
        setEmailSender(svc, emailSender);

        assertDoesNotThrow(() -> svc.sendAdminOrderNotification("ORD999888"));
        assertEquals(1, emailSender.sent.size());
        assertTrue(emailSender.sent.get(0).getSubject().contains("999888"));
    }
}
