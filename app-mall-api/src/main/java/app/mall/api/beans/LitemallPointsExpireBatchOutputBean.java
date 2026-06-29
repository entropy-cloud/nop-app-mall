//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallPointsExpireBatchOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _accountId;

    
        @PropMeta(propId=2)
    
        public String getAccountId(){
            return _accountId;
        }

        public void setAccountId(String value){
            this._accountId = value;
        }


        private String _userId;

    
        @PropMeta(propId=3)
    
        public String getUserId(){
            return _userId;
        }

        public void setUserId(String value){
            this._userId = value;
        }


        private Integer _totalPoints;

    
        @PropMeta(propId=4)
    
        public Integer getTotalPoints(){
            return _totalPoints;
        }

        public void setTotalPoints(Integer value){
            this._totalPoints = value;
        }


        private Integer _remainingPoints;

    
        @PropMeta(propId=5)
    
        public Integer getRemainingPoints(){
            return _remainingPoints;
        }

        public void setRemainingPoints(Integer value){
            this._remainingPoints = value;
        }


        private java.time.LocalDateTime _expireTime;

    
        @PropMeta(propId=6)
    
        public java.time.LocalDateTime getExpireTime(){
            return _expireTime;
        }

        public void setExpireTime(java.time.LocalDateTime value){
            this._expireTime = value;
        }


        private String _sourceType;

    
        @PropMeta(propId=7)
    
        public String getSourceType(){
            return _sourceType;
        }

        public void setSourceType(String value){
            this._sourceType = value;
        }


        private String _sourceId;

    
        @PropMeta(propId=8)
    
        public String getSourceId(){
            return _sourceId;
        }

        public void setSourceId(String value){
            this._sourceId = value;
        }


        private Integer _version;

    
        @PropMeta(propId=9)
    
        public Integer getVersion(){
            return _version;
        }

        public void setVersion(Integer value){
            this._version = value;
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


        private Map<String,Object> _account;

        public Map<String,Object> getAccount(){
            return _account;
        }

        public void setAccount(Map<String,Object> value){
            this._account = value;
        }


    }
