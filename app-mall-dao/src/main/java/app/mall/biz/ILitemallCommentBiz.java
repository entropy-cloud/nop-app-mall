
package app.mall.biz;

import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

import java.util.List;
import java.util.Map;

import app.mall.dao.dto.BatchCommentResultBean;
import app.mall.dao.entity.LitemallComment;

public interface ILitemallCommentBiz extends ICrudBiz<LitemallComment> {

    @BizMutation
    LitemallComment submitComment(@Name("orderGoodsId") String orderGoodsId,
                                  @Name("content") String content,
                                  @Name("star") int star,
                                  @Optional @Name("hasPicture") Boolean hasPicture,
                                  @Optional @Name("picUrls") String picUrls,
                                  @Optional @Name("pros") String pros,
                                  @Optional @Name("cons") String cons,
                                  @Optional @Name("semanticRating") Integer semanticRating,
                                  IServiceContext context);

    @BizQuery
    PageBean<LitemallComment> commentList(@Name("type") int type,
                                          @Name("valueId") String valueId,
                                          @Optional @Name("showType") String showType,
                                          @Name("page") int page,
                                          @Name("pageSize") int pageSize,
                                          IServiceContext context);

    @BizQuery
    Map<String, Object> getCommentSummary(@Name("type") int type,
                                          @Name("valueId") String valueId,
                                          IServiceContext context);

    @BizQuery
    PageBean<LitemallComment> myComments(@Name("page") int page,
                                         @Name("pageSize") int pageSize,
                                         IServiceContext context);

    @BizMutation
    LitemallComment adminReply(@Name("id") String id,
                               @Name("adminContent") String adminContent,
                               IServiceContext context);

    @BizMutation
    List<BatchCommentResultBean> batchAdminReply(@Name("items") List<Map<String, Object>> items,
                                                 IServiceContext context);

    @BizMutation
    List<BatchCommentResultBean> batchModerateComments(@Name("commentIds") List<String> commentIds,
                                                       @Name("action") String action,
                                                       IServiceContext context);

    @BizQuery
    PageBean<LitemallComment> getCommentReviewList(@Optional @Name("keyword") String keyword,
                                                    @Optional @Name("star") Integer star,
                                                    @Optional @Name("hasPicture") Boolean hasPicture,
                                                    @Optional @Name("startTime") String startTime,
                                                    @Optional @Name("endTime") String endTime,
                                                    @Optional @Name("page") Integer page,
                                                    @Optional @Name("pageSize") Integer pageSize,
                                                    IServiceContext context);
}