package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import app.mall.dao.entity.LitemallComment;

public interface ILitemallCommentBiz extends ICrudBiz<LitemallComment> {

    @BizMutation
    LitemallComment submitComment(@Name("orderGoodsId") String orderGoodsId,
                                  @Name("content") String content,
                                  @Name("star") int star,
                                  @Optional @Name("hasPicture") Boolean hasPicture,
                                  @Optional @Name("picUrls") String picUrls,
                                  IServiceContext context);

    @BizQuery
    PageBean<LitemallComment> commentList(@Name("type") int type,
                                          @Name("valueId") String valueId,
                                          @Name("page") int page,
                                          @Name("pageSize") int pageSize,
                                          IServiceContext context);

    @BizQuery
    PageBean<LitemallComment> myComments(@Name("page") int page,
                                         @Name("pageSize") int pageSize,
                                         IServiceContext context);

    @BizMutation
    LitemallComment adminReply(@Name("id") String id,
                               @Name("adminContent") String adminContent,
                               IServiceContext context);
}
