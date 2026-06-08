//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.crud;

    import io.nop.api.core.annotations.biz.BizModel;
    import app.mall.api.beans.LitemallCategoryInputBean;
    import app.mall.api.beans.LitemallCategoryOutputBean;
    import io.nop.api.core.api.ICrudApi;
    import io.nop.api.core.api.ICrudTreeApi;
    

    @BizModel("LitemallCategory")
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public interface LitemallCategoryApi extends ICrudApi<LitemallCategoryInputBean, LitemallCategoryOutputBean>,
        ICrudTreeApi<LitemallCategoryOutputBean> {
    }
