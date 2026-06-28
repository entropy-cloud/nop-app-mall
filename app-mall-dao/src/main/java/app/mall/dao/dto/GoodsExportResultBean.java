package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 商品导出（P36）结果。CSV 兜底（xlsx 写出为 successor），前端按 fileName 提供 Blob 下载。
 */
@DataBean
public class GoodsExportResultBean {
    private String fileName;
    private String csvContent;
    private int rowCount;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCsvContent() {
        return csvContent;
    }

    public void setCsvContent(String csvContent) {
        this.csvContent = csvContent;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }
}
