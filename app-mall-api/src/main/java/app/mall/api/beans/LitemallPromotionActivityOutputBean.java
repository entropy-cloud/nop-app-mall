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
    public class LitemallPromotionActivityOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _name;

    
        @PropMeta(propId=2)
    
        public String getName(){
            return _name;
        }

        public void setName(String value){
            this._name = value;
        }


        private Integer _discountType;

    
        @PropMeta(propId=3)
    
        public Integer getDiscountType(){
            return _discountType;
        }

        public void setDiscountType(Integer value){
            this._discountType = value;
        }


        private String _discountType_label;

    
        public String getDiscountType_label(){
            return _discountType_label;
        }

        public void setDiscountType_label(String value){
            this._discountType_label = value;
        }


        private java.time.LocalDateTime _startTime;

    
        @PropMeta(propId=4)
    
        public java.time.LocalDateTime getStartTime(){
            return _startTime;
        }

        public void setStartTime(java.time.LocalDateTime value){
            this._startTime = value;
        }


        private java.time.LocalDateTime _endTime;

    
        @PropMeta(propId=5)
    
        public java.time.LocalDateTime getEndTime(){
            return _endTime;
        }

        public void setEndTime(java.time.LocalDateTime value){
            this._endTime = value;
        }


        private Integer _status;

    
        @PropMeta(propId=6)
    
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


        private Integer _goodsScope;

    
        @PropMeta(propId=7)
    
        public Integer getGoodsScope(){
            return _goodsScope;
        }

        public void setGoodsScope(Integer value){
            this._goodsScope = value;
        }


        private String _goodsScope_label;

    
        public String getGoodsScope_label(){
            return _goodsScope_label;
        }

        public void setGoodsScope_label(String value){
            this._goodsScope_label = value;
        }


        private String _goodsScopeValue;

    
        @PropMeta(propId=8)
    
        public String getGoodsScopeValue(){
            return _goodsScopeValue;
        }

        public void setGoodsScopeValue(String value){
            this._goodsScopeValue = value;
        }


        private Integer _priority;

    
        @PropMeta(propId=9)
    
        public Integer getPriority(){
            return _priority;
        }

        public void setPriority(Integer value){
            this._priority = value;
        }


        private Integer _maxPerUser;

    
        @PropMeta(propId=10)
    
        public Integer getMaxPerUser(){
            return _maxPerUser;
        }

        public void setMaxPerUser(Integer value){
            this._maxPerUser = value;
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


        private List<Map<String,Object>> _tiers;

        public List<Map<String,Object>> getTiers(){
            return _tiers;
        }

        public void setTiers(List<Map<String,Object>> value){
            this._tiers = value;
        }


    }
