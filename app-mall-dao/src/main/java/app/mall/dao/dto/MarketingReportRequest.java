package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 营销活动效果分析多 sheet 导出（exportMarketingReport）请求参数。
 * 7 个业务字段（>5），按 BizModel 自检规则 #5 使用 @RequestBean + @DataBean。
 * GraphQL 入参与单报表导出先例一致：renderType 必填，时间窗与 4 个归因 ID 可选。
 */
@DataBean
public class MarketingReportRequest {

    private String renderType;
    private String startDate;
    private String endDate;
    private String promotionActivityId;
    private String flashSaleId;
    private String pinTuanActivityId;
    private String couponId;

    public String getRenderType() {
        return renderType;
    }

    public void setRenderType(String renderType) {
        this.renderType = renderType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPromotionActivityId() {
        return promotionActivityId;
    }

    public void setPromotionActivityId(String promotionActivityId) {
        this.promotionActivityId = promotionActivityId;
    }

    public String getFlashSaleId() {
        return flashSaleId;
    }

    public void setFlashSaleId(String flashSaleId) {
        this.flashSaleId = flashSaleId;
    }

    public String getPinTuanActivityId() {
        return pinTuanActivityId;
    }

    public void setPinTuanActivityId(String pinTuanActivityId) {
        this.pinTuanActivityId = pinTuanActivityId;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }
}
