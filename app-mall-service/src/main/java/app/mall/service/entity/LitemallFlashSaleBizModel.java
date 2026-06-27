package app.mall.service.entity;

import app.mall.biz.ILitemallAddressBiz;
import app.mall.biz.ILitemallFlashSaleBiz;
import app.mall.biz.ILitemallFlashSaleSessionBiz;
import app.mall.biz.ILitemallGoodsBiz;
import app.mall.biz.ILitemallGoodsProductBiz;
import app.mall.biz.ILitemallOrderBiz;
import app.mall.biz.ILitemallOrderGoodsBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.dao._AppMallDaoConstants;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallFlashSale;
import app.mall.dao.entity.LitemallFlashSaleSession;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import app.mall.dao.entity.LitemallOrder;
import app.mall.dao.entity.LitemallOrderGoods;
import app.mall.dao.mapper.LitemallFlashSaleSessionMapper;
import app.mall.dao.mapper.LitemallGoodsProductMapper;
import app.mall.service.notification.MallNotificationService;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.api.core.time.CoreMetrics;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static app.mall.service.AppMallErrors.ERR_FLASH_SALE_GOODS_OFF_SHELF;
import static app.mall.service.AppMallErrors.ERR_FLASH_SALE_GOODS_PRODUCT_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_FLASH_SALE_NOT_ACTIVE;
import static app.mall.service.AppMallErrors.ERR_FLASH_SALE_OVER_LIMIT_PER_ORDER;
import static app.mall.service.AppMallErrors.ERR_FLASH_SALE_PRODUCT_NOT_IN_ACTIVITY;
import static app.mall.service.AppMallErrors.ERR_FLASH_SALE_SESSION_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_FLASH_SALE_SESSION_NOT_IN_WINDOW;
import static app.mall.service.AppMallErrors.ERR_FLASH_SALE_SOLD_OUT;
import static app.mall.service.AppMallErrors.ERR_ORDER_ADDRESS_INVALID;
import static app.mall.service.AppMallErrors.ERR_ORDER_STOCK_INSUFFICIENT;

@BizModel("LitemallFlashSale")
public class LitemallFlashSaleBizModel extends CrudBizModel<LitemallFlashSale> implements ILitemallFlashSaleBiz {

    static final Logger LOG = LoggerFactory.getLogger(LitemallFlashSaleBizModel.class);

    static final int SESSION_STATUS_NOT_STARTED = 0;
    static final int SESSION_STATUS_IN_PROGRESS = 1;
    static final int SESSION_STATUS_FINISHED = 2;

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 100;

    @Inject
    ILitemallAddressBiz addressBiz;

    @Inject
    ILitemallGoodsBiz goodsBiz;

    @Inject
    ILitemallGoodsProductBiz goodsProductBiz;

    @Inject
    ILitemallOrderGoodsBiz orderGoodsBiz;

    @Inject
    ILitemallOrderBiz orderBiz;

    @Inject
    ILitemallFlashSaleSessionBiz flashSaleSessionBiz;

    @Inject
    ILitemallSystemBiz systemBiz;

    @Inject
    LitemallFlashSaleSessionMapper flashSaleSessionMapper;

    @Inject
    LitemallGoodsProductMapper goodsProductMapper;

    @Inject
    MallNotificationService notificationService;

    public LitemallFlashSaleBizModel() {
        setEntityName(LitemallFlashSale.class.getName());
    }

