//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallCouponInputBean extends CrudInputBase {

    
        private String _name;

    
        @PropMeta(propId=2)
    
        public String getName(){
            return _name;
        }

        public void setName(String value){
            this._name = value;
        }


        private String _desc;

    
        @PropMeta(propId=3)
    
        public String getDesc(){
            return _desc;
        }

        public void setDesc(String value){
            this._desc = value;
        }


        private String _tag;

    
        @PropMeta(propId=4)
    
        public String getTag(){
            return _tag;
        }

        public void setTag(String value){
            this._tag = value;
        }


        private java.time.LocalDateTime _endTime;

    
        @PropMeta(propId=17)
    
        public java.time.LocalDateTime getEndTime(){
            return _endTime;
        }

        public void setEndTime(java.time.LocalDateTime value){
            this._endTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=20)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private Integer _total;

    
        @PropMeta(propId=5)
    
        public Integer getTotal(){
            return _total;
        }

        public void setTotal(Integer value){
            this._total = value;
        }


        private java.math.BigDecimal _discount;

    
        @PropMeta(propId=6)
    
        public java.math.BigDecimal getDiscount(){
            return _discount;
        }

        public void setDiscount(java.math.BigDecimal value){
            this._discount = value;
        }


        private java.math.BigDecimal _min;

    
        @PropMeta(propId=7)
    
        public java.math.BigDecimal getMin(){
            return _min;
        }

        public void setMin(java.math.BigDecimal value){
            this._min = value;
        }


        private Integer _limit;

    
        @PropMeta(propId=8)
    
        public Integer getLimit(){
            return _limit;
        }

        public void setLimit(Integer value){
            this._limit = value;
        }


        private Integer _type;

    
        @PropMeta(propId=9)
    
        public Integer getType(){
            return _type;
        }

        public void setType(Integer value){
            this._type = value;
        }


        private Integer _status;

    
        @PropMeta(propId=10)
    
        public Integer getStatus(){
            return _status;
        }

        public void setStatus(Integer value){
            this._status = value;
        }


        private Integer _goodsType;

    
        @PropMeta(propId=11)
    
        public Integer getGoodsType(){
            return _goodsType;
        }

        public void setGoodsType(Integer value){
            this._goodsType = value;
        }


        private String _goodsValue;

    
        @PropMeta(propId=12)
    
        public String getGoodsValue(){
            return _goodsValue;
        }

        public void setGoodsValue(String value){
            this._goodsValue = value;
        }


        private String _code;

    
        @PropMeta(propId=13)
    
        public String getCode(){
            return _code;
        }

        public void setCode(String value){
            this._code = value;
        }


        private Integer _timeType;

    
        @PropMeta(propId=14)
    
        public Integer getTimeType(){
            return _timeType;
        }

        public void setTimeType(Integer value){
            this._timeType = value;
        }


        private Integer _days;

    
        @PropMeta(propId=15)
    
        public Integer getDays(){
            return _days;
        }

        public void setDays(Integer value){
            this._days = value;
        }


        private java.time.LocalDateTime _startTime;

    
        @PropMeta(propId=16)
    
        public java.time.LocalDateTime getStartTime(){
            return _startTime;
        }

        public void setStartTime(java.time.LocalDateTime value){
            this._startTime = value;
        }


    }
