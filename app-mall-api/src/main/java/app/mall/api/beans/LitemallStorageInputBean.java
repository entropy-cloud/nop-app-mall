//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallStorageInputBean extends CrudInputBase {

    
        private String _key;

    
        @PropMeta(propId=2)
    
        public String getKey(){
            return _key;
        }

        public void setKey(String value){
            this._key = value;
        }


        private String _name;

    
        @PropMeta(propId=3)
    
        public String getName(){
            return _name;
        }

        public void setName(String value){
            this._name = value;
        }


        private String _type;

    
        @PropMeta(propId=4)
    
        public String getType(){
            return _type;
        }

        public void setType(String value){
            this._type = value;
        }


        private Integer _size;

    
        @PropMeta(propId=5)
    
        public Integer getSize(){
            return _size;
        }

        public void setSize(Integer value){
            this._size = value;
        }


        private String _url;

    
        @PropMeta(propId=6)
    
        public String getUrl(){
            return _url;
        }

        public void setUrl(String value){
            this._url = value;
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
