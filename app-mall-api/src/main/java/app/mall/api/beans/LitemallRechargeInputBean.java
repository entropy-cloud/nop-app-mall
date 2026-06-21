//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallRechargeInputBean extends CrudInputBase {

    
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


        private Integer _payStatus;

    
        @PropMeta(propId=7)
    
        public Integer getPayStatus(){
            return _payStatus;
        }

        public void setPayStatus(Integer value){
            this._payStatus = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=10)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
