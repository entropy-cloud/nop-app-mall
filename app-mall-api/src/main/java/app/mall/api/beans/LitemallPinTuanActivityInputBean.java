//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallPinTuanActivityInputBean extends CrudInputBase {

    
        private String _goodsId;

    
        @PropMeta(propId=2)
    
        public String getGoodsId(){
            return _goodsId;
        }

        public void setGoodsId(String value){
            this._goodsId = value;
        }


        private String _productId;

    
        @PropMeta(propId=3)
    
        public String getProductId(){
            return _productId;
        }

        public void setProductId(String value){
            this._productId = value;
        }


        private java.math.BigDecimal _pinTuanPrice;

    
        @PropMeta(propId=4)
    
        public java.math.BigDecimal getPinTuanPrice(){
            return _pinTuanPrice;
        }

        public void setPinTuanPrice(java.math.BigDecimal value){
            this._pinTuanPrice = value;
        }


        private Integer _minUserCount;

    
        @PropMeta(propId=5)
    
        public Integer getMinUserCount(){
            return _minUserCount;
        }

        public void setMinUserCount(Integer value){
            this._minUserCount = value;
        }


        private Integer _maxUserCount;

    
        @PropMeta(propId=6)
    
        public Integer getMaxUserCount(){
            return _maxUserCount;
        }

        public void setMaxUserCount(Integer value){
            this._maxUserCount = value;
        }


        private Integer _expireHours;

    
        @PropMeta(propId=7)
    
        public Integer getExpireHours(){
            return _expireHours;
        }

        public void setExpireHours(Integer value){
            this._expireHours = value;
        }


        private Integer _status;

    
        @PropMeta(propId=8)
    
        public Integer getStatus(){
            return _status;
        }

        public void setStatus(Integer value){
            this._status = value;
        }


        private String _remark;

    
        @PropMeta(propId=9)
    
        public String getRemark(){
            return _remark;
        }

        public void setRemark(String value){
            this._remark = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=12)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
