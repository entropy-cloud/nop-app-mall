//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallCouponUserOutputBean {

    
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


        private String _couponId;

    
        @PropMeta(propId=3)
    
        public String getCouponId(){
            return _couponId;
        }

        public void setCouponId(String value){
            this._couponId = value;
        }


        private Integer _status;

    
        @PropMeta(propId=4)
    
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


        private java.time.LocalDateTime _usedTime;

    
        @PropMeta(propId=5)
    
        public java.time.LocalDateTime getUsedTime(){
            return _usedTime;
        }

        public void setUsedTime(java.time.LocalDateTime value){
            this._usedTime = value;
        }


        private java.time.LocalDateTime _startTime;

    
        @PropMeta(propId=6)
    
        public java.time.LocalDateTime getStartTime(){
            return _startTime;
        }

        public void setStartTime(java.time.LocalDateTime value){
            this._startTime = value;
        }


        private java.time.LocalDateTime _endTime;

    
        @PropMeta(propId=7)
    
        public java.time.LocalDateTime getEndTime(){
            return _endTime;
        }

        public void setEndTime(java.time.LocalDateTime value){
            this._endTime = value;
        }


        private String _orderId;

    
        @PropMeta(propId=8)
    
        public String getOrderId(){
            return _orderId;
        }

        public void setOrderId(String value){
            this._orderId = value;
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


        private Map<String,Object> _coupon;

        public Map<String,Object> getCoupon(){
            return _coupon;
        }

        public void setCoupon(Map<String,Object> value){
            this._coupon = value;
        }


        private Map<String,Object> _user;

        public Map<String,Object> getUser(){
            return _user;
        }

        public void setUser(Map<String,Object> value){
            this._user = value;
        }


    }
