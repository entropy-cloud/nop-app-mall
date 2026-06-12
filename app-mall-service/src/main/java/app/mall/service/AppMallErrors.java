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
}
