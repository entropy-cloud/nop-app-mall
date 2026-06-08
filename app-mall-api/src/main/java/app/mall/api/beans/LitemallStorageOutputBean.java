//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallStorageOutputBean {

    
        private Integer _id;

    
        @PropMeta(propId=1)
    
        public Integer getId(){
            return _id;
        }

        public void setId(Integer value){
            this._id = value;
        }


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


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=7)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=8)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
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
