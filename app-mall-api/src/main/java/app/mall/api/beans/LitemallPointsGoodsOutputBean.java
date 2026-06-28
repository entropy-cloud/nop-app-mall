//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallPointsGoodsOutputBean {

    
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


        private Integer _pointsPrice;

    
        @PropMeta(propId=4)
    
        public Integer getPointsPrice(){
            return _pointsPrice;
        }

        public void setPointsPrice(Integer value){
            this._pointsPrice = value;
        }


        private Integer _exchangeStock;

    
        @PropMeta(propId=5)
    
        public Integer getExchangeStock(){
            return _exchangeStock;
        }

        public void setExchangeStock(Integer value){
            this._exchangeStock = value;
        }


        private Integer _exchangedCount;

    
        @PropMeta(propId=6)
    
        public Integer getExchangedCount(){
            return _exchangedCount;
        }

        public void setExchangedCount(Integer value){
            this._exchangedCount = value;
        }


        private Integer _maxPerUser;

    
        @PropMeta(propId=7)
    
        public Integer getMaxPerUser(){
            return _maxPerUser;
        }

        public void setMaxPerUser(Integer value){
            this._maxPerUser = value;
        }


        private java.time.LocalDateTime _startTime;

    
        @PropMeta(propId=8)
    
        public java.time.LocalDateTime getStartTime(){
            return _startTime;
        }

        public void setStartTime(java.time.LocalDateTime value){
            this._startTime = value;
        }


        private java.time.LocalDateTime _endTime;

    
        @PropMeta(propId=9)
    
        public java.time.LocalDateTime getEndTime(){
            return _endTime;
        }

        public void setEndTime(java.time.LocalDateTime value){
            this._endTime = value;
        }


        private Integer _status;

    
        @PropMeta(propId=10)
    
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


        private String _remark;

    
        @PropMeta(propId=11)
    
        public String getRemark(){
            return _remark;
        }

        public void setRemark(String value){
            this._remark = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=12)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=13)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=14)
    
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


    }
