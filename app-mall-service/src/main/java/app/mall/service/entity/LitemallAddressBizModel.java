package app.mall.service.entity;

import app.mall.biz.ILitemallAddressBiz;
import app.mall.biz.ILitemallSystemBiz;
import app.mall.dao.entity.LitemallAddress;
import app.mall.dao.entity.LitemallSystem;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.util.List;

import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallAddress")
public class LitemallAddressBizModel extends CrudBizModel<LitemallAddress> implements ILitemallAddressBiz {

    @Inject
    ILitemallSystemBiz systemBiz;

    private static final int DEFAULT_ADDRESS_LIMIT = 20;

    public LitemallAddressBizModel() {
        setEntityName(LitemallAddress.class.getName());
    }

    @Override
    @BizQuery
    public List<LitemallAddress> list(IServiceContext context) {
        String userId = context.getUserId();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallAddress.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallAddress.PROP_NAME_deleted, false));
        return findList(query, null, context);
    }

    @Override
    @BizQuery
    public LitemallAddress detail(@Name("id") String id, IServiceContext context) {
        LitemallAddress entity = requireEntity(id, "detail", context);
        String userId = context.getUserId();
        if (!entity.getUserId().equals(userId)) {
            throw new NopException(ERR_ADDRESS_NOT_OWNER);
        }
        return entity;
    }

    @Override
    @BizMutation
    public LitemallAddress add(@Name("name") String name, @Name("phone") String phone,
                               @Name("province") String province, @Name("city") String city,
                               @Name("county") String county, @Name("addressDetail") String addressDetail,
                               @Name("areaCode") String areaCode, @Name("isDefault") Boolean isDefault,
                               IServiceContext context) {
        String userId = context.getUserId();

        QueryBean countQuery = new QueryBean();
        countQuery.addFilter(FilterBeans.eq(LitemallAddress.PROP_NAME_userId, userId));
        countQuery.addFilter(FilterBeans.eq(LitemallAddress.PROP_NAME_deleted, false));
        long count = dao().countByQuery(countQuery);
        int limit = DEFAULT_ADDRESS_LIMIT;
        String limitConfig = systemBiz.getConfig("mall_address_limit", context);
        if (limitConfig != null) {
            try {
                limit = Integer.parseInt(limitConfig);
            } catch (NumberFormatException ignored) {
            }
        }
        if (count >= limit) {
            throw new NopException(ERR_ADDRESS_LIMIT_EXCEEDED);
        }

        if (count == 0) {
            isDefault = true;
        }

        if (Boolean.TRUE.equals(isDefault)) {
            clearDefault(userId, context);
        }

        LitemallAddress entity = newEntity();
        entity.setUserId(userId);
        entity.setName(name);
        entity.setTel(phone);
        entity.setProvince(province);
        entity.setCity(city);
        entity.setCounty(county);
        entity.setAddressDetail(addressDetail);
        entity.setAreaCode(areaCode);
        entity.setIsDefault(isDefault);
        saveEntity(entity, "add", context);

        return entity;
    }

    @Override
    @BizMutation
    public LitemallAddress updateAddress(@Name("id") String id, @Name("name") String name,
                                         @Name("phone") String phone, @Name("province") String province,
                                         @Name("city") String city, @Name("county") String county,
                                         @Name("addressDetail") String addressDetail, @Name("areaCode") String areaCode,
                                         @Name("isDefault") Boolean isDefault, IServiceContext context) {
        LitemallAddress entity = requireEntity(id, "updateAddress", context);
        String userId = context.getUserId();
        if (!entity.getUserId().equals(userId)) {
            throw new NopException(ERR_ADDRESS_NOT_OWNER);
        }

        if (Boolean.TRUE.equals(isDefault)) {
            clearDefault(userId, context);
        }

        entity.setName(name);
        entity.setTel(phone);
        entity.setProvince(province);
        entity.setCity(city);
        entity.setCounty(county);
        entity.setAddressDetail(addressDetail);
        entity.setAreaCode(areaCode);
        entity.setIsDefault(isDefault);
        updateEntity(entity, "updateAddress", context);

        return entity;
    }

    @Override
    @BizMutation
    public LitemallAddress deleteAddress(@Name("id") String id, IServiceContext context) {
        LitemallAddress entity = requireEntity(id, "deleteAddress", context);
        String userId = context.getUserId();
        if (!entity.getUserId().equals(userId)) {
            throw new NopException(ERR_ADDRESS_NOT_OWNER);
        }
        deleteEntity(entity, "deleteAddress", context);
        return entity;
    }

    @Override
    @BizMutation
    public LitemallAddress setDefault(@Name("id") String id, IServiceContext context) {
        LitemallAddress entity = requireEntity(id, "setDefault", context);
        String userId = context.getUserId();
        if (!entity.getUserId().equals(userId)) {
            throw new NopException(ERR_ADDRESS_NOT_OWNER);
        }

        clearDefault(userId, context);
        entity.setIsDefault(true);
        updateEntity(entity, "setDefault", context);

        return entity;
    }

    private void clearDefault(String userId, IServiceContext context) {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallAddress.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallAddress.PROP_NAME_isDefault, true));
        query.addFilter(FilterBeans.eq(LitemallAddress.PROP_NAME_deleted, false));
        List<LitemallAddress> defaults = findList(query, null, context);
        for (LitemallAddress addr : defaults) {
            addr.setIsDefault(false);
        }
    }
}
