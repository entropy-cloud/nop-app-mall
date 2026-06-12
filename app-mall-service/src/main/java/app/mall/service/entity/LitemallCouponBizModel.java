package app.mall.service.entity;

import app.mall.biz.ILitemallCouponBiz;
import app.mall.biz.ILitemallCouponUserBiz;
import app.mall.dao.entity.LitemallCoupon;
import app.mall.dao.entity.LitemallCouponUser;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

@BizModel("LitemallCoupon")
public class LitemallCouponBizModel extends CrudBizModel<LitemallCoupon> implements ILitemallCouponBiz {

    @Inject
    ILitemallCouponUserBiz couponUserBiz;

    public LitemallCouponBizModel() {
        setEntityName(LitemallCoupon.class.getName());
    }

    @Override
    @BizMutation
    public LitemallCoupon publishCoupon(@Name("id") String id,
                                         IServiceContext context) {
        LitemallCoupon coupon = requireEntity(id, null, context);
        coupon.setStatus(0);
        return coupon;
    }

    @Override
    @BizMutation
    public LitemallCoupon unpublishCoupon(@Name("id") String id,
                                           IServiceContext context) {
        LitemallCoupon coupon = requireEntity(id, null, context);
        coupon.setStatus(2);
        return coupon;
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public PageBean<LitemallCoupon> listAvailableCoupons(@Name("page") int page,
                                                          @Name("pageSize") int pageSize,
                                                          IServiceContext context) {
        LocalDateTime now = LocalDateTime.now();

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCoupon.PROP_NAME_status, 0));
        query.addFilter(FilterBeans.eq(LitemallCoupon.PROP_NAME_deleted, false));
        query.addFilter(FilterBeans.eq(LitemallCoupon.PROP_NAME_type, 0));

        query.addFilter(FilterBeans.or(
                FilterBeans.gt(LitemallCoupon.PROP_NAME_total, 0),
                FilterBeans.eq(LitemallCoupon.PROP_NAME_total, 0)
        ));

        query.addFilter(FilterBeans.or(
                FilterBeans.isNull(LitemallCoupon.PROP_NAME_endTime),
                FilterBeans.gt(LitemallCoupon.PROP_NAME_endTime, now)
        ));

        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallCoupon.PROP_NAME_addTime, true);

        return findPage(query, null, context);
    }

    @Override
    @BizQuery
    public PageBean<LitemallCouponUser> listMyCoupons(@Optional @Name("status") Integer status,
                                                       @Name("page") int page,
                                                       @Name("pageSize") int pageSize,
                                                       IServiceContext context) {
        String userId = context.getUserId();

        QueryBean cuQuery = new QueryBean();
        cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_userId, userId));
        if (status != null) {
            cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_status, status));
        }
        cuQuery.addFilter(FilterBeans.eq(LitemallCouponUser.PROP_NAME_deleted, false));

        return couponUserBiz.findPage(cuQuery, null, context);
    }
}
