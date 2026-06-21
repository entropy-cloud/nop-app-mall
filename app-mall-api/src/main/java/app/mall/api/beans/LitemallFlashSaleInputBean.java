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
    public class LitemallFlashSaleInputBean extends CrudInputBase {

    
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


        private java.math.BigDecimal _flashPrice;

    
        @PropMeta(propId=4)
    
        public java.math.BigDecimal getFlashPrice(){
            return _flashPrice;
        }

        public void setFlashPrice(java.math.BigDecimal value){
            this._flashPrice = value;
        }


        private Integer _totalStock;

    
        @PropMeta(propId=5)
    
        public Integer getTotalStock(){
            return _totalStock;
        }

        public void setTotalStock(Integer value){
            this._totalStock = value;
        }


        private Integer _maxPerUser;

    
        @PropMeta(propId=6)
    
        public Integer getMaxPerUser(){
            return _maxPerUser;
        }

        public void setMaxPerUser(Integer value){
            this._maxPerUser = value;
        }


        private Integer _maxPerOrder;

    
        @PropMeta(propId=7)
    
        public Integer getMaxPerOrder(){
            return _maxPerOrder;
        }

        public void setMaxPerOrder(Integer value){
            this._maxPerOrder = value;
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


        private List<LitemallFlashSaleSessionInputBean> _sessions;

        public List<LitemallFlashSaleSessionInputBean> getSessions(){
            return _sessions;
        }

        public void setSessions(List<LitemallFlashSaleSessionInputBean> value){
            this._sessions = value;
        }


    }
