package app.mall.biz;

import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallRegion;

import java.util.List;
import java.util.Map;

public interface ILitemallRegionBiz extends ICrudBiz<LitemallRegion> {

    @BizQuery
    List<Map<String, Object>> getRegionTree();
}