    @Override
    @BizMutation
    public LitemallOrder flashSaleBuy(@Name("flashSaleSessionId") String flashSaleSessionId,
                                       @Name("addressId") String addressId,
                                       @Optional @Name("productId") String productId,
                                       @Name("number") Integer number,
                                       IServiceContext context) {
        // Decision A (Phase 1): independent flashSaleBuy path. Does NOT enter submit() —
        // therefore it does not interact with coupon / promotion / integral / groupon slots.
        // See docs/design/marketing-and-promotions.md 秒杀章节 "下单路径".
        String userId = context.getUserId();

        if (number == null || number <= 0) {
            throw new NopException(ERR_FLASH_SALE_OVER_LIMIT_PER_ORDER)
                    .param("requested", number);
        }

        LitemallFlashSaleSession session = requireSession(flashSaleSessionId, context);
        LitemallFlashSale activity = requireActivity(session.getFlashSaleId(), context);

        // Activity status guard: only ACTIVE activities are buyable.
        if (activity.getStatus() == null
                || activity.getStatus() != _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE) {
            throw new NopException(ERR_FLASH_SALE_NOT_ACTIVE)
                    .param("activityId", activity.orm_idString())
                    .param("status", activity.getStatus());
        }

        // Time-window guard (job boundary is best-effort; this is the authoritative check).
        LocalDateTime now = CoreMetrics.currentDateTime();
        if (!isInSessionWindow(session, now)) {
            throw new NopException(ERR_FLASH_SALE_SESSION_NOT_IN_WINDOW)
                    .param("sessionId", session.orm_idString())
                    .param("sessionStart", session.getSessionStart())
                    .param("sessionEnd", session.getSessionEnd());
        }

        LitemallGoods goods = goodsBiz.get(activity.getGoodsId(), false, context);
        if (goods == null || Boolean.TRUE.equals(goods.getDeleted())) {
            throw new NopException(ERR_FLASH_SALE_GOODS_OFF_SHELF)
                    .param("goodsId", activity.getGoodsId());
        }
        if (!Boolean.TRUE.equals(goods.getIsOnSale())) {
            throw new NopException(ERR_FLASH_SALE_GOODS_OFF_SHELF)
                    .param("goodsId", activity.getGoodsId());
        }

        // Resolve SKU: flashSaleBuy is a single-SKU direct buy path.
        // activity.productId == null => activity covers all SKUs of the goods (caller must supply productId).
        // activity.productId != null => activity is SKU-specific (productId arg must match).
        String resolvedProductId;
        if (activity.getProductId() != null) {
            if (productId != null && !productId.equals(activity.getProductId())) {
                throw new NopException(ERR_FLASH_SALE_PRODUCT_NOT_IN_ACTIVITY)
                        .param("productId", productId)
                        .param("activityProductId", activity.getProductId());
            }
            resolvedProductId = activity.getProductId();
        } else {
            if (productId == null || productId.isEmpty()) {
                // Activity covers all SKUs but the caller did not specify one. We cannot place a
                // flash sale order without a concrete SKU (stock deduction requires a product row).
                throw new NopException(ERR_FLASH_SALE_GOODS_PRODUCT_NOT_FOUND);
            }
            resolvedProductId = productId;
        }

        LitemallGoodsProduct product = goodsProductBiz.get(resolvedProductId, false, context);
        if (product == null || Boolean.TRUE.equals(product.getDeleted())) {
            throw new NopException(ERR_FLASH_SALE_GOODS_PRODUCT_NOT_FOUND)
                    .param("productId", resolvedProductId);
        }
        if (!Objects.equals(product.getGoodsId(), activity.getGoodsId())) {
            throw new NopException(ERR_FLASH_SALE_PRODUCT_NOT_IN_ACTIVITY)
                    .param("productId", resolvedProductId)
                    .param("activityGoodsId", activity.getGoodsId());
        }

        // maxPerOrder guard (Decision B — maxPerUser deferred as model-gap per Phase 1).
        if (activity.getMaxPerOrder() != null && activity.getMaxPerOrder() > 0
                && number > activity.getMaxPerOrder()) {
            throw new NopException(ERR_FLASH_SALE_OVER_LIMIT_PER_ORDER)
                    .param("requested", number)
                    .param("maxPerOrder", activity.getMaxPerOrder());
        }

        // Address validation (mirrors submit() rule: must belong to current user).
        LitemallAddress address = addressBiz.get(addressId, false, context);
        if (address == null || Boolean.TRUE.equals(address.getDeleted())) {
            throw new NopException(ERR_ORDER_ADDRESS_INVALID)
                    .param("addressId", addressId);
        }
        if (!Objects.equals(userId, address.getUserId())) {
            throw new NopException(ERR_ORDER_ADDRESS_INVALID)
                    .param("addressId", addressId)
                    .param("userId", userId);
        }

        // Note: do NOT call dao().clearEntitySessionCache() here. Doing so evicts ALL entities
        // from the session (including order/orderGoods saved below), which causes them to be
        // re-inserted at flush time → duplicate key / data-integrity-violation. The mapper
        // UPDATEs work correctly without clearing because they run through a separate SqlLib
        // path that executes real SQL regardless of the session cache state.

        // Atomic session stock deduction (Decision A). null/0 sessionStock => unlimited,
        // per Phase 1 semantics (aligned with P23 stockLimit=0).
        Integer sessionStock = session.getSessionStock();
        if (sessionStock != null && sessionStock > 0) {
            int affected = flashSaleSessionMapper.reduceFlashSaleSessionStock(
                    session.orm_idString(), number);
            if (affected == 0) {
                throw new NopException(ERR_FLASH_SALE_SOLD_OUT)
                        .param("sessionId", session.orm_idString())
                        .param("requested", number)
                        .param("sessionStock", sessionStock);
            }
        }

        // Atomic goods product stock deduction (same pattern as submit()).
        int productAffected = goodsProductMapper.reduceStock(resolvedProductId, number);
        if (productAffected == 0) {
            throw new NopException(ERR_ORDER_STOCK_INSUFFICIENT)
                    .param("productId", resolvedProductId)
                    .param("requested", number);
        }

        // Build the flash sale order via OrderBizModel (the Order entity's owning BizModel).
        // Creating the order from FlashSaleBizModel via raw daoProvider/saveEntity fails at
        // transaction commit because the relation cascade (order -> orderGoods) needs to run
        // within OrderBizModel's saveEntity pipeline. Delegating the order creation to
        // orderBiz.createFlashSaleOrder keeps the relation cascade in the correct context and
        // avoids the cross-BizModel INSERT ordering problem.
        BigDecimal flashPrice = activity.getFlashPrice();
        BigDecimal freightPrice = resolveFreightPrice(context);

        String fullAddress = (address.getProvince() != null ? address.getProvince() : "")
                + (address.getCity() != null ? address.getCity() : "")
                + (address.getCounty() != null ? address.getCounty() : "")
                + (address.getAddressDetail() != null ? address.getAddressDetail() : "");

        LitemallOrder order = orderBiz.createFlashSaleOrder(
                userId,
                activity.getGoodsId(),
                resolvedProductId,
                goods.getName(),
                goods.getGoodsSn(),
                product.getSpecifications(),
                goods.getPicUrl(),
                flashPrice,
                number,
                address.getName(),
                address.getTel(),
                fullAddress,
                freightPrice,
                context);

        return order;
    }

