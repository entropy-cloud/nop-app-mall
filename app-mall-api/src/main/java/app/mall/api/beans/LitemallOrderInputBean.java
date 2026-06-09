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
    public class LitemallOrderInputBean extends CrudInputBase {

    
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


        private Short _orderStatus;

    
        @PropMeta(propId=4)
    
        public Short getOrderStatus(){
            return _orderStatus;
        }

        public void setOrderStatus(Short value){
            this._orderStatus = value;
        }


        private Short _aftersaleStatus;

    
        @PropMeta(propId=5)
    
        public Short getAftersaleStatus(){
            return _aftersaleStatus;
        }

        public void setAftersaleStatus(Short value){
            this._aftersaleStatus = value;
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


        private Short _comments;

    
        @PropMeta(propId=27)
    
        public Short getComments(){
            return _comments;
        }

        public void setComments(Short value){
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


        private Boolean _deleted;

    
        @PropMeta(propId=31)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private java.util.List<java.lang.String> _relatedProductList_ids;

    
        public java.util.List<java.lang.String> getRelatedProductList_ids(){
            return _relatedProductList_ids;
        }

        public void setRelatedProductList_ids(java.util.List<java.lang.String> value){
            this._relatedProductList_ids = value;
        }


        private List<LitemallOrderGoodsInputBean> _orderGoods;

        public List<LitemallOrderGoodsInputBean> getOrderGoods(){
            return _orderGoods;
        }

        public void setOrderGoods(List<LitemallOrderGoodsInputBean> value){
            this._orderGoods = value;
        }


        private List<LitemallGoodsProductInputBean> _relatedProductList;

        public List<LitemallGoodsProductInputBean> getRelatedProductList(){
            return _relatedProductList;
        }

        public void setRelatedProductList(List<LitemallGoodsProductInputBean> value){
            this._relatedProductList = value;
        }


    }
