package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 报表标量计数（P19）。用于返回单一计数的 SQL-lib 查询。
 */
@DataBean
public class ReportScalarCountBean {
    private int totalCount;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
