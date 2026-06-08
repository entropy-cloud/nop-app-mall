//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallSearchHistoryInputBean extends CrudInputBase {

    
        private Integer _userId;

    
        @PropMeta(propId=2)
    
        public Integer getUserId(){
            return _userId;
        }

        public void setUserId(Integer value){
            this._userId = value;
        }


        private String _keyword;

    
        @PropMeta(propId=3)
    
        public String getKeyword(){
            return _keyword;
        }

        public void setKeyword(String value){
            this._keyword = value;
        }


        private String _from;

    
        @PropMeta(propId=4)
    
        public String getFrom(){
            return _from;
        }

        public void setFrom(String value){
            this._from = value;
        }


    }
