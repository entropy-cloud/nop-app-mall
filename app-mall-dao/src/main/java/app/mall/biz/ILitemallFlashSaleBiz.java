
package app.mall.biz;

import app.mall.dao.dto.FlashSaleEffectivenessBean;
import app.mall.dao.entity.LitemallFlashSale;
import app.mall.dao.entity.LitemallOrder;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.util.Map;

public interface ILitemallFlashSaleBiz extends ICrudBiz<LitemallFlashSale> {

    @BizMutation
    LitemallOrder flashSaleBuy(@Name("flashSaleSessionId") String flashSaleSessionId,
                                @Name("addressId") String addressId,
                                @Optional @Name("productId") String productId,
                                @Name("number") Integer number,
                                IServiceContext context);

    @BizQuery
    Map<String, Object> activeFlashSales(@Optional @Name("page") Integer page,
                                          @Optional @Name("pageSize") Integer pageSize,
                                          IServiceContext context);

    @BizQuery
    Map<String, Object> flashSaleDetail(@Name("id") String id,
                                         IServiceContext context);

    @BizQuery
    Map<String, Object> flashSaleForGoods(@Name("goodsId") String goodsId,
                                           @Optional @Name("productId") String productId,
                                           IServiceContext context);

    /**
     * 秒杀按场次效果统计（model-gap closure）：成交单数/GMV/参与人数/售罄率/限购命中拒绝数。
     * flashSaleId 可空（空=全量秒杀聚合）。售罄率=已售罄场次(sessionStock=0)/总场次。
     */
    @BizQuery
    FlashSaleEffectivenessBean getFlashSaleEffectiveness(@Optional @Name("flashSaleId") String flashSaleId,
                                                          @Optional @Name("startDate") String startDate,
                                                          @Optional @Name("endDate") String endDate,
                                                          IServiceContext context);

    @BizMutation
    LitemallFlashSale publishActivity(@Name("id") String id, IServiceContext context);

    @BizMutation
    LitemallFlashSale unpublishActivity(@Name("id") String id, IServiceContext context);
}

