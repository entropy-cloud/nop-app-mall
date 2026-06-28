package app.mall.service.entity;

import app.mall.biz.ILitemallGoodsBiz;
import app.mall.biz.ILitemallPointsGoodsBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallPointsGoods;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.api.core.time.CoreMetrics;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static app.mall.service.AppMallErrors.ERR_POINTS_GOODS_NOT_ACTIVE;
import static app.mall.service.AppMallErrors.ERR_POINTS_GOODS_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_PROMOTION_STATUS_TRANSITION_INVALID;

@BizModel("LitemallPointsGoods")
public class LitemallPointsGoodsBizModel extends CrudBizModel<LitemallPointsGoods>
        implements ILitemallPointsGoodsBiz {

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 100;

    @Inject
    ILitemallGoodsBiz goodsBiz;

    public LitemallPointsGoodsBizModel() {
        setEntityName(LitemallPointsGoods.class.getName());
    }

    @Override
    @BizQuery
    public Map<String, Object> activePointsGoods(@Optional @Name("page") Integer page,
                                                  @Optional @Name("pageSize") Integer pageSize,
                                                  IServiceContext context) {
        int pageNum = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        // Storefront list: only ACTIVE points goods. Time-window enforcement is applied
        // at exchange time (authoritative); the list shows all ACTIVE goods for browsing.
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallPointsGoods.PROP_NAME_status,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE));
        query.addOrderField(LitemallPointsGoods.PROP_NAME_addTime, true);
        query.setOffset((pageNum - 1) * size);
        query.setLimit(size);

        List<LitemallPointsGoods> goodsList = findList(query, null, context);
        List<Map<String, Object>> cards = new ArrayList<>();
        LocalDateTime now = CoreMetrics.currentDateTime();
        if (goodsList != null) {
            for (LitemallPointsGoods goods : goodsList) {
                cards.add(toCard(goods, now, context));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("page", pageNum);
        result.put("pageSize", size);
        result.put("list", cards);
        return result;
    }

    @Override
    @BizQuery
    public Map<String, Object> pointsGoodsDetail(@Name("id") String id, IServiceContext context) {
        LitemallPointsGoods goods = get(id, false, context);
        if (goods == null || Boolean.TRUE.equals(goods.getDeleted())) {
            throw new NopException(ERR_POINTS_GOODS_NOT_FOUND).param("id", id);
        }
        LocalDateTime now = CoreMetrics.currentDateTime();
        return toCard(goods, now, context);
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallPointsGoods publishActivity(@Name("id") String id, IServiceContext context) {
        LitemallPointsGoods goods = requireEntity(id, null, context);
        Integer status = goods.getStatus();
        if (status != null && (status == _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE
                || status == _AppMallDaoConstants.PROMOTION_STATUS_FINISHED)) {
            throw new NopException(ERR_PROMOTION_STATUS_TRANSITION_INVALID)
                    .param("id", id)
                    .param("currentStatus", status)
                    .param("targetStatus", _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        }
        goods.setStatus(_AppMallDaoConstants.PROMOTION_STATUS_ACTIVE);
        return goods;
    }

    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallPointsGoods unpublishActivity(@Name("id") String id, IServiceContext context) {
        LitemallPointsGoods goods = requireEntity(id, null, context);
        Integer status = goods.getStatus();
        if (status != null && (status == _AppMallDaoConstants.PROMOTION_STATUS_CLOSED
                || status == _AppMallDaoConstants.PROMOTION_STATUS_DRAFT)) {
            throw new NopException(ERR_PROMOTION_STATUS_TRANSITION_INVALID)
                    .param("id", id)
                    .param("currentStatus", status)
                    .param("targetStatus", _AppMallDaoConstants.PROMOTION_STATUS_CLOSED);
        }
        goods.setStatus(_AppMallDaoConstants.PROMOTION_STATUS_CLOSED);
        return goods;
    }

    private Map<String, Object> toCard(LitemallPointsGoods goods, LocalDateTime now, IServiceContext context) {
        Map<String, Object> card = new HashMap<>();
        card.put("id", goods.orm_idString());
        card.put("goodsId", goods.getGoodsId());
        card.put("productId", goods.getProductId());
        card.put("pointsPrice", goods.getPointsPrice());
        card.put("exchangeStock", goods.getExchangeStock());
        card.put("exchangedCount", goods.getExchangedCount());
        card.put("maxPerUser", goods.getMaxPerUser());
        card.put("startTime", goods.getStartTime());
        card.put("endTime", goods.getEndTime());
        card.put("status", goods.getStatus());
        card.put("inWindow", isInWindow(goods, now));
        // Snapshot retail goods name + pic for storefront display (lazy join via IBiz).
        LitemallGoods retail = goodsBiz.get(goods.getGoodsId(), false, context);
        if (retail != null) {
            card.put("goodsName", retail.getName());
            card.put("picUrl", retail.getPicUrl());
            card.put("brief", retail.getBrief());
        }
        return card;
    }

    static boolean isInWindow(LitemallPointsGoods goods, LocalDateTime now) {
        LocalDateTime start = goods.getStartTime();
        LocalDateTime end = goods.getEndTime();
        if (start != null && now.isBefore(start)) {
            return false;
        }
        return end == null || !now.isAfter(end);
    }
}
