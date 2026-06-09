//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallCategoryInputBean extends CrudInputBase {

    
        private String _name;

    
        @PropMeta(propId=2)
    
        public String getName(){
            return _name;
        }

        public void setName(String value){
            this._name = value;
        }


        private String _iconUrl;

    
        @PropMeta(propId=3)
    
        public String getIconUrl(){
            return _iconUrl;
        }

        public void setIconUrl(String value){
            this._iconUrl = value;
        }


        private String _picUrl;

    
        @PropMeta(propId=4)
    
        public String getPicUrl(){
            return _picUrl;
        }

        public void setPicUrl(String value){
            this._picUrl = value;
        }


        private String _keywords;

    
        @PropMeta(propId=5)
    
        public String getKeywords(){
            return _keywords;
        }

        public void setKeywords(String value){
            this._keywords = value;
        }


        private String _desc;

    
        @PropMeta(propId=6)
    
        public String getDesc(){
            return _desc;
        }

        public void setDesc(String value){
            this._desc = value;
        }


        private String _level;

    
        @PropMeta(propId=7)
    
        public String getLevel(){
            return _level;
        }

        public void setLevel(String value){
            this._level = value;
        }


        private String _pid;

    
        @PropMeta(propId=8)
    
        public String getPid(){
            return _pid;
        }

        public void setPid(String value){
            this._pid = value;
        }


        private Byte _sortOrder;

    
        @PropMeta(propId=9)
    
        public Byte getSortOrder(){
            return _sortOrder;
        }

        public void setSortOrder(Byte value){
            this._sortOrder = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=12)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
