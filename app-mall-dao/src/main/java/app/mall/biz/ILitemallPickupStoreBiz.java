
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallPickupStore;

import java.util.List;

public interface ILitemallPickupStoreBiz extends ICrudBiz<LitemallPickupStore>{

    /**
     * 返回启用中的自提门店列表（P31）。结算页选店消费者；返回含经纬度/营业时间。
     * status=0 视为启用（门店 status 无 dict，约定 0=启用/1=停用，见 order-and-cart.md）。
     */
    @BizQuery
    List<LitemallPickupStore> listActiveStores(IServiceContext context);
}
