//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallSystemInputBean extends CrudInputBase {

    
        private String _keyName;

    
        @PropMeta(propId=2)
    
        public String getKeyName(){
            return _keyName;
        }

        public void setKeyName(String value){
            this._keyName = value;
        }


        private String _keyValue;

    
        @PropMeta(propId=3)
    
        public String getKeyValue(){
            return _keyValue;
        }

        public void setKeyValue(String value){
            this._keyValue = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=6)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
