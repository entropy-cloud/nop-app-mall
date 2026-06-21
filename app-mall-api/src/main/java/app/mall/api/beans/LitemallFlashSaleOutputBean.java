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
    public class LitemallFlashSaleOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


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


        private String _status_label;

    
        public String getStatus_label(){
            return _status_label;
        }

        public void setStatus_label(String value){
            this._status_label = value;
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


        private Map<String,Object> _goods;

        public Map<String,Object> getGoods(){
            return _goods;
        }

        public void setGoods(Map<String,Object> value){
            this._goods = value;
        }


        private List<Map<String,Object>> _sessions;

        public List<Map<String,Object>> getSessions(){
            return _sessions;
        }

        public void setSessions(List<Map<String,Object>> value){
            this._sessions = value;
        }


    }
