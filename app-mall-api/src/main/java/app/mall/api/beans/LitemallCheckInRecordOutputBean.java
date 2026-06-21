//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallCheckInRecordOutputBean {

    
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


        private java.time.LocalDate _checkInDate;

    
        @PropMeta(propId=3)
    
        public java.time.LocalDate getCheckInDate(){
            return _checkInDate;
        }

        public void setCheckInDate(java.time.LocalDate value){
            this._checkInDate = value;
        }


        private Integer _consecutiveDays;

    
        @PropMeta(propId=4)
    
        public Integer getConsecutiveDays(){
            return _consecutiveDays;
        }

        public void setConsecutiveDays(Integer value){
            this._consecutiveDays = value;
        }


        private Integer _pointsEarned;

    
        @PropMeta(propId=5)
    
        public Integer getPointsEarned(){
            return _pointsEarned;
        }

        public void setPointsEarned(Integer value){
            this._pointsEarned = value;
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


        private Map<String,Object> _user;

        public Map<String,Object> getUser(){
            return _user;
        }

        public void setUser(Map<String,Object> value){
            this._user = value;
        }


    }
