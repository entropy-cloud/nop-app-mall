package app.mall.service;

import io.nop.api.core.exceptions.ErrorCode;

import static io.nop.api.core.exceptions.ErrorCode.define;

public interface AppMallErrors {
    ErrorCode ERR_AFTERSALE_NOT_ALLOW_REFUND =
            define("nop.err.mall.aftersale.not-allow-refund",
                    "售后不能进行退款操作");

    // 购物车错误码
    ErrorCode ERR_CART_STOCK_INSUFFICIENT =
            define("nop.err.mall.cart.stock-insufficient",
                    "商品库存不足");

    ErrorCode ERR_CART_PRODUCT_NOT_FOUND =
            define("nop.err.mall.cart.product-not-found",
                    "商品货品不存在");

    ErrorCode ERR_CART_NOT_FOUND =
            define("nop.err.mall.cart.cart-not-found",
                    "购物车记录不存在");

    // 订单错误码
    ErrorCode ERR_ORDER_NOT_ALLOW_CANCEL =
            define("nop.err.mall.order.not-allow-cancel",
                    "当前订单状态不允许取消");

    ErrorCode ERR_ORDER_NOT_ALLOW_SHIP =
            define("nop.err.mall.order.not-allow-ship",
                    "当前订单状态不允许发货");

    ErrorCode ERR_ORDER_NOT_ALLOW_CONFIRM =
            define("nop.err.mall.order.not-allow-confirm",
                    "当前订单状态不允许确认收货");

    ErrorCode ERR_ORDER_STOCK_INSUFFICIENT =
            define("nop.err.mall.order.stock-insufficient",
                    "订单商品库存不足");

    ErrorCode ERR_ORDER_CART_EMPTY =
            define("nop.err.mall.order.cart-empty",
                    "购物车中没有已勾选的商品");

    ErrorCode ERR_ORDER_ADDRESS_INVALID =
            define("nop.err.mall.order.address-invalid",
                    "收货地址无效");

    ErrorCode ERR_ORDER_NOT_FOUND =
            define("nop.err.mall.order.not-found",
                    "订单不存在");

    ErrorCode ERR_ORDER_NOT_ALLOW_PAY =
            define("nop.err.mall.order.not-allow-pay",
                    "当前订单状态不允许支付");

    ErrorCode ERR_ORDER_USE_REAL_PAYMENT =
            define("nop.err.mall.order.use-real-payment",
                    "真实支付模式非零金额订单必须通过微信扫码支付，不可直接确认");

    ErrorCode ERR_ORDER_NOT_ALLOW_DELETE =
            define("nop.err.mall.order.not-allow-delete",
                    "当前订单状态不允许删除");

    ErrorCode ERR_AFTERSALE_NOT_FOUND =
            define("nop.err.mall.aftersale.not-found",
                    "售后记录不存在");

    // 售后错误码
    ErrorCode ERR_AFTERSALE_NOT_ALLOW_APPLY =
            define("nop.err.mall.aftersale.not-allow-apply",
                    "当前订单不允许申请售后");

    ErrorCode ERR_AFTERSALE_NOT_ALLOW_CANCEL =
            define("nop.err.mall.aftersale.not-allow-cancel",
                    "当前售后状态不允许取消");

    ErrorCode ERR_AFTERSALE_REFUND_FAILED =
            define("nop.err.mall.aftersale.refund-failed",
                    "退款失败");

    ErrorCode ERR_AFTERSALE_AMOUNT_EXCEED =
            define("nop.err.mall.aftersale.amount-exceed",
                    "退款金额超过订单实付金额");

    ErrorCode ERR_AFTERSALE_ITEM_NOT_IN_ORDER =
            define("nop.err.mall.aftersale.item-not-in-order",
                    "售后商品项不属于该订单");

    ErrorCode ERR_AFTERSALE_ITEM_IN_PROGRESS =
            define("nop.err.mall.aftersale.item-in-progress",
                    "该商品项已有进行中的售后");

    ErrorCode ERR_AFTERSALE_TYPE_STATUS_MISMATCH =
            define("nop.err.mall.aftersale.type-status-mismatch",
                    "售后类型与订单状态不匹配");

    ErrorCode ERR_AFTERSALE_REASON_INVALID =
            define("nop.err.mall.aftersale.reason-invalid",
                    "售后原因不在字典选项内");

    String ARG_USERNAME = "username";

    ErrorCode ERR_USER_USERNAME_EXISTS =
            define("nop.err.mall.user.username-exists",
                    "用户名已注册", ARG_USERNAME);

    ErrorCode ERR_USER_USERNAME_EMPTY =
            define("nop.err.mall.user.username-empty",
                    "用户名不能为空");

    ErrorCode ERR_USER_PASSWORD_EMPTY =
            define("nop.err.mall.user.password-empty",
                    "密码不能为空");

    ErrorCode ERR_USER_MOBILE_EMPTY =
            define("nop.err.mall.user.mobile-empty",
                    "手机号不能为空");

    ErrorCode ERR_USER_USERNAME_TOO_SHORT =
            define("nop.err.mall.user.username-too-short",
                    "用户名长度不能少于2个字符");

