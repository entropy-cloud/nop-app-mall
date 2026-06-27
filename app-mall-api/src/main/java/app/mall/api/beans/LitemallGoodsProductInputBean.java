//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallGoodsProductInputBean extends CrudInputBase {

    
        private String _goodsId;

    
        @PropMeta(propId=2)
    
        public String getGoodsId(){
            return _goodsId;
        }

        public void setGoodsId(String value){
            this._goodsId = value;
        }


        private String _specifications;

    
        @PropMeta(propId=3)
    
        public String getSpecifications(){
            return _specifications;
        }

        public void setSpecifications(String value){
            this._specifications = value;
        }


        private java.math.BigDecimal _price;

    
        @PropMeta(propId=4)
    
        public java.math.BigDecimal getPrice(){
            return _price;
        }

        public void setPrice(java.math.BigDecimal value){
            this._price = value;
        }


        private Integer _number;

    
        @PropMeta(propId=5)
    
        public Integer getNumber(){
            return _number;
        }

        public void setNumber(Integer value){
            this._number = value;
        }


        private String _url;

    
        @PropMeta(propId=6)
    
        public String getUrl(){
            return _url;
        }

        public void setUrl(String value){
            this._url = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=9)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private Integer _safeStock;

    
        @PropMeta(propId=10)
    
        public Integer getSafeStock(){
            return _safeStock;
        }

        public void setSafeStock(Integer value){
            this._safeStock = value;
        }


        private java.math.BigDecimal _vipPrice;

    
        @PropMeta(propId=11)
    
        public java.math.BigDecimal getVipPrice(){
            return _vipPrice;
        }

        public void setVipPrice(java.math.BigDecimal value){
            this._vipPrice = value;
        }


    }
