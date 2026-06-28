
package app.mall.biz;

import app.mall.dao.entity.LitemallMaterial;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.PageBean;
import io.nop.core.context.IServiceContext;
import io.nop.orm.biz.ICrudBiz;

public interface ILitemallMaterialBiz extends ICrudBiz<LitemallMaterial>{

    @BizMutation
    @Auth(roles = "admin")
    LitemallMaterial uploadMaterial(@Name("fileUpload") String fileUpload,
                                    @Optional @Name("categoryId") String categoryId,
                                    @Optional @Name("tag") String tag,
                                    IServiceContext context);

    @BizMutation
    @Auth(roles = "admin")
    boolean deleteMaterial(@Name("id") String id, IServiceContext context);

    @BizQuery
    @Auth(roles = "admin")
    PageBean<LitemallMaterial> searchMaterials(@Optional @Name("keyword") String keyword,
                                                @Optional @Name("categoryId") String categoryId,
                                                @Optional @Name("fileType") String fileType,
                                                @Optional @Name("tag") String tag,
                                                @Name("page") int page,
                                                @Name("pageSize") int pageSize,
                                                IServiceContext context);
}
