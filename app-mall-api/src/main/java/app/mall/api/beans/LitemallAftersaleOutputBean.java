//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallAftersaleOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


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


        private String _type_label;

    
        public String getType_label(){
            return _type_label;
        }

        public void setType_label(String value){
            this._type_label = value;
        }


        private String _reason;

    
        @PropMeta(propId=6)
    
        public String getReason(){
            return _reason;
        }

        public void setReason(String value){
            this._reason = value;
        }


        private String _reason_label;

    
        public String getReason_label(){
            return _reason_label;
        }

        public void setReason_label(String value){
            this._reason_label = value;
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


        private String _status_label;

    
        public String getStatus_label(){
            return _status_label;
        }

        public void setStatus_label(String value){
            this._status_label = value;
        }


        private java.time.LocalDateTime _handleTime;

    
        @PropMeta(propId=11)
    
        public java.time.LocalDateTime getHandleTime(){
            return _handleTime;
        }

        public void setHandleTime(java.time.LocalDateTime value){
            this._handleTime = value;
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


        private String _orderItemId;

    
        @PropMeta(propId=15)
    
        public String getOrderItemId(){
            return _orderItemId;
        }

        public void setOrderItemId(String value){
            this._orderItemId = value;
        }


        private String _processNote;

    
        @PropMeta(propId=16)
    
        public String getProcessNote(){
            return _processNote;
        }

        public void setProcessNote(String value){
            this._processNote = value;
        }


        private java.time.LocalDateTime _processTime;

    
        @PropMeta(propId=17)
    
        public java.time.LocalDateTime getProcessTime(){
            return _processTime;
        }

        public void setProcessTime(java.time.LocalDateTime value){
            this._processTime = value;
        }


        private java.util.List<io.nop.api.core.beans.file.FileStatusBean> _picturesComponentFileStatusList;

    
        public java.util.List<io.nop.api.core.beans.file.FileStatusBean> getPicturesComponentFileStatusList(){
            return _picturesComponentFileStatusList;
        }

        public void setPicturesComponentFileStatusList(java.util.List<io.nop.api.core.beans.file.FileStatusBean> value){
            this._picturesComponentFileStatusList = value;
        }


        private Map<String,Object> _order;

        public Map<String,Object> getOrder(){
            return _order;
        }

        public void setOrder(Map<String,Object> value){
            this._order = value;
        }


    }
