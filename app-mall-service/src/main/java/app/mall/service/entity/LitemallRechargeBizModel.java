
package app.mall.service.entity;

import app.mall.biz.ILitemallRechargeBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.biz.ILitemallWalletBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallRecharge;
import app.mall.dao.entity.LitemallWallet;
import app.mall.pay.PayPrepayRequestBean;
import app.mall.pay.PayPrepayResponseBean;
import app.mall.pay.PayService;
import io.nop.api.core.annotations.biz.BizAction;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.core.lang.json.JsonTool;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static app.mall.service.AppMallErrors.ERR_RECHARGE_AMOUNT_INVALID;
import static app.mall.service.AppMallErrors.ERR_RECHARGE_NOT_ALLOW_CONFIRM;
import static app.mall.service.AppMallErrors.ERR_RECHARGE_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_RECHARGE_USE_REAL_PAYMENT;

@BizModel("LitemallRecharge")
public class LitemallRechargeBizModel extends CrudBizModel<LitemallRecharge> implements ILitemallRechargeBiz {
    // Recharge packages are stored as a JSON array in LitemallSystem (keyName=recharge_packages)
    // rather than a dedicated ORM entity — see plan Phase 2 Decision (ORM change is a Protected Area).
    public static final String CONFIG_RECHARGE_PACKAGES = "recharge_packages";

    // outTradeNo is derived (not stored): "RC" + zero-padded rechargeId, e.g. RC00000001.
    // 10 chars total, within WeChat Pay's 6–32 char limit. Strip "RC" + parseInt to recover rechargeId.
    // See plan Phase 2 Decision (no ORM column add — Protected Area).
    static final String OUT_TRADE_NO_PREFIX = "RC";

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Inject
    ILitemallSystemBiz systemBiz;

    @Inject
    ILitemallWalletBiz walletBiz;

    @Inject
    PayService payService;

    public LitemallRechargeBizModel(){
        setEntityName(LitemallRecharge.class.getName());
    }

    @Override
    @BizQuery
    public List<Map<String, Object>> getRechargePackages(IServiceContext context) {
        String json = systemBiz.getConfig(CONFIG_RECHARGE_PACKAGES, context);
        if (!StringHelper.isEmpty(json)) {
            try {
                Object parsed = JsonTool.parse(json);
                if (parsed instanceof List) {
                    List<Map<String, Object>> packages = new ArrayList<>();
                    for (Object item : (List<?>) parsed) {
                        if (item instanceof Map) {
                            packages.add((Map<String, Object>) item);
                        }
                    }
                    if (!packages.isEmpty()) {
                        return packages;
                    }
                }
            } catch (Exception e) {
                // Malformed config falls back to the default package set below.
            }
        }
        // Default package set when no config exists: a baseline 100+10 gift.
        List<Map<String, Object>> defaults = new ArrayList<>();
        Map<String, Object> pkg = new LinkedHashMap<>();
        pkg.put("id", "default-100");
        pkg.put("label", "充100送10");
        pkg.put("amount", new BigDecimal("100"));
        pkg.put("giftAmount", new BigDecimal("10"));
        defaults.add(pkg);
        return defaults;
    }

    @Override
    @BizMutation
    public Map<String, Object> createRecharge(@Name("amount") BigDecimal amount,
                                              @Optional @Name("packageId") String packageId,
                                              IServiceContext context) {
        String userId = context.getUserId();
        if (StringHelper.isEmpty(userId)) {
            throw new NopException(ERR_RECHARGE_AMOUNT_INVALID).param("reason", "login required");
        }
        BigDecimal giftAmount = ZERO;
        if (!StringHelper.isEmpty(packageId)) {
            Map<String, Object> pkg = findPackage(packageId, context);
            if (pkg == null) {
                throw new NopException(ERR_RECHARGE_AMOUNT_INVALID)
                        .param("reason", "package not found").param("packageId", packageId);
            }
            // When a package is selected, amount must match the package amount exactly.
            BigDecimal pkgAmount = toBigDecimal(pkg.get("amount"));
            if (amount == null || amount.compareTo(pkgAmount) != 0) {
                throw new NopException(ERR_RECHARGE_AMOUNT_INVALID)
                        .param("reason", "amount must match the selected package")
                        .param("packageId", packageId);
            }
            giftAmount = toBigDecimal(pkg.get("giftAmount"));
        } else {
            if (amount == null || amount.compareTo(ZERO) <= 0) {
                throw new NopException(ERR_RECHARGE_AMOUNT_INVALID).param("reason", "amount must be positive");
            }
        }

        // Ensure the wallet exists so a valid walletId can be recorded (walletId is a mandatory column
        // on LitemallRecharge). This lazy-creates the wallet with zero balance; the actual credit
        // happens at creditRecharge time after payment confirmation.
        LitemallWallet wallet = walletBiz.ensureWallet(userId, context);
        String walletId = wallet.orm_idString();

        LitemallRecharge recharge = newEntity();
        recharge.setUserId(userId);
        recharge.setWalletId(walletId);
        recharge.setAmount(amount);
        recharge.setGiftAmount(giftAmount);
        recharge.setPayChannel(_AppMallDaoConstants.PAY_CHANNEL_WECHAT);
        recharge.setPayStatus(_AppMallDaoConstants.PAY_STATUS_UNPAID);
        saveEntity(recharge, "createRecharge", context);

        // Derive outTradeNo from the persisted rechargeId (no stored column — Protected Area).
        String outTradeNo = deriveOutTradeNo(recharge.orm_idString());

        PayPrepayRequestBean payReq = new PayPrepayRequestBean();
        payReq.setOutTradeNo(outTradeNo);
        payReq.setTotalFee(amount);
        payReq.setDescription("钱包充值 " + outTradeNo);
        PayPrepayResponseBean payResp = payService.createPayment(payReq);

        Map<String, Object> result = new HashMap<>();
        result.put("rechargeId", recharge.orm_idString());
        result.put("amount", recharge.getAmount());
        result.put("giftAmount", recharge.getGiftAmount());
        result.put("payStatus", recharge.getPayStatus());
        result.put("outTradeNo", outTradeNo);
        result.put("codeUrl", payResp.getCodeUrl());
        return result;
    }

