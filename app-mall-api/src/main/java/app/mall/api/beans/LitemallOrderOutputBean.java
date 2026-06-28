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
    public class LitemallOrderOutputBean {

    
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


        private String _orderSn;

    
        @PropMeta(propId=3)
    
        public String getOrderSn(){
            return _orderSn;
        }

        public void setOrderSn(String value){
            this._orderSn = value;
        }


        private Integer _orderStatus;

    
        @PropMeta(propId=4)
    
        public Integer getOrderStatus(){
            return _orderStatus;
        }

        public void setOrderStatus(Integer value){
            this._orderStatus = value;
        }


        private String _orderStatus_label;

    
        public String getOrderStatus_label(){
            return _orderStatus_label;
        }

        public void setOrderStatus_label(String value){
            this._orderStatus_label = value;
        }


        private Integer _aftersaleStatus;

    
        @PropMeta(propId=5)
    
        public Integer getAftersaleStatus(){
            return _aftersaleStatus;
        }

        public void setAftersaleStatus(Integer value){
            this._aftersaleStatus = value;
        }


        private String _aftersaleStatus_label;

    
        public String getAftersaleStatus_label(){
            return _aftersaleStatus_label;
        }

        public void setAftersaleStatus_label(String value){
            this._aftersaleStatus_label = value;
        }


        private String _consignee;

    
        @PropMeta(propId=6)
    
        public String getConsignee(){
            return _consignee;
        }

        public void setConsignee(String value){
            this._consignee = value;
        }


        private String _mobile;

    
        @PropMeta(propId=7)
    
        public String getMobile(){
            return _mobile;
        }

        public void setMobile(String value){
            this._mobile = value;
        }


        private String _address;

    
        @PropMeta(propId=8)
    
        public String getAddress(){
            return _address;
        }

        public void setAddress(String value){
            this._address = value;
        }


        private String _message;

    
        @PropMeta(propId=9)
    
        public String getMessage(){
            return _message;
        }

        public void setMessage(String value){
            this._message = value;
        }


        private java.math.BigDecimal _goodsPrice;

    
        @PropMeta(propId=10)
    
        public java.math.BigDecimal getGoodsPrice(){
            return _goodsPrice;
        }

        public void setGoodsPrice(java.math.BigDecimal value){
            this._goodsPrice = value;
        }


        private java.math.BigDecimal _freightPrice;

    
        @PropMeta(propId=11)
    
        public java.math.BigDecimal getFreightPrice(){
            return _freightPrice;
        }

        public void setFreightPrice(java.math.BigDecimal value){
            this._freightPrice = value;
        }


        private java.math.BigDecimal _couponPrice;

    
        @PropMeta(propId=12)
    
        public java.math.BigDecimal getCouponPrice(){
            return _couponPrice;
        }

        public void setCouponPrice(java.math.BigDecimal value){
            this._couponPrice = value;
        }


        private java.math.BigDecimal _integralPrice;

    
        @PropMeta(propId=13)
    
        public java.math.BigDecimal getIntegralPrice(){
            return _integralPrice;
        }

        public void setIntegralPrice(java.math.BigDecimal value){
            this._integralPrice = value;
        }


        private java.math.BigDecimal _grouponPrice;

    
        @PropMeta(propId=14)
    
        public java.math.BigDecimal getGrouponPrice(){
            return _grouponPrice;
        }

        public void setGrouponPrice(java.math.BigDecimal value){
            this._grouponPrice = value;
        }


        private java.math.BigDecimal _orderPrice;

    
        @PropMeta(propId=15)
    
        public java.math.BigDecimal getOrderPrice(){
            return _orderPrice;
        }

        public void setOrderPrice(java.math.BigDecimal value){
            this._orderPrice = value;
        }


        private java.math.BigDecimal _actualPrice;

    
        @PropMeta(propId=16)
    
        public java.math.BigDecimal getActualPrice(){
            return _actualPrice;
        }

        public void setActualPrice(java.math.BigDecimal value){
            this._actualPrice = value;
        }


        private String _payId;

    
        @PropMeta(propId=17)
    
        public String getPayId(){
            return _payId;
        }

        public void setPayId(String value){
            this._payId = value;
        }


        private java.time.LocalDateTime _payTime;

    
        @PropMeta(propId=18)
    
        public java.time.LocalDateTime getPayTime(){
            return _payTime;
        }

        public void setPayTime(java.time.LocalDateTime value){
            this._payTime = value;
        }


        private String _shipSn;

    
        @PropMeta(propId=19)
    
        public String getShipSn(){
            return _shipSn;
        }

        public void setShipSn(String value){
            this._shipSn = value;
        }


        private String _shipChannel;

    
        @PropMeta(propId=20)
    
        public String getShipChannel(){
            return _shipChannel;
        }

        public void setShipChannel(String value){
            this._shipChannel = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=29)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=30)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=31)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private Integer _deliveryType;

    
        @PropMeta(propId=32)
    
        public Integer getDeliveryType(){
            return _deliveryType;
        }

        public void setDeliveryType(Integer value){
            this._deliveryType = value;
        }


        private String _deliveryType_label;

    
        public String getDeliveryType_label(){
            return _deliveryType_label;
        }

        public void setDeliveryType_label(String value){
            this._deliveryType_label = value;
        }


        private String _pickupStoreId;

    
        @PropMeta(propId=33)
    
        public String getPickupStoreId(){
            return _pickupStoreId;
        }

        public void setPickupStoreId(String value){
            this._pickupStoreId = value;
        }


        private String _pickupCode;

    
        @PropMeta(propId=34)
    
        public String getPickupCode(){
            return _pickupCode;
        }

        public void setPickupCode(String value){
            this._pickupCode = value;
        }


        private java.time.LocalDateTime _pickupTime;

    
        @PropMeta(propId=35)
    
        public java.time.LocalDateTime getPickupTime(){
            return _pickupTime;
        }

        public void setPickupTime(java.time.LocalDateTime value){
            this._pickupTime = value;
        }


        private java.math.BigDecimal _promotionPrice;

    
        @PropMeta(propId=36)
    
        public java.math.BigDecimal getPromotionPrice(){
            return _promotionPrice;
        }

        public void setPromotionPrice(java.math.BigDecimal value){
            this._promotionPrice = value;
        }


        private java.math.BigDecimal _pinTuanPrice;

    
        @PropMeta(propId=37)
    
        public java.math.BigDecimal getPinTuanPrice(){
            return _pinTuanPrice;
        }

        public void setPinTuanPrice(java.math.BigDecimal value){
            this._pinTuanPrice = value;
        }


        private Integer _payChannel;

    
        @PropMeta(propId=38)
    
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


        private java.math.BigDecimal _walletPayAmount;

    
        @PropMeta(propId=39)
    
        public java.math.BigDecimal getWalletPayAmount(){
            return _walletPayAmount;
        }

        public void setWalletPayAmount(java.math.BigDecimal value){
            this._walletPayAmount = value;
        }


        private String _adminRemark;

    
        @PropMeta(propId=40)
    
        public String getAdminRemark(){
            return _adminRemark;
        }

        public void setAdminRemark(String value){
            this._adminRemark = value;
        }


        private String _flashSaleSessionId;

    
        @PropMeta(propId=41)
    
        public String getFlashSaleSessionId(){
            return _flashSaleSessionId;
        }

        public void setFlashSaleSessionId(String value){
            this._flashSaleSessionId = value;
        }


        private java.util.List<java.lang.String> _relatedProductList_ids;

    
        public java.util.List<java.lang.String> getRelatedProductList_ids(){
            return _relatedProductList_ids;
        }

        public void setRelatedProductList_ids(java.util.List<java.lang.String> value){
            this._relatedProductList_ids = value;
        }


        private String _relatedProductList_label;

    
        public String getRelatedProductList_label(){
            return _relatedProductList_label;
        }

        public void setRelatedProductList_label(String value){
            this._relatedProductList_label = value;
        }


        private java.time.LocalDateTime _shipTime;

    
        @PropMeta(propId=21)
    
        public java.time.LocalDateTime getShipTime(){
            return _shipTime;
        }

        public void setShipTime(java.time.LocalDateTime value){
            this._shipTime = value;
        }


        private java.math.BigDecimal _refundAmount;

    
        @PropMeta(propId=22)
    
        public java.math.BigDecimal getRefundAmount(){
            return _refundAmount;
        }

        public void setRefundAmount(java.math.BigDecimal value){
            this._refundAmount = value;
        }


        private String _refundType;

    
        @PropMeta(propId=23)
    
        public String getRefundType(){
            return _refundType;
        }

        public void setRefundType(String value){
            this._refundType = value;
        }


        private String _refundContent;

    
        @PropMeta(propId=24)
    
        public String getRefundContent(){
            return _refundContent;
        }

        public void setRefundContent(String value){
            this._refundContent = value;
        }


        private java.time.LocalDateTime _refundTime;

    
        @PropMeta(propId=25)
    
        public java.time.LocalDateTime getRefundTime(){
            return _refundTime;
        }

        public void setRefundTime(java.time.LocalDateTime value){
            this._refundTime = value;
        }


        private java.time.LocalDateTime _confirmTime;

    
        @PropMeta(propId=26)
    
        public java.time.LocalDateTime getConfirmTime(){
            return _confirmTime;
        }

        public void setConfirmTime(java.time.LocalDateTime value){
            this._confirmTime = value;
        }


        private Integer _comments;

    
        @PropMeta(propId=27)
    
        public Integer getComments(){
            return _comments;
        }

        public void setComments(Integer value){
            this._comments = value;
        }


        private java.time.LocalDateTime _endTime;

    
        @PropMeta(propId=28)
    
        public java.time.LocalDateTime getEndTime(){
            return _endTime;
        }

        public void setEndTime(java.time.LocalDateTime value){
            this._endTime = value;
        }


        private List<Map<String,Object>> _orderGoods;

        public List<Map<String,Object>> getOrderGoods(){
            return _orderGoods;
        }

        public void setOrderGoods(List<Map<String,Object>> value){
            this._orderGoods = value;
        }


        private Map<String,Object> _pickupStore;

        public Map<String,Object> getPickupStore(){
            return _pickupStore;
        }

        public void setPickupStore(Map<String,Object> value){
            this._pickupStore = value;
        }


        private List<Map<String,Object>> _relatedProductList;

        public List<Map<String,Object>> getRelatedProductList(){
            return _relatedProductList;
        }

        public void setRelatedProductList(List<Map<String,Object>> value){
            this._relatedProductList = value;
        }


    }
