//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import java.util.List;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallRegionOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


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


        private String _type_label;

    
        public String getType_label(){
            return _type_label;
        }

        public void setType_label(String value){
            this._type_label = value;
        }


        private Integer _code;

    
        @PropMeta(propId=5)
    
        public Integer getCode(){
            return _code;
        }

        public void setCode(Integer value){
            this._code = value;
        }


        private Map<String,Object> _parent;

        public Map<String,Object> getParent(){
            return _parent;
        }

        public void setParent(Map<String,Object> value){
            this._parent = value;
        }


        private List<Map<String,Object>> _children;

        public List<Map<String,Object>> getChildren(){
            return _children;
        }

        public void setChildren(List<Map<String,Object>> value){
            this._children = value;
        }


    }
