//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallGoodsSpecificationInputBean extends CrudInputBase {

    
        private String _goodsId;

    
        @PropMeta(propId=2)
    
        public String getGoodsId(){
            return _goodsId;
        }

        public void setGoodsId(String value){
            this._goodsId = value;
        }


        private String _specification;

    
        @PropMeta(propId=3)
    
        public String getSpecification(){
            return _specification;
        }

        public void setSpecification(String value){
            this._specification = value;
        }


        private String _value;

    
        @PropMeta(propId=4)
    
        public String getValue(){
            return _value;
        }

        public void setValue(String value){
            this._value = value;
        }


        private String _picUrl;

    
        @PropMeta(propId=5)
    
        public String getPicUrl(){
            return _picUrl;
        }

        public void setPicUrl(String value){
            this._picUrl = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=8)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
