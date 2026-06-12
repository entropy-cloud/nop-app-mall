package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallAddress;

import java.util.List;

public interface ILitemallAddressBiz extends ICrudBiz<LitemallAddress> {

    @BizQuery
    List<LitemallAddress> list(IServiceContext context);

    @BizQuery
    LitemallAddress detail(@Name("id") String id, IServiceContext context);

    @BizMutation
    LitemallAddress add(@Name("name") String name, @Name("phone") String phone,
                        @Name("province") String province, @Name("city") String city,
                        @Name("county") String county, @Name("addressDetail") String addressDetail,
                        @Name("areaCode") String areaCode, @Name("isDefault") Boolean isDefault,
                        IServiceContext context);

    @BizMutation
    LitemallAddress updateAddress(@Name("id") String id, @Name("name") String name,
                                  @Name("phone") String phone, @Name("province") String province,
                                  @Name("city") String city, @Name("county") String county,
                                  @Name("addressDetail") String addressDetail, @Name("areaCode") String areaCode,
                                  @Name("isDefault") Boolean isDefault, IServiceContext context);

    @BizMutation
    LitemallAddress deleteAddress(@Name("id") String id, IServiceContext context);

    @BizMutation
    LitemallAddress setDefault(@Name("id") String id, IServiceContext context);
}
