//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallGrouponInputBean extends CrudInputBase {

    
        private Integer _orderId;

    
        @PropMeta(propId=2)
    
        public Integer getOrderId(){
            return _orderId;
        }

        public void setOrderId(Integer value){
            this._orderId = value;
        }


        private Integer _grouponId;

    
        @PropMeta(propId=3)
    
        public Integer getGrouponId(){
            return _grouponId;
        }

        public void setGrouponId(Integer value){
            this._grouponId = value;
        }


        private Integer _rulesId;

    
        @PropMeta(propId=4)
    
        public Integer getRulesId(){
            return _rulesId;
        }

        public void setRulesId(Integer value){
            this._rulesId = value;
        }


        private Integer _userId;

    
        @PropMeta(propId=5)
    
        public Integer getUserId(){
            return _userId;
        }

        public void setUserId(Integer value){
            this._userId = value;
        }


        private String _shareUrl;

    
        @PropMeta(propId=6)
    
        public String getShareUrl(){
            return _shareUrl;
        }

        public void setShareUrl(String value){
            this._shareUrl = value;
        }


        private Integer _creatorUserId;

    
        @PropMeta(propId=7)
    
        public Integer getCreatorUserId(){
            return _creatorUserId;
        }

        public void setCreatorUserId(Integer value){
            this._creatorUserId = value;
        }


        private java.time.LocalDateTime _creatorUserTime;

    
        @PropMeta(propId=8)
    
        public java.time.LocalDateTime getCreatorUserTime(){
            return _creatorUserTime;
        }

        public void setCreatorUserTime(java.time.LocalDateTime value){
            this._creatorUserTime = value;
        }


        private Short _status;

    
        @PropMeta(propId=9)
    
        public Short getStatus(){
            return _status;
        }

        public void setStatus(Short value){
            this._status = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=12)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
