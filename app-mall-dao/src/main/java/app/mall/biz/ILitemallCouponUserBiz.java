package app.mall.biz;

import io.nop.api.core.annotations.biz.BizAction;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallCouponUser;

import java.math.BigDecimal;
import java.util.List;

public interface ILitemallCouponUserBiz extends ICrudBiz<LitemallCouponUser> {

    @BizMutation
    LitemallCouponUser claimCoupon(@Name("couponId") String couponId,
                                   IServiceContext context);

    @BizAction
    LitemallCouponUser claimCouponForUser(@Name("couponId") String couponId,
                                          @Name("userId") String userId,
                                          IServiceContext context);

    @BizMutation
    LitemallCouponUser redeemCoupon(@Name("code") String code,
                                    IServiceContext context);

    @BizQuery
    BigDecimal selectCouponForOrder(@Name("couponUserId") String couponUserId,
                                    @Name("goodsPrice") BigDecimal goodsPrice,
                                    @Optional @Name("goodsIds") List<String> goodsIds,
                                    IServiceContext context);

    @BizMutation
    void useCoupon(@Name("couponUserId") String couponUserId,
                   @Name("orderId") String orderId,
                   IServiceContext context);

    @BizMutation
    void returnCoupon(@Name("couponUserId") String couponUserId,
                      IServiceContext context);

    @BizMutation
    int expireCoupons(IServiceContext context);
}