    ErrorCode ERR_USER_USERNAME_TOO_LONG =
            define("nop.err.mall.user.username-too-long",
                    "用户名长度不能超过63个字符");

    ErrorCode ERR_CATEGORY_HAS_CHILDREN =
            define("nop.err.mall.category.has-children",
                    "分类下有子分类，不可删除");

    ErrorCode ERR_CATEGORY_HAS_PRODUCTS =
            define("nop.err.mall.category.has-products",
                    "分类下有商品，不可删除");

    ErrorCode ERR_CATEGORY_NOT_LEAF =
            define("nop.err.mall.category.not-leaf",
                    "非叶子分类不可关联商品");

    ErrorCode ERR_GOODS_NO_SKU =
            define("nop.err.mall.goods.no-sku",
                    "商品无SKU不可上架");

    ErrorCode ERR_GOODS_NOT_ON_SALE =
            define("nop.err.mall.goods.not-on-sale",
                    "商品未上架");

    ErrorCode ERR_GOODS_NOT_FOUND =
            define("nop.err.mall.goods.not-found",
                    "商品不存在");

    ErrorCode ERR_GOODS_HAS_ORDER_HISTORY =
            define("nop.err.mall.goods.has-order-history",
                    "商品存在历史订单商品记录，不可删除，请改用下架");

    ErrorCode ERR_ADDRESS_LIMIT_EXCEEDED =
            define("nop.err.mall.address.limit-exceeded",
                    "地址数量超过上限");

    ErrorCode ERR_ADDRESS_NOT_FOUND =
            define("nop.err.mall.address.not-found",
                    "地址不存在");

    ErrorCode ERR_ADDRESS_NOT_OWNER =
            define("nop.err.mall.address.not-owner",
                    "非本人地址");

    // 收藏错误码
    ErrorCode ERR_COLLECT_ALREADY_EXISTS =
            define("nop.err.mall.collect.already-exists",
                    "已收藏");

    ErrorCode ERR_COLLECT_NOT_FOUND =
            define("nop.err.mall.collect.not-found",
                    "收藏记录不存在");

    // 评论错误码
    ErrorCode ERR_COMMENT_ORDER_NOT_RECEIVED =
            define("nop.err.mall.comment.order-not-received",
                    "订单未收货，不可评价");

    ErrorCode ERR_COMMENT_ALREADY_EXISTS =
            define("nop.err.mall.comment.already-exists",
                    "该订单商品已评价");

    ErrorCode ERR_COMMENT_EXPIRED =
            define("nop.err.mall.comment.expired",
                    "评价已过期，不可评价");

    ErrorCode ERR_COMMENT_NOT_OWNER =
            define("nop.err.mall.comment.not-owner",
                    "非本人订单，不可评价");

    ErrorCode ERR_COMMENT_ORDER_GOODS_NOT_FOUND =
            define("nop.err.mall.comment.order-goods-not-found",
                    "订单商品不存在");

    // 优惠券错误码
    ErrorCode ERR_COUPON_NOT_FOUND =
            define("nop.err.mall.coupon.not-found",
                    "优惠券不存在");

    ErrorCode ERR_COUPON_NOT_AVAILABLE =
            define("nop.err.mall.coupon.not-available",
                    "优惠券不可领取");

    ErrorCode ERR_COUPON_LIMIT_EXCEEDED =
            define("nop.err.mall.coupon.limit-exceeded",
                    "用户领券超限");

    ErrorCode ERR_COUPON_CODE_INVALID =
            define("nop.err.mall.coupon.code-invalid",
                    "兑换码无效");

    ErrorCode ERR_COUPON_USER_NOT_FOUND =
            define("nop.err.mall.coupon-user.not-found",
                    "用户优惠券不存在");

    ErrorCode ERR_COUPON_NOT_USABLE =
            define("nop.err.mall.coupon-user.not-usable",
                    "优惠券不可使用");

    ErrorCode ERR_COUPON_MIN_NOT_MET =
            define("nop.err.mall.coupon.min-not-met",
                    "未达到最低消费金额");

    ErrorCode ERR_COUPON_GOODS_NOT_MATCH =
            define("nop.err.mall.coupon.goods-not-match",
                    "商品不在优惠券适用范围");

    // 团购错误码
    ErrorCode ERR_GROUPON_RULES_NOT_FOUND =
            define("nop.err.mall.groupon-rules.not-found",
                    "团购规则不存在");

    ErrorCode ERR_GROUPON_RULES_NOT_AVAILABLE =
            define("nop.err.mall.groupon-rules.not-available",
                    "团购规则不可用");

    ErrorCode ERR_GROUPON_RULES_GOODS_NOT_ON_SALE =
            define("nop.err.mall.groupon-rules.goods-not-on-sale",
                    "团购关联商品未上架");

    ErrorCode ERR_GROUPON_NOT_FOUND =
            define("nop.err.mall.groupon.not-found",
                    "团购活动不存在");

    ErrorCode ERR_GROUPON_CANNOT_JOIN_OWN =
            define("nop.err.mall.groupon.cannot-join-own",
                    "不能加入自己发起的团购");

