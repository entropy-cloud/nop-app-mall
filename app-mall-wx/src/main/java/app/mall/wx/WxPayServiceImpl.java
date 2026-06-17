package app.mall.wx;

import app.mall.pay.PayPrepayRequestBean;
import app.mall.pay.PayPrepayResponseBean;
import app.mall.pay.PayRefundRequestBean;
import app.mall.pay.PayRefundResponseBean;
import app.mall.pay.PayService;
import app.mall.pay.PayStatusResponseBean;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.exception.HttpException;
import com.wechat.pay.java.core.exception.MalformedMessageException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.biz.RequestBean;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.exceptions.ErrorCode;
import io.nop.api.core.exceptions.NopException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@BizModel("PayService")
public class WxPayServiceImpl implements PayService {

    private static final Logger LOG = LoggerFactory.getLogger(WxPayServiceImpl.class);

    private static boolean forceRefundFailure = false;

    private boolean enabled;
    private String appId;
    private String mchId;
    private String apiV3Key;
    private String privateKeyPath;
    private String publicKeyPath;
    private String publicKeyId;
    private String merchantSerialNumber;
    private String notifyUrl;

    private NativePayService nativePayService;
    private RefundService refundService;
    private NotificationParser notificationParser;

    public static void setForceRefundFailure(boolean value) {
        forceRefundFailure = value;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public void setApiV3Key(String apiV3Key) {
        this.apiV3Key = apiV3Key;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }

    public void setPublicKeyId(String publicKeyId) {
        this.publicKeyId = publicKeyId;
    }

    public void setMerchantSerialNumber(String merchantSerialNumber) {
        this.merchantSerialNumber = merchantSerialNumber;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    @PostConstruct
    public void init() {
        if (!enabled) {
            LOG.info("WxPay is disabled (wxpay.enabled=false). Using demo mode.");
            return;
        }
        try {
            String privateKey = readPem(privateKeyPath);
            String publicKey = readPem(publicKeyPath);

            RSAPublicKeyConfig config = new RSAPublicKeyConfig.Builder()
                    .merchantId(mchId)
                    .privateKey(privateKey)
                    .merchantSerialNumber(merchantSerialNumber)
                    .publicKeyId(publicKeyId)
                    .publicKey(publicKey)
                    .apiV3Key(apiV3Key)
                    .build();

            nativePayService = new NativePayService.Builder().config(config).build();
            refundService = new RefundService.Builder().config(config).build();
            notificationParser = new NotificationParser(config);

            LOG.info("WxPay SDK initialized successfully (enabled=true)");
        } catch (Exception e) {
            LOG.error("Failed to initialize WxPay SDK", e);
            throw new NopException(ERR_WXPAY_CONFIG_INVALID, e)
                    .param(ARG_ERROR_MESSAGE, e.getMessage());
        }
    }

    @Override
    @BizQuery("isEnabled")
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    @BizMutation("createPayment")
    public PayPrepayResponseBean createPayment(@RequestBean PayPrepayRequestBean req) {
        if (!enabled) {
            LOG.warn("WxPay disabled, returning demo prepay response for outTradeNo={}", req.getOutTradeNo());
            PayPrepayResponseBean resp = new PayPrepayResponseBean();
            resp.setPayId("sample_prepay_" + req.getOutTradeNo());
            resp.setCodeUrl("weixin://wxpay/bizpayurl?pr=sample");
            resp.setTradeType("NATIVE");
            return resp;
        }

        try {
            PrepayRequest prepayReq = new PrepayRequest();
            prepayReq.setAppid(appId);
            prepayReq.setMchid(mchId);
            prepayReq.setDescription(req.getDescription());
            prepayReq.setOutTradeNo(req.getOutTradeNo());
            prepayReq.setNotifyUrl(req.getNotifyUrl() != null ? req.getNotifyUrl() : notifyUrl);

            Amount amount = new Amount();
            amount.setTotal(req.getTotalFee().multiply(new java.math.BigDecimal("100")).intValue());
            amount.setCurrency("CNY");
            prepayReq.setAmount(amount);

            com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse prepayResp =
                    nativePayService.prepay(prepayReq);

            PayPrepayResponseBean resp = new PayPrepayResponseBean();
            resp.setPayId("native_" + req.getOutTradeNo());
            resp.setCodeUrl(prepayResp.getCodeUrl());
            resp.setTradeType("NATIVE");
            return resp;
        } catch (HttpException | MalformedMessageException | ServiceException e) {
            LOG.error("createPayment failed for outTradeNo={}", req.getOutTradeNo(), e);
            throw new NopException(ERR_WXPAY_CREATE_PAYMENT_FAILED, e)
                    .param(ARG_OUT_TRADE_NO, req.getOutTradeNo())
                    .param(ARG_ERROR_MESSAGE, extractErrorMessage(e));
        }
    }

    @Override
    @BizQuery("queryPayment")
    public PayStatusResponseBean queryPayment(@Name("outTradeNo") String outTradeNo) {
        if (!enabled) {
            LOG.warn("WxPay disabled, returning demo query response for outTradeNo={}", outTradeNo);
            PayStatusResponseBean resp = new PayStatusResponseBean();
            resp.setSuccess(true);
            resp.setTradeState("NOTPAY");
            resp.setOutTradeNo(outTradeNo);
            return resp;
        }

        try {
            QueryOrderByOutTradeNoRequest queryReq = new QueryOrderByOutTradeNoRequest();
            queryReq.setOutTradeNo(outTradeNo);
            queryReq.setMchid(mchId);

            Transaction transaction = nativePayService.queryOrderByOutTradeNo(queryReq);

            PayStatusResponseBean resp = new PayStatusResponseBean();
            resp.setSuccess(Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState()));
            resp.setTradeState(transaction.getTradeState().name());
            resp.setTransactionId(transaction.getTransactionId());
            resp.setOutTradeNo(transaction.getOutTradeNo());
            return resp;
        } catch (HttpException | MalformedMessageException | ServiceException e) {
            LOG.error("queryPayment failed for outTradeNo={}", outTradeNo, e);
            throw new NopException(ERR_WXPAY_QUERY_FAILED, e)
                    .param(ARG_OUT_TRADE_NO, outTradeNo)
                    .param(ARG_ERROR_MESSAGE, extractErrorMessage(e));
        }
    }

    @Override
    @BizMutation("refund")
    public PayRefundResponseBean refund(@RequestBean PayRefundRequestBean req) {
        if (!enabled) {
            PayRefundResponseBean resp = new PayRefundResponseBean();
            if (forceRefundFailure) {
                resp.setSuccess(false);
            } else {
                resp.setRefundId("sample_refund_" + req.getOutRefundNo());
            }
            return resp;
        }

        try {
            AmountReq amountReq = new AmountReq();
            amountReq.setTotal(req.getTotalFee().multiply(new java.math.BigDecimal("100")).longValue());
            amountReq.setRefund(req.getRefundFee().multiply(new java.math.BigDecimal("100")).longValue());
            amountReq.setCurrency("CNY");

            CreateRequest refundReq = new CreateRequest();
            refundReq.setOutTradeNo(req.getOutTradeNo());
            refundReq.setOutRefundNo(req.getOutRefundNo());
            refundReq.setAmount(amountReq);
            refundReq.setNotifyUrl(notifyUrl);

            Refund refund = refundService.create(refundReq);

            PayRefundResponseBean resp = new PayRefundResponseBean();
            resp.setSuccess("SUCCESS".equals(refund.getStatus().name()));
            resp.setRefundId(refund.getRefundId());
            return resp;
        } catch (HttpException | MalformedMessageException | ServiceException e) {
            LOG.error("refund failed for outRefundNo={}", req.getOutRefundNo(), e);
            throw new NopException(ERR_WXPAY_REFUND_FAILED, e)
                    .param(ARG_OUT_REFUND_NO, req.getOutRefundNo())
                    .param(ARG_ERROR_MESSAGE, extractErrorMessage(e));
        }
    }

    public String parseNotifyBody(String body, String wechatpaySerial, String wechatpayNonce,
                                  String wechatpaySignature, String wechatpayTimestamp,
                                  String wechatpaySignatureType) {
        if (!enabled) {
            LOG.warn("WxPay disabled, skipping notify verification");
            return null;
        }

        try {
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(wechatpaySerial)
                    .nonce(wechatpayNonce)
                    .signature(wechatpaySignature)
                    .timestamp(wechatpayTimestamp)
                    .signType(wechatpaySignatureType)
                    .body(body)
                    .build();

            Transaction transaction = notificationParser.parse(requestParam, Transaction.class);
            LOG.info("Notify verified: outTradeNo={}, tradeState={}",
                    transaction.getOutTradeNo(), transaction.getTradeState());
            // Only signal SUCCESS to the callback; other trade states (e.g. USERPAYING, NOTPAY)
            // must not drive the order into PAY.
            if (Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
                return transaction.getOutTradeNo();
            }
            return null;
        } catch (ValidationException e) {
            LOG.error("Notify signature verification failed", e);
            throw new NopException(ERR_WXPAY_NOTIFY_VERIFY_FAILED, e)
                    .param(ARG_ERROR_MESSAGE, e.getMessage());
        }
    }

    private String readPem(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new NopException(ERR_WXPAY_CONFIG_INVALID, e)
                    .param(ARG_ERROR_MESSAGE, "Failed to read PEM file: " + path);
        }
    }

