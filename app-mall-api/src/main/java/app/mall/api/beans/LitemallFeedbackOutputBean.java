//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallFeedbackOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _userId;

    
        @PropMeta(propId=2)
    
        public String getUserId(){
            return _userId;
        }

        public void setUserId(String value){
            this._userId = value;
        }


        private String _username;

    
        @PropMeta(propId=3)
    
        public String getUsername(){
            return _username;
        }

        public void setUsername(String value){
            this._username = value;
        }


        private String _mobile;

    
        @PropMeta(propId=4)
    
        public String getMobile(){
            return _mobile;
        }

        public void setMobile(String value){
            this._mobile = value;
        }


        private String _feedType;

    
        @PropMeta(propId=5)
    
        public String getFeedType(){
            return _feedType;
        }

        public void setFeedType(String value){
            this._feedType = value;
        }


        private String _content;

    
        @PropMeta(propId=6)
    
        public String getContent(){
            return _content;
        }

        public void setContent(String value){
            this._content = value;
        }


        private Integer _status;

    
        @PropMeta(propId=7)
    
        public Integer getStatus(){
            return _status;
        }

        public void setStatus(Integer value){
            this._status = value;
        }


        private Boolean _hasPicture;

    
        @PropMeta(propId=8)
    
        public Boolean getHasPicture(){
            return _hasPicture;
        }

        public void setHasPicture(Boolean value){
            this._hasPicture = value;
        }


        private java.util.List<java.lang.String> _picUrls;

    
        @PropMeta(propId=9)
    
        public java.util.List<java.lang.String> getPicUrls(){
            return _picUrls;
        }

        public void setPicUrls(java.util.List<java.lang.String> value){
            this._picUrls = value;
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


        private java.util.List<io.nop.api.core.beans.file.FileStatusBean> _picUrlsComponentFileStatusList;

    
        public java.util.List<io.nop.api.core.beans.file.FileStatusBean> getPicUrlsComponentFileStatusList(){
            return _picUrlsComponentFileStatusList;
        }

        public void setPicUrlsComponentFileStatusList(java.util.List<io.nop.api.core.beans.file.FileStatusBean> value){
            this._picUrlsComponentFileStatusList = value;
        }


    }