    @Override
    @BizQuery
    public Map<String, Object> activeFlashSales(@Optional @Name("page") Integer page,
                                                  @Optional @Name("pageSize") Integer pageSize,
                                                  IServiceContext context) {
        int pageNum = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        // Pull ACTIVE activities only; group their sessions by sessionStatus into three buckets
        // (即将开始 / 进行中 / 已结束) for the storefront list.
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallFlashSale.PROP_NAME_status,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE));
        query.addOrderField(LitemallFlashSale.PROP_NAME_addTime, true);
        query.setOffset((pageNum - 1) * size);
        query.setLimit(size);

        List<LitemallFlashSale> activities = findList(query, null, context);

        List<Map<String, Object>> upcoming = new ArrayList<>();
        List<Map<String, Object>> ongoing = new ArrayList<>();
        List<Map<String, Object>> finished = new ArrayList<>();

        if (activities != null) {
            LocalDateTime now = CoreMetrics.currentDateTime();
            for (LitemallFlashSale activity : activities) {
                Map<String, Object> card = toActivityCard(activity, now, context);
                if (card == null) continue;
                // Use the most relevant live session to bucket the activity card.
                Integer bucket = (Integer) card.get("bucketStatus");
                if (bucket == null) continue;
                if (bucket == SESSION_STATUS_NOT_STARTED) {
                    upcoming.add(card);
                } else if (bucket == SESSION_STATUS_IN_PROGRESS) {
                    ongoing.add(card);
                } else {
                    finished.add(card);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("page", pageNum);
        result.put("pageSize", size);
        result.put("upcoming", upcoming);
        result.put("ongoing", ongoing);
        result.put("finished", finished);
        return result;
    }

    @Override
    @BizQuery
    public Map<String, Object> flashSaleDetail(@Name("id") String id, IServiceContext context) {
        LitemallFlashSale activity = get(id, false, context);
        if (activity == null || Boolean.TRUE.equals(activity.getDeleted())) {
            return null;
        }
        LocalDateTime now = CoreMetrics.currentDateTime();
        return toDetailMap(activity, now, context, true);
    }

    @Override
    @BizQuery
    public Map<String, Object> flashSaleForGoods(@Name("goodsId") String goodsId,
                                                   @Optional @Name("productId") String productId,
                                                   IServiceContext context) {
        if (goodsId == null || goodsId.isEmpty()) {
            return null;
        }
        // Find the most relevant ACTIVE flash sale for this goods (with an in-progress session).
        // Used by the storefront goods-detail page banner.
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallFlashSale.PROP_NAME_goodsId, goodsId));
        query.addFilter(FilterBeans.eq(LitemallFlashSale.PROP_NAME_status,
                _AppMallDaoConstants.PROMOTION_STATUS_ACTIVE));
        query.setLimit(50);

        List<LitemallFlashSale> activities = findList(query, null, context);
        if (activities == null || activities.isEmpty()) {
            return null;
        }
        LocalDateTime now = CoreMetrics.currentDateTime();
        for (LitemallFlashSale activity : activities) {
            // SKU-specific activity must match productId; null-productId activity covers all SKUs.
            if (activity.getProductId() != null
                    && productId != null
                    && !activity.getProductId().equals(productId)) {
                continue;
            }
            Map<String, Object> detail = toDetailMap(activity, now, context, false);
            if (detail == null) continue;
            // Only return the activity when it has an in-progress session for the banner.
            Integer liveStatus = (Integer) detail.get("liveSessionStatus");
            if (liveStatus != null && liveStatus == SESSION_STATUS_IN_PROGRESS) {
                return detail;
            }
        }
        return null;
    }

    private LitemallFlashSaleSession requireSession(String sessionId, IServiceContext context) {
        if (StringHelper.isEmpty(sessionId)) {
            throw new NopException(ERR_FLASH_SALE_SESSION_NOT_FOUND);
        }
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallFlashSaleSession.PROP_NAME_id, sessionId));
        LitemallFlashSaleSession session = flashSaleSessionBiz.findFirst(query, null, context);
        if (session == null || Boolean.TRUE.equals(session.getDeleted())) {
            throw new NopException(ERR_FLASH_SALE_SESSION_NOT_FOUND)
                    .param("sessionId", sessionId);
        }
        return session;
    }

    private LitemallFlashSale requireActivity(String activityId, IServiceContext context) {
        LitemallFlashSale activity = get(activityId, false, context);
        if (activity == null || Boolean.TRUE.equals(activity.getDeleted())) {
            throw new NopException(ERR_FLASH_SALE_NOT_ACTIVE)
                    .param("activityId", activityId);
        }
        return activity;
    }

    private boolean isInSessionWindow(LitemallFlashSaleSession session, LocalDateTime now) {
        LocalDateTime start = session.getSessionStart();
        LocalDateTime end = session.getSessionEnd();
        if (start != null && now.isBefore(start)) {
            return false;
        }
        return end == null || !now.isAfter(end);
    }

    private int computeSessionStatus(LitemallFlashSaleSession session, LocalDateTime now) {
        LocalDateTime start = session.getSessionStart();
        LocalDateTime end = session.getSessionEnd();
        if (start != null && now.isBefore(start)) {
            return SESSION_STATUS_NOT_STARTED;
        }
        if (end != null && now.isAfter(end)) {
            return SESSION_STATUS_FINISHED;
        }
        return SESSION_STATUS_IN_PROGRESS;
    }

    private BigDecimal resolveFreightPrice(IServiceContext context) {
        String freightConfig = systemBiz.getConfig("mall_freight_price", context);
        if (freightConfig != null && !freightConfig.isEmpty()) {
            try {
                return new BigDecimal(freightConfig);
            } catch (NumberFormatException ignored) {
                // Fall through to default zero — malformed config should not break checkout.
            }
        }
        return BigDecimal.ZERO;
    }

    private String generateOrderSn() {
        return StringHelper.generateUUID();
    }

    private List<LitemallFlashSaleSession> loadSessions(LitemallFlashSale activity,
                                                         IServiceContext context) {
        QueryBean q = new QueryBean();
        q.addFilter(FilterBeans.eq(LitemallFlashSaleSession.PROP_NAME_flashSaleId,
                activity.orm_idString()));
        q.addOrderField(LitemallFlashSaleSession.PROP_NAME_sessionStart, false);
        return flashSaleSessionBiz.findList(q, null, context);
    }

    private Map<String, Object> toActivityCard(LitemallFlashSale activity, LocalDateTime now,
                                                 IServiceContext context) {
        List<LitemallFlashSaleSession> sessions = loadSessions(activity, context);
        if (sessions == null || sessions.isEmpty()) {
            return null;
        }
        LitemallFlashSaleSession live = pickLiveSession(sessions, now);
        if (live == null) {
            return null;
        }
        Map<String, Object> card = new HashMap<>();
        card.put("id", activity.orm_idString());
        card.put("goodsId", activity.getGoodsId());
        card.put("productId", activity.getProductId());
        card.put("flashPrice", activity.getFlashPrice());
        card.put("totalStock", activity.getTotalStock());
        card.put("maxPerOrder", activity.getMaxPerOrder());
        card.put("maxPerUser", activity.getMaxPerUser());
        card.put("liveSessionId", live.orm_idString());
        card.put("sessionStart", live.getSessionStart());
        card.put("sessionEnd", live.getSessionEnd());
        card.put("sessionStock", live.getSessionStock());
        int bucket = computeSessionStatus(live, now);
        card.put("bucketStatus", bucket);
        card.put("liveSessionStatus", bucket);
        return card;
    }

    private LitemallFlashSaleSession pickLiveSession(List<LitemallFlashSaleSession> sessions,
                                                      LocalDateTime now) {
        // Prefer an in-progress session; else the soonest-upcoming; else the latest finished.
        LitemallFlashSaleSession ongoing = null;
        LitemallFlashSaleSession upcoming = null;
        LitemallFlashSaleSession latestFinished = null;
        for (LitemallFlashSaleSession s : sessions) {
            int status = computeSessionStatus(s, now);
            if (status == SESSION_STATUS_IN_PROGRESS) {
                if (ongoing == null
                        || (s.getSessionEnd() != null && ongoing.getSessionEnd() != null
                        && s.getSessionEnd().isBefore(ongoing.getSessionEnd()))) {
                    ongoing = s;
                }
            } else if (status == SESSION_STATUS_NOT_STARTED) {
                if (upcoming == null
                        || (s.getSessionStart() != null && upcoming.getSessionStart() != null
                        && s.getSessionStart().isBefore(upcoming.getSessionStart()))) {
                    upcoming = s;
                }
            } else {
                if (latestFinished == null
                        || (s.getSessionEnd() != null && latestFinished.getSessionEnd() != null
                        && s.getSessionEnd().isAfter(latestFinished.getSessionEnd()))) {
                    latestFinished = s;
                }
            }
        }
        if (ongoing != null) return ongoing;
        if (upcoming != null) return upcoming;
        return latestFinished;
    }

    private Map<String, Object> toDetailMap(LitemallFlashSale activity, LocalDateTime now,
                                             IServiceContext context, boolean includeAllSessions) {
        List<LitemallFlashSaleSession> sessions = loadSessions(activity, context);
        if (sessions == null || sessions.isEmpty()) {
            return null;
        }
        LitemallFlashSaleSession live = pickLiveSession(sessions, now);
        Integer liveStatus = live != null ? computeSessionStatus(live, now) : null;

        Map<String, Object> result = new HashMap<>();
        result.put("id", activity.orm_idString());
        result.put("goodsId", activity.getGoodsId());
        result.put("productId", activity.getProductId());
        result.put("flashPrice", activity.getFlashPrice());
        result.put("totalStock", activity.getTotalStock());
        result.put("maxPerOrder", activity.getMaxPerOrder());
        result.put("maxPerUser", activity.getMaxPerUser());
        result.put("status", activity.getStatus());
        result.put("liveSessionId", live != null ? live.orm_idString() : null);
        result.put("liveSessionStatus", liveStatus);
        result.put("sessionStart", live != null ? live.getSessionStart() : null);
        result.put("sessionEnd", live != null ? live.getSessionEnd() : null);
        result.put("sessionStock", live != null ? live.getSessionStock() : null);

        if (includeAllSessions) {
            List<Map<String, Object>> sessionList = new ArrayList<>();
            for (LitemallFlashSaleSession s : sessions) {
                Map<String, Object> sm = new HashMap<>();
                sm.put("id", s.orm_idString());
                sm.put("sessionStart", s.getSessionStart());
                sm.put("sessionEnd", s.getSessionEnd());
                sm.put("sessionStock", s.getSessionStock());
                sm.put("sessionStatus", computeSessionStatus(s, now));
                sessionList.add(sm);
            }
            result.put("sessions", sessionList);
        }
        return result;
    }
}
