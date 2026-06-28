package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 经营看板待办聚合（P18）。口径见 docs/design/system-configuration.md「报表与统计」。
 */
@DataBean
public class TodoAggregationBean {
    private int pendingShip;
    private int pendingRefund;
    private int aftersalePendingReview;
    private int stockWarning;
    private List<StockWarningItemBean> stockWarningDetails = new ArrayList<>();

    public int getPendingShip() {
        return pendingShip;
    }

    public void setPendingShip(int pendingShip) {
        this.pendingShip = pendingShip;
    }

    public int getPendingRefund() {
        return pendingRefund;
    }

    public void setPendingRefund(int pendingRefund) {
        this.pendingRefund = pendingRefund;
    }

    public int getAftersalePendingReview() {
        return aftersalePendingReview;
    }

    public void setAftersalePendingReview(int aftersalePendingReview) {
        this.aftersalePendingReview = aftersalePendingReview;
    }

    public int getStockWarning() {
        return stockWarning;
    }

    public void setStockWarning(int stockWarning) {
        this.stockWarning = stockWarning;
    }

    public List<StockWarningItemBean> getStockWarningDetails() {
        return stockWarningDetails;
    }

    public void setStockWarningDetails(List<StockWarningItemBean> stockWarningDetails) {
        this.stockWarningDetails = stockWarningDetails;
    }
}
