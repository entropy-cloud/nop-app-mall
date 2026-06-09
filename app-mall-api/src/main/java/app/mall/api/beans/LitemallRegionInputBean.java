//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallRegionInputBean extends CrudInputBase {

    
        private String _pid;

    
        @PropMeta(propId=2)
    
        public String getPid(){
            return _pid;
        }

        public void setPid(String value){
            this._pid = value;
        }


        private String _name;

    
        @PropMeta(propId=3)
    
        public String getName(){
            return _name;
        }

        public void setName(String value){
            this._name = value;
        }


        private Byte _type;

    
        @PropMeta(propId=4)
    
        public Byte getType(){
            return _type;
        }

        public void setType(Byte value){
            this._type = value;
        }


        private Integer _code;

    
        @PropMeta(propId=5)
    
        public Integer getCode(){
            return _code;
        }

        public void setCode(Integer value){
            this._code = value;
        }


    }
