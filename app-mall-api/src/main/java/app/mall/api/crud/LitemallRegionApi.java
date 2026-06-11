//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.crud;

    import io.nop.api.core.annotations.biz.BizModel;
    import app.mall.api.beans.LitemallRegionInputBean;
    import app.mall.api.beans.LitemallRegionOutputBean;
    import io.nop.api.core.api.ICrudApi;
    import io.nop.api.core.api.ICrudTreeApi;
    

    @BizModel("LitemallRegion")
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public interface LitemallRegionApi extends ICrudApi<LitemallRegionInputBean, LitemallRegionOutputBean>,
        ICrudTreeApi<LitemallRegionOutputBean> {
    }