    @Override
    @BizMutation
    public LitemallRecharge confirmRecharge(@Name("rechargeId") String rechargeId,
                                            IServiceContext context) {
        // Demo manual-confirmation path: only allowed when real WeChat Pay is disabled. Mirrors
        // LitemallOrderBizModel.pay's demo-mode guard.
        if (payService.isEnabled()) {
            throw new NopException(ERR_RECHARGE_USE_REAL_PAYMENT).param("rechargeId", rechargeId);
        }
        LitemallRecharge recharge = requireRecharge(rechargeId, context);
        requireOwnership(recharge, context);
        if (recharge.getPayStatus() != _AppMallDaoConstants.PAY_STATUS_UNPAID) {
            throw new NopException(ERR_RECHARGE_NOT_ALLOW_CONFIRM)
                    .param("rechargeId", rechargeId)
                    .param("status", recharge.getPayStatus());
        }
        return creditRecharge(recharge, context);
    }

    @Override
    @BizAction
    public void confirmRechargeByNotify(@Name("outTradeNo") String outTradeNo,
                                        @Name("transactionId") String transactionId,
                                        IServiceContext context) {
        // Trusted entry invoked by PaymentCallbackImpl after WeChat signature verification.
        if (StringHelper.isEmpty(outTradeNo) || !outTradeNo.startsWith(OUT_TRADE_NO_PREFIX)) {
            return;
        }
        String rechargeId;
        try {
            rechargeId = Long.toString(Long.parseLong(outTradeNo.substring(OUT_TRADE_NO_PREFIX.length())));
        } catch (NumberFormatException e) {
            return;
        }
        LitemallRecharge recharge = get(rechargeId, false, context);
        if (recharge == null) {
            return;
        }
        // Idempotent: duplicate/replayed WeChat notifies for an already-paid recharge are a no-op.
        if (recharge.getPayStatus() != _AppMallDaoConstants.PAY_STATUS_UNPAID) {
            return;
        }
        creditRecharge(recharge, context);
    }

    private LitemallRecharge creditRecharge(LitemallRecharge recharge, IServiceContext context) {
        // Credit recharge amount + gift amount in one atomic operation. sourceId=rechargeId makes
        // the credit idempotent at the source level (dedup is the caller's responsibility via the
        // UNPAID->PAID status guard above). creditBalance lazy-creates the wallet if needed.
        BigDecimal total = nvl(recharge.getAmount()).add(nvl(recharge.getGiftAmount()));
        LitemallWallet wallet = walletBiz.creditBalance(recharge.getUserId(), total,
                _AppMallDaoConstants.WALLET_CHANGE_TYPE_RECHARGE,
                LitemallWalletBizModel.SOURCE_TYPE_RECHARGE,
                recharge.orm_idString(),
                "钱包充值 " + recharge.orm_idString(),
                context);

        // creditBalance internally calls clearEntitySessionCache for the optimistic-lock UPDATE,
        // which detaches the recharge entity from the session. Reload it fresh before updating.
        String rechargeId = recharge.orm_idString();
        LitemallRecharge fresh = get(rechargeId, false, context);
        fresh.setWalletId(wallet.orm_idString());
        fresh.setPayStatus(_AppMallDaoConstants.PAY_STATUS_PAID);
        updateEntity(fresh, "creditRecharge", context);
        return fresh;
    }

    static String deriveOutTradeNo(String rechargeId) {
        long id = Long.parseLong(rechargeId);
        return OUT_TRADE_NO_PREFIX + String.format("%08d", id);
    }

    private LitemallRecharge requireRecharge(String rechargeId, IServiceContext context) {
        LitemallRecharge recharge = get(rechargeId, false, context);
        if (recharge == null) {
            throw new NopException(ERR_RECHARGE_NOT_FOUND).param("rechargeId", rechargeId);
        }
        return recharge;
    }

    private void requireOwnership(LitemallRecharge recharge, IServiceContext context) {
        String userId = context.getUserId();
        if (!StringHelper.isEmpty(userId) && !userId.equals(recharge.getUserId())) {
            throw new NopException(ERR_RECHARGE_NOT_ALLOW_CONFIRM)
                    .param("rechargeId", recharge.orm_idString())
                    .param("reason", "not owner");
        }
    }

    private Map<String, Object> findPackage(String packageId, IServiceContext context) {
        for (Map<String, Object> pkg : getRechargePackages(context)) {
            if (packageId.equals(String.valueOf(pkg.get("id")))) {
                return pkg;
            }
        }
        return null;
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return ZERO;
        }
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? ZERO : value;
    }
}
