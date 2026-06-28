
package app.mall.service.entity;

import app.mall.biz.ILitemallMaterialBiz;
import app.mall.dao.entity.LitemallMaterial;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.annotations.core.Optional;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.PageBean;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;
import io.nop.file.core.IFileRecord;
import io.nop.file.core.IFileStore;
import io.nop.orm.IOrmEntityFileStore;
import jakarta.inject.Inject;

import static io.nop.api.core.beans.FilterBeans.contains;
import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallMaterial")
public class LitemallMaterialBizModel extends CrudBizModel<LitemallMaterial> implements ILitemallMaterialBiz {

    @Inject
    IFileStore fileStore;

    @Inject
    IOrmEntityFileStore ormEntityFileStore;

    public LitemallMaterialBizModel() {
        setEntityName(LitemallMaterial.class.getName());
    }

    // 接收 AMIS file-upload 控件返回的 fileRef（/f/download/{fileId} 形式链接），解析 fileId 后通过 IFileStore
    // 获取 IFileRecord，提取文件名/大小，按 MIME 推断 fileType（image/video/file），并创建素材记录。
    // 参照 LitemallGoodsBizModel.parseGoodsImportExcel 的 fileStore 使用模式。
    @Override
    @BizMutation
    @Auth(roles = "admin")
    public LitemallMaterial uploadMaterial(@Name("fileUpload") String fileUpload,
                                           @Optional @Name("categoryId") String categoryId,
                                           @Optional @Name("tag") String tag,
                                           IServiceContext context) {
        if (StringHelper.isEmpty(fileUpload)) {
            throw new NopException(ERR_MATERIAL_FILE_EMPTY);
        }
        String fileId = ormEntityFileStore.decodeFileId(fileUpload);
        if (StringHelper.isEmpty(fileId)) {
            fileId = fileUpload;
        }
        IFileRecord fileRecord = fileStore.getFile(fileId);
        if (fileRecord == null) {
            throw new NopException(ERR_MATERIAL_FILE_EMPTY);
        }

        LitemallMaterial material = newEntity();
        material.setName(fileRecord.getFileName());
        material.setUrl(ormEntityFileStore.getFileLink(fileId));
        material.setFileSize((int) Math.min(fileRecord.getLength(), Integer.MAX_VALUE));
        material.setFileType(inferFileType(fileRecord));
        if (!StringHelper.isEmpty(categoryId)) {
            material.setCategoryId(categoryId);
        }
        if (!StringHelper.isEmpty(tag)) {
            material.setTag(tag);
        }
        saveEntity(material, null, context);
        return material;
    }

    // 逻辑删除素材记录（复用 CrudBizModel delete，走 deleted 逻辑删除标记）。
    // 文件本身不从 IFileStore 物理删除（goods pic / brand logo 等引用可能仍存在）。
    @Override
    @BizMutation
    @Auth(roles = "admin")
    public boolean deleteMaterial(@Name("id") String id, IServiceContext context) {
        return delete(id, context);
    }

    // keyword 匹配 name(contains)、categoryId eq、fileType eq、tag contains。
    // 参照 LitemallGoodsBizModel.applyAdminExportFilters 的 filter 组合模式。
    @Override
    @BizQuery
    @Auth(roles = "admin")
    public PageBean<LitemallMaterial> searchMaterials(@Optional @Name("keyword") String keyword,
                                                      @Optional @Name("categoryId") String categoryId,
                                                      @Optional @Name("fileType") String fileType,
                                                      @Optional @Name("tag") String tag,
                                                      @Name("page") int page,
                                                      @Name("pageSize") int pageSize,
                                                      IServiceContext context) {
        QueryBean query = new QueryBean();

        if (!StringHelper.isEmpty(keyword)) {
            query.addFilter(contains(LitemallMaterial.PROP_NAME_name, keyword));
        }
        if (!StringHelper.isEmpty(categoryId)) {
            query.addFilter(FilterBeans.eq(LitemallMaterial.PROP_NAME_categoryId, categoryId));
        }
        if (!StringHelper.isEmpty(fileType)) {
            query.addFilter(FilterBeans.eq(LitemallMaterial.PROP_NAME_fileType, fileType));
        }
        if (!StringHelper.isEmpty(tag)) {
            query.addFilter(contains(LitemallMaterial.PROP_NAME_tag, tag));
        }

        query.setOffset(page > 0 ? (page - 1) * pageSize : 0);
        query.setLimit(pageSize > 0 ? pageSize : 10);
        query.addOrderField(LitemallMaterial.PROP_NAME_addTime, true);

        return findPage(query, null, context);
    }

    private String inferFileType(IFileRecord fileRecord) {
        String mimeType = fileRecord.getMimeType();
        if (mimeType != null) {
            String lower = mimeType.toLowerCase();
            if (lower.startsWith("image/")) {
                return "image";
            }
            if (lower.startsWith("video/")) {
                return "video";
            }
        }
        String ext = fileRecord.getFileExt();
        if (ext != null) {
            String lower = ext.toLowerCase();
            if (lower.startsWith(".")) {
                lower = lower.substring(1);
            }
            if (IMAGE_EXTS.contains(lower)) {
                return "image";
            }
            if (VIDEO_EXTS.contains(lower)) {
                return "video";
            }
        }
        return "file";
    }

    private static final java.util.Set<String> IMAGE_EXTS = java.util.Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff", "ico");
    private static final java.util.Set<String> VIDEO_EXTS = java.util.Set.of(
            "mp4", "mov", "avi", "mkv", "webm", "flv", "wmv", "m4v", "mpg", "mpeg");
}
