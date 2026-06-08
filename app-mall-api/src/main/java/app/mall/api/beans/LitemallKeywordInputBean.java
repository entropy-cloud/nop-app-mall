//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallKeywordInputBean extends CrudInputBase {

    
        private String _keyword;

    
        @PropMeta(propId=2)
    
        public String getKeyword(){
            return _keyword;
        }

        public void setKeyword(String value){
            this._keyword = value;
        }


        private String _url;

    
        @PropMeta(propId=3)
    
        public String getUrl(){
            return _url;
        }

        public void setUrl(String value){
            this._url = value;
        }


        private Boolean _isHot;

    
        @PropMeta(propId=4)
    
        public Boolean getIsHot(){
            return _isHot;
        }

        public void setIsHot(Boolean value){
            this._isHot = value;
        }


        private Boolean _isDefault;

    
        @PropMeta(propId=5)
    
        public Boolean getIsDefault(){
            return _isDefault;
        }

        public void setIsDefault(Boolean value){
            this._isDefault = value;
        }


        private Integer _sortOrder;

    
        @PropMeta(propId=6)
    
        public Integer getSortOrder(){
            return _sortOrder;
        }

        public void setSortOrder(Integer value){
            this._sortOrder = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=9)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
