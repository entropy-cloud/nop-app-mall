//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallWalletOutputBean {

    
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


        private java.math.BigDecimal _balance;

    
        @PropMeta(propId=3)
    
        public java.math.BigDecimal getBalance(){
            return _balance;
        }

        public void setBalance(java.math.BigDecimal value){
            this._balance = value;
        }


        private java.math.BigDecimal _totalRecharge;

    
        @PropMeta(propId=4)
    
        public java.math.BigDecimal getTotalRecharge(){
            return _totalRecharge;
        }

        public void setTotalRecharge(java.math.BigDecimal value){
            this._totalRecharge = value;
        }


        private java.math.BigDecimal _totalSpent;

    
        @PropMeta(propId=5)
    
        public java.math.BigDecimal getTotalSpent(){
            return _totalSpent;
        }

        public void setTotalSpent(java.math.BigDecimal value){
            this._totalSpent = value;
        }


        private Integer _version;

    
        @PropMeta(propId=6)
    
        public Integer getVersion(){
            return _version;
        }

        public void setVersion(Integer value){
            this._version = value;
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


        private Map<String,Object> _user;

        public Map<String,Object> getUser(){
            return _user;
        }

        public void setUser(Map<String,Object> value){
            this._user = value;
        }


    }
