package app.mall.service;

import io.nop.api.core.exceptions.ErrorCode;

import static io.nop.api.core.exceptions.ErrorCode.define;

public interface AppMallErrors {
    ErrorCode ERR_AFTERSALE_NOT_ALLOW_REFUND =
            define("nop.err.mall.aftersale.not-allow-refund",
                    "售后不能进行退款操作");
}
