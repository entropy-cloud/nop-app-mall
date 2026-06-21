//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallRechargeOutputBean {

    
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


        private String _walletId;

    
        @PropMeta(propId=3)
    
        public String getWalletId(){
            return _walletId;
        }

        public void setWalletId(String value){
            this._walletId = value;
        }


        private java.math.BigDecimal _amount;

    
        @PropMeta(propId=4)
    
        public java.math.BigDecimal getAmount(){
            return _amount;
        }

        public void setAmount(java.math.BigDecimal value){
            this._amount = value;
        }


        private java.math.BigDecimal _giftAmount;

    
        @PropMeta(propId=5)
    
        public java.math.BigDecimal getGiftAmount(){
            return _giftAmount;
        }

        public void setGiftAmount(java.math.BigDecimal value){
            this._giftAmount = value;
        }


        private Integer _payChannel;

    
        @PropMeta(propId=6)
    
        public Integer getPayChannel(){
            return _payChannel;
        }

        public void setPayChannel(Integer value){
            this._payChannel = value;
        }


        private String _payChannel_label;

    
        public String getPayChannel_label(){
            return _payChannel_label;
        }

        public void setPayChannel_label(String value){
            this._payChannel_label = value;
        }


        private Integer _payStatus;

    
        @PropMeta(propId=7)
    
        public Integer getPayStatus(){
            return _payStatus;
        }

        public void setPayStatus(Integer value){
            this._payStatus = value;
        }


        private String _payStatus_label;

    
        public String getPayStatus_label(){
            return _payStatus_label;
        }

        public void setPayStatus_label(String value){
            this._payStatus_label = value;
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


        private Map<String,Object> _wallet;

        public Map<String,Object> getWallet(){
            return _wallet;
        }

        public void setWallet(Map<String,Object> value){
            this._wallet = value;
        }


        private Map<String,Object> _user;

        public Map<String,Object> getUser(){
            return _user;
        }

        public void setUser(Map<String,Object> value){
            this._user = value;
        }


    }