    ErrorCode ERR_GROUPON_ALREADY_JOINED =
            define("nop.err.mall.groupon.already-joined",
                    "已参加该团购");

    ErrorCode ERR_GROUPON_FULL =
            define("nop.err.mall.groupon.full",
                    "团购已满员");

    ErrorCode ERR_GROUPON_NOT_ACTIVE =
            define("nop.err.mall.groupon.not-active",
                    "团购已结束");

    // 通知错误码
    ErrorCode ERR_NOTIFICATION_SEND_FAILED =
            define("nop.err.mall.notification.send-failed",
                    "通知发送失败");

    // 密码重置错误码
    ErrorCode ERR_RESET_CODE_INVALID =
            define("nop.err.mall.reset-code.invalid",
                    "验证码无效或已过期");

    ErrorCode ERR_RESET_CODE_SEND_TOO_FREQUENT =
            define("nop.err.mall.reset-code.send-too-frequent",
                    "发送太频繁");

    ErrorCode ERR_USER_MOBILE_NOT_FOUND =
            define("nop.err.mall.user.mobile-not-found",
                    "手机号未注册");

    ErrorCode ERR_TOPIC_NOT_FOUND =
            define("nop.err.mall.topic.not-found",
                    "专题不存在");

    ErrorCode ERR_ORDER_PRICE_INVALID =
            define("nop.err.mall.order.price-invalid",
                    "订单价格无效");

    // 满减促销错误码
    ErrorCode ERR_PROMOTION_NOT_ACTIVE =
            define("nop.err.mall.promotion.not-active",
                    "满减活动不在有效期内或未上架");

    ErrorCode ERR_PROMOTION_GOODS_NOT_IN_SCOPE =
            define("nop.err.mall.promotion.goods-not-in-scope",
                    "订单商品不在满减活动适用范围");

    ErrorCode ERR_PROMOTION_TIER_NOT_MATCHED =
            define("nop.err.mall.promotion.tier-not-matched",
                    "订单金额未达到满减档位门槛");

    ErrorCode ERR_PROMOTION_STACKING_NOT_ALLOWED =
            define("nop.err.mall.promotion.stacking-not-allowed",
                    "满减与优惠券不可叠加，满减已自动生效，请取消优惠券");

    // 限时折扣错误码
    ErrorCode ERR_TIME_DISCOUNT_NOT_ACTIVE =
            define("nop.err.mall.time-discount.not-active",
                    "限时折扣活动不在有效期内或未上架");

    ErrorCode ERR_TIME_DISCOUNT_SOLD_OUT =
            define("nop.err.mall.time-discount.sold-out",
                    "限时折扣库存不足");

    ErrorCode ERR_TIME_DISCOUNT_NOT_IN_WINDOW =
            define("nop.err.mall.time-discount.not-in-window",
                    "限时折扣不在有效时间窗内");

    ErrorCode ERR_TIME_DISCOUNT_INVALID_VALUE =
            define("nop.err.mall.time-discount.invalid-discount-value",
                    "限时折扣值无效");

    // 会员等级错误码
    ErrorCode ERR_MEMBER_LEVEL_RULE_NOT_FOUND =
            define("nop.err.mall.member-level.rule-not-found",
                    "会员等级规则不存在");

    ErrorCode ERR_MEMBER_LEVEL_RULE_DUPLICATE =
            define("nop.err.mall.member-level.rule-duplicate",
                    "该等级的规则已存在");

    ErrorCode ERR_MEMBER_LEVEL_USER_NOT_FOUND =
            define("nop.err.mall.member-level.user-not-found",
                    "用户不存在");

    ErrorCode ERR_MEMBER_LEVEL_NOT_CONFIGURED =
            define("nop.err.mall.member-level.not-configured",
                    "未配置会员等级规则");

    // 积分错误码
    ErrorCode ERR_POINTS_INSUFFICIENT =
            define("nop.err.mall.points.insufficient",
                    "积分余额不足");

    ErrorCode ERR_POINTS_ACCOUNT_NOT_FOUND =
            define("nop.err.mall.points.account-not-found",
                    "积分账户不存在");

    ErrorCode ERR_POINTS_DEDUCT_EXCEED_LIMIT =
            define("nop.err.mall.points.deduct-exceed-limit",
                    "积分抵扣超过上限");

    ErrorCode ERR_POINTS_EARN_FAILED =
            define("nop.err.mall.points.earn-failed",
                    "积分获取失败，账户并发冲突请重试");

    ErrorCode ERR_POINTS_DUPLICATE_EARN =
            define("nop.err.mall.points.duplicate-earn",
                    "积分已发放过，不可重复发放");

    // 签到错误码
    ErrorCode ERR_CHECK_IN_ALREADY_TODAY =
            define("nop.err.mall.check-in.already-today",
                    "今日已签到，不可重复签到");

    ErrorCode ERR_CHECK_IN_RULE_MISSING =
            define("nop.err.mall.check-in.rule-missing",
                    "签到规则未配置，请联系管理员");
}
