package app.mall.service.entity;

import app.mall.biz.ILitemallCommentBiz;
import app.mall.biz.ILitemallOrderGoodsBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallComment;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.mapper.LitemallOrderGoodsMapper;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallComment")
public class LitemallCommentBizModel extends CrudBizModel<LitemallComment> implements ILitemallCommentBiz {

    @Inject
    ILitemallOrderGoodsBiz orderGoodsBiz;

    @Inject
    LitemallOrderGoodsMapper orderGoodsMapper;

    public LitemallCommentBizModel() {
        setEntityName(LitemallComment.class.getName());
    }

    @Override
    @BizMutation
    public LitemallComment submitComment(@Name("orderGoodsId") String orderGoodsId,
                                          @Name("content") String content,
                                          @Name("star") int star,
                                          @Optional @Name("hasPicture") Boolean hasPicture,
                                          @Optional @Name("picUrls") String picUrls,
                                          IServiceContext context) {
        String userId = context.getUserId();

        LitemallOrderGoods orderGoods = orderGoodsBiz.get(orderGoodsId, false, context);
        if (orderGoods == null) {
            throw new NopException(ERR_COMMENT_ORDER_GOODS_NOT_FOUND)
                    .param("orderGoodsId", orderGoodsId);
        }

        LitemallOrder order = orderGoods.getOrder();
        int orderStatus = order.getOrderStatus();
        if (orderStatus != _AppMallDaoConstants.ORDER_STATUS_CONFIRM
                && orderStatus != _AppMallDaoConstants.ORDER_STATUS_AUTO_CONFIRM) {
            throw new NopException(ERR_COMMENT_ORDER_NOT_RECEIVED)
                    .param("orderGoodsId", orderGoodsId)
                    .param("orderStatus", orderStatus);
        }

        if (!order.getUserId().equals(userId)) {
            throw new NopException(ERR_COMMENT_NOT_OWNER)
                    .param("orderGoodsId", orderGoodsId);
        }

        Integer commentFlag = orderGoods.getComment();
        if (commentFlag != null && commentFlag == -1) {
            throw new NopException(ERR_COMMENT_EXPIRED)
                    .param("orderGoodsId", orderGoodsId);
        }
        if (commentFlag != null && commentFlag > 0) {
            throw new NopException(ERR_COMMENT_ALREADY_EXISTS)
                    .param("orderGoodsId", orderGoodsId);
        }

        LitemallComment comment = newEntity();
        comment.setType(0);
        comment.setValueId(orderGoods.getGoodsId());
        comment.setContent(content);
        comment.setUserId(userId);
        comment.setStar(star);
        comment.setHasPicture(Boolean.TRUE.equals(hasPicture));
        if (picUrls != null) {
            comment.setPicUrls(picUrls);
        }
        saveEntity(comment, null, context);

        // Atomic conditional UPDATE: win the "first comment" race only if orderGoods.comment is still 0.
        // Affects 0 means a concurrent submission already claimed this slot.
        int affected = orderGoodsMapper.updateCommentFlagIfUnused(orderGoodsId, comment.orm_idString());
        if (affected == 0) {
            throw new NopException(ERR_COMMENT_ALREADY_EXISTS)
                    .param("orderGoodsId", orderGoodsId);
        }

        if (order.getComments() != null && order.getComments() > 0) {
            order.setComments(order.getComments() - 1);
        }

        return comment;
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallComment> commentList(@Name("type") int type,
                                                  @Name("valueId") String valueId,
                                                  @Name("page") int page,
                                                  @Name("pageSize") int pageSize,
                                                  IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallComment.PROP_NAME_type, type));
        query.addFilter(FilterBeans.eq(LitemallComment.PROP_NAME_valueId, valueId));
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallComment.PROP_NAME_addTime, true);

        return findPage(query, null, context);
    }

    @Override
    @BizQuery
    public PageBean<LitemallComment> myComments(@Name("page") int page,
                                                 @Name("pageSize") int pageSize,
                                                 IServiceContext context) {
        String userId = context.getUserId();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallComment.PROP_NAME_userId, userId));
        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallComment.PROP_NAME_addTime, true);

        return findPage(query, null, context);
    }

    @Override
    @BizMutation
    public LitemallComment adminReply(@Name("id") String id,
                                       @Name("adminContent") String adminContent,
                                       IServiceContext context) {
        LitemallComment comment = requireEntity(id, null, context);
        comment.setAdminContent(adminContent);
        return comment;
    }
}