    private String extractErrorMessage(Exception e) {
        if (e instanceof ServiceException) {
            return ((ServiceException) e).getErrorMessage();
        }
        return e.getMessage();
    }

    private static final String ARG_OUT_TRADE_NO = "outTradeNo";
    private static final String ARG_OUT_REFUND_NO = "outRefundNo";
    private static final String ARG_ERROR_MESSAGE = "errorMessage";

    static final ErrorCode ERR_WXPAY_DISABLED = ErrorCode.define(
            "nop.err.mall.wxpay.disabled", "微信支付未启用");
    static final ErrorCode ERR_WXPAY_CREATE_PAYMENT_FAILED = ErrorCode.define(
            "nop.err.mall.wxpay.create-payment-failed", "统一下单失败",
            ARG_OUT_TRADE_NO, ARG_ERROR_MESSAGE);
    static final ErrorCode ERR_WXPAY_QUERY_FAILED = ErrorCode.define(
            "nop.err.mall.wxpay.query-failed", "查询支付状态失败",
            ARG_OUT_TRADE_NO, ARG_ERROR_MESSAGE);
    static final ErrorCode ERR_WXPAY_REFUND_FAILED = ErrorCode.define(
            "nop.err.mall.wxpay.refund-failed", "退款失败",
            ARG_OUT_REFUND_NO, ARG_ERROR_MESSAGE);
    static final ErrorCode ERR_WXPAY_CONFIG_INVALID = ErrorCode.define(
            "nop.err.mall.wxpay.config-invalid", "微信支付配置无效",
            ARG_ERROR_MESSAGE);
    static final ErrorCode ERR_WXPAY_NOTIFY_VERIFY_FAILED = ErrorCode.define(
            "nop.err.mall.wxpay.notify-verify-failed", "回调签名验证失败",
            ARG_ERROR_MESSAGE);
}
