package app.mall.service.entity;

import io.nop.api.core.exceptions.NopException;
import io.nop.report.core.XptConstants;

import static app.mall.service.AppMallErrors.ARG_RENDER_TYPE;
import static app.mall.service.AppMallErrors.ERR_REPORT_RENDER_TYPE_INVALID;

/**
 * nop-report 导出渲染类型校验（Phase 1/2 共用）。
 * 仅允许 xlsx/pdf（导出场景），html 为屏幕预览不纳入导出入口。
 */
final class ReportRenderTypes {
    static final String XLSX = XptConstants.RENDER_TYPE_XLSX;
    static final String PDF = XptConstants.RENDER_TYPE_PDF;

    private ReportRenderTypes() {
    }

    static String validate(String renderType) {
        if (!XLSX.equals(renderType) && !PDF.equals(renderType)) {
            throw new NopException(ERR_REPORT_RENDER_TYPE_INVALID)
                    .param(ARG_RENDER_TYPE, renderType);
        }
        return renderType;
    }
}
