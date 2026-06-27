//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallUserMessageOutputBean {

    
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


        private Integer _msgType;

    
        @PropMeta(propId=3)
    
        public Integer getMsgType(){
            return _msgType;
        }

        public void setMsgType(Integer value){
            this._msgType = value;
        }


        private String _msgType_label;

    
        public String getMsgType_label(){
            return _msgType_label;
        }

        public void setMsgType_label(String value){
            this._msgType_label = value;
        }


        private String _title;

    
        @PropMeta(propId=4)
    
        public String getTitle(){
            return _title;
        }

        public void setTitle(String value){
            this._title = value;
        }


        private String _content;

    
        @PropMeta(propId=5)
    
        public String getContent(){
            return _content;
        }

        public void setContent(String value){
            this._content = value;
        }


        private Boolean _isRead;

    
        @PropMeta(propId=6)
    
        public Boolean getIsRead(){
            return _isRead;
        }

        public void setIsRead(Boolean value){
            this._isRead = value;
        }


        private java.time.LocalDateTime _readTime;

    
        @PropMeta(propId=7)
    
        public java.time.LocalDateTime getReadTime(){
            return _readTime;
        }

        public void setReadTime(java.time.LocalDateTime value){
            this._readTime = value;
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


    }
