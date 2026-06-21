//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallFlashSaleSessionOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _flashSaleId;

    
        @PropMeta(propId=2)
    
        public String getFlashSaleId(){
            return _flashSaleId;
        }

        public void setFlashSaleId(String value){
            this._flashSaleId = value;
        }


        private java.time.LocalDateTime _sessionStart;

    
        @PropMeta(propId=3)
    
        public java.time.LocalDateTime getSessionStart(){
            return _sessionStart;
        }

        public void setSessionStart(java.time.LocalDateTime value){
            this._sessionStart = value;
        }


        private java.time.LocalDateTime _sessionEnd;

    
        @PropMeta(propId=4)
    
        public java.time.LocalDateTime getSessionEnd(){
            return _sessionEnd;
        }

        public void setSessionEnd(java.time.LocalDateTime value){
            this._sessionEnd = value;
        }


        private Integer _sessionStock;

    
        @PropMeta(propId=5)
    
        public Integer getSessionStock(){
            return _sessionStock;
        }

        public void setSessionStock(Integer value){
            this._sessionStock = value;
        }


        private Integer _sessionStatus;

    
        @PropMeta(propId=6)
    
        public Integer getSessionStatus(){
            return _sessionStatus;
        }

        public void setSessionStatus(Integer value){
            this._sessionStatus = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=7)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=8)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=9)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private Map<String,Object> _flashSale;

        public Map<String,Object> getFlashSale(){
            return _flashSale;
        }

        public void setFlashSale(Map<String,Object> value){
            this._flashSale = value;
        }


    }
