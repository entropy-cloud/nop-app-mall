//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallGoodsAttributeInputBean extends CrudInputBase {

    
        private Integer _goodsId;

    
        @PropMeta(propId=2)
    
        public Integer getGoodsId(){
            return _goodsId;
        }

        public void setGoodsId(Integer value){
            this._goodsId = value;
        }


        private String _attribute;

    
        @PropMeta(propId=3)
    
        public String getAttribute(){
            return _attribute;
        }

        public void setAttribute(String value){
            this._attribute = value;
        }


        private String _value;

    
        @PropMeta(propId=4)
    
        public String getValue(){
            return _value;
        }

        public void setValue(String value){
            this._value = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=7)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
