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
}
