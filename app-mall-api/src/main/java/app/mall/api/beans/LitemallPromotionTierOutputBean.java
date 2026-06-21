//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallPromotionTierOutputBean {

    
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


        private java.math.BigDecimal _meetAmount;

    
        @PropMeta(propId=3)
    
        public java.math.BigDecimal getMeetAmount(){
            return _meetAmount;
        }

        public void setMeetAmount(java.math.BigDecimal value){
            this._meetAmount = value;
        }


        private java.math.BigDecimal _discountValue;

    
        @PropMeta(propId=4)
    
        public java.math.BigDecimal getDiscountValue(){
            return _discountValue;
        }

        public void setDiscountValue(java.math.BigDecimal value){
            this._discountValue = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=5)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=6)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=7)
    
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


    }
