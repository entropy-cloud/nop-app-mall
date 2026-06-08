//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallCollectInputBean extends CrudInputBase {

    
        private Integer _userId;

    
        @PropMeta(propId=2)
    
        public Integer getUserId(){
            return _userId;
        }

        public void setUserId(Integer value){
            this._userId = value;
        }


        private Integer _valueId;

    
        @PropMeta(propId=3)
    
        public Integer getValueId(){
            return _valueId;
        }

        public void setValueId(Integer value){
            this._valueId = value;
        }


        private Byte _type;

    
        @PropMeta(propId=4)
    
        public Byte getType(){
            return _type;
        }

        public void setType(Byte value){
            this._type = value;
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
