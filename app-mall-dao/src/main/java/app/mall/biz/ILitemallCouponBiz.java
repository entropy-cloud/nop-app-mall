package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallCoupon;
import app.mall.dao.entity.LitemallCouponUser;

public interface ILitemallCouponBiz extends ICrudBiz<LitemallCoupon> {

    @BizMutation
    LitemallCoupon publishCoupon(@Name("id") String id,
                                 IServiceContext context);

    @BizMutation
    LitemallCoupon unpublishCoupon(@Name("id") String id,
                                   IServiceContext context);

    @BizQuery
    PageBean<LitemallCoupon> listAvailableCoupons(@Name("page") int page,
                                                   @Name("pageSize") int pageSize,
                                                   IServiceContext context);

    @BizQuery
    PageBean<LitemallCouponUser> listMyCoupons(@Optional @Name("status") Integer status,
                                                @Name("page") int page,
                                                @Name("pageSize") int pageSize,
                                                IServiceContext context);
}
