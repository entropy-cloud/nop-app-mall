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
    public class LitemallPinTuanGroupOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _activityId;

    
        @PropMeta(propId=2)
    
        public String getActivityId(){
            return _activityId;
        }

        public void setActivityId(String value){
            this._activityId = value;
        }


        private String _creatorUserId;

    
        @PropMeta(propId=3)
    
        public String getCreatorUserId(){
            return _creatorUserId;
        }

        public void setCreatorUserId(String value){
            this._creatorUserId = value;
        }


        private String _orderId;

    
        @PropMeta(propId=4)
    
        public String getOrderId(){
            return _orderId;
        }

        public void setOrderId(String value){
            this._orderId = value;
        }


        private Integer _status;

    
        @PropMeta(propId=5)
    
        public Integer getStatus(){
            return _status;
        }

        public void setStatus(Integer value){
            this._status = value;
        }


        private String _status_label;

    
        public String getStatus_label(){
            return _status_label;
        }

        public void setStatus_label(String value){
            this._status_label = value;
        }


        private java.time.LocalDateTime _expireTime;

    
        @PropMeta(propId=6)
    
        public java.time.LocalDateTime getExpireTime(){
            return _expireTime;
        }

        public void setExpireTime(java.time.LocalDateTime value){
            this._expireTime = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=7)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=8)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=9)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private Map<String,Object> _activity;

        public Map<String,Object> getActivity(){
            return _activity;
        }

        public void setActivity(Map<String,Object> value){
            this._activity = value;
        }


        private Map<String,Object> _creator;

        public Map<String,Object> getCreator(){
            return _creator;
        }

        public void setCreator(Map<String,Object> value){
            this._creator = value;
        }


        private List<Map<String,Object>> _members;

        public List<Map<String,Object>> getMembers(){
            return _members;
        }

        public void setMembers(List<Map<String,Object>> value){
            this._members = value;
        }


    }
