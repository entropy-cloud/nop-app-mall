//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallLogOutputBean {

    
        private Integer _id;

    
        @PropMeta(propId=1)
    
        public Integer getId(){
            return _id;
        }

        public void setId(Integer value){
            this._id = value;
        }


        private String _admin;

    
        @PropMeta(propId=2)
    
        public String getAdmin(){
            return _admin;
        }

        public void setAdmin(String value){
            this._admin = value;
        }


        private String _ip;

    
        @PropMeta(propId=3)
    
        public String getIp(){
            return _ip;
        }

        public void setIp(String value){
            this._ip = value;
        }


        private Integer _type;

    
        @PropMeta(propId=4)
    
        public Integer getType(){
            return _type;
        }

        public void setType(Integer value){
            this._type = value;
        }


        private String _action;

    
        @PropMeta(propId=5)
    
        public String getAction(){
            return _action;
        }

        public void setAction(String value){
            this._action = value;
        }


        private Boolean _status;

    
        @PropMeta(propId=6)
    
        public Boolean getStatus(){
            return _status;
        }

        public void setStatus(Boolean value){
            this._status = value;
        }


        private String _result;

    
        @PropMeta(propId=7)
    
        public String getResult(){
            return _result;
        }

        public void setResult(String value){
            this._result = value;
        }


        private String _comment;

    
        @PropMeta(propId=8)
    
        public String getComment(){
            return _comment;
        }

        public void setComment(String value){
            this._comment = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=9)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=10)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=11)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
