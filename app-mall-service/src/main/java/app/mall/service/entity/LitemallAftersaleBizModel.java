package app.mall.service.entity;

import app.mall.biz.ILitemallAftersaleBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.dao.AppMallDaoConstants;
import app.mall.dao.dto.AftersaleApplyRequest;
import app.mall.dao.entity.LitemallAftersale;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.manager.MallLogManager;
import app.mall.dao.mapper.LitemallGoodsProductMapper;
import app.mall.pay.PayRefundRequestBean;
import app.mall.pay.PayService;
import app.mall.service.consts.NotifyType;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.biz.RequestBean;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.DateHelper;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.integration.api.sms.ISmsSender;
import io.nop.integration.api.sms.SmsMessage;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_ALLOW_APPLY;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_ALLOW_CANCEL;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_ALLOW_REFUND;
import static app.mall.service.AppMallErrors.ERR_AFTERSALE_NOT_FOUND;

@BizModel("LitemallAftersale")
public class LitemallAftersaleBizModel extends CrudBizModel<LitemallAftersale> implements ILitemallAftersaleBiz {

    @Inject
    @Nullable
    ISmsSender smsSender;

    @Inject
    PayService payService;

    @Inject
    MallLogManager logManager;

    @Inject
    ILitemallOrderBiz orderBiz;

    @Inject
    LitemallGoodsProductMapper goodsProductMapper;

    public LitemallAftersaleBizModel() {
        setEntityName(LitemallAftersale.class.getName());
    }

