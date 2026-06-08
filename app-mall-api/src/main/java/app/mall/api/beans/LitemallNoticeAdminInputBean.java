//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallNoticeAdminInputBean extends CrudInputBase {

    
        private Integer _noticeId;

    
        @PropMeta(propId=2)
    
        public Integer getNoticeId(){
            return _noticeId;
        }

        public void setNoticeId(Integer value){
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


        private Integer _adminId;

    
        @PropMeta(propId=4)
    
        public Integer getAdminId(){
            return _adminId;
        }

        public void setAdminId(Integer value){
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


        private Boolean _deleted;

    
        @PropMeta(propId=8)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
