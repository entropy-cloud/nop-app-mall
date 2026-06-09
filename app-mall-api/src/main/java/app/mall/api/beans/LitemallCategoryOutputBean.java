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
    public class LitemallCategoryOutputBean {

    
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


        private String _level_label;

    
        public String getLevel_label(){
            return _level_label;
        }

        public void setLevel_label(String value){
            this._level_label = value;
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


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=10)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=11)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=12)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private io.nop.api.core.beans.file.FileStatusBean _iconUrlComponentFileStatus;

    
        public io.nop.api.core.beans.file.FileStatusBean getIconUrlComponentFileStatus(){
            return _iconUrlComponentFileStatus;
        }

        public void setIconUrlComponentFileStatus(io.nop.api.core.beans.file.FileStatusBean value){
            this._iconUrlComponentFileStatus = value;
        }


        private io.nop.api.core.beans.file.FileStatusBean _picUrlComponentFileStatus;

    
        public io.nop.api.core.beans.file.FileStatusBean getPicUrlComponentFileStatus(){
            return _picUrlComponentFileStatus;
        }

        public void setPicUrlComponentFileStatus(io.nop.api.core.beans.file.FileStatusBean value){
            this._picUrlComponentFileStatus = value;
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