    @BizMutation
    public void batchApprove(@Name("ids") Set<String> ids, IServiceContext context) {
        List<LitemallAftersale> list = batchGet(ids, false, context);

        for (LitemallAftersale entity : list) {
            int status = entity.getStatus();
            if (status != AppMallDaoConstants.AFTERSALE_STATUS_REQUEST) {
                continue;
            }
            entity.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_APPROVED);
            entity.setHandleTime(DateHelper.currentDateTime());

            entity.getOrder().setAftersaleStatus(entity.getStatus());
        }
    }

    @BizMutation
    public void batchReject(@Name("ids") Set<String> ids, IServiceContext context) {
        List<LitemallAftersale> list = batchGet(ids, false, context);

        for (LitemallAftersale entity : list) {
            int status = entity.getStatus();
            if (status != AppMallDaoConstants.AFTERSALE_STATUS_REQUEST) {
                continue;
            }

            entity.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_REJECT);
            entity.setHandleTime(DateHelper.currentDateTime());

            entity.getOrder().setAftersaleStatus(entity.getStatus());
        }
    }

    @BizMutation
    public void refund(@Name("id") String id, IServiceContext context) {
        LitemallAftersale entity = get(id, false, context);
        int status = entity.getStatus();
        if (status != AppMallDaoConstants.AFTERSALE_STATUS_APPROVED) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_REFUND);
        }

        LitemallOrder order = entity.getOrder();

        PayRefundRequestBean wxPayRefundRequest = new PayRefundRequestBean();
        wxPayRefundRequest.setOutTradeNo(order.getOrderSn());
        wxPayRefundRequest.setOutRefundNo("refund_" + order.getOrderSn());
        wxPayRefundRequest.setTotalFee(order.getActualPrice());
        wxPayRefundRequest.setRefundFee(entity.getAmount());

        payService.refund(wxPayRefundRequest);

        entity.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_REFUND);
        entity.setHandleTime(DateHelper.currentDateTime());
        entity.getOrder().setAftersaleStatus(entity.getStatus());

        if (entity.getType() == AppMallDaoConstants.AFTERSALE_TYPE_GOODS_REQUIRED) {
            Set<LitemallOrderGoods> orderGoodsList = entity.getOrder().getOrderGoods();
            for (LitemallOrderGoods orderGoods : orderGoodsList) {
                String productId = orderGoods.getProductId();
                Integer number = orderGoods.getNumber();
                goodsProductMapper.addStock(productId, number);
            }
        }

        if (smsSender != null) {
            SmsMessage smsMessage = new SmsMessage();
            smsMessage.setMobile(order.getMobile());
            smsMessage.setType(NotifyType.REFUND.ordinal());
            smsMessage.setParams(Arrays.asList(StringHelper.tail(order.getOrderSn(), 6)));
            smsSender.sendMessage(smsMessage);
        }

        logManager.logOrderSucceed("退款", "订单编号 " + order.getOrderSn() + " 售后编号 " + entity.getAftersaleSn());
    }

    @Override
    @BizMutation
    public LitemallAftersale apply(@RequestBean AftersaleApplyRequest request,
                                    IServiceContext context) {
        String orderId = request.getOrderId();
        String userId = context.getUserId();

        LitemallOrder order = orderBiz.get(orderId, false, context);
        if (order == null || Boolean.TRUE.equals(order.getDeleted())) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_APPLY)
                    .param("orderId", orderId);
        }

        int orderStatus = order.getOrderStatus();
        if (orderStatus != AppMallDaoConstants.ORDER_STATUS_PAY
                && orderStatus != AppMallDaoConstants.ORDER_STATUS_CONFIRM
                && orderStatus != AppMallDaoConstants.ORDER_STATUS_AUTO_CONFIRM) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_APPLY)
                    .param("orderId", orderId)
                    .param("orderStatus", orderStatus);
        }

        if (order.getAftersaleStatus() != null
                && order.getAftersaleStatus() != AppMallDaoConstants.AFTERSALE_STATUS_INIT) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_APPLY)
                    .param("orderId", orderId);
        }

        LitemallAftersale aftersale = newEntity();
        aftersale.setAftersaleSn(generateAftersaleSn());
        aftersale.setOrderId(orderId);
        aftersale.setUserId(userId);
        aftersale.setType(request.getType());
        aftersale.setReason(request.getReason());
        aftersale.setAmount(request.getAmount() != null ? request.getAmount() : order.getActualPrice());
        aftersale.setPictures(request.getPictures());
        aftersale.setComment(request.getComment());
        aftersale.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_REQUEST);
        aftersale.setAddTime(LocalDateTime.now());
        aftersale.setUpdateTime(LocalDateTime.now());
        aftersale.setDeleted(false);

        order.setAftersaleStatus(AppMallDaoConstants.AFTERSALE_STATUS_REQUEST);

        saveEntity(aftersale, "apply", context);
        return aftersale;
    }

    @Override
    @BizMutation
    public LitemallAftersale cancel(@Name("id") String id,
                                    IServiceContext context) {
        LitemallAftersale aftersale = get(id, false, context);
        if (aftersale == null) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_CANCEL)
                    .param("id", id);
        }

        if (aftersale.getStatus() != AppMallDaoConstants.AFTERSALE_STATUS_REQUEST) {
            throw new NopException(ERR_AFTERSALE_NOT_ALLOW_CANCEL)
                    .param("id", id)
                    .param("status", aftersale.getStatus());
        }

        aftersale.setStatus(AppMallDaoConstants.AFTERSALE_STATUS_CANCELLED);

        LitemallOrder order = aftersale.getOrder();
        if (order != null) {
            order.setAftersaleStatus(AppMallDaoConstants.AFTERSALE_STATUS_INIT);
        }

        updateEntity(aftersale, "cancel", context);
        return aftersale;
    }

    @Override
    @BizQuery
    public List<LitemallAftersale> userList(IServiceContext context) {
        String userId = context.getUserId();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallAftersale.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallAftersale.PROP_NAME_deleted, false));
        query.addOrderField(LitemallAftersale.PROP_NAME_addTime, true);
        return findList(query, null, context);
    }

    @Override
    @BizQuery
    public LitemallAftersale userDetail(@Name("id") String id,
                                         IServiceContext context) {
        LitemallAftersale aftersale = get(id, false, context);
        if (aftersale == null || Boolean.TRUE.equals(aftersale.getDeleted())) {
            throw new NopException(ERR_AFTERSALE_NOT_FOUND)
                    .param("id", id);
        }
        if (!aftersale.getUserId().equals(context.getUserId())) {
            throw new NopException(ERR_AFTERSALE_NOT_FOUND)
                    .param("id", id);
        }
        return aftersale;
    }

    private String generateAftersaleSn() {
        return StringHelper.generateUUID();
    }
}
