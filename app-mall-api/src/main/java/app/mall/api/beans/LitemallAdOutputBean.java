//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallAdOutputBean {

    
        private Integer _id;

    
        @PropMeta(propId=1)
    
        public Integer getId(){
            return _id;
        }

        public void setId(Integer value){
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


        private String _link;

    
        @PropMeta(propId=3)
    
        public String getLink(){
            return _link;
        }

        public void setLink(String value){
            this._link = value;
        }


        private String _url;

    
        @PropMeta(propId=4)
    
        public String getUrl(){
            return _url;
        }

        public void setUrl(String value){
            this._url = value;
        }


        private Byte _position;

    
        @PropMeta(propId=5)
    
        public Byte getPosition(){
            return _position;
        }

        public void setPosition(Byte value){
            this._position = value;
        }


        private String _content;

    
        @PropMeta(propId=6)
    
        public String getContent(){
            return _content;
        }

        public void setContent(String value){
            this._content = value;
        }


        private java.time.LocalDateTime _startTime;

    
        @PropMeta(propId=7)
    
        public java.time.LocalDateTime getStartTime(){
            return _startTime;
        }

        public void setStartTime(java.time.LocalDateTime value){
            this._startTime = value;
        }


        private java.time.LocalDateTime _endTime;

    
        @PropMeta(propId=8)
    
        public java.time.LocalDateTime getEndTime(){
            return _endTime;
        }

        public void setEndTime(java.time.LocalDateTime value){
            this._endTime = value;
        }


        private Boolean _enabled;

    
        @PropMeta(propId=9)
    
        public Boolean getEnabled(){
            return _enabled;
        }

        public void setEnabled(Boolean value){
            this._enabled = value;
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


        private io.nop.api.core.beans.file.FileStatusBean _urlComponentFileStatus;

    
        public io.nop.api.core.beans.file.FileStatusBean getUrlComponentFileStatus(){
            return _urlComponentFileStatus;
        }

        public void setUrlComponentFileStatus(io.nop.api.core.beans.file.FileStatusBean value){
            this._urlComponentFileStatus = value;
        }


    }
