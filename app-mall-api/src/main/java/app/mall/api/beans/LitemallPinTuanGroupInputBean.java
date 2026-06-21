//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    import java.util.List;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallPinTuanGroupInputBean extends CrudInputBase {

    
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


        private java.time.LocalDateTime _expireTime;

    
        @PropMeta(propId=6)
    
        public java.time.LocalDateTime getExpireTime(){
            return _expireTime;
        }

        public void setExpireTime(java.time.LocalDateTime value){
            this._expireTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=9)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private List<LitemallPinTuanMemberInputBean> _members;

        public List<LitemallPinTuanMemberInputBean> getMembers(){
            return _members;
        }

        public void setMembers(List<LitemallPinTuanMemberInputBean> value){
            this._members = value;
        }


    }
