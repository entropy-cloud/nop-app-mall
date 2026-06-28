package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

/**
 * 商品批量运营（P36）单行结果。一行对应批量改价/改库存/上下架/导入中的一条记录，
 * 失败行带原因；部分失败不阻断成功行（复刻 {@code BatchShipResultBean} 聚合口径）。
 */
@DataBean
public class BatchGoodsResultBean {
    private String goodsId;
    private String goodsSn;
    private boolean success;
    private String reason;

    public BatchGoodsResultBean() {
    }

    public BatchGoodsResultBean(String goodsId, String goodsSn, boolean success, String reason) {
        this.goodsId = goodsId;
        this.goodsSn = goodsSn;
        this.success = success;
        this.reason = reason;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsSn() {
        return goodsSn;
    }

    public void setGoodsSn(String goodsSn) {
        this.goodsSn = goodsSn;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
