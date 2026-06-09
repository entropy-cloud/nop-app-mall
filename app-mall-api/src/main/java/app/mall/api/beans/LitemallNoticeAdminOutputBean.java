//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallNoticeAdminOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _noticeId;

    
        @PropMeta(propId=2)
    
        public String getNoticeId(){
            return _noticeId;
        }

        public void setNoticeId(String value){
            this._noticeId = value;
        }


        private String _noticeTitle;

    
        @PropMeta(propId=3)
    
        public String getNoticeTitle(){
            return _noticeTitle;
        }

        public void setNoticeTitle(String value){
            this._noticeTitle = value;
        }


        private String _adminId;

    
        @PropMeta(propId=4)
    
        public String getAdminId(){
            return _adminId;
        }

        public void setAdminId(String value){
            this._adminId = value;
        }


        private java.time.LocalDateTime _readTime;

    
        @PropMeta(propId=5)
    
        public java.time.LocalDateTime getReadTime(){
            return _readTime;
        }

        public void setReadTime(java.time.LocalDateTime value){
            this._readTime = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=6)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=7)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=8)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
