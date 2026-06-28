//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallPointsExchangeOrderInputBean extends CrudInputBase {

    
        private String _userId;

    
        @PropMeta(propId=2)
    
        public String getUserId(){
            return _userId;
        }

        public void setUserId(String value){
            this._userId = value;
        }


        private String _pointsGoodsId;

    
        @PropMeta(propId=3)
    
        public String getPointsGoodsId(){
            return _pointsGoodsId;
        }

        public void setPointsGoodsId(String value){
            this._pointsGoodsId = value;
        }


        private String _goodsId;

    
        @PropMeta(propId=4)
    
        public String getGoodsId(){
            return _goodsId;
        }

        public void setGoodsId(String value){
            this._goodsId = value;
        }


        private String _productId;

    
        @PropMeta(propId=5)
    
        public String getProductId(){
            return _productId;
        }

        public void setProductId(String value){
            this._productId = value;
        }


        private String _goodsName;

    
        @PropMeta(propId=6)
    
        public String getGoodsName(){
            return _goodsName;
        }

        public void setGoodsName(String value){
            this._goodsName = value;
        }


        private String _picUrl;

    
        @PropMeta(propId=7)
    
        public String getPicUrl(){
            return _picUrl;
        }

        public void setPicUrl(String value){
            this._picUrl = value;
        }


        private Integer _pointsPrice;

    
        @PropMeta(propId=8)
    
        public Integer getPointsPrice(){
            return _pointsPrice;
        }

        public void setPointsPrice(Integer value){
            this._pointsPrice = value;
        }


        private Integer _quantity;

    
        @PropMeta(propId=9)
    
        public Integer getQuantity(){
            return _quantity;
        }

        public void setQuantity(Integer value){
            this._quantity = value;
        }


        private Integer _totalPoints;

    
        @PropMeta(propId=10)
    
        public Integer getTotalPoints(){
            return _totalPoints;
        }

        public void setTotalPoints(Integer value){
            this._totalPoints = value;
        }


        private String _addressId;

    
        @PropMeta(propId=11)
    
        public String getAddressId(){
            return _addressId;
        }

        public void setAddressId(String value){
            this._addressId = value;
        }


        private String _consignee;

    
        @PropMeta(propId=12)
    
        public String getConsignee(){
            return _consignee;
        }

        public void setConsignee(String value){
            this._consignee = value;
        }


        private String _phone;

    
        @PropMeta(propId=13)
    
        public String getPhone(){
            return _phone;
        }

        public void setPhone(String value){
            this._phone = value;
        }


        private String _fullAddress;

    
        @PropMeta(propId=14)
    
        public String getFullAddress(){
            return _fullAddress;
        }

        public void setFullAddress(String value){
            this._fullAddress = value;
        }


        private Integer _exchangeStatus;

    
        @PropMeta(propId=15)
    
        public Integer getExchangeStatus(){
            return _exchangeStatus;
        }

        public void setExchangeStatus(Integer value){
            this._exchangeStatus = value;
        }


        private String _shipCode;

    
        @PropMeta(propId=16)
    
        public String getShipCode(){
            return _shipCode;
        }

        public void setShipCode(String value){
            this._shipCode = value;
        }


        private String _remark;

    
        @PropMeta(propId=17)
    
        public String getRemark(){
            return _remark;
        }

        public void setRemark(String value){
            this._remark = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=20)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private Integer _payStatus;

    
        @PropMeta(propId=21)
    
        public Integer getPayStatus(){
            return _payStatus;
        }

        public void setPayStatus(Integer value){
            this._payStatus = value;
        }


        private Integer _payChannel;

    
        @PropMeta(propId=22)
    
        public Integer getPayChannel(){
            return _payChannel;
        }

        public void setPayChannel(Integer value){
            this._payChannel = value;
        }


        private java.math.BigDecimal _cashPrice;

    
        @PropMeta(propId=23)
    
        public java.math.BigDecimal getCashPrice(){
            return _cashPrice;
        }

        public void setCashPrice(java.math.BigDecimal value){
            this._cashPrice = value;
        }


        private java.math.BigDecimal _walletPayAmount;

    
        @PropMeta(propId=24)
    
        public java.math.BigDecimal getWalletPayAmount(){
            return _walletPayAmount;
        }

        public void setWalletPayAmount(java.math.BigDecimal value){
            this._walletPayAmount = value;
        }


    }
