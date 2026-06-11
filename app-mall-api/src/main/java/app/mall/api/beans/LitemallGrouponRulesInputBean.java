//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallGrouponRulesInputBean extends CrudInputBase {

    
        private String _goodsId;

    
        @PropMeta(propId=2)
    
        public String getGoodsId(){
            return _goodsId;
        }

        public void setGoodsId(String value){
            this._goodsId = value;
        }


        private String _goodsName;

    
        @PropMeta(propId=3)
    
        public String getGoodsName(){
            return _goodsName;
        }

        public void setGoodsName(String value){
            this._goodsName = value;
        }


        private String _picUrl;

    
        @PropMeta(propId=4)
    
        public String getPicUrl(){
            return _picUrl;
        }

        public void setPicUrl(String value){
            this._picUrl = value;
        }


        private java.math.BigDecimal _discount;

    
        @PropMeta(propId=5)
    
        public java.math.BigDecimal getDiscount(){
            return _discount;
        }

        public void setDiscount(java.math.BigDecimal value){
            this._discount = value;
        }


        private Integer _discountMember;

    
        @PropMeta(propId=6)
    
        public Integer getDiscountMember(){
            return _discountMember;
        }

        public void setDiscountMember(Integer value){
            this._discountMember = value;
        }


        private java.time.LocalDateTime _expireTime;

    
        @PropMeta(propId=7)
    
        public java.time.LocalDateTime getExpireTime(){
            return _expireTime;
        }

        public void setExpireTime(java.time.LocalDateTime value){
            this._expireTime = value;
        }


        private Integer _status;

    
        @PropMeta(propId=8)
    
        public Integer getStatus(){
            return _status;
        }

        public void setStatus(Integer value){
            this._status = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=11)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
