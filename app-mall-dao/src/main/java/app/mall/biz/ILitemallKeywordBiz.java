package app.mall.biz;

import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallKeyword;

import java.util.List;

public interface ILitemallKeywordBiz extends ICrudBiz<LitemallKeyword> {

    @BizQuery
    List<LitemallKeyword> getHotKeywords();

    @BizQuery
    List<LitemallKeyword> getDefaultKeywords();
}
