//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallAftersaleInputBean extends CrudInputBase {

    
        private String _aftersaleSn;

    
        @PropMeta(propId=2)
    
        public String getAftersaleSn(){
            return _aftersaleSn;
        }

        public void setAftersaleSn(String value){
            this._aftersaleSn = value;
        }


        private String _orderId;

    
        @PropMeta(propId=3)
    
        public String getOrderId(){
            return _orderId;
        }

        public void setOrderId(String value){
            this._orderId = value;
        }


        private String _userId;

    
        @PropMeta(propId=4)
    
        public String getUserId(){
            return _userId;
        }

        public void setUserId(String value){
            this._userId = value;
        }


        private Integer _type;

    
        @PropMeta(propId=5)
    
        public Integer getType(){
            return _type;
        }

        public void setType(Integer value){
            this._type = value;
        }


        private String _reason;

    
        @PropMeta(propId=6)
    
        public String getReason(){
            return _reason;
        }

        public void setReason(String value){
            this._reason = value;
        }


        private java.math.BigDecimal _amount;

    
        @PropMeta(propId=7)
    
        public java.math.BigDecimal getAmount(){
            return _amount;
        }

        public void setAmount(java.math.BigDecimal value){
            this._amount = value;
        }


        private java.util.List<java.lang.String> _pictures;

    
        @PropMeta(propId=8)
    
        public java.util.List<java.lang.String> getPictures(){
            return _pictures;
        }

        public void setPictures(java.util.List<java.lang.String> value){
            this._pictures = value;
        }


        private String _comment;

    
        @PropMeta(propId=9)
    
        public String getComment(){
            return _comment;
        }

        public void setComment(String value){
            this._comment = value;
        }


        private Integer _status;

    
        @PropMeta(propId=10)
    
        public Integer getStatus(){
            return _status;
        }

        public void setStatus(Integer value){
            this._status = value;
        }


        private java.time.LocalDateTime _handleTime;

    
        @PropMeta(propId=11)
    
        public java.time.LocalDateTime getHandleTime(){
            return _handleTime;
        }

        public void setHandleTime(java.time.LocalDateTime value){
            this._handleTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=14)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
