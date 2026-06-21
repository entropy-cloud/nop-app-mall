//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallMaterialOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _name;

    
        @PropMeta(propId=2)
    
        public String getName(){
            return _name;
        }

        public void setName(String value){
            this._name = value;
        }


        private String _url;

    
        @PropMeta(propId=3)
    
        public String getUrl(){
            return _url;
        }

        public void setUrl(String value){
            this._url = value;
        }


        private String _fileType;

    
        @PropMeta(propId=4)
    
        public String getFileType(){
            return _fileType;
        }

        public void setFileType(String value){
            this._fileType = value;
        }


        private Integer _fileSize;

    
        @PropMeta(propId=5)
    
        public Integer getFileSize(){
            return _fileSize;
        }

        public void setFileSize(Integer value){
            this._fileSize = value;
        }


        private String _categoryId;

    
        @PropMeta(propId=6)
    
        public String getCategoryId(){
            return _categoryId;
        }

        public void setCategoryId(String value){
            this._categoryId = value;
        }


        private String _tag;

    
        @PropMeta(propId=7)
    
        public String getTag(){
            return _tag;
        }

        public void setTag(String value){
            this._tag = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=8)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=9)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=10)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private Map<String,Object> _category;

        public Map<String,Object> getCategory(){
            return _category;
        }

        public void setCategory(Map<String,Object> value){
            this._category = value;
        }


    }
