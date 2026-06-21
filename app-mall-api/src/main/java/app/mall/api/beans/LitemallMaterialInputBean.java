//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallMaterialInputBean extends CrudInputBase {

    
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


        private Boolean _deleted;

    
        @PropMeta(propId=10)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
